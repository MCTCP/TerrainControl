package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

public class FrozenSurfaceHelper
{
    private final LocalWorld world;
    private final WorldConfig worldConfig;
    private int decreaseFactor = 0;
    private final int maxPropagationSize = 15;
    private int currentPropagationSize = 0;

    public FrozenSurfaceHelper(LocalWorld world)
    {
        this.world = world;
        this.worldConfig = world.getConfigs().getWorldConfig();
    }

    /**
     * Freezes and Applied snow to an offset chunkCoordinate
     * @param chunkCoord The chunk to freeze and snow on
     */
    protected void freezeChunk(ChunkCoordinate chunkCoord)
    {
        int x = chunkCoord.getBlockXCenter();
        int z = chunkCoord.getBlockZCenter();
        for (int i = 0; i < ChunkCoordinate.CHUNK_X_SIZE; i++)
        {
            for (int j = 0; j < ChunkCoordinate.CHUNK_Z_SIZE; j++)
            {
                int blockToFreezeX = x + i;
                int blockToFreezeZ = z + j;
                freezeColumn(blockToFreezeX, blockToFreezeZ);
            }
        }
    }

    /**
     * Performs a liquid freeze and lays down a layer of snow on a Chunk column
     * @param x Location X
     * @param z Location Z
     */
    protected void freezeColumn(int x, int z)
    {
        // Using the calculated biome id so that ReplaceToBiomeName can't mess up the ids
        LocalBiome biome = world.getBiome(x, z);
        if (biome != null)
        {
            int blockToFreezeY = world.getHighestBlockYAt(x, z);
            float tempAtBlockToFreeze = biome.getTemperatureAt(x, blockToFreezeY, z);
            if (blockToFreezeY > 0 && tempAtBlockToFreeze < WorldStandardValues.SNOW_AND_ICE_MAX_TEMP)
            {
                this.currentPropagationSize = 0;
                // Start to freeze liquids
                if (!this.freezeLiquid(x, blockToFreezeY -1, z))
                {
                    // Snow has to be placed on an empty space on a block that accepts snow in the world
                    startSnowFall(x, blockToFreezeY, z, biome);
                }
            }
        }
    }

    /**
     * Attempts to freeze liquids at the given location
     * @param x Location X
     * @param y Location Y
     * @param z Location Z
     * @return If a liquid was present at the given location (not necessarily successful in freezing)
     */
    private boolean freezeLiquid(int x, int y, int z)
    {
        LocalBiome biome = world.getBiome(x, z);
        if (biome != null)
        {
            LocalMaterialData materialToFreeze = world.getMaterial(x, y, z);
            if (materialToFreeze.isLiquid())
            {
                // Water & Stationary Water => IceBlock
                freezeType(x, y, z, materialToFreeze, biome.getBiomeConfig().iceBlock, DefaultMaterial.WATER, DefaultMaterial.STATIONARY_WATER);
                // Lava & Stationary Lava => CooledLavaBlock
                freezeType(x, y, z, materialToFreeze, biome.getBiomeConfig().cooledLavaBlock, DefaultMaterial.LAVA, DefaultMaterial.STATIONARY_LAVA);
                return true;
            }
        }
        return false;
    }

    /**
     * Freezes two types of blocks to a third type at a specific location.
     *
     * Example: WATER and STATIONARY_WATER to ICE
     * @param x Location X
     * @param y Location Y
     * @param z Location Z
     * @param thawedMaterial The material to be checked and if passed, frozen
     * @param frozenMaterial The material to freeze the thawed material to if checks pass
     * @param check1 The first material to check for
     * @param check2 The second meterial to check for
     */
    private void freezeType(int x, int y, int z, LocalMaterialData thawedMaterial, LocalMaterialData frozenMaterial, DefaultMaterial check1, DefaultMaterial check2)
    {
        if ((thawedMaterial.isMaterial(check1) || thawedMaterial.isMaterial(check2)) && !frozenMaterial.isMaterial(check1) && !frozenMaterial.isMaterial(check2))
        {
            world.setBlock(x, y, z, frozenMaterial);
            if (worldConfig.fullyFreezeLakes && this.currentPropagationSize < this.maxPropagationSize)
            {
                propagateFreeze(x, y, z);
            }
        }

    }

