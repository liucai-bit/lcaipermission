package com.liucai.permission.core;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.alibaba.fastjson.JSON;
import com.liucai.core.exception.LcaiHttpException;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.bulider.LcaiPermissionRequestBulider;
import com.liucai.permission.view.LcaiPermissionActivity;
import com.liucai.tipsdialog.core.OnTipsDialogInterface;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
@SuppressWarnings("unused")
public class LcaiPermissionRequest {

    public LcaiPermissionRequestBulider builder;

    public LcaiPermissionRequest(LcaiPermissionRequestBulider builder) {
        this.builder = builder;
        LcaiLogUtils.i("权限申请-开始申请权限");
        checkSelPermission();
    }

    private LcaiPermissionRequest() {
    }

    /**
     * 校验权限是否授权，如果没有授权就去申请权限。
     * 注意：MANAGE_EXTERNAL_STORAGE 在这里只做「是否已授权」检查，
     * 未授权时作为普通待申请项传给 LcaiPermissionActivity，
     * 由 Activity 内部判断是否需要跳转「所有文件访问权限」设置页。
     */
    public void checkSelPermission() {
        LcaiLogUtils.i("权限申请-检查权限");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            LcaiLogUtils.i("权限申请-SDK_INT<23");
            if (builder.result != null) {
                builder.result.onReqPermissionPass();
            }
            return;
        }

        // 1. 把各种存储抽象标识解析成当前版本真实权限，并做判空、去重
        List<String> reqPermissions = storageVerify(builder.permissions);
        // 2. 过滤掉当前版本不支持的运行时权限
        reqPermissions = filterUnsupportedBySdk(reqPermissions);

        List<String> stringList = new ArrayList<>();
        Map<String, Boolean> permissions = new ConcurrentHashMap<>();

        for (String permission : reqPermissions) {
            if (TextUtils.equals(permission, LcaiPermissionString.NOTIFICATIONS)) {
                // Android 13 以下的通知抽象标识，不是真实运行时权限
                LcaiLogUtils.i("权限申请-含有Android 13以下通知权限申请",
                        "是否拥有通知权限:" + checkNotification());
            } else {
                if (!checkPermission(permission)) {
                    permissions.put(permission, false);
                    stringList.add(permission);
                }
            }
        }

