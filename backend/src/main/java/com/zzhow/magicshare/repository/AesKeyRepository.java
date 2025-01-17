package com.zzhow.magicshare.repository;

import com.zzhow.magicshare.pojo.entity.AesCrypto;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZZHow
 * @date 2025/01/16
 */
public class AesKeyRepository {
    private static final Map<String, AesCrypto> keys = new HashMap<>();

    private AesKeyRepository() {
    }

    public static void clear() {
        keys.clear();
    }

    public static void set(String session, AesCrypto aesCrypto) {
        keys.put(session, aesCrypto);
    }

    public static void set(String session, SecretKey key, byte[] iv) {
        keys.put(session, new AesCrypto(key, iv));
    }

    public static void delete(String session) {
        keys.remove(session);
    }

    public static AesCrypto get(String session) {
        return keys.get(session);
    }
}
