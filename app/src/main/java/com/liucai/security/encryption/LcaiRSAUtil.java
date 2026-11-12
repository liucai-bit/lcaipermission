package com.liucai.security.encryption;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.cert.Certificate;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description RSA 非对称加密/签名工具。私钥仅存在于 AndroidKeyStore，不可导出；
 * 公钥可完整导出并分发。加解密统一 OAEP-SHA256 + MGF1-SHA1，保证与 Keystore 私钥配对。
 * @Date 2026/11/12
 */
public final class LcaiRSAUtil {

    private static final String DEFAULT_ALIAS = "lcai_rsa_master_v1";
    private static final String ALGORITHM = "RSA";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "RSA/ECB/OAEPPadding";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int DEFAULT_KEY_SIZE = 2048;

    /**
     * OAEP-SHA256，MGF1 使用 SHA-1。
     * AndroidKeyStore 的 RSA-OAEP 私钥只接受 MGF1-SHA1，
     * 公钥加密也必须用同一参数，否则 Keystore 私钥无法解密。
     */
    private static final OAEPParameterSpec OAEP_SHA_256_MGF1_SHA1 = new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);

    private LcaiRSAUtil() {
    }

    public static KeyPair getOrCreateKeystoreKeyPair() throws GeneralSecurityException {
        return getOrCreateKeystoreKeyPair(DEFAULT_ALIAS);
    }

    public static KeyPair getOrCreateKeystoreKeyPair(String alias) throws GeneralSecurityException {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("alias is empty");
        }
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        try {
            keyStore.load(null);
            if (keyStore.containsAlias(alias)) {
                PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
                Certificate cert = keyStore.getCertificate(alias);
                if (privateKey == null || cert == null) {
                    throw new GeneralSecurityException("invalid keystore entry: " + alias);
                }
                return new KeyPair(cert.getPublicKey(), privateKey);
            }
        } catch (GeneralSecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralSecurityException("load keystore failed", e);
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT
                        | KeyProperties.PURPOSE_DECRYPT
                        | KeyProperties.PURPOSE_SIGN
                        | KeyProperties.PURPOSE_VERIFY)
                .setKeySize(DEFAULT_KEY_SIZE)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .build();
        keyPairGenerator.initialize(spec);
        return keyPairGenerator.generateKeyPair();
    }

    public static PublicKey getKeystorePublicKey() throws GeneralSecurityException {
        return getKeystorePublicKey(DEFAULT_ALIAS);
    }

    public static PublicKey getKeystorePublicKey(String alias) throws GeneralSecurityException {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException("alias is empty");
        }
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        try {
            keyStore.load(null);
            Certificate cert = keyStore.getCertificate(alias);
            if (cert == null) {
                throw new GeneralSecurityException("no certificate for alias: " + alias);
            }
            return cert.getPublicKey();
        } catch (GeneralSecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralSecurityException("get public key failed", e);
        }
    }

    public static String getKeystorePublicKeyBase64() throws GeneralSecurityException {
        return getKeystorePublicKeyBase64(DEFAULT_ALIAS);
    }

    public static String getKeystorePublicKeyBase64(String alias) throws GeneralSecurityException {
        return publicKeyToBase64(getKeystorePublicKey(alias));
    }

    // ==================== 加解密 ====================

    /**
     * 公钥加密。公钥可来自 Keystore 导出，也可由对端分发。
     * 注意：对端必须同样使用 OAEP-SHA256 + MGF1-SHA1，否则本地 Keystore 私钥无法解密。
     */
    public static String encrypt(String plaintext, PublicKey publicKey) throws GeneralSecurityException {
        if (plaintext == null) {
            return null;
        }
        return LcaiBase64Util.encode(
                encryptBytes(plaintext.getBytes(StandardCharsets.UTF_8), publicKey));
    }

    public static byte[] encryptBytes(byte[] data, PublicKey publicKey) throws GeneralSecurityException {
        if (data == null) {
            return null;
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKey is null");
        }
        int max = getMaxPlaintextLength(publicKey);
        if (data.length > max) {
            throw new IllegalArgumentException(
                    "RSA plaintext too long: " + data.length + " > " + max);
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, OAEP_SHA_256_MGF1_SHA1);
        return cipher.doFinal(data);
    }

    public static byte[] decryptWithKeystoreKey(byte[] ciphertext, PrivateKey privateKey)
            throws GeneralSecurityException {
        if (ciphertext == null || privateKey == null) {
            throw new IllegalArgumentException("ciphertext or privateKey is null");
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_SHA_256_MGF1_SHA1);
        return cipher.doFinal(ciphertext);
    }

    public static String decryptWithKeystoreKey(String ciphertextBase64, PrivateKey privateKey)
            throws GeneralSecurityException {
        if (ciphertextBase64 == null) {
            return null;
        }
        byte[] plain = decryptWithKeystoreKey(LcaiBase64Util.decode(ciphertextBase64), privateKey);
        return new String(plain, StandardCharsets.UTF_8);
    }

    // ==================== 签名/验签 ====================

    public static String signWithKeystoreKey(String data, PrivateKey privateKey)
            throws GeneralSecurityException {
        if (data == null || privateKey == null) {
            throw new IllegalArgumentException("data or privateKey is null");
        }
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return LcaiBase64Util.encode(signature.sign());
    }

    public static boolean verify(String data, String signatureBase64, PublicKey publicKey)
            throws GeneralSecurityException {
        if (data == null || signatureBase64 == null || publicKey == null) {
            return false;
        }
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return signature.verify(LcaiBase64Util.decode(signatureBase64));
    }

    // ==================== 公钥序列化（公钥可公开） ====================

    public static PublicKey publicKeyFromBase64(String base64) throws GeneralSecurityException {
        if (base64 == null) {
            throw new IllegalArgumentException("base64 is null");
        }
        byte[] bytes = LcaiBase64Util.decode(base64);
        return KeyFactory.getInstance(ALGORITHM)
                .generatePublic(new X509EncodedKeySpec(bytes));
    }

    public static String publicKeyToBase64(PublicKey publicKey) {
        if (publicKey == null) {
            return null;
        }
        return LcaiBase64Util.encode(publicKey.getEncoded());
    }

    public static int getMaxPlaintextLength(PublicKey publicKey) {
        if (!(publicKey instanceof RSAPublicKey)) {
            throw new IllegalArgumentException("publicKey is not RSA");
        }
        int keySizeBits = ((RSAPublicKey) publicKey).getModulus().bitLength();
        int hashLen = 32; // SHA-256
        return keySizeBits / 8 - 2 * hashLen - 2;
    }

    public static String getDefaultAlias() {
        return DEFAULT_ALIAS;
    }
}