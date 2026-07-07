package com.liucai.jsbridge.bridge;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/28
 */
public class LcaiDefaultHandler implements LcaiBridgeHandler {
    @Override
    public void handler(String data, LcaiCallbackFunction function) {
        if (function != null) {
            function.onCallback("LcaiFunction response data");
        }
    }
}
