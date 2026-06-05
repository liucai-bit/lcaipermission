package com.liucai.http.base;

import com.liucai.http.bulider.LcaiHttpRequestBulider;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author LIUCAI
 * @program lcpermission
 * @description
 * @Date 2026/6/5
 */
public interface LcaiBaseResponseResult {
    /**
     * 请求失败回调
     * @param code
     * @param msg
     */
    default void error(int code, String msg) {

    }

    /**
     * 请求成功回调
     * @param result
     */
    default void success(String result) {

    }

    /**
     * 请求成功回调
     * @param headers
     * @param result
     */
    default void success(Map<String, List<String>> headers, String result) {

    }

    /**
     * 请求成功回调
     * @param inputStream
     */
    default void success(InputStream inputStream) {

    }

    /**
     * 包含请求头
     * 请求成功回调
     * @param headers 请求头
     * @param inputStream
     */
    default void success(Map<String, List<String>> headers, InputStream inputStream) {

    }

    /**
     * 请求成功回调
     * @param progress
     * @param finish
     * @param bulider
     */
    default void success(int progress, boolean finish, LcaiHttpRequestBulider bulider) {

    }

    /**
     * 取消请求
     * @return
     */
    default boolean cancelRequest() {
        return false;
    }
}
