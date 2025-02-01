package com.zzhow.magicshare.repository;

import com.zzhow.magicshare.pojo.entity.AesCrypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZHow
 * @date 2025/1/30
 */
public class UserRepository {
    private static AesCrypto aesCrypto = null;
    private static KeyPair keyPair = null;
    private static String password = "123";
    private static final List<String> users = new ArrayList<>();

    static {
        try {
            // 生成随机 AES 密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // 选择 AES 256 位密钥
            SecretKey aesKey = keyGenerator.generateKey();
            // 生成随机 IV（初始化向量）
            byte[] iv = new byte[16]; // AES 块大小 128 位
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv); // 填充随机数据到 IV 数组
            UserRepository.aesCrypto = new AesCrypto(aesKey, iv);

            // 生成 RSA 密钥对
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static AesCrypto getAesCrypto() {
        return aesCrypto;
    }

    public static KeyPair getKeyPair() {
        return keyPair;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        UserRepository.password = password;
    }

    public static void clearPassword() {
        UserRepository.password = null;
    }

    public static void addUser(String sessionId) {
        users.add(sessionId);
    }

    public static void removeUser(String sessionId) {
        users.remove(sessionId);
    }

    public static boolean containsUser(String sessionId) {
        return users.contains(sessionId);
    }
}
