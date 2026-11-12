package com.liucai.security.encryption;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description AES-256 对称加密工具。优先使用 AndroidKeyStore 托管密钥，
 * 兼容 Base64 密钥方式（安全性较低，仅用于旧逻辑迁移）。
 * @Date 2026/11/12
 */
@SuppressWarnings("unused")
public final class LcaiAES256Util {
    private static final String DEFAULT_ALIAS = "lcai_aes_master_v1";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private LcaiAES256Util() {
    }

    // ==================== AndroidKeyStore 密钥 ====================

    /**
     * 在 AndroidKeyStore 中生成或获取 AES-256-GCM 密钥。
     * 密钥不可导出，加密时 Cipher 自动生成随机 IV。
     */
    public static SecretKey getOrCreateKeystoreKey(String alias) throws GeneralSecurityException {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("alias is empty");
        }
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        try {
            keyStore.load(null);
            if (keyStore.containsAlias(alias)) {
                return (SecretKey) keyStore.getKey(alias, null);
            }
        } catch (Exception e) {
            throw new GeneralSecurityException("load keystore failed", e);
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true) // 默认值，显式写出以强调
                .build();
        keyGenerator.init(spec);
        return keyGenerator.generateKey();
    }

    /**
     * 在 AndroidKeyStore 中生成或获取 AES-256-GCM 密钥。
     * 密钥不可导出，加密时 Cipher 自动生成随机 IV。
     */
    public static SecretKey getOrCreateKeystoreKey() throws GeneralSecurityException {
        return getOrCreateKeystoreKey(DEFAULT_ALIAS);
    }
    /**
     * 使用 Keystore 密钥加密。密文格式：12 字节 IV + GCM 密文（含 Tag）。
     */
    public static byte[] encryptWithKeystoreKey(byte[] plaintext, SecretKey key)
            throws GeneralSecurityException {
        if (plaintext == null) {
            return null;
        }
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        // 不传入 IV，让系统生成随机 IV（setRandomizedEncryptionRequired=true 时唯一安全做法）
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] ciphertext = cipher.doFinal(plaintext);
        byte[] iv = cipher.getIV(); // 取出系统生成的 IV，必须保存

        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
        return result;
    }

    public static String encryptWithKeystoreKey(String plaintext, SecretKey key)
            throws GeneralSecurityException {
        if (plaintext == null) {
            return null;
        }
        return LcaiBase64Util.encode(
                encryptWithKeystoreKey(plaintext.getBytes(StandardCharsets.UTF_8), key));
    }

    /**
     * 使用 Keystore 密钥解密。输入格式：12 字节 IV + GCM 密文。
     */
    public static byte[] decryptWithKeystoreKey(byte[] ivAndCiphertext, SecretKey key)
            throws GeneralSecurityException {
        if (ivAndCiphertext == null) {
            return null;
        }
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (ivAndCiphertext.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("ciphertext too short");
        }

        byte[] iv = Arrays.copyOfRange(ivAndCiphertext, 0, GCM_IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(ivAndCiphertext, GCM_IV_LENGTH, ivAndCiphertext.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        return cipher.doFinal(ciphertext);
    }

    public static String decryptWithKeystoreKey(String ciphertextBase64, SecretKey key)
            throws GeneralSecurityException {
        if (ciphertextBase64 == null) {
            return null;
        }
        byte[] decrypted = decryptWithKeystoreKey(LcaiBase64Util.decode(ciphertextBase64), key);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static String getDefaultAlias() {
        return DEFAULT_ALIAS;
    }
}