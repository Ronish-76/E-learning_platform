package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AssignmentsPage {
    private final Map<String, SubjectData> subjectDataMap = new HashMap<>();
    private final StringProperty currentSubject = new SimpleStringProperty();
    private final VBox dynamicContent = new VBox(20);
    
    public AssignmentsPage() {
        initializeSubjectData();
        currentSubject.addListener((observable, oldValue, newValue) -> updateContent(newValue));
    }

    private void initializeSubjectData() {
        // Initialize with sample data
        ObservableList<Assignment> scienceAssignments = FXCollections.observableArrayList(
            new Assignment("Ecosystem Diversity Worksheet", LocalDate.now().plusDays(1), "Not started", "25", "High"),
            new Assignment("Lab Report: Water Quality", LocalDate.now().plusDays(3), "In progress", "50", "Medium"),
            new Assignment("Environmental Impact Study", LocalDate.now().plusDays(7), "Not started", "100", "High")
        );
        
        ObservableList<Assignment> mathAssignments = FXCollections.observableArrayList(
            new Assignment("Quadratic Equations Practice", LocalDate.now().plusDays(2), "Not started", "20", "Medium"),
            new Assignment("Statistics Project", LocalDate.now().plusDays(5), "Not started", "75", "High"),
            new Assignment("Calculus Quiz", LocalDate.now(), "Due today", "30", "High")
        );
        
        ObservableList<Assignment> historyAssignments = FXCollections.observableArrayList(
            new Assignment("Ancient Civilizations Essay", LocalDate.now().plusDays(4), "Not started", "60", "Medium"),
            new Assignment("Historical Figure Presentation", LocalDate.now().plusDays(10), "Not started", "100", "High")
        );
        
        subjectDataMap.put("Science 2", new SubjectData("Science 2", "Period 1", "k6ertd", "#388E3C", scienceAssignments));
        subjectDataMap.put("Math 3", new SubjectData("Math 3", "Period 2", "m7athx", "#1976D2", mathAssignments));
        subjectDataMap.put("History", new SubjectData("History", "Period 3", "h8isty", "#D32F2F", historyAssignments));
    }

    public Node getView() {
        return getAssignmentsPage();
    }

    private BorderPane getAssignmentsPage() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(createNavBar());
        
        dynamicContent.setPadding(new Insets(10));
        dynamicContent.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        
        ScrollPane scrollPane = new ScrollPane(dynamicContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        mainLayout.setCenter(scrollPane);
        
        // Initialize with default subject
        currentSubject.set("Science ");
        
        return mainLayout;
    }
    
    private HBox createNavBar() {
        HBox navBar = new HBox(10);
        navBar.setPadding(new Insets(10));
        navBar.setStyle("-fx-background-color: #F5F5F5;");
        navBar.setAlignment(Pos.CENTER_LEFT);
        
        Label subjectLabel = new Label("Subject:");
        subjectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        ComboBox<String> subjectSelector = new ComboBox<>();
        subjectSelector.getItems().addAll(subjectDataMap.keySet());
        subjectSelector.setValue("Science 2");
        subjectSelector.valueProperty().bindBidirectional(currentSubject);
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> updateContent(currentSubject.get()));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search assignments...");
        searchField.setPrefWidth(200);
        
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchAssignments(searchField.getText()));
        
        navBar.getChildren().addAll(subjectLabel, subjectSelector, refreshButton, spacer, searchField, searchButton);
        
        return navBar;
    }
    
    private void updateContent(String subject) {
        dynamicContent.getChildren().clear();
        
        SubjectData data = subjectDataMap.get(subject);
        if (data != null) {
            VBox topSection = createTopSection(data);
            VBox statisticsSection = createStatisticsSection(data);
            VBox upcomingSection = createUpcomingSection(data);
            VBox postSection = createPostSection(data);
            
            dynamicContent.getChildren().addAll(topSection, statisticsSection, upcomingSection, postSection);
        }
    }

    private VBox createTopSection(SubjectData data) {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(15));
        topSection.setBackground(new Background(new BackgroundFill(Color.web(data.getColor()), CornerRadii.EMPTY, Insets.EMPTY)));

        Label subjectLabel = new Label(data.getSubjectName());
        subjectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        subjectLabel.setTextFill(Color.WHITE);

        Label periodLabel = new Label(data.getPeriod());
        periodLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        periodLabel.setTextFill(Color.WHITE);

        HBox codeBox = new HBox(20);
        codeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label classCodeLabel = new Label("Class code: " + data.getClassCode());
        classCodeLabel.setTextFill(Color.WHITE);
        
        Button copyCodeButton = new Button("Copy");
        copyCodeButton.setOnAction(e -> {
            // Simulate copying to clipboard
            System.out.println("Copied class code: " + data.getClassCode());
        });
        
        codeBox.getChildren().addAll(classCodeLabel, copyCodeButton);

        HBox meetBox = new HBox(20);
        meetBox.setAlignment(Pos.CENTER_LEFT);
        
        Button generateMeetButton = new Button("Generate Meet code");
        generateMeetButton.setOnAction(e -> {
            // Simulate generating a meet code
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Meet Code Generated");
            alert.setHeaderText(null);
            alert.setContentText("Meet code generated: " + data.getClassCode() + "-meet");
            alert.showAndWait();
        });
        
        meetBox.getChildren().add(generateMeetButton);

        VBox infoBox = new VBox(5, subjectLabel, periodLabel, codeBox, meetBox);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        topSection.getChildren().add(infoBox);
        return topSection;
    }
    
    private VBox createStatisticsSection(SubjectData data) {
        VBox statsSection = new VBox(10);
        statsSection.setPadding(new Insets(10));
        statsSection.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 10;");
        
        Label statsLabel = new Label("Class Statistics");
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        HBox statsCards = new HBox(15);
        statsCards.setAlignment(Pos.CENTER);
        
        VBox assignmentsCard = createStatsCard("Total Assignments", String.valueOf(data.getAssignments().size()), "#4CAF50");
        
        int dueToday = 0;
        int overdue = 0;
        for (Assignment assignment : data.getAssignments()) {
            if (assignment.getDueDate().equals(LocalDate.now())) {
                dueToday++;
            } else if (assignment.getDueDate().isBefore(LocalDate.now())) {
                overdue++;
            }
        }
        
        VBox dueTodayCard = createStatsCard("Due Today", String.valueOf(dueToday), "#2196F3");
        VBox overdueCard = createStatsCard("Overdue", String.valueOf(overdue), "#F44336");
        
        statsCards.getChildren().addAll(assignmentsCard, dueTodayCard, overdueCard);
        
        statsSection.getChildren().addAll(statsLabel, statsCards);
        return statsSection;
    }
    
    private VBox createStatsCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(150);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private VBox createUpcomingSection(SubjectData data) {
        VBox upcomingSection = new VBox(10);
        upcomingSection.setPadding(new Insets(10));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label upcomingLabel = new Label("Upcoming Assignments");
        upcomingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        ComboBox<String> sortOptions = new ComboBox<>();
        sortOptions.getItems().addAll("Due Date (Ascending)", "Due Date (Descending)", "Priority");
        sortOptions.setValue("Due Date (Ascending)");
        sortOptions.setOnAction(e -> sortAssignments(sortOptions.getValue(), data));
        
        header.getChildren().addAll(upcomingLabel, spacer, new Label("Sort by:"), sortOptions);

        TableView<Assignment> assignmentsTable = new TableView<>(data.getAssignments());
        assignmentsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        assignmentsTable.setPrefHeight(200);

        TableColumn<Assignment, String> assignmentColumn = new TableColumn<>("Assignment");
        assignmentColumn.setCellValueFactory(cellData -> cellData.getValue().assignmentNameProperty());
        assignmentColumn.setMinWidth(200);

        TableColumn<Assignment, String> dueDateColumn = new TableColumn<>("Due Date");
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().formattedDueDateProperty());
        dueDateColumn.setMinWidth(100);
        
        TableColumn<Assignment, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusColumn.setMinWidth(100);
        
        TableColumn<Assignment, String> pointsColumn = new TableColumn<>("Points");
        pointsColumn.setCellValueFactory(cellData -> cellData.getValue().pointsProperty());
        pointsColumn.setMinWidth(70);
        
        TableColumn<Assignment, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
        priorityColumn.setMinWidth(80);
        
        TableColumn<Assignment, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setMinWidth(150);
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button submitButton = new Button("Submit");
            
            {
                HBox buttonBox = new HBox(5, viewButton, submitButton);
                
                viewButton.setOnAction(event -> {
                    Assignment assignment = getTableView().getItems().get(getIndex());
                    viewAssignment(assignment);
                });
                
                submitButton.setOnAction(event -> {
                    Assignment assignment = getTableView().getItems().get(getIndex());
                    submitAssignment(assignment);
                });
                
                setGraphic(buttonBox);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });

        assignmentsTable.getColumns().addAll(assignmentColumn, dueDateColumn, statusColumn, pointsColumn, priorityColumn, actionsColumn);

        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewAllButton = new Button("View All");
        viewAllButton.setOnAction(e -> viewAllAssignments(data));
        
        Button addAssignmentButton = new Button("Add Assignment");
        addAssignmentButton.setOnAction(e -> addNewAssignment(data));
        
        buttonBar.getChildren().addAll(viewAllButton, addAssignmentButton);

        upcomingSection.getChildren().addAll(header, assignmentsTable, buttonBar);
        return upcomingSection;
    }

    private VBox createPostSection(SubjectData data) {
        VBox postSection = new VBox(10);
        postSection.setPadding(new Insets(15));
        postSection.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10;");

        Label sectionTitle = new Label("Class Materials");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        VBox materialCard = new VBox(10);
        materialCard.setPadding(new Insets(15));
        materialCard.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label postLabel = new Label("Watch this video about ecosystem diversity.");
        postLabel.setWrapText(true);
        postLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        HBox videoBox = new HBox(10);
        videoBox.setAlignment(Pos.CENTER_LEFT);
        
        // Placeholder for video thumbnail
        Region videoThumbnail = new Region();
        videoThumbnail.setPrefSize(120, 70);
        videoThumbnail.setStyle("-fx-background-color: #DDD;");
        
        VBox videoInfo = new VBox(5);
        Label videoLabel = new Label("The World's Oceans Ecosystem");
        videoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        videoLabel.setTextFill(Color.BLUE);
        
        Label videoDetails = new Label("YouTube - 3:56 minutes");
        videoDetails.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        videoInfo.getChildren().addAll(videoLabel, videoDetails);
        videoBox.getChildren().addAll(videoThumbnail, videoInfo);
        
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        Button watchButton = new Button("Watch Video");
        watchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        watchButton.setOnAction(e -> watchVideo());
        
        Button downloadButton = new Button("Download Resources");
        downloadButton.setOnAction(e -> downloadResources());
        
        buttonBar.getChildren().addAll(downloadButton, watchButton);
        
        materialCard.getChildren().addAll(postLabel, videoBox, buttonBar);
        
        Button addMaterialButton = new Button("Add Class Material");
        addMaterialButton.setOnAction(e -> addClassMaterial());
        
        postSection.getChildren().addAll(sectionTitle, materialCard, addMaterialButton);
        return postSection;
    }
    
    // Helper methods for the added functionality
    private void sortAssignments(String sortMethod, SubjectData data) {
        // Implementation of sorting logic
        switch (sortMethod) {
            case "Due Date (Ascending)":
                data.getAssignments().sort((a1, a2) -> a1.getDueDate().compareTo(a2.getDueDate()));
                break;
            case "Due Date (Descending)":
                data.getAssignments().sort((a1, a2) -> a2.getDueDate().compareTo(a1.getDueDate()));
                break;
            case "Priority":
                data.getAssignments().sort((a1, a2) -> {
                    // Sort by priority: High, Medium, Low
                    int p1 = getPriorityValue(a1.getPriority());
                    int p2 = getPriorityValue(a2.getPriority());
                    return Integer.compare(p1, p2);
                });
                break;
        }
    }
    
    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High": return 1;
            case "Medium": return 2;
            case "Low": return 3;
            default: return 4;
        }
    }
    
    private void viewAssignment(Assignment assignment) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Assignment Details");
        alert.setHeaderText(assignment.getAssignmentName());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label dueDateLabel = new Label("Due Date: " + assignment.getFormattedDueDate());
        Label statusLabel = new Label("Status: " + assignment.getStatus());
        Label pointsLabel = new Label("Points: " + assignment.getPoints());
        Label priorityLabel = new Label("Priority: " + assignment.getPriority());
        Label descriptionLabel = new Label("Description: This is a sample description for the assignment. It contains details about what students need to do to complete the assignment.");
        descriptionLabel.setWrapText(true);
        
        content.getChildren().addAll(dueDateLabel, statusLabel, pointsLabel, priorityLabel, descriptionLabel);
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    
    private void submitAssignment(Assignment assignment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Submit Assignment");
        alert.setHeaderText("Submit " + assignment.getAssignmentName());
        alert.setContentText("Are you sure you want to submit this assignment?");
        
        ButtonType submitButton = new ButtonType("Submit");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(submitButton, cancelButton);
        
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == submitButton) {
                // Simulate submission
                assignment.setStatus("Submitted");
                // Update the UI
                updateContent(currentSubject.get());
            }
        });
    }
    
    private void viewAllAssignments(SubjectData data) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("All Assignments");
        alert.setHeaderText(data.getSubjectName() + " - All Assignments");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        for (Assignment assignment : data.getAssignments()) {
            Label assignmentLabel = new Label(assignment.getAssignmentName() + " - " + assignment.getFormattedDueDate());
            content.getChildren().add(assignmentLabel);
        }
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    
    private void addNewAssignment(SubjectData data) {
        Dialog<Assignment> dialog = new Dialog<>();
        dialog.setTitle("Add New Assignment");
        dialog.setHeaderText("Enter Assignment Details");
        
        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create the form content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Assignment Name");
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Not started", "In progress", "Completed");
        statusCombo.setValue("Not started");
        
        TextField pointsField = new TextField();
        pointsField.setPromptText("Points");
        
        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("High", "Medium", "Low");
        priorityCombo.setValue("Medium");
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Due Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusCombo, 1, 2);
        grid.add(new Label("Points:"), 0, 3);
        grid.add(pointsField, 1, 3);
        grid.add(new Label("Priority:"), 0, 4);
        grid.add(priorityCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result to an assignment object when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Assignment(
                    nameField.getText(),
                    datePicker.getValue(),
                    statusCombo.getValue(),
                    pointsField.getText(),
                    priorityCombo.getValue()
                );
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(assignment -> {
            data.getAssignments().add(assignment);
            updateContent(currentSubject.get());
        });
    }
    
    private void searchAssignments(String query) {
        if (query == null || query.trim().isEmpty()) {
            updateContent(currentSubject.get());
            return;
        }
        
        SubjectData data = subjectDataMap.get(currentSubject.get());
        if (data != null) {
            ObservableList<Assignment> filteredAssignments = FXCollections.observableArrayList();
            
            for (Assignment assignment : data.getAssignments()) {
                if (assignment.getAssignmentName().toLowerCase().contains(query.toLowerCase())) {
                    filteredAssignments.add(assignment);
                }
            }
            
            // Create a temporary SubjectData object with filtered assignments
            SubjectData filteredData = new SubjectData(
                data.getSubjectName(),
                data.getPeriod(),
                data.getClassCode(),
                data.getColor(),
                filteredAssignments
            );
            
            // Update UI with filtered data
            dynamicContent.getChildren().clear();
            VBox topSection = createTopSection(data);
            VBox statisticsSection = createStatisticsSection(data);
            VBox upcomingSection = createUpcomingSection(filteredData);
            VBox postSection = createPostSection(data);
            
            dynamicContent.getChildren().addAll(topSection, statisticsSection, upcomingSection, postSection);
        }
    }
    
    private void watchVideo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Video Player");
        alert.setHeaderText("The World's Oceans Ecosystem");
        alert.setContentText("Video player would open here.");
        alert.showAndWait();
    }
    
    private void downloadResources() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Download Resources");
        alert.setHeaderText("Downloading Resources");
        alert.setContentText("Resources are being downloaded.");
        alert.showAndWait();
    }
    
    private void addClassMaterial() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Class Material");
        alert.setHeaderText("Add New Class Material");
        alert.setContentText("This feature would allow teachers to add new class materials.");
        alert.showAndWait();
    }

    // Model classes
    public static class Assignment {
        private final StringProperty assignmentName;
        private final LocalDate dueDate;
        private final StringProperty formattedDueDate;
        private final StringProperty status;
        private final StringProperty points;
        private final StringProperty priority;
        
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

        public Assignment(String assignmentName, LocalDate dueDate, String status, String points, String priority) {
            this.assignmentName = new SimpleStringProperty(assignmentName);
            this.dueDate = dueDate;
            this.formattedDueDate = new SimpleStringProperty(formatDueDate(dueDate));
            this.status = new SimpleStringProperty(status);
            this.points = new SimpleStringProperty(points);
            this.priority = new SimpleStringProperty(priority);
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
        
        public void setStatus(String newStatus) {
            status.set(newStatus);
        }
    }
    
    public static class SubjectData {
        private final String subjectName;
        private final String period;
        private final String classCode;
        private final String color;
        private final ObservableList<Assignment> assignments;
        
        public SubjectData(String subjectName, String period, String classCode, String color, ObservableList<Assignment> assignments) {
            this.subjectName = subjectName;
            this.period = period;
            this.classCode = classCode;
            this.color = color;
            this.assignments = assignments;
        }
        
        public String getSubjectName() { return subjectName; }
        public String getPeriod() { return period; }
        public String getClassCode() { return classCode; }
        public String getColor() { return color; }
        public ObservableList<Assignment> getAssignments() { return assignments; }
    }
}