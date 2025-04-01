package controllers_Instructors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

/**
 * Quizzes page for managing course quizzes
 */
public class QuizzesPage {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Quizzes");
        title.getStyleClass().add("page-title");
        
        // Filter controls
        HBox filterContainer = new HBox(10);
        filterContainer.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> courseFilter = new ComboBox<>();
        courseFilter.getItems().addAll("All Courses", "Introduction to Python", 
                "Web Development Fundamentals", "Data Science Essentials");
        courseFilter.setValue("All Courses");
        courseFilter.setPrefWidth(200);
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "Active", "Completed", "Scheduled", "Draft");
        statusFilter.setValue("All Status");
        
        Button applyFilterBtn = new Button("Apply");
        applyFilterBtn.getStyleClass().add("primary-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button createQuizBtn = new Button("+ Create Quiz");
        createQuizBtn.getStyleClass().add("success-button");
        
        filterContainer.getChildren().addAll(
            courseFilter, statusFilter, applyFilterBtn, spacer, createQuizBtn);
        
        // Quizzes list
        VBox quizzesList = new VBox(15);
        quizzesList.setPadding(new Insets(10));
        quizzesList.getChildren().addAll(
            createQuizItem("Python Basics Quiz", "Introduction to Python", "Active", "June 25, 2023", 142, 98),
            createQuizItem("HTML & CSS Assessment", "Web Development Fundamentals", "Completed", "May 30, 2023", 87, 87),
            createQuizItem("Data Structures Quiz", "Introduction to Python", "Active", "June 20, 2023", 142, 76),
            createQuizItem("JavaScript Fundamentals", "Web Development Fundamentals", "Scheduled", "July 5, 2023", 87, 0),
            createQuizItem("Statistics Quiz", "Data Science Essentials", "Draft", "Not published", 0, 0)
        );
        
        ScrollPane scrollPane = new ScrollPane(quizzesList);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        view.getChildren().addAll(title, filterContainer, scrollPane);
        return view;
    }
    
    private HBox createQuizItem(String title, String course, String status, 
                                String date, int totalStudents, int completed) {
        HBox item = new HBox();
        item.setPadding(new Insets(15));
        item.setSpacing(15);
        item.getStyleClass().add("quiz-item");
        
        // Status indicator - using fixed height instead of binding
        Rectangle statusIndicator = new Rectangle(8, 80);  // Fixed height of 80px
        if (status.equals("Active")) {
            statusIndicator.getStyleClass().add("active-indicator");
        } else if (status.equals("Completed")) {
            statusIndicator.getStyleClass().add("completed-indicator");
        } else if (status.equals("Scheduled")) {
            statusIndicator.getStyleClass().add("scheduled-indicator");
        } else {
            statusIndicator.getStyleClass().add("draft-indicator");
        }
        
        // Quiz information
        VBox quizInfo = new VBox(5);
        quizInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(quizInfo, Priority.ALWAYS);
        
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label quizTitle = new Label(title);
        quizTitle.getStyleClass().add("quiz-title");
        
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("status-label");
        statusLabel.getStyleClass().add(status.toLowerCase() + "-status");
        
        titleRow.getChildren().addAll(quizTitle, statusLabel);
        
        Label courseLabel = new Label("Course: " + course);
        courseLabel.getStyleClass().add("quiz-course");
        
        Label dateLabel = new Label(status.equals("Draft") ? "Not published yet" : 
                (status.equals("Completed") ? "Completed on: " + date : "Due: " + date));
        dateLabel.getStyleClass().add("quiz-date");
        
        quizInfo.getChildren().addAll(titleRow, courseLabel, dateLabel);
        
        // Quiz stats
        VBox statsBox = new VBox(10);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setMinWidth(150);
        
        if (!status.equals("Draft")) {
            double completionRate = totalStudents > 0 ? (double) completed / totalStudents * 100 : 0;
            
            Label completionLabel = new Label(String.format("%.1f%%", completionRate));
            completionLabel.getStyleClass().add("completion-rate");
            
            Label studentsLabel = new Label(completed + "/" + totalStudents + " Students");
            studentsLabel.getStyleClass().add("quiz-students");
            
            ProgressBar completionBar = new ProgressBar(completionRate / 100);
            completionBar.setPrefWidth(120);
            completionBar.getStyleClass().add("quiz-progress");
            
            statsBox.getChildren().addAll(completionLabel, completionBar, studentsLabel);
        } else {
            Label draftLabel = new Label("Draft");
            draftLabel.getStyleClass().add("draft-label");
            
            statsBox.getChildren().add(draftLabel);
        }
        
        // Actions
        VBox actionsBox = new VBox(5);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setMinWidth(100);
        
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("view-button");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("edit-button");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        
        Button resultsBtn = new Button("Results");
        resultsBtn.getStyleClass().add("results-button");
        resultsBtn.setMaxWidth(Double.MAX_VALUE);
        resultsBtn.setDisable(status.equals("Draft") || status.equals("Scheduled"));
        
        actionsBox.getChildren().addAll(viewBtn, editBtn);
        
        if (status.equals("Active") || status.equals("Completed")) {
            actionsBox.getChildren().add(resultsBtn);
        }
        
        item.getChildren().addAll(statusIndicator, quizInfo, statsBox, actionsBox);
        return item;
    }
}