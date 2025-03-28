package controllers_Admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Course Management page for admin operations on courses
 */
public class CourseManagementPage {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Course Management");
        title.getStyleClass().add("page-title");
        
        // Filter and actions section
        HBox actionContainer = new HBox(10);
        actionContainer.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search courses...");
        searchField.setPrefWidth(300);
        
        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("All Categories", "Programming", "Data Science", "Business", "Design");
        categoryFilter.setValue("All Categories");
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "Published", "Draft", "Archived");
        statusFilter.setValue("All Status");
        
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("primary-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addCourseBtn = new Button("Add New Course");
        addCourseBtn.getStyleClass().add("success-button");
        
        actionContainer.getChildren().addAll(searchField, categoryFilter, statusFilter, searchBtn, spacer, addCourseBtn);
        
        // Course cards
        FlowPane courseGrid = createCourseGrid();
        courseGrid.getStyleClass().add("course-grid");
        VBox.setVgrow(courseGrid, Priority.ALWAYS);
        
        view.getChildren().addAll(title, actionContainer, courseGrid);
        return view;
    }
    
    private FlowPane createCourseGrid() {
        FlowPane grid = new FlowPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));
        
        // Add sample course cards
        grid.getChildren().addAll(
            createCourseCard("Introduction to Programming", "John Smith", "Programming", "Published", 125),
            createCourseCard("Advanced Data Science", "Jane Doe", "Data Science", "Published", 98),
            createCourseCard("Business Analytics", "Bob Johnson", "Business", "Draft", 0),
            createCourseCard("UI/UX Design Principles", "Alice Williams", "Design", "Published", 210),
            createCourseCard("Machine Learning Basics", "Charlie Brown", "Data Science", "Published", 156),
            createCourseCard("Web Development Bootcamp", "Diana Prince", "Programming", "Archived", 430)
        );
        
        return grid;
    }
    
    private VBox createCourseCard(String title, String instructor, String category, String status, int enrollments) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("course-card");
        card.setPrefWidth(300);
        
        // Course status indicator
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("status-label");
        statusLabel.getStyleClass().add(status.toLowerCase() + "-status");
        statusBox.getChildren().add(statusLabel);
        
        // Course title
        Label courseTitle = new Label(title);
        courseTitle.getStyleClass().add("course-title");
        courseTitle.setWrapText(true);
        
        // Course info
        Label instructorLabel = new Label("Instructor: " + instructor);
        instructorLabel.getStyleClass().add("course-instructor");
        
        Label categoryLabel = new Label("Category: " + category);
        categoryLabel.getStyleClass().add("course-category");
        
        Label enrollmentLabel = new Label(enrollments + " Students Enrolled");
        enrollmentLabel.getStyleClass().add("course-enrollment");
        
        // Action buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("edit-button");
        
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("view-button");
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-button");
        
        buttonsBox.getChildren().addAll(editBtn, viewBtn, deleteBtn);
        
        card.getChildren().addAll(statusBox, courseTitle, instructorLabel, categoryLabel, enrollmentLabel, buttonsBox);
        
        return card;
    }
}