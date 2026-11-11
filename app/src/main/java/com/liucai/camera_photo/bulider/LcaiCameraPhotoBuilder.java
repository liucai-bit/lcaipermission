package com.liucai.camera_photo.bulider;

import com.liucai.camera_photo.core.LcaiGalleryCheckType;
import com.liucai.camera_photo.core.LcaiGalleryResult;
import com.liucai.camera_photo.core.LcaiGalleryResultType;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/4
 */
@SuppressWarnings("unused")
public class LcaiCameraPhotoBuilder {

    public LcaiGalleryCheckType type = LcaiGalleryCheckType.PHOTO;

    public LcaiCameraPhotoBuilder addCheck(LcaiGalleryCheckType type) {
        this.type = type;
        return this;
    }

    public LcaiGalleryResultType resultTyp = LcaiGalleryResultType.URL;

    public LcaiCameraPhotoBuilder addResult(LcaiGalleryResultType resultTyp) {
        this.resultTyp = resultTyp;
        return this;
    }

    public boolean addHeader;

    public LcaiCameraPhotoBuilder setAddHeader(boolean addHeader) {
        this.addHeader = addHeader;
        return this;
    }

    // 保留原来的写法，兼容老代码
    public LcaiCameraPhotoBuilder addHeader() {
        this.addHeader = true;
        return this;
    }

    public boolean save;

    public LcaiCameraPhotoBuilder setSave(boolean save) {
        this.save = save;
        return this;
    }

    public LcaiCameraPhotoBuilder save() {
        this.save = true;
        return this;
    }

    public LcaiGalleryResult result;

    public LcaiCameraPhotoBuilder setResult(LcaiGalleryResult result) {
        this.result = result;
        return this;
    }
}
