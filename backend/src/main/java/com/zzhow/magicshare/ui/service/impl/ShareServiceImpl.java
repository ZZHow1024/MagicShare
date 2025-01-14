package com.zzhow.magicshare.ui.service.impl;

import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.util.Application;
import com.zzhow.magicshare.ui.service.ShareService;
import com.zzhow.magicshare.util.FileUtil;
import com.zzhow.magicshare.util.InternetUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
public class ShareServiceImpl implements ShareService {
    private ConfigurableApplicationContext applicationContext;

    /**
     * 启动 MagicShare 服务
     *
     * @param portStr 端口号
     * @return 0-启动成功；1-端口号错误；2-端口被占用
     */
    @Override
    public byte startService(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            if (port < 0 || port > 65535)
                return 1;
            if (InternetUtil.isPortInUse(port))
                return 2;
            else {
                applicationContext = Application.startSpringBoot("--server.port=" + port);

                return 0;
            }
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * 停止 MagicShare 服务
     */
    @Override
    public void stopService() {
        SpringApplication.exit(applicationContext, () -> 0);
    }

    /**
     * 查找文件
     *
     * @param path 路径
     */
    @Override
    public void searchFile(String path) {
        FileRepository.clearFiles();
        FileUtil.find(path, FileRepository.getFiles());
    }
}
