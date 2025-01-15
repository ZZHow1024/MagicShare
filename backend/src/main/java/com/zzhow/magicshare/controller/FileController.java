package com.zzhow.magicshare.controller;

import com.zzhow.magicshare.pojo.dto.CryptoDTO;
import com.zzhow.magicshare.pojo.vo.CryptoVO;
import com.zzhow.magicshare.result.Result;
import com.zzhow.magicshare.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
@RestController()
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping(path = "/list")
    public Result<CryptoVO> fileList(@RequestBody CryptoDTO cryptoDTO) {
        return Result.success(fileService.getFileList(cryptoDTO.getPublicKey()));
    }

    @GetMapping(path = "/check")
    public Result<Boolean> checkCurrentShare(@RequestParam String shareId) {
        return Result.success(fileService.checkCurrentShare(shareId));
    }
}
