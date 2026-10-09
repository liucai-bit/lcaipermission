package com.liucai.jsbridge.web;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.log.LcaiLogUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Bridge WebViewClient。
 *
 * @author liucai
 * @Date 2026/5/28
 */
public class LcaiBridgeWebViewClient extends WebViewClient {

    private static final String TAG = "LcaiBridgeWebViewClient";

    @Nullable
    private String lastInjectedUrl;

    private final LcaiBridgeWebview webView;

    public LcaiBridgeWebViewClient(@NonNull LcaiBridgeWebview webView) {
        this.webView = webView;
    }

    // ======================== 旧 API（< 24） ========================

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        printLog( "shouldOverrideUrlLoading(old): " + url);
        return handleUrl(url);
    }

    // ======================== 新 API（>= 24） ========================

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = request.getUrl();
            String url = uri == null ? null : uri.toString();
            printLog( "shouldOverrideUrlLoading(new): " + url);
            return handleUrl(url);
        }
        return super.shouldOverrideUrlLoading(view, request);
    }

    private boolean handleUrl(@Nullable String url) {
        if (url == null) return false;

        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            printLog( "URL 解码失败: " + e.getMessage());
        }

        if (url.startsWith(LcaiBridgeUtil.YY_RETURN_DATA)) {
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(LcaiBridgeUtil.YY_OVERRIDE_SCHEMA)) {
            webView.flushMessageQueue();
            return true;
        }
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        printLog("onPageStarted: " + url);
        if (url != null && !url.equals(lastInjectedUrl)) {
            webView.setBridgeInjected(false);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        printLog( "onPageFinished: " + url);

        if (url != null && url.equals(lastInjectedUrl)) {
            printLog( "同一 URL 已处理过，跳过: " + url);
            webView.flushStartupMessages();
            return;
        }
        lastInjectedUrl = url;

        view.postDelayed(() -> detectAndInject(view), 100);
    }

    private void detectAndInject(@NonNull final WebView view) {
        if (LcaiBridgeWebview.toLoadJs == null) {
            printLog( "toLoadJs 为 null，跳过注入");
            webView.setBridgeInjected(true);
            webView.flushStartupMessages();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(LcaiBridgeUtil.JS_CHECK_BRIDGE_EXIST, value -> {
                boolean exists = "true".equals(value);
                if (exists) {
                    printLog( "JSBridge 已存在，跳过注入");
                    webView.setBridgeInjected(true);
                    webView.flushStartupMessages();
                } else {
                    printLog("JSBridge 不存在，开始注入");
                    injectBridge(view);
                }
            });
        } else {
            injectBridge(view);
            webView.setBridgeInjected(true);
            webView.flushStartupMessages();
        }
    }

    private void injectBridge(@NonNull final WebView view) {
        LcaiBridgeUtil.webViewLoadLocalJs(view, LcaiBridgeWebview.toLoadJs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.postDelayed(() -> view.evaluateJavascript(
                    LcaiBridgeUtil.JS_CHECK_BRIDGE_EXIST, value -> {
                        boolean ok = "true".equals(value);
                        webView.setBridgeInjected(ok);
                        printLog("注入校验结果: " + ok);

                        if (!ok) {
                            view.evaluateJavascript(
                                    LcaiBridgeUtil.JS_CHECK_BRIDGE_LOADED_FLAG,
                                    v1 ->   printLog( "LOADED 标记: " + v1));
                            view.evaluateJavascript(
                                    LcaiBridgeUtil.JS_CHECK_BRIDGE_SCRIPT_END,
                                    v2 ->   printLog( "SCRIPT_END 标记: " + v2));
                            view.evaluateJavascript("location.href",
                                    v3 ->   printLog("当前 location.href: " + v3));
                        }

                        webView.flushStartupMessages();
                    }), 100);
        } else {
            webView.setBridgeInjected(true);
            webView.flushStartupMessages();
        }
    }

    private void printLog(String message) {
        if (webView.getPrintLog()) {
            LcaiLogUtils.i(message);
        }
    }
}