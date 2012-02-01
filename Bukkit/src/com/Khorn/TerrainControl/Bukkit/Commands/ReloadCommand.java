package com.Khorn.TerrainControl.Bukkit.Commands;

import com.Khorn.TerrainControl.Bukkit.BiomeManager.BiomeManager;
import com.Khorn.TerrainControl.Bukkit.BukkitWorld;
import com.Khorn.TerrainControl.Bukkit.TCPlugin;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
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
        usage = "/tc reload [World]";
        help = "Reload world settings";
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

        oldSettings.isDeprecated = true;
        oldSettings.newSettings = world.getSettings();




        if (world.getSettings().ModeBiome == WorldConfig.BiomeMode.Normal)
        {
            net.minecraft.server.World worldServer = ((CraftWorld) Bukkit.getWorld(world.getName())).getHandle();
            ((BiomeManager) worldServer.worldProvider.c).Init(world);
        }

        sender.sendMessage(MessageColor + "WorldConfig for world " + world.getName() + " reloaded");
        return true;
    }
}
