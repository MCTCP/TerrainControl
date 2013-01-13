package com.khorn.terraincontrol.biomegenerators;


import com.khorn.terraincontrol.LocalWorld;


/**
 * Dummy class. Handled by special if-statements in the code.
 *
 */
public class VanillaBiomeGenerator extends BiomeGenerator
{

    public VanillaBiomeGenerator(LocalWorld world, BiomeCache cache)
    {
        super(world, cache);
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        return null;
    }

    @Override
    public float[] getTemperatures(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size)
    {
        return null;
    }

    @Override
    public float[] getRainfall(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size)
    {
        return null;
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        return null;
    }

    @Override
    public int getBiome(int x, int z)
    {
        return 0;
    }

    @Override
    public void cleanupCache()
    {
        
    }
    
}
