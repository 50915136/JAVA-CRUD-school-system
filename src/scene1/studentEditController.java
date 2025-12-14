package scene1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class studentEditController {

    @FXML
    private TextField cmstudenteditid;
    @FXML
    private ComboBox<String> cmstudenteditname;
    @FXML
    private ComboBox<String> cmstudenteditgender;
    @FXML
    private TextField inputage;
    @FXML
    private ComboBox<String> cominputstudy;

    @FXML
    private Button editsave;
    @FXML
    private Button editexit;

    private boolean isEditMode; // true = 修改, false = 新增
    private Student editingStudent; // 修改模式下接收的學生資料

    private final DBHelper dbHelper = new DBHelper();

    // ===========================================================
    // 設定新增/修改模式
    // ===========================================================
    public void setEditMode(boolean isEdit, Student student) {
        // 只有教師可以新增/修改
        if (!UserSession.isTeacher()) {
            showAlert("您沒有權限操作此功能！");
            closeWindow();
            return;
        }

        this.isEditMode = isEdit;
        this.editingStudent = student;

        // 性別選項
        cmstudenteditgender.getItems().addAll("男", "女");

        // 在學狀態選項
        cominputstudy.getItems().addAll("在學", "休學", "畢業", "肄業", "延畢");

        if (isEdit) {
            // 修改模式：載入資料
            cmstudenteditid.setText(student.getId());
            cmstudenteditid.setDisable(true); // 學號不可改

            cmstudenteditname.getItems().add(student.getName());
            cmstudenteditname.getSelectionModel().select(student.getName());

            cmstudenteditgender.getSelectionModel().select(student.getGender());
            inputage.setText(String.valueOf(student.getAge()));
            cominputstudy.getSelectionModel().select(student.getStatus());

        } else {
            // 新增模式：資料為空，並載入 users 裡還未加入 students 的學生
            cmstudenteditid.setDisable(false);
            loadAvailableUsers();
        }
    }

    // ===========================================================
    // 載入 users 裡的學生（role='student'）但未加入 students 的清單
    // ===========================================================
    private void loadAvailableUsers() {
        cmstudenteditname.getItems().clear();

        try (Connection conn = dbHelper.dbConnect()) {
            String sql = """
                        SELECT u.user_id, u.name
                        FROM users u
                        WHERE u.role = 'student'
                        AND NOT EXISTS (
                            SELECT 1 FROM students s WHERE s.student_id = u.user_id
                        )
                    """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                cmstudenteditname.getItems().add(rs.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===========================================================
    // 儲存（新增 or 修改）
    // ===========================================================
    @FXML
    private void onSave() {
        // 權限檢查
        if (!UserSession.isTeacher()) {
            showAlert("您沒有權限操作此功能！");
            return;
        }

        try (Connection conn = dbHelper.dbConnect()) {

            String id = cmstudenteditid.getText();
            String name = cmstudenteditname.getValue();
            String gender = cmstudenteditgender.getValue();
            String age = inputage.getText();
            String status = cominputstudy.getValue();

            // 基礎資料檢查
            if (id.isEmpty() || name == null || gender == null || age.isEmpty() || status == null) {
                showAlert("請完整填寫所有欄位！");
                return;
            }

            if (isEditMode) {
                // 修改 students 資料
                String sql = """
                            UPDATE students
                            SET name=?, gender=?, age=?, status=?
                            WHERE student_id=?
                        """;

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, gender);
                ps.setInt(3, Integer.parseInt(age));
                ps.setString(4, status);
                ps.setString(5, id);
                ps.executeUpdate();

                showAlert("學生資料更新成功！");

            } else {
                // 新增 students 資料
                String sql = """
                            INSERT INTO students(student_id, name, gender, age, status)
                            VALUES (?, ?, ?, ?, ?)
                        """;

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, id);
                ps.setString(2, name);
                ps.setString(3, gender);
                ps.setInt(4, Integer.parseInt(age));
                ps.setString(5, status);
                ps.executeUpdate();

                showAlert("新增學生成功！");
            }

            // 關閉視窗
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("資料儲存失敗，請檢查資料庫設定！");
        }
    }

    // ===========================================================
    // 取消按鈕
    // ===========================================================
    @FXML
    private void onExit() {
        closeWindow();
    }

    // ===========================================================
    // 工具：關閉視窗
    // ===========================================================
    private void closeWindow() {
        Stage stage = (Stage) editexit.getScene().getWindow();
        stage.close();
    }

    // ===========================================================
    // 工具：顯示提示訊息
    // ===========================================================
    private void showAlert(String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
