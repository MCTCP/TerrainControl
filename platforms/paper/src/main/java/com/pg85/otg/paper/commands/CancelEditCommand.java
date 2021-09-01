package com.pg85.otg.paper.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CancelEditCommand extends BaseCommand
{
	
	public CancelEditCommand() {
		super("canceledit");
		this.helpMessage = "Cancel and discard your current edit";
		this.usage = "/otg canceledit";
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		return new ArrayList<>();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args)
	{
		if (sender instanceof Player)
		{
			EditCommand.cancelSession((Player) sender);
		} else {
			sender.sendMessage("Only players can use this command");
		}
		return true;
	}
}
