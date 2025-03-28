package controllers_students;  // Make sure this is in the same package as your Login class

public class User {
    private int userID;
    private String username;
    private String email;
    private String passwordHash;
    private String role; // Admin, Instructor, Student
    
    // Constructor
    public User(int userID, String username, String email, String passwordHash, String role) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }
    
    // Getter and Setter methods
    public int getUserID() {
        return userID;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    // ToString Method for Debugging
    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}