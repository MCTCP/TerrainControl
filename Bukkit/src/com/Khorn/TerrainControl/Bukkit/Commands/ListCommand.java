package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.customobjects.CustomObject;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand extends BaseCommand
{
    public ListCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "list";
        perm = TCPerm.CMD_LIST.node;
        usage = "list [page]";
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