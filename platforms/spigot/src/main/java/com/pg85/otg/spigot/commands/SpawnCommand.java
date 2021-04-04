package com.pg85.otg.spigot.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.gen.OTGSpigotChunkGen;
import com.pg85.otg.spigot.gen.SpigotWorldGenRegion;
import com.pg85.otg.util.bo3.Rotation;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.Iterator;
import java.util.Random;

public class SpawnCommand
{
	public static boolean execute(CommandSender sender, String[] strings)
	{
		// /otg spawn BOTest : fence
		//TODO: need Tab Completion, and I don't like requiring a : separator. Still, it's way cleaner code
		// than if we were to deal with the spaces some other way. It's either this, or require "" for names with spaces.

		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can execute this command");
			return true;
		}
		Player player = (Player) sender;
		if (!(player.getWorld().getGenerator() instanceof OTGSpigotChunkGen)) {
			sender.sendMessage("Can only run this command in an OTG world");
			return true;
		}
		String input = StringUtils.join(strings, ' ');
		String[] split = input.split(":");
		if (split.length != 2) {
			sender.sendMessage("Spawn requires two parameters, separated by ':'. Ex: 'preset name:object name'");
			return true;
		}
		String presetName = split[0].trim();
		String objectName = split[1].trim();

		CustomObject objectToSpawn = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getObjectByName(
			objectName,
			presetName,
			OTG.getEngine().getOTGRootFolder(),
			false,
			OTG.getEngine().getLogger(),
			OTG.getEngine().getCustomObjectManager(),
			OTG.getEngine().getMaterialReader(),
			OTG.getEngine().getCustomObjectResourcesManager(),
			OTG.getEngine().getModLoadedChecker());

		if (objectToSpawn == null)
		{
			sender.sendMessage("Could not find an object by the name " + objectName + " in either " + presetName + " or Global objects");
			return true;
		}
		Block block = getWatchedBlock(player, false);
		Preset p = OTG.getEngine().getPresetLoader().getPresetByName(presetName);

		if (objectToSpawn.spawnForced(
			((OTGSpigotChunkGen) player.getWorld().getGenerator()).generator.getStructureCache(),
			new SpigotWorldGenRegion(p.getName(), p.getWorldConfig(), ((CraftWorld) player.getWorld()).getHandle(),
				((OTGSpigotChunkGen) player.getWorld().getGenerator()).generator),
			new Random(),
			Rotation.NORTH,
			block.getX(),
			block.getY(),
			block.getZ()
		))
		{
			sender.sendMessage("Spawned object " + objectName + " at " + block.getLocation().toString());
		}
		else
		{
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
}
