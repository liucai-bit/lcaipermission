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
 * @program lcpermission
 * @description
 * @Date 2026/5/28
 */
public class LcaiBridgeWebview extends WebView implements LcaiWebViewJavascriptBridge {

    public static final String toLoadJs = "WebViewJavascriptBridge.js";
    Map<String, LcaiCallbackFunction> responseCallbacks = new HashMap<String, LcaiCallbackFunction>();
    Map<String, LcaiBridgeHandler> messageHandlers = new HashMap<String, LcaiBridgeHandler>();
    LcaiBridgeHandler defaultHandler = new LcaiDefaultHandler();

    private List<LcaiMessage> startupMessage = new ArrayList<LcaiMessage>();

    public List<LcaiMessage> getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(List<LcaiMessage> startupMessage) {
        this.startupMessage = startupMessage;
    }

    private long uniqueId = 0;


    public LcaiBridgeWebview(@NonNull Context context) {
        super(context);
        init();
    }

    public LcaiBridgeWebview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LcaiBridgeWebview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setDefaultHandler(LcaiBridgeHandler handler) {
        this.defaultHandler = handler;
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

    void handlerReturnData(String url) {
        String functionName = LcaiBridgeUtil.getFunctionFromReturnUrl(url);
        LcaiCallbackFunction f = responseCallbacks.get(functionName);
        String data = LcaiBridgeUtil.getDataFromReturnUrl(url);
        if (f != null) {
            f.onCallback(data);
            responseCallbacks.remove(functionName);
            return;
        }
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
            String callbackStr = String.format(LcaiBridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId + (LcaiBridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
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

    void dispatchMessage(LcaiMessage m) {
        String messageJson = m.toJson();
        //escape special characters for json string
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        String javascriptCommand = String.format(LcaiBridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            this.loadUrl(javascriptCommand);
        }
    }

    void flushMessageQueue() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            loadUrl(LcaiBridgeUtil.JS_FETCH_QUEUE_FROM_JAVA, new LcaiCallbackFunction() {

                @Override
                public void onCallback(String data) {
                    // deserializeMessage
                    List<LcaiMessage> list = null;
                    try {
                        list = LcaiMessage.toArrayList(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    if (list == null || list.size() == 0) {
                        return;
                    }
                    for (int i = 0; i < list.size(); i++) {
                        LcaiMessage m = list.get(i);
                        String responseId = m.getResponseId();
                        // 是否是response
                        if (!TextUtils.isEmpty(responseId)) {
                            LcaiCallbackFunction function = responseCallbacks.get(responseId);
                            String responseData = m.getResponseData();
                            function.onCallback(responseData);
                            responseCallbacks.remove(responseId);
                        } else {
                            LcaiCallbackFunction responseFunction = null;
                            // if had callbackId
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
                                        // do nothing
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
                            }
                        }
                    }
                }
            });
        }
    }

    public void loadUrl(String jsUrl, LcaiCallbackFunction returnCallback) {
        this.loadUrl(jsUrl);
        responseCallbacks.put(LcaiBridgeUtil.parseFunctionName(jsUrl), returnCallback);
    }

    /**
     * register handler,so that javascript can call it
     *
     * @param handlerName
     * @param handler
     */
    public void registerHandler(String handlerName, LcaiBridgeHandler handler) {
        if (handler != null) {
            messageHandlers.put(handlerName, handler);
        }
    }

    /**
     * call javascript registered handler
     *
     * @param handlerName
     * @param data
     * @param callBack
     */
    public void callHandler(String handlerName, String data, LcaiCallbackFunction callBack) {
        doSend(handlerName, data, callBack);
    }

}
