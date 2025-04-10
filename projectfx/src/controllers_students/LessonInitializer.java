package controllers_students;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import dao.DatabaseConnection;

public class LessonInitializer extends Application {
    
    private TextArea logArea;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Initialize Course Lessons");
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(400);
        logArea.setWrapText(true);
        
        Button initButton = new Button("Initialize Lessons for All Courses");
        initButton.setOnAction(e -> initializeLessons());
        
        VBox root = new VBox(10);
        root.getChildren().addAll(
            new Label("This tool will add sample lessons to all courses in the database"),
            initButton,
            logArea
        );
        root.setStyle("-fx-padding: 20px;");
        
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void initializeLessons() {
        logArea.clear();
        log("Starting lesson initialization...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Ensure content column exists in Lessons table
                ensureContentColumnExists(conn);
                
                // Get all courses
                String getCourses = "SELECT courseID, courseName FROM Courses";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(getCourses)) {
                    
                    while (rs.next()) {
                        int courseId = rs.getInt("courseID");
                        String courseName = rs.getString("courseName");
                        log("Adding lessons to course: " + courseName + " (ID: " + courseId + ")");
                        
                        // Clear existing lessons for this course if needed
                        // Uncomment if you want to remove existing lessons
                        /*
                        try (PreparedStatement clearStmt = conn.prepareStatement(
                                "DELETE FROM Lessons WHERE courseID = ?")) {
                            clearStmt.setInt(1, courseId);
                            int cleared = clearStmt.executeUpdate();
                            log("Cleared " + cleared + " existing lessons");
                        }
                        */
                        
                        // Add subject-specific lessons based on course name
                        addLessonsForCourse(conn, courseId, courseName);
                    }
                }
                
                conn.commit();
                log("Lesson initialization completed successfully!");
                
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Lessons Added");
                alert.setContentText("Lessons have been successfully added to all courses.");
                alert.showAndWait();
                
            } catch (SQLException e) {
                conn.rollback();
                log("ERROR: " + e.getMessage());
                e.printStackTrace();
                
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to Initialize Lessons");
                alert.setContentText("An error occurred: " + e.getMessage());
                alert.showAndWait();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log("ERROR connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void ensureContentColumnExists(Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "Lessons", "content")) {
            if (!rs.next()) {
                log("Adding missing 'content' column to Lessons table");
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE Lessons ADD COLUMN content TEXT");
                }
            } else {
                log("Content column already exists in Lessons table");
            }
        }
    }
    
    private void addLessonsForCourse(Connection conn, int courseId, String courseName) throws SQLException {
        // Check if course already has lessons
        String checkLessons = "SELECT COUNT(*) AS lessonCount FROM Lessons WHERE courseID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkLessons)) {
            checkStmt.setInt(1, courseId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt("lessonCount") > 0) {
                    log("Course already has " + rs.getInt("lessonCount") + " lessons. Adding more lessons...");
                }
            }
        }
        
        // Create the lessons based on course subject
        String insertLesson = "INSERT INTO Lessons (courseID, title, category, content) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertLesson)) {
            // Common parameters
            pstmt.setInt(1, courseId);
            
            if (courseName.toLowerCase().contains("math")) {
                addMathLessons(pstmt);
            } else if (courseName.toLowerCase().contains("history")) {
                addHistoryLessons(pstmt);
            } else if (courseName.toLowerCase().contains("computer") || courseName.toLowerCase().contains("programming")) {
                addComputerScienceLessons(pstmt);
            } else if (courseName.toLowerCase().contains("biology")) {
                addBiologyLessons(pstmt);
            } else if (courseName.toLowerCase().contains("physics")) {
                addPhysicsLessons(pstmt);
            } else if (courseName.toLowerCase().contains("literature")) {
                addLiteratureLessons(pstmt);
            } else {
                // Default lessons for any other course
                addDefaultLessons(pstmt, courseName);
            }
        }
    }
    
    private void addMathLessons(PreparedStatement pstmt) throws SQLException {
        String[][] lessons = {
            {"Introduction to Calculus", "Fundamentals", 
                "# Introduction to Calculus\n\nCalculus is the mathematical study of continuous change. This lesson introduces the fundamental concepts of calculus including limits, derivatives, and integrals.\n\n## Key Concepts\n- Limits and continuity\n- Rates of change\n- The derivative\n- The integral\n- The Fundamental Theorem of Calculus"},
                
            {"Limits and Continuity", "Fundamentals", 
                "# Limits and Continuity\n\nThis lesson explores the concept of limits, which form the foundation of calculus. We'll learn how to evaluate limits algebraically and graphically.\n\n## Topics Covered\n- Definition of a limit\n- One-sided limits\n- Infinite limits\n- Continuity of functions\n- The Intermediate Value Theorem"},
                
            {"Derivatives and Rules", "Core Concepts", 
                "# Derivatives and Rules\n\nThe derivative measures the rate at which a function changes. This lesson covers how to find derivatives using various rules.\n\n## Derivative Rules\n- Power Rule\n- Product Rule\n- Quotient Rule\n- Chain Rule\n- Implicit Differentiation"},
                
            {"Applications of Derivatives", "Applications", 
                "# Applications of Derivatives\n\nDerivatives have many practical applications in science, engineering, and economics. This lesson explores how derivatives are used to solve real-world problems.\n\n## Applications\n- Finding rates of change\n- Optimization problems\n- Related rates\n- Linear approximation\n- Newton's Method"},
                
            {"Integration Techniques", "Advanced", 
                "# Integration Techniques\n\nIntegration is the process of finding the accumulation of quantities. This lesson covers various methods for evaluating integrals.\n\n## Techniques\n- Substitution method\n- Integration by parts\n- Trigonometric integrals\n- Partial fractions\n- Numerical integration"}
        };
        
        for (String[] lesson : lessons) {
            pstmt.setString(2, lesson[0]);  // title
            pstmt.setString(3, lesson[1]);  // category
            pstmt.setString(4, lesson[2]);  // content
            pstmt.executeUpdate();
            log("Added math lesson: " + lesson[0]);
        }
    }
    
    private void addHistoryLessons(PreparedStatement pstmt) throws SQLException {
        String[][] lessons = {
            {"Ancient Civilizations", "World History", 
                "# Ancient Civilizations\n\nThis lesson explores the earliest human civilizations including Mesopotamia, Egypt, Indus Valley, and China.\n\n## Major Civilizations\n- Sumerians and Babylonians\n- Ancient Egypt and the Pharaohs\n- Indus Valley\n- Ancient China\n\n## Key Developments\n- Writing systems\n- Agriculture and irrigation\n- Early legal codes\n- Monumental architecture"},
                
            {"The Middle Ages", "European History", 
                "# The Middle Ages\n\nThe Middle Ages, or Medieval Period, spans roughly from the 5th to the 15th century. This lesson covers the social, political, and cultural aspects of medieval Europe.\n\n## Topics\n- Feudalism and manorialism\n- The role of the Church\n- The Crusades\n- Medieval art and architecture\n- The Black Death"},
                
            {"The Renaissance", "European History", 
                "# The Renaissance\n\nThe Renaissance was a period of cultural, artistic, political, and scientific rebirth that followed the Middle Ages. This lesson explores the profound impact of this era.\n\n## Key Figures\n- Leonardo da Vinci\n- Michelangelo\n- Raphael\n- Niccolò Machiavelli\n\n## Developments\n- Humanism\n- Perspective in art\n- Scientific inquiry\n- Printing press"},
                
            {"World War II", "Modern History", 
                "# World War II\n\nWorld War II (1939-1945) was the deadliest global conflict in human history. This lesson examines the causes, major events, and consequences of this transformative war.\n\n## Topics\n- Rise of fascism and Nazism\n- Major battles and military strategies\n- The Holocaust\n- The atomic bomb and end of the war\n- Post-war order and the Cold War"},
                
            {"Civil Rights Movement", "American History", 
                "# Civil Rights Movement\n\nThe Civil Rights Movement in the United States was a decades-long struggle to end racial discrimination and segregation. This lesson explores key events, figures, and legislation.\n\n## Key Figures\n- Martin Luther King Jr.\n- Rosa Parks\n- Malcolm X\n\n## Important Events\n- Brown v. Board of Education\n- Montgomery Bus Boycott\n- March on Washington\n- Civil Rights Act of 1964"}
        };
        
        for (String[] lesson : lessons) {
            pstmt.setString(2, lesson[0]);  // title
            pstmt.setString(3, lesson[1]);  // category
            pstmt.setString(4, lesson[2]);  // content
            pstmt.executeUpdate();
            log("Added history lesson: " + lesson[0]);
        }
    }
    
    private void addComputerScienceLessons(PreparedStatement pstmt) throws SQLException {
        String[][] lessons = {
            {"Introduction to Programming", "Fundamentals", 
                "# Introduction to Programming\n\nThis lesson introduces the basic concepts of programming and computational thinking.\n\n## Topics\n- Variables and data types\n- Control structures (if/else, loops)\n- Functions and methods\n- Basic algorithms\n- Problem-solving techniques"},
                
            {"Data Structures", "Core Concepts", 
                "# Data Structures\n\nData structures are specialized formats for organizing and storing data. This lesson covers the most common data structures used in programming.\n\n## Data Structures Covered\n- Arrays and Lists\n- Stacks and Queues\n- Linked Lists\n- Trees and Binary Search Trees\n- Hash Tables\n- Graphs"},
                
            {"Object-Oriented Programming", "Programming Paradigms", 
                "# Object-Oriented Programming\n\nObject-Oriented Programming (OOP) is a programming paradigm based on the concept of objects. This lesson covers the principles and applications of OOP.\n\n## Key Concepts\n- Classes and Objects\n- Encapsulation\n- Inheritance\n- Polymorphism\n- Abstraction"},
                
            {"Database Systems", "Data Management", 
                "# Database Systems\n\nDatabase systems are used to store, retrieve, and manage data efficiently. This lesson covers relational databases and SQL.\n\n## Topics\n- Database design and normalization\n- SQL queries\n- Transactions and ACID properties\n- Indexing and optimization\n- NoSQL databases"},
                
            {"Web Development", "Applied Computing", 
                "# Web Development\n\nWeb development involves creating websites and web applications. This lesson introduces the fundamentals of front-end and back-end development.\n\n## Technologies\n- HTML, CSS, and JavaScript\n- Front-end frameworks\n- Server-side programming\n- RESTful APIs\n- Web security"}
        };
        
        for (String[] lesson : lessons) {
            pstmt.setString(2, lesson[0]);  // title
            pstmt.setString(3, lesson[1]);  // category
            pstmt.setString(4, lesson[2]);  // content
            pstmt.executeUpdate();
            log("Added computer science lesson: " + lesson[0]);
        }
    }
    
    private void addBiologyLessons(PreparedStatement pstmt) throws SQLException {
        String[][] lessons = {
            {"Cell Structure and Function", "Cellular Biology", 
                "# Cell Structure and Function\n\nThe cell is the basic unit of life. This lesson explores the structures within cells and their functions.\n\n## Cell Components\n- Cell membrane\n- Nucleus and genetic material\n- Mitochondria and energy production\n- Endoplasmic reticulum and Golgi apparatus\n- Lysosomes and cellular digestion"},
                
            {"Genetics and Inheritance", "Molecular Biology", 
                "# Genetics and Inheritance\n\nGenetics is the study of genes, heredity, and genetic variation. This lesson covers how traits are passed from parents to offspring.\n\n## Topics\n- DNA structure and replication\n- Genes and chromosomes\n- Mendelian inheritance\n- Gene expression\n- Genetic disorders"},
                
            {"Evolution and Natural Selection", "Evolutionary Biology", 
                "# Evolution and Natural Selection\n\nEvolution by natural selection explains how species change over time. This lesson explores Darwin's theory and the evidence supporting it.\n\n## Key Concepts\n- Natural selection and adaptation\n- Genetic variation and mutation\n- Speciation\n- Evidence for evolution\n- Human evolution"},
                
            {"Ecology and Ecosystems", "Environmental Biology", 
                "# Ecology and Ecosystems\n\nEcology is the study of interactions among organisms and their environment. This lesson covers ecosystem dynamics and environmental relationships.\n\n## Topics\n- Energy flow in ecosystems\n- Food webs and trophic levels\n- Biogeochemical cycles\n- Population dynamics\n- Biodiversity and conservation"},
                
            {"Human Anatomy and Physiology", "Human Biology", 
                "# Human Anatomy and Physiology\n\nThis lesson explores the structure and function of the human body, examining the various systems that work together to maintain life.\n\n## Body Systems\n- Cardiovascular system\n- Respiratory system\n- Digestive system\n- Nervous system\n- Endocrine system"}
        };
        
        for (String[] lesson : lessons) {
            pstmt.setString(2, lesson[0]);  // title
            pstmt.setString(3, lesson[1]);  // category
            pstmt.setString(4, lesson[2]);  // content
            pstmt.executeUpdate();
            log("Added biology lesson: " + lesson[0]);
        }
    }
    
    private void addPhysicsLessons(PreparedStatement pstmt) throws SQLException {
        String[][] lessons = {
            {"Mechanics", "Classical Physics", 
                "# Mechanics\n\nMechanics is the branch of physics dealing with the motion of objects and the forces acting on them. This lesson covers Newton's laws and applications.\n\n## Topics\n- Newton's Laws of Motion\n- Work, energy, and power\n- Momentum and collisions\n- Circular motion\n- Gravitation"},
                
            {"Thermodynamics", "Classical Physics", 
                "# Thermodynamics\n\nThermodynamics deals with heat, work, and temperature, and their relation to energy and entropy. This lesson explores the laws governing thermal phenomena.\n\n## Laws of Thermodynamics\n- Zeroth Law: Thermal equilibrium\n- First Law: Conservation of energy\n- Second Law: Entropy\n- Third Law: Absolute zero\n- Applications in heat engines and refrigeration"},
                
            {"Electricity and Magnetism", "Electromagnetism", 
                "# Electricity and Magnetism\n\nThis lesson covers electric charges, fields, currents, and magnetic phenomena, as well as the deep connection between electricity and magnetism.\n\n## Topics\n- Electric charge and Coulomb's Law\n- Electric fields and potential\n- Circuits and Ohm's Law\n- Magnetic fields and forces\n- Electromagnetic induction"},
                
            {"Waves and Optics", "Wave Physics", 
                "# Waves and Optics\n\nThis lesson explores mechanical and electromagnetic waves, including sound and light, and their properties and behaviors.\n\n## Wave Phenomena\n- Wave characteristics and propagation\n- Interference and diffraction\n- Reflection and refraction\n- Doppler effect\n- Lenses and optical instruments"},
                
            {"Quantum Mechanics", "Modern Physics", 
                "# Quantum Mechanics\n\nQuantum mechanics is the branch of physics dealing with atomic and subatomic systems. This lesson introduces the strange and counterintuitive world of quantum physics.\n\n## Topics\n- Wave-particle duality\n- Uncertainty principle\n- Schrödinger equation\n- Quantum tunneling\n- Quantum entanglement"}
        };
        
        for (String[] lesson : lessons) {
            pstmt.setString(2, lesson[0]);  // title
            pstmt.setString(3, lesson[1]);  // category
            pstmt.setString(4, lesson[2]);  // content
            pstmt.executeUpdate();
            log("Added physics lesson: " + lesson[0]);
        }
    }
    
    private void addLiteratureLessons(PreparedStatement pstmt) throws SQLException {
        String[][] lessons = {
            {"Introduction to Literary Analysis", "Fundamentals", 
                "# Introduction to Literary Analysis\n\nThis lesson introduces the fundamental concepts and techniques used to analyze and interpret literary texts.\n\n## Elements of Literature\n- Plot and conflict\n- Character development\n- Setting and atmosphere\n- Theme and symbolism\n- Narrative perspective"},
                
            {"Shakespearean Drama", "Classical Literature", 
                "# Shakespearean Drama\n\nWilliam Shakespeare is considered one of the greatest playwrights in history. This lesson explores his dramatic works and enduring legacy.\n\n## Major Works\n- Tragedies: Hamlet, Macbeth, King Lear\n- Comedies: A Midsummer Night's Dream, Much Ado About Nothing\n- Historical plays: Henry V, Richard III\n- Romances: The Tempest\n\n## Themes and Techniques\n- Iambic pentameter\n- Soliloquies and asides\n- Character complexity\n- Universal themes"},
                
            {"19th Century Novel", "Western Literature", 
                "# 19th Century Novel\n\nThe 19th century saw the rise of the novel as a dominant literary form. This lesson examines key authors and works from this influential period.\n\n## Major Authors\n- Jane Austen\n- Charles Dickens\n- Fyodor Dostoevsky\n- Leo Tolstoy\n- Mark Twain\n\n## Literary Movements\n- Romanticism\n- Realism\n- Naturalism"},
                
            {"Modernist Poetry", "Modern Literature", 
                "# Modernist Poetry\n\nModernist poetry emerged in the early 20th century as a response to industrialization and World War I. This lesson explores the innovative techniques and themes of modernist poets.\n\n## Key Poets\n- T.S. Eliot\n- Ezra Pound\n- W.B. Yeats\n- Wallace Stevens\n\n## Techniques\n- Free verse\n- Imagism\n- Fragmentation\n- Stream of consciousness"},
                
            {"Contemporary World Literature", "Global Literature", 
                "# Contemporary World Literature\n\nThis lesson explores diverse literary voices from around the world in the late 20th and early 21st centuries.\n\n## Notable Authors\n- Gabriel García Márquez (Colombia)\n- Chimamanda Ngozi Adichie (Nigeria)\n- Haruki Murakami (Japan)\n- Salman Rushdie (India/UK)\n\n## Themes\n- Postcolonialism\n- Globalization\n- Identity and diaspora\n- Magical realism"}
        };
        
        for (String[] lesson : lessons) {
            pstmt.setString(2, lesson[0]);  // title
            pstmt.setString(3, lesson[1]);  // category
            pstmt.setString(4, lesson[2]);  // content
            pstmt.executeUpdate();
            log("Added literature lesson: " + lesson[0]);
        }
    }
    
    private void addDefaultLessons(PreparedStatement pstmt, String courseName) throws SQLException {
        String[][] lessons = {
            {"Introduction to " + courseName, "Fundamentals", 
                "# Introduction to " + courseName + "\n\nThis lesson provides an overview of the key concepts and principles in " + courseName + ".\n\n## Topics Covered\n- Basic terminology and definitions\n- Historical context and development\n- Core principles\n- Modern applications\n- Research methodologies"},
                
            {"Foundational Concepts", "Core Concepts", 
                "# Foundational Concepts in " + courseName + "\n\nThis lesson explores the essential theories and ideas that form the foundation of " + courseName + ".\n\n## Key Concepts\n- Theoretical frameworks\n- Fundamental principles\n- Critical perspectives\n- Analytical approaches"},
                
            {"Practical Applications", "Applications", 
                "# Practical Applications of " + courseName + "\n\nThis lesson examines how the principles of " + courseName + " are applied in real-world contexts.\n\n## Application Areas\n- Professional practices\n- Industry standards\n- Case studies\n- Problem-solving approaches\n- Ethical considerations"},
                
            {"Advanced Topics", "Advanced", 
                "# Advanced Topics in " + courseName + "\n\nThis lesson delves into more complex and specialized areas within " + courseName + ".\n\n## Advanced Subjects\n- Cutting-edge research\n- Specialized methodologies\n- Complex problem domains\n- Interdisciplinary connections\n- Future directions"},
                
            {"Research and Current Trends", "Contemporary Issues", 
                "# Research and Current Trends in " + courseName + "\n\nThis lesson explores current research, emerging trends, and contemporary issues in " + courseName + ".\n\n## Current Developments\n- Recent discoveries\n- Ongoing debates\n- Technological innovations\n- Evolving practices\n- Future challenges and opportunities"}
        };
        
        for (String[] lesson : lessons) {
            pstmt.setString(2, lesson[0]);  // title
            pstmt.setString(3, lesson[1]);  // category
            pstmt.setString(4, lesson[2]);  // content
            pstmt.executeUpdate();
            log("Added default lesson: " + lesson[0]);
        }
    }
    
    private void log(String message) {
        logArea.appendText(message + "\n");
    }
}