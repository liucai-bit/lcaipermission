package com.liucai.security.core;

import com.liucai.security.encryption.LcaiAES256Util;
import com.liucai.security.encryption.LcaiBase64Util;
import com.liucai.security.encryption.LcaiEncryption;
import com.liucai.security.encryption.LcaiMD5Util;
import com.liucai.security.encryption.LcaiRSAUtil;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description 统一加密门面。AES 密钥与 RSA 私钥均由 AndroidKeyStore 托管，不可导出。
 * @Date 2026/11/12
 */
public final class LcaiEncryptionCore {

    private LcaiEncryptionCore() {
    }

    // ==================== 统一入口 ====================

    /**
     * 统一加密入口。
     *
     * @param type      加密类型
     * @param plaintext 明文
     * @param key       AES256 时为 Keystore 别名；RSA 时为公钥 Base64；BASE64/MD5 时忽略
     */
    public static String encrypt(LcaiEncryption type, String plaintext, String key)
            throws GeneralSecurityException {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        switch (type) {
            case BASE64:
                return LcaiBase64Util.encode(plaintext);
            case MD5:
                return LcaiMD5Util.md5(plaintext);
            case AES256:
                SecretKey aesKey = LcaiAES256Util.getOrCreateKeystoreKey(key);
                return LcaiAES256Util.encryptWithKeystoreKey(plaintext, aesKey);
            case RSA:
                PublicKey publicKey = LcaiRSAUtil.publicKeyFromBase64(key);
                return LcaiRSAUtil.encrypt(plaintext, publicKey);
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    /**
     * 统一解密入口。
     *
     * @param type       加密类型
     * @param ciphertext 密文
     * @param key        AES256/RSA 均为 Keystore 别名；BASE64 时忽略；MD5 不支持
     */
    public static String decrypt(LcaiEncryption type, String ciphertext, String key)
            throws GeneralSecurityException {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        switch (type) {
            case BASE64:
                return LcaiBase64Util.decodeToString(ciphertext);
            case MD5:
                throw new UnsupportedOperationException("MD5 is not reversible");
            case AES256:
                SecretKey aesKey = LcaiAES256Util.getOrCreateKeystoreKey(key);
                return LcaiAES256Util.decryptWithKeystoreKey(ciphertext, aesKey);
            case RSA:
                KeyPair keyPair = LcaiRSAUtil.getOrCreateKeystoreKeyPair(key);
                return LcaiRSAUtil.decryptWithKeystoreKey(ciphertext, keyPair.getPrivate());
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    // ==================== Keystore 便捷入口 ====================

    public static String encryptWithKeystoreAes(String plaintext, SecretKey key)
            throws GeneralSecurityException {
        return LcaiAES256Util.encryptWithKeystoreKey(plaintext, key);
    }

    public static String decryptWithKeystoreAes(String ciphertext, SecretKey key)
            throws GeneralSecurityException {
        return LcaiAES256Util.decryptWithKeystoreKey(ciphertext, key);
    }

    public static byte[] encryptWithKeystoreRsa(byte[] data, PublicKey publicKey)
            throws GeneralSecurityException {
        return LcaiRSAUtil.encryptBytes(data, publicKey);
    }

    public static String decryptWithKeystoreRsa(String ciphertextBase64, PrivateKey privateKey)
            throws GeneralSecurityException {
        return LcaiRSAUtil.decryptWithKeystoreKey(ciphertextBase64, privateKey);
    }

    // ==================== Keystore 密钥管理 ====================

    public static SecretKey getOrCreateAesKeystoreKey(String alias) throws GeneralSecurityException {
        return LcaiAES256Util.getOrCreateKeystoreKey(alias);
    }

    public static KeyPair getOrCreateRsaKeystoreKeyPair(String alias) throws GeneralSecurityException {
        return LcaiRSAUtil.getOrCreateKeystoreKeyPair(alias);
    }

    public static PublicKey getRsaKeystorePublicKey(String alias) throws GeneralSecurityException {
        return LcaiRSAUtil.getKeystorePublicKey(alias);
    }

    public static String getRsaKeystorePublicKeyBase64(String alias) throws GeneralSecurityException {
        return LcaiRSAUtil.getKeystorePublicKeyBase64(alias);
    }

    // ==================== 签名/验签 ====================

    public static String sign(String data, String rsaAlias) throws GeneralSecurityException {
        KeyPair keyPair = LcaiRSAUtil.getOrCreateKeystoreKeyPair(rsaAlias);
        return LcaiRSAUtil.signWithKeystoreKey(data, keyPair.getPrivate());
    }

    public static boolean verify(String data, String signatureBase64, String publicKeyBase64)
            throws GeneralSecurityException {
        PublicKey publicKey = LcaiRSAUtil.publicKeyFromBase64(publicKeyBase64);
        return LcaiRSAUtil.verify(data, signatureBase64, publicKey);
    }

    // ==================== 工具 ====================

    public static String rsaPublicKeyToBase64(PublicKey publicKey) {
        return LcaiRSAUtil.publicKeyToBase64(publicKey);
    }
}