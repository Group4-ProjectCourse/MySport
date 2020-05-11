package controller;

import com.jfoenix.controls.JFXComboBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import model.App;
import model.Tools.SceneSwitcher;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class About extends Menu implements Initializable {

    @FXML
    public Button buttonReturn;


    @Override
    protected void onBurgerOpen() {

    }

    @Override
    protected void onBurgerClose() {

    }

    @Override
    protected void onSceneSwitch() {

    }

    @Override
    protected void onAppClose() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void buttonReturnClick() {
        App.instance.setScene(SceneSwitcher.instance.getScene("Settings"));
    }
}