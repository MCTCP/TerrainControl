package com.khorn.terraincontrol.configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.DefaultMobType;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.UseBiome;
import com.khorn.terraincontrol.generator.resourcegens.AboveWaterGen;
import com.khorn.terraincontrol.generator.resourcegens.CactusGen;
import com.khorn.terraincontrol.generator.resourcegens.CustomObjectGen;
import com.khorn.terraincontrol.generator.resourcegens.DungeonGen;
import com.khorn.terraincontrol.generator.resourcegens.GrassGen;
import com.khorn.terraincontrol.generator.resourcegens.LiquidGen;
import com.khorn.terraincontrol.generator.resourcegens.OreGen;
import com.khorn.terraincontrol.generator.resourcegens.PlantGen;
import com.khorn.terraincontrol.generator.resourcegens.ReedGen;
import com.khorn.terraincontrol.generator.resourcegens.Resource;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import com.khorn.terraincontrol.generator.resourcegens.SmallLakeGen;
import com.khorn.terraincontrol.generator.resourcegens.TreeGen;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;
import com.khorn.terraincontrol.generator.resourcegens.UnderWaterOreGen;
import com.khorn.terraincontrol.generator.resourcegens.UndergroundLakeGen;
import com.khorn.terraincontrol.generator.resourcegens.VinesGen;
import com.khorn.terraincontrol.util.Txt;

public class BiomeConfig extends ConfigFile
{
    public short[][] ReplaceMatrixBlocks = new short[256][];
    public int ReplaceCount = 0;

    public int BiomeSize;
    public int BiomeRarity;

    public String BiomeColor;

    public ArrayList<String> BiomeIsBorder;
    public ArrayList<String> IsleInBiome;
    public ArrayList<String> NotBorderNear;

    // Surface config
    public float BiomeHeight;
    public float BiomeVolatility;

    public float BiomeTemperature;
    public float BiomeWetness;

    public boolean BiomeRivers;

    public byte SurfaceBlock;
    public byte GroundBlock;

    public String ReplaceBiomeName;

    public boolean UseWorldWaterLevel;
    public int waterLevelMax;
    public int waterLevelMin;
    public int waterBlock;
    public int iceBlock;

    public int SkyColor;
    public int WaterColor;

    public int GrassColor;
    public boolean GrassColorIsMultiplier;
    public int FoliageColor;
    public boolean FoliageColorIsMultiplier;

    public Resource[] ResourceSequence = new Resource[256];
    public SaplingGen[] SaplingTypes = new SaplingGen[4];
    public SaplingGen SaplingResource = null;

    public ArrayList<CustomObject> biomeObjects;
    public ArrayList<String> biomeObjectStrings;

    public double maxAverageHeight;
    public double maxAverageDepth;
    public double volatility1;
    public double volatility2;
    public double volatilityWeight1;
    public double volatilityWeight2;
    private double volatilityRaw1;
    private double volatilityRaw2;
    private double volatilityWeightRaw1;
    private double volatilityWeightRaw2;
    public boolean disableNotchHeightControl;
    public double[] heightMatrix;

    public int ResourceCount = 0;

    public LocalBiome Biome;

    public WorldConfig worldConfig;
    public String Name;

    // Spawn Config
    public boolean spawnMonstersAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnMonsters = new ArrayList<WeightedMobSpawnGroup>();
    public boolean spawnCreaturesAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnCreatures = new ArrayList<WeightedMobSpawnGroup>();
    public boolean spawnWaterCreaturesAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnWaterCreatures = new ArrayList<WeightedMobSpawnGroup>();

    public BiomeConfig(File settingsDir, LocalBiome biome, WorldConfig config)
    {
        this.Biome = biome;
        this.Name = biome.getName();
        worldConfig = config;
        InitDefaults();

        File settingsFile = new File(settingsDir, this.Name + TCDefaultValues.WorldBiomeConfigName.stringValue());

        this.ReadSettingsFile(settingsFile);
        this.RenameOldSettings();
        this.ReadConfigSettings();

        this.CorrectSettings();
        if (!settingsFile.exists())
            this.CreateDefaultResources();
        if (config.SettingsMode != WorldConfig.ConfigMode.WriteDisable)
            this.WriteSettingsFile(settingsFile, (config.SettingsMode == WorldConfig.ConfigMode.WriteAll));

        if (this.UseWorldWaterLevel)
        {
            this.waterLevelMax = worldConfig.waterLevelMax;
            this.waterLevelMin = worldConfig.waterLevelMin;
            this.waterBlock = worldConfig.waterBlock;
            this.iceBlock = worldConfig.iceBlock;
        }

        if (biome.isCustom())
            biome.setVisuals(this);
    }

    public int getTemperature()
    {
        return (int) (this.BiomeTemperature * 65536.0F);
    }

    public int getWetness()
    {
        return (int) (this.BiomeWetness * 65536.0F);
    }

