package com.zzhow.magicshare.repository;

/**
 * 简单分享模式存储库
 *
 * @author ZZHow
 * create 2026/08/12
 * update 2026/08/12
 */
public class SimpleShareRepository {
    private static boolean showDirectoryStructure = true;

    private SimpleShareRepository() {
    }

    public static boolean isShowDirectoryStructure() {
        return showDirectoryStructure;
    }

    public static void setShowDirectoryStructure(boolean showDirectoryStructure) {
        SimpleShareRepository.showDirectoryStructure = showDirectoryStructure;
    }
}
