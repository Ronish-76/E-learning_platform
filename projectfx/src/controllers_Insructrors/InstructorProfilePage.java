package controllers_Insructrors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Instructor Profile Page
 */
public class InstructorProfilePage {
    private String currentUsername = "Jane Instructor";
    
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Instructor Profile");
        title.getStyleClass().add("page-title");
        
        // Profile info section
        HBox profileSection = new HBox(30);
        profileSection.setPadding(new Insets(20));
        profileSection.getStyleClass().add("profile-section");
        
        // Left side - Avatar and basic info
        VBox profileBasics = new VBox(15);
        profileBasics.setAlignment(Pos.TOP_CENTER);
        
        // Avatar placeholder (in a real app, this would be a proper image)
        Rectangle avatarPlaceholder = new Rectangle(120, 120);
        avatarPlaceholder.setArcWidth(20);
        avatarPlaceholder.setArcHeight(20);
        avatarPlaceholder.setFill(Color.LIGHTGRAY);
        
        Button changeAvatarBtn = new Button("Change Avatar");
        changeAvatarBtn.getStyleClass().add("secondary-button");
        
        Label nameLabel = new Label(currentUsername);
        nameLabel.getStyleClass().add("profile-name");
        
        Label roleLabel = new Label("Senior Instructor");
        roleLabel.getStyleClass().add("profile-role");
        
        Label memberSinceLabel = new Label("Member since Jan 2021");
        memberSinceLabel.getStyleClass().add("profile-since");
        
        profileBasics.getChildren().addAll(avatarPlaceholder, changeAvatarBtn, nameLabel, roleLabel, memberSinceLabel);
        
        // Right side - Editable profile details
        VBox profileDetails = new VBox(15);
        profileDetails.setAlignment(Pos.TOP_LEFT);
        profileDetails.setPrefWidth(400);
        
        Label detailsTitle = new Label("Profile Details");
        detailsTitle.getStyleClass().add("section-title");
        
        // Create form fields
        GridPane formGrid = new GridPane();
        formGrid.setVgap(10);
        formGrid.setHgap(15);
        
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField("Jane");
        
        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField("Instructor");
        
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField("jane.instructor@example.com");
        
        Label phoneLabel = new Label("Phone:");
        TextField phoneField = new TextField("+1 555-987-6543");
        
        Label departmentLabel = new Label("Department:");
        TextField departmentField = new TextField("Computer Science");
        
        Label bioLabel = new Label("Bio:");
        TextArea bioField = new TextArea("Experienced software developer and educator with 10+ years in industry. Specializes in Python, Web Development, and Data Science.");
        bioField.setPrefRowCount(3);
        bioField.setWrapText(true);
        
        // Position form fields
        formGrid.add(firstNameLabel, 0, 0);
        formGrid.add(firstNameField, 1, 0);
        formGrid.add(lastNameLabel, 0, 1);
        formGrid.add(lastNameField, 1, 1);
        formGrid.add(emailLabel, 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(phoneLabel, 0, 3);
        formGrid.add(phoneField, 1, 3);
        formGrid.add(departmentLabel, 0, 4);
        formGrid.add(departmentField, 1, 4);
        formGrid.add(bioLabel, 0, 5);
        formGrid.add(bioField, 1, 5);
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setPadding(new Insets(15, 0, 0, 0));
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveProfileBtn = new Button("Save Changes");
        saveProfileBtn.getStyleClass().add("primary-button");
        
        Button resetBtn = new Button("Reset");
        resetBtn.getStyleClass().add("secondary-button");
        
        actionButtons.getChildren().addAll(resetBtn, saveProfileBtn);
        
        profileDetails.getChildren().addAll(detailsTitle, formGrid, actionButtons);
        
        profileSection.getChildren().addAll(profileBasics, profileDetails);
        
        // Teaching stats section
        VBox statsSection = new VBox(15);
        statsSection.setPadding(new Insets(20));
        statsSection.getStyleClass().add("stats-section");
        
        Label statsTitle = new Label("Teaching Statistics");
        statsTitle.getStyleClass().add("section-title");
        
        // Stats display
        HBox statsDisplay = new HBox(20);
        statsDisplay.setAlignment(Pos.CENTER);
        
        VBox coursesBox = createStatsBox("Courses Created", "12", "");
        VBox studentsBox = createStatsBox("Total Students", "1,456", "");
        VBox ratingsBox = createStatsBox("Average Rating", "4.8/5", "from 756 reviews");
        VBox completionBox = createStatsBox("Student Completion", "73%", "industry avg: 65%");
        
        statsDisplay.getChildren().addAll(coursesBox, studentsBox, ratingsBox, completionBox);
        
        statsSection.getChildren().addAll(statsTitle, statsDisplay);
        
        // Qualifications section
        VBox qualificationsSection = new VBox(15);
        qualificationsSection.setPadding(new Insets(20));
        qualificationsSection.getStyleClass().add("qualifications-section");
        
        HBox qualificationsHeader = new HBox();
        qualificationsHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label qualificationsTitle = new Label("Qualifications & Expertise");
        qualificationsTitle.getStyleClass().add("section-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addQualificationBtn = new Button("+ Add");
        addQualificationBtn.getStyleClass().add("add-button");
        
        qualificationsHeader.getChildren().addAll(qualificationsTitle, spacer, addQualificationBtn);
        
        // Qualifications list
        VBox qualificationsList = new VBox(10);
        qualificationsList.getChildren().addAll(
            createQualificationItem("M.S. Computer Science", "Stanford University", "2012 - 2014"),
            createQualificationItem("B.S. Software Engineering", "MIT", "2008 - 2012"),
            createQualificationItem("Senior Software Engineer", "Google", "2014 - 2019"),
            createQualificationItem("Python Certified Developer", "Python Institute", "2015"),
            createQualificationItem("AWS Certified Solutions Architect", "Amazon Web Services", "2018")
        );
        
        qualificationsSection.getChildren().addAll(qualificationsHeader, qualificationsList);
        
        view.getChildren().addAll(title, profileSection, statsSection, qualificationsSection);
        return view;
    }
    
    private VBox createStatsBox(String title, String value, String subtitle) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("stats-box");
        box.setPadding(new Insets(15));
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stats-title");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stats-value");
        
        box.getChildren().addAll(titleLabel, valueLabel);
        
        if (!subtitle.isEmpty()) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.getStyleClass().add("stats-subtitle");
            box.getChildren().add(subtitleLabel);
        }
        
        return box;
    }
    
    private HBox createQualificationItem(String title, String institution, String duration) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10, 15, 10, 15));
        item.getStyleClass().add("qualification-item");
        
        VBox details = new VBox(5);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("qualification-title");
        
        Label institutionLabel = new Label(institution);
        institutionLabel.getStyleClass().add("qualification-institution");
        
        details.getChildren().addAll(titleLabel, institutionLabel);
        
        Label durationLabel = new Label(duration);
        durationLabel.getStyleClass().add("qualification-duration");
        
        Button removeBtn = new Button("×");
        removeBtn.getStyleClass().add("remove-button");
        removeBtn.setTooltip(new Tooltip("Remove"));
        
        item.getChildren().addAll(details, durationLabel, removeBtn);
        return item;
    }
}