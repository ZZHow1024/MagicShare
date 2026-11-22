package com.zzhow.magicshare.controller;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.repository.UserRepository;
import com.zzhow.magicshare.util.CryptoUtil;
import com.zzhow.magicshare.util.HttpRangeUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 打包下载与在线预览控制类
 *
 * @author ZZHow
 * create 2026/11/18
 */
@RestController
@ConditionalOnProperty(name = "magicshare.simple-mode", havingValue = "false", matchIfMissing = true)
public class ArchiveController {
    private final CryptoUtil cryptoUtil = CryptoUtil.getInstance();

    /**
     * 将选中的分享文件打包为 ZIP 流式下载
     */
    @PostMapping("/api/archive")
    public ResponseEntity<StreamingResponseBody> archive(String token, @RequestBody List<String> paths) {
        String[] auth = verifyToken(token);
        if (auth == null || !UserRepository.verifyArchiveId(auth[0], auth[1]))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Map<String, FileDetail> filesByPath = new HashMap<>();
        for (FileDetail fileDetail : FileRepository.getFiles())
            filesByPath.put(normalizeRelativePath(fileDetail.getPath()), fileDetail);

        List<FileDetail> selectedFiles = new ArrayList<>();
        for (String path : paths) {
            FileDetail fileDetail = filesByPath.get(normalizeRelativePath(path));
            if (fileDetail != null)
                selectedFiles.add(fileDetail);
        }
        if (selectedFiles.isEmpty())
            return ResponseEntity.badRequest().build();

        StreamingResponseBody body = outputStream -> {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
                for (FileDetail fileDetail : selectedFiles) {
                    File file = new File(FileRepository.getBasePath() + fileDetail.getPath());
                    if (!file.isFile() || !file.canRead())
                        continue;
                    zipOutputStream.putNextEntry(new ZipEntry(normalizeRelativePath(fileDetail.getPath())));
                    Files.copy(file.toPath(), zipOutputStream);
                    zipOutputStream.closeEntry();
                }
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition("MagicShare-files.zip", false))
                .body(body);
    }

    /**
     * 在线预览分享文件（浏览器可直接渲染图片、文本、音视频、PDF，支持 Range 拖动）
     */
    @GetMapping("/api/preview")
    public ResponseEntity<InputStreamResource> preview(HttpServletRequest request, String token, String fileId) {
        String[] auth = verifyToken(token);
        if (auth == null || !UserRepository.verifyArchiveId(auth[0], auth[1]))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String requestedFileId;
        try {
            requestedFileId = new String(cryptoUtil.decryptAes(Base64.getDecoder().decode(fileId.replace(' ', '+'))), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        FileDetail target = null;
        for (FileDetail fileDetail : FileRepository.getFiles()) {
            if (fileDetail.getFileId().equals(requestedFileId)) {
                target = fileDetail;
                break;
            }
        }
        if (target == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        File file = new File(FileRepository.getBasePath() + target.getPath());
        if (!file.isFile() || !file.canRead())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        try {
            long fileLength = file.length();
            HttpRangeUtil.Range range = HttpRangeUtil.parse(request.getHeader(HttpHeaders.RANGE), fileLength);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(detectContentType(file));
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.setContentLength(range.length(fileLength));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(file.getName(), true));

            if (range.partial()) {
                headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + range.start() + "-" + range.end() + "/" + fileLength);
                return new ResponseEntity<>(new InputStreamResource(HttpRangeUtil.limitedInputStream(file, range)), headers, HttpStatus.PARTIAL_CONTENT);
            }

            return new ResponseEntity<>(new InputStreamResource(new FileInputStream(file)), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + file.length())
                    .build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 校验操作 token（AES 加密的 sessionId#archiveId）
     */
    private String[] verifyToken(String token) {
        if (token == null || token.isBlank())
            return null;

        try {
            byte[] decrypted = cryptoUtil.decryptAes(Base64.getDecoder().decode(token.replace(' ', '+')));
            String[] parts = new String(decrypted).split("#");
            if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank())
                return null;

            return parts;
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeRelativePath(String path) {
        if (path == null)
            return "";

        String normalizedPath = path.replace("\\", "/");
        while (normalizedPath.startsWith("/"))
            normalizedPath = normalizedPath.substring(1);
        while (normalizedPath.endsWith("/"))
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);

        return normalizedPath;
    }

    private String contentDisposition(String fileName, boolean inline) {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = inline ? "inline" : "attachment";
        return disposition + "; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
    }

    private MediaType detectContentType(File file) throws IOException {
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null)
            return MediaType.APPLICATION_OCTET_STREAM;

        return MediaType.parseMediaType(contentType);
    }
}