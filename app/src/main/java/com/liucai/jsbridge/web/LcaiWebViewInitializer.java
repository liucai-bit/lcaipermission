package com.liucai.jsbridge.web;

import android.webkit.WebSettings;

import androidx.annotation.NonNull;

/**
 * WebView 初始化。
 * @author liucai
 */
public final class LcaiWebViewInitializer {

    private LcaiWebViewInitializer() {
    }

    public static void applyDefaults(@NonNull LcaiBridgeWebview webView) {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setGeolocationEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
    }

    /**
     * 允许业务方覆盖部分配置。
     */
    public static void apply(@NonNull LcaiBridgeWebview webView,
                             @NonNull WebSettingsApplier applier) {
        applyDefaults(webView);
        applier.apply(webView.getSettings());
    }

    /** 配置覆盖接口 */
    public interface WebSettingsApplier {
        void apply(WebSettings settings);
    }
}