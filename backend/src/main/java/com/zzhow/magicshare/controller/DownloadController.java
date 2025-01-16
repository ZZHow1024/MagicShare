package com.zzhow.magicshare.controller;

import com.zzhow.magicshare.pojo.dto.CryptoDTO;
import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.FileRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
@RestController()
@RequestMapping("/api/download")
public class DownloadController {
    private static final int CHUNK_SIZE = 1024 * 1024 * 2; // 每块 2MB

    @GetMapping("/unencrypted/{fileId}")
    public ResponseEntity<Resource> downloadFile(String shareId, @PathVariable String fileId) {
        if (shareId == null || fileId == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!shareId.equals(FileRepository.getUuid())) {
            return ResponseEntity.notFound().build();
        }

        try {
            // 构建文件路径
            List<FileDetail> files = FileRepository.getFiles();
            int index = files.indexOf(new FileDetail(fileId));
            if (index == -1)
                return ResponseEntity.notFound().build();

            String path = FileRepository.getBasePath() + files.get(index).getPath();
            Path filePath = new File(path).toPath();

            Resource resource = new UrlResource(filePath.toUri());

            // 检查文件是否存在和可读
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 返回文件作为响应
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 文件分块加密与传输
    @CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", exposedHeaders = "X-Aes-Key, X-Aes-Iv")  // 允许暴露自定义响应头
    @PostMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadFile(@RequestBody CryptoDTO cryptoDTO) throws Exception {
        File file = new File(""); // 文件路径

        // Base64 解码
        String key = new String(Base64.getDecoder().decode(cryptoDTO.getKey()));

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

        // 初始化 AES 加密器
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);

        // 创建响应体进行流式传输
        StreamingResponseBody stream = outputStream -> {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[CHUNK_SIZE];  // 每块数据的大小
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    byte[] encryptedData;
                    if (bytesRead == CHUNK_SIZE)
                        encryptedData = cipher.doFinal(buffer);
                    else {
                        byte[] newBuffer = Arrays.copyOf(buffer, bytesRead);
                        encryptedData = cipher.doFinal(newBuffer);
                    }
                    outputStream.write(encryptedData, 0, encryptedData.length);
                    outputStream.flush();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"download.png\"")
                .header("Access-Control-Expose-Headers", "X-Aes-Key,X-Aes-Iv")
                .header("X-Aes-Key", Base64.getEncoder().encodeToString(encryptedAesKey))  // 返回加密的AES密钥
                .header("X-Aes-Iv", Base64.getEncoder().encodeToString(iv)) // 返回 IV
                .body(stream);
    }
}
