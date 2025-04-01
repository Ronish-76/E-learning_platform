package controllers_Instructors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * My Courses page for managing instructor's courses
 */
public class MyCourses {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("My Courses");
        title.getStyleClass().add("page-title");
        
        // Filter and actions section
        HBox actionContainer = new HBox(10);
        actionContainer.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search courses...");
        searchField.setPrefWidth(300);
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "Active", "Draft", "Archived");
        statusFilter.setValue("All Status");
        
        ComboBox<String> sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll("Newest First", "Oldest First", "A-Z", "Z-A", "Most Students");
        sortFilter.setValue("Newest First");
        
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("primary-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addCourseBtn = new Button("+ Create New Course");
        addCourseBtn.getStyleClass().add("success-button");
        
        actionContainer.getChildren().addAll(searchField, statusFilter, sortFilter, searchBtn, spacer, addCourseBtn);
        
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
            createCourseCard("Introduction to Python", "Programming", "Active", 142, 4.8),
            createCourseCard("Web Development Fundamentals", "Web", "Active", 87, 4.7),
            createCourseCard("Data Science Essentials", "Data Science", "Active", 64, 4.6),
            createCourseCard("Machine Learning Basics", "Data Science", "Draft", 0, 0),
            createCourseCard("Mobile App Development", "Programming", "Archived", 93, 4.2)
        );
        
        return grid;
    }
    
    private VBox createCourseCard(String title, String category, String status, int students, double rating) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(0, 0, 15, 0));
        card.getStyleClass().add("course-card");
        card.setPrefWidth(300);
        
        // Course image placeholder
        Rectangle imagePlaceholder = new Rectangle(300, 150);
        imagePlaceholder.setArcWidth(10);
        imagePlaceholder.setArcHeight(10);
        
        if (status.equals("Active")) {
            imagePlaceholder.setFill(Color.valueOf("#34495e"));
        } else if (status.equals("Draft")) {
            imagePlaceholder.setFill(Color.valueOf("#7f8c8d"));
        } else {
            imagePlaceholder.setFill(Color.valueOf("#95a5a6"));
        }
        
        // Status badge
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("status-label");
        statusLabel.getStyleClass().add(status.toLowerCase() + "-status");
        
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().addAll(imagePlaceholder, statusLabel);
        StackPane.setAlignment(statusLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(statusLabel, new Insets(10, 10, 0, 0));
        
        // Course details
        VBox details = new VBox(8);
        details.setPadding(new Insets(15));
        
        Label courseTitle = new Label(title);
        courseTitle.getStyleClass().add("course-title");
        courseTitle.setWrapText(true);
        
        Label categoryLabel = new Label("Category: " + category);
        categoryLabel.getStyleClass().add("course-category");
        
        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        
        Label studentsLabel = new Label(students + " Students");
        studentsLabel.getStyleClass().add("course-students");
        
        // Create rating stars
        HBox ratingBox = new HBox(2);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        
        if (students > 0) {
            Label ratingLabel = new Label(String.format("%.1f", rating));
            ratingLabel.getStyleClass().add("course-rating");
            
            Label starsLabel = new Label("★★★★★");
            starsLabel.getStyleClass().add("rating-stars");
            
            ratingBox.getChildren().addAll(ratingLabel, starsLabel);
        } else {
            Label noRatingLabel = new Label("No ratings yet");
            noRatingLabel.getStyleClass().add("no-rating");
            
            ratingBox.getChildren().add(noRatingLabel);
        }
        
        statsRow.getChildren().addAll(studentsLabel, ratingBox);
        
        // Action buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("edit-button");
        
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("view-button");
        
        Button statsBtn = new Button("Stats");
        statsBtn.getStyleClass().add("stats-button");
        
        buttonsBox.getChildren().addAll(editBtn, viewBtn, statsBtn);
        
        details.getChildren().addAll(courseTitle, categoryLabel, statsRow, buttonsBox);
        
        card.getChildren().addAll(imageContainer, details);
        return card;
    }
}