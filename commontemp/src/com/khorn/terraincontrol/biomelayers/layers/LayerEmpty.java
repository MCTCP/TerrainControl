package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.biomelayers.ArraysCache;

public class LayerEmpty extends Layer
{
    public LayerEmpty(long paramLong)
    {
        super(paramLong);
    }

    @Override
    public int[] GetBiomes(int cacheId, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt = ArraysCache.GetArray(cacheId, paramInt3 * paramInt4);
        for (int i = 0; i < arrayOfInt.length; i++)
            arrayOfInt[i] = 0;
        return arrayOfInt;
    }
}
