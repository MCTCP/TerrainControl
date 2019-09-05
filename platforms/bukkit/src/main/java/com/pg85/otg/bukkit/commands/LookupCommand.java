package com.pg85.otg.bukkit.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.pg85.otg.bukkit.BukkitBiome;
import com.pg85.otg.bukkit.OTGPerm;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.common.LocalWorld;

public class LookupCommand extends BaseCommand {
	LookupCommand(OTGPlugin _plugin) {
		super(_plugin);
		name = "lookup";
		perm = OTGPerm.CMD_LOOKUP.node;
		usage = "lookup <biome name or id>";
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {

		if (args.isEmpty())
			sender.sendMessage("Usage: /otg " + usage);

		LocalWorld world = this.getWorld(sender, "");

		String biomeNameOrID = "";
		for (int i = 0; i < args.size(); i++) {
			biomeNameOrID += args.get(i) + " ";
		}

		biomeNameOrID = biomeNameOrID.trim();

		int biomeId = -1;
		try {
			biomeId = Integer.parseInt(biomeNameOrID.replace(" ", ""));
		} catch (NumberFormatException ex) {
		}

		if (biomeId == -1) {
			BukkitBiome targetBiome = (BukkitBiome) world.getBiomeByNameOrNull(biomeNameOrID);
			if (targetBiome != null) {
				sender.sendMessage(MESSAGE_COLOR + "Biome \"" + VALUE_COLOR + biomeNameOrID + MESSAGE_COLOR
						+ "\" is currently registered with ID " + VALUE_COLOR + targetBiome.getIds().getOTGBiomeId());
			} else {
				sender.sendMessage(ERROR_COLOR + "The biome \"" + biomeNameOrID + "\" is not registered by OTG.");
			}
		} else {
			BukkitBiome targetBiome = (BukkitBiome) world.getBiomeByOTGIdOrNull(biomeId);
			if (targetBiome != null) {
				sender.sendMessage(MESSAGE_COLOR + "Biome ID " + VALUE_COLOR + biomeId + MESSAGE_COLOR
						+ " currently belongs to biome \"" + VALUE_COLOR + targetBiome.getName() + MESSAGE_COLOR
						+ "\"");
			} else {
				sender.sendMessage(ERROR_COLOR + "The biome ID " + biomeId + " is not registered by OTG.");
			}
		}
		return true;
	}
}