package scene1;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.*;

import java.sql.*;

public class GradeListController {

    @FXML
    private Label lagradelistcourse;

    @FXML
    private TableView<Grade> tvgradelist;
    @FXML
    private TableColumn<Grade, String> tcgradeid;
    @FXML
    private TableColumn<Grade, String> tcgradename;
    @FXML
    private TableColumn<Grade, Integer> tagraderegular;
    @FXML
    private TableColumn<Grade, Integer> tcgrademid;
    @FXML
    private TableColumn<Grade, Integer> tcgradefin;
    @FXML
    private TableColumn<Grade, Double> tcgradesum;
    @FXML
    private TableColumn<Grade, String> tcgradestatus;

    @FXML
    private TextField tfgradeinputname;
    @FXML
    private Button btngradelistcc;
    @FXML
    private Button btngradelistdelete;

    private final DBHelper db = new DBHelper();
    private final ObservableList<Grade> data = FXCollections.observableArrayList();

    private String courseId;
    private String courseName;

    /* ===== 初始化 ===== */
    @FXML
    public void initialize() {
        tcgradeid.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        tcgradename.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        tagraderegular.setCellValueFactory(new PropertyValueFactory<>("quiz"));
        tcgrademid.setCellValueFactory(new PropertyValueFactory<>("midterm"));
        tcgradefin.setCellValueFactory(new PropertyValueFactory<>("finalExam"));
        tcgradesum.setCellValueFactory(new PropertyValueFactory<>("total"));
        tcgradestatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    /* ===== 由外部設定課程 ===== */
    public void setCourse(String courseId) {
        this.courseId = courseId;
        if (courseId != null) {
            loadCourseName();
            loadGrades();
        }
    }

    private void loadCourseName() {
        try (Connection c = db.dbConnect()) {
            PreparedStatement ps = c.prepareStatement("SELECT course_name FROM courses WHERE course_id=?");
            ps.setString(1, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                courseName = rs.getString(1);
                lagradelistcourse.setText("課程：" + courseName);
            } else {
                courseName = "";
                lagradelistcourse.setText("課程：未找到");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===== 載入成績 ===== */
    public void loadGrades() {
        if (courseId == null)
            return;

        data.clear();
        String keyword = tfgradeinputname.getText().trim();
        String sql = """
                SELECT g.*, s.name
                FROM grades g
                JOIN students s ON g.student_id = s.student_id
                WHERE g.course_id = ?
                AND (s.name LIKE ? OR s.student_id LIKE ?)
                ORDER BY g.id ASC
                """;

        try (Connection c = db.dbConnect();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, courseId);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Grade(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        courseId,
                        courseName,
                        rs.getInt("quiz"),
                        rs.getInt("midterm"),
                        rs.getInt("final"),
                        rs.getDouble("total"),
                        rs.getString("status")));
            }
            tvgradelist.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "成績載入失敗").showAndWait();
        }
    }

    /* ===== 新增 / 修改 ===== */
    @FXML
    private void openGradeEdit() {
        if (courseId == null)
            return;

        Grade selected = tvgradelist.getSelectionModel().getSelectedItem();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GradeEdit.fxml"));
            Parent root = loader.load();

            GradeEditController ctrl = loader.getController();
            ctrl.init(courseId, selected);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(selected == null ? "新增成績" : "修改成績");
            stage.setResizable(false);
            stage.showAndWait();

            loadGrades(); // 即時更新

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "無法打開成績編輯視窗").showAndWait();
        }
    }

    /* ===== 刪除 ===== */
    @FXML
    private void deleteGrade() {
        Grade g = tvgradelist.getSelectionModel().getSelectedItem();
        if (g == null) {
            new Alert(Alert.AlertType.WARNING, "請先選擇一筆成績").showAndWait();
            return;
        }

        try (Connection c = db.dbConnect()) {
            PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM grades WHERE student_id=? AND course_id=?");
            ps.setString(1, g.getStudentId());
            ps.setString(2, courseId);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                loadGrades();
            } else {
                new Alert(Alert.AlertType.WARNING, "刪除失敗，請確認資料存在").showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "刪除失敗").showAndWait();
        }
    }
}
