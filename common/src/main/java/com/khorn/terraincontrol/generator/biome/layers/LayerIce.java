package com.khorn.terraincontrol.generator.biome.layers;


import com.khorn.terraincontrol.generator.biome.ArraysCache;

public class LayerIce extends Layer
{
    public LayerIce(long paramLong, Layer paramGenLayer, int _rarity)
    {
        super(paramLong);
        this.child = paramGenLayer;
        this.rarity = 101 - _rarity;
    }


    public int rarity = 5;

    @Override
    public int[] GetBiomes(ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int[] arrayOfInt1 = this.child.GetBiomes(arraysCache, x, z, x_size, z_size);

        int[] arrayOfInt2 = arraysCache.GetArray( x_size * z_size);
        for (int i = 0; i < z_size; i++)
        {
            for (int j = 0; j < x_size; j++)
            {
                SetSeed(z + i, x + j);      // reversed
                arrayOfInt2[(j + i * x_size)] = (nextInt(rarity) == 0 ? (arrayOfInt1[(j + i * x_size)] | IceBit) : arrayOfInt1[(j + i * x_size)]);
            }
        }

        return arrayOfInt2;
    }
}
