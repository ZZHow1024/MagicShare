package com.zzhow.magicshare.pojo.entity;

import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZZHow
 * @date 2025/02/04
 */
public class User {
    private WebSocketSession session;
    private final Map<String, Boolean> downloadId = new HashMap<>();

    public User() {
    }

    public User(WebSocketSession session) {
        this.session = session;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public Map<String, Boolean> getDownloadIdList() {
        return downloadId;
    }
}
