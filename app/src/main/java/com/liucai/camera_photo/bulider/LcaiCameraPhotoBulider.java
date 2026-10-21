package com.liucai.camera_photo.bulider;

import android.content.Context;

import com.liucai.camera_photo.core.LcaiGalleryResult;
import com.liucai.camera_photo.core.LcaiGalleryCheckType;
import com.liucai.camera_photo.core.LcaiGalleryResultType;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/4
 */
public class LcaiCameraPhotoBulider {

    public LcaiGalleryCheckType type = LcaiGalleryCheckType.PHOTO;

    public LcaiCameraPhotoBulider addCheck(LcaiGalleryCheckType type) {
        this.type = type;
        return this;
    }

    public LcaiGalleryResultType resultTyp = LcaiGalleryResultType.URL;

    public LcaiCameraPhotoBulider addResult(LcaiGalleryResultType resultTyp) {
        this.resultTyp = resultTyp;
        return this;
    }

    public boolean addHeader;

    public LcaiCameraPhotoBulider setAddHeader(boolean addHeader) {
        this.addHeader = addHeader;
        return this;
    }

    // 保留原来的写法，兼容老代码
    public LcaiCameraPhotoBulider addHeader() {
        this.addHeader = true;
        return this;
    }

    public boolean save;

    public LcaiCameraPhotoBulider setSave(boolean save) {
        this.save = save;
        return this;
    }

    public LcaiCameraPhotoBulider save() {
        this.save = true;
        return this;
    }

    public LcaiGalleryResult result;

    public LcaiCameraPhotoBulider setResult(LcaiGalleryResult result) {
        this.result = result;
        return this;
    }
}
