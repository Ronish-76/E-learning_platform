package controllers;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Set the application icon (ensure the path is correct)
        primaryStage.getIcons().add(new Image("file:C:/Users/HP/eclipse-workspace/projectfx/resources/app_icon.png"));

        Login loginPage = new Login();
        loginPage.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
