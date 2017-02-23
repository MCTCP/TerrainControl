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
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
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
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

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
            if (argString == null || argString.length == 0)
            {
                sender.addChatMessage(new TextComponentString("-- TerrainControl --"));
                sender.addChatMessage(new TextComponentString("Commands:"));
                sender.addChatMessage(
                        new TextComponentString("/tc worldinfo - Show author and description information for this world."));
                sender.addChatMessage(
                        new TextComponentString("/tc biome (-f, -s, -d, -m) - Show biome information for any biome at the player's coordinates."));
                sender.addChatMessage(
                		new TextComponentString("/tc entities - Show a list of entities that can be spawned inside BO3's using the Entity() tag."));
            } else if (argString[0].equals("worldinfo") || argString[0].equals("world"))
            {
                LocalWorld localWorld = this.worldLoader.getWorld(sender.getEntityWorld());
                if (localWorld != null)
                {
                    WorldConfig worldConfig = localWorld.getConfigs().getWorldConfig();
                    sender.addChatMessage(new TextComponentString("-- World info --"));
                    sender.addChatMessage(new TextComponentString("Author: " + worldConfig.author));
                    sender.addChatMessage(new TextComponentString("Description: " + worldConfig.description));
                } else
                {
                    sender.addChatMessage(
                            new TextComponentString(PluginStandardValues.PLUGIN_NAME + " is not enabled for this world."));
                }
                
            } else if (argString[0].equals("entities"))
            {                
	    		TerrainControl.log(LogMarker.INFO, "-- Entities List --");
	    		sender.addChatMessage(new TextComponentString("-- Entities List --"));
	    		sender.addChatMessage(new TextComponentString("Some of these, like ThrownPotion, FallingSand, Mob and Painting may crash the game so be sure to test your BO3 in single player."));
    			EnumCreatureType[] aenumcreaturetype = EnumCreatureType.values();
	    		for(String entry : EntityList.NAME_TO_CLASS.keySet())
	        	{
	    			String msg = entry;
	    		    for (int k3 = 0; k3 < aenumcreaturetype.length; ++k3)
	    		    {
	    		        EnumCreatureType enumcreaturetype = aenumcreaturetype[k3];
	    		        if(enumcreaturetype.getCreatureClass().isAssignableFrom(EntityList.NAME_TO_CLASS.get(entry)))
	    		        {
	    		        	msg += " (" + enumcreaturetype.name() + ")";
	    		        }
	    		    }

	        		TerrainControl.log(LogMarker.INFO, msg);
	        		sender.addChatMessage(new TextComponentString("- " + msg));
	        	}
	    		TerrainControl.log(LogMarker.INFO, "----");
	    		
            } else if (argString[0].equals("biome"))
            {
                BlockPos pos = sender.getPosition();
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();

                LocalWorld world = CommandHelper.getWorld(sender, "");

                if (world == null)
                {
                    sender.addChatMessage(
                            new TextComponentTranslation(ERROR_COLOR + "TerrainControl is not enabled for this world."));
                    return;
                }

                LocalBiome biome = world.getBiome(x, z);
                BiomeIds biomeIds = biome.getIds();
                sender.addChatMessage(
                        new TextComponentTranslation(MESSAGE_COLOR + "According to the biome generator, you are in the " + VALUE_COLOR + biome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + biomeIds.getGenerationId()));

                if (CommandHelper.containsArgument(argString, "-f"))
                {
                    sender.addChatMessage(
                            new TextComponentTranslation(MESSAGE_COLOR + "The base temperature of this biome is " + VALUE_COLOR + biome.getBiomeConfig().biomeTemperature + MESSAGE_COLOR + ", \nat your height it is " + VALUE_COLOR + biome.getTemperatureAt(
                                    x, y, z)));
                }

                if (CommandHelper.containsArgument(argString, "-s"))
                {
                    try
                    {
                        LocalBiome savedBiome = world.getSavedBiome(x, z);
                        BiomeIds savedIds = savedBiome.getIds();
                        sender.addChatMessage(
                                new TextComponentTranslation(MESSAGE_COLOR + "According to the world save files, you are in the " + VALUE_COLOR + savedBiome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + savedIds.getSavedId()));
                    } catch (BiomeNotFoundException e)
                    {
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
                        sender.addChatMessage(
                                new TextComponentTranslation(MESSAGE_COLOR + "BiomeDict: " + VALUE_COLOR + typesString));
                    } catch (BiomeNotFoundException e)
                    {
                        sender.addChatMessage(
                                new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
                    }
                }
                
                if (CommandHelper.containsArgument(argString, "-m"))
                {
                    try
                    {
	                	ForgeBiome calculatedBiome = (ForgeBiome)world.getCalculatedBiome(x, z);
	                	
	                    sender.addChatMessage(new TextComponentTranslation(MESSAGE_COLOR + "-- Biome mob spawning settings --"));
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
	                    sender.addChatMessage(
	                            new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
	                }
                }

                return;
            } else
            {
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
