package com.Khorn.TerrainControl.Commands;

import com.Khorn.TerrainControl.BiomeManager.BiomeManager;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.TCPlugin;
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
        WorldConfig worldSettings = this.getSettings(sender, args.size() > 0 ? args.get(0) : "");
        if (worldSettings == null)
        {
            sender.sendMessage(ErrorColor + "You need to select world");
            return true;
        }
        String worldName = worldSettings.WorldName;


        this.plugin.worldsSettings.remove(worldName);

        worldSettings.newSettings = this.plugin.GetSettings(worldName, false);
        worldSettings.newSettings.ChunkProvider = worldSettings.ChunkProvider;
        worldSettings.newSettings.ObjectGroups = worldSettings.ObjectGroups;
        worldSettings.isDeprecated = true;

        if (worldSettings.ModeBiome == WorldConfig.BiomeMode.Normal)
        {
            net.minecraft.server.World world = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();
            ((BiomeManager) world.worldProvider.b).Init(world, worldSettings.newSettings);
        }

        sender.sendMessage(MessageColor + "WorldConfig for world " + worldName + " reloaded");
        return true;
    }
}
