package com.pg85.otg.forge.commands;

import java.util.List;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.util.ChunkCoordinate;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
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

            ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords(playerX, playerZ);
            if (world != null)
            {
            	// TODO: Use BiomeProvider.findBiomePosition instead?
                int maxRadius = 1000;

                if (biomeId == -1)
                {
                    ForgeBiome targetBiome = (ForgeBiome) world.getBiomeByNameOrNull(biomeOrDimensionName);
                    if (targetBiome != null)
                    {
                        biomeId = targetBiome.getIds().getOTGBiomeId();
                    }
                }

                if (biomeId != -1)
                {
                    for (int cycle = 1; cycle < maxRadius; cycle++)
                    {
                        for (int x1 = playerX - cycle; x1 <= playerX + cycle; x1++)
                        {
                            if (x1 == playerX - cycle || x1 == playerX + cycle)
                            {
                                for (int z1 = playerZ - cycle; z1 <= playerZ + cycle; z1++)
                                {
                                    if (z1 == playerZ - cycle || z1 == playerZ + cycle)
                                    {
                                        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(
                                                playerChunk.getChunkX() + (x1 - playerX),
                                                playerChunk.getChunkZ() + (z1 - playerZ));

                                        ForgeBiome biome = (ForgeBiome) world.getBiome(chunkCoord.getBlockXCenter(),
                                                chunkCoord.getBlockZCenter());

                                        if (biome != null && biome.getIds().getOTGBiomeId() == biomeId)
                                        {
                                            sender.sendMessage(
                                                    new TextComponentTranslation(MESSAGE_COLOR + "Teleporting to \"" + VALUE_COLOR + biomeOrDimensionName + MESSAGE_COLOR + "\"."));
                                            ((Entity) sender).setPositionAndUpdate(chunkCoord.getBlockXCenter(),
                                                    world.getHighestBlockYAt(chunkCoord.getBlockXCenter(),
                                                            chunkCoord.getBlockZCenter(), true, true, false, false),
                                                    chunkCoord.getBlockZCenter());
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
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