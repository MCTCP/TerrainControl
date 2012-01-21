package com.Khorn.TerrainControl.Commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.Khorn.TerrainControl.TCPlugin;

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

        sender.sendMessage(MessageColor + "Done!");
        return true;
    }
}
