package com.zzhow.magicshare.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzhow.magicshare.pojo.vo.FileListVO;
import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.repository.UploadRepository;
import com.zzhow.magicshare.repository.UserRepository;
import com.zzhow.magicshare.service.FileService;
import com.zzhow.magicshare.util.CryptoUtil;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Arrays;
import java.util.Base64;

/**
 * 用户连接 WebSocket 处理器
 * <p>
 * 消息协议统一为 TYPE#payload（# 为唯一分隔符，TYPE 全大写语义化）：
 * <ul>
 *     <li>CLIENT_HELLO#Base64(客户端公钥) —— 建立连接；无密码时直接分配会话</li>
 *     <li>回复 SESSION_ID#Base64(加密的sessionId)，或（有密码时）CHALLENGE#Base64(加密的sessionId)#Base64(加密的nonce)</li>
 *     <li>AUTH#Base64(SHA256(sessionId+password+nonce)) —— 密码认证；回复 AUTH_OK 或 AUTH_REJECTED</li>
 *     <li>EXCHANGE#Base64(客户端公钥) —— 回复 EXCHANGE#Base64(加密的AES密钥)#Base64(IV)</li>
 *     <li>FILE_LIST[ #Base64(shareId)] —— 回复 FILE_LIST#Base64(加密的文件列表JSON)</li>
 *     <li>DOWNLOAD_ID#mode(QUICK|ENCRYPTED) —— 回复 DOWNLOAD_ID#mode#Base64(加密的一次性下载ID)</li>
 *     <li>UPLOAD_ID —— 回复 UPLOAD_ID#Base64(加密的上传ID)</li>
 *     <li>ARCHIVE_ID —— 回复 ARCHIVE_ID#Base64(加密的归档ID)</li>
 *     <li>错误统一为 ERROR#原因，并以 CLOSE_STATUS 关闭连接</li>
 * </ul>
 *
 * @author ZZHow
 * create 2025/1/30
 * update 2026/11/23
 */
public class UserWebSocketHandler extends TextWebSocketHandler {
    private final FileService fileService;
    private final CryptoUtil cryptoUtil = CryptoUtil.getInstance();

