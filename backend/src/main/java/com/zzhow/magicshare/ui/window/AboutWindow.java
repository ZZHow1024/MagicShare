package com.zzhow.magicshare.ui.window;

import com.zzhow.magicshare.MagicShareApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * @author ZZHow
 * @date 2025/01/17
 */
public class AboutWindow extends javafx.application.Application {
    private static Stage stage = null;

    @Override
    public void start(Stage stage) throws IOException {
        AboutWindow.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("about-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("MagicShare-About");
        stage.setScene(scene);
        Image icon = new Image(Objects.requireNonNull(MagicShareApplication.class.getResourceAsStream("/image/icon.png")));
        stage.getIcons().add(icon);
        stage.setResizable(false);
        stage.show();
    }

    public static void open() {
        Stage stage = new Stage();
        stage.setTitle("MagicShare-About");
        Image icon = new Image(Objects.requireNonNull(MagicShareApplication.class.getResourceAsStream("/image/icon.png")));
        stage.getIcons().add(icon);
        stage.setResizable(false);
        try {
            Pane load = FXMLLoader.load(Objects.requireNonNull(MainWindow.class.getResource("about-window.fxml")));
            Scene scene = new Scene(load);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        stage.close();
        MainWindow.open();
    }

    public static void show() {
        launch();
    }
}
