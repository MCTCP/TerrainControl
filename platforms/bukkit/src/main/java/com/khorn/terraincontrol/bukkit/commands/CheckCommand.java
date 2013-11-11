package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.configuration.WorldSettings;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.List;

public class CheckCommand extends BaseCommand
{
    public CheckCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "check";
        perm = TCPerm.CMD_CHECK.node;
        usage = "check World_Name";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        if (args.isEmpty())
        {
            sender.sendMessage(ERROR_COLOR + "You need to select world");
            return true;
        }

        String worldName = args.get(0);
        File settingsFolder = plugin.getWorldSettingsFolder(worldName);
        new WorldSettings(settingsFolder, new BukkitWorld(worldName), true);

        sender.sendMessage(MESSAGE_COLOR + "Done!");
        return true;
    }
}