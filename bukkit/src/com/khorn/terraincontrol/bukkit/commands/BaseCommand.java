package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.List;

public abstract class BaseCommand
{
    public String name;
    public String perm;
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
            {
                return null;
            }

            if (sender instanceof Player && plugin.worlds.containsKey(((Player) sender).getWorld().getUID()))
            {
                return plugin.worlds.get(((Player) sender).getWorld().getUID());
            }

            return null;
        }

        World world = Bukkit.getWorld(arg);

        if (world != null && plugin.worlds.containsKey(world.getUID()))
        {
            return plugin.worlds.get(world.getUID());
        }

        return null;
    }

    protected void ListMessage(CommandSender sender, List<String> lines, int page, String listName)
    {
        int pageCount = (lines.size() >> 3) + 1;
        if (page > pageCount)
        {
            page = pageCount;
        }

        sender.sendMessage(ChatColor.AQUA.toString() + listName + " page " + page + "/" + pageCount);
        page--;

        for (int i = page * 8; i < lines.size() && i < (page * 8 + 8); i++)
        {
            sender.sendMessage(lines.get(i));
        }
    }

    public String getHelp()
    {
        String ret = "do that";
        Permission permission = Bukkit.getPluginManager().getPermission(perm);
        if (permission != null)
        {
            String desc = permission.getDescription();
            if (desc != null && desc.trim().length() > 0)
            {
                ret = desc.trim();
            }
        }
        return ret;
    }

    public static final String ERROR_COLOR = ChatColor.RED.toString();
    public static final String MESSAGE_COLOR = ChatColor.GREEN.toString();
    public static final String VALUE_COLOR = ChatColor.DARK_GREEN.toString();
}