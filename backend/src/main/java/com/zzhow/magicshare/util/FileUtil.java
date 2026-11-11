package com.zzhow.magicshare.util;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.FileRepository;

import java.io.File;
import java.util.List;

/**
 * @author ZZHow
 * create 2025/01/14
 * update 2026/11/11
 */
public class FileUtil {
    public static final SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1);

    /**
     * 查找指定路径下的所有文件
     *
     * @param path 路径
     * @param res  所有文件的路径
     */
    public static void find(String path, List<FileDetail> res) {
        File currentPath = new File(path);

        if (!currentPath.exists())
            return;

        if (!currentPath.isDirectory()) {
            String fileName = currentPath.getName();
            String fileType = "unknown";
            if (fileName.lastIndexOf(".") != -1)
                fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            FileDetail fileDetail = new FileDetail(generator.generateId() + "", fileName, fileType, Math.round(currentPath.length() / 1024.0 * 10.0) / 10.0, "");
            res.add(fileDetail);
            return;
        }

        findFiles(currentPath.getAbsolutePath(), currentPath, res);
    }

    /**
     * 递归查找目录下的所有文件，相对路径始终基于最初的根路径计算
     *
     * @param rootPath  根路径
     * @param directory 当前遍历的目录
     * @param res       所有文件的路径
     */
    private static void findFiles(String rootPath, File directory, List<FileDetail> res) {
        File[] files = directory.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findFiles(rootPath, file, res);
            } else {
                String fileName = file.getName();
                String fileType = "unknown";
                if (fileName.lastIndexOf(".") != -1)
                    fileType = fileName.substring(fileName.lastIndexOf(".") + 1);

                FileDetail fileDetail = new FileDetail(generator.generateId() + "", fileName, fileType, Math.round(file.length() / 1024.0 * 10.0) / 10.0, file.getAbsolutePath().substring(rootPath.length()));
                res.add(fileDetail);
            }
        }
    }
}
