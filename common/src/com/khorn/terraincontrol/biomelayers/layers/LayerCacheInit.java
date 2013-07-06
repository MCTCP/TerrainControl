package com.khorn.terraincontrol.biomelayers.layers;

import com.khorn.terraincontrol.biomelayers.ArrayCache;
import com.khorn.terraincontrol.biomelayers.ArraysCacheManager;

public class LayerCacheInit extends Layer
{
    public LayerCacheInit(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.child = paramGenLayer;
    }

    @Override
    public int[] GetBiomes(ArrayCache arrayCache, int x, int z, int x_size, int z_size)
    {
        return new int[0];
    }

    @Override
    public int[] Calculate(int x, int z, int x_size, int z_size)
    {
        ArrayCache cache = ArraysCacheManager.GetCache();
        int[] out = this.child.GetBiomes(cache, x, z, x_size, z_size);
        ArraysCacheManager.ReleaseCache(cache);
        return out;
    }
}