package com.liucai.permission.view;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.liucai.core.util.log.LcaiLogUtils;
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
    private ActivityResultLauncher<Intent> manageStorageLauncher;

    /** 从设置页返回后需要继续申请的普通运行时权限 */
    private String[] pendingNormalPermissions = new String[0];

    private boolean finished = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            permissionArray = extras.getStringArray(LcaiPermissionString.PERMISSION_KEY);
            hasNotification = extras.getBoolean(LcaiPermissionString.HAS_NOTIFICATION, false);
            toSystem = extras.getBoolean(LcaiPermissionString.TO_SYSTEM, false);
        }

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::onNormalPermissionsResult);

        toSystemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> onBackFromSystemSettings());

        manageStorageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> onBackFromManageStorageSettings());

        if (toSystem) {
            LcaiLogUtils.i("权限申请-跳转设置页面");
            openAppSettings();
            return;
        }

        if (hasNotification) {
            LcaiLogUtils.i("权限申请-申请通知权限");
            recordNotificationPermission();
        }

        // 分离 MANAGE_EXTERNAL_STORAGE 和普通运行时权限
        boolean hasManageStorage = false;
        List<String> normalList = new ArrayList<>();
        if (permissionArray != null) {
            for (String p : permissionArray) {
                if (LcaiPermissionString.NOTIFICATIONS.equals(p)) {
                    // 低版本通知标识，不是真实运行时权限
                    continue;
                }
                if (LcaiPermissionString.MANAGE_EXTERNAL_STORAGE.equals(p)) {
                    hasManageStorage = true;
                } else {
                    normalList.add(p);
                }
            }
        }
        pendingNormalPermissions = normalList.toArray(new String[0]);

        if (hasManageStorage) {
            // 特殊权限，需要跳转「所有文件访问权限」设置页
            openManageExternalStorageSettings();
        } else {
            launchNormalPermissions();
        }
    }

    /**
     * 跳转「所有文件访问权限」设置页。
     * API < 30 上该权限不存在，直接视为已通过。
     */
    private void openManageExternalStorageSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            LcaiLogUtils.i("权限申请-API<30，MANAGE_EXTERNAL_STORAGE 视为已通过");
            recordManageStorageResult(true);
            launchNormalPermissions();
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            manageStorageLauncher.launch(intent);
        } catch (Exception e) {
            LcaiLogUtils.i("权限申请-跳转所有文件访问权限设置页失败:" + e.getMessage());
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                manageStorageLauncher.launch(intent);
            } catch (Exception ex) {
                LcaiLogUtils.i("权限申请-跳转所有文件访问权限列表失败:" + ex.getMessage());
                recordManageStorageResult(false);
                launchNormalPermissions();
            }
        }
    }

    /**
     * 从「所有文件访问权限」设置页返回后回调。
     */
    private void onBackFromManageStorageSettings() {
        boolean granted = checkManageExternalStorageGranted();
        LcaiLogUtils.i("权限申请-从所有文件访问权限设置页返回，是否授予:" + granted);
        recordManageStorageResult(granted);
        launchNormalPermissions();
    }

    private void recordManageStorageResult(boolean granted) {
        permissions.put(LcaiPermissionString.MANAGE_EXTERNAL_STORAGE, granted);

        LcaiPermissionEntity entity = new LcaiPermissionEntity();
        entity.setPermission(LcaiPermissionString.MANAGE_EXTERNAL_STORAGE);
        entity.setGranted(granted);
        // 特殊权限没有「永久拒绝」概念
        entity.setPermanentlyDenied(false);
        permissionEntities.add(entity);
    }

    private boolean checkManageExternalStorageGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true;
    }

    /**
     * 继续申请剩余的普通运行时权限，为空则直接结束。
     */
    private void launchNormalPermissions() {
        if (pendingNormalPermissions != null && pendingNormalPermissions.length > 0) {
            permissionLauncher.launch(pendingNormalPermissions);
        } else {
            finishAll();
        }
    }

    private void onNormalPermissionsResult(Map<String, Boolean> result) {
        LcaiLogUtils.i("权限申请-权限申请结果:" + result);
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            String p = entry.getKey();
            boolean granted = Boolean.TRUE.equals(entry.getValue());

            LcaiPermissionEntity entity = new LcaiPermissionEntity();
            entity.setPermission(p);
            entity.setGranted(granted);

            if (!granted) {
                entity.setPermanentlyDenied(isPermanentDenied(p));
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

    /**
     * 从 App 详情设置页返回后回调（toSystem=true 时使用）。
     */
    private void onBackFromSystemSettings() {
        if (permissionArray != null) {
            for (String p : permissionArray) {
                boolean granted;
                if (LcaiPermissionString.MANAGE_EXTERNAL_STORAGE.equals(p)) {
                    granted = checkManageExternalStorageGranted();
                } else {
                    granted = ContextCompat.checkSelfPermission(this, p)
                            == PackageManager.PERMISSION_GRANTED;
                }
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
        if (LcaiPermissionString.MANAGE_EXTERNAL_STORAGE.equals(permission)) {
            // 特殊权限没有「永久拒绝」概念
            return false;
        }
        return ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
                && !ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
    }
}