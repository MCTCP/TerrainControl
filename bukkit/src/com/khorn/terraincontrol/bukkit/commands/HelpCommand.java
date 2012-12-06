package com.khorn.terraincontrol.bukkit.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;

public class HelpCommand extends BaseCommand
{
    public HelpCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "help";
        perm = TCPerm.CMD_HELP.node;
        usage = "help";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        List<String> lines = new ArrayList<String>();
        for (BaseCommand command : plugin.commandExecutor.commandHashMap.values())
        {
            lines.add(MessageColor + "/tc " + command.usage + " - " + command.getHelp());
        }
        
        int page = 1;
        if (args.size() > 0)
        {
            try
            {
                page = Integer.parseInt(args.get(0));
            } catch (NumberFormatException  e)
            {
                sender.sendMessage(ErrorColor + "Wrong page number " + args.get(0));
            }
        }

        this.ListMessage(sender, lines, page, "Available commands");
        return true;
    }
}
