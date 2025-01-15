package com.zzhow.magicshare.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzhow.magicshare.pojo.vo.CryptoVO;
import com.zzhow.magicshare.pojo.vo.FileListVO;
import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
@Service
public class FileServiceImpl implements FileService {
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 获得分享的文件列表
     *
     * @param publicKey 公钥
     * @return 分享的文件列表
     */
    @Override
    public CryptoVO getFileList(String publicKey) {
        FileListVO fileListVO = new FileListVO(FileRepository.getUuid(), FileRepository.size(), FileRepository.getFiles());

        try {
            // Base64 解码
            publicKey = new String(Base64.getDecoder().decode(publicKey));
            // 去除 PEM 格式的头尾
            String publicKeyContent = publicKey.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);

            // 加载公钥
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey clientPublicKey = keyFactory.generatePublic(spec);

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
            rsaCipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);
            byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

            // 初始化 AES 加密器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);

            // 加密数据
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(fileListVO); // 将对象序列化为 JSON 字符串
            byte[] encryptedData = cipher.doFinal(jsonString.getBytes());

            // 返回加密的 AES 密钥、IV 和加密数据
            return new CryptoVO(Base64.getEncoder().encodeToString(encryptedAesKey), Base64.getEncoder().encodeToString(iv), Base64.getEncoder().encodeToString(encryptedData));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException |
                 InvalidKeySpecException | BadPaddingException | InvalidKeyException | JsonProcessingException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 检查是否为当前分享
     *
     * @param shareId 分享 ID
     * @return 是否为当前分享
     */
    @Override
    public boolean checkCurrentShare(String shareId) {
        return FileRepository.getUuid().equals(shareId);
    }
}
