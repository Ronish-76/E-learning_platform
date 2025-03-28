package controllers_students;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Callback;

public class CoursesPage extends Application {
    private TableView<Course> courseTable;
    private Label lessonTitleLabel;
    private TextArea lessonContentArea;
    private Button lessonActionButton;
    private Button previousButton;
    private Button nextButton;
    private Label courseProgressLabel;
    private ProgressBar overallProgressBar;
    private VBox rightPanel;

    // Color scheme
    private final String PRIMARY_COLOR = "#3498db";
    private final String SECONDARY_COLOR = "#2ecc71";
    private final String ACCENT_COLOR = "#e74c3c";
    private final String BACKGROUND_COLOR = "#f9f9f9";
    private final String CARD_COLOR = "#ffffff";
    private final String TEXT_COLOR = "#2c3e50";
    private final String SUBTEXT_COLOR = "#7f8c8d";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Learning Dashboard");
        
        Scene scene = new Scene(getCoursesPage(), 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Node getView() {
        return getCoursesPage();
    }

    private BorderPane getCoursesPage() {
        BorderPane coursesPage = new BorderPane();
        coursesPage.setPadding(new Insets(20));
        coursesPage.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // Create header with logo and title
        HBox header = createHeader();
        coursesPage.setTop(header);

        // Create main content
        VBox leftPanel = createLeftPanel();
        leftPanel.setPrefWidth(350);
        
        rightPanel = createRightPanel();

        SplitPane splitPane = new SplitPane(leftPanel, rightPanel);
        splitPane.setDividerPositions(0.35);
        splitPane.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        
        VBox contentWrapper = new VBox(10);
        contentWrapper.getChildren().addAll(createStatisticsBar(), splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        coursesPage.setCenter(contentWrapper);
        
        // Create footer
        HBox footer = createFooter();
        coursesPage.setBottom(footer);

        // Select first course by default AFTER all components are initialized
        courseTable.getSelectionModel().selectFirst();

        return coursesPage;
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setAlignment(Pos.CENTER_LEFT);
        
        // This would be your app logo
        Rectangle logoPlaceholder = new Rectangle(40, 40);
        logoPlaceholder.setArcWidth(10);
        logoPlaceholder.setArcHeight(10);
        logoPlaceholder.setFill(Color.web(PRIMARY_COLOR));
        
        Label titleLabel = new Label("Learning Dashboard");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button settingsButton = createIconButton("⚙", "Settings");
        Button profileButton = createIconButton("👤", "Profile");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search courses...");
        searchField.setPrefWidth(200);
        searchField.setStyle("-fx-background-radius: 20; -fx-background-color: #ecf0f1;");
        searchField.setPadding(new Insets(8, 15, 8, 15));
        
        header.getChildren().addAll(logoPlaceholder, titleLabel, spacer, searchField, settingsButton, profileButton);
        
        return header;
    }
    
    private Button createIconButton(String icon, String tooltip) {
        Button button = new Button(icon);
        button.setStyle("-fx-background-color: transparent; -fx-font-size: 18px;");
        button.setTooltip(new Tooltip(tooltip));
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 50%; -fx-font-size: 18px;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-font-size: 18px;"));
        return button;
    }
    
    private HBox createStatisticsBar() {
        HBox statsBar = new HBox(20);
        statsBar.setPadding(new Insets(15));
        statsBar.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 10;");
        statsBar.setEffect(createDropShadow(3));
        statsBar.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Your Progress");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Label statsLabel = new Label("7 Courses • 28 Lessons • 14 Hours");
        statsLabel.setFont(Font.font("Segoe UI", 14));
        statsLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        courseProgressLabel = new Label("Overall: 45%");
        courseProgressLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        courseProgressLabel.setTextFill(Color.web(PRIMARY_COLOR));
        
        overallProgressBar = new ProgressBar(0.45);
        overallProgressBar.setPrefWidth(200);
        overallProgressBar.setStyle("-fx-accent: " + PRIMARY_COLOR + ";");
        
        statsBar.getChildren().addAll(titleLabel, statsLabel, spacer, courseProgressLabel, overallProgressBar);
        
        return statsBar;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 10;");
        leftPanel.setEffect(createDropShadow(5));

        HBox panelHeader = new HBox(10);
        panelHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label panelTitle = new Label("My Courses");
        panelTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        panelTitle.setTextFill(Color.web(TEXT_COLOR));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        ComboBox<String> sortBox = new ComboBox<>(FXCollections.observableArrayList("All Courses", "In Progress", "Completed", "Not Started"));
        sortBox.setValue("All Courses");
        sortBox.setStyle("-fx-background-radius: 5;");
        
        panelHeader.getChildren().addAll(panelTitle, spacer, sortBox);
        
        courseTable = new TableView<>();
        courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        courseTable.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        TableColumn<Course, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        subjectColumn.setCellFactory(createSubjectCellFactory());

        TableColumn<Course, Double> progressColumn = new TableColumn<>("Progress");
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        progressColumn.setCellFactory(createProgressBarCellFactory());
        progressColumn.setPrefWidth(120);

        courseTable.getColumns().addAll(subjectColumn, progressColumn);
        courseTable.setItems(getSampleCourses());
        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateRightPanel(newSelection);
            }
        });
        
