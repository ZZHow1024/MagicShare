package com.zzhow.magicshare.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author ZZHow
 * create 2025/01/14
 * update 2026/08/12
 */
@Controller
@ConditionalOnProperty(name = "magicshare.simple-mode", havingValue = "false", matchIfMissing = true)
public class VueController {
    @RequestMapping("/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }
}
