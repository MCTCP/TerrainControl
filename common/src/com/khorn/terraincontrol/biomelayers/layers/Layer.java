package com.khorn.terraincontrol.biomelayers.layers;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;

import java.util.ArrayList;

public abstract class Layer
{
    protected long b;
    protected Layer child;
    private long c;
    protected long d;


    /*

   LayerIsland - chance to big land
   LayerLandRandom - a(3) - chance to increase big land
   GenLayerIcePlains - chance to ice
   GenLayerMushroomIsland - chance to mushroom island




   biome:
   1) is island
   2) size
   3) chance
   4) is shore
   5) color
   6) temperature
   7) downfall
   8) is snow biome
   9) Have rivers


   world
   1) chance to lands
   2) size of big lands
   3) chance to increase lands
   4) Chance for ice area
   5) Ice area size
   6) Rivers
   7) Rivers size





    */


    protected static final int BiomeBits = 255; // 255 63
    protected static final int LandBit = 256;   // 256 64
    protected static final int RiverBits = 3072; //3072 768
    protected static final int IceBit = 512;   // 512  128
    protected static final int IslandBit = 4096; // 4096 1024

    protected static int GetBiomeFromLayer(int BiomeAndLand)
    {
        if ((BiomeAndLand & LandBit) != 0)
            return (BiomeAndLand & BiomeBits);
        return 0;
    }

