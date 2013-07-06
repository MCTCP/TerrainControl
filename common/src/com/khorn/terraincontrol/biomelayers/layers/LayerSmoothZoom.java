package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.biomelayers.ArrayCache;

public class LayerSmoothZoom extends Layer
{
    public LayerSmoothZoom(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.child = paramGenLayer;
    }

    public int[] GetBiomes(ArrayCache arrayCache, int x, int z, int x_size, int z_size)
    {
        int i = x >> 1;
        int j = z >> 1;
        int k = (x_size >> 1) + 3;
        int m = (z_size >> 1) + 3;
        int[] arrayOfInt1 = this.child.GetBiomes(arrayCache, i, j, k, m);

        int[] arrayOfInt2 = arrayCache.GetArray( k * 2 * (m * 2));
        int n = k << 1;
        for (int i1 = 0; i1 < m - 1; i1++)
        {
            int i2 = i1 << 1;
            int i3 = i2 * n;
            int i4 = arrayOfInt1[((i1) * k)];
            int i5 = arrayOfInt1[((i1 + 1) * k)];
            for (int i6 = 0; i6 < k - 1; i6++)
            {
                SetSeed((long) (i6 + i << 1), (long) (i1 + j << 1));

                int i7 = arrayOfInt1[(i6 + 1 + (i1) * k)];
                int i8 = arrayOfInt1[(i6 + 1 + (i1 + 1) * k)];

                arrayOfInt2[i3] = i4;
                arrayOfInt2[(i3++ + n)] = (i4 + (i5 - i4) * nextInt(256) / 256);
                arrayOfInt2[i3] = (i4 + (i7 - i4) * nextInt(256) / 256);

                int i9 = i4 + (i7 - i4) * nextInt(256) / 256;
                int i10 = i5 + (i8 - i5) * nextInt(256) / 256;
                arrayOfInt2[(i3++ + n)] = (i9 + (i10 - i9) * nextInt(256) / 256);

                i4 = i7;
                i5 = i8;
            }
        }
        int[] arrayOfInt3 = arrayCache.GetArray( x_size * z_size);
        for (int i2 = 0; i2 < z_size; i2++)
        {
            System.arraycopy(arrayOfInt2, (i2 + (z & 0x1)) * (k << 1) + (x & 0x1), arrayOfInt3, i2 * x_size, x_size);
        }
        return arrayOfInt3;
    }

    public static Layer a(long paramLong, Layer paramGenLayer, int paramInt)
    {
        Layer localObject = paramGenLayer;
        for (int i = 0; i < paramInt; i++)
        {
            localObject = new LayerSmoothZoom(paramLong + i, localObject);
        }
        return localObject;
    }
}