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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.base.LcaiBaseActivity;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.jsbridge.bridge.LcaiCallbackFunction;
import com.liucai.jsbridge.bridge.LcaiDefaultHandler;
import com.liucai.permission.R;

import java.util.List;

/**
 * WebView 容器页。
 *
 * <p>可继承扩展：
 * <ul>
 *     <li>重写 {@link #getLayout()} 换布局</li>
 *     <li>重写 {@link #findWebView()} / {@link #findTitleView()} / {@link #findBackView()} 换 id</li>
 *     <li>重写 {@link #setupWebView} / {@link #setupCallbackAdapter} / {@link #setupJsBridge} 定制初始化</li>
 *     <li>重写 {@link #onBeforeLoadUrl} / {@link #onWebViewReady} / {@link #onPageLoaded} 加钩子</li>
 * </ul>
 *
 * @author liucai
 * @Date 2026/7/7
 */
public class WebActivity extends LcaiBaseActivity {

    protected static final String TAG = "WebActivity";
    public static final String BACK_METHOD = "__back";
    public static final String PERMISSION_METHOD = "__permission";

    protected WebActivityConfig config;
    protected LcaiBridgeWebview webView;
    protected LcaiWebCallbackAdapter callbackAdapter;
    protected LcaiJsBridgeDispatcher dispatcher;

    protected final Handler mainHandler = new Handler(Looper.getMainLooper());
    protected volatile boolean destroyed = false;
    protected volatile boolean finishing = false;


    @Override
    public int getLayout() {
        return R.layout.web_activity_layout;
    }

    /**
     * 查找 WebView。子类换布局时重写此方法。
     */
    @Nullable
    protected LcaiBridgeWebview findWebView() {
        return findViewById(R.id.web_activity_webview);
    }

    /**
     * 查找标题 View。子类换布局时重写此方法。
     */
    @Nullable
    protected View findTitleView() {
        return findViewById(R.id.web_activity_title_text);
    }

    /**
     * 查找返回按钮。子类换布局时重写此方法。
     */
    @Nullable
    protected View findBackView() {
        return findViewById(R.id.web_activity_back);
    }

    // ======================== 入口 ========================

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public void initView() {
        // 1. 解析 config
        if (!prepareConfig()) {
            return;
        }

        // 2. 查找 WebView
        webView = findWebView();
        if (webView == null) {
            LcaiLogUtils.e(TAG, "WebView 未找到，直接关闭");
            safeFinish();
            return;
        }

        // 3. 初始化各组件（子类可重写）
        setupWebView(webView);
        webView.setWebChromeClient(createChromeClient());
        setupCallbackAdapter();
        setupJsBridge();
        registerJavascriptInterfaces();
        setupTitle();
        setupBackButton();

        // 4. 加载 URL
        String url = onBeforeLoadUrl(config.getUrl());
        LcaiLogUtils.d(TAG, "WebView 初始化完成，开始加载:", url);
        if (!TextUtils.isEmpty(url)) {
            webView.loadUrl(url);
        }

        // 5. WebView 就绪钩子
        onWebViewReady(webView);
    }

    /**
     * 解析并校验 config。
     *
     * @return true 表示 config 有效
     */
    protected boolean prepareConfig() {
        config = getIntent().getParcelableExtra(LcaiWebActivityRouter.EXTRA_CONFIG);
        if (config == null || !config.isValid()) {
            LcaiLogUtils.e(TAG, "WebActivityConfig 为空或无效，直接关闭");
            safeFinish();
            return false;
        }
        config.attachCallback();
        return true;
    }

    /**
     * WebView 初始化（设置、默认 handler、WebViewClient）。
     */
    protected void setupWebView(@NonNull LcaiBridgeWebview webView) {
        LcaiWebViewInitializer.applyDefaults(webView);
        webView.setDefaultHandler(new LcaiDefaultHandler());
        webView.setWebViewClient(webView.generateBridgeWebViewClient());
    }

