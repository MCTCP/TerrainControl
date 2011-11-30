package com.Khorn.TerrainControl.BiomeManager.Layers;


import net.minecraft.server.*;

public class LayerBiomeBorder extends Layer
{
    public LayerBiomeBorder(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.a = paramGenLayer;
    }

    public int[] BiomeBorders = new int[]{-1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 15, -1, -1, -1, -1, -1};

    @Override
    public int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt1 = this.a.a(paramInt1 - 1, paramInt2 - 1, paramInt3 + 2, paramInt4 + 2);

        int[] arrayOfInt2 = IntCache.a(paramInt3 * paramInt4);
        for (int i = 0; i < paramInt4; i++)
        {
            for (int j = 0; j < paramInt3; j++)
            {
                a(j + paramInt1, i + paramInt2);
                int currentPiece = arrayOfInt1[(j + 1 + (i + 1) * (paramInt3 + 2))];
                if ((currentPiece&LandBit)!= 0 && BiomeBorders[currentPiece & BiomeBits] != -1)
                {
                    int i1 = arrayOfInt1[(j + 1 + (i + 1 - 1) * (paramInt3 + 2))] & (LandBit | BiomeBits);
                    int i2 = arrayOfInt1[(j + 1 + 1 + (i + 1) * (paramInt3 + 2))] & (LandBit | BiomeBits);
                    int i3 = arrayOfInt1[(j + 1 - 1 + (i + 1) * (paramInt3 + 2))] & (LandBit | BiomeBits);
                    int i4 = arrayOfInt1[(j + 1 + (i + 1 + 1) * (paramInt3 + 2))] & (LandBit | BiomeBits);
                    int i5 = currentPiece & (LandBit | BiomeBits);
                    if ((i1 != i5) || (i2 != i5) || (i3 != i5) || (i4 != i5))
                        currentPiece = (currentPiece & (IslandBit | RiverBits | IceBit)) | LandBit | BiomeBorders[currentPiece & BiomeBits];
                }
                arrayOfInt2[(j + i * paramInt3)] = currentPiece;

            }
        }

        return arrayOfInt2;
    }
}
