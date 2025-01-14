package com.zzhow.magicshare.repository;

import com.zzhow.magicshare.pojo.entity.FileDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
public class FileRepository {
    // 分享 ID
    private static String uuid = "";
    // 文件列表
    private static List<FileDetail> files = new ArrayList<>();

    private FileRepository() {
    }

    public static List<FileDetail> getFiles() {
        return files;
    }

    public static void clearFiles() {
        uuid = UUID.randomUUID().toString();
        files.clear();
    }

    public static void addFile(FileDetail fileDetail) {
        files.add(fileDetail);
    }

    public static int size() {
        return files.size();
    }

    public static String getUuid() {
        return uuid;
    }
}
