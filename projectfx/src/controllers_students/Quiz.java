package controllers_students;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import dao.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Quiz {
    private List<Question> questions;
    private Map<Integer, Integer> questionIdMap; // Maps index to database question ID
    private Map<Integer, String> userAnswers; // Stores user answers for each question
    private int currentQuestionIndex;
    private int score;
    private String subject;
    private int studentId;
    private boolean isQuizCompleted = false;
    
    // Quiz list data
    private ObservableList<QuizData> availableQuizzes = FXCollections.observableArrayList();
    
    // UI components
    private BorderPane mainLayout;
    private VBox quizListView;  // View for quiz selection
    private VBox quizContentView;  // View for actual quiz
    private Label questionLabel, scoreLabel, subjectLabel, questionCountLabel, timerLabel;
    private ToggleButton optionA, optionB, optionC, optionD;
    private ToggleGroup answerGroup;
    private Button prevQuestionButton, nextQuestionButton, finishQuizButton, backToListButton;
    private TableView<QuizData> quizTable;
    private Question currentQuestion;
    private ProgressBar progressBar;
    
    // UI styling constants
    private static final String PRIMARY_COLOR = "#3498db";
    private static final String SECONDARY_COLOR = "#2980b9";
    private static final String ACCENT_COLOR = "#e74c3c";
    private static final String BACKGROUND_COLOR = "#ecf0f1";
    private static final String SUCCESS_COLOR = "#2ecc71";
    private static final String WARNING_COLOR = "#f39c12";

    /**
     * Default constructor for creating the Quiz view from dashboard
     */
    public Quiz() {
        this.questions = new ArrayList<>();
        this.questionIdMap = new HashMap<>();
        this.userAnswers = new HashMap<>();
        this.currentQuestionIndex = 0;
        this.score = 0;
        
        // Get current student ID from Login
        User loggedInUser = Login.getLoggedInUser();
        if (loggedInUser != null) {
            this.studentId = getStudentIdForUser(loggedInUser.getUserID());
        } else {
            this.studentId = -1; // Default value if not logged in
        }
        
        setupUI();
        loadAvailableQuizzes();
    }

    /**
     * Get student ID for the logged-in user
     */
    private int getStudentIdForUser(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return -1;
            
            String query = "SELECT studentID FROM Students WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("studentID");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting student ID: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Sets up the UI components with enhanced styling
     */
    private void setupUI() {
        // Create main layout with background color
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Create the quiz list view
        quizListView = createQuizListView();
        
        // Create the quiz content view
        quizContentView = createQuizContentView();
        
        // Initially show the quiz list
        mainLayout.setCenter(quizListView);
    }
    
    /**
     * Creates the quiz list view
     */
    private VBox createQuizListView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        view.setEffect(dropShadow);
        
        // Header with logo and title
        HBox headerBox = createHeader();
        
        // Title
        Label titleLabel = new Label("Available Quizzes");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_COLOR + ";");
        
        // Instructions
        Label instructionsLabel = new Label("Select a quiz from the list below to begin.");
        instructionsLabel.setFont(Font.font("System", 14));
        instructionsLabel.setStyle("-fx-text-fill: #555555;");
        
        // Create table for available quizzes
        quizTable = new TableView<>();
        quizTable.setPlaceholder(new Label("No quizzes available"));
        quizTable.setPrefHeight(400);
        
        // Define table columns
        TableColumn<QuizData, Integer> idCol = new TableColumn<>("Quiz ID");
        idCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        idCol.setPrefWidth(80);
        
        TableColumn<QuizData, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubject()));
        subjectCol.setPrefWidth(150);
        
        TableColumn<QuizData, Integer> questionCountCol = new TableColumn<>("Questions");
        questionCountCol.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getQuestionCount()).asObject());
        questionCountCol.setPrefWidth(100);
        
        TableColumn<QuizData, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        
        // Custom status cell rendering (without using binding)
        statusCol.setCellFactory(column -> new TableCell<QuizData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Completed".equals(item)) {
                        setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
                    } else if ("Available".equals(item)) {
                        setStyle("-fx-text-fill: " + PRIMARY_COLOR + ";");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Action column with Start button
        TableColumn<QuizData, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        
        // Safe action column implementation that doesn't use binding
        actionCol.setCellFactory(column -> new TableCell<QuizData, Void>() {
            private final Button btn = new Button("Start Quiz");
            
            {
                btn.setStyle(
                    "-fx-background-color: " + PRIMARY_COLOR + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 4px;"
                );
                
                btn.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        QuizData quiz = getTableView().getItems().get(index);
                        
                        // If quiz is completed, show score instead of opening it
                        if ("Completed".equals(quiz.getStatus())) {
                            showCompletedQuizScore(quiz.getSubject());
                        } else {
                            startQuiz(quiz.getSubject());
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        QuizData quiz = getTableView().getItems().get(index);
                        
                        // Change button text and style based on status
                        if ("Completed".equals(quiz.getStatus())) {
                            btn.setText("View Results");
                            btn.setStyle(
                                "-fx-background-color: " + SECONDARY_COLOR + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 4px;"
                            );
                        } else {
                            btn.setText("Start Quiz");
                            btn.setStyle(
                                "-fx-background-color: " + PRIMARY_COLOR + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 4px;"
                            );
                        }
                        
                        setGraphic(btn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        quizTable.getColumns().addAll(idCol, subjectCol, questionCountCol, statusCol, actionCol);
        
        // Refresh button
        Button refreshButton = new Button("Refresh List");
        refreshButton.setStyle(
            "-fx-background-color: " + SECONDARY_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 8 15;"
        );
        refreshButton.setOnAction(e -> loadAvailableQuizzes());
        

        // Button container
        HBox buttonBox = new HBox(15, refreshButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        view.getChildren().addAll(
            headerBox,
            titleLabel,
            instructionsLabel,
            quizTable,
            buttonBox
        );
        
        return view;
    }
    
    /**
     * Creates the quiz content view
     */
    private VBox createQuizContentView() {
        VBox contentCard = new VBox(20);
        contentCard.setPadding(new Insets(20));
        contentCard.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10px; " +
                "-fx-padding: 20px;");
        
        // Add drop shadow effect to content card
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        contentCard.setEffect(dropShadow);
        
        // Header with logo and title
        HBox headerBox = createHeader();
        
        // Back button
        backToListButton = new Button("← Back to Quiz List");
        backToListButton.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: " + PRIMARY_COLOR + "; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 5 0;"
        );
        backToListButton.setOnAction(e -> confirmQuitQuiz());
        
        // Quiz information bar
        HBox infoBox = createInfoBar();
        
        // Progress tracking
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: " + PRIMARY_COLOR + ";");
        
        // Question display with enhanced styling
        questionLabel = new Label("Starting quiz...");
        questionLabel.setWrapText(true);
        questionLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        questionLabel.setPadding(new Insets(15, 10, 15, 10));
        questionLabel.setStyle("-fx-background-color: " + PRIMARY_COLOR + "33; " + // 20% opacity
                "-fx-background-radius: 5px; " +
                "-fx-padding: 15px;");
        questionLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Timer label (for future implementation)
        timerLabel = new Label("");
        timerLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        timerLabel.setStyle("-fx-text-fill: " + ACCENT_COLOR + ";");
        
        // Answer options with improved styling
        VBox optionsBox = createOptionsBox();
        
        // Navigation buttons with enhanced styling
        HBox navigationBox = createNavigationBox();
        
        // Finish quiz button
        finishQuizButton = createStyledButton("Finish & Submit Quiz", SUCCESS_COLOR);
        finishQuizButton.setPrefWidth(200);
        finishQuizButton.setOnAction(e -> finishQuiz());
        
        // Add components to the content card
        contentCard.getChildren().addAll(
                headerBox,
                backToListButton,
                infoBox,
                progressBar,
                questionLabel,
                optionsBox,
                navigationBox,
                finishQuizButton
        );
        
        return contentCard;
    }

    /**
     * Creates a stylish header with app logo and title
     */
    private HBox createHeader() {
        // App title
        Label titleLabel = new Label("QuizMaster");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_COLOR + ";");
        
        // Logo placeholder - replace path with actual logo if available
        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/resources/quiz_logo.png")));
            logoView.setFitHeight(40);
            logoView.setFitWidth(40);
            logoView.setPreserveRatio(true);
            HBox headerBox = new HBox(10, logoView, titleLabel);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            return headerBox;
        } catch (Exception e) {
            // Fallback if no image is found
            Label logoLabel = new Label("📝");
            logoLabel.setFont(Font.font("System", 30));
            HBox headerBox = new HBox(10, logoLabel, titleLabel);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            return headerBox;
        }
    }

    /**
     * Creates an information bar showing quiz progress
     */
    private HBox createInfoBar() {
        subjectLabel = new Label();
        subjectLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        subjectLabel.setStyle("-fx-text-fill: " + PRIMARY_COLOR + ";");
        
        questionCountLabel = new Label();
        questionCountLabel.setFont(Font.font("System", 14));
        
        scoreLabel = new Label("");
        scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        scoreLabel.setStyle("-fx-text-fill: " + SUCCESS_COLOR + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox infoBox = new HBox(20, subjectLabel, questionCountLabel, spacer, scoreLabel);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(10, 0, 10, 0));
        
        return infoBox;
    }

    /**
     * Creates a styled box for answer options
     */
    private VBox createOptionsBox() {
        answerGroup = new ToggleGroup();
        
        optionA = createOptionButton("A");
        optionB = createOptionButton("B");
        optionC = createOptionButton("C");
        optionD = createOptionButton("D");
        
        optionA.setToggleGroup(answerGroup);
        optionB.setToggleGroup(answerGroup);
        optionC.setToggleGroup(answerGroup);
        optionD.setToggleGroup(answerGroup);
        
        // Add listener to save the selected answer when option changes
        answerGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && currentQuestionIndex >= 0) {
                String selectedOption = "";
                
                if (newVal == optionA) selectedOption = "A";
                else if (newVal == optionB) selectedOption = "B";
                else if (newVal == optionC) selectedOption = "C";
                else if (newVal == optionD) selectedOption = "D";
                
                // Store the answer in the map
                userAnswers.put(currentQuestionIndex, selectedOption);
            }
        });
        
        VBox optionsBox = new VBox(15, optionA, optionB, optionC, optionD);
        optionsBox.setPadding(new Insets(20, 0, 20, 0));
        
        return optionsBox;
    }

    /**
     * Creates a stylish toggle button for answer options
     */
    private ToggleButton createOptionButton(String optionLetter) {
        ToggleButton optionButton = new ToggleButton();
        optionButton.setPrefWidth(Double.MAX_VALUE);
        optionButton.setAlignment(Pos.CENTER_LEFT);
        optionButton.setPadding(new Insets(12));
        optionButton.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px;");
        
        // Style when selected
        optionButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                optionButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "33; " + // 20% opacity
                        "-fx-border-color: " + PRIMARY_COLOR + "; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;");
            } else {
                optionButton.setStyle("-fx-background-color: white; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;");
            }
        });
        
        // Hover effect
        optionButton.setOnMouseEntered(e -> {
            if (!optionButton.isSelected()) {
                optionButton.setStyle("-fx-background-color: #f5f5f5; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;");
            }
        });
        optionButton.setOnMouseExited(e -> {
            if (!optionButton.isSelected()) {
                optionButton.setStyle("-fx-background-color: white; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;");
            }
        });
        
        return optionButton;
    }

    /**
     * Creates a navigation box with prev/next buttons
     */
    private HBox createNavigationBox() {
        prevQuestionButton = createStyledButton("← Previous Question", SECONDARY_COLOR);
        prevQuestionButton.setDisable(true);
        prevQuestionButton.setOnAction(e -> loadPreviousQuestion());
        
        nextQuestionButton = createStyledButton("Next Question →", PRIMARY_COLOR);
        nextQuestionButton.setOnAction(e -> loadNextQuestion());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox navigationBox = new HBox(15, prevQuestionButton, spacer, nextQuestionButton);
        navigationBox.setAlignment(Pos.CENTER);
        navigationBox.setPadding(new Insets(10, 0, 10, 0));
        
        return navigationBox;
    }

    /**
     * Creates a styled button with hover effects
     */
    private Button createStyledButton(String text, String baseColor) {
        Button button = new Button(text);
        button.setPrefWidth(180);
        button.setPrefHeight(40);
        button.setFont(Font.font("System", FontWeight.BOLD, 12));
        button.setStyle("-fx-background-color: " + baseColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5px;");
        
        // Lighten color for hover (make it 15% lighter)
        String hoverColor = baseColor;
        
        // Hover effects
        button.setOnMouseEntered(e -> {
            if (!button.isDisabled()) {
                button.setStyle("-fx-background-color: " + hoverColor + "CC; " + // 80% opacity for hover
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px;");
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisabled()) {
                button.setStyle("-fx-background-color: " + baseColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px;");
            }
        });
        
        // Style for disabled state
        button.disabledProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                button.setStyle("-fx-background-color: #cccccc; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-background-radius: 5px;");
            } else {
                button.setStyle("-fx-background-color: " + baseColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px;");
            }
        });
        
        return button;
    }
    
    /**
     * Shows the quiz list view
     */
    private void showQuizList() {
        // Reload available quizzes
        loadAvailableQuizzes();
        
        // Switch to quiz list view
        mainLayout.setCenter(quizListView);
        
        // Reset quiz state
        resetQuizState();
    }
    
    /**
     * Reset the quiz state
     */
    private void resetQuizState() {
        this.questions.clear();
        this.questionIdMap.clear();
        this.userAnswers.clear();
        this.currentQuestionIndex = 0;
        this.score = 0;
        this.isQuizCompleted = false;
    }
    
    /**
     * Shows the quiz content view
     */
    private void showQuizContent() {
        // Switch to quiz content view
        mainLayout.setCenter(quizContentView);
        
        // Update UI elements
        subjectLabel.setText("Subject: " + subject);
        scoreLabel.setText("");
        updateNavigationButtons();
    }

    /**
     * Returns the view component to be used in the dashboard
     * @return JavaFX Parent component
     */
    public Parent getView() {
        return mainLayout;
    }
    
    /**
     * Loads available quizzes from the database
     */
    private void loadAvailableQuizzes() {
        availableQuizzes.clear();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            
            // Query to get distinct subjects and question counts
            String query = "SELECT DISTINCT subject, " +
                          "COUNT(*) as questionCount " +
                          "FROM quiz_questions " +
                          "GROUP BY subject " +
                          "ORDER BY subject";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    String subject = rs.getString("subject");
                    int questionCount = rs.getInt("questionCount");
                    int quizId = getFirstQuestionIdForSubject(conn, subject);
                    
                    // Check completion status if student is logged in
                    String status = "Available";
                    if (studentId > 0) {
                        // Check if student has completed this quiz
                        status = checkQuizCompletionStatus(conn, subject, questionCount);
                    }
                    
                    // Add to list of available quizzes
                    availableQuizzes.add(new QuizData(quizId, subject, questionCount, status));
                }
                
                // Update table
                quizTable.setItems(availableQuizzes);
                
            }
        } catch (SQLException e) {
            System.err.println("Error loading available quizzes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get the first question ID for a subject
     */
    private int getFirstQuestionIdForSubject(Connection conn, String subject) throws SQLException {
        String query = "SELECT MIN(id) as firstId FROM quiz_questions WHERE subject = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, subject);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("firstId");
                }
            }
        }
        return -1;
    }
    
    /**
     * Check if a quiz has been completed by the student
     */
    private String checkQuizCompletionStatus(Connection conn, String subject, int totalQuestions) throws SQLException {
        String query = "SELECT COUNT(DISTINCT qr.questionID) as answered " +
                      "FROM QuizResults qr " +
                      "JOIN quiz_questions qq ON qr.questionID = qq.id " +
                      "WHERE qr.studentID = ? AND qq.subject = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, subject);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int answered = rs.getInt("answered");
                    // Consider quiz completed if student has answered 80% or more questions
                    if (answered >= totalQuestions * 0.8) {
                        return "Completed";
                    }
                }
            }
        }
        
        return "Available";
    }

    /**
     * Show score for a completed quiz without opening it
     */
    private void showCompletedQuizScore(String subject) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            
            // Get total questions
            String totalQuery = "SELECT COUNT(*) as total FROM quiz_questions WHERE subject = ?";
            int total = 0;
            
            try (PreparedStatement pstmt = conn.prepareStatement(totalQuery)) {
                pstmt.setString(1, subject);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getInt("total");
                    }
                }
            }
            
            // Get correct answers
            String scoreQuery = "SELECT COUNT(*) as correct FROM QuizResults qr " +
                               "JOIN quiz_questions qq ON qr.questionID = qq.id " +
                               "WHERE qr.studentID = ? AND qq.subject = ? AND qr.isCorrect = true";
            
            int correctCount = 0;
            
            try (PreparedStatement pstmt = conn.prepareStatement(scoreQuery)) {
                pstmt.setInt(1, studentId);
                pstmt.setString(2, subject);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        correctCount = rs.getInt("correct");
                    }
                }
            }
            
            // Calculate percentage
            double percentage = total > 0 ? (double) correctCount / total * 100 : 0;
            
            // Show result dialog
            showScoreDialog(subject, correctCount, total, percentage);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving quiz score: " + e.getMessage());
            e.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve quiz score.");
        }
    }
    
    /**
     * Show score dialog for completed quiz
     */
    private void showScoreDialog(String subject, int score, int total, double percentage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Quiz Results: " + subject);
        
        // Determine result message and color based on score
        String resultMessage;
        String resultColor;
        
        if (percentage >= 80) {
            resultMessage = "Excellent!";
            resultColor = SUCCESS_COLOR;
        } else if (percentage >= 60) {
            resultMessage = "Good job!";
            resultColor = PRIMARY_COLOR;
        } else if (percentage >= 40) {
            resultMessage = "Nice try!";
            resultColor = WARNING_COLOR;
        } else {
            resultMessage = "Keep practicing!";
            resultColor = ACCENT_COLOR;
        }
        
        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(resultMessage);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: " + resultColor + ";");
        
        Label scoreLabel = new Label("Your score: " + score + "/" + total);
        scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Label percentageLabel = new Label(String.format("%.2f%%", percentage));
        percentageLabel.setFont(Font.font("System", 16));
        
        ProgressBar resultProgress = new ProgressBar(percentage / 100);
        resultProgress.setPrefWidth(300);
        resultProgress.setStyle("-fx-accent: " + resultColor + ";");
        
        content.getChildren().addAll(titleLabel, scoreLabel, percentageLabel, resultProgress);
        
        // Add buttons
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        
        // Set content
        dialog.getDialogPane().setContent(content);
        
        // Style dialog
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        
        // Show dialog
        dialog.showAndWait();
    }

    /**
     * Loads questions from the database for the specified subject
     */
    private void loadQuestionsFromDatabase() {
        questions.clear();
        questionIdMap.clear();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            
            // Prepare SQL query to fetch questions for the specified subject
            String query = "SELECT * FROM quiz_questions WHERE subject = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, subject);
                
                // Execute query
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Process results
                    int index = 0;
                    while (resultSet.next()) {
                        int questionId = resultSet.getInt("id");
                        String questionText = resultSet.getString("question");
                        String optionA = resultSet.getString("option_a");
                        String optionB = resultSet.getString("option_b");
                        String optionC = resultSet.getString("option_c");
                        String optionD = resultSet.getString("option_d");
                        String correctOption = resultSet.getString("correct_option");
                        
                        Question question = new Question(questionText, optionA, optionB, optionC, optionD, correctOption);
                        questions.add(question);
                        
                        // Store the mapping between question index and database ID
                        questionIdMap.put(index, questionId);
                        index++;
                    }
                }
            }
            
            System.out.println("Loaded " + questions.size() + " questions for " + subject);
            
        } catch (SQLException e) {
            System.err.println("Error loading questions from database:");
            e.printStackTrace();
        }
    }

    /**
     * Starts a new quiz with the selected subject
     */
    private void startQuiz(String subject) {
        this.subject = subject;
        resetQuizState();
        
        // Load questions
        loadQuestionsFromDatabase();
        
        if (questions.isEmpty()) {
            showStyledAlert(Alert.AlertType.ERROR, "Error", "No questions available for the selected subject");
            return;
        }
        
        // Shuffle questions
        Collections.shuffle(questions);
        
        // Switch to quiz content view
        showQuizContent();
        
        // Load first question
        displayCurrentQuestion();
    }

    /**
     * Checks if there are more questions available
     * @return True if there are more questions, false otherwise
     */
    public boolean hasNextQuestion() {
        return currentQuestionIndex < questions.size() - 1;
    }

    /**
     * Checks if there are previous questions available
     * @return True if there are previous questions, false otherwise
     */
    public boolean hasPreviousQuestion() {
        return currentQuestionIndex > 0;
    }

    /**
     * Gets the current question
     * @return The current question object
     */
    public Question getCurrentQuestion() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }

    /**
     * Displays the current question
     */
    private void displayCurrentQuestion() {
        currentQuestion = getCurrentQuestion();
        
        if (currentQuestion != null) {
            // Update question count
            questionCountLabel.setText("Question: " + (currentQuestionIndex + 1) + 
                    " of " + questions.size());
            
            // Update progress bar
            double progress = (double) (currentQuestionIndex) / (questions.size() - 1);
            progressBar.setProgress(progress);
            
            // Update question text and options with animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), questionLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.3);
            fadeOut.setOnFinished(e -> {
                // Update question and options
                questionLabel.setText(currentQuestion.getText());
                optionA.setText("A: " + currentQuestion.getOptionA());
                optionB.setText("B: " + currentQuestion.getOptionB());
                optionC.setText("C: " + currentQuestion.getOptionC());
                optionD.setText("D: " + currentQuestion.getOptionD());
                
                // Set answer selection if user has answered this question before
                String savedAnswer = userAnswers.get(currentQuestionIndex);
                if (savedAnswer != null) {
                    switch (savedAnswer) {
                        case "A": answerGroup.selectToggle(optionA); break;
                        case "B": answerGroup.selectToggle(optionB); break;
                        case "C": answerGroup.selectToggle(optionC); break;
                        case "D": answerGroup.selectToggle(optionD); break;
                        default: answerGroup.selectToggle(null);
                    }
                } else {
                    answerGroup.selectToggle(null);
                }
                
                // Fade back in
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), questionLabel);
                fadeIn.setFromValue(0.3);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
            
            // Update navigation buttons
            updateNavigationButtons();
            
            // Show finish button only if there are answers
            finishQuizButton.setVisible(!userAnswers.isEmpty());
        }
    }
    
    /**
     * Update the navigation buttons based on current question index
     */
    private void updateNavigationButtons() {
        prevQuestionButton.setDisable(!hasPreviousQuestion());
        nextQuestionButton.setDisable(!hasNextQuestion());
        
        // Change next button text on last question
        if (!hasNextQuestion()) {
            // If we've answered all questions, enable the finish button
            if (userAnswers.size() >= questions.size()) {
                finishQuizButton.setDisable(false);
            }
        }
    }

    /**
     * Loads the next question
     */
    private void loadNextQuestion() {
        if (hasNextQuestion()) {
            currentQuestionIndex++;
            displayCurrentQuestion();
        }
    }
    
    /**
     * Loads the previous question
     */
    private void loadPreviousQuestion() {
        if (hasPreviousQuestion()) {
            currentQuestionIndex--;
            displayCurrentQuestion();
        }
    }
    
    /**
     * Confirm quit quiz dialog
     */
    private void confirmQuitQuiz() {
        if (userAnswers.isEmpty()) {
            showQuizList();
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quit Quiz");
        alert.setHeaderText("Are you sure you want to quit?");
        alert.setContentText("Your progress will be lost. You can return to the quiz later.");
        
        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        
        // Style buttons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setText("Quit Quiz");
            okButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 5px;");
        }
        
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 5px;");
        }
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                showQuizList();
            }
        });
    }

    /**
     * Finishes the quiz and grades it
     */
    private void finishQuiz() {
        // Check if user has answered all questions
        if (userAnswers.size() < questions.size()) {
            int unanswered = questions.size() - userAnswers.size();
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Incomplete Quiz");
            alert.setHeaderText("You have " + unanswered + " unanswered question(s)");
            alert.setContentText("Do you want to submit anyway? Unanswered questions will be marked as incorrect.");
            
            // Style the alert dialog
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: white;");
            
            // Style buttons
            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setText("Submit Anyway");
                okButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px;");
            }
            
            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            if (cancelButton != null) {
                cancelButton.setText("Continue Quiz");
                cancelButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5px;");
            }
            
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    gradeQuiz();
                }
            });
        } else {
            gradeQuiz();
        }
    }
    
    /**
     * Grades the quiz and shows results
     */
    private void gradeQuiz() {
        // Grade the quiz
        score = 0;
        
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String userAnswer = userAnswers.get(i);
            
            // Convert option letter to actual answer text
            String selectedAnswer = "";
            if (userAnswer != null) {
                switch (userAnswer) {
                    case "A": selectedAnswer = question.getOptionA(); break;
                    case "B": selectedAnswer = question.getOptionB(); break;
                    case "C": selectedAnswer = question.getOptionC(); break;
                    case "D": selectedAnswer = question.getOptionD(); break;
                }
            }
            
            boolean isCorrect = selectedAnswer.equals(question.getCorrectAnswer());
            
            // Increment score if correct
            if (isCorrect) {
                score++;
            }
            
            // Save the result to database
            if (studentId > 0 && userAnswer != null) {
                saveAnswerToDatabase(questionIdMap.get(i), userAnswer, isCorrect);
            }
        }
        
        // Mark quiz as completed
        markQuizAsCompleted();
        
        // Show results
        double percentage = questions.isEmpty() ? 0 : (double) score / questions.size() * 100;
        showResultDialog(score, questions.size(), percentage);
        
        // Update the quiz list to show completion status
        for (QuizData quiz : availableQuizzes) {
            if (quiz.getSubject().equals(subject)) {
                quiz.setStatus("Completed");
                break;
            }
        }
        
        // Return to quiz list
        showQuizList();
    }

    /**
     * Mark quiz as completed by updating completion status
     */
    private void markQuizAsCompleted() {
        if (studentId <= 0) {
            System.out.println("Cannot mark quiz as completed: Student ID not available.");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            
            // Create CompletedQuizzes table if it doesn't exist
            String createTableQuery = 
                "CREATE TABLE IF NOT EXISTS CompletedQuizzes (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "studentID INT NOT NULL, " +
                "subject VARCHAR(255) NOT NULL, " +
                "score INT NOT NULL, " +
                "totalQuestions INT NOT NULL, " +
                "completionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (studentID) REFERENCES Students(studentID), " +
                "UNIQUE KEY (studentID, subject)" +
                ")";
            
            try (PreparedStatement createStmt = conn.prepareStatement(createTableQuery)) {
                createStmt.executeUpdate();
            }
            
            // Insert or update completion record
            String query = 
                "INSERT INTO CompletedQuizzes (studentID, subject, score, totalQuestions) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE score = ?, completionDate = CURRENT_TIMESTAMP";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setString(2, subject);
                pstmt.setInt(3, score);
                pstmt.setInt(4, questions.size());
                pstmt.setInt(5, score);
                
                pstmt.executeUpdate();
                System.out.println("Quiz marked as completed for student ID: " + studentId);
            }
        } catch (SQLException e) {
            System.err.println("Error marking quiz as completed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows a styled result dialog with score information
     */
    private void showResultDialog(int score, int total, double percentage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Quiz Results");
        
        // Determine result message and color based on score
        String resultMessage;
        String resultColor;
        
        if (percentage >= 80) {
            resultMessage = "Excellent!";
            resultColor = SUCCESS_COLOR;
        } else if (percentage >= 60) {
            resultMessage = "Good job!";
            resultColor = PRIMARY_COLOR;
        } else if (percentage >= 40) {
            resultMessage = "Nice try!";
            resultColor = WARNING_COLOR;
        } else {
            resultMessage = "Keep practicing!";
            resultColor = ACCENT_COLOR;
        }
        
        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(resultMessage);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: " + resultColor + ";");
        
        Label scoreLabel = new Label("Your score: " + score + "/" + total);
        scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Label percentageLabel = new Label(String.format("%.2f%%", percentage));
        percentageLabel.setFont(Font.font("System", 16));
        
        ProgressBar resultProgress = new ProgressBar(percentage / 100);
        resultProgress.setPrefWidth(300);
        resultProgress.setStyle("-fx-accent: " + resultColor + ";");
        
        // Add completion message
        Label completionLabel = new Label("Quiz results have been saved!");
        completionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        completionLabel.setStyle("-fx-text-fill: " + SUCCESS_COLOR + ";");
        
        content.getChildren().addAll(titleLabel, scoreLabel, percentageLabel, resultProgress, completionLabel);
        
        // Add buttons
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButtonType);
        
        // Set content
        dialog.getDialogPane().setContent(content);
        
        // Style dialog
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        
        // Show dialog
        dialog.showAndWait();
    }
    
    /**
     * Save answer to the QuizResults table
     */
    private void saveAnswerToDatabase(int questionId, String selectedOption, boolean isCorrect) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            
            // First check if this answer already exists
            String checkQuery = "SELECT COUNT(*) FROM QuizResults WHERE questionID = ? AND studentID = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, questionId);
                checkStmt.setInt(2, studentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Already exists, let's update it
                        String updateQuery = "UPDATE QuizResults SET selectedOption = ?, isCorrect = ?, " +
                                            "submissionDate = CURRENT_TIMESTAMP WHERE questionID = ? AND studentID = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, selectedOption);
                            updateStmt.setBoolean(2, isCorrect);
                            updateStmt.setInt(3, questionId);
                            updateStmt.setInt(4, studentId);
                            updateStmt.executeUpdate();
                        }
                        return;
                    }
                }
            }
            
            // If not exists, insert new record
            String query = "INSERT INTO QuizResults (questionID, studentID, selectedOption, isCorrect) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, questionId);
                pstmt.setInt(2, studentId);
                pstmt.setString(3, selectedOption);
                pstmt.setBoolean(4, isCorrect);
                pstmt.executeUpdate();
                System.out.println("Saved answer for question ID: " + questionId + ", student ID: " + studentId);
            }
        } catch (SQLException e) {
            System.err.println("Error saving answer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows a styled alert dialog
     */
    private void showStyledAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        
        // Add an icon based on alert type
        String iconStyle = "";
        if (alertType == Alert.AlertType.INFORMATION) {
            iconStyle = "-fx-text-fill: " + PRIMARY_COLOR + ";";
        } else if (alertType == Alert.AlertType.WARNING) {
            iconStyle = "-fx-text-fill: " + WARNING_COLOR + ";";
        } else if (alertType == Alert.AlertType.ERROR) {
            iconStyle = "-fx-text-fill: " + ACCENT_COLOR + ";";
        }
        
        Label icon = new Label();
        icon.setStyle("-fx-font-size: 24px; " + iconStyle);
        if (alertType == Alert.AlertType.INFORMATION) {
            icon.setText("✓");
        } else if (alertType == Alert.AlertType.WARNING) {
            icon.setText("⚠");
        } else if (alertType == Alert.AlertType.ERROR) {
            icon.setText("✗");
        }
        
        // Style buttons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 5px;");
        }
        
        alert.showAndWait();
    }

    /**
     * QuizData class to hold data for available quizzes
     */
    public static class QuizData {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty subject;
        private final SimpleIntegerProperty questionCount;
        private final SimpleStringProperty status;
        
        public QuizData(int id, String subject, int questionCount, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.subject = new SimpleStringProperty(subject);
            this.questionCount = new SimpleIntegerProperty(questionCount);
            this.status = new SimpleStringProperty(status);
        }
        
        public int getId() { return id.get(); }
        public String getSubject() { return subject.get(); }
        public int getQuestionCount() { return questionCount.get(); }
        public String getStatus() { return status.get(); }
        
        public void setStatus(String status) { this.status.set(status); }
        
        public SimpleStringProperty statusProperty() { return status; }
    }
}