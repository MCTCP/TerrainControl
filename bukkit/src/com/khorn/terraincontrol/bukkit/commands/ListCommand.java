package com.khorn.terraincontrol.bukkit.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.customobjects.CustomObject;

public class ListCommand extends BaseCommand
{
    public ListCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "list";
        perm = TCPerm.CMD_LIST.node;
        usage = "list [-w World] [page]";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {

        int page = 1;

        if (args.size() > 1 && args.get(0).equals("-w"))
        {
            String worldName = args.get(1);
            if (args.size() > 2)
            {
                try
                {
                    page = Integer.parseInt(args.get(2));
                } catch (Exception e)
                {
                    sender.sendMessage(ErrorColor + "Wrong page number " + args.get(2));
                }
            }
            BukkitWorld world = this.getWorld(sender, worldName);

            if (world != null)
            {
                if (world.getSettings().customObjects.size() == 0)
                    sender.sendMessage(MessageColor + "This world does not have custom objects");

                List<String> pluginList = new ArrayList<String>();
                for (CustomObject object : world.getSettings().customObjects)
                {
                    pluginList.add(ValueColor + object.getName());
                }

                this.ListMessage(sender, pluginList, page, "World bo2 objects");

            } else
                sender.sendMessage(ErrorColor + "World not found " + worldName);
            return true;

        }
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

        Collection<CustomObject> globalObjects = TerrainControl.getCustomObjectManager().globalObjects.values();

        if (globalObjects.size() == 0)
            sender.sendMessage(MessageColor + "This global directory does not have custom objects");

        List<String> pluginList = new ArrayList<String>();
        for (CustomObject object : globalObjects)
        {
            pluginList.add(ValueColor + object.getName());
        }

        this.ListMessage(sender, pluginList, page, "Global bo2 objects");

        return true;

    }
}