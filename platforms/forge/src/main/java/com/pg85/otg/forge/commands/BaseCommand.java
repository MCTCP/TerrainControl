package com.pg85.otg.forge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;

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

	public abstract void build(LiteralArgumentBuilder<CommandSource> builder);
}
