package com.github.whz.client.controller;

import com.github.whz.client.basic.view.FXMLController;
import com.github.whz.client.mode.LoginInfo;
import com.github.whz.client.service.MainService;
import com.github.whz.hcnetsdk.model.DeviceInfo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class MainController implements Initializable {

    @FXML
    private TextField ipField;

    @FXML
    private TextField userField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField portField;

    @FXML
    private StackPane imagePanel;

    @FXML
    private ImageView imageView;

    @Resource
    private LoginInfo loginInfo;

    @Resource
    private MainService mainService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.ipField.textProperty().bindBidirectional(loginInfo.ipProperty());
        this.userField.textProperty().bindBidirectional(loginInfo.userProperty());
        this.passwordField.textProperty().bindBidirectional(loginInfo.passwordProperty());
        this.portField.textProperty().bindBidirectional(loginInfo.portProperty());
        this.imageView.fitWidthProperty().bind(imagePanel.widthProperty());
        this.imageView.fitHeightProperty().bind(imagePanel.heightProperty());
    }

    @FXML
    protected void onLoginButtonClick() {
        mainService.login(this.loginInfo);
    }

    @FXML
    protected void onPreviewButtonClick() {
        mainService.preview(1, true, imageView);
    }
}