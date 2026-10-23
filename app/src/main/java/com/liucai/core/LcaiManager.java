package com.liucai.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.liucai.camera_photo.bulider.LcaiCameraPhotoBulider;
import com.liucai.camera_photo.core.LcaiCameraPhoto;
import com.liucai.camera_photo.core.LcaiGalleryResult;
import com.liucai.core.util.log.LcaiLogUtils;
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

    private volatile boolean pendingToSystem = false;

    private LcaiPermissionRequestBulider currentPermissionBuilder;
    private LcaiCameraPhotoBulider currentPhotoBuilder;

    private LcaiManager() {
    }

    private static class LcaiManagerHelper {
        private static final LcaiManager INSTANCE = new LcaiManager();
    }

    public static class Internal {
        private Internal() {
        }

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

    // ============ 权限回调 ============

    private final LcaiPermissionActivityResult permissionActivityResult =
            new LcaiPermissionActivityResult() {
                @Override
                public void onPermissionResult(boolean granted,
                                               Map<String, Boolean> permissions) {
                    LcaiPermissionRequestBulider builder = currentPermissionBuilder;
                    LcaiReqPermissionResult result =
                            builder != null ? builder.result : null;

                    if (result == null) {
                        pendingToSystem = false;
                        clearPermissionReferences();
                        return;
                    }

                    if (granted) {
                        pendingToSystem = false;
                        clearPermissionReferences();
                        result.onReqPermissionPass();
                        return;
                    }

                    if (pendingToSystem) {
                        pendingToSystem = false;
                        clearPermissionReferences();
                        result.onReqPermissionNoPass(permissions);
                        return;
                    }

                    if (builder.system) {
                        if (builder.toSystemBuilder != null) {
                            final LcaiPermissionRequestBulider finalBuilder = builder;

                            builder.toSystemBuilder.setOnTipsDialogInterface(
                                    new OnTipsDialogInterface() {
                                        @Override
                                        public void onCancelListener() {
                                            LcaiReqPermissionResult r =
                                                    finalBuilder.result;
                                            clearPermissionReferences();
                                            if (r != null) {
                                                r.onReqPermissionNoPass(permissions);
                                            }
                                        }

                                        @Override
                                        public void onConfirmListener() {
                                            LcaiReqPermissionResult r =
                                                    finalBuilder.result;

                                            if (permissions == null
                                                    || permissions.isEmpty()) {
                                                clearPermissionReferences();
                                                if (r != null) {
                                                    r.onReqPermissionNoPass(permissions);
                                                }
                                                return;
                                            }

                                            toSystem(permissions,
                                                    finalBuilder.mActivity);
                                        }
                                    }).build();
                        } else {
                            toSystem(permissions, builder.mActivity);
                        }
                    } else {
                        clearPermissionReferences();
                        result.onReqPermissionNoPass(permissions);
                    }
                }
            };

    // ============ 相机/相册回调 ============

    private final LcaiGalleryResult photoCameraActivityResult =
            new LcaiGalleryResult() {
                @Override
                public void onUrl(String url) {
                    LcaiCameraPhotoBulider builder = currentPhotoBuilder;
                    clearPhotoReferences();

                    if (builder != null && builder.result != null) {
                        builder.result.onUrl(url);
                    }
                }

                @Override
                public void onBase64(String base64) {
                    LcaiCameraPhotoBulider builder = currentPhotoBuilder;
                    clearPhotoReferences();

                    if (builder != null && builder.result != null) {
                        builder.result.onBase64(base64);
                    }
                }

                @Override
                public void onCancel() {
                    LcaiCameraPhotoBulider builder = currentPhotoBuilder;
                    clearPhotoReferences();

                    if (builder != null && builder.result != null) {
                        builder.result.onCancel();
                    }
                }

                @Override
                public void onError(String error) {
                    LcaiCameraPhotoBulider builder = currentPhotoBuilder;
                    clearPhotoReferences();

                    if (builder != null && builder.result != null) {
                        builder.result.onError(error);
                    }
                }
            };

    // ============ 核心跳转逻辑 ============

    private void toSystem(Map<String, Boolean> permissions, Activity activity) {
        if (!isAlive(activity)) {
            pendingToSystem = false;
            clearPermissionReferences();
            return;
        }

        if (permissions == null || permissions.isEmpty()) {
            pendingToSystem = false;
            clearPermissionReferences();
            return;
        }

        List<String> stringList = new ArrayList<>();
        boolean hasNotification = false;

        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            String key = entry.getKey();
            if (TextUtils.equals(key, LcaiPermissionString.NOTIFICATIONS)) {
                hasNotification = true;
            } else if (key != null) {
                stringList.add(key);
            }
        }

        if (stringList.isEmpty() && !hasNotification) {
            pendingToSystem = false;
            clearPermissionReferences();
            return;
        }

        Intent intent = new Intent(activity, LcaiPermissionActivity.class);
        Bundle bundle = new Bundle();

        bundle.putStringArray(
                LcaiPermissionString.PERMISSION_KEY,
                stringList.toArray(new String[0]));
        bundle.putBoolean(LcaiPermissionString.HAS_NOTIFICATION, hasNotification);
        bundle.putBoolean(LcaiPermissionString.TO_SYSTEM, true);

        intent.putExtras(bundle);

        pendingToSystem = true;

        try {
            activity.startActivity(intent);
        } catch (Throwable t) {
            pendingToSystem = false;
            clearPermissionReferences();
            LcaiLogUtils.i("跳转系统权限页失败: " + t.getMessage());
        }
    }

    public void permissionReq(@NonNull LcaiPermissionRequestBulider builder) {
        clearPermissionReferences();
        pendingToSystem = false;
        currentPermissionBuilder = builder;
        new LcaiPermissionRequest(builder);
    }

    public void openPhotoOrCamera(@NonNull LcaiCameraPhotoBulider builder) {
        clearPhotoReferences();
        currentPhotoBuilder = builder;
        new LcaiCameraPhoto(builder).start();
    }

    public void clearPermissionReferences() {
        currentPermissionBuilder = null;
    }

    public void clearPhotoReferences() {
        currentPhotoBuilder = null;
    }

    private static boolean isAlive(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isDestroyed();
        }
        return true;
    }
}