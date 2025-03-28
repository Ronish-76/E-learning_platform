package controllers_Admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for user data display in tables
 */
public class UserData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty email;
    private final SimpleStringProperty role;
    private final SimpleStringProperty status;
    
    public UserData(String id, String name, String email, String role, String status) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
        this.status = new SimpleStringProperty(status);
    }
    
    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty emailProperty() { return email; }
    public StringProperty roleProperty() { return role; }
    public StringProperty statusProperty() { return status; }
    
    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getEmail() { return email.get(); }
    public String getRole() { return role.get(); }
    public String getStatus() { return status.get(); }
}