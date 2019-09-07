package com.pg85.otg.bukkit.commands;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.OTGPerm;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.bukkit.util.BO3Creator;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class ExportCommand extends BaseCommand {

	private final boolean hasWorldedit;

	public ExportCommand(OTGPlugin _plugin) {
		super(_plugin);
		name = "export";
		perm = OTGPerm.CMD_EXPORT.node;
		usage = "export <name> [center_block] [-a include_air] [-t include_tile_entities] [-o override]";
		hasWorldedit = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") != null;
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		if (!hasWorldedit) {
			sender.sendMessage(ERROR_COLOR + "You must have WorldEdit installed to use this command.");
			return true;
		}

		if (args.isEmpty()) {
			sender.sendMessage(ERROR_COLOR + "You must enter a name for the object.");
			return true;
		}

		Player player = (Player) sender;

		WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		Selection selection = worldEdit.getSelection(player);

		if (selection == null) {
			sender.sendMessage(ERROR_COLOR + "No WorldEdit selection found.");
			return true;
		}

		String bo3Name = args.get(0);
		File target = new File(OTG.getEngine().getGlobalObjectsDirectory(), bo3Name + ".bo3");

		if (target.exists() && !args.contains("-o")) {
			sender.sendMessage(ERROR_COLOR + "A BO3 with that name already exists, use -o to override.");
			return true;
		}

		BO3Creator creator = new BO3Creator(bo3Name);

		creator.includeAir(args.contains("-a"));
		creator.includeTiles(args.contains("-t"));

		String block = args.size() > 1 ? args.get(1) : "";
		boolean branch = selection.getWidth() > 32 || selection.getLength() > 32;

		creator.create(selection, block, branch);

		sender.sendMessage(String.format("%sBO3 %s%s %2$s(%dx%dx%d) %1%shas been saved to GlobalObjects.",
				MESSAGE_COLOR, VALUE_COLOR, name, selection.getWidth(), selection.getHeight(), selection.getLength()));

		if (branch)
			sender.sendMessage(MESSAGE_COLOR + "BO3 was larger than 32x32 so it has been split into branches.");

		return true;
	}

}
