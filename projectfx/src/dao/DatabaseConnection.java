package dao;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String DATABASE_NAME = "elearningplatform";
    private static final String USER = "root";
    private static final String PASSWORD = "ronish";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 1: Connect without DB to create it
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);
                System.out.println("Database created or already exists.");
            }

            // Step 2: Connect to the database
            connection = DriverManager.getConnection(URL + DATABASE_NAME, USER, PASSWORD);
            try (Statement stmt = connection.createStatement()) {
                createTables(stmt);
            }

            // Step 3: Insert default admin user
            String adminHashedPassword = BCrypt.hashpw("admin", BCrypt.gensalt());
            insertDefaultData(connection, adminHashedPassword);

            System.out.println("Database schema and admin user initialized.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    // Create all tables
    private static void createTables(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Users (" +
                "userID INT PRIMARY KEY AUTO_INCREMENT," +
                "username VARCHAR(255) UNIQUE NOT NULL," +
                "email VARCHAR(255) UNIQUE NOT NULL," +
                "passwordHash VARCHAR(60) NOT NULL," +
                "role ENUM('Admin', 'Instructor', 'Student') NOT NULL);" +

                "CREATE TABLE IF NOT EXISTS Students (" +
                "studentID INT PRIMARY KEY AUTO_INCREMENT," +
                "userID INT UNIQUE," +
                "FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE);" +

                "CREATE TABLE IF NOT EXISTS Instructor (" +
                "instructorID INT PRIMARY KEY AUTO_INCREMENT," +
                "userID INT UNIQUE," +
                "FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE);" +

                "CREATE TABLE IF NOT EXISTS Admin (" +
                "adminID INT PRIMARY KEY AUTO_INCREMENT," +
                "userID INT UNIQUE," +
                "FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE);" +

                "CREATE TABLE IF NOT EXISTS quiz_questions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "subject VARCHAR(255) NOT NULL," +
                "question TEXT NOT NULL," +
                "option_a VARCHAR(255) NOT NULL," +
                "option_b VARCHAR(255) NOT NULL," +
                "option_c VARCHAR(255)," +
                "option_d VARCHAR(255)," +
                "correct_option VARCHAR(255) NOT NULL);" +

                "CREATE TABLE IF NOT EXISTS QuizResults (" +
                "resultID INT PRIMARY KEY AUTO_INCREMENT," +
                "questionID INT NOT NULL," +
                "studentID INT NOT NULL," +
                "selectedOption VARCHAR(255) NOT NULL," +
                "isCorrect BOOLEAN DEFAULT FALSE," +
                "submissionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (studentID) REFERENCES Students(studentID)," +
                "FOREIGN KEY (questionID) REFERENCES quiz_questions(id)," +
                "UNIQUE KEY (questionID, studentID));" +

                "CREATE TABLE IF NOT EXISTS Courses (" +
                "courseID INT PRIMARY KEY AUTO_INCREMENT," +
                "courseName VARCHAR(255) NOT NULL UNIQUE," +
                "description TEXT," +
                "createdBy INT," +
                "courseColor VARCHAR(20)," +
                "FOREIGN KEY (createdBy) REFERENCES Users(userID) ON DELETE SET NULL);" +

                "CREATE TABLE IF NOT EXISTS Lessons (" +
                "lessonID INT PRIMARY KEY AUTO_INCREMENT," +
                "courseID INT NOT NULL," +
                "title VARCHAR(255) NOT NULL," +
                "category VARCHAR(100)," +
                "studentCount INT DEFAULT 0," +
                "rating DECIMAL(3,2) DEFAULT 0.0," +
                "content TEXT," +
                "FOREIGN KEY (courseID) REFERENCES Courses(courseID) ON DELETE CASCADE);" +

                "CREATE TABLE IF NOT EXISTS LessonProgress (" +
                "progressID INT PRIMARY KEY AUTO_INCREMENT," +
                "studentID INT NOT NULL," +
                "lessonID INT NOT NULL," +
                "completionStatus ENUM('Not Started', 'In Progress', 'Completed') DEFAULT 'Not Started'," +
                "completionDate TIMESTAMP NULL," +
                "lastAccessed TIMESTAMP NULL," +
                "FOREIGN KEY (studentID) REFERENCES Students(studentID)," +
                "FOREIGN KEY (lessonID) REFERENCES Lessons(lessonID)," +
                "UNIQUE KEY (studentID, lessonID));" +

                "CREATE TABLE IF NOT EXISTS Enrollments (" +
                "enrollmentID INT PRIMARY KEY AUTO_INCREMENT," +
                "studentID INT," +
                "courseID INT," +
                "enrollmentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "completionPercentage FLOAT DEFAULT 0.0," +
                "periodNumber INT DEFAULT 1," +
                "FOREIGN KEY (studentID) REFERENCES Students(studentID)," +
                "FOREIGN KEY (courseID) REFERENCES Courses(courseID)," +
                "UNIQUE (studentID, courseID));" +

                "CREATE TABLE IF NOT EXISTS Assignments (" +
                "assignmentID INT PRIMARY KEY AUTO_INCREMENT," +
                "courseID INT NOT NULL," +
                "title VARCHAR(255) NOT NULL," +
                "description TEXT," +
                "dueDate DATE NOT NULL," +
                "points VARCHAR(10) DEFAULT '10'," +
                "priority VARCHAR(20) DEFAULT 'Medium'," +
                "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (courseID) REFERENCES Courses(courseID));" +

                "CREATE TABLE IF NOT EXISTS AssignmentProgress (" +
                "progressID INT PRIMARY KEY AUTO_INCREMENT," +
                "assignmentID INT NOT NULL," +
                "studentID INT NOT NULL," +
                "content TEXT," +
                "status VARCHAR(50) DEFAULT 'Not started'," +
                "lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (assignmentID) REFERENCES Assignments(assignmentID)," +
                "FOREIGN KEY (studentID) REFERENCES Students(studentID)," +
                "UNIQUE KEY (assignmentID, studentID));" +

                "CREATE TABLE IF NOT EXISTS Activities (" +
                "activityID INT PRIMARY KEY AUTO_INCREMENT," +
                "studentID INT," +
                "courseID INT," +
                "activityType VARCHAR(50)," +
                "description TEXT," +
                "completionStatus VARCHAR(20)," +
                "activityDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (studentID) REFERENCES Students(studentID)," +
                "FOREIGN KEY (courseID) REFERENCES Courses(courseID));";

        for (String statement : sql.split(";")) {
            statement = statement.trim();
            if (!statement.isEmpty()) {
                stmt.executeUpdate(statement);
            }
        }
    }

    // Insert default admin user if it doesn't exist
    private static void insertDefaultData(Connection connection, String adminHashedPassword) throws SQLException {
        String checkUser = "SELECT userID FROM Users WHERE username = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkUser)) {
            checkStmt.setString(1, "admin");
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                String insertUser = "INSERT INTO Users (username, email, passwordHash, role) VALUES (?, ?, ?, 'Admin')";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertUser)) {
                    insertStmt.setString(1, "admin");
                    insertStmt.setString(2, "Admin@gmail.com");
                    insertStmt.setString(3, adminHashedPassword);
                    insertStmt.executeUpdate();
                    System.out.println("Admin user inserted.");
                }
            } else {
                System.out.println("Admin user already exists. Skipping insert.");
            }
        }
    }
}