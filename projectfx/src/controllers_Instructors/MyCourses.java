package controllers_Instructors;

import dao.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import controllers_students.Login;
import controllers_students.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * My Courses page for managing instructor's courses
 * Displays courses from the database for the logged-in instructor
 */
public class MyCourses {
    private int currentInstructorId = 0;
    private int currentUserId = 0;
    private String currentUsername = "";
    private boolean isLoggedIn = false;
    private List<CourseData> courses = new ArrayList<>();
    private FlowPane courseGrid;
    
    /**
     * Constructor with instructor ID
     */
    public MyCourses(int instructorId) {
        this.currentInstructorId = instructorId;
        findUserIdForInstructor();
        loadCoursesFromDatabase();
    }
    
    /**
     * Default constructor - will try to get user data from Login
     */
    public MyCourses() {
        loadUserDataFromLogin();
        loadCoursesFromDatabase();
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
            this.currentUserId = loggedInUser.getUserID();
            this.currentUsername = loggedInUser.getUsername();
            
            // Get instructor ID for the logged-in user
            findInstructorIdForUser();
            
            System.out.println("MyCourses loaded user data: " + currentUsername + 
                              " (User ID: " + currentUserId + 
                              ", Instructor ID: " + currentInstructorId + ")");
        } else {
            // Not logged in or not an instructor, find any instructor
            this.isLoggedIn = false;
            findFirstInstructor();
        }
    }
    
    /**
     * Find instructor ID for the logged-in user
     */
    private void findInstructorIdForUser() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            String query = "SELECT instructorID FROM Instructor WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, currentUserId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentInstructorId = rs.getInt("instructorID");
                        System.out.println("Found instructorID: " + currentInstructorId + 
                                          " for userID: " + currentUserId);
                    } else {
                        System.out.println("No instructor record found for user ID: " + currentUserId);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding instructor ID: " + e.getMessage());
        }
    }
    
    /**
     * Try to find the first instructor in the database
     */
    private void findFirstInstructor() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection is null");
                return;
            }
            
            String query = "SELECT i.instructorID, i.userID, u.username FROM Instructor i " +
                          "JOIN Users u ON i.userID = u.userID " +
                          "WHERE u.role = 'Instructor' " +
                          "LIMIT 1";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    currentInstructorId = rs.getInt("instructorID");
                    currentUserId = rs.getInt("userID");
                    currentUsername = rs.getString("username");
                    System.out.println("Found instructor ID: " + currentInstructorId + 
                                      ", userID: " + currentUserId +
                                      ", username: " + currentUsername);
                } else {
                    System.out.println("No instructors found in database");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding instructor: " + e.getMessage());
        }
    }
    
    /**
     * Find the user ID for a given instructor ID
     */
    private void findUserIdForInstructor() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            
            String query = "SELECT u.userID, u.username FROM Instructor i " +
                          "JOIN Users u ON i.userID = u.userID " +
                          "WHERE i.instructorID = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, currentInstructorId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentUserId = rs.getInt("userID");
                        currentUsername = rs.getString("username");
                        System.out.println("Found userID: " + currentUserId + 
                                          ", username: " + currentUsername +
                                          " for instructorID: " + currentInstructorId);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding user ID for instructor: " + e.getMessage());
        }
    }
    
    /**
     * Load courses from database
     */
    private void loadCoursesFromDatabase() {
        courses.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection is null");
                return;
            }
            
            // Basic query to get courses
            String query = "SELECT c.courseID, c.courseName, c.description, c.courseColor, " +
                          "COUNT(DISTINCT e.studentID) as studentCount " +
                          "FROM Courses c " +
                          "LEFT JOIN Enrollments e ON c.courseID = e.courseID " +
                          "GROUP BY c.courseID";
            
            // Try to use instructor ID if available
            if (currentUserId > 0) {
                query = "SELECT c.courseID, c.courseName, c.description, c.courseColor, " +
                        "COUNT(DISTINCT e.studentID) as studentCount " +
                        "FROM Courses c " +
                        "LEFT JOIN Enrollments e ON c.courseID = e.courseID " +
                        "WHERE c.createdBy = ? " +
                        "GROUP BY c.courseID";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                // Set instructor ID if using parameterized query
                if (currentUserId > 0) {
                    pstmt.setInt(1, currentUserId);
                }
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int courseId = rs.getInt("courseID");
                        String courseName = rs.getString("courseName");
                        String description = rs.getString("description");
                        String courseColor = rs.getString("courseColor");
                        int studentCount = rs.getInt("studentCount");
                        
                        // Generate a random rating between 4.0 and 5.0 for courses with students
                        double rating = 0.0;
                        if (studentCount > 0) {
                            rating = 4.0 + (new Random().nextDouble() * 1.0);
                        }
                        
                        // Ensure color is set
                        if (courseColor == null || courseColor.isEmpty()) {
                            courseColor = getRandomColor();
                        }
                        
                        // Add to courses list
                        courses.add(new CourseData(
                            courseId, courseName, description, courseColor, "Active", // Assume active status
                            getCategoryFromName(courseName),
                            studentCount, rating
                        ));
                    }
                }
                
                System.out.println("Loaded " + courses.size() + " courses from database for " + 
                                  (isLoggedIn ? "logged-in instructor" : "default instructor"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading courses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main method to return the view
     */
    public Node getView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #f5f7fa;");
        
        Label title = new Label("My Courses");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;");
        
        // Login status indicator - only show if not logged in
        if (!isLoggedIn) {
            Label loginStatus = new Label("Note: You are viewing default instructor data. Please log in for your courses.");
            loginStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
            view.getChildren().add(loginStatus);
        } else {
            // Show welcome with instructor name
            Label welcomeLabel = new Label("Welcome, " + currentUsername + ". Here are your courses:");
            welcomeLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 14px;");
            view.getChildren().add(welcomeLabel);
        }
        
        // Filter and actions section
        HBox actionContainer = createActionContainer();
        
        // Course cards
        courseGrid = createCourseGrid();
        courseGrid.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(courseGrid, Priority.ALWAYS);
        
        view.getChildren().addAll(title, actionContainer, courseGrid);
        return view;
    }
    
    /**
     * Create action container with search and filters
     */
    private HBox createActionContainer() {
        HBox actionContainer = new HBox(10);
        actionContainer.setPadding(new Insets(0, 0, 10, 0));
        actionContainer.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search courses...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addCourseBtn = new Button("+ Create New Course");
        addCourseBtn.setStyle(
            "-fx-background-color: #2ecc71; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 4; " +
            "-fx-padding: 8 15;"
        );
        
        // Add the create course dialog functionality
        addCourseBtn.setOnAction(e -> showCreateCourseDialog());
        
        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchCourses(newValue);
        });
        
        actionContainer.getChildren().addAll(searchField, spacer, addCourseBtn);
        return actionContainer;
    }
    
    /**
     * Search courses by name
     */
    private void searchCourses(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // If search is empty, show all courses
            loadCoursesFromDatabase();
        } else {
            // Filter courses by search term
            List<CourseData> filteredCourses = new ArrayList<>();
            for (CourseData course : courses) {
                if (course.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    filteredCourses.add(course);
                }
            }
            
            // Update displayed courses
            courses = filteredCourses;
        }
        
        // Update the UI
        updateCourseGrid();
    }
    
    /**
     * Shows dialog to create a new course
     */
    private void showCreateCourseDialog() {
        // If not logged in, show warning
        if (!isLoggedIn) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Login Required");
            alert.setHeaderText("Login Required");
            alert.setContentText("You need to be logged in as an instructor to create courses. Please log in first.");
            alert.showAndWait();
            return;
        }
        
        // Create the custom dialog
        Dialog<CourseData> dialog = new Dialog<>();
        dialog.setTitle("Create New Course");
        dialog.setHeaderText("Enter course details");
        
        // Set the button types
        ButtonType createButtonType = new ButtonType("Create Course", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Course Name");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Course Description");
        descriptionArea.setPrefRowCount(3);
        
        // Create a color picker
        ColorPicker colorPicker = new ColorPicker(Color.web("#3498db"));
        
        // Populate the grid
        grid.add(new Label("Course Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Course Color:"), 0, 2);
        grid.add(colorPicker, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        nameField.requestFocus();
        
        // Convert the result to a Course when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                // Basic validation
                if (nameField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Course name is required");
                    alert.setContentText("Please enter a name for your course.");
                    alert.showAndWait();
                    return null;
                }
                
                // Convert color to hex string
                String colorHex = String.format("#%02X%02X%02X",
                    (int)(colorPicker.getValue().getRed() * 255),
                    (int)(colorPicker.getValue().getGreen() * 255),
                    (int)(colorPicker.getValue().getBlue() * 255));
                
                // Create and return a new CourseData object
                return new CourseData(
                    0, // Temporary ID, will be replaced by database
                    nameField.getText().trim(),
                    descriptionArea.getText().trim(),
                    colorHex,
                    "Active",
                    getCategoryFromName(nameField.getText().trim()),
                    0, // No students yet
                    0.0 // No rating yet
                );
            }
            return null;
        });
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(courseData -> {
            if (createCourseInDatabase(courseData)) {
                // Reload courses and update view
                loadCoursesFromDatabase();
                updateCourseGrid();
            }
        });
    }
    
    /**
     * Create a new course in the database
     */
    private boolean createCourseInDatabase(CourseData courseData) {
        if (currentUserId <= 0) {
            System.out.println("No instructor user ID available to create course");
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return false;
            
            String query = "INSERT INTO Courses (courseName, description, createdBy, courseColor) VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, courseData.getName());
                pstmt.setString(2, courseData.getDescription());
                pstmt.setInt(3, currentUserId);
                pstmt.setString(4, courseData.getColor());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    System.out.println("Course created successfully: " + courseData.getName());
                    return true;
                } else {
                    System.out.println("Failed to create course: No rows affected");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating course: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to create course");
            alert.setContentText("Database error: " + e.getMessage());
            alert.showAndWait();
            
            return false;
        }
    }
    
    /**
     * Update the course grid with current courses
     */
    private void updateCourseGrid() {
        if (courseGrid != null) {
            courseGrid.getChildren().clear();
            
            if (courses.isEmpty()) {
                Label noCourses = new Label("No courses found. Click 'Create New Course' to get started.");
                noCourses.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-padding: 30; " +
                    "-fx-background-radius: 8; " +
                    "-fx-text-fill: #7f8c8d; " +
                    "-fx-font-size: 16px;"
                );
                courseGrid.getChildren().add(noCourses);
            } else {
                for (CourseData course : courses) {
                    courseGrid.getChildren().add(createCourseCard(
                        course.getId(),
                        course.getName(),
                        course.getDescription(),
                        course.getCategory(),
                        course.getStatus(),
                        course.getStudentCount(),
                        course.getRating(),
                        course.getColor()
                    ));
                }
            }
        }
    }
    
    /**
     * Create course grid from database data
     */
    private FlowPane createCourseGrid() {
        FlowPane grid = new FlowPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10));
        
        // Create a course card for each course in the database
        if (courses.isEmpty()) {
            // Display a message if no courses found
            Label noCourses = new Label("No courses found. Click 'Create New Course' to get started.");
            noCourses.setStyle(
                "-fx-background-color: white; " +
                "-fx-padding: 30; " +
                "-fx-background-radius: 8; " +
                "-fx-text-fill: #7f8c8d; " +
                "-fx-font-size: 16px;"
            );
            grid.getChildren().add(noCourses);
        } else {
            // Add cards for each course
            for (CourseData course : courses) {
                grid.getChildren().add(createCourseCard(
                    course.getId(),
                    course.getName(),
                    course.getDescription(),
                    course.getCategory(),
                    course.getStatus(),
                    course.getStudentCount(),
                    course.getRating(),
                    course.getColor()
                ));
            }
        }
        
        return grid;
    }
    
    /**
     * Create a card for an individual course
     */
    private VBox createCourseCard(int courseId, String title, String description, String category, 
                                 String status, int students, double rating, String colorHex) {
        VBox card = new VBox(0);
        card.setPadding(new Insets(0, 0, 0, 0));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2);"
        );
        card.setPrefWidth(300);
        
        // Parse color or use default
        Color cardColor;
        try {
            cardColor = Color.web(colorHex);
        } catch (Exception e) {
            cardColor = Color.valueOf("#3498db");
        }
        
        // Course image placeholder
        Rectangle imagePlaceholder = new Rectangle(300, 120);
        imagePlaceholder.setArcWidth(8);
        imagePlaceholder.setArcHeight(8);
        imagePlaceholder.setFill(cardColor);
        
        // Status badge
        Label statusLabel = new Label(status);
        statusLabel.setPadding(new Insets(3, 8, 3, 8));
        statusLabel.setStyle(
            "-fx-background-color: white; " +
            "-fx-text-fill: " + colorHex + "; " +
            "-fx-background-radius: 4; " +
            "-fx-font-weight: bold;"
        );
        
        // Course ID label for reference
        Label idLabel = new Label("ID: " + courseId);
        idLabel.setPadding(new Insets(3, 8, 3, 8));
        idLabel.setStyle(
            "-fx-background-color: rgba(255,255,255,0.7); " +
            "-fx-text-fill: #333333; " +
            "-fx-background-radius: 4; " +
            "-fx-font-size: 10px;"
        );
        
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().addAll(imagePlaceholder, statusLabel, idLabel);
        StackPane.setAlignment(statusLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(statusLabel, new Insets(10, 10, 0, 0));
        StackPane.setAlignment(idLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(idLabel, new Insets(0, 0, 10, 10));
        
        // Course details
        VBox details = new VBox(10);
        details.setPadding(new Insets(15));
        
        Label courseTitle = new Label(title);
        courseTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");
        courseTitle.setWrapText(true);
        
        // Description label
        Label descLabel = new Label(description != null && !description.isEmpty() ? 
                                   description : "No description available");
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40); // Limit height
        
        Label categoryLabel = new Label("Category: " + category);
        categoryLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        
        Label studentsLabel = new Label(students + " Students");
        studentsLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        // Create rating display
        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        
        if (students > 0) {
            Label ratingLabel = new Label(String.format("%.1f", rating));
            ratingLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            
            Label starsLabel = new Label("★★★★★");
            starsLabel.setStyle("-fx-text-fill: #f39c12;");
            
            ratingBox.getChildren().addAll(ratingLabel, starsLabel);
        } else {
            Label noRatingLabel = new Label("No ratings yet");
            noRatingLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            ratingBox.getChildren().add(noRatingLabel);
        }
        
        statsRow.getChildren().addAll(studentsLabel, ratingBox);
        
        // Action buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4;");
        editBtn.setPrefWidth(80);
        
        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-background-radius: 4;");
        viewBtn.setPrefWidth(80);
        
        Button statsBtn = new Button("Stats");
        statsBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 4;");
        statsBtn.setPrefWidth(80);
        
        // Button actions
        editBtn.setOnAction(e -> editCourse(courseId, title));
        viewBtn.setOnAction(e -> viewCourse(courseId, title));
        statsBtn.setOnAction(e -> viewCourseStats(courseId, title, students));
        
        buttonsBox.getChildren().addAll(editBtn, viewBtn, statsBtn);
        
        details.getChildren().addAll(courseTitle, descLabel, categoryLabel, statsRow, buttonsBox);
        card.getChildren().addAll(imageContainer, details);
        
        return card;
    }
    
    /**
     * Edit course action
     */
    private void editCourse(int courseId, String title) {
        if (!isLoggedIn) {
            showLoginRequiredAlert();
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Course");
        alert.setHeaderText("Edit Course: " + title);
        alert.setContentText("This would open a form to edit course ID: " + courseId);
        alert.showAndWait();
    }
    
    /**
     * View course action
     */
    private void viewCourse(int courseId, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("View Course");
        alert.setHeaderText("View Course: " + title);
        alert.setContentText("This would open a detailed view for course ID: " + courseId);
        alert.showAndWait();
    }
    
    /**
     * View course statistics action
     */
    private void viewCourseStats(int courseId, String title, int students) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Course Statistics");
        alert.setHeaderText("Statistics for: " + title);
        alert.setContentText("This would show detailed statistics for course ID: " + 
                            courseId + " with " + students + " students.");
        alert.showAndWait();
    }
    
    /**
     * Show login required alert
     */
    private void showLoginRequiredAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Login Required");
        alert.setHeaderText("Login Required");
        alert.setContentText("You need to be logged in as an instructor to perform this action.");
        alert.showAndWait();
    }
    
    /**
     * Try to determine a category based on course name
     */
    private String getCategoryFromName(String courseName) {
        courseName = courseName.toLowerCase();
        
        if (courseName.contains("python") || courseName.contains("java") || 
            courseName.contains("programming") || courseName.contains("code")) {
            return "Programming";
        } else if (courseName.contains("data") || courseName.contains("analytics") || 
                  courseName.contains("statistics") || courseName.contains("machine")) {
            return "Data Science";
        } else if (courseName.contains("web") || courseName.contains("html") || 
                  courseName.contains("css") || courseName.contains("javascript")) {
            return "Web";
        } else if (courseName.contains("math") || courseName.contains("calculus") || 
                  courseName.contains("algebra")) {
            return "Mathematics";
        } else if (courseName.contains("history") || courseName.contains("world") || 
                  courseName.contains("ancient")) {
            return "History";
        } else if (courseName.contains("science") || courseName.contains("physics") || 
                  courseName.contains("chemistry") || courseName.contains("biology")) {
            return "Science";
        } else if (courseName.contains("literature") || courseName.contains("writing") || 
                  courseName.contains("english")) {
            return "Literature";
        }
        
        return "General";
    }
    
    /**
     * Generate a random color
     */
    private String getRandomColor() {
        String[] colors = {
            "#3498db", "#2ecc71", "#e74c3c", "#f39c12", 
            "#9b59b6", "#1abc9c", "#34495e", "#e67e22"
        };
        int randomIndex = (int) (Math.random() * colors.length);
        return colors[randomIndex];
    }
    
    /**
     * Course data model class
     */
    private static class CourseData {
        private final int id;
        private final String name;
        private final String description;
        private final String color;
        private final String status;
        private final String category;
        private final int studentCount;
        private final double rating;
        
        public CourseData(int id, String name, String description, String color, 
                         String status, String category, int studentCount, double rating) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.color = color;
            this.status = status;
            this.category = category;
            this.studentCount = studentCount;
            this.rating = rating;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getColor() { return color; }
        public String getStatus() { return status; }
        public String getCategory() { return category; }
        public int getStudentCount() { return studentCount; }
        public double getRating() { return rating; }
    }
}