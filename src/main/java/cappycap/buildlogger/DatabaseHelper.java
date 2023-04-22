package cappycap.buildlogger;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
                return new CommandResult(insertedId, labels, xDim, yDim, zDim, xDimOld, yDimOld, zDimOld);
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Get the data entry only.
    public String getRegionData(int id)  throws SQLException {
        String sql = "SELECT data FROM regions WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("data");
                }
            }
        }

        return null;
    }

    public CommandResult getRegion(String id) throws SQLException {
        String sql = "SELECT * FROM regions WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return new CommandResult(resultSet.getInt("id"), resultSet.getString("labels"), resultSet.getInt("dim_x"), resultSet.getInt("dim_y"), resultSet.getInt("dim_z"), 0, 0, 0);
                }
            }
        }

        return null;
    }

    // get number of entries in DB.
    public int getTotalEntries() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM regions";

        try (Connection conn = connect();
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                return resultSet.getInt("count");
            }
        }

        return 0;
    }

    // Get the top labels found across all dataset entries.
    public Map<String, Integer> getTopLabels(int limit) throws SQLException {

        String query = "SELECT labels FROM regions";

        // We will count with a map.
        Map<String, Integer> labelCounts = new HashMap<>();

        try (Connection conn = connect();
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String labels = resultSet.getString("labels");
                String[] labelArray = labels.split(",");

                for (String label : labelArray) {
                    String trimmedLabel = label.trim();
                    labelCounts.put(trimmedLabel, labelCounts.getOrDefault(trimmedLabel, 0) + 1);
                }
            }
        }

        return labelCounts.entrySet().stream() // Send hashmap as a stream of values.
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // Sort entries by Ints in reverse (descending) order.
                .limit(limit) // After sorting, cut off the top limit.
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)); // Use LinkedHashMap to maintain order.
    }

}