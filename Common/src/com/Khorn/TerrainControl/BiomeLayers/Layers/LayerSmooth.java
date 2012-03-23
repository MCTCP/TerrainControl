package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.biomelayers.ArraysCache;

public class LayerSmooth extends Layer
{
    public LayerSmooth(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.child = paramGenLayer;
    }

    public int[] GetBiomes(int cacheId, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int i = paramInt1 - 1;
        int j = paramInt2 - 1;
        int k = paramInt3 + 2;
        int m = paramInt4 + 2;
        int[] arrayOfInt1 = this.child.GetBiomes(cacheId, i, j, k, m);

        int[] arrayOfInt2 = ArraysCache.GetArray(cacheId, paramInt3 * paramInt4);
        for (int n = 0; n < paramInt4; n++)
        {
            for (int i1 = 0; i1 < paramInt3; i1++)
            {
                int i2 = arrayOfInt1[(i1 + 0 + (n + 1) * k)];
                int i3 = arrayOfInt1[(i1 + 2 + (n + 1) * k)];
                int i4 = arrayOfInt1[(i1 + 1 + (n + 0) * k)];
                int i5 = arrayOfInt1[(i1 + 1 + (n + 2) * k)];
                int i6 = arrayOfInt1[(i1 + 1 + (n + 1) * k)];
                if ((i2 == i3) && (i4 == i5))
                {
                    SetSeed(i1 + paramInt1, n + paramInt2);
                    if (nextInt(2) == 0)
                        i6 = i2;
                    else
                        i6 = i4;
                } else
                {
                    if (i2 == i3)
                        i6 = i2;
                    if (i4 == i5)
                        i6 = i4;
                }
                arrayOfInt2[(i1 + n * paramInt3)] = i6;
            }

        }

        return arrayOfInt2;
    }
}