package com.Khorn.TerrainControl.BiomeManager.Layers;

import net.minecraft.server.*;

public class LayerRiverInit extends Layer
{
    public LayerRiverInit(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.a = paramGenLayer;
    }

    public int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt1 = this.a.a(paramInt1, paramInt2, paramInt3, paramInt4);

        int[] arrayOfInt2 = IntCache.a(paramInt3 * paramInt4);
        for (int i = 0; i < paramInt4; i++)
        {
            for (int j = 0; j < paramInt3; j++)
            {
                a(j + paramInt1, i + paramInt2);
                int currentPiece = arrayOfInt1[(j + i * paramInt3)];
                if (a(2) == 0)
                    currentPiece |= 256;
                else
                    currentPiece |= 512;

                arrayOfInt2[(j + i * paramInt3)] = currentPiece;
            }
        }

        return arrayOfInt2;
    }
}