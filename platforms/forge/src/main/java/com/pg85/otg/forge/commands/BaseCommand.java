package com.pg85.otg.forge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public abstract class BaseCommand
{
	protected final String name;
	protected String helpMessage = "No help message specified.";
	protected String usage = "No usage message specified.";
	protected String[] detailedHelp = new String[]
	{ "No detailed help specified." };

	public BaseCommand(String name) {
		this.name = name;
	}
	
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
	
	public IFormattableTextComponent createComponent(String text, String text2, TextFormatting color1, TextFormatting color2) {
		return new StringTextComponent(text).withStyle(color1).append(new StringTextComponent(text2).withStyle(color2));
	}

	public abstract void build(LiteralArgumentBuilder<CommandSource> builder);
}
