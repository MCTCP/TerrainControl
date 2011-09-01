package com.Khorn.PTMBukkit.Commands;

import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.Settings;
import org.bukkit.command.CommandSender;

import java.util.List;


public class ReloadCommand extends BaseCommand
{
    public ReloadCommand(PTMPlugin _plugin)
    {
        super(_plugin);
        name = "reload";
        usage = "/ptm reload [World]";
        help = "Reload world settings";
        workOnConsole = true;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Settings worldSettings = this.getSettings(sender, args.size() > 0 ? args.get(0) : "");
        if (worldSettings == null)
        {
            sender.sendMessage(this.ErrorColor + "You need to select world");
            return true;
        }
        String worldName = worldSettings.WorldName;

        this.plugin.worldsSettings.remove(worldName);

        worldSettings.newSettings = this.plugin.GetSettings(worldName);
        worldSettings.isDeprecated = true;

        sender.sendMessage(this.MessageColor + "Settings for world " + worldName + " reloaded");
        return true;
    }
}
