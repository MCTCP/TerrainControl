package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;

public class LayerIce extends Layer
{

    private int rarity = 10;

    LayerIce(long seed, int defaultOceanId, Layer childLayer)
    {
        super(seed, defaultOceanId);
        this.child = childLayer;
    }

    LayerIce(long seed, int defaultOceanId, Layer childLayer, int _rarity)
    {
        super(seed, defaultOceanId);
        this.child = childLayer;
        this.rarity = 101 - _rarity;
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(world, cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);

        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                initChunkSeed(z + zi, x + xi);      // reversed
                thisInts[(xi + zi * xSize)] = (nextInt(rarity) == 0 ? (childInts[(xi + zi * xSize)] | IceBit) : childInts[(xi + zi * xSize)]);
            }
        }
        return thisInts;
    }

}
