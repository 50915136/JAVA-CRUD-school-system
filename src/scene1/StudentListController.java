package scene1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;

public class StudentListController {

    private final DBHelper dbHelper = new DBHelper();

    @FXML
    private Label lbstudentlistname;
    @FXML
    private TextField tfstudentSearch;
    @FXML
    private Button btnstudentSearch;
    @FXML
    private Button btnstudentcc;
    @FXML
    private Button btnstudentdelete;

    @FXML
    private TableView<Student> tvstudentlist;
    @FXML
    private TableColumn<Student, String> tcstudentid;
    @FXML
    private TableColumn<Student, String> tcstudentname;
    @FXML
    private TableColumn<Student, String> tcstudentlistmow;
    @FXML
    private TableColumn<Student, Integer> tcstudentage;
    @FXML
    private TableColumn<Student, String> tcstudentstatus;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 初始化 TableView
        tcstudentid.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcstudentname.setCellValueFactory(new PropertyValueFactory<>("name"));
        tcstudentlistmow.setCellValueFactory(new PropertyValueFactory<>("gender"));
        tcstudentage.setCellValueFactory(new PropertyValueFactory<>("age"));
        tcstudentstatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 初始化按鈕事件
        btnstudentSearch.setOnAction(e -> loadStudents());
        btnstudentcc.setOnAction(e -> openStudentEdit());
        btnstudentdelete.setOnAction(e -> deleteStudent());

        // 設定登入者資訊
        lbstudentlistname.setText("登入者: " + UserSession.getUserName() +
                (UserSession.isTeacher() ? " 老師" : " 學生"));

        // 教師可用按鈕，學生不可見
        btnstudentcc.setVisible(UserSession.isTeacher());
        btnstudentdelete.setVisible(UserSession.isTeacher());

        // 載入資料
        loadStudents();
    }

    private void loadStudents() {
        studentList.clear();

        String keyword = tfstudentSearch.getText().trim();
        boolean isSearch = !keyword.isEmpty();

        String sql = isSearch
                ? "SELECT * FROM students WHERE student_id LIKE ? OR name LIKE ? ORDER BY student_id ASC"
                : "SELECT * FROM students ORDER BY student_id ASC";

        try (Connection conn = dbHelper.dbConnect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (isSearch) {
                String key = "%" + keyword + "%";
                stmt.setString(1, key);
                stmt.setString(2, key);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                studentList.add(new Student(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getInt("age"),
                        rs.getString("status")));
            }

            tvstudentlist.setItems(studentList);

        } catch (SQLException e) {
            showError("資料讀取錯誤：\n" + e.getMessage());
        }
    }

    private void openStudentEdit() {
        if (!UserSession.isTeacher())
            return;

        Student selected = tvstudentlist.getSelectionModel().getSelectedItem();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("studentEdit.fxml"));
            Parent root = loader.load();
            studentEditController controller = loader.getController();

            controller.setEditMode(selected != null, selected);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(selected != null ? "修改學生資料" : "新增學生資料");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            loadStudents();
        } catch (Exception e) {
            e.printStackTrace();
            showError("無法開啟編輯視窗：\n" + e.getMessage());
        }
    }

    private void deleteStudent() {
        if (!UserSession.isTeacher())
            return;

        Student selected = tvstudentlist.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("請先選擇要刪除的學生！");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "確定要刪除學生：" + selected.getName() + "（" + selected.getId() + "）？\n此動作將刪除 users 與 students 還有 grades 資料！");
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.OK)
            return;

        try (Connection conn = dbHelper.dbConnect()) {
            PreparedStatement ps1 = conn.prepareStatement("DELETE FROM students WHERE student_id=?");
            ps1.setString(1, selected.getId());
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement("DELETE FROM users WHERE user_id=?");
            ps2.setString(1, selected.getId());
            ps2.executeUpdate();

            showInfo("學生資料已成功刪除！");
            loadStudents();

        } catch (SQLException e) {
            showError("刪除失敗：\n" + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
