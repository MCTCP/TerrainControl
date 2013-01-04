package com.khorn.terraincontrol.generator.terrainsgens;

import com.khorn.terraincontrol.LocalWorld;

import java.util.Random;

public class TerrainGenBase
{
    protected int b = 8;
    protected Random c = new Random();
    protected LocalWorld d;

    public TerrainGenBase(LocalWorld world)
    {
        this.d = world;
    }

    public void a(int chunk_x, int chunk_z, byte[] paramArrayOfByte)
    {
        int i = this.b;

        this.c.setSeed(this.d.getSeed());
        long l1 = this.c.nextLong();
        long l2 = this.c.nextLong();

        for (int j = chunk_x - i; j <= chunk_x + i; j++)
            for (int k = chunk_z - i; k <= chunk_z + i; k++)
            {
                long l3 = j * l1;
                long l4 = k * l2;
                this.c.setSeed(l3 ^ l4 ^ this.d.getSeed());
                a(j, k, chunk_x, chunk_z, paramArrayOfByte);
            }
    }

    protected void a(int paramInt1, int paramInt2, int chunk_x, int chunk_z, byte[] paramArrayOfByte)
    {
    }
}
