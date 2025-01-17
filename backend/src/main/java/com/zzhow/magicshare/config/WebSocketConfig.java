package com.zzhow.magicshare.config;

import com.zzhow.magicshare.websocket.FileWebSocketHandler;
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
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册 WebSocket 端点并指定处理器
        registry.addHandler(new FileWebSocketHandler(), "/ws/download").setAllowedOrigins("*");
    }
}