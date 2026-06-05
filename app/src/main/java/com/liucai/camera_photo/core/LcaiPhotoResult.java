package com.liucai.camera_photo.core;

/**
 * @author LIUCAI
 * @program lcpermission
 * @description
 * @Date 2026/6/4
 */
public interface LcaiPhotoResult {
    /**
     * 返回文件地址
     * @param url
     */
    default void onUrl(String url) {

    }

    /**
     * 返回文件BASE64数据
     * @param base64
     */
    default void onBase64(String base64) {

    }

    /**
     * 错误回调
     * @param error
     */
    void onError(String error);
}
