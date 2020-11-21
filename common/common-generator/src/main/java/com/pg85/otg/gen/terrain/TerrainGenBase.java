package com.pg85.otg.gen.terrain;

import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.util.Random;

public abstract class TerrainGenBase
{
    protected int checkAreaSize = 8;
    protected Random random = new Random();
    private final long worldLong1;
    private final long worldLong2;
    private final long worldSeed;

    TerrainGenBase(long worldSeed)
    {
        this.random.setSeed(worldSeed);        
        worldLong1 = this.random.nextLong();
        worldLong2 = this.random.nextLong();
        this.worldSeed = worldSeed;
    }

    public void generate(IWorldGenRegion worldGenRegion, ChunkBuffer chunkBuffer)
    {
        int i = this.checkAreaSize;
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();
        int chunkX = chunkCoord.getChunkX();
        int chunkZ = chunkCoord.getChunkZ();

        for (int x = chunkX - i; x <= chunkX + i; x++)
        {
            for (int z = chunkZ - i; z <= chunkZ + i; z++)
            {
                long l3 = x * worldLong1;
                long l4 = z * worldLong2;
                this.random.setSeed(l3 ^ l4 ^ this.worldSeed);
                generateChunk(worldGenRegion, ChunkCoordinate.fromChunkCoords(x, z), chunkBuffer);
            }
        }
    }

    /**
     * Generates the structure for the given chunk. The terrain generator
     * calls this method for all chunks not more than {@link #checkAreaSize}
     * chunks away on either axis from the generatingChunk.
     *
     * @param currentChunk          The chunk we're searching.
     * @param generatingChunkBuffer The chunk that is currently being
     *                              generated.
     */
    protected abstract void generateChunk(IWorldGenRegion worldGenRegion, ChunkCoordinate currentChunk, ChunkBuffer generatingChunkBuffer);
}
