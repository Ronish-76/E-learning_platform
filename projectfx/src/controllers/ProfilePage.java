package controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.regex.Pattern;

public class ProfilePage {
    private TextField nameField;
    private TextField emailField;
    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    
    // User data storage
    private String currentPassword = "password123"; // Simulated stored password
    
    public Node getView() {
        return createProfilePage();
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
        
        // Add all components to main container
        profilePage.getChildren().addAll(
            profileLabel,
            formGrid,
            actionButtons
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
        
        // Name field
        Label nameLabel = new Label("Full Name:");
        nameField = new TextField("John Doe");
        styleTextField(nameField);
        addFormRow(grid, 1, nameLabel, nameField);
        
        // Email field
        Label emailLabel = new Label("Email Address:");
        emailField = new TextField("john.doe@example.com");
        styleTextField(emailField);
        addFormRow(grid, 2, emailLabel, emailField);
        
        // Add separator
        Separator separator = new Separator();
        GridPane.setColumnSpan(separator, 2);
        GridPane.setMargin(separator, new Insets(10, 0, 10, 0));
        grid.add(separator, 0, 3);
        
        // Password Change Section
        Label passwordSectionLabel = new Label("Change Password");
        passwordSectionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        passwordSectionLabel.setStyle("-fx-text-fill: #333333;");
        GridPane.setColumnSpan(passwordSectionLabel, 2);
        grid.add(passwordSectionLabel, 0, 4);
        
        // Current password field
        Label currentPasswordLabel = new Label("Current Password:");
        currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Enter current password");
        styleTextField(currentPasswordField);
        addFormRow(grid, 5, currentPasswordLabel, currentPasswordField);
        
        // New password field
        Label newPasswordLabel = new Label("New Password:");
        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        styleTextField(newPasswordField);
        addFormRow(grid, 6, newPasswordLabel, newPasswordField);
        
        // Confirm password field
        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        styleTextField(confirmPasswordField);
        addFormRow(grid, 7, confirmPasswordLabel, confirmPasswordField);
        
        // Password requirements
        Label passwordHintLabel = new Label("Password must be at least 8 characters with a number and special character");
        passwordHintLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        GridPane.setColumnSpan(passwordHintLabel, 2);
        grid.add(passwordHintLabel, 0, 8);
        
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
        // Reset fields to original values
        nameField.setText("John Doe");
        emailField.setText("john.doe@example.com");
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        
        showAlert(Alert.AlertType.INFORMATION, "Form Reset", 
                  "The form has been reset to default values.", null);
    }
    
    private void saveProfileChanges() {
        // Basic validation
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Name cannot be empty.", null);
            return;
        }
        
        if (!isValidEmail(emailField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid email address.", null);
            return;
        }
        
        // Password validation
        if (!currentPasswordField.getText().isEmpty() || !newPasswordField.getText().isEmpty()) {
            // Verify current password is correct
            if (!currentPasswordField.getText().equals(currentPassword)) {
                showAlert(Alert.AlertType.ERROR, "Password Error", "Current password is incorrect.", null);
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
        }
        
        // If validation passes, save changes
        String newName = nameField.getText();
        String newEmail = emailField.getText();
        
        StringBuilder successMessage = new StringBuilder();
        successMessage.append("Name: ").append(newName).append("\n");
        successMessage.append("Email: ").append(newEmail).append("\n");
        
        if (!newPasswordField.getText().isEmpty()) {
            // Update password
            currentPassword = newPasswordField.getText();
            successMessage.append("Password: Updated");
        } else {
            successMessage.append("Password: Not changed");
        }
        
        showAlert(Alert.AlertType.INFORMATION, "Profile Updated", 
                 "Changes Saved Successfully!", successMessage.toString());
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