package com.pg85.otg.forge.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class HelpCommand extends BaseCommand
{
    HelpCommand()
    {
        name = "help";
        usage = "help";
        description = "View this information.";
        needsOp = false;
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
		List<String> lines = new ArrayList<String>();
		for (BaseCommand command : OTGCommandHandler.getAllCommands())
		{
			lines.add(MESSAGE_COLOR + "/otg " + command.usage + VALUE_COLOR + " - " + command.description);
		}

		int page = 1;
		if (args.size() > 0)
		{
			try
			{
				page = Integer.parseInt(args.get(0));
			} catch (NumberFormatException e)
			{
				sender.sendMessage(new TextComponentString(ERROR_COLOR + "Invalid page number " + args.get(0)));
				return true;
			}
		}

		this.listMessage(sender, lines, page, "Available commands");
		return true;
    }
}