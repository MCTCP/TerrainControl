package com.pg85.otg.forge.commands;

import java.util.List;

import com.pg85.otg.common.LocalWorld;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;

public class StructureCommand extends BaseCommand {
	StructureCommand() {

		name = "structure";
		usage = "structure";
		description = "View author and description information for any structure at the player's coordinates.";
	}

	@Override
	public boolean onCommand(ICommandSender sender, List<String> args) {

		LocalWorld world = this.getWorld(sender, "");

		if (world == null) {
			sender.sendMessage(new TextComponentTranslation(
					ERROR_COLOR + "This command is only available for OpenTerrainGenerator worlds."));
			return true;
		}

		String structureInfo = world.getWorldSession().getStructureInfoAt(sender.getPosition().getX(),
				sender.getPosition().getZ());

		if (structureInfo.length() > 0) {
			for (String messagePart : structureInfo.split("\r\n")) {
				sender.sendMessage(new TextComponentTranslation(messagePart));
			}
		} else {
			sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "There is no structure at this location."));
		}
		return true;
	}
}