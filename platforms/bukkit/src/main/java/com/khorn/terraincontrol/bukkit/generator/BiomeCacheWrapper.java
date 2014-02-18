package com.khorn.terraincontrol.bukkit.generator;

import com.khorn.terraincontrol.bukkit.CustomBiome;
import net.minecraft.server.v1_7_R1.BiomeBase;
import net.minecraft.server.v1_7_R1.BiomeCache;
import net.minecraft.server.v1_7_R1.WorldChunkManager;

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
        return getId(handle.b(x, z));
    }

    private int getId(BiomeBase biome)
    {
        // Support for virtual biomes
        if (biome instanceof CustomBiome)
        {
            return ((CustomBiome) biome).generationId;
        }
        return biome.id;
    }

    @Override
    public void cleanupCache()
    {
        handle.a();
    }

    @Override
    public int[] getCachedBiomes(int x, int z)
    {
        BiomeBase[] cached = handle.d(x, z);
        int[] intCache = new int[cached.length];
        for (int i = 0; i < cached.length; i++)
        {
            intCache[i] = getId(cached[i]);
        }
        return intCache;
    }

}
