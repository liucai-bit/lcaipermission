package com.liucai.security.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description MD5 摘要工具。MD5 不安全，仅建议用于文件校验、兼容旧协议等非安全场景。
 * @Date 2026/11/12
 */
public final class LcaiMD5Util {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private LcaiMD5Util() {
    }

    public static String md5(String text) {
        if (text == null) {
            return null;
        }
        return md5(text.getBytes(StandardCharsets.UTF_8));
    }

    public static String md5(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return toHex(md.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not supported", e);
        }
    }

    public static String md5File(File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IOException("Invalid file");
        }
        try (InputStream in = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not supported", e);
        }
    }

    public static boolean verify(String text, String expectedHex) {
        String actual = md5(text);
        return constantTimeEquals(actual, expectedHex);
    }

    private static String toHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2] = HEX[v >>> 4];
            out[i * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(out);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}