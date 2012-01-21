package com.Khorn.TerrainControl.BiomeManager.Layers;

import com.Khorn.TerrainControl.BiomeManager.ArraysCache;
import com.Khorn.TerrainControl.Configuration.LocalBiome;


public class LayerBiomeInBiome extends Layer
{
    public LocalBiome biome;
    public int chance = 10;
    public boolean inOcean = false;

    public boolean[] BiomeIsles = new boolean[128];


    public LayerBiomeInBiome(long paramLong, Layer paramGenLayer)
    {
        super(paramLong);
        this.child = paramGenLayer;
        for (int i = 0; i < BiomeIsles.length; i++)
            BiomeIsles[i] = false;

    }

    @Override
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
                SetSeed(i1 + paramInt1, n + paramInt2);
                int currentPiece = arrayOfInt1[(i1 + 1 + (n + 1) * k)];

                boolean spawn = false;
                if (inOcean)
                {
                    int i2 = arrayOfInt1[(i1 + 0 + (n + 0) * k)] & LandBit;
                    int i3 = arrayOfInt1[(i1 + 2 + (n + 0) * k)] & LandBit;
                    int i4 = arrayOfInt1[(i1 + 0 + (n + 2) * k)] & LandBit;
                    int i5 = arrayOfInt1[(i1 + 2 + (n + 2) * k)] & LandBit;


                    if (((currentPiece & LandBit) == 0) && (i2 == 0) && (i3 == 0) && (i4 == 0) && (i5 == 0) && nextInt(chance) == 0)
                    {
                        currentPiece = (currentPiece & IceBit) | (currentPiece & RiverBits) | LandBit | biome.getId() | IslandBit;
                        spawn = true;
                    }
                }
                if (!spawn)
                {
                    int i2 = arrayOfInt1[(i1 + 0 + (n + 0) * k)] & BiomeBits;
                    int i3 = arrayOfInt1[(i1 + 2 + (n + 0) * k)] & BiomeBits;
                    int i4 = arrayOfInt1[(i1 + 0 + (n + 2) * k)] & BiomeBits;
                    int i5 = arrayOfInt1[(i1 + 2 + (n + 2) * k)] & BiomeBits;


                    if (BiomeIsles[(currentPiece & BiomeBits)] && BiomeIsles[i2] && BiomeIsles[i3] && BiomeIsles[i4] && BiomeIsles[i5] && nextInt(chance) == 0)
                        currentPiece = (currentPiece & LandBit) | (currentPiece & IceBit) | (currentPiece & RiverBits) | biome.getId() | IslandBit;

                }

                arrayOfInt2[(i1 + n * paramInt3)] = currentPiece;
            }
        }
        return arrayOfInt2;
    }
}
