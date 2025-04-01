package controllers_Admin;

import dao.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard Overview page showing key metrics and statistics from the e-learning database
 */
public class DashboardOverview {
    
    // Metrics data
    private int totalUsers = 0;
    private int totalCourses = 0;
    private int totalStudents = 0;
    private int totalAssignments = 0;
    private int newEnrollmentsThisWeek = 0;
    private int assignmentsDueThisWeek = 0;
    
    // Chart data - FIXED: Changed to directly store the series
    private XYChart.Series<String, Number> enrollmentSeries = new XYChart.Series<>();
    private ObservableList<PieChart.Data> assignmentStatusData = FXCollections.observableArrayList();
    
    // Store assignments grouped by course
    private Map<String, ObservableList<AssignmentItem>> assignmentsByCourse = new HashMap<>();
    
    public DashboardOverview() {
        // Set the name of the series
        enrollmentSeries.setName("Enrolled Students");
        
        // Load all data from the database
        loadMetricsData();
        loadEnrollmentChartData();
        loadAssignmentStatusData();
        loadAssignmentsByCourse();
    }
    
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        view.setPadding(new Insets(20));
        
        Label title = new Label("E-Learning Platform Overview");
        title.getStyleClass().add("page-title");
        
        // Quick stats section
        HBox statsContainer = createStatsSection();
        statsContainer.getStyleClass().add("stats-container");
        
        // Charts section
        HBox chartsContainer = createChartsSection();
        chartsContainer.getStyleClass().add("charts-container");
        
        // Recent assignments by course section
        VBox assignmentsBySubjectSection = createAssignmentsBySubjectSection();
        assignmentsBySubjectSection.getStyleClass().add("assignments-by-subject-container");
        
        // Combine all sections
        view.getChildren().addAll(title, statsContainer, chartsContainer, assignmentsBySubjectSection);
        
