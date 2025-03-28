package controllers_Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

/**
 * User Management page for admin operations on users
 */
public class UserManagementPage {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("User Management");
        title.getStyleClass().add("page-title");
        
        // Search and filter section
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search users...");
        searchField.setPrefWidth(300);
        
        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("All Roles", "Student", "Instructor", "Admin");
        roleFilter.setValue("All Roles");
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "Active", "Inactive", "Pending");
        statusFilter.setValue("All Status");
        
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("primary-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addUserBtn = new Button("Add New User");
        addUserBtn.getStyleClass().add("success-button");
        
        searchContainer.getChildren().addAll(searchField, roleFilter, statusFilter, searchBtn, spacer, addUserBtn);
        
        // Users table
        TableView<UserData> usersTable = createUsersTable();
        usersTable.getStyleClass().add("users-table");
        VBox.setVgrow(usersTable, Priority.ALWAYS);
        
        // Pagination
        HBox paginationContainer = new HBox(10);
        paginationContainer.setAlignment(Pos.CENTER);
        
        Label pageInfo = new Label("Showing 1-10 of 1,245 users");
        Button prevBtn = new Button("Previous");
        Button nextBtn = new Button("Next");
        prevBtn.setDisable(true);
        
        paginationContainer.getChildren().addAll(prevBtn, pageInfo, nextBtn);
        
        view.getChildren().addAll(title, searchContainer, usersTable, paginationContainer);
        return view;
    }
    
    private TableView<UserData> createUsersTable() {
        TableView<UserData> table = new TableView<>();
        
        // Create columns
        TableColumn<UserData, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        
        TableColumn<UserData, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        
        TableColumn<UserData, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        
        TableColumn<UserData, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        
        TableColumn<UserData, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        
        TableColumn<UserData, String> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(col -> new TableCell<UserData, String>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            
            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                
                editButton.setOnAction(event -> {
                    UserData userData = getTableView().getItems().get(getIndex());
                    // Handle edit action
                    System.out.println("Edit user: " + userData.getName());
                });
                
                deleteButton.setOnAction(event -> {
                    UserData userData = getTableView().getItems().get(getIndex());
                    // Handle delete action
                    System.out.println("Delete user: " + userData.getName());
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
        
        table.getColumns().addAll(idColumn, nameColumn, emailColumn, roleColumn, statusColumn, actionsColumn);
        
        // Add sample data
        ObservableList<UserData> data = FXCollections.observableArrayList(
            new UserData("001", "John Smith", "john.smith@example.com", "Student", "Active"),
            new UserData("002", "Jane Doe", "jane.doe@example.com", "Instructor", "Active"),
            new UserData("003", "Bob Johnson", "bob.johnson@example.com", "Student", "Inactive"),
            new UserData("004", "Alice Williams", "alice.williams@example.com", "Admin", "Active"),
            new UserData("005", "Charlie Brown", "charlie.brown@example.com", "Student", "Pending"),
            new UserData("006", "Diana Prince", "diana.prince@example.com", "Instructor", "Active"),
            new UserData("007", "Edward Norton", "edward.norton@example.com", "Student", "Active"),
            new UserData("008", "Fiona Apple", "fiona.apple@example.com", "Student", "Inactive"),
            new UserData("009", "George Lucas", "george.lucas@example.com", "Instructor", "Active"),
            new UserData("010", "Helen Hunt", "helen.hunt@example.com", "Student", "Active")
        );
        
        table.setItems(data);
        return table;
    }
}