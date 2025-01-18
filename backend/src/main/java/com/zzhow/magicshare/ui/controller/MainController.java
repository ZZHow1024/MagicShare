package com.zzhow.magicshare.ui.controller;

import com.zzhow.magicshare.repository.FileRepository;
import com.zzhow.magicshare.pojo.entity.FileDetail;
import com.zzhow.magicshare.ui.service.ShareService;
import com.zzhow.magicshare.ui.service.impl.ShareServiceImpl;
import com.zzhow.magicshare.ui.window.AboutWindow;
import com.zzhow.magicshare.ui.window.MainWindow;
import com.zzhow.magicshare.util.InternetUtil;
import com.zzhow.magicshare.util.MessageBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

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
    private TextField textField1;
    @FXML
    private TextField textField2;
    @FXML
    private Button button1;
    @FXML
    private Button button2;
    @FXML
    private TableView<FileDetail> tableView1;

    private final ShareService shareService = new ShareServiceImpl();

    private boolean serviceIsStarted = false;

    @FXML
    private void initialize() {
        label2.setText(InternetUtil.getLocalIpAddress());

        // 创建列
        TableColumn<FileDetail, String> fileNameCol = new TableColumn<>("文件名");
        fileNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        TableColumn<FileDetail, String> fileTypeCol = new TableColumn<>("类型");
        fileTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        TableColumn<FileDetail, String> fileSizeCol = new TableColumn<>("大小(KB)");
        fileSizeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSize() + ""));
        TableColumn<FileDetail, String> filePathCol = new TableColumn<>("相对路径");
        filePathCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPath()));
        tableView1.getColumns().addAll(fileNameCol, fileTypeCol, fileSizeCol, filePathCol);
        tableView1.setPlaceholder(new Label("分享列表为空"));

        // 设置列的宽度比例
        tableView1.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            fileNameCol.setPrefWidth(totalWidth * 0.25);
            fileTypeCol.setPrefWidth(totalWidth * 0.12);
            fileSizeCol.setPrefWidth(totalWidth * 0.13);
            filePathCol.setPrefWidth(totalWidth * 0.50);
        });
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
                MessageBox.error("端口号错误", "端口号应为 0～65535 的整数");
            }
            case 2 -> {
                MessageBox.error("端口被占用", "请尝试更换端口号");
            }
        }
    }

    @FXML
    private void onSelectFileClicked() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("打开文件/文件夹");
        try {
            textField2.setText(chooser.showOpenDialog(MainWindow.getStage()).getAbsolutePath());
            onSearchFileClicked();
        } catch (NullPointerException e) {
            // 未选择文件/文件夹
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
}
