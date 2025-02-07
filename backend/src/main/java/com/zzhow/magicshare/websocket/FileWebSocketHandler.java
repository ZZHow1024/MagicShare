package com.zzhow.magicshare.websocket;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.AesKeyRepository;
import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.repository.UserRepository;
import com.zzhow.magicshare.util.CryptoUtil;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * @author ZZHow
 * @date 2025/01/16
 */
public class FileWebSocketHandler extends TextWebSocketHandler {
    private static final int CHUNK_SIZE = 8192;
    private final CryptoUtil cryptoUtil = CryptoUtil.getInstance();

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        AesKeyRepository.delete(session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 消息格式：a,sessionId#downloadId,fileId
        if (message.getPayload().charAt(0) == 'a') {
            String[] split = message.getPayload().split(",");

            // AES 解密数据
            String[] data = new String(cryptoUtil.decryptAes(Base64.getDecoder().decode(split[1]))).split("#");
            String fileId = new String(cryptoUtil.decryptAes(Base64.getDecoder().decode(split[2])));

            // 验证 sessionId#downloadId
            if (!UserRepository.verifyDownloadId(data[0], data[1])) {
                session.sendMessage(new TextMessage("Not found"));
                session.close(CloseStatus.NOT_ACCEPTABLE);

                return;
            }

            // 计算分块数量
            List<FileDetail> files = FileRepository.getFiles();
            int index = files.indexOf(new FileDetail(fileId));
            if (index == -1) {
                session.sendMessage(new TextMessage("Not found"));
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }
            int block = (int) Math.ceil(files.get(index).getSize() / (CHUNK_SIZE / 1024.0));

            // AES 加密数据
            byte[] encryptedData = cryptoUtil.encryptAes((block + ""));

            session.sendMessage(new TextMessage("block#" + Base64.getEncoder().encodeToString(encryptedData)));
        } else if ((message.getPayload().charAt(0) == 'b')) { // 消息格式：b,fileId
            String[] split = message.getPayload().split(",");

            // AES 解密数据
            String fileId = new String(cryptoUtil.decryptAes(Base64.getDecoder().decode(split[1])));

            // 构建文件路径
            List<FileDetail> files = FileRepository.getFiles();
            int index = files.indexOf(new FileDetail(fileId));
            if (index == -1) {
                session.sendMessage(new TextMessage("Not found"));
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            String filePath = FileRepository.getBasePath() + files.get(index).getPath();

            // 初始化 AES 加密器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(UserRepository.getAesCrypto().getIv());
            cipher.init(Cipher.ENCRYPT_MODE, UserRepository.getAesCrypto().getKey(), ivSpec);

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