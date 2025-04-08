package controllers_Instructors;

import dao.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import controllers_students.Login;
import controllers_students.User;

import java.sql.*;

/**
 * Instructor Profile Page with database integration
 * Displays instructor information from the database with black text for visibility
 */
public class InstructorProfilePage {
    
    // User data from login
    private int userID;
    private int instructorID;
    private String username;
    private String email;
    private boolean isLoggedIn;
    
    // UI controls that need class-level access
    private VBox loginRequiredMessage;
    private VBox profileContent;
    private Label nameLabel;
    private TextField usernameField;
    private TextField emailField;
    
    /**
     * Default constructor - checks if user is logged in and loads data
     */
    public InstructorProfilePage() {
        loadUserDataFromLogin();
    }
    
    /**
     * Load user data from the Login class
     */
    private void loadUserDataFromLogin() {
        // Get the logged-in user from Login class
        User loggedInUser = Login.getLoggedInUser();
        
        if (loggedInUser != null && "Instructor".equals(loggedInUser.getRole())) {
            // User is logged in as instructor
            this.isLoggedIn = true;
            this.userID = loggedInUser.getUserID();
            this.username = loggedInUser.getUsername();
            this.email = loggedInUser.getEmail();
            
            // Load instructor ID silently (not shown in UI)
            loadInstructorID();
            
            System.out.println("Loaded user data for: " + username + " (ID: " + userID + ")");
        } else {
            // Not logged in or not an instructor
            this.isLoggedIn = false;
            System.out.println("No instructor is logged in");
        }
    }
    
