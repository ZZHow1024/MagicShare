package com.zzhow.magicshare.controller;

import com.zzhow.magicshare.pojo.entity.UploadTask;
import com.zzhow.magicshare.repository.ReceiveRepository;
import com.zzhow.magicshare.repository.UploadRepository;
import com.zzhow.magicshare.repository.UserRepository;
import com.zzhow.magicshare.util.CryptoUtil;
import com.zzhow.magicshare.util.UploadFileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.stream.Stream;

/**
 * Web 上传控制类
 *
 * @author ZZHow
 * create 2026/11/16
 */
@RestController
@RequestMapping("/api/upload")
@ConditionalOnProperty(name = "magicshare.simple-mode", havingValue = "false", matchIfMissing = true)
public class UploadController {
    private final CryptoUtil cryptoUtil = CryptoUtil.getInstance();

    // 分片大小下限 64 KB
    public static final int MIN_CHUNK_SIZE = 64 * 1024;
    // 分片大小上限 8 MB
    public static final int MAX_CHUNK_SIZE = 8 * 1024 * 1024;
    // 单文件大小上限 10 GB
    public static final long MAX_FILE_SIZE = 10L * 1024 * 1024 * 1024;
    // 单文件分片数上限
    public static final int MAX_TOTAL_CHUNKS = 200000;

