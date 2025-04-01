package controllers_Admin;

import dao.DatabaseConnection;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Course Management page for admin operations on courses with database integration
 */
public class CourseManagementPage {
    
    // UI components that need class-level access
    private TextField searchField;
    private FlowPane courseGrid;
    private ObservableList<CourseData> allCourses = FXCollections.observableArrayList();
    private FilteredList<CourseData> filteredCourses;
    
    // Color constants for UI
    private static final String PRIMARY_COLOR = "#3498db";
    private static final String SECONDARY_COLOR = "#2ecc71";
    private static final String DANGER_COLOR = "#e74c3c";
    private static final String WARNING_COLOR = "#f39c12";
    private static final String BACKGROUND_COLOR = "#f5f7fa";
    private static final String CARD_COLOR = "#ffffff";
    private static final String TEXT_COLOR = "#2c3e50";
    private static final String SUBTEXT_COLOR = "#7f8c8d";
    
    // Category colors
    private static final List<String> CATEGORY_COLORS = Arrays.asList(
        "#3498db", "#2ecc71", "#e74c3c", "#f39c12", "#9b59b6",
        "#1abc9c", "#d35400", "#c0392b", "#16a085", "#8e44ad"
    );
    
    // Sample categories if not specified in DB
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
        "Programming", "Data Science", "Business", "Design", "Marketing",
        "Mathematics", "Science", "Languages", "Arts", "Engineering"
    );
    
    /**
     * Get the main view for course management
     */
    public Node getView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setBackground(new Background(new BackgroundFill(
                Color.web(BACKGROUND_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        
        // Page header
        BorderPane header = createHeader();
        
        // Search card
        VBox searchCard = createSearchCard();
        
        // Course grid card - now expanded
        VBox courseGridCard = createCourseGridCard();
        VBox.setVgrow(courseGridCard, Priority.ALWAYS);
        
        view.getChildren().addAll(header, searchCard, courseGridCard);
        
        // Load data from database
        loadCoursesFromDatabase();
        
        return view;
    }
    
    /**
     * Creates the page header with title and add button
     */
    private BorderPane createHeader() {
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(0, 0, 10, 0));
        
        // Left side - title
        Label title = new Label("Course Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(TEXT_COLOR));
        
        // Right side - add course button
        Button addCourseBtn = new Button("Add New Course");
        addCourseBtn.setFont(Font.font("Arial", 14));
        addCourseBtn.setPadding(new Insets(10, 20, 10, 20));
        addCourseBtn.setStyle(
            "-fx-background-color: " + SECONDARY_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4;"
        );
        
        addCourseBtn.setOnAction(e -> showAddCourseDialog());
        
        header.setLeft(title);
        header.setRight(addCourseBtn);
        
        return header;
    }
    
    /**
     * Creates the search card
     */
    private VBox createSearchCard() {
        VBox searchCard = new VBox(15);
        searchCard.setPadding(new Insets(15));
        searchCard.setBackground(new Background(new BackgroundFill(
                Color.web(CARD_COLOR), new CornerRadii(8), Insets.EMPTY)));
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        searchCard.setEffect(dropShadow);
        
        // Search controls
        HBox searchControls = new HBox(15);
        searchControls.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Search courses by name...");
        searchField.setPrefWidth(400);
        searchField.setPadding(new Insets(10));
        searchField.setFont(Font.font("Arial", 14));
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterCourses();
        });
        
        Button clearBtn = new Button("Clear");
        clearBtn.setPadding(new Insets(10, 20, 10, 20));
        clearBtn.setStyle(
            "-fx-background-color: #ecf0f1;" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-background-radius: 4;"
        );
        clearBtn.setOnAction(e -> {
            searchField.clear();
        });
        
        searchControls.getChildren().addAll(searchField, clearBtn);
        
        searchCard.getChildren().add(searchControls);
        
        return searchCard;
    }
    
    /**
     * Creates the course grid card
     */
    private VBox createCourseGridCard() {
        VBox courseCardContainer = new VBox(15);
        courseCardContainer.setPadding(new Insets(20));
        courseCardContainer.setBackground(new Background(new BackgroundFill(
                Color.web(CARD_COLOR), new CornerRadii(8), Insets.EMPTY)));
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        courseCardContainer.setEffect(dropShadow);
        
        // Label for course grid
        Label coursesLabel = new Label("Course Catalog");
        coursesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        coursesLabel.setTextFill(Color.web(TEXT_COLOR));
        
        // Create the flow pane for course cards
        courseGrid = new FlowPane();
        courseGrid.setHgap(20);
        courseGrid.setVgap(20);
        courseGrid.setPadding(new Insets(15, 0, 0, 0));
        
        // Add a scroll pane for the course grid
        ScrollPane scrollPane = new ScrollPane(courseGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPannable(true);
        scrollPane.setPrefHeight(600); // Make it much taller
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        courseCardContainer.getChildren().addAll(coursesLabel, scrollPane);
        
        return courseCardContainer;
    }
    
    /**
     * Creates a card for an individual course
     */
    private VBox createCourseCard(CourseData course) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setBackground(new Background(new BackgroundFill(
                Color.web(CARD_COLOR), new CornerRadii(8), Insets.EMPTY)));
        card.setEffect(new DropShadow(4, 0, 3, Color.rgb(0, 0, 0, 0.15)));
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        
        // Course color band at top
        Rectangle colorBand = new Rectangle(290, 8);
        colorBand.setFill(Color.web(course.getCourseColor()));
        colorBand.setArcWidth(8);
        colorBand.setArcHeight(8);
        
        // Course title
        Label courseTitle = new Label(course.getCourseName());
        courseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        courseTitle.setTextFill(Color.web(TEXT_COLOR));
        courseTitle.setWrapText(true);
        
        // Course info
        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(10, 0, 10, 0));
        
        // Course instructor
        HBox instructorBox = new HBox(10);
        instructorBox.setAlignment(Pos.CENTER_LEFT);
        
        Circle instructorAvatar = new Circle(14);
        instructorAvatar.setFill(Color.web(course.getCourseColor()).deriveColor(1, 1, 1, 0.7));
        
        Label instructorInitial = new Label(course.getInstructor().substring(0, 1).toUpperCase());
        instructorInitial.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        instructorInitial.setTextFill(Color.WHITE);
        
        StackPane avatarStack = new StackPane(instructorAvatar, instructorInitial);
        
        Label instructorLabel = new Label("Instructor: " + course.getInstructor());
        instructorLabel.setFont(Font.font("Arial", 14));
        instructorLabel.setTextFill(Color.web(TEXT_COLOR));
        
        instructorBox.getChildren().addAll(avatarStack, instructorLabel);
        
        // Description excerpt
        Label descriptionLabel = new Label(course.getDescription());
        descriptionLabel.setFont(Font.font("Arial", 13));
        descriptionLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxHeight(80);
        
        infoBox.getChildren().addAll(instructorBox, descriptionLabel);
        
        // Action buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        
        Button editBtn = new Button("Edit");
        editBtn.setPrefWidth(90);
        editBtn.setPadding(new Insets(8, 15, 8, 15));
        editBtn.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4;"
        );
        editBtn.setOnAction(e -> showEditCourseDialog(course));
        
        Button viewBtn = new Button("View");
        viewBtn.setPrefWidth(90);
        viewBtn.setPadding(new Insets(8, 15, 8, 15));
        viewBtn.setStyle(
            "-fx-background-color: #95a5a6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4;"
        );
        viewBtn.setOnAction(e -> showCourseDetails(course));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setPrefWidth(90);
        deleteBtn.setPadding(new Insets(8, 15, 8, 15));
        deleteBtn.setStyle(
            "-fx-background-color: " + DANGER_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4;"
        );
        deleteBtn.setOnAction(e -> showDeleteConfirmation(course));
        
        buttonsBox.getChildren().addAll(editBtn, viewBtn, deleteBtn);
        
        card.getChildren().addAll(colorBand, courseTitle, infoBox, buttonsBox);
        
        return card;
    }
    
    /**
     * Load courses from the database
     */
    private void loadCoursesFromDatabase() {
        allCourses.clear();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // We need to join with the Users table to get instructor names
            String query = "SELECT c.courseID, c.courseName, c.description, c.courseColor, " +
                           "u.username AS instructorName " +
                           "FROM Courses c " +
                           "LEFT JOIN Users u ON c.createdBy = u.userID";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                Random random = new Random();
                
                while (rs.next()) {
                    // Simplify - just use a random category for display purposes
                    String category = DEFAULT_CATEGORIES.get(random.nextInt(DEFAULT_CATEGORIES.size()));
                    
                    String courseColor = rs.getString("courseColor");
                    if (courseColor == null || courseColor.isEmpty()) {
                        // If no color is set, use a random color
                        courseColor = CATEGORY_COLORS.get(random.nextInt(CATEGORY_COLORS.size()));
                    }
                    
                    // Get the instructor's name, or use "Unknown" if null
                    String instructorName = rs.getString("instructorName");
                    if (instructorName == null || instructorName.isEmpty()) {
                        instructorName = "Unknown Instructor";
                    }
                    
                    CourseData course = new CourseData(
                        rs.getInt("courseID"),
                        rs.getString("courseName"),
                        rs.getString("description") != null ? rs.getString("description") : "",
                        instructorName,
                        category,
                        "Published", // Default status
                        0, // We're not showing enrollments anymore
                        courseColor
                    );
                    
                    allCourses.add(course);
                }
                
                // Set up filtered list
                filteredCourses = new FilteredList<>(allCourses, p -> true);
                
                // Update the UI
                updateCourseGrid();
            }
        } catch (SQLException e) {
            showErrorDialog("Database Error", "Failed to load courses from database", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Filter courses based on search text
     */
    private void filterCourses() {
        String searchText = searchField.getText().toLowerCase();
        
        filteredCourses.setPredicate(course -> {
            // If search field is empty, show all courses
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            // Filter by search text
            return course.getCourseName().toLowerCase().contains(searchText) ||
                   course.getDescription().toLowerCase().contains(searchText) ||
                   course.getInstructor().toLowerCase().contains(searchText);
        });
        
        updateCourseGrid();
    }
    
    /**
     * Update the course grid with filtered courses
     */
    private void updateCourseGrid() {
        courseGrid.getChildren().clear();
        
        for (CourseData course : filteredCourses) {
            VBox courseCard = createCourseCard(course);
            courseGrid.getChildren().add(courseCard);
        }
        
        if (filteredCourses.isEmpty()) {
            Label noCoursesLabel = new Label("No courses found matching your criteria");
            noCoursesLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
            noCoursesLabel.setTextFill(Color.web(SUBTEXT_COLOR));
            noCoursesLabel.setTextAlignment(TextAlignment.CENTER);
            noCoursesLabel.setPrefWidth(600);
            noCoursesLabel.setPadding(new Insets(50));
            
            courseGrid.getChildren().add(noCoursesLabel);
        }
    }
    
    /**
     * Show dialog to add a new course
     */
    private void showAddCourseDialog() {
        Dialog<CourseData> dialog = new Dialog<>();
        dialog.setTitle("Add New Course");
        
        // Style dialog header
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(15));
        header.setBackground(new Background(new BackgroundFill(
                Color.web(SECONDARY_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        
        Label headerLabel = new Label("Add New Course");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.WHITE);
        
        header.setCenter(headerLabel);
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Course Name");
        nameField.setPrefWidth(300);
        
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Course Description");
        descriptionField.setPrefRowCount(4);
        descriptionField.setWrapText(true);
        
        // Color picker
        ColorPicker colorPicker = new ColorPicker(Color.web(CATEGORY_COLORS.get(0)));
        colorPicker.setPrefWidth(100);
        
        // Add labels with styling
        Label nameLabel = new Label("Course Name:");
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nameLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        descriptionLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Label colorLabel = new Label("Course Color:");
        colorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        colorLabel.setTextFill(Color.web(TEXT_COLOR));
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(colorLabel, 0, 1);
        grid.add(colorPicker, 1, 1);
        grid.add(descriptionLabel, 0, 2);
        grid.add(descriptionField, 1, 2);
        
        // Create content pane with header and form
        VBox content = new VBox();
        content.getChildren().addAll(header, grid);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(500);
        
        // Request focus on the name field by default
        nameField.requestFocus();
        
        // Convert the result to a CourseData when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validate input
                    String name = nameField.getText().trim();
                    String description = descriptionField.getText().trim();
                    String color = String.format("#%02X%02X%02X",
                            (int)(colorPicker.getValue().getRed() * 255),
                            (int)(colorPicker.getValue().getGreen() * 255),
                            (int)(colorPicker.getValue().getBlue() * 255));
                    
                    if (name.isEmpty()) {
                        throw new IllegalArgumentException("Course name is required");
                    }
                    
                    // Add course to database
                    if (addCourseToDatabase(name, description, color)) {
                        loadCoursesFromDatabase(); // Refresh the data
                        return new CourseData(0, name, description, "Current User", "", "", 0, color);
                    }
                    
                } catch (Exception e) {
                    showErrorDialog("Input Error", "Invalid input", e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    /**
     * Show dialog to edit an existing course
     */
    private void showEditCourseDialog(CourseData course) {
        Dialog<CourseData> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        
        // Style dialog header
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(15));
        header.setBackground(new Background(new BackgroundFill(
                Color.web(PRIMARY_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        
        Label headerLabel = new Label("Edit Course: " + course.getCourseName());
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.WHITE);
        
        header.setCenter(headerLabel);
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField(course.getCourseName());
        nameField.setPrefWidth(300);
        
        TextArea descriptionField = new TextArea(course.getDescription());
        descriptionField.setPrefRowCount(4);
        descriptionField.setWrapText(true);
        
        // Color picker
        ColorPicker colorPicker = new ColorPicker(Color.web(course.getCourseColor()));
        colorPicker.setPrefWidth(100);
        
        // Add labels with styling
        Label nameLabel = new Label("Course Name:");
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nameLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        descriptionLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Label colorLabel = new Label("Course Color:");
        colorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        colorLabel.setTextFill(Color.web(TEXT_COLOR));
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(colorLabel, 0, 1);
        grid.add(colorPicker, 1, 1);
        grid.add(descriptionLabel, 0, 2);
        grid.add(descriptionField, 1, 2);
        
        // Create content pane with header and form
        VBox content = new VBox();
        content.getChildren().addAll(header, grid);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(500);
        
        // Convert the result to a CourseData when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validate input
                    String name = nameField.getText().trim();
                    String description = descriptionField.getText().trim();
                    String color = String.format("#%02X%02X%02X",
                            (int)(colorPicker.getValue().getRed() * 255),
                            (int)(colorPicker.getValue().getGreen() * 255),
                            (int)(colorPicker.getValue().getBlue() * 255));
                    
                    if (name.isEmpty()) {
                        throw new IllegalArgumentException("Course name is required");
                    }
                    
                    // Update course in database
                    if (updateCourseInDatabase(course.getCourseId(), name, description, color)) {
                        loadCoursesFromDatabase(); // Refresh the data
                        return course;
                    }
                    
                } catch (Exception e) {
                    showErrorDialog("Input Error", "Invalid input", e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    /**
     * Show course details in a dialog
     */
    private void showCourseDetails(CourseData course) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Course Details");
        
        // Create the content pane
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Course header with color
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setBackground(new Background(new BackgroundFill(
                Color.web(course.getCourseColor()), new CornerRadii(10, 10, 0, 0, false), Insets.EMPTY)));
        header.setPrefHeight(100);
        
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(course.getCourseName());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        
        titleBox.getChildren().add(titleLabel);
        header.getChildren().add(titleBox);
        
        // Course info grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(15));
        
        // Instructor
        Label instructorHeader = new Label("Instructor");
        instructorHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        instructorHeader.setTextFill(Color.web(SUBTEXT_COLOR));
        
        Label instructorValue = new Label(course.getInstructor());
        instructorValue.setFont(Font.font("Arial", 16));
        instructorValue.setTextFill(Color.web(TEXT_COLOR));
        
        // Add info to grid
        infoGrid.add(instructorHeader, 0, 0);
        infoGrid.add(instructorValue, 0, 1);
        
        infoGrid.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-background-radius: 8;"
        );
        
        // Description section
        VBox descriptionSection = new VBox(10);
        
        Label descriptionHeader = new Label("Description");
        descriptionHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        descriptionHeader.setTextFill(Color.web(TEXT_COLOR));
        
        Label descriptionValue = new Label(course.getDescription());
        descriptionValue.setFont(Font.font("Arial", 14));
        descriptionValue.setTextFill(Color.web(TEXT_COLOR));
        descriptionValue.setWrapText(true);
        
        descriptionSection.getChildren().addAll(descriptionHeader, descriptionValue);
        
        // Close button
        Button closeButton = new Button("Close");
        closeButton.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4;"
        );
        closeButton.setPrefWidth(100);
        closeButton.setPadding(new Insets(8, 15, 8, 15));
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);
        
        // Add everything to the content pane
        content.getChildren().addAll(header, infoGrid, descriptionSection, buttonBox);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(550);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        
        closeButton.setOnAction(e -> dialog.close());
        
        dialog.showAndWait();
    }
    
    /**
     * Show confirmation dialog before deleting a course
     */
    private void showDeleteConfirmation(CourseData course) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this course?");
        alert.setContentText("Course: " + course.getCourseName() + "\nInstructor: " + course.getInstructor() + "\nThis action cannot be undone.");
        
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteButton, cancelButton);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                deleteCourseFromDatabase(course.getCourseId());
                loadCoursesFromDatabase(); // Refresh the data
            }
        });
    }
    
    /**
     * Add a new course to the database
     */
    private boolean addCourseToDatabase(String name, String description, String color) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // We'll assume the current user ID is 1 (admin) for simplicity
            // In a real app, you'd get the ID of the logged-in user
            int currentUserId = 1;
            
            String query = "INSERT INTO Courses (courseName, description, createdBy, courseColor) VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, description);
                pstmt.setInt(3, currentUserId);
                pstmt.setString(4, color);
                
                int affectedRows = pstmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            showErrorDialog("Database Error", "Failed to add course", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing course in the database
     */
    private boolean updateCourseInDatabase(int courseId, String name, String description, String color) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE Courses SET courseName = ?, description = ?, courseColor = ? WHERE courseID = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, description);
                pstmt.setString(3, color);
                pstmt.setInt(4, courseId);
                
                int affectedRows = pstmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            showErrorDialog("Database Error", "Failed to update course", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a course from the database
     */
    private boolean deleteCourseFromDatabase(int courseId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM Courses WHERE courseID = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, courseId);
                
                int affectedRows = pstmt.executeUpdate();
                
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            showErrorDialog("Database Error", "Failed to delete course", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Show an error dialog with details
     */
    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Course data model class
     */
    public static class CourseData {
        private final int courseId;
        private final SimpleStringProperty courseName;
        private final SimpleStringProperty description;
        private final SimpleStringProperty instructor;
        private final SimpleStringProperty category;
        private final SimpleStringProperty status;
        private final SimpleIntegerProperty enrollments;
        private final SimpleStringProperty courseColor;
        
        public CourseData(int courseId, String courseName, String description, String instructor,
                         String category, String status, int enrollments, String courseColor) {
            this.courseId = courseId;
            this.courseName = new SimpleStringProperty(courseName);
            this.description = new SimpleStringProperty(description);
            this.instructor = new SimpleStringProperty(instructor);
            this.category = new SimpleStringProperty(category);
            this.status = new SimpleStringProperty(status);
            this.enrollments = new SimpleIntegerProperty(enrollments);
            this.courseColor = new SimpleStringProperty(courseColor.isEmpty() ? "#3498db" : courseColor);
        }
        
        // Getters
        public int getCourseId() { return courseId; }
        public String getCourseName() { return courseName.get(); }
        public String getDescription() { return description.get(); }
        public String getInstructor() { return instructor.get(); }
        public String getCategory() { return category.get(); }
        public String getStatus() { return status.get(); }
        public int getEnrollments() { return enrollments.get(); }
        public String getCourseColor() { return courseColor.get(); }
        
        // Property getters
        public SimpleStringProperty courseNameProperty() { return courseName; }
        public SimpleStringProperty descriptionProperty() { return description; }
        public SimpleStringProperty instructorProperty() { return instructor; }
        public SimpleStringProperty categoryProperty() { return category; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleIntegerProperty enrollmentsProperty() { return enrollments; }
        public SimpleStringProperty courseColorProperty() { return courseColor; }
    }
}