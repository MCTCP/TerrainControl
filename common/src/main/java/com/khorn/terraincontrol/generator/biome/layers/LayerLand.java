package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.generator.biome.ArraysCache;

public class LayerLand extends Layer
{

    public int rarity = 5;

    public LayerLand(long seed, Layer childLayer, int _rarity)
    {
        super(seed);
        this.child = childLayer;
        this.rarity = 101 - _rarity;
    }

    @Override
    public int[] getInts(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);

        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                initChunkSeed(x + xi, z + zi);
                if (nextInt(rarity) == 0)
                    thisInts[(xi + zi * xSize)] = childInts[(xi + zi * xSize)] | LandBit;
                else
                    thisInts[(xi + zi * xSize)] = childInts[(xi + zi * xSize)];
            }
        }
        return thisInts;
    }

}
