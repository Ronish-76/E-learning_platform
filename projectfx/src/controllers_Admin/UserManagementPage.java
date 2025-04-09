package controllers_Admin;

import dao.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

/**
 * User Management page for admin operations on users
 * with database integration and enhanced UI
 */
public class UserManagementPage {
    // UI components that need class-level access
    private TableView<UserData> usersTable;
    private TextField searchField;
    private ComboBox<String> roleFilter;
    private ObservableList<UserData> masterData = FXCollections.observableArrayList();
    private FilteredList<UserData> filteredData;

    // Color constants for UI
    private static final String PRIMARY_COLOR = "#3498db";
    private static final String SECONDARY_COLOR = "#2ecc71";
    private static final String DANGER_COLOR = "#e74c3c";
    private static final String BACKGROUND_COLOR = "#f5f7fa";
    private static final String CARD_COLOR = "#ffffff";
    private static final String TEXT_COLOR = "#2c3e50";
    private static final String SUBTEXT_COLOR = "#7f8c8d";

    /**
     * Creates and returns the main view for User Management
     */
    public Node getView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setBackground(new Background(new BackgroundFill(
                Color.web(BACKGROUND_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));

        // Page header
        BorderPane header = createHeader();

        // Stats cards
        HBox statsCards = createStatsCards();

        // Search controls in a card
        VBox searchCard = createSearchCard();

        // Users table in a card
        VBox tableCard = createTableCard();
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        // Add everything to the main view
        view.getChildren().addAll(header, statsCards, searchCard, tableCard);

        // Load data from database
        loadUsersFromDatabase();

        return view;
    }

    /**
     * Creates the page header with title and actions
     */
    private BorderPane createHeader() {
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(0, 0, 10, 0));

        // Left side - title
        Label title = new Label("User Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setTextFill(Color.web(TEXT_COLOR));

        // Right side - add user button
        Button addUserBtn = new Button("Add New User");
        addUserBtn.setFont(Font.font("Arial", 14));
        addUserBtn.setPadding(new Insets(8, 15, 8, 15));
        addUserBtn.setStyle(
                "-fx-background-color: " + PRIMARY_COLOR + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 4;"
        );
        addUserBtn.setOnAction(e -> showAddUserDialog());

        header.setLeft(title);
        header.setRight(addUserBtn);

        return header;
    }

    /**
     * Creates statistics cards showing user counts
     */
    private HBox createStatsCards() {
        HBox statsCards = new HBox(15);
        statsCards.setPadding(new Insets(0, 0, 10, 0));

        // Total users stats card
        VBox totalUsersCard = createStatsCard("Total Users", "0", PRIMARY_COLOR);

        // Students stats card
        VBox studentsCard = createStatsCard("Students", "0", "#f39c12");

        // Instructors stats card
        VBox instructorsCard = createStatsCard("Instructors", "0", SECONDARY_COLOR);

        // Admins stats card
        VBox adminsCard = createStatsCard("Admins", "0", DANGER_COLOR);

        statsCards.getChildren().addAll(totalUsersCard, studentsCard, instructorsCard, adminsCard);

        return statsCards;
    }

    /**
     * Creates an individual statistics card
     */
    private VBox createStatsCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setPrefWidth(180);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setBackground(new Background(new BackgroundFill(
                Color.web(CARD_COLOR), new CornerRadii(8), Insets.EMPTY)));

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        card.setEffect(dropShadow);

        // Create icon circle
        StackPane iconContainer = new StackPane();
        Circle circle = new Circle(20);
        circle.setFill(Color.web(color).deriveColor(1, 1, 1, 0.2));
        Label iconLabel = new Label(label.substring(0, 1).toUpperCase());
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        iconLabel.setTextFill(Color.web(color));
        iconContainer.getChildren().addAll(circle, iconLabel);

        // Create stats value and label
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(TEXT_COLOR));
        valueLabel.setId(label.toLowerCase().replace(" ", "_") + "_count");

        Label nameLabel = new Label(label);
        nameLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        nameLabel.setTextFill(Color.web(SUBTEXT_COLOR));

        HBox contentBox = new HBox(15);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        VBox textBox = new VBox(2);
        textBox.getChildren().addAll(valueLabel, nameLabel);
        contentBox.getChildren().addAll(iconContainer, textBox);

