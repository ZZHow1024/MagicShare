package com.zzhow.magicshare.ui.controller;

import com.zzhow.magicshare.ui.window.PromptWindow;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author ZZHow
 * @date 2025/01/17
 */
public class PromptController {
    @FXML
    private Label title;
    @FXML
    private Label content;
    @FXML
    private Button exit;
    @FXML
    private Button accept;

    @FXML
    private void initialize() {
        String language = Locale.getDefault().toLanguageTag();
        if (language.contains("zh")) {
            if (language.contains("CN") || language.contains("cn"))
                language = "zh_HANS";
            else if (language.contains("HANS") || language.contains("Hans"))
                language = "zh_HANS";
            else
                language = "zh_HANT";
        } else {
            language = "en_US";
        }

        ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", Locale.of(language.split("_")[0], language.split("_")[1]));
        if (language.contains("en")) {
            title.setVisible(false);
            content.setLayoutY(65);
        } else {
            title.setText(bundle.getString("magicShare"));
        }
        content.setText(bundle.getString("promptContent"));
        exit.setText(bundle.getString("exit"));
        accept.setText(bundle.getString("accept"));
    }

    @FXML
    private void onAgreeClicked() {
        PromptWindow.close();
    }

    @FXML
    private void onExitClicked() {
        System.exit(0);
    }
}
