package com.zzhow.magicshare.controller;

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
    @GetMapping(path = "/list")
    public String fileList() {
        return "list";
    }
}
