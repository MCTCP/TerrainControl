package com.pg85.otg.generator.surface;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

public class FrozenSurfaceHelper
{
    private final LocalWorld world;
    private final WorldConfig worldConfig;
    private final int maxPropagationSize = 15;
    private int currentPropagationSize = 0;
	private int maxLayersOnLeaves = 3;

    public FrozenSurfaceHelper(LocalWorld world)
    {
        this.world = world;
        this.worldConfig = world.getConfigs().getWorldConfig();
    }

    /**
     * Freezes and Applied snow to an offset chunkCoordinate
     * @param chunkCoord The chunk to freeze and snow on
     */
    public void freezeChunk(ChunkCoordinate chunkCoord)
    {
        int x = chunkCoord.getBlockXCenter();
        int z = chunkCoord.getBlockZCenter();
        for (int i = 0; i < ChunkCoordinate.CHUNK_X_SIZE; i++)
        {
            for (int j = 0; j < ChunkCoordinate.CHUNK_Z_SIZE; j++)
            {
                int blockToFreezeX = x + i;
                int blockToFreezeZ = z + j;
                freezeColumn(blockToFreezeX, blockToFreezeZ, chunkCoord);
            }
        }
    }

    /**
     * Performs a liquid freeze and lays down a layer of snow on a Chunk column
     * @param x Location X
     * @param z Location Z
     */
    private void freezeColumn(int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        LocalBiome biome = world.getBiome(x, z);
        if (biome != null)
        {
            int blockToFreezeY = world.getHighestBlockAboveYAt(x, z, chunkBeingPopulated);
            float tempAtBlockToFreeze = biome.getTemperatureAt(x, blockToFreezeY, z);
            if (blockToFreezeY > 0 && tempAtBlockToFreeze < WorldStandardValues.SNOW_AND_ICE_TEMP)
            {
                this.currentPropagationSize = 0;
                // Start to freeze liquids
                if (!this.freezeLiquid(x, blockToFreezeY -1, z, chunkBeingPopulated))
                {
                    // Snow has to be placed on an empty space on a block that accepts snow in the world
                    startSnowFall(x, blockToFreezeY, z, biome, chunkBeingPopulated);
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
    private boolean freezeLiquid(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        LocalBiome biome = world.getBiome(x, z);
        if (biome != null)
        {
            LocalMaterialData materialToFreeze = world.getMaterial(x, y, z, chunkBeingPopulated);
            if (materialToFreeze.isLiquid())
            {
                // Water & Stationary Water => IceBlock
                freezeType(x, y, z, materialToFreeze, biome.getBiomeConfig().iceBlock, DefaultMaterial.WATER, DefaultMaterial.STATIONARY_WATER, chunkBeingPopulated);
                // Lava & Stationary Lava => CooledLavaBlock
                freezeType(x, y, z, materialToFreeze, biome.getBiomeConfig().cooledLavaBlock, DefaultMaterial.LAVA, DefaultMaterial.STATIONARY_LAVA, chunkBeingPopulated);
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
    private void freezeType(int x, int y, int z, LocalMaterialData thawedMaterial, LocalMaterialData frozenMaterial, DefaultMaterial check1, DefaultMaterial check2, ChunkCoordinate chunkBeingPopulated)
    {
        if ((thawedMaterial.isMaterial(check1) || thawedMaterial.isMaterial(check2)) && !frozenMaterial.isMaterial(check1) && !frozenMaterial.isMaterial(check2))
        {
            world.setBlock(x, y, z, frozenMaterial, null, chunkBeingPopulated);
            if (worldConfig.fullyFreezeLakes && this.currentPropagationSize < this.maxPropagationSize)
            {
                this.currentPropagationSize++;
                propagateFreeze(x, y, z, chunkBeingPopulated);
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
    private void startSnowFall(int x, int y, int z, LocalBiome biome, ChunkCoordinate chunkBeingPopulated)
    {
        int decreaseFactor = 0;
        BiomeConfig biomeConfig = biome.getBiomeConfig();

        float tempAtBlockToFreeze;
        int snowHeight;
        LocalMaterialData materialToSnowAt = null;
        LocalMaterialData materialToSnowOn = null;
        if(this.worldConfig.betterSnowFall)
        {
	        tempAtBlockToFreeze = biome.getTemperatureAt(x, y, z);
	        snowHeight = biomeConfig.getSnowHeight(tempAtBlockToFreeze);
        } else {
        	snowHeight = 0;
        }        
    	while (
			y > PluginStandardValues.WORLD_DEPTH + 1 && 
			decreaseFactor < 8 &&
			snowHeight - decreaseFactor >= 0
		)
        {
            materialToSnowAt = world.getMaterial(x, y, z, chunkBeingPopulated);
            materialToSnowOn = world.getMaterial(x, y - 1, z, chunkBeingPopulated);            
            if (
        		materialToSnowAt != null &&
        		materialToSnowOn != null &&
        		materialToSnowAt.isAir() &&
        		materialToSnowOn.canSnowFallOn()
    		)
            {
            	// If we've spawned all snow layers, exit.
                if(this.setSnowFallAtLocation(x, y, z, snowHeight - decreaseFactor, materialToSnowOn, chunkBeingPopulated))
                {
                	break;
                }
                // Spawned on leaves, which can only carry maxLayersOnLeaves snow layers. 
                // We have more snow layers to spawn.
                decreaseFactor += maxLayersOnLeaves;
            }
            if(materialToSnowOn == null || materialToSnowOn.isSolid())
            {
            	break;
            }
            y--;
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
    private boolean setSnowFallAtLocation(int x, int y, int z, int baseSnowHeight, LocalMaterialData materialToSnowOn, ChunkCoordinate chunkBeingPopulated)
    {
        LocalMaterialData snowMass;
        if (materialToSnowOn.isMaterial(DefaultMaterial.LEAVES) || materialToSnowOn.isMaterial(DefaultMaterial.LEAVES_2))
        {
            // Snow Layer(s) for trees, let each leaf carry maxLayersOnLeaves or less layers of snow,
        	// any remaining layers will fall through.
        	snowMass = MaterialHelper.toLocalMaterialData(DefaultMaterial.SNOW, baseSnowHeight <= maxLayersOnLeaves - 1 ? baseSnowHeight : maxLayersOnLeaves - 1);
            world.setBlock(x, y, z, snowMass, null, chunkBeingPopulated);
            return baseSnowHeight <= maxLayersOnLeaves - 1;
        }
        
        // Basic Snow Layer(s)
        snowMass = MaterialHelper.toLocalMaterialData(DefaultMaterial.SNOW, baseSnowHeight);
        world.setBlock(x, y, z, snowMass, null, chunkBeingPopulated);
        return true;
    }

    /**
     * Helps propagate the freezing of liquids.
     * @param x Location X
     * @param y Location Y
     * @param z Location Z
     */
    private void propagateFreeze(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        this.propagationHelper(x+1, y, z, chunkBeingPopulated);
        this.propagationHelper(x+1, y, z+1, chunkBeingPopulated);
        this.propagationHelper(x, y, z+1, chunkBeingPopulated);
        this.propagationHelper(x-1, y, z+1, chunkBeingPopulated);
        this.propagationHelper(x-1, y, z, chunkBeingPopulated);
        this.propagationHelper(x-1, y, z-1, chunkBeingPopulated);
        this.propagationHelper(x, y, z-1, chunkBeingPopulated);
        this.propagationHelper(x+1, y, z-1, chunkBeingPopulated);
    }

    /**
     * Called by propagateFreeze, does the actual checks and then called freezeLiquid
     * @param x Location X
     * @param y Location Y
     * @param z Location Z
     */
    private void propagationHelper(int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        if (world.getHighestBlockAboveYAt(x, z, chunkBeingPopulated) - 1 > y && this.currentPropagationSize < this.maxPropagationSize)
        {
            this.freezeLiquid(x, y, z, chunkBeingPopulated);
        }
    }
}
