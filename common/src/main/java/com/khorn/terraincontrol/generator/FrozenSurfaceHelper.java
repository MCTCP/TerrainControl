package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.helpers.MathHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

public class FrozenSurfaceHelper
{
    private final LocalWorld world;

    public FrozenSurfaceHelper(LocalWorld world)
    {
        this.world = world;
    }

    protected void freezeChunk(ChunkCoordinate chunkCoord)
    {
        int x = chunkCoord.getChunkX() * 16 + 8;
        int z = chunkCoord.getChunkZ() * 16 + 8;
        for (int i = 0; i < 16; i++)
        {
            for (int j = 0; j < 16; j++)
            {
                int blockToFreezeX = x + i;
                int blockToFreezeZ = z + j;
                freezeColumn(blockToFreezeX, blockToFreezeZ);
            }
        }
    }

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
                if (!this.freezeLiquid(x, blockToFreezeY -1, z))
                {
                    // Snow has to be placed on an empty space on a
                    // block that accepts snow in the world
                    LocalMaterialData blockToFreeze = world.getMaterial(x, blockToFreezeY - 1, z);
                    if (blockToFreeze.canSnowFallOn())
                    {
                        if (world.isEmpty(x, blockToFreezeY, z))
                        {
                            int decreaseBy = 0;
                            int snowHeight = biome.getBiomeConfig().getSnowHeight(tempAtBlockToFreeze);
                            // Less snow on trees
                            if (blockToFreeze.isMaterial(DefaultMaterial.LEAVES) || blockToFreeze.isMaterial(DefaultMaterial.LEAVES_2))
                            {
                                decreaseBy = MathHelper.clamp(MathHelper.ceil(MathHelper.sqrt(snowHeight)), 0, snowHeight);
                            }
                            world.setBlock(x, blockToFreezeY, z, TerrainControl.toLocalMaterialData(DefaultMaterial.SNOW, MathHelper.clamp(snowHeight - decreaseBy, 0, 8)));
                        }
                    }
                }
            }
        }
    }

    private boolean freezeLiquid(int x, int y, int z)
    {
        LocalBiome biome = world.getBiome(x, z);
        if (biome != null)
        {
            LocalMaterialData materialToFreeze = world.getMaterial(x, y, z);
            if (materialToFreeze.isMaterial(DefaultMaterial.WATER) || materialToFreeze.isMaterial(DefaultMaterial.STATIONARY_WATER))
            {
                world.setBlock(x, y, z, biome.getBiomeConfig().iceBlock);
                return true;
            }
        }
        return false;
    }
}
