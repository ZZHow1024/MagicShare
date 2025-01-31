package com.zzhow.magicshare.websocket;

import com.zzhow.magicshare.repository.UserRepository;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;

/**
 * @author ZZHow
 * @date 2025/1/30
 */
public class UserWebSocketHandler extends TextWebSocketHandler {
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UserRepository.removeUser(session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        if (message.getPayload().startsWith("ClientHello")) {
            try {
                session.sendMessage(new TextMessage("ServerHello#" + new String(Base64.getEncoder().encode(convertPublicKeyToPEM(UserRepository.getKeyPair().getPublic()).getBytes()))));
            } catch (IOException e) {
                try {
                    session.close(SERVER_ERROR);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else if (message.getPayload().startsWith("Syn")) {
            try {
                byte[] encryptedPassword = Base64.getDecoder().decode(message.getPayload().split("#")[1]);
                Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
                rsaCipher.init(Cipher.DECRYPT_MODE, UserRepository.getKeyPair().getPrivate());
                String password = new String(rsaCipher.doFinal(encryptedPassword));

                if (UserRepository.getPassword().equals(password)) {
                    UserRepository.addUser(session.getId());
                    session.sendMessage(new TextMessage("Syn#200"));
                } else {
                    session.sendMessage(new TextMessage("Syn#401"));
                }
            } catch (InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException |
                     NoSuchAlgorithmException | IOException |
                     BadPaddingException e) {
                e.printStackTrace();
                try {
                    session.close(SERVER_ERROR);
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
