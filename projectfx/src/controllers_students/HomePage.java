package controllers_students;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class HomePage {
    
    // Colors
    private final Color PRIMARY_LIGHT = Color.web("#f0f5ff");
    private final Color PRIMARY = Color.web("#4a86e8");
    private final Color SECONDARY = Color.web("#34a853");
    private final Color ACCENT = Color.web("#ea4335");
    private final Color NEUTRAL = Color.web("#f1f3f4");
    private final Color TEXT_PRIMARY = Color.web("#202124");
    private final Color TEXT_SECONDARY = Color.web("#5f6368");
    
    // Class data
    private final List<ClassData> classes = Arrays.asList(
            new ClassData("Mathematics", "Explore the fundamentals of calculus"),
            new ClassData("History", "Journey through major historical events"),
            new ClassData("Computer Science", "Learn programming and algorithms"),
            new ClassData("Biology", "Discover the science of living organisms"),
            new ClassData("Physics", "Understand the laws that govern the universe"),
            new ClassData("Literature", "Explore classic and modern literature")
    );
    
    public Node getView() {
        return getHomePage();
    }
    
    private ScrollPane getHomePage() {
        VBox homePageContent = new VBox(0); // No spacing between main sections
        homePageContent.setAlignment(Pos.TOP_CENTER);
        homePageContent.setStyle("-fx-background-color: #f4f4f4;");  // Light background color
        
        // 1. Top Section (Header)
        VBox headerSection = createHeaderSection();
        
        // 2. Main Content Section (Two Columns)
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER); // Changed to TOP_CENTER for better alignment
        mainContent.setPrefWidth(Double.MAX_VALUE); // Make sure it stretches to full width
        
        // Left Section: 'Join Classes'
        VBox leftSection = createLeftSection();
        HBox.setHgrow(leftSection, Priority.ALWAYS); // Make it grow to fill space
        
        // Right Section: 'Progress Overview'
        VBox rightSection = createRightSection();
        HBox.setHgrow(rightSection, Priority.ALWAYS); // Make it grow to fill space
        
        mainContent.getChildren().addAll(leftSection, rightSection);
        
        // 3. Bottom Section (Interactive Element for Assignment Management)
        VBox bottomSection = createBottomSection();
        
        homePageContent.getChildren().addAll(headerSection, mainContent, bottomSection);
        
        // Wrap content in ScrollPane
        ScrollPane scrollPane = new ScrollPane(homePageContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        return scrollPane;
    }
    
    private VBox createHeaderSection() {
        VBox header = new VBox();
        
        // Welcome Message with gradient background
        StackPane welcomePane = new StackPane();
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4a86e8")), new Stop(1, Color.web("#34a853")));
        welcomePane.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
        welcomePane.setPrefHeight(80);
        
        Text welcomeText = new Text("Welcome to Your Learning Journey!");
        welcomeText.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        welcomeText.setFill(Color.WHITE);
        
        welcomePane.getChildren().add(welcomeText);
        header.getChildren().add(welcomePane);
        
        // Empty Navigation Bar
        HBox navBar = new HBox(20);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(10, 20, 10, 20));
        navBar.setBackground(new Background(new BackgroundFill(NEUTRAL, CornerRadii.EMPTY, Insets.EMPTY)));
        
        // Navigation buttons removed as requested
        
        header.getChildren().add(navBar);
        
        return header;
    }
    
    private VBox createLeftSection() {
        VBox leftSection = new VBox(20);
        leftSection.setPrefWidth(550);
        leftSection.setPadding(new Insets(15));
        leftSection.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        leftSection.setAlignment(Pos.TOP_CENTER); // Center align contents
        
        // Section Title
        Text sectionTitle = new Text("Join Classes");
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 22));
        sectionTitle.setFill(TEXT_PRIMARY);
        
        // Grid for class cards
        GridPane classGrid = new GridPane();
        classGrid.setHgap(15);
        classGrid.setVgap(15);
        classGrid.setAlignment(Pos.CENTER); // Center align the grid
        
        int col = 0;
        int row = 0;
        
        for (ClassData classData : classes) {
            VBox classCard = createClassCard(classData);
            
            // Add card to grid
            classGrid.add(classCard, col, row);
            
            // Update grid position
            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }
        
        // Removed subject assignment button as requested
        
        leftSection.getChildren().addAll(sectionTitle, classGrid);
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(5);
        leftSection.setEffect(dropShadow);
        
        return leftSection;
    }
    
    private VBox createClassCard(ClassData classData) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(240);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        
        // Removed image placeholder as requested
        
        // Subject Title
        Label titleLabel = new Label(classData.getName());
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
        titleLabel.setTextFill(TEXT_PRIMARY);
        
        // Join Button with confirmation dialog
        Button joinButton = new Button("Join");
        joinButton.setStyle("-fx-background-color: #34a853; -fx-text-fill: white;");
        
        // Add confirmation dialog on button click
        joinButton.setOnAction(e -> {
            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Join Class");
            confirmationAlert.setHeaderText("Join " + classData.getName());
            confirmationAlert.setContentText("Are you sure you want to join " + classData.getName() + "?\n\n" + classData.getDescription());
            
            // If OK is clicked, show success message
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Alert successAlert = new Alert(AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Successfully Joined!");
                    successAlert.setContentText("You have successfully joined " + classData.getName() + ". You can now access course materials.");
                    successAlert.show();
                }
            });
        });
        
        // Add tooltip with description
        Tooltip tooltip = new Tooltip(classData.getDescription());
        Tooltip.install(card, tooltip);
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #f0f5ff; -fx-background-radius: 8; -fx-scale-x: 1.03; -fx-scale-y: 1.03;");
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-scale-x: 1; -fx-scale-y: 1;");
        });
        
        card.getChildren().addAll(titleLabel, joinButton);
        
        return card;
    }
    
    private VBox createRightSection() {
        VBox rightSection = new VBox(20);
        rightSection.setPrefWidth(450);
        rightSection.setPadding(new Insets(15));
        rightSection.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        // Section Title
        Text sectionTitle = new Text("Progress Overview");
        sectionTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 22));
        sectionTitle.setFill(TEXT_PRIMARY);
        
        // Recent Activities
        VBox recentActivities = createRecentActivitiesSection();
        
        // Completion Percentages
        VBox completionSection = createCompletionSection();
        
        // Circular Progress Indicator
        StackPane circularProgress = createCircularProgressIndicator();
        
        // Recent Dates
        VBox recentDates = createRecentDatesSection();
        
        // Analytics Chart
        LineChart<Number, Number> chart = createAnalyticsChart();
        VBox chartBox = new VBox(5);
        Label chartTitle = new Label("Learning Progress");
        chartTitle.setFont(Font.font("Roboto", FontWeight.MEDIUM, 14));
        chartBox.getChildren().addAll(chartTitle, chart);
        
        rightSection.getChildren().addAll(
                sectionTitle, 
                recentActivities, 
                completionSection, 
                circularProgress, 
                recentDates, 
                chartBox
        );
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(5);
        rightSection.setEffect(dropShadow);
        
        return rightSection;
    }
    
    private VBox createRecentActivitiesSection() {
        VBox section = new VBox(10);
        
        Label title = new Label("Recent Activities");
        title.setFont(Font.font("Roboto", FontWeight.MEDIUM, 16));
        
        VBox activities = new VBox(5);
        activities.getChildren().addAll(
                createActivityItem("Completed: 'Introduction to Algebra' Quiz", "✓"),
                createActivityItem("Started: 'History of the Renaissance' course", "→"),
                createActivityItem("Submitted: 'Python Programming' assignment", "✓")
        );
        
        section.getChildren().addAll(title, activities);
        return section;
    }
    
    private HBox createActivityItem(String text, String icon) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Roboto", 14));
        
        Label activityLabel = new Label(text);
        activityLabel.setFont(Font.font("Roboto", 14));
        
        item.getChildren().addAll(iconLabel, activityLabel);
        return item;
    }
    
    private VBox createCompletionSection() {
        VBox section = new VBox(10);
        
        Label title = new Label("Course Completion");
        title.setFont(Font.font("Roboto", FontWeight.MEDIUM, 16));
        
        // Progress bar for Computer Science
        HBox csProgress = new HBox(10);
        csProgress.setAlignment(Pos.CENTER_LEFT);
        Label csLabel = new Label("Computer Science:");
        ProgressBar csBar = new ProgressBar(0.8);
        csBar.setPrefWidth(150);
        Label csPercentage = new Label("80%");
        csProgress.getChildren().addAll(csLabel, csBar, csPercentage);
        
        // Progress bar for Mathematics
        HBox mathProgress = new HBox(10);
        mathProgress.setAlignment(Pos.CENTER_LEFT);
        Label mathLabel = new Label("Mathematics:");
        ProgressBar mathBar = new ProgressBar(0.65);
        mathBar.setPrefWidth(150);
        Label mathPercentage = new Label("65%");
        mathProgress.getChildren().addAll(mathLabel, mathBar, mathPercentage);
        
        section.getChildren().addAll(title, csProgress, mathProgress);
        return section;
    }
    
    private StackPane createCircularProgressIndicator() {
        StackPane container = new StackPane();
        container.setPadding(new Insets(10));
        container.setAlignment(Pos.CENTER);
        
        // Background circle
        Circle backgroundCircle = new Circle(60);
        backgroundCircle.setFill(Color.LIGHTGRAY);
        
        // Progress circle (75% completion)
        Circle progressCircle = new Circle(60);
        progressCircle.setFill(Color.TRANSPARENT);
        progressCircle.setStroke(PRIMARY);
        progressCircle.setStrokeWidth(10);
        // This is a simplification - in a real app you'd calculate the stroke dash array
        progressCircle.getStrokeDashArray().addAll(75.0 * 3.14, 25.0 * 3.14);
        
        // Percentage text
        Text percentageText = new Text("75%");
        percentageText.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
        
        // Label
        Text label = new Text("Overall Progress");
        label.setFont(Font.font("Roboto", 14));
        label.setTranslateY(80);
        
        container.getChildren().addAll(backgroundCircle, progressCircle, percentageText, label);
        return container;
    }
    
    private VBox createRecentDatesSection() {
        VBox section = new VBox(5);
        
        Label title = new Label("Recent Activity Dates");
        title.setFont(Font.font("Roboto", FontWeight.MEDIUM, 16));
        
        Label lastActivity = new Label("Last Activity: Completed Algebra Quiz on March 18");
        lastActivity.setFont(Font.font("Roboto", 14));
        
        Label lastLogin = new Label("Last Login: March 20");
        lastLogin.setFont(Font.font("Roboto", 14));
        
        section.getChildren().addAll(title, lastActivity, lastLogin);
        return section;
    }
    
    private LineChart<Number, Number> createAnalyticsChart() {
        NumberAxis xAxis = new NumberAxis(1, 7, 1);
        xAxis.setLabel("Week");
        
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Score");
        
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Learning Progress");
        chart.setPrefHeight(150);
        chart.setCreateSymbols(true);
        chart.setLegendVisible(false);
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(1, 65));
        series.getData().add(new XYChart.Data<>(2, 70));
        series.getData().add(new XYChart.Data<>(3, 68));
        series.getData().add(new XYChart.Data<>(4, 75));
        series.getData().add(new XYChart.Data<>(5, 82));
        series.getData().add(new XYChart.Data<>(6, 85));
        
        chart.getData().add(series);
        return chart;
    }
    
    private VBox createBottomSection() {
        VBox bottomSection = new VBox(10);
        bottomSection.setPadding(new Insets(20));
        bottomSection.setBackground(new Background(new BackgroundFill(NEUTRAL, CornerRadii.EMPTY, Insets.EMPTY)));
        
        Label title = new Label("Assignment Management");
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        
        // Assignment list
        VBox assignmentList = new VBox(5);
        assignmentList.getChildren().addAll(
                createAssignmentItem("Mathematics: Linear Equations", "Due: Mar 25", false),
                createAssignmentItem("Computer Science: Algorithm Design", "Due: Mar 22", true),
                createAssignmentItem("History: Renaissance Essay", "Due: Mar 30", false)
        );
        
        bottomSection.getChildren().addAll(title, assignmentList);
        return bottomSection;
    }
    
    private HBox createAssignmentItem(String title, String dueDate, boolean isUrgent) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(10));
        item.setAlignment(Pos.CENTER_LEFT);
        
        // Highlight urgent assignments
        if (isUrgent) {
            item.setStyle("-fx-background-color: #fff0f0; -fx-background-radius: 5;");
        } else {
            item.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        }
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Roboto", FontWeight.MEDIUM, 14));
        
        Label dueDateLabel = new Label(dueDate);
        dueDateLabel.setFont(Font.font("Roboto", 14));
        dueDateLabel.setTextFill(isUrgent ? ACCENT : TEXT_SECONDARY);
        
        Button viewButton = new Button("View Details");
        viewButton.setStyle("-fx-background-color: #f1f3f4;");
        
        Button startButton = new Button("Start Assignment");
        startButton.setStyle("-fx-background-color: #4a86e8; -fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        item.getChildren().addAll(titleLabel, dueDateLabel, spacer, viewButton, startButton);
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(1);
        dropShadow.setRadius(2);
        item.setEffect(dropShadow);
        
        return item;
    }
    
    // Helper class for class data
    private static class ClassData {
        private final String name;
        private final String description;
        
        public ClassData(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
    }
}