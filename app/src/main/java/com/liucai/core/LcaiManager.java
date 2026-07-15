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
import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/5
 */
public class LcaiManager{
    public static final Integer SETTING_CALLBACK = 1440;
    private static WeakReference<LcaiPermissionActivityResult> sPermissionResultRef;
    private WeakReference<LcaiPermissionRequestBulider> mBuilderRef;
    private static WeakReference<LcaiPhotoCameraActivityResult> sPhotoCameraResultRef;
    private WeakReference<LcaiCameraPhotoBulider> mBuilderPhotoRef;

    private LcaiPermissionActivityResult permissionActivityResult = new LcaiPermissionActivityResult() {
        @Override
        public void onPermissionResult(boolean granted) {
            LcaiPermissionRequestBulider builder = mBuilderRef != null ? mBuilderRef.get() : null;
            LcaiReqPermissionResult result = builder != null ? builder.result : null;

            if (result != null) {
                if (granted) {
                    result.onReqPermissionPass();
                } else {
                    if (builder.system) {
                        showNeverDialog(builder);
                    } else {
                        result.onReqPermissionNoPass();
                    }
                }
            }

            // 清理引用
            clearReferences();
        }
    };

    private LcaiPhotoCameraActivityResult photoCameraActivityResult = new LcaiPhotoCameraActivityResult() {
        @Override
        public void onUrl(String url) {
            LcaiCameraPhotoBulider bulider = mBuilderPhotoRef != null ? mBuilderPhotoRef.get() : null;
            LcaiPhotoResult result = bulider != null ? bulider.result : null;
            if (result != null) {
                result.onUrl(url);
            }
        }

        @Override
        public void onBase64(String base64) {
            LcaiCameraPhotoBulider bulider = mBuilderPhotoRef != null ? mBuilderPhotoRef.get() : null;
            LcaiPhotoResult result = bulider != null ? bulider.result : null;
            if (result != null) {
                result.onBase64(base64);
            }
        }

        @Override
        public void onError(String error) {
            LcaiCameraPhotoBulider bulider = mBuilderPhotoRef != null ? mBuilderPhotoRef.get() : null;
            LcaiPhotoResult result = bulider != null ? bulider.result : null;
            if (result != null) {
                result.onError(error);
            }
        }
    };


    private LcaiManager() {}

    private static class LcaiManagerHelper{
        private static final LcaiManager INSTANCE = new LcaiManager();
    }

    public static class Internal{
        private Internal() {}

        public static LcaiPermissionActivityResult getPermissionResult() {
            return sPermissionResultRef != null ? sPermissionResultRef.get() : null;
        }

        public static LcaiPhotoCameraActivityResult getPhotoCameraResult() {
            return sPhotoCameraResultRef != null ? sPhotoCameraResultRef.get() : null;
        }
    }

    public static LcaiManager getInstance() {
        return LcaiManagerHelper.INSTANCE;
    }

    public void permissionReq(@NonNull LcaiPermissionRequestBulider bulider) {
        sPermissionResultRef = new WeakReference<>(permissionActivityResult);
        mBuilderRef = new WeakReference<>(bulider);
        new LcaiPermissionRequest(bulider);
    }

    public void openPhotoOrCamera(@NonNull LcaiCameraPhotoBulider bulider) {
        sPhotoCameraResultRef = new WeakReference<>(photoCameraActivityResult);
        mBuilderPhotoRef = new WeakReference<>(bulider);
        new LcaiCameraPhoto(bulider);
    }

    public void showNeverDialog(LcaiPermissionRequestBulider bulider) {
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
                            bulider.result.onReqPermissionNoPass();
                        }
                    }

                    @Override
                    public void onConfirmListener() {
                        //跳转设置
                        // 有权限被永久拒绝，引导用户去设置页面
                        Uri packageURI = Uri.parse("package:" + bulider.mActivity.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        bulider.mActivity.startActivity(intent);
                    }
                }).bulid();

    }

    private void clearReferences() {
        sPermissionResultRef = null;
        mBuilderRef = null;
    }
}
