package com.Khorn.PTMBukkit.Commands;

import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.Settings;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class BaseCommand
{
    public String name;
    public String help;
    public String usage;
    public boolean workOnConsole;
    protected PTMPlugin plugin;


    public BaseCommand(PTMPlugin _plugin)
    {
        this.plugin = _plugin;
    }

    public abstract boolean onCommand(CommandSender sender, List<String> args);


    protected Settings getSettings(CommandSender sender, String arg)
    {
        if (arg.equals(""))
        {
            if (sender instanceof ConsoleCommandSender)
                return null;
            if (sender instanceof Player)
                return plugin.worldsSettings.get(((Player) sender).getWorld().getName());
            return null;
        }
        return plugin.worldsSettings.get(arg);
    }

    protected void ListMessage(CommandSender sender, List<String> lines, int page, String listName)
    {
        int pageCount = (lines.size() >> 3) +1;
        if (page > pageCount)
            page = pageCount;

        sender.sendMessage(ChatColor.AQUA.toString() + listName + " page " + page + "/" + pageCount);
        page--;

        for (int i = page*8; i < lines.size() && i < (page*8 +8) ; i++)
        {
            sender.sendMessage(lines.get(i));
        }
    }


    public static final String ErrorColor = ChatColor.RED.toString();
    public static final String MessageColor = ChatColor.GREEN.toString();
    public static final String ValueColor = ChatColor.DARK_GREEN.toString();
}
