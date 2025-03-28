package controllers_Insructrors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

/**
 * Dashboard Overview page showing key metrics and statistics
 */
public class DashboardOverview {
    private String currentUsername = "Jane Instructor";
    
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Instructor Dashboard");
        title.getStyleClass().add("page-title");
        
        // Welcome message with date
        HBox welcomeBox = new HBox();
        welcomeBox.setPadding(new Insets(0, 0, 10, 0));
        
        VBox welcomeMsg = new VBox(5);
        Label greeting = new Label("Welcome back, " + currentUsername + "!");
        greeting.getStyleClass().add("welcome-message");
        
        Label dateLabel = new Label("Today is June 21, 2023");
        dateLabel.getStyleClass().add("date-message");
        
        welcomeMsg.getChildren().addAll(greeting, dateLabel);
        welcomeBox.getChildren().add(welcomeMsg);
        
        // Quick stats section
        HBox statsContainer = createStatsSection();
        statsContainer.getStyleClass().add("stats-container");
        
        // To-do list section
        VBox todoSection = createTodoSection();
        todoSection.getStyleClass().add("todo-section");
        
        // Course progress section
        VBox courseProgressSection = createCourseProgressSection();
        courseProgressSection.getStyleClass().add("course-progress-section");
        
        // Upcoming deadlines section
        VBox upcomingDeadlines = createUpcomingDeadlinesSection();
        upcomingDeadlines.getStyleClass().add("deadlines-section");
        
        // Recent student activity
        VBox recentActivity = createRecentActivitySection();
        recentActivity.getStyleClass().add("activity-section");
        
        // Quick actions section
        HBox quickActions = createQuickActionButtons();
        quickActions.getStyleClass().add("actions-container");
        
        // Create a horizontal container for course progress and deadlines
        HBox progressDeadlinesContainer = new HBox(20, courseProgressSection, upcomingDeadlines);
        HBox.setHgrow(progressDeadlinesContainer, Priority.ALWAYS);
        
        // Combine all sections
        view.getChildren().addAll(
            title,
            welcomeBox,
            statsContainer,
            todoSection,
            progressDeadlinesContainer,
            recentActivity,
            quickActions
        );
        
