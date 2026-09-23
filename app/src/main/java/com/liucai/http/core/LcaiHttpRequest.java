package com.liucai.http.core;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.liucai.core.exception.LcaiHttpException;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.http.bulider.LcaiHttpRequestBulider;
import com.liucai.http.exception.LcaiHttpCancelException;
import com.liucai.http.thread.GlobalThreadPool;
import com.liucai.http.xml.LcaiHttpXmlHandler;

import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParserFactory;

/**
 * HTTP 请求执行器。
 *
 * @author liucai
 */
public class LcaiHttpRequest {

    private final LcaiHttpRequestBulider builder;

    private volatile HttpURLConnection connection;

    public LcaiHttpRequest(@NonNull LcaiHttpRequestBulider builder) {
        this.builder = builder;
        GlobalThreadPool.execute(this::run);
    }

    // ==================== 主流程 ====================

    private void run() {
        try {
            checkCancelled();

            // DOWNLOAD 本质是 GET
            if (builder.method == LcaiHttpMethod.DOWNLOAD) {
                builder.method = LcaiHttpMethod.GET;
            }

            openAndRequest();

        } catch (LcaiHttpCancelException ce) {
            handleCancel();
        } catch (Throwable t) {
            handleException(t);
        } finally {
            closeQuietly();
        }
    }

    private void openAndRequest() throws Exception {
        if (TextUtils.isEmpty(builder.url)) {
            notifyError(-1, "请求地址为空");
            return;
        }

        String finalUrl = builder.url;
        if (builder.method == LcaiHttpMethod.GET || builder.method == LcaiHttpMethod.DELETE) {
            finalUrl += buildQueryString(builder.mapParams);
        }

        LcaiLogUtils.d("LcaiHttp", "url=" + finalUrl + ", method=" + builder.method);

        URL url = new URL(finalUrl);
        connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(builder.connectTimeout);
        connection.setReadTimeout(builder.readTimeout);
        connection.setRequestMethod(builder.method.name());
        connection.setInstanceFollowRedirects(true);
        connection.setUseCaches(false);

        applyRequestHeaders();

        // 只有带 body 的方法才需要 doOutput
        if (hasRequestBody()) {
            connection.setDoOutput(true);
            writeRequestBody();
        }

        checkCancelled();

        int code = connection.getResponseCode();

        if (code >= 200 && code < 300) {
            handleSuccess(code);
        } else {
            handleErrorResponse(code);
        }
    }

    // ==================== 请求头 ====================

    private void applyRequestHeaders() {
        switch (builder.httpSend) {
            case JSON:
                connection.setRequestProperty("Content-Type",
                        "application/json; charset=" + builder.encode);
                break;
            case MAP:
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded; charset=" + builder.encode);
                break;
            case FORM_DATA:
                connection.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + builder.boundary);
                break;
        }

