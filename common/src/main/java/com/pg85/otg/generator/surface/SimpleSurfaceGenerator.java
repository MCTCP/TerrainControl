package com.pg85.otg.generator.surface;

import static com.pg85.otg.util.ChunkCoordinate.CHUNK_Y_SIZE;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.RawMaterialData;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.GeneratingChunk;
import com.pg85.otg.util.helpers.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

/**
 * Implementation of {@link SurfaceGenerator} that does absolutely nothing.
 *
 */
public class SimpleSurfaceGenerator implements SurfaceGenerator
{
    private final LocalMaterialData air = MaterialHelper.toLocalMaterialData(DefaultMaterial.AIR, 0);
    private final LocalMaterialData sandstone = MaterialHelper.toLocalMaterialData(DefaultMaterial.SANDSTONE, 0);
    private final LocalMaterialData red_sandstone = MaterialHelper.toLocalMaterialData(DefaultMaterial.RED_SANDSTONE, 0);

    @Override
    public LocalMaterialData getCustomBlockData(LocalWorld world, BiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	return null;
    }
    
    @Override
    public void spawn(LocalWorld world, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int xInWorld, int zInWorld)
    {
        spawnColumn(world, biomeConfig.surfaceBlock, biomeConfig.groundBlock, generatingChunk, chunkBuffer, biomeConfig, xInWorld & 0xf, zInWorld & 0xf);
    }

    // net.minecraft.world.biome.Biome.generateBiomeTerrain
    protected final void spawnColumn(LocalWorld world, LocalMaterialData defaultSurfaceBlock, LocalMaterialData defaultGroundBlock, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int x, int z)
    {
        if (defaultSurfaceBlock instanceof RawMaterialData) {
            defaultSurfaceBlock = ((RawMaterialData) defaultSurfaceBlock).readForWorld(world);
        }
        if (defaultGroundBlock instanceof RawMaterialData) {
            defaultGroundBlock = ((RawMaterialData) defaultGroundBlock).readForWorld(world);
        }
        
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
        
        LocalMaterialData stoneBlock = checkAndParseRawData(world, biomeConfig.stoneBlock);
        LocalMaterialData bedrockBlock = checkAndParseRawData(world, worldConfig.bedrockBlock);
        LocalMaterialData waterBlock = checkAndParseRawData(world, biomeConfig.waterBlock);
        LocalMaterialData iceBlock = checkAndParseRawData(world, biomeConfig.iceBlock);
        
        int surfaceBlocksCount = -1;
        final int currentWaterLevel = generatingChunk.getWaterLevel(x, z);
        for (int y = CHUNK_Y_SIZE - 1; y >= 0; y--)
        {
            if (generatingChunk.mustCreateBedrockAt(worldConfig, y))
            {
                // Place bedrock
                chunkBuffer.setBlock(x, y, z, bedrockBlock);
            } else {
                // Surface blocks logic (grass, dirt, sand, sandstone)
                final LocalMaterialData blockOnCurrentPos = chunkBuffer.getBlock(x, y, z);

                if (blockOnCurrentPos.isAir())
                {
                    // Reset when air is found
                    surfaceBlocksCount = -1;
                }
                else if (blockOnCurrentPos.equals(stoneBlock))
                {
                    if (surfaceBlocksCount == -1)
                    {
                        // Set when variable was reset
                        if (surfaceBlocksNoise <= 0 && !worldConfig.removeSurfaceStone)
                        {
                            currentSurfaceBlock = air;
                            currentGroundBlock = stoneBlock;
                        }
                        else if ((y >= currentWaterLevel - 4) && (y <= currentWaterLevel + 1))
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
                                currentSurfaceBlock = iceBlock;
                            } else {
                                currentSurfaceBlock = waterBlock;
                            }
                        }

                        // Place surface block
                        surfaceBlocksCount = surfaceBlocksNoise;                       
                        if (y >= currentWaterLevel - 1)
                        {
                        	chunkBuffer.setBlock(x, y, z, currentSurfaceBlock);
                        } else {
                        	chunkBuffer.setBlock(x, y, z, currentGroundBlock);
                        }
                    }
                    else if (surfaceBlocksCount > 0)
                    {
                        // Place ground block
                        surfaceBlocksCount--;
                        chunkBuffer.setBlock(x, y, z, currentGroundBlock);

                        // Place sandstone under stand
                        if ((surfaceBlocksCount == 0) && (currentGroundBlock.isMaterial(DefaultMaterial.SAND)) && surfaceBlocksNoise > 1)
                        {
                            surfaceBlocksCount = generatingChunk.random.nextInt(4) + Math.max(0, y - generatingChunk.getWaterLevel(x, z));
                            currentGroundBlock = currentGroundBlock.getBlockData() == 1 ? red_sandstone : sandstone;
                        }
                    }
                }
            }
        }
    }

    private LocalMaterialData checkAndParseRawData(LocalWorld world, LocalMaterialData data)
    {
        if (data instanceof RawMaterialData) {
            return ((RawMaterialData) data).readForWorld(world);
        } else {
            return data;
        }
    }

    @Override
    public String toString()
    {
        // Make sure that empty name is written to the config files
        return "";
    }
}
