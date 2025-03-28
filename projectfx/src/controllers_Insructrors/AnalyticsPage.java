package controllers_Insructrors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Analytics page for viewing course and student performance metrics
 */
public class AnalyticsPage {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Analytics & Insights");
        title.getStyleClass().add("page-title");
        
        // Course selector and date range
        HBox filterContainer = new HBox(10);
        filterContainer.setAlignment(Pos.CENTER_LEFT);
        filterContainer.setPadding(new Insets(0, 0, 10, 0));
        
        Label courseLabel = new Label("Course:");
        ComboBox<String> courseDropdown = new ComboBox<>();
        courseDropdown.getItems().addAll(
            "All Courses", 
            "Introduction to Python", 
            "Web Development Fundamentals",
            "Data Science Essentials"
        );
        courseDropdown.setValue("All Courses");
        
        Label dateRangeLabel = new Label("Time Period:");
        ComboBox<String> dateRangeDropdown = new ComboBox<>();
        dateRangeDropdown.getItems().addAll(
            "Last 7 Days", "Last 30 Days", "Last 90 Days", "All Time", "Custom Range"
        );
        dateRangeDropdown.setValue("Last 30 Days");
        
        Button updateBtn = new Button("Update");
        updateBtn.getStyleClass().add("primary-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button exportDataBtn = new Button("Export Data");
        exportDataBtn.getStyleClass().add("secondary-button");
        
        filterContainer.getChildren().addAll(
            courseLabel, courseDropdown, dateRangeLabel, dateRangeDropdown, 
            updateBtn, spacer, exportDataBtn);
        
        // Key metrics section
        HBox keyMetrics = new HBox(15);
        keyMetrics.getStyleClass().add("metrics-container");
        
        VBox enrollmentMetric = createMetricCard("Total Enrollment", "248", "+18 this month", "enrollment-metric");
        VBox completionMetric = createMetricCard("Completion Rate", "68%", "5% above average", "completion-metric");
        VBox engagementMetric = createMetricCard("Engagement Score", "7.8/10", "High engagement", "engagement-metric");
        VBox satisfactionMetric = createMetricCard("Student Satisfaction", "4.7/5", "Based on 156 reviews", "satisfaction-metric");
        
        keyMetrics.getChildren().addAll(enrollmentMetric, completionMetric, engagementMetric, satisfactionMetric);
        
        // Charts section - laid out in a 2x2 grid
        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(20);
        chartsGrid.setVgap(20);
        chartsGrid.setPadding(new Insets(10));
        
        // Enrollment trend chart
        VBox enrollmentChartBox = createChartBox("Enrollment Trend", createEnrollmentChart());
        GridPane.setConstraints(enrollmentChartBox, 0, 0);
        
        // Content engagement chart
        VBox engagementChartBox = createChartBox("Content Engagement", createEngagementChart());
        GridPane.setConstraints(engagementChartBox, 1, 0);
        
        // Completion rates chart
        VBox completionChartBox = createChartBox("Completion Rates", createCompletionChart());
        GridPane.setConstraints(completionChartBox, 0, 1);
        
        // Student performance chart
        VBox performanceChartBox = createChartBox("Student Performance", createPerformanceChart());
        GridPane.setConstraints(performanceChartBox, 1, 1);
        
        chartsGrid.getChildren().addAll(
            enrollmentChartBox, engagementChartBox, completionChartBox, performanceChartBox);
        VBox.setVgrow(chartsGrid, Priority.ALWAYS);
        
        // Popular content section
        VBox popularContentSection = new VBox(10);
        popularContentSection.setPadding(new Insets(15));
        popularContentSection.getStyleClass().add("popular-content-section");
        
        Label popularContentTitle = new Label("Most Popular Content");
        popularContentTitle.getStyleClass().add("section-title");
        
        TableView<ContentMetric> popularContentTable = createPopularContentTable();
        
        popularContentSection.getChildren().addAll(popularContentTitle, popularContentTable);
        
        view.getChildren().addAll(title, filterContainer, keyMetrics, chartsGrid, popularContentSection);
        return view;
    }
    
