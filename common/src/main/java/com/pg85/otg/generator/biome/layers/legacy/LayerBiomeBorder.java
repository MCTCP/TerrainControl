package com.pg85.otg.generator.biome.layers.legacy;

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
        boolean improvedBiomeBorders = world.getConfigs().getWorldConfig().improvedBiomeBorders;
        for (int zi = 0; zi < zSize; zi++)
        {       	
            for (int xi = 0; xi < xSize; xi++)
            {
                initChunkSeed(xi + x, zi + z);
                selection = childInts[(xi + 1 + (zi + 1) * (xSize + 2))];

                biomeId = getBiomeFromLayer(selection);
                if (bordersFrom[biomeId] != null)
                {
                    // check in a plus formation to see if the tile is suitable for an edge biome                	
                    northCheck = getBiomeFromLayer(childInts[(xi + 1 + (zi) * (xSize + 2))]);
                    southCheck = getBiomeFromLayer(childInts[(xi + 1 + (zi + 2) * (xSize + 2))]);
                    eastCheck = getBiomeFromLayer(childInts[(xi + 2 + (zi + 1) * (xSize + 2))]);
                    westCheck = getBiomeFromLayer(childInts[(xi + (zi + 1) * (xSize + 2))]);
                    
                    biomeFrom = bordersFrom[biomeId];
                    if (biomeFrom[northCheck] && biomeFrom[eastCheck] && biomeFrom[westCheck] && biomeFrom[southCheck])
                    {
                        if ((northCheck != biomeId) || (eastCheck != biomeId) || (westCheck != biomeId) || (southCheck != biomeId))
                        {
                            // if it is suitable, set the edge biome
                            selection = (selection & (IslandBit | RiverBits | IceBit)) | LandBit | bordersTo[biomeId] | BiomeBitsAreSetBit;
                        }
                        else if(improvedBiomeBorders)
                        {
                            // if it's not suitable, try again but sample in an X formation to make sure we didn't miss any potential edge
                            int nwCheck = getBiomeFromLayer(childInts[(xi + 0 + (zi) * (xSize + 2))]);
                            int neCheck = getBiomeFromLayer(childInts[(xi + 2 + (zi) * (xSize + 2))]);
                            int swCheck = getBiomeFromLayer(childInts[(xi + 0 + (zi + 2) * (xSize + 2))]);
                            int seCheck = getBiomeFromLayer(childInts[(xi + 2 + (zi + 2) * (xSize + 2))]);
                            if (biomeFrom[nwCheck] && biomeFrom[neCheck] && biomeFrom[swCheck] && biomeFrom[seCheck])
                            {
                                if ((nwCheck != biomeId) || (neCheck != biomeId) || (swCheck != biomeId) || (seCheck != biomeId))
                                {
                                    // if the second test is suitable, set the edge biome
                                    selection = (selection & (IslandBit | RiverBits | IceBit)) | LandBit | bordersTo[biomeId] | BiomeBitsAreSetBit;
                                }
                                // if the selection isn't suitable at this point, we can be almost certain that it's not an edge
                            }
                        }                        
                    }
                }
                thisInts[(xi + zi * xSize)] = selection;
            }
        }
        return thisInts;
    }
}
