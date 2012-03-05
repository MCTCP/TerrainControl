package com.Khorn.TerrainControl.Bukkit.Commands;

import com.Khorn.TerrainControl.Bukkit.TCPlugin;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CheckCommand extends BaseCommand
{
    public CheckCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "check";
        usage = "/tc check World_Name";
        help = "Checks or create TC settings for world ";
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

        plugin.CreateSettings(args.get(0), null);

        sender.sendMessage(MessageColor + "Done!");
        return true;
    }
}