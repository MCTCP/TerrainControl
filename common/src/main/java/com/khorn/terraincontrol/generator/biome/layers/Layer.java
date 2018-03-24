package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.generator.biome.ArraysCache;

/**
 * Layer is the abstract base class for the entire layering system.
 * This system works on the principle that given an array of integers
 * representing a given space on a Minecraft map, that each column of
 * blocks is represented by a single array index/location. At each location
 * a sequence of operations is performed such that the end result is a
 * full description of what should be created upon generation with
 * respect to biome information.
 * <p>
 * Each subclass will need to implement getInts() which is responsible
 * to acting upon the array of ints in it's own unique way. As previously
 * mentioned, each array index is associated with a column of blocks in
 * Minecraft. Each array index is acted upon using a bit masking system.
 * <p>
 * The bit masking system works by marking locations with specific properties
 * or data such as: Land, Island, Ice, River, Biome, and BiomeGroup.
 * For example, the first part of the system starts by assigning MainLayer
 * to LayerEmpty. This layer simply initializes the array of ints with zeros.
 * Next MainLayer is assigned to LayerZoom which has the job of increasing
 * the resolution of the array of ints. Notice how each new layer assigned
 * to MainLayer passes the previous MainLayer into the constructor. Next we
 * see MainLayer assigned to LayerLand. LayerLand randomly marks indices with
 * a Land flag. This flag is then used by the next layers, LayerLandRandom
 * and LayerBiomeGroup, to act upon the map in land specific ways. This process
 * continues for @generationDepth times and is then finished with a set of
 * layers that clean up after the layer system and get it ready for generation
 * <p>
 * It is important to note that getInts should, in most cases, call
 * this.child.getInts() which will follow the trail all the way back to the
 * initial `LayerEmpty` call and produce all modifications to the array as
 * it climbs back up the chain of getInts() calls.
 */
public abstract class Layer
{

    /**
     * The base seed set during layer construction, all other seeds are based
     * upon this one.
     */
    protected long baseSeed;

    /**
     * A general seed kept for use in world generation
     * @see #initWorldGenSeed(long)
     */
    protected long scrambledWorldSeed;

    /**
     * This seed is used for general random number generation within the Layers
     * system. It is based off of both the scrambledWorldSeed and baseSeed.
     * @see #initWorldGenSeed(long)
     * @see #initChunkSeed(long, long)
     */
    private long scrambledChunkSeed;

    /**
     * This seed is used for generating random numbers for biome groups
     * @see #initGroupSeed(long, long)
     */
    private long scrambledGroupSeed;

    /**
     * The layer to process before this one. getInts() should call
     * child.getInts() before doing any processing -- in most cases.
     */
    protected Layer child;

    /**
     * This helps our random numbers be a little more random
     */
    protected static final int entropy = 10000;

    /*
     * LayerIsland - chance to big land
     * LayerLandRandom - a(3) - chance to increase big land
     * GenLayerIcePlains - chance to ice
     * GenLayerMushroomIsland - chance to mushroom island
     *
     * biome:
     * 1) is island
     * 2) size
     * 3) chance
     * 4) is shore
     * 5) color
     * 6) temperature
     * 7) downfall
     * 8) is snow biome
     * 9) Have rivers
     *
     * world
     * 1) chance to lands
     * 2) size of big lands
     * 3) chance to increase lands
     * 4) Chance for ice area
     * 5) Ice area size
     * 6) Rivers
     * 7) Rivers size
     */
    // [ Biome Data ]
    protected static final int BiomeBits = 1023;            //>>	1st-10th Bits           // 255 63

    // [ Flags ]
    protected static final int LandBit = (1 << 10);         //>>	11th Bit, 1024          // 256 64
    protected static final int IslandBit = (1 << 11);       //>>	12th Bit, 2048          // 4096 1024
    protected static final int IceBit = (1 << 12);          //>>	13th Bit, 4096

    // [ Biome Group Data ]
    protected static final int BiomeGroupShift = 13;        //>>	Shift amount for biome group data
    protected static final int BiomeGroupBits = (127 << BiomeGroupShift);   //>>	14th-20th Bits, 1040384

    // [ River Data ]
    protected static final int RiverShift = 20;
    protected static final int RiverBits = (3 << RiverShift);               //>>	21st-22nd Bits, 3145728  //3072 768
    protected static final int RiverBitOne = (1 << RiverShift);             //>>	21st Bit, 1048576
    protected static final int RiverBitTwo = (1 << (RiverShift + 1));       //>>	22nd Bit, 2097152

    /**
     * In a single step, checks for land and when present returns biome data
     * @param selection The location to be checked
     * @return Biome Data or 0 when not on land
     */
    protected static int getBiomeFromLayer(int selection)
    {
        return (selection & LandBit) != 0 ? (selection & BiomeBits) : 0;
    }

