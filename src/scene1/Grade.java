package scene1;

import javafx.beans.property.*;

public class Grade {

    private final StringProperty studentId;
    private final StringProperty studentName;
    private final StringProperty courseId;
    private final StringProperty courseName;
    private final IntegerProperty quiz;
    private final IntegerProperty midterm;
    private final IntegerProperty finalExam; // 對應資料庫 final
    private final DoubleProperty total;
    private final StringProperty status;

    public Grade(String studentId, String studentName,
            String courseId, String courseName,
            int quiz, int midterm, int finalExam,
            double total, String status) {

        this.studentId = new SimpleStringProperty(studentId);
        this.studentName = new SimpleStringProperty(studentName);
        this.courseId = new SimpleStringProperty(courseId);
        this.courseName = new SimpleStringProperty(courseName);
        this.quiz = new SimpleIntegerProperty(quiz);
        this.midterm = new SimpleIntegerProperty(midterm);
        this.finalExam = new SimpleIntegerProperty(finalExam);
        this.total = new SimpleDoubleProperty(total);
        this.status = new SimpleStringProperty(status);
    }

    // ===== student =====
    public String getStudentId() {
        return studentId.get();
    }

    public StringProperty studentIdProperty() {
        return studentId;
    }

    public String getStudentName() {
        return studentName.get();
    }

    public StringProperty studentNameProperty() {
        return studentName;
    }

    // ===== course =====
    public String getCourseId() {
        return courseId.get();
    }

    public StringProperty courseIdProperty() {
        return courseId;
    }

    public String getCourseName() {
        return courseName.get();
    }

    public StringProperty courseNameProperty() {
        return courseName;
    }

    // ===== grades =====
    public int getQuiz() {
        return quiz.get();
    }

    public IntegerProperty quizProperty() {
        return quiz;
    }

    public int getMidterm() {
        return midterm.get();
    }

    public IntegerProperty midtermProperty() {
        return midterm;
    }

    // ⚠ 關鍵：不是 getFinal()
    public int getFinalExam() {
        return finalExam.get();
    }

    public IntegerProperty finalExamProperty() {
        return finalExam;
    }

    public double getTotal() {
        return total.get();
    }

    public DoubleProperty totalProperty() {
        return total;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }
}
