package com.Khorn.PTMBukkit.Commands;

import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.WorldConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;

public class CheckCommand extends BaseCommand
{
    public CheckCommand(PTMPlugin _plugin)
    {
        super(_plugin);
        name = "check";
        usage = "/ptm check [World]";
        help = "Checks or create PTM settings for world ";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        if (args.size() == 0)
        {
            sender.sendMessage(ErrorColor + "You need to select world");
            return true;
        }

        WorldConfig worldSettings = plugin.GetSettings(args.get(0), true);

        sender.sendMessage(MessageColor + "Done!");
        return true;
    }
}
