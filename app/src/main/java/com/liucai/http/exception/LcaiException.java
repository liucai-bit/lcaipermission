package com.liucai.http.exception;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/11/9
 */
public class LcaiException extends RuntimeException{

    public LcaiException(String message) {
        super("500--"+message);
    }

    public LcaiException(String code, String message) {
        super(code+"--"+message);
    }
}
