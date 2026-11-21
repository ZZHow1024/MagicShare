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

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author ZZHow
 * @date 2025/1/30
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
        if (message.getPayload().startsWith("ClientHello")) {
            try {
                // Base64 解码
                String publicKey = new String(Base64.getDecoder().decode(message.getPayload().split("#")[1]));

                // RSA 加密
                byte[] encryptedSessionId = cryptoUtil.encryptRsa(publicKey, session.getId());

                if (UserRepository.getPassword() == null) {
                    UserRepository.addUser(session.getId(), session);
                    session.sendMessage(new TextMessage("Syn#202#" + Base64.getEncoder().encodeToString(encryptedSessionId)));
                } else {
                    // 签发随机挑战，防止认证重放
                    String nonce = cryptoUtil.generateChallenge();
                    UserRepository.putChallenge(session.getId(), nonce);
                    byte[] encryptedNonce = cryptoUtil.encryptRsa(publicKey, nonce);
                    session.sendMessage(new TextMessage("ServerHello#" + Base64.getEncoder().encodeToString(encryptedSessionId)
                            + "#" + Base64.getEncoder().encodeToString(encryptedNonce)));
                }
            } catch (IOException e) {
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else if (message.getPayload().startsWith("Syn")) {
            byte[] decode = Base64.getDecoder().decode(message.getPayload().split("#")[1]);

            try {
                // 认证值 = SHA256(sessionId + password + 随机挑战)
                String nonce = UserRepository.getChallenge(session.getId());
                String data = session.getId() + UserRepository.getPassword() + (nonce == null ? "" : nonce);
                byte[] hashBytes = cryptoUtil.sha256(data);

                if (nonce != null && Arrays.equals(hashBytes, decode)) {
                    UserRepository.addUser(session.getId(), session);
                    UserRepository.removeChallenge(session.getId());
                    session.sendMessage(new TextMessage("Syn#200"));
                } else {
                    session.sendMessage(new TextMessage("Syn#401"));
                }
            } catch (IOException e) {
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else if (message.getPayload().startsWith("Exc")) {
            try {
                if (!UserRepository.containsUser(session.getId())) {
                    try {
                        session.close(CloseStatus.NOT_ACCEPTABLE);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return;
                }

                // Base64 解码
                String publicKey = new String(Base64.getDecoder().decode(message.getPayload().split("#")[1]));

                // 使用 RSA 加密 AES 密钥
                byte[] encryptedAesKey = cryptoUtil.encryptRsa(publicKey, UserRepository.getAesCrypto().getKey().getEncoded());

                // 返回加密的 AES 密钥和 IV
                session.sendMessage(new TextMessage("Exc#" + Base64.getEncoder().encodeToString(encryptedAesKey) + "#" + Base64.getEncoder().encodeToString(UserRepository.getAesCrypto().getIv())));
            } catch (IOException e) {
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else if (message.getPayload().startsWith("List")) {
            if (!UserRepository.containsUser(session.getId())) {
                try {
                    session.close(CloseStatus.NOT_ACCEPTABLE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return;
            }

            String[] split = message.getPayload().split("#");
            String shareId = "";
            if (split.length > 1)
                shareId = new String(Base64.getDecoder().decode(split[1].getBytes()));
            if (!fileService.checkCurrentShare(shareId)) {
                FileListVO fileListVO = new FileListVO(FileRepository.getUuid(), FileRepository.size(), FileRepository.getFiles());

                try {
                    // AES 加密数据
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonString = objectMapper.writeValueAsString(fileListVO); // 将对象序列化为 JSON 字符串
                    byte[] encryptedData = cryptoUtil.encryptAes(jsonString.getBytes());

                    // 发送加密数据
                    session.sendMessage(new TextMessage("List#" + Base64.getEncoder().encodeToString(encryptedData)));
                } catch (IOException e) {
                    try {
                        session.close(CloseStatus.SERVER_ERROR);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        } else if (message.getPayload().startsWith("Download")) {
            if (!UserRepository.containsUser(session.getId())) {
                try {
                    session.close(CloseStatus.NOT_ACCEPTABLE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return;
            }

            String downloadId = UserRepository.generateDownloadId(session.getId());
            try {
                // AES 加密数据
                byte[] encryptedData = cryptoUtil.encryptAes(downloadId);
                session.sendMessage(new TextMessage("Download#" + message.getPayload().split("#")[1] + "#" + Base64.getEncoder().encodeToString(encryptedData)));
            } catch (IOException e) {
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else if (message.getPayload().startsWith("Upload")) {
            if (!UserRepository.containsUser(session.getId())) {
                try {
                    session.close(CloseStatus.NOT_ACCEPTABLE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return;
            }

            String uploadId = UploadRepository.generateUploadId(session.getId());
            try {
                // AES 加密数据
                byte[] encryptedData = cryptoUtil.encryptAes(uploadId);
                session.sendMessage(new TextMessage("Upload#" + message.getPayload().split("#")[1] + "#" + Base64.getEncoder().encodeToString(encryptedData)));
            } catch (IOException e) {
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else if (message.getPayload().startsWith("Archive")) {
            if (!UserRepository.containsUser(session.getId())) {
                try {
                    session.close(CloseStatus.NOT_ACCEPTABLE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return;
            }

            String archiveId = UserRepository.generateArchiveId(session.getId());
            try {
                // AES 加密数据
                byte[] encryptedData = cryptoUtil.encryptAes(archiveId);
                session.sendMessage(new TextMessage("Archive#" + message.getPayload().split("#")[1] + "#" + Base64.getEncoder().encodeToString(encryptedData)));
            } catch (IOException e) {
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
