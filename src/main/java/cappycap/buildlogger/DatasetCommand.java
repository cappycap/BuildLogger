package cappycap.buildlogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.logging.Logger;
import java.util.Arrays;

public class DatasetCommand implements CommandExecutor {
    private SaveRegionCommand saveRegionCommand;
    private static final Logger LOGGER = Logger.getLogger("BuildLogger");

    public DatasetCommand(BuildLogger plugin) {
        this.saveRegionCommand = new SaveRegionCommand(plugin);
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
            }
        }

        return false;
    }
}