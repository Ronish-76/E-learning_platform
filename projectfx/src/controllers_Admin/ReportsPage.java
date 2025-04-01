package controllers_Admin;

import dao.DatabaseConnection;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Reports and Analytics page that displays various statistics from the database
 */
public class ReportsPage {
    
    // Color constants for UI
    private static final String PRIMARY_COLOR = "#3498db";
    private static final String SECONDARY_COLOR = "#2ecc71";
    private static final String DANGER_COLOR = "#e74c3c";
    private static final String WARNING_COLOR = "#f39c12";
    private static final String BACKGROUND_COLOR = "#f5f7fa";
    private static final String CARD_COLOR = "#ffffff";
    private static final String TEXT_COLOR = "#2c3e50";
    private static final String SUBTEXT_COLOR = "#7f8c8d";
    
    /**
     * Returns the main view for the Reports page
     */
    public Node getView() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setBackground(new Background(new BackgroundFill(
                Color.web(BACKGROUND_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        
        // Page title
        Label titleLabel = new Label("Reports & Analytics");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));
        titleLabel.setPadding(new Insets(0, 0, 20, 0));
        
        // Main content cards
        VBox userStatsCard = createUserStatsCard();
        VBox courseStatsCard = createCourseStatsCard();
        VBox quizStatsCard = createQuizStatsCard();
        VBox assignmentStatsCard = createAssignmentStatsCard();
        VBox instructorStatsCard = createInstructorStatsCard();
        
        mainContainer.getChildren().addAll(
            titleLabel, 
            userStatsCard, 
            courseStatsCard, 
            quizStatsCard, 
            instructorStatsCard,
            assignmentStatsCard
        );
        
        scrollPane.setContent(mainContainer);
        return scrollPane;
    }
    
    /**
     * Creates a card showing user statistics
     */
    private VBox createUserStatsCard() {
        VBox card = new VBox();
        card.setSpacing(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 8;");
        
        // Add drop shadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        card.setEffect(dropShadow);
        
        // Card title
        Label cardTitle = new Label("1. User Statistics");
        cardTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        cardTitle.setTextFill(Color.web(PRIMARY_COLOR));
        
        // Content
        VBox content = new VBox(10);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total users query
            String totalUsersQuery = "SELECT COUNT(*) as total FROM Users";
            
            // Users by role query
            String roleCountQuery = "SELECT role, COUNT(*) as count FROM Users GROUP BY role";
            
            int totalUsers = 0;
            int adminCount = 0;
            int instructorCount = 0;
            int studentCount = 0;
            
            // Execute total users query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(totalUsersQuery)) {
                
                if (rs.next()) {
                    totalUsers = rs.getInt("total");
                }
            }
            
            // Execute role count query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(roleCountQuery)) {
                
                while (rs.next()) {
                    String role = rs.getString("role");
                    int count = rs.getInt("count");
                    
                    if ("Admin".equalsIgnoreCase(role)) {
                        adminCount = count;
                    } else if ("Instructor".equalsIgnoreCase(role)) {
                        instructorCount = count;
                    } else if ("Student".equalsIgnoreCase(role)) {
                        studentCount = count;
                    }
                }
            }
            
            // Create total users row
            Label totalUsersLabel = new Label("Total Users: " + totalUsers);
            totalUsersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            // Create role breakdown
            HBox roleBreakdown = new HBox(20);
            roleBreakdown.setAlignment(Pos.CENTER);
            
            VBox adminBox = createCountBox("Admins", adminCount, DANGER_COLOR);
            VBox instructorBox = createCountBox("Instructors", instructorCount, SECONDARY_COLOR);
            VBox studentBox = createCountBox("Students", studentCount, WARNING_COLOR);
            
            roleBreakdown.getChildren().addAll(adminBox, instructorBox, studentBox);
            
            // Add pie chart for visual representation
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Admins", adminCount),
                new PieChart.Data("Instructors", instructorCount),
                new PieChart.Data("Students", studentCount)
            );
            
