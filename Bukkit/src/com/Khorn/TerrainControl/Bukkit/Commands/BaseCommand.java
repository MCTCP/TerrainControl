package com.Khorn.TerrainControl.Bukkit.Commands;

import com.Khorn.TerrainControl.Bukkit.BukkitWorld;
import com.Khorn.TerrainControl.Bukkit.TCPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
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
    protected TCPlugin plugin;


    public BaseCommand(TCPlugin _plugin)
    {
        this.plugin = _plugin;
    }

    public abstract boolean onCommand(CommandSender sender, List<String> args);


    protected BukkitWorld getWorld(CommandSender sender, String arg)
    {
        if (arg.equals(""))
        {
            if (sender instanceof ConsoleCommandSender)
                return null;
            if (sender instanceof Player && plugin.worlds.containsKey(((Player) sender).getWorld().getUID()))
                return plugin.worlds.get(((Player) sender).getWorld().getUID());
            return null;
        }
        World world = Bukkit.getWorld(name);
        if( world != null && plugin.worlds.containsKey(world.getUID()))
            return plugin.worlds.get(world.getUID());
        else
            return null;
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
