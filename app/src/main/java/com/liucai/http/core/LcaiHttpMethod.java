package com.liucai.http.core;

/**
 * HTTP 请求方法。
 *
 * @author liucai
 */
public enum LcaiHttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    /** 下载（内部会转成 GET）。 */
    DOWNLOAD
}