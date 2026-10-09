package com.liucai.jsbridge.web;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.jsbridge.bridge.LcaiCallbackFunction;

import java.util.HashSet;
import java.util.Set;

/**
 * JS 方法分发器。
 * @author liucai
 */
public class LcaiJsBridgeDispatcher {

    private static final String TAG = "LcaiJsBridgeDispatcher";

    public interface OnMethodInvokeListener {
        void onInvoke(@Nullable LcaiCallbackFunction callback,
                      @NonNull String method,
                      @Nullable String data);
    }

    private final LcaiBridgeWebview webView;
    private final OnMethodInvokeListener listener;
    private final Set<String> registered = new HashSet<>();

    public LcaiJsBridgeDispatcher(@NonNull LcaiBridgeWebview webView,
                                  @NonNull OnMethodInvokeListener listener) {
        this.webView = webView;
        this.listener = listener;
    }

    public void register(@Nullable String[] methods) {
        if (methods == null) return;
        for (String method : methods) {
            if (TextUtils.isEmpty(method) || registered.contains(method)) continue;
            registered.add(method);
            LcaiLogUtils.d(TAG, "注册 JS 方法:", method);
            webView.registerHandler(method,
                    (data, function) -> listener.onInvoke(function, method, data));
        }
    }

    public void unregisterAll() {
        registered.clear();
    }
}