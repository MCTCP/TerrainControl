package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.configuration.WorldSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public class ReloadCommand extends BaseCommand
{
    
    public ReloadCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "reload";
        perm = TCPerm.CMD_RELOAD.node;
        usage = "reload [world_name]";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        BukkitWorld world = this.getWorld(sender, args.size() > 0 ? args.get(0) : "");
        if (world == null)
        {
            sender.sendMessage(ERROR_COLOR + "World not found. Either you are not in a world with Terrain Control, or you are the console.");
            return false;
        }

        WorldSettings newSettings = new WorldSettings(plugin.getWorldSettingsFolder(world.getName()), world, false);
        world.setSettings(newSettings);

        sender.sendMessage(MESSAGE_COLOR + "Configs for world '" + world.getName() + "' reloaded");
        if (sender instanceof Player)
        {
            TerrainControl.log(Level.INFO, "{0} reloaded the config files for world '{1}'.", new Object[]
            {
                sender.getName(), world.getName()
            });
        }
        return true;
    }
    
}