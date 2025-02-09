package com.zzhow.magicshare.repository;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author ZZHow
 * @date 2025/2/1
 */
public class ConnectionCountBinding {
    private static final StringProperty countProperty = new SimpleStringProperty("0");

    public static String getCount() {
        return countProperty.get();
    }

    public static void setCount(String value) {
        // 在 JavaFX 应用线程上执行
        Platform.runLater(() -> {
            countProperty.set(value);
        });
    }

    public static StringProperty countProperty() {
        return countProperty;
    }
}
