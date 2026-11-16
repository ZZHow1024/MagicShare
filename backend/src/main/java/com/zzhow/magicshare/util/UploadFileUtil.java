package com.zzhow.magicshare.util;

import com.zzhow.magicshare.repository.ReceiveRepository;

import java.io.File;
import java.util.Locale;
import java.util.Set;

/**
 * 上传文件处理工具类
 *
 * @author ZZHow
 * create 2026/11/16
 */
public class UploadFileUtil {
    // Windows 保留设备名
    private static final Set<String> RESERVED_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");

    private UploadFileUtil() {
    }

    /**
     * 净化上传文件名：仅保留基础文件名，过滤非法与保留字符
     */
    public static String sanitizeFileName(String raw) {
        if (raw == null || raw.isBlank())
            return null;

        String name = raw.replace("\\", "/");
        int slash = name.lastIndexOf('/');
        if (slash != -1)
            name = name.substring(slash + 1);
        name = name.replaceAll("[\\p{Cntrl}]", "_");
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_").strip();
        if (name.length() > 120)
            name = name.substring(name.length() - 120);
        if (name.equals(".") || name.equals(".."))
            return null;
        while (name.endsWith(".") || name.endsWith(" "))
            name = name.substring(0, name.length() - 1);
        if (name.isBlank())
            return null;

        String upper = name.toUpperCase(Locale.ROOT);
        int dot = upper.indexOf('.');
        String base = dot == -1 ? upper : upper.substring(0, dot);
        if (RESERVED_NAMES.contains(base))
            name = "_" + name;

        return name;
    }

    /**
     * 解析接收目录下的目标文件，重名时追加序号
     */
    public static File resolveTargetFile(String fileName) {
        File dir = new File(ReceiveRepository.getReceiveDir());
        if (!dir.exists() && !dir.mkdirs())
            throw new IllegalStateException("Unable to create receive directory");

        File target = new File(dir, fileName);
        if (!target.exists())
            return target;

        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        String ext = dot > 0 ? fileName.substring(dot) : "";

        int count = 1;
        while (true) {
            File candidate = new File(dir, base + " (" + count + ")" + ext);
            if (!candidate.exists())
                return candidate;
            count++;
        }
    }
}