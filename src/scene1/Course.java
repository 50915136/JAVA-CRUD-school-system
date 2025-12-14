package scene1;

import javafx.beans.property.*;

public class Course {

    private final StringProperty courseId;
    private final StringProperty courseName;
    private final IntegerProperty credit;
    private final StringProperty status;

    public Course(String courseId, String courseName, int credit, String status) {
        this.courseId = new SimpleStringProperty(courseId);
        this.courseName = new SimpleStringProperty(courseName);
        this.credit = new SimpleIntegerProperty(credit);
        this.status = new SimpleStringProperty(status);
    }

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

    public int getCredit() {
        return credit.get();
    }

    public IntegerProperty creditProperty() {
        return credit;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }
}
