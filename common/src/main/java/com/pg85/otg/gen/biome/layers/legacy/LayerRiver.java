package com.pg85.otg.gen.biome.layers.legacy;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.gen.biome.ArraysCache;

public class LayerRiver extends Layer
{

    LayerRiver(long seed, int defaultOceanId, Layer childLayer)
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

        int northCheck;
        int southCheck;
        int eastCheck;
        int westCheck;
        int centerCheck;
        int currentPiece;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                northCheck = childInts[(xi + 1 + (zi) * xSize0)] & RiverBits;
                southCheck = childInts[(xi + 1 + (zi + 2) * xSize0)] & RiverBits;
                eastCheck = childInts[(xi + 2 + (zi + 1) * xSize0)] & RiverBits;
                westCheck = childInts[(xi + 0 + (zi + 1) * xSize0)] & RiverBits;
                centerCheck = childInts[(xi + 1 + (zi + 1) * xSize0)] & RiverBits;
                
                currentPiece = childInts[(xi + 1 + (zi + 1) * xSize0)];
                if ((centerCheck == 0) || (westCheck == 0) || (eastCheck == 0) || (northCheck == 0) || (southCheck == 0))
                {
                    currentPiece |= RiverBits;
                }
                else if ((centerCheck != westCheck) || (centerCheck != northCheck) || (centerCheck != eastCheck) || (centerCheck != southCheck))
                {
                    currentPiece |= RiverBits;
                } else {
                	// Remove any river bits entirely(?)
                    currentPiece |= RiverBits;
                    currentPiece ^= RiverBits;
                }
                thisInts[(xi + zi * xSize)] = currentPiece;
            }
        }

        return thisInts;
    }

}
