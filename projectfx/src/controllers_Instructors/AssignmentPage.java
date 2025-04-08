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
                    // View assignment implementation - display details
                    Alert details = new Alert(Alert.AlertType.INFORMATION);
                    details.setTitle("Assignment Details");
                    details.setHeaderText(data.getTitle());
                    details.setContentText(
                        "Description: " + data.getDescription() + "\n" +
                        "Course ID: " + data.getCourseId() + "\n" +
                        "Due Date: " + new SimpleDateFormat("MMM dd, yyyy").format(data.getDueDate()) + "\n" +
                        "Points: " + data.getPoints() + "\n" +
                        "Priority: " + data.getPriority()
                    );
                    details.showAndWait();
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
}