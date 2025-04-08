package controllers_Instructors;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import dao.DatabaseConnection;

/**
 * Quizzes page for managing course quizzes
 */
public class QuizzesPage {
    
    /**
     * Class to represent a quiz from the database
     */
    public static class Quiz {
        private String subject;
        private int questionCount;
        private String courseId;
        private String courseName;
        private String status;
        private java.util.Date dueDate;
        private int totalStudents;
        private int completedStudents;
        
        public Quiz(String subject, int questionCount, String courseId, String courseName, 
                   String status, java.util.Date dueDate, int totalStudents, int completedStudents) {
            this.subject = subject;
            this.questionCount = questionCount;
            this.courseId = courseId;
            this.courseName = courseName;
            this.status = status;
            this.dueDate = dueDate;
            this.totalStudents = totalStudents;
            this.completedStudents = completedStudents;
        }
        
        public String getSubject() { return subject; }
        public int getQuestionCount() { return questionCount; }
        public String getCourseId() { return courseId; }
        public String getCourseName() { return courseName; }
        public String getStatus() { return status; }
        public java.util.Date getDueDate() { return dueDate; }
        public int getTotalStudents() { return totalStudents; }
        public int getCompletedStudents() { return completedStudents; }
    }
    
    /**
     * Data Access Object for Quiz operations
     */
    public static class QuizDAO {
        
        /**
         * Get all quizzes from the database
         */
        public List<Quiz> getAllQuizzes() {
            List<Quiz> quizzes = new ArrayList<>();
            Map<String, String> subjectToCourse = mapSubjectsToCourses();
            
            // Get counts of questions by subject
            String query = "SELECT subject, COUNT(*) as question_count FROM quiz_questions GROUP BY subject";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String subject = rs.getString("subject");
                    int questionCount = rs.getInt("question_count");
                    
                    // Find matching course for this subject
                    String courseId = "0";
                    String courseName = subject; // Default to subject name if no matching course
                    
                    if (subjectToCourse.containsKey(subject)) {
                        String[] courseInfo = subjectToCourse.get(subject).split("\\|");
                        courseId = courseInfo[0];
                        courseName = courseInfo[1];
                    }
                    
                    // Generate quiz metadata (status, dates, completion)
                    // In a real app, this would come from a dedicated quizzes table
                    String status = getRandomStatus();
                    java.util.Date dueDate = generateDueDate(status);
                    int totalStudents = generateTotalStudents(status);
                    int completedStudents = generateCompletedStudents(status, totalStudents);
                    
                    Quiz quiz = new Quiz(
                        subject,
                        questionCount,
                        courseId,
                        courseName,
                        status,
                        dueDate,
                        totalStudents,
                        completedStudents
                    );
                    
                    quizzes.add(quiz);
                }
                
            } catch (SQLException e) {
                System.err.println("Error retrieving quizzes: " + e.getMessage());
            }
            
