package com.zzhow.magicshare;

import com.zzhow.magicshare.ui.window.MainView;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MagicShareApplication {
    public static void main(String[] args) {
        // 启动 Spring Boot
        Thread springThread = new Thread(() -> SpringApplication.run(SpringBootApplication.class, args));
        springThread.start();

        // 启动 JavaFX
        MainView.main(args);
    }
}
