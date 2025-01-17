package com.zzhow.magicshare.controller;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.FileRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
@RestController()
@RequestMapping("/api/download")
public class DownloadController {
    @GetMapping("/{fileId}")
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
}
