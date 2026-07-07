package com.liucai.jsbridge.web;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.liucai.jsbridge.bridge.LcaiCallbackFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/7/7
 */
public interface WebActivityCallback {

    default List<JsInterface> createJsMethod() {
        return new ArrayList<>();
    }

    default void onMethodBack(LcaiCallbackFunction jsBridgeCallback, JSONObject result, Context mContext) {
    }
}
