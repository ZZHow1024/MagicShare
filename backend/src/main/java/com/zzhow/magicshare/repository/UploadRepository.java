package com.zzhow.magicshare.repository;

import com.zzhow.magicshare.pojo.entity.UploadTask;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上传任务存储库
 *
 * @author ZZHow
 * create 2026/11/16
 */
public class UploadRepository {
    // 每会话待使用的上传 ID
    private static final Map<String, Set<String>> uploadIds = new ConcurrentHashMap<>();
    // 活动任务：任务 ID → 上传任务
    private static final Map<String, UploadTask> tasks = new ConcurrentHashMap<>();

    // 每会话待使用上传 ID 上限
    public static final int MAX_PENDING_UPLOAD_IDS = 20;
    // 活动任务数上限
    public static final int MAX_ACTIVE_TASKS = 50;
    // 任务空闲过期时间（24 小时）
    public static final long TASK_IDLE_EXPIRE = 24 * 60 * 60 * 1000L;

    private UploadRepository() {
    }

    /**
     * 为会话生成上传 ID
     */
    public static String generateUploadId(String sessionId) {
        Set<String> set = uploadIds.computeIfAbsent(sessionId, key -> ConcurrentHashMap.newKeySet());
        while (set.size() >= MAX_PENDING_UPLOAD_IDS) {
            Iterator<String> iterator = set.iterator();
            if (!iterator.hasNext())
                break;
            iterator.next();
            iterator.remove();
        }

        String uploadId = UUID.randomUUID().toString();
        set.add(uploadId);

        return uploadId;
    }

    public static boolean containsUploadId(String sessionId, String uploadId) {
        Set<String> set = uploadIds.get(sessionId);
        return set != null && set.contains(uploadId);
    }

    public static void removeUploadId(String sessionId, String uploadId) {
        Set<String> set = uploadIds.get(sessionId);
        if (set != null)
            set.remove(uploadId);
    }

    public static boolean canAddTask() {
        if (tasks.size() >= MAX_ACTIVE_TASKS)
            cleanupExpiredTasks();
        return tasks.size() < MAX_ACTIVE_TASKS;
    }

    public static void addTask(UploadTask task) {
        tasks.put(task.getTaskId(), task);
    }

    public static UploadTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    public static UploadTask removeTask(String taskId) {
        return tasks.remove(taskId);
    }

    /**
     * 查找同会话同名同大小的任务（用于页面刷新后续传）
     */
    public static UploadTask findTask(String sessionId, String fileName, long fileSize) {
        for (UploadTask task : tasks.values()) {
            if (task.getSessionId().equals(sessionId)
                    && task.getFileName().equals(fileName)
                    && task.getFileSize() == fileSize)
                return task;
        }

        return null;
    }

    private static void cleanupExpiredTasks() {
        for (Map.Entry<String, UploadTask> entry : tasks.entrySet()) {
            if (entry.getValue().isExpired(TASK_IDLE_EXPIRE))
                tasks.remove(entry.getKey());
        }
    }
}