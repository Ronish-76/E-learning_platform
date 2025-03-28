package controllers_Admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Admin Profile Page for viewing and editing admin information
 */
public class AdminProfilePage {
    private String currentUsername = "Admin User";
    
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Admin Profile");
        title.getStyleClass().add("page-title");
        
        // Profile info section
        HBox profileSection = new HBox(30);
        profileSection.setPadding(new Insets(20));
        profileSection.getStyleClass().add("profile-section");
        
        // Left side - Avatar and basic info
        VBox profileBasics = new VBox(15);
        profileBasics.setAlignment(Pos.TOP_CENTER);
        
        // Avatar placeholder (in a real app, this would be a proper image)
        Rectangle avatarPlaceholder = new Rectangle(120, 120);
        avatarPlaceholder.setArcWidth(20);
        avatarPlaceholder.setArcHeight(20);
        avatarPlaceholder.setFill(Color.LIGHTGRAY);
        
        Button changeAvatarBtn = new Button("Change Avatar");
        changeAvatarBtn.getStyleClass().add("secondary-button");
        
        Label nameLabel = new Label(currentUsername);
        nameLabel.getStyleClass().add("profile-name");
        
        Label roleLabel = new Label("Administrator");
        roleLabel.getStyleClass().add("profile-role");
        
        Label lastLoginLabel = new Label("Last login: Today, 14:25 PM");
        lastLoginLabel.getStyleClass().add("profile-last-login");
        
        profileBasics.getChildren().addAll(avatarPlaceholder, changeAvatarBtn, nameLabel, roleLabel, lastLoginLabel);
        
        // Right side - Editable profile details
        VBox profileDetails = new VBox(15);
        profileDetails.setAlignment(Pos.TOP_LEFT);
        profileDetails.setPrefWidth(400);
        
        Label detailsTitle = new Label("Profile Details");
        detailsTitle.getStyleClass().add("section-title");
        
        // Create form fields
        GridPane formGrid = new GridPane();
        formGrid.setVgap(10);
        formGrid.setHgap(15);
        
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField("Admin");
        
        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField("User");
        
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField("admin@example.com");
        
        Label phoneLabel = new Label("Phone:");
        TextField phoneField = new TextField("+1 555-123-4567");
        
        Label timeZoneLabel = new Label("Time Zone:");
        ComboBox<String> timeZoneField = new ComboBox<>();
        timeZoneField.getItems().addAll("UTC", "UTC-8 (PST)", "UTC-5 (EST)", "UTC+1 (CET)", "UTC+8 (CST)");
        timeZoneField.setValue("UTC");
        
        Label languageLabel = new Label("Language:");
        ComboBox<String> languageField = new ComboBox<>();
        languageField.getItems().addAll("English", "Spanish", "French", "German", "Chinese");
        languageField.setValue("English");
        
        // Position form fields
        formGrid.add(firstNameLabel, 0, 0);
        formGrid.add(firstNameField, 1, 0);
        formGrid.add(lastNameLabel, 0, 1);
        formGrid.add(lastNameField, 1, 1);
        formGrid.add(emailLabel, 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(phoneLabel, 0, 3);
        formGrid.add(phoneField, 1, 3);
        formGrid.add(timeZoneLabel, 0, 4);
        formGrid.add(timeZoneField, 1, 4);
        formGrid.add(languageLabel, 0, 5);
        formGrid.add(languageField, 1, 5);
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setPadding(new Insets(15, 0, 0, 0));
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveProfileBtn = new Button("Save Changes");
        saveProfileBtn.getStyleClass().add("primary-button");
        
        Button resetBtn = new Button("Reset");
        resetBtn.getStyleClass().add("secondary-button");
        
        actionButtons.getChildren().addAll(resetBtn, saveProfileBtn);
        
        profileDetails.getChildren().addAll(detailsTitle, formGrid, actionButtons);
        
        profileSection.getChildren().addAll(profileBasics, profileDetails);
        
        // Security section
        VBox securitySection = new VBox(15);
        securitySection.setPadding(new Insets(20));
        securitySection.getStyleClass().add("security-section");
        
        Label securityTitle = new Label("Security Settings");
        securityTitle.getStyleClass().add("section-title");
        
        // Password change
        VBox passwordChange = new VBox(10);
        passwordChange.setPadding(new Insets(0, 0, 20, 0));
        
        Label passwordTitle = new Label("Change Password");
        passwordTitle.getStyleClass().add("subsection-title");
        
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current Password");
        
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        
        Button changePasswordBtn = new Button("Update Password");
        changePasswordBtn.getStyleClass().add("primary-button");
        
        passwordChange.getChildren().addAll(
            passwordTitle,
            currentPasswordField,
            newPasswordField,
            confirmPasswordField,
            changePasswordBtn
        );
        
        // Two-factor authentication
        VBox twoFactorAuth = new VBox(10);
        
        Label twoFactorTitle = new Label("Two-Factor Authentication");
        twoFactorTitle.getStyleClass().add("subsection-title");
        
        CheckBox enableTwoFactor = new CheckBox("Enable two-factor authentication");
        enableTwoFactor.setSelected(true);
        
        Button configureTwoFactor = new Button("Configure 2FA");
        configureTwoFactor.getStyleClass().add("secondary-button");
        
        twoFactorAuth.getChildren().addAll(twoFactorTitle, enableTwoFactor, configureTwoFactor);
        
        securitySection.getChildren().addAll(securityTitle, passwordChange, twoFactorAuth);
        
        // Activity log section
        VBox activitySection = new VBox(15);
        activitySection.setPadding(new Insets(20));
        activitySection.getStyleClass().add("activity-section");
        
        HBox activityHeader = new HBox();
        activityHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label activityTitle = new Label("Recent Account Activity");
        activityTitle.getStyleClass().add("section-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button viewAllActivityBtn = new Button("View All");
        viewAllActivityBtn.getStyleClass().add("text-button");
        
        activityHeader.getChildren().addAll(activityTitle, spacer, viewAllActivityBtn);
        
        // Activity list
        VBox activityList = new VBox(8);
        activityList.getChildren().addAll(
            createActivityLogItem("Login successful", "Today, 14:25 PM", "192.168.1.105"),
            createActivityLogItem("Password changed", "June 18, 2023 10:30 AM", "192.168.1.105"),
            createActivityLogItem("Login successful", "June 18, 2023 10:15 AM", "192.168.1.105"),
            createActivityLogItem("Login successful", "June 17, 2023 09:45 AM", "10.0.15.25")
        );
        
        activitySection.getChildren().addAll(activityHeader, activityList);
        
        view.getChildren().addAll(title, profileSection, securitySection, activitySection);
        return view;
    }
    
    private HBox createActivityLogItem(String activity, String time, String ipAddress) {
        HBox item = new HBox();
        item.setPadding(new Insets(10));
        item.getStyleClass().add("activity-log-item");
        
        VBox details = new VBox(5);
        Label activityLabel = new Label(activity);
        activityLabel.getStyleClass().add("activity-log-action");
        
        HBox metaInfo = new HBox(15);
        Label timeLabel = new Label("Time: " + time);
        timeLabel.getStyleClass().add("activity-log-meta");
        
        Label ipLabel = new Label("IP: " + ipAddress);
        ipLabel.getStyleClass().add("activity-log-meta");
        
        metaInfo.getChildren().addAll(timeLabel, ipLabel);
        details.getChildren().addAll(activityLabel, metaInfo);
        
        item.getChildren().add(details);
        return item;
    }
}