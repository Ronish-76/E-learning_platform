package controllers_Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * System Logs page for reviewing system activity
 */
public class SystemLogsPage {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("System Logs");
        title.getStyleClass().add("page-title");
        
        // Filtering options
        HBox filterContainer = new HBox(10);
        filterContainer.setAlignment(Pos.CENTER_LEFT);
        
        Label logTypeLabel = new Label("Log Type:");
        ComboBox<String> logTypeFilter = new ComboBox<>();
        logTypeFilter.getItems().addAll(
            "All Logs", "Access Logs", "Error Logs", "Audit Logs", "Security Logs"
        );
        logTypeFilter.setValue("All Logs");
        
        Label severityLabel = new Label("Severity:");
        ComboBox<String> severityFilter = new ComboBox<>();
        severityFilter.getItems().addAll(
            "All Levels", "Info", "Warning", "Error", "Critical"
        );
        severityFilter.setValue("All Levels");
        
        Label dateRangeLabel = new Label("Time Range:");
        ComboBox<String> dateRangeFilter = new ComboBox<>();
        dateRangeFilter.getItems().addAll(
            "Last Hour", "Last 24 Hours", "Last 7 Days", "Last 30 Days", "Custom Range"
        );
        dateRangeFilter.setValue("Last 24 Hours");
        
        Button applyFilterBtn = new Button("Apply Filter");
        applyFilterBtn.getStyleClass().add("primary-button");
        
        filterContainer.getChildren().addAll(
            logTypeLabel, logTypeFilter,
            severityLabel, severityFilter,
            dateRangeLabel, dateRangeFilter,
            applyFilterBtn
        );
        
        // Logs table
        TableView<LogEntry> logsTable = createLogsTable();
        VBox.setVgrow(logsTable, Priority.ALWAYS);
        
        // Action buttons
        HBox actionContainer = new HBox(10);
        actionContainer.setAlignment(Pos.CENTER_RIGHT);
        
        Button refreshBtn = new Button("Refresh");
        Button exportBtn = new Button("Export Logs");
        Button clearBtn = new Button("Clear Logs");
        clearBtn.getStyleClass().add("danger-button");
        
        actionContainer.getChildren().addAll(refreshBtn, exportBtn, clearBtn);
        
        view.getChildren().addAll(title, filterContainer, logsTable, actionContainer);
        return view;
    }
    
    private TableView<LogEntry> createLogsTable() {
        TableView<LogEntry> table = new TableView<>();
        
        // Create columns
        TableColumn<LogEntry, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());
        
        TableColumn<LogEntry, String> levelCol = new TableColumn<>("Level");
        levelCol.setCellValueFactory(cellData -> cellData.getValue().levelProperty());
        
        TableColumn<LogEntry, String> sourceCol = new TableColumn<>("Source");
        sourceCol.setCellValueFactory(cellData -> cellData.getValue().sourceProperty());
        
        TableColumn<LogEntry, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(cellData -> cellData.getValue().messageProperty());
        messageCol.setPrefWidth(400);
        
        TableColumn<LogEntry, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(cellData -> cellData.getValue().userProperty());
        
        TableColumn<LogEntry, String> ipCol = new TableColumn<>("IP Address");
        ipCol.setCellValueFactory(cellData -> cellData.getValue().ipProperty());
        
        table.getColumns().addAll(timestampCol, levelCol, sourceCol, messageCol, userCol, ipCol);
        
        // Add sample data
        ObservableList<LogEntry> data = FXCollections.observableArrayList(
            new LogEntry("2023-06-20 14:58:23", "INFO", "UserService", "User login successful", "john.doe", "192.168.1.105"),
            new LogEntry("2023-06-20 14:55:17", "WARNING", "CourseService", "Failed to load course media", "admin", "10.0.0.15"),
            new LogEntry("2023-06-20 14:52:05", "ERROR", "PaymentService", "Payment gateway connection timeout", "system", "10.0.0.1"),
            new LogEntry("2023-06-20 14:45:33", "INFO", "CourseService", "New course published", "instructor1", "192.168.1.112"),
            new LogEntry("2023-06-20 14:42:10", "CRITICAL", "SecurityService", "Multiple failed login attempts detected", "system", "10.0.0.1"),
            new LogEntry("2023-06-20 14:38:56", "INFO", "UserService", "User registration completed", "jane.smith", "192.168.1.108"),
            new LogEntry("2023-06-20 14:35:22", "WARNING", "DatabaseService", "High database load detected", "system", "10.0.0.1"),
            new LogEntry("2023-06-20 14:30:15", "INFO", "EmailService", "Bulk email sent to 156 recipients", "admin", "10.0.0.15"),
            new LogEntry("2023-06-20 14:25:49", "ERROR", "FileService", "Failed to upload file: insufficient disk space", "instructor2", "192.168.1.115"),
            new LogEntry("2023-06-20 14:20:33", "INFO", "BackupService", "System backup completed successfully", "system", "10.0.0.1")
        );
        
        table.setItems(data);
        return table;
    }
}