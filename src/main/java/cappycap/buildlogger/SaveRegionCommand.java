package cappycap.buildlogger;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.w3c.dom.Text;

import java.util.logging.Logger;

public class SaveRegionCommand implements CommandExecutor {
    private final BuildLogger plugin;
    private static final Logger LOGGER = Logger.getLogger("BuildLogger");

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

                    CommandResult result = plugin.recordRegion(region, labels);

                    // Report ID.
                    TextComponent message = new TextComponent("Saved dataset entry with ID: ");
                    message.setColor(ChatColor.WHITE);

                    TextComponent idComponent = new TextComponent(Integer.toString(result.insertedId));
                    idComponent.setColor(ChatColor.YELLOW);
                    idComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Integer.toString(result.insertedId)));

                    message.addExtra(idComponent);

                    // Send ID.
                    sender.spigot().sendMessage(plugin.border);
                    sender.spigot().sendMessage(message);

                    if (result.xDim == result.xDimOld && result.yDim == result.yDimOld && result.zDim == result.zDimOld) {

                        // Report dimensions.
                        TextComponent newDimensionsMessage = new TextComponent("Dimensions: ");
                        newDimensionsMessage.setColor(ChatColor.WHITE);

                        TextComponent newDimensions = new TextComponent(String.format("%dx%dx%d", result.xDim, result.yDim, result.zDim));
                        newDimensions.setColor(ChatColor.YELLOW);

                        newDimensionsMessage.addExtra(newDimensions);

                        sender.spigot().sendMessage(newDimensionsMessage);

                    } else {
                        // Report old dimensions.
                        TextComponent dimensionsMessage = new TextComponent("Region dimensions: ");
                        dimensionsMessage.setColor(ChatColor.WHITE);

                        TextComponent oldDimensions = new TextComponent(String.format("%dx%dx%d", result.xDimOld, result.yDimOld, result.zDimOld));
                        oldDimensions.setColor(ChatColor.YELLOW);

                        dimensionsMessage.addExtra(oldDimensions);

                        // Report new dimensions.
                        TextComponent newDimensionsMessage = new TextComponent("Tidied dimensions: ");
                        newDimensionsMessage.setColor(ChatColor.WHITE);

                        TextComponent newDimensions = new TextComponent(String.format("%dx%dx%d", result.xDim, result.yDim, result.zDim));
                        newDimensions.setColor(ChatColor.YELLOW);

                        newDimensionsMessage.addExtra(newDimensions);

                        sender.spigot().sendMessage(dimensionsMessage);
                        sender.spigot().sendMessage(newDimensionsMessage);

                    }

                    sender.spigot().sendMessage(plugin.border);

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