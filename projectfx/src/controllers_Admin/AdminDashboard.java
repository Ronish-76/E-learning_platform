package controllers_Admin;

import java.net.URL;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.application.Platform;

/**
 * Admin dashboard application with rich UI features and admin functionality.
 */
public class AdminDashboard extends Application {
    private BorderPane root;
    private ScrollPane scrollContent;
    private VBox mainContent;
    private VBox leftSidebar;
    private Button activeButton = null;
    private String currentUsername = "Admin User";

    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        root.getStyleClass().add("main-root");
        
        // Left Sidebar with animation
        leftSidebar = createLeftSidebar(primaryStage);
        leftSidebar.setPrefWidth(240);
        root.setLeft(leftSidebar);
        
        // Top Section (Header)
        HBox topSection = createTopSection();
        root.setTop(topSection);
        
        // Main Content Area with scroll capability
        mainContent = new VBox();
        mainContent.setPadding(new Insets(25));
        mainContent.setSpacing(20);
        mainContent.getStyleClass().add("main-content");
        
        scrollContent = new ScrollPane(mainContent);
        scrollContent.setFitToWidth(true);
        scrollContent.setFitToHeight(true);
        scrollContent.getStyleClass().add("content-scroll");
        scrollContent.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollContent.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.setCenter(scrollContent);
        
        // Load the admin dashboard page initially
        loadContent(new DashboardOverview().getView());
        
        // Scene setup with responsive design
        Scene scene = new Scene(root, 1280, 800);
        
        // Load CSS file
        URL cssUrl = getClass().getResource("/styles/admin_styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("CSS file not found, using fallback styles");
            applyFallbackStyles();
        }
        
