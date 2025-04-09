package controllers_Instructors;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;
import dao.DatabaseConnection;

/**
 * QuizzesPage provides instructors with a comprehensive interface for:
 * - Viewing existing quiz questions and student results
 * - Creating new quiz questions (up to 5 at once)
 * - Analyzing student performance on quizzes
 */
public class QuizzesPage {
    // Models for quiz data
    public static class QuizQuestion {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty subject;
        private final SimpleStringProperty question;
        private final SimpleStringProperty optionA;
        private final SimpleStringProperty optionB;
        private final SimpleStringProperty optionC;
        private final SimpleStringProperty optionD;
        private final SimpleStringProperty correctOption;

        public QuizQuestion(int id, String subject, String question, String optionA,
                         String optionB, String optionC, String optionD, String correctOption) {
            this.id = new SimpleIntegerProperty(id);
            this.subject = new SimpleStringProperty(subject);
            this.question = new SimpleStringProperty(question);
            this.optionA = new SimpleStringProperty(optionA);
            this.optionB = new SimpleStringProperty(optionB);
            this.optionC = new SimpleStringProperty(optionC);
            this.optionD = new SimpleStringProperty(optionD);
            this.correctOption = new SimpleStringProperty(correctOption);
        }

        // Getters and property methods
        public int getId() { return id.get(); }
        public SimpleIntegerProperty idProperty() { return id; }
        public String getSubject() { return subject.get(); }
        public SimpleStringProperty subjectProperty() { return subject; }
        public String getQuestion() { return question.get(); }
        public SimpleStringProperty questionProperty() { return question; }
        public String getOptionA() { return optionA.get(); }
        public SimpleStringProperty optionAProperty() { return optionA; }
        public String getOptionB() { return optionB.get(); }
        public SimpleStringProperty optionBProperty() { return optionB; }
        public String getOptionC() { return optionC.get(); }
        public SimpleStringProperty optionCProperty() { return optionC; }
        public String getOptionD() { return optionD.get(); }
        public SimpleStringProperty optionDProperty() { return optionD; }
        public String getCorrectOption() { return correctOption.get(); }
        public SimpleStringProperty correctOptionProperty() { return correctOption; }
    }

    public static class QuizResult {
        private final SimpleIntegerProperty resultId;
        private final SimpleIntegerProperty questionId;
        private final SimpleIntegerProperty studentId;
        private final SimpleStringProperty studentName;
        private final SimpleStringProperty selectedOption;
        private final SimpleBooleanProperty correct;
        private final SimpleStringProperty submissionDate;
        private final SimpleStringProperty questionText;

        public QuizResult(int resultId, int questionId, int studentId, String studentName,
                       String selectedOption, boolean correct, String submissionDate, String questionText) {
            this.resultId = new SimpleIntegerProperty(resultId);
            this.questionId = new SimpleIntegerProperty(questionId);
            this.studentId = new SimpleIntegerProperty(studentId);
            this.studentName = new SimpleStringProperty(studentName);
            this.selectedOption = new SimpleStringProperty(selectedOption);
            this.correct = new SimpleBooleanProperty(correct);
            this.submissionDate = new SimpleStringProperty(submissionDate);
            this.questionText = new SimpleStringProperty(questionText);
        }

        // Getters and property methods
        public int getResultId() { return resultId.get(); }
        public SimpleIntegerProperty resultIdProperty() { return resultId; }
        public int getQuestionId() { return questionId.get(); }
        public SimpleIntegerProperty questionIdProperty() { return questionId; }
        public int getStudentId() { return studentId.get(); }
        public SimpleIntegerProperty studentIdProperty() { return studentId; }
        public String getStudentName() { return studentName.get(); }
        public SimpleStringProperty studentNameProperty() { return studentName; }
        public String getSelectedOption() { return selectedOption.get(); }
        public SimpleStringProperty selectedOptionProperty() { return selectedOption; }
        public boolean isCorrect() { return correct.get(); }
        public SimpleBooleanProperty correctProperty() { return correct; }
        public String getSubmissionDate() { return submissionDate.get(); }
        public SimpleStringProperty submissionDateProperty() { return submissionDate; }
        public String getQuestionText() { return questionText.get(); }
        public SimpleStringProperty questionTextProperty() { return questionText; }
    }

    // Summary statistics for a subject
    public static class QuizStatistics {
        private final String subject;
        private final int totalQuestions;
        private final int totalAttempts;
        private final int totalCorrect;
        private final double averageScore;

        public QuizStatistics(String subject, int totalQuestions, int totalAttempts,
                           int totalCorrect, double averageScore) {
            this.subject = subject;
            this.totalQuestions = totalQuestions;
            this.totalAttempts = totalAttempts;
            this.totalCorrect = totalCorrect;
            this.averageScore = averageScore;
        }

        public String getSubject() { return subject; }
        public int getTotalQuestions() { return totalQuestions; }
        public int getTotalAttempts() { return totalAttempts; }
        public int getTotalCorrect() { return totalCorrect; }
        public double getAverageScore() { return averageScore; }
    }

    // New quiz form for entry with a better UI
    private static class QuizForm extends VBox {
        private final ComboBox<String> subjectField;
        private final TextArea questionField;
        private final TextField optionAField;
        private final TextField optionBField;
        private final TextField optionCField;
        private final TextField optionDField;
        private final ComboBox<String> correctOptionField;

        public QuizForm(Set<String> subjects) {
            super(15);
            setPadding(new Insets(20));
            setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                    "-fx-border-radius: 10; -fx-background-radius: 10;");
            
