package controllers_Insructrors;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for assignment data display in tables
 */
public class AssignmentData {
    private final SimpleStringProperty title;
    private final SimpleStringProperty course;
    private final SimpleStringProperty dueDate;
    private final SimpleStringProperty status;
    private final SimpleStringProperty submissions;
    
    public AssignmentData(String title, String course, String dueDate, String status, String submissions) {
        this.title = new SimpleStringProperty(title);
        this.course = new SimpleStringProperty(course);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.status = new SimpleStringProperty(status);
        this.submissions = new SimpleStringProperty(submissions);
    }
    
    public StringProperty titleProperty() { return title; }
    public StringProperty courseProperty() { return course; }
    public StringProperty dueDateProperty() { return dueDate; }
    public StringProperty statusProperty() { return status; }
    public StringProperty submissionsProperty() { return submissions; }
    
    public String getTitle() { return title.get(); }
    public String getCourse() { return course.get(); }
    public String getDueDate() { return dueDate.get(); }
    public String getStatus() { return status.get(); }
    public String getSubmissions() { return submissions.get(); }
}