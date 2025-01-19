package com.zzhow.magicshare.ui.window;

import com.zzhow.magicshare.MagicShareApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * @author ZZHow
 * @date 2025/01/17
 */
public class PromptWindow extends javafx.application.Application {
    private static Stage stage = null;

    @Override
    public void start(Stage stage) throws IOException {
        PromptWindow.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("prompt-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 780, 450);
        stage.setTitle("MagicShare");
        stage.setScene(scene);
        Image icon = new Image(Objects.requireNonNull(MagicShareApplication.class.getResourceAsStream("/image/icon.png")));
        stage.getIcons().add(icon);
        stage.setResizable(false);
        stage.show();
    }

    public static void close() {
        stage.close();
        MainWindow.open();
    }

    public static void show() {
        launch();
    }
}
