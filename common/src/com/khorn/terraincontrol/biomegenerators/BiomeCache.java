package com.khorn.terraincontrol.biomegenerators;

public interface BiomeCache
{
    public int getBiome(int x, int z);
    
    public void cleanupCache();
    
    public int[] getCachedBiomes(int x, int z);
}
