package com.liucai.jsbridge.web;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.jsbridge.bridge.LcaiBridgeHandler;
import com.liucai.jsbridge.bridge.LcaiCallbackFunction;
import com.liucai.jsbridge.bridge.LcaiDefaultHandler;
import com.liucai.jsbridge.bridge.LcaiMessage;
import com.liucai.jsbridge.bridge.LcaiWebViewJavascriptBridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liucai
 * @Date 2026/5/28
 */
public class LcaiBridgeWebview extends WebView implements LcaiWebViewJavascriptBridge {
    public static final String toLoadJs = "WebViewJavascriptBridge.js";

    Map<String, LcaiCallbackFunction> responseCallbacks = new HashMap<>();
    Map<String, LcaiBridgeHandler> messageHandlers = new HashMap<>();
    LcaiBridgeHandler defaultHandler = new LcaiDefaultHandler();

    private List<LcaiMessage> startupMessage = new ArrayList<>();

    /** bridge JS 是否已注入（由 WebViewClient 维护） */
    private boolean bridgeInjected = false;
    private boolean printLog = false;

    private long uniqueId = 0;

    public LcaiBridgeWebview(@NonNull Context context) {
        super(context);
        init();
    }

    public LcaiBridgeWebview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LcaiBridgeWebview(@NonNull Context context,
                             @Nullable AttributeSet attrs,
                             int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setDefaultHandler(LcaiBridgeHandler handler) {
        this.defaultHandler = handler;
    }

    public void setPrintLog(boolean printLog) {
        this.printLog = printLog;
    }

    public boolean getPrintLog() {
        return printLog;
    }

    private void init() {
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        this.setWebViewClient(generateBridgeWebViewClient());
    }

    protected LcaiBridgeWebViewClient generateBridgeWebViewClient() {
        return new LcaiBridgeWebViewClient(this);
    }

    public boolean isBridgeInjected() {
        return bridgeInjected;
    }

    public void setBridgeInjected(boolean injected) {
        this.bridgeInjected = injected;
    }

    public List<LcaiMessage> getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(List<LcaiMessage> startupMessage) {
        this.startupMessage = startupMessage;
    }

    /**
     * 分发启动消息（供 WebViewClient 在 bridge 就绪后调用）。
     */
    public void flushStartupMessages() {
        if (startupMessage == null) return;
        for (LcaiMessage m : startupMessage) {
            dispatchMessage(m);
        }
        setStartupMessage(null);
    }

    @Override
    public void send(String data) {
        send(data, null);
    }

    @Override
    public void send(String data, LcaiCallbackFunction responseCallback) {
        doSend(null, data, responseCallback);
    }

    private void doSend(String handlerName, String data, LcaiCallbackFunction responseCallback) {
        LcaiMessage m = new LcaiMessage();
        if (!TextUtils.isEmpty(data)) {
            m.setData(data);
        }
        if (responseCallback != null) {
            String callbackStr = String.format(LcaiBridgeUtil.CALLBACK_ID_FORMAT,
                    ++uniqueId + (LcaiBridgeUtil.UNDERLINE_STR
                            + SystemClock.currentThreadTimeMillis()));
            responseCallbacks.put(callbackStr, responseCallback);
            m.setCallbackId(callbackStr);
        }
        if (!TextUtils.isEmpty(handlerName)) {
            m.setHandlerName(handlerName);
        }
        queueMessage(m);
    }

    private void queueMessage(LcaiMessage m) {
        if (startupMessage != null) {
            startupMessage.add(m);
        } else {
            dispatchMessage(m);
        }
    }

    /**
     * 把消息发给 H5。
     * <p>用 evaluateJavascript 代替 loadUrl("javascript:...")，避免打断 WebView 加载流程。
     */
    void dispatchMessage(LcaiMessage m) {
        String messageJson = m.toJson();
        if (messageJson == null) {
            printLog( "messageJson 为 null，跳过");
            return;
        }
        // 转义特殊字符
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");

        final String script =
                "WebViewJavascriptBridge._handleMessageFromNative('" + messageJson + "')";

        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            printLog( "dispatchMessage 不在主线程，post 到主线程");
            this.post(() -> execJs(script));
            return;
        }
        execJs(script);
    }

