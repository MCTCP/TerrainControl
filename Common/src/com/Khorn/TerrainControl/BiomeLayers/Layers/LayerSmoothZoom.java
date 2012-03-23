package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.biomelayers.ArraysCache;

public class LayerSmoothZoom extends Layer
{
    public LayerSmoothZoom(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.child = paramGenLayer;
    }

    public int[] GetBiomes(int cacheId, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int i = paramInt1 >> 1;
        int j = paramInt2 >> 1;
        int k = (paramInt3 >> 1) + 3;
        int m = (paramInt4 >> 1) + 3;
        int[] arrayOfInt1 = this.child.GetBiomes(cacheId, i, j, k, m);

        int[] arrayOfInt2 = ArraysCache.GetArray(cacheId, k * 2 * (m * 2));
        int n = k << 1;
        for (int i1 = 0; i1 < m - 1; i1++)
        {
            int i2 = i1 << 1;
            int i3 = i2 * n;
            int i4 = arrayOfInt1[(0 + (i1 + 0) * k)];
            int i5 = arrayOfInt1[(0 + (i1 + 1) * k)];
            for (int i6 = 0; i6 < k - 1; i6++)
            {
                SetSeed((long) (i6 + i << 1), (long) (i1 + j << 1));

                int i7 = arrayOfInt1[(i6 + 1 + (i1 + 0) * k)];
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
        int[] arrayOfInt3 = ArraysCache.GetArray(cacheId, paramInt3 * paramInt4);
        for (int i2 = 0; i2 < paramInt4; i2++)
        {
            System.arraycopy(arrayOfInt2, (i2 + (paramInt2 & 0x1)) * (k << 1) + (paramInt1 & 0x1), arrayOfInt3, i2 * paramInt3, paramInt3);
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