        primaryStage.setTitle("Admin Dashboard");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Apply entrance animation
        applyEntranceAnimation();
    }

    private void applyEntranceAnimation() {
        // Animated entrance for sidebar
        TranslateTransition sidebarTransition = new TranslateTransition(Duration.millis(800), leftSidebar);
        sidebarTransition.setFromX(-leftSidebar.getPrefWidth());
        sidebarTransition.setToX(0);
        sidebarTransition.play();
        
        // Animated entrance for main content
        TranslateTransition contentTransition = new TranslateTransition(Duration.millis(600), mainContent);
        contentTransition.setFromY(50);
        contentTransition.setToY(0);
        contentTransition.setDelay(Duration.millis(400));
        contentTransition.play();
        
        // Fade in effect for main content
        mainContent.setOpacity(0);
        mainContent.setStyle("-fx-opacity: 0;");
        mainContent.setStyle("-fx-opacity: 1; -fx-transition: opacity 0.8s ease;");
    }

    private VBox createLeftSidebar(Stage primaryStage) {
        VBox sidebar = new VBox(12); // Increased spacing
        sidebar.setId("sidebar");
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        
        // Logo and admin title
        Label adminTitle = new Label("ADMIN PANEL");
        adminTitle.getStyleClass().add("admin-title");
        
        // Navigation section title
        Label navTitle = new Label("MANAGEMENT");
        navTitle.getStyleClass().add("nav-section-title");
        navTitle.setPadding(new Insets(20, 0, 10, 0));
        
        // Create buttons with corresponding actions
        Button dashboardBtn = createSidebarButton("Dashboard", "dashboard_icon.png",
                () -> loadContent(new DashboardOverview().getView()));
        Button usersBtn = createSidebarButton("User Management", "users_icon.png",
                () -> loadContent(new UserManagementPage().getView()));
        Button coursesBtn = createSidebarButton("Course Management", "courses_admin_icon.png",
                () -> loadContent(new CourseManagementPage().getView()));
        Button reportsBtn = createSidebarButton("Reports & Analytics", "reports_icon.png",
                () -> loadContent(new ReportsPage().getView()));
                
        // Set dashboard as active by default
        setActiveButton(dashboardBtn);
        
        // System section
        Label systemTitle = new Label("SYSTEM");
        systemTitle.getStyleClass().add("nav-section-title");
        systemTitle.setPadding(new Insets(20, 0, 10, 0));
        
        Button settingsBtn = createSidebarButton("Settings", "settings_icon.png",
                () -> loadContent(new SettingsPage().getView()));
        Button logsBtn = createSidebarButton("System Logs", "logs_icon.png",
                () -> loadContent(new SystemLogsPage().getView()));
                
        Separator separator = new Separator();
        separator.getStyleClass().add("sidebar-separator");
        separator.setPadding(new Insets(15, 0, 15, 0));
        
        // Logout button
        Button logoutBtn = createSidebarButton("Logout", "logout_icon.png",
                () -> showLogoutConfirmation(primaryStage));
        logoutBtn.getStyleClass().add("logout-button");
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        sidebar.getChildren().addAll(
            adminTitle,
            navTitle,
            dashboardBtn,
            usersBtn,
            coursesBtn,
            reportsBtn,
            systemTitle,
            settingsBtn,
            logsBtn,
            spacer,
            separator,
            logoutBtn
        );
        
        return sidebar;
    }

    private void showLogoutConfirmation(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be redirected to the login page.");
        
        // Add styling to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("logout-dialog");
        
        // Show the dialog and wait for response
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Close the application (in a real app, you'd redirect to login)
                Platform.exit();
            }
        });
    }

    private Button createSidebarButton(String text, String iconFileName, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("sidebar-button");
        
        // In a real app, you'd have proper icon handling
        // For this example, we'll create a placeholder icon
        try {
            // Create the icon - using a placeholder instead of file path
            Rectangle iconPlaceholder = new Rectangle(20, 20);
            iconPlaceholder.setFill(Color.WHITE);
            iconPlaceholder.setOpacity(0.7);
            
            // Set the button style and icon
            button.setGraphic(iconPlaceholder);
        } catch (Exception e) {
            System.out.println("Using placeholder for icon: " + iconFileName);
        }
        
        button.setPrefWidth(200);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.BASELINE_LEFT);
        button.setPadding(new Insets(12, 15, 12, 15));
        
        // Button click event
        button.setOnAction(_ -> {
            setActiveButton(button);
            action.run();
        });
        
        return button;
    }

    private void setActiveButton(Button button) {
        // Remove active class from previous button
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active-button");
        }
        
        // Add active class to new button
        button.getStyleClass().add("active-button");
        activeButton = button;
    }

    private HBox createTopSection() {
        HBox topSection = new HBox();
        topSection.setPadding(new Insets(15, 25, 15, 25));
        topSection.getStyleClass().add("top-header");
        topSection.setAlignment(Pos.CENTER_LEFT);
        
        // Create hamburger menu button for mobile view
        Button menuBtn = new Button("≡");
        menuBtn.getStyleClass().add("hamburger-menu");
        
        Label dashboardLabel = new Label("Admin Dashboard");
        dashboardLabel.getStyleClass().add("dashboard-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Notifications button
        Button notifBtn = new Button("🔔");
        notifBtn.getStyleClass().add("header-icon-button");
        notifBtn.setTooltip(new Tooltip("Notifications"));
        
        // Alert count indicator
        Label alertCount = new Label("3");
        alertCount.getStyleClass().add("alert-count");
        
        StackPane notifContainer = new StackPane();
        notifContainer.getChildren().addAll(notifBtn, alertCount);
        StackPane.setAlignment(alertCount, Pos.TOP_RIGHT);
        
        // Profile Button with username
        Button profileBtn = new Button(currentUsername);
        profileBtn.getStyleClass().add("profile-button");
        profileBtn.setTooltip(new Tooltip("Admin Profile"));
        profileBtn.setOnAction(_ -> loadContent(new AdminProfilePage().getView()));
        
        topSection.getChildren().addAll(menuBtn, dashboardLabel, spacer, notifContainer, profileBtn);
        
        // Add drop shadow to the top section
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0.0, 0.0, 0.0, 0.2));
        topSection.setEffect(dropShadow);
        
        return topSection;
    }

    public void loadContent(Node content) {
        // Add fade transition effect
        content.setOpacity(0);
        mainContent.getChildren().setAll(content);
        
        // Fade in animation
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), content);
        tt.setFromY(20);
        tt.setToY(0);
        tt.play();
        
        content.setOpacity(1);
    }

    private void applyFallbackStyles() {
        // Apply styles directly in case CSS file is missing
        root.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-font-size: 14px;");
        leftSidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #252941, #1a1f36); -fx-padding: 20px 15px;");
        mainContent.setStyle("-fx-background-color: #f5f5f7;");
    }

    public static void main(String[] args) {
        launch(args);
    }
}