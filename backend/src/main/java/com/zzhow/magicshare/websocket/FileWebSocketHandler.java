package com.zzhow.magicshare.websocket;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.DownloadUserRepository;
import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.repository.UserRepository;
import com.zzhow.magicshare.util.CryptoUtil;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * 加密下载 WebSocket 处理器
 * <p>
 * 消息协议统一为 TYPE#payload（# 为唯一分隔符）：
 * <ul>
 *     <li>DOWNLOAD_AUTH#Base64(加密的token)#Base64(加密的fileId) —— 认证并统计分块；回复 CHUNK_COUNT#Base64(加密的块数)</li>
 *     <li>DOWNLOAD_START#Base64(加密的fileId) —— 开始传输；加密分块以 CHUNK#Base64(IV‖密文) 逐块发送，结束后回复 FINISHED</li>
 *     <li>错误统一为 ERROR#原因，并以 CLOSE_STATUS 关闭连接</li>
 * </ul>
 *
 * @author ZZHow
 * create 2025/01/16
 * update 2026/11/23
 */
public class FileWebSocketHandler extends AbstractWebSocketHandler {
    private static final int CHUNK_SIZE = 64 * 1024;
    private final CryptoUtil cryptoUtil = CryptoUtil.getInstance();
    private final DownloadUserRepository downloadUserRepository = DownloadUserRepository.getInstance();

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        downloadUserRepository.removeUser(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String type = message.getPayload().split("#", 2)[0];

        switch (type) {
            case "DOWNLOAD_AUTH" -> handleDownloadAuth(session, message.getPayload());
            case "DOWNLOAD_START" -> handleDownloadStart(session, message.getPayload());
            default -> error(session, "UNKNOWN_MESSAGE_TYPE");
        }
    }

    /**
     * 下载认证：校验一次性 downloadId，统计分块数
     */
    private void handleDownloadAuth(WebSocketSession session, String payload) {
        try {
            String[] authParts = new String(cryptoUtil.decryptAes(Base64.getDecoder().decode(splitArg(payload, 1)))).split("#");
            String fileId = new String(cryptoUtil.decryptAes(Base64.getDecoder().decode(splitArg(payload, 2))));

            // 验证 sessionId#downloadId
            if (!UserRepository.verifyDownloadId(authParts[0], authParts[1])) {
                error(session, "UNAUTHORIZED");
                closeQuietly(session, CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            int index = indexOfFile(fileId);
            if (index == -1) {
                error(session, "FILE_NOT_FOUND");
                closeQuietly(session, CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            // 按真实字节长度计算分块数（小于 1KB 的文件至少 1 块）
            long fileLength = new File(FileRepository.getBasePath() + fileAt(index).getPath()).length();
            int block = (int) Math.max(1, Math.ceil(fileLength / (double) CHUNK_SIZE));

            downloadUserRepository.addUser(session.getId());
            byte[] encryptedData = cryptoUtil.encryptAes(String.valueOf(block));
            send(session, "CHUNK_COUNT#" + Base64.getEncoder().encodeToString(encryptedData));
        } catch (Exception e) {
            error(session, "DECRYPTION_FAILED: " + e.getMessage());
        }
    }

    /**
     * 开始传输：逐块加密发送（每块随机 IV：IV || ciphertext）
     */
    private void handleDownloadStart(WebSocketSession session, String payload) {
        // 身份验证：仅已通过 DOWNLOAD_AUTH 的连接可开始传输
        if (!downloadUserRepository.containsUser(session.getId())) {
            error(session, "UNAUTHORIZED");
            closeQuietly(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        try {
            String fileId = new String(cryptoUtil.decryptAes(Base64.getDecoder().decode(splitArg(payload, 1))));

            int index = indexOfFile(fileId);
            if (index == -1) {
                error(session, "FILE_NOT_FOUND");
                closeQuietly(session, CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            String filePath = FileRepository.getBasePath() + fileAt(index).getPath();
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    // 传输加密分块（每块使用随机 IV：IV || ciphertext）
                    byte[] plain = bytesRead == CHUNK_SIZE ? buffer : Arrays.copyOf(buffer, bytesRead);
                    byte[] encryptedData = cryptoUtil.encryptAes(plain);
                    send(session, "CHUNK#" + Base64.getEncoder().encodeToString(encryptedData));
                }

                // 发送结束
                downloadUserRepository.removeUser(session.getId());
                send(session, "FINISHED");
            } catch (IOException e) {
                error(session, "IO_ERROR: " + e.getMessage());
                closeQuietly(session, CloseStatus.SERVER_ERROR);
            }
        } catch (Exception e) {
            error(session, "DECRYPTION_FAILED: " + e.getMessage());
        }
    }

    private FileDetail fileAt(int index) {
        return FileRepository.getFiles().get(index);
    }

    private int indexOfFile(String fileId) {
        return FileRepository.getFiles().indexOf(new FileDetail(fileId));
    }

    private String splitArg(String payload, int index) {
        String[] parts = payload.split("#", -1);
        return parts.length > index ? parts[index] : "";
    }

    private void send(WebSocketSession session, String payload) {
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void error(WebSocketSession session, String reason) {
        send(session, "ERROR#" + reason);
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}