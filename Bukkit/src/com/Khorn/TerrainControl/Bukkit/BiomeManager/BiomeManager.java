package com.Khorn.TerrainControl.Bukkit.BiomeManager;


import com.Khorn.TerrainControl.BiomeLayers.Layers.Layer;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.DefaultBiome;
import com.Khorn.TerrainControl.LocalWorld;
import net.minecraft.server.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BiomeManager extends WorldChunkManager implements IBiomeManager
{
    private Layer UnZoomedLayer;
    private Layer BiomeLayer;
    private Layer TemperatureLayer;
    private Layer DownfallLayer;
    private BiomeCache Cache = new BiomeCache(this);
    private final Object LockObject = new Object();
    private ArrayList<BiomeBase> f = new ArrayList<BiomeBase>();
    private float[] buffer;


    private WorldConfig worldConfig;


    public BiomeManager(LocalWorld world)
    {
        this.f.add(BiomeBase.FOREST);
        this.f.add(BiomeBase.SWAMPLAND);
        this.f.add(BiomeBase.TAIGA);

        this.Init(world);

    }

    public void Init(LocalWorld world)
    {
        this.worldConfig = world.getSettings();
        synchronized (this.LockObject)
        {
            this.Cache = new BiomeCache(this);
        }

        Layer[] layers = Layer.Init(world.getSeed(), world);

        this.UnZoomedLayer = layers[0];
        this.BiomeLayer = layers[1];
        this.TemperatureLayer = layers[2];
        this.DownfallLayer = layers[3];
    }

    @SuppressWarnings("rawtypes")
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
        synchronized (this.LockObject)
        {
            return this.Cache.b(paramInt1, paramInt2);
        }
    }


    public float[] getWetness(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramArrayOfFloat == null) || (paramArrayOfFloat.length < paramInt3 * paramInt4))
        {
            paramArrayOfFloat = new float[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.DownfallLayer.Calculate(paramInt1, paramInt2, paramInt3, paramInt4);
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
        synchronized (this.LockObject)
        {
            return a(this.Cache.c(i, i2), i1);
        }
    }

    @Override
    public float[] a(int i, int i1, int i2, int i3)
    {
        this.buffer = getTemperatures(this.buffer, i, i1, i2, i3);
        return this.buffer;
    }

    public float[] getTemperatures(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramArrayOfFloat == null) || (paramArrayOfFloat.length < paramInt3 * paramInt4))
        {
            paramArrayOfFloat = new float[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.TemperatureLayer.Calculate(paramInt1, paramInt2, paramInt3, paramInt4);
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
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < paramInt3 * paramInt4))
        {
            paramArrayOfBiomeBase = new BiomeBase[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.UnZoomedLayer.Calculate(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = BiomeBase.a[arrayOfInt[i]];
        }

        return paramArrayOfBiomeBase;
    }

    public BiomeBase[] a(BiomeBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean)
    {
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < paramInt3 * paramInt4))
        {
            paramArrayOfBiomeBase = new BiomeBase[paramInt3 * paramInt4];
        }

        if ((paramBoolean) && (paramInt3 == 16) && (paramInt4 == 16) && ((paramInt1 & 0xF) == 0) && ((paramInt2 & 0xF) == 0))
        {
            synchronized (this.LockObject)
            {
                BiomeBase[] localObject = this.Cache.d(paramInt1, paramInt2);
                System.arraycopy(localObject, 0, paramArrayOfBiomeBase, 0, paramInt3 * paramInt4);
            }
            return paramArrayOfBiomeBase;
        }
        int[] localObject = this.BiomeLayer.Calculate(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = BiomeBase.a[localObject[i]];
        }

        return paramArrayOfBiomeBase;
    }

    @SuppressWarnings("rawtypes")
    public boolean a(int paramInt1, int paramInt2, int paramInt3, List paramList)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        int[] arrayOfInt = this.UnZoomedLayer.Calculate(i, j, n, i1);
        for (int i2 = 0; i2 < n * i1; i2++)
        {
            if (arrayOfInt[i2] >= DefaultBiome.values().length)
                return false;
            BiomeBase localBiomeBase = BiomeBase.a[arrayOfInt[i2]];
            if (!paramList.contains(localBiomeBase))
                return false;
        }

        return true;
    }

    @SuppressWarnings("rawtypes")
    public ChunkPosition a(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        int[] arrayOfInt = this.UnZoomedLayer.Calculate(i, j, n, i1);
        ChunkPosition localChunkPosition = null;
        int i2 = 0;
        for (int i3 = 0; i3 < arrayOfInt.length; i3++)
        {
            if (arrayOfInt[i3] >= DefaultBiome.values().length)
                continue;
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
        synchronized (this.LockObject)
        {
            this.Cache.a();
        }
    }

    public int[] getBiomesUnZoomedTC(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if ((biomeArray == null) || (biomeArray.length < x_size * z_size))
        {
            biomeArray = new int[x_size * z_size];
        }

        int[] arrayOfInt = this.UnZoomedLayer.Calculate(x,  z,  x_size,  z_size);

        System.arraycopy(arrayOfInt,0,arrayOfInt,0,x_size * z_size);

        return biomeArray;
    }

    public float[] getTemperaturesTC(int x, int z, int x_size, int z_size)
    {
        return this.getTemperatures(null,x,z,x_size,z_size);
    }

    public int[] getBiomesTC(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if ((biomeArray == null) || (biomeArray.length < x_size * z_size))
        {
            biomeArray = new int[x_size * z_size];
        }

        if ((x_size == 16) && (z_size == 16) && ((x & 0xF) == 0) && ((z & 0xF) == 0))
        {
            synchronized (this.LockObject)
            {
                BiomeBase[] localObject = this.Cache.d(x, z);
                for(int i= 0; i< x_size*z_size;i++)
                    biomeArray[i] = localObject[i].K;

            }
            return biomeArray;
        }

        int[] arrayOfInt = this.BiomeLayer.Calculate(x,  z,  x_size,  z_size);

        System.arraycopy(arrayOfInt,0,arrayOfInt,0,x_size * z_size);

        return biomeArray;

    }

    public int getBiomeTC(int x, int z)
    {
        return this.getBiome(x,z).K;
    }
}

