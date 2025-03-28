package controllers_Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Reports and Analytics page
 */
public class ReportsPage {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Reports & Analytics");
        title.getStyleClass().add("page-title");
        
        // Filter controls
        HBox filterContainer = new HBox(15);
        filterContainer.setAlignment(Pos.CENTER_LEFT);
        filterContainer.setPadding(new Insets(10));
        
        Label dateRangeLabel = new Label("Date Range:");
        ComboBox<String> dateRangeFilter = new ComboBox<>();
        dateRangeFilter.getItems().addAll("Last 7 Days", "Last 30 Days", "This Month", "Last Month", "This Year", "Custom Range");
        dateRangeFilter.setValue("Last 30 Days");
        
        Button generateBtn = new Button("Generate Report");
        generateBtn.getStyleClass().add("primary-button");
        
        Button exportBtn = new Button("Export Data");
        exportBtn.getStyleClass().add("secondary-button");
        
        filterContainer.getChildren().addAll(dateRangeLabel, dateRangeFilter, generateBtn, exportBtn);
        
        // Key metrics section
        TilePane metricsGrid = new TilePane();
        metricsGrid.setPadding(new Insets(10));
        metricsGrid.setHgap(15);
        metricsGrid.setVgap(15);
        metricsGrid.setPrefColumns(2);
        
        // Add metric charts
        metricsGrid.getChildren().addAll(
            createMetricChart("User Registration Trend", ChartType.LINE),
            createMetricChart("Revenue Growth", ChartType.BAR),
            createMetricChart("Course Enrollment Distribution", ChartType.PIE),
            createMetricChart("User Activity by Hour", ChartType.AREA)
        );
        
        // Data tables section
        VBox tablesSection = new VBox(20);
        tablesSection.setPadding(new Insets(10));
        
        Label tablesTitle = new Label("Detailed Reports");
        tablesTitle.getStyleClass().add("section-title");
        
        TabPane reportTabs = new TabPane();
        reportTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab userReportTab = new Tab("User Reports");
        userReportTab.setContent(createSampleTableView("User Registration by Day",
            new String[]{"Date", "New Users", "Active Users", "Conversion Rate"}));
            
        Tab courseReportTab = new Tab("Course Reports");
        courseReportTab.setContent(createSampleTableView("Course Engagement",
            new String[]{"Course Name", "Enrollments", "Completion Rate", "Avg. Rating"}));
            
        Tab revenueReportTab = new Tab("Revenue Reports");
        revenueReportTab.setContent(createSampleTableView("Revenue by Source",
            new String[]{"Source", "Revenue", "Transactions", "Avg. Transaction"}));
            
        reportTabs.getTabs().addAll(userReportTab, courseReportTab, revenueReportTab);
        
        tablesSection.getChildren().addAll(tablesTitle, reportTabs);
        
        // Scheduled reports section
        VBox scheduledReports = new VBox(10);
        scheduledReports.setPadding(new Insets(15));
        scheduledReports.getStyleClass().add("scheduled-reports-section");
        
        HBox scheduledHeader = new HBox();
        scheduledHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label scheduledTitle = new Label("Scheduled Reports");
        scheduledTitle.getStyleClass().add("section-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button newScheduleBtn = new Button("Schedule New Report");
        newScheduleBtn.getStyleClass().add("primary-button");
        
        scheduledHeader.getChildren().addAll(scheduledTitle, spacer, newScheduleBtn);
        
        ListView<String> scheduledList = new ListView<>();
        scheduledList.getItems().addAll(
            "Monthly Revenue Report - Every 1st of month - Email to finance@example.com",
            "Weekly User Activity - Every Monday - Email to team@example.com",
            "Course Completion Rates - Every Sunday - Email to instructors@example.com"
        );
        scheduledList.setPrefHeight(120);
        
        scheduledReports.getChildren().addAll(scheduledHeader, scheduledList);
        
        view.getChildren().addAll(title, filterContainer, metricsGrid, tablesSection, scheduledReports);
        return view;
    }
    
