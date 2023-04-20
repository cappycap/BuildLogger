package cappycap.buildlogger;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.world.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SaveRegionCommand implements CommandExecutor {
    private final BuildLogger plugin;

    public SaveRegionCommand(BuildLogger plugin) {
        this.plugin = plugin;
    }

    // Save a region.
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return false;
        }

        if (args.length != 1) {
            return false;
        }

        String labels = args[0];

        Plugin worldEditPlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEditPlugin instanceof WorldEditPlugin wePlugin) {
            try {
                CuboidRegion region = getSelectedRegion(wePlugin, player);
                if (region != null) {
                    plugin.recordRegion(region, labels);
                    sender.sendMessage("Dataset entry saved with ID: ");
                    return true;
                } else {
                    sender.sendMessage("No region selected.");
                }
            } catch (Exception e) {
                sender.sendMessage("Error: " + e.getMessage());
            }
        }
        return false;
    }

    // Get a selected region from an online player.
    private CuboidRegion getSelectedRegion(WorldEditPlugin wePlugin, org.bukkit.entity.Player player) throws IncompleteRegionException {
        LocalSession localSession = wePlugin.getSession(player);
        RegionSelector regionSelector = localSession.getRegionSelector(BukkitAdapter.adapt(player.getWorld()));

        if (regionSelector.isDefined()) {
            Region region = regionSelector.getRegion(); // Get the selected region
            if (region instanceof CuboidRegion) {
                return (CuboidRegion) region;
            } else {
                throw new IllegalArgumentException("Only cuboid regions are supported.");
            }
        }

        return null;
    }
}