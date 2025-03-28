package controllers;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Registration extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox leftPanel = new VBox(5); // Reduced spacing between elements
        leftPanel.setPadding(new Insets(40));
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setStyle("-fx-background-color: #ffffff;");
        leftPanel.setMinWidth(400);

        Label logo = new Label("E-Learning");
        logo.setFont(Font.font("Inter", FontWeight.BOLD, 24));

        Label title = new Label("Create an account");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 28));

        Label subtitle = new Label("Start your 30-day free trial now!");
        subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.GRAY);

        VBox formContainer = new VBox(10); // Grouped form fields together
        
        Label usernameLabel = new Label("Username");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMinHeight(40);

        Label emailLabel = new Label("Email");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setMinHeight(40);

        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMinHeight(40);

        Label confirmPasswordLabel = new Label("Confirm Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setMinHeight(40);

        Button createAccountButton = new Button("Create Account");
        createAccountButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        createAccountButton.setMinHeight(40);
        createAccountButton.setMinWidth(300);
        createAccountButton.setOnAction(e -> validateForm(usernameField, emailField, passwordField, confirmPasswordField));

        Label loginLink = new Label("Already have an account? Log in");
        loginLink.setTextFill(Color.BLUE);
        loginLink.setOnMouseClicked(e -> new Login().start(primaryStage));

        formContainer.getChildren().addAll(
            usernameLabel, usernameField, 
            emailLabel, emailField, 
            passwordLabel, passwordField, 
            confirmPasswordLabel, confirmPasswordField, 
            createAccountButton, loginLink
        );

        leftPanel.getChildren().addAll(logo, title, subtitle, formContainer);

        StackPane rightPanel = new StackPane();
        rightPanel.setStyle("-fx-background-color: #f9f9f9;");
        rightPanel.setMinWidth(700);

        ImageView imageView = new ImageView(new Image("file:C:/Users/HP/eclipse-workspace/projectfx/resources/LALA.png"));
        imageView.setFitWidth(700);
        imageView.setPreserveRatio(true);

        rightPanel.getChildren().add(imageView);

        HBox mainContainer = new HBox(leftPanel, rightPanel);

        Scene scene = new Scene(mainContainer, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.setWidth(1100);
        primaryStage.setHeight(700);
        primaryStage.centerOnScreen();
        primaryStage.setResizable(false); // Disable resizing and remove the fullscreen button
        primaryStage.setTitle("E-Learning Platform");
        primaryStage.show();
    }

    private void validateForm(TextField username, TextField email, PasswordField password, PasswordField confirmPassword) {
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (username.getText().trim().isEmpty() || email.getText().trim().isEmpty() ||
            password.getText().trim().isEmpty() || confirmPassword.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please fill in all fields to proceed.");
        } else if (!email.getText().matches(emailPattern)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
        } else if (password.getText().length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Weak Password", "Password must be at least 6 characters long.");
        } else if (!password.getText().equals(confirmPassword.getText())) {
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "Passwords do not match. Please try again.");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your account has been created successfully!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
