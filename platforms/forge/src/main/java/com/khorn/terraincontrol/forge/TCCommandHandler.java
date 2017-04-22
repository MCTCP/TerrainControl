package com.khorn.terraincontrol.forge;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.util.CommandHelper;
import com.khorn.terraincontrol.logging.LogMarker;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.BiomeDictionary.Type;

final class TCCommandHandler implements ICommand
{
    private final List<String> aliases = Arrays.asList("otg");
    public static final TextFormatting ERROR_COLOR = TextFormatting.RED;
    public static final TextFormatting MESSAGE_COLOR = TextFormatting.GREEN;
    public static final TextFormatting VALUE_COLOR = TextFormatting.DARK_GREEN;

    @Override
    public String getCommandName()
    {
        return "otg";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "otg";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return this.aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] argString)
    {
        World mcWorld = sender.getEntityWorld();

        if (!mcWorld.isRemote) // Server side
        {
            ForgeWorld world = (ForgeWorld) ((ForgeEngine)TerrainControl.getEngine()).getWorld(mcWorld);
                        
            if (world == null && !((argString[0].equals("dimension") || argString[0].equals("dim")) && argString.length < 3))
            {
            	sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(
                        new TextComponentTranslation(ERROR_COLOR + "OpenTerrainGenerator is not enabled for this world."));
                return;
            }           
        	
            BlockPos pos = sender.getPosition();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            
            if (argString == null || argString.length == 0)
            {
        		sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(new TextComponentString("-- OpenTerrainGenerator --"));
                sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(new TextComponentString("Commands:"));
                sender.addChatMessage(
                        new TextComponentString(MESSAGE_COLOR + "/otg worldinfo " + VALUE_COLOR + "Show author and description information for this world."));
                sender.addChatMessage(
                        new TextComponentString(MESSAGE_COLOR + "/otg biome (-f, -s, -d, -m) " + VALUE_COLOR + "Show biome information for the biome at the player's coordinates."));
                sender.addChatMessage(
                		new TextComponentString(MESSAGE_COLOR + "/otg entities " + VALUE_COLOR + "Show a list of entities that can be spawned inside BO3's using the Entity() tag."));
                //sender.addChatMessage(
                		//new TextComponentString(MESSAGE_COLOR + "/otg cartographer " + VALUE_COLOR + "Teleports the player to the center of the Cartographer map. Same as /otg map."));
                sender.addChatMessage(
                		new TextComponentString(MESSAGE_COLOR + "/otg pregenerator <radius> " + VALUE_COLOR + "Sets the pre-generation radius to <radius> chunks. Same as /otg pregen <radius>."));
                sender.addChatMessage(
                		new TextComponentString(MESSAGE_COLOR + "/otg dimension " + VALUE_COLOR + "Shows the name and id of the dimension the player is currently in. Same as /otg dim."));
                sender.addChatMessage(
                		new TextComponentString(MESSAGE_COLOR + "/otg dimension -l " + VALUE_COLOR + "Shows a list of all dimensions. Same as /otg dim -l."));
                sender.addChatMessage(
                		new TextComponentString(MESSAGE_COLOR + "/otg dimension -c <dimension name> " + VALUE_COLOR + "Creates a dimension using world and biome configs from mods/OpenTerrainGenerator/worlds/<dimension name>. Custom dimensions can be accessed via a quartz portal. Biome names must be unique across dimensions. Same as /otg dim -c <dimension name>"));
                sender.addChatMessage(
                		new TextComponentString(MESSAGE_COLOR + "/otg dimension -d <dimension name> " + VALUE_COLOR + "Deletes the specified dimension. Dimension must be unloaded (dimensions unload automatically when no players are inside, this may take a minute). Same as /otg dim -d <dimension name>"));
                sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(new TextComponentString("Tips:"));
        		sender.addChatMessage(new TextComponentString(MESSAGE_COLOR + "- Check out OpenTerrainGenerator.ini and each world's WorldConfig.ini for optional features."));
        		sender.addChatMessage(new TextComponentString(MESSAGE_COLOR + "- When using the pre-generator in single player open the chat window so you can background MC without pausing the game."));
            }
            else if (argString[0].equals("worldinfo") || argString[0].equals("world"))
            {
                WorldConfig worldConfig = world.getConfigs().getWorldConfig();
        		sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(new TextComponentString("-- World info --"));
                sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(new TextComponentString(MESSAGE_COLOR + "Author: " + VALUE_COLOR + worldConfig.author));
                sender.addChatMessage(new TextComponentString(MESSAGE_COLOR + "Description: " + VALUE_COLOR + worldConfig.description));
            }
            else if (argString[0].equals("pregenerator") || argString[0].equals("pregen"))
            {
            	if (argString.length > 1)
            	{
            		int radius = 0;
            		try
            		{
            			radius = Integer.parseInt(argString[1]);
            		} catch(java.lang.NumberFormatException ex)
            		{
            			sender.addChatMessage(
                                new TextComponentTranslation(ERROR_COLOR + "\"" + argString[1] + "\" could not be parsed as a number."));
            			return;
            		}
            		
            		world = (ForgeWorld) ((ForgeEngine)TerrainControl.getEngine()).getWorld(DimensionManager.getWorld(0));
            		
	                int newRadius = world.getConfigs().getWorldConfig().PreGenerationRadius = ((ForgeEngine)TerrainControl.getEngine()).getPregenerator().setPregenerationRadius(radius, world.getWorld());
	                
	                ((ServerConfigProvider)world.getConfigs()).saveWorldConfig();
	                
        			sender.addChatMessage(
                            new TextComponentTranslation(MESSAGE_COLOR + "Pre-generator radius set to " + VALUE_COLOR + newRadius + MESSAGE_COLOR + "."));
        			return;
            	}
            }
            else if (argString[0].equals("entities"))
            {                
        		sender.addChatMessage(new TextComponentString(""));
	    		TerrainControl.log(LogMarker.INFO, "-- Entities List --");
	    		sender.addChatMessage(new TextComponentString("-- Entities List --"));
	    		sender.addChatMessage(new TextComponentString(""));
	    		sender.addChatMessage(new TextComponentString(MESSAGE_COLOR + "Some of these, like ThrownPotion, FallingSand, Mob and Painting may crash the game so be sure to test your BO3 in single player."));
    			sender.addChatMessage(new TextComponentString(""));
    			EnumCreatureType[] aenumcreaturetype = EnumCreatureType.values();
	    		for(String entry : EntityList.NAME_TO_CLASS.keySet())
	        	{
	    			String msg = entry;
	    		    for (int k3 = 0; k3 < aenumcreaturetype.length; ++k3)
	    		    {
	    		        EnumCreatureType enumcreaturetype = aenumcreaturetype[k3];
	    		        if(enumcreaturetype.getCreatureClass().isAssignableFrom(EntityList.NAME_TO_CLASS.get(entry)))
	    		        {
	    		        	msg += VALUE_COLOR + " (" + enumcreaturetype.name() + ")";
	    		        }
	    		    }
	        		TerrainControl.log(LogMarker.INFO, msg.replace("§2", "").replace("§", "").replace("§a", ""));
	        		sender.addChatMessage(new TextComponentString(MESSAGE_COLOR + "- " + msg));
	        	}
	    		TerrainControl.log(LogMarker.INFO, "----");
            }
            else if(argString[0].equals("dimension") || argString[0].equals("dim"))
            {
        		boolean dimensionsEnabled = ((ForgeEngine)TerrainControl.getEngine()).getDimensionsEnabled();		

        		if(!dimensionsEnabled)
        		{
            		sender.addChatMessage(new TextComponentString(""));
	    			sender.addChatMessage(
	                        new TextComponentTranslation(ERROR_COLOR + "Dimensions are not enabled in this world's WorldConfig file."));
	    			return;
        		}
            	
        		if (CommandHelper.containsArgument(argString, "-l"))
        		{
            		sender.addChatMessage(new TextComponentString(""));
        			sender.addChatMessage(new TextComponentString("-- Dimensions --"));
        			TerrainControl.log(LogMarker.INFO, "-- Dimensions --");
        			sender.addChatMessage(new TextComponentString(""));
        			
            		int dimId = sender.getEntityWorld().provider.getDimension();
            		DimensionType dimension = DimensionManager.getProviderType(dimId);
	    			sender.addChatMessage(new TextComponentTranslation(MESSAGE_COLOR + "Currently in dimension " + VALUE_COLOR + dimension.getName() + MESSAGE_COLOR + " at id " + VALUE_COLOR + dimId + MESSAGE_COLOR + "."));
	    			TerrainControl.log(LogMarker.INFO, "Currently in dimension " + dimension.getName() + " at id " + dimId + ".");
	    			sender.addChatMessage(new TextComponentString(""));
	    			
        			for(int i = -1; i < Long.SIZE << 4; i++)
					{
						if(DimensionManager.isDimensionRegistered(i))
						{
							DimensionType dimensionType = DimensionManager.getProviderType(i);
	    		        	String msg = VALUE_COLOR + dimensionType.getName() + MESSAGE_COLOR + " at id " + VALUE_COLOR + i + (TerrainControl.getUnloadedWorld(dimensionType.getName()) == null ? MESSAGE_COLOR + " (loaded)" : VALUE_COLOR +  " (unloaded)");
	    		        	sender.addChatMessage(new TextComponentString(MESSAGE_COLOR + "- " + msg));
	    	        		TerrainControl.log(LogMarker.INFO, msg.replace("§2", "").replace("§a", "").replace("§", ""));
						}
					}
        		} else {
	            	if(argString.length < 3)
	            	{
	            		int dimId = sender.getEntityWorld().provider.getDimension();
	            		DimensionType dimension = DimensionManager.getProviderType(dimId);
	            		sender.addChatMessage(new TextComponentString(""));
		    			sender.addChatMessage(
		                        new TextComponentTranslation(MESSAGE_COLOR + "Currently in dimension " + VALUE_COLOR + dimension.getName() + MESSAGE_COLOR + " at id " + VALUE_COLOR + dimId + MESSAGE_COLOR + "."));
	            	} else {
		            	String dimName = argString[2];
		            	if(argString.length > 3)
		            	{
			            	for(int i = 3; i < argString.length; i++)
			            	{
			            		dimName += " " + argString[i];
			            	}
		            	}

						World overWorld = DimensionManager.getWorld(0);								
						String mainWorldName = overWorld.getWorldInfo().getWorldName();
						if(mainWorldName.toLowerCase().trim().equals(dimName.toLowerCase().trim()))
						{
		    				sender.addChatMessage(new TextComponentString(""));
		                    sender.addChatMessage(
		                            new TextComponentTranslation(ERROR_COLOR + "Dimension name cannot be the same as world name."));
		                    return;
						}
		            	
		            	int existingDim = 0;
						for(int i = 2; i < Long.SIZE << 4; i++)
						{
							if(DimensionManager.isDimensionRegistered(i))
							{
								DimensionType dimensionType = DimensionManager.getProviderType(i);
								
								//if(!dimensionType.getSuffix().equals("OTG") && (i == 0 || i > 1))
								//{
									//throw new NotImplementedException();
								//}
								
								if(dimensionType.getSuffix().equals("OTG") && dimensionType.getName().equals(dimName))
								{
									existingDim = i;
									if(CommandHelper.containsArgument(argString, "-c"))
									{
					    				sender.addChatMessage(new TextComponentString(""));
					                    sender.addChatMessage(
					                            new TextComponentTranslation(ERROR_COLOR + "Dimension '" + dimName + "' already exists."));
					    				return;
									}
								}							
							}
						}
		            	
		            	if (CommandHelper.containsArgument(argString, "-d"))
		            	{
		            		if(existingDim > 1)
		            		{
		            			// First make sure world is unloaded
		            			  
		            			if(!((ForgeEngine)TerrainControl.getEngine()).worldLoader.worlds.containsKey(dimName) && ((ForgeEngine)TerrainControl.getEngine()).worldLoader.unloadedWorlds.containsKey(dimName))
		            			{
				            		DimensionManager.setWorld(existingDim, null, server);
				            		DimensionManager.unregisterDimension(existingDim);
				            		
				            		ForgeWorld unloadedWorld = (ForgeWorld) TerrainControl.getUnloadedWorld(dimName);
				            		unloadedWorld.unRegisterBiomes();				            		
				            		TCDimensionManager.UnloadCustomDimensionData(existingDim);
				            		
				            		BitSet dimensionMap = null;
				            		try {
				            			Field[] fields = DimensionManager.class.getDeclaredFields();
				            			for(Field field : fields)
				            			{
				            				Class<?> fieldClass = field.getType();
				            				if(fieldClass.equals(BitSet.class))
				            				{
				            					field.setAccessible(true);
				            					dimensionMap = (BitSet) field.get(new DimensionManager());
				            			        break;
				            				}
				            			}
				            		} catch (SecurityException e) {
				            			// TODO Auto-generated catch block
				            			e.printStackTrace();
				            		} catch (IllegalArgumentException e) {
				            			// TODO Auto-generated catch block
				            			e.printStackTrace();
				            		} catch (IllegalAccessException e) {
				            			// TODO Auto-generated catch block
				            			e.printStackTrace();
				            		}
				            		
			            			dimensionMap.clear(existingDim);
				            		
			        				// This biome was unregistered via a console command, delete its world data
			        				File dimensionSaveDir = new File(world.getWorld().getSaveHandler().getWorldDirectory() + "/DIM" + existingDim);
			        				if(dimensionSaveDir.exists() && dimensionSaveDir.isDirectory())
			        				{
			        					TerrainControl.log(LogMarker.INFO, "Deleting world save data for dimension " + existingDim);
			        					try {
											FileUtils.deleteDirectory(dimensionSaveDir);								
										} catch (IOException e) {
											TerrainControl.log(LogMarker.ERROR, "Could not delete directory: " + e.toString());
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
			        				}
				            		
					    			sender.addChatMessage(
					                        new TextComponentTranslation(MESSAGE_COLOR + "Deleted dimension " + VALUE_COLOR + dimName + MESSAGE_COLOR + " at id " + VALUE_COLOR + existingDim + MESSAGE_COLOR + "."));
					    			
					    			TCDimensionManager.SaveDimensionData();
		            			} else {
		        	    			sender.addChatMessage(
		        	                        new TextComponentTranslation(ERROR_COLOR + "Cannot delete dimension " + VALUE_COLOR + dimName + ERROR_COLOR + ", it is currently loaded. Dimensions are unloaded automatically if no players are inside (this may take a minute)."));
		            			}
		            		}
		            	}
		            	else if(CommandHelper.containsArgument(argString, "-c"))
		            	{	
	        				File worldConfigFile = new File(TerrainControl.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds/" + dimName + "/WorldConfig.ini");
	        				if(!worldConfigFile.exists())
	        				{
			    				sender.addChatMessage(new TextComponentString(""));
			                    sender.addChatMessage(
			                            new TextComponentTranslation(ERROR_COLOR + "Could not create dimension, mods/OpenTerrainGenerator/worlds/" + dimName + " could not be found or does not contain a WorldConfig.ini file."));
	        				} else {
	        					
	        					sender.addChatMessage(new TextComponentString(""));
				    			sender.addChatMessage(
				                        new TextComponentTranslation(MESSAGE_COLOR + "Creating new dimension..."));
	        					
								int newDimId = TCDimensionManager.createDimension(dimName, false, true, true);
								ForgeWorld createdWorld = (ForgeWorld) TerrainControl.getWorld(dimName);								
								DimensionManager.unloadWorld(createdWorld.getWorld().provider.getDimension());
				        		
				    			sender.addChatMessage(
				                        new TextComponentTranslation(MESSAGE_COLOR + "Created dimension " + VALUE_COLOR + dimName + MESSAGE_COLOR + " at id " + VALUE_COLOR + newDimId + MESSAGE_COLOR + "."));
	        				}
		            	}
	            	}
        		}
            }
            /*
            else if (argString[0].equals("cartographer") || argString[0].equals("map"))
            {
    			if(!TerrainControl.getPluginConfig().Cartographer)
    			{
    				sender.addChatMessage(new TextComponentString(""));
                    sender.addChatMessage(
                            new TextComponentTranslation(ERROR_COLOR + "Cartographer is not enabled in OpenTerrainGenerator.ini."));
    				return;
    			}
    			if(((EntityPlayer)sender).dimension != Cartographer.CartographerDimension)
    			{
    				sender.addChatMessage(new TextComponentString(""));
                    sender.addChatMessage(
                            new TextComponentTranslation(ERROR_COLOR + "Cartographer is not available for this dimension."));
    				return;
    			}

    			BlockPos spawnPoint =  world.getSpawnPoint();
    			
            	// TP player to the center of the map
    			// TODO: Change dimension
            	ChunkCoordinate spawnChunk = ChunkCoordinate.fromBlockCoords(spawnPoint.getX(), spawnPoint.getZ());
            	int newX = spawnChunk.getBlockXCenter();
            	int newZ = spawnChunk.getBlockZCenter();
            	int newY = world.getHighestBlockYAt(newX, newZ);
            	((EntityPlayer)sender.getCommandSenderEntity()).setPositionAndUpdate(newX, newY, newZ);
            }
            */ 
            else if (argString[0].equals("biome"))
            {
            	if(!(sender.getEntityWorld().getWorldInfo().getTerrainType() instanceof TCWorldType))
    			{
    				sender.addChatMessage(new TextComponentString(""));
                    sender.addChatMessage(
                            new TextComponentTranslation(ERROR_COLOR + "Biome information is not available for this dimension."));
    				return;
    			}

                LocalBiome biome = world.getBiome(x, z);
                BiomeIds biomeIds = biome.getIds();
                sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(
                        new TextComponentTranslation(MESSAGE_COLOR + "According to the biome generator, you are in the " + VALUE_COLOR + biome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + biomeIds.getGenerationId()));

                if (CommandHelper.containsArgument(argString, "-f"))
                {
                	sender.addChatMessage(new TextComponentString(""));
                    sender.addChatMessage(
                            new TextComponentTranslation(MESSAGE_COLOR + "The base temperature of this biome is " + VALUE_COLOR + biome.getBiomeConfig().biomeTemperature + MESSAGE_COLOR + ", \n" + MESSAGE_COLOR + " at your height it is " + VALUE_COLOR + biome.getTemperatureAt(
                                    x, y, z)));
                }

                if (CommandHelper.containsArgument(argString, "-s"))
                {
                    try
                    {
                        LocalBiome savedBiome = world.getSavedBiome(x, z);
                        BiomeIds savedIds = savedBiome.getIds();
                        sender.addChatMessage(new TextComponentString(""));
                        sender.addChatMessage(
                                new TextComponentTranslation(MESSAGE_COLOR + "According to the world save files, you are in the " + VALUE_COLOR + savedBiome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + savedIds.getSavedId()));
                    } catch (BiomeNotFoundException e)
                    {
                    	sender.addChatMessage(new TextComponentString(""));
                        sender.addChatMessage(
                                new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
                    }
                }
                
                if (CommandHelper.containsArgument(argString, "-d"))
                {
                    try
                    {
                        ForgeBiome savedBiome = (ForgeBiome)world.getSavedBiome(x, z);
                        
            			Type[] types = BiomeDictionary.getTypesForBiome(savedBiome.biomeBase);
            			String typesString = "";
            			for(Type type : types)
            			{
            				if(typesString.length() == 0)
            				{
            					typesString += type.name();
            				} else {
            					typesString += ", " + type.name();
            				}
            			}
            			sender.addChatMessage(new TextComponentString(""));
                        sender.addChatMessage(
                                new TextComponentTranslation(MESSAGE_COLOR + "BiomeDict: " + VALUE_COLOR + typesString));
                    } catch (BiomeNotFoundException e)
                    {
                    	sender.addChatMessage(new TextComponentString(""));
                        sender.addChatMessage(
                                new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
                    }
                }
                
                if (CommandHelper.containsArgument(argString, "-m"))
                {
                    try
                    {
	                	ForgeBiome calculatedBiome = (ForgeBiome)world.getCalculatedBiome(x, z);
	                	
	                	sender.addChatMessage(new TextComponentString(""));
	                    sender.addChatMessage(new TextComponentTranslation("-- Biome mob spawning settings --"));
		            	for(EnumCreatureType creatureType : EnumCreatureType.values())
		            	{
			    			sender.addChatMessage(new TextComponentString(""));
		            		sender.addChatMessage(new TextComponentTranslation(MESSAGE_COLOR + creatureType.name() + ": "));
			    			ArrayList<SpawnListEntry> creatureList = (ArrayList<SpawnListEntry>)calculatedBiome.biomeBase.getSpawnableList(creatureType);
			    			if(creatureList != null && creatureList.size() > 0)
			    			{
			    				for(SpawnListEntry spawnListEntry : creatureList)
			    				{
			    					sender.addChatMessage(new TextComponentTranslation(VALUE_COLOR + "{\"mob\": \"" + spawnListEntry.entityClass + "\", \"weight\": " + spawnListEntry.itemWeight + ", \"min\": " + spawnListEntry.minGroupCount + ", \"max\": " + spawnListEntry.maxGroupCount + "}"));
			    				}
			    			}
		            	}
	                } catch (BiomeNotFoundException e)
	                {
	                	sender.addChatMessage(new TextComponentString(""));
	                    sender.addChatMessage(
	                            new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
	                }
                }

                return;
            } else
            {
            	sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(new TextComponentString("Unknown command. Type /otg for a list of commands."));
            }
        }
    }   

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return sender.canCommandSenderUseCommand(2, this.getCommandName());
    }

    @Override
    public boolean isUsernameIndex(String[] var1, int var2)
    {
        return false;
    }

    @Override
    public int compareTo(ICommand that)
    {
        return this.getCommandName().compareTo(that.getCommandName());
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        return Collections.emptyList();
    }
}
