package com.liucai.camera_photo.bulider;

import android.content.Context;

import com.liucai.camera_photo.core.LcaiCameraPhoto;
import com.liucai.camera_photo.core.LcaiPhotoLib;
import com.liucai.camera_photo.core.LcaiPhotoResult;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/4
 */
public class LcaiCameraPhotoBulider {

    /**
     * 上下文对象
     */
    public Context mContext;

    public LcaiCameraPhotoBulider with(Context mContext) {
        this.mContext = mContext;
        return this;
    }

    /**
     *选择照片类型
     * 默认跳转相册选择
     */
    public String checkType = LcaiPhotoLib.CHECK_PHOTO;

    public LcaiCameraPhotoBulider setCheckType(String checkType) {
        this.checkType = checkType;
        return this;
    }

    /**
     * 返回类型
     * 默认返回url
     */
    public String resultTyp = LcaiPhotoLib.BACK_URL;

    public LcaiCameraPhotoBulider setResultType(String resultTyp) {
        this.resultTyp = resultTyp;
        return this;
    }

    /**
     * 是否添加头部
     * 返回类型为base64时使用
     */
    public boolean addHeader;

    public LcaiCameraPhotoBulider addHeader() {
        this.addHeader = true;
        return this;
    }

    /**
     * 是否保存照片
     * 拍照时使用
     */
    public boolean save;

    public LcaiCameraPhotoBulider save() {
        this.save = true;
        return this;
    }

    /**
     * 返回回调
     */
    public LcaiPhotoResult result;

    public LcaiCameraPhotoBulider setResult(LcaiPhotoResult result) {
        this.result = result;
        return this;
    }



}
