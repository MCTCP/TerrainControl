package com.pg85.otg.forge.commands;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructure;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.resource.CustomStructureGen;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.common.LocalBiome;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;

public class ExportDataCommand extends BaseCommand
{
    ExportDataCommand()
    {
        name = "exportBO4Data";
        usage = "exportBO4Data";
        description = "Exports all BO4 files and BO3 files that have isOTGPlus:true as BO4Data files (if none exist already). BO4Data files can significantly reduce filesize and loading times, and should be used by OTG content creators when packaging presets for players.";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        LocalWorld world = this.getWorld(sender, "");
        if(world.getConfigs().getWorldConfig().isOTGPlus)
        {
	        sender.sendMessage(
	                new TextComponentString(MESSAGE_COLOR + "Exporting .BO4Data files for world, this may take a while."));
	        
            OTG.log(LogMarker.INFO, "Initializing and exporting structure starts");
	        
            DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(world.getName());
            
	        // Make sure all structure starts in the world have been initialised
	        // so that getMinimumSize has been done and its data can be saved with the BO4Data.
	        for(LocalBiome biome : world.getAllBiomes())
	        {
	        	for(ConfigFunction<BiomeConfig> res : biome.getBiomeConfig().resourceSequence)
	        	{
	        		if(res instanceof CustomStructureGen)
	        		{
	        			for(StructuredCustomObject structure : ((CustomStructureGen)res).getObjects(dimConfig.PresetName))
	        			{
	        				if(structure != null) // Structure was in resource list but file could not be found.
	        				{
	        					if(structure instanceof BO4)
	        					{
	        						if(!OTG.bo4DataExists(((BO4)structure).getConfig()))
	        						{
		        	        			BO4CustomStructureCoordinate structureCoord = new BO4CustomStructureCoordinate(world, structure, null, Rotation.NORTH, 0, (short)0, 0, 0, false, false, null);
		        	        			BO4CustomStructure structureStart = new BO4CustomStructure(world, structureCoord);
		        	        			
		        	                	// Get minimum size (size if spawned with branchDepth 0)
		        	                	try {
		        	                		structureStart.getMinimumSize(world);
		        						}
		        	                	catch (InvalidConfigException e)
		        	                	{
		        							((BO4)structure).isInvalidConfig = true;
		        						}
		        	                	
		        	                	OTG.log(LogMarker.INFO, "Exporting .BO4Data for structure start " + ((BO4)structure).getName());
		        	    	            OTG.generateBO4Data(((BO4)structure).getConfig());
		        	    	            OTG.getEngine().getCustomObjectManager().getGlobalObjects().unloadCustomObjectFiles();
		        	    	            
		        	                	// TODO: Sending a message here will only show it after this command finishes, export async?
		        	                	//sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Exporting .BO4Data for structure start " + VALUE_COLOR + ((BO4)structure).getName()));
	        						}
	        					}
	        				}
	        			}
	        		}
	        	}
	        }

	        ArrayList<String> boNames = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForWorld(dimConfig.PresetName);

	        int i = 0;
	        for (String boName : boNames)
	        {
	            i++;
	        	CustomObject bo = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getObjectByName(boName, dimConfig.PresetName);
	        	if(bo != null && bo instanceof BO4 && !OTG.bo4DataExists(((BO4)bo).getConfig()))
	        	{
		            OTG.log(LogMarker.INFO, "Exporting .BO4Data " + i + "/" + boNames.size() + " " + boName);
                	// TODO: Sending a message here will only show it after this command finishes, export async?
		            // sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Exporting .BO4Data " + VALUE_COLOR + i + "/" + boNames.size() + " " + boName));
		            OTG.generateBO4Data(((BO4)bo).getConfig());
		            OTG.getEngine().getCustomObjectManager().getGlobalObjects().unloadCustomObjectFiles();
	        	}
	        }
	        sender.sendMessage(new TextComponentString(MESSAGE_COLOR + ".BO4Data export complete."));
        } else {
        	sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "The ExportBO4Data command is only available for IsOTGPlus:true worlds."));
        }
        return true;
    }
}