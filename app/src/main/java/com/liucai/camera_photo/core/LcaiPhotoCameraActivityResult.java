package com.liucai.camera_photo.core;

/**
 * @author LIUCAI
 * @program lcpermission
 * @description
 * @Date 2026/6/5
 */
public interface LcaiPhotoCameraActivityResult {
    default void onUrl(String url) {

    }

    default void onBase64(String base64) {

    }

    void onError(String error);
}
