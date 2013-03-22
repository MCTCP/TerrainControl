package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import com.khorn.terraincontrol.generator.resourcegens.SaplingType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.world.World;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;

import java.util.Random;

public class SaplingListener
{
    @ForgeSubscribe
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

        int blockId = world.getBlockId(x, y, z);
        BlockSapling saplingBlock = (BlockSapling) Block.sapling;

        if (blockId != saplingBlock.blockID)
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
                    if (saplingBlock.isSameSapling(world, x + jungleOffsetX, y, z + jungleOffsetZ, 3) && saplingBlock.isSameSapling(world, x + jungleOffsetX + 1, y, z + jungleOffsetZ, 3) && saplingBlock.isSameSapling(world, x + jungleOffsetX, y, z + jungleOffsetZ + 1, 3) && saplingBlock.isSameSapling(world, x + jungleOffsetX + 1, y, z + jungleOffsetZ + 1, 3))
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
            world.setBlockToAir(x + jungleOffsetX, y, z + jungleOffsetZ);
            world.setBlockToAir(x + jungleOffsetX + 1, y, z + jungleOffsetZ);
            world.setBlockToAir(x + jungleOffsetX, y, z + jungleOffsetZ + 1);
            world.setBlockToAir(x + jungleOffsetX + 1, y, z + jungleOffsetZ + 1);
        } else
        {
            world.setBlockToAir(x, y, z);
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
                world.setBlock(x + jungleOffsetX, y, z + jungleOffsetZ, blockId, blockData, 4);
                world.setBlock(x + jungleOffsetX + 1, y, z + jungleOffsetZ, blockId, blockData, 4);
                world.setBlock(x + jungleOffsetX, y, z + jungleOffsetZ + 1, blockId, blockData, 4);
                world.setBlock(x + jungleOffsetX + 1, y, z + jungleOffsetZ + 1, blockId, blockData, 4);
            } else
            {
                world.setBlock(x, y, z, blockId, blockData, 4);
            }
        }

    }

    @ForgeSubscribe
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
        if (event.ID == Block.mushroomRed.blockID)
        {
            gen = getSaplingGen(localWorld, SaplingType.RedMushroom, event.X, event.Z);
        } else if (event.ID == Block.mushroomBrown.blockID)
        {
            gen = getSaplingGen(localWorld, SaplingType.BrownMushroom, event.X, event.Z);
        }
        if (gen == null)
        {
            // No sapling gen specified for this type
            return;
        }

        // Generate mushroom
        event.setResult(Result.ALLOW);
        event.world.setBlockToAir(event.X, event.Y, event.Z);

        boolean mushroomGrown = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (gen.growSapling(localWorld, random, event.X, event.Y, event.Z))
            {
                mushroomGrown = true;
                break;
            }
        }
        if (!mushroomGrown)
        {
            // Restore mushroom
            event.world.setBlock(event.X, event.Y, event.Z, event.ID, 0, 2);
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
