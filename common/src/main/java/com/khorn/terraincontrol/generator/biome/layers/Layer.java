package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.generator.biome.ArraysCache;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.util.ArrayList;

public abstract class Layer
{

    protected long worldGenSeed;
    protected Layer child;
    private long chunkSeed;
    protected long baseSeed;

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

    protected static int GetBiomeFromLayer(int BiomeAndLand)
    {
        if ((BiomeAndLand & LandBit) != 0)
            return (BiomeAndLand & BiomeBits);
        return 0;
    }

    public static Layer[] Init(long paramLong, LocalWorld world)
    {

        /*
         * int BigLandSize = 2; //default 0, more - smaller
         * int ChanceToIncreaseLand = 6; //default 4
         * int MaxDepth = 10;
         */
        WorldSettings configs = world.getSettings();
        WorldConfig worldConfig = configs.worldConfig;

        LocalBiome[][] NormalBiomeMap = new LocalBiome[worldConfig.GenerationDepth + 1][];
        LocalBiome[][] IceBiomeMap = new LocalBiome[worldConfig.GenerationDepth + 1][];

        for (int i = 0; i < worldConfig.GenerationDepth + 1; i++)
        {
            ArrayList<LocalBiome> normalBiomes = new ArrayList<LocalBiome>();
            ArrayList<LocalBiome> iceBiomes = new ArrayList<LocalBiome>();
            for (LocalBiome biome : configs.biomes)
            {
                if (biome == null)
                    continue;

                BiomeConfig biomeConfig = biome.getBiomeConfig();

                if (biomeConfig.biomeSize != i)
                    continue;
                if (worldConfig.NormalBiomes.contains(biomeConfig.getName()))
                {
                    for (int t = 0; t < biomeConfig.biomeRarity; t++)
                        normalBiomes.add(biome);
                    worldConfig.normalBiomesRarity -= biomeConfig.biomeRarity;
                }

                if (worldConfig.IceBiomes.contains(biomeConfig.getName()))
                {
                    for (int t = 0; t < biomeConfig.biomeRarity; t++)
                        iceBiomes.add(biome);
                    worldConfig.iceBiomesRarity -= biomeConfig.biomeRarity;
                }

            }

            if (!normalBiomes.isEmpty())
                NormalBiomeMap[i] = normalBiomes.toArray(new LocalBiome[normalBiomes.size() + worldConfig.normalBiomesRarity]);
            else
                NormalBiomeMap[i] = new LocalBiome[0];

            if (!iceBiomes.isEmpty())
                IceBiomeMap[i] = iceBiomes.toArray(new LocalBiome[iceBiomes.size() + worldConfig.iceBiomesRarity]);
            else
                IceBiomeMap[i] = new LocalBiome[0];

        }

        Layer MainLayer = new LayerEmpty(1L);

        Layer RiverLayer = new LayerEmpty(1L);
        boolean riversStarted = false;

        for (int depth = 0; depth <= worldConfig.GenerationDepth; depth++)
        {

            MainLayer = new LayerZoom(2001 + depth, MainLayer);

            if (worldConfig.randomRivers && riversStarted)
                RiverLayer = new LayerZoom(2001 + depth, RiverLayer);

            if (worldConfig.LandSize == depth)
            {
                MainLayer = new LayerLand(1L, MainLayer, worldConfig.LandRarity);
                MainLayer = new LayerZoomFuzzy(2000L, MainLayer);
            }

            if (depth < (worldConfig.LandSize + worldConfig.LandFuzzy))
                MainLayer = new LayerLandRandom(depth, MainLayer);

            if (NormalBiomeMap[depth].length != 0 || IceBiomeMap[depth].length != 0)
                MainLayer = new LayerBiome(200, MainLayer, NormalBiomeMap[depth], IceBiomeMap[depth]);

            if (worldConfig.IceSize == depth)
                MainLayer = new LayerIce(depth, MainLayer, worldConfig.IceRarity);

            if (worldConfig.riverRarity == depth)
                if (worldConfig.randomRivers)
                {
                    RiverLayer = new LayerRiverInit(155, RiverLayer);
                    riversStarted = true;
                } else
                    MainLayer = new LayerRiverInit(155, MainLayer);

            if ((worldConfig.GenerationDepth - worldConfig.riverSize) == depth)
            {
                if (worldConfig.randomRivers)
                    RiverLayer = new LayerRiver(5 + depth, RiverLayer);
                else
                    MainLayer = new LayerRiver(5 + depth, MainLayer);
            }

            LayerBiomeBorder layerBiomeBorder = new LayerBiomeBorder(3000 + depth, world);
            boolean haveBorder = false;
            for (LocalBiome biome : configs.biomes)
            {
                if (biome == null)
                    continue;
                
                BiomeConfig biomeConfig = biome.getBiomeConfig();
                if (biomeConfig.biomeSize != depth)
                    continue;
                
                if (worldConfig.IsleBiomes.contains(biomeConfig.getName()) && biomeConfig.isleInBiome != null)
                {
                    int id = biome.getIds().getGenerationId();

                    LayerBiomeInBiome layerBiome = new LayerBiomeInBiome(4000 + id, MainLayer);
                    layerBiome.biome = biome;
                    for (String islandInName : biomeConfig.isleInBiome)
                    {
                        int islandIn = world.getBiomeByName(islandInName).getIds().getGenerationId();
                        if (islandIn == DefaultBiome.OCEAN.Id)
                            layerBiome.inOcean = true;
                        else
                            layerBiome.biomeIsles[islandIn] = true;
                    }

                    layerBiome.chance = (worldConfig.BiomeRarityScale + 1) - biomeConfig.biomeRarity;
                    MainLayer = layerBiome;
                }

                if (worldConfig.BorderBiomes.contains(biomeConfig.getName()) && biomeConfig.biomeIsBorder != null)
                {
                    haveBorder = true;
                    for (String replaceFromName : biomeConfig.biomeIsBorder)
                    {
                        int replaceFrom = world.getBiomeByName(replaceFromName).getIds().getGenerationId();
                        layerBiomeBorder.addBiome(biome, replaceFrom, world);

                    }
                }
            }

            if (haveBorder)
            {
                layerBiomeBorder.child = MainLayer;
                MainLayer = layerBiomeBorder;
            }
        }
        
        if (worldConfig.randomRivers)
            MainLayer = new LayerMixWithRiver(1L, MainLayer, RiverLayer, configs, world);
        else
            MainLayer = new LayerMix(1L, MainLayer, configs, world);

        MainLayer = new LayerSmooth(400L, MainLayer);

        if (worldConfig.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
        {

            if (worldConfig.imageMode == WorldConfig.ImageMode.ContinueNormal)
                MainLayer = new LayerFromImage(1L, MainLayer, worldConfig, world);
            else
                MainLayer = new LayerFromImage(1L, null, worldConfig, world);
        }
        
        Layer ZoomedLayer = new LayerZoomVoronoi(10L, MainLayer);

        //TemperatureLayer = new LayerTemperatureMix(TemperatureLayer, ZoomedLayer, 0, config);
        
        ZoomedLayer.initWorldGenSeed(paramLong);

        //MainLayer = new LayerCacheInit(1, MainLayer);
        //ZoomedLayer = new LayerCacheInit(1, ZoomedLayer);
        
        return new Layer[]{MainLayer, ZoomedLayer};
    }

    public Layer(long seed)
    {
        this.baseSeed = seed;
        this.baseSeed *= (this.baseSeed * 6364136223846793005L + 1442695040888963407L);
        this.baseSeed += seed;
        this.baseSeed *= (this.baseSeed * 6364136223846793005L + 1442695040888963407L);
        this.baseSeed += seed;
        this.baseSeed *= (this.baseSeed * 6364136223846793005L + 1442695040888963407L);
        this.baseSeed += seed;
    }

    public void initWorldGenSeed(long seed)
    {
        this.worldGenSeed = seed;
        if (this.child != null)
            this.child.initWorldGenSeed(seed);
        this.worldGenSeed *= (this.worldGenSeed * 6364136223846793005L + 1442695040888963407L);
        this.worldGenSeed += this.baseSeed;
        this.worldGenSeed *= (this.worldGenSeed * 6364136223846793005L + 1442695040888963407L);
        this.worldGenSeed += this.baseSeed;
        this.worldGenSeed *= (this.worldGenSeed * 6364136223846793005L + 1442695040888963407L);
        this.worldGenSeed += this.baseSeed;
    }

    protected void initChunkSeed(long x, long z)
    {
        this.chunkSeed = this.worldGenSeed;
        this.chunkSeed *= (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.chunkSeed += x;
        this.chunkSeed *= (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.chunkSeed += z;
        this.chunkSeed *= (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.chunkSeed += x;
        this.chunkSeed *= (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.chunkSeed += z;
    }

    protected int nextInt(int x)
    {
        int i = (int) ((this.chunkSeed >> 24) % x);
        if (i < 0)
            i += x;
        this.chunkSeed *= (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.chunkSeed += this.worldGenSeed;
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
