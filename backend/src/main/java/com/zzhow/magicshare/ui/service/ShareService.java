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
     * @return 0-启动成功；1-端口号错误；2-端口被占用
     */
    byte startService(String port);

    /**
     * 停止 MagicShare 服务
     */
    void stopService();
}
