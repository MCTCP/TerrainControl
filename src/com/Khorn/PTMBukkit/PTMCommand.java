package com.Khorn.PTMBukkit;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PTMCommand implements CommandExecutor
{
    private final PTMPlugin plugin;

    public PTMCommand(PTMPlugin plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        boolean isConsole = false;

        if (commandSender instanceof ConsoleCommandSender)
            isConsole = true;
        if ((commandSender instanceof Player) || isConsole)
        {
            if (!commandSender.isOp())
            {
                commandSender.sendMessage(ChatColor.RED.toString() + "You do not have permission to use this command!");
                return true;
            }

            if (strings.length == 0)
                return this.SendUsage(commandSender);

            if (strings[0].equals("reload"))
                return this.ReloadCommand(commandSender, strings, isConsole);

            return this.SendUsage(commandSender);
        }

        return true;
    }

    private boolean ReloadCommand(CommandSender commandSender, String[] strings, boolean isConsole)
    {
        String worldName;

        if (strings.length == 1)
        {
            if (isConsole)
            {
                commandSender.sendMessage(ChatColor.RED.toString() + "You need to select world");
                return true;
            }

            worldName = ((Player) commandSender).getWorld().getName();
        } else
            worldName = strings[1];

        if (this.plugin.worldsSettings.containsKey(worldName))
        {
            Settings set = this.plugin.worldsSettings.get(worldName);
            this.plugin.worldsSettings.remove(worldName);

            set.newSettings = this.plugin.GetSettings(worldName);
            set.isDeprecated = true;

            commandSender.sendMessage(ChatColor.GREEN.toString() + "Settings for world " + worldName + " reloaded");
            return true;

        }
        commandSender.sendMessage(ChatColor.RED.toString() + " World " + worldName + " not found");
        return true;
    }

    private boolean SendUsage(CommandSender commandSender)
    {
        commandSender.sendMessage(ChatColor.GREEN.toString() + "Available command:");
        commandSender.sendMessage(ChatColor.GREEN.toString() + "/ptm reload [World] - Reload world settings");
        return true;
    }
}
