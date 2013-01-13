package com.khorn.terraincontrol.biomegenerators;


import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;

public abstract class BiomeGenerator
{
    protected WorldConfig worldConfig;
    protected BiomeCache cache;
    public final Object lockObject = new Object();
    
    public BiomeGenerator(LocalWorld world, BiomeCache cache)
    {
        this.worldConfig = world.getSettings();
        this.cache = cache;
    }
    
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int xSize, int zSize)
    {
        // Fall back on getBiomes
        return getBiomes(biomeArray, x, z, xSize, zSize);
    }

    public abstract float[] getTemperatures(float[] paramArrayOfFloat, int x, int z, int xSize, int zSize);
    
    public abstract float[] getRainfall(float[] paramArrayOfFloat, int x, int z, int xSize, int zSize);

    public abstract int[] getBiomes(int[] biomeArray, int x, int z, int xSize, int zSize);

    public abstract int getBiome(int x, int z);
    
    public abstract void cleanupCache();
}