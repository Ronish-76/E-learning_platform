package controllers_students;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import dao.DatabaseConnection;

/**
 * A streamlined course management application that displays a list of enrolled courses
 * for the current student and shows detailed information when a course is selected.
 */
public class CoursesPage extends Application {
    // Constants for color scheme
    private static final String PRIMARY_COLOR = "#3498db";
    private static final String SECONDARY_COLOR = "#2ecc71";
    private static final String ACCENT_COLOR = "#e74c3c";
    private static final String BACKGROUND_COLOR = "#f9f9f9";
    private static final String CARD_COLOR = "#ffffff";
    private static final String TEXT_COLOR = "#2c3e50";
    private static final String SUBTEXT_COLOR = "#7f8c8d";
    
    // UI components
    private Label lessonTitleLabel;
    private TextArea lessonContentArea;
    private Button lessonActionButton;
    private Button previousButton;
    private Button nextButton;
    private StackPane mainContent;
    private BorderPane courseDetailView;
    
    // User information
    private User currentUser;
    private int currentUserId = 0;
    private int studentId = 0;
    
    // Course data from database
    private List<Course> enrolledCourses = new ArrayList<>();
    
    /**
     * Constructor for CoursesPage
     */
    public CoursesPage() {
        // Try to get logged-in user from Login class
        this.currentUser = Login.getLoggedInUser();
        if (currentUser != null) {
            this.currentUserId = currentUser.getUserID();
            try {
                this.studentId = getStudentId(currentUserId);
            } catch (SQLException e) {
                showError("Database Error", "Error retrieving student ID: " + e.getMessage());
            }
        }
        loadEnrolledCourses();
    }
    
