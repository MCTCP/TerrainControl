package com.pg85.otg.spigot.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.gen.MCWorldGenRegion;
import com.pg85.otg.spigot.gen.OTGSpigotChunkGen;
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
		boolean force = false;
		if(strings.containsKey("3"))
		{
			force = Boolean.getBoolean(strings.get("3"));
		}
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
		// TODO: Why can watched block be null?
		if(block == null)
		{
			block = player.getLocation().getBlock();
		}
		Preset preset = ExportCommand.getPresetOrDefault(presetName);
		SpigotWorldGenRegion genRegion;
		if((((CraftWorld)((Player)sender).getWorld()).getGenerator() instanceof OTGSpigotChunkGen))
		{
			genRegion = new SpigotWorldGenRegion(
				preset.getFolderName(), 
				preset.getWorldConfig(), 
				((CraftWorld)player.getWorld()).getHandle(),
				((OTGSpigotChunkGen)((CraftWorld)((Player)sender).getWorld()).getGenerator()).generator
			);
		} else {
			genRegion = new MCWorldGenRegion(
				preset.getFolderName(), 
				preset.getWorldConfig(), 
				((CraftWorld) player.getWorld()).getHandle()
			);
		}

		if(objectToSpawn instanceof BO4)
		{
	    	if(!(((CraftWorld)((Player)sender).getWorld()).getGenerator() instanceof OTGSpigotChunkGen))
	    	{
	    		sender.sendMessage("BO4 objects can only be spawned in OTG worlds/dimensions.");
	    		return true;
	    	}			
        	if(preset.getWorldConfig().getCustomStructureType() != CustomStructureType.BO4)
        	{
        		sender.sendMessage("Cannot spawn a BO4 structure in an isOTGPlus:false world, use a BO3 instead or recreate the world with IsOTGPlus:true in the worldconfig.");
        		return true;
        	}

    		CustomStructureCache cache = ((OTGSpigotChunkGen)((CraftWorld)((Player)sender).getWorld()).getGenerator()).generator.getStructureCache(player.getWorld().getWorldFolder().toPath());

        	// Try spawning the structure in available chunks around the player
            int maxRadius = 1000;  
            OTG.getEngine().getLogger().log(LogMarker.INFO, "Trying to plot BO4 structure within " + maxRadius + " chunks of player, with height bounds " + (force ? "disabled" : "enabled") + ". This may take a while."); 
            sender.sendMessage("Trying to plot BO4 structure within " + maxRadius + " chunks of player, with height bounds " + (force ? "disabled" : "enabled") + ". This may take a while.");
            int playerX = block.getX();
            int playerZ = block.getZ();
            ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords(playerX, playerZ);
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
                            	final ChunkCoordinate chunkCoordSpawned = 
                        			cache.plotBo4Structure(
                    					genRegion, 
                    					(BO4)objectToSpawn, 
                    					new ArrayList<String>(), 
                    					chunkCoord, 
                    					OTG.getEngine().getOTGRootFolder(), 
                    					OTG.getEngine().getPluginConfig().getSpawnLogEnabled(), 
                    					OTG.getEngine().getLogger(), 
                    					OTG.getEngine().getCustomObjectManager(), 
                    					OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), 
                    					OTG.getEngine().getCustomObjectResourcesManager(), 
                    					OTG.getEngine().getModLoadedChecker(), 
                    					force
                					)
                    			;
                            	
                            	if(chunkCoordSpawned != null)
                            	{
                                    OTG.getEngine().getLogger().log(LogMarker.INFO, objectToSpawn.getName() + " was spawned at " + chunkCoordSpawned.getBlockX() + " ~ " + chunkCoordSpawned.getBlockZ());
                            		sender.sendMessage(objectToSpawn.getName() + " was spawned at " + chunkCoordSpawned.getBlockX() + " ~ " + chunkCoordSpawned.getBlockZ());
                            		return true;
                            	}
                            }
                        }
                    }
                }
            }
            OTG.getEngine().getLogger().log(LogMarker.INFO, objectToSpawn.getName() + " could not be spawned. This can happen if the world is currently generating chunks, if no biomes with enough space could be found, or if there is an error in the structure's files. Enable SpawnLog:true in OTG.ini and check the logs for more information.");
            sender.sendMessage(objectToSpawn.getName() + " could not be spawned. This can happen if the world is currently generating chunks, if no biomes with enough space could be found, or if there is an error in the structure's files. Enable SpawnLog:true in OTG.ini and check the logs for more information.");
        	return true;        	
		} else {
			if (objectToSpawn.spawnForced(
				null,
				genRegion,
				new Random(),
				Rotation.NORTH,
				block.getX(),
				block.getY(),
				block.getZ(),
				!(genRegion instanceof MCWorldGenRegion)
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
	public static CustomObject getObject(String objectName, String presetFolderName)
	{
		if (presetFolderName == null)
		{
			presetFolderName = OTG.getEngine().getPresetLoader().getDefaultPresetFolderName();
		}
		return OTG.getEngine().getCustomObjectManager().getGlobalObjects().getObjectByName(
			objectName,
			presetFolderName,
			OTG.getEngine().getOTGRootFolder(),
			false,
			OTG.getEngine().getLogger(),
			OTG.getEngine().getCustomObjectManager(),
			OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName),
			OTG.getEngine().getCustomObjectResourcesManager(),
			OTG.getEngine().getModLoadedChecker()
		);
	}
}
