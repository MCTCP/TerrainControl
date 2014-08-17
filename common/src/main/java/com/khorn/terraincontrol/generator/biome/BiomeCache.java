package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Caches biomes to speed up the biome generator.
 *
 */
public class BiomeCache
{
    /** Reference to the world */
    private final LocalWorld world;
    /** The last time this BiomeCache was cleaned, in milliseconds. */
    private long lastCleanupTime;
    /**
     * The map of cached BiomeCacheBlocks.
     */
    private Map<ChunkCoordinate, BiomeCache.Block> cacheMap = new HashMap<ChunkCoordinate, Block>();

    public BiomeCache(LocalWorld world)
    {
        this.world = world;
    }

    /**
     * Returns a biome cache block at location specified.
     */
    private BiomeCache.Block getBiomeCacheBlock(ChunkCoordinate chunkCoord)
    {
        BiomeCache.Block block = (BiomeCache.Block) this.cacheMap.get(chunkCoord);

        if (block == null)
        {
            block = new BiomeCache.Block(world.getBiomeGenerator(), chunkCoord);
            this.cacheMap.put(chunkCoord, block);
        }

        block.lastAccessTime = System.currentTimeMillis();
        return block;
    }

    /**
     * Gets the id of the biome at the given location.
     *
     * @param blockX The x position of the block.
     * @param blockZ The z position of the block.
     * @return The id of the biome.
     */
    public int getCalculatedBiomeId(int blockX, int blockZ) {
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(blockX, blockZ);
        BiomeCache.Block cacheBlock = getBiomeCacheBlock(chunkCoord);
        return cacheBlock.getCalculatedBiomeId(blockX, blockZ);
    }

    /**
     * Removes BiomeCacheBlocks from this cache that haven't been accessed in at
     * least 30 seconds.
     */
    public void cleanupCache()
    {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCleanup = currentTime - this.lastCleanupTime;

        if (timeSinceLastCleanup < 7500L && timeSinceLastCleanup > 0L)
        {
            return;
        }

        this.lastCleanupTime = currentTime;

        for (Iterator<Entry<ChunkCoordinate, BiomeCache.Block>> it = cacheMap.entrySet().iterator(); it.hasNext();)
        {
            Entry<ChunkCoordinate, BiomeCache.Block> entry = it.next();
            BiomeCache.Block block = entry.getValue();
            long timeSinceLastAccessed = currentTime - block.lastAccessTime;

            if (timeSinceLastAccessed > 30000L || timeSinceLastAccessed < 0L)
            {
                it.remove();
            }
        }

    }

    /**
     * Returns the array of cached biome types in the BiomeCacheBlock at the
     * given location.
     */
    public int[] getCachedBiomes(ChunkCoordinate chunkCoord)
    {
        return this.getBiomeCacheBlock(chunkCoord).biomes;
    }

    /**
     * Caches the biomes of a single chunk.
     *
     */
    private static class Block
    {
        /**
         * The array of biome types stored in this BiomeCache.Block.
         */
        int[] biomes = new int[256];
        /**
         * The last time this BiomeCacheBlock was accessed, in milliseconds.
         */
        long lastAccessTime;

        Block(BiomeGenerator generator, ChunkCoordinate chunkCoord)
        {
            biomes = generator.getBiomes(null, chunkCoord.getBlockX(), chunkCoord.getBlockZ(), ChunkCoordinate.CHUNK_X_SIZE, ChunkCoordinate.CHUNK_Z_SIZE, OutputType.DEFAULT_FOR_WORLD);
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
}