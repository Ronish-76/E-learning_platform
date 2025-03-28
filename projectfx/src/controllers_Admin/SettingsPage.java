package controllers_Admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Settings page for configuring the system
 */
public class SettingsPage {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("System Settings");
        title.getStyleClass().add("page-title");
        
        // Create settings tabs
        TabPane settingsTabs = new TabPane();
        settingsTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab generalTab = new Tab("General");
        generalTab.setContent(createGeneralSettings());
        
        Tab usersTab = new Tab("User Settings");
        usersTab.setContent(createUserSettings());
        
        Tab appearanceTab = new Tab("Appearance");
        appearanceTab.setContent(createAppearanceSettings());
        
        Tab notificationsTab = new Tab("Notifications");
        notificationsTab.setContent(createNotificationSettings());
        
        Tab securityTab = new Tab("Security");
        securityTab.setContent(createSecuritySettings());
        
        settingsTabs.getTabs().addAll(generalTab, usersTab, appearanceTab, notificationsTab, securityTab);
        
        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setPadding(new Insets(20, 0, 0, 0));
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveBtn = new Button("Save Settings");
        saveBtn.getStyleClass().add("primary-button");
        
        Button resetBtn = new Button("Reset to Default");
        resetBtn.getStyleClass().add("secondary-button");
        
        actionButtons.getChildren().addAll(resetBtn, saveBtn);
        
