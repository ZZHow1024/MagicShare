package com.zzhow.magicshare.pojo.vo;

/**
 * @author ZZHow
 * @date 2025/01/15
 */
public class CryptoVO {
    private String key;
    private String iv;
    private String data;

    public CryptoVO() {
    }

    public CryptoVO(String key, String iv, String data) {
        this.key = key;
        this.iv = iv;
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
