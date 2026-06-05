package com.liucai.http.bulider;

import com.liucai.http.base.LcaiBaseResponseResult;
import com.liucai.http.core.LcaiHttpCall;
import com.liucai.http.core.LcaiHttpMethod;
import com.liucai.http.core.LcaiHttpRequest;
import com.liucai.http.core.LcaiHttpSend;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/5
 */
public class LcaiHttpRequestBulider {

    /**
     * 请求地址
     */
    public String url;

    public LcaiHttpRequestBulider setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 请求方式
     * 默认POST请求
     */
    public String method= LcaiHttpMethod.POST;

    public LcaiHttpRequestBulider setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * 请求头
     */
    public Map<String, Object> headers;

    public LcaiHttpRequestBulider addHeader(String key, Object value) {
        if (headers == null) {
            headers = new LinkedHashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    public LcaiHttpRequestBulider addHeaders(Map<String, Object> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * 请求参数
     * map
     */
    public Map<String, Object> mapParams;

    public LcaiHttpRequestBulider addMapParam(String key, String value) {
        if (mapParams == null) {
            mapParams = new LinkedHashMap<>();
        }
        mapParams.put(key, value);
        return this;
    }

    public LcaiHttpRequestBulider addMapParams(Map<String, Object> mapParams) {
        this.mapParams = mapParams;
        return this;
    }

    /**
     * 请求参数
     * 上传文件
     */
    public Map<String, File> fileParams;

    public LcaiHttpRequestBulider addFileParam(String key, File file) {
        if (fileParams == null) {
            fileParams = new LinkedHashMap<>();
        }
        fileParams.put(key, file);
        return this;
    }

    public LcaiHttpRequestBulider addFileParams(Map<String, File> fileParams) {
        this.fileParams = fileParams;
        return this;
    }

    /**
     * 请求参数
     * json
     */
    public String jsonParams;

    public LcaiHttpRequestBulider addJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
        return this;
    }

    /**
     * 连接超时时间
     */
    public int connectTimeout=10*1000;

    public LcaiHttpRequestBulider setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 读取超时时间
     */
    public int readTimeout=30*1000;

    public LcaiHttpRequestBulider setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * 返回格式
     * 默认返回json格式
     */
    public LcaiHttpCall httpCall=LcaiHttpCall.JSON;

    public LcaiHttpRequestBulider setHttpCall(LcaiHttpCall httpCall) {
        this.httpCall = httpCall;
        return this;
    }

    /**
     * 上传参数格式
     * 默认上传json格式
     */
    public LcaiHttpSend httpSend = LcaiHttpSend.JSON;

    public LcaiHttpRequestBulider setHttpSend(LcaiHttpSend httpSend) {
        this.httpSend = httpSend;
        return this;
    }

    /**
     * 编码格式
     * 默认UTF-8
     */
    public String encode = "utf-8";

    public LcaiHttpRequestBulider setEncode(String encode) {
        this.encode = encode;
        return this;
    }

    /**
     * 返回报文是否是XML格式
     */
    public boolean backXml;

    public LcaiHttpRequestBulider backXML() {
        this.backXml = true;
        return this;
    }

    /**
     * xml标签名
     * 返回报文为XML时使用
     */
    public String qName;

    public LcaiHttpRequestBulider setQName(String qName) {
        this.qName = qName;
        return this;
    }

    /**
     * 下载文件存储地址
     * 请求类型为 LcaiHttpMethod.DOWNLOAD 使用
     */
    public String savePath;

    public LcaiHttpRequestBulider setSavePath(String savePath) {
        this.savePath = savePath;
        return this;
    }

    /**
     * 下载文件保存名称
     * 返回格式为 LcaiHttpCall.DOWNLOAD时使用
     */
    public String fileName;

    public LcaiHttpRequestBulider setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * 返回数据是否包含请求头
     */
    public boolean withHeader;

    public LcaiHttpRequestBulider withHeader() {
        this.withHeader = true;
        return this;
    }

    /**
     * 是否停止下载
     * 请求类型 LcaiHttpMethod.DOWNLOAD 使用
     */
    public boolean stopDownload;

    public LcaiHttpRequestBulider stopDownload() {
        this.stopDownload = true;
        return this;
    }

    /**
     * 请求回调
     */
    public LcaiBaseResponseResult responseResult;

    public LcaiHttpRequestBulider setResponseResult(LcaiBaseResponseResult responseResult) {
        this.responseResult = responseResult;
        return this;
    }

    public LcaiHttpRequest excute() {
        return new LcaiHttpRequest(this);
    }
}
