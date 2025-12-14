package scene1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Paths;

public class DBHelper {

    private static final String DB_PATH = Paths.get("database", "allsystem.db").toString();

    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    public Connection dbConnect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
