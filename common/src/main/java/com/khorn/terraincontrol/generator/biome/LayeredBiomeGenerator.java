package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.generator.biome.layers.Layer;

/**
 * Skeletal implementation for biome modes that use the {@link Layer} class to
 * generate biomes.
 *
 */
public abstract class LayeredBiomeGenerator extends BiomeGenerator
{
    private Layer unZoomedLayer;
    private Layer biomeLayer;
    private OutputType defaultOutputType = OutputType.FULL;

    public LayeredBiomeGenerator(LocalWorld world)
    {
        super(world);

        Layer[] layers = initLayers();

        if (world.getConfigs().getWorldConfig().improvedRivers)
            defaultOutputType = OutputType.WITHOUT_RIVERS;

        this.unZoomedLayer = layers[0];
        this.biomeLayer = layers[1];
    }

    /**
     * Gets an array consisting of two elements. The first element (index 0)
     * is the unzoomed layer, the second element is the zoomed layer. This
     * method is called once from the constructor. No other invocations are
     * allowed.
     * @return The two layers.
     */
    protected abstract Layer[] initLayers();

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
        int[] arrayOfInt = this.unZoomedLayer.getInts(cache, x, z, x_size, z_size);
        ArraysCacheManager.ReleaseCache(cache);

        System.arraycopy(arrayOfInt, 0, biomeArray, 0, x_size * z_size);

        return biomeArray;
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
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
        int[] arrayOfInt = this.biomeLayer.getInts(cache, x, z, x_size, z_size);
        ArraysCacheManager.ReleaseCache(cache);

        System.arraycopy(arrayOfInt, 0, biomeArray, 0, x_size * z_size);

        return biomeArray;
    }

    @Override
    public boolean canGenerateUnZoomed()
    {
        return true;
    }

}
