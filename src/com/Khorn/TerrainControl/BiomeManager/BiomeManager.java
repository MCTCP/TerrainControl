package com.Khorn.TerrainControl.BiomeManager;


import com.Khorn.TerrainControl.BiomeManager.Layers.Layer;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import net.minecraft.server.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BiomeManager extends WorldChunkManager
{
    private Layer UnZoomedLayer;
    private Layer BiomeLayer;
    private Layer TemperatureLayer;
    private Layer DownfallLayer;
    private BiomeCache Cache = new BiomeCache(this);
    private ArrayList<BiomeBase> f = new ArrayList<BiomeBase>();
    private float[] buffer;

    private WorldConfig worldConfig;


    public BiomeManager(World paramWorld, WorldConfig config)
    {
        this.f.add(BiomeBase.FOREST);
        this.f.add(BiomeBase.SWAMPLAND);
        this.f.add(BiomeBase.TAIGA);

        this.Init(paramWorld,config);

    }

    public void Init(World paramWorld, WorldConfig config)
    {
        this.worldConfig = config;
        this.Cache = new BiomeCache(this);

        Layer[] layers = Layer.a(paramWorld.getSeed(), config);

        this.UnZoomedLayer = layers[0];
        this.BiomeLayer = layers[1];
        this.TemperatureLayer = layers[2];
        this.DownfallLayer = layers[3];
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
        return this.Cache.b(paramInt1, paramInt2);
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
            if (f1 < this.worldConfig.minMoisture)
                f1 = this.worldConfig.minMoisture;
            if (f1 > this.worldConfig.maxMoisture)
                f1 = this.worldConfig.maxMoisture;
            paramArrayOfFloat[i] = f1;
        }

        return paramArrayOfFloat;
    }

    @Override
    public float a(int i, int i1, int i2)
    {
        return a(this.Cache.c(i, i2), i1);
    }

    @Override
    public float[] a(int i, int i1, int i2, int i3)
    {
        this.buffer = getTemperatures(this.buffer, i, i1, i2, i3);
        return this.buffer;
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
            if (f1 < this.worldConfig.minTemperature)
                f1 = this.worldConfig.minTemperature;
            if (f1 > this.worldConfig.maxTemperature)
                f1 = this.worldConfig.maxTemperature;
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

        int[] arrayOfInt = this.UnZoomedLayer.a(paramInt1, paramInt2, paramInt3, paramInt4);
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
            BiomeBase[] localObject = this.Cache.d(paramInt1, paramInt2);
            System.arraycopy(localObject, 0, paramArrayOfBiomeBase, 0, paramInt3 * paramInt4);
            return paramArrayOfBiomeBase;
        }

        int[] localObject = this.BiomeLayer.a(paramInt1, paramInt2, paramInt3, paramInt4);
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

        int[] arrayOfInt = this.UnZoomedLayer.a(i, j, n, i1);
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
        int[] arrayOfInt = this.UnZoomedLayer.a(i, j, n, i1);
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
        this.Cache.a();
    }
}

