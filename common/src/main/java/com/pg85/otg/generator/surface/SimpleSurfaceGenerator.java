package com.pg85.otg.generator.surface;

import static com.pg85.otg.util.ChunkCoordinate.CHUNK_Y_SIZE;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.GeneratingChunk;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

/**
 * Implementation of {@link SurfaceGenerator} that does absolutely nothing.
 *
 */
public class SimpleSurfaceGenerator implements SurfaceGenerator
{
    private final LocalMaterialData air = OTG.toLocalMaterialData(DefaultMaterial.AIR, 0);
    private final LocalMaterialData sandstone = OTG.toLocalMaterialData(DefaultMaterial.SANDSTONE, 0);

    @Override
    public LocalMaterialData getCustomBlockData(LocalWorld world, BiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	return null;
    }
    
    @Override
    public void spawn(GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int xInWorld, int zInWorld)
    {
        spawnColumn(biomeConfig.surfaceBlock, biomeConfig.groundBlock, generatingChunk, chunkBuffer, biomeConfig, xInWorld & 0xf, zInWorld & 0xf);
    }

    protected final void spawnColumn(LocalMaterialData defaultSurfaceBlock, LocalMaterialData defaultGroundBlock, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int x, int z)
    {
        WorldConfig worldConfig = biomeConfig.worldConfig;
        float currentTemperature = biomeConfig.biomeTemperature;
        int surfaceBlocksNoise = (int) (generatingChunk.getNoise(x, z) / 3.0D + 3.0D + generatingChunk.random.nextDouble() * 0.25D);

        // Bedrock on the ceiling
        if (worldConfig.ceilingBedrock)
        {
            // Moved one block lower to fix lighting issues
            chunkBuffer.setBlock(x, generatingChunk.heightCap - 2, z, worldConfig.bedrockBlock);
        }

        // Loop from map height to zero to place bedrock and surface
        // blocks
        LocalMaterialData currentSurfaceBlock = defaultSurfaceBlock;
        LocalMaterialData currentGroundBlock = defaultGroundBlock;
        int surfaceBlocksCount = -1;
        final int currentWaterLevel = generatingChunk.getWaterLevel(x, z);
        for (int y = CHUNK_Y_SIZE - 1; y >= 0; y--)
        {
            if (generatingChunk.mustCreateBedrockAt(worldConfig, y))
            {
                // Place bedrock
                chunkBuffer.setBlock(x, y, z, worldConfig.bedrockBlock);
            } else
            {
                // Surface blocks logic (grass, dirt, sand, sandstone)
                final LocalMaterialData blockOnCurrentPos = chunkBuffer.getBlock(x, y, z);

                if (blockOnCurrentPos.isAir())
                {
                    // Reset when air is found
                    surfaceBlocksCount = -1;
                } else if (blockOnCurrentPos.equals(biomeConfig.stoneBlock))
                {
                    if (surfaceBlocksCount == -1)
                    {
                        // Set when variable was reset
                        if (surfaceBlocksNoise <= 0 && !worldConfig.removeSurfaceStone)
                        {
                            currentSurfaceBlock = air;
                            currentGroundBlock = biomeConfig.stoneBlock;
                        } else if ((y >= currentWaterLevel - 4) && (y <= currentWaterLevel + 1))
                        {
                            currentSurfaceBlock = defaultSurfaceBlock;
                            currentGroundBlock = defaultGroundBlock;
                        }

                        // Use blocks for the top of the water instead
                        // when on water
                        if ((y < currentWaterLevel) && (y > worldConfig.waterLevelMin) && currentSurfaceBlock.isAir())
                        {
                            if (currentTemperature < WorldStandardValues.SNOW_AND_ICE_MAX_TEMP)
                            {
                                currentSurfaceBlock = biomeConfig.iceBlock;
                            } else
                            {
                                currentSurfaceBlock = biomeConfig.waterBlock;
                            }
                        }

                        // Place surface block
                        surfaceBlocksCount = surfaceBlocksNoise;
                        if (y >= currentWaterLevel - 1)
                        {
                            chunkBuffer.setBlock(x, y, z, currentSurfaceBlock);
                        } else
                        {
                            chunkBuffer.setBlock(x, y, z, currentGroundBlock);
                        }

                    } else if (surfaceBlocksCount > 0)
                    {
                        // Place ground block
                        surfaceBlocksCount--;
                        chunkBuffer.setBlock(x, y, z, currentGroundBlock);

                        // Place sandstone under stand
                        if ((surfaceBlocksCount == 0) && (currentGroundBlock.isMaterial(DefaultMaterial.SAND)))
                        {
                            surfaceBlocksCount = generatingChunk.random.nextInt(4);
                            currentGroundBlock = sandstone;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString()
    {
        // Make sure that empty name is written to the config files
        return "";
    }
}
