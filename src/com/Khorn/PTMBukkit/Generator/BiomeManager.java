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
    private BiomeCache Cache = new BiomeCache(this);
    private ArrayList<BiomeBase> f = new ArrayList<BiomeBase>();

    private WorldConfig worldConfig;


    public BiomeManager(World paramWorld, WorldConfig config)
    {
        this.f.add(BiomeBase.FOREST);
        this.f.add(BiomeBase.SWAMPLAND);
        this.f.add(BiomeBase.TAIGA);
        this.worldConfig = config;

        this.InitLayers(paramWorld.getSeed());

    }

    private void InitLayers(long paramLong)
    {
        GenLayer LandLayer = new LayerIsland(1L);
        LandLayer = new GenLayerZoomFuzzy(2000L, LandLayer);

        for (int s = 1; s < this.worldConfig.landSize; s++)
        {
            LandLayer = new GenLayerIsland(s, LandLayer);
            LandLayer = new GenLayerZoom(2000 + s, LandLayer);
        }


        int i = this.worldConfig.biomeSize;

        GenLayer RiverLayer = LandLayer;
        if (this.worldConfig.riversEnabled)
        {

            RiverLayer = GenLayerZoom.a(1000L, RiverLayer, 0);
            RiverLayer = new GenLayerRiverInit(100L, RiverLayer);
            RiverLayer = GenLayerZoom.a(1000L, RiverLayer, i + 2);
            RiverLayer = new GenLayerRiver(1L, RiverLayer);
            RiverLayer = new GenLayerSmooth(1000L, RiverLayer);
        }

        GenLayer BiomeLayer = LandLayer;
        BiomeLayer = GenLayerZoom.a(1000L, BiomeLayer, 0);
        BiomeLayer = new GenLayerBiomePTM(200L, BiomeLayer, this.worldConfig);
        BiomeLayer = GenLayerZoom.a(1000L, BiomeLayer, 2);

        GenLayer TemperatureLayer = new GenLayerTemperature(BiomeLayer);
        GenLayer DownfallLayer = new GenLayerDownfall(BiomeLayer);

        for (int j = 0; j < i; j++)
        {
            BiomeLayer = new GenLayerZoom(1000 + j, BiomeLayer);
            if (j == 0)
                BiomeLayer = new GenLayerIsland(3L, BiomeLayer);
            TemperatureLayer = new GenLayerSmoothZoom(1000 + j, TemperatureLayer);
            TemperatureLayer = new GenLayerTemperatureMix(TemperatureLayer, BiomeLayer, j);
            DownfallLayer = new GenLayerSmoothZoom(1000 + j, DownfallLayer);
            DownfallLayer = new GenLayerDownfallMix(DownfallLayer, BiomeLayer, j);
        }

        BiomeLayer = new GenLayerSmooth(1000L, BiomeLayer);

        if (this.worldConfig.riversEnabled)
            BiomeLayer = new GenLayerRiverMix(100L, BiomeLayer, RiverLayer);

        TemperatureLayer = GenLayerSmoothZoom.a(1000L, TemperatureLayer, 2);
        DownfallLayer = GenLayerSmoothZoom.a(1000L, DownfallLayer, 2);

        GenLayerZoomVoronoi ZoomVoronoiLayer = new GenLayerZoomVoronoi(10L, BiomeLayer);

        BiomeLayer.b(paramLong);
        TemperatureLayer.b(paramLong);
        DownfallLayer.b(paramLong);

        ZoomVoronoiLayer.b(paramLong);

        this.RiverLayer = BiomeLayer;
        this.VoronoiAlgoritmLayer = ZoomVoronoiLayer;
        this.TemperatureLayer = TemperatureLayer;
        this.DownfallLayer = DownfallLayer;
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
        return this.Cache.a(paramInt1, paramInt2);
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
            BiomeBase[] localObject = this.Cache.b(paramInt1, paramInt2);
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
        this.Cache.a();
    }
}