        view.getChildren().addAll(title, settingsTabs, actionButtons);
        return view;
    }
    
    private VBox createGeneralSettings() {
        VBox settings = new VBox(15);
        settings.setPadding(new Insets(20));
        
        // System name setting
        VBox systemNameSetting = createSettingGroup(
            "System Name",
            "The name displayed in the browser title and emails",
            new TextField("Learning Management System")
        );
        
        // Support email setting
        VBox supportEmailSetting = createSettingGroup(
            "Support Email",
            "Email address for user support inquiries",
            new TextField("support@example.com")
        );
        
        // Timezone setting
        ComboBox<String> timezoneOptions = new ComboBox<>();
        timezoneOptions.getItems().addAll(
            "UTC", "America/New_York", "America/Los_Angeles", "Europe/London",
            "Europe/Paris", "Asia/Tokyo", "Australia/Sydney"
        );
        timezoneOptions.setValue("UTC");
        
        VBox timezoneSetting = createSettingGroup(
            "System Timezone",
            "Default timezone for date and time display",
            timezoneOptions
        );
        
        // Date format setting
        ToggleGroup dateFormatGroup = new ToggleGroup();
        RadioButton mdy = new RadioButton("MM/DD/YYYY");
        mdy.setToggleGroup(dateFormatGroup);
        mdy.setSelected(true);
        
        RadioButton dmy = new RadioButton("DD/MM/YYYY");
        dmy.setToggleGroup(dateFormatGroup);
        
        RadioButton ymd = new RadioButton("YYYY-MM-DD");
        ymd.setToggleGroup(dateFormatGroup);
        
        HBox dateOptions = new HBox(20);
        dateOptions.getChildren().addAll(mdy, dmy, ymd);
        
        VBox dateFormatSetting = createSettingGroup(
            "Date Format",
            "Default format for displaying dates",
            dateOptions
        );
        
        // Maintenance mode setting
        CheckBox maintenanceMode = new CheckBox("Enable maintenance mode");
        
        VBox maintenanceSetting = createSettingGroup(
            "Maintenance Mode",
            "When enabled, only administrators can access the system",
            maintenanceMode
        );
        
        settings.getChildren().addAll(
            systemNameSetting,
            supportEmailSetting,
            timezoneSetting,
            dateFormatSetting,
            maintenanceSetting
        );
        
        return settings;
    }
    
    private VBox createUserSettings() {
        VBox settings = new VBox(15);
        settings.setPadding(new Insets(20));
        
        // User registration setting
        CheckBox allowRegistration = new CheckBox("Allow user registration");
        allowRegistration.setSelected(true);
        
        VBox registrationSetting = createSettingGroup(
            "User Registration",
            "Allow new users to register accounts",
            allowRegistration
        );
        
        // Email verification setting
        CheckBox requireEmailVerification = new CheckBox("Require email verification");
        requireEmailVerification.setSelected(true);
        
        VBox emailVerificationSetting = createSettingGroup(
            "Email Verification",
            "Require users to verify their email before accessing the system",
            requireEmailVerification
        );
        
        // Default user role setting
        ComboBox<String> defaultRoleOptions = new ComboBox<>();
        defaultRoleOptions.getItems().addAll("Student", "Instructor");
        defaultRoleOptions.setValue("Student");
        
        VBox defaultRoleSetting = createSettingGroup(
            "Default User Role",
            "Role assigned to new users upon registration",
            defaultRoleOptions
        );
        
        // Session timeout setting
        ComboBox<String> sessionTimeoutOptions = new ComboBox<>();
        sessionTimeoutOptions.getItems().addAll(
            "30 minutes", "1 hour", "2 hours", "4 hours", "8 hours", "24 hours"
        );
        sessionTimeoutOptions.setValue("2 hours");
        
        VBox sessionTimeoutSetting = createSettingGroup(
            "Session Timeout",
            "How long until inactive users are automatically logged out",
            sessionTimeoutOptions
        );
        
        settings.getChildren().addAll(
            registrationSetting,
            emailVerificationSetting,
            defaultRoleSetting,
            sessionTimeoutSetting
        );
        
        return settings;
    }
    
    private VBox createAppearanceSettings() {
        VBox settings = new VBox(15);
        settings.setPadding(new Insets(20));
        
        // Theme setting
        ToggleGroup themeGroup = new ToggleGroup();
        RadioButton lightTheme = new RadioButton("Light");
        lightTheme.setToggleGroup(themeGroup);
        lightTheme.setSelected(true);
        
        RadioButton darkTheme = new RadioButton("Dark");
        darkTheme.setToggleGroup(themeGroup);
        
        RadioButton systemTheme = new RadioButton("Use System Setting");
        systemTheme.setToggleGroup(themeGroup);
        
        HBox themeOptions = new HBox(20);
        themeOptions.getChildren().addAll(lightTheme, darkTheme, systemTheme);
        
        VBox themeSetting = createSettingGroup(
            "System Theme",
            "Visual theme for the application interface",
            themeOptions
        );
        
        // Logo upload setting
        Button uploadLogoBtn = new Button("Upload Logo");
        uploadLogoBtn.getStyleClass().add("upload-button");
        
        VBox logoSetting = createSettingGroup(
            "System Logo",
            "Logo displayed in the header and emails (Recommended: 200x50px)",
            uploadLogoBtn
        );
        
        // Font size setting
        Slider fontSizeSlider = new Slider(80, 120, 100);
        fontSizeSlider.setShowTickMarks(true);
        fontSizeSlider.setShowTickLabels(true);
        fontSizeSlider.setMajorTickUnit(20);
        fontSizeSlider.setMinorTickCount(1);
        fontSizeSlider.setSnapToTicks(true);
        
        VBox fontSizeSetting = createSettingGroup(
            "Base Font Size",
            "Adjust the text size throughout the application (100% = default)",
            fontSizeSlider
        );
        
        // Color scheme setting
        ComboBox<String> colorSchemeOptions = new ComboBox<>();
        colorSchemeOptions.getItems().addAll(
            "Default Blue", "Professional Gray", "Vibrant Orange", "Green Earth", "Royal Purple", "Custom..."
        );
        colorSchemeOptions.setValue("Default Blue");
        
        VBox colorSchemeSetting = createSettingGroup(
            "Color Scheme",
            "Primary color scheme for buttons and accents",
            colorSchemeOptions
        );
        
        settings.getChildren().addAll(
            themeSetting,
            logoSetting,
            fontSizeSetting,
            colorSchemeSetting
        );
        
        return settings;
    }
    
    private VBox createNotificationSettings() {
        VBox settings = new VBox(15);
        settings.setPadding(new Insets(20));
        
        // Email notifications
        CheckBox emailNotifications = new CheckBox("Send email notifications");
        emailNotifications.setSelected(true);
        
        VBox emailNotifSetting = createSettingGroup(
            "Email Notifications",
            "Send system notifications via email",
            emailNotifications
        );
        
        // In-app notifications
        CheckBox inAppNotifications = new CheckBox("Show in-app notifications");
        inAppNotifications.setSelected(true);
        
        VBox inAppNotifSetting = createSettingGroup(
            "In-App Notifications",
            "Display notifications within the application",
            inAppNotifications
        );
        
        // Push notifications
        CheckBox pushNotifications = new CheckBox("Enable push notifications");
        pushNotifications.setSelected(false);
        
        VBox pushNotifSetting = createSettingGroup(
            "Push Notifications",
            "Send notifications to desktop or mobile devices",
            pushNotifications
        );
        
        // Admin notification events
        VBox adminNotifEvents = new VBox(10);
        adminNotifEvents.getChildren().addAll(
            new CheckBox("New user registration"),
            new CheckBox("Payment received"),
            new CheckBox("Support ticket created"),
            new CheckBox("Course published"),
            new CheckBox("System errors")
        );
        
        // Select all admin notifications
        for (Node node : adminNotifEvents.getChildren()) {
            ((CheckBox)node).setSelected(true);
        }
        
        VBox adminNotifSetting = createSettingGroup(
            "Admin Notification Events",
            "Events that trigger notifications for administrators",
            adminNotifEvents
        );
        
        settings.getChildren().addAll(
            emailNotifSetting,
            inAppNotifSetting,
            pushNotifSetting,
            adminNotifSetting
        );
        
        return settings;
    }
    
    private VBox createSecuritySettings() {
        VBox settings = new VBox(15);
        settings.setPadding(new Insets(20));
        
        // Password policy
        VBox passwordPolicy = new VBox(8);
        passwordPolicy.getChildren().addAll(
            new CheckBox("Require uppercase letters"),
            new CheckBox("Require numbers"),
            new CheckBox("Require special characters"),
            new CheckBox("Minimum 8 characters")
        );
        
        // Select all password policies
        for (Node node : passwordPolicy.getChildren()) {
            ((CheckBox)node).setSelected(true);
        }
        
        VBox passwordPolicySetting = createSettingGroup(
            "Password Policy",
            "Requirements for user passwords",
            passwordPolicy
        );
        
        // 2FA setting
        CheckBox require2FA = new CheckBox("Require 2FA for admins");
        require2FA.setSelected(true);
        
        VBox twoFactorSetting = createSettingGroup(
            "Two-Factor Authentication",
            "Additional security layer for account access",
            require2FA
        );
        
        // Login attempts
        ComboBox<String> loginAttemptOptions = new ComboBox<>();
        loginAttemptOptions.getItems().addAll(
            "3 attempts", "5 attempts", "10 attempts"
        );
        loginAttemptOptions.setValue("5 attempts");
        
        VBox loginAttemptsSetting = createSettingGroup(
            "Failed Login Attempts",
            "Number of failed attempts before account lockout",
            loginAttemptOptions
        );
        
        // API Key management
        Button manageApiKeys = new Button("Manage API Keys");
        manageApiKeys.getStyleClass().add("secondary-button");
        
        VBox apiKeySetting = createSettingGroup(
            "API Key Management",
            "Manage authentication keys for system API access",
            manageApiKeys
        );
        
        settings.getChildren().addAll(
            passwordPolicySetting,
            twoFactorSetting,
            loginAttemptsSetting,
            apiKeySetting
        );
        
        return settings;
    }
    
    private VBox createSettingGroup(String title, String description, Node control) {
        VBox group = new VBox(5);
        group.getStyleClass().add("setting-group");
        group.setPadding(new Insets(10, 0, 10, 0));
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("setting-title");
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("setting-description");
        
        group.getChildren().addAll(titleLabel, descLabel, control);
        return group;
    }
}