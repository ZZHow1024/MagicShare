package com.zzhow.magicshare.util;

import com.zzhow.magicshare.repository.UserRepository;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author ZZHow
 * @date 2025/2/7
 */
public class CryptoUtil {
    private static final CryptoUtil instance;
    private final Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    private final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

    static {
        try {
            instance = new CryptoUtil();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private CryptoUtil() throws NoSuchPaddingException, NoSuchAlgorithmException {
        
    }

    // RSA 解密
    public byte[] encryptRsa(String publicKey, byte[] cipherText) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 去除 PEM 格式的头尾
        String publicKeyContent = publicKey.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);

        // 加载公钥
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey clientPublicKey = keyFactory.generatePublic(spec);

        // 初始化 RSA 加密器
        rsaCipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);

        return rsaCipher.doFinal(cipherText);
    }

    public byte[] encryptRsa(String publicKey, String cipherText) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return encryptRsa(publicKey, cipherText.getBytes());
    }

    // AES 加密
    public byte[] encryptAes(byte[] plainText) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 初始化 AES 加密器
        aesCipher.init(Cipher.ENCRYPT_MODE, UserRepository.getAesCrypto().getKey(), new IvParameterSpec(UserRepository.getAesCrypto().getIv()));

        return aesCipher.doFinal(plainText);
    }

    public byte[] encryptAes(String plainText) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return encryptAes(plainText.getBytes());
    }

    // AES 解密
    public byte[] decryptAes(byte[] cipherText) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 初始化 AES 解密器
        aesCipher.init(Cipher.DECRYPT_MODE, UserRepository.getAesCrypto().getKey(), new IvParameterSpec(UserRepository.getAesCrypto().getIv()));

        return aesCipher.doFinal(cipherText);
    }

    public byte[] decryptAes(String cipherText) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return decryptAes(cipherText.getBytes());
    }

    // SHA256 哈希
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
