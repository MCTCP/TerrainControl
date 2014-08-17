package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;

/**
 * The biome generator. External plugins are allowed to implement this class on their own.
 *
 */
public abstract class BiomeGenerator
{
    protected LocalWorld world;
    protected BiomeCache cache;
    public final Object lockObject = new Object();

    public BiomeGenerator(LocalWorld world, BiomeCache cache)
    {
        this.world = world;
        this.cache = cache;
    }

    /**
     * Calculate biome ids array used in terrain generation.
     * 
     * @param biomeArray Output array. If it is null or wrong size return new
     *            array.
     * @param x The block x.
     * @param z The block z.
     * @param x_size Size of block in x coordinate.
     * @param z_size Size of blocks in z coordinate.
     * @param type Output type. May be FULL, WITHOUT_RIVERS, ONLY_RIVERS or
     *            DEFAULT_FOR_WORLD.
     * @return Array filled by biome ids.
     */
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int xSize, int zSize, OutputType type)
    {
        // Fall back on getBiomes
        // When overriding this method to actually generate unzoomed biomes,
        // make sure to also override canGenerateUnZoomed so that it returns
        // true.
        return getBiomes(biomeArray, x, z, xSize, zSize, type);
    }

    public abstract float[] getRainfall(float[] paramArrayOfFloat, int x, int z, int xSize, int zSize);

    public abstract int[] getBiomes(int[] biomeArray, int x, int z, int xSize, int zSize, OutputType type);

    public abstract int getBiome(int x, int z);

    public abstract void cleanupCache();

    public boolean canGenerateUnZoomed()
    {
        return false;
    }

}