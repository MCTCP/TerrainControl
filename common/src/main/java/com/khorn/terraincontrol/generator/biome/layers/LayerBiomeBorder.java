package com.khorn.terraincontrol.generator.biome.layers;


import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.generator.biome.ArraysCache;


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


    public void AddBiome(LocalBiome replaceTo, int ReplaceFrom, LocalWorld world)
    {
        this.BordersFrom[ReplaceFrom] = new boolean[world.getMaxBiomesCount()];

        for (int i = 0; i < this.BordersFrom[ReplaceFrom].length; i++)
        {
            LocalBiome biome = world.getBiomeById(i);
            this.BordersFrom[ReplaceFrom][i] = biome == null || !replaceTo.getBiomeConfig().notBorderNear.contains(biome.getName());
        }
        this.BordersTo[ReplaceFrom] = replaceTo.getIds().getGenerationId();
    }

    @Override
    public int[] GetBiomes(ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int[] arrayOfInt1 = this.child.GetBiomes(arraysCache, x - 1, z - 1, x_size + 2, z_size + 2);

        int[] arrayOfInt2 = arraysCache.GetArray(x_size * z_size);
        for (int i = 0; i < z_size; i++)
        {
            for (int j = 0; j < x_size; j++)
            {
                SetSeed(j + x, i + z);
                int currentPiece = arrayOfInt1[(j + 1 + (i + 1) * (x_size + 2))];

                int biomeId = GetBiomeFromLayer(currentPiece);
                if (BordersFrom[biomeId] != null)
                {
                    int i1 = GetBiomeFromLayer(arrayOfInt1[(j + 1 + (i + 1 - 1) * (x_size + 2))]);
                    int i2 = GetBiomeFromLayer(arrayOfInt1[(j + 1 + 1 + (i + 1) * (x_size + 2))]);
                    int i3 = GetBiomeFromLayer(arrayOfInt1[(j + 1 - 1 + (i + 1) * (x_size + 2))]);
                    int i4 = GetBiomeFromLayer(arrayOfInt1[(j + 1 + (i + 1 + 1) * (x_size + 2))]);
                    boolean[] biomeFrom = BordersFrom[biomeId];
                    if (biomeFrom[i1] && biomeFrom[i2] && biomeFrom[i3] && biomeFrom[i4])
                        if ((i1 != biomeId) || (i2 != biomeId) || (i3 != biomeId) || (i4 != biomeId))
                            currentPiece = (currentPiece & (IslandBit | RiverBits | IceBit)) | LandBit | BordersTo[biomeId];
                }

                arrayOfInt2[(j + i * x_size)] = currentPiece;

            }
        }

        return arrayOfInt2;
    }
}