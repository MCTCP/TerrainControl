package com.pg85.otg.gen.biome.layers.legacy;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.gen.biome.ArraysCache;

public class LayerEmpty extends Layer
{
    LayerEmpty(long seed, int defaultOceanId)
    {
        super(seed, defaultOceanId);
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] thisInts = cache.getArray(xSize * zSize);
        for (int i = 0; i < thisInts.length; i++)
        {
            thisInts[i] = 0;
        }
        return thisInts;
    }
}
