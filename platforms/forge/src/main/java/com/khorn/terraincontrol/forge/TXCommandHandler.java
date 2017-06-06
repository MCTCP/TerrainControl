package com.khorn.terraincontrol.forge;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.events.PlayerTracker;
import com.khorn.terraincontrol.forge.util.CommandHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.MobNames;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
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

final class TXCommandHandler implements ICommand
{
    private final List<String> aliases = Arrays.asList("otg");
    public static final TextFormatting ERROR_COLOR = TextFormatting.RED;
    public static final TextFormatting MESSAGE_COLOR = TextFormatting.GREEN;
    public static final TextFormatting VALUE_COLOR = TextFormatting.DARK_GREEN;

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] argString)
    {
        World mcWorld = sender.getEntityWorld();

		boolean isOp = sender.canUseCommand(2, this.getName());
        
        if (!mcWorld.isRemote) // Server side
        {
            ForgeWorld world = (ForgeWorld) ((ForgeEngine)TerrainControl.getEngine()).getWorld(mcWorld);
                        
            if (world == null && !((argString[0].equals("dimension") || argString[0].equals("dim")) && argString.length < 3))
            {
            	sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(
                        new TextComponentTranslation(ERROR_COLOR + "OpenTerrainGenerator is not enabled for this world."));
                return;
            }
        	
            BlockPos pos = sender.getPosition();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            
            if (argString == null || argString.length == 0)
            {
        		sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString("-- OpenTerrainGenerator --"));
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString("Commands:"));
                sender.sendMessage(
                        new TextComponentString(MESSAGE_COLOR + "/otg worldinfo " + VALUE_COLOR + "Show author and description information for this world."));
                sender.sendMessage(
                        new TextComponentString(MESSAGE_COLOR + "/otg biome (-f, -s, -d, -m) " + VALUE_COLOR + "Show biome information for the biome at the player's coordinates."));
                sender.sendMessage(
                		new TextComponentString(MESSAGE_COLOR + "/otg entities " + VALUE_COLOR + "Show a list of entities that can be spawned inside BO3's using the Entity() tag."));
                if(isOp)
                {                
	                sender.sendMessage(
	                		new TextComponentString(MESSAGE_COLOR + "/otg pregen <radius> " + VALUE_COLOR + "Sets the pre-generation radius to <radius> chunks. Same as /otg pregenerator <radius>."));
                }
                sender.sendMessage(
                		new TextComponentString(MESSAGE_COLOR + "/otg dim " + VALUE_COLOR + "Shows the name and id of the dimension the player is currently in. Same as /otg dimension."));
                sender.sendMessage(
                		new TextComponentString(MESSAGE_COLOR + "/otg dim -l " + VALUE_COLOR + "Shows a list of all dimensions. Same as /otg dimension -l."));
                if(isOp)
                {                
	                sender.sendMessage(
	                		new TextComponentString(MESSAGE_COLOR + "/otg dim -c <dimension name> " + VALUE_COLOR + "Creates a dimension using world and biome configs from mods/OpenTerrainGenerator/worlds/<dimension name>. Custom dimensions can be accessed via a quartz portal. Biome names must be unique across dimensions. Same as /otg dimension -c <dimension name>"));
	                sender.sendMessage(
	                		new TextComponentString(MESSAGE_COLOR + "/otg dim -d <dimension name> " + VALUE_COLOR + "Deletes the specified dimension. Dimension must be unloaded (dimensions unload automatically when no players are inside, this may take a minute). Same as /otg dimension -d <dimension name>"));
                }
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString("Tips:"));
        		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- Check out OpenTerrainGenerator.ini and each world's WorldConfig.ini for optional features."));
        		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- When using the pre-generator in single player open the chat window so you can background MC without pausing the game."));
            }
            else if (argString[0].equals("worldinfo") || argString[0].equals("world"))
            {
                WorldConfig worldConfig = world.getConfigs().getWorldConfig();
        		sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString("-- World info --"));
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Author: " + VALUE_COLOR + worldConfig.author));
                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Description: " + VALUE_COLOR + worldConfig.description));
            }
            else if (isOp && (argString[0].equals("pregenerator") || argString[0].equals("pregen")))
            {
            	if (argString.length > 1)
            	{
            		int radius = 0;
            		try
            		{
            			radius = Integer.parseInt(argString[1]);
            		} catch(java.lang.NumberFormatException ex)
            		{
            			sender.sendMessage(
                                new TextComponentTranslation(ERROR_COLOR + "\"" + argString[1] + "\" could not be parsed as a number."));
            			return;
            		}
            		
            		world = (ForgeWorld) ((ForgeEngine)TerrainControl.getEngine()).getWorld(DimensionManager.getWorld(0));
            		
	                int newRadius = world.getConfigs().getWorldConfig().PreGenerationRadius = ((ForgeEngine)TerrainControl.getEngine()).getPregenerator().setPregenerationRadius(radius, world.getWorld());
	                	                
	                ((ServerConfigProvider)world.getConfigs()).saveWorldConfig();
	                
        			sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Pre-generator radius set to " + VALUE_COLOR + newRadius + MESSAGE_COLOR + "."));
        			return;
            	}
            }
            else if (argString[0].equals("entities"))
            {                
        		sender.sendMessage(new TextComponentString(""));
	    		TerrainControl.log(LogMarker.INFO, "-- Entities List --");
	    		sender.sendMessage(new TextComponentString("-- Entities List --"));
	    		sender.sendMessage(new TextComponentString(""));
	    		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Some of these, like ThrownPotion, FallingSand, Mob and Painting may crash the game so be sure to test your BO3 in single player."));
    			sender.sendMessage(new TextComponentString(""));
    			EnumCreatureType[] aenumcreaturetype = EnumCreatureType.values();
	    		for(ResourceLocation entry : EntityList.getEntityNameList())
	        	{
	    			String msg = entry.getResourcePath();
	    		    for (int k3 = 0; k3 < aenumcreaturetype.length; ++k3)
	    		    {
	    		        EnumCreatureType enumcreaturetype = aenumcreaturetype[k3];
	    		        if(enumcreaturetype.getCreatureClass().isAssignableFrom(EntityList.getClass(entry)))
	    		        {
	    		        	msg += VALUE_COLOR + " (" + enumcreaturetype.name() + ")";
	    		        }
	    		    }
	        		TerrainControl.log(LogMarker.INFO, msg.replace("§2", "").replace("§", "").replace("§a", ""));
	        		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- " + msg));
	        	}
	    		TerrainControl.log(LogMarker.INFO, "----");
            }
            else if(argString[0].equals("dimension") || argString[0].equals("dim"))
            {      
            	if(argString.length < 2)
            	{
            		int dimId = sender.getEntityWorld().provider.getDimension();
            		DimensionType dimension = DimensionManager.getProviderType(dimId);
            		sender.sendMessage(new TextComponentString(""));
	    			sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Currently in dimension " + VALUE_COLOR + dimension.getName() + MESSAGE_COLOR + " at id " + VALUE_COLOR + dimId + MESSAGE_COLOR + "."));
            	}    	
            	else if (CommandHelper.containsArgument(argString, "-l"))
        		{
            		sender.sendMessage(new TextComponentString(""));
        			sender.sendMessage(new TextComponentString("-- Dimensions --"));
        			sender.sendMessage(new TextComponentString(""));
        			
            		int dimId = sender.getEntityWorld().provider.getDimension();
            		DimensionType dimension = DimensionManager.getProviderType(dimId);
	    			sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Currently in dimension " + VALUE_COLOR + dimension.getName() + MESSAGE_COLOR + " at id " + VALUE_COLOR + dimId + MESSAGE_COLOR + "."));
	    			sender.sendMessage(new TextComponentString(""));
	    			
        			for(int i = -1; i < Long.SIZE << 4; i++)
					{
						if(DimensionManager.isDimensionRegistered(i))
						{
							DimensionType dimensionType = DimensionManager.getProviderType(i);
	    		        	String msg = VALUE_COLOR + dimensionType.getName() + MESSAGE_COLOR + " at id " + VALUE_COLOR + i + (TerrainControl.getUnloadedWorld(dimensionType.getName()) == null ? MESSAGE_COLOR + " (loaded)" : VALUE_COLOR +  " (unloaded)");
	    		        	sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- " + msg));
						}
					}
        		}
            	else if(isOp && argString.length > 2)
            	{
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
	    				sender.sendMessage(new TextComponentString(""));
	                    sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Dimension name cannot be the same as world name."));
	                    return;
					}
	            	
	            	int existingDim = 0;
					for(int i = 2; i < Long.SIZE << 4; i++)
					{
						if(DimensionManager.isDimensionRegistered(i))
						{
							DimensionType dimensionType = DimensionManager.getProviderType(i);
							
							if(dimensionType.getSuffix().equals("OTG") && dimensionType.getName().equals(dimName))
							{
								existingDim = i;
								if(CommandHelper.containsArgument(argString, "-c"))
								{
				    				sender.sendMessage(new TextComponentString(""));
				                    sender.sendMessage(
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
	            			  
	            			if(((ForgeEngine)TerrainControl.getEngine()).worldLoader.isWorldUnloaded(dimName))
	            			{
	            				ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)TerrainControl.getEngine()).getUnloadedWorld(dimName);		            				
	            				TXDimensionManager.DeleteDimension(existingDim, forgeWorld, server, true);			        			
	            				
				    			sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Deleted dimension " + VALUE_COLOR + dimName + MESSAGE_COLOR + " at id " + VALUE_COLOR + existingDim + MESSAGE_COLOR + "."));					    		
				    			
				    			PlayerTracker.SendAllWorldAndBiomeConfigsToAllPlayers(sender.getServer());
				    			
	            			} else {
	        	    			sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Cannot delete dimension " + VALUE_COLOR + dimName + ERROR_COLOR + ", it is currently loaded. Dimensions are unloaded automatically if no players are inside (this may take a minute)."));
	            			}
	            		}
	            	}
	            	else if(CommandHelper.containsArgument(argString, "-c"))
	            	{	
        				File worldConfigFile = new File(TerrainControl.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds/" + dimName + "/WorldConfig.ini");
        				if(!worldConfigFile.exists())
        				{
		    				sender.sendMessage(new TextComponentString(""));
		                    sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Could not create dimension, mods/OpenTerrainGenerator/worlds/" + dimName + " could not be found or does not contain a WorldConfig.ini file."));
        				} else {
        					
        					sender.sendMessage(new TextComponentString(""));
			    			sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Creating new dimension..."));
        					
							int newDimId = TXDimensionManager.createDimension(dimName, false, true, true);
							ForgeWorld createdWorld = (ForgeWorld) TerrainControl.getWorld(dimName);								
							DimensionManager.unloadWorld(createdWorld.getWorld().provider.getDimension());
			        		
			    			sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Created dimension " + VALUE_COLOR + dimName + MESSAGE_COLOR + " at id " + VALUE_COLOR + newDimId + MESSAGE_COLOR + "."));
			    			
			    			PlayerTracker.SendAllWorldAndBiomeConfigsToAllPlayers(sender.getServer());
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
            	if(!(sender.getEntityWorld().getWorldInfo().getTerrainType() instanceof TXWorldType))
    			{
    				sender.sendMessage(new TextComponentString(""));
                    sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Biome information is not available for this dimension."));
    				return;
    			}

                LocalBiome biome = world.getBiome(x, z);
                BiomeIds biomeIds = biome.getIds();
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "According to the biome generator, you are in the " + VALUE_COLOR + biome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + biomeIds.getGenerationId()));

                if (CommandHelper.containsArgument(argString, "-f"))
                {
                	sender.sendMessage(new TextComponentString(""));
                    sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "The base temperature of this biome is " + VALUE_COLOR + biome.getBiomeConfig().biomeTemperature + MESSAGE_COLOR + ", \n" + MESSAGE_COLOR + " at your height it is " + VALUE_COLOR + biome.getTemperatureAt(x, y, z)));
                }

                if (CommandHelper.containsArgument(argString, "-s"))
                {
                    try
                    {
                        LocalBiome savedBiome = world.getSavedBiome(x, z);
                        BiomeIds savedIds = savedBiome.getIds();
                        sender.sendMessage(new TextComponentString(""));
                        sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "According to the world save files, you are in the " + VALUE_COLOR + savedBiome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + savedIds.getSavedId()));
                    } catch (BiomeNotFoundException e)
                    {
                    	sender.sendMessage(new TextComponentString(""));
                        sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
                    }
                }
                
                if (CommandHelper.containsArgument(argString, "-d"))
                {                	
                    try
                    {
                        ForgeBiome savedBiome = (ForgeBiome)world.getSavedBiome(x, z);
                        
            			Set<Type> types = BiomeDictionary.getTypes(savedBiome.biomeBase);
            			String typesString = "";
            			for(Type type : types)
            			{
            				if(typesString.length() == 0)
            				{
            					typesString += type.getName();
            				} else {
            					typesString += ", " + type.getName();
            				}
            			}
            			sender.sendMessage(new TextComponentString(""));
                        sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "BiomeDict: " + VALUE_COLOR + typesString));
                    } catch (BiomeNotFoundException e)
                    {
                    	sender.sendMessage(new TextComponentString(""));
                        sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
                    }
                }
                
                if (CommandHelper.containsArgument(argString, "-m"))
                {
                    try
                    {
	                	ForgeBiome calculatedBiome = (ForgeBiome)world.getCalculatedBiome(x, z);
	                	
	                	sender.sendMessage(new TextComponentString(""));
	                    sender.sendMessage(new TextComponentTranslation("-- Biome mob spawning settings --"));
		            	for(EnumCreatureType creatureType : EnumCreatureType.values())
		            	{
			    			sender.sendMessage(new TextComponentString(""));
		            		sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + creatureType.name() + ": "));
			    			ArrayList<SpawnListEntry> creatureList = (ArrayList<SpawnListEntry>)calculatedBiome.biomeBase.getSpawnableList(creatureType);
			    			if(creatureList != null && creatureList.size() > 0)
			    			{
			    				for(SpawnListEntry spawnListEntry : creatureList)
			    				{
			    					sender.sendMessage(new TextComponentTranslation(VALUE_COLOR + "{\"mob\": \"" + MobNames.toInternalName(spawnListEntry.entityClass.getSimpleName()) + "\", \"weight\": " + spawnListEntry.itemWeight + ", \"min\": " + spawnListEntry.minGroupCount + ", \"max\": " + spawnListEntry.maxGroupCount + "}"));
			    				}
			    			}
		            	}
	                }
                    catch (BiomeNotFoundException e)
	                {
	                	sender.sendMessage(new TextComponentString(""));
	                    sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
	                }
                }

                return;
            } else {
            	sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString("Unknown command. Type /otg for a list of commands."));
            }
        }
    }   

    @Override
    public boolean isUsernameIndex(String[] var1, int var2)
    {
        return false;
    }

	@Override
	public String getName() {
		return "otg";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "otg";
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server,
			ICommandSender sender, String[] args, BlockPos targetPos) {
		return null;
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
}
