package controllers_Instructors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

/**
 * Assignment page for managing course assignments
 */
public class AssignmentPage {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Assignments");
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
        statusFilter.getItems().addAll("All Status", "Open", "Closed", "Upcoming", "In Review");
        statusFilter.setValue("All Status");
        
        CheckBox needsReviewCheck = new CheckBox("Needs Review");
        
        Button applyFilterBtn = new Button("Apply");
        applyFilterBtn.getStyleClass().add("primary-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button createAssignmentBtn = new Button("+ Create Assignment");
        createAssignmentBtn.getStyleClass().add("success-button");
        
        filterContainer.getChildren().addAll(
            courseFilter, statusFilter, needsReviewCheck, applyFilterBtn, spacer, createAssignmentBtn);
        
        // Assignments tabs
        TabPane assignmentTabs = new TabPane();
        assignmentTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab activeTab = new Tab("Active");
        activeTab.setContent(createAssignmentTable(false));
        
        Tab gradedTab = new Tab("Graded");
        gradedTab.setContent(createAssignmentTable(true));
        
        Tab draftTab = new Tab("Drafts");
        draftTab.setContent(createDraftAssignmentsTable());
        
        assignmentTabs.getTabs().addAll(activeTab, gradedTab, draftTab);
        VBox.setVgrow(assignmentTabs, Priority.ALWAYS);
        
        view.getChildren().addAll(title, filterContainer, assignmentTabs);
        return view;
    }
    
    private Node createAssignmentTable(boolean graded) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        
        TableView<AssignmentData> table = new TableView<>();
        
        // Create columns
        TableColumn<AssignmentData, String> titleColumn = new TableColumn<>("Assignment Title");
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        titleColumn.setPrefWidth(250);
        
        TableColumn<AssignmentData, String> courseColumn = new TableColumn<>("Course");
        courseColumn.setCellValueFactory(cellData -> cellData.getValue().courseProperty());
        courseColumn.setPrefWidth(200);
        
        TableColumn<AssignmentData, String> dateColumn = new TableColumn<>("Due Date");
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
        
        TableColumn<AssignmentData, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        
        TableColumn<AssignmentData, String> submissionsColumn = new TableColumn<>("Submissions");
        submissionsColumn.setCellValueFactory(cellData -> cellData.getValue().submissionsProperty());
        
        TableColumn<AssignmentData, String> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(col -> new TableCell<AssignmentData, String>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button gradeBtn = new Button("Grade");
            
            {
                viewBtn.getStyleClass().add("view-button");
                editBtn.getStyleClass().add("edit-button");
                gradeBtn.getStyleClass().add("grade-button");
                
                viewBtn.setOnAction(event -> {
                    AssignmentData data = getTableView().getItems().get(getIndex());
                    // Handle view action
                    System.out.println("View assignment: " + data.getTitle());
                });
                
                editBtn.setOnAction(event -> {
                    AssignmentData data = getTableView().getItems().get(getIndex());
                    // Handle edit action
                    System.out.println("Edit assignment: " + data.getTitle());
                });
                
                gradeBtn.setOnAction(event -> {
                    AssignmentData data = getTableView().getItems().get(getIndex());
                    // Handle grade action
                    System.out.println("Grade assignment: " + data.getTitle());
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    AssignmentData data = getTableView().getItems().get(getIndex());
                    buttons.getChildren().add(viewBtn);
                    
                    if (!graded) {
                        buttons.getChildren().add(editBtn);
                    }
                    
                    if (data.getStatus().equals("In Review")) {
                        buttons.getChildren().add(gradeBtn);
                    }
                    
                    setGraphic(buttons);
                }
            }
        });
        
        table.getColumns().addAll(
            titleColumn, courseColumn, dateColumn, statusColumn, submissionsColumn, actionsColumn);
        
        // Add sample data
        ObservableList<AssignmentData> data = FXCollections.observableArrayList();
        
        if (graded) {
            data.addAll(
                new AssignmentData("Python Basics Quiz", "Introduction to Python", "May 15, 2023", "Closed", "142/142"),
                new AssignmentData("HTML & CSS Project", "Web Development Fundamentals", "May 20, 2023", "Closed", "85/87"),
                new AssignmentData("Data Visualization Assignment", "Data Science Essentials", "May 25, 2023", "Closed", "62/64")
            );
        } else {
            data.addAll(
                new AssignmentData("Final Python Project", "Introduction to Python", "June 25, 2023", "Open", "98/142"),
                new AssignmentData("JavaScript Functions Exercise", "Web Development Fundamentals", "June 22, 2023", "Open", "45/87"),
                new AssignmentData("Machine Learning Project", "Data Science Essentials", "July 5, 2023", "Upcoming", "0/64"),
                new AssignmentData("Web API Integration", "Web Development Fundamentals", "June 18, 2023", "In Review", "87/87")
            );
        }
        
        table.setItems(data);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        // Add a label for instructions if needed
        Label instruction = new Label(graded ? 
            "Showing graded assignments. Click on an assignment to see details." :
            "Showing active assignments. Remember to grade submissions on time.");
        instruction.getStyleClass().add("table-instruction");
        
        container.getChildren().addAll(instruction, table);
        return container;
    }
    
    private Node createDraftAssignmentsTable() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        
        TableView<AssignmentData> table = new TableView<>();
        
        // Create columns
        TableColumn<AssignmentData, String> titleColumn = new TableColumn<>("Assignment Title");
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        titleColumn.setPrefWidth(250);
        
        TableColumn<AssignmentData, String> courseColumn = new TableColumn<>("Course");
        courseColumn.setCellValueFactory(cellData -> cellData.getValue().courseProperty());
        courseColumn.setPrefWidth(200);
        
        TableColumn<AssignmentData, String> lastEditedColumn = new TableColumn<>("Last Edited");
        lastEditedColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
        
        TableColumn<AssignmentData, String> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(col -> new TableCell<AssignmentData, String>() {
            private final Button editBtn = new Button("Edit");
            private final Button publishBtn = new Button("Publish");
            private final Button deleteBtn = new Button("Delete");
            
            {
                editBtn.getStyleClass().add("edit-button");
                publishBtn.getStyleClass().add("publish-button");
                deleteBtn.getStyleClass().add("delete-button");
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(editBtn, publishBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
        
        table.getColumns().addAll(titleColumn, courseColumn, lastEditedColumn, actionsColumn);
        
        // Add sample data
        ObservableList<AssignmentData> data = FXCollections.observableArrayList(
            new AssignmentData("Advanced Python Concepts", "Introduction to Python", "June 10, 2023", "Draft", "N/A"),
            new AssignmentData("Responsive Design Project", "Web Development Fundamentals", "June 15, 2023", "Draft", "N/A")
        );
        
        table.setItems(data);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        // Add a label for instructions
        Label instruction = new Label("These are draft assignments. You can edit and publish them when ready.");
        instruction.getStyleClass().add("table-instruction");
        
        container.getChildren().addAll(instruction, table);
        return container;
    }
}