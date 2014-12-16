package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.*;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for creating instances of {@link Layer} tailored towards
 * biome modes.
 *
 */
public final class LayerFactory
{
    private LayerFactory()
    {
        // No instances
    }

    /**
     * Creates a pair of layers for use with the normal biome mode.
     * @param world
     * @return
     */
    public static Layer[] createNormal(LocalWorld world)
    {
        Layer mainLayer = initMainLayer(world);

        Layer zoomedLayer = new LayerZoomVoronoi(10L, mainLayer);
        zoomedLayer.initWorldGenSeed(world.getSeed());

        return new Layer[] {mainLayer, zoomedLayer};
    }

    /**
     * Creates a pair of layers for use with FromImage biome mode.
     * @param world The world to create layers for.
     * @return The pair.
     */
    public static Layer[] createFromImage(LocalWorld world)
    {
        WorldConfig worldConfig = world.getConfigs().getWorldConfig();
        Layer mainLayer = initMainLayer(world);
        if (worldConfig.imageMode == WorldConfig.ImageMode.ContinueNormal)
        {
            mainLayer = new LayerFromImage(1L, mainLayer, worldConfig, world);
        } else
        {
            mainLayer = new LayerFromImage(1L, null, worldConfig, world);
        }

        Layer zoomedLayer = new LayerZoomVoronoi(10L, mainLayer);

        zoomedLayer.initWorldGenSeed(world.getSeed());

        return new Layer[] {mainLayer, zoomedLayer};
    }

