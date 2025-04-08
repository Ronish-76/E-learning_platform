package controllers_students;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import dao.DatabaseConnection;

public class HomePage {
    // Colors
    private final Color PRIMARY_LIGHT = Color.web("#f0f5ff");
    private final Color PRIMARY = Color.web("#4a86e8");
    private final Color SECONDARY = Color.web("#34a853");
    private final Color ACCENT = Color.web("#ea4335");
    private final Color NEUTRAL = Color.web("#f1f3f4");
    private final Color TEXT_PRIMARY = Color.web("#202124");
    private final Color TEXT_SECONDARY = Color.web("#5f6368");
    
    // Current user
    private User currentUser; 
    private int currentUserId = 0;
    
    // Class data from database
    private List<ClassData> availableCourses = new ArrayList<>();
    private VBox mainContent; // Main content container to reload after course enrollment
    
    /**
     * Constructor for HomePage with no user (default)
     */
    public HomePage() {
        // Try to get logged-in user from Login class
        this.currentUser = Login.getLoggedInUser();
        if (currentUser != null) {
            this.currentUserId = currentUser.getUserID();
        }
        loadAvailableCourses();
    }
    
    /**
     * Constructor for HomePage with userId (when passed explicitly)
     */
    public HomePage(int userId) {
        this.currentUserId = userId;
        // If Login.getLoggedInUser() is available, prefer that
        User loggedInUser = Login.getLoggedInUser();
        if (loggedInUser != null) {
            this.currentUser = loggedInUser;
            this.currentUserId = loggedInUser.getUserID();
        }
        loadAvailableCourses();
    }
    
