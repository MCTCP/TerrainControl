package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfigFile;

/**
 * The biome generator. External plugins are allowed to implement this class on their own.
 *
 */
public abstract class BiomeGenerator
{
    protected WorldConfigFile worldConfig;
    protected BiomeCache cache;
    public final Object lockObject = new Object();

    public BiomeGenerator(LocalWorld world, BiomeCache cache)
    {
        this.worldConfig = world.getSettings();
        this.cache = cache;
    }

    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int xSize, int zSize, OutputType type)
    {
        // Fall back on getBiomes
        // When overriding this method to actually generate unzoomed biomes,
        // make sure to also override canGenerateUnZoomed so that it returns
        // true.
        return getBiomes(biomeArray, x, z, xSize, zSize, type);
    }

    public abstract float[] getTemperatures(float[] paramArrayOfFloat, int x, int z, int xSize, int zSize);

    public abstract float[] getRainfall(float[] paramArrayOfFloat, int x, int z, int xSize, int zSize);

    public abstract int[] getBiomes(int[] biomeArray, int x, int z, int xSize, int zSize, OutputType type);

    public abstract int getBiome(int x, int z);

    public abstract void cleanupCache();

    public boolean canGenerateUnZoomed()
    {
        return false;
    }

}