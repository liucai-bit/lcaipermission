package com.liucai.jsbridge.web;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.liucai.core.base.LcaiBaseActivity;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.jsbridge.bridge.LcaiCallbackFunction;
import com.liucai.jsbridge.bridge.LcaiDefaultHandler;
import com.liucai.permission.R;

import java.util.List;

/**
 * @author liucai
 * @Date 2026/7/7
 */
public class WebActivity extends LcaiBaseActivity {

    private static final String TAG = "WebActivity";
    public static final String BACK_METHOD = "__back";
    public static final String PERMISSION_METHOD = "__permission";

    private WebActivityConfig config;
    private LcaiBridgeWebview webView;
    private LcaiWebCallbackAdapter callbackAdapter;
    private LcaiJsBridgeDispatcher dispatcher;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile boolean destroyed = false;
    private volatile boolean finishing = false;

    @Override
    public int getLayout() {
        return R.layout.web_activity_layout;
    }

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public void initView() {
        // 1. 从 Intent 取 config
        config = getIntent().getParcelableExtra(LcaiWebActivityRouter.EXTRA_CONFIG);
        if (config == null || !config.isValid()) {
            LcaiLogUtils.e(TAG, "WebActivityConfig 为空或无效，直接关闭");
            safeFinish();
            return;
        }
        // 2. 绑定 callback / pageListener（从静态暂存取回）
        config.attachCallback();

        webView = findViewById(R.id.web_activity_webview);
        if (webView == null) {
            LcaiLogUtils.e(TAG, "WebView 未找到，直接关闭");
            safeFinish();
            return;
        }

        // 3. WebView 初始化
        LcaiWebViewInitializer.applyDefaults(webView);
        webView.setDefaultHandler(new LcaiDefaultHandler());
        webView.setWebViewClient(webView.generateBridgeWebViewClient());
        webView.setWebChromeClient(createChromeClient());

        // 4. 回调适配器
        callbackAdapter = new LcaiWebCallbackAdapter(
                config.getCallback() == null ? null :
                        (cb, payload, act) -> config.getCallback()
                                .onMethodBack(cb, payload, act));

        // 5. JS 方法注册
        dispatcher = new LcaiJsBridgeDispatcher(webView,
                (cb, method, data) -> verifyMethod(cb, method, data));
        dispatcher.register(config.getMethodArrays());

        // 6. JS 接口注入
        registerJavascriptInterfaces();

        // 7. UI
        setupTitle();
        setupBackButton();

        // 8. 加载
        LcaiLogUtils.d(TAG, "WebView 初始化完成，开始加载:", config.getUrl());
        webView.loadUrl(config.getUrl());
    }

    private WebChromeClient createChromeClient() {
        return new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                LcaiPermissionBridge.handle(
                        WebActivity.this,
                        request,
                        config == null ? null : config.getPermissionArray(),
                        permissions -> verifyMethod(null, PERMISSION_METHOD, "")
                );
            }

            @Override
            public void onProgressChanged(android.webkit.WebView view, int newProgress) {
                if (config != null && config.getPageListener() != null) {
                    config.getPageListener().onProgressChanged(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(android.webkit.WebView view, String title) {
                if (config != null && config.getPageListener() != null) {
                    config.getPageListener().onTitleReceived(title);
                }
            }
        };
    }

    @SuppressLint("JavascriptInterface")
    private void registerJavascriptInterfaces() {
        if (config.getCallback() == null) return;
        List<JsInterface> jsInterfaces = config.getCallback().createJsMethod();
        if (jsInterfaces == null || jsInterfaces.isEmpty()) return;
        for (JsInterface js : jsInterfaces) {
            if (js == null || TextUtils.isEmpty(js.methodName)) continue;
            LcaiLogUtils.d(TAG, "增加 JavascriptInterface:", js.methodName);
            webView.addJavascriptInterface(js, js.methodName);
        }
    }

    private void setupTitle() {
        TextView titleView = findViewById(R.id.web_activity_title_text);
        if (titleView == null) return;
        if (!config.isShowTitleBar()) {
            titleView.setVisibility(GONE);
            return;
        }
        if (!TextUtils.isEmpty(config.getTitle())) {
            titleView.setVisibility(VISIBLE);
            titleView.setText(config.getTitle());
        }
    }

    private void setupBackButton() {
        View back = findViewById(R.id.web_activity_back);
        if (back == null) return;
        if (!config.isShowBackButton()) {
            back.setVisibility(GONE);
            return;
        }
        back.setOnClickListener(v -> verifyMethod(null, BACK_METHOD, ""));
    }

    private void verifyMethod(@Nullable LcaiCallbackFunction cb,
                              @Nullable String method,
                              @Nullable String data) {
        if (callbackAdapter == null) {
            LcaiLogUtils.w(TAG, "callbackAdapter 为空，直接关闭");
            safeFinish();
            return;
        }
        callbackAdapter.dispatch(cb, method, data, this, this::safeFinish);
    }

    private void safeFinish() {
        if (finishing || isFinishing()) return;
        finishing = true;
        LcaiLogUtils.i(TAG, "安全退出");
        destroyWebView();
        finish();
    }

    private void destroyWebView() {
        if (destroyed) return;
        destroyed = true;
        LcaiWebViewLifecycle.safeDestroy(webView);
        webView = null;
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        if (callbackAdapter != null) {
            callbackAdapter.release();
            callbackAdapter = null;
        }
        if (dispatcher != null) {
            dispatcher.unregisterAll();
            dispatcher = null;
        }
        destroyWebView();
        if (config != null) {
            config.clear();
            config = null;
        }
        super.onDestroy();
    }
}