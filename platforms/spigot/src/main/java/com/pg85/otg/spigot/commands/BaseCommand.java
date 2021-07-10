package com.pg85.otg.spigot.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface BaseCommand
{
	public List<String> onTabComplete(CommandSender sender, String[] args);
	public boolean execute(CommandSender sender, String[] args);
}
