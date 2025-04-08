package controllers_Instructors;

import java.sql.*;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import dao.DatabaseConnection;

/**
 * Students page for viewing and managing course students
 */
public class StudentsPage {
    
    /**
     * Calculate letter grade from progress percentage
     */
    private static String calculateGrade(double progress) {
        if (progress >= 0.97) return "A+";
        if (progress >= 0.93) return "A";
        if (progress >= 0.90) return "A-";
        if (progress >= 0.87) return "B+";
        if (progress >= 0.83) return "B";
        if (progress >= 0.80) return "B-";
        if (progress >= 0.77) return "C+";
        if (progress >= 0.73) return "C";
        if (progress >= 0.70) return "C-";
        if (progress >= 0.67) return "D+";
        if (progress >= 0.63) return "D";
        if (progress >= 0.60) return "D-";
        return "F";
    }
    
    /**
     * Class to represent student data from the database
     */
    public static class StudentData {
        private final StringProperty name;
        private final StringProperty email;
        private final StringProperty course;
        private final DoubleProperty progress;
        private final StringProperty grade;
        private final StringProperty lastActive;
        private final IntegerProperty studentId;
        private final IntegerProperty userId;
        
        public StudentData(int studentId, int userId, String name, String email, String course, 
                          double progress) {
            this.studentId = new SimpleIntegerProperty(studentId);
            this.userId = new SimpleIntegerProperty(userId);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.course = new SimpleStringProperty(course != null ? course : "Not Enrolled");
            this.progress = new SimpleDoubleProperty(progress);
            this.grade = new SimpleStringProperty(StudentsPage.calculateGrade(progress));
            this.lastActive = new SimpleStringProperty(""); // We'll set this separately if available
        }
        
        // Getters and property methods
        public int getStudentId() { return studentId.get(); }
        public IntegerProperty studentIdProperty() { return studentId; }
        
        public int getUserId() { return userId.get(); }
        public IntegerProperty userIdProperty() { return userId; }
        
        public String getName() { return name.get(); }
        public StringProperty nameProperty() { return name; }
        
        public String getEmail() { return email.get(); }
        public StringProperty emailProperty() { return email; }
        
        public String getCourse() { return course.get(); }
        public StringProperty courseProperty() { return course; }
        
        public double getProgressValue() { return progress.get(); }
        public DoubleProperty progressProperty() { return progress; }
        
        public String getGrade() { return grade.get(); }
        public StringProperty gradeProperty() { return grade; }
        
        public String getLastActive() { return lastActive.get(); }
        public StringProperty lastActiveProperty() { return lastActive; }
        
        public void setLastActive(String lastActive) { this.lastActive.set(lastActive); }
    }
    
    /**
     * Data Access Object for student operations
     */
    public static class StudentDAO {
        
        /**
         * Get all students from the database
         */
        public List<StudentData> getAllStudents() {
            List<StudentData> students = new ArrayList<>();
            
            String query = "SELECT s.studentID, s.userID, u.username, u.email, c.courseName, " +
                          "IFNULL(e.completionPercentage, 0) as completionPercentage " +
                          "FROM Students s " +
                          "JOIN Users u ON s.userID = u.userID " +
                          "LEFT JOIN Enrollments e ON s.studentID = e.studentID " +
                          "LEFT JOIN Courses c ON e.courseID = c.courseID " +
                          "ORDER BY u.username";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    int studentId = rs.getInt("studentID");
                    int userId = rs.getInt("userID");
                    String name = rs.getString("username");
                    String email = rs.getString("email");
                    String course = rs.getString("courseName");
                    double progress = rs.getDouble("completionPercentage") / 100.0; // Convert to 0-1 scale
                    
                    StudentData student = new StudentData(
                        studentId, userId, name, email, course, progress);
                    
                    // Try to get last activity for this student
                    student.setLastActive(getLastActivity(studentId, conn));
                    
                    students.add(student);
                }
                
            } catch (SQLException e) {
                System.err.println("Error retrieving students: " + e.getMessage());
            }
            
