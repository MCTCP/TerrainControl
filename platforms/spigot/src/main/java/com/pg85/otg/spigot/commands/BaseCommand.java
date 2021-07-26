package com.pg85.otg.spigot.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

public abstract class BaseCommand
{
	protected String name;
	protected String helpMessage = "No help message specified.";
	protected String usage = "No usage message specified.";
	protected String[] detailedHelp = new String[]
	{ "No detailed help specified." };

	public String getName()
	{
		return name;
	}

	public String getHelpMessage()
	{
		return helpMessage;
	}

	public String getUsage()
	{
		return usage;
	}

	public String[] getDetailedHelp()
	{
		return this.detailedHelp;
	}
	
	public abstract List<String> onTabComplete(CommandSender sender, String[] args);
	public abstract boolean execute(CommandSender sender, String[] args);
}
