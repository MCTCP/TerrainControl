package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.customobjects.CustomObject;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
                    sender.sendMessage(ERROR_COLOR + "Wrong page number " + args.get(2));
                }
            }
            LocalWorld world = this.getWorld(sender, worldName);

            if (world != null)
            {
                if (world.getSettings().worldConfig.customObjects.isEmpty())
                    sender.sendMessage(MESSAGE_COLOR + "This world does not have custom objects");

                List<String> pluginList = new ArrayList<String>();
                for (CustomObject object : world.getSettings().worldConfig.customObjects)
                {
                    pluginList.add(VALUE_COLOR + object.getName());
                }

                this.ListMessage(sender, pluginList, page, "World objects");

            } else
                sender.sendMessage(ERROR_COLOR + "World not found " + worldName);
            return true;

        }
        if (args.size() > 0)
        {
            try
            {
                page = Integer.parseInt(args.get(0));
            } catch (Exception e)
            {
                sender.sendMessage(ERROR_COLOR + "Wrong page number " + args.get(0));
            }
        }

        Collection<CustomObject> globalObjects = TerrainControl.getCustomObjectManager().globalObjects.values();

        if (globalObjects.isEmpty())
            sender.sendMessage(MESSAGE_COLOR + "This global directory does not have custom objects");

        List<String> pluginList = new ArrayList<String>();
        for (CustomObject object : globalObjects)
        {
            if (object.canSpawnAsObject())
            {
                pluginList.add(VALUE_COLOR + object.getName());
            }
        }

        this.ListMessage(sender, pluginList, page, "Global objects", "Use /tc list -w [world] for world objects");

        return true;

    }
}