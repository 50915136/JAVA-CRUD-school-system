package scene1;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class GradeEditController {

    @FXML
    private ComboBox<String> cmbgradeeditname;
    @FXML
    private TextField tfgradeeditregular;
    @FXML
    private TextField tfgradeeditmid;
    @FXML
    private TextField tfgradeeditfin;
    @FXML
    private TextField tfgradeeditavg;

    private final DBHelper db = new DBHelper();
    private Grade editing;
    private String courseId;

    /* ===== 初始化（新增 / 修改共用） ===== */
    public void init(String courseId, Grade grade) {
        this.courseId = courseId;
        this.editing = grade;

        if (grade == null) {
            loadStudentsForInsert();
        } else {
            loadEditData();
        }
    }

    /* ===== 新增狀態 ===== */
    private void loadStudentsForInsert() {
        cmbgradeeditname.getItems().clear();
        try (Connection c = db.dbConnect();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT name FROM students " +
                                "WHERE student_id NOT IN " +
                                "(SELECT student_id FROM grades WHERE course_id=?) " +
                                "ORDER BY student_id ASC")) {

            ps.setString(1, courseId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                cmbgradeeditname.getItems().add(rs.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "載入學生資料失敗").showAndWait();
        }
    }

    /* ===== 修改狀態 ===== */
    private void loadEditData() {
        cmbgradeeditname.getItems().clear();
        cmbgradeeditname.getItems().add(editing.getStudentName());
        cmbgradeeditname.setValue(editing.getStudentName());
        cmbgradeeditname.setDisable(true);

        tfgradeeditregular.setText(String.valueOf(editing.getQuiz()));
        tfgradeeditmid.setText(String.valueOf(editing.getMidterm()));
        tfgradeeditfin.setText(String.valueOf(editing.getFinalExam()));
        tfgradeeditavg.setText(String.valueOf(editing.getTotal()));
    }

    /* ===== 儲存 ===== */
    @FXML
    private void save() {
        try {
            // 欄位驗證
            String name = cmbgradeeditname.getValue();
            if (name == null || name.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "請選擇學生").showAndWait();
                return;
            }

            int q = parseScore(tfgradeeditregular.getText(), "平時成績");
            int m = parseScore(tfgradeeditmid.getText(), "期中成績");
            int f = parseScore(tfgradeeditfin.getText(), "期末成績");

            double total = q * 0.3 + m * 0.3 + f * 0.4;
            String status = total >= 60 ? "及格" : "不及格";
            tfgradeeditavg.setText(String.format("%.2f", total));

            try (Connection c = db.dbConnect()) {
                String studentId = getStudentId(c, name);

                if (editing == null) {
                    // 防止重複新增
                    if (gradeExists(c, studentId, courseId)) {
                        new Alert(Alert.AlertType.WARNING, "該學生已存在此課程成績").showAndWait();
                        return;
                    }

                    PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO grades(student_id, course_id, quiz, midterm, final, total, status) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?)");
                    ps.setString(1, studentId);
                    ps.setString(2, courseId);
                    ps.setInt(3, q);
                    ps.setInt(4, m);
                    ps.setInt(5, f);
                    ps.setDouble(6, total);
                    ps.setString(7, status);
                    ps.executeUpdate();

                } else {
                    PreparedStatement ps = c.prepareStatement(
                            "UPDATE grades SET quiz=?, midterm=?, final=?, total=?, status=? " +
                                    "WHERE student_id=? AND course_id=?");
                    ps.setInt(1, q);
                    ps.setInt(2, m);
                    ps.setInt(3, f);
                    ps.setDouble(4, total);
                    ps.setString(5, status);
                    ps.setString(6, studentId);
                    ps.setString(7, courseId);
                    ps.executeUpdate();
                }
            }

            close();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "儲存失敗").showAndWait();
        }
    }

    /* ===== 解析成績並驗證範圍 ===== */
    private int parseScore(String text, String fieldName) throws Exception {
        try {
            int val = Integer.parseInt(text);
            if (val < 0 || val > 100)
                throw new NumberFormatException();
            return val;
        } catch (NumberFormatException e) {
            throw new Exception(fieldName + "必須為 0~100 的整數");
        }
    }

    /* ===== 檢查學生是否已存在該課程 ===== */
    private boolean gradeExists(Connection c, String studentId, String courseId) throws SQLException {
        PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM grades WHERE student_id=? AND course_id=?");
        ps.setString(1, studentId);
        ps.setString(2, courseId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    /* ===== 透過學生姓名取得 ID ===== */
    private String getStudentId(Connection c, String name) throws SQLException {
        PreparedStatement ps = c.prepareStatement("SELECT student_id FROM students WHERE name=?");
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return rs.getString("student_id");
        else
            throw new SQLException("找不到學生 ID");
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        ((Stage) tfgradeeditfin.getScene().getWindow()).close();
    }
}
