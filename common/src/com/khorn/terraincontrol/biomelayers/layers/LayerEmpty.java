package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.biomelayers.ArrayCache;

public class LayerEmpty extends Layer
{
    public LayerEmpty(long paramLong)
    {
        super(paramLong);
    }

    @Override
    public int[] GetBiomes(ArrayCache arrayCache, int x, int z, int x_size, int z_size)
    {
        int[] arrayOfInt = arrayCache.GetArray( x_size * z_size);
        for (int i = 0; i < arrayOfInt.length; i++)
            arrayOfInt[i] = 0;
        return arrayOfInt;
    }
}
