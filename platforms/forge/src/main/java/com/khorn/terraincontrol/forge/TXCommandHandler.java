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
        World world = sender.getEntityWorld();

        if (!world.isRemote) // Server side
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
                Biome biome = sender.getEntityWorld().getBiomeForCoordsBody(sender.getPosition());
                sender.sendMessage(new TextComponentString("-- Biome info --"));
                sender.sendMessage(new TextComponentString("Name: " + biome.getBiomeName()));
                sender.sendMessage(new TextComponentString("Id: " + Biome.getIdForBiome(biome)));
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
