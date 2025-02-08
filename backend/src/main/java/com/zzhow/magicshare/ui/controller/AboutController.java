package com.zzhow.magicshare.ui.controller;

import com.zzhow.magicshare.repository.LanguageRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ResourceBundle;

/**
 * @author ZZHow
 * @date 2025/2/8
 */
public class AboutController {
    @FXML
    private Label label1;
    @FXML
    private Label label2;
    @FXML
    private Label label3;
    @FXML
    private Label label4;
    @FXML
    private Label label5;

    @FXML
    private void initialize() {
        switchLanguage();
    }

    private void switchLanguage() {
        ResourceBundle bundle = LanguageRepository.bundle;

        if (LanguageRepository.getLanguage().contains("zh"))
            label2.setText(bundle.getString("magicShare") + " 2.0.0");
        else
            label2.setVisible(false);
        label3.setText(bundle.getString("features"));
        label4.setText(bundle.getString("featuresContent"));
    }
}
