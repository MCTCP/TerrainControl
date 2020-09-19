package com.pg85.otg.bukkit.commands;

import com.pg85.otg.bukkit.OTGPerm;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.bukkit.biomes.BukkitBiome;
import com.pg85.otg.bukkit.util.MobSpawnGroupHelper;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.exception.BiomeNotFoundException;

import net.minecraft.server.v1_12_R1.BiomeBase.BiomeMeta;
import net.minecraft.server.v1_12_R1.EnumCreatureType;
import com.pg85.otg.util.BiomeIds;
import com.pg85.otg.util.minecraft.defaults.EntityNames;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class BiomeCommand extends BaseCommand
{
    BiomeCommand(OTGPlugin _plugin)
    {
        super(_plugin);
        name = "biome";
        perm = OTGPerm.CMD_BIOME.node;
        usage = "biome [-f] [-s] [-m]";
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
                + biomeIds.getOTGBiomeId());

        if (args.contains("-f"))
        {
            sender.sendMessage(MESSAGE_COLOR + "The base temperature of this biome is " + VALUE_COLOR + biome.getBiomeConfig().biomeTemperature + MESSAGE_COLOR + ", \nat your height it is " + VALUE_COLOR
                    + biome.getTemperatureAt(x, y, z));
        }
        
        if (args.contains("-m"))
        {
            try
            {
                BukkitBiome calculatedBiome = (BukkitBiome) world.getCalculatedBiome(x, z);

                sender.sendMessage("");
                sender.sendMessage(MESSAGE_COLOR + "-- Biome mob spawning settings --");
                for (EnumCreatureType creatureType : EnumCreatureType.values())
                {
                    sender.sendMessage("");
                    sender.sendMessage(MESSAGE_COLOR + creatureType.name() + ": ");
                    ArrayList<BiomeMeta> creatureList = (ArrayList<BiomeMeta>) calculatedBiome.getHandle().getMobs(creatureType);
                    if (creatureList != null && creatureList.size() > 0)
                    {
                        for (BiomeMeta spawnListEntry : creatureList)
                        {
                            sender.sendMessage(
                        		VALUE_COLOR + "{\"mob\": \"" + EntityNames.toInternalName(spawnListEntry.b.getSimpleName()) + 
                        		"\", \"weight\": " + MobSpawnGroupHelper.getWeight(spawnListEntry) + 
                        		", \"min\": " + spawnListEntry.c + 
                        		", \"max\": " + spawnListEntry.d + "}"
                    		);
                        }
                    }
                }
            }
            catch (BiomeNotFoundException e)
            {
                sender.sendMessage("");
                sender.sendMessage(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here.");
            }
        }
        
        if (args.contains("-s"))
        {
            try
            {
                String savedBiomeName = world.getSavedBiomeName(x, z);
                sender.sendMessage(MESSAGE_COLOR + "According to the world save files, you are in the " + VALUE_COLOR
                        + savedBiomeName + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR
                        + biomeIds.getSavedId());
            } catch (BiomeNotFoundException e)
            {
                sender.sendMessage(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here.");
            }
        }

        return true;
    }
}