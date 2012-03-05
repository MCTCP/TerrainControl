package com.Khorn.TerrainControl.Bukkit.Commands;

import com.Khorn.TerrainControl.Bukkit.BukkitWorld;
import com.Khorn.TerrainControl.Bukkit.TCPlugin;
import com.Khorn.TerrainControl.CustomObjects.CustomObject;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand extends BaseCommand
{
    public ListCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "list";
        usage = "/tc list [page]";
        help = "List bob plugins for this world";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        BukkitWorld world = this.getWorld(sender, "");

        if (world != null)
        {
            if (world.getSettings().Objects.size() == 0)
                sender.sendMessage(MessageColor + "This world does not have custom objects");

            List<String> pluginList = new ArrayList<String>();
            for (CustomObject object : world.getSettings().Objects)
            {
                pluginList.add(ValueColor + object.name);
            }
            int page = 1;
            if (args.size() > 0)
            {
                try
                {
                    page = Integer.parseInt(args.get(0));
                } catch (Exception e)
                {
                     sender.sendMessage(ErrorColor + "Wrong page number " + args.get(0));
                }
            }
            this.ListMessage(sender, pluginList, page, "Bob plugins");
            return true;

        }
        sender.sendMessage(ErrorColor + "TC is not enabled for this world");
        return true;

    }
}