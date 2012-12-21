package com.khorn.terraincontrol.bukkit.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_4_6.CraftWorld;
import org.bukkit.entity.Player;

import com.khorn.terraincontrol.bukkit.BiomeReplace;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;

public class ReplaceBiomeCommand extends BaseCommand
{
    public ReplaceBiomeCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "replace";
        perm = TCPerm.CMD_REPLACE.node;
        usage = "replace biomeIDFrom biomeIdTo [World]";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        CraftWorld world = null;

        if (args.size() == 3)
        {
            world = (CraftWorld) Bukkit.getWorld(args.get(0));
            args.remove(0);
            if (world == null)
            {
                sender.sendMessage(ErrorColor + "You need to select world");
                return true;
            }
        }

        if (world == null)
        {
            if (sender instanceof ConsoleCommandSender)
            {
                sender.sendMessage(ErrorColor + "You need to select world");
                return true;
            }
            world = (CraftWorld) ((Player) sender).getWorld();
        }

        int biomeIdFrom;
        int biomeIdTo;
        try
        {
            biomeIdFrom = Integer.parseInt(args.get(0));
            biomeIdTo =  Integer.parseInt(args.get(1));
            args.remove(0);
            args.remove(0);
        }
        catch (Exception e)
        {
            sender.sendMessage(ErrorColor + "Wrong biome ids ");
            return true;
        }

        BiomeReplace replace = new BiomeReplace(world,biomeIdFrom,biomeIdTo,sender);

        this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, replace);

        return true;

    }
}
