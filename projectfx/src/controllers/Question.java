package controllers;

public class Question {
    private String text;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    
    public Question(String text, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
    }
    
    public String getText() {
        return text;
    }
    
    public String getOptionA() {
        return optionA;
    }
    
    public String getOptionB() {
        return optionB;
    }
    
    public String getOptionC() {
        return optionC;
    }
    
    public String getOptionD() {
        return optionD;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
}