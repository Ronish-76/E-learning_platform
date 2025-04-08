package controllers_Instructors;

import dao.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import controllers_students.Login;
import controllers_students.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard Overview page showing key metrics and statistics from database
 */
public class DashboardOverview {
    // Constants for UI styling
    private static final String BACKGROUND_COLOR = "#f5f7fa";
    private static final String PRIMARY_COLOR = "#3498db";
    private static final String SECONDARY_COLOR = "#2ecc71";
    private static final String DANGER_COLOR = "#e74c3c";
    
    // Data counts
    private int totalCourses = 0;
    private int totalStudents = 0;
    private int totalAssignments = 0;
    
    // Current user data
    private int userID = 0;
    private String currentUsername = "Instructor";
    private boolean isLoggedIn = false;
    
    /**
     * Constructor
     */
    public DashboardOverview() {
        // Load user data from login session
        loadUserDataFromLogin();
        
        // Load basic counts from database
        loadBasicCounts();
    }
    
    /**
     * Load user data from the Login class
     */
    private void loadUserDataFromLogin() {
        // Get the logged-in user from Login class
        User loggedInUser = Login.getLoggedInUser();
        
        if (loggedInUser != null && "Instructor".equals(loggedInUser.getRole())) {
            // User is logged in as instructor
            this.isLoggedIn = true;
            this.userID = loggedInUser.getUserID();
            this.currentUsername = loggedInUser.getUsername();
            System.out.println("Dashboard loaded user data for: " + currentUsername + " (ID: " + userID + ")");
        } else {
            // Not logged in or not an instructor, try to get any instructor
            loadInstructorName();
        }
    }
    
