package com.liucai.camera_photo.core;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/4
 */
public interface LcaiGalleryResult {
    /**返回文件地址*/
    default void onUrl(String url) {}
    /**返回文件BASE64数据 */
    default void onBase64(String base64) {}
    /**用户主动取消*/
    default void onCancel(){}
    /**错误回调*/
    void onError(String error);
}
