package com.Khorn.TerrainControl.BiomeManager.Layers;


import net.minecraft.server.*;

public class LayerMix extends Layer
{
    public LayerMix(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.a = paramGenLayer;
    }


    @Override
    public int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {

        int[] arrayOfInt1 = this.a.a(paramInt1, paramInt2, paramInt3, paramInt4);

        int[] arrayOfInt2 = IntCache.a(paramInt3 * paramInt4);
        for (int i = 0; i < paramInt4; i++)
        {
            for (int j = 0; j < paramInt3; j++)
            {
                int currentPiece = arrayOfInt1[(j + i * paramInt3)];
                if ((currentPiece & LandBit) != 0)
                {
                    if ((currentPiece & RiverBits) != 0 && (currentPiece & IslandBit) == 0)
                        if ((currentPiece & IceBit) != 0)
                            currentPiece = BiomeBase.FROZEN_RIVER.F;
                        else
                            currentPiece = BiomeBase.RIVER.F;
                    else
                        currentPiece = currentPiece & BiomeBits;

                } else if ((currentPiece & IceBit) != 0)
                    currentPiece = BiomeBase.FROZEN_OCEAN.F;
                else
                    currentPiece = 0;
                arrayOfInt2[(j + i * paramInt3)] = currentPiece;
            }
        }

        return arrayOfInt2;
    }
}