    private VBox createMetricCard(String title, String value, String subtitle, String styleClass) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.getStyleClass().addAll("metric-card", styleClass);
        HBox.setHgrow(card, Priority.ALWAYS);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("metric-subtitle");
        
        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        return card;
    }
    
    private VBox createChartBox(String title, Node chart) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.getStyleClass().add("chart-box");
        box.setPrefHeight(300);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("chart-title");
        
        box.getChildren().addAll(titleLabel, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        
        return box;
    }
    
    private Node createEnrollmentChart() {
        // Create an enrollment trend line chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Monthly Trend");
        lineChart.setAnimated(true);
        lineChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Jan", 120));
        series.getData().add(new XYChart.Data<>("Feb", 140));
        series.getData().add(new XYChart.Data<>("Mar", 155));
        series.getData().add(new XYChart.Data<>("Apr", 180));
        series.getData().add(new XYChart.Data<>("May", 210));
        series.getData().add(new XYChart.Data<>("Jun", 248));
        
        lineChart.getData().add(series);
        return lineChart;
    }
    
    private Node createEngagementChart() {
        // Create a bar chart for engagement by content type
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Engagement by Content Type");
        barChart.setAnimated(true);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Time (minutes)");
        series.getData().add(new XYChart.Data<>("Videos", 25));
        series.getData().add(new XYChart.Data<>("Lectures", 18));
        series.getData().add(new XYChart.Data<>("Readings", 12));
        series.getData().add(new XYChart.Data<>("Quizzes", 15));
        series.getData().add(new XYChart.Data<>("Assignments", 45));
        
        barChart.getData().add(series);
        return barChart;
    }
    
    private Node createCompletionChart() {
        // Create a pie chart for course completion rates
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Completion Status");
        pieChart.setLabelsVisible(true);
        
        pieChart.getData().addAll(
            new PieChart.Data("Completed (42%)", 42),
            new PieChart.Data("In Progress (26%)", 26),
            new PieChart.Data("Started (18%)", 18),
            new PieChart.Data("Not Started (14%)", 14)
        );
        
        return pieChart;
    }
    
    private Node createPerformanceChart() {
        // Create a stacked bar chart for grade distribution
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        stackedBarChart.setTitle("Grade Distribution");
        
        XYChart.Series<String, Number> pythonSeries = new XYChart.Series<>();
        pythonSeries.setName("Intro to Python");
        pythonSeries.getData().add(new XYChart.Data<>("A", 35));
        pythonSeries.getData().add(new XYChart.Data<>("B", 45));
        pythonSeries.getData().add(new XYChart.Data<>("C", 15));
        pythonSeries.getData().add(new XYChart.Data<>("D", 3));
        pythonSeries.getData().add(new XYChart.Data<>("F", 2));
        
        XYChart.Series<String, Number> webSeries = new XYChart.Series<>();
        webSeries.setName("Web Development");
        webSeries.getData().add(new XYChart.Data<>("A", 28));
        webSeries.getData().add(new XYChart.Data<>("B", 42));
        webSeries.getData().add(new XYChart.Data<>("C", 20));
        webSeries.getData().add(new XYChart.Data<>("D", 7));
        webSeries.getData().add(new XYChart.Data<>("F", 3));
        
        stackedBarChart.getData().addAll(pythonSeries, webSeries);
        return stackedBarChart;
    }
    
    private TableView<ContentMetric> createPopularContentTable() {
        TableView<ContentMetric> table = new TableView<>();
        
        // Create columns
        TableColumn<ContentMetric, String> contentColumn = new TableColumn<>("Content Name");
        contentColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        contentColumn.setPrefWidth(300);
        
        TableColumn<ContentMetric, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        
        TableColumn<ContentMetric, String> viewsColumn = new TableColumn<>("Views");
        viewsColumn.setCellValueFactory(cellData -> cellData.getValue().viewsProperty());
        
        TableColumn<ContentMetric, String> avgTimeColumn = new TableColumn<>("Avg. Time");
        avgTimeColumn.setCellValueFactory(cellData -> cellData.getValue().avgTimeProperty());
        
        TableColumn<ContentMetric, String> completionColumn = new TableColumn<>("Completion Rate");
        completionColumn.setCellValueFactory(cellData -> cellData.getValue().completionRateProperty());
        
        table.getColumns().addAll(contentColumn, typeColumn, viewsColumn, avgTimeColumn, completionColumn);
        
        // Add sample data
        ObservableList<ContentMetric> data = FXCollections.observableArrayList(
            new ContentMetric("Getting Started with Python", "Video", "142", "18:25", "96%"),
            new ContentMetric("Understanding Variables", "Lecture", "138", "22:10", "94%"),
            new ContentMetric("Python Data Types", "Reading", "125", "12:45", "87%"),
            new ContentMetric("Working with Strings and Numbers", "Video", "132", "15:30", "91%"),
            new ContentMetric("Your First Program", "Assignment", "142", "45:20", "98%")
        );
        
        table.setItems(data);
        return table;
    }
}