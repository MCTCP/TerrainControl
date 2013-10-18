package com.khorn.terraincontrol.generator.terrainsgens;

import com.khorn.terraincontrol.LocalWorld;

import java.util.Random;

public class TerrainGenBase
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

    public void generate(int chunk_x, int chunk_z, byte[] paramArrayOfByte)
    {
        int i = this.checkAreaSize;

        for (int x = chunk_x - i; x <= chunk_x + i; x++)
            for (int z = chunk_z - i; z <= chunk_z + i; z++)
            {
                long l3 = x * worldLong1;
                long l4 = z * worldLong2;
                this.random.setSeed(l3 ^ l4 ^ this.world.getSeed());
                generateChunk(x, z, chunk_x, chunk_z, paramArrayOfByte);
            }
    }

    protected void generateChunk(int chunk_x, int chunk_z, int real_chunk_x, int real_chunk_z, byte[] paramArrayOfByte)
    {
    }
}
