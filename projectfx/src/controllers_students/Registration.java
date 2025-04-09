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
import org.mindrot.jbcrypt.BCrypt;  // Import bcrypt

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import dao.DatabaseConnection;

public class Registration extends Application {
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        VBox leftPanel = new VBox(5);
        leftPanel.setPadding(new Insets(40));
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setStyle("-fx-background-color: #ffffff;");
        leftPanel.setMinWidth(400);
        
        Label logo = new Label("E-Learning");
        logo.setFont(Font.font("Inter", FontWeight.BOLD, 24)); 
        
        Label title = new Label("Create an account");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 28));
        
        Label subtitle = new Label("   ");
        subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.GRAY);
        
        VBox formContainer = new VBox(10);
        
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
        createAccountButton.setOnAction(e -> registerUser(usernameField.getText(), emailField.getText(), 
                                                        passwordField.getText(), confirmPasswordField.getText()));
        
        Label loginLink = new Label("Already have an account? Log in");
        loginLink.setTextFill(Color.BLUE);
        loginLink.setOnMouseClicked(e -> navigateToLogin());
        
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
        primaryStage.setResizable(false);
        primaryStage.setTitle("E-Learning Platform");
        primaryStage.show();
    }
    
    private void navigateToLogin() {
        // Open the login page
        Login loginPage = new Login();
        loginPage.start(primaryStage);
    }
    
    private void registerUser(String username, String email, String password, String confirmPassword) {
        // Validate input
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (username.trim().isEmpty() || email.trim().isEmpty() ||
            password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please fill in all fields to proceed.");
            return;
        } else if (!email.matches(emailPattern)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
            return;
        } else if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Weak Password", "Password must be at least 6 characters long.");
            return;
        } else if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "Passwords do not match. Please try again.");
            return;
        }
        
        // Hash the password using bcrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        // If all validation passes, insert user into database
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // Get database connection
            conn = DatabaseConnection.getConnection();
            
            // Check if username or email already exists
            String checkQuery = "SELECT COUNT(*) FROM Users WHERE username = ? OR email = ?";
            pstmt = conn.prepareStatement(checkQuery);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            rs = pstmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", 
                        "Username or email already exists. Please use different credentials.");
                return;
            }
            
            // Close resources from first query
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            
            // Insert user into Users table
            String insertUserSQL = "INSERT INTO Users (username, email, passwordHash, role) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertUserSQL, java.sql.Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword); // Store hashed password
            pstmt.setString(4, "Student");
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated user ID
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    
                    // Close resources
                    if (rs != null) rs.close();
                    if (pstmt != null) pstmt.close();
                    
                    // Insert into Students table
                    String insertStudentSQL = "INSERT INTO Students (studentID, userID) VALUES (?, ?)";
                    pstmt = conn.prepareStatement(insertStudentSQL);
                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                    
                    // Show a brief success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Registration successful! Redirecting to login page...");
                    successAlert.showAndWait();
                    
                    // Navigate to login page
                    navigateToLogin();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Failed to create user account.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close all resources
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
