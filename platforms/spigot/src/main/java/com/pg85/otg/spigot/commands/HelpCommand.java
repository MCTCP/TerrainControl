package com.pg85.otg.spigot.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HelpCommand implements BaseCommand
{

	@Override
	public boolean execute(CommandSender sender, String[] args)
	{
		// TODO: Update this
		sender.sendMessage("OTG Help");
		sender.sendMessage("/otg map -> Creates a 2048 x 2048 biome map of the world.");
		sender.sendMessage("/otg spawn <preset name> : <object name> : <coords> (BO3 only) or <force> (BO4 only)");
		sender.sendMessage("/otg data <dataType>");
		sender.sendMessage("/otg structure");
		sender.sendMessage("/otg flush");
		sender.sendMessage("/otg edit");
		sender.sendMessage("/otg export");
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		return Collections.emptyList();
	}

}
