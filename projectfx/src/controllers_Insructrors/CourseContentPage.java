package controllers_Insructrors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Course content page for managing course materials
 */
public class CourseContentPage {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Course Materials");
        title.getStyleClass().add("page-title");
        
        // Course selector
        HBox courseSelector = new HBox(10);
        courseSelector.setAlignment(Pos.CENTER_LEFT);
        
        Label courseLabel = new Label("Select Course:");
        
        ComboBox<String> courseDropdown = new ComboBox<>();
        courseDropdown.getItems().addAll(
            "Introduction to Python", 
            "Web Development Fundamentals",
            "Data Science Essentials"
        );
        courseDropdown.setValue("Introduction to Python");
        courseDropdown.setPrefWidth(250);
        
        Button createModuleBtn = new Button("+ Add Module");
        createModuleBtn.getStyleClass().add("primary-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button previewCourseBtn = new Button("Preview Course");
        previewCourseBtn.getStyleClass().add("secondary-button");
        
        courseSelector.getChildren().addAll(
            courseLabel, courseDropdown, spacer, createModuleBtn, previewCourseBtn);
        
        // Course content - Using an accordion to organize modules
        Accordion moduleAccordion = new Accordion();
        
        // Module 1
        TitledPane module1 = createCourseModule(
            "Module 1: Introduction to Programming",
            new String[]{
                "Lecture: Getting Started with Python",
                "Reading: Programming Fundamentals",
                "Video: Setting Up Your Environment",
                "Quiz: Programming Basics",
                "Assignment: Your First Program"
            }
        );
        
        // Module 2
        TitledPane module2 = createCourseModule(
            "Module 2: Variables and Data Types",
            new String[]{
                "Lecture: Understanding Variables",
                "Reading: Python Data Types",
                "Video: Working with Strings and Numbers",
                "Quiz: Data Types and Operations",
                "Assignment: Data Manipulation"
            }
        );
        
        // Module 3
        TitledPane module3 = createCourseModule(
            "Module 3: Control Flow",
            new String[]{
                "Lecture: Conditional Statements",
                "Reading: Loops in Python",
                "Video: Control Flow Examples",
                "Quiz: Conditional Logic",
                "Assignment: Control Flow Challenge"
            }
        );
        
        // Final Project
        TitledPane finalProject = createCourseModule(
            "Final Project",
            new String[]{
                "Project Guidelines",
                "Submission Requirements",
                "Grading Rubric",
                "Sample Projects"
            }
        );
        
        moduleAccordion.getPanes().addAll(module1, module2, module3, finalProject);
        
        // Expand the first module by default
        moduleAccordion.setExpandedPane(module1);
        VBox.setVgrow(moduleAccordion, Priority.ALWAYS);
        
        view.getChildren().addAll(title, courseSelector, moduleAccordion);
        return view;
    }
    
    private TitledPane createCourseModule(String moduleTitle, String[] contentItems) {
        VBox moduleContent = new VBox(5);
        moduleContent.setPadding(new Insets(10));
        
        Button addContentBtn = new Button("+ Add Content");
        addContentBtn.getStyleClass().add("add-content-button");
        moduleContent.getChildren().add(addContentBtn);
        
        // Add content items
        for (String item : contentItems) {
            HBox contentItem = createContentItem(item);
            moduleContent.getChildren().add(contentItem);
        }
        
        TitledPane modulePane = new TitledPane(moduleTitle, moduleContent);
        modulePane.getStyleClass().add("module-pane");
        
        // Module actions
        HBox moduleActions = new HBox(10);
        moduleActions.setPadding(new Insets(10, 0, 0, 0));
        moduleActions.setAlignment(Pos.CENTER_RIGHT);
        
        Button reorderBtn = new Button("Reorder Items");
        reorderBtn.getStyleClass().add("secondary-button");
        
        Button moduleSettingsBtn = new Button("Module Settings");
        moduleSettingsBtn.getStyleClass().add("secondary-button");
        
        moduleActions.getChildren().addAll(reorderBtn, moduleSettingsBtn);
        moduleContent.getChildren().add(moduleActions);
        
        return modulePane;
    }
    
    private HBox createContentItem(String title) {
        HBox item = new HBox();
        item.setPadding(new Insets(10));
        item.setSpacing(10);
        item.getStyleClass().add("content-item");
        
        // Determine icon based on content type
        String icon = "📄"; // Default document icon
        if (title.startsWith("Lecture:")) {
            icon = "📚";
        } else if (title.startsWith("Video:")) {
            icon = "🎬";
        } else if (title.startsWith("Quiz:")) {
            icon = "📝";
        } else if (title.startsWith("Assignment:")) {
            icon = "📋";
        } else if (title.startsWith("Reading:")) {
            icon = "📖";
        }
        
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("content-icon");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("content-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        HBox actionButtons = new HBox(5);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("sm-edit-button");
        
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("sm-view-button");
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("sm-delete-button");
        
        actionButtons.getChildren().addAll(editBtn, viewBtn, deleteBtn);
        
        item.getChildren().addAll(iconLabel, titleLabel, actionButtons);
        return item;
    }
}