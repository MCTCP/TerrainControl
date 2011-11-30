package com.Khorn.TerrainControl.BiomeManager.Layers;

import net.minecraft.server.*;

public class LayerLandRandom extends Layer
{
    public LayerLandRandom(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.a = paramGenLayer;
    }

    @SuppressWarnings({"PointlessArithmeticExpression"})
    public int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int i = paramInt1 - 1;
        int j = paramInt2 - 1;
        int k = paramInt3 + 2;
        int m = paramInt4 + 2;
        int[] arrayOfInt1 = this.a.a(i, j, k, m);

        int[] arrayOfInt2 = IntCache.a(paramInt3 * paramInt4);
        for (int n = 0; n < paramInt4; n++)
        {
            for (int i1 = 0; i1 < paramInt3; i1++)
            {
                int i2 = arrayOfInt1[(i1 + 0 + (n + 0) * k)] & LandBit;
                int i3 = arrayOfInt1[(i1 + 2 + (n + 0) * k)] & LandBit;
                int i4 = arrayOfInt1[(i1 + 0 + (n + 2) * k)] & LandBit;
                int i5 = arrayOfInt1[(i1 + 2 + (n + 2) * k)] & LandBit;
                int i6 = arrayOfInt1[(i1 + 1 + (n + 1) * k)] & LandBit;
                a(i1 + paramInt1, n + paramInt2);
                arrayOfInt2[(i1 + n * paramInt3)] = arrayOfInt1[(i1 + 1 + (n + 1) * k)] | LandBit;
                if ((i6 == 0) && ((i2 != 0) || (i3 != 0) || (i4 != 0) || (i5 != 0)))
                {
                    if (a(3) != 0)
                        arrayOfInt2[(i1 + n * paramInt3)] ^=  LandBit;

                } else if ((i6 > 0) && ((i2 == 0) || (i3 == 0) || (i4 == 0) || (i5 == 0)))
                {
                    if (a(5) == 0)
                        arrayOfInt2[(i1 + n * paramInt3)] ^= LandBit;

                } else if (i6 == 0)
                    arrayOfInt2[(i1 + n * paramInt3)] ^=  LandBit;
            }
        }
        return arrayOfInt2;
    }
}