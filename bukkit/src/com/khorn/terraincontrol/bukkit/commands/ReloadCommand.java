package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.TCWorldChunkManager;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.configuration.WorldConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.List;

public class ReloadCommand extends BaseCommand
{
    public ReloadCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "reload";
        perm = TCPerm.CMD_RELOAD.node;
        usage = "reload [World]";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        BukkitWorld world = this.getWorld(sender, args.size() > 0 ? args.get(0) : "");
        if (world == null)
        {
            sender.sendMessage(ErrorColor + "You need to select world");
            return true;
        }

        WorldConfig oldSettings = world.getSettings();

        this.plugin.CreateSettings(world.getName(),world);

        oldSettings.newSettings = world.getSettings();
        oldSettings.isDeprecated = true;

        if (world.getSettings().ModeBiome == WorldConfig.BiomeMode.Normal)
        {
            net.minecraft.server.World worldServer = ((CraftWorld) Bukkit.getWorld(world.getName())).getHandle();
            ((TCWorldChunkManager) worldServer.worldProvider.d).Init(world);
        }

        sender.sendMessage(MessageColor + "WorldConfig for world " + world.getName() + " reloaded");
        return true;
    }
}