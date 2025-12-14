package scene1;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StudentManagerController {

    @FXML
    private Label user;

    @FXML
    private Button studentmanager;
    @FXML
    private Button studentsearch;
    @FXML
    private Button coursemanager;
    @FXML
    private Button coursesearch;
    @FXML
    private Button scoresearch;

    @FXML
    private StackPane contentPane;

    // 已載入的頁面，避免重複載入
    private final Map<String, Parent> loadedPages = new HashMap<>();

    @FXML
    public void initialize() {
        // 初始化時全部按鈕隱藏
        safeSetButton(studentmanager, false);
        safeSetButton(studentsearch, false);
        safeSetButton(coursemanager, false);
        safeSetButton(coursesearch, false);
        safeSetButton(scoresearch, false);

        // 根據 UserSession 設定按鈕與登入者資訊
        setUserSession();
    }

    /** 使用 UserSession 初始化按鈕及 Label */
    @FXML
    public void setUserSession() {
        String name = UserSession.getUserName();
        boolean isTeacher = UserSession.isTeacher();
        boolean isStudent = UserSession.isStudent();

        // 顯示登入者資訊
        String roleStr = isTeacher ? "老師" : "學生";
        user.setText("登入者: " + name + " " + roleStr);

        // 教師可以操作管理模組，學生僅檢視
        safeSetButton(studentmanager, isTeacher);
        safeSetButton(coursemanager, isTeacher);

        // 學生可以使用搜尋功能
        safeSetButton(studentsearch, isStudent);
        safeSetButton(coursesearch, isStudent);
        safeSetButton(scoresearch, isStudent);
    }

    /** 設定按鈕顯示/啟用（安全版，避免 null） */
    private void safeSetButton(Button btn, boolean enable) {
        if (btn != null) {
            btn.setVisible(enable);
            btn.setDisable(!enable);
        }
    }

    /** 載入子模組 FXML 到 StackPane */
    private void loadModule(String fxmlPath) {
        try {
            Parent root;
            if (loadedPages.containsKey(fxmlPath)) {
                root = loadedPages.get(fxmlPath);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                root = loader.load();

                // 若子 Controller 實作 UserAware，傳遞登入資訊
                Object controller = loader.getController();
                if (controller instanceof UserAware ua) {
                    ua.setUserSession();
                }

                if (root instanceof Region regionRoot) {
                    regionRoot.prefWidthProperty().bind(contentPane.widthProperty());
                    regionRoot.prefHeightProperty().bind(contentPane.heightProperty());
                }

                loadedPages.put(fxmlPath, root);
            }

            contentPane.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("無法載入 FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ======== 按鈕事件 ========
    @FXML
    public void handleStudentManager() {
        loadModule("/scene1/studentList.fxml");
    }

    @FXML
    public void handleStudentSearch() {
        loadModule("/scene1/studentList.fxml");
    }

    @FXML
    public void handleCourseManager() {
        loadModule("/scene1/courseMain.fxml");
    }

    @FXML
    public void handleScoreManager() {
        loadModule("/scene1/GradeMain.fxml");
    }

    @FXML
    public void handleCourseSearch() {
        loadModule("/scene1/courseMain.fxml");
    }

    @FXML
    public void handleScoreSearch() {
        loadModule("/scene1/gradeQuery.fxml");
    }

    /** 介面規範：子 Controller 實作 UserAware 可接收登入資訊 */
    public interface UserAware {
        void setUserSession();
    }
}
