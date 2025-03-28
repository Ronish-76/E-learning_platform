package controllers_Admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for log entry data display
 */
public class LogEntry {
    private final SimpleStringProperty timestamp;
    private final SimpleStringProperty level;
    private final SimpleStringProperty source;
    private final SimpleStringProperty message;
    private final SimpleStringProperty user;
    private final SimpleStringProperty ip;
    
    public LogEntry(String timestamp, String level, String source, String message, String user, String ip) {
        this.timestamp = new SimpleStringProperty(timestamp);
        this.level = new SimpleStringProperty(level);
        this.source = new SimpleStringProperty(source);
        this.message = new SimpleStringProperty(message);
        this.user = new SimpleStringProperty(user);
        this.ip = new SimpleStringProperty(ip);
    }
    
    public StringProperty timestampProperty() { return timestamp; }
    public StringProperty levelProperty() { return level; }
    public StringProperty sourceProperty() { return source; }
    public StringProperty messageProperty() { return message; }
    public StringProperty userProperty() { return user; }
    public StringProperty ipProperty() { return ip; }
}