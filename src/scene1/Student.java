package scene1;

import javafx.beans.property.*;

public class Student {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty gender;
    private final IntegerProperty age;
    private final StringProperty status;

    public Student(String id, String name, String gender, int age, String status) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.gender = new SimpleStringProperty(gender);
        this.age = new SimpleIntegerProperty(age);
        this.status = new SimpleStringProperty(status);
    }

    public StringProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty genderProperty() {
        return gender;
    }

    public IntegerProperty ageProperty() {
        return age;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public String getGender() {
        return gender.get();
    }

    public int getAge() {
        return age.get();
    }

    public String getStatus() {
        return status.get();
    }
}
