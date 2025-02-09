package com.zzhow.magicshare.config;

import com.zzhow.magicshare.service.FileService;
import com.zzhow.magicshare.websocket.FileWebSocketHandler;
import com.zzhow.magicshare.websocket.UserWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author ZZHow
 * @date 2025/01/16
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private FileService fileService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册 WebSocket 端点并指定处理器
        registry.addHandler(new FileWebSocketHandler(), "/ws/download").setAllowedOrigins("*");
        registry.addHandler(new UserWebSocketHandler(fileService), "/ws/connect").setAllowedOrigins("*");
    }
}