        // Removed the call to selectFirst() here

        leftPanel.getChildren().addAll(panelHeader, courseTable);
        VBox.setVgrow(courseTable, Priority.ALWAYS);
        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setStyle("-fx-background-color: " + CARD_COLOR + "; -fx-background-radius: 10;");
        rightPanel.setEffect(createDropShadow(5));

        // Panel header with navigation
        HBox panelHeader = new HBox(10);
        panelHeader.setAlignment(Pos.CENTER_LEFT);
        
        lessonTitleLabel = new Label("Select a course");
        lessonTitleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lessonTitleLabel.setTextFill(Color.web(TEXT_COLOR));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        previousButton = new Button("←");
        previousButton.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 50%; -fx-min-width: 30px; -fx-min-height: 30px;");
        previousButton.setDisable(true);
        previousButton.setOnAction(e -> {
            int currentIndex = courseTable.getSelectionModel().getSelectedIndex();
            if (currentIndex > 0) {
                courseTable.getSelectionModel().select(currentIndex - 1);
            }
        });
        
        nextButton = new Button("→");
        nextButton.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 50%; -fx-min-width: 30px; -fx-min-height: 30px;");
        nextButton.setDisable(true);
        nextButton.setOnAction(e -> {
            int currentIndex = courseTable.getSelectionModel().getSelectedIndex();
            if (currentIndex < courseTable.getItems().size() - 1) {
                courseTable.getSelectionModel().select(currentIndex + 1);
            }
        });
        
        panelHeader.getChildren().addAll(lessonTitleLabel, spacer, previousButton, nextButton);

        // Lesson content section with metadata
        VBox contentSection = new VBox(15);
        contentSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        contentSection.setPadding(new Insets(15));
        
        HBox metadataBar = new HBox(20);
        metadataBar.setAlignment(Pos.CENTER_LEFT);
        
        Label timeLabel = new Label("⏱️ 20 min");
        timeLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        
        Label difficultyLabel = new Label("📊 Intermediate");
        difficultyLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        
        Label authorLabel = new Label("👨‍🏫 Prof. Smith");
        authorLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        
        metadataBar.getChildren().addAll(timeLabel, difficultyLabel, authorLabel);

        lessonContentArea = new TextArea();
        lessonContentArea.setWrapText(true);
        lessonContentArea.setEditable(false);
        lessonContentArea.setPrefHeight(300);
        lessonContentArea.setPromptText("Select a course to view lesson content");
        lessonContentArea.setStyle("-fx-control-inner-background: white; -fx-background-color: white; -fx-border-color: #ecf0f1; -fx-border-radius: 5;");
        lessonContentArea.setFont(Font.font("Segoe UI", 14));
        
