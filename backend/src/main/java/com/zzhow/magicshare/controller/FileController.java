package com.zzhow.magicshare.controller;

import com.zzhow.magicshare.pojo.vo.FileListVO;
import com.zzhow.magicshare.result.Result;
import com.zzhow.magicshare.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
@RestController()
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @GetMapping(path = "/list")
    public Result<FileListVO> fileList() {
        return Result.success(fileService.getFileList());
    }
}
