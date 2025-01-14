package com.zzhow.magicshare.pojo.vo;

import com.zzhow.magicshare.pojo.entity.FileDetail;

import java.util.List;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
public class FileListVO {
    // 分享 ID
    private String shareId;
    // 总文件数
    private Integer count;
    // 文件列表
    private List<FileDetail> files;

    public FileListVO() {
    }

    public FileListVO(String shareId, Integer count, List<FileDetail> files) {
        this.shareId = shareId;
        this.count = count;
        this.files = files;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public List<FileDetail> getFiles() {
        return files;
    }

    public void setFiles(List<FileDetail> files) {
        this.files = files;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
