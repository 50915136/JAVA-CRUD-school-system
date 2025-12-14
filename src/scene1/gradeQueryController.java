package scene1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.sql.*;

public class gradeQueryController implements StudentManagerController.UserAware {

    @FXML
    private Label lagradeQueryname;

    @FXML
    private TableView<Grade> gradeTable;
    @FXML
    private TableColumn<Grade, String> tcgradeQueryid;
    @FXML
    private TableColumn<Grade, String> tcgradeQueryname;
    @FXML
    private TableColumn<Grade, Integer> tcgradeQueryregular;
    @FXML
    private TableColumn<Grade, Integer> tcgradeQuerymid;
    @FXML
    private TableColumn<Grade, Integer> tcgradeQueryfin;
    @FXML
    private TableColumn<Grade, Double> tcgradeQuerytotal;
    @FXML
    private TableColumn<Grade, String> tcgradeQuerystatus;

    private final DBHelper db = new DBHelper();
    private String studentId;
    private String studentName;

    /** 接收學生 ID 並載入資料 */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        loadStudentName();
        loadGrades();
    }

    @FXML
    public void initialize() {
        // 設定 TableColumn 對應屬性
        tcgradeQueryid.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        tcgradeQueryname.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        tcgradeQueryregular.setCellValueFactory(new PropertyValueFactory<>("quiz"));
        tcgradeQuerymid.setCellValueFactory(new PropertyValueFactory<>("midterm"));
        tcgradeQueryfin.setCellValueFactory(new PropertyValueFactory<>("finalExam"));
        tcgradeQuerytotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        tcgradeQuerystatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 自定義顏色渲染
        setRedIfFail(tcgradeQueryregular);
        setRedIfFail(tcgradeQuerymid);
        setRedIfFail(tcgradeQueryfin);
        setRedIfFailDouble(tcgradeQuerytotal);
        setStatusColor(tcgradeQuerystatus);
    }

    private void setRedIfFail(TableColumn<Grade, Integer> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                } else {
                    setText(String.valueOf(value));
                    setTextFill(value < 60 ? Color.RED : Color.BLACK);
                }
            }
        });
    }

    private void setRedIfFailDouble(TableColumn<Grade, Double> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                } else {
                    setText(String.format("%.1f", value));
                    setTextFill(value < 60 ? Color.RED : Color.BLACK);
                }
            }
        });
    }

    private void setStatusColor(TableColumn<Grade, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                } else {
                    setText(value);
                    setTextFill("及格".equals(value) ? Color.GREEN : Color.RED);
                }
            }
        });
    }

    /** UserAware 介面方法，登入學生時會自動呼叫 */
    @Override
    public void setUserSession() {
        if (UserSession.isStudent()) {
            setStudentId(UserSession.getUserId());
        }
    }

    /** 取得學生姓名並顯示 */
    private void loadStudentName() {
        String sql = "SELECT name FROM students WHERE student_id = ?";
        try (Connection conn = db.dbConnect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                studentName = rs.getString("name");
                lagradeQueryname.setText("登入者: " + studentName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** 載入該學生的成績 */
    private void loadGrades() {
        if (studentId == null || studentName == null)
            return;

        ObservableList<Grade> list = FXCollections.observableArrayList();
        String sql = """
                SELECT g.course_id, c.course_name, g.quiz, g.midterm, g.final, g.total, g.status
                FROM grades g
                JOIN courses c ON g.course_id = c.course_id
                WHERE g.student_id = ?
                ORDER BY g.course_id ASC
                """;

        try (Connection conn = db.dbConnect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Grade(
                        studentId,
                        studentName,
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("quiz"),
                        rs.getInt("midterm"),
                        rs.getInt("final"),
                        rs.getDouble("total"),
                        rs.getString("status")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        gradeTable.setItems(list);
    }
}
