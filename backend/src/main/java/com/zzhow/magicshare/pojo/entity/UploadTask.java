package com.zzhow.magicshare.pojo.entity;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 上传任务实体类
 *
 * @author ZZHow
 * create 2026/11/16
 */
public class UploadTask {
    private final String taskId;
    // 会话可能因页面刷新被新会话接管
    private volatile String sessionId;
    private final String fileName;
    private final long fileSize;
    private final int chunkSize;
    private final int totalChunks;
    private final Path chunkDir;
    private final Set<Integer> receivedIndexes = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean merging = new AtomicBoolean(false);
    private volatile long lastActivityTime;

    public UploadTask(String taskId, String sessionId, String fileName, long fileSize, int chunkSize, Path chunkDir) {
        this.taskId = taskId;
        this.sessionId = sessionId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.chunkSize = chunkSize;
        this.totalChunks = (int) ((fileSize + chunkSize - 1) / chunkSize);
        this.chunkDir = chunkDir;
        this.lastActivityTime = System.currentTimeMillis();
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public Path getChunkDir() {
        return chunkDir;
    }

    public Set<Integer> getReceivedIndexes() {
        return receivedIndexes;
    }

    public void touch() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    public boolean isExpired(long idleMillis) {
        return System.currentTimeMillis() - lastActivityTime > idleMillis;
    }

    public boolean markMerging() {
        return merging.compareAndSet(false, true);
    }

    public void resetMerging() {
        merging.set(false);
    }
}