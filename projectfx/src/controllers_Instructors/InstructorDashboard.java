package controllers_Instructors;

import java.net.URL;

import controllers_Instructor.InstructorProfilePage;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.application.Platform;

/**
 * Instructor dashboard application with rich UI features and instructor functionality.
 */
public class InstructorDashboard extends Application {
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
        
        // Load the instructor dashboard page initially
        loadContent(new DashboardOverview().getView());
        
        // Scene setup with responsive design
        Scene scene = new Scene(root, 1280, 800);
        
        // Load CSS file
        URL cssUrl = getClass().getResource("/styles/instructor_styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("CSS file not found, using fallback styles");
            applyFallbackStyles();
        }
        
        primaryStage.setTitle("Instructor Dashboard");
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
        
        // Logo and instructor title
        Label instructorTitle = new Label("INSTRUCTOR PANEL");
        instructorTitle.getStyleClass().add("instructor-title");
        
        // Navigation section title
        Label navTitle = new Label("TEACHING");
        navTitle.getStyleClass().add("nav-section-title");
        navTitle.setPadding(new Insets(20, 0, 10, 0));
        
        // Create buttons with corresponding actions
        Button dashboardBtn = createSidebarButton("Home", "home_icon.png", 
                () -> loadContent(new DashboardOverview().getView()));
        Button coursesBtn = createSidebarButton("My Courses", "course_icon.png", 
                () -> loadContent(new MyCourses().getView()));
        Button assignmentsBtn = createSidebarButton("Assignments", "assignment_icon.png", 
                () -> loadContent(new AssignmentPage().getView()));
        Button quizzesBtn = createSidebarButton("Quizzes", "quiz_icon.png", 
                () -> loadContent(new QuizzesPage().getView()));
        Button studentsBtn = createSidebarButton("Students", "user_icon.png", 
                () -> loadContent(new StudentsPage().getView()));
                
        // Set dashboard as active by default
        setActiveButton(dashboardBtn);

        // Logout button with matching style
        Button logoutBtn = createLogoutButton(primaryStage);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        sidebar.getChildren().addAll(
            instructorTitle,
            navTitle,
            dashboardBtn, 
            coursesBtn, 
            assignmentsBtn, 
            quizzesBtn,
            studentsBtn,
            spacer,
            logoutBtn
        );
        
        return sidebar;
    }

    private Button createLogoutButton(Stage primaryStage) {
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("sidebar-button");  
        
        // Try to load logout icon
        try {
            URL iconUrl = getClass().getResource("/images/logout_icon.png");  
            if (iconUrl != null) {
                ImageView icon = new ImageView(iconUrl.toExternalForm());
                icon.setFitHeight(20);
                icon.setFitWidth(20);
                logoutBtn.setGraphic(icon);
            }
        } catch (Exception e) {
            System.out.println("Error loading logout icon");
        }
        
        // Additional styling specific to logout button
        logoutBtn.setStyle("-fx-text-fill: #e74c3c;");  // Red text for logout
        
        logoutBtn.setPrefWidth(200);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setAlignment(Pos.BASELINE_LEFT);
        logoutBtn.setPadding(new Insets(12, 15, 12, 15));
        
        logoutBtn.setOnAction(_ -> showLogoutConfirmation(primaryStage));
        
        return logoutBtn;
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
        
        try {
            // Load the image from resources directory
            URL iconUrl = getClass().getResource("/images/" + iconFileName);
            if (iconUrl != null) {
                ImageView icon = new ImageView(iconUrl.toExternalForm());
                icon.setFitHeight(20);
                icon.setFitWidth(20);
                button.setGraphic(icon);
            } else {
                System.out.println("Icon not found: " + iconFileName);
                // Create a placeholder if icon not found
                Rectangle iconPlaceholder = new Rectangle(20, 20);
                iconPlaceholder.setFill(Color.WHITE);
                iconPlaceholder.setOpacity(0.7);
                button.setGraphic(iconPlaceholder);
            }
        } catch (Exception e) {
            System.out.println("Error loading icon: " + iconFileName);
            e.printStackTrace();
            // Create a placeholder if exception occurs
            Rectangle iconPlaceholder = new Rectangle(20, 20);
            iconPlaceholder.setFill(Color.WHITE);
            iconPlaceholder.setOpacity(0.7);
            button.setGraphic(iconPlaceholder);
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

        Label dashboardLabel = new Label("Instructor Dashboard");
        dashboardLabel.getStyleClass().add("dashboard-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Profile Button with username
        Button profileBtn = new Button(currentUsername);
        profileBtn.getStyleClass().add("profile-button");
        profileBtn.setTooltip(new Tooltip("Instructor Profile"));
        
        // Add profile icon to profile button
        try {
            URL profileIconUrl = getClass().getResource("/images/profile_icon.png");
            if (profileIconUrl != null) {
                ImageView profileIcon = new ImageView(profileIconUrl.toExternalForm());
                profileIcon.setFitHeight(24);
                profileIcon.setFitWidth(24);
                profileBtn.setGraphic(profileIcon);
            }
        } catch (Exception e) {
            System.out.println("Error loading profile icon");
        }
        
        profileBtn.setOnAction(_ -> loadContent(new InstructorProfilePage().getView()));
        
        topSection.getChildren().addAll(dashboardLabel, spacer, profileBtn);
        
        // Add drop shadow to the top section
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0.0, 0.0, 0.0, 0.2));
        topSection.setEffect(dropShadow);
        
        return topSection;
    }

    private void loadContent(Node content) {
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
        leftSidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #34495e, #2c3e50); -fx-padding: 20px 15px;");
        mainContent.setStyle("-fx-background-color: #f5f5f7;");
    }

    public static void main(String[] args) {
        launch(args);
    }
}