    private enum ChartType { LINE, BAR, PIE, AREA }
    
    private VBox createMetricChart(String title, ChartType type) {
        VBox chartContainer = new VBox(10);
        chartContainer.setPadding(new Insets(15));
        chartContainer.getStyleClass().add("chart-container");
        chartContainer.setPrefHeight(300);
        
        Label chartTitle = new Label(title);
        chartTitle.getStyleClass().add("chart-title");
        
        Node chart;
        switch (type) {
            case LINE:
                chart = createLineChart();
                break;
            case BAR:
                chart = createBarChart();
                break;
            case PIE:
                chart = createPieChart();
                break;
            case AREA:
                chart = createAreaChart();
                break;
            default:
                chart = new Label("Chart not available");
        }
        
        chartContainer.getChildren().addAll(chartTitle, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        
        return chartContainer;
    }
    
    private Node createLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Daily Trend");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("New Users");
        series.getData().add(new XYChart.Data<>("Mon", 25));
        series.getData().add(new XYChart.Data<>("Tue", 30));
        series.getData().add(new XYChart.Data<>("Wed", 28));
        series.getData().add(new XYChart.Data<>("Thu", 32));
        series.getData().add(new XYChart.Data<>("Fri", 42));
        series.getData().add(new XYChart.Data<>("Sat", 22));
        series.getData().add(new XYChart.Data<>("Sun", 18));
        
        lineChart.getData().add(series);
        lineChart.setLegendVisible(false);
        
        return lineChart;
    }
    
    private Node createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Monthly Revenue");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        series.getData().add(new XYChart.Data<>("Jan", 30500));
        series.getData().add(new XYChart.Data<>("Feb", 35200));
        series.getData().add(new XYChart.Data<>("Mar", 38700));
        series.getData().add(new XYChart.Data<>("Apr", 42100));
        series.getData().add(new XYChart.Data<>("May", 41300));
        series.getData().add(new XYChart.Data<>("Jun", 43500));
        
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        
        return barChart;
    }
    
    private Node createPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Course Categories");
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Programming", 45),
            new PieChart.Data("Data Science", 25),
            new PieChart.Data("Business", 15),
            new PieChart.Data("Design", 10),
            new PieChart.Data("Other", 5)
        );
        
        pieChart.setData(pieChartData);
        return pieChart;
    }
    
    private Node createAreaChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setTitle("User Activity");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Active Users");
        series.getData().add(new XYChart.Data<>("00:00", 45));
        series.getData().add(new XYChart.Data<>("04:00", 20));
        series.getData().add(new XYChart.Data<>("08:00", 110));
        series.getData().add(new XYChart.Data<>("12:00", 350));
        series.getData().add(new XYChart.Data<>("16:00", 420));
        series.getData().add(new XYChart.Data<>("20:00", 280));
        
        areaChart.getData().add(series);
        areaChart.setLegendVisible(false);
        
        return areaChart;
    }
    
    private TableView<String[]> createSampleTableView(String title, String[] columns) {
        TableView<String[]> tableView = new TableView<>();
        
        // Add columns
        for (int i = 0; i < columns.length; i++) {
            final int columnIndex = i;
            TableColumn<String[], String> column = new TableColumn<>(columns[i]);
            column.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue()[columnIndex]));
            tableView.getColumns().add(column);
        }
        
        // Add sample data
        tableView.getItems().add(new String[]{"2023-06-01", "128", "1,245", "4.2%"});
        tableView.getItems().add(new String[]{"2023-06-02", "145", "1,356", "4.5%"});
        tableView.getItems().add(new String[]{"2023-06-03", "98", "1,105", "3.8%"});
        tableView.getItems().add(new String[]{"2023-06-04", "78", "985", "3.5%"});
        tableView.getItems().add(new String[]{"2023-06-05", "156", "1,458", "4.7%"});
        
        return tableView;
    }
}