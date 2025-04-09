package controllers_students;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.control.Alert.AlertType;
import dao.DatabaseConnection;

/**
 * Displays student assignments with a modern user interface
 * connected to a database for real data
 */
public class AssignmentsPage {
    private final Map<String, SubjectData> subjectDataMap = new HashMap<>();
    private final StringProperty currentSubject = new SimpleStringProperty();
    private final VBox dynamicContent = new VBox(20);
    
    // User information
    private User currentUser;
    private int currentUserId = 0;
    private int studentId = 0;
    
    /**
     * Creates a new AssignmentsPage instance
     */
    public AssignmentsPage() {
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
        loadSubjectsAndAssignments();
        currentSubject.addListener((observable, oldValue, newValue) -> updateContent(newValue));
    }
    
    /**
     * Creates a new AssignmentsPage instance with specified user ID
     */
    public AssignmentsPage(int userId) {
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
        loadSubjectsAndAssignments();
        currentSubject.addListener((observable, oldValue, newValue) -> updateContent(newValue));
    }
    
    /**
     * Loads subjects and assignments from the database
     */
    private void loadSubjectsAndAssignments() {
        subjectDataMap.clear();
        if (studentId <= 0) {
            showError("Authentication Error", "Please log in to view your assignments");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First get all subjects (courses) the student is enrolled in
            String subjectQuery = 
                "SELECT c.courseID, c.courseName, c.courseColor, " +
                "CONCAT('Period ', e.periodNumber) as period " +
                "FROM Courses c " +
                "JOIN Enrollments e ON c.courseID = e.courseID " +
                "WHERE e.studentID = ? " +
                "ORDER BY e.periodNumber";
                
            try (PreparedStatement pstmt = conn.prepareStatement(subjectQuery)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    boolean hasSubjects = false;
                    while (rs.next()) {
                        hasSubjects = true;
                        int courseId = rs.getInt("courseID");
                        String courseName = rs.getString("courseName");
                        String period = rs.getString("period");
                        String color = rs.getString("courseColor");
                        
                        // If color is null, assign a default color
                        if (color == null || color.isEmpty()) {
                            color = getDefaultColor(courseName);
                        }
                        
                        // Load assignments for this course
                        ObservableList<Assignment> assignments = loadAssignmentsForCourse(courseId);
                        
                        // Add to the subject map
                        subjectDataMap.put(courseName, new SubjectData(courseName, period, color, assignments));
                    }
                    
                    // Set initial subject if we have any
                    if (hasSubjects && !subjectDataMap.isEmpty()) {
                        currentSubject.set(subjectDataMap.keySet().iterator().next());
                    }
                }
            }
        } catch (SQLException e) {
            showError("Database Error", "Error loading subjects: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads assignments for a specific course
     */
    private ObservableList<Assignment> loadAssignmentsForCourse(int courseId) {
        ObservableList<Assignment> assignments = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = 
                "SELECT a.assignmentID, a.title, a.dueDate, ap.status, ap.content, " +
                "a.points, a.priority, a.description " +
                "FROM Assignments a " +
                "LEFT JOIN AssignmentProgress ap ON a.assignmentID = ap.assignmentID AND ap.studentID = ? " +
                "WHERE a.courseID = ? " +
                "ORDER BY a.dueDate";
                
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int assignmentId = rs.getInt("assignmentID");
                        String title = rs.getString("title");
                        LocalDate dueDate = rs.getDate("dueDate").toLocalDate();
                        
                        // Get status - if null, set as "Not started"
                        String status = rs.getString("status");
                        if (status == null || status.isEmpty()) {
                            status = "Not started";
                        }
                        
                        String content = rs.getString("content"); // Get submitted content if available
                        String points = rs.getString("points");
                        String priority = rs.getString("priority");
                        String description = rs.getString("description");
                        
                        Assignment assignment = new Assignment(assignmentId, title, dueDate, status, points, priority, description, content);
                        assignments.add(assignment);
                    }
                }
            }
        } catch (SQLException e) {
            showError("Database Error", "Error loading assignments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return assignments;
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
     * Returns a default color based on subject name
     */
    private String getDefaultColor(String subjectName) {
        // Convert to lowercase for case-insensitive comparison
        String lowerName = subjectName.toLowerCase();
        
        if (lowerName.contains("math") || lowerName.contains("calculus") || lowerName.contains("algebra")) {
            return "#3498DB"; // Blue
        } else if (lowerName.contains("science") || lowerName.contains("biology") || 
                   lowerName.contains("chemistry") || lowerName.contains("physics")) {
            return "#2ECC71"; // Green
        } else if (lowerName.contains("history") || lowerName.contains("social")) {
            return "#E74C3C"; // Red
        } else if (lowerName.contains("english") || lowerName.contains("literature") || 
                   lowerName.contains("language")) {
            return "#9B59B6"; // Purple
        } else if (lowerName.contains("art") || lowerName.contains("music") || 
                   lowerName.contains("drama")) {
            return "#F39C12"; // Orange
        } else {
            return "#34495E"; // Dark blue/gray
        }
    }
    
    /**
     * Returns the main view for the assignment page
     */
    public Node getView() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setBackground(new Background(new BackgroundFill(Color.web("#f5f7fa"), CornerRadii.EMPTY, Insets.EMPTY)));
        
        // Create the title bar
        BorderPane titleBar = createTitleBar();
        mainLayout.setTop(titleBar);
        
        // Create the content area with padding
        dynamicContent.setPadding(new Insets(20));
        dynamicContent.setSpacing(20);
        
        // Add the scroll pane for the content
        ScrollPane scrollPane = new ScrollPane(dynamicContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setBackground(new Background(new BackgroundFill(Color.web("#f5f7fa"), CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setPannable(true);
        
        // Add a drop shadow to the content
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        dropShadow.setRadius(5);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dynamicContent.setEffect(dropShadow);
        
        mainLayout.setCenter(scrollPane);
        
        // Initialize with default subject if available
        if (!subjectDataMap.isEmpty() && currentSubject.get() != null) {
            updateContent(currentSubject.get());
        } else {
            // Show a message if no subjects or assignments
            showNoSubjectsMessage();
        }
        
        return mainLayout;
    }
    
    /**
     * Shows a message when no subjects are available
     */
    private void showNoSubjectsMessage() {
        dynamicContent.getChildren().clear();
        
        VBox messageBox = new VBox(15);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(50));
        messageBox.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        
        Label noSubjectsLabel = new Label("No courses found");
        noSubjectsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        noSubjectsLabel.setTextFill(Color.web("#2c3e50"));
        
        Label suggestLabel = new Label("You are not enrolled in any courses yet. Please enroll in courses to see assignments.");
        suggestLabel.setFont(Font.font("Arial", 14));
        suggestLabel.setTextFill(Color.web("#7f8c8d"));
        suggestLabel.setWrapText(true);
        
        Button enrollButton = new Button("Browse Available Courses");
        enrollButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );
        
        // Here you'd add action to navigate to the courses page
        enrollButton.setOnAction(e -> {
            // Navigate to courses page
        });
        
        messageBox.getChildren().addAll(noSubjectsLabel, suggestLabel, enrollButton);
        dynamicContent.getChildren().add(messageBox);
    }
    
    /**
     * Creates the top title bar
     */
    private BorderPane createTitleBar() {
        BorderPane titleBar = new BorderPane();
        titleBar.setPadding(new Insets(15, 20, 15, 20));
        titleBar.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        
        // Add drop shadow to title bar
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        dropShadow.setRadius(3);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(1);
        titleBar.setEffect(dropShadow);
        
        // Left side - Title
        Label titleLabel = new Label("My Assignments");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        titleBar.setLeft(titleLabel);
        
        // Only show subject selector if we have subjects
        if (!subjectDataMap.isEmpty()) {
            // Right side - Subject selector
            HBox subjectSelectorBox = new HBox(10);
            subjectSelectorBox.setAlignment(Pos.CENTER_RIGHT);
            
            Label subjectLabel = new Label("Subject:");
            subjectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            subjectLabel.setTextFill(Color.web("#7f8c8d"));
            
            ComboBox<String> subjectSelector = new ComboBox<>();
            subjectSelector.getItems().addAll(subjectDataMap.keySet());
            
            if (currentSubject.get() != null) {
                subjectSelector.setValue(currentSubject.get());
            } else if (!subjectDataMap.isEmpty()) {
                subjectSelector.setValue(subjectDataMap.keySet().iterator().next());
            }
            
            subjectSelector.valueProperty().bindBidirectional(currentSubject);
            
            // Customize the combo box
            subjectSelector.setButtonCell(new SubjectListCell());
            subjectSelector.setCellFactory(p -> new SubjectListCell());
            
            subjectSelectorBox.getChildren().addAll(subjectLabel, subjectSelector);
            titleBar.setRight(subjectSelectorBox);
        }
        
        return titleBar;
    }
    
    /**
     * Updates the content based on the selected subject
     */
    private void updateContent(String subject) {
        dynamicContent.getChildren().clear();
        
        SubjectData data = subjectDataMap.get(subject);
        if (data != null) {
            VBox subjectHeaderSection = createSubjectHeaderSection(data);
            VBox summarySection = createSummarySection(data);
            VBox assignmentsSection = createAssignmentsSection(data);
            
            dynamicContent.getChildren().addAll(subjectHeaderSection, summarySection, assignmentsSection);
        } else {
            showNoSubjectsMessage();
        }
    }
    
    /**
     * Creates the subject header section with name and period
     */
    private VBox createSubjectHeaderSection(SubjectData data) {
        VBox headerSection = new VBox(10);
        headerSection.setPadding(new Insets(25));
        headerSection.setBackground(new Background(new BackgroundFill(Color.web(data.getColor()), new CornerRadii(10), Insets.EMPTY)));
        
        // Subject icon
        Circle subjectIcon = new Circle(40);
        subjectIcon.setFill(Color.WHITE);
        subjectIcon.setOpacity(0.2);
        
        // First letter of subject
        Label iconLetter = new Label(data.getSubjectName().substring(0, 1));
        iconLetter.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        iconLetter.setTextFill(Color.WHITE);
        
        StackPane iconStack = new StackPane(subjectIcon, iconLetter);
        
        // Subject name and period
        Label subjectLabel = new Label(data.getSubjectName());
        subjectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        subjectLabel.setTextFill(Color.WHITE);
        
        Label periodLabel = new Label(data.getPeriod());
        periodLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        periodLabel.setTextFill(Color.WHITE);
        periodLabel.setOpacity(0.9);
        
        VBox textBox = new VBox(5, subjectLabel, periodLabel);
        
        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        contentBox.getChildren().addAll(iconStack, textBox);
        
        headerSection.getChildren().add(contentBox);
        
        return headerSection;
    }
    
    /**
     * Creates a summary section showing counts of assignments
     */
    private VBox createSummarySection(SubjectData data) {
        VBox summarySection = new VBox(15);
        summarySection.setPadding(new Insets(20));
        summarySection.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        
        Label summaryLabel = new Label("Assignment Summary");
        summaryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        summaryLabel.setTextFill(Color.web("#2c3e50"));
        
        // Calculate stats
        int total = data.getAssignments().size();
        int notStarted = 0;
        int inProgress = 0;
        int completed = 0;
        
        for (Assignment assignment : data.getAssignments()) {
            switch (assignment.getStatus().toLowerCase()) {
                case "not started":
                    notStarted++;
                    break;
                case "in progress":
                    inProgress++;
                    break;
                case "completed":
                    completed++;
                    break;
            }
        }
        
        // Create stat tiles
        HBox statTiles = new HBox(15);
        statTiles.setAlignment(Pos.CENTER);
        
        VBox totalTile = createStatTile("Total", String.valueOf(total), "#3498db");
        VBox notStartedTile = createStatTile("Not Started", String.valueOf(notStarted), "#e74c3c");
        VBox inProgressTile = createStatTile("In Progress", String.valueOf(inProgress), "#f39c12");
        VBox completedTile = createStatTile("Completed", String.valueOf(completed), "#2ecc71");
        
        statTiles.getChildren().addAll(totalTile, notStartedTile, inProgressTile, completedTile);
        summarySection.getChildren().addAll(summaryLabel, statTiles);
        
        return summarySection;
    }
    
    /**
     * Creates a stat tile for the summary section
     */
    private VBox createStatTile(String label, String value, String color) {
        VBox tile = new VBox(5);
        tile.setPadding(new Insets(15));
        tile.setAlignment(Pos.CENTER);
        tile.setPrefWidth(150);
        tile.setBackground(new Background(new BackgroundFill(Color.web(color).deriveColor(1, 1, 1, 0.1), new CornerRadii(5), Insets.EMPTY)));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));
        
        Label descLabel = new Label(label);
        descLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        
        tile.getChildren().addAll(valueLabel, descLabel);
        
        return tile;
    }
    
    /**
     * Creates the assignments table section
     */
    private VBox createAssignmentsSection(SubjectData data) {
        VBox assignmentsSection = new VBox(15);
        assignmentsSection.setPadding(new Insets(20));
        assignmentsSection.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label assignmentsLabel = new Label("Assignments");
        assignmentsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        assignmentsLabel.setTextFill(Color.web("#2c3e50"));
        
  
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(assignmentsLabel, spacer);
       
        // Create assignments table with improved styling
        TableView<Assignment> assignmentsTable = createStyledTable(data);
        
        assignmentsSection.getChildren().addAll(headerBox, assignmentsTable);
        
        return assignmentsSection;
    }
    
    /**
     * Adds a new assignment to the course
     */
    private void addNewAssignment(String subjectName) {
        // In a real application, you would show a form and add to database
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Assignment");
        alert.setHeaderText("Add New Assignment");
        alert.setContentText("This would open a form to add a new assignment to " + subjectName);
        alert.showAndWait();
    }
    
    /**
     * Creates a styled table view for assignments
     */
    private TableView<Assignment> createStyledTable(SubjectData data) {
        TableView<Assignment> assignmentsTable = new TableView<>(data.getAssignments());
        assignmentsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        assignmentsTable.setPrefHeight(300);
        assignmentsTable.setPlaceholder(new Label("No assignments for this subject"));
        
        // Name column
        TableColumn<Assignment, String> assignmentColumn = new TableColumn<>("Assignment");
        assignmentColumn.setCellValueFactory(cellData -> cellData.getValue().assignmentNameProperty());
        assignmentColumn.setPrefWidth(250);
        
        // Due date column
        TableColumn<Assignment, String> dueDateColumn = new TableColumn<>("Due Date");
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().formattedDueDateProperty());
        dueDateColumn.setPrefWidth(120);
        dueDateColumn.setCellFactory(column -> new TableCell<Assignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Today")) {
                        setTextFill(Color.web("#e74c3c"));
                        setStyle("-fx-font-weight: bold;");
                    } else if (item.contains("Tomorrow")) {
                        setTextFill(Color.web("#f39c12"));
                        setStyle("-fx-font-weight: bold;");
                    } else if (item.contains("Overdue")) {
                        setTextFill(Color.web("#c0392b"));
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(Color.BLACK);
                        setStyle("");
                    }
                }
            }
        });
        
        // Status column with color indicators
        TableColumn<Assignment, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusColumn.setPrefWidth(120);
        statusColumn.setCellFactory(column -> new TableCell<Assignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox statusBox = new HBox(5);
                    statusBox.setAlignment(Pos.CENTER_LEFT);
                    Circle statusIndicator = new Circle(5);
                    if (item.equalsIgnoreCase("Not started")) {
                        statusIndicator.setFill(Color.web("#e74c3c"));
                    } else if (item.equalsIgnoreCase("In progress")) {
                        statusIndicator.setFill(Color.web("#f39c12"));
                    } else if (item.equalsIgnoreCase("Completed")) {
                        statusIndicator.setFill(Color.web("#2ecc71"));
                    } else {
                        statusIndicator.setFill(Color.web("#95a5a6"));
                    }
                    Label statusLabel = new Label(item);
                    statusBox.getChildren().addAll(statusIndicator, statusLabel);
                    setGraphic(statusBox);
                }
            }
        });
        
        // Points column
        TableColumn<Assignment, String> pointsColumn = new TableColumn<>("Points");
        pointsColumn.setCellValueFactory(cellData -> cellData.getValue().pointsProperty());
        pointsColumn.setPrefWidth(70);
        
        // Priority column with colors
        TableColumn<Assignment, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
        priorityColumn.setPrefWidth(80);
        priorityColumn.setCellFactory(column -> new TableCell<Assignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("High")) {
                        setTextFill(Color.web("#e74c3c"));
                        setStyle("-fx-font-weight: bold;");
                    } else if (item.equalsIgnoreCase("Medium")) {
                        setTextFill(Color.web("#f39c12"));
                    } else {
                        setTextFill(Color.web("#2ecc71"));
                    }
                }
            }
        });
        
        // Actions column with start assignment button
        TableColumn<Assignment, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(150);
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button startButton = new Button("Start Assignment");
            
            {
                startButton.setOnAction(event -> {
                    Assignment assignment = getTableView().getItems().get(getIndex());
                    startAssignment(assignment);
                });
                
                // Style the button
                startButton.setPrefWidth(120);
                startButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(startButton);
                    
                    // Update button text based on assignment status
                    Assignment assignment = getTableView().getItems().get(getIndex());
                    if (assignment.getStatus().equalsIgnoreCase("Completed")) {
                        startButton.setText("View Assignment");
                        startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    } else if (assignment.getStatus().equalsIgnoreCase("In progress")) {
                        startButton.setText("Continue");
                        startButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                    } else {
                        startButton.setText("Start Assignment");
                        startButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    }
                }
            }
        });
        
        assignmentsTable.getColumns().addAll(
            assignmentColumn, 
            dueDateColumn, 
            statusColumn, 
            pointsColumn, 
            priorityColumn, 
            actionsColumn
        );
        
        return assignmentsTable;
    }
    
    /**
     * Shows assignment details with submission option
     */
    private void startAssignment(Assignment assignment) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Assignment Details");
        dialog.setHeaderText(null);
        
        // Create a custom header
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setBackground(new Background(new BackgroundFill(Color.web("#3498db"), CornerRadii.EMPTY, Insets.EMPTY)));
        
        Label titleLabel = new Label(assignment.getAssignmentName());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        
        header.getChildren().add(titleLabel);
        
        // Create content
        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(20));
        
        // Assignment details section
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(15);
        detailsGrid.setPadding(new Insets(0, 0, 15, 0));
        
        // Row 0
        Label dueDateHeader = new Label("Due Date");
        dueDateHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        dueDateHeader.setTextFill(Color.web("#7f8c8d"));
        detailsGrid.add(dueDateHeader, 0, 0);
        
        Label statusHeader = new Label("Status");
        statusHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusHeader.setTextFill(Color.web("#7f8c8d"));
        detailsGrid.add(statusHeader, 1, 0);
        
        // Row 1
        Label dueDateValue = new Label(assignment.getFormattedDueDate());
        dueDateValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        detailsGrid.add(dueDateValue, 0, 1);
        
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Circle statusIndicator = new Circle(5);
        if (assignment.getStatus().equalsIgnoreCase("Not started")) {
            statusIndicator.setFill(Color.web("#e74c3c"));
        } else if (assignment.getStatus().equalsIgnoreCase("In progress")) {
            statusIndicator.setFill(Color.web("#f39c12"));
        } else if (assignment.getStatus().equalsIgnoreCase("Completed")) {
            statusIndicator.setFill(Color.web("#2ecc71"));
        }
        Label statusLabel = new Label(assignment.getStatus());
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statusBox.getChildren().addAll(statusIndicator, statusLabel);
        detailsGrid.add(statusBox, 1, 1);
        
        // Row 2
        Label pointsHeader = new Label("Points");
        pointsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        pointsHeader.setTextFill(Color.web("#7f8c8d"));
        detailsGrid.add(pointsHeader, 0, 2);
        
        Label priorityHeader = new Label("Priority");
        priorityHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        priorityHeader.setTextFill(Color.web("#7f8c8d"));
        detailsGrid.add(priorityHeader, 1, 2);
        
        // Row 3
        Label pointsValue = new Label(assignment.getPoints());
        pointsValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        detailsGrid.add(pointsValue, 0, 3);
        
        Label priorityValue = new Label(assignment.getPriority());
        priorityValue.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        if (assignment.getPriority().equalsIgnoreCase("High")) {
            priorityValue.setTextFill(Color.web("#e74c3c"));
        } else if (assignment.getPriority().equalsIgnoreCase("Medium")) {
            priorityValue.setTextFill(Color.web("#f39c12"));
        } else {
            priorityValue.setTextFill(Color.web("#2ecc71"));
        }
        detailsGrid.add(priorityValue, 1, 3);
        
        // Description section
        Label descriptionHeader = new Label("Description");
        descriptionHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        descriptionHeader.setTextFill(Color.web("#2c3e50"));
        
        TextArea descriptionArea = new TextArea(assignment.getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(80);
        descriptionArea.setEditable(false);
        descriptionArea.setStyle("-fx-control-inner-background: #f9f9f9;");
        
        // Submission section
        Label submissionHeader = new Label("Your Submission");
        submissionHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        submissionHeader.setTextFill(Color.web("#2c3e50"));
        
        TextArea submissionArea = new TextArea();
        submissionArea.setWrapText(true);
        submissionArea.setPrefHeight(150);
        
        // Pre-fill with existing content if available
        if (assignment.getContent() != null && !assignment.getContent().isEmpty()) {
            submissionArea.setText(assignment.getContent());
        }
        
        // Set readonly if already completed
        if (assignment.getStatus().equalsIgnoreCase("Completed")) {
            submissionArea.setEditable(false);
            submissionArea.setStyle("-fx-control-inner-background: #f9f9f9;");
        } else {
            submissionArea.setPromptText("Enter your submission here...");
        }
        
        // Add all components to content box
        contentBox.getChildren().addAll(
            detailsGrid,
            descriptionHeader,
            descriptionArea,
            submissionHeader,
            submissionArea
        );
        
        // Create buttons for actions
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = ButtonType.CANCEL;
        
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, cancelButtonType);
        
        // Only show submit button if not completed
        if (assignment.getStatus().equalsIgnoreCase("Completed")) {
            Node submitButton = dialog.getDialogPane().lookupButton(submitButtonType);
            submitButton.setVisible(false);
        }
        
        // Combine all components
        VBox dialogContent = new VBox();
        dialogContent.getChildren().addAll(header, contentBox);
        dialog.getDialogPane().setContent(dialogContent);
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return submissionArea.getText();
            }
            return null;
        });
        
        // Handle the result
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(content -> {
            // Save the content and update status
            saveAssignmentContent(assignment.getId(), content);
            
            // Refresh the view
            loadSubjectsAndAssignments();
            updateContent(currentSubject.get());
        });
    }
    
    /**
     * Saves assignment content and updates status to Completed
     */
    private void saveAssignmentContent(int assignmentId, String content) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if a record already exists
            String checkQuery = "SELECT * FROM AssignmentProgress WHERE assignmentID = ? AND studentID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setInt(1, assignmentId);
                pstmt.setInt(2, studentId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    // Update existing record
                    String updateQuery = "UPDATE AssignmentProgress SET content = ?, status = 'Completed', lastUpdated = CURRENT_TIMESTAMP " +
                                         "WHERE assignmentID = ? AND studentID = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, content);
                        updateStmt.setInt(2, assignmentId);
                        updateStmt.setInt(3, studentId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Insert new record
                    String insertQuery = "INSERT INTO AssignmentProgress (assignmentID, studentID, content, status, lastUpdated) " +
                                         "VALUES (?, ?, ?, 'Completed', CURRENT_TIMESTAMP)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, assignmentId);
                        insertStmt.setInt(2, studentId);
                        insertStmt.setString(3, content);
                        insertStmt.executeUpdate();
                    }
                }
            }
            
            // Show success message
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Assignment Submitted");
            alert.setHeaderText("Success");
            alert.setContentText("Your assignment has been submitted successfully and marked as completed.");
            alert.showAndWait();
            
        } catch (SQLException e) {
            showError("Database Error", "Error saving assignment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows an error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Custom cell for subject display in ComboBox
     */
    private class SubjectListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                SubjectData data = subjectDataMap.get(item);
                if (data != null) {
                    HBox content = new HBox(10);
                    content.setAlignment(Pos.CENTER_LEFT);
                    Rectangle colorBox = new Rectangle(12, 12);
                    colorBox.setFill(Color.web(data.getColor()));
                    colorBox.setArcWidth(3);
                    colorBox.setArcHeight(3);
                    Label nameLabel = new Label(item);
                    nameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
                    content.getChildren().addAll(colorBox, nameLabel);
                    setText(null);
                    setGraphic(content);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
        }
    }
    
    // Model classes
    public static class Assignment {
        private final int id;
        private final StringProperty assignmentName;
        private final LocalDate dueDate;
        private final StringProperty formattedDueDate;
        private final StringProperty status;
        private final StringProperty points;
        private final StringProperty priority;
        private final String description;
        private final String content;
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        
        public Assignment(int id, String assignmentName, LocalDate dueDate, String status, String points, 
                         String priority, String description, String content) {
            this.id = id;
            this.assignmentName = new SimpleStringProperty(assignmentName);
            this.dueDate = dueDate;
            this.formattedDueDate = new SimpleStringProperty(formatDueDate(dueDate));
            this.status = new SimpleStringProperty(status);
            this.points = new SimpleStringProperty(points);
            this.priority = new SimpleStringProperty(priority);
            this.description = description != null ? description : "No description available.";
            this.content = content;
        }
        
        private String formatDueDate(LocalDate date) {
            if (date.equals(LocalDate.now())) {
                return "Due Today";
            } else if (date.equals(LocalDate.now().plusDays(1))) {
                return "Due Tomorrow";
            } else if (date.isBefore(LocalDate.now())) {
                return "Overdue";
            } else {
                return "Due " + date.format(formatter);
            }
        }
        
        public int getId() { return id; }
        public StringProperty assignmentNameProperty() { return assignmentName; }
        public StringProperty formattedDueDateProperty() { return formattedDueDate; }
        public StringProperty statusProperty() { return status; }
        public StringProperty pointsProperty() { return points; }
        public StringProperty priorityProperty() { return priority; }
        public String getAssignmentName() { return assignmentName.get(); }
        public LocalDate getDueDate() { return dueDate; }
        public String getFormattedDueDate() { return formattedDueDate.get(); }
        public String getStatus() { return status.get(); }
        public String getPoints() { return points.get(); }
        public String getPriority() { return priority.get(); }
        public String getDescription() { return description; }
        public String getContent() { return content; }
        public void setStatus(String newStatus) {
            status.set(newStatus);
        }
    }
    
    public static class SubjectData {
        private final String subjectName;
        private final String period;
        private final String color;
        private final ObservableList<Assignment> assignments;
        
        public SubjectData(String subjectName, String period, String color, ObservableList<Assignment> assignments) {
            this.subjectName = subjectName;
            this.period = period;
            this.color = color;
            this.assignments = assignments;
        }
        
        public String getSubjectName() { return subjectName; }
        public String getPeriod() { return period; }
        public String getColor() { return color; }
        public ObservableList<Assignment> getAssignments() { return assignments; }
    }
}