        contentSection.getChildren().addAll(metadataBar, lessonContentArea);
        VBox.setVgrow(contentSection, Priority.ALWAYS);

        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button resourcesButton = new Button("Resources");
        resourcesButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY_COLOR + "; -fx-border-color: " + PRIMARY_COLOR + "; -fx-border-radius: 5;");
        resourcesButton.setPrefWidth(120);
        
        Button notesButton = new Button("My Notes");
        notesButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY_COLOR + "; -fx-border-color: " + PRIMARY_COLOR + "; -fx-border-radius: 5;");
        notesButton.setPrefWidth(120);

        lessonActionButton = new Button("Start Lesson");
        lessonActionButton.setPrefWidth(150);
        lessonActionButton.setStyle(
            "-fx-background-color: " + SECONDARY_COLOR + "; " +
            "-fx-text-fill: white; " + 
            "-fx-background-radius: 5; " +
            "-fx-font-weight: bold;"
        );
        lessonActionButton.setOnAction(e -> {
            Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                // Implement lesson action logic here
                if (selectedCourse.getProgress() == 0.0) {
                    // Start the lesson
                    lessonActionButton.setText("Continue Lesson");
                    lessonActionButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");
                } else if (selectedCourse.getProgress() < 1.0) {
                    // Continue the lesson
                    lessonActionButton.setText("Review Lesson");
                    lessonActionButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");
                }
            }
        });
        
        buttonBox.getChildren().addAll(resourcesButton, notesButton, lessonActionButton);

        rightPanel.getChildren().addAll(panelHeader, contentSection, buttonBox);
        return rightPanel;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(15, 0, 0, 0));
        footer.setAlignment(Pos.CENTER_RIGHT);
        
        Label supportLabel = new Label("Need help? Contact support");
        supportLabel.setTextFill(Color.web(SUBTEXT_COLOR));
        supportLabel.setFont(Font.font("Segoe UI", 12));
        
        footer.getChildren().add(supportLabel);
        
        return footer;
    }

    private void updateRightPanel(Course course) {
        // Create fade out transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), rightPanel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.8);
        
        TranslateTransition moveOut = new TranslateTransition(Duration.millis(200), rightPanel);
        moveOut.setByX(20);
        
        fadeOut.setOnFinished(e -> {
            // Update content
            lessonTitleLabel.setText(course.getName());
            lessonContentArea.setText(course.getLessonContent());
            
            Button actionButton = lessonActionButton;
            if (course.getProgress() == 0.0) {
                actionButton.setText("Start Lesson");
                actionButton.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");
            } else if (course.getProgress() < 1.0) {
                actionButton.setText("Continue Lesson");
                actionButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");
            } else {
                actionButton.setText("Review Lesson");
                actionButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");
            }
            
            // Enable buttons
            lessonActionButton.setDisable(false);
            previousButton.setDisable(courseTable.getSelectionModel().getSelectedIndex() == 0);
            nextButton.setDisable(courseTable.getSelectionModel().getSelectedIndex() == courseTable.getItems().size() - 1);
            
            // Create fade in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), rightPanel);
            fadeIn.setFromValue(0.8);
            fadeIn.setToValue(1.0);
            
            TranslateTransition moveIn = new TranslateTransition(Duration.millis(300), rightPanel);
            moveIn.setByX(-20);
            
            fadeIn.play();
            moveIn.play();
        });
        
        fadeOut.play();
        moveOut.play();
    }

    private Callback<TableColumn<Course, Double>, TableCell<Course, Double>> createProgressBarCellFactory() {
        return column -> new TableCell<>() {
            private final ProgressBar progressBar = new ProgressBar();
            private final Label percentLabel = new Label();
            private final HBox container = new HBox(10);
            
            {
                progressBar.setMaxWidth(Double.MAX_VALUE);
                progressBar.setPrefHeight(10);
                progressBar.setStyle("-fx-accent: " + PRIMARY_COLOR + ";");
                HBox.setHgrow(progressBar, Priority.ALWAYS);
                
                percentLabel.setTextFill(Color.web(TEXT_COLOR));
                percentLabel.setFont(Font.font("Segoe UI", 12));
                
                container.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().addAll(progressBar, percentLabel);
            }

            @Override
            protected void updateItem(Double progress, boolean empty) {
                super.updateItem(progress, empty);
                
                if (empty || progress == null) {
                    setGraphic(null);
                } else {
                    progressBar.setProgress(progress);
                    percentLabel.setText(String.format("%.0f%%", progress * 100));
                    setGraphic(container);
                }
            }
        };
    }
    
    private Callback<TableColumn<Course, String>, TableCell<Course, String>> createSubjectCellFactory() {
        return column -> new TableCell<>() {
            private final Label nameLabel = new Label();
            private final Label descLabel = new Label();
            private final VBox container = new VBox(3);
            
            {
                nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                nameLabel.setTextFill(Color.web(TEXT_COLOR));
                
                descLabel.setFont(Font.font("Segoe UI", 12));
                descLabel.setTextFill(Color.web(SUBTEXT_COLOR));
                
                container.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().addAll(nameLabel, descLabel);
                container.setPadding(new Insets(5, 0, 5, 0));
            }

            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                
                if (empty || name == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(name);
                    if (name.equals("Mathematics")) {
                        descLabel.setText("Algebra • Advanced");
                    } else if (name.equals("Science")) {
                        descLabel.setText("Physics • Intermediate");
                    } else if (name.equals("History")) {
                        descLabel.setText("Ancient Rome • Advanced");
                    } else if (name.equals("Literature")) {
                        descLabel.setText("Shakespeare • Intermediate");
                    } else if (name.equals("Computer Science")) {
                        descLabel.setText("Programming • Intermediate");
                    } else if (name.equals("Art")) {
                        descLabel.setText("Impressionism • Beginner");
                    } else if (name.equals("Music")) {
                        descLabel.setText("Theory • Beginner");
                    }
                    setGraphic(container);
                }
            }
        };
    }

    private ObservableList<Course> getSampleCourses() {
        return FXCollections.observableArrayList(
            new Course("Mathematics", 0.75, "This lesson covers advanced algebraic equations and their applications in real-world scenarios. We'll explore linear equations, quadratic formulas, and how to solve complex problems step by step.\n\nLearning Objectives:\n• Understand the fundamental properties of algebraic expressions\n• Learn techniques for solving quadratic equations\n• Apply algebraic concepts to word problems\n• Develop problem-solving strategies using algebraic methods"),
            new Course("Science", 0.45, "Introduction to the scientific method and Newton's laws of motion. This lesson explores how scientists investigate natural phenomena and the fundamental principles that govern motion.\n\nTopics covered:\n• The steps of the scientific method\n• Newton's First Law: Inertia\n• Newton's Second Law: F=ma\n• Newton's Third Law: Action and Reaction\n• Practical applications of Newton's laws in everyday situations"),
            new Course("History", 0.90, "The rise and fall of the Roman Empire. This comprehensive lesson examines the historical factors that contributed to Rome's dominance and eventual decline.\n\nKey periods covered:\n• The founding of Rome and early republican period\n• The transition from Republic to Empire\n• The Pax Romana and height of imperial power\n• Economic and military challenges of the late empire\n• The division and eventual fall of Rome"),
            new Course("Literature", 0.30, "Analysis of Shakespeare's plays, focusing on Hamlet. This lesson examines the themes, characters, and literary devices in one of the most famous tragedies ever written.\n\nAreas of focus:\n• Historical context of Elizabethan theater\n• Character analysis: Hamlet, Ophelia, Claudius, and Gertrude\n• Major themes: revenge, madness, mortality\n• Shakespeare's use of soliloquy and theatrical devices\n• The play's influence on modern literature"),
            new Course("Computer Science", 0.60, "Basic programming concepts and data structures. This lesson introduces fundamental programming principles and common data structures used in software development.\n\nConcepts covered:\n• Variables, data types, and operations\n• Control structures: conditionals and loops\n• Arrays, lists, and dictionaries\n• Introduction to algorithms and complexity\n• Problem-solving approaches in programming"),
            new Course("Art", 0.15, "Exploring impressionism and its major artists. This lesson examines the revolutionary art movement that changed how we perceive visual representation.\n\nHighlights:\n• Origins of Impressionism in 19th century France\n• Key artists: Monet, Renoir, Degas, and Morisot\n• Techniques and characteristics of Impressionist painting\n• The movement's impact on subsequent art developments\n• Analysis of iconic Impressionist works"),
            new Course("Music", 0.0, "Introduction to music theory and composition. This foundational lesson covers the basic elements needed to understand and create music.\n\nTopics include:\n• Musical notation and reading sheet music\n• Scales, keys, and chord structures\n• Rhythm, tempo, and time signatures\n• Basic compositional techniques\n• Practical exercises for beginning musicians")
        );
    }

    private DropShadow createDropShadow(double radius) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(radius);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(1);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        return dropShadow;
    }

    public static class Course {
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty progress;
        private final String lessonContent;

        public Course(String name, double progress, String lessonContent) {
            this.name = new SimpleStringProperty(name);
            this.progress = new SimpleDoubleProperty(progress);
            this.lessonContent = lessonContent;
        }

        public String getName() { return name.get(); }
        public double getProgress() { return progress.get(); }
        public String getLessonContent() { return lessonContent; }
    }
}