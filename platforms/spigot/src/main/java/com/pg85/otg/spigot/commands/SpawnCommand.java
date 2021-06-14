package com.pg85.otg.spigot.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.gen.SpigotWorldGenRegion;
import com.pg85.otg.util.bo3.Rotation;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import java.util.*;

public class SpawnCommand
{
	public static boolean execute(CommandSender sender, HashMap<String, String> strings)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can execute this command");
			return true;
		}
		Player player = (Player) sender;

		String presetName = strings.get("1");
		String objectName = strings.get("2");		
		
		if (presetName == null || objectName == null)
		{
			sender.sendMessage("Please specify a preset and an object");
			return true;
		}
		presetName = presetName.equalsIgnoreCase("global") ? null : presetName;

		CustomObject objectToSpawn = getObject(objectName, presetName);

		if (objectToSpawn == null)
		{
			sender.sendMessage("Could not find an object by the name " + objectName + " in either " + presetName + " or Global objects");
			return true;
		}

		Block block = getWatchedBlock(player, false);
		Preset preset = ExportCommand.getPresetOrDefault(presetName);
		
		if (objectToSpawn.spawnForced(
			null,
			new SpigotWorldGenRegion(preset.getFolderName(), preset.getWorldConfig(), ((CraftWorld) player.getWorld()).getHandle(),
				((CraftWorld) player.getWorld()).getHandle().getChunkProvider().getChunkGenerator()),
			new Random(),
			Rotation.NORTH,
			block.getX(),
			block.getY(),
			block.getZ()
		))
		{
			sender.sendMessage("Spawned object " + objectName + " at " + block.getLocation().toString());
		} else {
			sender.sendMessage("Failed to spawn object " + objectName+". Is it a BO4?");
		}

		return true;
	}
	public static Block getWatchedBlock(Player me, boolean verbose)
	{
		if (me == null)
			return null;

		Block block;
		Block previousBlock = null;

		Iterator<Block> itr = new BlockIterator(me, 200);
		while (itr.hasNext())
		{
			block = itr.next();
			if (block.getType() != Material.AIR && block.getType() != Material.TALL_GRASS)
			{
				return previousBlock;
			}
			previousBlock = block;
		}

		if (verbose)
		{
			me.sendMessage("No block in sight.");
		}

		return null;
	}
	public static CustomObject getObject(String objectName, String presetName)
	{
		if (presetName == null)
		{
			presetName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
		}
		return OTG.getEngine().getCustomObjectManager().getGlobalObjects().getObjectByName(
			objectName,
			presetName,
			OTG.getEngine().getOTGRootFolder(),
			false,
			OTG.getEngine().getLogger(),
			OTG.getEngine().getCustomObjectManager(),
			OTG.getEngine().getMaterialReader(),
			OTG.getEngine().getCustomObjectResourcesManager(),
			OTG.getEngine().getModLoadedChecker());
	}
}
