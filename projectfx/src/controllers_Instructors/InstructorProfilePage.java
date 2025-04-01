package controllers_Instructors;

import dao.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.*;

/**
 * Instructor Profile Page with database integration
 * Displays instructor information from the database with black text for visibility
 */
public class InstructorProfilePage {
    // User data from session
    private int userID = 0;
    private int instructorID = 0;
    private String username = "";
    private String email = "";
    private String department = "";
    private String officeHours = "";
    
    // UI controls that need class-level access
    private Label nameLabel;
    private TextField usernameField;
    private TextField emailField;
    private TextField departmentField;
    private TextField officeHoursField;
    
    /**
     * Default constructor
     */
    public InstructorProfilePage() {
        loadInstructorData();
    }
    
    /**
     * Loads instructor data from the database
     */
    private void loadInstructorData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to get first instructor
            String query = "SELECT u.userID, u.username, u.email, i.instructorID " +
                    "FROM Users u " +
                    "JOIN Instructor i ON u.userID = i.userID " +
                    "WHERE u.role = 'Instructor' " +
                    "LIMIT 1";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                this.userID = rs.getInt("userID");
                this.instructorID = rs.getInt("instructorID");
                this.username = rs.getString("username");
                this.email = rs.getString("email");
                
                // Using default values for department and office hours
                this.department = "Computer Science";
                this.officeHours = "Mon/Wed 2-4PM";
            } else {
                // No instructor found, using default values for UI display
                this.username = "instructor";
                this.email = "instructor@university.edu";
                this.department = "Computer Science";
                this.officeHours = "Mon/Wed 2-4PM";
                System.err.println("Warning: No instructor data found");
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading instructor data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create and return the instructor profile view
     */
    public Node getView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #f5f7fa;");
        
        // Page title
        Label title = new Label("Instructor Profile");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        // Profile card
        VBox profileCard = createProfileCard();
        
        // Add sections to main view
        view.getChildren().addAll(title, profileCard);
        
        return view;
    }
    
    /**
     * Create the profile information card
     */
    private VBox createProfileCard() {
        VBox profileSection = new VBox(15);
        profileSection.setPadding(new Insets(20));
        profileSection.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 5, 0, 0, 2);"
        );
        
        // Profile header with name
        nameLabel = new Label(username);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");
        
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
        formGrid.setVgap(15);
        formGrid.setHgap(15);
        formGrid.setPadding(new Insets(10, 0, 10, 0));
        
        // Username
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        usernameField = new TextField(username);
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(300);
        usernameField.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray;");
        
        // Email
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        emailField = new TextField(email);
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray;");
        
        // Department
        Label departmentLabel = new Label("Department:");
        departmentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        departmentField = new TextField(department);
        departmentField.setPromptText("Department");
        departmentField.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray;");
        
        // Office Hours
        Label officeHoursLabel = new Label("Office Hours:");
        officeHoursLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
        
    
        // Add fields to grid
        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);
        formGrid.add(emailLabel, 0, 1);
        formGrid.add(emailField, 1, 1);
        formGrid.add(departmentLabel, 0, 2);
        formGrid.add(departmentField, 1, 2);

        
        return formGrid;
    }
    
    /**
     * Create action buttons for the profile section
     */
    private HBox createActionButtons() {
        HBox actionButtons = new HBox(10);
        actionButtons.setPadding(new Insets(10, 0, 10, 0));
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: #ecf0f1;" +
            "-fx-text-fill: black;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8 15 8 15;"
        );
        cancelBtn.setOnAction(e -> {
            // Reset form to original values
            usernameField.setText(username);
            emailField.setText(email);
            departmentField.setText(department);
            officeHoursField.setText(officeHours);
        });
        
        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle(
            "-fx-background-color: #2ecc71;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 8 15 8 15;"
        );
        saveBtn.setOnAction(e -> saveProfileChanges());
        
        actionButtons.getChildren().addAll(cancelBtn, saveBtn);
        return actionButtons;
    }
    
    /**
     * Save profile changes to the database
     */
    private void saveProfileChanges() {
        // Get values from form fields
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newDepartment = departmentField.getText().trim();
        String newOfficeHours = officeHoursField.getText().trim();
        
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
                    department = newDepartment;
                    officeHours = newOfficeHours;
                    
                    // Update the name label in the UI
                    nameLabel.setText(username);
                    
                    showAlert(AlertType.INFORMATION, "Success", "Profile updated successfully!");
                } else {
                    showAlert(AlertType.ERROR, "Error", "No changes were made.");
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