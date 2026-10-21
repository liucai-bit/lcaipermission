package com.liucai.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.liucai.camera_photo.bulider.LcaiCameraPhotoBulider;
import com.liucai.camera_photo.core.LcaiCameraPhoto;
import com.liucai.camera_photo.core.LcaiGalleryResult;
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.bulider.LcaiPermissionRequestBulider;
import com.liucai.permission.core.LcaiPermissionActivityResult;
import com.liucai.permission.core.LcaiPermissionRequest;
import com.liucai.permission.core.LcaiPermissionString;
import com.liucai.permission.core.LcaiReqPermissionResult;
import com.liucai.permission.view.LcaiPermissionActivity;
import com.liucai.tipsdialog.core.OnTipsDialogInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LcaiManager {
    private boolean pendingToSystem = false;

    // ============ 权限回调 ============
    private final LcaiPermissionActivityResult permissionActivityResult = new LcaiPermissionActivityResult() {
        @Override
        public void onPermissionResult(boolean granted, Map<String, Boolean> permissions) {
            // 改用强引用，避免传匿名对象时被GC提前回收
            LcaiPermissionRequestBulider builder = currentPermissionBuilder;
            LcaiReqPermissionResult result = builder != null ? builder.result : null;
            if (result != null) {
                if (granted) {
                    result.onReqPermissionPass();
                    pendingToSystem = false;
                    clearPermissionReferences();
                } else {
                    if (pendingToSystem) {
                        pendingToSystem = false;
                        result.onReqPermissionNoPass(permissions);
                        clearPermissionReferences();
                        return;
                    }
                    if (builder.system) {
                        if (builder.toSystemBuilder != null) {
                            builder.toSystemBuilder.setOnTipsDialogInterface(new OnTipsDialogInterface() {
                                @Override
                                public void onCancelListener() {
                                    if (builder.result != null) {
                                        builder.result.onReqPermissionNoPass(permissions);
                                        clearPermissionReferences();
                                    }
                                }
                                @Override
                                public void onConfirmListener() {
                                    toSystem(permissions, builder.mActivity);
                                }
                            }).build();
                        } else {
                            toSystem(permissions, builder.mActivity);
                        }
                    } else {
                        result.onReqPermissionNoPass(permissions);
                        clearPermissionReferences();
                    }
                }
            } else {
                clearPermissionReferences();
            }
        }
    };

    private final LcaiGalleryResult photoCameraActivityResult = new LcaiGalleryResult() {
        @Override
        public void onUrl(String url) {
            LcaiCameraPhotoBulider builder = currentPhotoBuilder;
            if (builder != null && builder.result != null) {
                builder.result.onUrl(url);
            }
        }

        @Override
        public void onBase64(String base64) {
            LcaiCameraPhotoBulider builder = currentPhotoBuilder;
            if (builder != null && builder.result != null) {
                builder.result.onBase64(base64);
            }
        }

        @Override
        public void onCancel() {
            LcaiCameraPhotoBulider builder = currentPhotoBuilder;
            if (builder != null && builder.result != null) {
                builder.result.onCancel();
            }
        }

        @Override
        public void onError(String error) {
            LcaiCameraPhotoBulider builder = currentPhotoBuilder;
            if (builder != null && builder.result != null) {
                builder.result.onError(error);
            }
        }
    };

    // 使用强引用持有 Builder，防止业务方用匿名/局部变量传参时被系统提前 GC
    private LcaiPermissionRequestBulider currentPermissionBuilder;
    private LcaiCameraPhotoBulider currentPhotoBuilder;

    private LcaiManager() {}

    private static class LcaiManagerHelper {
        private static final LcaiManager INSTANCE = new LcaiManager();
    }

    public static class Internal {
        private Internal() {}

        public static LcaiPermissionActivityResult getPermissionResult() {
            return getInstance().permissionActivityResult;
        }

        public static LcaiGalleryResult getPhotoCameraResult() {
            return getInstance().photoCameraActivityResult;
        }
    }

    public static LcaiManager getInstance() {
        return LcaiManagerHelper.INSTANCE;
    }

    // ============ 核心跳转逻辑 ============

    private void toSystem(Map<String, Boolean> permissions, Activity mActivity) {
        if (mActivity == null || mActivity.isFinishing() || mActivity.isDestroyed()) {
            clearPermissionReferences();
            return;
        }
        if (!permissions.isEmpty()) {
            List<String> stringList = new ArrayList<>();
            boolean hasNotification = false;
            for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                if (TextUtils.equals(entry.getKey(), LcaiPermissionString.NOTIFICATIONS)) {
                    hasNotification = true;
                } else {
                    stringList.add(entry.getKey());
                }
            }
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putStringArray(LcaiPermissionString.PERMISSION_KEY, stringList.toArray(new String[0]));
            bundle.putBoolean(LcaiPermissionString.HAS_NOTIFICATION, hasNotification);
            bundle.putBoolean(LcaiPermissionString.TO_SYSTEM, true);
            intent.putExtras(bundle);
            intent.setClass(mActivity, LcaiPermissionActivity.class);
            pendingToSystem = true;
            mActivity.startActivity(intent);
        }
    }

    public void permissionReq(@NonNull LcaiPermissionRequestBulider bulider) {
        clearPermissionReferences();
        pendingToSystem = false;
        currentPermissionBuilder = bulider; // 强引用
        new LcaiPermissionRequest(bulider);
    }

    public void openPhotoOrCamera(@NonNull LcaiCameraPhotoBulider bulider) {
        clearPhotoReferences();
        currentPhotoBuilder = bulider; // 强引用
        // 调用我们重构后的 LcaiCameraPhoto.start()
        new LcaiCameraPhoto(bulider).start();
    }

    /**
     * 供 LcaiCamearPhtoActivity 在 onDestroy 中安全清理引用
     */
    public void clearPermissionReferences() {
        currentPermissionBuilder = null;
    }

    public void clearPhotoReferences() {
        currentPhotoBuilder = null;
    }
}