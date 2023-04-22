package cappycap.buildlogger;

import java.sql.*;
import java.util.logging.Logger;

public class DatabaseHelper {
    private final String url;
    private static final Logger LOGGER = Logger.getLogger("BuildLogger");
    private String pluginVersion;

    public DatabaseHelper(String fileName, BuildLogger plugin) {
        url = "jdbc:sqlite:" + fileName;
        createTable();
        pluginVersion = plugin.getDescription().getVersion();
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
                + " data BLOB NOT NULL,\n"
                + " dim_x INTEGER NOT NULL,\n"
                + " dim_y INTEGER NOT NULL,\n"
                + " dim_z INTEGER NOT NULL,\n"
                + " version TEXT NOT NULL\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public CommandResult saveRegion(String data, String labels, int xDim, int yDim, int zDim, int xDimOld, int yDimOld, int zDimOld) {
        String sql = "INSERT INTO regions(labels, data, dim_x, dim_y, dim_z, version) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, labels);
            pstmt.setString(2, data);
            pstmt.setInt(3, xDim);
            pstmt.setInt(4, yDim);
            pstmt.setInt(5, zDim);
            pstmt.setString(6, pluginVersion);
            pstmt.executeUpdate();
            // Get id of statement.
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int insertedId = rs.getInt(1);
                LOGGER.info("Inserted row:\nid=" + insertedId + "\nlabels=" + labels + "\ndims="+xDim+"x"+yDim+"x"+zDim);
                return new CommandResult(insertedId, xDim, yDim, zDim, xDimOld, yDimOld, zDimOld);
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

}