package com.pg85.otg.forge.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.world.ForgeWorld;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

public class TPCommand extends BaseCommand
{
    TPCommand()
    {
        name = "tp";
        usage = "tp <biome/dimension name or id>";
        description = "Attempt to teleport to the target biome or dimension (Max range: 1000 chunks).";
    }

    @Override
    public boolean onCommand(ICommandSender sender, List<String> args)
    {
        if (args.isEmpty())
		{
            return false;
		}

        BlockPos pos = sender.getPosition();
        int playerX = pos.getX();
        int playerZ = pos.getZ();

        LocalWorld world = this.getWorld(sender, "");

        String biomeOrDimensionName = "";
        for (int i = 0; i < args.size(); i++)
        {
            biomeOrDimensionName += args.get(i) + " ";
        }
        if (biomeOrDimensionName != null && biomeOrDimensionName.trim().length() > 0)
        {
            biomeOrDimensionName = biomeOrDimensionName.trim();
            sender.sendMessage(
                    new TextComponentTranslation(MESSAGE_COLOR + "Searching for destination biome or dimension \"" + VALUE_COLOR + biomeOrDimensionName + MESSAGE_COLOR + "\"."));

            int biomeId = -1;
            try {
                biomeId = Integer.parseInt(biomeOrDimensionName.replace(" ", ""));
            } catch (NumberFormatException ex) {
                // Do nothing
            }

            // Check dimension names
            for (int i = -1; i < Long.SIZE << 4; i++)
            {
                if (DimensionManager.isDimensionRegistered(i))
                {
                    DimensionType dimensionType = DimensionManager.getProviderType(i);
                    if (dimensionType.getName().toLowerCase().trim().equals(biomeOrDimensionName.toLowerCase()))
                    {
                        OTGTeleporter.changeDimension(i, (EntityPlayerMP) sender.getCommandSenderEntity(), false, true);
                        return true;
                    }
                }
            }

            if (world != null)
            {
                Biome targetMCBiome = null;
                if (biomeId == -1)
                {
                    ForgeBiome targetBiome = (ForgeBiome) world.getBiomeByNameOrNull(biomeOrDimensionName);
                    if (targetBiome != null)
                    {
                    	targetMCBiome = targetBiome.biomeBase;
                    }
                } else {
                	ForgeBiome targetBiome = (ForgeBiome) world.getBiomeByOTGIdOrNull(biomeId);
                	if(targetBiome != null)
                	{
                		targetMCBiome = targetBiome.biomeBase;
                	}
                }

                if(targetMCBiome != null)
                {
	                ArrayList<Biome> biomes = new ArrayList<Biome>();
	                biomes.add(targetMCBiome);
	                BlockPos biomePos = ((ForgeWorld)world).getWorld().getBiomeProvider().findBiomePosition(playerX, playerZ, 8000, biomes, new Random());
	                if(biomePos != null)
	                {
		                sender.sendMessage(
		                        new TextComponentTranslation(MESSAGE_COLOR + "Teleporting to \"" + VALUE_COLOR + biomeOrDimensionName + MESSAGE_COLOR + "\"."));
		                ((Entity) sender).setPositionAndUpdate(biomePos.getX(), world.getHighestBlockYAt(biomePos.getX(), biomePos.getZ(), true, true, false, false, false, null), biomePos.getZ());
	                	return true;
	                }
                }
            }
            sender.sendMessage(
                    new TextComponentTranslation(ERROR_COLOR + "Could not find biome \"" + biomeOrDimensionName + "\"."));
            return true;
        }
        return false;
    }
}