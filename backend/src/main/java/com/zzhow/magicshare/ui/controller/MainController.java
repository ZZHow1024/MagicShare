package com.zzhow.magicshare.ui.controller;

import com.zzhow.magicshare.util.InternetUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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
    private void initialize() {
        label2.setText(InternetUtil.getLocalIpAddress());
    }
}
