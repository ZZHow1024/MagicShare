package com.zzhow.magicshare.pojo.entity;

import javax.crypto.SecretKey;

/**
 * @author ZZHow
 * @date 2025/01/16
 */
public class AesCrypto {
    private SecretKey key;
    private byte[] iv;

    public AesCrypto() {
    }

    public AesCrypto(SecretKey key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }

    public SecretKey getKey() {
        return key;
    }

    public void setKey(SecretKey key) {
        this.key = key;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }
}
