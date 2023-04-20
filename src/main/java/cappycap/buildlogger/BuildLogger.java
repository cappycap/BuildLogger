package cappycap.buildlogger;

import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.google.gson.Gson;

public class BuildLogger extends JavaPlugin {

    private DatabaseHelper dbHelper;

    @Override
    public void onEnable() {

        getLogger().info("BuildLogger initialized.");

        // Initialize database.
        dbHelper = new DatabaseHelper("regions.db");

        // Register dataset commands.
        this.getCommand("dataset").setExecutor(new DatasetCommand(this));

    }

    @Override
    public void onDisable() {
        getLogger().info("BuildLogger disabled. Bye!!!");
    }

    // Save a region's block information as a 3D matrix in the SQLite file.
    public void recordRegion(CuboidRegion region, String labels) {

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        BlockVector3 min = region.getMinimumPoint();
        int width = region.getWidth();
        int height = region.getHeight();
        int length = region.getLength();

        String[][][] blocks = new String[width][height][length];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {

                    // Need to explore the best variable to store here. Will try block ID for now.
                    blocks[x][y][z] = clipboard.getBlock(min.add(x, y, z)).getBlockType().getId();

                }
            }
        }

        // Convert the 3D matrix to a JSON string.
        String data = new Gson().toJson(blocks);

        // Save the JSON string and labels to the SQLite database.
        dbHelper.saveRegion(data, labels);

    }

}
