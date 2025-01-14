package com.zzhow.magicshare.service;

import com.zzhow.magicshare.pojo.vo.FileListVO;

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
    FileListVO getFileList();
}
