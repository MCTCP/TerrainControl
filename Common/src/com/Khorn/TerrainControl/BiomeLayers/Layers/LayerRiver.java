package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.biomelayers.ArraysCache;

public class LayerRiver extends Layer
{
    public LayerRiver(long paramLong, Layer paramGenLayer)
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
                int i2 = arrayOfInt1[(i1 + 0 + (n + 1) * k)] & RiverBits;
                int i3 = arrayOfInt1[(i1 + 2 + (n + 1) * k)] & RiverBits;
                int i4 = arrayOfInt1[(i1 + 1 + (n + 0) * k)] & RiverBits;
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
                arrayOfInt2[(i1 + n * paramInt3)] = currentPiece;
            }
        }

        return arrayOfInt2;
    }
}