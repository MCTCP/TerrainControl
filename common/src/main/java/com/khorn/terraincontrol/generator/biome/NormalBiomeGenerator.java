package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.biome.layers.Layer;

/**
 * This is the normal biome mode, which has all of Terrain Control's features.
 */
public class NormalBiomeGenerator extends BiomeGenerator
{
    private Layer unZoomedLayer;
    private Layer biomeLayer;
    private OutputType defaultOutputType = OutputType.FULL;

    public NormalBiomeGenerator(LocalWorld world, BiomeCache cache)
    {
        super(world, cache);

        Layer[] layers = Layer.Init(world.getSeed(), world);

        if (world.getSettings().worldConfig.improvedRivers)
            defaultOutputType = OutputType.WITHOUT_RIVERS;

        this.unZoomedLayer = layers[0];
        this.biomeLayer = layers[1];
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        if ((biomeArray == null) || (biomeArray.length < x_size * z_size))
        {
            biomeArray = new int[x_size * z_size];
        }
        ArraysCache cache = ArraysCacheManager.GetCache();
        if (outputType == OutputType.DEFAULT_FOR_WORLD)
            cache.outputType = defaultOutputType;
        else
            cache.outputType = outputType;
        int[] arrayOfInt = this.unZoomedLayer.GetBiomes(cache, x, z, x_size, z_size);
        ArraysCacheManager.ReleaseCache(cache);

        System.arraycopy(arrayOfInt, 0, biomeArray, 0, x_size * z_size);

        return biomeArray;
    }

    @Override
    public float[] getRainfall(float[] paramArrayOfFloat, int x, int y, int x_size, int z_size)
    {
        if ((paramArrayOfFloat == null) || (paramArrayOfFloat.length < x_size * z_size))
        {
            paramArrayOfFloat = new float[x_size * z_size];
        }
        ArraysCache cache = ArraysCacheManager.GetCache();
        cache.outputType = defaultOutputType;

        int[] arrayOfInt = this.biomeLayer.GetBiomes(cache, x, y, x_size, z_size);
        ArraysCacheManager.ReleaseCache(cache);
        WorldConfig worldConfig = world.getSettings().worldConfig;
        for (int i = 0; i < x_size * z_size; i++)
        {
            float f1 = world.getBiomeById(arrayOfInt[i]).getBiomeConfig().biomeWetness;
            if (f1 < worldConfig.minMoisture)
                f1 = (float) worldConfig.minMoisture;
            if (f1 > worldConfig.maxMoisture)
                f1 = (float) worldConfig.maxMoisture;
            paramArrayOfFloat[i] = f1;
        }

        return paramArrayOfFloat;
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
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
        ArraysCache cache = ArraysCacheManager.GetCache();
        if (outputType == OutputType.DEFAULT_FOR_WORLD)
            cache.outputType = defaultOutputType;
        else
            cache.outputType = outputType;
        int[] arrayOfInt = this.biomeLayer.GetBiomes(cache, x, z, x_size, z_size);
        ArraysCacheManager.ReleaseCache(cache);

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

    @Override
    public boolean canGenerateUnZoomed()
    {
        return true;
    }
}