    private void CreateDefaultResources()
    {
        Resource resource;

        // Small lakes
        resource = Resource.createResource(worldConfig, SmallLakeGen.class, DefaultMaterial.WATER.id, TCDefaultValues.SmallLakeWaterFrequency.intValue(), TCDefaultValues.SmallLakeWaterRarity.intValue(), TCDefaultValues.SmallLakeMinAltitude.intValue(), TCDefaultValues.SmallLakeMaxAltitude.intValue());
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Small lakes
        resource = Resource.createResource(worldConfig, SmallLakeGen.class, DefaultMaterial.LAVA.id, TCDefaultValues.SmallLakeLavaFrequency.intValue(), TCDefaultValues.SmallLakeLavaRarity.intValue(), TCDefaultValues.SmallLakeMinAltitude.intValue(), TCDefaultValues.SmallLakeMaxAltitude.intValue());
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Underground lakes
        resource = Resource.createResource(worldConfig, UndergroundLakeGen.class, TCDefaultValues.undergroundLakeMinSize.intValue(), TCDefaultValues.undergroundLakeMaxSize.intValue(), TCDefaultValues.undergroundLakeFrequency.intValue(), TCDefaultValues.undergroundLakeRarity.intValue(), TCDefaultValues.undergroundLakeMinAltitude.intValue(), TCDefaultValues.undergroundLakeMaxAltitude.intValue());
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Dungeon
        resource = Resource.createResource(worldConfig, DungeonGen.class, TCDefaultValues.dungeonFrequency.intValue(), TCDefaultValues.dungeonRarity.intValue(), TCDefaultValues.dungeonMinAltitude.intValue(), this.worldConfig.WorldHeight);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Dirt
        resource = Resource.createResource(worldConfig, OreGen.class, DefaultMaterial.DIRT.id, TCDefaultValues.dirtDepositSize.intValue(), TCDefaultValues.dirtDepositFrequency.intValue(), TCDefaultValues.dirtDepositRarity.intValue(), TCDefaultValues.dirtDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Gravel
        resource = Resource.createResource(worldConfig, OreGen.class, DefaultMaterial.GRAVEL.id, TCDefaultValues.gravelDepositSize.intValue(), TCDefaultValues.gravelDepositFrequency.intValue(), TCDefaultValues.gravelDepositRarity.intValue(), TCDefaultValues.gravelDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Clay
        resource = Resource.createResource(worldConfig, OreGen.class, DefaultMaterial.CLAY.id, TCDefaultValues.clayDepositSize.intValue(), TCDefaultValues.clayDepositFrequency.intValue(), TCDefaultValues.clayDepositRarity.intValue(), TCDefaultValues.clayDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Coal
        resource = Resource.createResource(worldConfig, OreGen.class, DefaultMaterial.COAL_ORE.id, TCDefaultValues.coalDepositSize.intValue(), TCDefaultValues.coalDepositFrequency.intValue(), TCDefaultValues.coalDepositRarity.intValue(), TCDefaultValues.coalDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Iron
        resource = Resource.createResource(worldConfig, OreGen.class, DefaultMaterial.IRON_ORE.id, TCDefaultValues.ironDepositSize.intValue(), TCDefaultValues.ironDepositFrequency.intValue(), TCDefaultValues.ironDepositRarity.intValue(), TCDefaultValues.ironDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 2, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Gold
        resource = Resource.createResource(worldConfig, OreGen.class, DefaultMaterial.GOLD_ORE.id, TCDefaultValues.goldDepositSize.intValue(), TCDefaultValues.goldDepositFrequency.intValue(), TCDefaultValues.goldDepositRarity.intValue(), TCDefaultValues.goldDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 4, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Redstone
        resource = Resource.createResource(worldConfig, OreGen.class, DefaultMaterial.REDSTONE_ORE.id, TCDefaultValues.redstoneDepositSize.intValue(), TCDefaultValues.redstoneDepositFrequency.intValue(), TCDefaultValues.redstoneDepositRarity.intValue(), TCDefaultValues.redstoneDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Diamond
        resource = Resource.createResource(worldConfig, OreGen.class, DefaultMaterial.DIAMOND_ORE.id, TCDefaultValues.diamondDepositSize.intValue(), TCDefaultValues.diamondDepositFrequency.intValue(), TCDefaultValues.diamondDepositRarity.intValue(), TCDefaultValues.diamondDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Lapislazuli
        resource = Resource.createResource(worldConfig, OreGen.class, DefaultMaterial.LAPIS_ORE.id, TCDefaultValues.lapislazuliDepositSize.intValue(), TCDefaultValues.lapislazuliDepositFrequency.intValue(), TCDefaultValues.lapislazuliDepositRarity.intValue(), TCDefaultValues.lapislazuliDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        DefaultBiome biome = DefaultBiome.getBiome(this.Biome.getId());

        if (biome != null && (biome == DefaultBiome.EXTREME_HILLS || biome == DefaultBiome.SMALL_MOUNTAINS))
        {
            resource = Resource.createResource(worldConfig, OreGen.class, DefaultMaterial.EMERALD_ORE.id, TCDefaultValues.emeraldDepositSize.intValue(), TCDefaultValues.emeraldDepositFrequency.intValue(), TCDefaultValues.emeraldDepositRarity.intValue(), TCDefaultValues.emeraldDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 4, DefaultMaterial.STONE.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        // Under water sand
        resource = Resource.createResource(worldConfig, UnderWaterOreGen.class, DefaultMaterial.SAND.id, TCDefaultValues.waterSandDepositSize.intValue(), TCDefaultValues.waterSandDepositFrequency.intValue(), TCDefaultValues.waterSandDepositRarity.intValue(), DefaultMaterial.DIRT.id, DefaultMaterial.GRASS.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Under water clay
        if (this.DefaultClay > 0)
        {
            resource = Resource.createResource(worldConfig, UnderWaterOreGen.class, DefaultMaterial.CLAY.id, TCDefaultValues.waterClayDepositSize.intValue(), this.DefaultClay, TCDefaultValues.waterClayDepositRarity.intValue(), DefaultMaterial.DIRT.id, DefaultMaterial.CLAY.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }
        // Custom objects
        resource = Resource.createResource(worldConfig, CustomObjectGen.class, "UseWorld");
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Trees
        if (biome != null)
            switch (biome)
            {
            case OCEAN: // Ocean - default
            case EXTREME_HILLS: // BigHills - default
            case RIVER: // River - default
            case SMALL_MOUNTAINS: // SmallHills
                resource = Resource.createResource(worldConfig, TreeGen.class, this.DefaultTrees, TreeType.BigTree, 1, TreeType.Tree, 9);
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case PLAINS: // Plains - no tree
            case DESERT: // Desert - no tree
            case DESERT_HILLS: // HillsDesert
                break;
            case FOREST_HILLS: // HillsForest
            case FOREST: // Forest - forest
                resource = Resource.createResource(worldConfig, TreeGen.class, this.DefaultTrees, TreeType.Forest, 20, TreeType.BigTree, 10, TreeType.Tree, 100);
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case TAIGA_HILLS: // HillsTaiga
            case TAIGA: // Taiga - taiga
                resource = Resource.createResource(worldConfig, TreeGen.class, this.DefaultTrees, TreeType.Taiga1, 35, TreeType.Taiga2, 100);
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case SWAMPLAND: // Swamp - swamp
                resource = Resource.createResource(worldConfig, TreeGen.class, this.DefaultTrees, TreeType.SwampTree, 100);
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case MUSHROOM_ISLAND: // Mushroom island
                resource = Resource.createResource(worldConfig, TreeGen.class, this.DefaultTrees, TreeType.HugeMushroom, 100);
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;
            case JUNGLE:// Jungle
            case JUNGLE_HILLS:
                resource = Resource.createResource(worldConfig, TreeGen.class, this.DefaultTrees, TreeType.BigTree, 10, TreeType.GroundBush, 50, TreeType.JungleTree, 35, TreeType.CocoaTree, 100);
                this.ResourceSequence[this.ResourceCount++] = resource;
                break;

            }
        if (this.DefaultWaterLily > 0)
        {
            resource = Resource.createResource(worldConfig, AboveWaterGen.class, DefaultMaterial.WATER_LILY.id, this.DefaultWaterLily, 100);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultFlowers > 0)
        {
            // Red flower
            resource = Resource.createResource(worldConfig, PlantGen.class, DefaultMaterial.RED_ROSE.id, this.DefaultFlowers, TCDefaultValues.roseDepositRarity.intValue(), TCDefaultValues.roseDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SOIL.id);
            this.ResourceSequence[this.ResourceCount++] = resource;

            // Yellow flower
            resource = Resource.createResource(worldConfig, PlantGen.class, DefaultMaterial.YELLOW_FLOWER.id, this.DefaultFlowers, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SOIL.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultMushroom > 0)
        {
            // Red mushroom
            resource = Resource.createResource(worldConfig, PlantGen.class, DefaultMaterial.RED_MUSHROOM.id, this.DefaultMushroom, TCDefaultValues.redMushroomDepositRarity.intValue(), TCDefaultValues.redMushroomDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id);
            this.ResourceSequence[this.ResourceCount++] = resource;

            // Brown mushroom
            resource = Resource.createResource(worldConfig, PlantGen.class, DefaultMaterial.BROWN_MUSHROOM.id, this.DefaultMushroom, TCDefaultValues.brownMushroomDepositRarity.intValue(), TCDefaultValues.brownMushroomDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultGrass > 0)
        {
            // Grass
            resource = Resource.createResource(worldConfig, GrassGen.class, DefaultMaterial.LONG_GRASS.id, 1, this.DefaultGrass, TCDefaultValues.longGrassDepositRarity.intValue(), DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultDeadBrush > 0)
        {
            // Dead Bush
            resource = Resource.createResource(worldConfig, GrassGen.class, DefaultMaterial.DEAD_BUSH.id, 0, this.DefaultDeadBrush, TCDefaultValues.deadBushDepositRarity.intValue(), DefaultMaterial.SAND.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        // Pumpkin
        resource = Resource.createResource(worldConfig, PlantGen.class, DefaultMaterial.PUMPKIN.id, TCDefaultValues.pumpkinDepositFrequency.intValue(), TCDefaultValues.pumpkinDepositRarity.intValue(), TCDefaultValues.pumpkinDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        if (this.DefaultReed > 0)
        {
            // Reed
            resource = Resource.createResource(worldConfig, ReedGen.class, DefaultMaterial.SUGAR_CANE_BLOCK.id, this.DefaultReed, TCDefaultValues.reedDepositRarity.intValue(), TCDefaultValues.reedDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SAND.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.DefaultCactus > 0)
        {
            // Cactus
            resource = Resource.createResource(worldConfig, CactusGen.class, DefaultMaterial.CACTUS.id, this.DefaultCactus, TCDefaultValues.cactusDepositRarity.intValue(), TCDefaultValues.cactusDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.SAND.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }
        if (biome == DefaultBiome.JUNGLE || biome == DefaultBiome.JUNGLE_HILLS) 
        {
            resource = Resource.createResource(worldConfig, VinesGen.class, TCDefaultValues.vinesFrequency.intValue(), TCDefaultValues.vinesRarity.intValue(), TCDefaultValues.vinesMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.VINE.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        // Water source
        resource = Resource.createResource(worldConfig, LiquidGen.class, DefaultMaterial.WATER.id, TCDefaultValues.waterSourceDepositFrequency.intValue(), TCDefaultValues.waterSourceDepositRarity.intValue(), TCDefaultValues.waterSourceDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Lava source
        resource = Resource.createResource(worldConfig, LiquidGen.class, DefaultMaterial.LAVA.id, TCDefaultValues.lavaSourceDepositFrequency.intValue(), TCDefaultValues.lavaSourceDepositRarity.intValue(), TCDefaultValues.lavaSourceDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

    }

    protected void ReadConfigSettings()
    {
        this.BiomeSize = ReadModSettings(TCDefaultValues.BiomeSize.name(), this.DefaultSize);
        this.BiomeRarity = ReadModSettings(TCDefaultValues.BiomeRarity.name(), this.DefaultRarity);

        this.BiomeColor = ReadModSettings(TCDefaultValues.BiomeColor.name(), this.DefaultColor);

        this.BiomeRivers = ReadModSettings(TCDefaultValues.BiomeRivers.name(), this.DefaultRiver);

        this.IsleInBiome = ReadModSettings(TCDefaultValues.IsleInBiome.name(), this.DefaultIsle);
        this.BiomeIsBorder = ReadModSettings(TCDefaultValues.BiomeIsBorder.name(), this.DefaultBorder);
        this.NotBorderNear = ReadModSettings(TCDefaultValues.NotBorderNear.name(), this.DefaultNotBorderNear);

        this.BiomeTemperature = ReadModSettings(TCDefaultValues.BiomeTemperature.name(), this.DefaultBiomeTemperature);
        this.BiomeWetness = ReadModSettings(TCDefaultValues.BiomeWetness.name(), this.DefaultBiomeWetness);

        this.ReplaceBiomeName = ReadSettings(TCDefaultValues.ReplaceToBiomeName);

        this.BiomeHeight = ReadModSettings(TCDefaultValues.BiomeHeight.name(), this.DefaultBiomeSurface);
        this.BiomeVolatility = ReadModSettings(TCDefaultValues.BiomeVolatility.name(), this.DefaultBiomeVolatility);

        this.SurfaceBlock = ReadModSettings(TCDefaultValues.SurfaceBlock.name(), this.DefaultSurfaceBlock);
        this.GroundBlock = ReadModSettings(TCDefaultValues.GroundBlock.name(), this.DefaultGroundBlock);

        this.UseWorldWaterLevel = ReadSettings(TCDefaultValues.UseWorldWaterLevel);
        this.waterLevelMax = ReadSettings(TCDefaultValues.WaterLevelMax);
        this.waterLevelMin = ReadSettings(TCDefaultValues.WaterLevelMin);
        this.waterBlock = ReadSettings(TCDefaultValues.WaterBlock);
        this.iceBlock = ReadSettings(TCDefaultValues.IceBlock);

        this.SkyColor = ReadSettings(TCDefaultValues.SkyColor);
        this.WaterColor = ReadModSettingsColor(TCDefaultValues.WaterColor.name(), this.DefaultWaterColorMultiplier);
        this.GrassColor = ReadModSettingsColor(TCDefaultValues.GrassColor.name(), this.DefaultGrassColor);
        this.GrassColorIsMultiplier = ReadSettings(TCDefaultValues.GrassColorIsMultiplier);
        this.FoliageColor = ReadModSettingsColor(TCDefaultValues.FoliageColor.name(), this.DefaultFoliageColor);
        this.FoliageColorIsMultiplier = ReadSettings(TCDefaultValues.FoliageColorIsMultiplier);

        this.volatilityRaw1 = ReadSettings(TCDefaultValues.Volatility1);
        this.volatilityRaw2 = ReadSettings(TCDefaultValues.Volatility2);
        this.volatilityWeightRaw1 = ReadSettings(TCDefaultValues.VolatilityWeight1);
        this.volatilityWeightRaw2 = ReadSettings(TCDefaultValues.VolatilityWeight2);
        this.disableNotchHeightControl = ReadSettings(TCDefaultValues.DisableBiomeHeight);
        this.maxAverageHeight = ReadSettings(TCDefaultValues.MaxAverageHeight);
        this.maxAverageDepth = ReadSettings(TCDefaultValues.MaxAverageDepth);

        if (DefaultBiome.getBiome(this.Biome.getId()) == null)
        {
            // Only for custom biomes
            this.spawnMonstersAddDefaults = ReadModSettings("spawnMonstersAddDefaults", true);
            this.spawnMonsters = ReadModSettings("spawnMonsters", new ArrayList<WeightedMobSpawnGroup>());
            this.spawnCreaturesAddDefaults = ReadModSettings("spawnCreaturesAddDefaults", true);
            this.spawnCreatures = ReadModSettings("spawnCreatures", new ArrayList<WeightedMobSpawnGroup>());
            this.spawnWaterCreaturesAddDefaults = ReadModSettings("spawnWaterCreaturesAddDefaults", true);
            this.spawnWaterCreatures = ReadModSettings("spawnWaterCreatures", new ArrayList<WeightedMobSpawnGroup>());
        }

        this.ReadCustomObjectSettings();
        this.ReadReplaceSettings();
        this.ReadResourceSettings();
        this.ReadHeightSettings();
    }

    private void ReadHeightSettings()
    {
        this.heightMatrix = new double[this.worldConfig.WorldHeight / 8 + 1];

        ArrayList<String> keys = ReadSettings(TCDefaultValues.CustomHeightControl);
        try
        {
            if (keys.size() != (this.worldConfig.WorldHeight / 8 + 1))
                return;
            for (int i = 0; i < this.worldConfig.WorldHeight / 8 + 1; i++)
                this.heightMatrix[i] = Double.valueOf(keys.get(i));

        } catch (NumberFormatException e)
        {
            System.out.println("Wrong height settings: '" + this.SettingsCache.get(TCDefaultValues.CustomHeightControl.name().toLowerCase()) + "'");
        }
    }

    private void ReadReplaceSettings()
    {
        String settingValue = ReadComplexValue("ReplacedBlocks");

        if (settingValue.equals("") || settingValue.equals("None"))
            return;

        String[] keys = ReadComplexString(settingValue);
        try
        {
            for (String key : keys)
            {

                int start = key.indexOf("(");
                int end = key.lastIndexOf(")");
                if (start != -1 && end != -1)
                {
                    key = key.substring(start + 1, end);
                    String[] values = key.split(",");
                    if (values.length != 2 && values.length != 5)
                        continue;

                    short fromBlockId = Short.valueOf(values[0]);
                    short toBlockId = Short.valueOf(values[1]);

                    int min_y = 0;
                    int max_y = worldConfig.WorldHeight;
                    short blockData = 0;
                    if (values.length == 5)
                    {
                        blockData = Short.valueOf(values[2]);
                        min_y = Integer.valueOf(values[3]);
                        max_y = Integer.valueOf(values[4]);
                        min_y = applyBounds(min_y, 0, worldConfig.WorldHeight - 1);
                        max_y = applyBounds(max_y, 0, worldConfig.WorldHeight - 1, min_y);
                    }

                    if (this.ReplaceMatrixBlocks[fromBlockId] == null)
                    {
                        this.ReplaceMatrixBlocks[fromBlockId] = new short[worldConfig.WorldHeight];
                        for (int i = 0; i < worldConfig.WorldHeight; i++)
                            this.ReplaceMatrixBlocks[fromBlockId][i] = -1;
                    }
                    for (int y = min_y; y < max_y; y++)
                        this.ReplaceMatrixBlocks[fromBlockId][y] = (short) (toBlockId << 4 | blockData);
                    ReplaceCount++;

                }

            }

        } catch (NumberFormatException e)
        {
            System.out.println("Wrong replace settings: '" + this.SettingsCache.get(settingValue) + "'");
        }

    }

    private void ReadResourceSettings()
    {
        ArrayList<Integer> LineNumbers = new ArrayList<Integer>();

        for (Map.Entry<String, String> entry : this.SettingsCache.entrySet())
        {
            String key = entry.getKey();
            int start = key.indexOf("(");
            int end = key.lastIndexOf(")");
            if (start != -1 && end != -1)
            {
                String name = key.substring(0, start);
                String[] props = ReadComplexString(key.substring(start + 1, end));

                ConfigFunction<WorldConfig> res = TerrainControl.getConfigFunctionsManager().getConfigFunction(name, worldConfig, this.Name + " on line " + entry.getValue(), Arrays.asList(props));

                if (res != null)
                {

                    if (res instanceof SaplingGen)
                    {
                        SaplingGen sapling = (SaplingGen) res;
                        if (sapling.saplingType == -1)
                            this.SaplingResource = sapling;
                        else
                            this.SaplingTypes[sapling.saplingType] = sapling;

                    } else if (res instanceof Resource)
                    {
                        LineNumbers.add(Integer.valueOf(entry.getValue()));
                        this.ResourceSequence[this.ResourceCount++] = (Resource) res;
                    }
                }
            }
        }

        Resource buffer;
        for (int i = 0; i < this.ResourceCount; i++)
        {
            buffer = this.ResourceSequence[i];
            int intBuffer = LineNumbers.get(i);
            int minimal = i;
            for (int t = i; t < this.ResourceCount; t++)
            {
                if (LineNumbers.get(t) < intBuffer)
                {
                    intBuffer = LineNumbers.get(t);
                    minimal = t;
                }
            }
            this.ResourceSequence[i] = this.ResourceSequence[minimal];
            this.ResourceSequence[minimal] = buffer;
            LineNumbers.set(minimal, LineNumbers.get(i));
        }
    }

    private void ReadCustomObjectSettings()
    {
        biomeObjects = new ArrayList<CustomObject>();
        biomeObjectStrings = new ArrayList<String>();

        // Read from BiomeObjects setting
        String biomeObjectsValue = ReadModSettings("biomeobjects", "");
        if (biomeObjectsValue.length() > 0)
        {
            String[] customObjectStrings = biomeObjectsValue.split(",");
            for (String customObjectString : customObjectStrings)
            {
                CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString(customObjectString, worldConfig);
                if (object != null && !(object instanceof UseBiome))
                {
                    biomeObjects.add(object);
                    biomeObjectStrings.add(customObjectString);
                }
            }
        }
    }

    protected void WriteConfigSettings() throws IOException
    {
        WriteTitle(this.Name + " biome config");

        WriteComment("Biome size from 0 to GenerationDepth. Show in what zoom level biome will be generated (see GenerationDepth)");
        WriteComment("Higher numbers=Smaller% of world / Lower numbers=Bigger % of world");
        WriteComment("Don`t work on Ocean and River (frozen versions too) biomes until not added as normal biome.");
        WriteValue(TCDefaultValues.BiomeSize.name(), this.BiomeSize);
        this.WriteNewLine();

        WriteComment("Biome rarity from 100 to 1. If this is normal or ice biome - chance for spawn this biome then others.");
        WriteComment("Example for normal biome :");
        WriteComment("  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).");
        WriteComment("  50 rarity mean 1/11 chance than other");
        WriteComment("For isle biome this is chance to spawn isle in good place.");
        WriteComment("Don`t work on Ocean and River (frozen versions too) biomes until not added as normal biome.");
        WriteValue(TCDefaultValues.BiomeRarity.name(), this.BiomeRarity);
        this.WriteNewLine();

        WriteComment("Biome color in hex value. Biome color in /tc map command");
        WriteValue(TCDefaultValues.BiomeColor.name(), this.BiomeColor);
        this.WriteNewLine();

        WriteComment("True if biome can pass rivers.");
        WriteValue(TCDefaultValues.BiomeRivers.name(), this.BiomeRivers);
        this.WriteNewLine();

        WriteComment("Biome name list where this biome will be spawned as isle. Like Mushroom isle in Ocean.  This work only if this biome is in IsleBiomes in world config");
        WriteValue(TCDefaultValues.IsleInBiome.name(), this.IsleInBiome);
        this.WriteNewLine();

        WriteComment("Biome name list where this biome will be border.Like Mushroom isle shore. Use is compared as IsleInBiome");
        WriteValue(TCDefaultValues.BiomeIsBorder.name(), this.BiomeIsBorder);
        this.WriteNewLine();

        WriteComment("Biome name list near border is not applied. ");
        WriteValue(TCDefaultValues.NotBorderNear.name(), this.NotBorderNear);
        this.WriteNewLine();

        WriteComment("Biome temperature. Float value from 0.0 to 1.0");
        WriteValue(TCDefaultValues.BiomeTemperature.name(), this.BiomeTemperature);
        this.WriteNewLine();
        WriteComment("Biome wetness. Float value from 0.0 to 1.0");
        WriteValue(TCDefaultValues.BiomeWetness.name(), this.BiomeWetness);
        this.WriteNewLine();

        WriteComment("Replace this biome to specified after all generations. Warning this will cause saplings and mob spawning work as in specified biome");
        WriteValue(TCDefaultValues.ReplaceToBiomeName.name(), this.ReplaceBiomeName);
        this.WriteNewLine();

        WriteTitle("Terrain Generator Variables");
        WriteComment("BiomeHeight mean how much height will be added in terrain generation");
        WriteComment("It is double value from -10.0 to 10.0");
        WriteComment("Value 0.0 equivalent half of map height with all other default settings");
        WriteValue(TCDefaultValues.BiomeHeight.name(), this.BiomeHeight);

        this.WriteNewLine();
        WriteComment("Biome volatility.");
        WriteValue(TCDefaultValues.BiomeVolatility.name(), this.BiomeVolatility);

        WriteNewLine();
        WriteComment("If this value is greater than 0, then it will affect how much, on average, the terrain will rise before leveling off when it begins to increase in elevation.");
        WriteComment("If the value is less than 0, then it will cause the terrain to either increase to a lower height before leveling out or decrease in height if the value is a large enough negative.");
        WriteValue(TCDefaultValues.MaxAverageHeight.name(), this.maxAverageHeight);

        WriteNewLine();
        WriteComment("If this value is greater than 0, then it will affect how much, on average, the terrain (usually at the ottom of the ocean) will fall before leveling off when it begins to decrease in elevation. ");
        WriteComment("If the value is less than 0, then it will cause the terrain to either fall to a lesser depth before leveling out or increase in height if the value is a large enough negative.");
        WriteValue(TCDefaultValues.MaxAverageDepth.name(), this.maxAverageDepth);

        WriteNewLine();
        WriteComment("Another type of noise. This noise is independent from biomes. The larger the values the more chaotic/volatile landscape generation becomes.");
        WriteComment("Setting the values to negative will have the opposite effect and make landscape generation calmer/gentler.");
        WriteValue(TCDefaultValues.Volatility1.name(), this.volatilityRaw1);
        WriteValue(TCDefaultValues.Volatility2.name(), this.volatilityRaw2);

        WriteNewLine();
        WriteComment("Adjust the weight of the corresponding volatility settings. This allows you to change how prevalent you want either of the volatility settings to be in the terrain.");
        WriteValue(TCDefaultValues.VolatilityWeight1.name(), this.volatilityWeightRaw1);
        WriteValue(TCDefaultValues.VolatilityWeight2.name(), this.volatilityWeightRaw2);

        WriteNewLine();
        WriteComment("Disable all noises except Volatility1 and Volatility2. Also disable default block chance from height.");
        WriteValue(TCDefaultValues.DisableBiomeHeight.name(), this.disableNotchHeightControl);
        WriteNewLine();
        WriteComment("List of custom height factor, 17 double entries, each entire control of about 7 blocks height from down. Positive entry - better chance of spawn blocks, negative - smaller");
        WriteComment("Values which affect your configuration may be found only experimental. That may be very big, like ~3000.0 depends from height");
        WriteComment("Example:");
        WriteComment("  CustomHeightControl:0.0,-2500.0,0.0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0");
        WriteComment("Make empty layer above bedrock layer. ");
        WriteHeightSettings();

        this.WriteNewLine();
        WriteComment("Surface block id");
        WriteValue(TCDefaultValues.SurfaceBlock.name(), this.SurfaceBlock);

        this.WriteNewLine();
        WriteComment("Block id from stone to surface, like dirt in plain biome ");
        WriteValue(TCDefaultValues.GroundBlock.name(), this.GroundBlock);
        WriteNewLine();

        WriteComment("If disabled use water levels and blocks below for this biome");
        WriteValue(TCDefaultValues.UseWorldWaterLevel.name(), this.UseWorldWaterLevel);
        WriteNewLine();

        WriteComment("Set water level. Every empty block under this level will be fill water or another block from WaterBlock ");
        WriteValue(TCDefaultValues.WaterLevelMax.name(), this.waterLevelMax);
        WriteValue(TCDefaultValues.WaterLevelMin.name(), this.waterLevelMin);
        WriteNewLine();
        WriteComment("BlockId used as water in WaterLevel");
        WriteValue(TCDefaultValues.WaterBlock.name(), this.waterBlock);
        WriteNewLine();
        WriteComment("BlockId used as ice");
        WriteValue(TCDefaultValues.IceBlock.name(), this.iceBlock);

        this.WriteNewLine();
        WriteComment("Replace Variable: (BlockIdFrom,BlockIdTo[,BlockDataTo,minHeight,maxHeight])");
        WriteComment("Example :");
        WriteComment("  ReplacedBlocks:(2,3,0,100,127),(13,20)");
        WriteComment("Replace grass block to dirt from 100 to 127 height and replace gravel to glass on all height ");
        WriteModReplaceSettings();

        this.WriteTitle("Biome visual settings");
        this.WriteComment("Warning this section will work only for clients with single version of TerrainControl");

        this.WriteNewLine();
        this.WriteComment("Biome sky color");
        this.WriteColorValue(TCDefaultValues.SkyColor.name(), this.SkyColor);

        this.WriteNewLine();
        this.WriteComment("Biome water color multiplier ");
        this.WriteColorValue(TCDefaultValues.WaterColor.name(), this.WaterColor);

        this.WriteNewLine();
        this.WriteComment("Biome grass color");
        this.WriteColorValue(TCDefaultValues.GrassColor.name(), this.GrassColor);

        this.WriteNewLine();
        this.WriteComment("Whether the grass color is a multiplier");
        this.WriteComment("If you set it to true, the color will be based on this value, the BiomeTemperature and the BiomeWetness");
        this.WriteComment("If you set it to false, the grass color will be just this color");
        this.WriteValue(TCDefaultValues.GrassColorIsMultiplier.name(), this.GrassColorIsMultiplier);

        this.WriteNewLine();
        this.WriteComment("Biome foliage color");
        this.WriteColorValue(TCDefaultValues.FoliageColor.name(), this.FoliageColor);

        this.WriteNewLine();
        this.WriteComment("Whether the foliage color is a multiplier. See GrassColorIsMultiplier for details");
        this.WriteValue(TCDefaultValues.FoliageColorIsMultiplier.name(), this.FoliageColorIsMultiplier);

        this.WriteNewLine();
        this.WriteTitle("Sapling resource");
        this.WriteComment("Work like Tree resource instead first parameter");
        this.WriteSaplingSettings();

        this.WriteNewLine();
        this.WriteTitle("Resource queue");
        this.WriteComment("This section control all resources spawning after terrain generation");
        this.WriteComment("So first line is first resource which will be placed. Second line - second resource.");
        this.WriteComment("By default this set to be near vanilla settings.");
        this.WriteComment("");
        this.WriteComment("Keep in mind that a high size, frequency or rarity might slow down terrain generation.");
        this.WriteComment("");
        this.WriteComment("Possible resources:");
        this.WriteComment("SmallLake(Block[.BlockData],Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.WriteComment("Dungeon(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.WriteComment("UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.WriteComment("Ore(Block[.BlockData],Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("UnderWaterOre(Block,Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("CustomObject()");
        this.WriteComment("Tree(Frequency,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])");
        this.WriteComment("Plant(Block[.BlockData],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("Grass(Block,BlockData,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("Reed(Block,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("Cactus(Block,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("Liquid(Block,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.WriteComment("AboveWaterRes(Block,Frequency,Rarity)");
        this.WriteComment("Vines(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.WriteComment("");
        this.WriteComment("Some comments:  ");
        this.WriteComment("Block and BlockSource - can be id or name, Frequency - is count of attempts for place resource");
        this.WriteComment("Rarity - chance for each attempt, Rarity:100 - mean 100% to pass, Rarity:1 - mean 1% to pass");
        this.WriteComment("MinAltitude and MaxAltitude - height limits");
        this.WriteComment("BlockSource - mean where or whereupon resource will be placed ");
        this.WriteComment("CustomObject resource - mean where in queue custom object will be trying to place");
        this.WriteComment("Tree types: ");
        this.WriteComment("     Tree");
        this.WriteComment("     BigTree");
        this.WriteComment("     Forest");
        this.WriteComment("     HugeMushroom");
        this.WriteComment("     SwampTree");
        this.WriteComment("     Taiga1");
        this.WriteComment("     Taiga2");
        this.WriteComment("     JungleTree");
        this.WriteComment("     GroundBush");
        this.WriteComment("     CocoaTree");
        this.WriteComment("TreeType_Chance - similar Rarity. Example:");
        this.WriteComment("  Tree(10,Taiga1,35,Taiga2,100) - plugin trying to 10 attempts, in each attempt he try place Taiga1 ( 35% chance ) if not he place Taiga2 (100% chance)");
        this.WriteComment("Plant resource used for place something like flower, small mushrooms, pumpkins");
        this.WriteComment("Liquid resource is one block water or lava source");
        this.WriteComment("");

        this.WriteResources();

        this.WriteTitle("Custom objects");
        this.WriteComment("These objects will spawn when using the UseBiome keyword.");
        this.WriteCustomObjects();

        if (DefaultBiome.getBiome(this.Biome.getId()) != null)
        {
            this.WriteTitle("MOB SPAWNING");
            this.WriteComment("Mob spawning control did not work with default biomes.");
            return;
        }

        this.WriteNewLine();
        this.WriteTitle("MOB SPAWNING");
        this.WriteComment("========<TUTORIAL>========");
        this.WriteComment("This is where you configure mob spawning. Changing this section is optional.");
        this.WriteComment("");
        this.WriteComment("#STEP1: Understanding what a mobgroup is.");
        this.WriteComment("A mobgroups is made of four parts. They are mob, weight, min and max.");
        this.WriteComment("The mob is one of the avaliable mobnames: " + Txt.implodeCommaAnd(DefaultMobType.getPreferedNames()));
        this.WriteComment("The weight is used for a random selection. This is a positive integer.");
        this.WriteComment("The min is the minimum amount of mobs spawning as a group. This is a positive integer.");
        this.WriteComment("The max is the maximum amount of mobs spawning as a group. This is a positive integer.");
        this.WriteComment("");
        this.WriteComment("#STEP2: Understanding how write a mobgroup as JSON as well as lists of them.");
        this.WriteComment("Json is a tree document format: http://en.wikipedia.org/wiki/JSON");
        this.WriteComment("Write a mobgroup like this: {\"mob\": \"mobname\", \"weight\": integer, \"min\": integer, \"max\": integer}");
        this.WriteComment("For example: {\"mob\": \"Ocelot\", \"weight\": 10, \"min\": 2, \"max\": 6}");
        this.WriteComment("For example: {\"mob\": \"MushroomCow\", \"weight\": 5, \"min\": 2, \"max\": 2}");
        this.WriteComment("A json list of mobgroups looks like this: [mobgroup, mobgroup, mobgroup...]");
        this.WriteComment("This would be an ampty list: []");
        this.WriteComment("You can validate your json here: http://jsonlint.com/");
        this.WriteComment("");
        this.WriteComment("#STEP3: Understanding what to do with all this info");
        this.WriteComment("There are three categories of mobs: monsters, creatures and watercreatures.");
        this.WriteComment("These list may be populated with default values if thee booleans bellow is set to true");
        this.WriteComment("You may also add your own mobgroups in the lists below");
        this.WriteComment("");
        this.WriteComment("#STEP4: What is in the default mob groups?");
        this.WriteComment("The default mob groups are controlled by vanilla minecraft.");
        this.WriteComment("At 2012-03-24 you could find them here: https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/BiomeBase.java#L75");
        this.WriteComment("In simple terms:");
        this.WriteComment("Default creatures: [{\"mob\": \"Sheep\", \"weight\": 12, \"min\": 4, \"max\": 4}, {\"mob\": \"Pig\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Chicken\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Cow\", \"weight\": 8, \"min\": 4, \"max\": 4}]");
        this.WriteComment("Default monsters: [{\"mob\": \"Spider\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Zombie\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Skeleton\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Creeper\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Slime\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Enderman\", \"weight\": 1, \"min\": 1, \"max\": 4}]");
        this.WriteComment("Default watercreatures: [{\"mob\": \"Squid\", \"weight\": 10, \"min\": 4, \"max\": 4}]");
        this.WriteComment("");
        this.WriteComment("So for example ocelots wont spawn unless you add them below.");

        this.WriteNewLine();
        this.WriteComment("========<CONFIGURATION>========");

        this.WriteComment("Should we add the default monster spawn groups?");
        WriteValue("spawnMonstersAddDefaults", this.spawnMonstersAddDefaults);
        this.WriteComment("Add extra monster spawn groups here");
        WriteValue("spawnMonsters", this.spawnMonsters);
        this.WriteNewLine();

        this.WriteComment("Should we add the default creature spawn groups?");
        WriteValue("spawnCreaturesAddDefaults", this.spawnCreaturesAddDefaults);
        this.WriteComment("Add extra creature spawn groups here");
        WriteValue("spawnCreatures", this.spawnCreatures);
        this.WriteNewLine();

        this.WriteComment("Should we add the default watercreature spawn groups?");
        WriteValue("spawnWaterCreaturesAddDefaults", this.spawnWaterCreaturesAddDefaults);
        this.WriteComment("Add extra watercreature spawn groups here");
        WriteValue("spawnWaterCreatures", this.spawnWaterCreatures);
        this.WriteNewLine();

    }

    private void WriteHeightSettings() throws IOException
    {
        String output = Double.toString(this.heightMatrix[0]);
        for (int i = 1; i < this.heightMatrix.length; i++)
            output = output + "," + Double.toString(this.heightMatrix[i]);

        this.WriteValue(TCDefaultValues.CustomHeightControl.name(), output);
    }

    private void WriteModReplaceSettings() throws IOException
    {
        if (this.ReplaceCount == 0)
        {
            this.WriteValue("ReplacedBlocks", "None");
            return;
        }
        String output = "";

        for (int id = 0; id < ReplaceMatrixBlocks.length; id++)
        {
            if (ReplaceMatrixBlocks[id] == null)
                continue;

            int replaceTo = -1;
            int y_start = 0;

            for (int y = 0; y < ReplaceMatrixBlocks[id].length; y++)
            {
                if (ReplaceMatrixBlocks[id][y] == replaceTo)
                    continue;

                if (replaceTo == -1)
                {
                    y_start = y;
                    replaceTo = ReplaceMatrixBlocks[id][y];
                    continue;
                }
                output += "(" + id + "," + (replaceTo >> 4);
                if ((replaceTo & 0xF) > 0 || y_start != 0 || y != (ReplaceMatrixBlocks[id].length - 1))
                    output += "," + (replaceTo & 0xF) + "," + y_start + "," + y;
                output += "),";

                replaceTo = -1;
            }
            if (replaceTo != -1)
            {

                output += "(" + id + "," + (replaceTo >> 4);
                if ((replaceTo & 0xF) > 0 || y_start != 0)
                    output += "," + (replaceTo & 0xF) + "," + y_start + "," + (ReplaceMatrixBlocks[id].length - 1);
                output += "),";

            }
        }
        this.WriteValue("ReplacedBlocks", output.substring(0, output.length() - 1));
    }

    private void WriteResources() throws IOException
    {
        for (int i = 0; i < this.ResourceCount; i++)
        {
            this.WriteValue(this.ResourceSequence[i].makeString());
        }
    }

    private void WriteCustomObjects() throws IOException
    {
        StringBuilder builder = new StringBuilder();
        for (String objectString : biomeObjectStrings)
        {
            builder.append(objectString);
            builder.append(',');
        }
        if (builder.length() > 0)
        {
            // Delete last char
            builder.deleteCharAt(builder.length() - 1);
        }
        this.WriteValue("BiomeObjects", builder.toString());
    }

    private void WriteSaplingSettings() throws IOException
    {
        if (this.SaplingResource != null)
            this.WriteValue(SaplingResource.makeString());

        for (SaplingGen res : this.SaplingTypes)
            if (res != null)
                this.WriteValue(res.makeString());

    }

    protected void CorrectSettings()
    {
        this.BiomeSize = applyBounds(this.BiomeSize, 0, this.worldConfig.GenerationDepth);
        this.BiomeHeight = (float) applyBounds(this.BiomeHeight, -10.0, 10.0);
        this.BiomeRarity = applyBounds(this.BiomeRarity, 1, this.worldConfig.BiomeRarityScale);

        this.BiomeTemperature = applyBounds(this.BiomeTemperature, 0.0F, 1.0F);
        this.BiomeWetness = applyBounds(this.BiomeWetness, 0.0F, 1.0F);

        this.IsleInBiome = filterBiomes(this.IsleInBiome, this.worldConfig.CustomBiomes);
        this.BiomeIsBorder = filterBiomes(this.BiomeIsBorder, this.worldConfig.CustomBiomes);
        this.NotBorderNear = filterBiomes(this.NotBorderNear, this.worldConfig.CustomBiomes);

        this.volatility1 = this.volatilityRaw1 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw1) + 1.0D) : this.volatilityRaw1 + 1.0D;
        this.volatility2 = this.volatilityRaw2 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw2) + 1.0D) : this.volatilityRaw2 + 1.0D;

        this.volatilityWeight1 = (this.volatilityWeightRaw1 - 0.5D) * 24.0D;
        this.volatilityWeight2 = (0.5D - this.volatilityWeightRaw2) * 24.0D;

        this.waterLevelMin = applyBounds(this.waterLevelMin, 0, this.worldConfig.WorldHeight - 1);
        this.waterLevelMax = applyBounds(this.waterLevelMax, 0, this.worldConfig.WorldHeight - 1, this.waterLevelMin);

        this.ReplaceBiomeName = (DefaultBiome.Contain(this.ReplaceBiomeName) || this.worldConfig.CustomBiomes.contains(this.ReplaceBiomeName)) ? this.ReplaceBiomeName : "";
    }

    protected void RenameOldSettings()
    {
        // Old values from WorldConfig
        TCDefaultValues[] copyFromWorld = { TCDefaultValues.MaxAverageHeight, TCDefaultValues.MaxAverageDepth, TCDefaultValues.Volatility1, TCDefaultValues.Volatility2, TCDefaultValues.VolatilityWeight1, TCDefaultValues.VolatilityWeight2, TCDefaultValues.DisableBiomeHeight, TCDefaultValues.CustomHeightControl };
        for (TCDefaultValues value : copyFromWorld)
            if (this.worldConfig.SettingsCache.containsKey(value.name().toLowerCase()))
            {
                // this.SettingsCache.put(value.name(),
                // this.worldConfig.SettingsCache.get(value.name().toLowerCase()));
                this.SettingsCache.put(value.name().toLowerCase(), this.worldConfig.SettingsCache.get(value.name().toLowerCase()));
            }

        // disableNotchPonds
        if (this.SettingsCache.containsKey("disableNotchPonds".toLowerCase()))
        {
            if (!ReadModSettings("disableNotchPonds".toLowerCase(), false))
            {
                this.SettingsCache.put("SmallLake(WATER,4,7,8," + this.worldConfig.WorldHeight + ")", "0");
                this.SettingsCache.put("SmallLake(LAVA,2,3,8," + (this.worldConfig.WorldHeight - 8) + ")", "1");
            }

        }

        // CustomTreeChance
        int customTreeChance = 0; // Default value
        if (worldConfig.SettingsCache.containsKey("customtreechance"))
        {
            try
            {
                customTreeChance = Integer.parseInt(worldConfig.SettingsCache.get("customtreechance"));
            } catch (NumberFormatException e)
            {
                // Ignore, so leave customTreeChance at 0
            }
        }
        if (customTreeChance == 100)
        {
            this.SettingsCache.put("Sapling(All,UseWorld,100)", "-");
        }
        if (customTreeChance > 0 && customTreeChance < 100)
        {
            this.SettingsCache.put("Sapling(0,UseWorld," + customTreeChance + ",BigTree,10,Tree,100)", "-"); // Oak
            this.SettingsCache.put("Sapling(1,UseWorld," + customTreeChance + ",Taiga2,100)", "-"); // Redwood
            this.SettingsCache.put("Sapling(2,UseWorld," + customTreeChance + ",Forest,100)", "-"); // Birch
            this.SettingsCache.put("Sapling(3,UseWorld," + customTreeChance + ",CocoaTree,100)", "-"); // Jungle
        }

        // ReplacedBlocks
        String replaceBlocksValue;
        boolean oldReplaceFound = false;
        if (this.SettingsCache.containsKey("replacedblocks"))
        {
            replaceBlocksValue = this.SettingsCache.get("replacedblocks");
            if (replaceBlocksValue.contains("="))
            {
                oldReplaceFound = true;
                this.SettingsCache.remove("replacedblocks");
            }
        } else
        {
            replaceBlocksValue = ReadComplexValue("ReplacedBlocks");
            if (replaceBlocksValue.contains("="))
            {
                oldReplaceFound = true;
                this.SettingsCache.remove("ReplacedBlocks" + ":" + replaceBlocksValue);
            }
        }

        if (oldReplaceFound)
        {
            String[] values = replaceBlocksValue.split(",");
            String output = "";

            for (String block : values)
            {
                try
                {

                    String fromId = block.split("=")[0];
                    String toId = block.split("=")[1];

                    String toData = "0";
                    String minHeight = "0";
                    String maxHeight = "" + worldConfig.WorldHeight;

                    Boolean longForm = false;

                    int start = toId.indexOf("(");
                    int end = toId.indexOf(")");
                    if (start != -1 && end != -1)
                    {
                        String[] ranges = toId.substring(start + 1, end).split("-");
                        toId = toId.substring(0, start);
                        minHeight = ranges[0];
                        maxHeight = ranges[1];
                        longForm = true;
                    }
                    if (toData.contains("\\."))
                    {
                        String[] temp = toId.split("\\.");
                        toId = temp[0];
                        toData = temp[1];
                        longForm = true;
                    }

                    if (longForm)
                        output = output + "(" + fromId + "," + toId + "," + toData + "," + minHeight + "," + maxHeight + "),";
                    else
                        output = output + "(" + fromId + "," + toId + "),";
                } catch (Exception ignored)
                {

                }

            }

            this.SettingsCache.put("ReplacedBlocks" + ":" + output.substring(0, output.length() - 1), "");

        }
    }

    private int DefaultTrees = 1;
    private int DefaultFlowers = 2;
    private int DefaultGrass = 10;
    private int DefaultDeadBrush = 0;
    private int DefaultMushroom = 0;
    private int DefaultReed = 0;
    private int DefaultCactus = 0;
    private int DefaultClay = 1;
    private float DefaultBiomeSurface = 0.1F;
    private float DefaultBiomeVolatility = 0.3F;
    private byte DefaultSurfaceBlock = (byte) DefaultMaterial.GRASS.id;
    private byte DefaultGroundBlock = (byte) DefaultMaterial.DIRT.id;
    private float DefaultBiomeTemperature = 0.5F;
    private float DefaultBiomeWetness = 0.5F;
    private ArrayList<String> DefaultIsle = new ArrayList<String>();
    private ArrayList<String> DefaultBorder = new ArrayList<String>();
    private ArrayList<String> DefaultNotBorderNear = new ArrayList<String>();
    private boolean DefaultRiver = true;
    private int DefaultSize = 4;
    private int DefaultRarity = 100;
    private String DefaultColor = "0x000000";
    private int DefaultWaterLily = 0;
    private String DefaultWaterColorMultiplier = "0xFFFFFF";
    private String DefaultGrassColor = "0xFFFFFF";
    private String DefaultFoliageColor = "0xFFFFFF";

    private void InitDefaults()
    {
        this.DefaultBiomeSurface = this.Biome.getSurfaceHeight();
        this.DefaultBiomeVolatility = this.Biome.getSurfaceVolatility();
        this.DefaultSurfaceBlock = this.Biome.getSurfaceBlock();
        this.DefaultGroundBlock = this.Biome.getGroundBlock();
        this.DefaultBiomeTemperature = this.Biome.getTemperature();
        this.DefaultBiomeWetness = this.Biome.getWetness();

        switch (this.Biome.getId())
        {
        case 0:
            this.DefaultColor = "0x3333FF";
            break;
        case 1:
        {
            this.DefaultTrees = 0;
            this.DefaultFlowers = 4;
            this.DefaultGrass = 20;
            this.DefaultColor = "0x999900";
            break;
        }
        case 2:
        {
            this.DefaultTrees = 0;
            this.DefaultDeadBrush = 4;
            this.DefaultGrass = 0;
            this.DefaultReed = 10;
            this.DefaultCactus = 10;
            this.DefaultColor = "0xFFCC33";
            break;
        }
        case 3:
            this.DefaultColor = "0x333300";
            break;
        case 4:
        {
            this.DefaultTrees = 10;
            this.DefaultGrass = 15;
            this.DefaultColor = "0x00FF00";
            break;
        }
        case 5:
        {
            this.DefaultTrees = 10;
            this.DefaultGrass = 10;
            this.DefaultColor = "0x007700";
            break;
        }
        case 6:
        {
            this.DefaultTrees = 2;
            this.DefaultFlowers = -999;
            this.DefaultDeadBrush = 1;
            this.DefaultMushroom = 8;
            this.DefaultReed = 10;
            this.DefaultClay = 1;
            this.DefaultWaterLily = 1;
            this.DefaultColor = "0x99CC66";
            this.DefaultWaterColorMultiplier = "0xe0ffae";
            this.DefaultGrassColor = "0x7E6E7E";
            this.DefaultFoliageColor = "0x7E6E7E";
            break;
        }
        case 7:
            this.DefaultSize = 8;
            this.DefaultRarity = 95;
            this.DefaultIsle.add(DefaultBiome.SWAMPLAND.Name);
            this.DefaultColor = "0x00CCCC";
        case 8:
        case 9:

            break;
        case 10:
            this.DefaultColor = "0xFFFFFF";
            break;
        case 11:
            this.DefaultColor = "0x66FFFF";
            break;
        case 12:
            this.DefaultColor = "0xCCCCCC";
            break;
        case 13:
            this.DefaultColor = "0xCC9966";
            break;
        case 14:
        {
            this.DefaultSurfaceBlock = (byte) DefaultMaterial.MYCEL.id;
            this.DefaultMushroom = 1;
            this.DefaultGrass = 0;
            this.DefaultFlowers = 0;
            this.DefaultTrees = 0;
            this.DefaultRarity = 1;
            this.DefaultRiver = false;
            this.DefaultSize = 6;
            this.DefaultIsle.add(DefaultBiome.OCEAN.Name);
            this.DefaultColor = "0xFF33CC";
            this.DefaultWaterLily = 1;
            break;
        }
        case 15:
        {
            this.DefaultRiver = false;
            this.DefaultSize = 9;
            this.DefaultBorder.add(DefaultBiome.MUSHROOM_ISLAND.Name);
            this.DefaultColor = "0xFF9999";
            break;
        }
        case 16:
            this.DefaultTrees = 0;
            this.DefaultSize = 8;
            this.DefaultBorder.add(DefaultBiome.OCEAN.Name);
            this.DefaultNotBorderNear.add(DefaultBiome.RIVER.Name);
            this.DefaultNotBorderNear.add(DefaultBiome.SWAMPLAND.Name);
            this.DefaultNotBorderNear.add(DefaultBiome.EXTREME_HILLS.Name);
            this.DefaultNotBorderNear.add(DefaultBiome.MUSHROOM_ISLAND.Name);
            this.DefaultColor = "0xFFFF00";
            break;
        case 17:
            this.DefaultSize = 6;
            this.DefaultRarity = 97;
            this.DefaultIsle.add(DefaultBiome.DESERT.Name);
            this.DefaultTrees = 0;
            this.DefaultDeadBrush = 4;
            this.DefaultGrass = 0;
            this.DefaultReed = 50;
            this.DefaultCactus = 10;
            this.DefaultColor = "0x996600";
            break;
        case 18:
            this.DefaultSize = 6;
            this.DefaultRarity = 97;
            this.DefaultIsle.add(DefaultBiome.FOREST.Name);
            this.DefaultTrees = 10;
            this.DefaultGrass = 15;
            this.DefaultColor = "0x009900";
            break;
        case 19:
            this.DefaultSize = 6;
            this.DefaultRarity = 97;
            this.DefaultIsle.add(DefaultBiome.TAIGA.Name);
            this.DefaultTrees = 10;
            this.DefaultGrass = 10;
            this.DefaultColor = "0x003300";
            break;
        case 20:
            this.DefaultSize = 8;
            this.DefaultBorder.add(DefaultBiome.EXTREME_HILLS.Name);
            this.DefaultColor = "0x666600";
            break;
        case 21:
            this.DefaultTrees = 50;
            this.DefaultGrass = 25;
            this.DefaultFlowers = 4;
            this.DefaultColor = "0xCC6600";
            break;
        case 22:
            this.DefaultTrees = 50;
            this.DefaultGrass = 25;
            this.DefaultFlowers = 4;
            this.DefaultColor = "0x663300";
            this.DefaultIsle.add(DefaultBiome.JUNGLE.Name);
            break;
        }
    }

    public void Serialize(DataOutputStream stream) throws IOException
    {
        WriteStringToStream(stream, this.Name);

        stream.writeFloat(this.BiomeTemperature);
        stream.writeFloat(this.BiomeWetness);
        stream.writeInt(this.SkyColor);
        stream.writeInt(this.WaterColor);
        stream.writeInt(this.GrassColor);
        stream.writeBoolean(this.GrassColorIsMultiplier);
        stream.writeInt(this.FoliageColor);
        stream.writeBoolean(this.FoliageColorIsMultiplier);
    }

    public BiomeConfig(DataInputStream stream, WorldConfig config, LocalBiome biome) throws IOException
    {
        this.Name = ReadStringFromStream(stream);
        this.Biome = biome;
        this.worldConfig = config;

        this.BiomeTemperature = stream.readFloat();
        this.BiomeWetness = stream.readFloat();
        this.SkyColor = stream.readInt();
        this.WaterColor = stream.readInt();
        this.GrassColor = stream.readInt();
        this.GrassColorIsMultiplier = stream.readBoolean();
        this.FoliageColor = stream.readInt();
        this.FoliageColorIsMultiplier = stream.readBoolean();
    }
}