    public static Layer[] Init(long paramLong, LocalWorld world)
    {

        /*int BigLandSize = 2;  //default 0, more - smaller
        int ChanceToIncreaseLand = 6; //default 4
        int MaxDepth = 10;     */

        WorldConfig config = world.getSettings();

        LocalBiome[][] NormalBiomeMap = new LocalBiome[config.GenerationDepth + 1][];
        LocalBiome[][] IceBiomeMap = new LocalBiome[config.GenerationDepth + 1][];


        for (int i = 0; i < config.GenerationDepth + 1; i++)
        {
            ArrayList<LocalBiome> normalBiomes = new ArrayList<LocalBiome>();
            ArrayList<LocalBiome> iceBiomes = new ArrayList<LocalBiome>();
            for (BiomeConfig biomeConfig : config.biomeConfigs)
            {
                if (biomeConfig == null)
                    continue;

                if (biomeConfig.BiomeSize != i)
                    continue;
                if (config.NormalBiomes.contains(biomeConfig.name))
                {
                    for (int t = 0; t < biomeConfig.BiomeRarity; t++)
                        normalBiomes.add(biomeConfig.Biome);
                    config.normalBiomesRarity -= biomeConfig.BiomeRarity;
                }

                if (config.IceBiomes.contains(biomeConfig.name))
                {
                    for (int t = 0; t < biomeConfig.BiomeRarity; t++)
                        iceBiomes.add(biomeConfig.Biome);
                    config.iceBiomesRarity -= biomeConfig.BiomeRarity;
                }

            }

            if (normalBiomes.size() != 0)
                NormalBiomeMap[i] = normalBiomes.toArray(new LocalBiome[normalBiomes.size() + config.normalBiomesRarity]);
            else
                NormalBiomeMap[i] = new LocalBiome[0];

            if (iceBiomes.size() != 0)
                IceBiomeMap[i] = iceBiomes.toArray(new LocalBiome[iceBiomes.size() + config.iceBiomesRarity]);
            else
                IceBiomeMap[i] = new LocalBiome[0];


        }


        Layer MainLayer = new LayerEmpty(1L);


        for (int depth = 0; depth <= config.GenerationDepth; depth++)
        {

            MainLayer = new LayerZoom(2001 + depth, MainLayer);

            if (config.LandSize == depth)
            {
                MainLayer = new LayerLand(1L, MainLayer, config.LandRarity);
                MainLayer = new LayerZoomFuzzy(2000L, MainLayer);
            }

            if (depth < (config.LandSize + config.LandFuzzy))
                MainLayer = new LayerLandRandom(depth, MainLayer);


            if (NormalBiomeMap[depth].length != 0 || IceBiomeMap[depth].length != 0)
            {

                LayerBiome layerBiome = new LayerBiome(200, MainLayer);
                layerBiome.biomes = NormalBiomeMap[depth];
                layerBiome.ice_biomes = IceBiomeMap[depth];
                MainLayer = layerBiome;
            }


            if (config.IceSize == depth)
                MainLayer = new LayerIce(depth, MainLayer, config.IceRarity);

            if (config.RiverRarity == depth)
                MainLayer = new LayerRiverInit(155, MainLayer);

            if ((config.GenerationDepth - config.RiverSize) == depth)
                MainLayer = new LayerRiver(5 + depth, MainLayer);

            LayerBiomeBorder layerBiomeBorder = new LayerBiomeBorder(3000 + depth, world);
            boolean haveBorder = false;
            for (BiomeConfig biomeConfig : config.biomeConfigs)
            {
                if (biomeConfig == null)
                    continue;
                if (biomeConfig.BiomeSize != depth)
                    continue;
                if (config.IsleBiomes.contains(biomeConfig.name) && biomeConfig.IsleInBiome != null)
                {
                    int id = biomeConfig.Biome.getId();
                    if (biomeConfig.Biome.isCustom())
                        id = biomeConfig.Biome.getCustomId();  // Must be decreased by count of new biomes since 1.1 for compatible 1.1 maps

                    LayerBiomeInBiome layerBiome = new LayerBiomeInBiome(4000 + id, MainLayer);
                    layerBiome.biome = biomeConfig.Biome;
                    for (String islandInName : biomeConfig.IsleInBiome)
                    {
                        int islandIn = world.getBiomeIdByName(islandInName);
                        if (islandIn == DefaultBiome.OCEAN.Id)
                            layerBiome.inOcean = true;
                        else
                            layerBiome.BiomeIsles[islandIn] = true;
                    }

                    layerBiome.chance = (config.BiomeRarityScale + 1) - biomeConfig.BiomeRarity;
                    MainLayer = layerBiome;
                }

                if (config.BorderBiomes.contains(biomeConfig.name) && biomeConfig.BiomeIsBorder != null)
                {
                    haveBorder = true;

                    for (String replaceFromName : biomeConfig.BiomeIsBorder)
                    {
                        int replaceFrom = world.getBiomeIdByName(replaceFromName);
                        layerBiomeBorder.AddBiome(biomeConfig, replaceFrom, world);

                    }

                }
            }


            if (haveBorder)
            {
                layerBiomeBorder.child = MainLayer;
                MainLayer = layerBiomeBorder;
            }


        }
        MainLayer = new LayerMix(1L, MainLayer, config, world);

        MainLayer = new LayerSmooth(400L, MainLayer);

        if (config.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
        {

            if (config.imageMode == WorldConfig.ImageMode.ContinueNormal)
                MainLayer = new LayerFromImage(1L, MainLayer, config, world);
            else
                MainLayer = new LayerFromImage(1L, null, config, world);
        }


        Layer ZoomedLayer = new LayerZoomVoronoi(10L, MainLayer);

        //TemperatureLayer = new LayerTemperatureMix(TemperatureLayer, ZoomedLayer, 0, config);

        ZoomedLayer.b(paramLong);

        MainLayer = new LayerCacheInit(1, MainLayer);
        ZoomedLayer = new LayerCacheInit(1, ZoomedLayer);

        return new Layer[]{MainLayer, ZoomedLayer};
    }

    public Layer(long paramLong)
    {
        this.d = paramLong;
        this.d *= (this.d * 6364136223846793005L + 1442695040888963407L);
        this.d += paramLong;
        this.d *= (this.d * 6364136223846793005L + 1442695040888963407L);
        this.d += paramLong;
        this.d *= (this.d * 6364136223846793005L + 1442695040888963407L);
        this.d += paramLong;
    }

    public void b(long paramLong)
    {
        this.b = paramLong;
        if (this.child != null)
            this.child.b(paramLong);
        this.b *= (this.b * 6364136223846793005L + 1442695040888963407L);
        this.b += this.d;
        this.b *= (this.b * 6364136223846793005L + 1442695040888963407L);
        this.b += this.d;
        this.b *= (this.b * 6364136223846793005L + 1442695040888963407L);
        this.b += this.d;
    }

    protected void SetSeed(long paramLong1, long paramLong2)
    {
        this.c = this.b;
        this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
        this.c += paramLong1;
        this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
        this.c += paramLong2;
        this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
        this.c += paramLong1;
        this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
        this.c += paramLong2;
    }

    protected int nextInt(int paramInt)
    {
        int i = (int) ((this.c >> 24) % paramInt);
        if (i < 0)
            i += paramInt;
        this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
        this.c += this.b;
        return i;
    }

    protected abstract int[] GetBiomes(int cacheId, int x, int z, int x_size, int z_size);

    public int[] Calculate(int x, int z, int x_size, int z_size)
    {
        return new int[0];
    }

}
