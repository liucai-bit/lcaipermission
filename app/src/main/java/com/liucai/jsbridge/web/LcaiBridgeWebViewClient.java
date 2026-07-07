package com.liucai.jsbridge.web;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.liucai.jsbridge.bridge.LcaiMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/28
 */
public class LcaiBridgeWebViewClient extends WebViewClient {
    private LcaiBridgeWebview webView;

    public LcaiBridgeWebViewClient(LcaiBridgeWebview webView) {
        this.webView = webView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url.startsWith(LcaiBridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(LcaiBridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            webView.flushMessageQueue();
            return true;
        } else {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (LcaiBridgeWebview.toLoadJs != null) {
            LcaiBridgeUtil.webViewLoadLocalJs(view, LcaiBridgeWebview.toLoadJs);
        }

        //
        if (webView.getStartupMessage() != null) {
            for (LcaiMessage m : webView.getStartupMessage()) {
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

}
