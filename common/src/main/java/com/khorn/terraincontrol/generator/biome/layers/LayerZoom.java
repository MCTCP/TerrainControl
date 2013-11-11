package com.khorn.terraincontrol.generator.biome.layers;


import com.khorn.terraincontrol.generator.biome.ArraysCache;

public class LayerZoom extends Layer
{
    public LayerZoom(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.child = paramGenLayer;
    }

    @Override
    public int[] GetBiomes(ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int i = x >> 1;
        int j = z >> 1;
        int k = (x_size >> 1) + 3;
        int m = (z_size >> 1) + 3;
        int[] arrayOfInt1 = this.child.GetBiomes(arraysCache, i, j, k, m);

        int[] arrayOfInt2 = arraysCache.GetArray( k * 2 * (m * 2));
        int n = k << 1;
        int i2;
        for (int i1 = 0; i1 < m - 1; i1++)
        {
            i2 = i1 << 1;
            int i3 = i2 * n;
            int i4 = arrayOfInt1[((i1) * k)];
            int i5 = arrayOfInt1[((i1 + 1) * k)];
            for (int i6 = 0; i6 < k - 1; i6++)
            {
                SetSeed((long) (i6 + i << 1), (long) (i1 + j << 1));
                int i7 = arrayOfInt1[(i6 + 1 + (i1) * k)];
                int i8 = arrayOfInt1[(i6 + 1 + (i1 + 1) * k)];

                arrayOfInt2[i3] = i4;
                arrayOfInt2[i3++ + n] = RndParam(i4, i5);
                arrayOfInt2[i3] = RndParam(i4, i7);
                arrayOfInt2[i3++ + n] = b(i4, i7, i5, i8);

                i4 = i7;
                i5 = i8;
            }
        }
        int[] arrayOfInt3 = arraysCache.GetArray( x_size * z_size);
        for (i2 = 0; i2 < z_size; i2++)
        {
            System.arraycopy(arrayOfInt2, (i2 + (z & 0x1)) * (k << 1) + (x & 0x1), arrayOfInt3, i2 * x_size, x_size);
        }
        return arrayOfInt3;
    }

    protected int RndParam(int paramInt1, int paramInt2)
    {
        return nextInt(2) == 0 ? paramInt1 : paramInt2;
    }

    protected int b(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramInt2 == paramInt3) && (paramInt3 == paramInt4))
            return paramInt2;
        if ((paramInt1 == paramInt2) && (paramInt1 == paramInt3))
            return paramInt1;
        if ((paramInt1 == paramInt2) && (paramInt1 == paramInt4))
            return paramInt1;
        if ((paramInt1 == paramInt3) && (paramInt1 == paramInt4))
            return paramInt1;

        if ((paramInt1 == paramInt2) && (paramInt3 != paramInt4))
            return paramInt1;
        if ((paramInt1 == paramInt3) && (paramInt2 != paramInt4))
            return paramInt1;
        if ((paramInt1 == paramInt4) && (paramInt2 != paramInt3))
            return paramInt1;

        if ((paramInt2 == paramInt3) && (paramInt1 != paramInt4))
            return paramInt2;
        if ((paramInt2 == paramInt4) && (paramInt1 != paramInt3))
            return paramInt2;

        if ((paramInt3 == paramInt4) && (paramInt1 != paramInt2))
            return paramInt3;


        int i = nextInt(4);
        if (i == 0)
            return paramInt1;
        if (i == 1)
            return paramInt2;
        if (i == 2)
            return paramInt3;
        return paramInt4;
    }

}