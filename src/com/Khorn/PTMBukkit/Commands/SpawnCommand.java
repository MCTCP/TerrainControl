package com.Khorn.PTMBukkit.Commands;


import com.Khorn.PTMBukkit.CustomObjects.CustomObject;
import com.Khorn.PTMBukkit.PTMPlayer;
import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.Settings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SpawnCommand extends BaseCommand
{
    public SpawnCommand(PTMPlugin _plugin)
    {
        super(_plugin);
        name = "spawn";
        usage = "/ptm spawn BOBName";
        help = "Spawn BOB to specified place";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Settings worldSettings = this.getSettings(sender, "");

        if (worldSettings == null)
        {
            sender.sendMessage(ErrorColor + "PTM is not enabled for this world");
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
            sender.sendMessage(ErrorColor + "BOB plugin not found, use '/ptm list' for search");
            return true;
        }

        PTMPlayer player = plugin.GetPlayer((Player)sender);
        player.hasObjectToSpawn = true;
        player.object = spawnObject;
        sender.sendMessage(MessageColor + "Click to block for object spawn");
        return true;


    }
}
