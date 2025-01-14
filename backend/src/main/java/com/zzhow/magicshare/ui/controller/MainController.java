package com.zzhow.magicshare.ui.controller;

import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.ui.service.ShareService;
import com.zzhow.magicshare.ui.service.impl.ShareServiceImpl;
import com.zzhow.magicshare.util.FileUtil;
import com.zzhow.magicshare.util.InternetUtil;
import com.zzhow.magicshare.util.MessageBox;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZHow
 * @date 2025/1/13
 */
public class MainController {
    @FXML
    private Label label1;
    @FXML
    private Label label2;
    @FXML
    private Label label3;
    @FXML
    private Label label4;
    @FXML
    private TextField textField1;
    @FXML
    private TextField textField2;
    @FXML
    private Button button1;
    @FXML
    private Button button2;

    private final ShareService shareService = new ShareServiceImpl();

    private boolean serviceIsStarted = false;

    @FXML
    private void initialize() {
        label2.setText(InternetUtil.getLocalIpAddress());
    }

    @FXML
    private void onStartOrStopServiceClicked() {
        if (serviceIsStarted) {
            button1.setText("启动服务");
            shareService.stopService();
            MessageBox.success("停止成功", "MagicShare 服务停止成功");
            serviceIsStarted = false;

            return;
        }


        byte i = shareService.startService(textField1.getText());
        switch (i) {
            case 0 -> {
                MessageBox.success("启动成功", "MagicShare 服务启动成功");
                button1.setText("停止服务");
                serviceIsStarted = true;
            }
            case 1 -> {
                MessageBox.error("端口号错误", "端口号应为 0～65535 的整数");
            }
            case 2 -> {
                MessageBox.error("端口被占用", "请尝试更换端口号");
            }
        }
    }

    @FXML
    private void onSearchFileClicked() {
        String path = textField2.getText();
        List<FileDetail> files = new ArrayList<>();

        FileUtil.find(path, files);
    }
}
