package com.khorn.terraincontrol.biomelayers.layers;


import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.biomelayers.ArraysCache;
import com.khorn.terraincontrol.configuration.BiomeConfig;


public class LayerBiomeBorder extends Layer
{
    public LayerBiomeBorder(long paramLong, LocalWorld world)
    {
        super(paramLong);
        this.BordersFrom = new boolean[world.getMaxBiomesCount()][];
        this.BordersTo = new int[world.getMaxBiomesCount()];
    }

    private boolean[][] BordersFrom;
    private int[] BordersTo;


    public void AddBiome(BiomeConfig ReplaceTo, int ReplaceFrom, LocalWorld world)
    {
        this.BordersFrom[ReplaceFrom] = new boolean[world.getMaxBiomesCount()];

        for (int i = 0; i < this.BordersFrom[ReplaceFrom].length; i++)
        {
            LocalBiome biome = world.getBiomeById(i);
            this.BordersFrom[ReplaceFrom][i] = biome == null || !ReplaceTo.NotBorderNear.contains(biome.getName());
        }
        this.BordersTo[ReplaceFrom] = ReplaceTo.Biome.getId();
    }

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

                int biomeId = GetBiomeFromLayer(currentPiece);
                if (BordersFrom[biomeId] != null)
                {
                    int i1 = GetBiomeFromLayer(arrayOfInt1[(j + 1 + (i + 1 - 1) * (paramInt3 + 2))]);
                    int i2 = GetBiomeFromLayer(arrayOfInt1[(j + 1 + 1 + (i + 1) * (paramInt3 + 2))]);
                    int i3 = GetBiomeFromLayer(arrayOfInt1[(j + 1 - 1 + (i + 1) * (paramInt3 + 2))]);
                    int i4 = GetBiomeFromLayer(arrayOfInt1[(j + 1 + (i + 1 + 1) * (paramInt3 + 2))]);
                    boolean[] biomeFrom = BordersFrom[biomeId];
                    if (biomeFrom[i1] && biomeFrom[i2] && biomeFrom[i3] && biomeFrom[i4])
                        if ((i1 != biomeId) || (i2 != biomeId) || (i3 != biomeId) || (i4 != biomeId))
                            currentPiece = (currentPiece & (IslandBit | RiverBits | IceBit)) | LandBit | BordersTo[biomeId];
                }

                arrayOfInt2[(j + i * paramInt3)] = currentPiece;

            }
        }

        return arrayOfInt2;
    }
}