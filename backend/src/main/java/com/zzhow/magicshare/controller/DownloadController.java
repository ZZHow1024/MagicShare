package com.zzhow.magicshare.controller;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(String token, String shareId, @PathVariable String fileId) {
        try {
            if (token == null || shareId == null || fileId == null) {
                return ResponseEntity.badRequest().build();
            }

            // 初始化 AES 解密器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(UserRepository.getAesCrypto().getIv());
            cipher.init(Cipher.DECRYPT_MODE, UserRepository.getAesCrypto().getKey(), ivSpec);

            // 解密数据
            String[] split = new String(cipher.doFinal(Base64.getDecoder().decode(token))).split("#");

            // 验证 sessionId#downloadId
            if (!UserRepository.verifyDownloadId(split[0], split[1])) {
                return ResponseEntity.notFound().build();
            }

            // 解密数据
            shareId = new String(cipher.doFinal(Base64.getDecoder().decode(shareId)));
            fileId = new String(cipher.doFinal(Base64.getDecoder().decode(fileId)));

            if (!shareId.equals(FileRepository.getUuid())) {
                return ResponseEntity.notFound().build();
            }

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
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8) + "\"")
                    .body(resource);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
