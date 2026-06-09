package com.liucai.core;

import androidx.annotation.NonNull;

import com.liucai.camera_photo.bulider.LcaiCameraPhotoBulider;
import com.liucai.camera_photo.core.LcaiCameraPhoto;
import com.liucai.camera_photo.core.LcaiPhotoCameraActivityResult;
import com.liucai.camera_photo.core.LcaiPhotoResult;
import com.liucai.permission.bulider.LcaiPermissionRequestBulider;
import com.liucai.permission.core.LcaiPermissionActivityResult;
import com.liucai.permission.core.LcaiPermissionRequest;
import com.liucai.permission.core.LcaiReqPermissionResult;

import java.lang.ref.WeakReference;

/**
 * @author LIUCAI
 * @program lcpermission
 * @description
 * @Date 2026/6/5
 */
public class LcaiManager{

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
                    result.onReqPermissionNoPass();
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

    private void clearReferences() {
        sPermissionResultRef = null;
        mBuilderRef = null;
    }
}