        if (builder.headers != null) {
            for (Map.Entry<String, String> e : builder.headers.entrySet()) {
                connection.setRequestProperty(e.getKey(), e.getValue());
            }
        }
    }

    // ==================== 请求体 ====================

    private boolean hasRequestBody() {
        return builder.method == LcaiHttpMethod.POST
                || builder.method == LcaiHttpMethod.PUT;
    }

    private void writeRequestBody() throws IOException {
        switch (builder.httpSend) {
            case JSON:
                writeJsonBody();
                break;
            case MAP:
                writeMapBody();
                break;
            case FORM_DATA:
                writeMultipartBody();
                break;
        }
    }

    private void writeJsonBody() throws IOException {
        String body = getJsonBody();
        if (TextUtils.isEmpty(body)) {
            return;
        }
        try (OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream(), builder.encode)) {
            w.write(body);
            w.flush();
        }
    }

    private void writeMapBody() throws IOException {
        String body = buildFormBody();
        if (TextUtils.isEmpty(body)) {
            return;
        }
        try (OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream(), builder.encode)) {
            w.write(body);
            w.flush();
        }
    }

    private void writeMultipartBody() throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            LcaiMultipartWriter.write(os, builder);
        }
    }

    @NonNull
    private String getJsonBody() {
        if (!TextUtils.isEmpty(builder.jsonParams)) {
            return builder.jsonParams;
        }
        if (builder.mapParams != null && !builder.mapParams.isEmpty()) {
            JSONObject obj = new JSONObject();
            for (Map.Entry<String, Object> e : builder.mapParams.entrySet()) {
                obj.put(e.getKey(), e.getValue());
            }
            return obj.toJSONString();
        }
        return "";
    }

    @NonNull
    private String buildFormBody() {
        if (builder.mapParams == null || builder.mapParams.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : builder.mapParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(encode(e.getKey())).append('=').append(encode(String.valueOf(e.getValue())));
        }
        return sb.toString();
    }

    // ==================== 响应处理 ====================

    private void handleSuccess(int code) throws Exception {
        Map<String, List<String>> headerFields = connection.getHeaderFields();

        switch (builder.httpCall) {
            case RESPONSE:
                handleStreamResponse(headerFields);
                break;
            case JSON:
                if (builder.backXml) {
                    handleXmlResponse(headerFields);
                } else {
                    handleStringResponse(headerFields);
                }
                break;
            case DOWNLOAD:
                handleDownload();
                break;
        }
    }

    private void handleStreamResponse(Map<String, List<String>> headers) {
        InputStream is = null;
        try {
            is = wrapGzip(connection.getInputStream());
            if (builder.responseResult == null) {
                return;
            }
            if (builder.withHeader) {
                builder.responseResult.onSuccess(headers, is);
            } else {
                builder.responseResult.onSuccess(is);
            }
            // 注意：此处不关闭 is，调用方负责
            is = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                try { is.close(); } catch (IOException ignored) {}
            }
        }
    }

    private void handleStringResponse(Map<String, List<String>> headers) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(wrapGzip(connection.getInputStream()), builder.encode))) {
            String line;
            while ((line = br.readLine()) != null) {
                checkCancelled();
                sb.append(line);
            }
        }
        String body = sb.toString();
        if (TextUtils.isEmpty(body)) {
            throw new LcaiHttpException("响应体为空");
        }
        if (builder.responseResult == null) {
            return;
        }
        if (builder.withHeader) {
            builder.responseResult.onSuccess(headers, body);
        } else {
            builder.responseResult.onSuccess(body);
        }
    }

    private void handleXmlResponse(Map<String, List<String>> headers) throws Exception {
        if (TextUtils.isEmpty(builder.qName)) {
            throw new LcaiHttpException("backXml=true 时 qName 不能为空");
        }
        LcaiHttpXmlHandler handler = new LcaiHttpXmlHandler(builder.qName);
        try (InputStream is = wrapGzip(connection.getInputStream())) {
            SAXParserFactory.newInstance().newSAXParser()
                    .parse(new InputSource(new InputStreamReader(is, builder.encode)), handler);
        }
        String result = JSONObject.toJSONString(handler.getMaps());
        if (builder.responseResult == null) {
            return;
        }
        if (builder.withHeader) {
            builder.responseResult.onSuccess(headers, result);
        } else {
            builder.responseResult.onSuccess(result);
        }
    }

    private void handleDownload() throws Exception {
        if (TextUtils.isEmpty(builder.savePath) || TextUtils.isEmpty(builder.fileName)) {
            notifyError(-1, "存储地址或文件名称不能为空");
            return;
        }

        File dir = new File(builder.savePath);
        if (!dir.exists() && !dir.mkdirs()) {
            notifyError(-1, "创建下载目录失败");
            return;
        }

        File target = new File(dir, builder.fileName);
        long total = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            total = connection.getContentLengthLong();
        }
        long downloaded = 0;
        int lastProgress = -1;

        try (InputStream in = wrapGzip(connection.getInputStream());
             FileOutputStream fos = new FileOutputStream(target)) {

            byte[] buf = new byte[8 * 1024];
            int n;
            while ((n = in.read(buf)) != -1) {
                checkCancelled();
                fos.write(buf, 0, n);
                downloaded += n;

                if (total > 0 && builder.responseResult != null) {
                    int progress = (int) (downloaded * 100 / total);
                    if (progress != lastProgress) {
                        lastProgress = progress;
                        builder.responseResult.onProgress(progress, false);
                    }
                }
            }
        }

        if (builder.responseResult != null) {
            builder.responseResult.onProgress(100, true);
        }
    }

    private void handleErrorResponse(int code) {
        String body = "";
        InputStream err = connection.getErrorStream();
        if (err != null) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(err, builder.encode))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                body = sb.toString();
            } catch (IOException ignored) {
            }
        }
        notifyError(code, body);
    }

    // ==================== 工具方法 ====================

    private InputStream wrapGzip(InputStream is) throws IOException {
        String encoding = connection.getContentEncoding();
        if (encoding != null && encoding.toLowerCase().contains("gzip")) {
            return new GZIPInputStream(is);
        }
        return new BufferedInputStream(is);
    }

    @NonNull
    private String buildQueryString(@Nullable Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("?");
        for (Map.Entry<String, Object> e : params.entrySet()) {
            if (sb.length() > 1) {
                sb.append('&');
            }
            sb.append(encode(e.getKey())).append('=').append(encode(String.valueOf(e.getValue())));
        }
        return sb.toString();
    }

    @NonNull
    private String encode(@NonNull String s) {
        try {
            return URLEncoder.encode(s, builder.encode);
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    private void checkCancelled() {
        if (builder.responseResult != null && builder.responseResult.isCancelled()) {
            throw new LcaiHttpCancelException();
        }
    }

    private void notifyError(int code, @NonNull String msg) {
        if (builder.responseResult != null) {
            builder.responseResult.onFailure(code, msg);
        }
    }

    private void handleException(@NonNull Throwable t) {
        int code;
        String msg;
        if (t instanceof IOException) {
            code = -2;
            msg = "网络连接错误: " + t.getMessage();
        } else if (t instanceof LcaiHttpException) {
            code = -3;
            msg = "HTTP 处理错误: " + t.getMessage();
        } else {
            code = -1;
            msg = "请求失败: " + t.getMessage();
        }
        LcaiLogUtils.e("LcaiHttp", msg, t);
        notifyError(code, msg);
    }

    private void handleCancel() {
        notifyError(-4, "请求已被取消");

        // 下载取消时删除临时文件
        if (builder.httpCall == LcaiHttpCall.DOWNLOAD
                && !TextUtils.isEmpty(builder.savePath)
                && !TextUtils.isEmpty(builder.fileName)) {
            File f = new File(builder.savePath, builder.fileName);
            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }
        LcaiLogUtils.d("LcaiHttp", "请求取消");
    }

    private void closeQuietly() {
        HttpURLConnection c = connection;
        connection = null;
        if (c != null) {
            try {
                c.disconnect();
            } catch (Exception ignored) {
            }
        }
    }
}