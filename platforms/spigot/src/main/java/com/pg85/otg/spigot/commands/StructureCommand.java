package com.pg85.otg.spigot.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructure;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructure;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.spigot.gen.OTGSpigotChunkGen;

import java.nio.file.Path;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;

public class StructureCommand
{
	public static boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can execute this command");
			return true;
		}
	
		String structureInfo = "";
		ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords((int)((Player)sender).getLocation().getBlockX(), (int)((Player)sender).getLocation().getBlockZ());
		// if the player is in range

		CustomStructure worldInfoChunk = ((OTGSpigotChunkGen)((CraftWorld)((Player)sender).getWorld()).getGenerator()).generator.getStructureCache().getChunkData(playerChunk);
		if(worldInfoChunk != null)
		{
			Path otgRootFolder = OTG.getEngine().getOTGRootFolder();
			boolean spawnLog = OTG.getEngine().getPluginConfig().getSpawnLogEnabled();
			ILogger logger = OTG.getEngine().getLogger();
			CustomObjectManager customObjectManager = OTG.getEngine().getCustomObjectManager();
			IMaterialReader materialReader = OTG.getEngine().getMaterialReader();
			CustomObjectResourcesManager manager = OTG.getEngine().getCustomObjectResourcesManager();
			IModLoadedChecker modLoadedChecker = OTG.getEngine().getModLoadedChecker();
			
			if(worldInfoChunk instanceof BO4CustomStructure)
			{				
				structureInfo += "-- BO4 Info -- \r\nName: " + ((BO4)worldInfoChunk.start.getObject(otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getConfig().getName().replace("Start", "") + "\r\nAuthor: " + ((BO4)worldInfoChunk.start.getObject(otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getConfig().author + "\r\nDescription: " + ((BO4)worldInfoChunk.start.getObject(otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getConfig().description;
				String branchesInChunk = ((BO4CustomStructure)worldInfoChunk).getObjectsToSpawnInfo().get(playerChunk);
				if(branchesInChunk != null && branchesInChunk.length() > 0)
				{
					structureInfo += "\r\n" + branchesInChunk;
				}
			} else {
				structureInfo += "-- BO3 Info -- \r\nName: " + ((BO3)worldInfoChunk.start.getObject(otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getSettings().getName().replace("Start", "") + "\r\nAuthor: " + ((BO3)worldInfoChunk.start.getObject(otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getSettings().author + "\r\nDescription: " + ((BO3)worldInfoChunk.start.getObject(otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getSettings().description;
			}
		}		
	
		sender.sendMessage(structureInfo);
		return true;
	}
}
