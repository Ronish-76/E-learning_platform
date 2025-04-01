package controllers_Admin;

import java.net.URL;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
    private String currentUsername = "Profile";

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
        
        // Create buttons with the correct icon names from your resources folder
        Button dashboardBtn = createSidebarButton("Home", "home_icon.png",
                () -> loadContent(new DashboardOverview().getView()));
        Button usersBtn = createSidebarButton("User Management", "user_icon.png",
                () -> loadContent(new UserManagementPage().getView()));
        Button coursesBtn = createSidebarButton("Course Management", "course_icon.png",
                () -> loadContent(new CourseManagementPage().getView()));
        Button reportsBtn = createSidebarButton("Reports & Analytics", "report_icon.png",
                () -> loadContent(new ReportsPage().getView()));
                
        // Set dashboard as active by default
        setActiveButton(dashboardBtn);
                
        Separator separator = new Separator();
        separator.getStyleClass().add("sidebar-separator");
        separator.setPadding(new Insets(15, 0, 15, 0));
        
        // Logout button - plain button without icon
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("sidebar-button");
        logoutBtn.getStyleClass().add("logout-button");
        logoutBtn.setPrefWidth(200);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setAlignment(Pos.BASELINE_LEFT);
        logoutBtn.setPadding(new Insets(12, 15, 12, 15));
        logoutBtn.setOnAction(_ -> showLogoutConfirmation(primaryStage));
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        sidebar.getChildren().addAll(
            adminTitle,
            navTitle,
            dashboardBtn,
            usersBtn,
            coursesBtn,
            reportsBtn,
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
        
        // Try to load icon directly from the resources folder shown in the file explorer
        try {
            // First try with the exact name from your resources folder
            String iconPath = "/images/" + iconFileName;
            URL iconUrl = getClass().getResource(iconPath);
            
            // If not found with direct path, try with resources prefix
            if (iconUrl == null) {
                iconPath = "/resources/images/" + iconFileName;
                iconUrl = getClass().getResource(iconPath);
            }
            
            if (iconUrl != null) {
                Image icon = new Image(iconUrl.toExternalForm(), 20, 20, true, true);
                ImageView iconView = new ImageView(icon);
                button.setGraphic(iconView);
            } else {
                System.out.println("Icon not found: " + iconFileName);
                // Use a default icon or no icon
            }
        } catch (Exception e) {
            System.out.println("Error loading icon " + iconFileName + ": " + e.getMessage());
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
        
        Label dashboardLabel = new Label("Admin Dashboard");
        dashboardLabel.getStyleClass().add("dashboard-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
     
        // Create a completely new profile button without any circle styling
        Label usernameLabel = new Label(currentUsername);
        usernameLabel.getStyleClass().add("username-label");
        
        // Create a standard button for profile access
        Button profileBtn = new Button();
        profileBtn.getStyleClass().add("text-button"); // Use a clean style without any circle
        profileBtn.setGraphic(usernameLabel);
        profileBtn.setBackground(Background.EMPTY); // Remove any background
        profileBtn.setBorder(Border.EMPTY); // Remove any border
        profileBtn.setPadding(new Insets(5, 10, 5, 10)); // Add some padding
        profileBtn.setCursor(Cursor.HAND); // Show hand cursor on hover
        profileBtn.setTooltip(new Tooltip("Admin Profile"));
        profileBtn.setOnAction(_ -> loadContent(new AdminProfilePage().getView()));
        
        topSection.getChildren().addAll(dashboardLabel, spacer, profileBtn);
        
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