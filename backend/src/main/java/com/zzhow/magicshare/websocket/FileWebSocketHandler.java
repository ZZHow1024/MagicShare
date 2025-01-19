package com.zzhow.magicshare.websocket;

import com.zzhow.magicshare.pojo.entity.AesCrypto;
import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.AesKeyRepository;
import com.zzhow.magicshare.repository.FileRepository;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * @author ZZHow
 * @date 2025/01/16
 */
public class FileWebSocketHandler extends TextWebSocketHandler {
    private static final int CHUNK_SIZE = 8192;

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        AesKeyRepository.delete(session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 消息格式：a,publicKey,fileId
        if (message.getPayload().charAt(0) == 'a') {
            String[] split = message.getPayload().split(",");

            // 计算分块数量
            List<FileDetail> files = FileRepository.getFiles();
            int index = files.indexOf(new FileDetail(split[2]));
            if (index == -1) {
                session.sendMessage(new TextMessage("Not found"));
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }
            int block = (int) Math.ceil(files.get(index).getSize() / (CHUNK_SIZE / 1024.0));

            // Base64 解码
            String key = new String(Base64.getDecoder().decode(split[1]));

            // 去除 PEM 格式的头尾
            key = key.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);

            // 加载公钥
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            // 生成随机 AES 密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // 选择 AES 256 位密钥
            SecretKey aesKey = keyGenerator.generateKey();

            // 生成随机 IV（初始化向量）
            byte[] iv = new byte[16]; // AES 块大小 128 位
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv); // 填充随机数据到 iv 数组

            // 使用 RSA 加密 AES 密钥
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

            AesKeyRepository.set(session.getId(), aesKey, iv);
            session.sendMessage(new TextMessage("key#" + Base64.getEncoder().encodeToString(encryptedAesKey) + ",iv#" + Base64.getEncoder().encodeToString(iv) + ",block#" + block));

            // 消息格式：b,fileId
        } else if ((message.getPayload().charAt(0) == 'b')) {
            String[] split = message.getPayload().split(",");

            // 构建文件路径
            List<FileDetail> files = FileRepository.getFiles();
            int index = files.indexOf(new FileDetail(split[1]));
            if (index == -1) {
                session.sendMessage(new TextMessage("Not found"));
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            String filePath = FileRepository.getBasePath() + files.get(index).getPath();

            // 初始化 AES 加密器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            AesCrypto aesCrypto = AesKeyRepository.get(session.getId());
            IvParameterSpec ivSpec = new IvParameterSpec(aesCrypto.getIv());
            cipher.init(Cipher.ENCRYPT_MODE, aesCrypto.getKey(), ivSpec);

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    // 通过 WebSocket 传输文件数据
                    byte[] encryptedData;
                    if (bytesRead == CHUNK_SIZE)
                        encryptedData = cipher.doFinal(buffer);
                    else {
                        byte[] newBuffer = Arrays.copyOf(buffer, bytesRead);
                        encryptedData = cipher.doFinal(newBuffer);
                    }
                    session.sendMessage(new TextMessage(new String(Base64.getEncoder().encode(encryptedData))));
                }

                // 发送结束
                session.sendMessage(new TextMessage("fin"));
            } catch (IOException e) {
                session.sendMessage(new TextMessage("Error: " + e.getMessage()));
                session.close();
            }
        } else {
            session.sendMessage(new TextMessage("Bad Request"));
            session.close();
        }
    }
}