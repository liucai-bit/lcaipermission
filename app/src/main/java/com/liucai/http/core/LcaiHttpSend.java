package com.liucai.http.core;

/**
 * 请求体发送格式。
 */
public enum LcaiHttpSend {
    /** JSON body。 */
    JSON,
    /** form-urlencoded body。 */
    MAP,
    /** multipart/form-data body（文件上传）。 */
    FORM_DATA
}