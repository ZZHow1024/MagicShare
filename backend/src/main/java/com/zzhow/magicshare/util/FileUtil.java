package com.zzhow.magicshare.util;

import com.zzhow.magicshare.pojo.entity.FileDetail;

import java.io.File;
import java.util.List;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
public class FileUtil {
    /**
     * 查找指定路径下的所有文件
     *
     * @param path 路径
     * @param res  所有文件的路径
     */
    public static void find(String path, List<FileDetail> res) {
        File currentPath = new File(path);
        File[] files = currentPath.listFiles();

        if (files == null) return;

        int count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                find(file.getAbsolutePath(), res);
            } else {
                String fileName = file.getName();
                String fileType = "unknown";
                if (fileName.lastIndexOf(".") != -1)
                    fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                FileDetail fileDetail = new FileDetail(count++, fileName, fileType, file.getAbsolutePath());
                res.add(fileDetail);
            }
        }
    }
}