            return students;
        }
        
        /**
         * Get last activity date for a student
         */
        private String getLastActivity(int studentId, Connection conn) {
            String query = "SELECT MAX(activityDate) as lastActivity FROM Activities WHERE studentID = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, studentId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getTimestamp("lastActivity") != null) {
                        return formatLastActive(rs.getTimestamp("lastActivity"));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving last activity: " + e.getMessage());
            }
            
            return "No activity";
        }
        
        /**
         * Get students enrolled in a specific course
         */
        public List<StudentData> getStudentsByCourse(String courseName) {
            List<StudentData> students = new ArrayList<>();
            
            String query = "SELECT s.studentID, s.userID, u.username, u.email, c.courseName, " +
                          "e.completionPercentage " +
                          "FROM Students s " +
                          "JOIN Users u ON s.userID = u.userID " +
                          "JOIN Enrollments e ON s.studentID = e.studentID " +
                          "JOIN Courses c ON e.courseID = c.courseID " +
                          "WHERE c.courseName = ? " +
                          "ORDER BY u.username";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, courseName);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int studentId = rs.getInt("studentID");
                        int userId = rs.getInt("userID");
                        String name = rs.getString("username");
                        String email = rs.getString("email");
                        String course = rs.getString("courseName");
                        double progress = rs.getDouble("completionPercentage") / 100.0;
                        
                        StudentData student = new StudentData(
                            studentId, userId, name, email, course, progress);
                        
                        student.setLastActive(getLastActivity(studentId, conn));
                        students.add(student);
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Error retrieving students by course: " + e.getMessage());
            }
            
            return students;
        }
        
        /**
         * Get students filtered by progress status
         */
        public List<StudentData> getStudentsByProgress(String progressStatus) {
            List<StudentData> allStudents = getAllStudents();
            List<StudentData> filteredStudents = new ArrayList<>();
            
            for (StudentData student : allStudents) {
                double progress = student.getProgressValue();
                
                boolean matches = false;
                switch (progressStatus) {
                    case "Not Started":
                        matches = progress < 0.1; // Less than 10%
                        break;
                    case "In Progress":
                        matches = progress >= 0.1 && progress < 0.9; // 10% to 90%
                        break;
                    case "Completed":
                        matches = progress >= 0.9; // 90% or more
                        break;
                    default:
                        matches = true; // "All Progress"
                }
                
                if (matches) {
                    filteredStudents.add(student);
                }
            }
            
            return filteredStudents;
        }
        
        /**
         * Search students by name or email
         */
        public List<StudentData> searchStudents(String searchTerm) {
            List<StudentData> students = new ArrayList<>();
            
            String query = "SELECT s.studentID, s.userID, u.username, u.email, c.courseName, " +
                          "IFNULL(e.completionPercentage, 0) as completionPercentage " +
                          "FROM Students s " +
                          "JOIN Users u ON s.userID = u.userID " +
                          "LEFT JOIN Enrollments e ON s.studentID = e.studentID " +
                          "LEFT JOIN Courses c ON e.courseID = c.courseID " +
                          "WHERE u.username LIKE ? OR u.email LIKE ? " +
                          "ORDER BY u.username";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, "%" + searchTerm + "%");
                stmt.setString(2, "%" + searchTerm + "%");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int studentId = rs.getInt("studentID");
                        int userId = rs.getInt("userID");
                        String name = rs.getString("username");
                        String email = rs.getString("email");
                        String course = rs.getString("courseName");
                        double progress = rs.getDouble("completionPercentage") / 100.0;
                        
                        StudentData student = new StudentData(
                            studentId, userId, name, email, course, progress);
                        
                        student.setLastActive(getLastActivity(studentId, conn));
                        students.add(student);
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Error searching students: " + e.getMessage());
            }
            
            return students;
        }
        
        /**
         * Get all available courses
         */
        public List<String> getAllCourseNames() {
            List<String> courseNames = new ArrayList<>();
            courseNames.add("All Courses"); // Default option
            
            String query = "SELECT courseName FROM Courses ORDER BY courseName";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    courseNames.add(rs.getString("courseName"));
                }
                
            } catch (SQLException e) {
                System.err.println("Error retrieving course names: " + e.getMessage());
            }
            
            return courseNames;
        }
        
        /**
         * Get summary statistics for students
         */
        public Map<String, Object> getStudentStats() {
            Map<String, Object> stats = new HashMap<>();
            
            // Set default values
            stats.put("totalStudents", 0);
            stats.put("activeStudents", 0);
            stats.put("activePercentage", 0.0);
            stats.put("avgProgress", 0.0);
            stats.put("avgGrade", "N/A");
            stats.put("avgPercentage", 0.0);
            
            // Count total students
            String countQuery = "SELECT COUNT(*) FROM Students";
            
            // Count active students (activity in the last 7 days)
            String activeQuery = "SELECT COUNT(DISTINCT studentID) FROM Activities " +
                               "WHERE activityDate >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)";
            
            // Get average progress
            String progressQuery = "SELECT AVG(completionPercentage) FROM Enrollments";
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Total students
                try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.put("totalStudents", rs.getInt(1));
                    }
                }
                
                // Active students
                try (PreparedStatement stmt = conn.prepareStatement(activeQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int activeCount = rs.getInt(1);
                        stats.put("activeStudents", activeCount);
                        
                        int totalStudents = (int) stats.get("totalStudents");
                        if (totalStudents > 0) {
                            double percentage = (double) activeCount / totalStudents * 100.0;
                            stats.put("activePercentage", percentage);
                        }
                    }
                }
                
                // Average progress
                try (PreparedStatement stmt = conn.prepareStatement(progressQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double avgProgress = rs.getDouble(1);
                        stats.put("avgProgress", avgProgress);
                        
                        // Calculate average grade based on progress
                        String avgGrade = StudentsPage.calculateGrade(avgProgress / 100.0);
                        stats.put("avgGrade", avgGrade);
                        stats.put("avgPercentage", avgProgress);
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Error retrieving student statistics: " + e.getMessage());
            }
            
            return stats;
        }
        
        /**
         * Format the last active date
         */
        private String formatLastActive(Timestamp timestamp) {
            if (timestamp == null) {
                return "Never";
            }
            
            long now = System.currentTimeMillis();
            long diff = now - timestamp.getTime();
            
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            
            if (days > 30) {
                return "Over a month ago";
            } else if (days > 7) {
                return days / 7 + " weeks ago";
            } else if (days > 0) {
                return days + (days == 1 ? " day ago" : " days ago");
            } else if (hours > 0) {
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (minutes > 0) {
                return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
            } else {
                return "Just now";
            }
        }
    }
    
    private StudentDAO studentDAO = new StudentDAO();
    private ObservableList<StudentData> students;
    private TableView<StudentData> studentsTable;
    
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Students");
        title.getStyleClass().add("page-title");
        
        // Filter and search section
        HBox filterContainer = new HBox(10);
        filterContainer.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search students...");
        searchField.setPrefWidth(300);
        
        // Get course names from database
        List<String> courseNames = studentDAO.getAllCourseNames();
        ComboBox<String> courseFilter = new ComboBox<>(FXCollections.observableArrayList(courseNames));
        courseFilter.setValue("All Courses");
        
        ComboBox<String> progressFilter = new ComboBox<>();
        progressFilter.getItems().addAll("All Progress", "Not Started", "In Progress", "Completed");
        progressFilter.setValue("All Progress");
        
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("primary-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button exportBtn = new Button("Export List");
        exportBtn.getStyleClass().add("secondary-button");
        
        Button messageAllBtn = new Button("Message All");
        messageAllBtn.getStyleClass().add("primary-button");
        
        filterContainer.getChildren().addAll(
            searchField, courseFilter, progressFilter, searchBtn, spacer, exportBtn, messageAllBtn);
        
        // Load students from database
        students = FXCollections.observableArrayList(studentDAO.getAllStudents());
        
        // Students table
        studentsTable = createStudentsTable();
        studentsTable.setItems(students);
        VBox.setVgrow(studentsTable, Priority.ALWAYS);
        
        // Search button action
        searchBtn.setOnAction(e -> {
            String searchTerm = searchField.getText().trim();
            String selectedCourse = courseFilter.getValue();
            String selectedProgress = progressFilter.getValue();
            
            // Apply filters
            applyFilters(searchTerm, selectedCourse, selectedProgress);
        });
        
        // Message all button
        messageAllBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message Students");
            alert.setHeaderText("Send Message");
            alert.setContentText("This would open a message composer to send a message to all " + 
                               students.size() + " students in the current view.");
            alert.showAndWait();
        });
        
        // Export button
        exportBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export List");
            alert.setHeaderText("Export Student List");
            alert.setContentText("This would export the current list of " + 
                               students.size() + " students to CSV or Excel.");
            alert.showAndWait();
        });
        
        // Get statistics
        Map<String, Object> stats = studentDAO.getStudentStats();
        
        // Summary statistics
        HBox summaryStats = new HBox(20);
        summaryStats.setPadding(new Insets(15));
        summaryStats.setAlignment(Pos.CENTER);
        
        VBox totalStudents = createStatBox("Total Students", 
                                          String.valueOf(stats.get("totalStudents")), "");
        
        int activeCount = (int) stats.get("activeStudents");
        double activePercentage = (double) stats.get("activePercentage");
        VBox activeStudents = createStatBox("Active This Week", 
                                          String.valueOf(activeCount), 
                                          String.format("%.1f%%", activePercentage));
        
        double avgProgress = (double) stats.get("avgProgress");
        VBox avgProgressBox = createStatBox("Average Progress", 
                                          String.format("%.1f%%", avgProgress), "");
        
        String avgGrade = (String) stats.get("avgGrade");
        double avgPercentage = (double) stats.get("avgPercentage");
        VBox avgGradeBox = createStatBox("Average Grade", 
                                        avgGrade, 
                                        String.format("%.1f%%", avgPercentage));
        
        summaryStats.getChildren().addAll(totalStudents, activeStudents, avgProgressBox, avgGradeBox);
        
        view.getChildren().addAll(title, filterContainer, studentsTable, summaryStats);
        return view;
    }
    
    /**
     * Apply filters to the student list
     */
    private void applyFilters(String searchTerm, String course, String progress) {
        List<StudentData> filteredStudents;
        
        if (!searchTerm.isEmpty()) {
            // Search by name or email
            filteredStudents = studentDAO.searchStudents(searchTerm);
        } else if (!"All Courses".equals(course)) {
            // Filter by course
            filteredStudents = studentDAO.getStudentsByCourse(course);
        } else {
            // Get all students
            filteredStudents = studentDAO.getAllStudents();
        }
        
        // Apply progress filter if needed
        if (!"All Progress".equals(progress)) {
            List<StudentData> progressFiltered = new ArrayList<>();
            
            for (StudentData student : filteredStudents) {
                double progressValue = student.getProgressValue();
                
                boolean matches = false;
                switch (progress) {
                    case "Not Started":
                        matches = progressValue < 0.1; // Less than 10%
                        break;
                    case "In Progress":
                        matches = progressValue >= 0.1 && progressValue < 0.9; // 10% to 90%
                        break;
                    case "Completed":
                        matches = progressValue >= 0.9; // 90% or more
                        break;
                }
                
                if (matches) {
                    progressFiltered.add(student);
                }
            }
            
            filteredStudents = progressFiltered;
        }
        
        // Update table data
        students.clear();
        students.addAll(filteredStudents);
    }
    
    private TableView<StudentData> createStudentsTable() {
        TableView<StudentData> table = new TableView<>();
        
        // Set black header style for all columns
        String headerStyle = "-fx-text-fill: black; -fx-font-weight: bold;";
        
        // Create columns
        TableColumn<StudentData, String> nameColumn = new TableColumn<>("Student Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameColumn.setPrefWidth(200);
        nameColumn.setStyle(headerStyle);
        
        TableColumn<StudentData, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        emailColumn.setPrefWidth(200);
        emailColumn.setStyle(headerStyle);
        
        TableColumn<StudentData, String> courseColumn = new TableColumn<>("Course");
        courseColumn.setCellValueFactory(cellData -> cellData.getValue().courseProperty());
        courseColumn.setPrefWidth(180);
        courseColumn.setStyle(headerStyle);
        
        TableColumn<StudentData, Double> progressColumn = new TableColumn<>("Progress");
        progressColumn.setCellFactory(col -> new TableCell<StudentData, Double>() {
            private final ProgressBar progressBar = new ProgressBar();
            private final Label progressLabel = new Label();
            
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    StudentData data = getTableView().getItems().get(getIndex());
                    double progress = data.getProgressValue();
                    progressBar.setProgress(progress);
                    progressBar.setPrefWidth(80);
                    progressLabel.setText(String.format("%.0f%%", progress * 100));
                    progressLabel.setPadding(new Insets(0, 0, 0, 5));
                    
                    HBox container = new HBox(5);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.getChildren().addAll(progressBar, progressLabel);
                    setGraphic(container);
                }
            }
        });
        progressColumn.setStyle(headerStyle);
        
        TableColumn<StudentData, String> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellValueFactory(cellData -> cellData.getValue().gradeProperty());
        gradeColumn.setStyle(headerStyle);
        
        TableColumn<StudentData, String> lastActiveColumn = new TableColumn<>("Last Active");
        lastActiveColumn.setCellValueFactory(cellData -> cellData.getValue().lastActiveProperty());
        lastActiveColumn.setStyle(headerStyle);
        
        TableColumn<StudentData, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(col -> new TableCell<StudentData, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button messageBtn = new Button("Message");
            
            {
                viewBtn.getStyleClass().add("view-button");
                messageBtn.getStyleClass().add("message-button");
                
                viewBtn.setOnAction(e -> {
                    StudentData data = getTableView().getItems().get(getIndex());
                    showStudentDetailsDialog(data);
                });
                
                messageBtn.setOnAction(e -> {
                    StudentData data = getTableView().getItems().get(getIndex());
                    showSendMessageDialog(data);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(viewBtn, messageBtn);
                    setGraphic(buttons);
                }
            }
        });
        actionsColumn.setStyle(headerStyle);
        
        table.getColumns().addAll(
            nameColumn, emailColumn, courseColumn, progressColumn, 
            gradeColumn, lastActiveColumn, actionsColumn);
        
        // Add a placeholder for empty table
        Label placeholderLabel = new Label("No students found");
        placeholderLabel.setStyle("-fx-text-fill: black;");
        table.setPlaceholder(placeholderLabel);
        
        return table;
    }
    
    /**
     * Show student details dialog
     */
    private void showStudentDetailsDialog(StudentData student) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Student Details");
        alert.setHeaderText(student.getName());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10, 0, 0, 0));
        
        content.getChildren().addAll(
            new Label("Email: " + student.getEmail()),
            new Label("Student ID: " + student.getStudentId()),
            new Label("Course: " + student.getCourse()),
            new Label("Progress: " + String.format("%.1f%%", student.getProgressValue() * 100)),
            new Label("Grade: " + student.getGrade()),
            new Label("Last Active: " + student.getLastActive())
        );
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    
    /**
     * Show send message dialog
     */
    private void showSendMessageDialog(StudentData student) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Send Message");
        alert.setHeaderText("Send Message to " + student.getName());
        alert.setContentText("This would open a message composer to send a message to " + student.getEmail());
        alert.showAndWait();
    }
    
    private VBox createStatBox(String title, String value, String subtitle) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("stat-box");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        
        box.getChildren().addAll(titleLabel, valueLabel);
        
        if (!subtitle.isEmpty()) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.getStyleClass().add("stat-subtitle");
            box.getChildren().add(subtitleLabel);
        }
        
        return box;
    }
}