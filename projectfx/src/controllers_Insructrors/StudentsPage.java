package controllers_Insructrors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Students page for viewing and managing course students
 */
public class StudentsPage {
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
        
        ComboBox<String> courseFilter = new ComboBox<>();
        courseFilter.getItems().addAll("All Courses", "Introduction to Python", 
                "Web Development Fundamentals", "Data Science Essentials");
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
        
        // Students table
        TableView<StudentData> studentsTable = createStudentsTable();
        VBox.setVgrow(studentsTable, Priority.ALWAYS);
        
        // Summary statistics
        HBox summaryStats = new HBox(20);
        summaryStats.setPadding(new Insets(15));
        summaryStats.setAlignment(Pos.CENTER);
        
        VBox totalStudents = createStatBox("Total Students", "248", "");
        VBox activeStudents = createStatBox("Active This Week", "156", "62.9%");
        VBox avgProgress = createStatBox("Average Progress", "68.5%", "");
        VBox avgGrade = createStatBox("Average Grade", "B+", "87.2%");
        
        summaryStats.getChildren().addAll(totalStudents, activeStudents, avgProgress, avgGrade);
        
        view.getChildren().addAll(title, filterContainer, studentsTable, summaryStats);
        return view;
    }
    
    private TableView<StudentData> createStudentsTable() {
        TableView<StudentData> table = new TableView<>();
        
        // Create columns
        TableColumn<StudentData, String> nameColumn = new TableColumn<>("Student Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameColumn.setPrefWidth(200);
        
        TableColumn<StudentData, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        emailColumn.setPrefWidth(200);
        
        TableColumn<StudentData, String> courseColumn = new TableColumn<>("Course");
        courseColumn.setCellValueFactory(cellData -> cellData.getValue().courseProperty());
        courseColumn.setPrefWidth(180);
        
        TableColumn<StudentData, String> progressColumn = new TableColumn<>("Progress");
        progressColumn.setCellFactory(col -> new TableCell<StudentData, String>() {
            private final ProgressBar progressBar = new ProgressBar();
            private final Label progressLabel = new Label();
            
            @Override
            protected void updateItem(String item, boolean empty) {
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
        
        TableColumn<StudentData, String> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellValueFactory(cellData -> cellData.getValue().gradeProperty());
        
        TableColumn<StudentData, String> lastActiveColumn = new TableColumn<>("Last Active");
        lastActiveColumn.setCellValueFactory(cellData -> cellData.getValue().lastActiveProperty());
        
        TableColumn<StudentData, String> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(col -> new TableCell<StudentData, String>() {
            private final Button viewBtn = new Button("View");
            private final Button messageBtn = new Button("Message");
            
            {
                viewBtn.getStyleClass().add("view-button");
                messageBtn.getStyleClass().add("message-button");
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
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
        
        table.getColumns().addAll(
            nameColumn, emailColumn, courseColumn, progressColumn, 
            gradeColumn, lastActiveColumn, actionsColumn);
        
        // Add sample data
        ObservableList<StudentData> data = FXCollections.observableArrayList(
            new StudentData("John Smith", "john.smith@example.com", "Introduction to Python", 0.85, "A-", "Today"),
            new StudentData("Emma Wilson", "emma.wilson@example.com", "Web Development Fundamentals", 0.72, "B+", "Yesterday"),
            new StudentData("Michael Brown", "michael.brown@example.com", "Data Science Essentials", 0.45, "C", "3 days ago"),
            new StudentData("Sarah Johnson", "sarah.j@example.com", "Introduction to Python", 0.92, "A", "Today"),
            new StudentData("Robert Thompson", "robert.t@example.com", "Web Development Fundamentals", 0.68, "B", "2 days ago"),
            new StudentData("Jennifer Davis", "jennifer.d@example.com", "Data Science Essentials", 0.33, "D+", "1 week ago"),
            new StudentData("David Wilson", "david.w@example.com", "Introduction to Python", 0.78, "B+", "Yesterday"),
            new StudentData("Lisa Anderson", "lisa.a@example.com", "Web Development Fundamentals", 0.90, "A", "Today")
        );
        
        table.setItems(data);
        return table;
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