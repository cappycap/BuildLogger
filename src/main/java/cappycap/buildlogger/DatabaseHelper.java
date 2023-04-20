package cappycap.buildlogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class DatabaseHelper {
    private final String url;

    public DatabaseHelper(String fileName) {
        url = "jdbc:sqlite:" + fileName;
        createTable();
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS regions (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " labels TEXT NOT NULL UNIQUE,\n"
                + " data BLOB NOT NULL\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void saveRegion(String data, String labels) {
        String sql = "INSERT INTO regions(labels, data) VALUES(?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, labels);
            pstmt.setString(2, data);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}