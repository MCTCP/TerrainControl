package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;

/**
 * Dummy class. Handled by special if-statements in the 
 * implementations of LocalWorld.
 *
 */
public class VanillaBiomeGenerator extends BiomeGenerator
{

    public VanillaBiomeGenerator(LocalWorld world, BiomeCache cache)
    {
        super(world, cache);
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        throw new UnsupportedOperationException("Dummy class");
    }

    @Override
    public float[] getRainfall(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size)
    {
        throw new UnsupportedOperationException("Dummy class");
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size, OutputType outputType)
    {
        throw new UnsupportedOperationException("Dummy class");
    }

    @Override
    public int getBiome(int x, int z)
    {
        throw new UnsupportedOperationException("Dummy class");
    }

    @Override
    public void cleanupCache()
    {

    }

    @Override
    public boolean canGenerateUnZoomed()
    {
        throw new UnsupportedOperationException("Dummy class");
    }

}