    /**
     * Determines all Y locations that need snow, and how much snow in each Y location based on temperature
     * and transparent block pass-through. Sets snow to determined height for each Y location applicable.
     * @param x Location X
     * @param y Location Y
     * @param z Location Z
     * @param biome The biome associated with the chunk column
     */
    private void startSnowFall(int x, int y, int z, LocalBiome biome)
    {
        decreaseFactor = 0;
        BiomeConfig biomeConfig = biome.getBiomeConfig();

        float tempAtBlockToFreeze = biome.getTemperatureAt(x, y, z);
        int snowHeight = biomeConfig.getSnowHeight(tempAtBlockToFreeze);
        // Decreased snow amounts for leaves
        LocalMaterialData materialToSnowAt = world.getMaterial(x, y, z);
        LocalMaterialData materialToSnowOn = world.getMaterial(x, y - 1, z);
        if (materialToSnowAt.isAir() && materialToSnowOn.canSnowFallOn())
        {
            this.setSnowFallAtLocation(x, y--, z, snowHeight, materialToSnowOn);
        }
        if (worldConfig.betterSnowFall) {
            do
            {
                materialToSnowAt = world.getMaterial(x, --y, z);
                materialToSnowOn = world.getMaterial(x, y - 1, z);
                if (materialToSnowAt.isAir() && materialToSnowOn.canSnowFallOn())
                {
                    this.setSnowFallAtLocation(x, y--, z, snowHeight, materialToSnowOn);
                    continue;
                }
                if (!materialToSnowAt.isAir())
                {
                    ++decreaseFactor;
                }
            } while (!materialToSnowAt.isSolid() && y > 0);
        }
    }

    /**
     * Applied snow to a location
     * @param x Location X
     * @param y Location Y
     * @param z Location Z
     * @param baseSnowHeight The base height snow should be
     * @param materialToSnowOn The material that might have snow applied
     */
    private void setSnowFallAtLocation(int x, int y, int z, int baseSnowHeight, LocalMaterialData materialToSnowOn)
    {
        int snowHeightOnLeaves = MathHelper.clamp(MathHelper.ceil(MathHelper.sqrt(baseSnowHeight)), 0, baseSnowHeight);
        LocalMaterialData snowMass;
        if (worldConfig.betterSnowFall && (materialToSnowOn.isMaterial(DefaultMaterial.LEAVES) || materialToSnowOn.isMaterial(DefaultMaterial.LEAVES_2)))
        {
            // Snow Layer(s) for trees
            snowMass = TerrainControl.toLocalMaterialData(DefaultMaterial.SNOW, MathHelper.clamp(snowHeightOnLeaves, 0, 8));
        } else
        {
            // Basic Snow Layer(s)
            snowMass = TerrainControl.toLocalMaterialData(DefaultMaterial.SNOW, MathHelper.clamp(baseSnowHeight - decreaseFactor, 0, 8));
        }
        world.setBlock(x, y, z, snowMass);
    }

    /**
     * Helps propagate the freezing of liquids.
     * @param x Location X
     * @param y Location Y
     * @param z Location Z
     */
    private void propagateFreeze(int x, int y, int z)
    {
        this.propagationHelper(x+1, y, z);
        this.propagationHelper(x+1, y, z+1);
        this.propagationHelper(x, y, z+1);
        this.propagationHelper(x-1, y, z+1);
        this.propagationHelper(x-1, y, z);
        this.propagationHelper(x-1, y, z-1);
        this.propagationHelper(x, y, z-1);
        this.propagationHelper(x+1, y, z-1);
    }

    /**
     * Called by propagateFreeze, does the actual checks and then called freezeLiquid
     * @param x Location X
     * @param y Location Y
     * @param z Location Z
     */
    private void propagationHelper(int x, int y, int z)
    {
        if (world.getHighestBlockYAt(x, z)-1 > y && this.currentPropagationSize < this.maxPropagationSize)
        {
            this.freezeLiquid(x, y, z);
        }
    }

}
