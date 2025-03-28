package controllers_students;

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

public class Dashboard_S extends Application {
    private BorderPane root;
    private ScrollPane scrollContent;
    private VBox mainContent;
    private VBox leftSidebar;
    private Button activeButton = null;

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

        // Load the home page initially
        loadContent(new HomePage().getView());

        // Scene setup with responsive design
        Scene scene = new Scene(root, 1280, 800);

        // Load CSS file
        URL cssUrl = getClass().getResource("/styles/style.css");

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("CSS file not found, using fallback styles");
            // Apply fallback styles directly if CSS file is missing
            applyFallbackStyles();
        }

        primaryStage.setTitle("E-Learning Dashboard");
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
        
        // Navigation section title
        Label navTitle = new Label("NAVIGATION");
        navTitle.getStyleClass().add("nav-section-title");
        
        // Create buttons with corresponding actions
        Button homeBtn = createSidebarButton("Home", "home_icon.png", () -> loadContent(new HomePage().getView()));
        Button coursesBtn = createSidebarButton("My Courses", "course_icon.png", () -> loadContent(new CoursesPage().getView()));
        Button assignmentsBtn = createSidebarButton("Assignments", "assignment_icon.png", () -> loadContent(new AssignmentsPage().getView()));
        Button quizBtn = createSidebarButton("Quizzes", "quiz_icon.png", () -> loadContent(new Quiz().getView()));
        
        // Set home as active by default
        setActiveButton(homeBtn);
        
        // Help section
        Label helpTitle = new Label("HELP");
        helpTitle.getStyleClass().add("nav-section-title");
        helpTitle.setPadding(new Insets(20, 0, 10, 0));
        
        Button supportBtn = createSidebarButton("Support", "support_icon.png", () -> showSupportDialog());
        Button faqBtn = createSidebarButton("FAQ", "faq_icon.png", () -> loadContent(new FAQPage().getView()));
        
        Separator separator = new Separator();
        separator.getStyleClass().add("sidebar-separator");
        separator.setPadding(new Insets(15, 0, 15, 0));
        
        // Logout button
        Button logoutBtn = createSidebarButton("Logout", "logout_icon.png", () -> showLogoutConfirmation(primaryStage));
        logoutBtn.getStyleClass().add("logout-button");
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        sidebar.getChildren().addAll(
            navTitle,
            homeBtn, 
            coursesBtn, 
            assignmentsBtn, 
            quizBtn,
            helpTitle,
            supportBtn,
            faqBtn,
            spacer,
            separator,
            logoutBtn
        );
        
        return sidebar;
    }

    private void showLogoutConfirmation(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Click OK to exit the application or Cancel to stay.");
        
        // Add styling to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("logout-dialog");
        
        // Show the dialog and wait for response
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Close the application
                Platform.exit();
            }
        });
    }

    private Button createSidebarButton(String text, String iconFileName, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("sidebar-button");

        try {
            // Create the icon
            ImageView icon = new ImageView(new Image("file:C:/Users/HP/eclipse-workspace/projectfx/resources/" + iconFileName));
            icon.setFitHeight(20);
            icon.setFitWidth(20);
            icon.setPreserveRatio(true);
            
            // Set the button style and icon
            button.setGraphic(icon);
        } catch (Exception e) {
            System.out.println("Icon not found: " + iconFileName);
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
        Button menuBtn = new Button();
        ImageView menuIcon = new ImageView(new Image("file:C:/Users/HP/eclipse-workspace/projectfx/resources/menu_icon.png"));
        menuIcon.setFitHeight(20);
        menuIcon.setFitWidth(20);
        menuBtn.setGraphic(menuIcon);
        menuBtn.getStyleClass().add("hamburger-menu");
        
        Label dashboardLabel = new Label("Learning Dashboard");
        dashboardLabel.getStyleClass().add("dashboard-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Notifications button
        Button notifBtn = new Button();
        ImageView notifIcon = new ImageView(new Image("file:C:/Users/HP/eclipse-workspace/projectfx/resources/notification_icon.png"));
        notifIcon.setFitHeight(20);
        notifIcon.setFitWidth(20);
        notifBtn.setGraphic(notifIcon);
        notifBtn.getStyleClass().add("header-icon-button");
        notifBtn.setTooltip(new Tooltip("Notifications"));
        
        // Profile Button with avatar
        Button profileBtn = new Button();
        ImageView profileIcon = new ImageView(new Image("file:C:/Users/HP/eclipse-workspace/projectfx/resources/profile_icon.png"));
        profileIcon.setFitHeight(30);
        profileIcon.setFitWidth(30);
        profileBtn.setGraphic(profileIcon);
        profileBtn.getStyleClass().add("profile-button");
        profileBtn.setTooltip(new Tooltip("Your Profile"));
        profileBtn.setOnAction(_ -> loadContent(new ProfilePage().getView()));

        topSection.getChildren().addAll(menuBtn, dashboardLabel, spacer, notifBtn, profileBtn);
        
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
    
    private void showSupportDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Support Center");
        dialog.setHeaderText("How can we help you?");
        
        // Create the support form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField name = new TextField();
        name.setPromptText("Your name");
        TextArea issue = new TextArea();
        issue.setPromptText("Describe your issue");
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Issue:"), 0, 1);
        grid.add(issue, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return name.getText();
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    private void applyFallbackStyles() {
        // Apply styles directly in case CSS file is missing
        root.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-font-size: 14px;");
        leftSidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e); -fx-padding: 20px 15px;");
        // Add more fallback styles as needed
    }
    
    private class FAQPage {
        public Node getView() {
            VBox view = new VBox(20);
            view.getStyleClass().add("page-content");
            
            Label title = new Label("Frequently Asked Questions");
            title.getStyleClass().add("page-title");
            
            VBox faqList = new VBox(15);
            
            // Add FAQ items
            faqList.getChildren().addAll(
                createFaqItem("How do I enroll in a course?", 
                    "To enroll in a course, browse the courses page, select your desired course, and click the 'Enroll' button."),
                createFaqItem("How do I submit assignments?", 
                    "Navigate to the Assignments page, select the assignment you want to submit, and use the submission form to upload your work."),
                createFaqItem("How are quizzes graded?", 
                    "Quizzes are automatically graded upon submission. You'll receive immediate feedback on your performance."),
                createFaqItem("Can I download course materials for offline use?", 
                    "Yes, most course materials can be downloaded for offline use. Look for the download icon next to each resource.")
            );
            
            view.getChildren().addAll(title, faqList);
            return view;
        }
        
        private VBox createFaqItem(String question, String answer) {
            VBox item = new VBox(10);
            item.getStyleClass().add("faq-item");
            item.setPadding(new Insets(15));
            
            Label questionLabel = new Label(question);
            questionLabel.getStyleClass().add("faq-question");
            
            Label answerLabel = new Label(answer);
            answerLabel.getStyleClass().add("faq-answer");
            answerLabel.setWrapText(true);
            
            item.getChildren().addAll(questionLabel, answerLabel);
            return item;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}