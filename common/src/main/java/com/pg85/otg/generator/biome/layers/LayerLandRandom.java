package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;

public class LayerLandRandom extends Layer
{
    LayerLandRandom(long seed, int defaultOceanId, Layer childLayer)
    {
        super(seed, defaultOceanId);
        this.child = childLayer;
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int x0 = x - 1;
        int z0 = z - 1;
        int xSize0 = xSize + 2;
        int zSize0 = zSize + 2;
        int[] childInts = this.child.getInts(world, cache, x0, z0, xSize0, zSize0);
        int[] thisInts = cache.getArray(xSize * zSize);

        int nwCheck;
        int neCheck;
        int swCheck;
        int seCheck;
        int centerCheck;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                nwCheck = childInts[(xi + 0 + (zi) * xSize0)] & LandBit;
                neCheck = childInts[(xi + 2 + (zi) * xSize0)] & LandBit;
                swCheck = childInts[(xi + 0 + (zi + 2) * xSize0)] & LandBit;
                seCheck = childInts[(xi + 2 + (zi + 2) * xSize0)] & LandBit;
                centerCheck = childInts[(xi + 1 + (zi + 1) * xSize0)] & LandBit;
                initChunkSeed(xi + x, zi + z);
                initGroupSeed(xi + x, zi + z);
                thisInts[(xi + zi * xSize)] = childInts[(xi + 1 + (zi + 1) * xSize0)] | LandBit;

                // Chances to reset LandBit
                if ((centerCheck == 0) && ((nwCheck != 0) || (neCheck != 0) || (swCheck != 0) || (seCheck != 0)))
                {
                    if (nextInt(3) != 0)
                    {
                        thisInts[(xi + zi * xSize)] ^= LandBit;
                    }
                }
                else if ((centerCheck > 0) && ((nwCheck == 0) || (neCheck == 0) || (swCheck == 0) || (seCheck == 0)))
                {
                    if (nextInt(5) == 0)
                    {
                        thisInts[(xi + zi * xSize)] ^= LandBit;
                    }
                }
                else if (centerCheck == 0)
                {
                    thisInts[(xi + zi * xSize)] ^= LandBit;
                }
            }
        }
        return thisInts;
    }
}
