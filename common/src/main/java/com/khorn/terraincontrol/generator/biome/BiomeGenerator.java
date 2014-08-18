package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;

/**
 * The biome generator. External plugins are allowed to implement this class
 * on their own as long as it is registered using
 * {@link BiomeModeManager#register(String, Class)}.
 *
 * <p>
 * A biome generator doesn't have to implement all methods in this class. If
 * it can't generate biomes at a lower precision, {@link
 * #getBiomesUnZoomed(int[], int, int, int, int, OutputType)} doesn't have to
 * be overridden. If it doesn't have its own cache, {@link #cleanupCache()}
 * and {@link #getBiome(int, int)} also don't have to be overridden.
 *
 * <p>
 * Two methods exist to detect whether the generator supports {@link
 * #canGenerateUnZoomed() unzoomed biomes} and {@link #isCached() cached
 * lookups}. By default, both methods return {@code false}. If the biome
 * generator does support those features the methods must be overridden.
 */
public abstract class BiomeGenerator
{
    protected final LocalWorld world;

    public BiomeGenerator(LocalWorld world)
    {
        this.world = world;
    }

    /**
     * Calculates the biome ids array used in terrain generation.
     * 
     * @param biomeArray Output array. If it is null or wrong size return new
     *            array.
     * @param x The block x.
     * @param z The block z.
     * @param xSize Size of block in x coordinate.
     * @param zSize Size of blocks in z coordinate.
     * @param type Output type.
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

    /**
     * Gets the biome of a single column. Only available for cached biome
     * generators, as the method would be way too slow otherwise.
     * @param blockX X coord of the column.
     * @param blockZ Z coord of the column.
     * @return The biome ud.
     * @throws UnsupportedOperationException If {@link #isCached()} == false.
     */
    public int getBiome(int blockX, int blockZ) throws UnsupportedOperationException
    {
        if (isCached())
        {
            // Implementation has a bug
            throw new AssertionError("isCached() == true, but getBiome is not overridden");
        } else
        {
            throw new UnsupportedOperationException("isCached() == false, so no single biome lookups");
        }
    }

    /**
     * Cleans up the cache.
     * @throws UnsupportedOperationException If {@link #isCached()} == false.
     */
    public void cleanupCache() throws UnsupportedOperationException
    {
        if (isCached())
        {
            // Implementation has a bug
            throw new AssertionError("isCached() == true, but cleanupCache is not overridden");
        } else
        {
            throw new UnsupportedOperationException("isCached() == false, so no cache to cleanup");
        }
    }

    public boolean canGenerateUnZoomed()
    {
        return false;
    }

    /**
     * Gets whether this biome generator is cached. Cached biome generators
     * have an implementation for {@link #getBiome(int, int)} and {@link #cleanupCache()}.
     * @return True if this biome generator is cached, false otherwise.
     */
    public boolean isCached()
    {
        return false;
    }

}