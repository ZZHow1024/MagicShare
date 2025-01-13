package com.zzhow.magicshare.ui.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author ZZHow
 * @date 2025/1/12
 */
public class MainView extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainView.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("MagicShare");
        stage.setScene(scene);
        // Image icon = new Image(Objects.requireNonNull(MagicShareApplication.class.getResourceAsStream("/image/icon.png")));
        // stage.getIcons().add(icon);
        stage.setResizable(false);
        stage.setOnHiding(windowEvent -> {
            System.exit(0);
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}