            // Add drop shadow effect
            setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));
            
            // Subject field with styled label
            Label subjectLabel = createFormLabel("Subject");
            
            subjectField = new ComboBox<>();
            subjectField.setEditable(true);
            subjectField.getItems().addAll(subjects);
            subjectField.setPrefWidth(400);
            subjectField.setPromptText("Enter or select subject");
            styleControl(subjectField, "-fx-font-size: 14px;");
            
            // Question field with styled label
            Label questionLabel = createFormLabel("Question");
            
            questionField = new TextArea();
            questionField.setPrefRowCount(3);
            questionField.setWrapText(true);
            questionField.setPromptText("Enter the question text");
            styleControl(questionField, "-fx-font-size: 14px;");
            
            // Options section with a colored container
            Label optionsLabel = createFormLabel("Options");
            
            VBox optionsContainer = new VBox(10);
            optionsContainer.setPadding(new Insets(15));
            optionsContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");
            
            // Option A - styled
            HBox optionABox = new HBox(10);
            optionABox.setAlignment(Pos.CENTER_LEFT);
            
            Label optionALabel = new Label("A:");
            optionALabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            optionALabel.setTextFill(Color.web("#3498db"));
            optionALabel.setMinWidth(30);
            
            optionAField = new TextField();
            optionAField.setPromptText("Option A");
            styleControl(optionAField, "-fx-font-size: 14px;");
            HBox.setHgrow(optionAField, Priority.ALWAYS);
            
            optionABox.getChildren().addAll(optionALabel, optionAField);
            
            // Option B - styled
            HBox optionBBox = new HBox(10);
            optionBBox.setAlignment(Pos.CENTER_LEFT);
            
            Label optionBLabel = new Label("B:");
            optionBLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            optionBLabel.setTextFill(Color.web("#2ecc71"));
            optionBLabel.setMinWidth(30);
            
            optionBField = new TextField();
            optionBField.setPromptText("Option B");
            styleControl(optionBField, "-fx-font-size: 14px;");
            HBox.setHgrow(optionBField, Priority.ALWAYS);
            
            optionBBox.getChildren().addAll(optionBLabel, optionBField);
            
            // Option C - styled
            HBox optionCBox = new HBox(10);
            optionCBox.setAlignment(Pos.CENTER_LEFT);
            
            Label optionCLabel = new Label("C:");
            optionCLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            optionCLabel.setTextFill(Color.web("#f39c12"));
            optionCLabel.setMinWidth(30);
            
            optionCField = new TextField();
            optionCField.setPromptText("Option C (optional)");
            styleControl(optionCField, "-fx-font-size: 14px;");
            HBox.setHgrow(optionCField, Priority.ALWAYS);
            
            optionCBox.getChildren().addAll(optionCLabel, optionCField);
            
            // Option D - styled
            HBox optionDBox = new HBox(10);
            optionDBox.setAlignment(Pos.CENTER_LEFT);
            
            Label optionDLabel = new Label("D:");
            optionDLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            optionDLabel.setTextFill(Color.web("#e74c3c"));
            optionDLabel.setMinWidth(30);
            
            optionDField = new TextField();
            optionDField.setPromptText("Option D (optional)");
            styleControl(optionDField, "-fx-font-size: 14px;");
            HBox.setHgrow(optionDField, Priority.ALWAYS);
            
            optionDBox.getChildren().addAll(optionDLabel, optionDField);
            
            optionsContainer.getChildren().addAll(optionABox, optionBBox, optionCBox, optionDBox);
            
            // Correct option with styled label
            Label correctOptionLabel = createFormLabel("Correct Answer");
            
            HBox correctOptionBox = new HBox(10);
            correctOptionBox.setAlignment(Pos.CENTER_LEFT);
            
            correctOptionField = new ComboBox<>();
            correctOptionField.getItems().addAll("A", "B", "C", "D");
            correctOptionField.setValue("A");
            correctOptionField.setPrefWidth(100);
            styleControl(correctOptionField, "-fx-font-size: 14px;");
            
            // Add colored rectangles to represent answers
            Rectangle optARect = createColoredRectangle("#3498db");
            Rectangle optBRect = createColoredRectangle("#2ecc71");
            Rectangle optCRect = createColoredRectangle("#f39c12");
            Rectangle optDRect = createColoredRectangle("#e74c3c");
            
            correctOptionBox.getChildren().addAll(correctOptionField, 
                                                new Label("A:"), optARect, 
                                                new Label("B:"), optBRect, 
                                                new Label("C:"), optCRect, 
                                                new Label("D:"), optDRect);
            
            getChildren().addAll(
                subjectLabel, subjectField,
                questionLabel, questionField,
                optionsLabel, optionsContainer,
                correctOptionLabel, correctOptionBox
            );
        }
        
        private Label createFormLabel(String text) {
            Label label = new Label(text);
            label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            label.setTextFill(Color.web("#2c3e50"));
            label.setPadding(new Insets(5, 0, 5, 0));
            return label;
        }
        
        private void styleControl(Control control, String additionalStyle) {
            control.setStyle("-fx-background-color: white; -fx-border-color: #dcdcdc; " +
                           "-fx-border-radius: 5; " + additionalStyle);
        }
        
        private Rectangle createColoredRectangle(String colorHex) {
            Rectangle rect = new Rectangle(20, 20);
            rect.setFill(Color.web(colorHex));
            rect.setArcWidth(5);
            rect.setArcHeight(5);
            return rect;
        }

        // Validate form data
        public boolean isValid() {
            return subjectField.getValue() != null && !subjectField.getValue().trim().isEmpty() &&
                   !questionField.getText().trim().isEmpty() &&
                   !optionAField.getText().trim().isEmpty() &&
                   !optionBField.getText().trim().isEmpty() &&
                   correctOptionField.getValue() != null;
        }

        // Create a QuizQuestion from form data
        public QuizQuestion createQuizQuestion() {
            return new QuizQuestion(
                0, // ID will be set by the database
                subjectField.getValue().trim(),
                questionField.getText().trim(),
                optionAField.getText().trim(),
                optionBField.getText().trim(),
                optionCField.getText().trim(),
                optionDField.getText().trim(),
                correctOptionField.getValue()
            );
        }

        // Clear form fields
        public void clear() {
            questionField.clear();
            optionAField.clear();
            optionBField.clear();
            optionCField.clear();
            optionDField.clear();
            correctOptionField.setValue("A");
        }
    }

    // Main UI components
    private TabPane tabPane;
    private TableView<QuizQuestion> questionsTable;
    private TableView<QuizResult> resultsTable;
    private ComboBox<String> subjectFilterComboBox;
    private ObservableList<QuizQuestion> allQuestions;
    private ObservableList<QuizResult> allResults;
    private ObservableList<QuizStatistics> subjectStatistics;
    private Label totalQuestionsLabel;
    private Label totalResultsLabel;

    // Database access
    private Connection dbConnection;

    // Create and configure the main view
    public Node getView() {
        try {
            // Establish database connection
            dbConnection = DatabaseConnection.getConnection();
            System.out.println("Database connection established for QuizzesPage");

            // Load initial data
            loadAllQuizData();

            // Create main container with background
            BorderPane mainContainer = new BorderPane();
            mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f7fa, #e9edf2);");
            
            // Create header
            HBox header = createHeader("Quiz Management Dashboard");
            mainContainer.setTop(header);

            // Create main TabPane with styling
            tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            tabPane.setStyle("-fx-tab-min-width: 150px; -fx-tab-max-width: 150px; -fx-tab-min-height: 40px;");
            
            // Create and add tabs
            Tab overviewTab = createOverviewTab();
            Tab createTab = createQuizCreationTab();
            tabPane.getTabs().addAll(overviewTab, createTab);
            
            // Style the tabs
            for (Tab tab : tabPane.getTabs()) {
                // Add some padding and make it look better
                tab.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            
            // Add TabPane to the center with padding
            BorderPane.setMargin(tabPane, new Insets(0, 20, 20, 20));
            mainContainer.setCenter(tabPane);

            return mainContainer;
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message with better styling
            VBox errorContainer = new VBox(20);
            errorContainer.setAlignment(Pos.CENTER);
            errorContainer.setPadding(new Insets(40));
            errorContainer.setStyle("-fx-background-color: #fff5f5;");
            
            Label errorTitle = new Label("Database Connection Error");
            errorTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            errorTitle.setTextFill(Color.web("#e74c3c"));
            
            Label errorLabel = new Label(e.getMessage());
            errorLabel.setFont(Font.font("Arial", 16));
            errorLabel.setTextFill(Color.web("#c0392b"));
            errorLabel.setWrapText(true);
            errorLabel.setTextAlignment(TextAlignment.CENTER);
            
            errorContainer.getChildren().addAll(errorTitle, errorLabel);
            
            return errorContainer;
        }
    }
    
    // Create a header with the given title
    private HBox createHeader(String title) {
        HBox header = new HBox();
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #3498db;");
        
        Label headerLabel = new Label(title);
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.WHITE);
        
        header.getChildren().add(headerLabel);
        return header;
    }

    // Load all quiz questions and results from the database
    private void loadAllQuizData() throws SQLException {
        // Load unique subjects for filtering
        Set<String> subjects = new HashSet<>();
        subjects.add("All Subjects");

        // Load quiz questions
        allQuestions = FXCollections.observableArrayList();
        String questionQuery = "SELECT * FROM quiz_questions ORDER BY subject, id";
        try (PreparedStatement stmt = dbConnection.prepareStatement(questionQuery);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                QuizQuestion question = new QuizQuestion(
                    rs.getInt("id"),
                    rs.getString("subject"),
                    rs.getString("question"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("correct_option")
                );
                allQuestions.add(question);
                subjects.add(rs.getString("subject"));
            }
        }

        // Load quiz results
        allResults = FXCollections.observableArrayList();
        String resultQuery =
            "SELECT qr.resultID, qr.questionID, qr.studentID, u.username AS studentName, " +
            "qr.selectedOption, qr.isCorrect, qr.submissionDate, qq.question " +
            "FROM QuizResults qr " +
            "JOIN quiz_questions qq ON qr.questionID = qq.id " +
            "JOIN Students s ON qr.studentID = s.studentID " +
            "JOIN Users u ON s.userID = u.userID " +
            "ORDER BY qr.submissionDate DESC";
        try (PreparedStatement stmt = dbConnection.prepareStatement(resultQuery);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Timestamp submissionTime = rs.getTimestamp("submissionDate");
                String formattedDate = submissionTime != null ?
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(submissionTime) : "N/A";
                QuizResult result = new QuizResult(
                    rs.getInt("resultID"),
                    rs.getInt("questionID"),
                    rs.getInt("studentID"),
                    rs.getString("studentName"),
                    rs.getString("selectedOption"),
                    rs.getBoolean("isCorrect"),
                    formattedDate,
                    rs.getString("question")
                );
                allResults.add(result);
            }
        }

        // Load statistics by subject
        subjectStatistics = FXCollections.observableArrayList();
        for (String subject : subjects) {
            if (!"All Subjects".equals(subject)) {
                int totalQuestions = 0;
                int totalAttempts = 0;
                int totalCorrect = 0;
                
                // Count questions for this subject
                for (QuizQuestion q : allQuestions) {
                    if (subject.equals(q.getSubject())) {
                        totalQuestions++;
                    }
                }
                
                // Count attempts and correct answers
                for (QuizResult r : allResults) {
                    QuizQuestion question = findQuestionById(r.getQuestionId());
                    if (question != null && subject.equals(question.getSubject())) {
                        totalAttempts++;
                        if (r.isCorrect()) {
                            totalCorrect++;
                        }
                    }
                }
                
                double averageScore = totalAttempts > 0 ? (double) totalCorrect / totalAttempts * 100.0 : 0.0;
                subjectStatistics.add(new QuizStatistics(
                    subject, totalQuestions, totalAttempts, totalCorrect, averageScore
                ));
            }
        }

        // Add "All Subjects" filter option if needed
        if (subjectFilterComboBox != null) {
            subjectFilterComboBox.getItems().setAll(subjects);
            subjectFilterComboBox.setValue("All Subjects");
        }

        // Update summary labels
        if (totalQuestionsLabel != null) {
            totalQuestionsLabel.setText("Total Questions: " + allQuestions.size());
        }
        if (totalResultsLabel != null) {
            totalResultsLabel.setText("Total Results: " + allResults.size());
        }
    }

    // Create the Quiz Overview tab
    private Tab createOverviewTab() {
        Tab tab = new Tab("Quiz Overview");
        
        // Create scroll pane to handle content overflow
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        // Main layout within scroll pane
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        
        // Top section with filter and summary - card style
        VBox filterCard = new VBox(15);
        filterCard.setPadding(new Insets(20));
        filterCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        // Filter section with color styling
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter by Subject:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        filterLabel.setTextFill(Color.web("#2c3e50"));
        
        subjectFilterComboBox = new ComboBox<>();
        subjectFilterComboBox.getStyleClass().add("filter-combo");
        subjectFilterComboBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; " +
                                     "-fx-border-radius: 5; -fx-font-size: 14px;");
        subjectFilterComboBox.setPrefWidth(200);
        
        // Get unique subjects from database
        Set<String> subjects = new HashSet<>();
        subjects.add("All Subjects");
        for (QuizQuestion q : allQuestions) {
            subjects.add(q.getSubject());
        }
        subjectFilterComboBox.getItems().addAll(subjects);
        subjectFilterComboBox.setValue("All Subjects");
        
        // Filter by subject
        subjectFilterComboBox.setOnAction(e -> {
            String selectedSubject = subjectFilterComboBox.getValue();
            filterQuestionsBySubject(selectedSubject);
            filterResultsBySubject(selectedSubject);
        });
        
        // Summary labels with enhanced styling
        HBox summaryBox = new HBox(30);
        summaryBox.setAlignment(Pos.CENTER_RIGHT);
        
        totalQuestionsLabel = createSummaryLabel("Total Questions: " + allQuestions.size(), "#3498db");
        totalResultsLabel = createSummaryLabel("Total Results: " + allResults.size(), "#2ecc71");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        filterBox.getChildren().addAll(filterLabel, subjectFilterComboBox, spacer);
        summaryBox.getChildren().addAll(totalQuestionsLabel, totalResultsLabel);
        
        filterCard.getChildren().addAll(filterBox, new Separator(), summaryBox);
        
        // Statistics panel - card style
        TitledPane statsPane = createStatisticsPane();
        statsPane.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                         "-fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Center section with TabPane for Questions and Results
        TabPane contentTabs = new TabPane();
        contentTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        contentTabs.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        // Questions tab
        Tab questionsTab = createStyledTab("Quiz Questions", "#3498db");
        questionsTable = createQuestionsTable();
        
        // Wrap the table in a VBox with header
        VBox questionContent = new VBox(15);
        questionContent.setPadding(new Insets(15));
        questionContent.setStyle("-fx-background-color: white;");
        
        Label questionsHeader = new Label("All Quiz Questions");
        questionsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        questionsHeader.setTextFill(Color.web("#3498db"));
        
        // Add search field
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_RIGHT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search questions...");
        searchField.setStyle("-fx-background-color: white; -fx-border-color: #ddd; " +
                          "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 5 15;");
        searchField.setPrefWidth(250);
        
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                           "-fx-background-radius: 20; -fx-font-weight: bold;");
        
        searchBox.getChildren().addAll(searchField, searchButton);
        
        // Add header, search box, and table to container
        questionContent.getChildren().addAll(questionsHeader, searchBox, questionsTable);
        VBox.setVgrow(questionsTable, Priority.ALWAYS);
        
        questionsTab.setContent(questionContent);
        
        // Results tab
        Tab resultsTab = createStyledTab("Quiz Results", "#2ecc71");
        resultsTable = createResultsTable();
        
        // Wrap the table in a VBox with header
        VBox resultsContent = new VBox(15);
        resultsContent.setPadding(new Insets(15));
        resultsContent.setStyle("-fx-background-color: white;");
        
        Label resultsHeader = new Label("Student Quiz Results");
        resultsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        resultsHeader.setTextFill(Color.web("#2ecc71"));
        
        // Add table to container
        resultsContent.getChildren().addAll(resultsHeader, resultsTable);
        VBox.setVgrow(resultsTable, Priority.ALWAYS);
        
        resultsTab.setContent(resultsContent);
        
        contentTabs.getTabs().addAll(questionsTab, resultsTab);
        
        // Add all components to main layout
        layout.getChildren().addAll(filterCard, statsPane, contentTabs);
        VBox.setVgrow(contentTabs, Priority.ALWAYS);
        
        scrollPane.setContent(layout);
        tab.setContent(scrollPane);
        
        return tab;
    }
    
    // Create a styled tab
    private Tab createStyledTab(String text, String color) {
        Tab tab = new Tab(text);
        tab.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        return tab;
    }
    
    // Create a styled summary label
    private Label createSummaryLabel(String text, String color) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        label.setTextFill(Color.web(color));
        
        // Add shaped background
        StackPane container = new StackPane();
        Rectangle bg = new Rectangle(200, 40);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(Color.web(color + "15"));  // Light version of the color
        container.getChildren().addAll(bg, label);
        
        return label;
    }

    // Create the Quiz Creation tab
    private Tab createQuizCreationTab() {
        Tab tab = new Tab("Create Quiz");
        
        // Scroll pane for content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        // Main container with padding
        VBox content = new VBox(30);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);
        
        // Title section with better styling
        VBox titleBox = new VBox(10);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 20, 0));
        titleBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                        "-fx-padding: 20;");
        
        Label titleLabel = new Label("Create New Quiz Questions");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#3498db"));
        
        Label instructionsLabel = new Label("Fill in the details for up to 5 quiz questions. " +
                                         "At least the first form must be completed.");
        instructionsLabel.setFont(Font.font("Arial", 16));
        instructionsLabel.setTextFill(Color.web("#7f8c8d"));
        instructionsLabel.setWrapText(true);
        instructionsLabel.setTextAlignment(TextAlignment.CENTER);
        
        titleBox.getChildren().addAll(titleLabel, instructionsLabel);
        
        // Get existing subjects
        Set<String> subjects = new HashSet<>();
        for (QuizQuestion q : allQuestions) {
            subjects.add(q.getSubject());
        }
        
        // Create forms container with nice styling
        VBox formsContainer = new VBox(20);
        formsContainer.setPadding(new Insets(10));
        
        // Forms accordion - more interactive and space-saving
        Accordion formsAccordion = new Accordion();
        
        // Add 5 quiz forms
        List<QuizForm> quizForms = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            QuizForm form = new QuizForm(subjects);
            
            // Create a titled pane with nice styling
            TitledPane formPane = new TitledPane();
            formPane.setText("Question " + (i + 1));
            formPane.setContent(form);
            formPane.setAnimated(true);
            formPane.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            formsAccordion.getPanes().add(formPane);
            quizForms.add(form);
            
            // First form is expanded by default
            if (i == 0) {
                formsAccordion.setExpandedPane(formPane);
            }
        }
        
        formsContainer.getChildren().add(formsAccordion);
        
        // Buttons with better styling and icons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button clearButton = new Button("Clear All");
        clearButton.setStyle(
            "-fx-background-color: #e74c3c; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12 25; " +
            "-fx-background-radius: 30;"
        );
        
        Button saveButton = new Button("Save Quiz Questions");
        saveButton.setStyle(
            "-fx-background-color: #2ecc71; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12 25; " +
            "-fx-background-radius: 30;"
        );
        
        buttonBox.getChildren().addAll(clearButton, saveButton);
        
        // Add help text
        Label helpText = new Label("* Required fields must be completed");
        helpText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        helpText.setTextFill(Color.web("#7f8c8d"));
        helpText.setPadding(new Insets(5, 0, 0, 0));
        
        // Add confirmation message container (initially hidden)
        StackPane confirmationContainer = new StackPane();
        confirmationContainer.setVisible(false);
        confirmationContainer.setStyle(
            "-fx-background-color: #d4edda; " +
            "-fx-border-color: #c3e6cb; " +
            "-fx-border-radius: 5; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 5;"
        );
        
        Label confirmationLabel = new Label("Quiz questions saved successfully!");
        confirmationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        confirmationLabel.setTextFill(Color.web("#155724"));
        
        confirmationContainer.getChildren().add(confirmationLabel);
        
        // Button actions
        clearButton.setOnAction(e -> {
            for (QuizForm form : quizForms) {
                form.clear();
            }
            // Hide confirmation if showing
            confirmationContainer.setVisible(false);
        });
        
        saveButton.setOnAction(e -> {
            // Check that at least first form is valid
            if (!quizForms.get(0).isValid()) {
                showAlert(Alert.AlertType.ERROR,
                       "Validation Error",
                       "Please complete at least the first quiz form with all required fields.");
                return;
            }
            
            // Collect valid quiz questions
            List<QuizQuestion> newQuestions = new ArrayList<>();
            for (QuizForm form : quizForms) {
                if (form.isValid()) {
                    newQuestions.add(form.createQuizQuestion());
                }
            }
            
            // Save questions to database
            boolean success = saveQuizQuestions(newQuestions);
            if (success) {
                // Show confirmation
                confirmationContainer.setVisible(true);
                
                // Clear forms after successful save
                for (QuizForm form : quizForms) {
                    form.clear();
                }
                
                // Reload data to update tables
                try {
                    loadAllQuizData();
                    filterQuestionsBySubject(subjectFilterComboBox.getValue());
                    filterResultsBySubject(subjectFilterComboBox.getValue());
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR,
                           "Database Error",
                           "Error reloading quiz data: " + ex.getMessage());
                }
            }
        });
        
        // Main content structure
        VBox mainContentBox = new VBox(30);
        mainContentBox.setPadding(new Insets(0));
        mainContentBox.getChildren().addAll(
            titleBox,
            formsContainer,
            confirmationContainer,
            buttonBox,
            helpText
        );
        
        content.getChildren().add(mainContentBox);
        scrollPane.setContent(content);
        tab.setContent(scrollPane);
        
        return tab;
    }

    // Create a table for quiz questions with improved styling
    private TableView<QuizQuestion> createQuestionsTable() {
        TableView<QuizQuestion> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-table-cell-border-color: #eaecee; " +
            "-fx-table-header-border-color: #eaecee; " +
            "-fx-border-color: #eaecee;"
        );
        
        // ID column
        TableColumn<QuizQuestion, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(50);
        idColumn.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        
        // Subject column
        TableColumn<QuizQuestion, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        subjectColumn.setPrefWidth(150);
        
        // Question column
        TableColumn<QuizQuestion, String> questionColumn = new TableColumn<>("Question");
        questionColumn.setCellValueFactory(new PropertyValueFactory<>("question"));
        questionColumn.setPrefWidth(300);
        
        // Style the cells to show wrapping text
        questionColumn.setCellFactory(tc -> {
            TableCell<QuizQuestion, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(questionColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
        
        // Options columns group
        TableColumn<QuizQuestion, String> optionsGroup = new TableColumn<>("Options");
        optionsGroup.setStyle("-fx-alignment: CENTER;");
        
        // Option A with colored cell
        TableColumn<QuizQuestion, String> optionAColumn = new TableColumn<>("A");
        optionAColumn.setCellValueFactory(new PropertyValueFactory<>("optionA"));
        optionAColumn.setPrefWidth(120);
        optionAColumn.setCellFactory(col -> createColoredOptionCell("#3498db"));
        
        // Option B with colored cell
        TableColumn<QuizQuestion, String> optionBColumn = new TableColumn<>("B");
        optionBColumn.setCellValueFactory(new PropertyValueFactory<>("optionB"));
        optionBColumn.setPrefWidth(120);
        optionBColumn.setCellFactory(col -> createColoredOptionCell("#2ecc71"));
        
        // Option C with colored cell
        TableColumn<QuizQuestion, String> optionCColumn = new TableColumn<>("C");
        optionCColumn.setCellValueFactory(new PropertyValueFactory<>("optionC"));
        optionCColumn.setPrefWidth(120);
        optionCColumn.setCellFactory(col -> createColoredOptionCell("#f39c12"));
        
        // Option D with colored cell
        TableColumn<QuizQuestion, String> optionDColumn = new TableColumn<>("D");
        optionDColumn.setCellValueFactory(new PropertyValueFactory<>("optionD"));
        optionDColumn.setPrefWidth(120);
        optionDColumn.setCellFactory(col -> createColoredOptionCell("#e74c3c"));
        
        optionsGroup.getColumns().addAll(optionAColumn, optionBColumn, optionCColumn, optionDColumn);
        
        // Correct option column with highlighting
        TableColumn<QuizQuestion, String> correctColumn = new TableColumn<>("Correct");
        correctColumn.setCellValueFactory(new PropertyValueFactory<>("correctOption"));
        correctColumn.setPrefWidth(80);
        correctColumn.setStyle("-fx-alignment: CENTER;");
        
        // Style the correct answer cell
        correctColumn.setCellFactory(column -> new TableCell<QuizQuestion, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    
                    // Different background color based on the correct option
                    if ("A".equals(item)) {
                        setStyle("-fx-background-color: #3498db; -fx-alignment: center;");
                    } else if ("B".equals(item)) {
                        setStyle("-fx-background-color: #2ecc71; -fx-alignment: center;");
                    } else if ("C".equals(item)) {
                        setStyle("-fx-background-color: #f39c12; -fx-alignment: center;");
                    } else if ("D".equals(item)) {
                        setStyle("-fx-background-color: #e74c3c; -fx-alignment: center;");
                    }
                }
            }
        });
        
        // Add columns to table
        table.getColumns().addAll(idColumn, subjectColumn, questionColumn, optionsGroup, correctColumn);
        
        // Set data
        table.setItems(allQuestions);
        
        // Make it possible to detect row double-click with visual feedback
        table.setRowFactory(tv -> {
            TableRow<QuizQuestion> row = new TableRow<>();
            
            // Add hover effect
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #ecf0f1;");
                }
            });
            
            row.setOnMouseExited(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("");
                }
            });
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    QuizQuestion question = row.getItem();
                    showQuestionResults(question);
                }
            });
            
            return row;
        });
        
        return table;
    }
    
    // Helper method to create colored option cells
    private <T> TableCell<T, String> createColoredOptionCell(String colorHex) {
        return new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(Color.BLACK);
                    setFont(Font.font("Arial", 12));
                    setStyle("-fx-border-width: 0 0 0 4; -fx-border-color: " + colorHex + ";");
                }
            }
        };
    }

    // Create a table for quiz results with improved styling
    private TableView<QuizResult> createResultsTable() {
        TableView<QuizResult> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-table-cell-border-color: #eaecee; " +
            "-fx-table-header-border-color: #eaecee; " +
            "-fx-border-color: #eaecee;"
        );
        
        // Question column with wrapping text
        TableColumn<QuizResult, String> questionColumn = new TableColumn<>("Question");
        questionColumn.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        questionColumn.setPrefWidth(300);
        
        // Style the cells to show wrapping text
        questionColumn.setCellFactory(tc -> {
            TableCell<QuizResult, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(questionColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
        
        // Student column with icon
        TableColumn<QuizResult, String> studentColumn = new TableColumn<>("Student");
        studentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentColumn.setPrefWidth(150);
        
        // Style student cells with icon
        studentColumn.setCellFactory(column -> new TableCell<QuizResult, String>() {
            private final HBox container = new HBox(5);
            private final Region icon = new Region();
            private final Label nameLabel = new Label();
            
            {
                icon.setPrefSize(16, 16);
                icon.setStyle("-fx-background-color: #7f8c8d;");
                icon.setShape(new javafx.scene.shape.Circle(8, 8, 8));
                container.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().addAll(icon, nameLabel);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    nameLabel.setText(item);
                    setGraphic(container);
                }
            }
        });
        
        // Selected option column with colored background
        TableColumn<QuizResult, String> selectedColumn = new TableColumn<>("Selected");
        selectedColumn.setCellValueFactory(new PropertyValueFactory<>("selectedOption"));
        selectedColumn.setPrefWidth(80);
        selectedColumn.setStyle("-fx-alignment: CENTER;");
        
        // Style selected option with colors matching the options
        selectedColumn.setCellFactory(column -> new TableCell<QuizResult, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    
                    // Color based on selected option
                    if ("A".equals(item)) {
                        setStyle("-fx-background-color: #3498db; -fx-alignment: center;");
                    } else if ("B".equals(item)) {
                        setStyle("-fx-background-color: #2ecc71; -fx-alignment: center;");
                    } else if ("C".equals(item)) {
                        setStyle("-fx-background-color: #f39c12; -fx-alignment: center;");
                    } else if ("D".equals(item)) {
                        setStyle("-fx-background-color: #e74c3c; -fx-alignment: center;");
                    }
                }
            }
        });
        
        // Correct column with check/X mark
        TableColumn<QuizResult, Boolean> correctColumn = new TableColumn<>("Correct");
        correctColumn.setCellValueFactory(new PropertyValueFactory<>("correct"));
        correctColumn.setPrefWidth(80);
        correctColumn.setStyle("-fx-alignment: CENTER;");
        
        // Style the correct column with checkmarks and X marks
        correctColumn.setCellFactory(column -> new TableCell<QuizResult, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    if (item) {
                        setGraphic(createIcon("✓", "#2ecc71"));
                        setStyle("-fx-background-color: #d4edda;");
                    } else {
                        setGraphic(createIcon("✗", "#e74c3c")); 
                        setStyle("-fx-background-color: #f8d7da;");
                    }
                }
            }
            
            private StackPane createIcon(String text, String color) {
                StackPane pane = new StackPane();
                Label label = new Label(text);
                label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                label.setTextFill(Color.web(color));
                pane.getChildren().add(label);
                return pane;
            }
        });
        
        // Date column with formatted date
        TableColumn<QuizResult, String> dateColumn = new TableColumn<>("Submission Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("submissionDate"));
        dateColumn.setPrefWidth(180);
        
        // Style date cells
        dateColumn.setCellFactory(column -> new TableCell<QuizResult, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setTextFill(Color.web("#34495e"));
                    setFont(Font.font("Arial", 12));
                }
            }
        });
        
        // Add columns to table
        table.getColumns().addAll(questionColumn, studentColumn, selectedColumn, correctColumn, dateColumn);
        
        // Set data
        table.setItems(allResults);
        
        return table;
    }

    // Create statistics pane that shows performance by subject with enhanced styling
    private TitledPane createStatisticsPane() {
        TitledPane pane = new TitledPane();
        pane.setText("Quiz Statistics by Subject");
        pane.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        pane.setTextFill(Color.web("#34495e"));
        pane.setCollapsible(true);
        pane.setExpanded(true);
        pane.setStyle("-fx-background-color: white;");
        
        // Create table for statistics
        TableView<QuizStatistics> statsTable = new TableView<>();
        statsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        statsTable.setStyle(
            "-fx-background-color: white; " +
            "-fx-table-cell-border-color: #eaecee; " +
            "-fx-table-header-border-color: #eaecee;"
        );
        
        // Subject column
        TableColumn<QuizStatistics, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubject()));
        subjectColumn.setPrefWidth(150);
        
        // Highlight subject with different background colors
        subjectColumn.setCellFactory(column -> new TableCell<QuizStatistics, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    
                    // Unique color for each subject based on its hash code
                    int hashCode = Math.abs(item.hashCode());
                    String[] colors = {
                        "#3498db", "#2ecc71", "#9b59b6", "#f1c40f", "#e74c3c", 
                        "#1abc9c", "#d35400", "#34495e", "#16a085", "#27ae60"
                    };
                    
                    String color = colors[hashCode % colors.length];
                    setStyle("-fx-background-color: " + color + "; -fx-padding: 5;");
                }
            }
        });
        
        // Questions count column
        TableColumn<QuizStatistics, Number> questionsColumn = new TableColumn<>("Total Questions");
        questionsColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTotalQuestions()));
        questionsColumn.setPrefWidth(120);
        questionsColumn.setStyle("-fx-alignment: CENTER;");
        
        // Total attempts column
        TableColumn<QuizStatistics, Number> attemptsColumn = new TableColumn<>("Total Attempts");
        attemptsColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTotalAttempts()));
        attemptsColumn.setPrefWidth(120);
        attemptsColumn.setStyle("-fx-alignment: CENTER;");
        
        // Correct answers column
        TableColumn<QuizStatistics, Number> correctColumn = new TableColumn<>("Correct Answers");
        correctColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTotalCorrect()));
        correctColumn.setPrefWidth(120);
        correctColumn.setStyle("-fx-alignment: CENTER;");
        
        // Average score column with percentage formatting
        TableColumn<QuizStatistics, String> avgScoreColumn = new TableColumn<>("Average Score");
        avgScoreColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.format("%.1f%%", cellData.getValue().getAverageScore())));
        avgScoreColumn.setPrefWidth(120);
        avgScoreColumn.setStyle("-fx-alignment: CENTER;");
        
        // Style the average score column
        avgScoreColumn.setCellFactory(column -> new TableCell<QuizStatistics, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    // Parse percentage for conditional formatting
                    double percentage;
                    try {
                        percentage = Double.parseDouble(item.replace("%", ""));
                    } catch (NumberFormatException e) {
                        percentage = 0;
                    }
                    
                    if (percentage < 60) {
                        setTextFill(Color.web("#e74c3c")); // Red
                    } else if (percentage < 80) {
                        setTextFill(Color.web("#f39c12")); // Yellow/Orange
                    } else {
                        setTextFill(Color.web("#2ecc71")); // Green
                    }
                    
                    setFont(Font.font("Arial", FontWeight.BOLD, 14));
                }
            }
        });
        
        // Progress column with stylized progress bar
        TableColumn<QuizStatistics, Void> progressColumn = new TableColumn<>("Completion");
        progressColumn.setCellFactory(col -> new TableCell<QuizStatistics, Void>() {
            private final ProgressBar progressBar = new ProgressBar();
            private final Label percentLabel = new Label();
            private final HBox container = new HBox(10);
            
            {
                progressBar.setPrefWidth(150);
                percentLabel.setTextFill(Color.web("#34495e"));
                percentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                container.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().addAll(progressBar, percentLabel);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    QuizStatistics stats = getTableView().getItems().get(getIndex());
                    double progress = stats.getAverageScore() / 100.0;
                    progressBar.setProgress(progress);
                    percentLabel.setText(String.format("%.1f%%", stats.getAverageScore()));
                    
                    // Set color based on score
                    if (progress < 0.6) {
                        progressBar.setStyle("-fx-accent: #e74c3c;"); // Red
                    } else if (progress < 0.8) {
                        progressBar.setStyle("-fx-accent: #f39c12;"); // Yellow/Orange
                    } else {
                        progressBar.setStyle("-fx-accent: #2ecc71;"); // Green
                    }
                    
                    setGraphic(container);
                }
            }
        });
        
        // Add columns to table
        statsTable.getColumns().addAll(
            subjectColumn, questionsColumn, attemptsColumn,
            correctColumn, avgScoreColumn, progressColumn
        );
        
        // Set data
        statsTable.setItems(subjectStatistics);
        
        pane.setContent(statsTable);
        return pane;
    }

    // Filter questions by subject
    private void filterQuestionsBySubject(String subject) {
        if ("All Subjects".equals(subject)) {
            questionsTable.setItems(allQuestions);
        } else {
            ObservableList<QuizQuestion> filtered = allQuestions.filtered(
                q -> subject.equals(q.getSubject())
            );
            questionsTable.setItems(filtered);
        }
        totalQuestionsLabel.setText("Total Questions: " + questionsTable.getItems().size());
    }

    // Filter results by subject
    private void filterResultsBySubject(String subject) {
        if ("All Subjects".equals(subject)) {
            resultsTable.setItems(allResults);
        } else {
            ObservableList<QuizResult> filtered = allResults.filtered(
                r -> {
                    QuizQuestion question = findQuestionById(r.getQuestionId());
                    return question != null && subject.equals(question.getSubject());
                }
            );
            resultsTable.setItems(filtered);
        }
        totalResultsLabel.setText("Total Results: " + resultsTable.getItems().size());
    }

    // Find a question by ID
    private QuizQuestion findQuestionById(int id) {
        for (QuizQuestion q : allQuestions) {
            if (q.getId() == id) {
                return q;
            }
        }
        return null;
    }

    // Save quiz questions to database
    private boolean saveQuizQuestions(List<QuizQuestion> questions) {
        if (questions.isEmpty()) {
            return false;
        }
        
        try {
            // Prepare statement for inserting questions
            String insertQuery =
                "INSERT INTO quiz_questions (subject, question, option_a, option_b, option_c, option_d, correct_option) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = dbConnection.prepareStatement(insertQuery)) {
                // Start transaction
                dbConnection.setAutoCommit(false);
                
                for (QuizQuestion q : questions) {
                    stmt.setString(1, q.getSubject());
                    stmt.setString(2, q.getQuestion());
                    stmt.setString(3, q.getOptionA());
                    stmt.setString(4, q.getOptionB());
                    
                    // Handle optional fields
                    if (q.getOptionC() == null || q.getOptionC().isEmpty()) {
                        stmt.setNull(5, java.sql.Types.VARCHAR);
                    } else {
                        stmt.setString(5, q.getOptionC());
                    }
                    
                    if (q.getOptionD() == null || q.getOptionD().isEmpty()) {
                        stmt.setNull(6, java.sql.Types.VARCHAR);
                    } else {
                        stmt.setString(6, q.getOptionD());
                    }
                    
                    stmt.setString(7, q.getCorrectOption());
                    stmt.addBatch();
                }
                
                // Execute batch
                int[] results = stmt.executeBatch();
                
                // Commit if all successful
                dbConnection.commit();
                dbConnection.setAutoCommit(true);
                
                // Return true if all inserts succeeded
                for (int result : results) {
                    if (result <= 0) {
                        return false;
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            try {
                // Rollback on error
                dbConnection.rollback();
                dbConnection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            showAlert(Alert.AlertType.ERROR,
                   "Database Error",
                   "Error saving quiz questions: " + e.getMessage());
            return false;
        }
    }

    // Show detailed results for a specific question with improved UI
    private void showQuestionResults(QuizQuestion question) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Results for Question #" + question.getId());
        dialog.setHeaderText(null); // We'll create a custom header
        
        // Create a more visually appealing dialog pane
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setPrefWidth(700);
        dialogPane.setPrefHeight(600);
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setStyle("-fx-background-color: white;");
        
        // Create filtered list of results for this question
        ObservableList<QuizResult> questionResults = FXCollections.observableArrayList();
        for (QuizResult result : allResults) {
            if (result.getQuestionId() == question.getId()) {
                questionResults.add(result);
            }
        }
        
        // Create content with styling
        VBox content = new VBox(20);
        content.setPadding(new Insets(0, 20, 20, 20));
        
        // Custom header with question info
        VBox headerBox = new VBox(10);
        headerBox.setStyle("-fx-background-color: #3498db; -fx-padding: 20;");
        
        Label questionIdLabel = new Label("Question #" + question.getId() + " - " + question.getSubject());
        questionIdLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        questionIdLabel.setTextFill(Color.WHITE);
        
        Label questionTextLabel = new Label(question.getQuestion());
        questionTextLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        questionTextLabel.setTextFill(Color.WHITE);
        questionTextLabel.setWrapText(true);
        
        headerBox.getChildren().addAll(questionIdLabel, questionTextLabel);
        
        // Add question details in a grid with colored options
        GridPane questionDetails = new GridPane();
        questionDetails.setVgap(15);
        questionDetails.setHgap(15);
        questionDetails.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 5;");
        
        // Create answer option boxes with styling
        HBox optionABox = createOptionBox("A", question.getOptionA(), "#3498db", 
                                          question.getCorrectOption().equals("A"));
        HBox optionBBox = createOptionBox("B", question.getOptionB(), "#2ecc71", 
                                          question.getCorrectOption().equals("B"));
        HBox optionCBox = createOptionBox("C", question.getOptionC(), "#f39c12", 
                                          question.getCorrectOption().equals("C"));
        HBox optionDBox = createOptionBox("D", question.getOptionD(), "#e74c3c", 
                                          question.getCorrectOption().equals("D"));
        
        // Add options to grid, 2 per row
        questionDetails.add(optionABox, 0, 0);
        questionDetails.add(optionBBox, 1, 0);
        questionDetails.add(optionCBox, 0, 1);
        questionDetails.add(optionDBox, 1, 1);
        
        // Make columns equal width
        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        column1.setPercentWidth(50);
        column2.setPercentWidth(50);
        questionDetails.getColumnConstraints().addAll(column1, column2);
        
        // Results statistics card
        VBox statsBox = new VBox(10);
        statsBox.setStyle("-fx-background-color: white; -fx-padding: 15; " +
                        "-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        // Calculate statistics
        int totalAnswers = questionResults.size();
        int correctAnswers = 0;
        for (QuizResult result : questionResults) {
            if (result.isCorrect()) {
                correctAnswers++;
            }
        }
        double correctPercentage = totalAnswers > 0 ?
            (double) correctAnswers / totalAnswers * 100 : 0;
        
        // Create statistics labels
        Label summaryTitle = new Label("Performance Summary");
        summaryTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        summaryTitle.setTextFill(Color.web("#34495e"));
        
        // Statistics with progress bars
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(10);
        
        statsGrid.add(new Label("Total Responses:"), 0, 0);
        statsGrid.add(new Label(Integer.toString(totalAnswers)), 1, 0);
        
        statsGrid.add(new Label("Correct Answers:"), 0, 1);
        Label correctLabel = new Label(Integer.toString(correctAnswers));
        correctLabel.setTextFill(Color.web("#2ecc71"));
        correctLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statsGrid.add(correctLabel, 1, 1);
        
        statsGrid.add(new Label("Success Rate:"), 0, 2);
        
        // Add progress bar for success rate
        HBox rateBox = new HBox(10);
        ProgressBar rateBar = new ProgressBar(correctPercentage / 100);
        rateBar.setPrefWidth(150);
        
        // Color based on percentage
        if (correctPercentage < 60) {
            rateBar.setStyle("-fx-accent: #e74c3c;"); // Red
        } else if (correctPercentage < 80) {
            rateBar.setStyle("-fx-accent: #f39c12;"); // Orange
        } else {
            rateBar.setStyle("-fx-accent: #2ecc71;"); // Green
        }
        
        Label percentLabel = new Label(String.format("%.1f%%", correctPercentage));
        percentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rateBox.getChildren().addAll(rateBar, percentLabel);
        statsGrid.add(rateBox, 1, 2);
        
        statsBox.getChildren().addAll(summaryTitle, new Separator(), statsGrid);
        
        // Create results table with styling
        TableView<QuizResult> resultsTable = new TableView<>(questionResults);
        resultsTable.setPlaceholder(new Label("No results available for this question"));
        
        // Student column
        TableColumn<QuizResult, String> studentColumn = new TableColumn<>("Student");
        studentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentColumn.setPrefWidth(150);
        
        // Selected option column with colors
        TableColumn<QuizResult, String> selectedColumn = new TableColumn<>("Selected");
        selectedColumn.setCellValueFactory(new PropertyValueFactory<>("selectedOption"));
        selectedColumn.setPrefWidth(80);
        
        // Style the selected option cells with colors
        selectedColumn.setCellFactory(column -> new TableCell<QuizResult, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    setAlignment(Pos.CENTER);
                    
                    // Color based on selected option
                    if ("A".equals(item)) {
                        setStyle("-fx-background-color: #3498db;");
                    } else if ("B".equals(item)) {
                        setStyle("-fx-background-color: #2ecc71;");
                    } else if ("C".equals(item)) {
                        setStyle("-fx-background-color: #f39c12;");
                    } else if ("D".equals(item)) {
                        setStyle("-fx-background-color: #e74c3c;");
                    }
                }
            }
        });
        
        // Correct column with check/X icons
        TableColumn<QuizResult, Boolean> correctColumn = new TableColumn<>("Correct");
        correctColumn.setCellValueFactory(new PropertyValueFactory<>("correct"));
        correctColumn.setPrefWidth(80);
        
        // Style the correct column with icons
        correctColumn.setCellFactory(column -> new TableCell<QuizResult, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    if (item) {
                        setText("✓");
                        setTextFill(Color.web("#2ecc71"));
                        setStyle("-fx-background-color: #d4edda;");
                    } else {
                        setText("✗");
                        setTextFill(Color.web("#e74c3c"));
                        setStyle("-fx-background-color: #f8d7da;");
                    }
                    
                    setAlignment(Pos.CENTER);
                    setFont(Font.font("Arial", FontWeight.BOLD, 16));
                }
            }
        });
        
        // Date column with formatting
        TableColumn<QuizResult, String> dateColumn = new TableColumn<>("Submission Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("submissionDate"));
        dateColumn.setPrefWidth(180);
        
        // Add columns to table
        resultsTable.getColumns().addAll(studentColumn, selectedColumn, correctColumn, dateColumn);
        VBox.setVgrow(resultsTable, Priority.ALWAYS);
        
        // Add all components to content
        content.getChildren().addAll(
            headerBox,
            questionDetails,
            statsBox,
            new Label("Student Responses"),
            resultsTable
        );
        
        // Set dialog content
        dialogPane.setContent(content);
        
        // Add close button
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        closeButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 15; " +
            "-fx-background-radius: 5;"
        );
        
        dialog.showAndWait();
    }
    
    // Helper method to create an option box for question details
    private HBox createOptionBox(String optionLetter, String optionText, String color, boolean isCorrect) {
        HBox box = new HBox(15);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: " + color + "; -fx-border-width: 1; " +
                   "-fx-border-radius: 5; -fx-background-radius: 5; " +
                   (isCorrect ? "-fx-background-color: " + color + "15;" : ""));
        
        // Option letter label
        Label letterLabel = new Label(optionLetter);
        letterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        letterLabel.setTextFill(Color.WHITE);
        letterLabel.setAlignment(Pos.CENTER);
        letterLabel.setMinWidth(30);
        letterLabel.setMinHeight(30);
        letterLabel.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15;");
        
        // Option text with truncation if needed
        Label textLabel = new Label(optionText != null ? optionText : "-");
        textLabel.setWrapText(true);
        textLabel.setFont(Font.font("Arial", 14));
        textLabel.setTextFill(Color.web("#34495e"));
        HBox.setHgrow(textLabel, Priority.ALWAYS);
        
        // Add correct answer indicator if this is the correct option
        if (isCorrect) {
            Label correctLabel = new Label("✓");
            correctLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            correctLabel.setTextFill(Color.web("#2ecc71"));
            box.getChildren().addAll(letterLabel, textLabel, correctLabel);
        } else {
            box.getChildren().addAll(letterLabel, textLabel);
        }
        
        return box;
    }

    // Show styled alert dialog
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        
        // Set background color based on alert type
        String bgColor = "#ffffff";
        String textColor = "#333333";
        
        if (type == Alert.AlertType.ERROR) {
            bgColor = "#fff5f5";
            textColor = "#e74c3c";
        } else if (type == Alert.AlertType.WARNING) {
            bgColor = "#fff9e6";
            textColor = "#f39c12";
        } else if (type == Alert.AlertType.INFORMATION) {
            bgColor = "#ebf8ff";
            textColor = "#3498db";
        } else if (type == Alert.AlertType.CONFIRMATION) {
            bgColor = "#e6fffa";
            textColor = "#1abc9c";
        }
        
        dialogPane.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 1px;"
        );
        
        // Find and style any labels to ensure text visibility
        for (Node node : dialogPane.lookupAll(".label")) {
            if (node instanceof Label) {
                ((Label) node).setTextFill(Color.web(textColor));
                ((Label) node).setFont(Font.font("Arial", 14));
            }
        }
        
        // Style the buttons
        for (ButtonType buttonType : alert.getDialogPane().getButtonTypes()) {
            Button button = (Button) alert.getDialogPane().lookupButton(buttonType);
            
            if (buttonType == ButtonType.OK || buttonType == ButtonType.YES) {
                button.setStyle(
                    "-fx-background-color: #3498db; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 5;"
                );
            } else if (buttonType == ButtonType.CANCEL || buttonType == ButtonType.NO) {
                button.setStyle(
                    "-fx-background-color: #e74c3c; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 5;"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: #95a5a6; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 5;"
                );
            }
        }
        
        alert.showAndWait();
    }
}