package com.liucai.camera_photo.view;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.liucai.camera_photo.core.LcaiGalleryCheckType;
import com.liucai.camera_photo.core.LcaiGalleryResultType;
import com.liucai.core.LcaiManager;
import com.liucai.core.base.LcaiBasePermissionActivity;
import com.liucai.core.util.file.LcaiFileInterface;
import com.liucai.core.util.file.LcaiFileProvider;
import com.liucai.core.util.file.LcaiFileUtils;
import com.liucai.core.util.text.TextUtils;

import java.io.File;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/4
 */
public class LcaiCamearPhtoActivity extends LcaiBasePermissionActivity {
    public static final String OPEN_KEY = "OPEN_CAMERA_PHOTO";
    public static final String RESULT_KEY = "RESULT_CAMERA_PHOTO";
    public static final String SAVE_IMAGE_KEY = "SAVE_IMAGE";
    public static final String ADD_HEADER_KEY = "ADD_HEADER";
    private String checkType;
    private String resultType;
    private boolean saveImage;
    private boolean addHeader;
    private File imageFile;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> photoLauncher;
    private boolean isLaunched = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            checkType = bundle.getString(OPEN_KEY);
            resultType = bundle.getString(RESULT_KEY);
            saveImage = bundle.getBoolean(SAVE_IMAGE_KEY);
            addHeader = bundle.getBoolean(ADD_HEADER_KEY);
        } else {
            sendError("获取参数失败");
            return;
        }

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), this::cameraResult);
        photoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), this::photoResult);
        if (savedInstanceState == null) {
            launchPicker();
        }
    }

    private void launchPicker() {
        if (isLaunched) return;
        if (TextUtils.equals(checkType, LcaiGalleryCheckType.PHOTO.name())) {
            isLaunched = true;
            photoLauncher.launch("image/*");
        } else if (TextUtils.equals(checkType, LcaiGalleryCheckType.CAMERA.name())) {
            isLaunched = true;
            imageFile = LcaiFileUtils.createCameraImageFile(this);
            if (imageFile == null) {
                sendError("创建图片文件失败");
                return;
            }
            Uri imageUri = LcaiFileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", imageFile);
            cameraLauncher.launch(imageUri);
        }
    }

    public void photoResult(Uri callback) {
        if (callback == null) {
            sendCancel();
            finish();
            return;
        }

        if (TextUtils.equals(resultType, LcaiGalleryResultType.BASE64.name())) {
            JSONObject jsonObject = LcaiFileUtils.convertImageToBase64(LcaiCamearPhtoActivity.this, callback, addHeader);
            sendBase64(jsonObject.toJSONString());
        } else if (TextUtils.equals(resultType, LcaiGalleryResultType.URL.name())) {
            String path = LcaiFileUtils.getPath(LcaiCamearPhtoActivity.this, callback);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fileType", "image");
            File file = new File(path);
            if (file.exists()) {
                long fileSize = file.length() / 1024;
                jsonObject.put("fileSize", fileSize);
            }
            jsonObject.put("content", path);
            sendUrl(jsonObject.toJSONString());
        }
        finish();
    }

    public void cameraResult(boolean callback) {
        if (!callback) {
            LcaiFileUtils.clearFile(imageFile);
            sendCancel(); // 取消拍照
            finish();
            return;
        }

        if (TextUtils.equals(resultType, LcaiGalleryResultType.BASE64.name())) {
            JSONObject jsonObject = LcaiFileUtils.converImagePathToBase64(imageFile.getAbsolutePath(), addHeader);
            sendBase64(jsonObject.toJSONString());
        } else if (TextUtils.equals(resultType, LcaiGalleryResultType.URL.name()) && !saveImage) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fileType", "image");
            if (imageFile.exists()) {
                jsonObject.put("fileSize", imageFile.length() / 1024);
            }
            jsonObject.put("content", imageFile.getAbsolutePath());
            sendUrl(jsonObject.toJSONString());
        }
        if (saveImage) {
            LcaiFileUtils.saveImageToGallery(this, imageFile, new LcaiFileInterface() {
                @Override
                public void onUriPath(String filePath) {
                    if (TextUtils.equals(resultType, LcaiGalleryResultType.URL.name())) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("fileType", "image");
                        File file = new File(filePath);
                        if (file.exists()) {
                            jsonObject.put("fileSize", file.length() / 1024);
                        }
                        jsonObject.put("content", filePath);
                        sendUrl(jsonObject.toJSONString());
                    }
                    LcaiFileUtils.clearFile(imageFile);
                }
            });
        } else {
            if (TextUtils.equals(resultType, LcaiGalleryResultType.BASE64.name())) {
                LcaiFileUtils.clearFile(imageFile);
            }
        }
        finish();
    }
    private void sendUrl(String url) {
        if (LcaiManager.Internal.getPhotoCameraResult() != null) {
            LcaiManager.Internal.getPhotoCameraResult().onUrl(url);
        }
    }
    private void sendBase64(String base64) {
        if (LcaiManager.Internal.getPhotoCameraResult() != null) {
            LcaiManager.Internal.getPhotoCameraResult().onBase64(base64);
        }
    }
    private void sendError(String error) {
        if (LcaiManager.Internal.getPhotoCameraResult() != null) {
            LcaiManager.Internal.getPhotoCameraResult().onError(error);
        }
    }
    private void sendCancel() {
        if (LcaiManager.Internal.getPhotoCameraResult() != null) {
            LcaiManager.Internal.getPhotoCameraResult().onCancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LcaiManager.getInstance().clearPhotoReferences();
    }
}
