package com.zzhow.magicshare.websocket;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.DownloadUserRepository;
import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.repository.UserRepository;
import com.zzhow.magicshare.util.CryptoUtil;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.BufferedInputStream;
import java.io.File;
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
    private static final int CHUNK_SIZE = 64 * 1024;
    private final CryptoUtil cryptoUtil = CryptoUtil.getInstance();
    private final DownloadUserRepository downloadUserRepository = DownloadUserRepository.getInstance();

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        downloadUserRepository.removeUser(session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 文本消息格式：a,sessionId#downloadId,fileId（控制消息）；加密分块以二进制帧发送
        if (message.getPayload().charAt(0) == 'a') {
            String[] split = message.getPayload().split(",");

            // AES 解密数据
            String[] data = new String(cryptoUtil.decryptAes(Base64.getDecoder().decode(split[1]))).split("#");
            String fileId = new String(cryptoUtil.decryptAes(Base64.getDecoder().decode(split[2])));

            // 验证 sessionId#downloadId
            if (!UserRepository.verifyDownloadId(data[0], data[1])) {
                session.sendMessage(new TextMessage("Not Acceptable"));
                session.close(CloseStatus.NOT_ACCEPTABLE);

                return;
            }

            // 计算分块数量
            List<FileDetail> files = FileRepository.getFiles();
            int index = files.indexOf(new FileDetail(fileId));
            if (index == -1) {
                session.sendMessage(new TextMessage("Not Found"));
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }
            // 按真实字节长度计算分块数（FileDetail.size 为 KB 近似值，小于 1KB 的文件会算出 0 块）
            long fileLength = new File(FileRepository.getBasePath() + files.get(index).getPath()).length();
            int block = (int) Math.max(1, Math.ceil(fileLength / (double) CHUNK_SIZE));

            // AES 加密数据
            byte[] encryptedData = cryptoUtil.encryptAes((block + ""));

            downloadUserRepository.addUser(session.getId());
            session.sendMessage(new TextMessage("block#" + Base64.getEncoder().encodeToString(encryptedData)));
        } else if ((message.getPayload().charAt(0) == 'b')) { // 文本消息格式：b,fileId
            // 身份验证
            if (!downloadUserRepository.containsUser(session.getId())) {
                session.sendMessage(new TextMessage("Not Acceptable"));
                session.close(CloseStatus.NOT_ACCEPTABLE);

                return;
            }

            String[] split = message.getPayload().split(",");

            // AES 解密数据
            String fileId = new String(cryptoUtil.decryptAes(Base64.getDecoder().decode(split[1])));

            // 构建文件路径
            List<FileDetail> files = FileRepository.getFiles();
            int index = files.indexOf(new FileDetail(fileId));
            if (index == -1) {
                session.sendMessage(new TextMessage("Not Found"));
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            String filePath = FileRepository.getBasePath() + files.get(index).getPath();

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    // 通过 WebSocket 传输文件数据（每块使用随机 IV：IV || ciphertext，Base64 文本帧）
                    byte[] plain = bytesRead == CHUNK_SIZE ? buffer : Arrays.copyOf(buffer, bytesRead);
                    byte[] encryptedData = cryptoUtil.encryptAes(plain);
                    session.sendMessage(new TextMessage(new String(Base64.getEncoder().encode(encryptedData))));
                }

                // 发送结束
                downloadUserRepository.removeUser(session.getId());
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