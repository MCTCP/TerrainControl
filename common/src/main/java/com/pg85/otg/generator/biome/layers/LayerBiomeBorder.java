package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;

public class LayerBiomeBorder extends Layer
{
    private boolean[][] bordersFrom;
    private int[] bordersTo;
    private int defaultOceanId;

    public LayerBiomeBorder(long seed, LocalWorld world, int defaultOceanId)
    {
        super(seed);
        this.defaultOceanId = defaultOceanId;
        this.bordersFrom = new boolean[world.getMaxBiomesCount()][];
        this.bordersTo = new int[world.getMaxBiomesCount()];
    }

    public void addBiome(LocalBiome replaceTo, int replaceFrom, LocalWorld world)
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
    
    /**
     * In a single step, checks for land and when present returns biome data
     * @param selection The location to be checked
     * @return Biome Data or 0 when not on land
     */
    protected int getBiomeFromLayer(int selection)
    {
        return (selection & LandBit) != 0 ? (selection & BiomeBits) : this.defaultOceanId;
    }
}
