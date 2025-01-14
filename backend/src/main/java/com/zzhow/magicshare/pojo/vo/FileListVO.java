package com.zzhow.magicshare.pojo.vo;

import com.zzhow.magicshare.pojo.entity.FileDetail;

import java.util.List;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
public class FileListVO {
    // 分享 ID
    private String uuid;
    // 文件列表
    private List<FileDetail> files;

    public FileListVO() {
    }

    public FileListVO(String uuid, List<FileDetail> files) {
        this.uuid = uuid;
        this.files = files;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<FileDetail> getFiles() {
        return files;
    }

    public void setFiles(List<FileDetail> files) {
        this.files = files;
    }
}
