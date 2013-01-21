package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CheckCommand extends BaseCommand
{
    public CheckCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "check";
        perm = TCPerm.CMD_CHECK.node;
        usage = "check World_Name";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        if (args.size() == 0)
        {
            sender.sendMessage(ERROR_COLOR + "You need to select world");
            return true;
        }

        plugin.CreateSettings(args.get(0), null);

        sender.sendMessage(MESSAGE_COLOR + "Done!");
        return true;
    }
}