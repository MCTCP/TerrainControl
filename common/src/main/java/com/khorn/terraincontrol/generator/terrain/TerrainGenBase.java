package com.khorn.terraincontrol.generator.terrain;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import java.util.Random;

public abstract class TerrainGenBase
{
    protected int checkAreaSize = 8;
    protected Random random = new Random();
    protected LocalWorld world;
    private final long worldLong1;
    private final long worldLong2;

    public TerrainGenBase(LocalWorld world)
    {
        this.world = world;
        this.random.setSeed(this.world.getSeed());
        worldLong1 = this.random.nextLong();
        worldLong2 = this.random.nextLong();
    }

    public void generate(ChunkCoordinate chunkCoord, byte[] paramArrayOfByte)
    {
        int i = this.checkAreaSize;
        int chunkX = chunkCoord.getChunkX();
        int chunkZ = chunkCoord.getChunkZ();

        for (int x = chunkX - i; x <= chunkX + i; x++)
            for (int z = chunkZ - i; z <= chunkZ + i; z++)
            {
                long l3 = x * worldLong1;
                long l4 = z * worldLong2;
                this.random.setSeed(l3 ^ l4 ^ this.world.getSeed());
                generateChunk(ChunkCoordinate.fromChunkCoords(x, z), chunkCoord, paramArrayOfByte);
            }
    }

    /**
     * Generates the structure for the given chunk. The terrain generator
     * calls this method for all chunks not more than {@link #checkAreaSize}
     * chunks away on either axis from the generatingChunk.
     *
     * @param currentChunk    The chunk we're searching.
     * @param generatingChunk The chunk that is currently being generated.
     * @param blocks          The blocks of the chunk that is currently
     *                        being generated.
     */
    protected abstract void generateChunk(ChunkCoordinate currentChunk, ChunkCoordinate generatingChunk, byte[] blocks);
}
