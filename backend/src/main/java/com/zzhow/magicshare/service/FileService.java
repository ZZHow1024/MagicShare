package com.zzhow.magicshare.service;

import com.zzhow.magicshare.pojo.vo.CryptoVO;
import com.zzhow.magicshare.pojo.vo.FileListVO;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
public interface FileService {
    /**
     * 获得分享的文件列表
     *
     * @param publicKey 公钥
     * @return 分享的文件列表（经过 AES 加密）
     */
    CryptoVO getFileList(String publicKey);

    /**
     * 检查是否为当前分享
     *
     * @param shareId 分享 ID
     * @return 是否为当前分享
     */
    boolean checkCurrentShare(String shareId);
}
