package controllers_students;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dao.DatabaseConnection;

public class Login extends Application {
    
    // Create a User object to store the logged-in user information
    private static User loggedInUser = null;

    @Override
    public void start(Stage primaryStage) {
        VBox leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(40));
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setStyle("-fx-background-color: #ffffff;");
        leftPanel.setMinWidth(400);

        Label logo = new Label("E-Learning");
        logo.setFont(Font.font("Inter", FontWeight.BOLD, 24));

        Label title = new Label("Log in to your account");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 28));

        Label subtitle = new Label("Welcome back! Please enter your details.");
        subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.GRAY);

        Label emailLabel = new Label("Username");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your username");
        emailField.setMinHeight(40);

        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMinHeight(40);

        Button loginButton = new Button("Log in");
        loginButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        loginButton.setMinHeight(40);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        
        loginButton.setOnAction(e -> validateForm(emailField, passwordField, primaryStage));

        Separator separator = new Separator();
        separator.setMaxWidth(Double.MAX_VALUE);

        Label signUpLink = new Label("Don't have an account? Sign up");
        signUpLink.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        signUpLink.setTextFill(Color.BLUE);
        signUpLink.setOnMouseClicked(e -> new Registration().start(primaryStage));

        leftPanel.getChildren().addAll(logo, title, subtitle, emailLabel, emailField, passwordLabel, passwordField, loginButton, separator, signUpLink);

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
        primaryStage.setTitle("Log In");
        primaryStage.show();
    }

    private void validateForm(TextField usernameField, PasswordField passwordField, Stage primaryStage) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please enter both username and password.");
            return;
        }
        
        // Authenticate user against database
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Cannot connect to database. Please try again later.");
                return;
            }
            
            String query = "SELECT * FROM users WHERE username = ? AND passwordHash = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Note: In a production app, you would hash the password
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // User found, create User object with data from database
                loggedInUser = new User(
                    rs.getInt("userID"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("passwordHash"),
                    rs.getString("role")
                );
                
                // Show success message and wait for user to click OK
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login Success");
                alert.setHeaderText(null);
                alert.setContentText("Welcome " + loggedInUser.getUsername() + "! You have successfully logged in as " + loggedInUser.getRole() + ".");
                
                // Wait for user to close the alert
                alert.showAndWait();
                
                // Close login window and open dashboard
                primaryStage.close();
                launchDashboard();
            } else {
                // User not found or password incorrect
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password. Please try again.");
                passwordField.clear(); // Clear password field for security
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void launchDashboard() {
        // You might want to pass the loggedInUser to the Dashboard
        Dashboard_S dashboard = new Dashboard_S();
        Stage dashboardStage = new Stage();
        try {
            // Potentially modify this to pass the user information
            dashboard.start(dashboardStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Getter for the logged-in user
    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static void main(String[] args) {
        launch(args);
    }
}