        return view;
    }
    
    private HBox createStatsSection() {
        HBox container = new HBox(15);
        container.setPadding(new Insets(10));
        container.setAlignment(Pos.CENTER);
        
        // Create stat cards
        VBox coursesCard = createStatCard("Courses", "5", "3 active, 2 draft", "courses-stat");
        VBox studentsCard = createStatCard("Students", "248", "+12 this month", "students-stat");
        VBox assignmentsCard = createStatCard("Assignments", "18", "4 pending review", "assignments-stat");
        VBox ratingsCard = createStatCard("Avg. Rating", "4.8", "from 156 reviews", "ratings-stat");
        
        container.getChildren().addAll(coursesCard, studentsCard, assignmentsCard, ratingsCard);
        HBox.setHgrow(coursesCard, Priority.ALWAYS);
        HBox.setHgrow(studentsCard, Priority.ALWAYS);
        HBox.setHgrow(assignmentsCard, Priority.ALWAYS);
        HBox.setHgrow(ratingsCard, Priority.ALWAYS);
        
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
    
    private VBox createTodoSection() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("todo-container");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("To-Do List");
        title.getStyleClass().add("section-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addTaskBtn = new Button("+ Add Task");
        addTaskBtn.getStyleClass().add("add-task-button");
        
        header.getChildren().addAll(title, spacer, addTaskBtn);
        
        // Todo items
        VBox todoList = new VBox(5);
        todoList.getChildren().addAll(
            createTodoItem("Review assignment submissions for 'Introduction to Python'", true),
            createTodoItem("Prepare lecture materials for next week", false),
            createTodoItem("Grade final projects for 'Web Development'", false),
            createTodoItem("Update course syllabus for next semester", false),
            createTodoItem("Respond to student forum questions", true)
        );
        
        container.getChildren().addAll(header, todoList);
        return container;
    }
    
    private HBox createTodoItem(String task, boolean highPriority) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(8));
        item.getStyleClass().add("todo-item");
        
        CheckBox checkbox = new CheckBox();
        
        Label taskLabel = new Label(task);
        taskLabel.setWrapText(true);
        HBox.setHgrow(taskLabel, Priority.ALWAYS);
        
        if (highPriority) {
            Label priorityLabel = new Label("High Priority");
            priorityLabel.getStyleClass().add("priority-label");
            item.getChildren().addAll(checkbox, taskLabel, priorityLabel);
        } else {
            item.getChildren().addAll(checkbox, taskLabel);
        }
        
        return item;
    }
    
    private VBox createCourseProgressSection() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("course-container");
        container.setPrefWidth(400);
        
        Label title = new Label("Course Progress");
        title.getStyleClass().add("section-title");
        
        // Course progress items
        VBox courseList = new VBox(15);
        courseList.getChildren().addAll(
            createCourseProgressItem("Introduction to Python", 85),
            createCourseProgressItem("Web Development Fundamentals", 72),
            createCourseProgressItem("Data Science Essentials", 45)
        );
        
        container.getChildren().addAll(title, courseList);
        return container;
    }
    
    private VBox createCourseProgressItem(String courseName, int progressPercent) {
        VBox item = new VBox(5);
        item.getStyleClass().add("course-progress-item");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(courseName);
        nameLabel.getStyleClass().add("course-name");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label percentLabel = new Label(progressPercent + "%");
        percentLabel.getStyleClass().add("progress-percent");
        
        header.getChildren().addAll(nameLabel, spacer, percentLabel);
        
        // Progress bar
        ProgressBar progressBar = new ProgressBar(progressPercent / 100.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add("course-progress-bar");
        
        item.getChildren().addAll(header, progressBar);
        return item;
    }
    
    private VBox createUpcomingDeadlinesSection() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("deadlines-container");
        container.setPrefWidth(400);
        
        Label title = new Label("Upcoming Deadlines");
        title.getStyleClass().add("section-title");
        
        // Deadline items
        VBox deadlinesList = new VBox(10);
        deadlinesList.getChildren().addAll(
            createDeadlineItem("Final Project Submission", "Introduction to Python", "June 25, 2023"),
            createDeadlineItem("Quiz #3", "Web Development Fundamentals", "June 27, 2023"),
            createDeadlineItem("Mid-term Exam", "Data Science Essentials", "July 2, 2023"),
            createDeadlineItem("Group Presentation", "Web Development Fundamentals", "July 10, 2023")
        );
        
        container.getChildren().addAll(title, deadlinesList);
        return container;
    }
    
    private HBox createDeadlineItem(String title, String course, String date) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10));
        item.getStyleClass().add("deadline-item");
        
        // Left color indicator
        Rectangle indicator = new Rectangle(5, 50);
        indicator.getStyleClass().add("deadline-indicator");
        
        // Details
        VBox details = new VBox(3);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("deadline-title");
        
        Label courseLabel = new Label(course);
        courseLabel.getStyleClass().add("deadline-course");
        
        Label dateLabel = new Label("Due: " + date);
        dateLabel.getStyleClass().add("deadline-date");
        
        details.getChildren().addAll(titleLabel, courseLabel, dateLabel);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        // Add button
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("view-button");
        viewBtn.setAlignment(Pos.CENTER_RIGHT);
        
        item.getChildren().addAll(indicator, details, viewBtn);
        return item;
    }
    
    private VBox createRecentActivitySection() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("recent-activity-section");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Recent Student Activity");
        title.getStyleClass().add("section-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button viewAllBtn = new Button("View All");
        viewAllBtn.getStyleClass().add("view-all-button");
        
        header.getChildren().addAll(title, spacer, viewAllBtn);
        
        // Activity list
        VBox activityList = new VBox(12);
        activityList.getChildren().addAll(
            createActivityItem("John Smith submitted an assignment", "Introduction to Python", "15 minutes ago"),
            createActivityItem("Emma Wilson joined your course", "Web Development Fundamentals", "1 hour ago"),
            createActivityItem("Michael Brown posted a question in forum", "Data Science Essentials", "3 hours ago"),
            createActivityItem("Sarah Johnson completed Quiz #2", "Introduction to Python", "Yesterday")
        );
        
        container.getChildren().addAll(header, activityList);
        return container;
    }
    
    private HBox createActivityItem(String activity, String course, String time) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(10));
        item.getStyleClass().add("activity-item");
        
        // Activity indicator
        Rectangle indicator = new Rectangle(8, 50);
        indicator.getStyleClass().add("activity-indicator");
        
        // Content
        VBox content = new VBox(5);
        Label activityLabel = new Label(activity);
        activityLabel.getStyleClass().add("activity-title");
        
        Label courseLabel = new Label(course);
        courseLabel.getStyleClass().add("activity-course");
        
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("activity-time");
        
        content.getChildren().addAll(activityLabel, courseLabel, timeLabel);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        item.getChildren().addAll(indicator, content);
        return item;
    }
    
    private HBox createQuickActionButtons() {
        HBox container = new HBox(15);
        container.setPadding(new Insets(10, 10, 30, 10));
        
        Button createCourseBtn = new Button("Create Course");
        createCourseBtn.getStyleClass().add("action-button");
        
        Button createAssignmentBtn = new Button("Create Assignment");
        createAssignmentBtn.getStyleClass().add("action-button");
        
        Button createQuizBtn = new Button("Create Quiz");
        createQuizBtn.getStyleClass().add("action-button");
        
        Button messageStudentsBtn = new Button("Message Students");
        messageStudentsBtn.getStyleClass().add("action-button");
        
        container.getChildren().addAll(createCourseBtn, createAssignmentBtn, createQuizBtn, messageStudentsBtn);
        return container;
    }
}