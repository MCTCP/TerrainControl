package com.pg85.otg.spigot.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pg85.otg.OTG;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

public class FlushCommand implements BaseCommand
{
	public boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Only players can execute this command");
			return true;
		}

		OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Unloading BO2/BO3/BO4 files");
		OTG.getEngine().getCustomObjectManager().reloadCustomObjectFiles();
		sender.sendMessage("Objects unloaded.");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		return Collections.emptyList();
	}
}
