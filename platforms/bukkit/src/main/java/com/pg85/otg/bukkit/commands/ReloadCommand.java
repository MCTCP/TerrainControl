package com.pg85.otg.bukkit.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.BukkitWorld;
import com.pg85.otg.bukkit.OTGPerm;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.logging.LogMarker;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ReloadCommand extends BaseCommand
{
    
    public ReloadCommand(OTGPlugin _plugin)
    {
        super(_plugin);
        name = "reload";
        perm = OTGPerm.CMD_RELOAD.node;
        usage = "reload [world_name]";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        BukkitWorld world = (BukkitWorld) this.getWorld(sender, args.size() > 0 ? args.get(0) : "");
        if (world == null)
        {
            sender.sendMessage(ERROR_COLOR + "World not found. Either you are not in a world with Open Terrain Generator, or you are the console.");
            return false;
        }

        world.reloadSettings();

        sender.sendMessage(MESSAGE_COLOR + "Configs for world '" + world.getName() + "' reloaded");
        if (sender instanceof Player)
        {
            OTG.log(LogMarker.INFO, "{} reloaded the config files for world '{}'.", new Object[]
            {
                sender.getName(), world.getName()
            });
        }
        return true;
    }
    
}