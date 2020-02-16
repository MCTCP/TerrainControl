package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;

public class LayerBiomeBorder extends Layer
{
    private boolean[][] bordersFrom;
    private int[] bordersTo;

    LayerBiomeBorder(long seed, LocalWorld world, int defaultOceanId)
    {
        super(seed, defaultOceanId);
        this.bordersFrom = new boolean[world.getMaxBiomesCount()][];
        this.bordersTo = new int[world.getMaxBiomesCount()];
    }

    void addBiome(LocalBiome replaceTo, int replaceFrom, LocalWorld world)
    {
        this.bordersFrom[replaceFrom] = new boolean[world.getMaxBiomesCount()];

        for (int i = 0; i < this.bordersFrom[replaceFrom].length; i++)
        {
        	LocalBiome biome = world.getBiomeByOTGIdOrNull(i);            
            this.bordersFrom[replaceFrom][i] = biome == null || !replaceTo.getBiomeConfig().notBorderNear.contains(biome.getName());
        }
        this.bordersTo[replaceFrom] = replaceTo.getIds().getOTGBiomeId();
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(world, cache, x - 1, z - 1, xSize + 2, zSize + 2);
        int[] thisInts = cache.getArray(xSize * zSize);
        
        int selection;
        int northCheck;
        int southCheck;
        int eastCheck;
        int westCheck;
        boolean[] biomeFrom;
        int biomeId;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                initChunkSeed(xi + x, zi + z);
                selection = childInts[(xi + 1 + (zi + 1) * (xSize + 2))];

                biomeId = getBiomeFromLayer(selection);
                if (bordersFrom[biomeId] != null)
                {
                    northCheck = getBiomeFromLayer(childInts[(xi + 1 + (zi) * (xSize + 2))]);
                    southCheck = getBiomeFromLayer(childInts[(xi + 1 + (zi + 2) * (xSize + 2))]);
                    eastCheck = getBiomeFromLayer(childInts[(xi + 2 + (zi + 1) * (xSize + 2))]);
                    westCheck = getBiomeFromLayer(childInts[(xi + (zi + 1) * (xSize + 2))]);
                    
                    biomeFrom = bordersFrom[biomeId];
                    if (biomeFrom[northCheck] && biomeFrom[eastCheck] && biomeFrom[westCheck] && biomeFrom[southCheck])
                    {
                        if ((northCheck != biomeId) || (eastCheck != biomeId) || (westCheck != biomeId) || (southCheck != biomeId))
                        {
                            selection = (selection & (IslandBit | RiverBits | IceBit)) | LandBit | bordersTo[biomeId] | BiomeBitsAreSetBit;
                        }
                    }
                }

                thisInts[(xi + zi * xSize)] = selection;

            }
        }

        return thisInts;
    }
}
