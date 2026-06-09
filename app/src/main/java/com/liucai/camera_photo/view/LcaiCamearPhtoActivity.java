package com.liucai.camera_photo.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.liucai.camera_photo.core.LcaiPhotoLib;
import com.liucai.camera_photo.core.LcaiPhotoResult;
import com.liucai.core.LcaiManager;
import com.liucai.core.base.LcaiBasePermissionActivity;
import com.liucai.core.util.file.LcaiFileInterface;
import com.liucai.core.util.file.LcaiFileProvider;
import com.liucai.core.util.file.LcaiFileUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.http.thread.GlobalThreadPool;
import com.liucai.http.thread.LcaiRunnableUtils;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @author LIUCAI
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

    private Uri imageUri;

    private ActivityResultLauncher<Uri> cameraLauncher;

    private ActivityResultLauncher<String> photoLauncher;

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
            LcaiManager.Internal.getPhotoCameraResult().onError("获取参数失败");
        }

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), callback -> {
            cameraResult(callback);
        });

        photoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), callback -> {
            photoResult(callback);
        });

        if (TextUtils.equals(checkType, LcaiPhotoLib.CHECK_PHOTO)) {
            photoLauncher.launch("image/*");
        }

        if (TextUtils.equals(checkType, LcaiPhotoLib.OPEN_CAMERA)) {
            imageFile = LcaiFileUtils.createCameraImageFile(this);
            imageUri = LcaiFileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", imageFile);
            cameraLauncher.launch(imageUri);
        }
    }

    public void photoResult(Uri callback) {
        if (callback != null) {
            if (TextUtils.equals(resultType, LcaiPhotoLib.BACK_BASE64)) {
                JSONObject jsonObject = LcaiFileUtils.convertImageToBase64(LcaiCamearPhtoActivity.this, callback, false);
                LcaiManager.Internal.getPhotoCameraResult().onBase64(jsonObject.toJSONString());
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
                LcaiManager.Internal.getPhotoCameraResult().onUrl(jsonObject.toJSONString());
            }
        }
        finish();
    }

    public void cameraResult(boolean callback) {
        if (callback) {
            if (TextUtils.equals(resultType, LcaiPhotoLib.BACK_BASE64)) {
                JSONObject jsonObject = LcaiFileUtils.converImagePathToBase64(imageFile.getAbsolutePath(), addHeader);
                LcaiManager.Internal.getPhotoCameraResult().onBase64(jsonObject.toJSONString());
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
                            LcaiManager.Internal.getPhotoCameraResult().onUrl(jsonObject.toJSONString());
                        }

                        if (TextUtils.equals(resultType, LcaiPhotoLib.BACK_BASE64)) {
                            JSONObject jsonObject = LcaiFileUtils.converImagePathToBase64(filePath, addHeader);
                            LcaiManager.Internal.getPhotoCameraResult().onBase64(jsonObject.toJSONString());
                        }
                    }
                });
            } else {
                LcaiFileUtils.clearFile(imageFile);
            }
        } else {
            LcaiFileUtils.clearFile(imageFile);
            LcaiManager.Internal.getPhotoCameraResult().onError("拍摄照片失败");
        }
        finish();
    }
}
