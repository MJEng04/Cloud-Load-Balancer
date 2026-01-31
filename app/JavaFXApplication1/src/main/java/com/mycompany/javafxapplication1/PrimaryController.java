package com.mycompany.javafxapplication1;

import com.mycompany.javafxapplication1.database.UserDAO; 
import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class PrimaryController {

    @FXML
    private Button registerBtn;

    @FXML
    private TextField userTextField;

    @FXML
    private PasswordField passPasswordField;

    @FXML
    private void registerBtnHandler(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) registerBtn.getScene().getWindow();
        DB myObj = new DB();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Register a new User");
            secondaryStage.show();
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void dialogue(String headerMsg, String contentMsg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);
        alert.showAndWait();
    }

    @FXML
    private void switchToSecondary() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) registerBtn.getScene().getWindow();
        try {
            UserDAO userDAO = new UserDAO();
            String username = userTextField.getText();
            String password = passPasswordField.getText();
            
            // Login Validation with MySQL
            if(userDAO.validateUser(username, password)){
                // Loads file management screen
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("filemanagement.fxml"));
                Parent root = loader.load();
                
                // Retrieves controller and sets username
                FileManagementController controller = loader.getController();
                controller.setUsername(username);
                
                // Shows file management screen
                Scene scene = new Scene(root, 800, 600);
                secondaryStage.setScene(scene);
                secondaryStage.setTitle("File Management - " + username);
                secondaryStage.show();
                primaryStage.close();
            }
            else{
                dialogue("Invalid User Name / Password","Please try again!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
