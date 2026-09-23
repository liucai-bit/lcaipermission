package com.liucai.http.exception;

/**
 * 用于内部流程控制，表示请求被主动取消。
 */
public class LcaiHttpCancelException extends RuntimeException {
    public LcaiHttpCancelException() {
        super("request cancelled");
    }
}