    private static long getScrambledBaseSeed(long baseSeed)
    {
        long scrambledBaseSeed = baseSeed;
        scrambledBaseSeed *= (scrambledBaseSeed * 6364136223846793005L + 1442695040888963407L);
        scrambledBaseSeed += baseSeed;
        scrambledBaseSeed *= (scrambledBaseSeed * 6364136223846793005L + 1442695040888963407L);
        scrambledBaseSeed += baseSeed;
        scrambledBaseSeed *= (scrambledBaseSeed * 6364136223846793005L + 1442695040888963407L);
        scrambledBaseSeed += baseSeed;
        return scrambledBaseSeed;
    }

    protected static long getScrambledWorldSeed(long baseSeed, long worldSeed)
    {
        long scrambledBaseSeed = getScrambledBaseSeed(baseSeed);
        long scrambledWorldSeed = worldSeed;
        scrambledWorldSeed *= (scrambledWorldSeed * 6364136223846793005L + 1442695040888963407L);
        scrambledWorldSeed += scrambledBaseSeed;
        scrambledWorldSeed *= (scrambledWorldSeed * 6364136223846793005L + 1442695040888963407L);
        scrambledWorldSeed += scrambledBaseSeed;
        scrambledWorldSeed *= (scrambledWorldSeed * 6364136223846793005L + 1442695040888963407L);
        scrambledWorldSeed += scrambledBaseSeed;
        return scrambledWorldSeed;
    }

    protected Layer(long seed)
    {
        this.baseSeed = seed;
    }

    public Layer()
    {
    }



    public void initWorldGenSeed(long worldSeed)
    {
        if (this.child != null)
            this.child.initWorldGenSeed(worldSeed);

        this.scrambledWorldSeed = getScrambledWorldSeed(this.baseSeed, worldSeed);
    }

    protected void initChunkSeed(long x, long z)
    {
        this.scrambledChunkSeed = this.scrambledWorldSeed;
        this.scrambledChunkSeed *= (this.scrambledChunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.scrambledChunkSeed += x;
        this.scrambledChunkSeed *= (this.scrambledChunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.scrambledChunkSeed += z;
        this.scrambledChunkSeed *= (this.scrambledChunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.scrambledChunkSeed += x;
        this.scrambledChunkSeed *= (this.scrambledChunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.scrambledChunkSeed += z;
    }

    protected void initGroupSeed(long x, long z)
    {
        this.scrambledGroupSeed = this.scrambledChunkSeed;
        this.scrambledGroupSeed *= (this.scrambledGroupSeed * 6364136223846793005L + 1442695040888963407L);
        this.scrambledGroupSeed += x;
        this.scrambledGroupSeed *= (this.scrambledGroupSeed * 6364136223846793005L + 1442695040888963407L);
        this.scrambledGroupSeed += z;
        this.scrambledGroupSeed *= (this.scrambledGroupSeed * 6364136223846793005L + 1442695040888963407L);
        this.scrambledGroupSeed += x;
        this.scrambledGroupSeed *= (this.scrambledGroupSeed * 6364136223846793005L + 1442695040888963407L);
        this.scrambledGroupSeed += z;
    }

    protected int nextInt(int x)
    {
        int i = (int) ((this.scrambledChunkSeed >> 24) % x);
        if (i < 0)
            i += x;
        this.scrambledChunkSeed *= (this.scrambledChunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.scrambledChunkSeed += this.scrambledWorldSeed;
        return i;
    }

    protected int nextGroupInt(int x)
    {
        int i = (int) ((this.scrambledGroupSeed >> 24) % x);
        if (i < 0)
            i += x;
        this.scrambledGroupSeed *= (this.scrambledGroupSeed * 6364136223846793005L + 1442695040888963407L);
        this.scrambledGroupSeed += this.scrambledChunkSeed;
        return i;
    }

    public abstract int[] getInts(ArraysCache cache, int x, int z, int xSize, int zSize);

    protected int getRandomInArray(int... biomes)
    {
        return biomes[this.nextInt(biomes.length)];
    }

    protected int getRandomOf4(int a, int b, int c, int d)
    {
        return b == c && c == d
               ? b
               : (a == b && a == c
                  ? a
                  : (a == b && a == d
                     ? a
                     : (a == c && a == d
                        ? a
                        : (a == b && c != d
                           ? a
                           : (a == c && b != d
                              ? a
                              : (a == d && b != c
                                 ? a
                                 : (b == c && a != d
                                    ? b
                                    : (b == d && a != c
                                       ? b
                                       : (c == d && a != b
                                          ? c
                                          : this.getRandomInArray(new int[]
                                          {
                                              a, b, c, d
        }))))))))));
    }

}