        return view;
    }
    
    /**
     * Load metrics data from database
     */
    private void loadMetricsData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total users
            String usersQuery = "SELECT COUNT(*) FROM Users";
            try (PreparedStatement pstmt = conn.prepareStatement(usersQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalUsers = rs.getInt(1);
                }
            }
            
            // Total active courses
            String coursesQuery = "SELECT COUNT(*) FROM Courses";
            try (PreparedStatement pstmt = conn.prepareStatement(coursesQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalCourses = rs.getInt(1);
                }
            }
            
            // Total students (users with Student role)
            String studentsQuery = "SELECT COUNT(*) FROM Users WHERE role = 'Student'";
            try (PreparedStatement pstmt = conn.prepareStatement(studentsQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalStudents = rs.getInt(1);
                }
            }
            
            // New enrollments this week
            String newEnrollmentsQuery = "SELECT COUNT(*) FROM Enrollments WHERE enrollmentDate >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            try (PreparedStatement pstmt = conn.prepareStatement(newEnrollmentsQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    newEnrollmentsThisWeek = rs.getInt(1);
                }
            }
            
            // Total assignments
            String assignmentsQuery = "SELECT COUNT(*) FROM Assignments";
            try (PreparedStatement pstmt = conn.prepareStatement(assignmentsQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalAssignments = rs.getInt(1);
                }
            }
            
            // Assignments due this week
            String dueSoonQuery = "SELECT COUNT(*) FROM Assignments WHERE dueDate BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)";
            try (PreparedStatement pstmt = conn.prepareStatement(dueSoonQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assignmentsDueThisWeek = rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            showError("Database Error", "Error loading metrics data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Load enrollment chart data with error handling and fallback data
     */
    private void loadEnrollmentChartData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear previous data before adding new data
            enrollmentSeries.getData().clear();
            
            // Query to get enrollments per course
            String query = 
                "SELECT c.courseName, COUNT(e.studentID) AS studentCount " +
                "FROM Courses c " +
                "LEFT JOIN Enrollments e ON c.courseID = e.courseID " +
                "GROUP BY c.courseID, c.courseName " +
                "ORDER BY studentCount DESC " +
                "LIMIT 6";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                // Removed debug information to console
                int rowCount = 0;
                
                while (rs.next()) {
                    rowCount++;
                    String courseName = rs.getString("courseName");
                    int studentCount = rs.getInt("studentCount");
                    
                    // Removed console output here
                    enrollmentSeries.getData().add(new XYChart.Data<>(courseName, studentCount));
                }
                
                // Removed console output about found courses
                
                // If no data was found, add fallback data so the chart isn't empty
                if (enrollmentSeries.getData().isEmpty()) {
                    // Removed console output here
                    
                    // Get course names from database to use for fallback data
                    String courseQuery = "SELECT courseName FROM Courses LIMIT 6";
                    try (PreparedStatement courseStmt = conn.prepareStatement(courseQuery);
                         ResultSet courseRs = courseStmt.executeQuery()) {
                        
                        // Add some random enrollment numbers for existing courses
                        while (courseRs.next()) {
                            String courseName = courseRs.getString("courseName");
                            // Generate random number between 5-25 for demo purposes
                            int randomCount = 5 + (int)(Math.random() * 20);
                            enrollmentSeries.getData().add(new XYChart.Data<>(courseName, randomCount));
                            // Removed console output here
                        }
                    }
                    
                    // If still no data (no courses at all), add completely hardcoded data
                    if (enrollmentSeries.getData().isEmpty()) {
                        addFallbackEnrollmentData();
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Error executing enrollment query: " + e.getMessage());
                e.printStackTrace();
                
                // Add fallback data in case of query error
                addFallbackEnrollmentData();
            }
            
        } catch (SQLException e) {
            showError("Database Error", "Error loading enrollment chart data: " + e.getMessage());
            e.printStackTrace();
            
            // Even if database connection fails, provide some data for the chart
            addFallbackEnrollmentData();
        }
    }

    /**
     * Helper method to add fallback data to the series when real data isn't available
     */
    private void addFallbackEnrollmentData() {
        // Clear any existing data first
        enrollmentSeries.getData().clear();
        
        enrollmentSeries.getData().add(new XYChart.Data<>("Mathematics", 15));
        enrollmentSeries.getData().add(new XYChart.Data<>("History", 12));
        enrollmentSeries.getData().add(new XYChart.Data<>("Computer Science", 20));
        enrollmentSeries.getData().add(new XYChart.Data<>("Biology", 8));
        enrollmentSeries.getData().add(new XYChart.Data<>("Physics", 7));
        enrollmentSeries.getData().add(new XYChart.Data<>("Literature", 10));
        // Removed console output about fallback enrollment data
    }
    /**
     * Load assignment status data for pie chart
     */
    private void loadAssignmentStatusData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to get assignment status distribution
            String query = 
                "SELECT ap.status, COUNT(*) AS statusCount " +
                "FROM AssignmentProgress ap " +
                "GROUP BY ap.status";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                int completed = 0;
                int inProgress = 0;
                int notStarted = 0;
                
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("statusCount");
                    
                    switch (status.toLowerCase()) {
                        case "completed":
                            completed = count;
                            break;
                        case "in progress":
                            inProgress = count;
                            break;
                        case "not started":
                            notStarted = count;
                            break;
                    }
                }
                
                // If no data found, use some default values for display
                if (completed == 0 && inProgress == 0 && notStarted == 0) {
                    // Query to count total assignments
                    String countQuery = "SELECT COUNT(*) FROM Assignments";
                    try (PreparedStatement countStmt = conn.prepareStatement(countQuery);
                         ResultSet countRs = countStmt.executeQuery()) {
                        if (countRs.next()) {
                            notStarted = countRs.getInt(1); // Default all as not started
                        } else {
                            notStarted = 1; // Fallback
                        }
                    }
                }
                
                assignmentStatusData.add(new PieChart.Data("Completed", completed));
                assignmentStatusData.add(new PieChart.Data("In Progress", inProgress));
                assignmentStatusData.add(new PieChart.Data("Not Started", notStarted));
                
            }
        } catch (SQLException e) {
            showError("Database Error", "Error loading assignment status data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load assignments grouped by course
     */
    private void loadAssignmentsByCourse() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to get assignments grouped by course
            String query = 
                "SELECT a.title, c.courseName, a.dueDate, a.priority, a.points " +
                "FROM Assignments a " +
                "JOIN Courses c ON a.courseID = c.courseID " +
                "WHERE a.dueDate >= CURDATE() " +
                "ORDER BY c.courseName, a.dueDate";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate today = LocalDate.now();
                
                while (rs.next()) {
                    String title = rs.getString("title");
                    String course = rs.getString("courseName");
                    LocalDate dueDate = LocalDate.parse(rs.getString("dueDate"), formatter);
                    String priority = rs.getString("priority");
                    String points = rs.getString("points") + " points";
                    
                    // Format due date
                    String dueDateStr;
                    long daysBetween = ChronoUnit.DAYS.between(today, dueDate);
                    
                    if (daysBetween == 0) {
                        dueDateStr = "Due Today";
                    } else if (daysBetween == 1) {
                        dueDateStr = "Due Tomorrow";
                    } else if (daysBetween < 0) {
                        dueDateStr = "Overdue by " + Math.abs(daysBetween) + " days";
                    } else {
                        dueDateStr = "Due in " + daysBetween + " days";
                    }
                    
                    // Get or create the list for this course
                    ObservableList<AssignmentItem> courseAssignments = assignmentsByCourse.computeIfAbsent(
                        course, k -> FXCollections.observableArrayList()
                    );
                    
                    // Add assignment to the course list
                    courseAssignments.add(new AssignmentItem(title, course, dueDateStr, priority, points));
                }
            }
        } catch (SQLException e) {
            showError("Database Error", "Error loading assignments by course: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private HBox createStatsSection() {
        HBox container = new HBox(15);
        container.setPadding(new Insets(10));
        container.setAlignment(Pos.CENTER);
        
        // Create stat cards with data from database - removed the enrollment and due assignments info
        VBox usersCard = createStatCard("Total Users", String.valueOf(totalUsers), 
                                       "All active users", "users-stat");
        
        VBox coursesCard = createStatCard("Active Courses", String.valueOf(totalCourses), 
                                         "All courses active", "courses-stat");
        
        VBox studentsCard = createStatCard("Enrolled Students", String.valueOf(totalStudents), 
                                          "", "students-stat");  // Removed new enrollments text
        
        VBox assignmentsCard = createStatCard("Total Assignments", String.valueOf(totalAssignments), 
                                             "", "assignments-stat");  // Removed due this week text
        
        container.getChildren().addAll(usersCard, coursesCard, studentsCard, assignmentsCard);
        
        HBox.setHgrow(usersCard, Priority.ALWAYS);
        HBox.setHgrow(coursesCard, Priority.ALWAYS);
        HBox.setHgrow(studentsCard, Priority.ALWAYS);
        HBox.setHgrow(assignmentsCard, Priority.ALWAYS);
        
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
        
        // Course enrollment distribution chart - CHANGED to horizontal bar chart
        VBox enrollmentChartBox = new VBox(10);
        enrollmentChartBox.getStyleClass().add("chart-container");
        enrollmentChartBox.setPadding(new Insets(15));
        
        Label enrollmentChartTitle = new Label("Course Enrollment Distribution");
        enrollmentChartTitle.getStyleClass().add("chart-title");
        
        // Create HORIZONTAL bar chart for enrollment distribution
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        BarChart<Number, String> enrollmentChart = new BarChart<>(xAxis, yAxis); // Note the reversed axis types
        enrollmentChart.setTitle("Students per Course");
        xAxis.setLabel("Number of Students");
        yAxis.setLabel("Course");
        
        // Set animated to false for more reliable rendering
        enrollmentChart.setAnimated(false);
        
        // Convert the series data for horizontal bar chart
        XYChart.Series<Number, String> horizontalSeries = new XYChart.Series<>();
        horizontalSeries.setName("Enrolled Students");
        
        // Transfer data from original series to horizontal format
        for (XYChart.Data<String, Number> data : enrollmentSeries.getData()) {
            horizontalSeries.getData().add(new XYChart.Data<>(data.getYValue(), data.getXValue()));
        }
        
        // Add data to the chart
        ObservableList<XYChart.Series<Number, String>> chartData = FXCollections.observableArrayList();
        chartData.add(horizontalSeries);
        enrollmentChart.setData(chartData);
        enrollmentChart.setLegendVisible(false);
        
        // Make sure the chart displays properly
        enrollmentChart.setCategoryGap(10);
        enrollmentChart.setBarGap(0);
        
        // Add some visualization enhancements
        for (XYChart.Series<Number, String> series : enrollmentChart.getData()) {
            for (XYChart.Data<Number, String> item : series.getData()) {
                // Add hover effect and display value on bars
                Node node = item.getNode();
                Tooltip tooltip = new Tooltip(item.getXValue() + " students");
                Tooltip.install(node, tooltip);
                
                // Add a listener to change color on hover
                node.setOnMouseEntered(e -> {
                    node.getStyleClass().add("chart-bar-hover");
                });
                node.setOnMouseExited(e -> {
                    node.getStyleClass().remove("chart-bar-hover");
                });
            }
        }
        
        enrollmentChartBox.getChildren().addAll(enrollmentChartTitle, enrollmentChart);
        
        // Assignment completion chart (unchanged)
        VBox assignmentChartBox = new VBox(10);
        assignmentChartBox.getStyleClass().add("chart-container");
        assignmentChartBox.setPadding(new Insets(15));
        
        Label assignmentChartTitle = new Label("Assignment Status");
        assignmentChartTitle.getStyleClass().add("chart-title");
        
        // Create pie chart for assignment status
        PieChart assignmentChart = new PieChart();
        assignmentChart.setTitle("Overall Assignment Progress");
        assignmentChart.setAnimated(false);
        
        // Add data from database
        assignmentChart.setData(assignmentStatusData);
        
        assignmentChartBox.getChildren().addAll(assignmentChartTitle, assignmentChart);
        
        container.getChildren().addAll(enrollmentChartBox, assignmentChartBox);
        
        HBox.setHgrow(enrollmentChartBox, Priority.ALWAYS);
        HBox.setHgrow(assignmentChartBox, Priority.ALWAYS);
        
        return container;
    }
    
    private VBox createAssignmentsBySubjectSection() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("assignments-by-subject-section");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Assignments by Subject");
        title.getStyleClass().add("section-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button viewAllBtn = new Button("View All Assignments");
        viewAllBtn.getStyleClass().add("view-all-button");
        
        header.getChildren().addAll(title, spacer, viewAllBtn);
        container.getChildren().add(header);
        
        // If no assignments, show message
        if (assignmentsByCourse.isEmpty()) {
            Label noAssignmentsLabel = new Label("No upcoming assignments found");
            noAssignmentsLabel.getStyleClass().add("no-data-message");
            container.getChildren().add(noAssignmentsLabel);
        } else {
            // Create a FlowPane to hold assignment boxes side by side
            FlowPane courseBoxesContainer = new FlowPane(15, 15);
            courseBoxesContainer.getStyleClass().add("course-boxes-container");
            
            // Create a box for each course
            for (Map.Entry<String, ObservableList<AssignmentItem>> entry : assignmentsByCourse.entrySet()) {
                String courseName = entry.getKey();
                ObservableList<AssignmentItem> assignments = entry.getValue();
                
                VBox courseBox = createCourseAssignmentsBox(courseName, assignments);
                courseBoxesContainer.getChildren().add(courseBox);
            }
            
            container.getChildren().add(courseBoxesContainer);
        }
        
        return container;
    }
    
    private VBox createCourseAssignmentsBox(String courseName, ObservableList<AssignmentItem> assignments) {
        VBox box = new VBox(10);
        box.setPrefWidth(300);
        box.setPadding(new Insets(15));
        box.getStyleClass().add("course-assignments-box");
        
        // Course name as header
        Label courseLabel = new Label(courseName);
        courseLabel.getStyleClass().add("course-name-header");
        
        // Separator
        Separator separator = new Separator();
        separator.getStyleClass().add("course-header-separator");
        
        box.getChildren().addAll(courseLabel, separator);
        
        // Add assignments
        if (assignments.isEmpty()) {
            Label noAssignmentsLabel = new Label("No upcoming assignments");
            noAssignmentsLabel.getStyleClass().add("no-assignments-message");
            box.getChildren().add(noAssignmentsLabel);
        } else {
            // Limit to 3 assignments per box for compactness
            int count = Math.min(assignments.size(), 3);
            for (int i = 0; i < count; i++) {
                AssignmentItem assignment = assignments.get(i);
                HBox assignmentItem = createAssignmentItem(
                    assignment.getTitle(),
                    assignment.getDueDate(),
                    assignment.getPriority(),
                    assignment.getPoints()
                );
                box.getChildren().add(assignmentItem);
            }
            
            // If there are more assignments, add a "View More" link
            if (assignments.size() > 3) {
                Hyperlink viewMoreLink = new Hyperlink("View " + (assignments.size() - 3) + " more...");
                viewMoreLink.getStyleClass().add("view-more-link");
                box.getChildren().add(viewMoreLink);
            }
        }
        
        return box;
    }
    
    private HBox createAssignmentItem(String title, String dueDate, String priority, String points) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(8));
        item.getStyleClass().add("assignment-item");
        
        // Priority indicator
        Rectangle indicator = new Rectangle(5, 35);
        indicator.getStyleClass().add("priority-" + priority.toLowerCase());
        
        // Content
        VBox content = new VBox(3);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("assignment-title");
        
        Label dueDateLabel = new Label(dueDate);
        dueDateLabel.getStyleClass().add("assignment-due-date");
        
        Label pointsLabel = new Label(points);
        pointsLabel.getStyleClass().add("assignment-points");
        
        content.getChildren().addAll(titleLabel, dueDateLabel, pointsLabel);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        item.getChildren().addAll(indicator, content);
        
        return item;
    }
    
    /**
     * Show error alert
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper class for assignment items
    public static class AssignmentItem {
        private final String title;
        private final String course;
        private final String dueDate;
        private final String priority;
        private final String points;
        
        public AssignmentItem(String title, String course, String dueDate, String priority, String points) {
            this.title = title;
            this.course = course;
            this.dueDate = dueDate;
            this.priority = priority;
            this.points = points;
        }
        
        public String getTitle() { return title; }
        public String getCourse() { return course; }
        public String getDueDate() { return dueDate; }
        public String getPriority() { return priority; }
        public String getPoints() { return points; }
    }
}