        if (!stringList.isEmpty() || this.builder.hasNotification) {
            if (builder.checkPermission) {
                LcaiLogUtils.i("权限申请-权限检查结束，需要申请" + stringList.size() + "个权限");
                if (builder.result != null) {
                    LcaiLogUtils.i("权限申请-权限申请结束，申请结果:不通过");
                    builder.result.onReqPermissionNoPass(permissions);
                }
                return;
            }

            LcaiLogUtils.i("权限申请-是否需要展示自定义弹窗:" + builder.asDialog);
            if (builder.asDialog) {
                if (builder.builder != null) {
                    builder.builder.setOnTipsDialogInterface(new OnTipsDialogInterface() {
                        @Override
                        public void onCancelListener() {
                            LcaiLogUtils.i("权限申请-弹窗点击取消");
                            if (builder.result != null) {
                                builder.result.onReqPermissionNoPass(permissions);
                            }
                        }

                        @Override
                        public void onConfirmListener() {
                            reqPermission(stringList);
                        }
                    }).build();
                } else {
                    // 自定义弹窗 builder 为空时直接申请，避免流程卡死
                    LcaiLogUtils.i("权限申请-自定义弹窗 builder 为空，直接申请权限");
                    reqPermission(stringList);
                }
            } else {
                reqPermission(stringList);
            }
        } else {
            if (builder.result != null) {
                builder.result.onReqPermissionPass();
            }
        }
    }

    /**
     * 把存储抽象标识解析为当前系统版本对应的真实权限，并做判空、去重。
     * <p>
     * 支持的抽象标识：
     * STORAGE（deprecated，等价于三种读取）
     * STORAGE_READ_IMAGE / STORAGE_READ_VIDEO / STORAGE_READ_AUDIO
     * STORAGE_WRITE（API 29+ 无需权限，解析结果为空）
     */
    @SuppressWarnings("deprecation")
    public List<String> storageVerify(List<String> list) {
        List<String> permissions = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return permissions;
        }
        LcaiLogUtils.i("权限申请-解析存储权限标识");

        boolean needReadImage = false;
        boolean needReadVideo = false;
        boolean needReadAudio = false;
        boolean needWrite = false;

        // LinkedHashSet 去重且保持传入顺序
        Set<String> dedup = new LinkedHashSet<>();
        for (String permission : list) {
            if (TextUtils.isEmpty(permission)) {
                continue;
            }
            if (TextUtils.equals(permission, LcaiPermissionString.STORAGE_READ_IMAGE)) {
                needReadImage = true;
            } else if (TextUtils.equals(permission, LcaiPermissionString.STORAGE_READ_VIDEO)) {
                needReadVideo = true;
            } else if (TextUtils.equals(permission, LcaiPermissionString.STORAGE_READ_AUDIO)) {
                needReadAudio = true;
            } else if (TextUtils.equals(permission, LcaiPermissionString.STORAGE_WRITE)) {
                needWrite = true;
            } else {
                dedup.add(permission);
            }
        }

        // 读取：按版本映射，29~32 上三种读取都会收敛到 READ_EXTERNAL_STORAGE，自动去重
        if (needReadImage || needReadVideo || needReadAudio) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+：细粒度媒体权限
                if (needReadImage) {
                    dedup.add(LcaiPermissionString.READ_MEDIA_IMAGES);
                }
                if (needReadVideo) {
                    dedup.add(LcaiPermissionString.READ_MEDIA_VIDEO);
                }
                if (needReadAudio) {
                    dedup.add(LcaiPermissionString.READ_MEDIA_AUDIO);
                }
            } else {
                // Android 6 ~ 12L：统一使用 READ_EXTERNAL_STORAGE
                dedup.add(LcaiPermissionString.READ_EXTERNAL_STORAGE);
            }
        }

        // 写入：API 29+ 写公共媒体用 MediaStore，无需权限
        if (needWrite) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                // Android 9 及以下：传统写入权限
                dedup.add(LcaiPermissionString.WRITE_EXTERNAL_STORAGE);
            } else {
                LcaiLogUtils.i("权限申请-STORAGE_WRITE 在 API 29+ 无需权限，跳过（请使用 MediaStore 写入）");
            }
        }

        permissions.addAll(dedup);
        return permissions;
    }

    /**
     * 过滤掉当前系统版本不支持的运行时权限，避免低版本申请不存在权限后一律返回 false。
     */
    private List<String> filterUnsupportedBySdk(List<String> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String p : list) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (TextUtils.equals(p, LcaiPermissionString.NOTIFICATIONS_13)
                        || TextUtils.equals(p, LcaiPermissionString.READ_MEDIA_IMAGES)
                        || TextUtils.equals(p, LcaiPermissionString.READ_MEDIA_VIDEO)
                        || TextUtils.equals(p, LcaiPermissionString.READ_MEDIA_AUDIO)) {
                    LcaiLogUtils.i("权限申请-当前版本不支持权限，已忽略:" + p);
                    continue;
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                if (TextUtils.equals(p, LcaiPermissionString.BLUETOOTH_SCAN)
                        || TextUtils.equals(p, LcaiPermissionString.BLUETOOTH_CONNECT)) {
                    LcaiLogUtils.i("权限申请-当前版本不支持蓝牙新权限，已忽略:" + p);
                    continue;
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (TextUtils.equals(p, LcaiPermissionString.ACCESS_BACKGROUND_LOCATION)) {
                    LcaiLogUtils.i("权限申请-当前版本不支持后台定位权限，已忽略:" + p);
                    continue;
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (TextUtils.equals(p, LcaiPermissionString.MANAGE_EXTERNAL_STORAGE)) {
                    LcaiLogUtils.i("权限申请-当前版本不支持所有文件访问权限，已忽略:" + p);
                    continue;
                }
            }
            result.add(p);
        }
        return result;
    }

    public boolean checkPermission(String permission) {
        if (TextUtils.isEmpty(permission) || !permission.startsWith("android.permission.")) {
            throw new LcaiHttpException("权限申请-请传入正常的权限字符串，例如: android.permission.CAMERA");
        }

        if (TextUtils.equals(permission, LcaiPermissionString.MANAGE_EXTERNAL_STORAGE)) {
            return checkManageExternalStorage();
        }

        int i = ContextCompat.checkSelfPermission(builder.mActivity, permission);
        return i == PermissionChecker.PERMISSION_GRANTED;
    }

    /**
     * 检查 MANAGE_EXTERNAL_STORAGE（所有文件访问权限）是否已授予。
     * API < 30 不存在该权限，视为已授权（使用旧存储权限即可）。
     * 注意：这里只做检查，实际跳转设置页由 LcaiPermissionActivity 负责。
     */
    private boolean checkManageExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true;
    }

    /**
     * 检查 Android 13 以下通知是否授权
     */
    public boolean checkNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) this.builder.mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            return (manager != null && manager.areNotificationsEnabled());
        }
        // Android 8 以下没有通知开关
        return true;
    }

    public void reqPermission(List<String> stringList) {
        List<String> requestList = stringList == null
                ? new ArrayList<>()
                : new ArrayList<>(new LinkedHashSet<>(stringList));

        LcaiLogUtils.i("权限申请-跳转开始申请权限", "待申请权限:" + JSON.toJSONString(requestList));

        if (requestList.isEmpty() && !this.builder.hasNotification) {
            // 正常情况下不会走到这里：未授权权限一定在 stringList 中；
            // 仅当 hasNotification 为 true 且没有其他运行时权限时，才允许空数组进入 Activity 处理通知。
            LcaiLogUtils.i("权限申请-无可申请的运行时权限，直接结束");
            return;
        }

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putStringArray(LcaiPermissionString.PERMISSION_KEY, requestList.toArray(new String[0]));
        bundle.putBoolean(LcaiPermissionString.HAS_NOTIFICATION, this.builder.hasNotification);
        intent.putExtras(bundle);
        intent.setClass(builder.mActivity, LcaiPermissionActivity.class);
        builder.mActivity.startActivity(intent);
    }
}