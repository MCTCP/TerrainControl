package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.generator.biome.ArraysCache;

public class LayerZoom extends Layer
{

    public LayerZoom(long seed, Layer childLayer)
    {
        super(seed);
        this.child = childLayer;
    }

    @Override
    public int[] getInts(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int x0 = x >> 1;
        int z0 = z >> 1;
        int xSize0 = (xSize >> 1) + 3;
        int zSize0 = (zSize >> 1) + 3;
        int[] childInts = this.child.getInts(cache, x0, z0, xSize0, zSize0);
        int[] thisInts = cache.getArray(xSize0 * 2 * (zSize0 * 2));

        int n = xSize0 << 1;
        for (int zi = 0; zi < zSize0 - 1; zi++)
        {
            int i2 = zi << 1;
            int i3 = i2 * n;
            int i4 = childInts[((zi) * xSize0)];
            int i5 = childInts[((zi + 1) * xSize0)];
            for (int xi = 0; xi < xSize0 - 1; xi++)
            {
                initChunkSeed((long) (xi + x0 << 1), (long) (zi + z0 << 1));
                int northCheck = childInts[(xi + 1 + (zi) * xSize0)];
                int centerCheck = childInts[(xi + 1 + (zi + 1) * xSize0)];

                thisInts[i3] = i4;
                thisInts[(i3++ + n)] = RndParam(i4, i5);
                thisInts[i3] = RndParam(i4, northCheck);
                thisInts[(i3++ + n)] = getRandomOf4(i4, northCheck, i5, centerCheck);

                i4 = northCheck;
                i5 = centerCheck;
            }
        }
        int[] ret = cache.getArray(xSize * zSize);
        for (int i2 = 0; i2 < zSize; i2++)
        {
            System.arraycopy(thisInts, (i2 + (z & 0x1)) * (xSize0 << 1) + (x & 0x1), ret, i2 * xSize, xSize);
        }
        return ret;
    }

    protected int RndParam(int a, int b)
    {
        return nextInt(2) == 0 ? a : b;
    }

}
