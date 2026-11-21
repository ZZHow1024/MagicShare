package com.zzhow.magicshare.util;

import com.zzhow.magicshare.repository.UserRepository;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 加解密工具类
 * <p>
 * Cipher 实例不是线程安全的，每次调用创建新的实例，避免并发状态污染；
 * 算法与密钥由服务端统一管理，初始化类异常包装为非受检异常，简化调用方处理。
 *
 * @author ZZHow
 * create 2025/2/7
 * update 2026/11/21
 */
public class CryptoUtil {
    private static final CryptoUtil instance;

    static {
        try {
            instance = new CryptoUtil();
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private CryptoUtil() throws NoSuchPaddingException {

    }

    private static Cipher createAesCipher(int mode) {
        try {
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCipher.init(mode, UserRepository.getAesCrypto().getKey(),
                    new IvParameterSpec(UserRepository.getAesCrypto().getIv()));
            return aesCipher;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize AES cipher", e);
        }
    }

    private static Cipher createRsaEncryptCipher(String publicKey) {
        try {
            String publicKeyContent = publicKey.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s+", "");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
            PublicKey clientPublicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);
            return rsaCipher;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize RSA cipher", e);
        }
    }

    private static Cipher createRsaDecryptCipher(PrivateKey privateKey) {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return rsaCipher;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize RSA cipher", e);
        }
    }

    /**
     * 使用客户端公钥加密（RSA-OAEP）
     */
    public byte[] encryptRsa(String publicKey, byte[] cipherText) {
        Cipher rsaCipher = createRsaEncryptCipher(publicKey);
        try {
            return rsaCipher.doFinal(cipherText);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("RSA encryption failed", e);
        }
    }

    public byte[] encryptRsa(String publicKey, String cipherText) {
        return encryptRsa(publicKey, cipherText.getBytes());
    }

    /**
     * AES 加密
     */
    public byte[] encryptAes(byte[] plainText) {
        Cipher aesCipher = createAesCipher(Cipher.ENCRYPT_MODE);
        try {
            return aesCipher.doFinal(plainText);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES encryption failed", e);
        }
    }

    public byte[] encryptAes(String plainText) {
        return encryptAes(plainText.getBytes());
    }

    /**
     * AES 解密
     */
    public byte[] decryptAes(byte[] cipherText) {
        Cipher aesCipher = createAesCipher(Cipher.DECRYPT_MODE);
        try {
            return aesCipher.doFinal(cipherText);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES decryption failed", e);
        }
    }

    public byte[] decryptAes(String cipherText) {
        return decryptAes(cipherText.getBytes());
    }

    /**
     * SHA256 哈希
     */
    public byte[] sha256(byte[] plainText) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(plainText);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] sha256(String plainText) {
        return sha256(plainText.getBytes());
    }

    public static CryptoUtil getInstance() {
        return instance;
    }
}