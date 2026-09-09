package com.liucai.permission.view;

import android.app.Activity;
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

import com.liucai.core.LcaiManager;
import com.liucai.core.base.LcaiBasePermissionActivity;
import com.liucai.permission.bulider.LcaiPermissionRequestBulider;
import com.liucai.permission.core.LcaiPermissionString;
import com.liucai.permission.core.LcaiReqPermissionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public class LcaiPermissionActivity extends LcaiBasePermissionActivity {

    private String[] permissionArray;
    private boolean hasNotification;
    private boolean toSystem;
    private Map<String, Boolean> permissions;
    private ActivityResultLauncher permissionLauncher;
    private ActivityResultLauncher toSystemLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        permissions = new ConcurrentHashMap<>();
        if (extras != null) {
            permissionArray = extras.getStringArray(LcaiPermissionString.PERMISSION_KEY);
            hasNotification = extras.getBoolean(LcaiPermissionString.HAS_NOTIFICATION, false);
            toSystem = extras.getBoolean(LcaiPermissionString.TO_SYSTEM, false);
        }

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean granted = true;
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                if (!entry.getValue()) {
                    granted = false;
                    //判断是否永久拒绝
                    boolean isPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(this, entry.getKey()) &&
                            ContextCompat.checkSelfPermission(this, entry.getKey()) != PackageManager.PERMISSION_GRANTED;
                    permissions.put(entry.getKey(), isPermanentlyDenied);
                }
            }
            // 合并通知权限的结果
            handleNotificationPermission();

            // 如果请求了通知权限且未通过，整体结果应为 false
            if (hasNotification && Boolean.FALSE.equals(permissions.get(LcaiPermissionString.NOTIFICATIONS))) {
                granted = false;
            }

            LcaiManager.Internal.getPermissionResult().onPermissionResult(granted, permissions);
            finish();
        });

        toSystemLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                LcaiPermissionRequestBulider bulider = new LcaiPermissionRequestBulider().with(this);
                if (hasNotification) {
                    bulider.addNotification();
                }
                bulider.check(true)
                        .addPermission(permissionArray)
                        .addResult(new LcaiReqPermissionResult() {
                            @Override
                            public void onReqPermissionPass() {
                                LcaiManager.Internal.getPermissionResult().onPermissionResult(true, new HashMap<>());
                            }

                            @Override
                            public void onReqPermissionNoPass(Map<String, Boolean> permissions) {
                                LcaiManager.Internal.getPermissionResult().onPermissionResult(false, permissions);
                            }
                        });
                LcaiManager.getInstance().permissionReq(bulider);

            }
        });

        if (toSystem) {
            Uri packageURI = Uri.parse("package:" + getPackageName());
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
            toSystemLauncher.launch(intent);
            return;
        }

        if (hasNotification) {
            // 预先检查并放入 map
            handleNotificationPermission();
            // 如果还有其他常规权限需要申请
            if (permissionArray != null && permissionArray.length > 0) {
                permissionLauncher.launch(permissionArray);
            } else {
                // 只有通知权限，直接回调
                boolean allGranted = Boolean.TRUE.equals(permissions.get(LcaiPermissionString.NOTIFICATIONS));
                LcaiManager.Internal.getPermissionResult().onPermissionResult(allGranted, permissions);
                finish();
            }
        } else {
            if (permissionArray != null && permissionArray.length > 0) {
                permissionLauncher.launch(permissionArray);
            } else {
                LcaiManager.Internal.getPermissionResult().onPermissionResult(true, permissions);
                finish();
            }
        }
    }

    /**
     * 统一处理通知权限状态
     */
    private void handleNotificationPermission() {
        if (hasNotification) {
            boolean isNotifyGranted = checkNotification();
            permissions.put(LcaiPermissionString.NOTIFICATIONS, isNotifyGranted);
        }
    }

    /**
     * 检查Android 13 以下通知是否授权
     *
     * @return
     */
    public boolean checkNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            return manager != null && manager.areNotificationsEnabled();
        }
        // Android 8 以下默认视为已开启（规避部分厂商 ROM 的 Bug）
        return true;
    }
}
