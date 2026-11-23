package com.zzhow.magicshare.repository;

import java.io.File;
import java.util.function.Consumer;

/**
 * 接收设置存储库（Web 上传）
 *
 * @author ZZHow
 * create 2026/11/16
 */
public class ReceiveRepository {
    // 是否允许接收文件（默认关闭）
    private static boolean allowUpload = false;
    // 接收目录（默认 用户下载目录：Windows/macOS/Linux 均为 {user.home}/Downloads 标准位置）
    private static String receiveDir = System.getProperty("user.home") + File.separator + "Downloads";
    // 上传完成通知监听器（供桌面端注册）
    private static Consumer<String> uploadCompleteListener;

    private ReceiveRepository() {
    }

    public static boolean isAllowUpload() {
        return allowUpload;
    }

    public static void setAllowUpload(boolean allowUpload) {
        ReceiveRepository.allowUpload = allowUpload;
    }

    public static String getReceiveDir() {
        return receiveDir;
    }

    public static void setReceiveDir(String receiveDir) {
        if (receiveDir != null && !receiveDir.isBlank())
            ReceiveRepository.receiveDir = receiveDir;
    }

    public static void setUploadCompleteListener(Consumer<String> uploadCompleteListener) {
        ReceiveRepository.uploadCompleteListener = uploadCompleteListener;
    }

    public static void notifyUploadComplete(String filePath) {
        Consumer<String> listener = uploadCompleteListener;
        if (listener != null)
            listener.accept(filePath);
    }
}