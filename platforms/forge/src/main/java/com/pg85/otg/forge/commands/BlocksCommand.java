package com.pg85.otg.forge.commands;

import java.util.List;
import java.util.Set;

import com.pg85.otg.OTG;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BlocksCommand extends BaseCommand
{
    BlocksCommand()
    {
        name = "blocks";
        usage = "blocks";
        description = "View a list of block names that can be spawned inside objects with the Block() tag and used in biome- and world-configs.";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        OTG.log(LogMarker.INFO, "-- Blocks List --");
        sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "-- Blocks List --"));

        Set<ResourceLocation> as = ForgeRegistries.BLOCKS.getKeys();
        for (ResourceLocation blockAlias : as)
        {
            OTG.log(LogMarker.INFO, blockAlias + "");
            sender.sendMessage(new TextComponentString(MESSAGE_COLOR + "- " + blockAlias + ""));
        }

        OTG.log(LogMarker.INFO, "----");
        return true;
    }
}