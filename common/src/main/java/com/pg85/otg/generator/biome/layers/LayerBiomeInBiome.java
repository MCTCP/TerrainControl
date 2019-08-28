package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;

import java.util.ArrayList;
import java.util.List;

public class LayerBiomeInBiome extends Layer
{	
    private static class Isle
    {
        short biomeId;
        int chance = 10;
        boolean[] canSpawnIn = new boolean[1024];
        long scrambledWorldSeed;
        boolean inOcean = false;
    }

    private final long worldSeed;
    private List<Isle> isles = new ArrayList<Isle>();

    LayerBiomeInBiome(Layer childLayer, long worldSeed, int defaultOceanId)
    {
    	this.defaultOceanId = defaultOceanId;
        this.worldSeed = worldSeed;
        this.child = childLayer;
    }

    /**
     * Adds an isle to be spawned by this layer.
     * @param biome The biome of the isle.
     * @param chance The isle spawns when nextInt(chance) == 0.
     * @param biomeCanSpawnIn The biomes the isle can spawn in.
     */
    void addIsle(LocalBiome biome, int chance, boolean[] biomeCanSpawnIn, boolean inOcean)
    {
        Isle isle = new Isle();
        isle.biomeId = (short) biome.getIds().getOTGBiomeId();
        isle.chance = chance;
        isle.canSpawnIn = biomeCanSpawnIn;
        isle.inOcean = inOcean;

        // Pre-calculate the seeds unique for this layer
        // (keep in mind that the resulting world seed is based on the base seed)
        
        // OTGBiomeId's changed for v7, to support legacy worlds we need the same rng as for v6, we'll have to make sure that:
        // 1. Default biomes produce the same biomeid as their saved id, as it was for v6.
        // 2. Virtual biomes and custom biomes use the same OTG biome id as for v6, at least for legacy worlds. 
        // 2 is taken care of by using custombiomes id data from the worldconfig in ServerConfigProvider. 1 We'll have to do here.
        
        int rngSeed = isle.biomeId;        
        if(DefaultBiome.getId(biome.getName()) != null)
        {
        	rngSeed = (short) biome.getIds().getSavedId();        	
        }
        
        isle.scrambledWorldSeed = getScrambledWorldSeed(4000 + rngSeed, this.worldSeed);

        this.isles.add(isle);
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int x0 = x - 1;
        int z0 = z - 1;
        int xSize0 = xSize + 2;
        int zSize0 = zSize + 2;
        int[] childInts = this.child.getInts(world, cache, x0, z0, xSize0, zSize0);
        int[] thisInts = cache.getArray(xSize * zSize);

        int selection;
        boolean alreadySpawned;
        int nwCheck;
        int neCheck;
        int swCheck;
        int seCheck;
        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {
                // Start by just copying the biome from the child layer
                selection = childInts[(xi + 1 + (zi + 1) * xSize0)];

                // Then decide whether an isle should spawn
                for (Isle isle : this.isles)
                {
                    // Make the scrambled world seed unique for each isle
                    // (each island used to have its own layer)
                    this.scrambledWorldSeed = isle.scrambledWorldSeed;
                    initChunkSeed(xi + x, zi + z);
                    alreadySpawned = false;
                    if (isle.inOcean)
                    {
                        nwCheck = childInts[(xi + 0 + (zi) * xSize0)] & LandBit;
                        neCheck = childInts[(xi + 2 + (zi) * xSize0)] & LandBit;
                        swCheck = childInts[(xi + 0 + (zi + 2) * xSize0)] & LandBit;
                        seCheck = childInts[(xi + 2 + (zi + 2) * xSize0)] & LandBit;

                        if (((selection & LandBit) == 0) && (nwCheck == 0) && (neCheck == 0) && (swCheck == 0) && (seCheck == 0) && nextInt(isle.chance) == 0)
                        {
                            selection = (selection & IceBit) | (selection & RiverBits) | LandBit | isle.biomeId | IslandBit | BiomeBitsAreSetBit;
                            alreadySpawned = true;
                        }
                    }
                    if (!alreadySpawned)
                    {
                        nwCheck = getBiomeFromLayer(childInts[(xi + 0 + (zi) * xSize0)]);
                        neCheck = getBiomeFromLayer(childInts[(xi + 2 + (zi) * xSize0)]);
                        swCheck = getBiomeFromLayer(childInts[(xi + 0 + (zi + 2) * xSize0)]);
                        seCheck = getBiomeFromLayer(childInts[(xi + 2 + (zi + 2) * xSize0)]);

                        if (
                    		isle.canSpawnIn[getBiomeFromLayer(selection)] && 
                    		isle.canSpawnIn[nwCheck] && 
                    		isle.canSpawnIn[neCheck] && 
                    		isle.canSpawnIn[swCheck] && 
                    		isle.canSpawnIn[seCheck] && 
                    		nextInt(isle.chance) == 0
                		)
                        {
                            selection = (selection & LandBit) | (selection & IceBit) | (selection & RiverBits) | isle.biomeId | IslandBit | BiomeBitsAreSetBit;
                        }
                    }
                }
                thisInts[(xi + zi * xSize)] = selection;
            }
        }
        return thisInts;
    }
}
