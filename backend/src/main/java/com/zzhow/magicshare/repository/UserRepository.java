package com.zzhow.magicshare.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzhow.magicshare.pojo.entity.AesCrypto;
import com.zzhow.magicshare.pojo.vo.FileListVO;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.security.*;
import java.util.*;

/**
 * @author ZZHow
 * @date 2025/1/30
 */
public class UserRepository {
    private static AesCrypto aesCrypto = null;
    private static KeyPair keyPair = null;
    private static String password = "123";
    private static final Map<String, WebSocketSession> users = new HashMap<>();

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

    public static void addUser(String sessionId, WebSocketSession session) {
        users.put(sessionId, session);
    }

    public static void removeUser(String sessionId) {
        users.remove(sessionId);
    }

    public static boolean containsUser(String sessionId) {
        return users.get(sessionId) != null;
    }

    public static void sendListToAll() {
        Set<String> strings = users.keySet();
        for (String sessionId : strings) {
            WebSocketSession session = users.get(sessionId);
            if (session.isOpen()) {
                FileListVO fileListVO = new FileListVO(FileRepository.getUuid(), FileRepository.size(), FileRepository.getFiles());

                try {
                    // 初始化 AES 加密器
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    IvParameterSpec ivSpec = new IvParameterSpec(UserRepository.getAesCrypto().getIv());
                    cipher.init(Cipher.ENCRYPT_MODE, UserRepository.getAesCrypto().getKey(), ivSpec);

                    // 加密数据
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonString = objectMapper.writeValueAsString(fileListVO); // 将对象序列化为 JSON 字符串
                    byte[] encryptedData = cipher.doFinal(jsonString.getBytes());

                    // 发送加密数据
                    session.sendMessage(new TextMessage("List#" + Base64.getEncoder().encodeToString(encryptedData)));
                } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException |
                         InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException |
                         InvalidKeyException e) {
                    try {
                        session.close(CloseStatus.SERVER_ERROR);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }
}
