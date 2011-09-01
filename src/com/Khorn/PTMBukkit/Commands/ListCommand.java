package com.Khorn.PTMBukkit.Commands;

import com.Khorn.PTMBukkit.CustomObjects.CustomObject;
import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.Settings;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand extends BaseCommand
{
    public ListCommand(PTMPlugin _plugin)
    {
        super(_plugin);
        name = "list";
        usage = "/ptm list [page]";
        help = "List bob plugins for this world";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Settings worldSettings = this.getSettings(sender, "");

        if (worldSettings != null)
        {
            if (worldSettings.Objects.size() == 0)
                sender.sendMessage(MessageColor + "This world does not have custom objects");

            List<String> pluginList = new ArrayList<String>();
            for (CustomObject object : worldSettings.Objects)
            {
                pluginList.add(ValueColor + object.name);
            }
            int page = 1;
            if (args.size() > 0)
            {
                try
                {
                    page = Integer.getInteger(args.get(0));
                } catch (NullPointerException e)
                {
                     sender.sendMessage(ErrorColor + "Wrong page number " + args.get(0));
                }
            }
            this.ListMessage(sender, pluginList, page, "Bob plugins");
            return true;

        }
        sender.sendMessage(ErrorColor + "PTM is not enabled for this world");
        return true;

    }
}
