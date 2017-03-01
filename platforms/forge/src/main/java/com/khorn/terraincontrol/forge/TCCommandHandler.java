package com.khorn.terraincontrol.forge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.util.CommandHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;

final class TCCommandHandler implements ICommand
{
    private final List<String> aliases = Arrays.asList("tc");
    private final WorldLoader worldLoader;
    public static final TextFormatting ERROR_COLOR = TextFormatting.RED;
    public static final TextFormatting MESSAGE_COLOR = TextFormatting.GREEN;
    public static final TextFormatting VALUE_COLOR = TextFormatting.DARK_GREEN;

    TCCommandHandler(WorldLoader worldLoader)
    {
        this.worldLoader = Preconditions.checkNotNull(worldLoader);
    }

    @Override
    public String getCommandName()
    {
        return "tc";
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return "tc";
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
            ForgeWorld world = (ForgeWorld)CommandHelper.getWorld(sender, "");

            if (world == null)
            {
            	sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(
                        new TextComponentTranslation(ERROR_COLOR + "TerrainControl is not enabled for this world."));
                return;
            }           
        	
            BlockPos pos = sender.getPosition();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            
            if (argString == null || argString.length == 0)
            {
        		sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(new TextComponentString("-- TerrainControl --"));
                sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(new TextComponentString("Commands:"));
                sender.addChatMessage(
                        new TextComponentString(MESSAGE_COLOR + "/tc worldinfo " + VALUE_COLOR + "Show author and description information for this world."));
                sender.addChatMessage(
                        new TextComponentString(MESSAGE_COLOR + "/tc biome (-f, -s, -d, -m) " + VALUE_COLOR + "Show biome information for the biome at the player's coordinates."));
                sender.addChatMessage(
                		new TextComponentString(MESSAGE_COLOR + "/tc entities " + VALUE_COLOR + "Show a list of entities that can be spawned inside BO3's using the Entity() tag."));
                sender.addChatMessage(
                		new TextComponentString(MESSAGE_COLOR + "/tc cartographer " + VALUE_COLOR + "Teleports the player to the center of the Cartographer map. Same as /tc map."));
                sender.addChatMessage(
                		new TextComponentString(MESSAGE_COLOR + "/tc cartographer -tp " + VALUE_COLOR + "Teleports the player the location they are standing on on the Cartographer map. Area must exist and have been populated. Same as /tc map -tp."));
                sender.addChatMessage(
                		new TextComponentString(MESSAGE_COLOR + "/tc pregenerator -r <radius> " + VALUE_COLOR + "Sets the pre-generation radius to <radius> chunks. Same as /tc pregen -r <radius>."));
                sender.addChatMessage(new TextComponentString(""));
                sender.addChatMessage(new TextComponentString("Tips:"));
        		sender.addChatMessage(new TextComponentString(MESSAGE_COLOR + "- Check out TerrainControl.ini for optional features."));
        		sender.addChatMessage(new TextComponentString(MESSAGE_COLOR + "- When using the pre-generator open the chat window so you can background MC without pausing the game."));
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
            	if (CommandHelper.containsArgument(argString, "-r") && argString.length > 2)
            	{
            		int radius = 0;
            		try
            		{
            			radius = Integer.parseInt(argString[2]);
            		} catch(java.lang.NumberFormatException ex)
            		{
            			sender.addChatMessage(
                                new TextComponentTranslation(ERROR_COLOR + "\"" + argString[2] + "\" could not be parsed as a number."));
            			return;
            		}
	                int newRadius = world.getConfigs().getWorldConfig().PreGenerationRadius = ((ForgeEngine)TerrainControl.getEngine()).getPregenerator().setPregenerationRadius(radius);	                
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
	        		TerrainControl.log(LogMarker.INFO, msg);
	        		sender.addChatMessage(new TextComponentString(MESSAGE_COLOR + "- " + msg));
	        	}
	    		TerrainControl.log(LogMarker.INFO, "----");
            }
            else if (argString[0].equals("cartographer") || argString[0].equals("map"))
            {
    			if(Minecraft.getMinecraft().thePlayer.dimension != 0)
    			{
    				sender.addChatMessage(new TextComponentString(""));
                    sender.addChatMessage(
                            new TextComponentTranslation(ERROR_COLOR + "Cartographer is not available for this dimension."));
    				return;
    			}
    			if(!TerrainControl.getPluginConfig().Cartographer)
    			{
    				sender.addChatMessage(new TextComponentString(""));
                    sender.addChatMessage(
                            new TextComponentTranslation(ERROR_COLOR + "Cartographer is not enabled in TerrainControl.ini."));
    				return;
    			}           
                
    			BlockPos spawnPoint =  world.getSpawnPoint();
    			
                if (CommandHelper.containsArgument(argString, "-tp"))
                {
                	// TP player to the location they are standing on on the map
                	                	
                	WorldServer worldServer = null;                	                	                	
                	Minecraft mc = Minecraft.getMinecraft();
                	if (mc.isIntegratedServerRunning())
                	{
                	    worldServer = mc.getIntegratedServer().worldServerForDimension(mc.thePlayer.dimension);
                	} else {
                	    worldServer = ((EntityPlayer)sender.getCommandSenderEntity()).getServer().getServer().worldServerForDimension(mc.thePlayer.dimension);
                	}
                	
                	ChunkCoordinate spawnChunk = ChunkCoordinate.fromBlockCoords(spawnPoint.getX(), spawnPoint.getZ());
                	int newX = spawnChunk.getBlockXCenter() + ((x - spawnChunk.getBlockXCenter()) * 16);
                	int newZ = spawnChunk.getBlockZCenter() + ((z - spawnChunk.getBlockZCenter()) * 16);
                	int newY = world.getHighestBlockYAt(newX, newZ);                	
                	ChunkCoordinate destinationChunk = ChunkCoordinate.fromBlockCoords(newX, newZ);
                	
                	// Only allow existing and populated chunks as destination	                	
                	if(
            			worldServer.getChunkProvider().chunkExists(destinationChunk.getChunkX(), destinationChunk.getChunkZ()) &&
            			worldServer.getChunkProvider().provideChunk(destinationChunk.getChunkX(), destinationChunk.getChunkZ()).isTerrainPopulated()
        			)
                	{
                		((EntityPlayer)sender.getCommandSenderEntity()).setPositionAndUpdate(newX, newY, newZ);
                	}
                } else {
                	// TP player to the center of the map
                	ChunkCoordinate spawnChunk = ChunkCoordinate.fromBlockCoords(spawnPoint.getX(), spawnPoint.getZ());
                	int newX = spawnChunk.getBlockXCenter();
                	int newZ = spawnChunk.getBlockZCenter();
                	int newY = world.getHighestBlockYAt(newX, newZ);
                	((EntityPlayer)sender.getCommandSenderEntity()).setPositionAndUpdate(newX, newY, newZ);
                }
            } 
            else if (argString[0].equals("biome"))
            {
    			if(Minecraft.getMinecraft().thePlayer.dimension != 0)
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
	                    sender.addChatMessage(new TextComponentTranslation(""));
		            	for(EnumCreatureType creatureType : EnumCreatureType.values())
		            	{
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
                sender.addChatMessage(new TextComponentString("Unknown command. Type /tc for a list of commands."));
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
