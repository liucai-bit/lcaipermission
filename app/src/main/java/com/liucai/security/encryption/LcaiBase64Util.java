package com.liucai.security.encryption;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description Base64 编码/解码工具。BASE64 不是加密，不能用于保护敏感数据。
 * @Date 2026/11/12
 */
@SuppressWarnings("unused")
public final class LcaiBase64Util {

    private LcaiBase64Util() {
    }

    public static String encode(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    public static String encode(String text) {
        if (text == null) {
            return null;
        }
        return encode(text.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decode(String base64) {
        if (base64 == null) {
            return null;
        }
        return Base64.decode(base64, Base64.NO_WRAP);
    }

    public static String decodeToString(String base64) {
        byte[] bytes = decode(base64);
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }

    public static String encodeUrlSafe(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.encodeToString(data,
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    public static byte[] decodeUrlSafe(String base64) {
        if (base64 == null) {
            return null;
        }
        return Base64.decode(base64,
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }
}