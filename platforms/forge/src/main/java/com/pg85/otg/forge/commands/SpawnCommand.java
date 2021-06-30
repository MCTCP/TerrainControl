package com.pg85.otg.forge.commands;

import com.pg85.otg.OTG;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.forge.gen.MCWorldGenRegion;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.storage.FolderName;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

public class SpawnCommand
{
	public static int execute(CommandSource source, String presetName, String objectName, BlockPos blockPos, boolean force)
	{
		try
		{
			presetName = presetName != null && presetName.equalsIgnoreCase("global") ? null : presetName;
			CustomObject objectToSpawn = getObject(objectName, presetName);

			if (objectToSpawn == null)
			{
				source.sendSuccess(new StringTextComponent("Could not find an object by the name " + objectName + " in either " + presetName + " or Global objects"), false);
				return 0;
			}

			Preset preset = ExportCommand.getPresetOrDefault(presetName);
			ForgeWorldGenRegion genRegion;
			if(source.getLevel().getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator)
			{
				genRegion = new ForgeWorldGenRegion(
					preset.getFolderName(), 
					preset.getWorldConfig(), 
					source.getLevel(), 
					(OTGNoiseChunkGenerator)source.getLevel().getChunkSource().getGenerator()
				);
			} else {
				genRegion = new MCWorldGenRegion(
					preset.getFolderName(), 
					preset.getWorldConfig(), 
					source.getLevel()
				);
			}
			
			Path worldSaveFolder = source.getLevel().getServer().getWorldPath(FolderName.PLAYER_DATA_DIR).getParent();

			if(objectToSpawn instanceof BO4)
			{
				if(!(source.getLevel().getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
				{
	        		source.sendSuccess(new StringTextComponent("BO4 objects can only be spawned in OTG worlds/dimensions."), false);
	        		return 0;					
				}
	        	if(preset.getWorldConfig().getCustomStructureType() != CustomStructureType.BO4)
	        	{
	        		source.sendSuccess(new StringTextComponent("Cannot spawn a BO4 structure in an isOTGPlus:false world, use a BO3 instead or recreate the world with IsOTGPlus:true in the worldconfig."), false);
	        		return 0;
	        	}
	        	
				CustomStructureCache cache = ((OTGNoiseChunkGenerator) source.getLevel().getChunkSource().getGenerator()).getStructureCache(worldSaveFolder);
	        	
	        	// Try spawning the structure in available chunks around the player
	        	int maxRadius = 1000;
	        	source.sendSuccess(new StringTextComponent("Trying to plot BO4 structure within " + maxRadius + " chunks of player, with height bounds " + (force ? "disabled" : "enabled") + ". This may take a while."), false);
	            int playerX = blockPos.getX();
	            int playerZ = blockPos.getZ();
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
	                            if(
                            		!source.getLevel().hasChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()) || 
                            		!source.getLevel().getChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ()).getStatus().isOrAfter(ChunkStatus.FEATURES)
                        		)
	                            {
	                            	// TODO: Add targetBiomes parameter for command.
	                            	final ChunkCoordinate chunkCoordSpawned = 
                            			cache.plotBo4Structure(
	                            			genRegion, 
	                            			(BO4)objectToSpawn, 
	                            			new ArrayList<String>(), 
	                            			chunkCoord, 
	                            			OTG.getEngine().getOTGRootFolder(),
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
	                            		source.sendSuccess(new StringTextComponent(objectToSpawn.getName() + " was spawned at: "), false);
	                            		ITextComponent itextcomponent = TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("chat.coordinates", chunkCoordSpawned.getBlockX(), "~", chunkCoordSpawned.getBlockZ())).withStyle((p_241055_1_) -> {
	                            			return p_241055_1_.withColor(TextFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + chunkCoordSpawned.getBlockX() + " ~ " + chunkCoordSpawned.getBlockZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip")));
	                            		});	                            		
	                            		source.sendSuccess(itextcomponent, false);
	                            		return 0;
	                            	}
	                            }
	                        }
	                    }
	                }
	            }
	            source.sendSuccess(new StringTextComponent(objectToSpawn.getName() + " could not be spawned. This can happen if the world is currently generating chunks, if no biomes with enough space could be found, or if there is an error in the structure's files. Enable SpawnLog:true in OTG.ini and check the logs for more information."), false);
	        	return 0;
			} else {
				if (objectToSpawn.spawnForced(
					null,
					genRegion,
					new Random(),
					Rotation.NORTH,
					blockPos.getX(),
					blockPos.getY(),
					blockPos.getZ(),
					!(genRegion instanceof MCWorldGenRegion)
				))
				{
            		source.sendSuccess(new StringTextComponent(objectToSpawn.getName() + " was spawned at: "), false);
            		ITextComponent itextcomponent = TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("chat.coordinates", blockPos.getX(), "~", blockPos.getZ())).withStyle((p_241055_1_) -> {
            			return p_241055_1_.withColor(TextFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos.getX() + " ~ " + blockPos.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip")));
            		});	                            		
            		source.sendSuccess(itextcomponent, false);
				} else {
					source.sendSuccess(new StringTextComponent("Failed to spawn object " + objectName), false);
				}
			}
		}
		catch (Exception e)
		{
			source.sendSuccess(new StringTextComponent("Something went wrong, please check logs"), false);
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, String.format("Error during spawn command: ", (Object[])e.getStackTrace()));
		}
		return 0;
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
			OTG.getEngine().getLogger(),
			OTG.getEngine().getCustomObjectManager(),
			OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName),
			OTG.getEngine().getCustomObjectResourcesManager(),
			OTG.getEngine().getModLoadedChecker());
	}
}
