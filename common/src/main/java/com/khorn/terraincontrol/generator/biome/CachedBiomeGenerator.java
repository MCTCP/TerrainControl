package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Wraps uncached biome generators.
 * @see BiomeModeManager#createCached(Class, LocalWorld)
 */
class CachedBiomeGenerator extends BiomeGenerator
{
    /**
     * Caches the biomes of a single chunk.
     *
     */
    private static class Block
    {
        /**
         * The array of biome types stored in this BiomeCache.Block.
         */
        private int[] biomes = new int[ChunkCoordinate.CHUNK_X_SIZE * ChunkCoordinate.CHUNK_Z_SIZE];
        /**
         * The last time this BiomeCacheBlock was accessed, in milliseconds.
         */
        private long lastAccessTime;

        Block(BiomeGenerator generator, ChunkCoordinate chunkCoord)
        {
            biomes = generator.getBiomes(biomes, chunkCoord.getBlockX(), chunkCoord.getBlockZ(), ChunkCoordinate.CHUNK_X_SIZE,
                    ChunkCoordinate.CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);
        }

        /**
         * Gets the biome type id of the column at the given location.
         * @param blockX X location of the column, must fall in this cache
         *               block.
         * @param blockZ Z location of the column, must fall in this cache
         *               block.
         * @return The biome type id.
         */
        int getCalculatedBiomeId(int blockX, int blockZ)
        {
            return biomes[blockX & 15 | (blockZ & 15) << 4];
        }
    }

    /**
     * The map of cached BiomeCacheBlocks.
     */
    private Map<ChunkCoordinate, CachedBiomeGenerator.Block> cacheMap = new HashMap<ChunkCoordinate, Block>();
    /**
     * The uncached biome generator.
     */
    private final BiomeGenerator generator;
    /**
     * The last time this BiomeCache was cleaned, in milliseconds.
     */
    private long lastCleanupTime;

    private CachedBiomeGenerator(BiomeGenerator generator)
    {
        super(generator.world);
        this.generator = generator;
    }

    /**
     * Gets a cached generator that generates biomes like the given generator.
     * If the given generator is already cached, it is returned immediately.
     * If it isn't cached it is wrapped inside a {@link CachedBiomeGenerator}.
     * @param generator A potentially uncached biome generator.
     * @return A cached biome generator.
     * @see BiomeModeManager#createCached(Class, LocalWorld)
     */
    static BiomeGenerator makeCached(BiomeGenerator generator)
    {
        if (generator.isCached())
        {
            return generator;
        }
        return new CachedBiomeGenerator(generator);
    }

    @Override
    public void cleanupCache()
    {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCleanup = currentTime - this.lastCleanupTime;

        if (timeSinceLastCleanup < 7500L && timeSinceLastCleanup > 0L)
        {
            return;
        }

        this.lastCleanupTime = currentTime;

        for (Iterator<Entry<ChunkCoordinate, CachedBiomeGenerator.Block>> it = cacheMap.entrySet().iterator(); it.hasNext();)
        {
            Entry<ChunkCoordinate, CachedBiomeGenerator.Block> entry = it.next();
            CachedBiomeGenerator.Block block = entry.getValue();
            long timeSinceLastAccessed = currentTime - block.lastAccessTime;

            if (timeSinceLastAccessed > 30000L || timeSinceLastAccessed < 0L)
            {
                it.remove();
            }
        }

    }

    @Override
    public int getBiome(int x, int z)
    {
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
        CachedBiomeGenerator.Block cacheBlock = getBiomeCacheBlock(chunkCoord);
        return cacheBlock.getCalculatedBiomeId(x, z);
    }

    /**
     * Returns a biome cache block at location specified.
     * @param chunkCoord The chunk to get the cache entry for.
     * @return The biome cache block.
     */
    private CachedBiomeGenerator.Block getBiomeCacheBlock(ChunkCoordinate chunkCoord)
    {
        CachedBiomeGenerator.Block block = this.cacheMap.get(chunkCoord);

        if (block == null)
        {
            block = new CachedBiomeGenerator.Block(generator, chunkCoord);
            this.cacheMap.put(chunkCoord, block);
        }

        block.lastAccessTime = System.currentTimeMillis();
        return block;
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int xSize, int zSize, OutputType type)
    {
        if (xSize == ChunkCoordinate.CHUNK_X_SIZE && zSize == ChunkCoordinate.CHUNK_Z_SIZE && (x & 0xF) == 0 && (z & 0xF) == 0)
        {
            if (biomeArray == null || biomeArray.length < xSize * zSize)
            {
                biomeArray = new int[xSize * zSize];
            }
            int[] cachedBiomes = getCachedBiomes(ChunkCoordinate.fromBlockCoords(x, z));
            // Avoid leaking references to the cached array - Minecraft likes
            // to change those arrays, corrupting the cache
            System.arraycopy(cachedBiomes, 0, biomeArray, 0, xSize * zSize);
            return biomeArray;
        }
        return generator.getBiomes(biomeArray, x, z, xSize, zSize, type);
    }

    @Override
    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int xSize, int zSize, OutputType type)
    {
        return generator.getBiomesUnZoomed(biomeArray, x, z, xSize, zSize, type);
    }

    @Override
    public boolean canGenerateUnZoomed()
    {
        return generator.canGenerateUnZoomed();
    }

    /**
     * Returns the array of cached biome types in the BiomeCacheBlock at the
     * given location.
     * @param chunkCoord The chunk to get cached biomes for.
     * @return The biomes.
     */
    public int[] getCachedBiomes(ChunkCoordinate chunkCoord)
    {
        return this.getBiomeCacheBlock(chunkCoord).biomes;
    }

    @Override
    public boolean isCached()
    {
        return true;
    }

    @Override
    public BiomeGenerator unwrap()
    {
        return generator.unwrap();
    }
}
