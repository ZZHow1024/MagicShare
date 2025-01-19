package com.zzhow.magicshare.util;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
@SpringBootApplication(proxyBeanMethods = false, scanBasePackages = "com.zzhow.magicshare")
public class Application {
    public static ConfigurableApplicationContext startSpringBoot(String args) {
        return SpringApplication.run(Application.class, args);
    }
}
