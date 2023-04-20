package cappycap.buildlogger;

import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.google.gson.Gson;
import org.bukkit.Material;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;

import java.util.HashMap;
import java.util.Map;

public class BuildLogger extends JavaPlugin {

    // Material mappings for recording 3D matrices and converting back.
    private static final Map<String, Integer> blockIdToNumber = new HashMap<>();
    private static final Map<Integer, String> numberToBlockId = new HashMap<>();

    private DatabaseHelper dbHelper;

    @Override
    public void onEnable() {

        getLogger().info("BuildLogger initialized.");

        // Initialize database.
        dbHelper = new DatabaseHelper("regions.db");

        // Register dataset commands.
        this.getCommand("dataset").setExecutor(new DatasetCommand(this));

        // Build Material hashmaps for matrices.
        int index = 0;
        for (Material material : Material.values()) {
            // Only include blocks in the mapping, skipping other materials like items
            if (material.isBlock()) {
                String blockId = material.getKey().toString();
                getLogger().info("Loaded "+blockId+" as "+index);
                blockIdToNumber.put(blockId, index);
                numberToBlockId.put(index, blockId);
                index++;
            }
        }

    }

    @Override
    public void onDisable() {
        getLogger().info("BuildLogger disabled. Bye!!!");
    }

    // Save a region's block information as a 3D integer matrix in the SQLite file.
    public int recordRegion(CuboidRegion region, String labels) {

        // Get the WorldEdit instance
        WorldEdit worldEdit = WorldEdit.getInstance();

        try (EditSession editSession = worldEdit.newEditSession(region.getWorld())) {

            // Create a Clipboard to hold the region's data
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
            BlockVector3 min = region.getMinimumPoint();

            // Copy the region's data from the EditSession to the Clipboard
            ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
            Operations.completeLegacy(copy);

            int width = region.getWidth();
            int height = region.getHeight();
            int length = region.getLength();

            int[][][] blocks = new int[width][height][length];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < length; z++) {

                        // Calculate position of this block relative to minimum region point.
                        BlockVector3 targetVector = min.add(x, y, z);
                        int xT = targetVector.getX();
                        int yT = targetVector.getY();
                        int zT = targetVector.getZ();
                        String coordinates = String.format("X: %d, Y: %d, Z: %d", xT, yT, zT);
                        getLogger().info("Looking at coordinates: " + coordinates);

                        // Acquire block's integer ID from HashMap.
                        String blockId = clipboard.getBlock(targetVector).getBlockType().getId();
                        int blockNumber = blockIdToNumber.getOrDefault(blockId, 0);
                        getLogger().info("Found "+blockNumber+" from "+blockId);

                        // Record in dataset matrix.
                        blocks[x][y][z] = blockNumber;

                    }
                }
            }

            // Convert the 3D matrix to a JSON string.
            String data = new Gson().toJson(blocks);

            getLogger().info("Data to save: "+data);

            // Save the JSON string and labels to the SQLite database.
            return dbHelper.saveRegion(data, labels);

        } catch (Exception e) {
            getLogger().warning("Failed to copy region data: " + e.getMessage());
        }

        return -1;

    }

    // Convert a matrix of block numbers back to a matrix of block IDs.
    public String[][][] convertMatrixToBlockIds(String[][][] blockNumberMatrix) {
        int width = blockNumberMatrix.length;
        int height = blockNumberMatrix[0].length;
        int length = blockNumberMatrix[0][0].length;

        String[][][] blockIdMatrix = new String[width][height][length];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int blockNumber = Integer.parseInt(blockNumberMatrix[x][y][z]);
                    String blockId = numberToBlockId.getOrDefault(blockNumber, "minecraft:air");
                    blockIdMatrix[x][y][z] = blockId;
                }
            }
        }

        return blockIdMatrix;
    }

}
