package com.zzhow.magicshare.ui.controller;

import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.repository.LanguageRepository;
import com.zzhow.magicshare.ui.service.ShareService;
import com.zzhow.magicshare.ui.service.impl.ShareServiceImpl;
import com.zzhow.magicshare.ui.window.AboutWindow;
import com.zzhow.magicshare.ui.window.MainWindow;
import com.zzhow.magicshare.util.InternetUtil;
import com.zzhow.magicshare.util.MessageBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author ZZHow
 * @date 2025/1/13
 */
public class MainController {
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
    private Label label6;
    @FXML
    private Label label7;
    @FXML
    private TextField textField1;
    @FXML
    private TextField textField2;
    @FXML
    private Button button1;
    @FXML
    private Button button2;
    @FXML
    private Button button3;
    @FXML
    private Button button4;
    @FXML
    private TableView<FileDetail> tableView1;
    @FXML
    private ChoiceBox<String> languageSelector;

    private final ShareService shareService = new ShareServiceImpl();

    private boolean serviceIsStarted = false;

    @FXML
    private void initialize() {
        label2.setText(InternetUtil.getLocalIpAddress());

        // 创建列
        TableColumn<FileDetail, String> fileNameCol = new TableColumn<>("File name");
        fileNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        TableColumn<FileDetail, String> fileTypeCol = new TableColumn<>("Type");
        fileTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        TableColumn<FileDetail, String> fileSizeCol = new TableColumn<>("Size(KB)");
        fileSizeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSize() + ""));
        TableColumn<FileDetail, String> filePathCol = new TableColumn<>("Relative path");
        filePathCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPath()));
        tableView1.getColumns().addAll(fileNameCol, fileTypeCol, fileSizeCol, filePathCol);
        tableView1.setPlaceholder(new Label("Share list is empty"));

        // 设置列的宽度比例
        tableView1.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            fileNameCol.setPrefWidth(totalWidth * 0.25);
            fileTypeCol.setPrefWidth(totalWidth * 0.12);
            fileSizeCol.setPrefWidth(totalWidth * 0.13);
            filePathCol.setPrefWidth(totalWidth * 0.50);
        });

        languageSelector.getItems().addAll("简体中文", "繁體中文", "English");
        String language = Locale.getDefault().toLanguageTag();
        if (language.contains("zh")) {
            if (language.contains("Hans"))
                language = "zh_Hans";
            else if (language.contains("Hant"))
                language = "zh_Hant";
            else if (language.contains("CN"))
                language = "zh_Hans";
            else
                language = "zh_Hant";
        } else {
            language = "en_US";
        }

        language = switch (language) {
            case "zh_Hans" -> "简体中文";
            case "zh_Hant" -> "繁體中文";
            case "en_US" -> "English";
            default -> "简体中文";
        };
        languageSelector.setValue(language);
        switchLanguage();
    }

    @FXML
    private void onStartOrStopServiceKeyDown(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER))
            onStartOrStopServiceClicked();
    }

    @FXML
    private void onStartOrStopServiceClicked() {
        if (serviceIsStarted) {
            textField1.setDisable(false);
            label1.setText("内网IPv4地址：");
            label2.setText(InternetUtil.getLocalIpAddress());
            button1.setText("启动服务");
            shareService.stopService();
            MessageBox.success("停止成功", "MagicShare 服务停止成功");
            serviceIsStarted = false;

            return;
        }


        byte i = shareService.startService(textField1.getText());
        switch (i) {
            case 0 -> {
                textField1.setDisable(true);
                label1.setText("分享URL：");
                label2.setText("http://" + InternetUtil.getLocalIpAddress() + ":" + textField1.getText());
                MessageBox.success("启动成功", "MagicShare 服务启动成功");
                button1.setText("停止服务");
                serviceIsStarted = true;
            }
            case 1 -> {
                MessageBox.error("端口号错误", "端口号应为 1～65535 的整数");
            }
            case 2 -> {
                MessageBox.error("端口被占用", "请尝试更换端口号");
            }
        }
    }

    @FXML
    private void onSelectFileClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择文件夹");
        try {
            textField2.setText(directoryChooser.showDialog(MainWindow.getStage()).getAbsolutePath());
            onSearchFileClicked();
        } catch (NullPointerException e) {
            // 未选择文件夹
        }
    }

    @FXML
    private void onClearFileClicked() {
        textField2.setText("");
        onSearchFileClicked();
    }

    @FXML
    private void onSearchFileKeyDown(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER)
            onSearchFileClicked();
    }

    @FXML
    private void onSearchFileClicked() {
        label6.setText("0");
        tableView1.getItems().clear();

        shareService.searchFile(textField2.getText());

        for (FileDetail fileDetail : FileRepository.getFiles())
            tableView1.getItems().add(fileDetail);
        label6.setText(FileRepository.size() + "");
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != event.getTarget() // 是否从外部拖拽
                && event.getDragboard().hasFiles()) { // 是否拖拽了文件
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);  // 接受拖拽的文件
        }
        event.consume();
    }

    @FXML
    private void onDragFile(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        if (!files.isEmpty()) {
            textField2.setText(files.get(0).getAbsolutePath());
        }

        onSearchFileClicked();
    }

    @FXML
    private void onAboutClicked() {
        AboutWindow.open();
    }

    @FXML
    private void switchLanguage() {
        String selectorValue = languageSelector.getValue();
        selectorValue = switch (selectorValue) {
            case "简体中文" -> "zh_Hans";
            case "繁體中文" -> "zh_Hant";
            case "English" -> "en_US";
            default -> "zh_Hans";
        };

        LanguageRepository.setLanguage(selectorValue);

        ResourceBundle bundle = LanguageRepository.bundle;

        ObservableList<TableColumn<FileDetail, ?>> columns = tableView1.getColumns();
        columns.get(0).setText(bundle.getString("fileName"));
        columns.get(1).setText(bundle.getString("type"));
        columns.get(2).setText(bundle.getString("size"));
        columns.get(3).setText(bundle.getString("relativePath"));
        tableView1.setPlaceholder(new Label(bundle.getString("shareListIsEmpty")));

        label1.setText(bundle.getString("label1"));
        label3.setText(bundle.getString("label3"));
        label4.setText(bundle.getString("label4"));
        label5.setText(bundle.getString("label5"));
        label7.setText(bundle.getString("label7"));
        button1.setText(bundle.getString("button1"));
        button2.setText(bundle.getString("button2"));
        button3.setText(bundle.getString("button3"));
        button4.setText(bundle.getString("button4"));
    }
}
