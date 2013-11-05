package com.khorn.terraincontrol.generator.biome.layers;


import com.khorn.terraincontrol.generator.biome.ArraysCache;

public class LayerRiverInit extends Layer
{
    public LayerRiverInit(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.child = paramGenLayer;
    }

    @Override
    public int[] GetBiomes(ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int[] arrayOfInt1 = this.child.GetBiomes(arraysCache, x, z, x_size, z_size);

        int[] arrayOfInt2 = arraysCache.GetArray( x_size * z_size);
        for (int i = 0; i < z_size; i++)
        {
            for (int j = 0; j < x_size; j++)
            {
                SetSeed(i + z, j + x);           // reversed
                int currentPiece = arrayOfInt1[(j + i * x_size)];
                if (nextInt(2) == 0)
                    currentPiece |= RiverBitOne;
                else
                    currentPiece |= RiverBitTwo;

                arrayOfInt2[(j + i * x_size)] = currentPiece;
            }
        }

        return arrayOfInt2;
    }
}