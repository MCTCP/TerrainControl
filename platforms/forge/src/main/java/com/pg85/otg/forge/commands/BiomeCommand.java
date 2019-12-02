package com.pg85.otg.forge.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.pg85.otg.common.BiomeIds;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.exception.BiomeNotFoundException;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.util.minecraft.defaults.MobNames;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class BiomeCommand extends BaseCommand
{
    BiomeCommand()
    {

        name = "biome";
        usage = "biome [-f] [-s] [-d] [-m]";
        description = "View information about your current biome.";
        needsOp = false;
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        BlockPos location = this.getLocation(sender);
        int x = location.getX();
        int y = location.getY();
        int z = location.getZ();

        LocalWorld world = this.getWorld(sender, "");

        if (world == null)
        {
            sender.sendMessage(new TextComponentString(ERROR_COLOR + "OTG is not enabled for this world."));
            return true;
        }

        LocalBiome biome = world.getBiome(x, z);
        BiomeIds biomeIds = biome.getIds();

        sender.sendMessage(
                new TextComponentString(MESSAGE_COLOR + "According to the biome generator, you are in the " + VALUE_COLOR + biome.getName() + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + biomeIds.getOTGBiomeId()));

        if (args.contains("-f"))
        {
            sender.sendMessage(new TextComponentString(""));
            sender.sendMessage(
                    new TextComponentString(MESSAGE_COLOR + "The base temperature of this biome is " + VALUE_COLOR + biome.getBiomeConfig().biomeTemperature + MESSAGE_COLOR + ", at your height it is " + VALUE_COLOR + biome.getTemperatureAt(
                            x, y, z)));
        }

        if (args.contains("-s"))
        {
            try
            {
                String savedBiomeName = world.getSavedBiomeName(x, y);
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(
                        new TextComponentTranslation(MESSAGE_COLOR + "According to the world save files, you are in the " + VALUE_COLOR + savedBiomeName + MESSAGE_COLOR + " biome, with id " + VALUE_COLOR + biome.getIds().getSavedId()));
            } catch (BiomeNotFoundException e)
            {
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(
                        new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
            }
        }

        if (args.contains("-d"))
        {
            try
            {
                ForgeBiome forgeBiome = (ForgeBiome) world.getBiome(x, z);

                Set<Type> types = BiomeDictionary.getTypes(forgeBiome.biomeBase);
                String typesString = "";
                for (Type type : types)
                {
                    if (typesString.length() == 0)
                    {
                        typesString += type.getName();
                    } else
                    {
                        typesString += ", " + type.getName();
                    }
                }
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(
                        new TextComponentTranslation(MESSAGE_COLOR + "BiomeDict: " + VALUE_COLOR + typesString));
            } catch (BiomeNotFoundException e)
            {
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(
                        new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
            }
        }

        if (args.contains("-m"))
        {
            try
            {
                ForgeBiome calculatedBiome = (ForgeBiome) world.getCalculatedBiome(x, z);

                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(
                        new TextComponentTranslation(TextFormatting.AQUA + "-- Biome mob spawning settings --"));
                for (EnumCreatureType creatureType : EnumCreatureType.values())
                {
                    sender.sendMessage(new TextComponentString(""));
                    sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + creatureType.name() + ": "));
                    ArrayList<SpawnListEntry> creatureList = (ArrayList<SpawnListEntry>) calculatedBiome.biomeBase.getSpawnableList(
                            creatureType);
                    if (creatureList != null && creatureList.size() > 0)
                    {
                        for (SpawnListEntry spawnListEntry : creatureList)
                        {
                            sender.sendMessage(
                                    new TextComponentTranslation(VALUE_COLOR + "{\"mob\": \"" + MobNames.toInternalName(
                                            spawnListEntry.entityClass.getSimpleName()) + "\", \"weight\": " + spawnListEntry.itemWeight + ", \"min\": " + spawnListEntry.minGroupCount + ", \"max\": " + spawnListEntry.maxGroupCount + "}"));
                        }
                    }
                }
            } catch (BiomeNotFoundException e)
            {
                sender.sendMessage(new TextComponentString(""));
                sender.sendMessage(
                        new TextComponentTranslation(ERROR_COLOR + "An unknown biome (" + e.getBiomeName() + ") was saved to the save files here."));
            }
        }
        return true;
    }
}