package controller;

import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import model.App;
import model.People.Leader;
import model.People.User;
import model.Tools.SceneSwitcher;

import java.net.URL;
import java.util.ResourceBundle;

public class Login implements Initializable {

    @FXML
    private TextField email;
    @FXML
    private Button forgotPassword;
    @FXML
    private PasswordField password;
    @FXML
    private Button login, signUp;
    @FXML
    private Label error;
    @FXML
    private JFXToggleButton toggle;
    @FXML
    private Line line0, line1;

    private String appId = "2864933576924620";
    private String appSecret = "5b11cb850b5100f411224c64f188360f";
    private Stage stage;

    private static final String SUCCESS_URL = "https://www.facebook.com/connect/login_success.html";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        password.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused){
                password.setText("");
                error.setText("");
            }
        });
        signUp.setOnMouseClicked(e -> App.instance.setScene(SceneSwitcher.instance.getScene("CreateAccount")));
        login.setOnMouseClicked(e -> {
            if(toggle.isSelected())
            {
                App.instance.setScene(SceneSwitcher.instance.getScene("Home"));
                return;
            }
            if(checkEmail() && checkPassword()){
                int id;
//                    Thread thread = new Thread(() -> {
//                        id = App.databaseManager.checkCredentials(email.getText(), password.getText());
//                    });
//                    thread.start();
//                    thread.join();
                if((id = App.mySqlManager.checkCredentials(email.getText(), password.getText())) == -1){
                    error.setText("Incorrect email or password");
                    redLines();
                    return;
                }
                email.setText("");
                password.setText("");
                error.setText("");
                redLines();
                User user = App.mySqlManager.getUser(id);
                App.instance.setSession(App.mySqlManager.getUser(id));
                if(user instanceof Leader)
                    new Thread(() -> ((Calendar)SceneSwitcher.instance.getController("Calendar")).loadUser(((Leader)user).getLeaderOf())).start();
                App.instance.setScene(SceneSwitcher.instance.getScene("Home"));
                resetLines();
            }
        });
    }

    private boolean checkEmail() {
        if(email.getText().isBlank()){
            error.setText("Email is empty");
            return false;
        }
        else if(!email.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")){
            error.setText("Incorrect email format");
            return false;
        }
        return true;
    }

    private boolean checkPassword(){
        if(password.getText().isBlank()){
            error.setText("Password is empty");
            return false;
        }
        else if(password.getText().length() < 5){
            error.setText("Incorrect password length");
            return false;
        }
        return true;
    }

    private void redLines(){
        //?change CSS class
        line0.setStroke(Paint.valueOf("#ff4c4c"));
        line0.setStrokeWidth(2);
        line1.setStroke(Paint.valueOf("#ff4c4c"));
        line1.setStrokeWidth(2);
        email.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused){
                line0.setStroke(Paint.valueOf("#000000"));
                line0.setStrokeWidth(1);
                line1.setStroke(Paint.valueOf("#000000"));
                line1.setStrokeWidth(1);
                error.setText("");
            }
        });
    }

    private void resetLines(){
        line0.setStroke(Paint.valueOf("#000000"));
        line0.setStrokeWidth(1);
        line1.setStroke(Paint.valueOf("#000000"));
        line1.setStrokeWidth(1);
    }
}