            PieChart pieChart = new PieChart(pieChartData);
            pieChart.setTitle("Users by Role");
            pieChart.setLabelsVisible(true);
            pieChart.setLegendVisible(true);
            
            content.getChildren().addAll(totalUsersLabel, roleBreakdown, pieChart);
            
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading user data: " + e.getMessage());
            errorLabel.setTextFill(Color.web(DANGER_COLOR));
            content.getChildren().add(errorLabel);
        }
        
        card.getChildren().addAll(cardTitle, content);
        return card;
    }
    
    /**
     * Creates a card showing course statistics
     */
    private VBox createCourseStatsCard() {
        VBox card = new VBox();
        card.setSpacing(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 8;");
        
        // Add drop shadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        card.setEffect(dropShadow);
        
        // Card title
        Label cardTitle = new Label("2. Course Analytics");
        cardTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        cardTitle.setTextFill(Color.web(PRIMARY_COLOR));
        
        // Content
        VBox content = new VBox(10);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total courses
            String totalCoursesQuery = "SELECT COUNT(*) as total FROM Courses";
            
            // Courses per instructor
            String coursesPerInstructorQuery = "SELECT u.username, COUNT(c.courseID) as course_count " +
                                              "FROM Users u " +
                                              "JOIN Courses c ON u.userID = c.createdBy " +
                                              "GROUP BY u.userID " +
                                              "ORDER BY course_count DESC";
            
            // Enrollments per course
            String enrollmentsPerCourseQuery = "SELECT c.courseName, COUNT(e.enrollmentID) as enrollment_count " +
                                              "FROM Courses c " +
                                              "LEFT JOIN Enrollments e ON c.courseID = e.courseID " +
                                              "GROUP BY c.courseID " +
                                              "ORDER BY enrollment_count DESC " +
                                              "LIMIT 5";
            
            int totalCourses = 0;
            
            // Execute total courses query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(totalCoursesQuery)) {
                
                if (rs.next()) {
                    totalCourses = rs.getInt("total");
                }
            }
            
            // Create total courses label
            Label totalCoursesLabel = new Label("Total Courses: " + totalCourses);
            totalCoursesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            // Create courses per instructor table
            TableView<CoursePerInstructorData> instructorTable = new TableView<>();
            
            TableColumn<CoursePerInstructorData, String> instructorColumn = new TableColumn<>("Instructor");
            instructorColumn.setCellValueFactory(data -> data.getValue().instructorNameProperty());
            instructorColumn.setPrefWidth(200);
            
            TableColumn<CoursePerInstructorData, Integer> courseCountColumn = new TableColumn<>("Courses Created");
            courseCountColumn.setCellValueFactory(data -> data.getValue().courseCountProperty().asObject());
            courseCountColumn.setPrefWidth(120);
            
            instructorTable.getColumns().addAll(instructorColumn, courseCountColumn);
            
            ObservableList<CoursePerInstructorData> instructorData = FXCollections.observableArrayList();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(coursesPerInstructorQuery)) {
                
                while (rs.next()) {
                    String instructorName = rs.getString("username");
                    int courseCount = rs.getInt("course_count");
                    
                    instructorData.add(new CoursePerInstructorData(instructorName, courseCount));
                }
            }
            
            instructorTable.setItems(instructorData);
            instructorTable.setPrefHeight(200);
            
            // Create enrollment bar chart
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            
            BarChart<String, Number> enrollmentChart = new BarChart<>(xAxis, yAxis);
            enrollmentChart.setTitle("Enrollment Count per Course");
            xAxis.setLabel("Course");
            yAxis.setLabel("Enrollment Count");
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Enrollments");
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(enrollmentsPerCourseQuery)) {
                
                while (rs.next()) {
                    String courseName = rs.getString("courseName");
                    int enrollmentCount = rs.getInt("enrollment_count");
                    
                    series.getData().add(new XYChart.Data<>(courseName, enrollmentCount));
                }
            }
            
            enrollmentChart.getData().add(series);
            
            content.getChildren().addAll(totalCoursesLabel, 
                                        new Label("Courses per Instructor:"), 
                                        instructorTable,
                                        new Label("Enrollment Count per Course:"),
                                        enrollmentChart);
            
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading course data: " + e.getMessage());
            errorLabel.setTextFill(Color.web(DANGER_COLOR));
            content.getChildren().add(errorLabel);
        }
        
        card.getChildren().addAll(cardTitle, content);
        return card;
    }
    
    /**
     * Creates a card showing quiz statistics
     */
    private VBox createQuizStatsCard() {
        VBox card = new VBox();
        card.setSpacing(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 8;");
        
        // Add drop shadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        card.setEffect(dropShadow);
        
        // Card title
        Label cardTitle = new Label("3. Quiz Questions by Subject");
        cardTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        cardTitle.setTextFill(Color.web(PRIMARY_COLOR));
        
        // Content
        VBox content = new VBox(10);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Quiz questions by subject
            String quizBySubjectQuery = "SELECT subject, COUNT(*) as question_count " +
                                        "FROM quiz_questions " +
                                        "GROUP BY subject " +
                                        "ORDER BY question_count DESC";
            
            // Create pie chart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(quizBySubjectQuery)) {
                
                while (rs.next()) {
                    String subject = rs.getString("subject");
                    int count = rs.getInt("question_count");
                    
                    pieChartData.add(new PieChart.Data(subject + " (" + count + ")", count));
                }
            }
            
            if (!pieChartData.isEmpty()) {
                PieChart subjectPieChart = new PieChart(pieChartData);
                subjectPieChart.setTitle("Quiz Questions per Subject");
                subjectPieChart.setLabelsVisible(true);
                
                content.getChildren().add(subjectPieChart);
            } else {
                content.getChildren().add(new Label("No quiz questions found"));
            }
            
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading quiz data: " + e.getMessage());
            errorLabel.setTextFill(Color.web(DANGER_COLOR));
            content.getChildren().add(errorLabel);
        }
        
        card.getChildren().addAll(cardTitle, content);
        return card;
    }
    
    /**
     * Creates a card showing instructor statistics
     */
    private VBox createInstructorStatsCard() {
        VBox card = new VBox();
        card.setSpacing(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 8;");
        
        // Add drop shadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        card.setEffect(dropShadow);
        
        // Card title
        Label cardTitle = new Label("4. Instructor Contributions");
        cardTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        cardTitle.setTextFill(Color.web(PRIMARY_COLOR));
        
        // Content
        VBox content = new VBox(10);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Instructor contributions query
            String instructorContributionsQuery = "SELECT u.username, COUNT(c.courseID) as course_count " +
                                                 "FROM Users u " +
                                                 "JOIN Courses c ON u.userID = c.createdBy " +
                                                 "WHERE u.role = 'Instructor' " +
                                                 "GROUP BY u.userID " +
                                                 "ORDER BY course_count DESC";
            
            // Create table view
            TableView<InstructorContributionData> instructorTable = new TableView<>();
            
            TableColumn<InstructorContributionData, String> instructorColumn = new TableColumn<>("Instructor");
            instructorColumn.setCellValueFactory(data -> data.getValue().instructorNameProperty());
            instructorColumn.setPrefWidth(250);
            
            TableColumn<InstructorContributionData, Integer> courseCountColumn = new TableColumn<>("Courses Created");
            courseCountColumn.setCellValueFactory(data -> data.getValue().courseCountProperty().asObject());
            courseCountColumn.setPrefWidth(150);
            
            instructorTable.getColumns().addAll(instructorColumn, courseCountColumn);
            
            ObservableList<InstructorContributionData> instructorData = FXCollections.observableArrayList();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(instructorContributionsQuery)) {
                
                while (rs.next()) {
                    String instructorName = rs.getString("username");
                    int courseCount = rs.getInt("course_count");
                    
                    instructorData.add(new InstructorContributionData(instructorName, courseCount));
                }
            }
            
            instructorTable.setItems(instructorData);
            instructorTable.setPrefHeight(250);
            
            // Bar chart for visual representation
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            
            BarChart<String, Number> contributionChart = new BarChart<>(xAxis, yAxis);
            contributionChart.setTitle("Number of Courses Created by Each Instructor");
            xAxis.setLabel("Instructor");
            yAxis.setLabel("Courses Created");
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Courses Created");
            
            // Only add top 5 instructors to the chart for clarity
            int count = 0;
            for (InstructorContributionData data : instructorData) {
                if (count < 5) {
                    series.getData().add(new XYChart.Data<>(data.instructorNameProperty().get(), 
                                                          data.courseCountProperty().get()));
                    count++;
                } else {
                    break;
                }
            }
            
            contributionChart.getData().add(series);
            
            content.getChildren().addAll(instructorTable, contributionChart);
            
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading instructor data: " + e.getMessage());
            errorLabel.setTextFill(Color.web(DANGER_COLOR));
            content.getChildren().add(errorLabel);
        }
        
        card.getChildren().addAll(cardTitle, content);
        return card;
    }
    
    /**
     * Creates a card showing assignment statistics
     */
    private VBox createAssignmentStatsCard() {
        VBox card = new VBox();
        card.setSpacing(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 8;");
        
        // Add drop shadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        card.setEffect(dropShadow);
        
        // Card title
        Label cardTitle = new Label("5. Assignments & Deadlines");
        cardTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        cardTitle.setTextFill(Color.web(PRIMARY_COLOR));
        
        // Content
        VBox content = new VBox(10);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Upcoming assignments query
            String upcomingAssignmentsQuery = "SELECT a.title, c.courseName, a.dueDate, a.points, a.priority " +
                                             "FROM Assignments a " +
                                             "JOIN Courses c ON a.courseID = c.courseID " +
                                             "WHERE a.dueDate >= CURRENT_DATE " +
                                             "ORDER BY a.dueDate ASC " +
                                             "LIMIT 10";
            
            // Assignments status query
            String assignmentStatusQuery = "SELECT status, COUNT(*) as count " +
                                          "FROM AssignmentProgress " +
                                          "GROUP BY status";
            
            // Create upcoming assignments table
            Label upcomingLabel = new Label("Upcoming Assignment Deadlines:");
            upcomingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            TableView<UpcomingAssignmentData> assignmentsTable = new TableView<>();
            
            TableColumn<UpcomingAssignmentData, String> titleColumn = new TableColumn<>("Assignment");
            titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());
            titleColumn.setPrefWidth(200);
            
            TableColumn<UpcomingAssignmentData, String> courseColumn = new TableColumn<>("Course");
            courseColumn.setCellValueFactory(data -> data.getValue().courseProperty());
            courseColumn.setPrefWidth(150);
            
            TableColumn<UpcomingAssignmentData, String> dueDateColumn = new TableColumn<>("Due Date");
            dueDateColumn.setCellValueFactory(data -> data.getValue().dueDateProperty());
            dueDateColumn.setPrefWidth(100);
            
            TableColumn<UpcomingAssignmentData, String> pointsColumn = new TableColumn<>("Points");
            pointsColumn.setCellValueFactory(data -> data.getValue().pointsProperty());
            pointsColumn.setPrefWidth(70);
            
            TableColumn<UpcomingAssignmentData, String> priorityColumn = new TableColumn<>("Priority");
            priorityColumn.setCellValueFactory(data -> data.getValue().priorityProperty());
            priorityColumn.setPrefWidth(80);
            priorityColumn.setCellFactory(column -> new TableCell<UpcomingAssignmentData, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.equalsIgnoreCase("High")) {
                            setTextFill(Color.web(DANGER_COLOR));
                            setStyle("-fx-font-weight: bold;");
                        } else if (item.equalsIgnoreCase("Medium")) {
                            setTextFill(Color.web(WARNING_COLOR));
                        } else {
                            setTextFill(Color.web(SECONDARY_COLOR));
                        }
                    }
                }
            });
            
            assignmentsTable.getColumns().addAll(titleColumn, courseColumn, dueDateColumn, pointsColumn, priorityColumn);
            
            ObservableList<UpcomingAssignmentData> assignmentData = FXCollections.observableArrayList();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(upcomingAssignmentsQuery)) {
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                
                while (rs.next()) {
                    String title = rs.getString("title");
                    String courseName = rs.getString("courseName");
                    LocalDate dueDate = rs.getDate("dueDate").toLocalDate();
                    String points = rs.getString("points");
                    String priority = rs.getString("priority");
                    
                    assignmentData.add(new UpcomingAssignmentData(
                        title, courseName, dueDate.format(formatter), points, priority));
                }
            }
            
            assignmentsTable.setItems(assignmentData);
            assignmentsTable.setPrefHeight(250);
            
            // Assignment status pie chart
            Label statusLabel = new Label("Assignment Completion Status:");
            statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(assignmentStatusQuery)) {
                
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("count");
                    
                    pieChartData.add(new PieChart.Data(status + " (" + count + ")", count));
                }
            }
            
            PieChart statusChart = new PieChart(pieChartData);
            statusChart.setTitle("Assignment Status Distribution");
            statusChart.setLabelsVisible(true);
            
            content.getChildren().addAll(upcomingLabel, assignmentsTable, statusLabel, statusChart);
            
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading assignment data: " + e.getMessage());
            errorLabel.setTextFill(Color.web(DANGER_COLOR));
            content.getChildren().add(errorLabel);
        }
        
        card.getChildren().addAll(cardTitle, content);
        return card;
    }
    
    /**
     * Creates a count box for statistics display
     */
    private VBox createCountBox(String label, int count, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 5;");
        
        Label countLabel = new Label(String.valueOf(count));
        countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        countLabel.setTextFill(Color.web(color));
        
        Label nameLabel = new Label(label);
        nameLabel.setFont(Font.font("Arial", 14));
        
        box.getChildren().addAll(countLabel, nameLabel);
        
        return box;
    }
    
    /**
     * Data class for courses per instructor
     */
    private static class CoursePerInstructorData {
        private final SimpleStringProperty instructorName;
        private final SimpleIntegerProperty courseCount;
        
        public CoursePerInstructorData(String instructorName, int courseCount) {
            this.instructorName = new SimpleStringProperty(instructorName);
            this.courseCount = new SimpleIntegerProperty(courseCount);
        }
        
        public SimpleStringProperty instructorNameProperty() {
            return instructorName;
        }
        
        public SimpleIntegerProperty courseCountProperty() {
            return courseCount;
        }
    }
    
    /**
     * Data class for instructor contributions
     */
    private static class InstructorContributionData {
        private final SimpleStringProperty instructorName;
        private final SimpleIntegerProperty courseCount;
        
        public InstructorContributionData(String instructorName, int courseCount) {
            this.instructorName = new SimpleStringProperty(instructorName);
            this.courseCount = new SimpleIntegerProperty(courseCount);
        }
        
        public SimpleStringProperty instructorNameProperty() {
            return instructorName;
        }
        
        public SimpleIntegerProperty courseCountProperty() {
            return courseCount;
        }
    }
    
    /**
     * Data class for upcoming assignments
     */
    private static class UpcomingAssignmentData {
        private final SimpleStringProperty title;
        private final SimpleStringProperty course;
        private final SimpleStringProperty dueDate;
        private final SimpleStringProperty points;
        private final SimpleStringProperty priority;
        
        public UpcomingAssignmentData(String title, String course, String dueDate, String points, String priority) {
            this.title = new SimpleStringProperty(title);
            this.course = new SimpleStringProperty(course);
            this.dueDate = new SimpleStringProperty(dueDate);
            this.points = new SimpleStringProperty(points);
            this.priority = new SimpleStringProperty(priority);
        }
        
        public SimpleStringProperty titleProperty() {
            return title;
        }
        
        public SimpleStringProperty courseProperty() {
            return course;
        }
        
        public SimpleStringProperty dueDateProperty() {
            return dueDate;
        }
        
        public SimpleStringProperty pointsProperty() {
            return points;
        }
        
        public SimpleStringProperty priorityProperty() {
            return priority;
        }
    }
}