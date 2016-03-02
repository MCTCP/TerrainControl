package com.khorn.terraincontrol.generator;

import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_X_SIZE;
import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_Z_SIZE;

import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import java.util.Random;

/**
 * Holds early generator information about a chunk, like water levels, noise
 * values, etc.
 */
public final class GeneratingChunk
{

    private static final int BEDROCK_LAYER_HEIGHT = 5;

    public final int heightCap;
    public final Random random;
    private final byte[] waterLevel;
    private final double[] surfaceNoise;

    GeneratingChunk(Random random, byte[] waterLevel, double[] surfaceNoise, int heightCap)
    {
        this.random = random;
        this.waterLevel = waterLevel;
        this.surfaceNoise = surfaceNoise;
        this.heightCap = heightCap;
    }

    /**
     * Gets the surface noise value at the given position.
     * 
     * @param x X position, 0 <= x < {@value ChunkCoordinate#CHUNK_X_SIZE}.
     * @param z Z position, 0 <= z < {@value ChunkCoordinate#CHUNK_X_SIZE}.
     * @return The surface noise value.
     */
    public double getNoise(int x, int z)
    {
        return this.surfaceNoise[x + z * CHUNK_X_SIZE];
    }

    /**
     * Gets the water level at the given position.
     * 
     * @param x X position, 0 <= x < {@value ChunkCoordinate#CHUNK_X_SIZE}.
     * @param z Z position, 0 <= z < {@value ChunkCoordinate#CHUNK_X_SIZE}.
     * @return The water level.
     */
    public int getWaterLevel(int x, int z)
    {
        return this.waterLevel[z + x * CHUNK_Z_SIZE] & 0xff;
    }

    /**
     * Gets whether bedrock should be created at the given position.
     *
     * @param worldConfig The worldConfig, for bedrock settings.
     * @param y           The y position.
     * @return True if bedrock should be created, false otherwise.
     */
    public boolean mustCreateBedrockAt(WorldConfig worldConfig, int y)
    {
        // The "- 2" that appears in this method, comes from that heightCap -
        // 1 is the highest place where a block can be placed, and heightCap -
        // 2 is the highest place where bedrock can be generated to make sure
        // there are no light glitches - see #117

        // Handle flat bedrock
        if (worldConfig.flatBedrock)
        {
            if (!worldConfig.disableBedrock && y == 0)
            {
                return true;
            }
            if (worldConfig.ceilingBedrock && y >= this.heightCap - 2)
            {
                return true;
            }
            return false;
        }

        // Otherwise we have normal bedrock
        if (!worldConfig.disableBedrock && y < 5)
        {
            return y <= this.random.nextInt(BEDROCK_LAYER_HEIGHT);
        }
        if (worldConfig.ceilingBedrock)
        {
            int amountBelowHeightCap = this.heightCap - y - 2;
            if (amountBelowHeightCap < 0 || amountBelowHeightCap > BEDROCK_LAYER_HEIGHT)
            {
                return false;
            }

            return amountBelowHeightCap <= this.random.nextInt(BEDROCK_LAYER_HEIGHT);
        }
        return false;
    }

}