    /**
     * Load available courses from database, excluding ones the student is already enrolled in
     */
    private void loadAvailableCourses() {
        availableCourses.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query;
            PreparedStatement pstmt;
            if (currentUserId > 0) {
                // If user is logged in, load courses they haven't enrolled in yet
                query = "SELECT c.courseID, c.courseName, c.description FROM Courses c " +
                        "WHERE c.courseID NOT IN (" +
                        " SELECT e.courseID FROM Enrollments e " +
                        " JOIN Students s ON e.studentID = s.studentID " +
                        " WHERE s.userID = ?" +
                        ")";
                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, currentUserId);
            } else {
                // If no user is logged in, load all courses
                query = "SELECT courseID, courseName, description FROM Courses";
                pstmt = conn.prepareStatement(query);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ClassData course = new ClassData(
                        rs.getInt("courseID"),
                        rs.getString("courseName"),
                        rs.getString("description")
                    );
                    availableCourses.add(course);
                }
            }
        } catch (SQLException e) {
            showError("Database Error", "Error loading courses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Node getView() {
        mainContent = new VBox(0);
        mainContent.getChildren().add(getHomePage());
        return mainContent;
    }
    
    private ScrollPane getHomePage() {
        VBox homePageContent = new VBox(20); // Add spacing between main sections
        homePageContent.setAlignment(Pos.TOP_CENTER);
        homePageContent.setStyle("-fx-background-color: #f4f4f4;");
        homePageContent.setPadding(new Insets(0, 0, 20, 0)); // Add bottom padding
        
        // Add header with platform title
        VBox headerSection = createHeaderSection();
        
        // Main content layout with two columns with 75%/25% distribution
        HBox mainColumns = new HBox(20);
        mainColumns.setPadding(new Insets(0, 20, 0, 20));
        mainColumns.setAlignment(Pos.TOP_CENTER);
        mainColumns.setPrefWidth(Double.MAX_VALUE);
        
        // Left Section: 'Join Classes' - 75% of the width
        VBox leftSection = createExpandedJoinClassesSection();
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        
        // Right Section: 'Announcements' - 25% of the width
        VBox rightSection = createAnnouncementsSection();
        HBox.setHgrow(rightSection, Priority.ALWAYS);
        
        // Set the width proportions for 75/25 split
        double totalWidth = 100.0; // percentage
        double leftWidth = totalWidth * 0.75; // 75%
        double rightWidth = totalWidth * 0.25; // 25%
        
        // Set constraints to maintain the 75/25 split
        ColumnConstraints leftConstraint = new ColumnConstraints();
        leftConstraint.setPercentWidth(leftWidth);
        ColumnConstraints rightConstraint = new ColumnConstraints();
        rightConstraint.setPercentWidth(rightWidth);
        
        // Create an HBox with width constraints
        GridPane mainGrid = new GridPane();
        mainGrid.setPadding(new Insets(0, 20, 0, 20));
        mainGrid.setHgap(20);
        mainGrid.getColumnConstraints().addAll(leftConstraint, rightConstraint);
        mainGrid.add(leftSection, 0, 0);
        mainGrid.add(rightSection, 1, 0);
        
        // Add all sections to the main content
        homePageContent.getChildren().addAll(headerSection, mainGrid);
        
        // Wrap content in ScrollPane
        ScrollPane scrollPane = new ScrollPane(homePageContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        return scrollPane;
    }
    
    private VBox createHeaderSection() {
        VBox header = new VBox();
        // Platform title with gradient background
        StackPane titlePane = new StackPane();
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4a86e8")), new Stop(1, Color.web("#34a853")));
        titlePane.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
        titlePane.setPrefHeight(70);
        
        Text platformTitle = new Text("E-Learning Platform");
        platformTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        platformTitle.setFill(Color.WHITE);
        titlePane.getChildren().add(platformTitle);
        
        header.getChildren().add(titlePane);
        return header;
    }
    
    private VBox createAnnouncementsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 20, 10, 20));
        section.setAlignment(Pos.TOP_LEFT);
        
        // Adjust width to be narrower for 25% width
        section.setPrefWidth(300); // Starting width, will scale with constraints
        
        // Section title
        Label title = new Label("Announcements");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        title.setTextFill(TEXT_PRIMARY);
        
        // Announcements container
        VBox announcements = new VBox(15);
        announcements.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        announcements.setPadding(new Insets(20));
        
        // Sample announcements - in a real app, these would come from the database
        VBox announcement1 = createAnnouncement(
            "New Courses Available",
            "We've added several new courses to our catalog. Check them out below!",
            "2 days ago"
        );
        
        VBox announcement2 = createAnnouncement(
            "Welcome to Our Platform",
            "Thank you for joining our e-learning platform. We're excited to have you with us!",
            "2 days ago"
        );
        
        announcements.getChildren().addAll(announcement1, new Separator(), announcement2);
        
        // Add a drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(5);
        announcements.setEffect(dropShadow);
        
        section.getChildren().addAll(title, announcements);
        return section;
    }
    
    private VBox createAnnouncement(String title, String content, String date) {
        VBox announcement = new VBox(8);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        titleLabel.setTextFill(TEXT_PRIMARY);
        
        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("Roboto", 14));
        contentLabel.setWrapText(true);
        contentLabel.setTextFill(TEXT_PRIMARY);
        
        Label dateLabel = new Label(date);
        dateLabel.setFont(Font.font("Roboto", FontWeight.LIGHT, 12));
        dateLabel.setTextFill(TEXT_SECONDARY);
        
        announcement.getChildren().addAll(titleLabel, contentLabel, dateLabel);
        return announcement;
    }
    
    private VBox createExpandedJoinClassesSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        section.setAlignment(Pos.TOP_CENTER);
        
        // Make this section larger (75% of the page width)
        section.setPrefWidth(900); // Initial size, will scale with constraints
        
        // Section Title
        Text sectionTitle = new Text("Available Courses");
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        sectionTitle.setFill(TEXT_PRIMARY);
        
        // Section description
        Label descriptionLabel = new Label("Browse and join available courses to start your learning journey");
        descriptionLabel.setFont(Font.font("Roboto", 16));
        descriptionLabel.setTextFill(TEXT_SECONDARY);
        descriptionLabel.setPadding(new Insets(0, 0, 10, 0));
        
        if (availableCourses.isEmpty()) {
            // Display a message if no courses are available
            VBox noCoursesContainer = new VBox(15);
            noCoursesContainer.setAlignment(Pos.CENTER);
            noCoursesContainer.setPadding(new Insets(30));
            noCoursesContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
            
            Label noCoursesLabel = new Label("You are already enrolled in all available courses.");
            noCoursesLabel.setFont(Font.font("Roboto", 16));
            noCoursesLabel.setTextFill(TEXT_PRIMARY);
            noCoursesLabel.setWrapText(true);
            
            Label suggestLabel = new Label("Check your enrolled courses for learning materials.");
            suggestLabel.setFont(Font.font("Roboto", 14));
            suggestLabel.setTextFill(TEXT_SECONDARY);
            suggestLabel.setWrapText(true);
            
            noCoursesContainer.getChildren().addAll(noCoursesLabel, suggestLabel);
            section.getChildren().addAll(sectionTitle, descriptionLabel, noCoursesContainer);
        } else {
            // Grid for class cards - now can fit 3 cards per row in the wider space
            GridPane classGrid = new GridPane();
            classGrid.setHgap(20); // Horizontal gap
            classGrid.setVgap(20); // Vertical gap
            classGrid.setAlignment(Pos.CENTER);
            
            int col = 0;
            int row = 0;
            int maxColsPerRow = 3; // Now we can fit 3 cards in the wider space
            
            for (ClassData classData : availableCourses) {
                VBox classCard = createExpandedClassCard(classData);
                
                // Add card to grid
                classGrid.add(classCard, col, row);
                
                // Update grid position
                col++;
                if (col >= maxColsPerRow) { // 3 columns for larger section
                    col = 0;
                    row++;
                }
            }
            section.getChildren().addAll(sectionTitle, descriptionLabel, classGrid);
        }
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(5);
        section.setEffect(dropShadow);
        
        return section;
    }
    
    private VBox createExpandedClassCard(ClassData classData) {
        VBox card = new VBox(15); // Increased spacing between elements
        card.setPadding(new Insets(20)); // Increased padding
        card.setPrefWidth(250); // Slightly smaller than before to fit 3 in a row
        card.setPrefHeight(200); // Set height to make cards uniform
        card.setAlignment(Pos.TOP_LEFT); // Align content to top-left for better layout
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        
        // Course icon/image placeholder
        HBox iconRow = new HBox();
        iconRow.setAlignment(Pos.CENTER_LEFT);
        
        Circle courseIcon = new Circle(20);
        courseIcon.setFill(PRIMARY);
        
        Label courseIconText = new Label(classData.getName().substring(0, 1).toUpperCase());
        courseIconText.setTextFill(Color.WHITE);
        courseIconText.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        
        StackPane iconContainer = new StackPane(courseIcon, courseIconText);
        
        Label courseLabel = new Label(classData.getName());
        courseLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        courseLabel.setTextFill(TEXT_PRIMARY);
        courseLabel.setPadding(new Insets(0, 0, 0, 10));
        
        iconRow.getChildren().addAll(iconContainer, courseLabel);
        
        // Description with proper wrapping
        Label descriptionLabel = new Label(classData.getDescription());
        descriptionLabel.setFont(Font.font("Roboto", 14));
        descriptionLabel.setTextFill(TEXT_SECONDARY);
        descriptionLabel.setWrapText(true);
        
        // Add a region to push the button to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Join Button with confirmation dialog
        Button joinButton = new Button("Join Course");
        joinButton.setPrefWidth(150);
        joinButton.setPrefHeight(35);
        joinButton.setStyle("-fx-background-color: #34a853; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Add confirmation dialog on button click
        joinButton.setOnAction(e -> {
            if (currentUserId <= 0) {
                showError("Authentication Required", "Please log in to join a class.");
                return;
            }
            
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Join Course");
            confirmationAlert.setHeaderText("Join " + classData.getName());
            confirmationAlert.setContentText("Are you sure you want to join " + classData.getName() + "?\n\n" + classData.getDescription());
            
            // If OK is clicked, enroll the student in the course
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        enrollStudentInCourse(classData.getId());
                        
                        // Show success message
                        Alert successAlert = new Alert(AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("Successfully Joined!");
                        successAlert.setContentText("You have successfully joined " + classData.getName() + ". You can now access course materials.");
                        successAlert.show();
                        
                        // Reload available courses and refresh the view
                        loadAvailableCourses();
                        refreshView();
                    } catch (Exception ex) {
                        showError("Enrollment Error", ex.getMessage());
                    }
                }
            });
        });
        
        // Add tooltip with description
        Tooltip tooltip = new Tooltip(classData.getDescription());
        Tooltip.install(card, tooltip);
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #f0f5ff; -fx-background-radius: 8; -fx-scale-x: 1.03; -fx-scale-y: 1.03;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-scale-x: 1; -fx-scale-y: 1;");
        });
        
        card.getChildren().addAll(iconRow, descriptionLabel, spacer, joinButton);
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(1);
        dropShadow.setRadius(3);
        card.setEffect(dropShadow);
        
        return card;
    }
    
    private void enrollStudentInCourse(int courseId) throws SQLException {
        // Get student ID from user ID
        int studentId = getStudentId(currentUserId);
        if (studentId <= 0) {
            throw new SQLException("You must be a student to enroll in courses.");
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if already enrolled
            String checkQuery = "SELECT * FROM Enrollments WHERE studentID = ? AND courseID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("You are already enrolled in this course.");
                    }
                }
            }
            
            // Enroll student
            String enrollQuery = "INSERT INTO Enrollments (studentID, courseID) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(enrollQuery)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                pstmt.executeUpdate();
                
                // Log activity
                logActivity(studentId, courseId, "Enrollment", "Joined the course", "Completed");
            }
        }
    }
    
    private void refreshView() {
        // Replace the current content with the updated view
        if (mainContent != null) {
            mainContent.getChildren().clear();
            mainContent.getChildren().add(getHomePage());
        }
    }
    
    private int getStudentId(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT studentID FROM Students WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("studentID");
                    } else {
                        // Check if user exists but isn't a student
                        String userCheckQuery = "SELECT role FROM Users WHERE userID = ?";
                        try (PreparedStatement userPstmt = conn.prepareStatement(userCheckQuery)) {
                            userPstmt.setInt(1, userId);
                            try (ResultSet userRs = userPstmt.executeQuery()) {
                                if (userRs.next()) {
                                    String role = userRs.getString("role");
                                    if (!"Student".equals(role)) {
                                        throw new SQLException("You must have a student account to enroll in courses. Current role: " + role);
                                    }
                                }
                            }
                        }
                        
                        // If we get here, user exists and is a student but doesn't have a student record
                        // Create a student record
                        String insertQuery = "INSERT INTO Students (studentID, userID) VALUES (?, ?)";
                        try (PreparedStatement insertPstmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                            // Use userID as studentID for simplicity
                            insertPstmt.setInt(1, userId);
                            insertPstmt.setInt(2, userId);
                            insertPstmt.executeUpdate();
                            try (ResultSet generatedKeys = insertPstmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    return generatedKeys.getInt(1);
                                } else {
                                    return userId; // Fallback to using userId as studentId
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void logActivity(int studentId, int courseId, String activityType, String description, String status) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Activities (studentID, courseID, activityType, description, completionStatus) VALUES (?, ?, ?, ?, ?)";
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
    
    // Helper method to show error alerts
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper class for class data
    private static class ClassData {
        private final int id;
        private final String name;
        private final String description;
        
        public ClassData(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public int getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Keep your other methods unchanged
}