package com.zzhow.magicshare.websocket;

import com.zzhow.magicshare.repository.UserRepository;
import com.zzhow.magicshare.service.FileService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;

/**
 * @author ZZHow
 * @date 2025/1/30
 */
public class UserWebSocketHandler extends TextWebSocketHandler {
    private FileService fileService;

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
                // 去除 PEM 格式的头尾
                String publicKeyContent = publicKey.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s+", "");
                byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);

                // 加载公钥
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey clientPublicKey = keyFactory.generatePublic(spec);

                // 使用 RSA 加密 SessionId
                Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
                rsaCipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);
                byte[] encryptedAesKey = rsaCipher.doFinal(session.getId().getBytes());

                if (UserRepository.getPassword() == null) {
                    UserRepository.addUser(session.getId(), session);
                    session.sendMessage(new TextMessage("Syn#202#" + Base64.getEncoder().encodeToString(encryptedAesKey)));
                } else
                    session.sendMessage(new TextMessage("ServerHello#" + Base64.getEncoder().encodeToString(encryptedAesKey)));
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException |
                     IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
                try {
                    session.close(SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else if (message.getPayload().startsWith("Syn")) {
            byte[] decode = Base64.getDecoder().decode(message.getPayload().split("#")[1]);

            try {
                String data = session.getId() + UserRepository.getPassword();
                byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(data.getBytes());

                if (Arrays.equals(hashBytes, decode)) {
                    UserRepository.addUser(session.getId(), session);
                    session.sendMessage(new TextMessage("Syn#200"));
                } else {
                    session.sendMessage(new TextMessage("Syn#401"));
                }
            } catch (NoSuchAlgorithmException | IOException e) {
                try {
                    session.close(SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else if (message.getPayload().startsWith("Exc")) {
            try {
                // Base64 解码
                String publicKey = new String(Base64.getDecoder().decode(message.getPayload().split("#")[1]));
                // 去除 PEM 格式的头尾
                String publicKeyContent = publicKey.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s+", "");
                byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);

                // 加载公钥
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey clientPublicKey = keyFactory.generatePublic(spec);

                // 使用 RSA 加密 AES 密钥
                Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
                rsaCipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);
                byte[] encryptedAesKey = rsaCipher.doFinal(UserRepository.getAesCrypto().getKey().getEncoded());

                // 返回加密的 AES 密钥和 IV
                session.sendMessage(new TextMessage("Exc#" + Base64.getEncoder().encodeToString(encryptedAesKey) + "#" + Base64.getEncoder().encodeToString(UserRepository.getAesCrypto().getIv())));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException |
                     IllegalBlockSizeException | BadPaddingException | IOException | InvalidKeyException e) {
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
            }

            String[] split = message.getPayload().split("#");
            String shareId = "";
            if (split.length > 1)
                shareId = new String(Base64.getDecoder().decode(split[1].getBytes()));
            if (!fileService.checkCurrentShare(shareId))
                try {
                    session.sendMessage(new TextMessage("List#" + fileService.getFileList()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        } else if (message.getPayload().startsWith("Download")) {
            String downloadId = UserRepository.generateDownloadId(session.getId());
            try {
                // 初始化 AES 加密器
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec ivSpec = new IvParameterSpec(UserRepository.getAesCrypto().getIv());
                cipher.init(Cipher.ENCRYPT_MODE, UserRepository.getAesCrypto().getKey(), ivSpec);

                // 加密数据
                byte[] encryptedData = cipher.doFinal(downloadId.getBytes());
                session.sendMessage(new TextMessage("Download#" + message.getPayload().split("#")[1] + "#" + Base64.getEncoder().encodeToString(encryptedData)));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                     InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException |
                     IOException e) {
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * 将公钥转换为 PEM 格式
     */
    public String convertPublicKeyToPEM(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        String base64PublicKey = Base64.getEncoder().encodeToString(publicKeyBytes);

        return "-----BEGIN PUBLIC KEY-----\n" +
                base64PublicKey.replaceAll("(.{64})", "$1\n") + // 每 64 字符换行
                "\n-----END PUBLIC KEY-----";
    }
}
