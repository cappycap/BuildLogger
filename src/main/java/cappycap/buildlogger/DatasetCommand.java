package cappycap.buildlogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class DatasetCommand implements CommandExecutor {
    private SaveRegionCommand saveRegionCommand;

    public DatasetCommand(BuildLogger plugin) {
        this.saveRegionCommand = new SaveRegionCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String subcommand = args[0];

            if ("add".equalsIgnoreCase(subcommand) && sender instanceof Player) {
                // Remove the 'add' subcommand from the arguments array
                String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

                // Call the onCommand method of SaveRegionCommand with the new arguments
                return saveRegionCommand.onCommand(sender, command, label, newArgs);
            }
        }

        return false;
    }
}