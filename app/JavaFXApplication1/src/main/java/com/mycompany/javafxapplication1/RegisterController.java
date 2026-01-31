package com.mycompany.javafxapplication1;

import com.mycompany.javafxapplication1.database.UserDAO;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Incorporated MySQL
public class RegisterController {

    @FXML
    private Button registerBtn;

    @FXML
    private Button backLoginBtn;

    @FXML
    private PasswordField passPasswordField;

    @FXML
    private PasswordField rePassPasswordField;

    @FXML
    private TextField userTextField;
    
    @FXML
    private Text fileText;
    
    @FXML
    private Button selectBtn;
    
    @FXML
    private void selectBtnHandler(ActionEvent event) throws IOException {
        Stage primaryStage = (Stage) selectBtn.getScene().getWindow();
        primaryStage.setTitle("Select a File");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        
        if(selectedFile!=null){
            fileText.setText((String)selectedFile.getCanonicalPath());
        }
        
    }

    private void dialogue(String headerMsg, String contentMsg, Alert.AlertType type) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);
        Optional<ButtonType> result = alert.showAndWait();
    }

    @FXML
    private void registerBtnHandler(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) registerBtn.getScene().getWindow();
        
        
        try {
            
            String username = userTextField.getText();
            String password = passPasswordField.getText();
            String rePassword = rePassPasswordField.getText();
            
            // Validation
            if (username.isEmpty() || password.isEmpty()) {
                dialogue("Empty Fields", "Please fill in all fields.", Alert.AlertType.WARNING);
                return;
            }
            
            if (!password.equals(rePassword)) {
                dialogue("Password Mismatch", "Passwords do not match. Please try again.", Alert.AlertType.ERROR);
                return;
            }
            
            // NEW: Register with MySQL
            UserDAO userDAO = new UserDAO();
            
            // Check if user already exists
            if (userDAO.userExists(username)) {
                dialogue("Username Taken", "This username already exists. Please choose another.", Alert.AlertType.ERROR);
                return;
            }
            
            // Register user
            if (userDAO.registerUser(username, password)) {
                dialogue("Success!", "Account created successfully!\n\nYou can now login.", Alert.AlertType.INFORMATION);
                
                // Go to file management screen
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("filemanagement.fxml"));
                Parent root = loader.load();
                
                FileManagementController controller = loader.getController();
                controller.setUsername(username);
                
                Scene scene = new Scene(root, 800, 600);
                secondaryStage.setScene(scene);
                secondaryStage.setTitle("File Management - " + username);
                secondaryStage.show();
                primaryStage.close();
                
            } else {
                dialogue("Registration Failed", "Could not create account. Please try again.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    

    @FXML
    private void backLoginBtnHandler(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) backLoginBtn.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("primary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Login");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
