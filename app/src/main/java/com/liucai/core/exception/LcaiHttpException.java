package com.liucai.core.exception;

import com.liucai.core.util.log.LcaiLogUtils;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/5
 */
public class LcaiHttpException extends RuntimeException {

    public LcaiHttpException(String message, Throwable cause) {
        super(message, cause);
        LcaiLogUtils.e(message,cause.getMessage());
    }

    public LcaiHttpException(String message) {
        super(message);
        LcaiLogUtils.e(message);
    }
}
