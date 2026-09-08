package com.liucai.core;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.liucai.camera_photo.bulider.LcaiCameraPhotoBulider;
import com.liucai.camera_photo.core.LcaiCameraPhoto;
import com.liucai.camera_photo.core.LcaiPhotoCameraActivityResult;
import com.liucai.camera_photo.core.LcaiPhotoResult;
import com.liucai.permission.bulider.LcaiPermissionRequestBulider;
import com.liucai.permission.core.LcaiPermissionActivityResult;
import com.liucai.permission.core.LcaiPermissionRequest;
import com.liucai.permission.core.LcaiReqPermissionResult;
import com.liucai.tipsdialog.bulider.LcaiTipsDialogBulider;
import com.liucai.tipsdialog.core.OnTipsDialogInterface;

import java.lang.ref.WeakReference;
import java.util.Map;

public class LcaiManager {

    private final LcaiPermissionActivityResult permissionActivityResult = new LcaiPermissionActivityResult() {
        @Override
        public void onPermissionResult(boolean granted, Map<String, Boolean> permissions) {
            LcaiPermissionRequestBulider builder = mBuilderRef != null ? mBuilderRef.get() : null;
            LcaiReqPermissionResult result = builder != null ? builder.result : null;

            if (result != null) {
                if (granted) {
                    result.onReqPermissionPass();
                } else {
                    if (builder.system) {
                        showNeverDialog(builder, permissions);
                    } else {
                        result.onReqPermissionNoPass(permissions);
                    }
                }
            }
            // 权限回调完成后清理引用
            clearPermissionReferences();
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

    public void permissionReq(@NonNull LcaiPermissionRequestBulider bulider) {
        clearPermissionReferences();
        mBuilderRef = new WeakReference<>(bulider);
        new LcaiPermissionRequest(bulider);
    }

    public void openPhotoOrCamera(@NonNull LcaiCameraPhotoBulider bulider) {
        clearPhotoReferences(); // 清理旧的相机引用
        mBuilderPhotoRef = new WeakReference<>(bulider);
        new LcaiCameraPhoto(bulider);
    }

    public void showNeverDialog(LcaiPermissionRequestBulider bulider, Map<String, Boolean> permissions) {
        new LcaiTipsDialogBulider()
                .with(bulider.mActivity)
                .addTitle(bulider.title)
                .addTitleColor(bulider.titleColor)
                .addTitleSize(bulider.titleSize)
                .addContent(bulider.neverContent)
                .addContentColor(bulider.contentColor)
                .addContentSize(bulider.contentSize)
                .addCancelText("取消")
                .addCancelSize(bulider.btnSize)
                .addCancelColor(bulider.leftColor)
                .addCancelBackground(bulider.leftBg)
                .addConfirmText("立即设置")
                .addConfirmColor(bulider.rightColor)
                .addConfirmSize(bulider.btnSize)
                .addConfirmBackground(bulider.rightBg)
                .addDialogInterface(new OnTipsDialogInterface() {
                    @Override
                    public void onCancelListener() {
                        if (bulider.result != null) {
                            bulider.result.onReqPermissionNoPass(permissions);
                        }
                    }

                    @Override
                    public void onConfirmListener() {
                        Uri packageURI = Uri.parse("package:" + bulider.mActivity.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        bulider.mActivity.startActivity(intent);
                    }
                }).bulid();
    }

    private void clearPermissionReferences() {
        mBuilderRef = null;
    }

    private void clearPhotoReferences() {
        mBuilderPhotoRef = null;
    }
}