    /**
     * Creates a pair of layers for use with the BeforeGroups biome mode.
     * @param world The world to create layers for.
     * @return The pair.
     */
    public static Layer[] createBeforeGroups(LocalWorld world)
    {

        /*
         * int BigLandSize = 2; //default 0, more - smaller int
         * ChanceToIncreaseLand = 6; //default 4 int MaxDepth = 10;
         */
        ConfigProvider configs = world.getConfigs();
        WorldConfig worldConfig = configs.getWorldConfig();
        BiomeGroupManager worldGroupManager = worldConfig.biomeGroupManager;

        BiomeGroup normalGroup = worldGroupManager.getGroupByName(WorldStandardValues.NORMAL_BIOMES.getName());
        BiomeGroup iceGroup = worldGroupManager.getGroupByName(WorldStandardValues.ICE_BIOMES.getName());
        if (normalGroup == null)
        {
            normalGroup = BiomeGroup.createNormalGroup(worldConfig);
            normalGroup.processBiomeData(world);
        }
        if (iceGroup == null)
        {
            iceGroup = BiomeGroup.createIceGroup(worldConfig);
            iceGroup.processBiomeData(world);
        }

        LocalBiome[][] normalBiomeMap = new LocalBiome[worldConfig.GenerationDepth + 1][];
        LocalBiome[][] iceBiomeMap = new LocalBiome[worldConfig.GenerationDepth + 1][];

        for (int i = 0; i < worldConfig.GenerationDepth + 1; i++)
        {
            List<LocalBiome> normalBiomes = new ArrayList<LocalBiome>();
            List<LocalBiome> iceBiomes = new ArrayList<LocalBiome>();
            for (LocalBiome biome : configs.getBiomeArray())
            {
                if (biome == null)
                    continue;

                BiomeConfig biomeConfig = biome.getBiomeConfig();

                if (biomeConfig.biomeSize != i)
                    continue;
                if (normalGroup.containsBiome(biomeConfig.getName()))
                {
                    for (int t = 0; t < biomeConfig.biomeRarity; t++)
                        normalBiomes.add(biome);
                    normalGroup.totalGroupRarity -= biomeConfig.biomeRarity;
                }

                if (iceGroup.containsBiome(biomeConfig.getName()))
                {
                    for (int t = 0; t < biomeConfig.biomeRarity; t++)
                        iceBiomes.add(biome);
                    iceGroup.totalGroupRarity -= biomeConfig.biomeRarity;
                }

            }

            if (!normalBiomes.isEmpty())
                normalBiomeMap[i] = normalBiomes.toArray(new LocalBiome[normalBiomes.size() + normalGroup.totalGroupRarity]);
            else
                normalBiomeMap[i] = new LocalBiome[0];

            if (!iceBiomes.isEmpty())
                iceBiomeMap[i] = iceBiomes.toArray(new LocalBiome[iceBiomes.size() + iceGroup.totalGroupRarity]);
            else
                iceBiomeMap[i] = new LocalBiome[0];

        }

        Layer mainLayer = new LayerEmpty(1L);

        Layer RiverLayer = new LayerEmpty(1L);
        boolean riversStarted = false;

        for (int depth = 0; depth <= worldConfig.GenerationDepth; depth++)
        {

            mainLayer = new LayerZoom(2001 + depth, mainLayer);

            if (worldConfig.randomRivers && riversStarted)
                RiverLayer = new LayerZoom(2001 + depth, RiverLayer);

            if (worldConfig.LandSize == depth)
            {
                mainLayer = new LayerLand(1L, mainLayer, worldConfig.LandRarity);
                mainLayer = new LayerZoomFuzzy(2000L, mainLayer);
            }

            if (depth < (worldConfig.LandSize + worldConfig.LandFuzzy))
                mainLayer = new LayerLandRandom(depth, mainLayer);

            if (normalBiomeMap[depth].length != 0 || iceBiomeMap[depth].length != 0)
                mainLayer = new LayerBiomeBeforeGroups(200, mainLayer, normalBiomeMap[depth], iceBiomeMap[depth]);

            if (iceGroup.getGenerationDepth() == depth)
                mainLayer = new LayerIce(depth, mainLayer, iceGroup.getGroupRarity());

            if (worldConfig.riverRarity == depth)
                if (worldConfig.randomRivers)
                {
                    RiverLayer = new LayerRiverInit(155, RiverLayer);
                    riversStarted = true;
                } else
                    mainLayer = new LayerRiverInit(155, mainLayer);

            if ((worldConfig.GenerationDepth - worldConfig.riverSize) == depth)
            {
                if (worldConfig.randomRivers)
                    RiverLayer = new LayerRiver(5 + depth, RiverLayer);
                else
                    mainLayer = new LayerRiver(5 + depth, mainLayer);
            }

            LayerBiomeBorder layerBiomeBorder = new LayerBiomeBorder(3000 + depth, world);
            boolean haveBorder = false;
            for (LocalBiome biome : configs.getBiomeArray())
            {
                if (biome == null)
                    continue;

                BiomeConfig biomeConfig = biome.getBiomeConfig();
                if (biomeConfig.biomeSize != depth)
                    continue;

                if (worldConfig.IsleBiomes.contains(biomeConfig.getName()) && biomeConfig.isleInBiome != null)
                {
                    int id = biome.getIds().getGenerationId();

                    LayerBiomeInBiome layerBiome = new LayerBiomeInBiome(4000 + id, mainLayer);
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
                    mainLayer = layerBiome;
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
                layerBiomeBorder.child = mainLayer;
                mainLayer = layerBiomeBorder;
            }
        }

        if (worldConfig.randomRivers)
            mainLayer = new LayerMixWithRiver(1L, mainLayer, RiverLayer, configs, world);
        else
            mainLayer = new LayerMix(1L, mainLayer, configs, world);

        mainLayer = new LayerSmooth(400L, mainLayer);

        if (worldConfig.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
        {

            if (worldConfig.imageMode == WorldConfig.ImageMode.ContinueNormal)
                mainLayer = new LayerFromImage(1L, mainLayer, worldConfig, world);
            else
                mainLayer = new LayerFromImage(1L, null, worldConfig, world);
        }

        Layer zoomedLayer = new LayerZoomVoronoi(10L, mainLayer);

        zoomedLayer.initWorldGenSeed(world.getSeed());

        return new Layer[] {mainLayer, zoomedLayer};
    }

    private static Layer initMainLayer(LocalWorld world)
    {
        ConfigProvider configs = world.getConfigs();
        WorldConfig worldConfig = configs.getWorldConfig();
        BiomeGroupManager groupManager = worldConfig.biomeGroupManager;

        Layer mainLayer = new LayerEmpty(1L);
        Layer RiverLayer = new LayerEmpty(1L);

        boolean riversStarted = false;

        for (int depth = 0; depth <= worldConfig.GenerationDepth; depth++)
        {

            mainLayer = new LayerZoom(2001 + depth, mainLayer);

            if (worldConfig.randomRivers && riversStarted)
                RiverLayer = new LayerZoom(2001 + depth, RiverLayer);

            if (worldConfig.LandSize == depth)
            {
                mainLayer = new LayerLand(1L, mainLayer, worldConfig.LandRarity);
                mainLayer = new LayerZoomFuzzy(2000L, mainLayer);
            }

            if (depth < (worldConfig.LandSize + worldConfig.LandFuzzy))
            {
                mainLayer = new LayerLandRandom(depth, mainLayer);
            }

            if (!groupManager.isGroupDepthMapEmpty(depth))
            {
                mainLayer = new LayerBiomeGroups(mainLayer, groupManager, depth, worldConfig.FreezeAllColdGroupBiomes);
            }

            if (!groupManager.isBiomeDepthMapEmpty(depth))
            {
                mainLayer = new LayerBiome(200, mainLayer, groupManager, depth, worldConfig.FrozenOceanTemperature);
            }

            if (depth == 3)
            {
                mainLayer = new LayerIce(depth, mainLayer);
            }

            if (worldConfig.riverRarity == depth)
            {
                if (worldConfig.randomRivers)
                {
                    RiverLayer = new LayerRiverInit(155, RiverLayer);
                    riversStarted = true;
                } else
                    mainLayer = new LayerRiverInit(155, mainLayer);
            }

            if ((worldConfig.GenerationDepth - worldConfig.riverSize) == depth)
            {
                if (worldConfig.randomRivers)
                    RiverLayer = new LayerRiver(5 + depth, RiverLayer);
                else
                    mainLayer = new LayerRiver(5 + depth, mainLayer);
            }

            LayerBiomeBorder layerBiomeBorder = new LayerBiomeBorder(3000 + depth, world);
            boolean haveBorder = false;
            for (LocalBiome biome : configs.getBiomeArray())
            {
                if (biome == null)
                    continue;

                BiomeConfig biomeConfig = biome.getBiomeConfig();
                if (biomeConfig.biomeSize != depth)
                    continue;

                if (worldConfig.IsleBiomes.contains(biomeConfig.getName()) && biomeConfig.isleInBiome != null)
                {
                    int id = biome.getIds().getGenerationId();

                    LayerBiomeInBiome layerBiome = new LayerBiomeInBiome(4000 + id, mainLayer);
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
                    mainLayer = layerBiome;
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
                layerBiomeBorder.child = mainLayer;
                mainLayer = layerBiomeBorder;
            }
        }

        if (worldConfig.randomRivers)
            mainLayer = new LayerMixWithRiver(1L, mainLayer, RiverLayer, configs, world);
        else
            mainLayer = new LayerMix(1L, mainLayer, configs, world);

        mainLayer = new LayerSmooth(400L, mainLayer);

        return mainLayer;
    }
}
