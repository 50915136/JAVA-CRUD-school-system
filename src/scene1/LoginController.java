package scene1;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private final DBHelper db = new DBHelper();

    @FXML
    public void handleLogin() {
        String userId = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        String sql = "SELECT name, role FROM users WHERE user_id=? AND password=?";

        try (Connection conn = db.dbConnect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String role = rs.getString("role");

                // ✅ 存入全域登入狀態
                UserSession.set(userId, name, role);

                // ✅ 切換到主系統畫面（同一個 Stage）
                switchToMain();

            } else {
                System.out.println("帳號或密碼錯誤");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/scene1/student_manager.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("學生成績與課程管理系統");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
