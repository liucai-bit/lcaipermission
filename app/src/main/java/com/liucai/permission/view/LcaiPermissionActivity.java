package com.liucai.permission.view;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.liucai.core.LcaiManager;
import com.liucai.core.apputils.GlobalModelString;
import com.liucai.core.base.LcaiBasePermissionActivity;
import com.liucai.permission.bean.LcaiPermissionEntity;
import com.liucai.permission.core.LcaiPermissionString;
import com.liucai.preference.LcaiPreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/10/14
 */
public class LcaiPermissionActivity extends LcaiBasePermissionActivity {

    private String[] permissionArray;
    private boolean hasNotification;
    private boolean toSystem;

    /** 结果 map：value 统一语义 = 是否通过。 */
    private final Map<String, Boolean> permissions = new HashMap<>();
    private final List<LcaiPermissionEntity> permissionEntities = new ArrayList<>();

    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> toSystemLauncher;

    private boolean finished = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            permissionArray = extras.getStringArray(LcaiPermissionString.PERMISSION_KEY);
            hasNotification  = extras.getBoolean(LcaiPermissionString.HAS_NOTIFICATION, false);
            toSystem         = extras.getBoolean(LcaiPermissionString.TO_SYSTEM, false);
        }

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::onNormalPermissionsResult);

        toSystemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    onBackFromSystemSettings();
                });

        if (toSystem) {
            openAppSettings();
            return;
        }

        if (hasNotification) {
            recordNotificationPermission();
        }

        String[] toRequest = filterOutNotification(permissionArray);
        if (toRequest.length > 0) {
            permissionLauncher.launch(toRequest);
        } else {
            finishAll();
        }
    }


    private void onNormalPermissionsResult(Map<String, Boolean> result) {
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            String p = entry.getKey();
            boolean granted = Boolean.TRUE.equals(entry.getValue());

            LcaiPermissionEntity entity = new LcaiPermissionEntity();
            entity.setPermission(p);
            entity.setGranted(granted);

            boolean permanent = false;
            if (!granted) {
                permanent = isPermanentDenied(p);
                entity.setPermanentlyDenied(permanent);
            }
            permissionEntities.add(entity);

            permissions.put(p, granted);
        }
        finishAll();
    }

    private void recordNotificationPermission() {
        LcaiPermissionEntity entity = new LcaiPermissionEntity();
        entity.setPermission(LcaiPermissionString.NOTIFICATIONS);

        boolean granted = checkNotificationGranted();
        entity.setGranted(granted);
        permissionEntities.add(entity);
        permissions.put(LcaiPermissionString.NOTIFICATIONS, granted);
    }

    private boolean checkNotificationGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            return manager != null && manager.areNotificationsEnabled();
        }
        return true;
    }

    private void onBackFromSystemSettings() {
        if (permissionArray != null) {
            for (String p : permissionArray) {
                boolean granted = ContextCompat.checkSelfPermission(this, p)
                        == PackageManager.PERMISSION_GRANTED;
                permissions.put(p, granted);
                LcaiPermissionEntity e = new LcaiPermissionEntity();
                e.setPermission(p);
                e.setGranted(granted);
                e.setPermanentlyDenied(!granted && isPermanentDenied(p));
                permissionEntities.add(e);
            }
        }
        if (hasNotification) {
            recordNotificationPermission();
        }
        finishAll();
    }

    private void openAppSettings() {
        Uri packageURI = Uri.parse("package:" + getPackageName());
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        toSystemLauncher.launch(intent);
    }

    private void finishAll() {
        if (finished) return;
        finished = true;

        List<LcaiPermissionEntity> deduped = dedupEntities(permissionEntities);
        permissionEntities.clear();
        permissionEntities.addAll(deduped);

        LcaiPreferenceUtils.put(
                GlobalModelString.PERMISSION_APPLIED,
                JSON.toJSONString(permissionEntities));
        boolean granted = true;
        for (Map.Entry<String, Boolean> e : permissions.entrySet()) {
            if (!Boolean.TRUE.equals(e.getValue())) {
                granted = false;
                break;
            }
        }
        LcaiManager.Internal.getPermissionResult()
                .onPermissionResult(granted, permissions);
        finish();
    }

    private List<LcaiPermissionEntity> dedupEntities(List<LcaiPermissionEntity> src) {
        Map<String, LcaiPermissionEntity> map = new HashMap<>();
        for (LcaiPermissionEntity e : src) {
            if (e.getPermission() != null) {
                map.put(e.getPermission(), e);
            }
        }
        return new ArrayList<>(map.values());
    }

    private boolean isPermanentDenied(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
                && !ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
    }

    private String[] filterOutNotification(String[] arr) {
        if (arr == null) return new String[0];
        List<String> list = new ArrayList<>();
        for (String p : arr) {
            if (LcaiPermissionString.NOTIFICATIONS.equals(p)) continue;
            list.add(p);
        }
        return list.toArray(new String[0]);
    }
}