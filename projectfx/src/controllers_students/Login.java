package controllers_students;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import controllers_Admin.AdminDashboard;
import controllers_Instructors.InstructorDashboard;
import dao.DatabaseConnection;

public class Login extends Application {

    private static User loggedInUser = null;

    @Override
    public void start(Stage primaryStage) {
        VBox leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(40));
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setStyle("-fx-background-color: #ffffff;");
        leftPanel.setMinWidth(400);

        Label logo = new Label("E-Learning");
        logo.setFont(Font.font("Inter", FontWeight.BOLD, 24));

        Label title = new Label("Log in to your account");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 28));

        Label subtitle = new Label("Welcome back! Please enter your details.");
        subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.GRAY);

        Label emailLabel = new Label("Username");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your username");
        emailField.setMinHeight(40);

        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMinHeight(40);

        Button loginButton = new Button("Log in");
        loginButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        loginButton.setMinHeight(40);
        loginButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.setOnAction(e -> validateForm(emailField, passwordField, primaryStage));

        Separator separator = new Separator();
        separator.setMaxWidth(Double.MAX_VALUE);

        Label signUpLink = new Label("Don't have an account? Sign up");
        signUpLink.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        signUpLink.setTextFill(Color.BLUE);
        signUpLink.setOnMouseClicked(e -> new Registration().start(primaryStage));

        leftPanel.getChildren().addAll(
                logo, title, subtitle,
                emailLabel, emailField,
                passwordLabel, passwordField,
                loginButton, separator, signUpLink
        );

        StackPane rightPanel = new StackPane();
        rightPanel.setStyle("-fx-background-color: #f9f9f9;");
        rightPanel.setMinWidth(700);

        ImageView imageView = new ImageView(new Image("file:C:/Users/HP/eclipse-workspace/projectfx/resources/LALA.png"));
        imageView.setFitWidth(700);
        imageView.setPreserveRatio(true);

        rightPanel.getChildren().add(imageView);

        HBox mainContainer = new HBox(leftPanel, rightPanel);

        Scene scene = new Scene(mainContainer, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.setWidth(1100);
        primaryStage.setHeight(700);
        primaryStage.centerOnScreen();
        primaryStage.setResizable(false);
        primaryStage.setTitle("Log In");
        primaryStage.show();
    }

    private void validateForm(TextField usernameField, PasswordField passwordField, Stage primaryStage) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please enter both username and password.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Cannot connect to database. Please try again later.");
                return;
            }

            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("passwordHash");

                // Compare the password using BCrypt
                if (org.mindrot.jbcrypt.BCrypt.checkpw(password, storedHash)) {
                    loggedInUser = new User(
                        rs.getInt("userID"),
                        rs.getString("username"),  
                        rs.getString("email"),
                        storedHash, // storing the hash here (optional)
                        rs.getString("role")
                    );

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Login Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Welcome " + loggedInUser.getUsername() + "! You have successfully logged in as " + loggedInUser.getRole() + ".");
                    alert.showAndWait();

                    primaryStage.close();
                    launchDashboard();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password. Please try again.");
                    passwordField.clear();
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password. Please try again.");
                passwordField.clear();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void launchDashboard() {
        Stage dashboardStage = new Stage();

        try {
            String userRole = loggedInUser.getRole();

            switch (userRole) {
                case "Student":
                    Dashboard_S studentDashboard = new Dashboard_S();
                    studentDashboard.start(dashboardStage);
                    break;

                case "Instructor":
                    InstructorDashboard instructorDashboard = new InstructorDashboard();
                    instructorDashboard.start(dashboardStage);
                    break;

                case "Admin":
                    AdminDashboard adminDashboard = new AdminDashboard();
                    adminDashboard.start(dashboardStage);
                    break;

                default:
                    showAlert(Alert.AlertType.ERROR, "Unknown Role", "User role '" + userRole + "' is not recognized.");
                    new Login().start(new Stage());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
