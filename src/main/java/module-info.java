module com.zzhow.magicshare {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires spring.boot;
    requires spring.boot.autoconfigure;


    opens com.zzhow.magicshare to javafx.fxml;
    exports com.zzhow.magicshare;
    opens com.zzhow.magicshare.ui to javafx.fxml;
    exports com.zzhow.magicshare.ui;
}