package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import model.App;
import model.Tools.SceneSwitcher;

import java.net.URL;
import java.util.ResourceBundle;

public class Support extends Menu implements Initializable {

    @FXML
    private Button faq, contactForm;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bindTab(this);

    }

    @FXML
    public void contactFormButtonClick() {
        App.instance.setScene(SceneSwitcher.instance.getScene("ContactForm"));
    }

    @FXML
    public void faqButtonClick() {
        App.instance.setScene(SceneSwitcher.instance.getScene("FAQ"));
    }

    @Override
    protected void onBurgerOpen() {

    }

    @Override
    protected void onBurgerClose() {

    }

    @Override
    protected void onBeforeSceneSwitch() {

    }

    @Override
    protected void onBeforeLogout() {

    }
}
