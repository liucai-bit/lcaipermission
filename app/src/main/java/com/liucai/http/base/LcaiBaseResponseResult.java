package com.liucai.http.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.http.bulider.LcaiHttpRequestBulider;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * HTTP 请求回调。所有方法均有默认空实现，子类按需重写。
 *
 * <p>注意：所有返回 {@link InputStream} 的回调，调用方<b>必须</b>在使用完毕后关闭流。
 *
 * @author liucai
 */
public interface LcaiBaseResponseResult {

    // ---------------- 失败 ----------------

    /** 请求失败（网络错误、HTTP 非 2xx、解析错误、取消等）。 */
    default void onFailure(int code, @NonNull String msg) {
        error(code, msg); // 兼容旧方法
    }

    /** @deprecated 使用 {@link #onFailure(int, String)}。 */
    @Deprecated
    default void error(int code, @NonNull String msg) {
    }

    // ---------------- 成功：字符串 ----------------

    /** 成功且返回字符串（无 header）。 */
    default void onSuccess(@NonNull String result) {
        success(result);
    }

    /** 成功且返回字符串（含 header）。 */
    default void onSuccess(@NonNull Map<String, List<String>> headers, @NonNull String result) {
        success(headers, result);
    }

    /** @deprecated 使用 {@link #onSuccess(String)}。 */
    @Deprecated
    default void success(@NonNull String result) {
    }

    /** @deprecated 使用 {@link #onSuccess(Map, String)}。 */
    @Deprecated
    default void success(@NonNull Map<String, List<String>> headers, @NonNull String result) {
    }

    // ---------------- 成功：InputStream ----------------

    /** 成功且返回流（无 header）。调用方负责关闭。 */
    default void onSuccess(@NonNull InputStream inputStream) {
        success(inputStream);
    }

    /** 成功且返回流（含 header）。调用方负责关闭。 */
    default void onSuccess(@NonNull Map<String, List<String>> headers, @NonNull InputStream inputStream) {
        success(headers, inputStream);
    }

    /** @deprecated 使用 {@link #onSuccess(InputStream)}。 */
    @Deprecated
    default void success(@NonNull InputStream inputStream) {
    }

    /** @deprecated 使用 {@link #onSuccess(Map, InputStream)}。 */
    @Deprecated
    default void success(@NonNull Map<String, List<String>> headers, @NonNull InputStream inputStream) {
    }

    // ---------------- 下载进度 ----------------

    /** 下载进度回调。 */
    default void onProgress(int progress, boolean finish) {
    }

    /** @deprecated 使用 {@link #onProgress(int, boolean)}。 */
    @Deprecated
    default void success(int progress, boolean finish, @Nullable LcaiHttpRequestBulider builder) {
        onProgress(progress, finish);
    }

    // ---------------- 取消 ----------------

    /**
     * 是否已请求取消。
     * 在请求的各个阶段会被轮询检查。
     */
    default boolean isCancelled() {
        return cancelRequest();
    }

    /** @deprecated 使用 {@link #isCancelled()}。 */
    @Deprecated
    default boolean cancelRequest() {
        return false;
    }
}