package com.zzhow.magicshare.pojo.dto;

/**
 * @author ZZHow
 * @date 2025/01/15
 */
public class CryptoDTO {
    private String publicKey;

    public CryptoDTO() {
    }

    public CryptoDTO(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
