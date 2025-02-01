package com.zzhow.magicshare.pojo.vo;

/**
 * @author ZZHow
 * @date 2025/01/15
 */
public class CryptoVO {
    private String key;
    private byte[] iv;

    public CryptoVO() {
    }

    public CryptoVO(String key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }
}