    /**
     * 创建 WebChromeClient（处理权限、进度、标题）。
     */
    @NonNull
    protected WebChromeClient createChromeClient() {
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

    /**
     * 创建回调适配器。
     */
    protected void setupCallbackAdapter() {
        final WebActivityCallback finalCb = config.getCallback();
        if (finalCb != null) {
            callbackAdapter = new LcaiWebCallbackAdapter(
                    (cb, payload, activity) -> finalCb.onMethodBack(cb, payload, activity));
        } else {
            callbackAdapter = new LcaiWebCallbackAdapter(null);
        }
    }

    /**
     * 注册 JS 方法分发器。
     */
    protected void setupJsBridge() {
        dispatcher = new LcaiJsBridgeDispatcher(webView,
                (cb, method, data) -> verifyMethod(cb, method, data));
        dispatcher.register(config.getMethodArrays());
    }

    /**
     * 注入 addJavascriptInterface 对象。
     */
    @SuppressLint("JavascriptInterface")
    protected void registerJavascriptInterfaces() {
        if (config.getCallback() == null) return;
        List<JsInterface> jsInterfaces = config.getCallback().createJsMethod();
        if (jsInterfaces == null || jsInterfaces.isEmpty()) return;
        for (JsInterface js : jsInterfaces) {
            if (js == null || TextUtils.isEmpty(js.methodName)) continue;
            LcaiLogUtils.d(TAG, "增加 JavascriptInterface:", js.methodName);
            webView.addJavascriptInterface(js, js.methodName);
        }
    }

    /**
     * 设置标题栏。
     */
    protected void setupTitle() {
        View titleView = findTitleView();
        if (!(titleView instanceof TextView)) return;

        TextView tv = (TextView) titleView;
        if (!config.isShowTitleBar()) {
            tv.setVisibility(GONE);
            return;
        }
        if (!TextUtils.isEmpty(config.getTitle())) {
            tv.setVisibility(VISIBLE);
            tv.setText(config.getTitle());
        }
    }

    /**
     * 设置返回按钮。
     */
    protected void setupBackButton() {
        View back = findBackView();
        if (back == null) return;
        if (!config.isShowBackButton()) {
            back.setVisibility(GONE);
            return;
        }
        back.setOnClickListener(v -> verifyMethod(null, BACK_METHOD, ""));
    }

    /**
     * 加载 URL 前的钩子，可修改 URL。
     */
    @NonNull
    protected String onBeforeLoadUrl(@NonNull String url) {
        return url;
    }

    /**
     * WebView 就绪钩子（初始化完成，但 URL 可能还没加载完）。
     */
    protected void onWebViewReady(@NonNull LcaiBridgeWebview webView) {
        // 默认空实现
    }

    /**
     * 页面加载完成钩子（由子类重写 WebViewClient 触发，可选）。
     */
    protected void onPageLoaded(@NonNull String url) {
        // 默认空实现
    }

    /**
     * 触发 JS 方法回调。
     */
    protected void verifyMethod(@Nullable LcaiCallbackFunction cb,
                                @Nullable String method,
                                @Nullable String data) {
        if (callbackAdapter == null) {
            LcaiLogUtils.w(TAG, "callbackAdapter 为空，直接关闭");
            safeFinish();
            return;
        }
        callbackAdapter.dispatch(cb, method, data, this, this::safeFinish);
    }

    /**
     * 安全关闭页面（幂等）。
     */
    protected void safeFinish() {
        if (finishing || isFinishing()) return;
        finishing = true;
        LcaiLogUtils.i(TAG, "安全退出");
        destroyWebView();
        finish();
    }

    /**
     * 销毁 WebView（幂等）。
     */
    protected void destroyWebView() {
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


    /**
     * 获取 WebView（子类或外部可访问）。
     */
    @Nullable
    public LcaiBridgeWebview getWebView() {
        return webView;
    }

    /**
     * 获取配置。
     */
    @Nullable
    public WebActivityConfig getConfig() {
        return config;
    }
}