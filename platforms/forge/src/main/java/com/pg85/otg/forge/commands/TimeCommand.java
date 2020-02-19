package com.pg85.otg.forge.commands;

import com.pg85.otg.OTG;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

import java.text.ParseException;
import java.util.List;

public class TimeCommand extends BaseCommand {

    TimeCommand()
    {
        name = "time";
        usage = "time <set/add> <time>";
        description = "Sets time in the current dimension.";
        needsOp = true;
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args) {
        int newTime = 0;
        if (args.size() != 2)
        {
            return false;
        }
        try
        {
            newTime = Integer.parseInt(args.get(1));
        }
        catch (NumberFormatException e)
        {
            sender.sendMessage(new TextComponentString(args.get(1)+" is not a number"));
            return true;
        }
        if (args.get(0).equalsIgnoreCase("add"))
        {
            newTime += sender.getEntityWorld().getWorldTime();
        }
        else if (!args.get(0).equalsIgnoreCase("set"))
        {
            return false;
        }
        sender.getEntityWorld().setWorldTime(newTime);
        sender.sendMessage(new TextComponentString("Time is now "+newTime));
        return true;
    }
}
