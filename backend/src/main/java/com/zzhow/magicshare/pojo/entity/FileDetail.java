package com.zzhow.magicshare.pojo.entity;

import java.util.Objects;

/**
 * 文件实体类
 *
 * @author ZZHow
 * @date 2025/01/14
 */
public class FileDetail {
    // 文件 ID
    private String fileId;
    // 文件名
    private String name;
    // 文件类型
    private String type;
    // 文件大小（KB）
    private double size;
    // 文件路径
    private String path;

    public FileDetail() {
    }

    public FileDetail(String fileId) {
        this.fileId = fileId;
    }

    public FileDetail(String fileId, String name, String type, double size, String path) {
        this.fileId = fileId;
        this.name = name;
        this.type = type;
        this.size = size;
        this.path = path;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "FileDetail{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", path='" + path + '\'' +
                "}\n";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FileDetail that = (FileDetail) o;
        return this.fileId.equals(that.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fileId);
    }
}
