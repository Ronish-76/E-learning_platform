package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dao.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class Quiz {
    private List<Question> questions;
    private int currentQuestionIndex;
    private int score;
    private String subject;
    
    // UI components
    private BorderPane mainLayout;
    private Label questionLabel, scoreLabel, subjectLabel, questionCountLabel, timerLabel;
    private ToggleButton optionA, optionB, optionC, optionD;
    private ToggleGroup answerGroup;
    private Button submitButton, nextButton, startNewQuizButton;
    private ComboBox<String> subjectComboBox;
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
        this.currentQuestionIndex = 0;
        this.score = 0;
        setupUI();
    }
    
    /**
     * Constructor to create a new quiz with questions from a specific subject
     * @param subject The subject for which to fetch questions
     */
    public Quiz(String subject) {
        this.subject = subject;
        this.questions = new ArrayList<>();
        this.currentQuestionIndex = 0;
        this.score = 0;
        
        loadQuestionsFromDatabase();
        
        if (!questions.isEmpty()) {
            // Shuffle questions for randomized order
            Collections.shuffle(questions);
        }
        
        setupUI();
    }
    
    /**
     * Sets up the UI components with enhanced styling
     */
    private void setupUI() {
        // Create main layout with background color
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Create card-like container for content
        VBox contentCard = new VBox(20);
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
        
        // Subject selection with styled components
        VBox subjectSelectionBox = createSubjectSelection();
        
        // Quiz information bar
        HBox infoBox = createInfoBar();
        
        // Progress tracking
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: " + PRIMARY_COLOR + ";");
        
        // Question display with enhanced styling
        questionLabel = new Label("Select a subject to start the quiz");
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
        
        // Control buttons with enhanced styling
        HBox buttonBox = createButtonBox();
        
        // Add components to the content card
        contentCard.getChildren().addAll(
                headerBox,
                subjectSelectionBox,
                infoBox,
                progressBar,
                questionLabel,
                optionsBox,
                buttonBox
        );
        
        // Set margins for the content card
        BorderPane.setMargin(contentCard, new Insets(20));
        mainLayout.setCenter(contentCard);
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
     * Creates a styled subject selection area
     */
    private VBox createSubjectSelection() {
        Label selectSubjectLabel = new Label("Select Subject:");
        selectSubjectLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        subjectComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Mathematics", "Science", "History", "English"));
        subjectComboBox.setPrefWidth(200);
        subjectComboBox.setPromptText("Choose a subject");
        subjectComboBox.setStyle("-fx-background-color: white; " +
                               "-fx-border-color: " + PRIMARY_COLOR + "; " +
                               "-fx-border-radius: 5px;");
        
        Button startQuizButton = new Button("Start Quiz");
        startQuizButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; " +
                               "-fx-text-fill: white; " +
                               "-fx-font-weight: bold; " +
                               "-fx-background-radius: 5px;");
        startQuizButton.setPrefWidth(120);
        startQuizButton.setOnAction(e -> startQuiz(subjectComboBox.getValue()));
        
        // Button hover effect
        startQuizButton.setOnMouseEntered(e -> 
            startQuizButton.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; " +
                                   "-fx-text-fill: white; " +
                                   "-fx-font-weight: bold; " +
                                   "-fx-background-radius: 5px;"));
        
        startQuizButton.setOnMouseExited(e -> 
            startQuizButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; " +
                                   "-fx-text-fill: white; " +
                                   "-fx-font-weight: bold; " +
                                   "-fx-background-radius: 5px;"));
        
        HBox selectionRow = new HBox(15, selectSubjectLabel, subjectComboBox, startQuizButton);
        selectionRow.setAlignment(Pos.CENTER_LEFT);
        
        VBox selectionBox = new VBox(10, selectionRow);
        selectionBox.setStyle("-fx-padding: 10px; " +
                            "-fx-border-color: #e0e0e0; " +
                            "-fx-border-width: 0 0 1 0;");
        selectionBox.setPadding(new Insets(0, 0, 15, 0));
        
        return selectionBox;
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
        
        scoreLabel = new Label("Score: 0");
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
     * Creates a button box with styled control buttons
     */
    private HBox createButtonBox() {
        submitButton = createStyledButton("Submit Answer", PRIMARY_COLOR);
        submitButton.setDisable(true);
        submitButton.setOnAction(e -> checkAnswer());
        
        nextButton = createStyledButton("Next Question", SECONDARY_COLOR);
        nextButton.setDisable(true);
        nextButton.setOnAction(e -> loadNextQuestion());
        
        startNewQuizButton = createStyledButton("Start New Quiz", WARNING_COLOR);
        startNewQuizButton.setDisable(true);
        startNewQuizButton.setOnAction(e -> resetQuiz());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox buttonBox = new HBox(15, submitButton, nextButton, spacer, startNewQuizButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        
        return buttonBox;
    }
    
    /**
     * Creates a styled button with hover effects
     */
    private Button createStyledButton(String text, String baseColor) {
        Button button = new Button(text);
        button.setPrefWidth(150);
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
     * Returns the view component to be used in the dashboard
     * @return JavaFX Parent component
     */
    public Parent getView() {
        return mainLayout;
    }
    
    /**
     * Loads questions from the database for the specified subject
     */
    private void loadQuestionsFromDatabase() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            // Get connection to database
            connection = DatabaseConnection.getConnection();
            
            // Prepare SQL query to fetch questions for the specified subject
            String query = "SELECT * FROM quiz_questions WHERE subject = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, subject);
            
            // Execute query
            resultSet = statement.executeQuery();
            
            // Process results
            while (resultSet.next()) {
                String questionText = resultSet.getString("question");
                String optionA = resultSet.getString("option_a");
                String optionB = resultSet.getString("option_b");
                String optionC = resultSet.getString("option_c");
                String optionD = resultSet.getString("option_d");
                String correctOption = resultSet.getString("correct_option");
                
                Question question = new Question(questionText, optionA, optionB, optionC, optionD, correctOption);
                questions.add(question);
            }
            
            System.out.println("Loaded " + questions.size() + " questions for " + subject);
            
        } catch (SQLException e) {
            System.err.println("Error loading questions from database:");
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database resources:");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Starts a new quiz with the selected subject
     * @param subject The subject for the quiz
     */
    private void startQuiz(String subject) {
        if (subject == null || subject.isEmpty()) {
            showStyledAlert(Alert.AlertType.WARNING, "Warning", "Please select a subject");
            return;
        }
        
        // Set subject and reset quiz
        this.subject = subject;
        this.questions.clear();
        this.currentQuestionIndex = 0;
        this.score = 0;
        
        // Load questions
        loadQuestionsFromDatabase();
        
        if (questions.isEmpty()) {
            showStyledAlert(Alert.AlertType.ERROR, "Error", "No questions available for the selected subject");
            return;
        }
        
        // Shuffle questions
        Collections.shuffle(questions);
        
        // Update UI with animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), questionLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // Update UI elements
            subjectLabel.setText("Subject: " + subject);
            scoreLabel.setText("Score: 0");
            
            // Load first question
            loadNextQuestion();
            
            // Fade back in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), questionLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
        
        // Update progress bar
        progressBar.setProgress(0);
        
        // Enable/disable buttons
        submitButton.setDisable(false);
        nextButton.setDisable(true);
        startNewQuizButton.setDisable(false);
    }
    
    /**
     * Checks if there are more questions available
     * @return True if there are more questions, false otherwise
     */
    public boolean hasNextQuestion() {
        return currentQuestionIndex < questions.size();
    }
    
    /**
     * Gets the current question
     * @return The current question object
     */
    public Question getCurrentQuestion() {
        if (hasNextQuestion()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }
    
    /**
     * Loads the next question with animation
     */
    private void loadNextQuestion() {
        // Reset selection
        answerGroup.selectToggle(null);
        
        if (hasNextQuestion()) {
            // Get current question
            currentQuestion = getCurrentQuestion();
            
            // Update question count
            questionCountLabel.setText("Question: " + (currentQuestionIndex + 1) + 
                                     " of " + questions.size());
            
            // Update progress bar
            double progress = (double) currentQuestionIndex / questions.size();
            progressBar.setProgress(progress);
            
            // Display question and options with animation
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
                
                // Fade back in
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), questionLabel);
                fadeIn.setFromValue(0.3);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
            
            // Enable submit button and disable next button
            submitButton.setDisable(false);
            nextButton.setDisable(true);
        } else {
            // Quiz completed
            questionLabel.setText("Quiz completed!");
            
            // Clear options
            optionA.setText("");
            optionB.setText("");
            optionC.setText("");
            optionD.setText("");
            
            // Update progress bar to show completion
            progressBar.setProgress(1.0);
            progressBar.setStyle("-fx-accent: " + SUCCESS_COLOR + ";");
            
            // Disable buttons
            submitButton.setDisable(true);
            nextButton.setDisable(true);
            
            // Show final score
            double percentage = questions.isEmpty() ? 0 : (double) score / questions.size() * 100;
            showResultDialog(score, questions.size(), percentage);
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
        
        content.getChildren().addAll(titleLabel, scoreLabel, percentageLabel, resultProgress);
        
        // Add buttons
        ButtonType tryAgainButtonType = new ButtonType("Try Again", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(tryAgainButtonType, closeButtonType);
        
        // Set content
        dialog.getDialogPane().setContent(content);
        
        // Style dialog
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        
        // Handle result
        dialog.showAndWait().ifPresent(result -> {
            if (result == tryAgainButtonType) {
                resetQuiz();
            }
        });
    }
    
    /**
     * Checks the submitted answer with visual feedback
     */
    private void checkAnswer() {
        Toggle selectedToggle = answerGroup.getSelectedToggle();
        
        if (selectedToggle == null) {
            showStyledAlert(Alert.AlertType.WARNING, "Warning", "Please select an answer");
            return;
        }
        
        ToggleButton selectedButton = (ToggleButton) selectedToggle;
        
        // Get selected answer text
        String selectedAnswer = "";
        if (selectedButton == optionA) {
            selectedAnswer = currentQuestion.getOptionA();
        } else if (selectedButton == optionB) {
            selectedAnswer = currentQuestion.getOptionB();
        } else if (selectedButton == optionC) {
            selectedAnswer = currentQuestion.getOptionC();
        } else if (selectedButton == optionD) {
            selectedAnswer = currentQuestion.getOptionD();
        }
        
        // Check answer
        boolean isCorrect = selectedAnswer.equals(currentQuestion.getCorrectAnswer());
        if (isCorrect) {
            score++;
            // Highlight correct answer in green
            selectedButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + "33; " + // 20% opacity
                                 "-fx-border-color: " + SUCCESS_COLOR + "; " +
                                 "-fx-border-radius: 5px; " +
                                 "-fx-background-radius: 5px;");
        } else {
            // Highlight wrong answer in red
            selectedButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "33; " + // 20% opacity
                                 "-fx-border-color: " + ACCENT_COLOR + "; " +
                                 "-fx-border-radius: 5px; " +
                                 "-fx-background-radius: 5px;");
            
            // Find and highlight correct answer
            ToggleButton correctButton = findCorrectAnswerButton(currentQuestion.getCorrectAnswer());
            if (correctButton != null) {
                correctButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + "33; " + // 20% opacity
                                    "-fx-border-color: " + SUCCESS_COLOR + "; " +
                                    "-fx-border-radius: 5px; " +
                                    "-fx-background-radius: 5px;");
            }
        }
        
        // Move to next question
        currentQuestionIndex++;
        
        // Update score with animation
        FadeTransition scoreFade = new FadeTransition(Duration.millis(200), scoreLabel);
        scoreFade.setFromValue(1.0);
        scoreFade.setToValue(0.3);
        scoreFade.setOnFinished(e -> {
            scoreLabel.setText("Score: " + score);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), scoreLabel);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        scoreFade.play();
        
        // Show feedback
        if (isCorrect) {
            showStyledAlert(Alert.AlertType.INFORMATION, "Correct!", "Your answer is correct!");
        } else {
            showStyledAlert(Alert.AlertType.INFORMATION, "Incorrect", 
                         "Your answer is incorrect. The correct answer is: " + currentQuestion.getCorrectAnswer());
        }
        
        // Enable/disable buttons
        submitButton.setDisable(true);
        nextButton.setDisable(false);
    }
    
    /**
     * Finds the toggle button containing the correct answer
     */
    private ToggleButton findCorrectAnswerButton(String correctAnswer) {
        if (optionA.getText().contains(correctAnswer)) return optionA;
        if (optionB.getText().contains(correctAnswer)) return optionB;
        if (optionC.getText().contains(correctAnswer)) return optionC;
        if (optionD.getText().contains(correctAnswer)) return optionD;
        return null;
    }
    
    /**
     * Resets the quiz with animation
     */
    private void resetQuiz() {
        // Reset UI with animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), questionLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);
        fadeOut.setOnFinished(e -> {
            // Reset UI
            questionLabel.setText("Select a subject to start the quiz");
            subjectLabel.setText("");
            questionCountLabel.setText("");
            scoreLabel.setText("Score: 0");
            
            // Clear options
            optionA.setText("");
            optionB.setText("");
            optionC.setText("");
            optionD.setText("");
            
            // Reset selection
            answerGroup.selectToggle(null);
            
            // Reset progress bar
            progressBar.setProgress(0);
            progressBar.setStyle("-fx-accent: " + PRIMARY_COLOR + ";");
            
            // Fade back in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), questionLabel);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
        
        // Reset quiz state
        this.questions.clear();
        this.currentQuestionIndex = 0;
        this.score = 0;
        this.subject = null;
        
     // Disable buttons
        submitButton.setDisable(true);
        nextButton.setDisable(true);
        startNewQuizButton.setDisable(true);
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
     * Gets the current score
     * @return The current score
     */
    public int getScore() {
        return score;
    }
    
    /**
     * Gets the total number of questions
     * @return The total number of questions
     */
    public int getTotalQuestions() {
        return questions.size();
    }
    
    /**
     * Gets the current question number (1-based index)
     * @return The current question number
     */
    public int getCurrentQuestionNumber() {
        return currentQuestionIndex + 1;
    }
    
    /**
     * Gets the subject of the quiz
     * @return The subject
     */
    public String getSubject() {
        return subject;
    }
}