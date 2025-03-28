package controllers_Insructrors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Messages page for student communications
 */
public class MessagesPage {
    public Node getView() {
        VBox view = new VBox(20);
        view.getStyleClass().add("page-content");
        
        Label title = new Label("Messages");
        title.getStyleClass().add("page-title");
        
        // Split pane for messages list and message content
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0, 0.35);
        
        // Left side - Conversations list
        VBox conversationsBox = new VBox(10);
        conversationsBox.setPadding(new Insets(10));
        
        // Search and filters
        TextField searchField = new TextField();
        searchField.setPromptText("Search messages...");
        
        HBox filterButtons = new HBox(10);
        filterButtons.setPadding(new Insets(10, 0, 10, 0));
        
        ToggleGroup filterGroup = new ToggleGroup();
        
        ToggleButton allBtn = new ToggleButton("All");
        allBtn.setToggleGroup(filterGroup);
        allBtn.setSelected(true);
        allBtn.getStyleClass().add("filter-button");
        
        ToggleButton unreadBtn = new ToggleButton("Unread");
        unreadBtn.setToggleGroup(filterGroup);
        unreadBtn.getStyleClass().add("filter-button");
        
        ToggleButton flaggedBtn = new ToggleButton("Flagged");
        flaggedBtn.setToggleGroup(filterGroup);
        flaggedBtn.getStyleClass().add("filter-button");
        
        filterButtons.getChildren().addAll(allBtn, unreadBtn, flaggedBtn);
        
        // Conversations list
        ScrollPane conversationsScroll = new ScrollPane();
        conversationsScroll.setFitToWidth(true);
        conversationsScroll.setFitToHeight(true);
        VBox.setVgrow(conversationsScroll, Priority.ALWAYS);
        
        VBox conversationsList = new VBox(0);
        conversationsList.getChildren().addAll(
            createConversationItem("John Smith", "I have a question about the assignment", "10 minutes ago", true),
            createConversationItem("Emma Wilson", "Thank you for your feedback on my project", "2 hours ago", false),
            createConversationItem("Michael Brown", "Regarding the exam schedule", "Yesterday", false),
            createConversationItem("Sarah Johnson", "Extension request for assignment", "Yesterday", true),
            createConversationItem("Robert Thompson", "Re: Course materials for week 5", "June 18", false),
            createConversationItem("Lisa Anderson", "Quiz results inquiry", "June 17", false),
            createConversationItem("David Wilson", "Question about Python functions", "June 15", false),
            createConversationItem("Mark Davis", "Group project coordination", "June 12", false)
        );
        
        conversationsScroll.setContent(conversationsList);
        
        conversationsBox.getChildren().addAll(searchField, filterButtons, conversationsScroll);
        
        // Right side - Message content and reply
        VBox messageContentBox = new VBox(10);
        messageContentBox.setPadding(new Insets(10));
        
        // Message header
        HBox messageHeader = new HBox(10);
        messageHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label studentName = new Label("John Smith");
        studentName.getStyleClass().add("student-name");
        
        Label courseLabel = new Label("Introduction to Python");
        courseLabel.getStyleClass().add("message-course");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Button archiveBtn = new Button("Archive");
        archiveBtn.getStyleClass().add("secondary-button");
        
        Button flagBtn = new Button("Flag");
        flagBtn.getStyleClass().add("secondary-button");
        
        messageHeader.getChildren().addAll(studentName, courseLabel, headerSpacer, archiveBtn, flagBtn);
        
        // Message thread
        ScrollPane messageScroll = new ScrollPane();
        messageScroll.setFitToWidth(true);
        VBox.setVgrow(messageScroll, Priority.ALWAYS);
        
        VBox messageThread = new VBox(15);
        messageThread.setPadding(new Insets(10));
        messageThread.getChildren().addAll(
            createReceivedMessage(
                "Hello Professor, I have a question about the Python assignment due next week. " +
                "I'm having trouble with the file I/O section. Could you clarify how we should handle file errors?",
                "Today, 10:15 AM"),
            createSentMessage(
                "Hi John, I'd be happy to help. For file handling, you should use try/except blocks to catch " +
                "potential errors such as FileNotFoundError. Did you review the lecture notes from week 3? " +
                "They contain examples of proper error handling.",
                "Today, 10:22 AM"),
            createReceivedMessage(
                "I did look at those notes, but I'm confused about when to use 'finally' blocks. " +
                "Also, should we close the file in the 'try' block or in the 'finally' block?",
                "Today, 10:25 AM")
        );
        
        messageScroll.setContent(messageThread);
        
        // Reply area
        HBox replyContainer = new HBox(10);
        replyContainer.setAlignment(Pos.CENTER);
        
        TextArea replyField = new TextArea();
        replyField.setPromptText("Type your message here...");
        replyField.setPrefRowCount(3);
        HBox.setHgrow(replyField, Priority.ALWAYS);
        
        Button attachBtn = new Button("📎");
        attachBtn.getStyleClass().add("attach-button");
        attachBtn.setTooltip(new Tooltip("Attach File"));
        
        Button sendBtn = new Button("Send");
        sendBtn.getStyleClass().add("primary-button");
        
        replyContainer.getChildren().addAll(replyField, attachBtn, sendBtn);
        
        messageContentBox.getChildren().addAll(messageHeader, messageScroll, replyContainer);
        
        // Add both sides to the split pane
        splitPane.getItems().addAll(conversationsBox, messageContentBox);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        view.getChildren().addAll(title, splitPane);
        return view;
    }
    
    private HBox createConversationItem(String name, String preview, String time, boolean unread) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(15));
        item.getStyleClass().add("conversation-item");
        if (unread) {
            item.getStyleClass().add("unread-conversation");
        }
        
        VBox content = new VBox(5);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        HBox nameRow = new HBox(5);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("conversation-name");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("conversation-time");
        
        nameRow.getChildren().addAll(nameLabel, spacer, timeLabel);
        
        Label previewLabel = new Label(preview);
        previewLabel.getStyleClass().add("conversation-preview");
        previewLabel.setWrapText(true);
        
        content.getChildren().addAll(nameRow, previewLabel);
        
        if (unread) {
            Circle unreadIndicator = new Circle(5);
            unreadIndicator.getStyleClass().add("unread-indicator");
            item.getChildren().addAll(unreadIndicator, content);
        } else {
            item.getChildren().add(content);
            item.setPadding(new Insets(15, 15, 15, 25));
        }
        
        // Selected state for first item
        if (name.equals("John Smith")) {
            item.getStyleClass().add("selected-conversation");
        }
        
        return item;
    }
    
    private VBox createReceivedMessage(String text, String time) {
        VBox message = new VBox(5);
        message.setAlignment(Pos.CENTER_LEFT);
        message.setMaxWidth(500);
        
        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("received-message");
        
        Text messageText = new Text(text);
        messageText.setWrappingWidth(480);
        textFlow.getChildren().add(messageText);
        
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("message-time");
        
        message.getChildren().addAll(textFlow, timeLabel);
        return message;
    }
    
    private VBox createSentMessage(String text, String time) {
        VBox message = new VBox(5);
        message.setAlignment(Pos.CENTER_RIGHT);
        message.setMaxWidth(500);
        
        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("sent-message");
        
        Text messageText = new Text(text);
        messageText.setWrappingWidth(480);
        textFlow.getChildren().add(messageText);
        
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("message-time");
        timeLabel.setAlignment(Pos.CENTER_RIGHT);
        
        message.getChildren().addAll(textFlow, timeLabel);
        return message;
    }
}