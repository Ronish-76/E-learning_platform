package controllers_Instructors;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import dao.DatabaseConnection;

public class AssignmentPage {
    public static class Assignment {
        private final IntegerProperty assignmentId; 
        private final IntegerProperty courseId;
        private final StringProperty title;
        private final StringProperty description;
        private final ObjectProperty<Date> dueDate;
        private final StringProperty points;
        private final StringProperty priority;
        private final ObjectProperty<Date> createdAt;
        
        /**
         * Constructs a new Assignment with all fields
         */
        public Assignment(int assignmentId, int courseId, String title, String description, 
                         Date dueDate, String points, String priority, Date createdAt) {
            this.assignmentId = new SimpleIntegerProperty(assignmentId);
            this.courseId = new SimpleIntegerProperty(courseId);
            this.title = new SimpleStringProperty(title);
            this.description = new SimpleStringProperty(description);
            this.dueDate = new SimpleObjectProperty<>(dueDate);
            this.points = new SimpleStringProperty(points);
            this.priority = new SimpleStringProperty(priority);
            this.createdAt = new SimpleObjectProperty<>(createdAt);
        }
        
        // Getters and setters
        public int getAssignmentId() { return assignmentId.get(); }
        public IntegerProperty assignmentIdProperty() { return assignmentId; }
        public void setAssignmentId(int id) { this.assignmentId.set(id); }
        
        public int getCourseId() { return courseId.get(); }
        public IntegerProperty courseIdProperty() { return courseId; }
        public void setCourseId(int id) { this.courseId.set(id); }
        
        public String getTitle() { return title.get(); }
        public StringProperty titleProperty() { return title; }
        public void setTitle(String title) { this.title.set(title); }
        
        public String getDescription() { return description.get(); }
        public StringProperty descriptionProperty() { return description; }
        public void setDescription(String description) { this.description.set(description); }
        
        public Date getDueDate() { return dueDate.get(); }
        public ObjectProperty<Date> dueDateProperty() { return dueDate; }
        public void setDueDate(Date dueDate) { this.dueDate.set(dueDate); }
        
        public String getPoints() { return points.get(); }
        public StringProperty pointsProperty() { return points; }
        public void setPoints(String points) { this.points.set(points); }
        
        public String getPriority() { return priority.get(); }
        public StringProperty priorityProperty() { return priority; }
        public void setPriority(String priority) { this.priority.set(priority); }
        
        public Date getCreatedAt() { return createdAt.get(); }
        public ObjectProperty<Date> createdAtProperty() { return createdAt; }
        public void setCreatedAt(Date createdAt) { this.createdAt.set(createdAt); }
    }
    
    /**
     * Class representing a student assignment submission
     */
    public static class StudentSubmission {
        private final IntegerProperty studentId;
        private final StringProperty studentName;
        private final StringProperty status;
        private final StringProperty content;
        private final ObjectProperty<Date> lastUpdated;
        
        public StudentSubmission(int studentId, String studentName, String status, 
                                String content, Date lastUpdated) {
            this.studentId = new SimpleIntegerProperty(studentId);
            this.studentName = new SimpleStringProperty(studentName);
            this.status = new SimpleStringProperty(status);
            this.content = new SimpleStringProperty(content);
            this.lastUpdated = new SimpleObjectProperty<>(lastUpdated);
        }
        
        public int getStudentId() { return studentId.get(); }
        public IntegerProperty studentIdProperty() { return studentId; }
        
        public String getStudentName() { return studentName.get(); }
        public StringProperty studentNameProperty() { return studentName; }
        
        public String getStatus() { return status.get(); }
        public StringProperty statusProperty() { return status; }
        
        public String getContent() { return content.get(); }
        public StringProperty contentProperty() { return content; }
        
        public Date getLastUpdated() { return lastUpdated.get(); }
        public ObjectProperty<Date> lastUpdatedProperty() { return lastUpdated; }
    }
    