    /**
     * Constructor with user ID parameter
     */
    public CoursesPage(int userId) {
        this.currentUserId = userId;
        // If Login.getLoggedInUser() is available, prefer that
        User loggedInUser = Login.getLoggedInUser();
        if (loggedInUser != null) {
            this.currentUser = loggedInUser;
            this.currentUserId = loggedInUser.getUserID();
        }
        
        try {
            this.studentId = getStudentId(currentUserId);
        } catch (SQLException e) {
            showError("Database Error", "Error retrieving student ID: " + e.getMessage());
        }
        
        loadEnrolledCourses();
    }
    
    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Start method required by JavaFX Application
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("My Courses");
        BorderPane mainView = createMainView();
        Scene scene = new Scene(mainView, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Returns the main view for the application
     */
    public Node getView() {
        return createMainView();
    }
    
    /**
     * Load enrolled courses from database for the current student
     */
    private void loadEnrolledCourses() {
        enrolledCourses.clear();
        
        if (studentId <= 0) {
            return; // No student ID, can't load courses
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT c.courseID, c.courseName, c.description, " +
                          "COALESCE(MAX(a.completionStatus = 'Completed'), 0) * 100.0 / " +
                          "(SELECT COUNT(*) FROM Activities WHERE courseID = c.courseID) as progress " +
                          "FROM Courses c " +
                          "JOIN Enrollments e ON c.courseID = e.courseID " +
                          "LEFT JOIN Activities a ON c.courseID = a.courseID AND a.studentID = e.studentID " +
                          "WHERE e.studentID = ? " +
                          "GROUP BY c.courseID";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int courseId = rs.getInt("courseID");
                        String courseName = rs.getString("courseName");
                        String description = rs.getString("description");
                        
                        // Get progress as a value between 0 and 1
                        double progress = rs.getDouble("progress") / 100.0;
                        if (rs.wasNull()) {
                            progress = 0.0; // Default progress if NULL
                        }
                        
                        // Get course content from the database
                        String content = getCourseContent(courseId);
                        
                        Course course = new Course(
                            courseId,
                            courseName, 
                            progress, 
                            content,
                            description
                        );
                        
                        enrolledCourses.add(course);
                    }
                }
            }
        } catch (SQLException e) {
            showError("Database Error", "Error loading enrolled courses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get course content from the database
     */
    private String getCourseContent(int courseId) {
        StringBuilder content = new StringBuilder();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT lessonTitle, lessonContent FROM Lessons WHERE courseID = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, courseId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String title = rs.getString("lessonTitle");
                        String lessonContent = rs.getString("lessonContent");
                        
                        content.append("## ").append(title).append("\n\n");
                        content.append(lessonContent).append("\n\n");
                    }
                }
            }
        } catch (SQLException e) {
            content.append("Error loading course content: ").append(e.getMessage());
        }
        
        // If no content was found, provide default content
        if (content.length() == 0) {
            content.append("This course doesn't have any lessons yet. Check back later for updates!");
        }
        
        return content.toString();
    }
    
    /**
     * Creates and returns the main view with courses list
     */
    private BorderPane createMainView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Header with title
        HBox header = createHeader();
        root.setTop(header);
        
        // Main content area with courses
        mainContent = new StackPane();
        mainContent.setPadding(new Insets(20));
        VBox coursesView = createCoursesView();
        courseDetailView = createCourseDetailView();
        mainContent.getChildren().add(coursesView);
        root.setCenter(mainContent);
        
        // Footer
        HBox footer = createFooter();
        root.setBottom(footer);
        
        return root;
    }
    
    /**
     * Creates the header section with title
     */
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(20, 30, 15, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + CARD_COLOR + ";");
        
        // App logo
        Rectangle logoPlaceholder = new Rectangle(40, 40);
        logoPlaceholder.setArcWidth(10);
        logoPlaceholder.setArcHeight(10);
        logoPlaceholder.setFill(Color.web(PRIMARY_COLOR));
        
        Label titleLabel = new Label("My Enrolled Courses");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));
        
        header.getChildren().addAll(logoPlaceholder, titleLabel);
        
        // Add drop shadow
        DropShadow dropShadow = createDropShadow(3);
        header.setEffect(dropShadow);
        
        return header;
    }
    
    /**
     * Creates the courses view with a list of enrolled courses
     */
    private VBox createCoursesView() {
        VBox coursesView = new VBox(20);
        coursesView.setPadding(new Insets(20));
        coursesView.setAlignment(Pos.TOP_CENTER);
        
        Label welcomeLabel = new Label("Welcome to Your Learning Journey");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Label subtitleLabel = new Label("Select a course to view its content");
        subtitleLabel.setFont(Font.font("Segoe UI", 16));
        subtitleLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        
        // Show message if no courses are enrolled
        if (enrolledCourses.isEmpty()) {
            VBox noCoursesBox = new VBox(15);
            noCoursesBox.setAlignment(Pos.CENTER);
            noCoursesBox.setPadding(new Insets(50));
            noCoursesBox.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 10;");
            
            Label noCoursesLabel = new Label("You haven't enrolled in any courses yet");
            noCoursesLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            noCoursesLabel.setTextFill(Color.web(TEXT_COLOR));
            
            Label enrollSuggestion = new Label("Visit the Available Courses page to browse and enroll in courses");
            enrollSuggestion.setFont(Font.font("Segoe UI", 14));
            enrollSuggestion.setTextFill(Color.web(SUBTEXT_COLOR));
            
            Button browseButton = new Button("Browse Available Courses");
            browseButton.setStyle(
                "-fx-background-color: " + PRIMARY_COLOR + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-font-weight: bold;"
            );
            
            // Add action to navigate to HomePage or trigger the appropriate method
            browseButton.setOnAction(e -> {
                // Navigate to the Home Page or equivalent
                // This would depend on your navigation setup
            });
            
            noCoursesBox.getChildren().addAll(noCoursesLabel, enrollSuggestion, browseButton);
            coursesView.getChildren().addAll(welcomeLabel, subtitleLabel, noCoursesBox);
            
            return coursesView;
        }
        
        // Courses grid
        GridPane courseGrid = new GridPane();
        courseGrid.setHgap(20);
        courseGrid.setVgap(20);
        courseGrid.setAlignment(Pos.CENTER);
        
        ObservableList<Course> courses = FXCollections.observableArrayList(enrolledCourses);
        
        int column = 0;
        int row = 0;
        for (Course course : courses) {
            VBox courseCard = createCourseCard(course);
            courseGrid.add(courseCard, column, row);
            
            column++;
            if (column > 2) {
                column = 0;
                row++;
            }
        }
        
        coursesView.getChildren().addAll(welcomeLabel, subtitleLabel, courseGrid);
        return coursesView;
    }
    
    /**
     * Creates a card for an individual course
     */
    private VBox createCourseCard(Course course) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(320);
        card.setPrefHeight(200);
        card.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 10;");
        card.setEffect(createDropShadow(5));
        
        Label titleLabel = new Label(course.getName());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));
        
        // Use the description from the course object
        Label descLabel = new Label(course.getDescription());
        descLabel.setFont(Font.font("Segoe UI", 14));
        descLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        descLabel.setWrapText(true);
        
        // Progress indicator
        HBox progressBox = new HBox(10);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        
        ProgressBar progressBar = new ProgressBar(course.getProgress());
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: " + PRIMARY_COLOR + ";");
        
        Label percentLabel = new Label(String.format("%.0f%%", course.getProgress() * 100));
        percentLabel.setTextFill(Color.web(PRIMARY_COLOR));
        percentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        progressBox.getChildren().addAll(progressBar, percentLabel);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Action button
        Button actionButton = new Button(getButtonTextForProgress(course.getProgress()));
        actionButton.setPrefWidth(Double.MAX_VALUE);
        actionButton.setStyle(getButtonStyleForProgress(course.getProgress()));
        
        // Set click event
        card.setOnMouseClicked(e -> showCourseDetail(course));
        
        card.getChildren().addAll(titleLabel, descLabel, spacer, progressBox, actionButton);
        return card;
    }
    
    /**
     * Creates the detailed view of a course
     */
    private BorderPane createCourseDetailView() {
        BorderPane detailView = new BorderPane();
        detailView.setPadding(new Insets(20));
        detailView.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 10;");
        
        // Header with back button and title
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Button backButton = new Button("← Back to courses");
        backButton.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: " + PRIMARY_COLOR + ";" +
            "-fx-font-weight: bold;"
        );
        backButton.setOnAction(e -> returnToCoursesView());
        
        lessonTitleLabel = new Label("Course Details");
        lessonTitleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lessonTitleLabel.setTextFill(Color.web(TEXT_COLOR));
        
        header.getChildren().addAll(backButton, lessonTitleLabel);
        
        // Course content
        VBox contentSection = new VBox(20);
        contentSection.setPadding(new Insets(20));
        contentSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        
        // Metadata bar
        HBox metadataBar = new HBox(20);
        metadataBar.setAlignment(Pos.CENTER_LEFT);
        metadataBar.setPadding(new Insets(10, 0, 10, 0));
        
        Label timeLabel = new Label("⏱️ 20 min");
        timeLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        timeLabel.setFont(Font.font("Segoe UI", 14));
        
        Label difficultyLabel = new Label("📊 Intermediate");
        difficultyLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        difficultyLabel.setFont(Font.font("Segoe UI", 14));
        
        Label authorLabel = new Label("👨‍🏫 Prof. Smith");
        authorLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        authorLabel.setFont(Font.font("Segoe UI", 14));
        
        metadataBar.getChildren().addAll(timeLabel, difficultyLabel, authorLabel);
        
        // Lesson content
        lessonContentArea = new TextArea();
        lessonContentArea.setWrapText(true);
        lessonContentArea.setEditable(false);
        lessonContentArea.setPrefHeight(400);
        lessonContentArea.setStyle(
            "-fx-control-inner-background: white; " +
            "-fx-background-color: white; " +
            "-fx-border-color: #ecf0f1; " +
            "-fx-border-radius: 5;"
        );
        lessonContentArea.setFont(Font.font("Segoe UI", 14));
        
        contentSection.getChildren().addAll(metadataBar, lessonContentArea);
        VBox.setVgrow(contentSection, Priority.ALWAYS);
        
        // Button row
        HBox buttonRow = new HBox(15);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setPadding(new Insets(20, 0, 0, 0));
        
        previousButton = new Button("Previous Lesson");
        previousButton.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: " + PRIMARY_COLOR + "; " +
            "-fx-border-color: " + PRIMARY_COLOR + "; " +
            "-fx-border-radius: 5;"
        );
        previousButton.setPrefWidth(150);
        
        nextButton = new Button("Next Lesson");
        nextButton.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: " + PRIMARY_COLOR + "; " +
            "-fx-border-color: " + PRIMARY_COLOR + "; " +
            "-fx-border-radius: 5;"
        );
        nextButton.setPrefWidth(150);
        
        lessonActionButton = new Button("Start Lesson");
        lessonActionButton.setPrefWidth(200);
        lessonActionButton.setStyle(
            "-fx-background-color: " + SECONDARY_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5; " +
            "-fx-font-weight: bold;"
        );
        
        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        buttonRow.getChildren().addAll(previousButton, spacer1, lessonActionButton, spacer2, nextButton);
        
        detailView.setTop(header);
        detailView.setCenter(contentSection);
        detailView.setBottom(buttonRow);
        
        return detailView;
    }
    
    /**
     * Creates the footer section
     */
    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: " + CARD_COLOR + ";");
        
        Label copyrightLabel = new Label("© 2023 Learning Platform | Need help? Contact support");
        copyrightLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        copyrightLabel.setFont(Font.font("Segoe UI", 12));
        
        footer.getChildren().add(copyrightLabel);
        return footer;
    }
    
    /**
     * Shows detailed information for a selected course
     */
    private void showCourseDetail(Course course) {
        // Update course detail content
        lessonTitleLabel.setText(course.getName());
        lessonContentArea.setText(course.getLessonContent());
        
        // Update action button based on progress
        lessonActionButton.setText(getButtonTextForProgress(course.getProgress()));
        lessonActionButton.setStyle(getButtonStyleForProgress(course.getProgress()));
        
        // Set action for the lesson button (e.g., mark as completed)
        lessonActionButton.setOnAction(e -> {
            try {
                markLessonAsCompleted(course.getId(), studentId);
                
                // Show success message
                Alert successAlert = new Alert(AlertType.INFORMATION);
                successAlert.setTitle("Progress Updated");
                successAlert.setHeaderText("Lesson Completed!");
                successAlert.setContentText("Your progress has been updated.");
                successAlert.show();
                
                // Reload courses to refresh progress
                loadEnrolledCourses();
                returnToCoursesView();
                
            } catch (SQLException ex) {
                showError("Error", "Could not update progress: " + ex.getMessage());
            }
        });
        
        // Transition to the detail view
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), mainContent);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            mainContent.getChildren().clear();
            mainContent.getChildren().add(courseDetailView);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainContent);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }
    
    /**
     * Mark a lesson as completed in the database
     */
    private void markLessonAsCompleted(int courseId, int studentId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get the next lesson that isn't completed
            String lessonQuery = "SELECT lessonID FROM Lessons WHERE courseID = ? " +
                                "AND lessonID NOT IN (" +
                                "  SELECT lessonID FROM LessonProgress " +
                                "  WHERE studentID = ? AND completionStatus = 'Completed'" +
                                ") ORDER BY lessonID LIMIT 1";
            
            try (PreparedStatement pstmt = conn.prepareStatement(lessonQuery)) {
                pstmt.setInt(1, courseId);
                pstmt.setInt(2, studentId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int lessonId = rs.getInt("lessonID");
                        
                        // Mark the lesson as completed
                        String updateQuery = "INSERT INTO LessonProgress (studentID, lessonID, completionStatus, completionDate) " +
                                          "VALUES (?, ?, 'Completed', CURRENT_TIMESTAMP) " +
                                          "ON DUPLICATE KEY UPDATE completionStatus = 'Completed', completionDate = CURRENT_TIMESTAMP";
                        
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, studentId);
                            updateStmt.setInt(2, lessonId);
                            updateStmt.executeUpdate();
                        }
                        
                        // Log activity
                        logActivity(studentId, courseId, "Lesson Completion", "Completed a lesson", "Completed");
                    } else {
                        throw new SQLException("No more lessons to complete for this course");
                    }
                }
            }
        }
    }
    
    /**
     * Log an activity in the database
     */
    private void logActivity(int studentId, int courseId, String activityType, String description, String status) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Activities (studentID, courseID, activityType, description, completionStatus) " +
                          "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                pstmt.setString(3, activityType);
                pstmt.setString(4, description);
                pstmt.setString(5, status);
                pstmt.executeUpdate();
            }
        }
    }
    
    /**
     * Returns to the courses list view
     */
    private void returnToCoursesView() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), mainContent);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            mainContent.getChildren().clear();
            mainContent.getChildren().add(createCoursesView());
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainContent);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }
    
    /**
     * Returns appropriate button text based on course progress
     */
    private String getButtonTextForProgress(double progress) {
        if (progress == 0.0) {
            return "Start Course";
        } else if (progress < 1.0) {
            return "Continue Course";
        } else {
            return "Review Course";
        }
    }
    
    /**
     * Returns appropriate button style based on course progress
     */
    private String getButtonStyleForProgress(double progress) {
        String baseStyle = "-fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;";
        if (progress == 0.0) {
            return "-fx-background-color: " + SECONDARY_COLOR + "; " + baseStyle;
        } else if (progress < 1.0) {
            return "-fx-background-color: " + PRIMARY_COLOR + "; " + baseStyle;
        } else {
            return "-fx-background-color: " + ACCENT_COLOR + "; " + baseStyle;
        }
    }
    
    /**
     * Get student ID from user ID
     */
    private int getStudentId(int userId) throws SQLException {
        if (userId <= 0) {
            return 0;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT studentID FROM Students WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("studentID");
                    } else {
                        return 0; // No student record found
                    }
                }
            }
        }
    }
    
    /**
     * Creates a drop shadow effect with specified radius
     */
    private DropShadow createDropShadow(double radius) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(radius);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        return dropShadow;
    }
    
    /**
     * Helper method to show error alerts
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Course data model class
     */
    public static class Course {
        private final int id;
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty progress;
        private final String lessonContent;
        private final String description;
        
        /**
         * Creates a new Course instance
         * 
         * @param id The course ID
         * @param name The name of the course
         * @param progress The completion progress (0.0 to 1.0)
         * @param lessonContent The content for the lesson
         * @param description The course description
         */
        public Course(int id, String name, double progress, String lessonContent, String description) {
            this.id = id;
            this.name = new SimpleStringProperty(name);
            this.progress = new SimpleDoubleProperty(progress);
            this.lessonContent = lessonContent;
            this.description = description;
        }
        
        public int getId() { return id; }
        public String getName() { return name.get(); }
        public double getProgress() { return progress.get(); }
        public String getLessonContent() { return lessonContent; }
        public String getDescription() { return description; }
    }
}