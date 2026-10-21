package com.liucai.camera_photo.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.liucai.camera_photo.bulider.LcaiCameraPhotoBulider;
import com.liucai.camera_photo.view.LcaiCamearPhtoActivity;
import com.liucai.core.base.LcaiActivityManager;
import com.liucai.core.util.log.LcaiLogUtils;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/4
 */
public class LcaiCameraPhoto {

    public LcaiCameraPhotoBulider builder;

    public LcaiCameraPhoto(LcaiCameraPhotoBulider builder) {
        this.builder = builder;
    }

    public void start() {
        Activity activity = LcaiActivityManager.getInstance().getResumedActivity();
        if (activity == null) {
            LcaiLogUtils.d("LcaiCameraPhoto start failed: context is null");
            return;
        }

        if (activity.isFinishing() || activity.isDestroyed()) {
            LcaiLogUtils.d("LcaiCameraPhoto start failed: activity is finishing or destroyed");
            return;
        }
        Intent intent = new Intent(activity, LcaiCamearPhtoActivity.class);
        intent.putExtra(LcaiCamearPhtoActivity.OPEN_KEY, builder.type.name());
        intent.putExtra(LcaiCamearPhtoActivity.RESULT_KEY, builder.resultTyp.name());
        intent.putExtra(LcaiCamearPhtoActivity.SAVE_IMAGE_KEY, builder.save);
        intent.putExtra(LcaiCamearPhtoActivity.ADD_HEADER_KEY, builder.addHeader);
        activity.startActivity(intent);
    }
}