package com.pg85.otg.spigot.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructure;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructure;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.spigot.gen.OTGSpigotChunkGen;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;

public class StructureCommand implements BaseCommand
{
	public boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Only players can execute this command");
			return true;
		}
		Player player = (Player) sender;
	
		String structureInfo = "";
		ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords((int)((Player)sender).getLocation().getBlockX(), (int)((Player)sender).getLocation().getBlockZ());
		// if the player is in range

		CustomStructure worldInfoChunk = ((OTGSpigotChunkGen)((CraftWorld)((Player)sender).getWorld()).getGenerator()).generator.getStructureCache(player.getWorld().getWorldFolder().toPath()).getChunkData(playerChunk);
		if(worldInfoChunk != null)
		{
			Path otgRootFolder = OTG.getEngine().getOTGRootFolder();
			ILogger logger = OTG.getEngine().getLogger();
			CustomObjectManager customObjectManager = OTG.getEngine().getCustomObjectManager();
			IMaterialReader materialReader = OTG.getEngine().getPresetLoader().getMaterialReader(((OTGSpigotChunkGen)((CraftWorld)((Player)sender).getWorld()).getGenerator()).getPreset().getFolderName());
			CustomObjectResourcesManager manager = OTG.getEngine().getCustomObjectResourcesManager();
			IModLoadedChecker modLoadedChecker = OTG.getEngine().getModLoadedChecker();
			
			if(worldInfoChunk instanceof BO4CustomStructure)
			{				
				structureInfo += "-- BO4 Info -- \nName: " + ((BO4)worldInfoChunk.start.getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getConfig().getName().replace("Start", "") + "\nAuthor: " + ((BO4)worldInfoChunk.start.getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getConfig().author + "\nDescription: " + ((BO4)worldInfoChunk.start.getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getConfig().description;
				String branchesInChunk = ((BO4CustomStructure)worldInfoChunk).getObjectsToSpawnInfo().get(playerChunk);
				if(branchesInChunk != null && branchesInChunk.length() > 0)
				{
					structureInfo += "\n" + branchesInChunk;
				}
			} else {
				structureInfo += "-- BO3 Info -- \nName: " + ((BO3)worldInfoChunk.start.getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getSettings().getName().replace("Start", "") + "\nAuthor: " + ((BO3)worldInfoChunk.start.getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getSettings().author + "\nDescription: " + ((BO3)worldInfoChunk.start.getObject(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker)).getSettings().description;
			}
		}		
	
		sender.sendMessage(structureInfo);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		return Collections.emptyList();
	}
}
