package com.Khorn.PTMBukkit.Commands;

import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.Settings;
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
        help = "Checks PTM is enable for this world";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        if ((sender instanceof ConsoleCommandSender) && args.size() == 0)
        {
            sender.sendMessage(this.ErrorColor + "You need to select world");
            return true;
        }

        Settings worldSettings = this.getSettings(sender, args.size() > 0 ? args.get(0) : "");

        if (worldSettings != null)
            sender.sendMessage(MessageColor + "Ptm is enabled for " + worldSettings.WorldName);
        else
            sender.sendMessage(MessageColor + "Ptm is disabled for this world");
        return true;
    }
}
