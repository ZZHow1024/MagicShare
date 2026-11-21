package com.zzhow.magicshare.util;

import com.zzhow.magicshare.repository.UserRepository;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 加解密工具类
 * <p>
 * Cipher 实例不是线程安全的，每次调用创建新的实例，避免并发状态污染；
 * AES 加密采用每消息随机 IV（密文格式：IV || ciphertext），避免固定 IV 的模式泄露；
 * RSA-OAEP 摘要与 MGF1 统一使用 SHA-256，不依赖 JDK 实现细节；
 * 算法与密钥由服务端统一管理，初始化类异常包装为非受检异常，简化调用方处理。
 *
 * @author ZZHow
 * create 2025/2/7
 * update 2026/11/21
 */
public class CryptoUtil {
    private static final CryptoUtil instance;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int IV_LENGTH = 16;

    // RSA-OAEP 显式参数：摘要与 MGF1 统一使用 SHA-256
    private static final OAEPParameterSpec OAEP_SPEC =
            new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    static {
        try {
            instance = new CryptoUtil();
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private CryptoUtil() throws NoSuchPaddingException {

    }

    private static Cipher createAesCipher(int mode, byte[] iv) {
        try {
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCipher.init(mode, UserRepository.getAesCrypto().getKey(), new IvParameterSpec(iv));
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
            rsaCipher.init(Cipher.ENCRYPT_MODE, clientPublicKey, OAEP_SPEC);
            return rsaCipher;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize RSA cipher", e);
        }
    }

    private static Cipher createRsaDecryptCipher(PrivateKey privateKey) {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_SPEC);
            return rsaCipher;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize RSA cipher", e);
        }
    }

    /**
     * 使用客户端公钥加密（RSA-OAEP，摘要与 MGF1 均为 SHA-256）
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
     * AES 加密，返回 IV || ciphertext
     */
    public byte[] encryptAes(byte[] plainText) {
        byte[] iv = new byte[IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        Cipher aesCipher = createAesCipher(Cipher.ENCRYPT_MODE, iv);
        try {
            byte[] cipherText = aesCipher.doFinal(plainText);
            byte[] result = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(cipherText, 0, result, iv.length, cipherText.length);
            return result;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES encryption failed", e);
        }
    }

    public byte[] encryptAes(String plainText) {
        return encryptAes(plainText.getBytes());
    }

    /**
     * AES 解密，输入为 IV || ciphertext
     */
    public byte[] decryptAes(byte[] cipherTextWithIv) {
        if (cipherTextWithIv == null || cipherTextWithIv.length <= IV_LENGTH)
            throw new IllegalArgumentException("Ciphertext too short");

        byte[] iv = java.util.Arrays.copyOfRange(cipherTextWithIv, 0, IV_LENGTH);
        byte[] cipherText = java.util.Arrays.copyOfRange(cipherTextWithIv, IV_LENGTH, cipherTextWithIv.length);
        Cipher aesCipher = createAesCipher(Cipher.DECRYPT_MODE, iv);
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
     * 生成十六进制随机挑战值（用于连接密码认证防重放）
     */
    public String generateChallenge() {
        byte[] nonce = new byte[16];
        SECURE_RANDOM.nextBytes(nonce);
        StringBuilder hex = new StringBuilder(nonce.length * 2);
        for (byte b : nonce)
            hex.append(String.format("%02x", b));
        return hex.toString();
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