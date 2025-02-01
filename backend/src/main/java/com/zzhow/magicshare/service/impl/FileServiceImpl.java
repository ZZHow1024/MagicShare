package com.zzhow.magicshare.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzhow.magicshare.pojo.vo.FileListVO;
import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.repository.UserRepository;
import com.zzhow.magicshare.service.FileService;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.util.Base64;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
@Service
public class FileServiceImpl implements FileService {
    /**
     * 获得分享的文件列表
     *
     * @return 分享的文件列表
     */
    @Override
    public String getFileList() {
        FileListVO fileListVO = new FileListVO(FileRepository.getUuid(), FileRepository.size(), FileRepository.getFiles());

        try {
            // 初始化 AES 加密器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(UserRepository.getAesCrypto().getIv());
            cipher.init(Cipher.ENCRYPT_MODE, UserRepository.getAesCrypto().getKey(), ivSpec);

            // 加密数据
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(fileListVO); // 将对象序列化为 JSON 字符串
            byte[] encryptedData = cipher.doFinal(jsonString.getBytes());

            // 返回加密数据
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException |
                 BadPaddingException | InvalidKeyException | JsonProcessingException |
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
