package com.zzhow.magicshare.pojo.entity;

/**
 * 文件实体类
 *
 * @author ZZHow
 * @date 2025/01/14
 */
public class FileDetail {
    private String name;
    private String type;
    private String path;

    public FileDetail() {
    }

    public FileDetail(String name, String type, String path) {
        this.name = name;
        this.type = type;
        this.path = path;
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
}
