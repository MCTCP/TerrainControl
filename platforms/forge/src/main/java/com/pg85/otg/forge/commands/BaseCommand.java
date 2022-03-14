package com.pg85.otg.forge.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

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
	
	public static MutableComponent createComponent(String text, String text2, ChatFormatting color1, ChatFormatting color2) {
		return new TextComponent(text).withStyle(color1).append(new TextComponent(text2).withStyle(color2));
	}

	public abstract void build(LiteralArgumentBuilder<CommandSourceStack> builder);
}
