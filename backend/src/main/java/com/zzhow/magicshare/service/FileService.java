package com.zzhow.magicshare.service;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
public interface FileService {
    /**
     * 获得分享的文件列表
     *
     * @return 分享的文件列表（经过 AES 加密）
     */
    String getFileList();

    /**
     * 检查是否为当前分享
     *
     * @param shareId 分享 ID
     * @return 是否为当前分享
     */
    boolean checkCurrentShare(String shareId);
}
