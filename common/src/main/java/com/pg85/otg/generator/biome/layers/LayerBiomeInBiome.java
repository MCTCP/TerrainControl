package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;

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

	private int defaultOceanId;
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
        isle.scrambledWorldSeed = getScrambledWorldSeed(4000 + isle.biomeId, this.worldSeed);

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
                        nwCheck = childInts[(xi + 0 + (zi) * xSize0)];
                        nwCheck = (nwCheck & BiomeBitsAreSetBit) != 0 ? nwCheck & BiomeBits : this.defaultOceanId;
                        neCheck = childInts[(xi + 2 + (zi) * xSize0)] & BiomeBits;
                        neCheck = (neCheck & BiomeBitsAreSetBit) != 0 ? neCheck & BiomeBits : this.defaultOceanId;
                        swCheck = childInts[(xi + 0 + (zi + 2) * xSize0)] & BiomeBits;
                        swCheck = (swCheck & BiomeBitsAreSetBit) != 0 ? swCheck & BiomeBits : this.defaultOceanId;
                        seCheck = childInts[(xi + 2 + (zi + 2) * xSize0)] & BiomeBits;
                        seCheck = (seCheck & BiomeBitsAreSetBit) != 0 ? seCheck & BiomeBits : this.defaultOceanId;

                        if (isle.canSpawnIn[(selection & BiomeBitsAreSetBit) != 0 ? (selection & BiomeBits) : this.defaultOceanId] && isle.canSpawnIn[nwCheck] && isle.canSpawnIn[neCheck] && isle.canSpawnIn[swCheck] && isle.canSpawnIn[seCheck] && nextInt(isle.chance) == 0)
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
