package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.biomelayers.ArraysCache;

public class LayerRiverInit extends Layer
{
    public LayerRiverInit(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.child = paramGenLayer;
    }

    public int[] GetBiomes(int cacheId, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt1 = this.child.GetBiomes(cacheId, paramInt1, paramInt2, paramInt3, paramInt4);

        int[] arrayOfInt2 = ArraysCache.GetArray(cacheId, paramInt3 * paramInt4);
        for (int i = 0; i < paramInt4; i++)
        {
            for (int j = 0; j < paramInt3; j++)
            {
                SetSeed(i + paramInt2, j + paramInt1);           // reversed
                int currentPiece = arrayOfInt1[(j + i * paramInt3)];
                if (nextInt(2) == 0)
                    currentPiece |= 1024;
                else
                    currentPiece |= 2048;

                arrayOfInt2[(j + i * paramInt3)] = currentPiece;
            }
        }

        return arrayOfInt2;
    }
}