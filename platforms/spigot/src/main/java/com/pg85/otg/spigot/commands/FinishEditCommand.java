package com.pg85.otg.spigot.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FinishEditCommand extends BaseCommand
{
	
	public FinishEditCommand() {
		super("finishedit");
		this.helpMessage = "Finish and save your current edit.";
		this.usage = "/otg finishedit";
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
			EditCommand.finishSession((Player) sender);
		} else {
			sender.sendMessage("Only players can use this command");
		}
		return true;
	}
}
