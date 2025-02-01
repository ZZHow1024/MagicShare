package com.zzhow.magicshare.ui.service;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
public interface ShareService {
    /**
     * 启动 MagicShare 服务
     *
     * @param port 端口号
     * @return 0-启动成功；1-端口号错误；2-端口被占用；3-连接密码不能为空；4-连接密码错误
     */
    byte startService(String port, String password, boolean isEnablePassword);

    /**
     * 停止 MagicShare 服务
     */
    void stopService();

    /**
     * 查找文件
     *
     * @param path 路径
     */
    void searchFile(String path);
}
