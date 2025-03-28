package controllers_Insructrors;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for content metrics display
 */
public class ContentMetric {
    private final SimpleStringProperty name;
    private final SimpleStringProperty type;
    private final SimpleStringProperty views;
    private final SimpleStringProperty avgTime;
    private final SimpleStringProperty completionRate;
    
    public ContentMetric(String name, String type, String views, String avgTime, String completionRate) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.views = new SimpleStringProperty(views);
        this.avgTime = new SimpleStringProperty(avgTime);
        this.completionRate = new SimpleStringProperty(completionRate);
    }
    
    public StringProperty nameProperty() { return name; }
    public StringProperty typeProperty() { return type; }
    public StringProperty viewsProperty() { return views; }
    public StringProperty avgTimeProperty() { return avgTime; }
    public StringProperty completionRateProperty() { return completionRate; }
}