    /**
     * 统一的 JS 执行入口。
     */
    private void execJs(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.evaluateJavascript(script, value ->
                    printLog( "execJs result: " + value));
        } else {
            this.loadUrl("javascript:" + script);
        }
    }

    void handlerReturnData(String url) {
        String functionName = LcaiBridgeUtil.getFunctionFromReturnUrl(url);
        LcaiCallbackFunction f = responseCallbacks.get(functionName);
        String data = LcaiBridgeUtil.getDataFromReturnUrl(url);
        if (f != null) {
            f.onCallback(data);
            responseCallbacks.remove(functionName);
        }
    }

    /**
     * 拉取 H5 的消息队列。
     */
    void flushMessageQueue() {
        printLog( "flushMessageQueue 开始");
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            printLog( "flushMessageQueue 不在主线程，post 到主线程");
            this.post(this::flushMessageQueue);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.evaluateJavascript("WebViewJavascriptBridge._fetchQueue()", value -> {
                printLog( "_fetchQueue 返回: " + value);
                if (value == null || "null".equals(value)) return;
                String json = unescapeJsString(value);
                handleQueueData(json);
            });
        } else {
            // 低版本只能走 loadUrl + responseCallbacks
            loadUrl(LcaiBridgeUtil.JS_FETCH_QUEUE_FROM_JAVA, new LcaiCallbackFunction() {
                @Override
                public void onCallback(String data) {
                    handleQueueData(data);
                }
            });
        }
    }

    /**
     * 处理从 H5 拉回来的消息队列。
     */
    private void handleQueueData(String data) {
        if (TextUtils.isEmpty(data)) {
            printLog( "handleQueueData: 空队列");
            return;
        }
        List<LcaiMessage> list;
        try {
            list = LcaiMessage.toArrayList(data);
        } catch (Exception e) {
            printLog( "解析消息队列失败: " + e.getMessage());
            return;
        }
        if (list == null || list.isEmpty()) {
            printLog( "handleQueueData: 队列为空");
            return;
        }

        printLog( "handleQueueData: 队列长度=" + list.size());

        for (LcaiMessage m : list) {
            String responseId = m.getResponseId();

            // 1. 是 H5 对原生调用的响应
            if (!TextUtils.isEmpty(responseId)) {
                LcaiCallbackFunction function = responseCallbacks.get(responseId);
                if (function != null) {
                    function.onCallback(m.getResponseData());
                    responseCallbacks.remove(responseId);
                }
                continue;
            }

            // 2. 是 H5 主动调用原生
            LcaiCallbackFunction responseFunction;
            final String callbackId = m.getCallbackId();
            if (!TextUtils.isEmpty(callbackId)) {
                responseFunction = new LcaiCallbackFunction() {
                    @Override
                    public void onCallback(String data) {
                        LcaiMessage responseMsg = new LcaiMessage();
                        responseMsg.setResponseId(callbackId);
                        responseMsg.setResponseData(data);
                        queueMessage(responseMsg);
                    }
                };
            } else {
                responseFunction = new LcaiCallbackFunction() {
                    @Override
                    public void onCallback(String data) {
                        // 无 callbackId，忽略
                    }
                };
            }

            LcaiBridgeHandler handler;
            if (!TextUtils.isEmpty(m.getHandlerName())) {
                handler = messageHandlers.get(m.getHandlerName());
            } else {
                handler = defaultHandler;
            }
            if (handler != null) {
                handler.handler(m.getData(), responseFunction);
            } else {
                printLog( "未找到 handler: " + m.getHandlerName());
            }
        }
    }

    /**
     * 去掉 evaluateJavascript 返回值的转义。
     */
    private static String unescapeJsString(String value) {
        if (value == null) return null;
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value
                .replace("\\\\", "\\")
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    /**
     * @deprecated 仅供 API < 19 使用，新代码请用 {@link #execJs} 或 {@link #dispatchMessage}
     */
    @Deprecated
    public void loadUrl(String jsUrl, LcaiCallbackFunction returnCallback) {
        this.loadUrl(jsUrl);
        if (returnCallback != null) {
            responseCallbacks.put(LcaiBridgeUtil.parseFunctionName(jsUrl), returnCallback);
        }
    }

    public void registerHandler(String handlerName, LcaiBridgeHandler handler) {
        if (handler != null) {
            messageHandlers.put(handlerName, handler);
        }
    }

    public void callHandler(String handlerName, String data, LcaiCallbackFunction callBack) {
        doSend(handlerName, data, callBack);
    }

    public void printLog(String message) {
        if (printLog) {
            LcaiLogUtils.i( message);
        }
    }
}