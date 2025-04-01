package controllers_Admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.Alert.AlertType;

/**
 * Simplified Admin Profile Page
 */
public class AdminProfilePage {
    // Basic user data
    private String username = "admin";
    private String email = "Admin@gmail.com";
    
    public Node getView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        
        Label title = new Label("Admin Profile");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Profile info section
        VBox profileSection = new VBox(15);
        profileSection.setPadding(new Insets(20));
        profileSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5px;");
        
        Label nameLabel = new Label(username);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Profile form
        GridPane formGrid = new GridPane();
        formGrid.setVgap(10);
        formGrid.setHgap(15);
        
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField(username);
        
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(email);
        
        // Position form fields
        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);
        formGrid.add(emailLabel, 0, 1);
        formGrid.add(emailField, 1, 1);
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setPadding(new Insets(15, 0, 0, 0));
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveProfileBtn = new Button("Save Changes");
        saveProfileBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        saveProfileBtn.setOnAction(e -> {
            username = usernameField.getText();
            email = emailField.getText();
            nameLabel.setText(username);
            showAlert(AlertType.INFORMATION, "Success", "Profile saved successfully!");
        });
        
        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(e -> {
            usernameField.setText(username);
            emailField.setText(email);
        });
        
        actionButtons.getChildren().addAll(resetBtn, saveProfileBtn);
        
        // Password change section
        VBox passwordChange = new VBox(10);
        passwordChange.setPadding(new Insets(20, 0, 0, 0));
        
        Label passwordTitle = new Label("Change Password");
        passwordTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current Password");
        
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        
        Button changePasswordBtn = new Button("Update Password");
        changePasswordBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        changePasswordBtn.setOnAction(e -> {
            if (currentPasswordField.getText().isEmpty() || 
                newPasswordField.getText().isEmpty() || 
                confirmPasswordField.getText().isEmpty()) {
                showAlert(AlertType.ERROR, "Error", "All password fields are required.");
                return;
            }
            
            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showAlert(AlertType.ERROR, "Error", "New passwords do not match.");
                return;
            }
            
            showAlert(AlertType.INFORMATION, "Success", "Password updated successfully!");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        });
        
        profileSection.getChildren().addAll(
            nameLabel, 
            formGrid, 
            actionButtons, 
            passwordTitle, 
            currentPasswordField, 
            newPasswordField, 
            confirmPasswordField, 
            changePasswordBtn
        );
        
        // Add all sections to main view
        view.getChildren().addAll(title, profileSection);
        return view;
    }
    
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}