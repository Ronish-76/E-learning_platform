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
 * - Creating new quiz questions
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

    private static class QuizForm extends VBox {
        private final TextArea questionField;
        private final TextField optionAField;
        private final TextField optionBField;
        private final TextField optionCField;
        private final TextField optionDField;
        private final TextField correctOptionField; // Changed from ComboBox to TextField
        private ComboBox<String> subjectField;  // Made non-final so it can be set externally

        public QuizForm(Set<String> subjects) {
            super(15);
            setPadding(new Insets(20));
            setStyle("-fx-background-color: #446687; -fx-border-color: #446687; " +
                    "-fx-border-radius: 5; -fx-background-radius: 5;");
            // Add drop shadow effect
            setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));

            // Subject field is now managed externally
            subjectField = new ComboBox<>();
            
            // Question field with styled label and description
            Label questionLabel = createFormLabel("Question");
            Label questionDescription = createDescriptionLabel("Enter the full text of your question");
            questionField = new TextArea();
            questionField.setPrefRowCount(3);
            questionField.setWrapText(true);
            styleControl(questionField, "-fx-font-size: 14px;");

            // Options section with a colored container
            Label optionsLabel = createFormLabel("Options");
            Label optionsDescription = createDescriptionLabel("Add answer options for students to choose from");
            VBox optionsContainer = new VBox(10);
            optionsContainer.setPadding(new Insets(15));
            optionsContainer.setStyle("-fx-background-color: #446687; -fx-background-radius: 5;");

            // Option A - styled with description
            HBox optionABox = new HBox(10);
            optionABox.setAlignment(Pos.CENTER_LEFT);
            Label optionALabel = new Label("A:");
            optionALabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            optionALabel.setTextFill(Color.BLACK);
            optionALabel.setMinWidth(30);
            optionAField = new TextField();
            styleControl(optionAField, "-fx-font-size: 14px;");
            HBox.setHgrow(optionAField, Priority.ALWAYS);
            optionABox.getChildren().addAll(optionALabel, optionAField);

            // Option B - styled
            HBox optionBBox = new HBox(10);
            optionBBox.setAlignment(Pos.CENTER_LEFT);
            Label optionBLabel = new Label("B:");
            optionBLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            optionBLabel.setTextFill(Color.BLACK);
            optionBLabel.setMinWidth(30);
            optionBField = new TextField();
            styleControl(optionBField, "-fx-font-size: 14px;");
            HBox.setHgrow(optionBField, Priority.ALWAYS);
            optionBBox.getChildren().addAll(optionBLabel, optionBField);

            // Option C - styled
            HBox optionCBox = new HBox(10);
            optionCBox.setAlignment(Pos.CENTER_LEFT);
            Label optionCLabel = new Label("C:");
            optionCLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            optionCLabel.setTextFill(Color.BLACK);
            optionCLabel.setMinWidth(30);
            optionCField = new TextField();
            Label optionCDescription = new Label("(optional)");
            optionCDescription.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            optionCDescription.setTextFill(Color.GRAY);
            styleControl(optionCField, "-fx-font-size: 14px;");
            HBox.setHgrow(optionCField, Priority.ALWAYS);
            optionCBox.getChildren().addAll(optionCLabel, optionCField, optionCDescription);

            // Option D - styled
            HBox optionDBox = new HBox(10);
            optionDBox.setAlignment(Pos.CENTER_LEFT);
            Label optionDLabel = new Label("D:");
            optionDLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            optionDLabel.setTextFill(Color.BLACK);
            optionDLabel.setMinWidth(30);
            optionDField = new TextField();
            Label optionDDescription = new Label("(optional)");
            optionDDescription.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            optionDDescription.setTextFill(Color.GRAY);
            styleControl(optionDField, "-fx-font-size: 14px;");
            HBox.setHgrow(optionDField, Priority.ALWAYS);
            optionDBox.getChildren().addAll(optionDLabel, optionDField, optionDDescription);

            optionsContainer.getChildren().addAll(optionABox, optionBBox, optionCBox, optionDBox);

            // Correct option with styled label and description - CHANGED to TextField
            Label correctOptionLabel = createFormLabel("Correct Answer");
            Label correctOptionDescription = createDescriptionLabel("Enter the correct answer text");
            correctOptionField = new TextField();
            styleControl(correctOptionField, "-fx-font-size: 14px;");

            getChildren().addAll(
                    questionLabel, questionDescription, questionField,
                    optionsLabel, optionsDescription, optionsContainer,
                    correctOptionLabel, correctOptionDescription, correctOptionField
            );
        }

        public void setSubjectField(ComboBox<String> subjectField) {
            this.subjectField = subjectField;
        }

        private Label createFormLabel(String text) {
            Label label = new Label(text);
            label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            label.setTextFill(Color.BLACK);
            label.setPadding(new Insets(5, 0, 0, 0));
            return label;
        }

        private Label createDescriptionLabel(String text) {
            Label label = new Label(text);
            label.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            label.setTextFill(Color.BLACK);
            label.setPadding(new Insets(0, 0, 5, 0));
            return label;
        }

        private void styleControl(Control control, String additionalStyle) {
            control.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; " +
                    "-fx-border-radius: 5; " + additionalStyle + "-fx-text-fill: black;");
        }

        public boolean isValid() {
            return subjectField != null && 
                   subjectField.getValue() != null && 
                   !subjectField.getValue().trim().isEmpty() &&
                   !questionField.getText().trim().isEmpty() &&
                   !optionAField.getText().trim().isEmpty() &&
                   !optionBField.getText().trim().isEmpty() &&
                   !correctOptionField.getText().trim().isEmpty(); // Check text not selection
        }

        public QuizQuestion createQuizQuestion() {
            return new QuizQuestion(
                    0, // ID will be set by the database
                    subjectField.getValue().trim(),
                    questionField.getText().trim(),
                    optionAField.getText().trim(),
                    optionBField.getText().trim(),
                    optionCField.getText().trim(),
                    optionDField.getText().trim(),
                    correctOptionField.getText().trim() // Store the actual answer text
            );
        }

        public void clear() {
            questionField.clear();
            optionAField.clear();
            optionBField.clear();
            optionCField.clear();
            optionDField.clear();
            correctOptionField.clear(); // Clear the text field
        }
    }

    // Main UI components
    private BorderPane mainLayout;
    private StackPane contentArea;
    private HBox topBar;
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

            // Create main layout
            mainLayout = new BorderPane();
            mainLayout.setStyle("-fx-background-color: #d6e4ff;"); // Darker blue background

            // Create top navigation bar
            createTopBar();
            mainLayout.setTop(topBar);

            // Create content area
            contentArea = new StackPane();
            contentArea.setPadding(new Insets(20));
            mainLayout.setCenter(contentArea);

            // Show overview by default
            showOverview();

            return mainLayout;
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
            errorTitle.setTextFill(Color.BLACK);

            Label errorLabel = new Label(e.getMessage());
            errorLabel.setFont(Font.font("Arial", 16));
            errorLabel.setTextFill(Color.BLACK);
            errorLabel.setWrapText(true);
            errorLabel.setTextAlignment(TextAlignment.CENTER);

            errorContainer.getChildren().addAll(errorTitle, errorLabel);
            return errorContainer;
        }
    }

    // Create the top navigation bar
    private void createTopBar() {
        topBar = new HBox();
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(20);
        topBar.setStyle("-fx-background-color: #48A969; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 5);");

        // Logo/Title
        Label titleLabel = new Label("Quiz Management System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.BLACK);
        titleLabel.setStyle("-fx-text-fill: black; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Navigation buttons
        Button overviewBtn = createNavButton("Dashboard");
        Button questionsBtn = createNavButton("Quiz Questions");
        Button resultsBtn = createNavButton("Quiz Results");
        Button createBtn = createNavButton("Create Quiz");

        // Add actions to buttons
        overviewBtn.setOnAction(e -> showOverview());
        questionsBtn.setOnAction(e -> showQuizQuestions());
        resultsBtn.setOnAction(e -> showQuizResults());
        createBtn.setOnAction(e -> showCreateQuiz());

        // Subject filter
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER);
        Label filterLabel = new Label("Filter by Subject:");
        filterLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        filterLabel.setTextFill(Color.BLACK);

        subjectFilterComboBox = new ComboBox<>();
        subjectFilterComboBox.setStyle("-fx-background-color: white; -fx-prompt-text-fill: #666;");
        subjectFilterComboBox.setPrefWidth(200);

        // Get unique subjects from database
        Set<String> subjects = new HashSet<>();
        subjects.add("All Subjects");
        for (QuizQuestion q : allQuestions) {
            subjects.add(q.getSubject());
        }
        subjectFilterComboBox.getItems().addAll(subjects);
        subjectFilterComboBox.setValue("All Subjects");

        // Apply filter when selection changes - FIXED: added null checks
        subjectFilterComboBox.setOnAction(e -> {
            String selectedSubject = subjectFilterComboBox.getValue();
            // Only filter if tables are initialized
            if (questionsTable != null) {
                filterQuestionsBySubject(selectedSubject);
            }
            if (resultsTable != null) {
                filterResultsBySubject(selectedSubject);
            }
        });

        // Add to filter box
        filterBox.getChildren().addAll(filterLabel, subjectFilterComboBox);

        // Add all to top bar with spacing
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(titleLabel, overviewBtn, questionsBtn, resultsBtn, createBtn, spacer, filterBox);
    }

    // Create a navigation button
    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        button.setTextFill(Color.BLACK);
        button.setStyle("-fx-background-color: transparent; -fx-padding: 5 10;");

        // Add hover effect
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: #209447; -fx-padding: 5 10;")
        );
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: transparent; -fx-padding: 5 10;")
        );

        return button;
    }

    // In the showOverview() method
    private void showOverview() {
        // Clear current content
        contentArea.getChildren().clear();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(0, 10, 20, 10));

        Label header = new Label("Quiz Management Dashboard");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        header.setTextFill(Color.BLACK); 
        header.setStyle("-fx-text-fill: black; -fx-font-size: 24px; -fx-font-weight: bold;");
        header.setPadding(new Insets(0, 0, 10, 0));

        // Statistics section
        TitledPane statsPane = createStatisticsPane();
        statsPane.setText("Performance by Subject");
        statsPane.setExpanded(true);
        VBox.setMargin(statsPane, new Insets(10, 0, 10, 0));

        VBox recentQuestionsSection = new VBox(10);

        // Label for recent questions
        Label recentQuestionsLabel = new Label("Recent Quiz Questions");
        recentQuestionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        recentQuestionsLabel.setTextFill(Color.BLACK);
        recentQuestionsLabel.setStyle("-fx-text-fill: black; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Important fix: Only add the label once
        recentQuestionsSection.getChildren().add(recentQuestionsLabel);
        recentQuestionsSection.setStyle("-fx-spacing: 10px;");

        // Create a smaller table with limited rows
        TableView<QuizQuestion> recentQuestionsTable = createQuestionsTable();
        recentQuestionsTable.setMaxHeight(250);

        // Only show the latest 5 questions
        ObservableList<QuizQuestion> recentQuestions = FXCollections.observableArrayList();
        int count = Math.min(5, allQuestions.size());
        for (int i = 0; i < count; i++) {
            recentQuestions.add(allQuestions.get(i));
        }
        recentQuestionsTable.setItems(recentQuestions);

        Button viewAllQuestionsBtn = new Button("View All Questions");
        viewAllQuestionsBtn.setStyle("-fx-background-color: #A7C4D7; -fx-text-fill: black;");
        viewAllQuestionsBtn.setOnAction(e -> showQuizQuestions());

        HBox buttonBox = new HBox(viewAllQuestionsBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Add the table and button box
        recentQuestionsSection.getChildren().addAll(recentQuestionsTable, buttonBox);

        // Add all components to the content
        content.getChildren().addAll(header, statsPane, recentQuestionsSection);
        scrollPane.setContent(content);
        contentArea.getChildren().add(scrollPane);
    }

    // Show the quiz questions page
    private void showQuizQuestions() {
        // Clear current content
        contentArea.getChildren().clear();

        VBox content = new VBox(15);
        content.setPadding(new Insets(0, 10, 20, 10));

        // Header with search
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Label title = new Label("Quiz Questions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.BLACK);
        title.setStyle("-fx-text-fill: black; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Search section
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_RIGHT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search questions...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 15;");

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: black; " +
                "-fx-background-radius: 5; -fx-font-weight: bold; -fx-padding: 8 15;");

        searchBox.getChildren().addAll(searchField, searchButton);

        // Add spacer to push search to right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, searchBox);

        // Create full questions table
        questionsTable = createQuestionsTable();
        VBox.setVgrow(questionsTable, Priority.ALWAYS);

        // Apply current filter
        if (subjectFilterComboBox.getValue() != null) {
            filterQuestionsBySubject(subjectFilterComboBox.getValue());
        } else {
            questionsTable.setItems(allQuestions);
        }

        // Search functionality
        searchButton.setOnAction(e -> {
            String searchText = searchField.getText().toLowerCase();
            if (searchText.isEmpty()) {
                filterQuestionsBySubject(subjectFilterComboBox.getValue());
            } else {
                ObservableList<QuizQuestion> searchResults = allQuestions.filtered(q -> 
                    q.getQuestion().toLowerCase().contains(searchText) ||
                    q.getSubject().toLowerCase().contains(searchText) ||
                    q.getOptionA().toLowerCase().contains(searchText) ||
                    q.getOptionB().toLowerCase().contains(searchText) ||
                    q.getOptionC().toLowerCase().contains(searchText) ||
                    q.getOptionD().toLowerCase().contains(searchText) ||
                    q.getCorrectOption().toLowerCase().contains(searchText)
                );
                questionsTable.setItems(searchResults);
            }
        });

        // Add all to content
        content.getChildren().addAll(header, questionsTable);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        contentArea.getChildren().add(scrollPane);
    }

    // Show the quiz results page
    private void showQuizResults() {
        // Clear current content
        contentArea.getChildren().clear();

        VBox content = new VBox(15);
        content.setPadding(new Insets(0, 10, 20, 10));

        Label title = new Label("Student Quiz Results");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.BLACK);
        title.setStyle("-fx-text-fill: black; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Create summary section
        HBox summaryBox = new HBox(20);
        summaryBox.setAlignment(Pos.CENTER_LEFT);

        totalResultsLabel = createSummaryLabel("Total Results: " + allResults.size(), "#0e9f6e");

        // Add correct/incorrect ratio
        int correctCount = 0;
        for (QuizResult result : allResults) {
            if (result.isCorrect()) {
                correctCount++;
            }
        }
        int totalCount = allResults.size();
        double correctPercent = totalCount > 0 ? (double)correctCount / totalCount * 100 : 0;

        Label correctRatioLabel = createSummaryLabel(
            String.format("Correct Answers: %d (%.1f%%)", correctCount, correctPercent), 
            correctPercent >= 70 ? "#0e9f6e" : "#e02424"
        );

        // Inline style for correctRatioLabel
        correctRatioLabel.setStyle(
            String.format("-fx-text-fill: %s; -fx-font-size: 16px; -fx-font-weight: bold;", 
            correctPercent >= 70 ? "#0e9f6e" : "#e02424")
        );

        summaryBox.getChildren().addAll(totalResultsLabel, correctRatioLabel);

        // Create results table
        resultsTable = createResultsTable();
        VBox.setVgrow(resultsTable, Priority.ALWAYS);

        // Apply current filter
        if (subjectFilterComboBox.getValue() != null) {
            filterResultsBySubject(subjectFilterComboBox.getValue());
        } else {
            resultsTable.setItems(allResults);
        }

        // Add all to content
        content.getChildren().addAll(title, summaryBox, resultsTable);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        contentArea.getChildren().add(scrollPane);
    }

    private void showCreateQuiz() {
        // Clear current content
        contentArea.getChildren().clear();
        
        // Set the background color of the content area to grey
        contentArea.setStyle("-fx-background-color: #f9fafb;"); // Light background for the outer area
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(0, 20, 20, 20));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: #d3d3d3;"); // Set grey background for the quiz creation area
        
        // Title section with better styling
        Label titleLabel = new Label("Create New Quiz Questions");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.BLACK);
        titleLabel.setPadding(new Insets(0, 0, 5, 0));
        titleLabel.setStyle("-fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: black; -fx-padding: 0 0 5 0;");
        
        // Instructions label with enhanced styling
        Label instructionsLabel = new Label("Fill in the details for up to 5 quiz questions. At least the first form must be completed.");
        instructionsLabel.setFont(Font.font("Arial", 16));
        instructionsLabel.setTextFill(Color.BLACK);
        instructionsLabel.setWrapText(true);
        instructionsLabel.setTextAlignment(TextAlignment.CENTER);
        instructionsLabel.setStyle("-fx-font-family: Arial; -fx-font-size: 16px; -fx-text-fill: black; -fx-wrap-text: true; -fx-text-alignment: center;");
        
        // Get existing subjects from database for the subject selector
        Set<String> subjects = new HashSet<>();
        for (QuizQuestion q : allQuestions) {
            subjects.add(q.getSubject());
        }
        
        // Create a subject selector at the top level
        VBox subjectBox = new VBox(5);
        Label subjectLabel = new Label("Select Subject for All Questions:");
        subjectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        subjectLabel.setTextFill(Color.BLACK);
        
        ComboBox<String> sharedSubjectField = new ComboBox<>();
        sharedSubjectField.setEditable(true);
        sharedSubjectField.getItems().addAll(subjects);
        sharedSubjectField.setPrefWidth(400);
        sharedSubjectField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-font-size: 14px; -fx-text-fill: black;");
        
        subjectBox.getChildren().addAll(subjectLabel, sharedSubjectField);
        subjectBox.setStyle("-fx-background-color: #a9c4d4; -fx-padding: 10; -fx-background-radius: 5;");
        
        VBox titleBox = new VBox(10);
        titleBox.getChildren().addAll(titleLabel, instructionsLabel, subjectBox);
        titleBox.setPadding(new Insets(0, 0, 15, 0));
        
        Accordion formsAccordion = new Accordion();
        formsAccordion.setPrefWidth(800);
        
        List<QuizForm> quizForms = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            QuizForm form = new QuizForm(subjects);
            // Set the shared subject field for this form
            form.setSubjectField(sharedSubjectField);
            
            TitledPane formPane = new TitledPane();
            formPane.setText("Question " + (i + 1));
            formPane.setContent(form);
            formPane.setAnimated(true);
            formPane.setStyle(
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-background-color: #ffffff; " +
                "-fx-text-fill: black;"
            );
            
            formsAccordion.getPanes().add(formPane);
            quizForms.add(form);
            
            if (i == 0) {
                formsAccordion.setExpandedPane(formPane);
            }
        }
        
        // Confirmation message container
        StackPane confirmationContainer = new StackPane();
        confirmationContainer.setVisible(false);
        confirmationContainer.setMinHeight(60);
        confirmationContainer.setStyle(
            "-fx-background-color: #d1fae5; " +
            "-fx-border-color: #34d399; " +
            "-fx-border-radius: 5; " +
            "-fx-padding: 15; " +
            "-fx-background-radius: 5;"
        );
        
        Label confirmationLabel = new Label("Quiz questions saved successfully!");
        confirmationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        confirmationLabel.setTextFill(Color.BLACK);
        confirmationContainer.getChildren().add(confirmationLabel);
        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Button clearButton = new Button("Clear All");
        clearButton.setStyle(
            "-fx-background-color: #ef4444; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );
        
        Button saveButton = new Button("Save Quiz Questions");
        saveButton.setStyle(
            "-fx-background-color: #10b981; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );
        
        buttonBox.getChildren().addAll(clearButton, saveButton);
        
        Label helpText = new Label("* Required fields must be completed");
        helpText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        helpText.setTextFill(Color.BLACK);
        helpText.setPadding(new Insets(5, 0, 0, 0));
        
        // Button actions
        clearButton.setOnAction(e -> {
            sharedSubjectField.setValue(null);
            for (QuizForm form : quizForms) {
                form.clear();
            }
            confirmationContainer.setVisible(false);
        });
        
        saveButton.setOnAction(e -> {
            // Check if the subject is selected
            if (sharedSubjectField.getValue() == null || sharedSubjectField.getValue().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR,
                    "Validation Error",
                    "Please select or enter a subject for the quiz questions.");
                return;
            }
            
            // Check if at least the first form is completed
            if (!quizForms.get(0).isValid()) {
                showAlert(Alert.AlertType.ERROR,
                    "Validation Error",
                    "Please complete at least the first quiz form with all required fields.");
                return;
            }
            
            List<QuizQuestion> newQuestions = new ArrayList<>();
            for (QuizForm form : quizForms) {
                if (form.isValid()) {
                    newQuestions.add(form.createQuizQuestion());
                }
            }
            
            boolean success = saveQuizQuestions(newQuestions);
            if (success) {
                confirmationContainer.setVisible(true);
                for (QuizForm form : quizForms) {
                    form.clear();
                }
                try {
                    loadAllQuizData();
                    if (questionsTable != null) {
                        filterQuestionsBySubject(subjectFilterComboBox.getValue());
                    }
                    if (resultsTable != null) {
                        filterResultsBySubject(subjectFilterComboBox.getValue());
                    }
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR,
                        "Database Error",
                        "Error reloading quiz data: " + ex.getMessage());
                }
            }
        });
        
        content.getChildren().addAll(titleBox, formsAccordion, confirmationContainer, buttonBox, helpText);
        scrollPane.setContent(content);
        contentArea.getChildren().add(scrollPane);
    }

    // Create a styled summary label
    private Label createSummaryLabel(String text, String color) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        label.setTextFill(Color.BLACK);
        label.setPadding(new Insets(10, 15, 10, 15));
        label.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 5;");
        return label;
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
                
                double averageScore = totalAttempts > 0 ? 
                    (double) totalCorrect / totalAttempts * 100.0 : 0.0;
                
                subjectStatistics.add(new QuizStatistics(
                    subject, totalQuestions, totalAttempts, totalCorrect, averageScore
                ));
            }
        }
        
        // Update subject filter dropdown
        if (subjectFilterComboBox != null) {
            subjectFilterComboBox.getItems().setAll(subjects);
            if (!subjectFilterComboBox.getItems().contains(subjectFilterComboBox.getValue())) {
                subjectFilterComboBox.setValue("All Subjects");
            }
        }
    }

    // Create a table for quiz questions with improved styling
    private TableView<QuizQuestion> createQuestionsTable() {
        TableView<QuizQuestion> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-table-cell-border-color: #e5e5e5; " +
            "-fx-table-header-border-color: #e5e5e5; " +
            "-fx-border-color: #e5e5e5;"
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
            text.setFill(Color.BLACK);
            return cell;
        });
        
        // Options columns group
        TableColumn<QuizQuestion, String> optionsGroup = new TableColumn<>("Options");
        optionsGroup.setStyle("-fx-alignment: CENTER;");
        
        // Option A with colored cell
        TableColumn<QuizQuestion, String> optionAColumn = new TableColumn<>("A");
        optionAColumn.setCellValueFactory(new PropertyValueFactory<>("optionA"));
        optionAColumn.setPrefWidth(120);
        optionAColumn.setCellFactory(col -> createColoredOptionCell("#2563eb"));
        
        // Option B with colored cell
        TableColumn<QuizQuestion, String> optionBColumn = new TableColumn<>("B");
        optionBColumn.setCellValueFactory(new PropertyValueFactory<>("optionB"));
        optionBColumn.setPrefWidth(120);
        optionBColumn.setCellFactory(col -> createColoredOptionCell("#0e9f6e"));
        
        // Option C with colored cell
        TableColumn<QuizQuestion, String> optionCColumn = new TableColumn<>("C");
        optionCColumn.setCellValueFactory(new PropertyValueFactory<>("optionC"));
        optionCColumn.setPrefWidth(120);
        optionCColumn.setCellFactory(col -> createColoredOptionCell("#9f1239"));
        
        // Option D with colored cell
        TableColumn<QuizQuestion, String> optionDColumn = new TableColumn<>("D");
        optionDColumn.setCellValueFactory(new PropertyValueFactory<>("optionD"));
        optionDColumn.setPrefWidth(120);
        optionDColumn.setCellFactory(col -> createColoredOptionCell("#7e3af2"));
        
        optionsGroup.getColumns().addAll(optionAColumn, optionBColumn, optionCColumn, optionDColumn);
        
        // Correct option column with highlighting - UPDATED to show full answer
        TableColumn<QuizQuestion, String> correctColumn = new TableColumn<>("Correct Answer");
        correctColumn.setCellValueFactory(new PropertyValueFactory<>("correctOption"));
        correctColumn.setPrefWidth(150); // Make it wider to fit the answer text
        correctColumn.setStyle("-fx-alignment: CENTER;");
        
        // Style the correct answer cell - simplified to just show text
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
                    setTextFill(Color.BLACK);
                    setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    setStyle("-fx-background-color: #d1fae5; -fx-alignment: center;"); // Green background
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
                    row.setStyle("-fx-background-color: #f3f4f6;");
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
            "-fx-table-cell-border-color: #e5e5e5; " +
            "-fx-table-header-border-color: #e5e5e5; " +
            "-fx-border-color: #e5e5e5;"
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
            text.setFill(Color.BLACK);
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
                icon.setStyle("-fx-background-color: #4b5563;");
                icon.setShape(new javafx.scene.shape.Circle(8, 8, 8));
                container.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().addAll(icon, nameLabel);
                nameLabel.setTextFill(Color.BLACK);
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
        
        // Selected option column (now showing the full answer text)
        TableColumn<QuizResult, String> selectedColumn = new TableColumn<>("Selected Answer");
        selectedColumn.setCellValueFactory(new PropertyValueFactory<>("selectedOption"));
        selectedColumn.setPrefWidth(150); // Wider for answer text
        selectedColumn.setStyle("-fx-alignment: CENTER;");
        
        // Style selected option cells
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
                    setTextFill(Color.BLACK);
                    setFont(Font.font("Arial", 14));
                    setStyle("-fx-background-color: #f0f9ff; -fx-alignment: center;"); // Light blue background
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
                        setGraphic(createIcon("✓", "#0e9f6e"));
                        setStyle("-fx-background-color: #d1fae5;");
                    } else {
                        setGraphic(createIcon("✗", "#e02424")); 
                        setStyle("-fx-background-color: #fee2e2;");
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
                    setTextFill(Color.BLACK);
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
        pane.setTextFill(Color.BLACK);
        pane.setCollapsible(true);
        pane.setExpanded(true);
        pane.setStyle("-fx-background-color: white;");
        
        // Create table for statistics
        TableView<QuizStatistics> statsTable = new TableView<>();
        statsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        statsTable.setStyle(
            "-fx-background-color: white; " +
            "-fx-table-cell-border-color: #e5e5e5; " +
            "-fx-table-header-border-color: #e5e5e5;"
        );
        
        // Subject column
        TableColumn<QuizStatistics, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubject()));
        subjectColumn.setPrefWidth(150);
        
        // Subject column with background color
        subjectColumn.setCellFactory(column -> new TableCell<QuizStatistics, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(Color.BLACK);
                    setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    setStyle("-fx-background-color: #eff6ff; -fx-padding: 5;");
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
                    setTextFill(Color.BLACK);
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
                percentLabel.setTextFill(Color.BLACK);
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
                        progressBar.setStyle("-fx-accent: #e02424;"); // Red
                    } else if (progress < 0.8) {
                        progressBar.setStyle("-fx-accent: #d97706;"); // Yellow/Orange
                    } else {
                        progressBar.setStyle("-fx-accent: #0e9f6e;"); // Green
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

    // Filter questions by subject - FIXED: added null check
    private void filterQuestionsBySubject(String subject) {
        if (questionsTable == null) return; // Add null check
        
        if ("All Subjects".equals(subject)) {
            questionsTable.setItems(allQuestions);
        } else {
            ObservableList<QuizQuestion> filtered = allQuestions.filtered(
                q -> subject.equals(q.getSubject())
            );
            questionsTable.setItems(filtered);
        }
        
        if (totalQuestionsLabel != null) {
            totalQuestionsLabel.setText("Total Questions: " + questionsTable.getItems().size());
        }
    }

    // Filter results by subject - FIXED: added null check
    private void filterResultsBySubject(String subject) {
        if (resultsTable == null) return; // Add null check
        
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
        
        if (totalResultsLabel != null) {
            totalResultsLabel.setText("Total Results: " + resultsTable.getItems().size());
        }
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
        headerBox.setStyle("-fx-background-color: #2563eb; -fx-padding: 20;");
        
        Label questionIdLabel = new Label("Question #" + question.getId() + " - " + question.getSubject());
        questionIdLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        questionIdLabel.setTextFill(Color.BLACK);
        
        Label questionTextLabel = new Label(question.getQuestion());
        questionTextLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        questionTextLabel.setTextFill(Color.BLACK);
        questionTextLabel.setWrapText(true);
        
        headerBox.getChildren().addAll(questionIdLabel, questionTextLabel);
        
        // Add question details in a grid with colored options
        GridPane questionDetails = new GridPane();
        questionDetails.setVgap(15);
        questionDetails.setHgap(15);
        questionDetails.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 5;");
        
        // Create answer option boxes with styling - UPDATED to compare with correct answer text
        String correctAnswer = question.getCorrectOption();
        HBox optionABox = createOptionBox("A", question.getOptionA(), "#dbeafe", 
            correctAnswer.equals(question.getOptionA()));
        HBox optionBBox = createOptionBox("B", question.getOptionB(), "#d1fae5", 
            correctAnswer.equals(question.getOptionB()));
        HBox optionCBox = createOptionBox("C", question.getOptionC(), "#fee2e2", 
            correctAnswer.equals(question.getOptionC()));
        HBox optionDBox = createOptionBox("D", question.getOptionD(), "#ede9fe", 
            correctAnswer.equals(question.getOptionD()));
        
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
        summaryTitle.setTextFill(Color.BLACK);
        
        // Statistics with progress bars
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(10);
        
        statsGrid.add(new Label("Total Responses:"), 0, 0);
        Label totalLabel = new Label(Integer.toString(totalAnswers));
        totalLabel.setTextFill(Color.BLACK);
        statsGrid.add(totalLabel, 1, 0);
        
        statsGrid.add(new Label("Correct Answers:"), 0, 1);
        Label correctLabel = new Label(Integer.toString(correctAnswers));
        correctLabel.setTextFill(Color.BLACK);
        correctLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statsGrid.add(correctLabel, 1, 1);
        
        statsGrid.add(new Label("Success Rate:"), 0, 2);
        
        // Add progress bar for success rate
        HBox rateBox = new HBox(10);
        ProgressBar rateBar = new ProgressBar(correctPercentage / 100);
        rateBar.setPrefWidth(150);
        
        // Color based on percentage
        if (correctPercentage < 60) {
            rateBar.setStyle("-fx-accent: #e02424;"); // Red
        } else if (correctPercentage < 80) {
            rateBar.setStyle("-fx-accent: #d97706;"); // Orange
        } else {
            rateBar.setStyle("-fx-accent: #0e9f6e;"); // Green
        }
        
        Label percentLabel = new Label(String.format("%.1f%%", correctPercentage));
        percentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        percentLabel.setTextFill(Color.BLACK);
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
        
        // Selected option column with full answer text
        TableColumn<QuizResult, String> selectedColumn = new TableColumn<>("Selected Answer");
        selectedColumn.setCellValueFactory(new PropertyValueFactory<>("selectedOption"));
        selectedColumn.setPrefWidth(150); // Wider for answer text
        
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
                    setTextFill(Color.BLACK);
                    setFont(Font.font("Arial", 14));
                    setAlignment(Pos.CENTER);
                    
                    // Highlight if selected option is correct
                    if (item.equals(correctAnswer)) {
                        setStyle("-fx-background-color: #d1fae5;"); // Green for correct
                    } else {
                        setStyle("-fx-background-color: #fee2e2;"); // Red for incorrect
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
                        setTextFill(Color.web("#0e9f6e"));
                        setStyle("-fx-background-color: #d1fae5;");
                    } else {
                        setText("✗");
                        setTextFill(Color.web("#e02424"));
                        setStyle("-fx-background-color: #fee2e2;");
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
            "-fx-background-color: #2563eb; " +
            "-fx-text-fill: black; " +
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
        box.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; " +
            "-fx-border-radius: 5; -fx-background-radius: 5; " +
            (isCorrect ? "-fx-background-color: " + color + ";" : ""));
        
        // Option letter label
        Label letterLabel = new Label(optionLetter);
        letterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        letterLabel.setTextFill(Color.BLACK);
        letterLabel.setAlignment(Pos.CENTER);
        letterLabel.setMinWidth(30);
        letterLabel.setMinHeight(30);
        letterLabel.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15;");
        
        // Option text with truncation if needed
        Label textLabel = new Label(optionText != null ? optionText : "-");
        textLabel.setWrapText(true);
        textLabel.setFont(Font.font("Arial", 14));
        textLabel.setTextFill(Color.BLACK);
        HBox.setHgrow(textLabel, Priority.ALWAYS);
        
        // Add correct answer indicator if this is the correct option
        if (isCorrect) {
            Label correctLabel = new Label("✓");
            correctLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            correctLabel.setTextFill(Color.web("#0e9f6e"));
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
        if (type == Alert.AlertType.ERROR) {
            bgColor = "#fee2e2";
        } else if (type == Alert.AlertType.WARNING) {
            bgColor = "#fef3c7";
        } else if (type == Alert.AlertType.INFORMATION) {
            bgColor = "#dbeafe";
        } else if (type == Alert.AlertType.CONFIRMATION) {
            bgColor = "#d1fae5";
        }
        
        dialogPane.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-border-color: #cccccc; " +
            "-fx-border-width: 1px;"
        );
        
        // Find and style any labels to ensure text visibility
        for (Node node : dialogPane.lookupAll(".label")) {
            if (node instanceof Label) {
                ((Label) node).setTextFill(Color.BLACK);
                ((Label) node).setFont(Font.font("Arial", 14));
            }
        }
        
        // Style the buttons
        for (ButtonType buttonType : alert.getDialogPane().getButtonTypes()) {
            Button button = (Button) alert.getDialogPane().lookupButton(buttonType);
            if (buttonType == ButtonType.OK || buttonType == ButtonType.YES) {
                button.setStyle(
                    "-fx-background-color: #2563eb; " +
                    "-fx-text-fill: black; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 5;"
                );
            } else if (buttonType == ButtonType.CANCEL || buttonType == ButtonType.NO) {
                button.setStyle(
                    "-fx-background-color: #e02424; " +
                    "-fx-text-fill: black; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 5;"
                );
            } else {
                button.setStyle(
                    "-fx-background-color: #6b7280; " +
                    "-fx-text-fill: black; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 5;"
                );
            }
        }
        
        alert.showAndWait();
    }
}