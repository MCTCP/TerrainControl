package com.pg85.otg.gen.biome;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;

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
    private static class Chunk
    {
        /**
         * The array of biome types stored in this BiomeCache.Block.
         */
        private int[] biomes = new int[ChunkCoordinate.CHUNK_SIZE * ChunkCoordinate.CHUNK_SIZE];

        Chunk(BiomeGenerator generator, ChunkCoordinate chunkCoord)
        {
            biomes = generator.getBiomes(biomes, chunkCoord.getBlockX(), chunkCoord.getBlockZ(), ChunkCoordinate.CHUNK_SIZE,
                    ChunkCoordinate.CHUNK_SIZE, OutputType.DEFAULT_FOR_WORLD);
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
    private FifoMap<ChunkCoordinate, CachedBiomeGenerator.Chunk> cacheMap = new FifoMap<ChunkCoordinate, Chunk>(4096); // TODO: This gets slow at large sizes, test/profile to find the best size.
    /**
     * The uncached biome generator.
     */
    private final BiomeGenerator generator;

    public CachedBiomeGenerator(BiomeGenerator generator)
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
    public void cleanupCache() { }

    @Override
    public int getBiome(int x, int z)
    {
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
        CachedBiomeGenerator.Chunk cacheBlock = getBiomeCacheChunk(chunkCoord);
        return cacheBlock.getCalculatedBiomeId(x, z);
    }

    /**
     * Returns a biome cache block at location specified.
     * @param chunkCoord The chunk to get the cache entry for.
     * @return The biome cache block.
     */
    private CachedBiomeGenerator.Chunk getBiomeCacheChunk(ChunkCoordinate chunkCoord)
    {
        CachedBiomeGenerator.Chunk chunk = this.cacheMap.get(chunkCoord);

        if (chunk == null)
        {
        	chunk = new CachedBiomeGenerator.Chunk(generator, chunkCoord);
            this.cacheMap.put(chunkCoord, chunk);
        }

        return chunk;
    }

    @Override
    public int[] getBiomes(int[] biomeArray, int x, int z, int xSize, int zSize, OutputType type)
    {
        if (xSize == ChunkCoordinate.CHUNK_SIZE && zSize == ChunkCoordinate.CHUNK_SIZE && (x & 0xF) == 0 && (z & 0xF) == 0)
        {
            if (biomeArray == null || biomeArray.length < xSize * zSize)
            {
                biomeArray = new int[xSize * zSize];
            }
            int[] cachedBiomes = getCachedBiomes(ChunkCoordinate.fromBlockCoords(x, z));
            // Avoid leaking references to the cached array - Minecraft likes
            // to change those arrays, corrupting the cache.
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
    private int[] getCachedBiomes(ChunkCoordinate chunkCoord)
    {
        return this.getBiomeCacheChunk(chunkCoord).biomes;
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
