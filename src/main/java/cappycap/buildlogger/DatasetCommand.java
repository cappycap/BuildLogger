package cappycap.buildlogger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.w3c.dom.Text;

import java.util.Map;
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
            List<String> subcommands = Arrays.asList("help", "add", "view", "stats", "paste");

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

                // Remove the 'add' subcommand from the arguments array
                String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
                String labels = String.join(" ", newArgs);

                // Call the onCommand method of SaveRegionCommand with the new arguments
                return saveRegionCommand.onCommand(sender, command, label, new String[]{labels});

            } else if ("view".equalsIgnoreCase(subcommand) && sender instanceof Player) {

                try {
                    viewRegion(sender, args[1]);
                    return true;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            } else if ("help".equalsIgnoreCase(subcommand) && sender instanceof Player)  {

                sender.spigot().sendMessage(plugin.border);

                TextComponent helpAdd = createHelpMessage("/dataset add [labels]",
                        "Save the selected WorldEdit region with the specified labels. Labels should be comma-separated.", false);
                sender.spigot().sendMessage(helpAdd);

                TextComponent helpView = createHelpMessage("/dataset view [id]",
                        "View dimensions and labels for the specified entry.", false);
                sender.spigot().sendMessage(helpView);

                TextComponent helpPaste = createHelpMessage("/dataset paste [id]",
                        "Paste entry at your position. Be careful!", false);
                sender.spigot().sendMessage(helpPaste);

                TextComponent helpStats = createHelpMessage("/dataset stats",
                        "View the local dataset size and top labels.", true);
                sender.spigot().sendMessage(helpStats);

                sender.spigot().sendMessage(plugin.border);

                return true;

            } else if ("stats".equalsIgnoreCase(subcommand) && sender instanceof Player) {

                try {

                    // Fetch total entries and top X labels.
                    var limit = 10;
                    int totalEntries = dbHelper.getTotalEntries();
                    Map<String, Integer> topLabels = dbHelper.getTopLabels(limit);

                    sender.spigot().sendMessage(plugin.border);
                    TextComponent totalEntriesMessage = new TextComponent("Total entries in the database: ");
                    TextComponent totalEntriesValue = new TextComponent(Integer.toString(totalEntries));
                    totalEntriesValue.setColor(ChatColor.YELLOW);
                    totalEntriesMessage.addExtra(totalEntriesValue);
                    sender.spigot().sendMessage(totalEntriesMessage);

                    TextComponent topLabelsMessage = new TextComponent("Top ");
                    TextComponent topLabelsValue = new TextComponent(Integer.toString(limit));
                    topLabelsValue.setColor(ChatColor.YELLOW);
                    topLabelsMessage.addExtra(topLabelsValue);
                    topLabelsMessage.addExtra(" labels:");
                    sender.spigot().sendMessage(topLabelsMessage);

                    // Map through our top labels and report.
                    int index = 1;
                    for (Map.Entry<String, Integer> entry : topLabels.entrySet()) {
                        TextComponent labelComp = new TextComponent(index + ". " + entry.getKey() + " - ");
                        TextComponent count = new TextComponent(Integer.toString(entry.getValue()));
                        count.setColor(ChatColor.YELLOW);
                        labelComp.addExtra(count);
                        sender.spigot().sendMessage(labelComp);
                        index++;
                    }

                    sender.spigot().sendMessage(plugin.border);

                } catch (SQLException e) {
                    sender.sendMessage("Error retrieving stats: " + e.getMessage());
                    e.printStackTrace();
                }

                return true;

            } else if ("paste".equalsIgnoreCase(subcommand) && sender instanceof Player) {

                if (args.length == 2) {
                    int id;
                    try {
                        id = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid ID. Please enter a valid number.");
                        return false;
                    }
                    plugin.pasteRegion(id, (Player) sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /dataset paste <id>");
                }

                return true;

            }

        }

        return false;
    }

    // Make explaining commands easier.
    private TextComponent createHelpMessage(String command, String description, boolean last) {

        TextComponent helpMessage = new TextComponent(command);
        helpMessage.setColor(ChatColor.YELLOW);
        String end = (!last) ? "\n" : "";
        TextComponent desc = new TextComponent("\n" + description + end);
        desc.setColor(ChatColor.WHITE);
        helpMessage.addExtra(desc);

        return helpMessage;
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