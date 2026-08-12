package com.zzhow.magicshare.util;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author ZZHow
 * create 2025/01/14
 * update 2026/08/12
 */
@SpringBootApplication(proxyBeanMethods = false, scanBasePackages = "com.zzhow.magicshare")
public class Application {
    public static ConfigurableApplicationContext startSpringBoot(String... args) {
        return SpringApplication.run(Application.class, args);
    }
}
