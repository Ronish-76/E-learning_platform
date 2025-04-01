package controllers_Instructors;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for student data display in tables
 */
public class StudentData {
    private final SimpleStringProperty name;
    private final SimpleStringProperty email;
    private final SimpleStringProperty course;
    private final SimpleDoubleProperty progress;
    private final SimpleStringProperty grade;
    private final SimpleStringProperty lastActive;
    
    public StudentData(String name, String email, String course, double progress, String grade, String lastActive) {
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.course = new SimpleStringProperty(course);
        this.progress = new SimpleDoubleProperty(progress);
        this.grade = new SimpleStringProperty(grade);
        this.lastActive = new SimpleStringProperty(lastActive);
    }
    
    public StringProperty nameProperty() { return name; }
    public StringProperty emailProperty() { return email; }
    public StringProperty courseProperty() { return course; }
    public DoubleProperty progressProperty() { return progress; }
    public StringProperty gradeProperty() { return grade; }
    public StringProperty lastActiveProperty() { return lastActive; }
    
    public String getName() { return name.get(); }
    public String getEmail() { return email.get(); }
    public String getCourse() { return course.get(); }
    public double getProgressValue() { return progress.get(); }
    public String getGrade() { return grade.get(); }
    public String getLastActive() { return lastActive.get(); }
}