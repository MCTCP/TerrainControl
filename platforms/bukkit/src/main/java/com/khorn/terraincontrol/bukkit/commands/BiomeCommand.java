package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TXPlugin;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BiomeCommand extends BaseCommand
{
    public BiomeCommand(TXPlugin _plugin)
    {
        super(_plugin);
        name = "biome";
        perm = TCPerm.CMD_BIOME.node;
        usage = "biome [-f] [-s]";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Location location = this.getLocation(sender);
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        LocalWorld world = this.getWorld(sender, "");
        
        if (world == null)
        {
            sender.sendMessage(ERROR_COLOR + "Plugin is not enabled for this world.");
            return true;
        }

        LocalBiome biome = world.getBiome(x, z);
        BiomeIds biomeIds = biome.getIds();

        sender.sendMessage(MESSAGE_COLOR + "According to the biome generator, you are in the " + VALUE_COLOR + biome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR
                + biomeIds.getGenerationId());

        if (args.contains("-f"))
        {
            sender.sendMessage(MESSAGE_COLOR + "The base temperature of this biome is " + VALUE_COLOR + biome.getBiomeConfig().biomeTemperature + MESSAGE_COLOR + ", \nat your height it is " + VALUE_COLOR
                    + biome.getTemperatureAt(x, y, z));
        }

        if (args.contains("-s"))
        {
            try
            {
                LocalBiome savedBiome = world.getSavedBiome(x, z);
                BiomeIds savedIds = savedBiome.getIds();
                sender.sendMessage(MESSAGE_COLOR + "According to the world save files, you are in the " + VALUE_COLOR
                        + savedBiome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR
                        + savedIds.getSavedId());
            } catch (BiomeNotFoundException e)
            {
                sender.sendMessage(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here.");
            }
        }

        return true;
    }
}