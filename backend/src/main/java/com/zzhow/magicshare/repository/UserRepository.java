package com.zzhow.magicshare.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzhow.magicshare.util.CryptoUtil;
import com.zzhow.magicshare.pojo.entity.AesCrypto;
import com.zzhow.magicshare.pojo.entity.User;
import com.zzhow.magicshare.pojo.vo.FileListVO;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ZZHow
 * @date 2025/1/30
 */
public class UserRepository {
    private static AesCrypto aesCrypto = null;
    private static KeyPair keyPair = null;
    private static String password = null;
    private static final Map<String, User> users = new HashMap<>();

    // 连接密码认证的随机挑战（sessionId -> nonce，防止认证重放）
    private static final Map<String, String> challengeNonces = new HashMap<>();

    public static void putChallenge(String sessionId, String nonce) {
        challengeNonces.put(sessionId, nonce);
    }

    public static String getChallenge(String sessionId) {
        return challengeNonces.get(sessionId);
    }

    public static void removeChallenge(String sessionId) {
        challengeNonces.remove(sessionId);
    }

    // 一次性 ID 延迟清理专用守护线程池（避免每次验证都新建线程池导致线程泄漏）
    private static final ScheduledExecutorService ID_CLEANUP_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "magicshare-id-cleanup");
                thread.setDaemon(true);
                return thread;
            });

    static {
        initialize();
    }

    public static void initialize() {
        // 清空用户列表
        users.clear();

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
            UserRepository.keyPair = keyPairGenerator.generateKeyPair();
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
        users.put(sessionId, new User(session));
        ConnectionCountBinding.setCount(users.size() + "");
    }

    public static void removeUser(String sessionId) {
        users.remove(sessionId);
        ConnectionCountBinding.setCount(users.size() + "");
    }

    public static User getUser(String sessionId) {
        return users.get(sessionId);
    }

    public static boolean containsUser(String sessionId) {
        return users.get(sessionId) != null;
    }

    public static String generateDownloadId(String sessionId) {
        String downloadId = UUID.randomUUID().toString();
        users.get(sessionId).getDownloadIdList().put(downloadId, true);

        return downloadId;
    }

    public static boolean verifyDownloadId(String sessionId, String downloadId) {
        if (users.get(sessionId) != null && users.get(sessionId).getDownloadIdList().containsKey(downloadId)) {
            if (users.get(sessionId).getDownloadIdList().get(downloadId)) {
                users.get(sessionId).getDownloadIdList().put(downloadId, false);

                // 延迟 10s 删除 downloadId
                ID_CLEANUP_EXECUTOR.schedule(() -> {
                    if (!users.containsKey(sessionId)) return;
                    users.get(sessionId).getDownloadIdList().remove(downloadId);
                }, 10, TimeUnit.SECONDS);
            }

            return true;
        }

        return false;
    }

    public static String generateArchiveId(String sessionId) {
        String archiveId = UUID.randomUUID().toString();
        users.get(sessionId).getArchiveIdList().put(archiveId, true);

        return archiveId;
    }

    public static boolean verifyArchiveId(String sessionId, String archiveId) {
        if (users.get(sessionId) != null && users.get(sessionId).getArchiveIdList().containsKey(archiveId)) {
            if (users.get(sessionId).getArchiveIdList().get(archiveId)) {
                users.get(sessionId).getArchiveIdList().put(archiveId, false);

                // 延迟 10s 删除 archiveId
                ID_CLEANUP_EXECUTOR.schedule(() -> {
                    if (!users.containsKey(sessionId)) return;
                    users.get(sessionId).getArchiveIdList().remove(archiveId);
                }, 10, TimeUnit.SECONDS);
            }

            return true;
        }

        return false;
    }

    public static void sendListToAll() {
        CryptoUtil cryptoUtil = CryptoUtil.getInstance();
        Set<String> strings = users.keySet();
        for (String sessionId : strings) {
            User user = users.get(sessionId);
            if (user != null && user.getSession().isOpen()) {
                FileListVO fileListVO = new FileListVO(FileRepository.getUuid(), FileRepository.size(), FileRepository.getFiles());

                try {
                    // 加密数据
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonString = objectMapper.writeValueAsString(fileListVO); // 将对象序列化为 JSON 字符串
                    byte[] encryptedData = cryptoUtil.encryptAes(jsonString);

                    // 发送加密数据
                    user.getSession().sendMessage(new TextMessage("List#" + Base64.getEncoder().encodeToString(encryptedData)));
                } catch (IOException e) {
                    try {
                        user.getSession().close(CloseStatus.SERVER_ERROR);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }
}
