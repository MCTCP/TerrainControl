package com.khorn.terraincontrol.forge.events;

import java.util.Random;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.WorldLoader;
import com.khorn.terraincontrol.generator.resource.SaplingGen;
import com.khorn.terraincontrol.generator.resource.SaplingType;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SaplingListener
{
    private static class SaplingGrower
    {
        private final LocalWorld world;
        private final LocalMaterialData material;
        private final SaplingType saplingType;

        private BlockPos blockPos;

        private SaplingGrower(LocalWorld world, BlockPos blockPos)
        {
            this.world = world;
            this.material = world.getMaterial(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            this.blockPos = blockPos;

            // Check whether block is a sapling
            if (!this.material.isMaterial(DefaultMaterial.SAPLING))
            {
                this.saplingType = null;
                return;
            }

            // Try to find big (2x2) sapling
            SaplingType bigSaplingType = getBigSaplingType(this.material.getBlockData());
            if (bigSaplingType != null)
            {
                if (findFourSaplings())
                {
                    this.saplingType = bigSaplingType;
                    return;
                }
            }

            // Try to find small sapling
            this.saplingType = getSmallSaplingType(this.material.getBlockData());
        }

        /**
         * Gets whether the saplings are placed in a 2x2 pattern. If successful,
         * it adjusts {@link SaplingGrower#blockPos} to represent the top left
         * sapling (with the lowest x and z).
         *
         * @return Whether the saplings are placed in a 2x2 pattern.
         */
        private boolean findFourSaplings()
        {
            int x = this.blockPos.getX();
            int y = this.blockPos.getY();
            int z = this.blockPos.getZ();
            for (int treeOffsetX = 0; treeOffsetX >= -1; --treeOffsetX)
            {
                for (int treeOffsetZ = 0; treeOffsetZ >= -1; --treeOffsetZ)
                {
                    if (isSameSapling(this.material, this.world.getMaterial(x + treeOffsetX, y, z + treeOffsetZ))
                            && isSameSapling(this.material, this.world.getMaterial(x + treeOffsetX + 1, y, z + treeOffsetZ))
                            && isSameSapling(this.material, this.world.getMaterial(x + treeOffsetX, y, z + treeOffsetZ + 1))
                            && isSameSapling(this.material, this.world.getMaterial(x + treeOffsetX + 1, y, z + treeOffsetZ + 1)))
                    {
                        // Found! Adjust internal position
                        this.blockPos = this.blockPos.add(treeOffsetX, 0, treeOffsetZ);
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Checks if the sapling types are the same, ignoring growth stages.
         * @param sapling1 The first material to compare.
         * @param sapling2 The second material to compare.
         * @return True if both materials are saplings and are of the same type.
         */
        private boolean isSameSapling(LocalMaterialData sapling1, LocalMaterialData sapling2)
        {
            return sapling1.isMaterial(DefaultMaterial.SAPLING) 
                    && sapling2.isMaterial(DefaultMaterial.SAPLING) 
                    && sapling1.getBlockData() % 8 == sapling2.getBlockData() % 8;
        }

        /**
         * Gets the sapling type, based on the assumption that the sapling is
         * not placed in a 2x2 pattern.
         * 
         * @param data
         *            The block data of the sapling block.
         * @return The sapling type, or null if not found.
         */
        private SaplingType getSmallSaplingType(int data)
        {
            switch (data % 8)
            { // % 8 makes it ignore growth stage
                case 0:
                    return SaplingType.Oak;
                case 1:
                    return SaplingType.Redwood;
                case 2:
                    return SaplingType.Birch;
                case 3:
                    return SaplingType.SmallJungle;
                case 4:
                    return SaplingType.Acacia;
            }
            return null;
        }

        /**
         * Gets the sapling type, based on the assumption that the saplings must
         * be placed in a 2x2 pattern. Will never return one of the smaller
         * sapling types.
         * 
         * @param data
         *            The block data of the sapling block.
         * @return The sapling type, or null if not found.
         */
        private SaplingType getBigSaplingType(int data)
        {
            switch (data % 8)
            { // % 8 makes it ignore growth stage
                case 1:
                    return SaplingType.HugeRedwood;
                case 3:
                    return SaplingType.BigJungle;
                case 5:
                    return SaplingType.DarkOak;
            }
            return null;
        }
    }

    private final WorldLoader worldLoader;

    public SaplingListener(WorldLoader worldLoader)
    {
        this.worldLoader = Preconditions.checkNotNull(worldLoader);
    }

    @SubscribeEvent
    public void onSaplingGrow(SaplingGrowTreeEvent event)
    {
        World world = event.getWorld();
        LocalWorld localWorld = this.worldLoader.getWorld(world);
        BlockPos blockPos = event.getPos();

        if (localWorld == null)
        {
            // World not managed by Terrain Control
            return;
        }

        SaplingGrower saplingGrower = new SaplingGrower(localWorld, blockPos);

        if (saplingGrower.saplingType == null)
        {
            // Unsupported sapling
            return;
        }

        // Get the sapling generator
        SaplingGen saplingGen = this.getSaplingGen(localWorld, saplingGrower.saplingType, saplingGrower.blockPos);
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
        IBlockState air = Blocks.AIR.getDefaultState();
        boolean wideTrunk = saplingGrower.saplingType.requiresFourSaplings();
        if (wideTrunk)
        {
            world.setBlockState(saplingGrower.blockPos, air);
            world.setBlockState(saplingGrower.blockPos.add(1, 0, 0), air);
            world.setBlockState(saplingGrower.blockPos.add(0, 0, 1), air);
            world.setBlockState(saplingGrower.blockPos.add(1, 0, 1), air);
        } else
        {
            world.setBlockState(saplingGrower.blockPos, air);
        }

        // Try ten times to grow sapling
        boolean saplingGrown = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (saplingGen.growSapling(localWorld, random, wideTrunk, saplingGrower.blockPos.getX(),
                    saplingGrower.blockPos.getY(), saplingGrower.blockPos.getZ()))
            {
                saplingGrown = true;
                break;
            }
        }

        if (!saplingGrown)
        {
            // Restore sapling
            int saplingX = saplingGrower.blockPos.getX();
            int saplingY = saplingGrower.blockPos.getY();
            int saplingZ = saplingGrower.blockPos.getZ();
            if (saplingGrower.saplingType.requiresFourSaplings())
            {
                localWorld.setBlock(saplingX, saplingY, saplingZ, saplingGrower.material);
                localWorld.setBlock(saplingX + 1, saplingY, saplingZ, saplingGrower.material);
                localWorld.setBlock(saplingX, saplingY, saplingZ + 1, saplingGrower.material);
                localWorld.setBlock(saplingX + 1, saplingY, saplingZ + 1, saplingGrower.material);
            } else
            {
                localWorld.setBlock(saplingX, saplingY, saplingZ, saplingGrower.material);
            }
        }

    }

    @SubscribeEvent
    public void onBonemealUse(BonemealEvent event)
    {
        LocalWorld localWorld = this.worldLoader.getWorld(event.getWorld());
        if (localWorld == null)
        {
            // World not managed by Terrain Control
            return;
        }

        // Get sapling gen
        SaplingGen gen = null;
        if (event.getBlock() == Blocks.RED_MUSHROOM_BLOCK)
        {
            gen = getSaplingGen(localWorld, SaplingType.RedMushroom, event.getPos());
        } else if (event.getBlock() == Blocks.BROWN_MUSHROOM_BLOCK)
        {
            gen = getSaplingGen(localWorld, SaplingType.BrownMushroom, event.getPos());
        }
        if (gen == null)
        {
            // No sapling gen specified for this type
            return;
        }

        // Generate mushroom
        event.setResult(Result.ALLOW);
        event.getWorld().setBlockState(event.getPos(), Blocks.AIR.getDefaultState());

        boolean mushroomGrown = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (gen.growSapling(localWorld, random, false, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ()))
            {
                mushroomGrown = true;
                break;
            }
        }
        if (!mushroomGrown)
        {
            // Restore mushroom
            event.getWorld().setBlockState(event.getPos(), event.getBlock());
        }
    }

    // Can return null
    private SaplingGen getSaplingGen(LocalWorld world, SaplingType type, BlockPos blockPos)
    {
        try
        {
            LocalBiome biome = world.getSavedBiome(blockPos.getX(), blockPos.getZ());
            return biome.getBiomeConfig().getSaplingGen(type);
        } catch (BiomeNotFoundException e)
        {
            return null;
        }
    }
}
