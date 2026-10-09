package com.liucai.jsbridge.web;

import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author liucai
 * @Date 2026/5/28
 */
public class LcaiBridgeUtil {

    private static final String TAG = "LcaiBridgeUtil";

    final static String YY_OVERRIDE_SCHEMA = "yy://";
    final static String YY_RETURN_DATA = YY_OVERRIDE_SCHEMA + "return/";
    final static String YY_FETCH_QUEUE = YY_RETURN_DATA + "_fetchQueue/";
    final static String EMPTY_STR = "";
    final static String UNDERLINE_STR = "_";
    final static String SPLIT_MARK = "/";

    final static String CALLBACK_ID_FORMAT = "JAVA_CB_%s";
    final static String JS_HANDLE_MESSAGE_FROM_JAVA =
            "javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');";
    final static String JS_FETCH_QUEUE_FROM_JAVA =
            "javascript:WebViewJavascriptBridge._fetchQueue();";
    public final static String JAVASCRIPT_STR = "javascript:";


    public final static String JS_CHECK_BRIDGE_EXIST =
            "(function(){" +
                    "  try {" +
                    "    return typeof window.WebViewJavascriptBridge !== 'undefined'" +
                    "        && window.WebViewJavascriptBridge !== null;" +
                    "  } catch(e) {" +
                    "    return false;" +
                    "  }" +
                    "})()";

    public final static String JS_CHECK_BRIDGE_VERSION =
            "(function(){" +
                    "  try {" +
                    "    var b = window.WebViewJavascriptBridge;" +
                    "    if (!b) return 'none';" +
                    "    return b.__version__ || 'unknown';" +
                    "  } catch(e) {" +
                    "    return 'error';" +
                    "  }" +
                    "})()";

    public final static String JS_CHECK_BRIDGE_LOADED_FLAG =
            "(function(){" +
                    "  try {" +
                    "    return typeof window.__LCAI_BRIDGE_LOADED__ !== 'undefined'" +
                    "        && window.__LCAI_BRIDGE_LOADED__ === true;" +
                    "  } catch(e) {" +
                    "    return false;" +
                    "  }" +
                    "})()";

    public final static String JS_CHECK_BRIDGE_SCRIPT_END =
            "(function(){" +
                    "  try {" +
                    "    return typeof window.__LCAI_BRIDGE_SCRIPT_END__ !== 'undefined'" +
                    "        && window.__LCAI_BRIDGE_SCRIPT_END__ === true;" +
                    "  } catch(e) {" +
                    "    return false;" +
                    "  }" +
                    "})()";


    public static String parseFunctionName(String jsUrl) {
        return jsUrl.replace("javascript:WebViewJavascriptBridge.", "")
                .replaceAll("\\(.*\\);", "");
    }

    public static String getDataFromReturnUrl(String url) {
        if (url.startsWith(YY_FETCH_QUEUE)) {
            return url.replace(YY_FETCH_QUEUE, EMPTY_STR);
        }
        String temp = url.replace(YY_RETURN_DATA, EMPTY_STR);
        String[] functionAndData = temp.split(SPLIT_MARK);
        if (functionAndData.length >= 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < functionAndData.length; i++) {
                sb.append(functionAndData[i]);
            }
            return sb.toString();
        }
        return null;
    }

    public static String getFunctionFromReturnUrl(String url) {
        String temp = url.replace(YY_RETURN_DATA, EMPTY_STR);
        String[] functionAndData = temp.split(SPLIT_MARK);
        if (functionAndData.length >= 1) {
            return functionAndData[0];
        }
        return null;
    }

    public static void webViewLoadJs(WebView view, String url) {
        String js = "var newscript = document.createElement(\"script\");";
        js += "newscript.src=\"" + url + "\";";
        js += "document.scripts[0].parentNode.insertBefore(newscript,document.scripts[0]);";
        view.loadUrl("javascript:" + js);
    }

    /**
     * 加载本地 JS。
     */
    public static void webViewLoadLocalJs(WebView view, String path) {
        String jsContent = assetFile2Str(view.getContext(), path);
        if (TextUtils.isEmpty(jsContent)) {
            LcaiLogUtils.e(TAG, "加载本地 JS 失败: " + path);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(jsContent, value -> {
                LcaiLogUtils.d(TAG, "本地 JS 注入完成: " + path + ", result=" + value);
                // 立即检测
                view.evaluateJavascript("typeof window.WebViewJavascriptBridge",
                        v -> LcaiLogUtils.d(TAG, "检测 bridge typeof: " + v));
            });
        } else {
            view.loadUrl("javascript:" + jsContent);
        }
    }

    public static void evaluateJavascript(@NonNull WebView webView,
                                          @NonNull String script,
                                          @Nullable EvaluateCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(script, value -> {
                if (callback != null) callback.onResult(value);
            });
        } else {
            webView.loadUrl("javascript:" + script);
            if (callback != null) callback.onResult(null);
        }
    }

    public interface EvaluateCallback {
        void onResult(@Nullable String value);
    }

    /**
     * 读取 assets 文件（字节流一次性读，不丢字符）。
     */
    public static String assetFile2Str(Context c, String urlStr) {
        InputStream in = null;
        try {
            in = c.getAssets().open(urlStr);
            int size = in.available();
            byte[] buffer = new byte[size];
            int total = 0;
            int read;
            while (total < size && (read = in.read(buffer, total, size - total)) > 0) {
                total += read;
            }
            if (total <= 0) return null;

            String content = new String(buffer, 0, total, StandardCharsets.UTF_8);

            // 去掉 UTF-8 BOM
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }
            // 替换危险的 Unicode 行分隔符
            content = content.replace("\u2028", "\\u2028")
                    .replace("\u2029", "\\u2029");

            return content;
        } catch (Exception e) {
            LcaiLogUtils.e(TAG, "读取 asset 失败: " + urlStr + ", " + e.getMessage());
        } finally {
            if (in != null) {
                try { in.close(); } catch (IOException ignored) {}
            }
        }
        return null;
    }
}