    /**
     * 查询文件接收是否启用（供 Web 上传页门控展示，状态本身不敏感）
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ok(Map.of("allowUpload", ReceiveRepository.isAllowUpload()));
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> init(@RequestBody UploadInitRequest initRequest, String token) {
        if (!ReceiveRepository.isAllowUpload())
            return error(HttpStatus.FORBIDDEN, "Upload is disabled");

        String[] auth = verifyToken(token);
        if (auth == null)
            return error(HttpStatus.UNAUTHORIZED, "Invalid token");
        String sessionId = auth[0];
        String uploadId = auth[1];

        if (!UserRepository.containsUser(sessionId))
            return error(HttpStatus.UNAUTHORIZED, "Invalid session");
        if (!UploadRepository.containsUploadId(sessionId, uploadId))
            return error(HttpStatus.UNAUTHORIZED, "Invalid upload id");

        String fileName = UploadFileUtil.sanitizeFileName(initRequest.fileName());
        if (fileName == null)
            return error(HttpStatus.BAD_REQUEST, "Invalid file name");
        if (initRequest.fileSize() <= 0 || initRequest.fileSize() > MAX_FILE_SIZE)
            return error(HttpStatus.BAD_REQUEST, "Invalid file size");
        if (initRequest.chunkSize() < MIN_CHUNK_SIZE || initRequest.chunkSize() > MAX_CHUNK_SIZE)
            return error(HttpStatus.BAD_REQUEST, "Invalid chunk size");
        int totalChunks = (int) ((initRequest.fileSize() + initRequest.chunkSize() - 1) / initRequest.chunkSize());
        if (totalChunks > MAX_TOTAL_CHUNKS)
            return error(HttpStatus.BAD_REQUEST, "Too many chunks");

        // 断点续传：优先按前端保存的 taskId 接管任务（页面刷新后 WebSocket 会话已更换）
        if (initRequest.taskId() != null && !initRequest.taskId().isBlank()) {
            UploadTask resumed = UploadRepository.getTask(initRequest.taskId());
            if (resumed != null && resumed.getFileName().equals(fileName) && resumed.getFileSize() == initRequest.fileSize()) {
                resumed.setSessionId(sessionId);
                resumed.touch();
                return ok(Map.of(
                        "taskId", resumed.getTaskId(),
                        "chunkSize", resumed.getChunkSize(),
                        "totalChunks", resumed.getTotalChunks(),
                        "receivedIndexes", List.copyOf(resumed.getReceivedIndexes())));
            }
        }

        // 同会话内续传：同名同大小的任务直接复用
        UploadTask existing = UploadRepository.findTask(sessionId, fileName, initRequest.fileSize());
        if (existing != null) {
            existing.touch();
            return ok(Map.of(
                    "taskId", existing.getTaskId(),
                    "chunkSize", existing.getChunkSize(),
                    "totalChunks", existing.getTotalChunks(),
                    "receivedIndexes", List.copyOf(existing.getReceivedIndexes())));
        }

        if (!UploadRepository.canAddTask())
            return error(HttpStatus.SERVICE_UNAVAILABLE, "Too many active uploads");

        Path chunkDir = Path.of(System.getProperty("java.io.tmpdir"), "MagicShare", "upload", uploadId);
        try {
            Files.createDirectories(chunkDir);
        } catch (IOException e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create upload directory");
        }

        UploadTask task = new UploadTask(uploadId, sessionId, fileName, initRequest.fileSize(), initRequest.chunkSize(), chunkDir);
        UploadRepository.addTask(task);

        return ok(Map.of(
                "taskId", task.getTaskId(),
                "chunkSize", task.getChunkSize(),
                "totalChunks", task.getTotalChunks(),
                "receivedIndexes", List.of()));
    }

    @PostMapping("/chunk")
    public ResponseEntity<Map<String, Object>> chunk(String token, String taskId, Integer index, @RequestBody byte[] data) throws IOException {
        if (!ReceiveRepository.isAllowUpload())
            return error(HttpStatus.FORBIDDEN, "Upload is disabled");

        String[] auth = verifyToken(token);
        if (auth == null)
            return error(HttpStatus.UNAUTHORIZED, "Invalid token");

        UploadTask task = UploadRepository.getTask(taskId);
        if (task == null || !task.getSessionId().equals(auth[0]))
            return error(HttpStatus.NOT_FOUND, "Task not found");
        if (index == null || index < 0 || index >= task.getTotalChunks())
            return error(HttpStatus.BAD_REQUEST, "Invalid index");
        if (data == null || data.length == 0)
            return error(HttpStatus.BAD_REQUEST, "Empty chunk");
        if (data.length > task.getChunkSize())
            return error(HttpStatus.BAD_REQUEST, "Chunk is too large");
        // 非末分片必须为整分片，末分片必须等于剩余大小
        if (index < task.getTotalChunks() - 1 && data.length < task.getChunkSize())
            return error(HttpStatus.BAD_REQUEST, "Chunk size mismatch");
        if (index == task.getTotalChunks() - 1) {
            long expected = task.getFileSize() - (long) (task.getTotalChunks() - 1) * task.getChunkSize();
            if (data.length != expected)
                return error(HttpStatus.BAD_REQUEST, "Chunk size mismatch");
        }

        Files.write(task.getChunkDir().resolve(index + ".part"), data);
        task.getReceivedIndexes().add(index);
        task.touch();

        return ok(Map.of("received", task.getReceivedIndexes().size()));
    }

    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> merge(String token, String taskId) {
        if (!ReceiveRepository.isAllowUpload())
            return error(HttpStatus.FORBIDDEN, "Upload is disabled");

        String[] auth = verifyToken(token);
        if (auth == null)
            return error(HttpStatus.UNAUTHORIZED, "Invalid token");

        UploadTask task = UploadRepository.getTask(taskId);
        if (task == null || !task.getSessionId().equals(auth[0]))
            return error(HttpStatus.NOT_FOUND, "Task not found");
        if (!task.markMerging())
            return error(HttpStatus.CONFLICT, "Merge is already in progress");

        File target = null;
        try {
            if (task.getReceivedIndexes().size() != task.getTotalChunks())
                return error(HttpStatus.BAD_REQUEST, "Incomplete upload");
            for (int i = 0; i < task.getTotalChunks(); i++) {
                if (!Files.exists(task.getChunkDir().resolve(i + ".part")))
                    return error(HttpStatus.BAD_REQUEST, "Missing chunk " + i);
            }

            target = UploadFileUtil.resolveTargetFile(task.getFileName());
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target))) {
                for (int i = 0; i < task.getTotalChunks(); i++)
                    Files.copy(task.getChunkDir().resolve(i + ".part"), outputStream);
            }

            UploadRepository.removeTask(task.getTaskId());
            UploadRepository.removeUploadId(task.getSessionId(), task.getTaskId());
            deleteDirectory(task.getChunkDir());
            ReceiveRepository.notifyUploadComplete(target.getAbsolutePath());

            return ok(Map.of("fileName", target.getName()));
        } catch (IOException e) {
            e.printStackTrace();
            if (target != null && target.exists())
                target.delete();
            task.resetMerging();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Merge failed");
        }
    }

    @PostMapping("/abort")
    public ResponseEntity<Map<String, Object>> abort(String token, String taskId) throws IOException {
        if (!ReceiveRepository.isAllowUpload())
            return error(HttpStatus.FORBIDDEN, "Upload is disabled");

        String[] auth = verifyToken(token);
        if (auth == null)
            return error(HttpStatus.UNAUTHORIZED, "Invalid token");

        UploadTask task = UploadRepository.removeTask(taskId);
        if (task == null || !task.getSessionId().equals(auth[0]))
            return error(HttpStatus.NOT_FOUND, "Task not found");

        UploadRepository.removeUploadId(task.getSessionId(), task.getTaskId());
        deleteDirectory(task.getChunkDir());

        return ok(Map.of("aborted", true));
    }

    /**
     * 校验上传 token（AES 加密的 sessionId#uploadId）
     */
    private String[] verifyToken(String token) {
        if (token == null || token.isBlank())
            return null;

        try {
            // 查询参数中 '+' 可能被解码为空格，Base64 不含空格，可安全还原
            byte[] decrypted = cryptoUtil.decryptAes(Base64.getDecoder().decode(token.replace(' ', '+')));
            String[] parts = new String(decrypted).split("#");
            if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank())
                return null;

            return parts;
        } catch (Exception e) {
            return null;
        }
    }

    private void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory))
            return;

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private ResponseEntity<Map<String, Object>> ok(Map<String, Object> body) {
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    /**
     * 上传初始化请求体
     */
    record UploadInitRequest(String fileName, long fileSize, int chunkSize, String taskId) {
    }
}