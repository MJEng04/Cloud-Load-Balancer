package com.mycompany.javafxapplication1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            // Loads login screen
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("primary.fxml"));
            Parent root = loader.load();
            
            // Sets up inital scene
            Scene scene = new Scene(root, 640, 480);
            stage.setScene(scene);
            stage.setTitle("Load Balancer Application - Login");
            stage.show();
       
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

}