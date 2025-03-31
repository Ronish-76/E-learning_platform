package controllers_students;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import dao.DatabaseConnection;

public class ProfilePage {
    
    private TextField nameField;
    private TextField emailField;
    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    
    // Current user info
    private int currentUserId = 0;
    private String currentUsername = "";
    private String currentEmail = "";
    private String storedPasswordHash = "";
    private String userRole = "";
    
    // Debug info label to show errors or status
    private Label debugInfoLabel;
    
    public ProfilePage() {
        // Default constructor used when no user ID is provided
        debugInfoLabel = new Label("No user ID provided");
        debugInfoLabel.setStyle("-fx-text-fill: red;");
    }
    
    public ProfilePage(int userId) {
        this.currentUserId = userId;
        debugInfoLabel = new Label("Loading user ID: " + userId);
        loadUserDataFromDatabase();
    }
    
    // Method to set user ID after construction
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        debugInfoLabel.setText("User ID set to: " + userId);
        loadUserDataFromDatabase();
    }
    
    // Load user data from database
    private void loadUserDataFromDatabase() {
        if (currentUserId <= 0) {
            debugInfoLabel.setText("Invalid user ID: " + currentUserId);
            return; // No user ID provided
        }
        
        Connection conn = null;
        try {
            debugInfoLabel.setText("Connecting to database...");
            conn = DatabaseConnection.getConnection();
            
            if (conn == null) {
                debugInfoLabel.setText("Database connection failed - null connection");
                return;
            }
            
            String query = "SELECT username, email, passwordHash, role FROM Users WHERE userID = ?";
            debugInfoLabel.setText("Executing query for user ID: " + currentUserId);
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, currentUserId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentUsername = rs.getString("username");
                        currentEmail = rs.getString("email");
                        storedPasswordHash = rs.getString("passwordHash");
                        userRole = rs.getString("role");
                        
                        debugInfoLabel.setText("User data loaded: " + currentUsername);
                    } else {
                        debugInfoLabel.setText("User not found with ID: " + currentUserId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            debugInfoLabel.setText("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            debugInfoLabel.setText("Error: " + e.getMessage());
        } finally {
            // If your DatabaseConnection manages connection closures, you might not need this
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public Node getView() {
        VBox view = createProfilePage();
        
        // If we have no user data, show a message instead of empty fields
        if (currentUsername.isEmpty() && currentEmail.isEmpty()) {
            VBox errorBox = new VBox(20);
            errorBox.setAlignment(Pos.CENTER);
            errorBox.setPadding(new Insets(50));
            
            Label errorTitle = new Label("User Profile Not Available");
            errorTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            
            Label errorMessage = new Label("Could not load user profile data. Please ensure you are logged in.");
            errorMessage.setWrapText(true);
            
            // Add the debug info label
            debugInfoLabel.setWrapText(true);
            
            Button retryButton = new Button("Retry");
            retryButton.setOnAction(e -> loadUserDataFromDatabase());
            
            errorBox.getChildren().addAll(errorTitle, errorMessage, debugInfoLabel, retryButton);
            return errorBox;
        }
        
        return view;
    }
    
    private VBox createProfilePage() {
        VBox profilePage = new VBox(20);
        profilePage.setPadding(new Insets(30));
        profilePage.setAlignment(Pos.TOP_CENTER);
        profilePage.setStyle("-fx-background-color: #f4f4f4;");
        
        // Page title
        Label profileLabel = new Label("User Profile");
        profileLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        profileLabel.setStyle("-fx-text-fill: #333333;");
        
        // Create form content
        GridPane formGrid = createFormGrid();
        
        // Add action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button resetButton = new Button("Reset");
        styleButton(resetButton, "#555555", "#eeeeee");
        resetButton.setOnAction(e -> resetForm());
        
        Button saveButton = new Button("Save Changes");
        styleButton(saveButton, "white", "#4285f4");
        saveButton.setOnAction(e -> saveProfileChanges());
        
        actionButtons.getChildren().addAll(resetButton, saveButton);
        
        // Add debug info at the bottom
        debugInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        
        // Add all components to main container
        profilePage.getChildren().addAll(
            profileLabel,
            formGrid,
            actionButtons,
            debugInfoLabel
        );
        
        return profilePage;
    }
    
    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20, 10, 20, 10));
        grid.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Personal Information Section
        Label personalInfoLabel = new Label("Personal Information");
        personalInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        personalInfoLabel.setStyle("-fx-text-fill: #333333;");
        GridPane.setColumnSpan(personalInfoLabel, 2);
        grid.add(personalInfoLabel, 0, 0);
        
        // Username field (readonly since it's the primary identifier)
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField(currentUsername);
        usernameField.setEditable(false);
        usernameField.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #dddddd; " +
                "-fx-border-radius: 3; -fx-background-radius: 3;");
        addFormRow(grid, 1, usernameLabel, usernameField);
        
        // Name field (for display purposes, not stored in database currently)
        Label nameLabel = new Label("Full Name:");
        nameField = new TextField(currentUsername); // Using username as name placeholder
        styleTextField(nameField);
        addFormRow(grid, 2, nameLabel, nameField);
        
        // Email field
        Label emailLabel = new Label("Email Address:");
        emailField = new TextField(currentEmail);
        styleTextField(emailField);
        addFormRow(grid, 3, emailLabel, emailField);
        
        // User role (readonly)
        Label roleLabel = new Label("User Role:");
        TextField roleField = new TextField(userRole);
        roleField.setEditable(false);
        roleField.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #dddddd; " +
                "-fx-border-radius: 3; -fx-background-radius: 3;");
        addFormRow(grid, 4, roleLabel, roleField);
        
        // Add separator
        Separator separator = new Separator();
        GridPane.setColumnSpan(separator, 2);
        GridPane.setMargin(separator, new Insets(10, 0, 10, 0));
        grid.add(separator, 0, 5);
        
        // Password Change Section
        Label passwordSectionLabel = new Label("Change Password");
        passwordSectionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        passwordSectionLabel.setStyle("-fx-text-fill: #333333;");
        GridPane.setColumnSpan(passwordSectionLabel, 2);
        grid.add(passwordSectionLabel, 0, 6);
        
        // Current password field
        Label currentPasswordLabel = new Label("Current Password:");
        currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Enter current password");
        styleTextField(currentPasswordField);
        addFormRow(grid, 7, currentPasswordLabel, currentPasswordField);
        
        // New password field
        Label newPasswordLabel = new Label("New Password:");
        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        styleTextField(newPasswordField);
        addFormRow(grid, 8, newPasswordLabel, newPasswordField);
        
        // Confirm password field
        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        styleTextField(confirmPasswordField);
        addFormRow(grid, 9, confirmPasswordLabel, confirmPasswordField);
        
        // Password requirements
        Label passwordHintLabel = new Label("Password must be at least 8 characters with a number and special character");
        passwordHintLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        GridPane.setColumnSpan(passwordHintLabel, 2);
        grid.add(passwordHintLabel, 0, 10);
        
        return grid;
    }
    
    private void addFormRow(GridPane grid, int row, Label label, Control field) {
        label.setFont(Font.font("Arial", 14));
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }
    
    private void styleTextField(TextField field) {
        field.setPrefHeight(30);
        field.setMaxWidth(300);
        field.setStyle("-fx-border-color: #dddddd; -fx-border-radius: 3; -fx-background-radius: 3;");
        
        // Add focus effect
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("-fx-border-color: #4285f4; -fx-border-radius: 3; -fx-background-radius: 3;");
            } else {
                field.setStyle("-fx-border-color: #dddddd; -fx-border-radius: 3; -fx-background-radius: 3;");
            }
        });
    }
    
    private void styleButton(Button button, String textColor, String bgColor) {
        button.setPrefHeight(35);
        button.setPrefWidth(120);
        button.setFont(Font.font("Arial", 14));
        button.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-background-radius: 3;"
        );
    }
    
    private void resetForm() {
        // Reset fields to original values from database
        if (currentUserId > 0) {
            loadUserDataFromDatabase();
            nameField.setText(currentUsername);  
            emailField.setText(currentEmail);
        } else {
            // Default values if no user is loaded
            nameField.setText("John Doe");
            emailField.setText("john.doe@example.com");
        }
        
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        
        showAlert(Alert.AlertType.INFORMATION, "Form Reset", 
                "The form has been reset to saved values.", null);
    }
    
    private void saveProfileChanges() {
        // Check if user is logged in
        if (currentUserId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Authentication Error", 
                    "You must be logged in to update your profile.", null);
            return;
        }
        
        // Basic validation
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Name cannot be empty.", null);
            return;
        }
        
        if (!isValidEmail(emailField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid email address.", null);
            return;
        }
        
        try {
            // If user wants to change password
            if (!currentPasswordField.getText().isEmpty() || !newPasswordField.getText().isEmpty()) {
                // Password validation
                
                // Verify current password is correct (for a real app, you would hash the input and compare hashes)
                if (!verifyPassword(currentPasswordField.getText(), storedPasswordHash)) {
                    showAlert(Alert.AlertType.ERROR, "Password Error", 
                            "Current password is incorrect.", null);
                    return;
                }
                
                // Verify new password meets requirements
                if (!isStrongPassword(newPasswordField.getText())) {
                    showAlert(Alert.AlertType.ERROR, "Password Error", 
                            "New password must be at least 8 characters with a number and special character.", null);
                    return;
                }
                
                // Verify passwords match
                if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert(Alert.AlertType.ERROR, "Password Error", 
                            "New password and confirmation password don't match.", null);
                    return;
                }
                
                // Update user with new password
                updateUserWithPassword();
            } else {
                // Just update email and other info without changing password
                updateUserWithoutPassword();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to update profile", e.getMessage());
        }
    }
    
    private void updateUserWithoutPassword() throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String query = "UPDATE Users SET email = ? WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, emailField.getText());
                pstmt.setInt(2, currentUserId);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    currentEmail = emailField.getText();
                    debugInfoLabel.setText("Profile updated successfully");
                    showAlert(Alert.AlertType.INFORMATION, "Profile Updated", 
                            "Your profile has been updated successfully!", null);
                } else {
                    debugInfoLabel.setText("Update failed - no rows affected");
                    showAlert(Alert.AlertType.ERROR, "Update Failed", 
                            "No changes were made to your profile.", null);
                }
            }
        } finally {
            // If your DatabaseConnection manages connection closures, you might not need this
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void updateUserWithPassword() throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String query = "UPDATE Users SET email = ?, passwordHash = ? WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, emailField.getText());
                // In a real app, you would hash the password before storing it
                String hashedPassword = hashPassword(newPasswordField.getText());
                pstmt.setString(2, hashedPassword);
                pstmt.setInt(3, currentUserId);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    currentEmail = emailField.getText();
                    storedPasswordHash = hashedPassword;
                    
                    // Clear password fields after successful update
                    currentPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                    
                    debugInfoLabel.setText("Profile and password updated successfully");
                    showAlert(Alert.AlertType.INFORMATION, "Profile Updated", 
                            "Your profile and password have been updated successfully!", null);
                } else {
                    debugInfoLabel.setText("Update failed - no rows affected");
                    showAlert(Alert.AlertType.ERROR, "Update Failed", 
                            "No changes were made to your profile.", null);
                }
            }
        } finally {
            // If your DatabaseConnection manages connection closures, you might not need this
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // For a real application, use a proper hashing algorithm with salt
    private String hashPassword(String password) {
        // This is a simple placeholder. In a real app, use a library like BCrypt
        return "hashed_" + password;
    }
    
    // For a real application, properly validate the hashed password
    private boolean verifyPassword(String inputPassword, String storedHash) {
        // This is a simple placeholder. In a real app, use proper password verification
        return storedHash.equals("hashed_" + inputPassword) || 
               // For testing/demo purposes, also accept direct match with stored hash
               inputPassword.equals(storedHash);
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    
    private boolean isStrongPassword(String password) {
        // At least 8 chars, contains digit and special char
        return password.length() >= 8 && 
               password.matches(".*\\d.*") && 
               password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (content != null) {
            alert.setContentText(content);
        }
        alert.showAndWait();
    }
}