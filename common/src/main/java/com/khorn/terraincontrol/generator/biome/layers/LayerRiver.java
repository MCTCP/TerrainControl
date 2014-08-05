package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.generator.biome.ArraysCache;

public class LayerRiver extends Layer
{

    public LayerRiver(long seed, Layer childLayer)
    {
        super(seed);
        this.child = childLayer;
    }

    @Override
    public int[] getInts(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int x0 = x - 1;
        int z0 = z - 1;
        int xSize0 = xSize + 2;
        int zSize0 = zSize + 2;
        int[] childInts = this.child.getInts(cache, x0, z0, xSize0, zSize0);
        int[] thisInts = cache.getArray(xSize * zSize);

        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                int northCheck = childInts[(xi + 1 + (zi) * xSize0)] & RiverBits;
                int southCheck = childInts[(xi + 1 + (zi + 2) * xSize0)] & RiverBits;
                int eastCheck = childInts[(xi + 2 + (zi + 1) * xSize0)] & RiverBits;
                int westCheck = childInts[(xi + 0 + (zi + 1) * xSize0)] & RiverBits;
                int centerCheck = childInts[(xi + 1 + (zi + 1) * xSize0)] & RiverBits;
                
                int currentPiece = childInts[(xi + 1 + (zi + 1) * xSize0)];
                if ((centerCheck == 0) || (westCheck == 0) || (eastCheck == 0) || (northCheck == 0) || (southCheck == 0))
                    currentPiece |= RiverBits;
                else if ((centerCheck != westCheck) || (centerCheck != northCheck) || (centerCheck != eastCheck) || (centerCheck != southCheck))
                    currentPiece |= RiverBits;
                else
                {
                    currentPiece |= RiverBits;
                    currentPiece ^= RiverBits;
                }
                thisInts[(xi + zi * xSize)] = currentPiece;
            }
        }

        return thisInts;
    }

}
