package com.liucai.jsbridge.web;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.liucai.core.LcaiManager;
import com.liucai.core.base.LcaiBaseActivity;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.jsbridge.bridge.LcaiBridgeHandler;
import com.liucai.jsbridge.bridge.LcaiCallbackFunction;
import com.liucai.jsbridge.bridge.LcaiDefaultHandler;
import com.liucai.permission.R;
import com.liucai.permission.bulider.LcaiPermissionRequestBulider;
import com.liucai.permission.core.LcaiReqPermissionResult;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/7
 */
public class WebActivity extends LcaiBaseActivity {
    public static final String BACK_METHOD = "__back";
    public static final String PERMISSION_METHOD = "__permission";
    private WebActivityConfig config;
    private LinearLayout mWebActivityBack;
    private TextView mWebActivityTitleText;
    private LcaiBridgeWebview mWebActivityWebview;
    private Handler handler;
    @Override
    public int getLayout() {
        return R.layout.web_activity_layout;
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public void initView() {
        config = WebActivityConfig.getConfig();
        handler = new Handler(Looper.getMainLooper());
        mWebActivityBack = findViewById(R.id.web_activity_back);
        mWebActivityTitleText = findViewById(R.id.web_activity_title_text);
        mWebActivityWebview = findViewById(R.id.web_activity_webview);

        if (!TextUtils.isEmpty(config.title)) {
            mWebActivityTitleText.setVisibility(VISIBLE);
            mWebActivityTitleText.setText(config.title);
        }

        mWebActivityBack.setOnClickListener(v->{
            verifyMethod(null, BACK_METHOD, "");
        });

        mWebActivityWebview.setDefaultHandler(new LcaiDefaultHandler());

        WebSettings settings = mWebActivityWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setGeolocationEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(false);
            settings.setAllowUniversalAccessFromFileURLs(false);
        }

        mWebActivityWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                LcaiManager.getInstance().permissionReq(new LcaiPermissionRequestBulider()
                        .with(WebActivity.this)
                        .addPermission(config.permissionArray)
                        .check(true)
                        .addResult(new LcaiReqPermissionResult() {
                            @Override
                            public void onReqPermissionPass() {
                                LcaiLogUtils.d("系统权限已授予，直接授权webview");
                                request.grant(request.getResources());
                            }

                            @Override
                            public void onReqPermissionNoPass() {
                                request.deny();
                                LcaiLogUtils.d("系统权限未授权，返回失败");
                                verifyMethod(null, PERMISSION_METHOD, "");
                            }
                        }));
            }
        });

        if (config.methodArrays!=null && config.methodArrays.length > 0) {
            for (String method : config.methodArrays) {
                LcaiLogUtils.d("增加JSBridge方法", method);
                mWebActivityWebview.registerHandler(method, new LcaiBridgeHandler() {
                    @Override
                    public void handler(String data, LcaiCallbackFunction function) {
                        verifyMethod(function,method,data);
                    }
                });
            }
        }

        if (config.callback != null) {
            if (config.callback.createJsMethod() != null && config.callback.createJsMethod().size() > 0) {
                for (JsInterface jsInterface : config.callback.createJsMethod()) {
                    LcaiLogUtils.d("增加Javascript", jsInterface.methodName);
                    mWebActivityWebview.addJavascriptInterface(jsInterface,jsInterface.methodName);
                }
            }
        }

        LcaiLogUtils.d("webview 初始化完成！");
        if (!TextUtils.isEmpty(config.url)) {
            LcaiLogUtils.d("开始加载地址", config.url);
            mWebActivityWebview.loadUrl(config.url);
        }
    }

    private void verifyMethod(LcaiCallbackFunction jsBridgeCallback,String method, String data) {
        LcaiLogUtils.d("处理回调", "method:" + method, "data:", data);
        WebActivityCallback callback = config.getCallback();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", method);
        jsonObject.put("data", data);
        if (callback != null) {
            LcaiLogUtils.d("回调原生数据",jsonObject.toJSONString());
            callback.onMethodBack(jsBridgeCallback,jsonObject,this);
            handler.postDelayed(() -> safeFinish(), 300);
        }
    }

    @Override
    protected void onDestroy() {
        // 清理 WebView
        if (mWebActivityWebview != null) {
            mWebActivityWebview.removeAllViews();
            mWebActivityWebview.destroy();
            mWebActivityWebview = null;
            config.clear();
        }
        super.onDestroy();
    }

    /**
     * 关闭页面
     * 清除webview
     */
    private void safeFinish() {
        LcaiLogUtils.i( "安全退出");
        try {
            destroyWebView();
            finish();
        } catch (Exception e) {
            LcaiLogUtils.e( "安全退出异常: " + e.getMessage(), e);
            finish();
        }
    }

    /**
     * 清除webview
     */
    private void destroyWebView() {
        LcaiLogUtils.i( "开始销毁WebView");
        if (mWebActivityWebview != null) {
            try {
                LcaiLogUtils.i( "加载空白页");
                mWebActivityWebview.loadUrl("about:blank");

                if (mWebActivityWebview.getParent() != null) {
                    ((ViewGroup) mWebActivityWebview.getParent()).removeView(mWebActivityWebview);
                    LcaiLogUtils.i( "从父视图移除WebView");
                }

                mWebActivityWebview.stopLoading();
                mWebActivityWebview.setWebChromeClient(null);
                mWebActivityWebview.setWebViewClient(null);
                mWebActivityWebview.destroy();
                mWebActivityWebview = null;
                LcaiLogUtils.i( "WebView销毁完成");
            } catch (Exception e) {
                LcaiLogUtils.e( "销毁WebView异常: " + e.getMessage(), e);
            }
        } else {
            LcaiLogUtils.w( "WebView为空或未初始化，无需销毁");
        }
    }
}
