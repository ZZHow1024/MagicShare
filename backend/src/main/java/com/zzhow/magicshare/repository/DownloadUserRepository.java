package com.zzhow.magicshare.repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZZHow
 * @date 2025/2/8
 */
public class DownloadUserRepository {
    private static final DownloadUserRepository instance = new DownloadUserRepository();
    private final Set<String> users = ConcurrentHashMap.newKeySet();

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
