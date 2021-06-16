package com.pg85.otg.spigot.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.logging.LogMarker;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlushCommand
{
	public static boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can execute this command");
			return true;
		}
	
		OTG.log(LogMarker.INFO, "Unloading BO2/BO3/BO4 files");
		OTG.getEngine().getCustomObjectManager().reloadCustomObjectFiles();
		sender.sendMessage("Objects unloaded.");
		return true;
	}
}
