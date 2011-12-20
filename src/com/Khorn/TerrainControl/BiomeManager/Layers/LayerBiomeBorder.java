package com.Khorn.TerrainControl.BiomeManager.Layers;


import com.Khorn.TerrainControl.BiomeManager.ArraysCache;

public class LayerBiomeBorder extends Layer
{
    public LayerBiomeBorder(long paramLong)
    {
        super(paramLong);
        for (int i = 0; i < BiomeBorders.length; i++)
            BiomeBorders[i] = -1;
    }

    public int OceanBorder = -1;
    public int[] BiomeBorders = new int[64];

    @Override
    public int[] GetBiomes(int cacheId, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt1 = this.child.GetBiomes(cacheId, paramInt1 - 1, paramInt2 - 1, paramInt3 + 2, paramInt4 + 2);

        int[] arrayOfInt2 = ArraysCache.GetArray(cacheId, paramInt3 * paramInt4);
        for (int i = 0; i < paramInt4; i++)
        {
            for (int j = 0; j < paramInt3; j++)
            {
                SetSeed(j + paramInt1, i + paramInt2);
                int currentPiece = arrayOfInt1[(j + 1 + (i + 1) * (paramInt3 + 2))];
                if ((currentPiece & LandBit) != 0 && BiomeBorders[currentPiece & BiomeBits] != -1)
                {
                    int i1 = arrayOfInt1[(j + 1 + (i + 1 - 1) * (paramInt3 + 2))] & (LandBit | BiomeBits);
                    int i2 = arrayOfInt1[(j + 1 + 1 + (i + 1) * (paramInt3 + 2))] & (LandBit | BiomeBits);
                    int i3 = arrayOfInt1[(j + 1 - 1 + (i + 1) * (paramInt3 + 2))] & (LandBit | BiomeBits);
                    int i4 = arrayOfInt1[(j + 1 + (i + 1 + 1) * (paramInt3 + 2))] & (LandBit | BiomeBits);
                    int i5 = currentPiece & (LandBit | BiomeBits);
                    if ((i1 != i5) || (i2 != i5) || (i3 != i5) || (i4 != i5))
                        currentPiece = (currentPiece & (IslandBit | RiverBits | IceBit)) | LandBit | BiomeBorders[currentPiece & BiomeBits];
                }
                if ((currentPiece & LandBit) == 0 && OceanBorder != -1)
                {
                    int i1 = arrayOfInt1[(j + 1 + (i + 1 - 1) * (paramInt3 + 2))] & LandBit;
                    int i2 = arrayOfInt1[(j + 1 + 1 + (i + 1) * (paramInt3 + 2))] & LandBit;
                    int i3 = arrayOfInt1[(j + 1 - 1 + (i + 1) * (paramInt3 + 2))] & LandBit;
                    int i4 = arrayOfInt1[(j + 1 + (i + 1 + 1) * (paramInt3 + 2))] & LandBit;
                    int i5 = currentPiece & LandBit;
                    if ((i1 != i5) || (i2 != i5) || (i3 != i5) || (i4 != i5))
                        currentPiece = (currentPiece & (IslandBit | RiverBits | IceBit)) | LandBit | OceanBorder;
                }

                arrayOfInt2[(j + i * paramInt3)] = currentPiece;

            }
        }

        return arrayOfInt2;
    }
}
