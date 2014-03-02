package com.khorn.terraincontrol.forge.generator;

import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class BiomeCacheWrapper implements com.khorn.terraincontrol.generator.biome.BiomeCache
{
    private BiomeCache handle;

    public BiomeCacheWrapper(WorldChunkManager manager)
    {
        this.handle = new BiomeCache(manager);
    }

    @Override
    public int getBiome(int x, int z)
    {
        return getId(handle.getBiomeGenAt(x, z));
    }

    private int getId(BiomeGenBase biome)
    {
        // Support for virtual biomes
        if (biome instanceof BiomeGenCustom)
        {
            return ((BiomeGenCustom) biome).generationId;
        }
        return biome.biomeID;
    }

    @Override
    public void cleanupCache()
    {
        handle.cleanupCache();
    }

    @Override
    public int[] getCachedBiomes(int x, int z)
    {
        BiomeGenBase[] cached = handle.getCachedBiomes(x, z);
        int[] intCache = new int[cached.length];
        for (int i = 0; i < cached.length; i++)
        {
            intCache[i] = getId(cached[i]);
        }
        return intCache;
    }
}
