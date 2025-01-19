package com.zzhow.magicshare.util;

import com.zzhow.magicshare.MagicShareApplication;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * @author ZZHow
 * @date 2025/01/14
 */
public class MessageBox {

    public static void alert(Alert.AlertType type, String title, String headerText, String contentText) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        Image icon = new Image(Objects.requireNonNull(MagicShareApplication.class.getResourceAsStream("/image/icon.png")));
        stage.getIcons().add(icon);

        alert.showAndWait();
    }

    public static void error(String headerText, String contentText) {
        alert(Alert.AlertType.ERROR,
                "错误",
                headerText,
                contentText);
    }

    public static void success(String headerText, String contentText) {
        alert(Alert.AlertType.INFORMATION,
                "成功",
                headerText,
                contentText);
    }

}
