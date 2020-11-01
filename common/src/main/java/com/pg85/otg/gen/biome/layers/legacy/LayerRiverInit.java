package com.pg85.otg.gen.biome.layers.legacy;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.gen.biome.ArraysCache;

public class LayerRiverInit extends Layer
{

    LayerRiverInit(long paramLong, int defaultOceanId, Layer paramGenLayer)
    {
        super(paramLong, defaultOceanId);
        this.child = paramGenLayer;
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(world, cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);

        int currentPiece;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                initChunkSeed(zi + z, xi + x);           // reversed
                currentPiece = childInts[(xi + zi * xSize)];
                if (nextInt(2) == 0)
                {
                    currentPiece |= RiverBitOne;
                } else {
                    currentPiece |= RiverBitTwo;
                }

                thisInts[(xi + zi * xSize)] = currentPiece;                
            }
        }
        return thisInts;
    }

}