    // ==================== ASSIGNMENT DAO CLASS ====================
    /**
     * Data Access Object for Assignment-related database operations
     */
    public static class AssignmentDAO {
        /**
         * Retrieve all assignments from the database
         */
        public List<Assignment> getAllAssignments() {
            List<Assignment> assignments = new ArrayList<>();
            String query = "SELECT * FROM Assignments ORDER BY dueDate";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Assignment assignment = new Assignment(
                        rs.getInt("assignmentID"),
                        rs.getInt("courseID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("dueDate"),
                        rs.getString("points"),
                        rs.getString("priority"),
                        rs.getTimestamp("createdAt")
                    );
                    assignments.add(assignment);
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving assignments: " + e.getMessage());
            }
            return assignments;
        }
        
        /**
         * Delete an assignment from the database
         */
        public boolean deleteAssignment(int assignmentId) {
            String query = "DELETE FROM Assignments WHERE assignmentID = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, assignmentId);
                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            } catch (SQLException e) {
                System.err.println("Error deleting assignment: " + e.getMessage());
                return false;
            }
        }
        
        /**
         * Create a new assignment in the database
         */
        public int createAssignment(Assignment assignment) {
            String query = "INSERT INTO Assignments (courseID, title, description, dueDate, points, priority) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, assignment.getCourseId());
                stmt.setString(2, assignment.getTitle());
                stmt.setString(3, assignment.getDescription());
                stmt.setDate(4, new java.sql.Date(assignment.getDueDate().getTime()));
                stmt.setString(5, assignment.getPoints());
                stmt.setString(6, assignment.getPriority());
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating assignment failed, no rows affected.");
                }
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating assignment failed, no ID obtained.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error creating assignment: " + e.getMessage());
                return -1;
            }
        }
        
        /**
         * Get all courses for the dropdown
         */
        public List<Course> getAllCourses() {
            List<Course> courses = new ArrayList<>();
            String query = "SELECT courseID, courseName FROM Courses";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course(
                        rs.getInt("courseID"),
                        rs.getString("courseName")
                    );
                    courses.add(course);
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving courses: " + e.getMessage());
            }
            return courses;
        }
        
        /**
         * Get course name from course ID
         */
        public String getCourseName(int courseId) {
            String query = "SELECT courseName FROM Courses WHERE courseID = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, courseId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("courseName");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving course name: " + e.getMessage());
            }
            return "Unknown Course";
        }
        
        /**
         * Get all student submissions for an assignment
         */
        public List<StudentSubmission> getAssignmentSubmissions(int assignmentId) {
            List<StudentSubmission> submissions = new ArrayList<>();
            String query = 
                "SELECT s.studentID, u.username as studentName, " +
                "ap.status, ap.content, ap.lastUpdated " +
                "FROM Students s " +
                "JOIN Users u ON s.userID = u.userID " +
                "JOIN Enrollments e ON s.studentID = e.studentID " +
                "JOIN Assignments a ON e.courseID = a.courseID " +
                "LEFT JOIN AssignmentProgress ap ON a.assignmentID = ap.assignmentID AND s.studentID = ap.studentID " +
                "WHERE a.assignmentID = ? " +
                "ORDER BY u.username";
                
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, assignmentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String status = rs.getString("status");
                        if (status == null || status.isEmpty()) {
                            status = "Not started";
                        }
                        
                        StudentSubmission submission = new StudentSubmission(
                            rs.getInt("studentID"),
                            rs.getString("studentName"),
                            status,
                            rs.getString("content"),
                            rs.getTimestamp("lastUpdated")
                        );
                        submissions.add(submission);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving student submissions: " + e.getMessage());
            }
            return submissions;
        }
    }
    
    /**
     * Simple course model for the dropdown
     */
    public static class Course {
        private final int courseId;
        private final String courseName;
        
        public Course(int courseId, String courseName) {
            this.courseId = courseId;
            this.courseName = courseName;
        }
        
        public int getCourseId() {
            return courseId;
        }
        
        public String getCourseName() {
            return courseName;
        }
        
        @Override
        public String toString() {
            return courseName;
        }
    }
    
    // ==================== ASSIGNMENT PAGE IMPLEMENTATION ====================
    private final AssignmentDAO assignmentDAO;
    private ObservableList<Assignment> allAssignments;
    
    public AssignmentPage() {
        this.assignmentDAO = new AssignmentDAO();
    }
    
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Assignments");
        title.getStyleClass().add("page-title");
        
        // Action buttons
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setPadding(new Insets(0, 20, 0, 0));
        
        Button createAssignmentBtn = new Button("+ Create Assignment");
        createAssignmentBtn.getStyleClass().add("success-button");
        createAssignmentBtn.setOnAction(e -> createNewAssignment());
        
        buttonContainer.getChildren().add(createAssignmentBtn);
        
        // Load all assignments
        loadAssignments();
        
        // Create the assignment table
        TableView<Assignment> table = createAssignmentTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        
        view.getChildren().addAll(title, buttonContainer, table);
        return view;
    }
    
    /**
     * Load all assignments from database
     */
    private void loadAssignments() {
        List<Assignment> assignments = assignmentDAO.getAllAssignments();
        allAssignments = FXCollections.observableArrayList(assignments);
    }
    
    /**
     * Open dialog to create a new assignment
     */
    private void createNewAssignment() {
        // Create a new stage for the dialog
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Create New Assignment");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        
        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        // Get courses for dropdown
        List<Course> courses = assignmentDAO.getAllCourses();
        ComboBox<Course> courseComboBox = new ComboBox<>(FXCollections.observableArrayList(courses));
        
        // Form fields
        TextField titleField = new TextField();
        titleField.setPromptText("Assignment Title");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Assignment Description");
        descriptionArea.setPrefRowCount(4);
        
        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(7));
        
        TextField pointsField = new TextField("10");
        pointsField.setPromptText("Points");
        
        ComboBox<String> priorityComboBox = new ComboBox<>(
            FXCollections.observableArrayList("Low", "Medium", "High")
        );
        priorityComboBox.setValue("Medium");
        
        // Add labels and fields to the grid
        int row = 0;
        grid.add(new Label("Course:"), 0, row);
        grid.add(courseComboBox, 1, row);
        row++;
        
        grid.add(new Label("Title:"), 0, row);
        grid.add(titleField, 1, row);
        row++;
        
        grid.add(new Label("Description:"), 0, row);
        grid.add(descriptionArea, 1, row);
        row++;
        
        grid.add(new Label("Due Date:"), 0, row);
        grid.add(dueDatePicker, 1, row);
        row++;
        
        grid.add(new Label("Points:"), 0, row);
        grid.add(pointsField, 1, row);
        row++;
        
        grid.add(new Label("Priority:"), 0, row);
        grid.add(priorityComboBox, 1, row);
        row++;
        
        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, row);
        
        // Save button action
        saveButton.setOnAction(event -> {
            // Validate input
            if (courseComboBox.getValue() == null) {
                showAlert("Validation Error", "Please select a course.");
                return;
            }
            if (titleField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Please enter a title.");
                return;
            }
            if (dueDatePicker.getValue() == null) {
                showAlert("Validation Error", "Please select a due date.");
                return;
            }
            
            try {
                int points = Integer.parseInt(pointsField.getText().trim());
                if (points <= 0) {
                    showAlert("Validation Error", "Points must be a positive number.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Points must be a valid number.");
                return;
            }
            
            // Create new assignment
            Date dueDate = Date.from(dueDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Assignment newAssignment = new Assignment(
                0, // ID will be set by the database
                courseComboBox.getValue().getCourseId(),
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                dueDate,
                pointsField.getText().trim(),
                priorityComboBox.getValue(),
                new Date() // Current date as created date
            );
            
            // Save to database
            int assignmentId = assignmentDAO.createAssignment(newAssignment);
            if (assignmentId > 0) {
                newAssignment.setAssignmentId(assignmentId);
                allAssignments.add(newAssignment);
                dialogStage.close();
            } else {
                showAlert("Database Error", "Failed to create assignment.");
            }
        });
        
        // Cancel button action
        cancelButton.setOnAction(event -> dialogStage.close());
        
        // Create the scene and show the dialog
        Scene scene = new Scene(grid);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Create a table view for assignments
     * 
     * @return TableView containing all assignments
     */
    private TableView<Assignment> createAssignmentTable() {
        TableView<Assignment> table = new TableView<>();
        table.setPadding(new Insets(15));
        
        // Create column header style
        String headerStyle = "-fx-text-fill: black; -fx-font-weight: bold;";
        
        // Create columns
        TableColumn<Assignment, String> titleColumn = new TableColumn<>("Assignment Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(250);
        titleColumn.setStyle(headerStyle);
        
        TableColumn<Assignment, Integer> courseIdColumn = new TableColumn<>("Course ID");
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseIdColumn.setPrefWidth(80);
        courseIdColumn.setStyle(headerStyle);
        
        TableColumn<Assignment, Date> dueDateColumn = new TableColumn<>("Due Date");
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateColumn.setCellFactory(col -> new TableCell<Assignment, Date>() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(date));
                }
            }
        });
        dueDateColumn.setPrefWidth(120);
        dueDateColumn.setStyle(headerStyle);
        
        TableColumn<Assignment, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityColumn.setPrefWidth(80);
        priorityColumn.setStyle(headerStyle);
        
        TableColumn<Assignment, String> pointsColumn = new TableColumn<>("Points");
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));
        pointsColumn.setPrefWidth(60);
        pointsColumn.setStyle(headerStyle);
        
        TableColumn<Assignment, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellFactory(col -> new TableCell<Assignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Assignment assignment = getTableView().getItems().get(getIndex());
                    if (assignment == null || assignment.getDueDate() == null) {
                        setText("");
                        return;
                    }
                    
                    Date today = new Date();
                    if (assignment.getDueDate().before(today)) {
                        setText("Closed");
                        setStyle("-fx-text-fill: red;");
                    } else if (assignment.getDueDate().getTime() - today.getTime() > 7 * 24 * 60 * 60 * 1000) {
                        setText("Upcoming");
                        setStyle("-fx-text-fill: #888888;"); 
                    } else {
                        setText("Open");
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });
        statusColumn.setPrefWidth(80);
        statusColumn.setStyle(headerStyle);
        
        TableColumn<Assignment, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(col -> new TableCell<Assignment, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            
            {
                viewBtn.getStyleClass().add("view-button");
                editBtn.getStyleClass().add("edit-button");
                deleteBtn.getStyleClass().add("delete-button");
                
                // Make buttons smaller
                viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2px 8px;");
                editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2px 8px;");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2px 8px;");
                
                viewBtn.setOnAction(event -> {
                    Assignment data = getTableView().getItems().get(getIndex());
                    // View student submissions
                    viewAssignmentSubmissions(data);
                });
                
                editBtn.setOnAction(event -> {
                    Assignment data = getTableView().getItems().get(getIndex());
                    System.out.println("Edit assignment: " + data.getTitle());
                    // Edit assignment implementation would go here
                });
                
                deleteBtn.setOnAction(event -> {
                    Assignment data = getTableView().getItems().get(getIndex());
                    
                    // Show confirmation dialog
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete Assignment");
                    alert.setHeaderText("Delete Assignment");
                    alert.setContentText("Are you sure you want to delete the assignment: " + data.getTitle() + "?");
                    
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            boolean success = assignmentDAO.deleteAssignment(data.getAssignmentId());
                            if (success) {
                                allAssignments.remove(data);
                            } else {
                                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                errorAlert.setTitle("Error");
                                errorAlert.setHeaderText("Delete Failed");
                                errorAlert.setContentText("Failed to delete the assignment.");
                                errorAlert.showAndWait();
                            }
                        }
                    });
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
        actionsColumn.setPrefWidth(180);
        actionsColumn.setStyle(headerStyle);
        
        table.getColumns().addAll(
            titleColumn, courseIdColumn, dueDateColumn, priorityColumn, pointsColumn, statusColumn, actionsColumn
        );
        
        // Set the data
        table.setItems(allAssignments);
        
        // Add a placeholder message when no assignments are available
        Label placeholderLabel = new Label("No assignments available");
        placeholderLabel.setStyle("-fx-text-fill: black;");
        table.setPlaceholder(placeholderLabel);
        
        return table;
    }
    
    /**
     * View student submissions for an assignment
     */
    private void viewAssignmentSubmissions(Assignment assignment) {
        // Create a new stage for the submissions view
        Stage stage = new Stage();
        stage.setTitle("Student Submissions: " + assignment.getTitle());
        stage.initModality(Modality.APPLICATION_MODAL);
        
        // Main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Header section
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        // Title
        Label titleLabel = new Label(assignment.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Course name
        String courseName = assignmentDAO.getCourseName(assignment.getCourseId());
        Label courseLabel = new Label("Course: " + courseName);
        courseLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        
        // Due date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
        Label dueDateLabel = new Label("Due Date: " + dateFormat.format(assignment.getDueDate()));
        dueDateLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        
        // Description
        Label descriptionHeader = new Label("Description:");
        descriptionHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        TextArea descriptionArea = new TextArea(assignment.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(80);
        
        headerBox.getChildren().addAll(titleLabel, courseLabel, dueDateLabel, descriptionHeader, descriptionArea);
        root.setTop(headerBox);
        
        // Center content - Table of student submissions
        VBox centerBox = new VBox(15);
        
        Label submissionsLabel = new Label("Student Submissions");
        submissionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        // Get student submissions
        List<StudentSubmission> submissions = assignmentDAO.getAssignmentSubmissions(assignment.getAssignmentId());
        TableView<StudentSubmission> submissionsTable = createSubmissionsTable(submissions);
        
        centerBox.getChildren().addAll(submissionsLabel, submissionsTable);
        root.setCenter(centerBox);
        
        // Bottom - Close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> stage.close());
        
        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        bottomBox.getChildren().add(closeButton);
        
        root.setBottom(bottomBox);
        
        // Set scene and show
        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    /**
     * Create a table for student submissions
     */
    private TableView<StudentSubmission> createSubmissionsTable(List<StudentSubmission> submissions) {
        ObservableList<StudentSubmission> data = FXCollections.observableArrayList(submissions);
        TableView<StudentSubmission> table = new TableView<>(data);
        
        // Create column header style
        String headerStyle = "-fx-text-fill: black; -fx-font-weight: bold;";
        
        // Student name column
        TableColumn<StudentSubmission, String> nameColumn = new TableColumn<>("Student Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        nameColumn.setPrefWidth(200);
        nameColumn.setStyle(headerStyle);
        
        // Status column with color indicators
        TableColumn<StudentSubmission, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(120);
        statusColumn.setStyle(headerStyle);
        statusColumn.setCellFactory(col -> new TableCell<StudentSubmission, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(status);
                    if (status.equalsIgnoreCase("Completed")) {
                        setTextFill(Color.GREEN);
                        setStyle("-fx-font-weight: bold;");
                    } else if (status.equalsIgnoreCase("In Progress")) {
                        setTextFill(Color.ORANGE);
                    } else {
                        setTextFill(Color.RED);
                    }
                }
            }
        });
        
        // Last Updated column
        TableColumn<StudentSubmission, Date> dateColumn = new TableColumn<>("Last Updated");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));
        dateColumn.setPrefWidth(150);
        dateColumn.setStyle(headerStyle);
        dateColumn.setCellFactory(col -> new TableCell<StudentSubmission, Date>() {
            private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
            
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(date));
                }
            }
        });
        
        // Actions column with view button
        TableColumn<StudentSubmission, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(100);
        actionsColumn.setStyle(headerStyle);
        actionsColumn.setCellFactory(col -> new TableCell<StudentSubmission, Void>() {
            private final Button viewBtn = new Button("View Submission");
            
            {
                viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2px 8px;");
                viewBtn.setOnAction(event -> {
                    StudentSubmission submission = getTableView().getItems().get(getIndex());
                    viewStudentSubmission(submission);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    StudentSubmission submission = getTableView().getItems().get(getIndex());
                    if (submission.getStatus().equalsIgnoreCase("Not started")) {
                        setGraphic(new Label("No submission"));
                    } else {
                        setGraphic(viewBtn);
                    }
                }
            }
        });
        
        // Set up table
        table.getColumns().addAll(nameColumn, statusColumn, dateColumn, actionsColumn);
        
        // Add a placeholder message when no submissions are available
        Label placeholderLabel = new Label("No student submissions found");
        placeholderLabel.setStyle("-fx-text-fill: black;");
        table.setPlaceholder(placeholderLabel);
        
        // Allow table to grow with window
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return table;
    }
    
    /**
     * View an individual student's submission
     */
    private void viewStudentSubmission(StudentSubmission submission) {
        // Create a dialog to show the submission
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Student Submission");
        dialog.setHeaderText("Submission by " + submission.getStudentName());
        
        // Set the content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(700);
        content.setPrefHeight(500);
        
        // Status with color indicator
        HBox statusBox = new HBox(10);
        Label statusLabel = new Label("Status:");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Label statusValue = new Label(submission.getStatus());
        statusValue.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        if (submission.getStatus().equalsIgnoreCase("Completed")) {
            statusValue.setTextFill(Color.GREEN);
        } else if (submission.getStatus().equalsIgnoreCase("In Progress")) {
            statusValue.setTextFill(Color.ORANGE);
        } else {
            statusValue.setTextFill(Color.RED);
        }
        
        statusBox.getChildren().addAll(statusLabel, statusValue);
        
        // Last updated date
        HBox dateBox = new HBox(10);
        Label dateLabel = new Label("Last Updated:");
        dateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
        Label dateValue = new Label(submission.getLastUpdated() != null ? 
                                   dateFormat.format(submission.getLastUpdated()) : "Never");
        dateValue.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        dateBox.getChildren().addAll(dateLabel, dateValue);
        
        // Submission content header
        Label contentHeader = new Label("Submission Content:");
        contentHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Create a bordered text area for the submission content
        TextArea contentArea = new TextArea(submission.getContent() != null && !submission.getContent().isEmpty() ? 
                                           submission.getContent() : "No content submitted.");
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setPrefHeight(300);
        contentArea.setStyle("-fx-control-inner-background: #f9f9f9; -fx-border-color: #ddd;");
        
        // Grading section - could be expanded in a real app
        Label gradingHeader = new Label("Instructor Feedback:");
        gradingHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("Enter feedback for the student here...");
        feedbackArea.setWrapText(true);
        feedbackArea.setPrefHeight(100);
        
        HBox gradeBox = new HBox(10);
        gradeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label gradeLabel = new Label("Grade:");
        gradeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        TextField gradeField = new TextField();
        gradeField.setPrefWidth(100);
         
        Button saveGradeBtn = new Button("Save Feedback");
        saveGradeBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        saveGradeBtn.setOnAction(e -> {
            // In a real implementation, this would save the feedback to the database
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Feedback Saved");
            alert.setHeaderText(null);
            alert.setContentText("The feedback has been saved successfully.");
            alert.showAndWait();
        });
        
        gradeBox.getChildren().addAll(gradeLabel, gradeField, saveGradeBtn);
        
        // Add all components to content
        content.getChildren().addAll(
            statusBox, 
            dateBox, 
            contentHeader, 
            contentArea,
            gradingHeader,
            feedbackArea,
            gradeBox
        );
        
        // Add close button
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        
        // Set the content for the dialog
        dialog.getDialogPane().setContent(content);
        
        // Make the dialog bigger
        dialog.getDialogPane().setPrefSize(800, 600);
        
        // Show the dialog
        dialog.showAndWait();
    }
}