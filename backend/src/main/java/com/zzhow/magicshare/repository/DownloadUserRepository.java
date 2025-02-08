package com.zzhow.magicshare.repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZHow
 * @date 2025/2/8
 */
public class DownloadUserRepository {
    private static final DownloadUserRepository instance = new DownloadUserRepository();
    private final List<String> users = new ArrayList<>();

    private DownloadUserRepository() {

    }

    public void addUser(String sessionId) {
        users.add(sessionId);
    }

    public void removeUser(String sessionId) {
        users.remove(sessionId);
    }

    public boolean containsUser(String sessionId) {
        return users.contains(sessionId);
    }

    public static DownloadUserRepository getInstance() {
        return instance;
    }
}
