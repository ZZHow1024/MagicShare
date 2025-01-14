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
    private Long id;
    // 文件名
    private String name;
    // 文件类型
    private String type;
    // 文件路径
    private String path;

    public FileDetail() {
    }

    public FileDetail(Long id) {
        this.id = id;
    }

    public FileDetail(Long id, String name, String type, String path) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FileDetail that = (FileDetail) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
