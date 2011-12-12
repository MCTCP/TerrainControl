package com.Khorn.TerrainControl.BiomeManager.Layers;

import com.Khorn.TerrainControl.BiomeManager.ArraysCache;

public class LayerCacheInit extends Layer
{
    public LayerCacheInit(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.child = paramGenLayer;
    }

    @Override
    public int[] GetBiomes(int cacheId, int x, int z, int x_size, int z_size)
    {
        return new int[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int[] Calculate(int x, int z, int x_size, int z_size)
    {
        int cache = ArraysCache.GetCacheId();
        int[] out = this.child.GetBiomes(cache, x, z, x_size, z_size);
        ArraysCache.ReleaseCacheId(cache);
        return out;
    }
}
