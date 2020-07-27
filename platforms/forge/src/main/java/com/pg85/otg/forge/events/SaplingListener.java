package com.pg85.otg.forge.events;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.generator.resource.SaplingGen;
import com.pg85.otg.generator.resource.SaplingType;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class SaplingListener
{
    @SubscribeEvent
    public void onSaplingGrow(SaplingGrowTreeEvent event)
    {
        World world = event.getWorld();
        ForgeWorld localWorld = ((ForgeEngine)OTG.getEngine()).getWorld(world);
        BlockPos blockPos = event.getPos();

        if (localWorld == null)
        {
            // World not managed by Open Terrain Generator
            return;
        }

        LocalBiome biome = localWorld.getBiome(blockPos.getX(), blockPos.getZ());
        BiomeConfig biomeConfig = biome.getBiomeConfig();

        //SaplingGrower saplingGrower = new SaplingGrower(localWorld, blockPos);

        // Get material
        LocalMaterialData material = localWorld.getMaterial
                (blockPos.getX(), blockPos.getY(), blockPos.getZ(), null);
        // Check material not null
        if (material == null) {
            return;
        }

        // Check whether block is a sapling. If not, assume custom sapling block.
        SaplingType saplingType = null;
        if (material.toDefaultMaterial() != DefaultMaterial.SAPLING) {
            saplingType = SaplingType.Custom;
        }
        boolean wideTrunk;
        // Try to find big (2x2) sapling
        SaplingType bigSaplingType = getBigSaplingType(material.getBlockData());
        // Check for widetrunk if big species or if custom tree
        if (bigSaplingType != null || saplingType == SaplingType.Custom)
        {
            BlockPos result = findFourSaplings(blockPos, material, localWorld);
            if (result != null)
            {
                if (saplingType != SaplingType.Custom)
                {
                    saplingType = bigSaplingType;
                }
                blockPos = result;
                wideTrunk = true;
            }
            else
            {
                // Not a wide trunk
                wideTrunk = false;
            }
        } else {
            // Not big sapling and not custom, cannot be big
            wideTrunk = false;
        }

        // If not big sapling, try to find small sapling
        if (saplingType == null)
        {
            saplingType = getSmallSaplingType(material.getBlockData());
        }

        if (saplingType == null)
        {
            // Unsupported sapling
            saplingType = SaplingType.Custom;
        }

        // Get the sapling generator
        SaplingGen sapling;
        if (saplingType == SaplingType.Custom)
        {
            sapling = biomeConfig.getCustomSaplingGen(material, wideTrunk);
        }
        else
        {
            sapling = biomeConfig.getSaplingGen(saplingType);
        }
        // Check inheritance
        if (sapling == null)
        {
            BiomeConfig parent = getParent(biome, localWorld);
            if (parent != null) {
                if (saplingType == SaplingType.Custom) {
                    sapling = parent.getCustomSaplingGen(material, wideTrunk);
                } else {
                    sapling = parent.getSaplingGen(saplingType);
                }
            }
        }

        if (sapling == null) {
            // No sapling generator set for this sapling
            return;
        }

        // When we have reached this point, we know that we have to handle the
        // event ourselves
        // So cancel it
        event.setResult(Result.DENY);

        // Remove saplings
        IBlockState air = Blocks.AIR.getDefaultState();

        if (wideTrunk)
        {
            world.setBlockState(blockPos, air);
            world.setBlockState(blockPos.add(1, 0, 0), air);
            world.setBlockState(blockPos.add(0, 0, 1), air);
            world.setBlockState(blockPos.add(1, 0, 1), air);
        } else {
            world.setBlockState(blockPos, air);
        }

        // Try ten times to grow sapling
        boolean saplingGrown = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (sapling.growSapling(localWorld, random, wideTrunk, blockPos.getX(), blockPos.getY(), blockPos.getZ()))
            {
                saplingGrown = true;
                break;
            }
        }

        if (!saplingGrown)
        {
            // Restore sapling
            int saplingX = blockPos.getX();
            int saplingY = blockPos.getY();
            int saplingZ = blockPos.getZ();
            if (saplingType.requiresFourSaplings())
            {
                localWorld.setBlock
                        (saplingX, saplingY, saplingZ, material, null, null);
                localWorld.setBlock
                        (saplingX + 1, saplingY, saplingZ, material, null, null);
                localWorld.setBlock
                        (saplingX, saplingY, saplingZ + 1, material, null, null);
                localWorld.setBlock
                        (saplingX + 1, saplingY, saplingZ + 1, material, null, null);
            } else {
                localWorld.setBlock
                        (saplingX, saplingY, saplingZ, material, null, null);
            }
        }
    }

    @SubscribeEvent
    public void onBonemealUse(BonemealEvent event)
    {
        ForgeWorld localWorld = ((ForgeEngine)OTG.getEngine()).getWorld(event.getWorld());
        if (localWorld == null)
        {
            // World not managed by Open Terrain Generator
            return;
        }
        LocalBiome biome = localWorld.getBiome(event.getPos().getX(), event.getPos().getZ());
        if (biome == null)
        {
            return;
        }
        BiomeConfig biomeConfig = biome.getBiomeConfig();

        // Get sapling gen
        SaplingGen gen = null;
        SaplingType type = null;
        if (event.getBlock() == Blocks.RED_MUSHROOM_BLOCK)
        {
            type = SaplingType.RedMushroom;
        } else if (event.getBlock() == Blocks.BROWN_MUSHROOM_BLOCK)
        {
            type = SaplingType.BrownMushroom;
        }
        else {
            return;
        }
        gen = biomeConfig.getSaplingGen(type);
        if (gen == null)
        {
            BiomeConfig parent = getParent(biome, localWorld);
            if (parent != null)
            {
                gen = parent.getSaplingGen(type);
            }
        }
        // Neither this nor parent has matching saplingType, return;
        if (gen == null) {
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
    /**
     * Gets the parent BiomeConfig of a given biome, if it has one and sapling inheritance is enabled.
     * Used for checking sapling inheritance.
     * @param biome The input biome
     * @param world The world that contains said biome
     * @return The BiomeConfig of the parent biome, or null if not applicable
     */
    private BiomeConfig getParent(LocalBiome biome, LocalWorld world)
    {
        BiomeConfig biomeConfig = biome.getBiomeConfig();
        if (biomeConfig.inheritSaplingResource
                && biomeConfig.replaceToBiomeName != null
                && biomeConfig.replaceToBiomeName.trim().length() > 0)
        {
           return world.getBiomeByNameOrNull(biomeConfig.replaceToBiomeName).getBiomeConfig();
        }
        return null;
    }
    /**
     * Gets the sapling type, based on the assumption that the sapling is
     * not placed in a 2x2 pattern.
     *
     * @param data The block data of the sapling block.
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
     * @param data The block data of the sapling block.
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
    /**
     * Check that the sapling types are the same, ignoring growth stages.
     * @param sapling1 The first material to compare.
     * @param sapling2 The second material to compare.
     * @return True if both saplings are of the same type
     */
    private boolean isSameSapling(LocalMaterialData sapling1, LocalMaterialData sapling2)
    {
        if (sapling1 == null || sapling2 == null)
            return false;
        return sapling1.getBlockData() % 8 == sapling2.getBlockData() % 8
                && sapling1.toDefaultMaterial() == sapling2.toDefaultMaterial();

    }
    /**
     * Gets whether the saplings are placed in a 2x2 pattern. If successful,
     * it returns a BlockPos that represents the top left
     * sapling (with the lowest x and z). If not, it returns null.
     *
     * @return BlockPos of sapling with lowest X and Z, or null if not four saplings
     */
    private BlockPos findFourSaplings(BlockPos blockPos, LocalMaterialData material, LocalWorld world)
    {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        for (int treeOffsetX = 0; treeOffsetX >= -1; --treeOffsetX)
        {
            for (int treeOffsetZ = 0; treeOffsetZ >= -1; --treeOffsetZ)
            {
                if (isSameSapling(material, world.getMaterial(x + treeOffsetX, y, z + treeOffsetZ, null))
                        && isSameSapling(material, world.getMaterial(x + treeOffsetX + 1, y, z + treeOffsetZ, null))
                        && isSameSapling(material, world.getMaterial(x + treeOffsetX, y, z + treeOffsetZ + 1, null))
                        && isSameSapling(material, world.getMaterial(x + treeOffsetX + 1, y, z + treeOffsetZ + 1, null)))
                {
                    // Found! Adjust internal position
                    return blockPos.add(treeOffsetX, 0, treeOffsetZ);
                }
            }
        }
        return null;
    }
}
