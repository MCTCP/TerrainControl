package com.khorn.terraincontrol.generator.biome.layers;


import com.khorn.terraincontrol.generator.biome.ArraysCache;

public class LayerEmpty extends Layer
{
    public LayerEmpty(long paramLong)
    {
        super(paramLong);
    }

    @Override
    public int[] GetBiomes(ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int[] arrayOfInt = arraysCache.GetArray( x_size * z_size);
        for (int i = 0; i < arrayOfInt.length; i++)
            arrayOfInt[i] = 0;
        return arrayOfInt;
    }
}
