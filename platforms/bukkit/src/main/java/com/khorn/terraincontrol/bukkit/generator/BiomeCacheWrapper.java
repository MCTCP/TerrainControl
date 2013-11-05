package com.khorn.terraincontrol.bukkit.generator;

import net.minecraft.server.v1_6_R3.BiomeBase;
import net.minecraft.server.v1_6_R3.BiomeCache;
import net.minecraft.server.v1_6_R3.WorldChunkManager;

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
        return handle.b(x, z).id;
    }

    @Override
    public void cleanupCache()
    {
        handle.a();
    }

    @Override
    public int[] getCachedBiomes(int x, int z)
    {
        BiomeBase[] cached = handle.e(x, z);
        int[] intCache = new int[cached.length];
        for (int i = 0; i < cached.length; i++)
        {
            intCache[i] = cached[i].id;
        }
        return intCache;
    }

}
