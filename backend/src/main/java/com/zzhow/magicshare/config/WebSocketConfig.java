package com.zzhow.magicshare.config;

import com.zzhow.magicshare.service.FileService;
import com.zzhow.magicshare.websocket.FileWebSocketHandler;
import com.zzhow.magicshare.websocket.UserWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * @author ZZHow
 * create 2025/01/16
 * update 2026/11/21
 */
@Configuration
@EnableWebSocket
@ConditionalOnProperty(name = "magicshare.simple-mode", havingValue = "false", matchIfMissing = true)
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private FileService fileService;

    /**
     * 提升容器文本/二进制帧缓冲上限，
     * 避免加密下载的大分块帧（8KB 明文经 Base64 后约 11KB）被默认 8KB 缓冲截断。
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(512 * 1024);
        container.setMaxBinaryMessageBufferSize(512 * 1024);
        return container;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册 WebSocket 端点并指定处理器
        registry.addHandler(new FileWebSocketHandler(), "/ws/download").setAllowedOrigins("*");
        registry.addHandler(new UserWebSocketHandler(fileService), "/ws/connect").setAllowedOrigins("*");
    }
}