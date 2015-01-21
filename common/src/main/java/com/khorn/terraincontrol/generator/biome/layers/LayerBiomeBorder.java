package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.generator.biome.ArraysCache;

public class LayerBiomeBorder extends Layer
{

    private boolean[][] bordersFrom;
    private int[] bordersTo;

    public LayerBiomeBorder(long seed, LocalWorld world)
    {
        super(seed);
        this.bordersFrom = new boolean[world.getMaxBiomesCount()][];
        this.bordersTo = new int[world.getMaxBiomesCount()];
    }

    public void addBiome(LocalBiome replaceTo, int replaceFrom, LocalWorld world)
    {
        this.bordersFrom[replaceFrom] = new boolean[world.getMaxBiomesCount()];

        for (int i = 0; i < this.bordersFrom[replaceFrom].length; i++)
        {
            LocalBiome biome = world.getBiomeByIdOrNull(i);
            this.bordersFrom[replaceFrom][i] = biome == null || !replaceTo.getBiomeConfig().notBorderNear.contains(biome.getName());
        }
        this.bordersTo[replaceFrom] = replaceTo.getIds().getGenerationId();
    }

    @Override
    public int[] getInts(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(cache, x - 1, z - 1, xSize + 2, zSize + 2);
        int[] thisInts = cache.getArray(xSize * zSize);

        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                initChunkSeed(xi + x, zi + z);
                int selection = childInts[(xi + 1 + (zi + 1) * (xSize + 2))];

                int biomeId = getBiomeFromLayer(selection);
                if (bordersFrom[biomeId] != null)
                {
                    int northCheck = getBiomeFromLayer(childInts[(xi + 1 + (zi) * (xSize + 2))]);
                    int southCheck = getBiomeFromLayer(childInts[(xi + 1 + (zi + 2) * (xSize + 2))]);
                    int eastCheck = getBiomeFromLayer(childInts[(xi + 2 + (zi + 1) * (xSize + 2))]);
                    int westCheck = getBiomeFromLayer(childInts[(xi + (zi + 1) * (xSize + 2))]);
                    
                    boolean[] biomeFrom = bordersFrom[biomeId];
                    if (biomeFrom[northCheck] && biomeFrom[eastCheck] && biomeFrom[westCheck] && biomeFrom[southCheck])
                        if ((northCheck != biomeId) || (eastCheck != biomeId) || (westCheck != biomeId) || (southCheck != biomeId))
                            selection = (selection & (IslandBit | RiverBits | IceBit)) | LandBit | bordersTo[biomeId];
                }

                thisInts[(xi + zi * xSize)] = selection;

            }
        }

        return thisInts;
    }

}
