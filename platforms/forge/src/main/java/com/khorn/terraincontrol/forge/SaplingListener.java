package com.khorn.terraincontrol.forge;

import cpw.mods.fml.common.eventhandler.Event.Result;

import net.minecraft.init.Blocks;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import com.khorn.terraincontrol.generator.resourcegens.SaplingType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;

import java.util.Random;

public class SaplingListener
{
    @SubscribeEvent
    public void onSaplingGrow(SaplingGrowTreeEvent event)
    {
        int x = event.x;
        int y = event.y;
        int z = event.z;
        World world = event.world;
        LocalWorld localWorld = WorldHelper.toLocalWorld(world);

        if (localWorld == null)
        {
            // World not managed by Terrain Control
            return;
        }

        Block block = world.func_147439_a(x, y, z); // world.getBlock
        BlockSapling saplingBlock = (BlockSapling) Blocks.sapling;

        if (block != saplingBlock)
        {
            // Only vanilla saplings
            return;
        }

        int blockData = world.getBlockMetadata(x, y, z) & 3;

        SaplingType treeToGrow = null;

        boolean hugeJungleTreeHasGrown = false;
        int jungleOffsetX = 0;
        int jungleOffsetZ = 0;

        // Get the sapling type
        if (blockData == 1)
        {
            // Redwood trees
            treeToGrow = SaplingType.Redwood;
        } else if (blockData == 2)
        {
            // Birch trees
            treeToGrow = SaplingType.Birch;
        } else if (blockData == 3)
        {
            // Jungle trees
            for (jungleOffsetX = 0; jungleOffsetX >= -1; --jungleOffsetX)
            {
                for (jungleOffsetZ = 0; jungleOffsetZ >= -1; --jungleOffsetZ)
                {
                    // if (saplingBlock.isSameSapling(..) ..
                    if (saplingBlock.func_149880_a(world, x + jungleOffsetX, y, z + jungleOffsetZ, 3) && saplingBlock.func_149880_a(world, x + jungleOffsetX + 1, y, z + jungleOffsetZ, 3) && saplingBlock.func_149880_a(world, x + jungleOffsetX, y, z + jungleOffsetZ + 1, 3) && saplingBlock.func_149880_a(world, x + jungleOffsetX + 1, y, z + jungleOffsetZ + 1, 3))
                    {
                        treeToGrow = SaplingType.BigJungle;
                        hugeJungleTreeHasGrown = true;
                        break;
                    }
                }

                if (treeToGrow != null)
                {
                    break;
                }
            }

            if (treeToGrow == null)
            {
                jungleOffsetZ = 0;
                jungleOffsetX = 0;
                treeToGrow = SaplingType.SmallJungle;
            }
        } else
        {
            // Normal trees
            treeToGrow = SaplingType.Oak;
        }

        // Get the sapling generator
        SaplingGen saplingGen = this.getSaplingGen(localWorld, treeToGrow, x, z);
        if (saplingGen == null)
        {
            // No sapling generator set for this sapling
            return;
        }

        // When we have reached this point, we know that we have to handle the
        // event ourselves
        // So cancel it
        event.setResult(Result.DENY);

        // Remove saplings
        if (hugeJungleTreeHasGrown)
        {
            // world.setBlock(...)
            world.func_147444_c(x + jungleOffsetX, y, z + jungleOffsetZ, Blocks.air);
            world.func_147444_c(x + jungleOffsetX + 1, y, z + jungleOffsetZ, Blocks.air);
            world.func_147444_c(x + jungleOffsetX, y, z + jungleOffsetZ + 1, Blocks.air);
            world.func_147444_c(x + jungleOffsetX + 1, y, z + jungleOffsetZ + 1, Blocks.air);
        } else
        {
            world.func_147444_c(x, y, z, Blocks.air);
        }

        // Try ten times to grow sapling
        boolean saplingGrown = false;
        for (int i = 0; i < 10; i++)
        {
            if (saplingGen.growSapling(localWorld, new Random(), x + jungleOffsetX, y, z + jungleOffsetZ))
            {
                saplingGrown = true;
                break;
            }
        }

        if (!saplingGrown)
        {
            // Restore sapling
            if (hugeJungleTreeHasGrown)
            {
                // world.setBlock(...)
                world.func_147446_b(x + jungleOffsetX, y, z + jungleOffsetZ, block, blockData, 4);
                world.func_147446_b(x + jungleOffsetX + 1, y, z + jungleOffsetZ, block, blockData, 4);
                world.func_147446_b(x + jungleOffsetX, y, z + jungleOffsetZ + 1, block, blockData, 4);
                world.func_147446_b(x + jungleOffsetX + 1, y, z + jungleOffsetZ + 1, block, blockData, 4);
            } else
            {
                world.func_147446_b(x, y, z, block, blockData, 4);
            }
        }

    }

    @SubscribeEvent
    public void onBonemealUse(BonemealEvent event)
    {
        LocalWorld localWorld = WorldHelper.toLocalWorld(event.world);
        if (localWorld == null)
        {
            // World not managerd by Terrain Control
            return;
        }

        // Get sapling gen
        SaplingGen gen = null;
        if (event.block == Blocks.red_mushroom_block)
        {
            gen = getSaplingGen(localWorld, SaplingType.RedMushroom, event.x, event.z);
        } else if (event.block == Blocks.brown_mushroom_block)
        {
            gen = getSaplingGen(localWorld, SaplingType.BrownMushroom, event.x, event.z);
        }
        if (gen == null)
        {
            // No sapling gen specified for this type
            return;
        }

        // Generate mushroom
        event.setResult(Result.ALLOW);
        // event.world.setBlock
        event.world.func_147444_c(event.x, event.y, event.z, Blocks.air);

        boolean mushroomGrown = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (gen.growSapling(localWorld, random, event.x, event.y, event.z))
            {
                mushroomGrown = true;
                break;
            }
        }
        if (!mushroomGrown)
        {
            // Restore mushroom
            // event.world.setBlock(...)
            event.world.func_147446_b(event.x, event.y, event.z, event.block, 0, 2);
        }
    }

    // Can return null
    public SaplingGen getSaplingGen(LocalWorld world, SaplingType type, int x, int z)
    {
        BiomeConfig biomeConfig = world.getSettings().biomeConfigs[world.getBiomeId(x, z)];
        if (biomeConfig == null)
        {
            return null;
        }
        return biomeConfig.getSaplingGen(type);
    }
}
