package cappycap.buildlogger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.w3c.dom.Text;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Arrays;

public class DatasetCommand implements CommandExecutor, TabCompleter {
    private SaveRegionCommand saveRegionCommand;
    private static final Logger LOGGER = Logger.getLogger("BuildLogger");

    private DatabaseHelper dbHelper;
    private BuildLogger plugin;

    public DatasetCommand(BuildLogger plugin) {
        this.saveRegionCommand = new SaveRegionCommand(plugin);
        dbHelper = plugin.getDatabaseHelper();
        this.plugin = plugin;
    }

    // Autocomplete our commands for ease of use.
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("add", "view", "edit", "del");

            if (!args[0].isEmpty()) {
                List<String> completions = new ArrayList<>();
                for (String subcommand : subcommands) {
                    if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(subcommand);
                    }
                }
                return completions;
            }

            return subcommands;
        }

        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0) {
            String subcommand = args[0];

            if ("add".equalsIgnoreCase(subcommand) && sender instanceof Player) {

                LOGGER.info("Running subcommand "+subcommand);
                // Remove the 'add' subcommand from the arguments array
                String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

                String labels = String.join(" ", newArgs);

                LOGGER.info("Passing labels "+labels);
                // Call the onCommand method of SaveRegionCommand with the new arguments
                return saveRegionCommand.onCommand(sender, command, label, new String[]{labels});
            } else if ("view".equalsIgnoreCase(subcommand) && sender instanceof Player) {
                try {
                    viewRegion(sender, args[1]);
                    return true;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return false;
    }


    // Helper function for viewing region data.
    private void viewRegion(CommandSender sender, String id) throws SQLException {

        // Retrieve the region data from the database
        CommandResult result = dbHelper.getRegion(id);

        if (result != null) {

            // Send the region data to the player or console.
            TextComponent titleComponent = new TextComponent("Dataset ID: ");
            titleComponent.setColor(ChatColor.WHITE);

            TextComponent idComponent = new TextComponent(id);
            idComponent.setColor(ChatColor.YELLOW);
            titleComponent.addExtra(idComponent);

            // Dims.
            TextComponent dimsComponent = new TextComponent("Dimensions: ");
            dimsComponent.setColor(ChatColor.WHITE);

            TextComponent newDimensions = new TextComponent(String.format("%dx%dx%d", result.xDim, result.yDim, result.zDim));
            newDimensions.setColor(ChatColor.YELLOW);
            dimsComponent.addExtra(newDimensions);

            // Labels.
            TextComponent labelsComponent = new TextComponent("Labels: ");
            labelsComponent.setColor(ChatColor.WHITE);

            TextComponent labelsInfo = new TextComponent(result.labels);
            labelsInfo.setColor(ChatColor.YELLOW);
            labelsComponent.addExtra(labelsInfo);

            sender.spigot().sendMessage(plugin.border);
            sender.spigot().sendMessage(titleComponent);
            sender.spigot().sendMessage(dimsComponent);
            sender.spigot().sendMessage(labelsComponent);
            sender.spigot().sendMessage(plugin.border);

        } else {
            sender.sendMessage("No region found with ID " + id);
        }
    }
}