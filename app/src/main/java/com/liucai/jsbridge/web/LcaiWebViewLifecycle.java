package com.liucai.jsbridge.web;

import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import com.liucai.core.util.log.LcaiLogUtils;

/**
 * WebView 销毁工具。
 *
 * @author liucai
 */
public final class LcaiWebViewLifecycle {

    private static final String TAG = "LcaiWebViewLifecycle";

    private LcaiWebViewLifecycle() {
    }

    public static void safeDestroy(@Nullable WebView webView) {
        if (webView == null) {
            LcaiLogUtils.w(TAG, "WebView 为空，无需销毁");
            return;
        }
        try {
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            if (webView.getParent() instanceof ViewGroup) {
                ((ViewGroup) webView.getParent()).removeView(webView);
            }
            webView.removeAllViews();
            webView.destroy();
            LcaiLogUtils.i(TAG, "WebView 销毁完成");
        } catch (Exception e) {
            LcaiLogUtils.e(TAG, "销毁 WebView 异常: " + e.getMessage(), e);
        }
    }
}