        card.getChildren().add(contentBox);

        return card;
    }

    /**
     * Creates the search controls card
     */
    private VBox createSearchCard() {
        VBox searchCard = new VBox(15);
        searchCard.setPadding(new Insets(15));
        searchCard.setBackground(new Background(new BackgroundFill(
                Color.web(CARD_COLOR), new CornerRadii(8), Insets.EMPTY)));

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        searchCard.setEffect(dropShadow);

        // Label for search section
        Label searchLabel = new Label("Search Users");
        searchLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        searchLabel.setTextFill(Color.web(TEXT_COLOR));

        // Search controls
        HBox searchControls = new HBox(15);
        searchControls.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by username or email...");
        searchField.setPrefWidth(300);
        searchField.setPadding(new Insets(8));
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterData();
        });

        roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("All Roles", "Admin", "Instructor", "Student");
        roleFilter.setValue("All Roles");
        roleFilter.setPrefWidth(150);
        roleFilter.setPadding(new Insets(2));
        roleFilter.valueProperty().addListener((obs, oldValue, newValue) -> {
            filterData();
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setPadding(new Insets(8, 15, 8, 15));
        clearBtn.setStyle(
                "-fx-background-color: #ecf0f1;" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-background-radius: 4;"
        );
        clearBtn.setOnAction(e -> {
            searchField.clear();
            roleFilter.setValue("All Roles");
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addInstructorBtn = new Button("Add Instructor");
        addInstructorBtn.setPadding(new Insets(8, 15, 8, 15));
        addInstructorBtn.setStyle(
                "-fx-background-color: " + SECONDARY_COLOR + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 4;"
        );
        addInstructorBtn.setOnAction(e -> showAddInstructorDialog());

        searchControls.getChildren().addAll(searchField, roleFilter, clearBtn, spacer, addInstructorBtn);

        searchCard.getChildren().addAll(searchLabel, searchControls);

        return searchCard;
    }

    /**
     * Creates the table card with users data
     */
    private VBox createTableCard() {
        VBox tableCard = new VBox(15);
        tableCard.setPadding(new Insets(15));
        tableCard.setBackground(new Background(new BackgroundFill(
                Color.web(CARD_COLOR), new CornerRadii(8), Insets.EMPTY)));

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        tableCard.setEffect(dropShadow);

        // Label for users table
        Label tableLabel = new Label("User List");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tableLabel.setTextFill(Color.web(TEXT_COLOR));

        // Create table
        usersTable = createUsersTable();
        VBox.setVgrow(usersTable, Priority.ALWAYS);

        // Create pagination controls
        HBox paginationControls = createPaginationControls();

        tableCard.getChildren().addAll(tableLabel, usersTable, paginationControls);

        return tableCard;
    }

    /**
     * Creates the users table
     */
    private TableView<UserData> createUsersTable() {
        TableView<UserData> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ID column
        TableColumn<UserData, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        idCol.setMinWidth(50);
        idCol.setMaxWidth(80);

        // Username column
        TableColumn<UserData, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        usernameCol.setMinWidth(100);

        // Email column
        TableColumn<UserData, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        emailCol.setMinWidth(200);

        // Role column with color coding
        TableColumn<UserData, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        roleCol.setMinWidth(100);
        roleCol.setCellFactory(column -> new TableCell<UserData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(5);
                    container.setAlignment(Pos.CENTER_LEFT);
                    Circle roleIndicator = new Circle(5);
                    if (item.equalsIgnoreCase("Admin")) {
                        roleIndicator.setFill(Color.web(DANGER_COLOR));
                    } else if (item.equalsIgnoreCase("Instructor")) {
                        roleIndicator.setFill(Color.web(SECONDARY_COLOR));
                    } else if (item.equalsIgnoreCase("Student")) {
                        roleIndicator.setFill(Color.web("#f39c12"));
                    }
                    Label roleLabel = new Label(item);
                    container.getChildren().addAll(roleIndicator, roleLabel);
                    setGraphic(container);
                }
            }
        });

        // Actions column
        TableColumn<UserData, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setMinWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final HBox actionContainer = new HBox(8);
            private final Button editBtn = new Button("Edit");
            private final Button resetPassBtn = new Button("Reset Password");
            {
                actionContainer.setAlignment(Pos.CENTER);
                editBtn.setStyle(
                        "-fx-background-color: " + PRIMARY_COLOR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;"
                );
                resetPassBtn.setStyle(
                        "-fx-background-color: #f39c12;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 4;"
                );

                // Set button actions
                editBtn.setOnAction(e -> {
                    UserData user = getTableRow().getItem();
                    if (user != null) {
                        showEditUserDialog(user);
                    }
                });
                resetPassBtn.setOnAction(e -> {
                    UserData user = getTableRow().getItem();
                    if (user != null) {
                        showResetPasswordDialog(user);
                    }
                });
                actionContainer.getChildren().addAll(editBtn, resetPassBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionContainer);
            }
        });

        table.getColumns().addAll(idCol, usernameCol, emailCol, roleCol, actionsCol);

        // Initialize filtered list
        filteredData = new FilteredList<>(masterData, p -> true);
        table.setItems(filteredData);

        return table;
    }

    /**
     * Creates pagination controls
     */
    private HBox createPaginationControls() {
        HBox pagination = new HBox(10);
        pagination.setPadding(new Insets(10, 0, 0, 0));
        pagination.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("◄ Previous");
        prevBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + PRIMARY_COLOR + ";" +
                "-fx-border-color: " + PRIMARY_COLOR + ";" +
                "-fx-border-radius: 4;"
        );
        prevBtn.setPadding(new Insets(5, 15, 5, 15));

        Label pageInfo = new Label("Page 1 of 1");
        pageInfo.setFont(Font.font("Arial", 14));
        pageInfo.setTextFill(Color.web(TEXT_COLOR));

        Button nextBtn = new Button("Next ►");
        nextBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + PRIMARY_COLOR + ";" +
                "-fx-border-color: " + PRIMARY_COLOR + ";" +
                "-fx-border-radius: 4;"
        );
        nextBtn.setPadding(new Insets(5, 15, 5, 15));

        pagination.getChildren().addAll(prevBtn, pageInfo, nextBtn);

        // Disable buttons for now since pagination is not fully implemented
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);

        return pagination;
    }

    /**
     * Filter table data based on search text and role filter
     */
    private void filterData() {
        String searchText = searchField.getText().toLowerCase();
        String role = roleFilter.getValue();

        filteredData.setPredicate(user -> {
            // If text field is empty and role is "All Roles", show all users
            if ((searchText == null || searchText.isEmpty()) && 
                (role == null || role.equals("All Roles"))) {
                return true;
            }

            // Filter by role if needed
            if (role != null && !role.equals("All Roles") && 
                !user.getRole().equalsIgnoreCase(role)) {
                return false;
            }

            // Filter by search text
            if (searchText != null && !searchText.isEmpty()) {
                return user.getUsername().toLowerCase().contains(searchText) ||
                       user.getEmail().toLowerCase().contains(searchText);
            }

            return true;
        });

        updateStatsCounts();
    }

    /**
     * Load users from database using DatabaseConnection class
     */
    private void loadUsersFromDatabase() {
        masterData.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT userID, username, email, role FROM Users";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    UserData user = new UserData(
                            rs.getString("userID"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("role")
                    );
                    masterData.add(user);
                }
            }
            updateStatsCounts();
        } catch (SQLException e) {
            showErrorDialog("Database Error", "Failed to load users from database", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the counts in the stats cards
     */
    private void updateStatsCounts() {
        int totalCount = 0;
        int studentCount = 0;
        int instructorCount = 0;
        int adminCount = 0;

        for (UserData user : masterData) {
            totalCount++;
            switch (user.getRole().toLowerCase()) {
                case "student":
                    studentCount++;
                    break;
                case "instructor":
                    instructorCount++;
                    break;
                case "admin":
                    adminCount++;
                    break;
            }
        }

        // Update the labels using the ID we assigned earlier
        Scene scene = usersTable.getScene();
        if (scene != null) {
            ((Label) scene.lookup("#total_users_count")).setText(String.valueOf(totalCount));
            ((Label) scene.lookup("#students_count")).setText(String.valueOf(studentCount));
            ((Label) scene.lookup("#instructors_count")).setText(String.valueOf(instructorCount));
            ((Label) scene.lookup("#admins_count")).setText(String.valueOf(adminCount));
        }
    }

    /**
     * Show dialog to add a new user
     */
    private void showAddUserDialog() {
        Dialog<UserData> dialog = new Dialog<>();
        dialog.setTitle("Add New User");

        // Style dialog header
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(15));
        header.setBackground(new Background(new BackgroundFill(
                Color.web(PRIMARY_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        Label headerLabel = new Label("Add New User");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.WHITE);
        header.setCenter(headerLabel);

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(250);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Student", "Instructor", "Admin");
        roleCombo.setValue("Student");

        // Add labels with styling
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        usernameLabel.setTextFill(Color.web(TEXT_COLOR));

        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        emailLabel.setTextFill(Color.web(TEXT_COLOR));

        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        passwordLabel.setTextFill(Color.web(TEXT_COLOR));

        Label roleLabel = new Label("Role:");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        roleLabel.setTextFill(Color.web(TEXT_COLOR));

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(roleLabel, 0, 3);
        grid.add(roleCombo, 1, 3);

        // Create content pane with header and form
        VBox content = new VBox();
        content.getChildren().addAll(header, grid);
        dialog.getDialogPane().setContent(content);

        // Request focus on the username field by default
        usernameField.requestFocus();

        // Convert the result to a UserData when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validate input
                    String username = usernameField.getText().trim();
                    String email = emailField.getText().trim();
                    String password = passwordField.getText();
                    String role = roleCombo.getValue();

                    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        throw new IllegalArgumentException("All fields are required");
                    }

                    // Add user to database
                    if (addUserToDatabase(username, email, password, role)) {
                        loadUsersFromDatabase(); // Refresh the data
                        return new UserData("0", username, email, role);
                    }
                } catch (Exception e) {
                    showErrorDialog("Input Error", "Invalid input", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Show dialog to add a new instructor
     */
    private void showAddInstructorDialog() {
        Dialog<UserData> dialog = new Dialog<>();
        dialog.setTitle("Add New Instructor");

        // Style dialog header
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(15));
        header.setBackground(new Background(new BackgroundFill(
                Color.web(SECONDARY_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        Label headerLabel = new Label("Add New Instructor");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.WHITE);
        header.setCenter(headerLabel);

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(250);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // Add labels with styling
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        usernameLabel.setTextFill(Color.web(TEXT_COLOR));

        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        emailLabel.setTextFill(Color.web(TEXT_COLOR));

        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        passwordLabel.setTextFill(Color.web(TEXT_COLOR));

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);

        // Create content pane with header and form
        VBox content = new VBox();
        content.getChildren().addAll(header, grid);
        dialog.getDialogPane().setContent(content);

        // Request focus on the username field by default
        usernameField.requestFocus();

        // Convert the result to a UserData when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validate input
                    String username = usernameField.getText().trim();
                    String email = emailField.getText().trim();
                    String password = passwordField.getText();

                    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        throw new IllegalArgumentException("All fields are required");
                    }

                    // Add instructor to database
                    if (addUserToDatabase(username, email, password, "Instructor")) {
                        loadUsersFromDatabase(); // Refresh the data
                        return new UserData("0", username, email, "Instructor");
                    }
                } catch (Exception e) {
                    showErrorDialog("Input Error", "Invalid input", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Show dialog to edit an existing user
     */
    private void showEditUserDialog(UserData user) {
        Dialog<UserData> dialog = new Dialog<>();
        dialog.setTitle("Edit User");

        // Style dialog header
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(15));
        header.setBackground(new Background(new BackgroundFill(
                Color.web(PRIMARY_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        Label headerLabel = new Label("Edit User: " + user.getUsername());
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.WHITE);
        header.setCenter(headerLabel);

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField(user.getUsername());
        TextField emailField = new TextField(user.getEmail());
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Student", "Instructor", "Admin");
        roleCombo.setValue(user.getRole());

        // Add labels with styling
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        usernameLabel.setTextFill(Color.web(TEXT_COLOR));

        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        emailLabel.setTextFill(Color.web(TEXT_COLOR));

        Label roleLabel = new Label("Role:");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        roleLabel.setTextFill(Color.web(TEXT_COLOR));

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(roleLabel, 0, 2);
        grid.add(roleCombo, 1, 2);

        // Create content pane with header and form
        VBox content = new VBox();
        content.getChildren().addAll(header, grid);
        dialog.getDialogPane().setContent(content);

        // Convert the result to a UserData when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validate input
                    String username = usernameField.getText().trim();
                    String email = emailField.getText().trim();
                    String role = roleCombo.getValue();

                    if (username.isEmpty() || email.isEmpty()) {
                        throw new IllegalArgumentException("Username and email are required");
                    }

                    // Update user in database
                    if (updateUserInDatabase(user.getId(), username, email, role)) {
                        loadUsersFromDatabase(); // Refresh the data
                        return user;
                    }
                } catch (Exception e) {
                    showErrorDialog("Input Error", "Invalid input", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Show dialog to reset user password
     */
    private void showResetPasswordDialog(UserData user) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");

        // Style dialog header
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(15));
        header.setBackground(new Background(new BackgroundFill(
                Color.web("#f39c12"), CornerRadii.EMPTY, Insets.EMPTY)));
        Label headerLabel = new Label("Reset Password: " + user.getUsername());
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.WHITE);
        header.setCenter(headerLabel);

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Reset", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        // Add labels with styling
        Label newPasswordLabel = new Label("New Password:");
        newPasswordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        newPasswordLabel.setTextFill(Color.web(TEXT_COLOR));

        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        confirmPasswordLabel.setTextFill(Color.web(TEXT_COLOR));

        grid.add(newPasswordLabel, 0, 0);
        grid.add(newPasswordField, 1, 0);
        grid.add(confirmPasswordLabel, 0, 1);
        grid.add(confirmPasswordField, 1, 1);

        // Create content pane with header and form
        VBox content = new VBox();
        content.getChildren().addAll(header, grid);
        dialog.getDialogPane().setContent(content);

        // Request focus on the new password field by default
        newPasswordField.requestFocus();

        // Convert the result when the reset button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validate input
                    String newPassword = newPasswordField.getText();
                    String confirmPassword = confirmPasswordField.getText();

                    if (newPassword.isEmpty()) {
                        throw new IllegalArgumentException("Password cannot be empty");
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        throw new IllegalArgumentException("Passwords do not match");
                    }

                    // Reset password in database
                    if (resetPasswordInDatabase(user.getId(), newPassword)) {
                        return newPassword;
                    }
                } catch (Exception e) {
                    showErrorDialog("Input Error", "Invalid input", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Add a new user to the database
     */
    private boolean addUserToDatabase(String username, String email, String password, String role) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Hash the password using BCrypt
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // Insert into Users table with correct column name 'passwordHash'
            String userQuery = "INSERT INTO Users (username, email, passwordHash, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                pstmt.setString(3, hashedPassword); // Using the hashed password
                pstmt.setString(4, role);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                // Get the auto-generated user ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        // Now insert into the role-specific table
                        if (role.equalsIgnoreCase("Student")) {
                            insertStudent(conn, userId);
                        } else if (role.equalsIgnoreCase("Instructor")) {
                            insertInstructor(conn, userId);
                        } else if (role.equalsIgnoreCase("Admin")) {
                            insertAdmin(conn, userId);
                        }
                        conn.commit(); // Commit transaction
                        return true;
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            // If there's an error, roll back the transaction
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showErrorDialog("Database Error", "Failed to add user", e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Insert a new student record
     */
    private void insertStudent(Connection conn, int userId) throws SQLException {
        String query = "INSERT INTO Students (studentID, userID) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId); // Using userID as studentID for simplicity
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Insert a new instructor record
     */
    private void insertInstructor(Connection conn, int userId) throws SQLException {
        String query = "INSERT INTO Instructor (instructorID, userID) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId); // Using userID as instructorID for simplicity
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Insert a new admin record
     */
    private void insertAdmin(Connection conn, int userId) throws SQLException {
        String query = "INSERT INTO Admin (adminID, userID) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId); // Using userID as adminID for simplicity
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Update an existing user in the database
     */
    private boolean updateUserInDatabase(String userId, String username, String email, String role) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // First, check the current role
            String currentRoleQuery = "SELECT role FROM Users WHERE userID = ?";
            String currentRole = "";
            try (PreparedStatement pstmt = conn.prepareStatement(currentRoleQuery)) {
                pstmt.setInt(1, Integer.parseInt(userId));
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentRole = rs.getString("role");
                    } else {
                        throw new SQLException("User not found");
                    }
                }
            }

            // Update user information
            String updateQuery = "UPDATE Users SET username = ?, email = ?, role = ? WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                pstmt.setString(3, role);
                pstmt.setInt(4, Integer.parseInt(userId));
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Updating user failed, no rows affected.");
                }

                // If role has changed, update role-specific tables
                if (!currentRole.equalsIgnoreCase(role)) {
                    // Remove from previous role table
                    if (currentRole.equalsIgnoreCase("Student")) {
                        removeStudent(conn, Integer.parseInt(userId));
                    } else if (currentRole.equalsIgnoreCase("Instructor")) {
                        removeInstructor(conn, Integer.parseInt(userId));
                    } else if (currentRole.equalsIgnoreCase("Admin")) {
                        removeAdmin(conn, Integer.parseInt(userId));
                    }

                    // Add to new role table
                    if (role.equalsIgnoreCase("Student")) {
                        insertStudent(conn, Integer.parseInt(userId));
                    } else if (role.equalsIgnoreCase("Instructor")) {
                        insertInstructor(conn, Integer.parseInt(userId));
                    } else if (role.equalsIgnoreCase("Admin")) {
                        insertAdmin(conn, Integer.parseInt(userId));
                    }
                }

                conn.commit(); // Commit transaction
                return true;
            }
        } catch (SQLException e) {
            // If there's an error, roll back the transaction
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showErrorDialog("Database Error", "Failed to update user", e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Remove a student record along with related activity records
     */
    private void removeStudent(Connection conn, int userId) throws SQLException {
        int studentId = -1;
        // Step 1: Get studentID from userID
        String fetchStudentIdQuery = "SELECT studentID FROM Students WHERE userID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(fetchStudentIdQuery)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    studentId = rs.getInt("studentID");
                }
            }
        }

        if (studentId != -1) {
            // Step 2: Delete activities associated with this studentID
            String deleteActivitiesQuery = "DELETE FROM activities WHERE studentID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteActivitiesQuery)) {
                pstmt.setInt(1, studentId);
                pstmt.executeUpdate();
            }

            // Step 3: Delete student record
            String deleteStudentQuery = "DELETE FROM Students WHERE studentID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteStudentQuery)) {
                pstmt.setInt(1, studentId);
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * Remove an instructor record
     */
    private void removeInstructor(Connection conn, int userId) throws SQLException {
        String query = "DELETE FROM Instructor WHERE userID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Remove an admin record
     */
    private void removeAdmin(Connection conn, int userId) throws SQLException {
        String query = "DELETE FROM Admin WHERE userID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Reset a user's password in the database
     */
    private boolean resetPasswordInDatabase(String userId, String newPassword) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Hash the new password using BCrypt
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            
            String query = "UPDATE Users SET passwordHash = ? WHERE userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, hashedPassword); // Using the hashed password
                pstmt.setInt(2, Integer.parseInt(userId));
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Resetting password failed, no rows affected.");
                }
                return true;
            }
        } catch (SQLException e) {
            showErrorDialog("Database Error", "Failed to reset password", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Show an error dialog with details
     */
    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * User data model class with JavaFX properties for TableView
     */
    public static class UserData {
        private final SimpleStringProperty id;
        private final SimpleStringProperty username;
        private final SimpleStringProperty email;
        private final SimpleStringProperty role;

        public UserData(String id, String username, String email, String role) {
            this.id = new SimpleStringProperty(id);
            this.username = new SimpleStringProperty(username);
            this.email = new SimpleStringProperty(email);
            this.role = new SimpleStringProperty(role);
        }

        // Getters
        public String getId() { return id.get(); }
        public String getUsername() { return username.get(); }
        public String getEmail() { return email.get(); }
        public String getRole() { return role.get(); }

        // Setters
        public void setUsername(String value) { username.set(value); }
        public void setEmail(String value) { email.set(value); }
        public void setRole(String value) { role.set(value); }

        // Property getters for TableView
        public SimpleStringProperty idProperty() { return id; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty roleProperty() { return role; }
    }
}