package com.liucai.camera_photo.view;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.liucai.camera_photo.core.LcaiPhotoLib;
import com.liucai.core.LcaiManager;
import com.liucai.core.base.LcaiBasePermissionActivity;
import com.liucai.core.util.file.LcaiFileInterface;
import com.liucai.core.util.file.LcaiFileProvider;
import com.liucai.core.util.file.LcaiFileUtils;
import com.liucai.core.util.text.TextUtils;

import java.io.File;
import java.util.Objects;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getIntent().getExtras();
        if (savedInstanceState != null) {
            checkType = savedInstanceState.getString(OPEN_KEY);
            resultType = savedInstanceState.getString(RESULT_KEY);
            saveImage = savedInstanceState.getBoolean(SAVE_IMAGE_KEY);
            addHeader = savedInstanceState.getBoolean(ADD_HEADER_KEY);
        }else {
            Objects.requireNonNull(LcaiManager.Internal.getPhotoCameraResult()).onError("获取参数失败");
        }

        ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), this::cameraResult);

        ActivityResultLauncher<String> photoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), this::photoResult);

        if (TextUtils.equals(checkType, LcaiPhotoLib.CHECK_PHOTO)) {
            photoLauncher.launch("image/*");
        }

        if (TextUtils.equals(checkType, LcaiPhotoLib.OPEN_CAMERA)) {
            imageFile = LcaiFileUtils.createCameraImageFile(this);
            Uri imageUri = LcaiFileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", imageFile);
            cameraLauncher.launch(imageUri);
        }
    }

    public void photoResult(Uri callback) {
        if (callback != null) {
            if (TextUtils.equals(resultType, LcaiPhotoLib.BACK_BASE64)) {
                JSONObject jsonObject = LcaiFileUtils.convertImageToBase64(LcaiCamearPhtoActivity.this, callback, false);
                Objects.requireNonNull(LcaiManager.Internal.getPhotoCameraResult()).onBase64(jsonObject.toJSONString());
            }

            if (TextUtils.equals(resultType, LcaiPhotoLib.BACK_URL)) {
                String path=LcaiFileUtils.getPath(LcaiCamearPhtoActivity.this, callback);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("fileType", "image");
                File file = new File(path);
                if (file.exists()) {
                    long fileSize = file.length()/1024;
                    jsonObject.put("fileSize", fileSize);
                }
                jsonObject.put("content", path);
                Objects.requireNonNull(LcaiManager.Internal.getPhotoCameraResult()).onUrl(jsonObject.toJSONString());
            }
        }
        finish();
    }

    public void cameraResult(boolean callback) {
        if (callback) {
            if (TextUtils.equals(resultType, LcaiPhotoLib.BACK_BASE64)) {
                JSONObject jsonObject = LcaiFileUtils.converImagePathToBase64(imageFile.getAbsolutePath(), addHeader);
                Objects.requireNonNull(LcaiManager.Internal.getPhotoCameraResult()).onBase64(jsonObject.toJSONString());
            }

            if (saveImage) {
                LcaiFileUtils.saveImageToGallery(this, imageFile, new LcaiFileInterface() {
                    @Override
                    public void onUriPath(String filePath) {
                        if (TextUtils.equals(resultType, LcaiPhotoLib.BACK_URL)) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("fileType", "image");
                            File file = new File(filePath);
                            if (file.exists()) {
                                long fileSize = file.length()/1024;
                                jsonObject.put("fileSize", fileSize);
                            }
                            jsonObject.put("content", filePath);
                            Objects.requireNonNull(LcaiManager.Internal.getPhotoCameraResult()).onUrl(jsonObject.toJSONString());
                        }

                        if (TextUtils.equals(resultType, LcaiPhotoLib.BACK_BASE64)) {
                            JSONObject jsonObject = LcaiFileUtils.converImagePathToBase64(filePath, addHeader);
                            Objects.requireNonNull(LcaiManager.Internal.getPhotoCameraResult()).onBase64(jsonObject.toJSONString());
                        }
                    }
                });
            } else {
                LcaiFileUtils.clearFile(imageFile);
            }
        } else {
            LcaiFileUtils.clearFile(imageFile);
            Objects.requireNonNull(LcaiManager.Internal.getPhotoCameraResult()).onError("拍摄照片失败");
        }
        finish();
    }
}
