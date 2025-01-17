package com.zzhow.magicshare.ui.controller;

import com.zzhow.magicshare.ui.window.PromptWindow;
import javafx.fxml.FXML;

/**
 * @author ZZHow
 * @date 2025/01/17
 */
public class PromptController {
    @FXML
    private void onAgreeClicked() {
        PromptWindow.close();
    }

    @FXML
    private void onExitClicked() {
        System.exit(0);
    }
}
