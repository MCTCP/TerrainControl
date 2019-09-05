package com.pg85.otg.forge.commands;

import java.util.List;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.forge.biomes.ForgeBiome;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;

public class LookupCommand extends BaseCommand {
	LookupCommand() {

		name = "lookup";
		usage = "lookup <biome name or id>";
		description = "Look up a registered biome by name or id.";
	}

	@Override
	public boolean onCommand(ICommandSender sender, List<String> args) {

		if (args.isEmpty())
			return false;

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
			ForgeBiome targetBiome = (ForgeBiome) world.getBiomeByNameOrNull(biomeNameOrID);
			if (targetBiome != null) {
				sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Biome \"" + VALUE_COLOR + biomeNameOrID
						+ MESSAGE_COLOR + "\" is currently registered with ID " + VALUE_COLOR
						+ targetBiome.getIds().getOTGBiomeId()));
			} else {
				sender.sendMessage(new TextComponentTranslation(
						ERROR_COLOR + "The biome \"" + biomeNameOrID + "\" is not registered by OTG."));
			}
		} else {
			ForgeBiome targetBiome = (ForgeBiome) world.getBiomeByOTGIdOrNull(biomeId);
			if (targetBiome != null) {
				sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Biome ID " + VALUE_COLOR + biomeId
						+ MESSAGE_COLOR + " currently belongs to biome \"" + VALUE_COLOR + targetBiome.getName()
						+ MESSAGE_COLOR + "\""));
			} else {
				sender.sendMessage(new TextComponentTranslation(
						ERROR_COLOR + "The biome ID " + biomeId + " is not registered by OTG."));
			}
		}
		return true;
	}
}