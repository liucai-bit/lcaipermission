package com.liucai.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.liucai.camera_photo.bulider.LcaiCameraPhotoBulider;
import com.liucai.camera_photo.core.LcaiCameraPhoto;
import com.liucai.camera_photo.core.LcaiPhotoCameraActivityResult;
import com.liucai.camera_photo.core.LcaiPhotoResult;
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.bulider.LcaiPermissionRequestBulider;
import com.liucai.permission.core.LcaiPermissionActivityResult;
import com.liucai.permission.core.LcaiPermissionRequest;
import com.liucai.permission.core.LcaiPermissionString;
import com.liucai.permission.core.LcaiReqPermissionResult;
import com.liucai.permission.view.LcaiPermissionActivity;
import com.liucai.tipsdialog.core.OnTipsDialogInterface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LcaiManager {
    private boolean pendingToSystem = false;
    private final LcaiPermissionActivityResult permissionActivityResult = new LcaiPermissionActivityResult() {
        @Override
        public void onPermissionResult(boolean granted, Map<String, Boolean> permissions) {
            LcaiPermissionRequestBulider builder = mBuilderRef != null ? mBuilderRef.get() : null;
            LcaiReqPermissionResult result = builder != null ? builder.result : null;
            if (result != null) {
                if (granted) {
                    result.onReqPermissionPass();
                    pendingToSystem = false;
                    // 权限回调完成后清理引用
                    clearPermissionReferences();
                } else {
                    if (pendingToSystem) {
                        pendingToSystem = false;
                        result.onReqPermissionNoPass(permissions);
                        // 权限回调完成后清理引用
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
                                        // 权限回调完成后清理引用
                                        clearPermissionReferences();
                                    }
                                }

                                @Override
                                public void onConfirmListener() {
                                    toSystem(permissions,builder.mActivity);
                                }
                            }).build();
                        } else {
                           toSystem(permissions,builder.mActivity);
                        }
                    } else {
                        result.onReqPermissionNoPass(permissions);
                        // 权限回调完成后清理引用
                        clearPermissionReferences();
                    }
                }
            } else {
                // 权限回调完成后清理引用
                clearPermissionReferences();
            }
        }
    };

    private final LcaiPhotoCameraActivityResult photoCameraActivityResult = new LcaiPhotoCameraActivityResult() {
        @Override
        public void onUrl(String url) {
            LcaiCameraPhotoBulider bulider = mBuilderPhotoRef != null ? mBuilderPhotoRef.get() : null;
            LcaiPhotoResult result = bulider != null ? bulider.result : null;
            if (result != null) result.onUrl(url);
        }

        @Override
        public void onBase64(String base64) {
            LcaiCameraPhotoBulider bulider = mBuilderPhotoRef != null ? mBuilderPhotoRef.get() : null;
            LcaiPhotoResult result = bulider != null ? bulider.result : null;
            if (result != null) result.onBase64(base64);
        }

        @Override
        public void onError(String error) {
            LcaiCameraPhotoBulider bulider = mBuilderPhotoRef != null ? mBuilderPhotoRef.get() : null;
            LcaiPhotoResult result = bulider != null ? bulider.result : null;
            if (result != null) result.onError(error);
        }
    };

    private WeakReference<LcaiPermissionRequestBulider> mBuilderRef;
    private WeakReference<LcaiCameraPhotoBulider> mBuilderPhotoRef;

    private LcaiManager() {}

    private static class LcaiManagerHelper {
        private static final LcaiManager INSTANCE = new LcaiManager();
    }

    public static class Internal {
        private Internal() {}

        public static LcaiPermissionActivityResult getPermissionResult() {
            return getInstance().permissionActivityResult;
        }

        public static LcaiPhotoCameraActivityResult getPhotoCameraResult() {
            return getInstance().photoCameraActivityResult;
        }
    }

    public static LcaiManager getInstance() {
        return LcaiManagerHelper.INSTANCE;
    }

    private void toSystem(Map<String, Boolean> permissions, Activity mActivity) {
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
            bundle.putStringArray(LcaiPermissionString.PERMISSION_KEY, stringList.toArray(new String[stringList.size()]));
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
        mBuilderRef = new WeakReference<>(bulider);
        new LcaiPermissionRequest(bulider);
    }

    public void openPhotoOrCamera(@NonNull LcaiCameraPhotoBulider bulider) {
        clearPhotoReferences(); // 清理旧的相机引用
        mBuilderPhotoRef = new WeakReference<>(bulider);
        new LcaiCameraPhoto(bulider);
    }
    private void clearPermissionReferences() {
        mBuilderRef = null;
    }

    private void clearPhotoReferences() {
        mBuilderPhotoRef = null;
    }
}