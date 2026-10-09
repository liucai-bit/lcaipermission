package com.liucai.jsbridge.web;

import android.content.Context;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.liucai.jsbridge.bridge.LcaiCallbackFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liucai
 */
public interface WebActivityCallback {

    /** 注入到 H5 的 JS 接口对象 */
    default List<JsInterface> createJsMethod() {
        return new ArrayList<>();
    }

    /**
     * JS 方法回调。
     * @return true 表示处理完后自动关闭页面；false 表示保持页面
     */
    default boolean onMethodBack(@Nullable LcaiCallbackFunction jsBridgeCallback,
                                 JSONObject result,
                                 Context context) {
        return false;
    }
}