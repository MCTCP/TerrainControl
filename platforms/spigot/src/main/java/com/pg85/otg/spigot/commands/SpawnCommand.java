package com.pg85.otg.spigot.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.spigot.gen.SpigotWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
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
		if (!(sender instanceof Player))
		{
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
		SpigotWorldGenRegion genRegion = new SpigotWorldGenRegion(
			preset.getFolderName(), 
			preset.getWorldConfig(), 
			((CraftWorld) player.getWorld()).getHandle(), 
			((CraftWorld) player.getWorld()).getHandle().getChunkProvider().getChunkGenerator()
		);
		CustomStructureCache cache = 
			((CraftWorld) player.getWorld()).getHandle().getChunkProvider().getChunkGenerator() instanceof OTGNoiseChunkGenerator ?
			((OTGNoiseChunkGenerator)((CraftWorld) player.getWorld()).getHandle().getChunkProvider().getChunkGenerator()).getStructureCache(player.getWorld().getWorldFolder().toPath()) :
			null
		;

		// Cache is only null in non-OTG worlds
		if (cache == null && objectToSpawn.doReplaceBlocks())
		{
			sender.sendMessage("Cannot spawn objects with DoReplaceBlocks in non-OTG worlds");
			return true;
		}
		
		if(objectToSpawn instanceof BO4)
		{
        	if(preset.getWorldConfig().getCustomStructureType() != CustomStructureType.BO4)
        	{
        		sender.sendMessage("Cannot spawn a BO4 structure in an isOTGPlus:false world, use a BO3 instead or recreate the world with IsOTGPlus:true in the worldconfig.");
        		return true;
        	}
        	
        	// Try spawning the structure in available chunks around the player
            int playerX = block.getX();
            int playerZ = block.getZ();
            ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords(playerX, playerZ);
            int maxRadius = 1000;
            ChunkCoordinate chunkCoord;
            for (int cycle = 1; cycle < maxRadius; cycle++)
            {
                for (int x1 = playerX - cycle; x1 <= playerX + cycle; x1++)
                {
                    for (int z1 = playerZ - cycle; z1 <= playerZ + cycle; z1++)
                    {
                        if (x1 == playerX - cycle || x1 == playerX + cycle || z1 == playerZ - cycle || z1 == playerZ + cycle)
                        {
                            chunkCoord = ChunkCoordinate.fromChunkCoords(
                                playerChunk.getChunkX() + (x1 - playerX),
                                playerChunk.getChunkZ() + (z1 - playerZ)
                            );

                            // Find an area of chunks nearby that hasn't been generated yet, so we can plot BO4's on top.
                            // The plotter will avoid any chunks that have already been plotted, but let's not spam it more
                            // than we need to.
                            if(!player.getWorld().isChunkGenerated(chunkCoord.getChunkX(), chunkCoord.getChunkZ()))
                            {
                            	// TODO: Add targetBiomes parameter for command.
                            	final ChunkCoordinate chunkCoordSpawned = cache.plotBo4Structure(genRegion, (BO4)objectToSpawn, new ArrayList<String>(), chunkCoord, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getPluginConfig().getSpawnLogEnabled(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getMaterialReader(), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
                            	if(chunkCoordSpawned != null)
                            	{
                            		sender.sendMessage(objectToSpawn.getName() + " was spawned at " + chunkCoordSpawned.getBlockX() + " ~ " + chunkCoordSpawned.getBlockZ());
                            		return true;
                            	}
                            }
                        }
                    }
                }
            }
            sender.sendMessage(objectToSpawn.getName() + " could not be spawned. This can happen if the world is currently generating chunks, if no biomes with enough space could be found, or if there is an error in the structure's files. Enable SpawnLog:true in OTG.ini and check the logs for more information.");
        	return true;        	
		} else {		
			if (objectToSpawn.spawnForced(
				null,
				new SpigotWorldGenRegion(
					preset.getFolderName(), 
					preset.getWorldConfig(), 
					((CraftWorld) player.getWorld()).getHandle(),
					((CraftWorld) player.getWorld()).getHandle().getChunkProvider().getChunkGenerator()
				),
				new Random(),
				Rotation.NORTH,
				block.getX(),
				block.getY(),
				block.getZ()
			))
			{
				sender.sendMessage("Spawned object " + objectName + " at " + block.getLocation().toString());
			} else {
				sender.sendMessage("Failed to spawn object " + objectName);
			}
		}

		return true;
	}
	public static Block getWatchedBlock(Player me, boolean verbose)
	{
		if (me == null)
		{
			return null;
		}

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
			OTG.getEngine().getModLoadedChecker()
		);
	}
}
