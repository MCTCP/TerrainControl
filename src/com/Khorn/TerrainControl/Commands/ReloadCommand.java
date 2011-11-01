package com.Khorn.TerrainControl.Commands;

import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.TCPlugin;
import org.bukkit.command.CommandSender;

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

        worldSettings.newSettings = this.plugin.GetSettings(worldName,false);
        worldSettings.isDeprecated = true;

        sender.sendMessage(MessageColor + "WorldConfig for world " + worldName + " reloaded");
        return true;
    }
}