    public UserWebSocketHandler(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UserRepository.removeUser(session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String type = message.getPayload().split("#", 2)[0];

        switch (type) {
            case "CLIENT_HELLO" -> handleClientHello(session, message.getPayload());
            case "AUTH" -> handleAuth(session, message.getPayload());
            case "EXCHANGE" -> handleExchange(session, message.getPayload());
            case "FILE_LIST" -> handleFileList(session, message.getPayload());
            case "DOWNLOAD_ID" -> handleDownloadId(session, message.getPayload());
            case "UPLOAD_ID" -> handleUploadId(session);
            case "ARCHIVE_ID" -> handleArchiveId(session);
            default -> {
                System.out.println("[PROTO] unknown type = [" + type + "] payloadHead=[" + message.getPayload().substring(0, Math.min(30, message.getPayload().length())) + "]");
                reject(session, "UNKNOWN_MESSAGE_TYPE");
            }
        }
    }

    /**
     * 建立连接：无密码时直接分配会话；有密码时签发随机挑战
     */
    private void handleClientHello(WebSocketSession session, String payload) {
        String publicKey = new String(Base64.getDecoder().decode(arg(payload, 1)));
        byte[] encryptedSessionId = cryptoUtil.encryptRsa(publicKey, session.getId());

        if (UserRepository.getPassword() == null) {
            UserRepository.addUser(session.getId(), session);
            send(session, "SESSION_ID#" + Base64.getEncoder().encodeToString(encryptedSessionId));
        } else {
            // 签发随机挑战，防止认证重放
            String nonce = cryptoUtil.generateChallenge();
            UserRepository.putChallenge(session.getId(), nonce);
            byte[] encryptedNonce = cryptoUtil.encryptRsa(publicKey, nonce);
            send(session, "CHALLENGE#" + Base64.getEncoder().encodeToString(encryptedSessionId)
                    + "#" + Base64.getEncoder().encodeToString(encryptedNonce));
        }
    }

    /**
     * 密码认证：校验 SHA256(sessionId + password + 随机挑战)
     */
    private void handleAuth(WebSocketSession session, String payload) {
        byte[] receivedHash = Base64.getDecoder().decode(arg(payload, 1));
        String nonce = UserRepository.getChallenge(session.getId());
        String authSource = session.getId() + UserRepository.getPassword() + (nonce == null ? "" : nonce);
        byte[] expectedHash = cryptoUtil.sha256(authSource);

        if (nonce != null && Arrays.equals(expectedHash, receivedHash)) {
            System.out.println("[PROTO] AUTH_OK nonce=" + nonce);
            UserRepository.addUser(session.getId(), session);
            UserRepository.removeChallenge(session.getId());
            send(session, "AUTH_OK");
        } else {
            System.out.println("[PROTO] AUTH_REJECTED nonce=" + nonce + " receivedLen=" + receivedHash.length);
            send(session, "AUTH_REJECTED");
        }
    }

    /**
     * 密钥交换：使用客户端公钥加密会话 AES 密钥
     */
    private void handleExchange(WebSocketSession session, String payload) {
        if (!UserRepository.containsUser(session.getId())) {
            reject(session, "NOT_AUTHENTICATED");
            return;
        }

        String publicKey = new String(Base64.getDecoder().decode(arg(payload, 1)));
        byte[] encryptedAesKey = cryptoUtil.encryptRsa(publicKey, UserRepository.getAesCrypto().getKey().getEncoded());
        send(session, "EXCHANGE#" + Base64.getEncoder().encodeToString(encryptedAesKey)
                + "#" + Base64.getEncoder().encodeToString(UserRepository.getAesCrypto().getIv()));
    }

    /**
     * 文件列表：客户端 shareId 与当前分享一致时不重复推送
     */
    private void handleFileList(WebSocketSession session, String payload) {
        if (!UserRepository.containsUser(session.getId())) {
            reject(session, "NOT_AUTHENTICATED");
            return;
        }

        String encodedShareId = arg(payload, 1);
        String shareId = encodedShareId.isEmpty() ? "" : new String(Base64.getDecoder().decode(encodedShareId));
        if (fileService.checkCurrentShare(shareId))
            return;

        FileListVO fileListVO = new FileListVO(FileRepository.getUuid(), FileRepository.size(), FileRepository.getFiles());
        byte[] encryptedData = cryptoUtil.encryptAes(fileListVOJson(fileListVO));
        send(session, "FILE_LIST#" + Base64.getEncoder().encodeToString(encryptedData));
    }

    /**
     * 签发一次性下载 ID
     */
    private void handleDownloadId(WebSocketSession session, String payload) {
        if (!UserRepository.containsUser(session.getId())) {
            reject(session, "NOT_AUTHENTICATED");
            return;
        }

        String mode = arg(payload, 1);
        String downloadId = UserRepository.generateDownloadId(session.getId());
        byte[] encryptedData = cryptoUtil.encryptAes(downloadId);
        send(session, "DOWNLOAD_ID#" + mode + "#" + Base64.getEncoder().encodeToString(encryptedData));
    }

    /**
     * 签发上传 ID
     */
    private void handleUploadId(WebSocketSession session) {
        if (!UserRepository.containsUser(session.getId())) {
            reject(session, "NOT_AUTHENTICATED");
            return;
        }

        String uploadId = UploadRepository.generateUploadId(session.getId());
        byte[] encryptedData = cryptoUtil.encryptAes(uploadId);
        send(session, "UPLOAD_ID#" + Base64.getEncoder().encodeToString(encryptedData));
    }

    /**
     * 签发归档 ID
     */
    private void handleArchiveId(WebSocketSession session) {
        System.out.println("[PROTO] ARCHIVE_ID requested by " + session.getId());
        if (!UserRepository.containsUser(session.getId())) {
            reject(session, "NOT_AUTHENTICATED");
            return;
        }

        String archiveId = UserRepository.generateArchiveId(session.getId());
        byte[] encryptedData = cryptoUtil.encryptAes(archiveId);
        send(session, "ARCHIVE_ID#" + Base64.getEncoder().encodeToString(encryptedData));
    }

    private String fileListVOJson(FileListVO fileListVO) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(fileListVO);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize file list", e);
        }
    }

    /**
     * 未认证时的统一拒绝：发送错误并关闭连接
     */
    private void reject(WebSocketSession session, String reason) {
        send(session, "ERROR#" + reason);
        try {
            session.close(CloseStatus.NOT_ACCEPTABLE);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void send(WebSocketSession session, String payload) {
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取 TYPE#payload 中第 index 段（0 为 TYPE）
     */
    private String arg(String payload, int index) {
        String[] parts = payload.split("#", -1);
        return parts.length > index ? parts[index] : "";
    }
}