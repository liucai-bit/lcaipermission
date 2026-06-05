package com.liucai.camera_photo.core;

import android.content.Intent;
import android.os.Bundle;

import com.liucai.camera_photo.bulider.LcaiCameraPhotoBulider;
import com.liucai.camera_photo.view.LcaiCamearPhtoActivity;

/**
 * @author LIUCAI
 * @program lcpermission
 * @description
 * @Date 2026/6/4
 */
public class LcaiCameraPhoto {

    public LcaiCameraPhotoBulider bulider;

    public LcaiCameraPhoto(LcaiCameraPhotoBulider bulider) {
        this.bulider = bulider;
        init();
    }

    private void init() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(LcaiCamearPhtoActivity.OPEN_KEY, bulider.checkType);
        bundle.putString(LcaiCamearPhtoActivity.RESULT_KEY, bulider.resultTyp);
        bundle.putBoolean(LcaiCamearPhtoActivity.SAVE_IMAGE_KEY,bulider.save);
        bundle.putBoolean(LcaiCamearPhtoActivity.ADD_HEADER_KEY, bulider.addHeader);
        intent.putExtras(bundle);
        intent.setClass(bulider.mContext,LcaiCamearPhtoActivity.class);
        bulider.mContext.startActivity(intent);

    }
}
