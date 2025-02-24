package com.zzhow.magicshare.util;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.FileRepository;

import java.io.File;
import java.util.List;

/**
 * @author ZZHow
 * @date 2025/01/14
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

        File[] files = currentPath.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                find(file.getAbsolutePath(), res);
            } else {
                String fileName = file.getName();
                String fileType = "unknown";
                if (fileName.lastIndexOf(".") != -1)
                    fileType = fileName.substring(fileName.lastIndexOf(".") + 1);

                FileDetail fileDetail = new FileDetail(generator.generateId() + "", fileName, fileType, Math.round(file.length() / 1024.0 * 10.0) / 10.0, file.getAbsolutePath().replace(path, ""));
                res.add(fileDetail);
            }
        }
    }
}
