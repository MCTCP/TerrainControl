package com.Khorn.TerrainControl.BiomeManager.Layers;

import net.minecraft.server.*;

public class LayerEmpty extends Layer
{
    public LayerEmpty(long paramLong)
    {
        super(paramLong);
    }

    @Override
    public int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt = IntCache.a(paramInt3 * paramInt4);
        for (int i = 0; i < arrayOfInt.length; i++)
            arrayOfInt[i] = 0;
        return arrayOfInt;
    }
}
