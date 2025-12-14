package scene1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/scene1/loginmod.fxml"));// 載入登入介面
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("學生成績與課程管理系統 - 登入");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
