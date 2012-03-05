package com.Khorn.TerrainControl.Generator.TerrainsGens;

import com.Khorn.TerrainControl.LocalWorld;

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

    public void a(  int paramInt1, int paramInt2, byte[] paramArrayOfByte)
    {
        int i = this.b;

        this.c.setSeed(this.d.getSeed());
        long l1 = this.c.nextLong();
        long l2 = this.c.nextLong();

        for (int j = paramInt1 - i; j <= paramInt1 + i; j++)
            for (int k = paramInt2 - i; k <= paramInt2 + i; k++)
            {
                long l3 = j * l1;
                long l4 = k * l2;
                this.c.setSeed(l3 ^ l4 ^ this.d.getSeed());
                a(j, k, paramInt1, paramInt2, paramArrayOfByte);
            }
    }

    protected void a(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte)
    {
    }
}
