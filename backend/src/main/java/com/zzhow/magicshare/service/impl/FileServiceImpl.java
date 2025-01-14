package com.zzhow.magicshare.service.impl;

import com.zzhow.magicshare.pojo.vo.FileListVO;
import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.service.FileService;
import org.springframework.stereotype.Service;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
@Service
public class FileServiceImpl implements FileService {
    /**
     * 获得分享的文件列表
     *
     * @return 分享的文件列表
     */
    @Override
    public FileListVO getFileList() {
        return new FileListVO(FileRepository.getUuid(), FileRepository.getFiles());
    }
}
