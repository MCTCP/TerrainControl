package com.pg85.otg.forge.events;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.pg85.otg.BiomeIds;
import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalMaterialData;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ServerConfigProvider;
import com.pg85.otg.configuration.WorldConfig;
import com.pg85.otg.exception.BiomeNotFoundException;
import com.pg85.otg.forge.ForgeBiome;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.ForgeWorldSession;
import com.pg85.otg.forge.OTGPlugin;
import com.pg85.otg.forge.dimensions.OTGDimensionManager;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.generator.Pregenerator;
import com.pg85.otg.forge.util.CommandHelper;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.minecraftTypes.MobNames;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class OTGCommandHandler implements ICommand
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
            ForgeWorld world = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getWorld(mcWorld);

            if (world == null && (argString.length == 0 || !((argString[0].equals("dimension") || argString[0].equals("dim")) && argString.length < 3)))
            {
            	sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "OpenTerrainGenerator is not enabled for this world."));
                return;
            }

            BlockPos pos = sender.getPosition();
            int playerX = pos.getX();
            int playerY = pos.getY();
            int playerZ = pos.getZ();

    		if(argString != null && argString.length > 0 && argString[0].equals("summon"))
			{
    			int radius = 1;
    			if(argString.length > 1)
    			{
    				radius = Integer.parseInt(argString[1]);
    			}
				if(radius > 50)
				{
					radius = 50;
					OTG.log(LogMarker.INFO, "Error in summon call: Parameter radius can be no higher than 50. Radius was set to 50.");
				}
    			argString = new String[] { "GetModData", "OTG", radius + "" };
			}

            if (argString == null || argString.length == 0)
            {
        		sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString("-- OpenTerrainGenerator --"));
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString("Commands:"));
                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg worldinfo " + VALUE_COLOR + "Show author and description information for this world."));
                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg biome <-f, -s, -d, -m> " + VALUE_COLOR + "Show biome information for the biome at the player's coordinates."));
               	sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg bo3 " + VALUE_COLOR + "Show author and description information for any structure at the player's coordinates."));

                if(isOp)
                {
                    sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg tp <biome name or id / dimension name> " + VALUE_COLOR + "Teleport to the given dimension or the nearest biome with the given name or id (max distance 16000 blocks)."));
                	sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg pregen " + VALUE_COLOR + "Shows the status of any currently active pre-generators. Same as /otg pregenerator."));
	                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg pregen <radius> " + VALUE_COLOR + "Sets the pre-generation radius for the curren world to <radius> chunks. Same as /otg pregenerator <radius>."));
	                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg pregen <radius> <dimension id> " + VALUE_COLOR + "Sets the pre-generation radius for dimension <dimension id> to <radius> chunks. Same as /otg pregenerator <radius> <dimension id>."));
                }

                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg dim " + VALUE_COLOR + "Shows the name and id of the dimension the player is currently in. Same as /otg dimension."));
                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg dim -l " + VALUE_COLOR + "Shows a list of all dimensions. Same as /otg dimension -l."));
                if(isOp)
                {
	                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg dim -c <dimension name> " + VALUE_COLOR + "Creates a dimension using world and biome configs from mods/OpenTerrainGenerator/worlds/<dimension name>. Custom dimensions can be accessed via a quartz portal. Biome names must be unique across dimensions. Same as /otg dimension -c <dimension name>"));
	                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg dim -d <dimension name> " + VALUE_COLOR + "Deletes the specified dimension. Dimension must be unloaded (dimensions unload automatically when no players are inside, this may take a minute). Same as /otg dimension -d <dimension name>"));
                }

				if(isOp)
				{
					sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg blocks " + VALUE_COLOR + "Show a list of block names that can be spawned inside BO3's with the Block() tag and used in biome- and world-configs."));
	                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg entities " + VALUE_COLOR + "Show a list of entities that can be spawned inside BO3's using the Entity() tag."));
					sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg unloadbo3s " + VALUE_COLOR + "Unloads all loaded BO2/BO3 files, use this to refresh BO2's/BO3's after editing them."));
					sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg GetModData <ModName> <Radius> " + VALUE_COLOR + "Sends any ModData() tags in BO3's within the specified <Radius> in chunks to the specified <ModName>. Some OTG mob spawning commands can be used this way. Be sure to set up ModData() tags in your BO3 to make this work."));
					sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "/otg summon <Radius> " + VALUE_COLOR + "Shorthand for /mcw GetModData OTG <Radius>. Used to summon mobs and entities that are configured to spawn inside BO3's."));
				}

                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString("Tips:"));
        		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- Check out OTG.ini and each world's WorldConfig.ini for optional features."));
        		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- When using the pre-generator in single player open the chat window so you can background MC without pausing the game."));
        		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- When using the pre-generator in single player press F3 to toggle the pre-generator HUD showing the pre-generator status."));
        		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- When using the pre-generator in multiplayer use the /otg pregen command to see the pre-generator status."));
            }
        	else if(argString[0].equals("blocks") && isOp)
        	{
	    		OTG.log(LogMarker.INFO, "-- Blocks List --");
	    		sender.sendMessage(new TextComponentString("-- Blocks List --"));

	    		Set<ResourceLocation>  as = ForgeRegistries.BLOCKS.getKeys();
	    		for(ResourceLocation blockAlias : as)
	    		{
	    			OTG.log(LogMarker.INFO, blockAlias + "");
	    			sender.sendMessage(new TextComponentString(blockAlias + ""));
	    		}

	    		OTG.log(LogMarker.INFO, "----");
        	}
        	else if(argString[0].equals("unloadbo3s") && isOp)
        	{
	    		OTG.log(LogMarker.INFO, "Unloading BO3's");
	    		OTG.getEngine().ReloadCustomObjectFiles();
	    		OTG.log(LogMarker.INFO, "BO3's unloaded");
	    		sender.sendMessage(new TextComponentString("BO3's unloaded"));
        	}
            else if (isOp && argString[0].equals("tp") && argString.length > 1)
            {
            	String biomeName = "";
            	for(int i = 1; i < argString.length; i++)
            	{
            		biomeName += argString[i] + " ";
            	}
            	if(biomeName != null && biomeName.length() > 0)
            	{
            		int biomeId = -1;
            		try
            		{
            			biomeId = Integer.parseInt(biomeName.replace(" ", ""));
            		}
            		catch(NumberFormatException ex) { }

            		// Check dimension names
        			for(int i = -1; i < Long.SIZE << 4; i++)
					{
						if(DimensionManager.isDimensionRegistered(i))
						{
							DimensionType dimensionType = DimensionManager.getProviderType(i);
							if(dimensionType.getName().toLowerCase().trim().equals(biomeName.toLowerCase().trim()))
							{
								OTGTeleporter.changeDimension(i, (EntityPlayerMP) sender.getCommandSenderEntity(), true);
								return;
							}
						}
					}

					ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords(playerX, playerZ);

            		int maxRadius = 1000;
            		for(int cycle = 1; cycle < maxRadius; cycle++)
            		{
                		for(int x1 = playerX - cycle; x1 <= playerX + cycle; x1++)
                		{
                			for(int z1 = playerZ - cycle; z1 <= playerZ + cycle; z1++)
                			{
                				if(x1 == playerX - cycle || x1 == playerX + cycle)
                				{
                					if(z1 == playerZ - cycle || z1 == playerZ + cycle)
                					{
                						ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(playerChunk.getChunkX() + (x1 - playerX), playerChunk.getChunkZ() + (z1 - playerZ));

                						ForgeBiome biome = (ForgeBiome)world.getBiome(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter());

                						if(
            								biome != null &&
            								(
        	    								(
        											biomeId == -1 &&
        											biome.getName().toLowerCase().replace(" ", "").equals(biomeName.toLowerCase().replace(" ", ""))
        										) || (
        											biomeId != -1 &&
        											biome.getIds().getGenerationId() == biomeId
        										)
        									)
        								)
                						{
                							((Entity)sender).setPositionAndUpdate(chunkCoord.getBlockXCenter(), world.getHighestBlockYAt(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter(), true, true, false, false), chunkCoord.getBlockZCenter());
                							return;
                						}
                					}
                				}
                			}
                		}
            		}
            		sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Could not find biome \"" + biomeName + "\"."));
            	}
            }
            else if (argString[0].equals("worldinfo") || argString[0].equals("world"))
            {
                WorldConfig worldConfig = world.getConfigs().getWorldConfig();
        		sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString("-- World info --"));
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Name: " + VALUE_COLOR + world.getName()));
                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Author: " + VALUE_COLOR + worldConfig.author));
                sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Description: " + VALUE_COLOR + worldConfig.description));
            }
            else if (isOp && (argString[0].equals("pregenerator") || argString[0].equals("pregen")))
            {
            	if (argString.length == 1)
            	{
        			boolean isRunningAndNotDone = false;
        			ArrayList<Pregenerator> pregenerators = new ArrayList<Pregenerator>();
        			for(LocalWorld localWorld : OTG.getAllWorlds())
        			{
        				ForgeWorld forgeWorld = (ForgeWorld)localWorld;
        				Pregenerator pregenerator = ((ForgeWorldSession)forgeWorld.GetWorldSession()).getPregenerator();
        				if(pregenerator.getPregeneratorIsRunning() && pregenerator.preGeneratorProgressStatus != "Done")
        				{
        					isRunningAndNotDone = true;
        					pregenerators.add(pregenerator);
        				}
        			}

        	    	if(isRunningAndNotDone)
        	    	{
        		        for(Pregenerator pregenerator : pregenerators)
        		        {
        		        	sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Generating \"" + VALUE_COLOR + pregenerator.pregenerationWorld + "\" " + MESSAGE_COLOR + (pregenerator.progressScreenWorldSizeInBlocks > 0 ? "(" + pregenerator.progressScreenWorldSizeInBlocks + "x" + pregenerator.progressScreenWorldSizeInBlocks  + " blocks)" : "")));
        		        	sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Progress: " + VALUE_COLOR + pregenerator.preGeneratorProgress + "%"));
        		        	sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Chunks: " + VALUE_COLOR + pregenerator.preGeneratorProgressStatus));
        		        	sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Elapsed: " + VALUE_COLOR + pregenerator.progressScreenElapsedTime));
        		        	sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Estimated: " + VALUE_COLOR + pregenerator.progressScreenEstimatedTime));
        		        	sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "---"));
        		        }

        		        long i = Runtime.getRuntime().maxMemory();
        		        long j = Runtime.getRuntime().totalMemory();
        		        long k = Runtime.getRuntime().freeMemory();
        		        long l = j - k;
        		        sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Memory: " + VALUE_COLOR + Long.valueOf(BytesToMb(l)) + "/" +  Long.valueOf(BytesToMb(i)) + " MB"));
        	    	} else {
        	    		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "No pre-generator is currently running."));
        	    	}
            	}
            	if (argString.length > 1)
            	{
            		int radius = 0;
            		try
            		{
            			radius = Integer.parseInt(argString[1]);
            		} catch(java.lang.NumberFormatException ex)
            		{
            			sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "\"" + argString[1] + "\" could not be parsed as a number."));
            			return;
            		}

            		int dimensionId = 0;
                	if (argString.length > 2)
                	{
                		try
                		{
                			dimensionId = Integer.parseInt(argString[2]);
                		}
                		catch(java.lang.NumberFormatException ex)
                		{
                			sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "\"" + argString[1] + "\" could not be parsed as a number."));
                			return;
                		}
                	}

                	ForgeWorld targetWorld = world;

                	if(!DimensionManager.isDimensionRegistered(dimensionId))
                	{
            			sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Dimension with id \"" + dimensionId + "\" does not exist."));
            			return;
                	} else {
                		targetWorld = ((ForgeEngine)OTG.getEngine()).getWorldByDimId(dimensionId);
                		if(targetWorld == null)
                		{
                			DimensionManager.initDimension(dimensionId);
                			targetWorld = ((ForgeEngine)OTG.getEngine()).getWorldByDimId(dimensionId);
                		}
                		if(targetWorld == null)
                		{
                			sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Could not find OTG world for dimension with id \"" + dimensionId + "\"."));
                			return;
                		}
                	}

                	targetWorld.GetWorldSession().setPregenerationRadius(radius);
	                int newRadius = targetWorld.getConfigs().getWorldConfig().PreGenerationRadius = targetWorld.GetWorldSession().getPregenerationRadius();

	                ((ServerConfigProvider)targetWorld.getConfigs()).saveWorldConfig();

        			sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Pre-generator radius set to " + VALUE_COLOR + newRadius + MESSAGE_COLOR + "."));
        			return;
            	}
            }
            else if (argString[0].equals("entities"))
            {
        		sender.sendMessage(new TextComponentString(""));
	    		OTG.log(LogMarker.INFO, "-- Entities List --");
	    		sender.sendMessage(new TextComponentString("-- Entities List --"));
	    		sender.sendMessage(new TextComponentString(""));
	    		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "Some of these, like ThrownPotion, FallingSand, Mob and Painting may crash the game so be sure to test your BO3 in single player."));
    			sender.sendMessage(new TextComponentString(""));
    			EnumCreatureType[] aenumcreaturetype = EnumCreatureType.values();
	    		for(ResourceLocation entry : EntityList.getEntityNameList())
	        	{
    		        if(EntityList.getClass(entry) != null)
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
		        		OTG.log(LogMarker.INFO, msg.replace("§2", "").replace("§", "").replace("§a", ""));
		        		sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- " + msg));
    		        } else {
    		        	// This can happen for LIGHTNING_BOLT since it appears to be added to the
    		        	// getEntityNameList list but doesn't actually have an entity registered
    		        	// TODO: Find out how lightning bolt is supposed to work and make sure
    		        	// all other entities are registered properly (including ones added by other mods).
    		        }
	        	}
	    		OTG.log(LogMarker.INFO, "----");
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

							LocalWorld forgeWorld = null;
							if(i == 0)
							{
								forgeWorld = (ForgeWorld)((ForgeEngine)OTG.getEngine()).getOverWorld();
							} else {
								forgeWorld = OTG.getWorld(dimensionType.getName());
								if(forgeWorld == null)
								{
									forgeWorld = OTG.getUnloadedWorld(dimensionType.getName());
								}
							}

							String materials = "";
							if(
								forgeWorld != null &&
								forgeWorld.getConfigs().getWorldConfig().DimensionPortalMaterials != null &&
								forgeWorld.getConfigs().getWorldConfig().DimensionPortalMaterials.size() > 0
							)
							{
								materials += MESSAGE_COLOR + " (";
								for(LocalMaterialData material : forgeWorld.getConfigs().getWorldConfig().DimensionPortalMaterials)
								{
									materials += VALUE_COLOR + material.toString() + MESSAGE_COLOR + ", ";
								}
								materials = materials.substring(0, materials.length() - 2);
								materials += MESSAGE_COLOR + ")";
							}

							String msg = VALUE_COLOR + dimensionType.getName() + MESSAGE_COLOR + " at id " + VALUE_COLOR + i + ((OTG.getUnloadedWorld(dimensionType.getName()) == null ? MESSAGE_COLOR + " (loaded)" : VALUE_COLOR +  " (unloaded)") + materials);
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

							if(dimensionType.getSuffix() != null && dimensionType.getSuffix().equals("OTG") && dimensionType.getName().equals(dimName))
							{
								existingDim = i;
								if(CommandHelper.containsArgument(argString, "-c"))
								{
				    				sender.sendMessage(new TextComponentString(""));
				                    sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Dimension '" + dimName + "' already exists."));
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

	            			if(((ForgeEngine)OTG.getEngine()).getWorldLoader().isWorldUnloaded(dimName))
	            			{
	            				ForgeWorld forgeWorld = (ForgeWorld) ((ForgeEngine)OTG.getEngine()).getUnloadedWorld(dimName);
	            				OTGDimensionManager.DeleteDimension(existingDim, forgeWorld, server, true);

				    			sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Deleted dimension " + VALUE_COLOR + dimName + MESSAGE_COLOR + " at id " + VALUE_COLOR + existingDim + MESSAGE_COLOR + "."));

				    			PlayerTracker.SendAllWorldAndBiomeConfigsToAllPlayers(sender.getServer());

	            			} else {
	        	    			sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Cannot delete dimension " + VALUE_COLOR + dimName + ERROR_COLOR + ", it is currently loaded. Dimensions are unloaded automatically if no players are inside (this may take a minute)."));
	            			}
	            		}
	            	}
	            	else if(CommandHelper.containsArgument(argString, "-c"))
	            	{
        				File worldConfigFile = new File(OTG.getEngine().getTCDataFolder().getAbsolutePath() + "/worlds/" + dimName + "/WorldConfig.ini");
        				if(!worldConfigFile.exists())
        				{
		    				sender.sendMessage(new TextComponentString(""));
		                    sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Could not create dimension, mods/OpenTerrainGenerator/worlds/" + dimName + " could not be found or does not contain a WorldConfig.ini file."));
        				} else {

        					sender.sendMessage(new TextComponentString(""));
			    			sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Creating new dimension..."));

							int newDimId = OTGDimensionManager.createDimension(dimName, false, true, true);
							ForgeWorld createdWorld = (ForgeWorld) OTG.getWorld(dimName);
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
    			if(!OTG.getPluginConfig().Cartographer)
    			{
    				sender.addChatMessage(new TextComponentString(""));
                    sender.addChatMessage(
                            new TextComponentTranslation(ERROR_COLOR + "Cartographer is not enabled in OTG.ini."));
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
            	if(
                	(
            			sender.getEntityWorld().getWorldInfo() instanceof DerivedWorldInfo &&
            			!((DerivedWorldInfo)sender.getEntityWorld().getWorldInfo()).delegate.getGeneratorOptions().equals("OpenTerrainGenerator")
        			) ||
                	(
            			!(sender.getEntityWorld().getWorldInfo() instanceof DerivedWorldInfo) &&
            			!sender.getEntityWorld().getWorldInfo().getGeneratorOptions().equals("OpenTerrainGenerator")
    				)
    			)
    			{
    				sender.sendMessage(new TextComponentString(""));
                    sender.sendMessage(new TextComponentTranslation(ERROR_COLOR + "Biome information is not available for this dimension."));
    				return;
    			}

                LocalBiome biome = world.getBiome(playerX, playerZ);
                BiomeIds biomeIds = biome.getIds();
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "According to the biome generator, you are in the " + VALUE_COLOR + biome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + biomeIds.getGenerationId()));

                if (CommandHelper.containsArgument(argString, "-f"))
                {
                	sender.sendMessage(new TextComponentString(""));
                    sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "The base temperature of this biome is " + VALUE_COLOR + biome.getBiomeConfig().biomeTemperature + MESSAGE_COLOR + ", \n" + MESSAGE_COLOR + " at your height it is " + VALUE_COLOR + biome.getTemperatureAt(playerX, playerY, playerZ)));
                }

                if (CommandHelper.containsArgument(argString, "-s"))
                {
                    try
                    {
                        LocalBiome savedBiome = world.getSavedBiome(playerX, playerZ);
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
                        ForgeBiome savedBiome = (ForgeBiome)world.getSavedBiome(playerX, playerZ);

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
	                	ForgeBiome calculatedBiome = (ForgeBiome)world.getCalculatedBiome(playerX, playerZ);

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
            }
        	else if(argString[0].toLowerCase().trim().equals("bo3") || argString[0].toLowerCase().trim().equals("bo3info"))
        	{
    			String structureInfo = world.GetWorldSession().GetStructureInfoAt(sender.getPosition().getX(),sender.getPosition().getZ());

    			if(structureInfo.length() > 0)
    			{
	    			for(String messagePart : structureInfo.split("\r\n"))
	    			{
	    				sender.sendMessage(new TextComponentTranslation(messagePart));
	    			}
        		} else {
        			sender.sendMessage(new TextComponentTranslation("There's nothing here."));
        		}
        	}
        	else if(argString[0].equals("GetModData") && argString.length > 1)
        	{
        		if (!(sender instanceof EntityPlayer) || isOp) // If the request was made by a player then check if the player is opped
        		{
	        		if(argString.length == 2)
	        		{
	            		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(playerX, playerZ);
	        			FMLInterModComms.sendRuntimeMessage(OTGPlugin.instance, argString[1], "GetModData", sender.getEntityWorld().getWorldInfo().getWorldName() + "," + chunkCoord.getChunkX() + "," + chunkCoord.getChunkZ());
	        		}
	        		else if(argString.length == 3)
	        		{
	        			try
	        			{
	        				ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(playerX, playerZ);
	        				int radius = Integer.parseInt(argString[2]);
	        				if(radius > 50)
	        				{
	        					radius = 50;
	        					OTG.log(LogMarker.INFO, "Error in GetModData call: Parameter radius can be no higher than 50. Radius was set to 50.");
	        				}
	        				for(int x = -radius; x <= radius; x++)
	        				{
	        					for(int z = -radius; z <= radius; z++)
	        					{
	        		        		ChunkCoordinate chunkCoord2 = ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + x, chunkCoord.getChunkZ() + z);
	    		        			FMLInterModComms.sendRuntimeMessage(OTGPlugin.instance, argString[1], "GetModData", sender.getEntityWorld().getWorldInfo().getWorldName() + "," + chunkCoord2.getChunkX() + "," + chunkCoord2.getChunkZ());
	        					}
	        				}
	        			}
	        			catch(NumberFormatException ex)
	        			{
	        				OTG.log(LogMarker.INFO, "Error in GetModData call: value \"" + argString[2] + "\" was expected to be a number");
	        			}
	        		}
        		}
        	} else {
            	sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(new TextComponentString("Unknown command. Type /otg for a list of commands."));
            }
        }
    }

    private static long BytesToMb(long bytes)
    {
        return bytes / 1024L / 1024L;
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
