package com.pg85.otg.generator.terrain;

import com.pg85.otg.LocalMaterialData;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;

import java.util.Random;

public abstract class TerrainGenBase
{
    // Hardcoded materials that cannot be changed for now
    protected final LocalMaterialData air = OTG.toLocalMaterialData(DefaultMaterial.AIR, 0);
    protected final LocalMaterialData lava = OTG.toLocalMaterialData(DefaultMaterial.STATIONARY_LAVA, 0);

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

    public void generate(ChunkBuffer chunkBuffer)
    {
        int i = this.checkAreaSize;
        ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();
        int chunkX = chunkCoord.getChunkX();
        int chunkZ = chunkCoord.getChunkZ();

        for (int x = chunkX - i; x <= chunkX + i; x++)
            for (int z = chunkZ - i; z <= chunkZ + i; z++)
            {
                long l3 = x * worldLong1;
                long l4 = z * worldLong2;
                this.random.setSeed(l3 ^ l4 ^ this.world.getSeed());
                generateChunk(ChunkCoordinate.fromChunkCoords(x, z), chunkBuffer);
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
    protected abstract void generateChunk(ChunkCoordinate currentChunk, ChunkBuffer generatingChunkBuffer);
}
