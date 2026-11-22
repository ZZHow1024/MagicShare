package com.zzhow.magicshare.ui.controller;

import com.zzhow.magicshare.repository.LanguageRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author ZZHow
 * create 2025/2/8
 * update 2026/11/22
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

    /**
     * 读取构建时由 Maven 注入的版本号（version.properties，单一来源为 pom.xml）
     */
    private String readVersion() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("version.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty("version", "unknown");
        } catch (IOException | NullPointerException e) {
            return "unknown";
        }
    }

    private void switchLanguage() {
        ResourceBundle bundle = LanguageRepository.bundle;

        if (LanguageRepository.getLanguage().contains("zh"))
            label2.setText(bundle.getString("magicShare") + " " + readVersion());
        else
            label2.setVisible(false);
        label3.setText(bundle.getString("features"));
        label4.setText(bundle.getString("featuresContent"));
    }
}
