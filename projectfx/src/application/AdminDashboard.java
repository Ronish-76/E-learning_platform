package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AdminDashboard extends Application {

    private VBox sidebar;
    private VBox messagePanel;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = createContent();
        Scene scene = new Scene(root, 1000, 600);

        // Apply external CSS file
        scene.getStylesheets().add(getClass().getResource("/admin_style.css").toExternalForm());

        primaryStage.setTitle("Admin Dashboard");
        primaryStage.setScene(scene);

        // Add resize listener to adjust sidebar and message panel
        primaryStage.widthProperty().addListener((obs, oldWidth, newWidth) -> adjustLayout(newWidth.doubleValue()));

        primaryStage.show();
    }

    private void adjustLayout(double width) {
        if (width <= 800) {
            sidebar.setPrefWidth(150); // Smaller sidebar for narrow screens
            messagePanel.setPrefWidth(200); // Smaller message panel
        } else {
            sidebar.setPrefWidth(200); // Default sidebar width
            messagePanel.setPrefWidth(300); // Default message panel width
        }
    }

    private BorderPane createContent() {
        // Root layout
        BorderPane root = new BorderPane();

        // Top Bar
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Sidebar (Left)
        sidebar = createSidebar();
        root.setLeft(sidebar);

        // Center (Project Cards)
        GridPane projectGrid = createProjectGrid();
        root.setCenter(projectGrid);

        // Right (Client Messages)
        messagePanel = createMessagePanel();
        root.setRight(messagePanel);

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("DASHBOARD");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search");

        Button adminButton = new Button("ADMIN");

        topBar.getChildren().addAll(title, searchField, adminButton);
        return topBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200); // Default width
        sidebar.setPadding(new Insets(10));

        Label homeLabel = new Label("HOME");
        homeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox stats = new VBox(5);
        stats.getChildren().addAll(
            new Label("45 In Progress"),
            new Label("24 Upcoming"),
            new Label("62 Total Projects")
        );

        DatePicker datePicker = new DatePicker();

        sidebar.getChildren().addAll(homeLabel, stats, datePicker);
        return sidebar;
    }

    private GridPane createProjectGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Example project cards
        grid.add(createProjectCard("Web Designing", "Prototyping", 0.6, "2 Days Left"), 0, 0);
        grid.add(createProjectCard("Testing", "Prototyping", 0.5, "2 Days Left"), 1, 0);
        grid.add(createProjectCard("SVG Animations", "Prototyping", 0.8, "2 Days Left"), 2, 0);
        grid.add(createProjectCard("UI Development", "Prototyping", 0.2, "2 Days Left"), 0, 1);
        grid.add(createProjectCard("Data Analysis", "Prototyping", 0.6, "2 Days Left"), 1, 1);
        grid.add(createProjectCard("Web Designing", "Prototyping", 0.4, "2 Days Left"), 2, 1);

        return grid;
    }

    private VBox createProjectCard(String title, String subtitle, double progress, String daysLeft) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");  

        Label dateLabel = new Label("December 10, 2020");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label(subtitle);
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setStyle("-fx-background-color: #e0e0e0;");
        Label progressLabel = new Label(String.format("%.0f%%", progress * 100));

        Label daysLabel = new Label(daysLeft);

        card.getChildren().addAll(dateLabel, titleLabel, subtitleLabel, progressBar, progressLabel, daysLabel);
        return card;
    }

    private VBox createMessagePanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(300);
        panel.setPadding(new Insets(10));

        Label title = new Label("Client Messages");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox messages = new VBox(10);
        messages.getChildren().addAll(
            createMessage("Stephanie", "I got your first assignment. It was quite good. We can continue with the next assignment.", "Dec, 12"),
            createMessage("Mark", "Hey, can you tell me for your progress of project? I'm waiting for you.", "Dec, 12"),
            createMessage("David", "Awesome! I like it. We can schedule a meeting for the next one.", "Dec, 12"),
            createMessage("Jessica", "I am really impressed! Can’t wait to see the final result.", "Dec, 11")
        );

        panel.getChildren().addAll(title, messages);
        return panel;
    }

    private HBox createMessage(String name, String message, String date) {
        HBox messageBox = new HBox(10);

        javafx.scene.shape.Circle avatar = new javafx.scene.shape.Circle(15, Color.GRAY);
        avatar.setStroke(Color.BLACK);
        avatar.setStrokeWidth(1);

        VBox textBox = new VBox(5);
        Label nameLabel = new Label(name);
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        Label dateLabel = new Label(date);

        textBox.getChildren().addAll(nameLabel, messageLabel, dateLabel);
        messageBox.getChildren().addAll(avatar, textBox);
        return messageBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
