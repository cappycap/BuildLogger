package cappycap.buildlogger;

import java.sql.*;

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

    public int saveRegion(String data, String labels) {
        String sql = "INSERT INTO regions(labels, data) VALUES(?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, labels);
            pstmt.setString(2, data);
            pstmt.executeUpdate();
            // Get id of statement.
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

}