package com.pg85.otg.forge.commands;

import java.util.List;
import java.util.Map.Entry;

import com.pg85.otg.common.LocalWorld;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BiomesCommand extends BaseCommand
{
    BiomesCommand()
    {
        name = "biomes";
        usage = "biomes";
        description = "View a list of registerd biomes.";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        LocalWorld world = this.getWorld(sender, "");
        if (world == null)
        {
            sender.sendMessage(new TextComponentString(""));
            sender.sendMessage(
                    new TextComponentTranslation(ERROR_COLOR + "This command is only available for OpenTerrainGenerator worlds."));
            return true;
        }

        sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "ForgeRegistries.BIOMES contains:"));
        sender.sendMessage(new TextComponentTranslation(""));
        for (Entry<ResourceLocation, Biome> entry : ForgeRegistries.BIOMES.getEntries())
        {
            sender.sendMessage(
                    new TextComponentTranslation(VALUE_COLOR + entry.getKey().toString() + MESSAGE_COLOR + " : " + VALUE_COLOR + entry.getValue().toString()));
        }

        sender.sendMessage(new TextComponentTranslation(""));
        sender.sendMessage(new TextComponentTranslation(MESSAGE_COLOR + "Biome.REGISTRY.registryObjects contains:"));
        sender.sendMessage(new TextComponentTranslation(""));
        for (Entry<ResourceLocation, Biome> entry : Biome.REGISTRY.registryObjects.entrySet())
        {
            sender.sendMessage(
                    new TextComponentTranslation(VALUE_COLOR + entry.getKey().toString() + MESSAGE_COLOR + " : " + VALUE_COLOR + entry.getValue().toString()));
        }

        sender.sendMessage(new TextComponentTranslation(""));
        sender.sendMessage(
                new TextComponentTranslation(MESSAGE_COLOR + "Biome.REGISTRY.inverseRegistryObjects contains:"));
        sender.sendMessage(new TextComponentTranslation(""));
        for (Entry<Biome, ResourceLocation> entry : Biome.REGISTRY.inverseObjectRegistry.entrySet())
        {
            sender.sendMessage(
                    new TextComponentTranslation(VALUE_COLOR + entry.getKey().toString() + MESSAGE_COLOR + " : " + VALUE_COLOR + entry.getValue().toString()));
        }
        return true;
    }
}