    /**
     * Load instructor ID from the database
     */
    private void loadInstructorID() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT instructorID FROM Instructor WHERE userID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        this.instructorID = rs.getInt("instructorID");
                        System.out.println("Found instructor ID: " + instructorID);
                    } else {
                        System.err.println("No instructor record found for user ID: " + userID);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading instructor ID: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create and return the instructor profile view
     */
    public Node getView() {
        StackPane container = new StackPane();
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f7fa;");
        
        VBox view = new VBox(30);
        view.setMaxWidth(800);  // Constrain width for better layout
        view.setAlignment(Pos.TOP_CENTER);
        
        // Page title
        Label title = new Label("Instructor Profile");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        // Create login required message
        loginRequiredMessage = createLoginRequiredMessage();
        
        // Create profile content
        profileContent = createProfileContent();
        
        // Show appropriate content based on login status
        if (isLoggedIn) {
            profileContent.setVisible(true);
            loginRequiredMessage.setVisible(false);
        } else {
            profileContent.setVisible(false);
            loginRequiredMessage.setVisible(true);
        }
        
        // Add sections to main view
        view.getChildren().addAll(title, loginRequiredMessage, profileContent);
        container.getChildren().add(view);
        
        return container;
    }
    
    /**
     * Create the login required message
     */
    private VBox createLoginRequiredMessage() {
        VBox messageBox = new VBox(15);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(5, 20, 50, 20));
        messageBox.setMaxWidth(600);
        messageBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 5, 0, 0, 2);"
        );
        
        Label messageLabel = new Label("Please log in to view your profile");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        Label infoLabel = new Label("You need to be logged in as an instructor to access this page.");
        infoLabel.setStyle("-fx-text-fill: #555555;");
        
        Button loginButton = new Button("Go to Login");
        loginButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );
        loginButton.setOnAction(e -> redirectToLogin());
        
        messageBox.getChildren().addAll(messageLabel, infoLabel, loginButton);
        return messageBox;
    }
    
    /**
     * Redirect to login page
     */
    private void redirectToLogin() {
        try {
            // Close current window (if possible)
            Stage currentStage = (Stage) loginRequiredMessage.getScene().getWindow();
            currentStage.close();
            
            // Open login page
            Stage loginStage = new Stage();
            new Login().start(loginStage);
        } catch (Exception ex) {
            System.err.println("Error redirecting to login: " + ex.getMessage());
            ex.printStackTrace();
            
            // Show alert as fallback
            showAlert(AlertType.INFORMATION, "Login Required", 
                     "Please close this window and log in as an instructor.");
        }
    }
    
    /**
     * Create the profile content
     */
    private VBox createProfileContent() {
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(900);
        
        // Profile card
        VBox profileCard = createProfileCard();
        
        content.getChildren().add(profileCard);
        return content;
    }
    
    /**
     * Create the profile information card
     */
    private VBox createProfileCard() {
        VBox profileSection = new VBox(25);
        profileSection.setPadding(new Insets(40));
        profileSection.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 5, 0, 0, 2);"
        );
        
        // Profile header with name
        nameLabel = new Label(username);
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;");
        nameLabel.setAlignment(Pos.CENTER);
        
        // Create profile form
        GridPane formGrid = createProfileForm();
        
        // Action buttons
        HBox actionButtons = createActionButtons();
        
        // Add all elements to the profile section
        profileSection.getChildren().addAll(
            nameLabel,
            formGrid,
            actionButtons
        );
        
        return profileSection;
    }
    
    /**
     * Create the profile form grid
     */
    private GridPane createProfileForm() {
        GridPane formGrid = new GridPane();
        formGrid.setVgap(20);
        formGrid.setHgap(20);
        formGrid.setPadding(new Insets(20, 0, 20, 0));
        formGrid.setAlignment(Pos.CENTER);
        
        // Username
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
        usernameField = new TextField(username);
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(350);
        usernameField.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray;");
        
        // Email
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
        emailField = new TextField(email);
        emailField.setPromptText("Email");
        emailField.setPrefWidth(350);
        emailField.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray;");
        
        // Add fields to grid
        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);
        formGrid.add(emailLabel, 0, 1);
        formGrid.add(emailField, 1, 1);
        
        return formGrid;
    }
    
    /**
     * Create action buttons for the profile section
     */
    private HBox createActionButtons() {
        HBox actionButtons = new HBox(15);
        actionButtons.setPadding(new Insets(10, 0, 10, 0));
        actionButtons.setAlignment(Pos.CENTER);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: #ecf0f1;" +
            "-fx-text-fill: black;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 10 25;" +
            "-fx-font-weight: bold;"
        );
        cancelBtn.setOnAction(e -> {
            // Reset form to original values
            usernameField.setText(username);
            emailField.setText(email);
        });
        
        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle(
            "-fx-background-color: #2ecc71;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 10 25;" +
            "-fx-font-weight: bold;"
        );
        saveBtn.setOnAction(e -> saveProfileChanges());
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 10 25;" +
            "-fx-font-weight: bold;"
        );
        refreshBtn.setOnAction(e -> refreshProfile());
        
        actionButtons.getChildren().addAll(refreshBtn, cancelBtn, saveBtn);
        return actionButtons;
    }
    
    /**
     * Refresh the profile data from login
     */
    private void refreshProfile() {
        loadUserDataFromLogin();
        
        if (isLoggedIn) {
            // Update UI elements with fresh data
            nameLabel.setText(username);
            usernameField.setText(username);
            emailField.setText(email);
            
            // Show profile content
            profileContent.setVisible(true);
            loginRequiredMessage.setVisible(false);
            
            showAlert(AlertType.INFORMATION, "Refreshed", "Profile data has been refreshed.");
        } else {
            // Show login required message
            profileContent.setVisible(false);
            loginRequiredMessage.setVisible(true);
            
            showAlert(AlertType.WARNING, "Not Logged In", 
                     "You are not logged in as an instructor. Please log in first.");
        }
    }
    
    /**
     * Save profile changes to the database
     */
    private void saveProfileChanges() {
        // Make sure user is still logged in
        if (!isLoggedIn) {
            showAlert(AlertType.ERROR, "Not Logged In", 
                     "You must be logged in to save changes. Please log in again.");
            return;
        }
        
        // Get values from form fields
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();
        
        // Validate form inputs
        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "Username and email are required.");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Update Users table
            String userUpdateQuery = "UPDATE Users SET username = ?, email = ? WHERE userID = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userUpdateQuery)) {
                userStmt.setString(1, newUsername);
                userStmt.setString(2, newEmail);
                userStmt.setInt(3, userID);
                
                int rowsAffected = userStmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Update was successful
                    // Update local variables
                    username = newUsername;
                    email = newEmail;
                    
                    // Update the name label in the UI
                    nameLabel.setText(username);
                    
                    // Note: In a real app, you would also update the Login.loggedInUser object
                    // This may need a method in Login class to update the user object
                    
                    showAlert(AlertType.INFORMATION, "Success", "Profile updated successfully!");
                } else {
                    showAlert(AlertType.ERROR, "Error", "No changes were made. User ID may not exist.");
                }
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Database Error", "Failed to update profile: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Display an alert dialog
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Make sure dialog text is black too
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-text-fill: black;");
        dialogPane.getStyleClass().add("alert");
        
        alert.showAndWait();
    }
}