package com.pg85.otg.generator.biome.layers.legacy;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;

public class LayerLand extends Layer
{

    private int rarity = 5;

    LayerLand(long seed, int defaultOceanId, Layer childLayer, int _rarity)
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
                initChunkSeed(x + xi, z + zi);
                if (nextInt(rarity) == 0)
                {
                    thisInts[(xi + zi * xSize)] = childInts[(xi + zi * xSize)] | LandBit;
                } else {
                    thisInts[(xi + zi * xSize)] = childInts[(xi + zi * xSize)];
                }
            }
        }
        return thisInts;
    }

}
