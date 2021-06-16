package com.pg85.otg.forge.commands;

import java.nio.file.Path;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructure;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructure;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

public class StructureCommand
{
	protected static int showStructureInfo(CommandSource source)
	{
		if (!(source.getLevel().getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new StringTextComponent("OTG is not enabled in this world"), false);
			return 0;
		}
		
		String structureInfo = "";
		ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords((int)source.getPosition().x, (int)source.getPosition().z);
		// if the player is in range
		CustomStructure worldInfoChunk = ((OTGNoiseChunkGenerator)source.getLevel().getChunkSource().generator).getStructureCache().getChunkData(playerChunk);
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
		
		source.sendSuccess(new StringTextComponent(structureInfo), false);
		return 0;
	}
}
