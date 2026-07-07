package com.liucai.jsbridge.bridge;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/28
 */
public interface LcaiWebViewJavascriptBridge {
    void send(String data);

    void send(String data, LcaiCallbackFunction function);
}
