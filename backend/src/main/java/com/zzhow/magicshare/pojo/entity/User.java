package com.zzhow.magicshare.pojo.entity;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZHow
 * @date 2025/02/04
 */
public class User {
    private WebSocketSession session;
    private final List<String> downloadId = new ArrayList<>();

    public User() {
    }

    public User(WebSocketSession session) {
        this.session = session;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public List<String> getDownloadIdList() {
        return downloadId;
    }
}
