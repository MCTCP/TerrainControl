package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.generator.biome.ArraysCache;

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

    public LayerBiomeInBiome(Layer childLayer, long worldSeed)
    {
        this.worldSeed = worldSeed;
        this.child = childLayer;
    }

    /**
     * Adds an isle to be spawned by this layer.
     * @param biome The biome of the isle.
     * @param chance The isle spawns when nextInt(chance) == 0.
     * @param biomeCanSpawnIn The biomes the isle can spawn in.
     */
    public void addIsle(LocalBiome biome, int chance, boolean[] biomeCanSpawnIn, boolean inOcean)
    {
        Isle isle = new Isle();
        isle.biomeId = (short) biome.getIds().getGenerationId();
        isle.chance = chance;
        isle.canSpawnIn = biomeCanSpawnIn;
        isle.inOcean = inOcean;

        // Pre-calculate the seeds unique for this layer
        // (keep in mind that the resulting world seed is based on the base seed)
        isle.scrambledWorldSeed = getScrambledWorldSeed(4000 + isle.biomeId, this.worldSeed);

        this.isles.add(isle);
    }

    @Override
    public int[] getInts(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int x0 = x - 1;
        int z0 = z - 1;
        int xSize0 = xSize + 2;
        int zSize0 = zSize + 2;
        int[] childInts = this.child.getInts(cache, x0, z0, xSize0, zSize0);
        int[] thisInts = cache.getArray(xSize * zSize);

        for (int zi = 0; zi < zSize; zi++)
        {
            for (int xi = 0; xi < xSize; xi++)
            {

                // Start by just copying the biome from the child layer
                int selection = childInts[(xi + 1 + (zi + 1) * xSize0)];

                // Then decide whether an isle should spawn
                for (Isle isle : this.isles)
                {
                    // Make the scrambled world seed unique for each isle
                    // (each island used to have its own layer)
                    this.scrambledWorldSeed = isle.scrambledWorldSeed;
                    initChunkSeed(xi + x, zi + z);
                    boolean alreadySpawned = false;
                    if (isle.inOcean)
                    {
                        int nwCheck = childInts[(xi + 0 + (zi) * xSize0)] & LandBit;
                        int neCheck = childInts[(xi + 2 + (zi) * xSize0)] & LandBit;
                        int swCheck = childInts[(xi + 0 + (zi + 2) * xSize0)] & LandBit;
                        int seCheck = childInts[(xi + 2 + (zi + 2) * xSize0)] & LandBit;

                        if (((selection & LandBit) == 0) && (nwCheck == 0) && (neCheck == 0) && (swCheck == 0) && (seCheck == 0) && nextInt(isle.chance) == 0)
                        {
                            selection = (selection & IceBit) | (selection & RiverBits) | LandBit | isle.biomeId | IslandBit;
                            alreadySpawned = true;
                        }
                    }
                    if (!alreadySpawned)
                    {
                        int nwCheck = childInts[(xi + 0 + (zi) * xSize0)] & BiomeBits;
                        int neCheck = childInts[(xi + 2 + (zi) * xSize0)] & BiomeBits;
                        int swCheck = childInts[(xi + 0 + (zi + 2) * xSize0)] & BiomeBits;
                        int seCheck = childInts[(xi + 2 + (zi + 2) * xSize0)] & BiomeBits;

                        if (isle.canSpawnIn[(selection & BiomeBits)] && isle.canSpawnIn[nwCheck] && isle.canSpawnIn[neCheck] && isle.canSpawnIn[swCheck] && isle.canSpawnIn[seCheck] && nextInt(isle.chance) == 0)
                            selection = (selection & LandBit) | (selection & IceBit) | (selection & RiverBits) | isle.biomeId | IslandBit;
                    }
                }
                thisInts[(xi + zi * xSize)] = selection;
            }
        }
        return thisInts;
    }

}
