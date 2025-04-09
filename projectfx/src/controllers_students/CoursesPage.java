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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import dao.DatabaseConnection;

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
    private int currentCourseId = 0;
    private int currentLessonId = 0;

    public CoursesPage() {
        this.currentUser = Login.getLoggedInUser();
        if (currentUser != null) {
            this.currentUserId = currentUser.getUserID();
            try {
                this.studentId = getStudentId(currentUserId);
                System.out.println("Student ID: " + studentId); // Debug output
            } catch (SQLException e) {
                showError("Database Error", "Error retrieving student ID: " + e.getMessage());
                e.printStackTrace();
            }
        }
        loadEnrolledCourses();
    }

    public CoursesPage(int userId) {
        this.currentUserId = userId;
        User loggedInUser = Login.getLoggedInUser();
        if (loggedInUser != null) {
            this.currentUser = loggedInUser;
            this.currentUserId = loggedInUser.getUserID();
        }
        try {
            this.studentId = getStudentId(currentUserId);
            System.out.println("Student ID: " + studentId); // Debug output
        } catch (SQLException e) {
            showError("Database Error", "Error retrieving student ID: " + e.getMessage());
            e.printStackTrace();
        }
        loadEnrolledCourses();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("My Courses");
        BorderPane mainView = createMainView();
        Scene scene = new Scene(mainView, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Node getView() {
        return createMainView();
    }

    private void loadEnrolledCourses() {
        enrolledCourses.clear();
        if (studentId <= 0) {
            System.out.println("No student ID available, cannot load courses");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Database connection successful for loading enrolled courses");
            // Updated query to correctly calculate progress
            String query = "SELECT c.courseID, c.courseName, c.description, " +
                "IFNULL(ROUND((SELECT COUNT(*) FROM LessonProgress lp " +
                "JOIN Lessons l ON lp.lessonID = l.lessonID " +
                "WHERE l.courseID = c.courseID AND lp.studentID = ? " +
                "AND lp.completionStatus = 'Completed') * 100.0 / " +
                "(SELECT COUNT(*) FROM Lessons WHERE courseID = c.courseID), 0), 0) as progressPercentage " +
                "FROM Courses c " +
                "JOIN Enrollments e ON c.courseID = e.courseID " +
                "WHERE e.studentID = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, studentId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int courseId = rs.getInt("courseID");
                        String courseName = rs.getString("courseName");
                        String description = rs.getString("description");
                        double progress = rs.getDouble("progressPercentage") / 100.0;
                        String content = getCourseContent(courseId);
                        
                        Course course = new Course(
                            courseId,
                            courseName,
                            progress,
                            content,
                            description != null ? description : "No description available"
                        );
                        
                        enrolledCourses.add(course);
                        System.out.println("Loaded course: " + courseName + " with progress: " + progress);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
            showError("Database Error", "Error loading enrolled courses: " + e.getMessage());
        }
    }

    private String getCourseContent(int courseId) {
        StringBuilder content = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First, check if the content column exists
            boolean contentColumnExists = checkIfContentColumnExists(conn);
            
            // Get the total number of lessons for this course
            int totalLessons = 0;
            String countQuery = "SELECT COUNT(*) as total FROM Lessons WHERE courseID = ?";
            try (PreparedStatement countStmt = conn.prepareStatement(countQuery)) {
                countStmt.setInt(1, courseId);
                try (ResultSet countRs = countStmt.executeQuery()) {
                    if (countRs.next()) {
                        totalLessons = countRs.getInt("total");
                    }
                }
            }
            
            if (totalLessons == 0) {
                return "This course doesn't have any lessons yet. Check back later for updates!";
            }
            
            // Add a course overview heading
            content.append("Course Overview\n\n");
            content.append("This course contains " + totalLessons + " lessons:\n\n");
            
            String query;
            if (contentColumnExists) {
                query = "SELECT l.lessonID, l.title, l.category, " +
                       "l.content, lp.completionStatus, lp.completionDate " +
                       "FROM Lessons l " +
                       "LEFT JOIN LessonProgress lp ON l.lessonID = lp.lessonID AND lp.studentID = ? " +
                       "WHERE l.courseID = ? " +
                       "ORDER BY l.lessonID";
            } else {
                // If content column doesn't exist, don't include it in the query
                query = "SELECT l.lessonID, l.title, l.category, " +
                       "lp.completionStatus, lp.completionDate " +
                       "FROM Lessons l " +
                       "LEFT JOIN LessonProgress lp ON l.lessonID = lp.lessonID AND lp.studentID = ? " +
                       "WHERE l.courseID = ? " +
                       "ORDER BY l.lessonID";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    int lessonNumber = 1;
                    while (rs.next()) {
                        int lessonId = rs.getInt("lessonID");
                        String title = rs.getString("title");
                        String category = rs.getString("category");
                        String status = rs.getString("completionStatus");
                        Date completionDate = rs.getDate("completionDate");
                        
                        // Add lesson number and title in a clean format
                        content.append(lessonNumber + ". " + title + "\n");
                        content.append("   Category: " + (category != null ? category : "Uncategorized") + "\n");
                        content.append("   Status: " + (status != null ? status : "Not Started") + "\n");
                        
                        if (completionDate != null) {
                            content.append("   Completed on: " + completionDate + "\n");
                        }
                        
                        // Only add content preview if the column exists
                        if (contentColumnExists) {
                            String lessonContent = rs.getString("content");
                            if (lessonContent != null && !lessonContent.isEmpty()) {
                                // Remove markdown formatting and take a short preview
                                String cleanContent = lessonContent
                                    .replaceAll("#\\s+", "")
                                    .replaceAll("\\*\\*", "")
                                    .trim();
                                
                                if (cleanContent.length() > 100) {
                                    content.append("   Preview: " + cleanContent.substring(0, 100) + "...\n");
                                } else {
                                    content.append("   Preview: " + cleanContent + "\n");
                                }
                            } else {
                                content.append("   Preview: Content will be available soon.\n");
                            }
                        }
                        
                        content.append("\n");
                        lessonNumber++;
                    }
                }
            }
        } catch (SQLException e) {
            content.append("Error loading course content: ").append(e.getMessage());
            e.printStackTrace();
        }
        
        if (content.length() == 0) {
            content.append("This course doesn't have any lessons yet. Check back later for updates!");
        }
        
        return content.toString();
    }

    // Check if content column exists in Lessons table
    private boolean checkIfContentColumnExists(Connection conn) throws SQLException {
        boolean exists = false;
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "Lessons", "content")) {
            exists = rs.next();
        }
        System.out.println("Content column exists: " + exists);
        
        // If column doesn't exist, create it
        if (!exists) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE Lessons ADD COLUMN content TEXT");
                System.out.println("Added missing content column to Lessons table");
                return true;
            } catch (SQLException e) {
                System.out.println("Failed to add content column: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return exists;
    }

    private BorderPane createMainView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        HBox header = createHeader();
        root.setTop(header);
        
        mainContent = new StackPane();
        mainContent.setPadding(new Insets(20));
        
        VBox coursesView = createCoursesView();
        courseDetailView = createCourseDetailView();
        
        mainContent.getChildren().add(coursesView);
        root.setCenter(mainContent);
        
        HBox footer = createFooter();
        root.setBottom(footer);
        
        return root;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(20, 30, 15, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + CARD_COLOR + ";");
        
        Rectangle logoPlaceholder = new Rectangle(40, 40);
        logoPlaceholder.setArcWidth(10);
        logoPlaceholder.setArcHeight(10);
        logoPlaceholder.setFill(Color.web(PRIMARY_COLOR));
        
        Label titleLabel = new Label("My Enrolled Courses");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));
        
        header.getChildren().addAll(logoPlaceholder, titleLabel);
        header.setEffect(createDropShadow(3));
        
        return header;
    }

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
        
        System.out.println("Enrolled courses count: " + enrolledCourses.size());
        
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
            browseButton.setOnAction(e -> {
                // Navigate to available courses page (to be implemented)
                showError("Navigation", "Navigate to Available Courses page - This feature is coming soon!");
            });
            
            noCoursesBox.getChildren().addAll(noCoursesLabel, enrollSuggestion, browseButton);
            coursesView.getChildren().addAll(welcomeLabel, subtitleLabel, noCoursesBox);
            
            return coursesView;
        }
        
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
        
        Label descLabel = new Label(course.getDescription());
        descLabel.setFont(Font.font("Segoe UI", 14));
        descLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(60);
        
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
        
        Button actionButton = new Button(getButtonTextForProgress(course.getProgress()));
        actionButton.setPrefWidth(Double.MAX_VALUE);
        actionButton.setStyle(getButtonStyleForProgress(course.getProgress()));
        
        // Add action to the card's button
        actionButton.setOnAction(e -> {
            currentCourseId = course.getId();
            showCourseDetail(course);
        });
        
        card.setOnMouseClicked(e -> {
            currentCourseId = course.getId();
            showCourseDetail(course);
        });
        
        card.getChildren().addAll(titleLabel, descLabel, spacer, progressBox, actionButton);
        
        return card;
    }

    private BorderPane createCourseDetailView() {
        BorderPane detailView = new BorderPane();
        detailView.setPadding(new Insets(20));
        detailView.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 10;");
        
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
        
        VBox contentSection = new VBox(20);
        contentSection.setPadding(new Insets(20));
        contentSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        
        HBox metadataBar = new HBox(20);
        metadataBar.setAlignment(Pos.CENTER_LEFT);
        metadataBar.setPadding(new Insets(10, 0, 10, 0));
        
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
        previousButton.setOnAction(e -> navigateToPreviousLesson());
        
        nextButton = new Button("Next Lesson");
        nextButton.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: " + PRIMARY_COLOR + "; " +
            "-fx-border-color: " + PRIMARY_COLOR + "; " +
            "-fx-border-radius: 5;"
        );
        nextButton.setPrefWidth(150);
        nextButton.setOnAction(e -> navigateToNextLesson());
        
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

    private void navigateToPreviousLesson() {
        try {
            int prevLessonId = getPreviousLesson(currentCourseId, currentLessonId);
            if (prevLessonId > 0) {
                currentLessonId = prevLessonId;
                updateLessonContent();
            } else {
                showError("Navigation", "You are at the first lesson of this course.");
            }
        } catch (SQLException e) {
            showError("Database Error", "Error navigating to previous lesson: " + e.getMessage());
        }
    }

    private void navigateToNextLesson() {
        try {
            int nextLessonId = getNextLesson(currentCourseId, currentLessonId);
            if (nextLessonId > 0) {
                currentLessonId = nextLessonId;
                updateLessonContent();
            } else {
                showError("Navigation", "You are at the last lesson of this course.");
            }
        } catch (SQLException e) {
            showError("Database Error", "Error navigating to next lesson: " + e.getMessage());
        }
    }

    private void updateLessonContent() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if content column exists
            boolean contentColumnExists = checkIfContentColumnExists(conn);
            
            // Use appropriate query based on column existence
            String query;
            if (contentColumnExists) {
                query = "SELECT title, content, category FROM Lessons WHERE lessonID = ?";
            } else {
                query = "SELECT title, category FROM Lessons WHERE lessonID = ?";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, currentLessonId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String title = rs.getString("title");
                        String category = rs.getString("category");
                        
                        // Update the title in a cleaner format
                        lessonTitleLabel.setText(title);
                        
                        // Get content if column exists
                        String content;
                        if (contentColumnExists) {
                            content = rs.getString("content");
                            if (content == null || content.isEmpty()) {
                                content = "No content available for this lesson. The instructor has not added content yet.";
                                // Add placeholder content since the column exists but is empty
                                addPlaceholderContent(currentLessonId, title, category);
                            } else {
                                // Clean up the content by removing markdown formatting
                                content = formatLessonContent(content, title, category);
                            }
                        } else {
                            content = "The lesson content feature has been newly added. Please check back later for updated content.";
                        }
                        
                        lessonContentArea.setText(content);
                        
                        // Update completion status button
                        boolean isCompleted = isLessonCompleted(currentLessonId, studentId);
                        if (isCompleted) {
                            lessonActionButton.setText("Completed");
                            lessonActionButton.setStyle(
                                "-fx-background-color: " + ACCENT_COLOR + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 5; " +
                                "-fx-font-weight: bold;"
                            );
                        } else {
                            lessonActionButton.setText("Mark as Completed");
                            lessonActionButton.setStyle(
                                "-fx-background-color: " + SECONDARY_COLOR + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 5; " +
                                "-fx-font-weight: bold;"
                            );
                            // Set action for marking lesson as completed
                            lessonActionButton.setOnAction(e -> {
                                try {
                                    markLessonAsCompleted(currentCourseId, studentId, currentLessonId);
                                    updateLessonContent(); // Refresh the content
                                    Alert successAlert = new Alert(AlertType.INFORMATION);
                                    successAlert.setTitle("Progress Updated");
                                    successAlert.setHeaderText("Lesson Completed!");
                                    successAlert.setContentText("Your progress has been updated.");
                                    successAlert.show();
                                } catch (SQLException ex) {
                                    showError("Error", "Could not update progress: " + ex.getMessage());
                                }
                            });
                        }
                    }
                }
            }
        } catch (SQLException e) {
            showError("Database Error", "Error loading lesson content: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Format lesson content to remove markdown and make it more readable
     */
    private String formatLessonContent(String content, String title, String category) {
        // Remove markdown headers
        content = content.replaceAll("#\\s+", "");
        // Remove markdown bold formatting
        content = content.replaceAll("\\*\\*", "");
        
        // Create a nicely formatted lesson view
        StringBuilder formattedContent = new StringBuilder();
        
        // Add a nice lesson header
        formattedContent.append("LESSON: ").append(title).append("\n\n");
        formattedContent.append("Category: ").append(category).append("\n\n");
        formattedContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        
        // Add the cleaned content
        formattedContent.append(content);
        
        return formattedContent.toString();
    }

    private void addPlaceholderContent(int lessonId, String title, String category) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Create formatted content without markdown
            String content = "LESSON: " + title + "\n\n" +
                "Welcome to this lesson on " + title + " in the " + category + " category.\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Topics that will be covered:\n\n" +
                "1. Introduction to " + title + "\n" +
                "2. Key concepts and principles\n" +
                "3. Practical applications\n" +
                "4. Exercises and examples";
                
            String query = "UPDATE Lessons SET content = ? WHERE lessonID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, content);
                pstmt.setInt(2, lessonId);
                pstmt.executeUpdate();
                System.out.println("Added placeholder content for lesson ID: " + lessonId);
            }
        } catch (SQLException e) {
            System.out.println("Error adding placeholder content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getPreviousLesson(int courseId, int currentLessonId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT MAX(lessonID) as prevLesson FROM Lessons " +
                "WHERE courseID = ? AND lessonID < ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, courseId);
                pstmt.setInt(2, currentLessonId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getInt("prevLesson") : 0;
                }
            }
        }
    }

    private int getNextLesson(int courseId, int currentLessonId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT MIN(lessonID) as nextLesson FROM Lessons " +
                "WHERE courseID = ? AND lessonID > ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, courseId);
                pstmt.setInt(2, currentLessonId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getInt("nextLesson") : 0;
                }
            }
        }
    }

    private boolean isLessonCompleted(int lessonId, int studentId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT completionStatus FROM LessonProgress " +
                "WHERE lessonID = ? AND studentID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, lessonId);
                pstmt.setInt(2, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() && "Completed".equals(rs.getString("completionStatus"));
                }
            }
        }
    }

    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: " + CARD_COLOR + ";");
        return footer;
    }

    private void showCourseDetail(Course course) {
        lessonTitleLabel.setText(course.getName());
        
        // Format the overview content to be more readable without markdown
        String formattedContent = formatCourseOverview(course.getLessonContent());
        lessonContentArea.setText(formattedContent);
        
        // Get first lesson for this course
        currentLessonId = getFirstLesson(course.getId());
        
        // Set up the action button
        lessonActionButton.setText(getButtonTextForProgress(course.getProgress()));
        lessonActionButton.setStyle(getButtonStyleForProgress(course.getProgress()));
        
        // Set up navigation buttons
        boolean hasNextLesson = hasNextLesson(course.getId(), currentLessonId);
        boolean hasPrevLesson = hasPreviousLesson(course.getId(), currentLessonId);
        nextButton.setDisable(!hasNextLesson);
        previousButton.setDisable(!hasPrevLesson);
        
        // Set up action button behavior based on the course progress
        if (course.getProgress() == 0.0) {
            // First time starting course
            lessonActionButton.setOnAction(e -> {
                try {
                    markLessonAsCompleted(course.getId(), studentId, currentLessonId);
                    loadEnrolledCourses(); // Refresh data
                    showCourseDetail(getCourseById(course.getId())); // Refresh view
                } catch (SQLException ex) {
                    showError("Error", "Could not update progress: " + ex.getMessage());
                }
            });
        } else if (course.getProgress() < 1.0) {
            // Course in progress
            lessonActionButton.setText("Continue Learning");
            lessonActionButton.setOnAction(e -> {
                try {
                    int nextIncompleteLesson = getNextIncompleteLesson(course.getId(), studentId);
                    if (nextIncompleteLesson > 0) {
                        currentLessonId = nextIncompleteLesson;
                        updateLessonContent();
                    } else {
                        showError("Course Progress", "No incomplete lessons found.");
                    }
                } catch (SQLException ex) {
                    showError("Error", "Could not find next incomplete lesson: " + ex.getMessage());
                }
            });
        } else {
            // Course completed
            lessonActionButton.setText("Review Course");
            lessonActionButton.setOnAction(e -> {
                // Just display the course content for review
                currentLessonId = getFirstLesson(course.getId());
                updateLessonContent();
            });
        }
        
        // Animate transition to course detail view
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
     * Format the course overview content to be readable without markdown
     */
    private String formatCourseOverview(String content) {
        // If the content is the default "no lessons" message, return it as is
        if (content.startsWith("This course doesn't have any lessons yet")) {
            return content;
        }
        
        // Replace heading syntax with clean format
        String formattedContent = content.replaceAll("##\\s+", "")
                                         .replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        
        // Make the overview title more prominent
        if (formattedContent.startsWith("Course Overview")) {
            formattedContent = "COURSE OVERVIEW\n" +
                              "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                              formattedContent.substring("Course Overview".length()).trim();
        }
        
        return formattedContent;
    }

    private Course getCourseById(int courseId) {
        for (Course course : enrolledCourses) {
            if (course.getId() == courseId) {
                return course;
            }
        }
        return null;
    }

    private int getFirstLesson(int courseId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT MIN(lessonID) as firstLesson FROM Lessons WHERE courseID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getInt("firstLesson") : 0;
                }
            }
        } catch (SQLException e) {
            showError("Database Error", "Error retrieving first lesson: " + e.getMessage());
            return 0;
        }
    }

    private boolean hasNextLesson(int courseId, int currentLessonId) {
        try {
            return getNextLesson(courseId, currentLessonId) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean hasPreviousLesson(int courseId, int currentLessonId) {
        try {
            return getPreviousLesson(courseId, currentLessonId) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private int getNextIncompleteLesson(int courseId, int studentId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // This query finds the first lesson in the course that doesn't have a completed status
            String query = "SELECT l.lessonID FROM Lessons l " +
                "LEFT JOIN LessonProgress lp ON l.lessonID = lp.lessonID AND lp.studentID = ? " +
                "WHERE l.courseID = ? AND (lp.completionStatus IS NULL OR lp.completionStatus != 'Completed') " +
                "ORDER BY l.lessonID LIMIT 1";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getInt("lessonID") : 0;
                }
            }
        }
    }

    private void markLessonAsCompleted(int courseId, int studentId, int lessonId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction
            
            try {
                // First, check if the record already exists
                String checkQuery = "SELECT * FROM LessonProgress WHERE studentID = ? AND lessonID = ?";
                boolean recordExists = false;
                
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, studentId);
                    checkStmt.setInt(2, lessonId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        recordExists = rs.next();
                    }
                }
                
                // Either insert new record or update existing one
                String query;
                if (recordExists) {
                    query = "UPDATE LessonProgress SET completionStatus = 'Completed', completionDate = CURRENT_TIMESTAMP " +
                        "WHERE studentID = ? AND lessonID = ?";
                } else {
                    query = "INSERT INTO LessonProgress (studentID, lessonID, completionStatus, completionDate) " +
                        "VALUES (?, ?, 'Completed', CURRENT_TIMESTAMP)";
                }
                
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, studentId);
                    pstmt.setInt(2, lessonId);
                    pstmt.executeUpdate();
                }
                
                // Update the overall course progress in Enrollments table
                String progressQuery = "UPDATE Enrollments e " +
                    "SET completionPercentage = (" +
                    "  SELECT COUNT(*) * 100.0 / (SELECT COUNT(*) FROM Lessons WHERE courseID = ?) " +
                    "  FROM LessonProgress lp " +
                    "  JOIN Lessons l ON lp.lessonID = l.lessonID " +
                    "  WHERE l.courseID = ? AND lp.studentID = ? AND lp.completionStatus = 'Completed'" +
                    ") WHERE e.studentID = ? AND e.courseID = ?";
                
                try (PreparedStatement pstmt = conn.prepareStatement(progressQuery)) {
                    pstmt.setInt(1, courseId);
                    pstmt.setInt(2, courseId);
                    pstmt.setInt(3, studentId);
                    pstmt.setInt(4, studentId);
                    pstmt.setInt(5, courseId);
                    pstmt.executeUpdate();
                }
                
                // Log the activity
                logActivity(studentId, courseId, "Lesson Completion",
                    "Completed lesson ID: " + lessonId, "Completed");
                
                conn.commit(); // Commit transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw e;
            } finally {
                conn.setAutoCommit(true); // Reset auto-commit
            }
        }
    }

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

    private void returnToCoursesView() {
        // Reload the course data in case changes were made
        loadEnrolledCourses();
        
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

    private String getButtonTextForProgress(double progress) {
        if (progress == 0.0) {
            return "Start Course";
        } else if (progress < 1.0) {
            return "Continue Course";
        } else {
            return "Review Course";
        }
    }

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
                        // If no student found, try to insert a new student (for testing purposes)
                        // In a real app, you might handle this differently
                        return createNewStudent(userId, conn);
                    }
                }
            }
        }
    }

    private int createNewStudent(int userId, Connection conn) throws SQLException {
        // This is only for testing purposes - in production, students should be created through proper registration
        String insertQuery = "INSERT INTO Students (studentID, userID) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            int newStudentId = getNextAvailableStudentId(conn);
            pstmt.setInt(1, newStudentId);
            pstmt.setInt(2, userId);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                System.out.println("Created new student with ID: " + newStudentId + " for user ID: " + userId);
                return newStudentId;
            } else {
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("Failed to create new student: " + e.getMessage());
            return 0;
        }
    }

    private int getNextAvailableStudentId(Connection conn) throws SQLException {
        String query = "SELECT MAX(studentID) + 1 as nextId FROM Students";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? Math.max(1, rs.getInt("nextId")) : 1;
        }
    }

    private DropShadow createDropShadow(double radius) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(radius);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        return dropShadow;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Course {
        private final int id;
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty progress;
        private final String lessonContent;
        private final String description;

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