    /**
     * Load basic counts from database
     */
    private void loadBasicCounts() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection is null");
                return;
            }
            
            // Count total courses
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Courses")) {
                if (rs.next()) {
                    totalCourses = rs.getInt("count");
                    System.out.println("Loaded " + totalCourses + " courses from database");
                }
            } catch (SQLException e) {
                System.out.println("Error counting courses: " + e.getMessage());
            }
            
            // Count total students
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Students")) {
                if (rs.next()) {
                    totalStudents = rs.getInt("count");
                    System.out.println("Loaded " + totalStudents + " students from database");
                }
            } catch (SQLException e) {
                System.out.println("Error counting students: " + e.getMessage());
            }
            
            // Count total assignments
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Assignments")) {
                if (rs.next()) {
                    totalAssignments = rs.getInt("count");
                    System.out.println("Loaded " + totalAssignments + " assignments from database");
                }
            } catch (SQLException e) {
                System.out.println("Error counting assignments: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Try to load any instructor name if no logged-in user
     */
    private void loadInstructorName() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            
            // Get first instructor name
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT u.userID, u.username FROM Users u " +
                     "JOIN Instructor i ON u.userID = i.userID " +
                     "LIMIT 1")) {
                if (rs.next()) {
                    this.userID = rs.getInt("userID");
                    this.currentUsername = rs.getString("username");
                    System.out.println("Loaded default instructor name: " + currentUsername);
                }
            } catch (SQLException e) {
                System.out.println("Error loading instructor name: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error in loadInstructorName: " + e.getMessage());
        }
    }
    
    /**
     * Main method to create and return the dashboard view
     */
    public Node getView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Page title
        Label title = new Label("Instructor Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        // Login status indicator - only show if not logged in
        if (!isLoggedIn) {
            Label loginStatus = new Label("Note: You are viewing default data. Please log in for personalized dashboard.");
            loginStatus.setStyle("-fx-text-fill: " + DANGER_COLOR + "; -fx-font-style: italic;");
            view.getChildren().add(loginStatus);
        }
        
        // Welcome message with current date
        HBox welcomeBox = createWelcomeSection();
        
        // Stats cards - basic information from database
        HBox statsCards = createStatsSection();
        
        // Add all components to main view
        view.getChildren().add(title);
        view.getChildren().add(welcomeBox);
        view.getChildren().add(statsCards);
        
        // Add course section if available
        VBox courseSection = createCourseSection();
        if (courseSection != null) {
            view.getChildren().add(courseSection);
        }
        
        // Add assignments section if available
        VBox assignmentSection = createAssignmentSection();
        if (assignmentSection != null) {
            view.getChildren().add(assignmentSection);
        }
        
        // Note: "Create Quick Action Buttons" section has been removed
        
        return view;
    }
    
    /**
     * Create welcome section with current date
     */
    private HBox createWelcomeSection() {
        HBox welcomeBox = new HBox();
        welcomeBox.setPadding(new Insets(0, 0, 10, 0));
        
        VBox welcomeMsg = new VBox(5);
        Label greeting = new Label("Welcome back, " + currentUsername + "!");
        greeting.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        // Get current date formatted
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        Label dateLabel = new Label("Today is " + today);
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        // Add user ID if logged in (for debugging/verification)
        if (isLoggedIn && userID > 0) {
            Label userIdLabel = new Label("User ID: " + userID);
            userIdLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #bdc3c7;");
            welcomeMsg.getChildren().addAll(greeting, dateLabel, userIdLabel);
        } else {
            welcomeMsg.getChildren().addAll(greeting, dateLabel);
        }
        
        welcomeBox.getChildren().add(welcomeMsg);
        return welcomeBox;
    }
    
    /**
     * Create stats section with basic counts
     */
    private HBox createStatsSection() {
        HBox container = new HBox(15);
        container.setPadding(new Insets(10));
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2);");
        
        // Create stat cards for courses, students, and assignments
        VBox coursesCard = createStatCard("Courses", String.valueOf(totalCourses), 
                                         "Total courses", PRIMARY_COLOR);
        VBox studentsCard = createStatCard("Students", String.valueOf(totalStudents), 
                                          "Total students", SECONDARY_COLOR);
        VBox assignmentsCard = createStatCard("Assignments", String.valueOf(totalAssignments), 
                                             "Total assignments", DANGER_COLOR);
        
        container.getChildren().addAll(coursesCard, studentsCard, assignmentsCard);
        HBox.setHgrow(coursesCard, Priority.ALWAYS);
        HBox.setHgrow(studentsCard, Priority.ALWAYS);
        HBox.setHgrow(assignmentsCard, Priority.ALWAYS);
        
        return container;
    }
    
    /**
     * Create a basic stat card
     */
    private VBox createStatCard(String title, String value, String subtitle, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 4;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
        
        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        return card;
    }
    
    /**
     * Create course section with data from database
     */
    private VBox createCourseSection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return null;
            
            // Check if there are any courses
            String countQuery = "SELECT COUNT(*) as count FROM Courses";
            if (isLoggedIn) {
                // If user is logged in, try to show only their courses
                // This is a simplified query - you'd need an actual relationship between instructors and courses
                countQuery = "SELECT COUNT(*) as count FROM Courses WHERE createdBy = " + userID;
            }
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countQuery)) {
                if (rs.next() && rs.getInt("count") > 0) {
                    VBox container = new VBox(15);
                    container.setPadding(new Insets(20));
                    container.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2);");
                    
                    Label title = new Label("Your Courses");
                    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");
                    
                    VBox courseList = new VBox(10);
                    
                    // Get course data
                    String courseQuery = "SELECT courseID, courseName, description FROM Courses LIMIT 5";
                    if (isLoggedIn) {
                        // If user is logged in, show only their courses
                        courseQuery = "SELECT courseID, courseName, description FROM Courses WHERE createdBy = " + userID + " LIMIT 5";
                    }
                    
                    try (Statement courseStmt = conn.createStatement();
                         ResultSet courseRs = courseStmt.executeQuery(courseQuery)) {
                        while (courseRs.next()) {
                            int courseId = courseRs.getInt("courseID");
                            String courseName = courseRs.getString("courseName");
                            String description = courseRs.getString("description");
                            if (description == null) description = "No description available";
                            
                            courseList.getChildren().add(
                                createCourseItem(courseId, courseName, description)
                            );
                        }
                    }
                    
                    container.getChildren().addAll(title, courseList);
                    return container;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading courses: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Create a course item for display
     */
    private HBox createCourseItem(int courseId, String courseName, String description) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 4;");
        
        // Color indicator
        Rectangle indicator = new Rectangle(5, 60);
        indicator.setStyle("-fx-fill: " + PRIMARY_COLOR + ";");
        
        // Course details
        VBox details = new VBox(5);
        Label nameLabel = new Label(courseName);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        Label descLabel = new Label(description.length() > 100 ? 
                                   description.substring(0, 97) + "..." : description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);
        
        details.getChildren().addAll(nameLabel, descLabel);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        // View button
        Button viewBtn = new Button("View");
        viewBtn.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4;"
        );
        
        item.getChildren().addAll(indicator, details, viewBtn);
        return item;
    }
    
    /**
     * Create assignments section with data from database
     */
    private VBox createAssignmentSection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return null;
            
            // Check if there are any assignments
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM Assignments")) {
                if (rs.next() && rs.getInt("count") > 0) {
                    VBox container = new VBox(15);
                    container.setPadding(new Insets(20));
                    container.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2);");
                    
                    Label title = new Label("Upcoming Assignments");
                    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");
                    
                    VBox assignmentList = new VBox(10);
                    
                    // Get assignment data - add filter by instructor if applicable
                    String assignmentQuery = 
                        "SELECT a.title, a.dueDate, a.priority, c.courseName " +
                        "FROM Assignments a " +
                        "JOIN Courses c ON a.courseID = c.courseID " +
                        "ORDER BY a.dueDate ASC " +
                        "LIMIT 5";
                    
                    if (isLoggedIn) {
                        // If user is logged in, try to show only their assignments
                        // This assumes courses have a createdBy field
                        assignmentQuery = 
                            "SELECT a.title, a.dueDate, a.priority, c.courseName " +
                            "FROM Assignments a " +
                            "JOIN Courses c ON a.courseID = c.courseID " +
                            "WHERE c.createdBy = " + userID + " " +
                            "ORDER BY a.dueDate ASC " +
                            "LIMIT 5";
                    }
                    
                    try (Statement asmtStmt = conn.createStatement();
                         ResultSet asmtRs = asmtStmt.executeQuery(assignmentQuery)) {
                        while (asmtRs.next()) {
                            String title1 = asmtRs.getString("title");
                            Date dueDate = asmtRs.getDate("dueDate");
                            String priority = asmtRs.getString("priority");
                            String courseName = asmtRs.getString("courseName");
                            
                            // Format due date
                            String formattedDate = "No due date";
                            if (dueDate != null) {
                                formattedDate = new java.text.SimpleDateFormat("MMM d, yyyy").format(dueDate);
                            }
                            
                            // Default priority if null
                            if (priority == null) priority = "Medium";
                            
                            assignmentList.getChildren().add(
                                createAssignmentItem(title1, courseName, formattedDate, priority)
                            );
                        }
                    }
                    
                    container.getChildren().addAll(title, assignmentList);
                    return container;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading assignments: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Create an assignment item for display
     */
    private HBox createAssignmentItem(String title, String course, String dueDate, String priority) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 4;");
        
        // Color indicator based on priority
        String indicatorColor;
        if (priority.equalsIgnoreCase("High")) {
            indicatorColor = DANGER_COLOR;
        } else if (priority.equalsIgnoreCase("Medium")) {
            indicatorColor = "#f39c12"; // Orange
        } else {
            indicatorColor = SECONDARY_COLOR;
        }
        
        Rectangle indicator = new Rectangle(5, 60);
        indicator.setStyle("-fx-fill: " + indicatorColor + ";");
        
        // Assignment details
        VBox details = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        Label courseLabel = new Label(course);
        courseLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        Label dateLabel = new Label("Due: " + dueDate);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        details.getChildren().addAll(titleLabel, courseLabel, dateLabel);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        // Priority label
        Label priorityLabel = new Label(priority);
        priorityLabel.setStyle(
            "-fx-background-color: " + indicatorColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 2 8;" +
            "-fx-background-radius: 4;"
        );
        
        item.getChildren().addAll(indicator, details, priorityLabel);
        return item;
    }
    
    // Note: The createQuickActionButtons() method has been completely removed
}