module com.zzhow.magicshare {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.web;


    opens com.zzhow.magicshare to javafx.fxml;
    exports com.zzhow.magicshare;
    opens com.zzhow.magicshare.ui.window to javafx.fxml;
    exports com.zzhow.magicshare.ui.window;
    opens com.zzhow.magicshare.ui.controller to javafx.fxml;
    exports com.zzhow.magicshare.ui.controller;

    exports com.zzhow.magicshare.controller;
    exports com.zzhow.magicshare.util;
    opens com.zzhow.magicshare.util to javafx.fxml;

    exports com.zzhow.magicshare.pojo.entity;
}