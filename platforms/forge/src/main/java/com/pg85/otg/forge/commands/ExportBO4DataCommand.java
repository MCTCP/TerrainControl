package com.pg85.otg.forge.commands;

import java.nio.file.Path;
import java.util.ArrayList;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.config.biome.BiomeConfig;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.bo4.BO4Data;
import com.pg85.otg.customobject.resource.CustomStructureResource;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructure;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IStructuredCustomObject;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.storage.LevelResource;

public class ExportBO4DataCommand extends BaseCommand
{
	private static boolean isRunning = false;
	private static boolean isDone = false;
	private static int current = 0;
	private static int total = 0;
	private static String boName = "";
	
	public ExportBO4DataCommand()
	{
		super("preset");
		this.helpMessage = "Exports all BO4 files and BO3 files that have isOTGPlus:true as BO4Data files (if none exist already). BO4Data files can significantly reduce filesize and loading times, and should be used by OTG content creators when packaging presets for players.";
		this.usage = "/otg exportbo4data";
	}
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(Commands.literal("exportbo4data")
			.executes(context -> exportBO4Data(context.getSource()))
		);
	}
	
	private int exportBO4Data(CommandSourceStack source)
	{		
		if (!(source.getLevel().getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(new TextComponent("OTG is not enabled in this world"), false);
			return 0;
		}

		Preset preset = ((OTGNoiseChunkGenerator)source.getLevel().getChunkSource().getGenerator()).getPreset();
        if(preset.getWorldConfig().getCustomStructureType() == CustomStructureType.BO4)
        {
        	if(!isRunning)
        	{
        		isDone = false;
        		isRunning = true;        		
            	source.sendSuccess(new TextComponent("Exporting .BO4Data files for world, this may take a while."), false);
            	source.sendSuccess(new TextComponent("Run this command again to see progress or check the logs."), false);
        		new Thread(() -> {        		
		            OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Initializing and exporting structure starts");
			                    
			        // Make sure all structure starts in the world have been initialised
			        // so that getMinimumSize has been done and its data can be saved with the BO4Data.
			        for(IBiomeConfig biomeConfig : preset.getAllBiomeConfigs())
			        {
			        	for(ConfigFunction<IBiomeConfig> res : ((BiomeConfig)biomeConfig).getResourceQueue())
			        	{
			        		if(res instanceof CustomStructureResource)
			        		{
			        			for(IStructuredCustomObject structure : ((CustomStructureResource)res).getObjects(preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker()))
			        			{
			        				if(structure != null) // Structure was in resource list but file could not be found.
			        				{
			        					if(structure instanceof BO4)
			        					{
			        						if(!BO4Data.bo4DataExists(((BO4)structure).getConfig()))
			        						{
				        	        			BO4CustomStructureCoordinate structureCoord = new BO4CustomStructureCoordinate(preset.getFolderName(), structure, null, Rotation.NORTH, 0, (short)0, 0, 0, false, false, null);
				        	        			BO4CustomStructure structureStart = new BO4CustomStructure(source.getLevel().getSeed(), structureCoord, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
				        	        			
				        	                	// Get minimum size (size if spawned with branchDepth 0)
				        	                	try {
				        	                		// World save folder name may not be identical to level name, fetch it.
				        	                		Path worldSaveFolder = source.getLevel().getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).getParent();
				        	                		IWorldGenRegion worldGenRegion = new ForgeWorldGenRegion(preset.getFolderName(), preset.getWorldConfig(), source.getLevel(), (OTGNoiseChunkGenerator)source.getLevel().getChunkSource().getGenerator());
				        	                		structureStart.getMinimumSize(((OTGNoiseChunkGenerator)source.getLevel().getChunkSource().getGenerator()).getStructureCache(worldSaveFolder), worldGenRegion, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
				        						}
				        	                	catch (InvalidConfigException e)
				        	                	{
				        							((BO4)structure).isInvalidConfig = true;
				        						}
				        	                	
				        	                	OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Exporting .BO4Data for structure start " + ((BO4)structure).getName());
				        	                	boName = ((BO4)structure).getName();
				        	                	BO4Data.generateBO4Data(((BO4)structure).getConfig(), preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
				        	    	            OTG.getEngine().getCustomObjectManager().getGlobalObjects().unloadCustomObjectFiles();
			        						}
			        					}
			        				}
			        			}
			        		}
			        	}
			        }
		
			        ArrayList<String> boNames = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForPreset(preset.getFolderName(), OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
		
			        current = 0;
			        total = boNames.size();
			        for (String boName : boNames)
			        {
			        	current++;
			        	CustomObject bo = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getObjectByName(boName, preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
			        	if(bo != null && bo instanceof BO4 && !BO4Data.bo4DataExists(((BO4)bo).getConfig()))
			        	{
			        		OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Exporting .BO4Data " + current + "/" + total + " " + boName);
			        		BO4Data.generateBO4Data(((BO4)bo).getConfig(), preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
				            OTG.getEngine().getCustomObjectManager().getGlobalObjects().unloadCustomObjectFiles();
			        	}
			        }
			        OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Exporting .BO4Data done.");
			        isDone = true;
        		}).start();
        	} else {
        		if(isDone)
        		{
					isRunning = false;
					isDone = false;        			
        			source.sendSuccess(new TextComponent("OTG exportbo4data is done."), false);
        		} else {
        			source.sendSuccess(new TextComponent("OTG exportbo4data is running, " + (current == 0 ? "exporting structure start " + boName : " exporting " + current + "/" + total)), false);	
        		}
        	}
        } else {
        	source.sendSuccess(new TextComponent("The ExportBO4Data command is only available for CustomStructureType:BO4 worlds."), false);
        }
		return 0;
	}
}
