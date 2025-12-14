package scene1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class CourseEditController {

    @FXML
    private TextField tfcourseeditid;
    @FXML
    private TextField tfcourseeditname;
    @FXML
    private ComboBox<Integer> cmbcourseeditcredit;
    @FXML
    private ComboBox<String> cmbcourseeditstatus;

    private final DBHelper db = new DBHelper();
    private courseMainController mainController;
    private String teacherId;
    private boolean editMode = false;

    /** 初始化 ComboBox */
    @FXML
    public void initialize() {
        cmbcourseeditcredit.getItems().addAll(1, 2, 3);
        cmbcourseeditstatus.getItems().addAll("準備中", "進行中", "已結束");
    }

    public void setMainController(courseMainController controller) {
        this.mainController = controller;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    /** 設定修改模式課程 */
    public void setEditCourse(Course course) {
        editMode = true;

        tfcourseeditid.setText(course.getCourseId());
        tfcourseeditid.setDisable(true);
        tfcourseeditname.setText(course.getCourseName());
        cmbcourseeditcredit.setValue(course.getCredit());
        cmbcourseeditstatus.setValue(course.getStatus());
    }

    /** 儲存（新增或修改） */
    @FXML
    public void save() {
        // -----------------------------
        // 權限檢查
        // -----------------------------
        if (teacherId == null || teacherId.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "你沒有權限操作課程").showAndWait();
            return;
        }

        String id = tfcourseeditid.getText().trim();
        String name = tfcourseeditname.getText().trim();
        Integer credit = cmbcourseeditcredit.getValue();
        String status = cmbcourseeditstatus.getValue();

        // -----------------------------
        // 欄位檢查
        // -----------------------------
        if (id.isEmpty() || name.isEmpty() || credit == null || status == null) {
            new Alert(Alert.AlertType.WARNING, "請完整填寫所有欄位").showAndWait();
            return;
        }

        if (credit < 1 || credit > 3) {
            new Alert(Alert.AlertType.WARNING, "學分數必須介於 1~3").showAndWait();
            return;
        }

        try (Connection conn = db.dbConnect()) {
            if (editMode) {
                // -----------------------------
                // 修改課程
                // -----------------------------
                String sql = "UPDATE courses SET course_name=?, credit=?, status=? WHERE course_id=? AND teacher_id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setInt(2, credit);
                ps.setString(3, status);
                ps.setString(4, id);
                ps.setString(5, teacherId);

                int rows = ps.executeUpdate();
                if (rows == 0) {
                    new Alert(Alert.AlertType.ERROR, "課程不存在或沒有權限修改").showAndWait();
                    return;
                }

            } else {
                // -----------------------------
                // 新增課程前檢查代號重複
                // -----------------------------
                String checkSql = "SELECT COUNT(*) FROM courses WHERE course_id=?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, id);
                ResultSet rsCheck = checkStmt.executeQuery();
                if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                    new Alert(Alert.AlertType.WARNING, "課程代號已存在，請更換").showAndWait();
                    return;
                }

                // -----------------------------
                // 新增課程 + 建立 grades
                // -----------------------------
                conn.setAutoCommit(false);

                String insertCourse = "INSERT INTO courses(course_id, course_name, credit, status, teacher_id) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(insertCourse);
                ps.setString(1, id);
                ps.setString(2, name);
                ps.setInt(3, credit);
                ps.setString(4, status);
                ps.setString(5, teacherId);
                ps.executeUpdate();

                // 新增後自動給所有學生建 grades
                ResultSet rs = conn.createStatement().executeQuery("SELECT student_id FROM students");
                PreparedStatement psGrade = conn
                        .prepareStatement("INSERT INTO grades(student_id, course_id) VALUES (?, ?)");
                while (rs.next()) {
                    psGrade.setString(1, rs.getString("student_id"));
                    psGrade.setString(2, id);
                    psGrade.executeUpdate();
                }

                conn.commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "儲存失敗").showAndWait();
            return;
        }

        mainController.loadCourses();
        close();
    }

    /** 關閉視窗 */
    @FXML
    public void close() {
        Stage stage = (Stage) tfcourseeditid.getScene().getWindow();
        stage.close();
    }
}
