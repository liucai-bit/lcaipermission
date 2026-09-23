package com.liucai.core.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 业务/网络异常。
 * @author liucai
 */
public class LcaiHttpException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 默认无错误码。 */
    public static final int NO_CODE = -1;

    private final int code;

    // ---------------- 构造函数 ----------------

    public LcaiHttpException(@NonNull String message) {
        this(message, NO_CODE);
    }

    public LcaiHttpException(@NonNull String message, int code) {
        super(message);
        this.code = code;
    }

    public LcaiHttpException(@NonNull String message, @Nullable Throwable cause) {
        this(message, NO_CODE, cause);
    }

    public LcaiHttpException(@NonNull String message, int code, @Nullable Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public LcaiHttpException(@NonNull Throwable cause) {
        this(cause.getMessage() != null ? cause.getMessage() : "unknown", NO_CODE, cause);
    }

    // ---------------- 业务属性 ----------------

    public int getCode() {
        return code;
    }

    public boolean hasCode() {
        return code != NO_CODE;
    }

    @NonNull
    @Override
    public String toString() {
        String base = super.toString();
        return hasCode() ? ("[" + code + "] " + base) : base;
    }
}