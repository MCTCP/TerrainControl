package com.Khorn.TerrainControl.BiomeManager.Layers;

import com.Khorn.TerrainControl.Configuration.BiomeConfig;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import net.minecraft.server.*;

import java.util.ArrayList;

public abstract class Layer
{
    private long b;
    protected Layer a;
    private long c;
    private long d;


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


    protected static final int BiomeBits = 63;
    protected static final int LandBit = 64;
    protected static final int RiverBits = 768;
    protected static final int IceBit = 128;
    protected static final int IslandBit = 1024;

    public static Layer[] a(long paramLong, WorldConfig config)
    {

        /*int BigLandSize = 2;  //default 0, more - smaller
        int ChanceToIncreaseLand = 6; //default 4
        int MaxDepth = 10;     */

        ArrayList<BiomeConfig> BiomeMap = new ArrayList<BiomeConfig>();
        ArrayList<BiomeConfig> BiomeIceMap = new ArrayList<BiomeConfig>();


        for (BiomeConfig biomeConfig : config.biomeConfigs)
        {
            if (biomeConfig.IsNormalBiome)
            {
                if (biomeConfig.IceBiome)
                    BiomeMap.add(biomeConfig);
                else
                    BiomeIceMap.add(biomeConfig);

            }

        }


        Layer MainLayer = new LayerEmpty(1L);


        for (int depth = 0; depth < config.GenerationDepth; depth++)
        {
            MainLayer = new LayerZoom(2001 + depth, MainLayer);

            if (config.LandSize == depth)
            {
                MainLayer = new LayerLand(1L, MainLayer, config.LandRarity);
                MainLayer = new LayerZoomFuzzy(2000L, MainLayer);
            }

            if (depth < (config.LandSize + config.LandFuzzy))
                MainLayer = new LayerLandRandom(depth, MainLayer);

            ArrayList<BiomeBase> biomes = new ArrayList<BiomeBase>();
            ArrayList<BiomeBase> iceBiomes = new ArrayList<BiomeBase>();

            int i = 0;
            while (i < BiomeMap.size())
            {
                BiomeConfig biomeConfig = BiomeMap.get(i);
                if (biomeConfig.BiomeSize == depth)
                {
                    for (int t = 0; t < biomeConfig.BiomeRarity; t++)
                        biomes.add(biomeConfig.Biome);

                    BiomeMap.remove(biomeConfig);
                    continue;

                }

                for (int t = 0; t < biomeConfig.BiomeRarity; t++)
                    biomes.add(null);
                i++;

            }
            i = 0;
            while (i < BiomeIceMap.size())
            {
                BiomeConfig biomeConfig = BiomeIceMap.get(i);
                if (biomeConfig.BiomeSize == depth)
                {
                    for (int t = 0; t < biomeConfig.BiomeRarity; t++)
                        iceBiomes.add(biomeConfig.Biome);

                    BiomeMap.remove(biomeConfig);
                    continue;

                }

                for (int t = 0; t < biomeConfig.BiomeRarity; t++)
                    iceBiomes.add(null);
                i++;

            }


            if (biomes.size() != 0 || iceBiomes.size() != 0)
            {

                BiomeBase[] biomesArray = new BiomeBase[biomes.size()];
                LayerBiome layerBiome = new LayerBiome(200, MainLayer);
                layerBiome.biomes = biomes.toArray(biomesArray);
                layerBiome.ice_biomes = iceBiomes.toArray(biomesArray);
                MainLayer = layerBiome;
            }



            if (config.IceSize == depth)
                MainLayer = new LayerIce(depth, MainLayer,config.IceRarity);

            if (config.RiverRarity == depth)
                MainLayer = new LayerRiverInit(100, MainLayer);

            if ((config.GenerationDepth-config.RiverRarity) == depth)
                MainLayer = new LayerRiver(1 + depth, MainLayer);

            for(BiomeConfig biomeConfig : config.biomeConfigs)
            {



            }
            if (depth == 5)
                MainLayer = new LayerBiomeInBiome(4000 + depth, MainLayer);

            if (depth == 8)
                MainLayer = new LayerBiomeBorder(3000 + depth, MainLayer);

        }
        MainLayer = new LayerMix(1L, MainLayer);

        MainLayer = new LayerSmooth(400L, MainLayer);

        Layer TemperatureLayer = new LayerTemperature(MainLayer);
        TemperatureLayer = new LayerTemperatureMix(TemperatureLayer, MainLayer, 1);
        TemperatureLayer = LayerSmoothZoom.a(1000L, TemperatureLayer, 2);

        Layer DownfallLayer = MainLayer;


        Layer ZoomedLayer = new LayerZoomVoronoi(10L, MainLayer);

        ZoomedLayer.b(paramLong);
        TemperatureLayer.b(paramLong);

        return new Layer[]{MainLayer, ZoomedLayer, TemperatureLayer, DownfallLayer};
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
        if (this.a != null)
            this.a.b(paramLong);
        this.b *= (this.b * 6364136223846793005L + 1442695040888963407L);
        this.b += this.d;
        this.b *= (this.b * 6364136223846793005L + 1442695040888963407L);
        this.b += this.d;
        this.b *= (this.b * 6364136223846793005L + 1442695040888963407L);
        this.b += this.d;
    }

    public void a(long paramLong1, long paramLong2)
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

    protected int a(int paramInt)
    {
        int i = (int) ((this.c >> 24) % paramInt);
        if (i < 0)
            i += paramInt;
        this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
        this.c += this.b;
        return i;
    }

    public abstract int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
}