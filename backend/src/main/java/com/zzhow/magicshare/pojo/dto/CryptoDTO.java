package com.zzhow.magicshare.pojo.dto;

/**
 * @author ZZHow
 * @date 2025/01/15
 */
public class CryptoDTO {
    private String key;

    public CryptoDTO() {
    }

    public CryptoDTO(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
