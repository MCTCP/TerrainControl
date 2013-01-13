package com.khorn.terraincontrol.biomegenerators;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.biomelayers.layers.Layer;

/**
 * This is the normal biome mode, which has all of Terrain Control's features.
 *
 */
public class NormalBiomeGenerator extends BiomeGenerator
{
    private Layer unZoomedLayer;
    private Layer biomeLayer;
    
    public NormalBiomeGenerator(LocalWorld world, BiomeCache cache)
    {
        super(world, cache);
        
        Layer[] layers = Layer.Init(world.getSeed(), world);

        this.unZoomedLayer = layers[0];
        this.biomeLayer = layers[1];
    }
    
    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        if ((biomeArray == null) || (biomeArray.length < x_size * z_size))
        {
            biomeArray = new int[x_size * z_size];
        }

        int[] arrayOfInt = this.unZoomedLayer.Calculate(x, z, x_size, z_size);

        System.arraycopy(arrayOfInt, 0, biomeArray, 0, x_size * z_size);

        return biomeArray;
    }

    @Override
    public float[] getTemperatures(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size)
    {       
        if ((paramArrayOfFloat == null) || (paramArrayOfFloat.length < x_size * z_size))
        {
            paramArrayOfFloat = new float[x_size * z_size];
        }

        int[] arrayOfInt = this.biomeLayer.Calculate(x, z, x_size, z_size);
        for (int i = 0; i < x_size * z_size; i++)
        {
            float f1 = worldConfig.biomeConfigs[arrayOfInt[i]].getTemperature() / 65536.0F;
            if (f1 < worldConfig.minTemperature)
                f1 = worldConfig.minTemperature;
            if (f1 > worldConfig.maxTemperature)
                f1 = worldConfig.maxTemperature;
            paramArrayOfFloat[i] = f1;
        }

        return paramArrayOfFloat;
    }
    
    @Override
    public float[] getRainfall(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {  
        if ((paramArrayOfFloat == null) || (paramArrayOfFloat.length < paramInt3 * paramInt4))
        {
            paramArrayOfFloat = new float[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.biomeLayer.Calculate(paramInt1, paramInt2, paramInt3, paramInt4);
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            float f1 = worldConfig.biomeConfigs[arrayOfInt[i]].getWetness() / 65536.0F;
            if (f1 < worldConfig.minMoisture)
                f1 = worldConfig.minMoisture;
            if (f1 > worldConfig.maxMoisture)
                f1 = worldConfig.maxMoisture;
            paramArrayOfFloat[i] = f1;
        }

        return paramArrayOfFloat;
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        boolean useCache = true;
        if ((biomeArray == null) || (biomeArray.length < x_size * z_size))
        {
            biomeArray = new int[x_size * z_size];
            useCache = false;
        }

        if (useCache && (x_size == 16) && (z_size == 16) && ((x & 0xF) == 0) && ((z & 0xF) == 0))
        {
            synchronized (this.lockObject)
            {
                biomeArray = this.cache.getCachedBiomes(x, z);
            }
            return biomeArray;
        }

        int[] arrayOfInt = this.biomeLayer.Calculate(x, z, x_size, z_size);

        System.arraycopy(arrayOfInt, 0, biomeArray, 0, x_size * z_size);

        return biomeArray;

    }

    @Override
    public int getBiome(int x, int z)
    {
        return cache.getBiome(x, z);
    }

    @Override
    public void cleanupCache()
    {
        cache.cleanupCache();
    }
}
