package com.liucai.jsbridge.web;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.liucai.jsbridge.bridge.LcaiCallbackFunction;

/**
 * 回调协议适配器。
 *
 * @author liucai
 */
public class LcaiWebCallbackAdapter {

    public interface Callback {
        /**
         * @return true 表示处理完后自动关闭页面；false 表示保持页面
         */
        boolean onMethodBack(@Nullable LcaiCallbackFunction jsCallback,
                             @NonNull JSONObject payload,
                             @NonNull Activity activity);
    }

    private static final long AUTO_CLOSE_DELAY_MS = 300L;

    private final Callback callback;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public LcaiWebCallbackAdapter(@Nullable Callback callback) {
        this.callback = callback;
    }

    /**
     * 分发回调。
     *
     * @param onClose 回调完成后是否自动关闭页面；null 表示不关闭
     */
    public void dispatch(@Nullable LcaiCallbackFunction jsCallback,
                         @Nullable String method,
                         @Nullable String data,
                         @NonNull Activity activity,
                         @Nullable Runnable onClose) {
        JSONObject payload = new JSONObject();
        payload.put("method", method);
        payload.put("data", data);

        boolean autoClose = false;
        if (callback != null) {
            autoClose = callback.onMethodBack(jsCallback, payload, activity);
        } else {
            // 没有 callback 时，默认关闭（比如返回键、权限拒绝）
            autoClose = true;
        }

        if (autoClose && onClose != null) {
            handler.postDelayed(onClose, AUTO_CLOSE_DELAY_MS);
        }
    }

    public void release() {
        handler.removeCallbacksAndMessages(null);
    }
}