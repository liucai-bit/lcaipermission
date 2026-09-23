package com.liucai.http.bulider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.http.base.LcaiBaseResponseResult;
import com.liucai.http.core.LcaiHttpCall;
import com.liucai.http.core.LcaiHttpMethod;
import com.liucai.http.core.LcaiHttpRequest;
import com.liucai.http.core.LcaiHttpSend;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP 请求构造器。
 *
 * <p>每个 Builder 只应服务于一个请求，不要在多个请求间共享。
 *
 * @author liucai
 */
public class LcaiHttpRequestBulider {

    // ---------------- 请求基本信息 ----------------

    public String url;

    public LcaiHttpRequestBulider setUrl(@NonNull String url) {
        this.url = url;
        return this;
    }

    /** 请求方法，默认 POST。 */
    public LcaiHttpMethod method = LcaiHttpMethod.POST;

    public LcaiHttpRequestBulider setMethod(@NonNull LcaiHttpMethod method) {
        this.method = method;
        return this;
    }

    // ---------------- 请求头 ----------------

    public Map<String, String> headers;

    public LcaiHttpRequestBulider addHeader(@NonNull String key, @NonNull Object value) {
        if (headers == null) {
            headers = new LinkedHashMap<>();
        }
        headers.put(key, value.toString());
        return this;
    }

    /**
     * 追加请求头
     */
    public LcaiHttpRequestBulider addHeaders(@Nullable Map<String, ?> newHeaders) {
        if (newHeaders == null || newHeaders.isEmpty()) {
            return this;
        }
        if (headers == null) {
            headers = new LinkedHashMap<>();
        }
        for (Map.Entry<String, ?> e : newHeaders.entrySet()) {
            if (e.getKey() != null && e.getValue() != null) {
                headers.put(e.getKey(), e.getValue().toString());
            }
        }
        return this;
    }

    // ---------------- Map 参数 ----------------

    public Map<String, Object> mapParams;

    public LcaiHttpRequestBulider addMapParam(@NonNull String key, @NonNull Object value) {
        if (mapParams == null) {
            mapParams = new LinkedHashMap<>();
        }
        mapParams.put(key, value);
        return this;
    }

    public LcaiHttpRequestBulider addMapParams(@Nullable Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return this;
        }
        if (mapParams == null) {
            mapParams = new LinkedHashMap<>();
        }
        mapParams.putAll(params);
        return this;
    }

    // ---------------- 文件参数 ----------------

    public Map<String, File> fileParams;

    public LcaiHttpRequestBulider addFileParam(@NonNull String key, @NonNull File file) {
        if (fileParams == null) {
            fileParams = new LinkedHashMap<>();
        }
        fileParams.put(key, file);
        return this;
    }

    public LcaiHttpRequestBulider addFileParams(@Nullable Map<String, File> files) {
        if (files == null || files.isEmpty()) {
            return this;
        }
        if (fileParams == null) {
            fileParams = new LinkedHashMap<>();
        }
        fileParams.putAll(files);
        return this;
    }

    // ---------------- JSON body ----------------

    public String jsonParams;

    public LcaiHttpRequestBulider addJsonParams(@NonNull String jsonParams) {
        this.jsonParams = jsonParams;
        return this;
    }

    // ---------------- 超时 ----------------

    public int connectTimeout = 10_000;

    public LcaiHttpRequestBulider setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int readTimeout = 30_000;

    public LcaiHttpRequestBulider setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    // ---------------- 响应/请求格式 ----------------

    public LcaiHttpCall httpCall = LcaiHttpCall.JSON;

    public LcaiHttpRequestBulider setHttpCall(@NonNull LcaiHttpCall httpCall) {
        this.httpCall = httpCall;
        return this;
    }

    public LcaiHttpSend httpSend = LcaiHttpSend.JSON;

    public LcaiHttpRequestBulider setHttpSend(@NonNull LcaiHttpSend httpSend) {
        this.httpSend = httpSend;
        return this;
    }

    public String encode = "UTF-8";

    public LcaiHttpRequestBulider setEncode(@NonNull String encode) {
        this.encode = encode;
        return this;
    }

    // ---------------- XML ----------------

    public boolean backXml;

    public LcaiHttpRequestBulider backXML() {
        this.backXml = true;
        return this;
    }

    public String qName;

    public LcaiHttpRequestBulider setQName(@NonNull String qName) {
        this.qName = qName;
        return this;
    }

    // ---------------- 下载 ----------------

    public String savePath;

    public LcaiHttpRequestBulider setSavePath(@NonNull String savePath) {
        this.savePath = savePath;
        return this;
    }

    public String fileName;

    public LcaiHttpRequestBulider setFileName(@NonNull String fileName) {
        this.fileName = fileName;
        return this;
    }

    /** 是否随响应回调带 header。 */
    public boolean withHeader;

    public LcaiHttpRequestBulider withHeader() {
        this.withHeader = true;
        return this;
    }

    /** @deprecated 由 {@link LcaiHttpCall#DOWNLOAD} 的取消逻辑自动处理。 */
    @Deprecated
    public boolean stopDownload;

    @Deprecated
    public LcaiHttpRequestBulider stopDownload() {
        this.stopDownload = true;
        return this;
    }

    // ---------------- 请求回调 ----------------

    public LcaiBaseResponseResult responseResult;

    public LcaiHttpRequestBulider setResponseResult(@NonNull LcaiBaseResponseResult responseResult) {
        this.responseResult = responseResult;
        return this;
    }

    // ---------------- multipart boundary ----------------

    /**
     * 一次请求内唯一的 boundary，避免 openConnect 与写 body 时不一致。
     */
    public final String boundary = "----LcaiBoundary" + UUID.randomUUID().toString().replace("-", "");

    // ---------------- 执行 ----------------

    /**
     * 发起请求。
     * @deprecated 拼写错误，请使用 {@link #execute()}。
     */
    @Deprecated
    public LcaiHttpRequest excute() {
        return execute();
    }

    public LcaiHttpRequest execute() {
        return new LcaiHttpRequest(this);
    }
}