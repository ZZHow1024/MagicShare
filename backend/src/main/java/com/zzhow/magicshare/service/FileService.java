package com.zzhow.magicshare.service;

import com.zzhow.magicshare.pojo.entity.FileDetail;

import java.util.List;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
public interface FileService {
    /**
     * 获得分享的文件列表
     *
     * @return 分享的文件列表
     */
    List<FileDetail> getFileList();
}
