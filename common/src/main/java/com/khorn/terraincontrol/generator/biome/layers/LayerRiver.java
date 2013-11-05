package com.khorn.terraincontrol.generator.biome.layers;


import com.khorn.terraincontrol.generator.biome.ArraysCache;

public class LayerRiver extends Layer
{
    public LayerRiver(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.child = paramGenLayer;
    }

    @Override
    public int[] GetBiomes(ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int i = x - 1;
        int j = z - 1;
        int k = x_size + 2;
        int m = z_size + 2;
        int[] arrayOfInt1 = this.child.GetBiomes(arraysCache, i, j, k, m);

        int[] arrayOfInt2 = arraysCache.GetArray( x_size * z_size);
        for (int n = 0; n < z_size; n++)
        {
            for (int i1 = 0; i1 < x_size; i1++)
            {
                int i2 = arrayOfInt1[(i1 + 0 + (n + 1) * k)] & RiverBits;
                int i3 = arrayOfInt1[(i1 + 2 + (n + 1) * k)] & RiverBits;
                int i4 = arrayOfInt1[(i1 + 1 + (n) * k)] & RiverBits;
                int i5 = arrayOfInt1[(i1 + 1 + (n + 2) * k)] & RiverBits;
                int i6 = arrayOfInt1[(i1 + 1 + (n + 1) * k)] & RiverBits;
                int currentPiece = arrayOfInt1[(i1 + 1 + (n + 1) * k)];
                if ((i6 == 0) || (i2 == 0) || (i3 == 0) || (i4 == 0) || (i5 == 0))
                    currentPiece |= RiverBits;
                else if ((i6 != i2) || (i6 != i4) || (i6 != i3) || (i6 != i5))
                    currentPiece |= RiverBits;
                else
                {
                    currentPiece |= RiverBits;
                    currentPiece ^= RiverBits;
                }
                arrayOfInt2[(i1 + n * x_size)] = currentPiece;
            }
        }

        return arrayOfInt2;
    }
}