package controllers_Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

/**
 * Dashboard Overview page showing key metrics and statistics
 */
public class DashboardOverview {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Dashboard Overview");
        title.getStyleClass().add("page-title");
        
        // Quick stats section
        HBox statsContainer = createStatsSection();
        statsContainer.getStyleClass().add("stats-container");
        
        // Charts section
        HBox chartsContainer = createChartsSection();
        chartsContainer.getStyleClass().add("charts-container");
        
        // Recent activity section
        VBox recentActivity = createRecentActivitySection();
        recentActivity.getStyleClass().add("activity-container");
        
        // Quick actions section
        HBox quickActions = createQuickActionButtons();
        quickActions.getStyleClass().add("actions-container");
        
        // Combine all sections
        view.getChildren().addAll(title, statsContainer, chartsContainer, recentActivity, quickActions);
        return view;
    }
    
    private HBox createStatsSection() {
        HBox container = new HBox(15);
        container.setPadding(new Insets(10));
        container.setAlignment(Pos.CENTER);
        
        // Create stat cards
        VBox usersCard = createStatCard("Total Users", "1,245", "+12% this month", "users-stat");
        VBox coursesCard = createStatCard("Active Courses", "87", "+3 this week", "courses-stat");
        VBox revenueCard = createStatCard("Monthly Revenue", "$43,500", "+8% from last month", "revenue-stat");
        VBox supportCard = createStatCard("Support Tickets", "24", "5 pending resolution", "tickets-stat");
        
        container.getChildren().addAll(usersCard, coursesCard, revenueCard, supportCard);
        HBox.setHgrow(usersCard, Priority.ALWAYS);
        HBox.setHgrow(coursesCard, Priority.ALWAYS);
        HBox.setHgrow(revenueCard, Priority.ALWAYS);
        HBox.setHgrow(supportCard, Priority.ALWAYS);
        
        return container;
    }
    
    private VBox createStatCard(String title, String value, String subtitle, String styleClass) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.getStyleClass().addAll("stat-card", styleClass);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("stat-subtitle");
        
        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        
        // Add hover effect
        card.setOnMouseEntered(e -> card.getStyleClass().add("stat-card-hover"));
        card.setOnMouseExited(e -> card.getStyleClass().remove("stat-card-hover"));
        
        return card;
    }
    
    private HBox createChartsSection() {
        HBox container = new HBox(20);
        container.setPadding(new Insets(10));
        
        // User growth chart
        VBox userChartBox = new VBox(10);
        userChartBox.getStyleClass().add("chart-container");
        userChartBox.setPadding(new Insets(15));
        
        Label userChartTitle = new Label("User Growth");
        userChartTitle.getStyleClass().add("chart-title");
        
        // Create area chart for user growth
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<String, Number> userChart = new AreaChart<>(xAxis, yAxis);
        userChart.setTitle("Last 6 Months");
        xAxis.setLabel("Month");
        yAxis.setLabel("Users");
        
        // Add data to chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Users");
        series.getData().add(new XYChart.Data<>("Jan", 800));
        series.getData().add(new XYChart.Data<>("Feb", 950));
        series.getData().add(new XYChart.Data<>("Mar", 1020));
        series.getData().add(new XYChart.Data<>("Apr", 1080));
        series.getData().add(new XYChart.Data<>("May", 1150));
        series.getData().add(new XYChart.Data<>("Jun", 1245));
        userChart.getData().add(series);
        userChart.setLegendVisible(false);
        
        userChartBox.getChildren().addAll(userChartTitle, userChart);
        
        // Revenue chart
        VBox revenueChartBox = new VBox(10);
        revenueChartBox.getStyleClass().add("chart-container");
        revenueChartBox.setPadding(new Insets(15));
        
        Label revenueChartTitle = new Label("Revenue Breakdown");
        revenueChartTitle.getStyleClass().add("chart-title");
        
        // Create pie chart for revenue
        PieChart revenueChart = new PieChart();
        revenueChart.setTitle("Revenue Sources");
        
        // Add data to chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Course Sales", 62),
            new PieChart.Data("Subscriptions", 25),
            new PieChart.Data("Consulting", 10),
            new PieChart.Data("Other", 3)
        );
        revenueChart.setData(pieChartData);
        
        revenueChartBox.getChildren().addAll(revenueChartTitle, revenueChart);
        
        container.getChildren().addAll(userChartBox, revenueChartBox);
        HBox.setHgrow(userChartBox, Priority.ALWAYS);
        HBox.setHgrow(revenueChartBox, Priority.ALWAYS);
        
        return container;
    }
    
    private VBox createRecentActivitySection() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("recent-activity-section");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Recent Activity");
        title.getStyleClass().add("section-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button viewAllBtn = new Button("View All");
        viewAllBtn.getStyleClass().add("view-all-button");
        
        header.getChildren().addAll(title, spacer, viewAllBtn);
        
        // Activity list
        VBox activityList = new VBox(12);
        activityList.getChildren().addAll(
            createActivityItem("New user registered", "John Smith created an account", "10 minutes ago"),
            createActivityItem("Course published", "Advanced Machine Learning is now live", "2 hours ago"),
            createActivityItem("Payment received", "Subscription payment from Enterprise client", "5 hours ago"),
            createActivityItem("Support ticket resolved", "Issue #345 - Login problem fixed", "Yesterday")
        );
        
        container.getChildren().addAll(header, activityList);
        return container;
    }
    
    private HBox createActivityItem(String title, String description, String timestamp) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(10));
        item.getStyleClass().add("activity-item");
        
        // Activity indicator
        Rectangle indicator = new Rectangle(8, 50);
        indicator.getStyleClass().add("activity-indicator");
        
        // Content
        VBox content = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("activity-title");
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("activity-description");
        Label timeLabel = new Label(timestamp);
        timeLabel.getStyleClass().add("activity-time");
        content.getChildren().addAll(titleLabel, descLabel, timeLabel);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        // Action button
        Button actionBtn = new Button("Details");
        actionBtn.getStyleClass().add("activity-action");
        
        item.getChildren().addAll(indicator, content, actionBtn);
        return item;
    }
    
    private HBox createQuickActionButtons() {
        HBox container = new HBox(15);
        container.setPadding(new Insets(10, 10, 30, 10));
        
        Button addUserBtn = new Button("Add New User");
        addUserBtn.getStyleClass().add("action-button");
        
        Button addCourseBtn = new Button("Create Course");
        addCourseBtn.getStyleClass().add("action-button");
        
        Button generateReportBtn = new Button("Generate Report");
        generateReportBtn.getStyleClass().add("action-button");
        
        Button systemSettingsBtn = new Button("System Settings");
        systemSettingsBtn.getStyleClass().add("action-button");
        
        container.getChildren().addAll(addUserBtn, addCourseBtn, generateReportBtn, systemSettingsBtn);
        return container;
    }
}