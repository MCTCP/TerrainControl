package com.pg85.otg.spigot.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class BaseCommand
{
	protected final String name;
	protected String helpMessage = "No help message specified.";
	protected String usage = "No usage message specified.";
	protected String[] detailedHelp = new String[]
	{ "No detailed help specified." };

	public BaseCommand(String name)
	{
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

	public ComponentBuilder createComponent(String text1, String text2, ChatColor color1, ChatColor color2)
	{
		TextComponent component1 = new TextComponent(text1);
		component1.setColor(color1);
		TextComponent component2 = new TextComponent(text2);
		component2.setColor(color2);

		return new ComponentBuilder(component1).append(component2);
	}

	public BaseComponent createComponent(String text, ChatColor color)
	{
		TextComponent component = new TextComponent(text);
		component.setColor(color);

		return component;
	}

	public abstract List<String> onTabComplete(CommandSender sender, String[] args);

	public abstract boolean execute(CommandSender sender, String[] args);
}
