package com.Khorn.PTMBukkit.Generator;


import com.Khorn.PTMBukkit.WorldConfig;
import net.minecraft.server.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BiomeManager extends WorldChunkManager
{
    private GenLayer RiverLayer;
    private GenLayer VoronoiAlgoritmLayer;
    private GenLayer TemperatureLayer;
    private GenLayer DownfallLayer;
    private BiomeCache e = new BiomeCache(this);
    private List f;


    public BiomeManager(World paramWorld, WorldConfig config)
    {
        this.f = new ArrayList();
        this.f.add(BiomeBase.FOREST);
        this.f.add(BiomeBase.SWAMPLAND);
        this.f.add(BiomeBase.TAIGA);

        GenLayer[] arrayOfGenLayer = GenLayer.a(paramWorld.getSeed());
        this.RiverLayer = arrayOfGenLayer[0];
        this.VoronoiAlgoritmLayer = arrayOfGenLayer[1];
        this.TemperatureLayer = arrayOfGenLayer[2];
        this.DownfallLayer = arrayOfGenLayer[3];
    }

    public List a()
    {
        return this.f;
    }

    public BiomeBase a(ChunkCoordIntPair paramChunkCoordIntPair)
    {
        return getBiome(paramChunkCoordIntPair.x << 4, paramChunkCoordIntPair.z << 4);
    }

    public BiomeBase getBiome(int paramInt1, int paramInt2)
    {
        return this.e.a(paramInt1, paramInt2);
    }


    public float[] getWetness(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        IntCache.a();
        if ((paramArrayOfFloat == null) || (paramArrayOfFloat.length < paramInt3 * paramInt4))
        {
            paramArrayOfFloat = new float[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.DownfallLayer.a(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            float f1 = arrayOfInt[i] / 65536.0F;
            if (f1 > 1.0F)
                f1 = 1.0F;
            paramArrayOfFloat[i] = f1;
        }

        return paramArrayOfFloat;
    }

    public float[] getTemperatures(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        IntCache.a();
        if ((paramArrayOfFloat == null) || (paramArrayOfFloat.length < paramInt3 * paramInt4))
        {
            paramArrayOfFloat = new float[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.TemperatureLayer.a(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            float f1 = arrayOfInt[i] / 65536.0F;
            if (f1 > 1.0F)
                f1 = 1.0F;
            paramArrayOfFloat[i] = f1;
        }

        return paramArrayOfFloat;
    }

    public BiomeBase[] getBiomes(BiomeBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        IntCache.a();
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < paramInt3 * paramInt4))
        {
            paramArrayOfBiomeBase = new BiomeBase[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.RiverLayer.a(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = BiomeBase.a[arrayOfInt[i]];
        }

        return paramArrayOfBiomeBase;
    }

    public BiomeBase[] a(BiomeBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        return a(paramArrayOfBiomeBase, paramInt1, paramInt2, paramInt3, paramInt4, true);
    }

    public BiomeBase[] a(BiomeBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean)
    {
        IntCache.a();
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < paramInt3 * paramInt4))
        {
            paramArrayOfBiomeBase = new BiomeBase[paramInt3 * paramInt4];
        }

        if ((paramBoolean) && (paramInt3 == 16) && (paramInt4 == 16) && ((paramInt1 & 0xF) == 0) && ((paramInt2 & 0xF) == 0))
        {
            BiomeBase[] localObject = this.e.b(paramInt1, paramInt2);
            System.arraycopy(localObject, 0, paramArrayOfBiomeBase, 0, paramInt3 * paramInt4);
            return paramArrayOfBiomeBase;
        }

        int[] localObject = this.VoronoiAlgoritmLayer.a(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = BiomeBase.a[localObject[i]];
        }

        return paramArrayOfBiomeBase;
    }

    public boolean a(int paramInt1, int paramInt2, int paramInt3, List paramList)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;

        int[] arrayOfInt = this.RiverLayer.a(i, j, n, i1);
        for (int i2 = 0; i2 < n * i1; i2++)
        {
            BiomeBase localBiomeBase = BiomeBase.a[arrayOfInt[i2]];
            if (!paramList.contains(localBiomeBase))
                return false;
        }

        return true;
    }

    public ChunkPosition a(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        int[] arrayOfInt = this.RiverLayer.a(i, j, n, i1);
        ChunkPosition localChunkPosition = null;
        int i2 = 0;
        for (int i3 = 0; i3 < arrayOfInt.length; i3++)
        {
            int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            BiomeBase localBiomeBase = BiomeBase.a[arrayOfInt[i3]];
            if ((!paramList.contains(localBiomeBase)) || ((localChunkPosition != null) && (paramRandom.nextInt(i2 + 1) != 0)))
                continue;
            localChunkPosition = new ChunkPosition(i4, 0, i5);
            i2++;
        }

        return localChunkPosition;
    }

    public void b()
    {
        this.e.a();
    }
}

