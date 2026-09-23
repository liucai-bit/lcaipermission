package com.liucai.http.core;

/**
 * 响应体解析方式。
 */
public enum LcaiHttpCall {
    /** 以字符串（JSON/XML）形式返回。 */
    JSON,
    /** 以 {@link java.io.InputStream} 形式返回。 */
    RESPONSE,
    /** 下载到本地文件。 */
    DOWNLOAD
}