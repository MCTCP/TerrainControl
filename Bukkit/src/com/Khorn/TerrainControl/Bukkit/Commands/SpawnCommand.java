package com.Khorn.TerrainControl.Bukkit.Commands;


import com.Khorn.TerrainControl.Bukkit.TCPlayer;
import com.Khorn.TerrainControl.Bukkit.TCPlugin;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.CustomObjects.CustomObject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SpawnCommand extends BaseCommand
{
    public SpawnCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "spawn";
        usage = "/tc spawn BOBName";
        help = "Spawn BOB to specified place";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        WorldConfig worldSettings = this.getSettings(sender, "");

        if (worldSettings == null)
        {
            sender.sendMessage(ErrorColor + "TC is not enabled for this world");
            return true;
        }
        if (args.size() == 0)
        {
            sender.sendMessage(ErrorColor + "Type BOB file name");
            return true;
        }
        CustomObject spawnObject = null;
        for (CustomObject object : worldSettings.Objects)
        {
            if (object.name.equals(args.get(0)))
                spawnObject = object;
        }
        if (spawnObject == null)
        {
            sender.sendMessage(ErrorColor + "BOB plugin not found, use '/tc list' for search");
            return true;
        }

        TCPlayer player = plugin.GetPlayer((Player)sender);
        player.hasObjectToSpawn = true;
        player.object = spawnObject;
        sender.sendMessage(MessageColor + "Click to block for object spawn");
        return true;


    }
}
