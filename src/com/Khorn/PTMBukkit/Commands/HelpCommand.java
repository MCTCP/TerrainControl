package com.Khorn.PTMBukkit.Commands;

import com.Khorn.PTMBukkit.PTMPlugin;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends BaseCommand
{
    private List<String> lines = new ArrayList<String>();

    public HelpCommand(PTMPlugin _plugin)
    {
        super(_plugin);
        name = "help";
        usage = "/ptm help";
        help = "Show help";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        int page = 1;
        if (args.size() > 0)
        {
            try
            {
                page = Integer.parseInt(args.get(0));
            } catch (NullPointerException e)
            {
                sender.sendMessage(ErrorColor + "Wrong page number " + args.get(0));
            }
        }

        this.ListMessage(sender, lines, page, "Available commands");
        return true;
    }

    public void AddHelp(BaseCommand command)
    {
        this.lines.add(MessageColor + command.usage + " - " + command.help);
    }
}
