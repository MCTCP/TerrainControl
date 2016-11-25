package com.khorn.terraincontrol.forge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

final class TXCommandHandler implements ICommand
{
    private final List<String> aliases = Arrays.asList("tc");
    private final WorldLoader worldLoader;

    TXCommandHandler(WorldLoader worldLoader)
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
        return aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] argString)
    {
        World world = sender.getEntityWorld();

        if (!world.isRemote) // Server side
        {
            if (argString == null || argString.length == 0)
            {
                sender.addChatMessage(new TextComponentString("-- TerrainControl --"));
                sender.addChatMessage(new TextComponentString("Commands:"));
                sender.addChatMessage(new TextComponentString("/tc worldinfo - Show author and description information for this world."));
                sender.addChatMessage(new TextComponentString("/tc biome - Show biome information for any biome at the player's coordinates."));
            } else if (argString[0].equals("worldinfo"))
            {
                LocalWorld localWorld = worldLoader.getWorld(sender.getEntityWorld());
                if (localWorld != null)
                {
                    WorldConfig worldConfig = localWorld.getConfigs().getWorldConfig();
                    sender.addChatMessage(new TextComponentString("-- World info --"));
                    sender.addChatMessage(new TextComponentString("Author: " + worldConfig.author));
                    sender.addChatMessage(new TextComponentString("Description: " + worldConfig.description));
                } else
                {
                    sender.addChatMessage(new TextComponentString(PluginStandardValues.PLUGIN_NAME + " is not enabled for this world."));
                }
            } else if (argString[0].equals("biome"))
            {
                Biome biome = sender.getEntityWorld().getBiomeForCoordsBody(sender.getPosition());
                sender.addChatMessage(new TextComponentString("-- Biome info --"));
                sender.addChatMessage(new TextComponentString("Name: " + biome.getBiomeName()));
                sender.addChatMessage(new TextComponentString("Id: " + Biome.getIdForBiome(biome)));
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
