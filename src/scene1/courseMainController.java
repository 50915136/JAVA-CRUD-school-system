package scene1;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class courseMainController {

    @FXML
    private TableView<Course> courseTable;
    @FXML
    private TableColumn<Course, String> courseid;
    @FXML
    private TableColumn<Course, String> coursename;
    @FXML
    private TableColumn<Course, Integer> coursefan;
    @FXML
    private TableColumn<Course, String> coursestate;

    @FXML
    private TextField coursesearchfield;

    @FXML
    private Button btncoursemaincc;
    @FXML
    private Button btncoursemaindelete;
    @FXML
    private Button btncoursemaingrade;

    private final DBHelper db = new DBHelper();
    private String teacherId;

    @FXML
    public void initialize() {
        // 設定 TableColumn 對應屬性
        courseid.setCellValueFactory(c -> c.getValue().courseIdProperty());
        coursename.setCellValueFactory(c -> c.getValue().courseNameProperty());
        coursefan.setCellValueFactory(c -> c.getValue().creditProperty().asObject());
        coursestate.setCellValueFactory(c -> c.getValue().statusProperty());

        // 使用 UserSession 判斷按鈕權限
        if (UserSession.isTeacher()) {
            teacherId = UserSession.getUserId();
            btncoursemaincc.setDisable(false);
            btncoursemaindelete.setDisable(false);
            btncoursemaingrade.setDisable(false);
        } else {
            teacherId = null; // 學生沒有 teacherId
            btncoursemaincc.setDisable(true);
            btncoursemaindelete.setDisable(true);
            btncoursemaingrade.setDisable(true);
        }

        // 綁定成績管理按鈕事件
        btncoursemaingrade.setOnAction(e -> openGradeList());

        loadCourses();
    }

    /** 載入課程 */
    @FXML
    public void loadCourses() {
        ObservableList<Course> list = FXCollections.observableArrayList();
        String keyword = coursesearchfield.getText().trim();

        String sql;
        if (UserSession.isTeacher()) {
            sql = keyword.isEmpty()
                    ? "SELECT course_id, course_name, credit, status FROM courses WHERE teacher_id=? ORDER BY course_id ASC"
                    : "SELECT course_id, course_name, credit, status FROM courses WHERE teacher_id=? AND (course_id LIKE ? OR course_name LIKE ?) ORDER BY course_id ASC";
        } else {
            // 學生可以查看所有課程，搜尋可用
            sql = keyword.isEmpty()
                    ? "SELECT course_id, course_name, credit, status FROM courses ORDER BY course_id ASC"
                    : "SELECT course_id, course_name, credit, status FROM courses WHERE course_id LIKE ? OR course_name LIKE ? ORDER BY course_id ASC";
        }

        try (Connection conn = db.dbConnect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            if (UserSession.isTeacher()) {
                ps.setString(1, teacherId);
                if (!keyword.isEmpty()) {
                    ps.setString(2, "%" + keyword + "%");
                    ps.setString(3, "%" + keyword + "%");
                }
            } else {
                if (!keyword.isEmpty()) {
                    ps.setString(1, "%" + keyword + "%");
                    ps.setString(2, "%" + keyword + "%");
                }
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Course(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("credit"),
                        rs.getString("status")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        courseTable.setItems(list);
    }

    /** 新增 / 修改課程（教師才可用） */
    @FXML
    public void openCourseEdit() {
        if (!UserSession.isTeacher())
            return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CourseEdit.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            CourseEditController controller = loader.getController();
            controller.setMainController(this);
            controller.setTeacherId(teacherId);

            Course selected = courseTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.setEditCourse(selected); // 修改模式
            }

            stage.setTitle("課程編輯");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 刪除課程（教師才可用） */
    @FXML
    public void deleteCourse() {
        if (!UserSession.isTeacher())
            return;

        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        try (Connection conn = db.dbConnect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM grades WHERE course_id=?");
                    PreparedStatement ps2 = conn
                            .prepareStatement("DELETE FROM courses WHERE course_id=? AND teacher_id=?")) {

                ps1.setString(1, selected.getCourseId());
                ps1.executeUpdate();

                ps2.setString(1, selected.getCourseId());
                ps2.setString(2, teacherId);
                ps2.executeUpdate();

                conn.commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        loadCourses();
    }

    /** 成績管理（教師才可用） */
    @FXML
    public void openGradeList() {
        if (!UserSession.isTeacher())
            return;

        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "請先選擇一門課程！");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GradeList.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            GradeListController controller = loader.getController();
            controller.setCourse(selected.getCourseId()); // 傳遞課程 ID

            stage.setTitle("課程成績管理 - " + selected.getCourseName());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