            return quizzes;
        }
        
        /**
         * Generate a due date based on quiz status
         */
        private java.util.Date generateDueDate(String status) {
            Calendar cal = Calendar.getInstance();
            
            if (status.equals("Completed")) {
                // Past date for completed quizzes
                cal.add(Calendar.DAY_OF_MONTH, -((int)(Math.random() * 30) + 1));
            } else if (status.equals("Scheduled")) {
                // Future date for scheduled quizzes
                cal.add(Calendar.DAY_OF_MONTH, (int)(Math.random() * 30) + 10);
            } else if (status.equals("Active")) {
                // Near future for active quizzes
                cal.add(Calendar.DAY_OF_MONTH, (int)(Math.random() * 10) + 1);
            }
            
            return cal.getTime();
        }
        
        /**
         * Get all courses from the database
         */
        public List<String> getAllCourseNames() {
            List<String> courseNames = new ArrayList<>();
            courseNames.add("All Courses"); // Default option
            
            String query = "SELECT courseName FROM Courses ORDER BY courseName";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    courseNames.add(rs.getString("courseName"));
                }
                
            } catch (SQLException e) {
                System.err.println("Error retrieving course names: " + e.getMessage());
            }
            
            return courseNames;
        }
        
        /**
         * Map subject names to course IDs and names
         * This creates a relationship between quiz subjects and courses
         */
        private Map<String, String> mapSubjectsToCourses() {
            Map<String, String> mapping = new HashMap<>();
            
            // Define mappings between subjects in quiz_questions and courses
            // Format: "subject" -> "courseID|courseName"
            mapping.put("Mathematics", "1|Mathematics");
            mapping.put("History", "2|History");
            mapping.put("Science", "4|Biology"); // Mapping Science to Biology course
            mapping.put("English", "6|Literature"); // Mapping English to Literature course
            
            // Attempt to get actual course data from database to improve mappings
            String query = "SELECT courseID, courseName FROM Courses";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    int courseId = rs.getInt("courseID");
                    String courseName = rs.getString("courseName");
                    
                    // Update existing mappings or add new ones based on course name
                    if (courseName.contains("Math") || courseName.contains("Calculus")) {
                        mapping.put("Mathematics", courseId + "|" + courseName);
                    } else if (courseName.contains("History") || courseName.contains("Civilization")) {
                        mapping.put("History", courseId + "|" + courseName);
                    } else if (courseName.contains("Biology") || courseName.contains("Science")) {
                        mapping.put("Science", courseId + "|" + courseName);
                    } else if (courseName.contains("Literature") || courseName.contains("English")) {
                        mapping.put("English", courseId + "|" + courseName);
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Error mapping subjects to courses: " + e.getMessage());
            }
            
            return mapping;
        }
        
        /**
         * Generate a random number of total students based on status
         */
        private int generateTotalStudents(String status) {
            if (status.equals("Draft")) {
                return 0;
            } else {
                return (int)(Math.random() * 100) + 30;
            }
        }
        
        /**
         * Generate a random number of completed students based on status and total
         */
        private int generateCompletedStudents(String status, int total) {
            if (status.equals("Draft") || status.equals("Scheduled")) {
                return 0;
            } else if (status.equals("Completed")) {
                return (int)(total * (0.8 + Math.random() * 0.2)); // 80-100% completion
            } else {
                return (int)(total * Math.random() * 0.8); // 0-80% completion
            }
        }
        
        /**
         * Get random status for demonstration purposes
         */
        private String getRandomStatus() {
            String[] statuses = {"Active", "Completed", "Scheduled", "Draft"};
            return statuses[(int)(Math.random() * statuses.length)];
        }
        
        /**
         * Get the number of questions for a specific subject
         */
        public int getQuestionCount(String subject) {
            String query = "SELECT COUNT(*) FROM quiz_questions WHERE subject = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, subject);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Error getting question count: " + e.getMessage());
            }
            
            return 0;
        }
    }
    
    private QuizDAO quizDAO = new QuizDAO();
    private List<Quiz> allQuizzes;
    private List<Quiz> filteredQuizzes;
    
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Quizzes");
        title.getStyleClass().add("page-title");
        
        // Filter controls
        HBox filterContainer = new HBox(10);
        filterContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Get course names from database
        List<String> courseNames = quizDAO.getAllCourseNames();
        ComboBox<String> courseFilter = new ComboBox<>(FXCollections.observableArrayList(courseNames));
        courseFilter.setValue("All Courses");
        courseFilter.setPrefWidth(200);
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "Active", "Completed", "Scheduled", "Draft");
        statusFilter.setValue("All Status");
        
        Button applyFilterBtn = new Button("Apply");
        applyFilterBtn.getStyleClass().add("primary-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button createQuizBtn = new Button("+ Create Quiz");
        createQuizBtn.getStyleClass().add("success-button");
        
        filterContainer.getChildren().addAll(
            courseFilter, statusFilter, applyFilterBtn, spacer, createQuizBtn);
        
        // Load quizzes from database
        allQuizzes = quizDAO.getAllQuizzes();
        filteredQuizzes = new ArrayList<>(allQuizzes);
        
        // Create quizzes list container
        VBox quizzesList = new VBox(15);
        quizzesList.setPadding(new Insets(10));
        
        // Apply filter button action
        applyFilterBtn.setOnAction(e -> {
            String selectedCourse = courseFilter.getValue();
            String selectedStatus = statusFilter.getValue();
            
            // Filter quizzes
            filteredQuizzes.clear();
            for (Quiz quiz : allQuizzes) {
                if (("All Courses".equals(selectedCourse) || quiz.getCourseName().equals(selectedCourse)) &&
                    ("All Status".equals(selectedStatus) || quiz.getStatus().equals(selectedStatus))) {
                    filteredQuizzes.add(quiz);
                }
            }
            
            // Update UI
            updateQuizzesList(quizzesList);
        });
        
        // Create quiz button action
        createQuizBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Create Quiz");
            alert.setHeaderText("Quiz Creation");
            alert.setContentText("Quiz creation functionality would be implemented here.");
            alert.showAndWait();
        });
        
        // Initial population of quizzes list
        updateQuizzesList(quizzesList);
        
        ScrollPane scrollPane = new ScrollPane(quizzesList);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        view.getChildren().addAll(title, filterContainer, scrollPane);
        return view;
    }
    
    /**
     * Update the quizzes list with filtered quizzes
     */
    private void updateQuizzesList(VBox quizzesList) {
        quizzesList.getChildren().clear();
        
        if (filteredQuizzes.isEmpty()) {
            Label noQuizzesLabel = new Label("No quizzes match your filters.");
            noQuizzesLabel.getStyleClass().add("no-content-label");
            quizzesList.getChildren().add(noQuizzesLabel);
        } else {
            for (Quiz quiz : filteredQuizzes) {
                String dateString = formatDate(quiz.getDueDate(), quiz.getStatus());
                quizzesList.getChildren().add(createQuizItem(
                    quiz.getSubject() + " Quiz (" + quiz.getQuestionCount() + " questions)",
                    quiz.getCourseName(), 
                    quiz.getStatus(),
                    dateString,
                    quiz.getTotalStudents(),
                    quiz.getCompletedStudents()
                ));
            }
        }
    }
    
    /**
     * Format date based on quiz status
     */
    private String formatDate(java.util.Date date, String status) {
        if (status.equals("Draft")) {
            return "Not published";
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        return sdf.format(date);
    }
    
    private HBox createQuizItem(String title, String course, String status,
                               String date, int totalStudents, int completed) {
        HBox item = new HBox();
        item.setPadding(new Insets(15));
        item.setSpacing(15);
        item.getStyleClass().add("quiz-item");
        item.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 4px;");
        
        // Status indicator - using fixed height instead of binding
        Rectangle statusIndicator = new Rectangle(8, 80); // Fixed height of 80px
        if (status.equals("Active")) {
            statusIndicator.setStyle("-fx-fill: #2ecc71;"); // Green
        } else if (status.equals("Completed")) {
            statusIndicator.setStyle("-fx-fill: #3498db;"); // Blue
        } else if (status.equals("Scheduled")) {
            statusIndicator.setStyle("-fx-fill: #f1c40f;"); // Yellow
        } else {
            statusIndicator.setStyle("-fx-fill: #95a5a6;"); // Gray
        }
        
        // Quiz information
        VBox quizInfo = new VBox(5);
        quizInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(quizInfo, Priority.ALWAYS);
        
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label quizTitle = new Label(title);
        quizTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label statusLabel = new Label(status);
        statusLabel.setPadding(new Insets(2, 8, 2, 8));
        
        // Set status label style based on status
        if (status.equals("Active")) {
            statusLabel.setStyle("-fx-background-color: #e8f8f5; -fx-text-fill: #27ae60; -fx-border-color: #27ae60; -fx-border-radius: 3px;");
        } else if (status.equals("Completed")) {
            statusLabel.setStyle("-fx-background-color: #eaf2f8; -fx-text-fill: #2980b9; -fx-border-color: #2980b9; -fx-border-radius: 3px;");
        } else if (status.equals("Scheduled")) {
            statusLabel.setStyle("-fx-background-color: #fef9e7; -fx-text-fill: #d35400; -fx-border-color: #d35400; -fx-border-radius: 3px;");
        } else {
            statusLabel.setStyle("-fx-background-color: #f2f3f4; -fx-text-fill: #7f8c8d; -fx-border-color: #7f8c8d; -fx-border-radius: 3px;");
        }
        
        titleRow.getChildren().addAll(quizTitle, statusLabel);
        
        Label courseLabel = new Label("Course: " + course);
        courseLabel.setStyle("-fx-text-fill: #555555;");
        
        Label dateLabel = new Label(status.equals("Draft") ? "Not published yet" :
                           (status.equals("Completed") ? "Completed on: " + date : "Due: " + date));
        dateLabel.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
        
        quizInfo.getChildren().addAll(titleRow, courseLabel, dateLabel);
        
        // Quiz stats
        VBox statsBox = new VBox(10);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setMinWidth(150);
        
        if (!status.equals("Draft")) {
            double completionRate = totalStudents > 0 ? (double) completed / totalStudents * 100 : 0;
            
            Label completionLabel = new Label(String.format("%.1f%%", completionRate));
            completionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            
            Label studentsLabel = new Label(completed + "/" + totalStudents + " Students");
            studentsLabel.setStyle("-fx-text-fill: #555555;");
            
            ProgressBar completionBar = new ProgressBar(completionRate / 100);
            completionBar.setPrefWidth(120);
            completionBar.setStyle("-fx-accent: #3498db;");
            
            statsBox.getChildren().addAll(completionLabel, completionBar, studentsLabel);
        } else {
            Label draftLabel = new Label("Draft");
            draftLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
            statsBox.getChildren().add(draftLabel);
        }
        
        // Actions
        VBox actionsBox = new VBox(5);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setMinWidth(100);
        
        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("View Quiz");
            alert.setHeaderText(title);
            alert.setContentText("This would display the quiz questions for " + title);
            alert.showAndWait();
        });
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        editBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Edit Quiz");
            alert.setHeaderText(title);
            alert.setContentText("This would allow editing the quiz: " + title);
            alert.showAndWait();
        });
        
        Button resultsBtn = new Button("Results");
        resultsBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        resultsBtn.setMaxWidth(Double.MAX_VALUE);
        resultsBtn.setDisable(status.equals("Draft") || status.equals("Scheduled"));
        resultsBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Quiz Results");
            alert.setHeaderText(title + " Results");
            alert.setContentText("Completion rate: " + completed + "/" + totalStudents + " students\n" +
                               "This would display detailed results for the quiz.");
            alert.showAndWait();
        });
        
        actionsBox.getChildren().addAll(viewBtn, editBtn);
        
        if (status.equals("Active") || status.equals("Completed")) {
            actionsBox.getChildren().add(resultsBtn);
        }
        
        item.getChildren().addAll(statusIndicator, quizInfo, statsBox, actionsBox);
        return item;
    }
}