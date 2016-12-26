package com.khorn.terraincontrol.forge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.util.CommandHelper;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

final class TXCommandHandler implements ICommand
{
    private final List<String> aliases = Arrays.asList("tc");
    private final WorldLoader worldLoader;
    public static final TextFormatting ERROR_COLOR = TextFormatting.RED;
    public static final TextFormatting MESSAGE_COLOR = TextFormatting.GREEN;
    public static final TextFormatting VALUE_COLOR = TextFormatting.DARK_GREEN;

    TXCommandHandler(WorldLoader worldLoader)
    {
        this.worldLoader = Preconditions.checkNotNull(worldLoader);
    }

    @Override
    public String getName()
    {
        return "tc";
    }

    @Override
    public String getUsage(ICommandSender var1)
    {
        return "tc";
    }

    @Override
    public List<String> getAliases()
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
                sender.sendMessage(new TextComponentString("-- TerrainControl --"));
                sender.sendMessage(new TextComponentString("Commands:"));
                sender.sendMessage(new TextComponentString("/tc worldinfo - Show author and description information for this world."));
                sender.sendMessage(new TextComponentString("/tc biome - Show biome information for any biome at the player's coordinates."));
            } else if (argString[0].equals("worldinfo"))
            {
                LocalWorld localWorld = this.worldLoader.getWorld(sender.getEntityWorld());
                if (localWorld != null)
                {
                    WorldConfig worldConfig = localWorld.getConfigs().getWorldConfig();
                    sender.sendMessage(new TextComponentString("-- World info --"));
                    sender.sendMessage(new TextComponentString("Author: " + worldConfig.author));
                    sender.sendMessage(new TextComponentString("Description: " + worldConfig.description));
                } else
                {
                    sender.sendMessage(new TextComponentString(PluginStandardValues.PLUGIN_NAME + " is not enabled for this world."));
                }
            } else if (argString[0].equals("biome"))
            {
                BlockPos pos = sender.getPosition();
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();

                LocalWorld world = CommandHelper.getWorld(sender, "");

                if (world == null)
                {
                    sender.sendMessage(
                            new TextComponentTranslation(ERROR_COLOR + "TerrainControl is not enabled for this world."));
                    return;
                }

                LocalBiome biome = world.getBiome(x, z);
                BiomeIds biomeIds = biome.getIds();
                sender.sendMessage(
                        new TextComponentTranslation(MESSAGE_COLOR + "According to the biome generator, you are in the " + VALUE_COLOR + biome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + biomeIds.getGenerationId()));

                if (CommandHelper.containsArgument(argString, "-f"))
                {
                    sender.sendMessage(
                            new TextComponentTranslation(MESSAGE_COLOR + "The base temperature of this biome is " + VALUE_COLOR + biome.getBiomeConfig().biomeTemperature + MESSAGE_COLOR + ", \nat your height it is " + VALUE_COLOR + biome.getTemperatureAt(
                                    x, y, z)));
                }

                if (CommandHelper.containsArgument(argString, "-s"))
                {
                    try
                    {
                        LocalBiome savedBiome = world.getSavedBiome(x, z);
                        BiomeIds savedIds = savedBiome.getIds();
                        sender.sendMessage(
                                new TextComponentTranslation(MESSAGE_COLOR + "According to the world save files, you are in the " + VALUE_COLOR + savedBiome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + savedIds.getSavedId()));
                    } catch (BiomeNotFoundException e)
                    {
                        sender.sendMessage(
                                new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
                    }
                }

                return;
            } else
            {
                sender.sendMessage(new TextComponentString("Unknown command. Type /tc for a list of commands."));
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return sender.canUseCommand(2, this.getName());
    }

    @Override
    public boolean isUsernameIndex(String[] var1, int var2)
    {
        return false;
    }

    @Override
    public int compareTo(ICommand that)
    {
        return this.getName().compareTo(that.getName());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        return Collections.emptyList();
    }
}
