package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.UseBiome;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.resourcegens.*;
import com.khorn.terraincontrol.util.StringHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BiomeConfig extends ConfigFile
{

    public short[][] replaceMatrixBlocks = new short[TerrainControl.supportedBlockIds][];
    public int ReplaceCount = 0;

    public String riverBiome;
    public float riverHeight;
    public float riverVolatility;
    public int riverWaterLevel;
    public double[] riverHeightMatrix;
    
    public int BiomeSize;
    public int BiomeRarity;
    
    public String BiomeColor;
    
    public ArrayList<String> BiomeIsBorder;
    public ArrayList<String> IsleInBiome;
    public ArrayList<String> NotBorderNear;
    
    // Surface config
    public float BiomeHeight;
    public float BiomeVolatility;
    public int SmoothRadius;
    
    public float BiomeTemperature;
    public float BiomeWetness;

    public int StoneBlock;
    public int SurfaceBlock;
    public int GroundBlock;

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
    private SaplingGen[] saplingTypes = new SaplingGen[20];
    private SaplingGen saplingResource = null;

    public ArrayList<CustomObject> biomeObjects;
    public CustomStructureGen structureGen;
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

    // Structures
    public boolean strongholdsEnabled;
    public boolean netherFortressesEnabled;

    public enum VillageType
    {
        disabled,
        wood,
        sandstone
    }

    public VillageType villageType;
    public double mineshaftsRarity;

    public enum RareBuildingType
    {
        disabled,
        desertPyramid,
        jungleTemple,
        swampHut
    }

    public RareBuildingType rareBuildingType;

    public int ResourceCount = 0;

    public LocalBiome Biome;

    public WorldConfig worldConfig;

    // Spawn Config
    public boolean spawnMonstersAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnMonsters = new ArrayList<WeightedMobSpawnGroup>();
    public boolean spawnCreaturesAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnCreatures = new ArrayList<WeightedMobSpawnGroup>();
    public boolean spawnWaterCreaturesAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnWaterCreatures = new ArrayList<WeightedMobSpawnGroup>();
    public boolean spawnAmbientCreaturesAddDefaults = true;
    public List<WeightedMobSpawnGroup> spawnAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();

    public BiomeConfig(File settingsDir, LocalBiome biome, WorldConfig config)
    {
        super(biome.getName(), new File(settingsDir, biome.getName() + TCDefaultValues.WorldBiomeConfigName.stringValue()));
        this.Biome = biome;
        worldConfig = config;
        initDefaults();

        this.readSettingsFile();
        this.renameOldSettings();
        this.readConfigSettings();

        this.correctSettings();
        if (!file.exists())
            this.createDefaultResources();
        if (config.SettingsMode != WorldConfig.ConfigMode.WriteDisable)
            this.writeSettingsFile(config.SettingsMode == WorldConfig.ConfigMode.WriteAll);

        if (this.UseWorldWaterLevel)
        {
            this.waterLevelMax = worldConfig.waterLevelMax;
            this.waterLevelMin = worldConfig.waterLevelMin;
            this.waterBlock = worldConfig.waterBlock;
            this.iceBlock = worldConfig.iceBlock;
        }

        if (biome.isCustom())
            biome.setEffects(this);
    }

    public int getTemperature()
    {
        return (int) (this.BiomeTemperature * 65536.0F);
    }

    public int getWetness()
    {
        return (int) (this.BiomeWetness * 65536.0F);
    }

    public SaplingGen getSaplingGen(SaplingType type)
    {
        SaplingGen gen = this.saplingTypes[type.getSaplingId()];
        if (gen == null && type.growsTree())
        {
            gen = this.saplingResource;
        }
        return gen;
    }

    private void createDefaultResources()
    {
        Resource resource;

        // Small water lakes
        if (this.defaultWaterLakes)
        {
            resource = Resource.createResource(this, SmallLakeGen.class, DefaultMaterial.WATER.id, TCDefaultValues.SmallLakeWaterFrequency.intValue(), TCDefaultValues.SmallLakeWaterRarity.intValue(), TCDefaultValues.SmallLakeMinAltitude.intValue(), TCDefaultValues.SmallLakeMaxAltitude.intValue());
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        // Small lava lakes
        resource = Resource.createResource(this, SmallLakeGen.class, DefaultMaterial.LAVA.id, TCDefaultValues.SmallLakeLavaFrequency.intValue(), TCDefaultValues.SmallLakeLavaRarity.intValue(), TCDefaultValues.SmallLakeMinAltitude.intValue(), TCDefaultValues.SmallLakeMaxAltitude.intValue());
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Underground lakes
        resource = Resource.createResource(this, UndergroundLakeGen.class, TCDefaultValues.undergroundLakeMinSize.intValue(), TCDefaultValues.undergroundLakeMaxSize.intValue(), TCDefaultValues.undergroundLakeFrequency.intValue(), TCDefaultValues.undergroundLakeRarity.intValue(), TCDefaultValues.undergroundLakeMinAltitude.intValue(), TCDefaultValues.undergroundLakeMaxAltitude.intValue());
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Dungeon
        resource = Resource.createResource(this, DungeonGen.class, TCDefaultValues.dungeonFrequency.intValue(), TCDefaultValues.dungeonRarity.intValue(), TCDefaultValues.dungeonMinAltitude.intValue(), this.worldConfig.WorldHeight);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Dirt
        resource = Resource.createResource(this, OreGen.class, DefaultMaterial.DIRT.id, TCDefaultValues.dirtDepositSize.intValue(), TCDefaultValues.dirtDepositFrequency.intValue(), TCDefaultValues.dirtDepositRarity.intValue(), TCDefaultValues.dirtDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Gravel
        resource = Resource.createResource(this, OreGen.class, DefaultMaterial.GRAVEL.id, TCDefaultValues.gravelDepositSize.intValue(), TCDefaultValues.gravelDepositFrequency.intValue(), TCDefaultValues.gravelDepositRarity.intValue(), TCDefaultValues.gravelDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Coal
        resource = Resource.createResource(this, OreGen.class, DefaultMaterial.COAL_ORE.id, TCDefaultValues.coalDepositSize.intValue(), TCDefaultValues.coalDepositFrequency.intValue(), TCDefaultValues.coalDepositRarity.intValue(), TCDefaultValues.coalDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Iron
        resource = Resource.createResource(this, OreGen.class, DefaultMaterial.IRON_ORE.id, TCDefaultValues.ironDepositSize.intValue(), TCDefaultValues.ironDepositFrequency.intValue(), TCDefaultValues.ironDepositRarity.intValue(), TCDefaultValues.ironDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 2, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Gold
        resource = Resource.createResource(this, OreGen.class, DefaultMaterial.GOLD_ORE.id, TCDefaultValues.goldDepositSize.intValue(), TCDefaultValues.goldDepositFrequency.intValue(), TCDefaultValues.goldDepositRarity.intValue(), TCDefaultValues.goldDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 4, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Redstone
        resource = Resource.createResource(this, OreGen.class, DefaultMaterial.REDSTONE_ORE.id, TCDefaultValues.redstoneDepositSize.intValue(), TCDefaultValues.redstoneDepositFrequency.intValue(), TCDefaultValues.redstoneDepositRarity.intValue(), TCDefaultValues.redstoneDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Diamond
        resource = Resource.createResource(this, OreGen.class, DefaultMaterial.DIAMOND_ORE.id, TCDefaultValues.diamondDepositSize.intValue(), TCDefaultValues.diamondDepositFrequency.intValue(), TCDefaultValues.diamondDepositRarity.intValue(), TCDefaultValues.diamondDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Lapislazuli
        resource = Resource.createResource(this, OreGen.class, DefaultMaterial.LAPIS_ORE.id, TCDefaultValues.lapislazuliDepositSize.intValue(), TCDefaultValues.lapislazuliDepositFrequency.intValue(), TCDefaultValues.lapislazuliDepositRarity.intValue(), TCDefaultValues.lapislazuliDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 8, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        DefaultBiome biome = DefaultBiome.getBiome(this.Biome.getId());

        if (biome != null && (biome == DefaultBiome.EXTREME_HILLS || biome == DefaultBiome.SMALL_MOUNTAINS))
        {
            resource = Resource.createResource(this, OreGen.class, DefaultMaterial.EMERALD_ORE.id, TCDefaultValues.emeraldDepositSize.intValue(), TCDefaultValues.emeraldDepositFrequency.intValue(), TCDefaultValues.emeraldDepositRarity.intValue(), TCDefaultValues.emeraldDepositMinAltitude.intValue(), this.worldConfig.WorldHeight / 4, DefaultMaterial.STONE.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        // Under water sand
        resource = Resource.createResource(this, UnderWaterOreGen.class, DefaultMaterial.SAND.id, TCDefaultValues.waterSandDepositSize.intValue(), TCDefaultValues.waterSandDepositFrequency.intValue(), TCDefaultValues.waterSandDepositRarity.intValue(), DefaultMaterial.DIRT.id, DefaultMaterial.GRASS.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Under water clay
        if (this.defaultClay > 0)
        {
            resource = Resource.createResource(this, UnderWaterOreGen.class, DefaultMaterial.CLAY.id, TCDefaultValues.waterClayDepositSize.intValue(), this.defaultClay, TCDefaultValues.waterClayDepositRarity.intValue(), DefaultMaterial.DIRT.id, DefaultMaterial.CLAY.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }
        // Custom objects
        resource = Resource.createResource(this, CustomObjectGen.class, "UseWorld");
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Trees
        if (biome != null)
            switch (biome)
            {
                case OCEAN: // Ocean - default
                case EXTREME_HILLS: // BigHills - default
                case RIVER: // River - default
                case SMALL_MOUNTAINS: // SmallHills
                    resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.BigTree, 1, TreeType.Tree, 9);
                    this.ResourceSequence[this.ResourceCount++] = resource;
                    break;
                case PLAINS: // Plains - no tree
                case DESERT: // Desert - no tree
                case DESERT_HILLS: // HillsDesert
                    break;
                case FOREST_HILLS: // HillsForest
                case FOREST: // Forest - forest
                    resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.Forest, 20, TreeType.BigTree, 10, TreeType.Tree, 100);
                    this.ResourceSequence[this.ResourceCount++] = resource;
                    break;
                case TAIGA_HILLS: // HillsTaiga
                case TAIGA: // Taiga - taiga
                    resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.Taiga1, 35, TreeType.Taiga2, 100);
                    this.ResourceSequence[this.ResourceCount++] = resource;
                    break;
                case SWAMPLAND: // Swamp - swamp
                    resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.SwampTree, 100);
                    this.ResourceSequence[this.ResourceCount++] = resource;
                    break;
                case MUSHROOM_ISLAND: // Mushroom island
                    resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.HugeMushroom, 100);
                    this.ResourceSequence[this.ResourceCount++] = resource;
                    break;
                case JUNGLE:// Jungle
                case JUNGLE_HILLS:
                    resource = Resource.createResource(this, TreeGen.class, this.defaultTrees, TreeType.BigTree, 10, TreeType.GroundBush, 50, TreeType.JungleTree, 35, TreeType.CocoaTree, 100);
                    this.ResourceSequence[this.ResourceCount++] = resource;
                    break;

            }
        if (this.defaultWaterLily > 0)
        {
            resource = Resource.createResource(this, AboveWaterGen.class, DefaultMaterial.WATER_LILY.id, this.defaultWaterLily, 100);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.defaultFlowers > 0)
        {
            // Red flower
            resource = Resource.createResource(this, PlantGen.class, DefaultMaterial.RED_ROSE.id, this.defaultFlowers, TCDefaultValues.roseDepositRarity.intValue(), TCDefaultValues.roseDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SOIL.id);
            this.ResourceSequence[this.ResourceCount++] = resource;

            // Yellow flower
            resource = Resource.createResource(this, PlantGen.class, DefaultMaterial.YELLOW_FLOWER.id, this.defaultFlowers, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SOIL.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.defaultMushroom > 0)
        {
            // Red mushroom
            resource = Resource.createResource(this, PlantGen.class, DefaultMaterial.RED_MUSHROOM.id, this.defaultMushroom, TCDefaultValues.redMushroomDepositRarity.intValue(), TCDefaultValues.redMushroomDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, defaultSurfaceBlock, DefaultMaterial.DIRT.id);
            this.ResourceSequence[this.ResourceCount++] = resource;

            // Brown mushroom
            resource = Resource.createResource(this, PlantGen.class, DefaultMaterial.BROWN_MUSHROOM.id, this.defaultMushroom, TCDefaultValues.brownMushroomDepositRarity.intValue(), TCDefaultValues.brownMushroomDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, defaultSurfaceBlock, DefaultMaterial.DIRT.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.defaultGrass > 0)
        {
            // Grass
            resource = Resource.createResource(this, GrassGen.class, DefaultMaterial.LONG_GRASS.id, 1, this.defaultGrass, TCDefaultValues.longGrassDepositRarity.intValue(), DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.defaultDeadBrush > 0)
        {
            // Dead Bush
            resource = Resource.createResource(this, GrassGen.class, DefaultMaterial.DEAD_BUSH.id, 0, this.defaultDeadBrush, TCDefaultValues.deadBushDepositRarity.intValue(), DefaultMaterial.SAND.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        // Pumpkin
        resource = Resource.createResource(this, PlantGen.class, DefaultMaterial.PUMPKIN.id, TCDefaultValues.pumpkinDepositFrequency.intValue(), TCDefaultValues.pumpkinDepositRarity.intValue(), TCDefaultValues.pumpkinDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        if (this.defaultReed > 0)
        {
            // Reed
            resource = Resource.createResource(this, ReedGen.class, DefaultMaterial.SUGAR_CANE_BLOCK.id, this.defaultReed, TCDefaultValues.reedDepositRarity.intValue(), TCDefaultValues.reedDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SAND.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        if (this.defaultCactus > 0)
        {
            // Cactus
            resource = Resource.createResource(this, CactusGen.class, DefaultMaterial.CACTUS.id, this.defaultCactus, TCDefaultValues.cactusDepositRarity.intValue(), TCDefaultValues.cactusDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.SAND.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }
        if (biome == DefaultBiome.JUNGLE || biome == DefaultBiome.JUNGLE_HILLS)
        {
            resource = Resource.createResource(this, VinesGen.class, TCDefaultValues.vinesFrequency.intValue(), TCDefaultValues.vinesRarity.intValue(), TCDefaultValues.vinesMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.VINE.id);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }

        // Water source
        resource = Resource.createResource(this, LiquidGen.class, DefaultMaterial.WATER.id, TCDefaultValues.waterSourceDepositFrequency.intValue(), TCDefaultValues.waterSourceDepositRarity.intValue(), TCDefaultValues.waterSourceDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Lava source
        resource = Resource.createResource(this, LiquidGen.class, DefaultMaterial.LAVA.id, TCDefaultValues.lavaSourceDepositFrequency.intValue(), TCDefaultValues.lavaSourceDepositRarity.intValue(), TCDefaultValues.lavaSourceDepositMinAltitude.intValue(), this.worldConfig.WorldHeight, DefaultMaterial.STONE.id);
        this.ResourceSequence[this.ResourceCount++] = resource;

        // Desert wells
        if (this.defaultWell != null)
        {
            resource = Resource.createResource(this, WellGen.class, this.defaultWell);
            this.ResourceSequence[this.ResourceCount++] = resource;
        }
    }

    @Override
    protected void readConfigSettings()
    {
        this.BiomeSize = readModSettings(TCDefaultValues.BiomeSize.name(), this.defaultSize);
        this.BiomeRarity = readModSettings(TCDefaultValues.BiomeRarity.name(), this.defaultRarity);

        this.BiomeColor = readModSettings(TCDefaultValues.BiomeColor.name(), this.defaultColor);

        this.riverBiome = readModSettings(TCDefaultValues.RiverBiome.name(), this.defaultRiverBiome);

        this.IsleInBiome = readModSettings(TCDefaultValues.IsleInBiome.name(), this.defaultIsle);
        this.BiomeIsBorder = readModSettings(TCDefaultValues.BiomeIsBorder.name(), this.defaultBorder);
        this.NotBorderNear = readModSettings(TCDefaultValues.NotBorderNear.name(), this.defaultNotBorderNear);

        this.BiomeTemperature = readModSettings(TCDefaultValues.BiomeTemperature.name(), this.defaultBiomeTemperature);
        this.BiomeWetness = readModSettings(TCDefaultValues.BiomeWetness.name(), this.defaultBiomeWetness);

        this.ReplaceBiomeName = readSettings(TCDefaultValues.ReplaceToBiomeName);

        this.BiomeHeight = readModSettings(TCDefaultValues.BiomeHeight.name(), this.defaultBiomeSurface);
        this.BiomeVolatility = readModSettings(TCDefaultValues.BiomeVolatility.name(), this.defaultBiomeVolatility);
        this.SmoothRadius = readSettings(TCDefaultValues.SmoothRadius);

        this.StoneBlock = readSettings(TCDefaultValues.StoneBlock);
        this.SurfaceBlock = readModSettings(TCDefaultValues.SurfaceBlock.name(), this.defaultSurfaceBlock);
        this.GroundBlock = readModSettings(TCDefaultValues.GroundBlock.name(), this.defaultGroundBlock);

        this.UseWorldWaterLevel = readSettings(TCDefaultValues.UseWorldWaterLevel);
        this.waterLevelMax = readSettings(TCDefaultValues.WaterLevelMax);
        this.waterLevelMin = readSettings(TCDefaultValues.WaterLevelMin);
        this.waterBlock = readSettings(TCDefaultValues.WaterBlock);
        this.iceBlock = readSettings(TCDefaultValues.IceBlock);

        this.SkyColor = readSettings(TCDefaultValues.SkyColor);
        this.WaterColor = readModSettingsColor(TCDefaultValues.WaterColor.name(), this.defaultWaterColorMultiplier);
        this.GrassColor = readModSettingsColor(TCDefaultValues.GrassColor.name(), this.defaultGrassColor);
        this.GrassColorIsMultiplier = readSettings(TCDefaultValues.GrassColorIsMultiplier);
        this.FoliageColor = readModSettingsColor(TCDefaultValues.FoliageColor.name(), this.defaultFoliageColor);
        this.FoliageColorIsMultiplier = readSettings(TCDefaultValues.FoliageColorIsMultiplier);

        this.volatilityRaw1 = readSettings(TCDefaultValues.Volatility1);
        this.volatilityRaw2 = readSettings(TCDefaultValues.Volatility2);
        this.volatilityWeightRaw1 = readSettings(TCDefaultValues.VolatilityWeight1);
        this.volatilityWeightRaw2 = readSettings(TCDefaultValues.VolatilityWeight2);
        this.disableNotchHeightControl = readSettings(TCDefaultValues.DisableBiomeHeight);
        this.maxAverageHeight = readSettings(TCDefaultValues.MaxAverageHeight);
        this.maxAverageDepth = readSettings(TCDefaultValues.MaxAverageDepth);

        this.riverHeight = readSettings(TCDefaultValues.RiverHeight);
        this.riverVolatility = readSettings(TCDefaultValues.RiverVolatility);
        this.riverWaterLevel = readSettings(TCDefaultValues.RiverWaterLevel);

        this.strongholdsEnabled = readModSettings(TCDefaultValues.StrongholdsEnabled.name(), this.defaultStrongholds);
        this.netherFortressesEnabled = readModSettings(TCDefaultValues.NetherFortressesEnabled.name(), true);
        this.villageType = (VillageType) readModSettings(TCDefaultValues.VillageType.name(), this.defaultVillageType);
        this.mineshaftsRarity = readSettings(TCDefaultValues.MineshaftRarity);
        this.rareBuildingType = (RareBuildingType) readModSettings(TCDefaultValues.RareBuildingType.name(), this.defaultRareBuildingType);

        if (DefaultBiome.getBiome(this.Biome.getId()) == null)
        {
            // Only for custom biomes
            this.spawnMonstersAddDefaults = readModSettings("spawnMonstersAddDefaults", true);
            this.spawnMonsters = readModSettings("spawnMonsters", new ArrayList<WeightedMobSpawnGroup>());
            this.spawnCreaturesAddDefaults = readModSettings("spawnCreaturesAddDefaults", true);
            this.spawnCreatures = readModSettings("spawnCreatures", new ArrayList<WeightedMobSpawnGroup>());
            this.spawnWaterCreaturesAddDefaults = readModSettings("spawnWaterCreaturesAddDefaults", true);
            this.spawnWaterCreatures = readModSettings("spawnWaterCreatures", new ArrayList<WeightedMobSpawnGroup>());
            this.spawnAmbientCreaturesAddDefaults = readModSettings("spawnAmbientCreaturesAddDefaults", true);
            this.spawnAmbientCreatures = readModSettings("spawnAmbientCreatures", new ArrayList<WeightedMobSpawnGroup>());
        }

        this.ReadCustomObjectSettings();
        this.ReadReplaceSettings();
        this.ReadResourceSettings();
        this.heightMatrix = new double[this.worldConfig.WorldHeight / 8 + 1];
        this.readHeightSettings(this.heightMatrix, TCDefaultValues.CustomHeightControl);
        this.riverHeightMatrix = new double[this.worldConfig.WorldHeight / 8 + 1];
        this.readHeightSettings(this.riverHeightMatrix, TCDefaultValues.RiverCustomHeightControl);
    }

    private void readHeightSettings(double[] heightMatrix, TCSetting setting)
    {
        ArrayList<String> keys = readSettings(setting);
        try
        {
            for (int i = 0; i < heightMatrix.length && i < keys.size(); i++)
            {
                heightMatrix[i] = Double.parseDouble(keys.get(i));
            }
        } catch (NumberFormatException e)
        {
            logSettingValueInvalid(setting.name(), e);
        }
    }

    private void ReadReplaceSettings()
    {
        String settingValue = readModSettings("ReplacedBlocks", "None");

        if (settingValue.equals("") || settingValue.equals("None"))
            return;

        String[] keys = readComplexString(settingValue);
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
                    if (values.length == 5)
                    {
                        // Replace in TC 2.3 style found
                        values = new String[]
                        {
                            values[0], values[1] + ":" + values[2], values[3], "" + (Integer.parseInt(values[4]) - 1)
                        };
                    }

                    if (values.length != 2 && values.length != 4)
                        continue;

                    short fromBlockId = (short) StringHelper.readBlockId(values[0]);
                    short toBlockId = (short) StringHelper.readBlockId(values[1]);
                    short blockData = (short) StringHelper.readBlockData(values[1]);

                    int minY = 0;
                    int maxY = worldConfig.WorldHeight - 1;

                    if (values.length == 4)
                    {
                        minY = Integer.valueOf(values[2]);
                        maxY = Integer.valueOf(values[3]);
                        minY = applyBounds(minY, 0, worldConfig.WorldHeight - 1);
                        maxY = applyBounds(maxY, minY, worldConfig.WorldHeight - 1);
                    }

                    if (this.replaceMatrixBlocks[fromBlockId] == null)
                    {
                        this.replaceMatrixBlocks[fromBlockId] = new short[worldConfig.WorldHeight];
                        for (int i = 0; i < worldConfig.WorldHeight; i++)
                            this.replaceMatrixBlocks[fromBlockId][i] = -1;
                    }
                    for (int y = minY; y <= maxY; y++)
                        this.replaceMatrixBlocks[fromBlockId][y] = (short) (toBlockId << 4 | blockData);
                    ReplaceCount++;

                }

            }

        } catch (NumberFormatException e)
        {
            TerrainControl.log(Level.WARNING, "Wrong replace settings: ''{0}''", this.settingsCache.get(settingValue));
        } catch (InvalidConfigException e)
        {
            TerrainControl.log(Level.WARNING, "Wrong replace settings: ''{0}''", this.settingsCache.get(settingValue));
        }

    }

    private void ReadResourceSettings()
    {
        ArrayList<Integer> LineNumbers = new ArrayList<Integer>();

        for (Map.Entry<String, String> entry : this.settingsCache.entrySet())
        {
            String key = entry.getKey();
            int start = key.indexOf("(");
            int end = key.lastIndexOf(")");
            if (start != -1 && end != -1)
            {
                String name = key.substring(0, start);
                String[] props = readComplexString(key.substring(start + 1, end));

                ConfigFunction<BiomeConfig> res = TerrainControl.getConfigFunctionsManager().getConfigFunction(name, this, this.name + " on line " + entry.getValue(), Arrays.asList(props));

                if (res != null)
                {

                    if (res instanceof SaplingGen && res.isValid())
                    {
                        SaplingGen sapling = (SaplingGen) res;
                        if (sapling.saplingType == SaplingType.All)
                            this.saplingResource = sapling;
                        else
                            this.saplingTypes[sapling.saplingType.getSaplingId()] = sapling;

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
        String biomeObjectsValue = readModSettings("biomeobjects", "");
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

    @Override
    protected void writeConfigSettings() throws IOException
    {
        if (DefaultBiome.getBiome(this.Biome.getId()) != null)
        {
            writeComment("This is the biome config file of the " + this.name + " biome, which is one of the vanilla biomes.");
        } else
        {
            writeComment("This is the biome config file of the " + this.name + " biome, which is a custom biome.");
        }
        writeNewLine();

        writeBigTitle("Biome placement");

        writeComment("Biome size from 0 to GenerationDepth. Defines in which biome layer this biome will be generated (see GenerationDepth).");
        writeComment("Higher numbers give a smaller biome, lower numbers a larger biome.");
        writeComment("Oceans and rivers are generated using a dirrerent algorithm in the default settings,");
        writeComment("(they aren't in one of the biome lists), so this setting won't affect them.");
        writeValue(TCDefaultValues.BiomeSize.name(), this.BiomeSize);
        this.writeNewLine();

        // TODO
        writeComment("Biome rarity from 100 to 1. If this is normal or ice biome - chance for spawn this biome then others.");
        writeComment("Example for normal biome :");
        writeComment("  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).");
        writeComment("  50 rarity mean 1/11 chance than other");
        writeComment("For isle biome this is chance to spawn isle in good place.");
        writeComment("Don`t work on Ocean and River (frozen versions too) biomes until not added as normal biome.");
        writeValue(TCDefaultValues.BiomeRarity.name(), this.BiomeRarity);
        this.writeNewLine();

        writeComment("The hexadecimal color value of this biome. Used in the output of the /tc map command,");
        writeComment("and used in the input of BiomeMode:FromImage.");
        writeValue(TCDefaultValues.BiomeColor.name(), this.BiomeColor);
        this.writeNewLine();

        writeComment("Replace this biome to specified after all generations. Warning this will cause saplings and mob spawning work as in specified biome");
        writeValue(TCDefaultValues.ReplaceToBiomeName.name(), this.ReplaceBiomeName);
        this.writeNewLine();

        writeSmallTitle("Isle biomes only");

        writeComment("Biome name list where this biome will be spawned as isle. Like Mushroom isle in Ocean.  This work only if this biome is in IsleBiomes in world config");
        writeValue(TCDefaultValues.IsleInBiome.name(), this.IsleInBiome);
        this.writeNewLine();

        writeSmallTitle("Border biomes only");

        writeComment("Biome name list where this biome will be border.Like Mushroom isle shore. Use is compared as IsleInBiome");
        writeValue(TCDefaultValues.BiomeIsBorder.name(), this.BiomeIsBorder);
        this.writeNewLine();

        writeComment("Biome name list near border is not applied. ");
        writeValue(TCDefaultValues.NotBorderNear.name(), this.NotBorderNear);
        this.writeNewLine();

        writeBigTitle("Biome height and volatility");
        writeComment("BiomeHeight mean how much height will be added in terrain generation");
        writeComment("It is double value from -10.0 to 10.0");
        writeComment("Value 0.0 equivalent half of map height with all other default settings");
        writeValue(TCDefaultValues.BiomeHeight.name(), this.BiomeHeight);

        this.writeNewLine();
        writeComment("Biome volatility.");
        writeValue(TCDefaultValues.BiomeVolatility.name(), this.BiomeVolatility);

        this.writeNewLine();
        writeComment("Smooth radius between biomes. Must be between 0 and 32, inclusive. The resulting");
        writeComment("smooth radius seems to be  (thisSmoothRadius + 1 + smoothRadiusOfBiomeOnOtherSide) * 4 .");
        writeComment("So if two biomes next to each other have both a smooth radius of 2, the");
        writeComment("resulting smooth area will be (2 + 1 + 2) * 4 = 20 blocks wide.");
        writeValue(TCDefaultValues.SmoothRadius.name(), this.SmoothRadius);

        writeNewLine();
        writeComment("If this value is greater than 0, then it will affect how much, on average, the terrain will rise before leveling off when it begins to increase in elevation.");
        writeComment("If the value is less than 0, then it will cause the terrain to either increase to a lower height before leveling out or decrease in height if the value is a large enough negative.");
        writeValue(TCDefaultValues.MaxAverageHeight.name(), this.maxAverageHeight);

        writeNewLine();
        writeComment("If this value is greater than 0, then it will affect how much, on average, the terrain (usually at the ottom of the ocean) will fall before leveling off when it begins to decrease in elevation. ");
        writeComment("If the value is less than 0, then it will cause the terrain to either fall to a lesser depth before leveling out or increase in height if the value is a large enough negative.");
        writeValue(TCDefaultValues.MaxAverageDepth.name(), this.maxAverageDepth);

        writeNewLine();
        writeComment("Another type of noise. This noise is independent from biomes. The larger the values the more chaotic/volatile landscape generation becomes.");
        writeComment("Setting the values to negative will have the opposite effect and make landscape generation calmer/gentler.");
        writeValue(TCDefaultValues.Volatility1.name(), this.volatilityRaw1);
        writeValue(TCDefaultValues.Volatility2.name(), this.volatilityRaw2);

        writeNewLine();
        writeComment("Adjust the weight of the corresponding volatility settings. This allows you to change how prevalent you want either of the volatility settings to be in the terrain.");
        writeValue(TCDefaultValues.VolatilityWeight1.name(), this.volatilityWeightRaw1);
        writeValue(TCDefaultValues.VolatilityWeight2.name(), this.volatilityWeightRaw2);

        writeNewLine();
        writeComment("Disable all noises except Volatility1 and Volatility2. Also disable default block chance from height.");
        writeValue(TCDefaultValues.DisableBiomeHeight.name(), this.disableNotchHeightControl);
        writeNewLine();
        writeComment("List of custom height factor, 17 double entries, each entire control of about 7 blocks height from down. Positive entry - better chance of spawn blocks, negative - smaller");
        writeComment("Values which affect your configuration may be found only experimental. That may be very big, like ~3000.0 depends from height");
        writeComment("Example:");
        writeComment("  CustomHeightControl:0.0,-2500.0,0.0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0");
        writeComment("Make empty layer above bedrock layer. ");
        writeHeightSettings(this.heightMatrix, TCDefaultValues.CustomHeightControl);

        writeBigTitle("Rivers");
        writeComment("There are two different river systems - the standard one and the improved one.");
        writeComment("See the ImprovedRivers settting in the WorldConfig. Both modes have different");
        writeComment("river settings, so carefully read the headers to know which settings you can use.");
        writeNewLine();

        writeSmallTitle("ImprovedRivers:false");
        writeComment("Only available when ImprovedRivers is set to false in the WorldConfig.");
        writeComment("Sets which biome is used as the river biome.");
        writeValue(TCDefaultValues.RiverBiome.name(), this.riverBiome);
        writeNewLine();

        writeSmallTitle("ImprovedRivers:true");
        writeComment("Only available when ImprovedRivers is set to true in the WorldConfig.");
        writeComment("");
        writeComment("Works the same as BiomeHeight (scroll up), but is used where a river is generated in this biome");
        writeValue(TCDefaultValues.RiverHeight.name(), this.riverHeight);
        writeNewLine();

        writeComment("Works the same as BiomeVolatility (scroll up), but is used where a river is generated in this biome");
        writeValue(TCDefaultValues.RiverVolatility.name(), this.riverVolatility);
        writeNewLine();

        writeComment("Works the same as WaterLevelMax (scroll down), but is used where a river is generated in this biome");
        writeComment("Can be used to create elevated rivers");
        writeValue(TCDefaultValues.RiverWaterLevel.name(), this.riverWaterLevel);
        writeNewLine();

        writeComment("Works the same as CustomHeightControl (scroll up), but is used where a river is generated in this biome");
        writeHeightSettings(this.riverHeightMatrix, TCDefaultValues.RiverCustomHeightControl);
        writeNewLine();

        this.writeBigTitle("Blocks");

        this.writeNewLine();
        writeComment("Stone block id");
        writeValue(TCDefaultValues.StoneBlock.name(), this.StoneBlock);

        this.writeNewLine();
        writeComment("Surface block id");
        writeValue(TCDefaultValues.SurfaceBlock.name(), this.SurfaceBlock);

        this.writeNewLine();
        writeComment("Block id from stone to surface, like dirt in plain biome ");
        writeValue(TCDefaultValues.GroundBlock.name(), this.GroundBlock);

        this.writeNewLine();
        writeComment("Replace Variable: (blockFrom,blockTo[:blockDataTo][,minHeight,maxHeight])");
        writeComment("Example :");
        writeComment("  ReplacedBlocks:(GRASS,DIRT,100,127),(GRAVEL,GLASS)");
        writeComment("Replace grass block to dirt from 100 to 127 height and replace gravel to glass on all height ");
        WriteModReplaceSettings();

        this.writeBigTitle("Water and ice");

        writeComment("Set this to false to use the water and ice settings of this biome.");
        writeValue(TCDefaultValues.UseWorldWaterLevel.name(), this.UseWorldWaterLevel);
        writeNewLine();

        writeComment("Set water level. Every empty between this levels will be fill water or another block from WaterBlock.");
        writeValue(TCDefaultValues.WaterLevelMax.name(), this.waterLevelMax);
        writeValue(TCDefaultValues.WaterLevelMin.name(), this.waterLevelMin);
        writeNewLine();
        writeComment("BlockId used as water in WaterLevelMax");
        writeValue(TCDefaultValues.WaterBlock.name(), this.waterBlock);
        writeNewLine();
        writeComment("BlockId used as ice. Ice only spawns if the BiomeTemperture is low enough.");
        writeValue(TCDefaultValues.IceBlock.name(), this.iceBlock);

        this.writeBigTitle("Visuals and weather");
        this.writeComment("Most of the settings here only have an effect on players with the client version of Terrain Control installed.");

        writeComment("Biome temperature. Float value from 0.0 to 1.0.");
        writeValue(TCDefaultValues.BiomeTemperature.name(), this.BiomeTemperature);
        this.writeNewLine();

        writeComment("Biome wetness. Float value from 0.0 to 1.0.");
        writeValue(TCDefaultValues.BiomeWetness.name(), this.BiomeWetness);
        this.writeNewLine();

        this.writeComment("Biome sky color.");
        this.writeColorValue(TCDefaultValues.SkyColor.name(), this.SkyColor);

        this.writeNewLine();
        this.writeComment("Biome water color multiplier.");
        this.writeColorValue(TCDefaultValues.WaterColor.name(), this.WaterColor);

        this.writeNewLine();
        this.writeComment("Biome grass color.");
        this.writeColorValue(TCDefaultValues.GrassColor.name(), this.GrassColor);

        this.writeNewLine();
        this.writeComment("Whether the grass color is a multiplier.");
        this.writeComment("If you set it to true, the color will be based on this value, the BiomeTemperature and the BiomeWetness.");
        this.writeComment("If you set it to false, the grass color will be just this color.");
        this.writeValue(TCDefaultValues.GrassColorIsMultiplier.name(), this.GrassColorIsMultiplier);

        this.writeNewLine();
        this.writeComment("Biome foliage color.");
        this.writeColorValue(TCDefaultValues.FoliageColor.name(), this.FoliageColor);

        this.writeNewLine();
        this.writeComment("Whether the foliage color is a multiplier. See GrassColorIsMultiplier for details.");
        this.writeValue(TCDefaultValues.FoliageColorIsMultiplier.name(), this.FoliageColorIsMultiplier);

        this.writeNewLine();
        this.writeBigTitle("Resource queue");
        this.writeComment("This section control all resources spawning after terrain generation.");
        this.writeComment("The resources will be placed in this order.");
        this.writeComment("");
        this.writeComment("Keep in mind that a high size, frequency or rarity might slow down terrain generation.");
        this.writeComment("");
        this.writeComment("Possible resources:");
        this.writeComment("SmallLake(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("Dungeon(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("Ore(Block[:Data],Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("UnderWaterOre(Block[:Data],Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("CustomObject(Object[,AnotherObject[,...]])");
        this.writeComment("CustomStructure([Object,Object_Chance[,AnotherObject,Object_Chance[,...]]])");
        this.writeComment("Tree(Frequency,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])");
        this.writeComment("Plant(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Grass(Block,BlockData,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Reed(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Cactus(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("Liquid(Block[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])");
        this.writeComment("AboveWaterRes(Block[:Data],Frequency,Rarity)");
        this.writeComment("Vines(Frequency,Rarity,MinAltitude,MaxAltitude)");
        this.writeComment("Vein(Block[:Data],MinRadius,MaxRadius,Rarity,OreSize,OreFrequency,OreRarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])");
        this.writeComment("Well(BaseBlock[:Data],HalfSlabBlock[:Data],WaterBlock[:Data],Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])");
        this.writeComment("");
        this.writeComment("Block and BlockSource: can be id or name, Frequency - is count of attempts for place resource");
        this.writeComment("Rarity: chance for each attempt, Rarity:100 - mean 100% to pass, Rarity:1 - mean 1% to pass");
        this.writeComment("MinAltitude and MaxAltitude: height limits");
        this.writeComment("BlockSource: mean where or whereupon resource will be placed ");
        this.writeComment("TreeType: Tree - BigTree - Forest (a birch tree) - HugeMushroom (not a tree but still counts)");
        this.writeComment("   Taiga1 - Taiga2 - JungleTree (the huge jungle tree) - GroundBush - CocoaTree");
        this.writeComment("   You can also use your own custom objects, as long as they have set Tree to true in their settings.");
        this.writeComment("TreeType_Chance: similar Rarity. Example:");
        this.writeComment("  Tree(10,Taiga1,35,Taiga2,100) - plugin tries 10 times, for each attempt it tries to place Taiga1 (35% chance),");
        this.writeComment("  if that fails, it attempts to place Taiga2 (100% chance).");
        this.writeComment("Object: can be a any kind of custom object (bo2 or bo3) but without the file extension. You can");
        this.writeComment("also use UseWorld to spawn one of the object in the WorldObjects folder and UseBiome to spawn");
        this.writeComment("one of the objects in the BiomeObjects setting. When using BO2s for UseWorld, the BO2 must have");
        this.writeComment("this biome in their spawnInBiome setting.");
        this.writeComment("Object_Chance: Like TreeType_Chance.");
        this.writeComment("");
        this.writeComment("Plant and Grass resource: both a resource of one block. Plant can place blocks underground, Grass cannot.");
        this.writeComment("Liquid resource: a one-block water or lava source");
        this.writeComment("SmallLake and UnderGroundLake resources: small lakes of about 8x8 blocks");
        this.writeComment("Vein resource: not in vanilla. Starts an area where ores will spawn. Can be slow, so use a low Rarity (smaller than 1).");
        this.writeComment("CustomStructure resource: starts a BO3 structure in the chunk.");
        this.writeComment("");

        this.WriteResources();

        this.writeNewLine();
        this.writeBigTitle("Sapling resource");
        this.writeComment("Terrain Control allows you to grow your custom objects from saplings, instead");
        this.writeComment("of the vanilla trees. Add one or more Sapling functions here to override vanilla");
        this.writeComment("spawning for that sapling.");
        this.writeComment("");
        this.writeComment("The syntax is: Sapling(SaplingType,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])");
        this.writeComment("Works like Tree resource instead first parameter.");
        this.writeComment("");
        this.writeComment("Sapling types: " + StringHelper.join(SaplingType.values(), ", "));
        this.writeComment("All - will make the tree spawn from all saplings, but not from mushrooms.");
        this.writeComment("BigJungle - for when 4 jungle saplings grow at once.");
        this.writeComment("RedMushroom/BrownMushroom - will only grow when bonemeal is used.");
        this.WriteSaplingSettings();

        this.writeBigTitle("Custom objects");
        this.writeComment("These objects will spawn when using the UseBiome keyword.");
        this.WriteCustomObjects();

        this.writeBigTitle("Structures");
        this.writeComment("Here you can change, enable or disable the stuctures.");
        this.writeComment("If you have disabled the structure in the WorldConfig, it won't spawn,");
        this.writeComment("regardless of these settings.");
        this.writeNewLine();
        this.writeComment("Disables strongholds for this biome. If there is no suitable biome nearby,");
        this.writeComment("Minecraft will ignore this setting.");
        this.writeValue(TCDefaultValues.StrongholdsEnabled.name(), strongholdsEnabled);
        this.writeNewLine();
        this.writeComment("Whether a Nether Fortress can start in this biome. Might extend to neighbor biomes.");
        this.writeValue(TCDefaultValues.NetherFortressesEnabled.name(), netherFortressesEnabled);
        this.writeNewLine();
        this.writeComment("The village type in this biome. Can be wood, sandstone or disabled.");
        this.writeValue(TCDefaultValues.VillageType.name(), villageType.toString());
        this.writeNewLine();
        this.writeComment("The mineshaft rarity from 0 to 100. 0 = no mineshafts, 1 = default rarity, 100 = a wooden chaos.");
        this.writeValue(TCDefaultValues.MineshaftRarity.name(), mineshaftsRarity);
        this.writeNewLine();
        this.writeComment("The type of the aboveground rare building in this biome. Can be desertPyramid, jungleTemple, swampHut or disabled.");
        this.writeValue(TCDefaultValues.RareBuildingType.name(), rareBuildingType.toString());

        this.writeNewLine();
        this.writeBigTitle("Mob spawning");
        if (DefaultBiome.getBiome(this.Biome.getId()) != null)
        {
            // Stop in the default biomes
            this.writeComment("Mob spawning control doesn't work in default biomes.");
            return;
        }

        this.writeComment("========<TUTORIAL>========");
        this.writeComment("This is where you configure mob spawning. Changing this section is optional.");
        this.writeComment("");
        this.writeComment("#STEP1: Understanding what a mobgroup is.");
        this.writeComment("A mobgroups is made of four parts. They are mob, weight, min and max.");
        this.writeComment("The mob is one of the Minecraft internal mob names.");
        this.writeComment("See http://www.minecraftwiki.net/wiki/Chunk_format#Mobs");
        this.writeComment("The weight is used for a random selection. This is a positive integer.");
        this.writeComment("The min is the minimum amount of mobs spawning as a group. This is a positive integer.");
        this.writeComment("The max is the maximum amount of mobs spawning as a group. This is a positive integer.");
        this.writeComment("");
        this.writeComment("#STEP2: Understanding how write a mobgroup as JSON as well as lists of them.");
        this.writeComment("Json is a tree document format: http://en.wikipedia.org/wiki/JSON");
        this.writeComment("Write a mobgroup like this: {\"mob\": \"mobname\", \"weight\": integer, \"min\": integer, \"max\": integer}");
        this.writeComment("For example: {\"mob\": \"Ocelot\", \"weight\": 10, \"min\": 2, \"max\": 6}");
        this.writeComment("For example: {\"mob\": \"MushroomCow\", \"weight\": 5, \"min\": 2, \"max\": 2}");
        this.writeComment("A json list of mobgroups looks like this: [mobgroup, mobgroup, mobgroup...]");
        this.writeComment("This would be an ampty list: []");
        this.writeComment("You can validate your json here: http://jsonlint.com/");
        this.writeComment("");
        this.writeComment("#STEP3: Understanding what to do with all this info");
        this.writeComment("There are three categories of mobs: monsters, creatures and watercreatures.");
        this.writeComment("These list may be populated with Default values if thee booleans bellow is set to true");
        this.writeComment("You may also add your own mobgroups in the lists below");
        this.writeComment("");
        this.writeComment("#STEP4: What is in the default mob groups?");
        this.writeComment("The default mob groups are controlled by vanilla minecraft.");
        this.writeComment("At 2012-03-24 you could find them here: https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/BiomeBase.java#L75");
        this.writeComment("In simple terms:");
        this.writeComment("default creatures: [{\"mob\": \"Sheep\", \"weight\": 12, \"min\": 4, \"max\": 4}, {\"mob\": \"Pig\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Chicken\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Cow\", \"weight\": 8, \"min\": 4, \"max\": 4}]");
        this.writeComment("default monsters: [{\"mob\": \"Spider\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Zombie\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Skeleton\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Creeper\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Slime\", \"weight\": 10, \"min\": 4, \"max\": 4}, {\"mob\": \"Enderman\", \"weight\": 1, \"min\": 1, \"max\": 4}]");
        this.writeComment("default watercreatures: [{\"mob\": \"Squid\", \"weight\": 10, \"min\": 4, \"max\": 4}]");
        this.writeComment("");
        this.writeComment("So for example ocelots wont spawn unless you add them below.");

        this.writeNewLine();
        this.writeComment("========<CONFIGURATION>========");

        this.writeComment("Should we add the default monster spawn groups?");
        writeValue("spawnMonstersAddDefaults", this.spawnMonstersAddDefaults);
        this.writeComment("Add extra monster spawn groups here");
        writeValue("spawnMonsters", this.spawnMonsters);
        this.writeNewLine();

        this.writeComment("Should we add the default creature spawn groups?");
        writeValue("spawnCreaturesAddDefaults", this.spawnCreaturesAddDefaults);
        this.writeComment("Add extra creature spawn groups here");
        writeValue("spawnCreatures", this.spawnCreatures);
        this.writeNewLine();

        this.writeComment("Should we add the default watercreature spawn groups?");
        writeValue("spawnWaterCreaturesAddDefaults", this.spawnWaterCreaturesAddDefaults);
        this.writeComment("Add extra watercreature spawn groups here");
        writeValue("spawnWaterCreatures", this.spawnWaterCreatures);
        this.writeNewLine();

        this.writeComment("Should we add the default ambient creature spawn groups? (Currently only bats)");
        writeValue("spawnAmbientCreaturesAddDefaults", this.spawnAmbientCreaturesAddDefaults);
        this.writeComment("Add extra ambient creature spawn groups here");
        writeValue("spawnAmbientCreatures", this.spawnAmbientCreatures);
        this.writeNewLine();

    }

    private void writeHeightSettings(double[] heightMatrix, TCSetting setting) throws IOException
    {
        String output = Double.toString(heightMatrix[0]);
        for (int i = 1; i < heightMatrix.length; i++)
            output = output + "," + Double.toString(heightMatrix[i]);

        this.writeValue(setting.name(), output);
    }

    private void WriteModReplaceSettings() throws IOException
    {
        if (this.ReplaceCount == 0)
        {
            this.writeValue("ReplacedBlocks", "None");
            return;
        }
        String output = "";

        // Read all block ids
        for (int blockIdFrom = 0; blockIdFrom < replaceMatrixBlocks.length; blockIdFrom++)
        {
            if (replaceMatrixBlocks[blockIdFrom] == null)
                continue;

            int previousReplaceTo = -1; // What the y coord just below had it's
                                        // replace setting set to
            int yStart = 0;

            for (int y = 0; y <= replaceMatrixBlocks[blockIdFrom].length; y++)
            {
                int currentReplaceTo = (y == replaceMatrixBlocks[blockIdFrom].length) ? -1 : replaceMatrixBlocks[blockIdFrom][y];

                if (currentReplaceTo == previousReplaceTo)
                {
                    // Same as the previous entry, do nothing
                    continue;
                }

                // Not the same as the previous entry, previous entry wasn't -1
                // So we have found the end of a replace setting
                if (previousReplaceTo != -1)
                {
                    output += "(" + StringHelper.makeMaterial(blockIdFrom) + ",";
                    if (yStart != 0 || y != (replaceMatrixBlocks[blockIdFrom].length))
                    {
                        // Long form
                        output += StringHelper.makeMaterial(previousReplaceTo >> 4, previousReplaceTo & 0xF) + "," + yStart + "," + (y - 1);
                    } else
                    {
                        // Short form
                        output += StringHelper.makeMaterial(previousReplaceTo >> 4, previousReplaceTo & 0xF);
                    }
                    output += "),";

                    // Reset the previousReplaceTo
                    previousReplaceTo = -1;
                }

                if (previousReplaceTo == -1)
                {
                    // Not the same as the previous entry, previous entry was -1
                    // So we have found the start of a new replace setting
                    yStart = y;
                    previousReplaceTo = currentReplaceTo;
                }
            }
        }
        this.writeValue("ReplacedBlocks", output.substring(0, output.length() - 1));
    }

    private void WriteResources() throws IOException
    {
        for (int i = 0; i < this.ResourceCount; i++)
        {
            this.writeValue(this.ResourceSequence[i].write());
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
        this.writeValue("BiomeObjects", builder.toString());
    }

    private void WriteSaplingSettings() throws IOException
    {
        if (this.saplingResource != null)
            this.writeValue(saplingResource.write());

        for (SaplingGen res : this.saplingTypes)
            if (res != null)
                this.writeValue(res.write());

    }

    protected void correctSettings()
    {
        this.BiomeSize = applyBounds(this.BiomeSize, 0, this.worldConfig.GenerationDepth);
        this.BiomeHeight = (float) applyBounds(this.BiomeHeight, -10.0, 10.0);
        this.BiomeRarity = applyBounds(this.BiomeRarity, 1, this.worldConfig.BiomeRarityScale);

        this.SmoothRadius = applyBounds(this.SmoothRadius,0,32);

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
        
        this.riverBiome = (DefaultBiome.Contain(this.riverBiome) || this.worldConfig.CustomBiomes.contains(this.riverBiome)) ? this.riverBiome : "";
    }

    protected void renameOldSettings()
    {
        // Old values from WorldConfig
        TCDefaultValues[] copyFromWorld =
        {
            TCDefaultValues.MaxAverageHeight, TCDefaultValues.MaxAverageDepth, TCDefaultValues.Volatility1, TCDefaultValues.Volatility2, TCDefaultValues.VolatilityWeight1, TCDefaultValues.VolatilityWeight2, TCDefaultValues.DisableBiomeHeight, TCDefaultValues.CustomHeightControl
        };
        for (TCDefaultValues value : copyFromWorld)
            if (this.worldConfig.settingsCache.containsKey(value.name().toLowerCase()))
            {
                // this.SettingsCache.put(value.name(),
                // this.worldConfig.SettingsCache.get(value.name().toLowerCase()));
                this.settingsCache.put(value.name().toLowerCase(), this.worldConfig.settingsCache.get(value.name().toLowerCase()));
            }

        // disableNotchPonds
        if (this.settingsCache.containsKey("disableNotchPonds".toLowerCase()))
        {
            if (!readModSettings("disableNotchPonds".toLowerCase(), false))
            {
                this.settingsCache.put("SmallLake(WATER,4,7,8," + this.worldConfig.WorldHeight + ")", "0");
                this.settingsCache.put("SmallLake(LAVA,2,3,8," + (this.worldConfig.WorldHeight - 8) + ")", "1");
            }

        }

        // CustomTreeChance
        int customTreeChance = 0; // Default value
        if (worldConfig.settingsCache.containsKey("customtreechance"))
        {
            try
            {
                customTreeChance = Integer.parseInt(worldConfig.settingsCache.get("customtreechance"));
            } catch (NumberFormatException e)
            {
                // Ignore, so leave customTreeChance at 0
            }
        }
        if (customTreeChance == 100)
        {
            this.settingsCache.put("Sapling(All,UseWorld,100)", "-");
        }
        if (customTreeChance > 0 && customTreeChance < 100)
        {
            this.settingsCache.put("Sapling(0,UseWorld," + customTreeChance + ",BigTree,10,Tree,100)", "-"); // Oak
            this.settingsCache.put("Sapling(1,UseWorld," + customTreeChance + ",Taiga2,100)", "-"); // Redwood
            this.settingsCache.put("Sapling(2,UseWorld," + customTreeChance + ",Forest,100)", "-"); // Birch
            this.settingsCache.put("Sapling(3,UseWorld," + customTreeChance + ",CocoaTree,100)", "-"); // Jungle
        }

        // BiomeRivers
        if (!readModSettings("BiomeRivers", true))
        {
            // If the rivers were disabled using the old setting, disable them
            // also using the new setting
            this.settingsCache.put("riverbiome", "");
        }

        // ReplacedBlocks
        String replacedBlocksValue = readModSettings("ReplacedBlocks", "None");

        if (replacedBlocksValue.contains("="))
        {
            String[] values = replacedBlocksValue.split(",");
            String output = "";

            for (String replacedBlock : values)
            {
                try
                {
                    String fromId = replacedBlock.split("=")[0];
                    String toId = replacedBlock.split("=")[1];

                    String toData = "0";
                    String minHeight = "0";
                    int maxHeight = worldConfig.WorldHeight;

                    boolean longForm = false;

                    int start = toId.indexOf("(");
                    int end = toId.indexOf(")");
                    if (start != -1 && end != -1)
                    {
                        String[] ranges = toId.substring(start + 1, end).split("-");
                        toId = toId.substring(0, start);
                        minHeight = ranges[0];
                        maxHeight = Integer.parseInt(ranges[1]);
                        longForm = true;
                    }
                    if (toId.contains("."))
                    {
                        String[] temp = toId.split("\\.");
                        toId = temp[0];
                        toData = temp[1];
                        longForm = true;
                    }

                    if (longForm)
                        output = output + "(" + fromId + "," + toId + ":" + toData + "," + minHeight + "," + (maxHeight - 1) + "),";
                    else
                        output = output + "(" + fromId + "," + toId + "),";
                } catch (Exception ignored)
                {
                }

            }

            this.settingsCache.put("replacedblocks", output.substring(0, output.length() - 1));
        }
    }
    protected boolean defaultWaterLakes = true;
    protected int defaultTrees = 1;
    protected int defaultFlowers = 2;
    protected int defaultGrass = 10;
    protected int defaultDeadBrush = 0;
    protected int defaultMushroom = 0;
    protected int defaultReed = 0;
    protected int defaultCactus = 0;
    protected int defaultClay = 1;
    protected Object[] defaultWell;
    protected float defaultBiomeSurface = 0.1F;
    protected float defaultBiomeVolatility = 0.3F;
    protected byte defaultSurfaceBlock = (byte) DefaultMaterial.GRASS.id;
    protected byte defaultGroundBlock = (byte) DefaultMaterial.DIRT.id;
    protected float defaultBiomeTemperature = 0.5F;
    protected float defaultBiomeWetness = 0.5F;
    protected ArrayList<String> defaultIsle = new ArrayList<String>();
    protected ArrayList<String> defaultBorder = new ArrayList<String>();
    protected ArrayList<String> defaultNotBorderNear = new ArrayList<String>();
    protected String defaultRiverBiome = DefaultBiome.RIVER.Name;
    protected int defaultSize = 4;
    protected int defaultRarity = 100;
    protected String defaultColor = "0x000000";
    protected int defaultWaterLily = 0;
    protected String defaultWaterColorMultiplier = "0xFFFFFF";
    protected String defaultGrassColor = "0xFFFFFF";
    protected String defaultFoliageColor = "0xFFFFFF";
    protected boolean defaultStrongholds = true;
    protected VillageType defaultVillageType = VillageType.disabled;
    protected RareBuildingType defaultRareBuildingType = RareBuildingType.disabled;

    protected void initDefaults()
    {
        this.defaultBiomeSurface = this.Biome.getSurfaceHeight();
        this.defaultBiomeVolatility = this.Biome.getSurfaceVolatility();
        this.defaultSurfaceBlock = this.Biome.getSurfaceBlock();
        this.defaultGroundBlock = this.Biome.getGroundBlock();
        this.defaultBiomeTemperature = this.Biome.getTemperature();
        this.defaultBiomeWetness = this.Biome.getWetness();

        switch (this.Biome.getId())
        {
            case 0: // Ocean
                this.defaultColor = "0x3333FF";
                this.defaultStrongholds = false;
                this.defaultRiverBiome = "";
                break;
            case 1: // Plains
                this.defaultTrees = 0;
                this.defaultFlowers = 4;
                this.defaultGrass = 100;
                this.defaultColor = "0x999900";
                this.defaultStrongholds = false;
                this.defaultVillageType = VillageType.wood;
                break;
            case 2: // Desert
                this.defaultWaterLakes = false;
                this.defaultTrees = 0;
                this.defaultDeadBrush = 4;
                this.defaultGrass = 0;
                this.defaultReed = 10;
                this.defaultCactus = 10;
                this.defaultColor = "0xFFCC33";
                this.defaultWell = new Object[]
                {
                    DefaultMaterial.SANDSTONE, DefaultMaterial.STEP + ":1", DefaultMaterial.WATER, 1, 0.1, 2, worldConfig.WorldHeight, DefaultMaterial.SAND
                };
                this.defaultVillageType = VillageType.sandstone;
                this.defaultRareBuildingType = RareBuildingType.desertPyramid;
                break;
            case 3: // Extreme hills
                this.defaultColor = "0x333300";
                break;
            case 4: // Forest
                this.defaultTrees = 10;
                this.defaultGrass = 15;
                this.defaultColor = "0x00FF00";
                break;
            case 5: // Taiga
                this.defaultTrees = 10;
                this.defaultGrass = 10;
                this.defaultColor = "0x007700";
                break;
            case 6: // Swampland
                this.defaultTrees = 2;
                this.defaultFlowers = -999;
                this.defaultDeadBrush = 1;
                this.defaultMushroom = 8;
                this.defaultReed = 10;
                this.defaultClay = 1;
                this.defaultWaterLily = 1;
                this.defaultColor = "0x99CC66";
                this.defaultWaterColorMultiplier = "0xe0ffae";
                this.defaultGrassColor = "0x7E6E7E";
                this.defaultFoliageColor = "0x7E6E7E";
                this.defaultRareBuildingType = RareBuildingType.swampHut;
                break;
            case 7: // River
                this.defaultSize = 8;
                this.defaultRarity = 95;
                this.defaultIsle.add(DefaultBiome.SWAMPLAND.Name);
                this.defaultColor = "0x00CCCC";
                this.defaultStrongholds = false;
            case 8: // Hell
            case 9: // Sky
                break;
            case 10: // FrozenOcean
                this.defaultColor = "0xFFFFFF";
                this.defaultStrongholds = false;
                this.defaultRiverBiome = "";
                break;
            case 11: // FrozenRiver
                this.defaultColor = "0x66FFFF";
                this.defaultStrongholds = false;
                break;
            case 12: // Ice Plains
                this.defaultColor = "0xCCCCCC";
                if (worldConfig.readModSettings("FrozenRivers", true))
                {
                    // Only make river frozen if there isn't some old setting
                    // that prevents it
                    this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
                }
                break;
            case 13: // Ice Mountains
                this.defaultColor = "0xCC9966";
                if (worldConfig.readModSettings("FrozenRivers", true))
                {
                    // Only make river frozen if there isn't some old setting
                    // that prevents it
                    this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
                }
                break;
            case 14: // MushroomIsland
                this.defaultSurfaceBlock = (byte) DefaultMaterial.MYCEL.id;
                this.defaultMushroom = 1;
                this.defaultGrass = 0;
                this.defaultFlowers = 0;
                this.defaultTrees = 0;
                this.defaultRarity = 1;
                this.defaultRiverBiome = "";
                this.defaultSize = 6;
                this.defaultIsle.add(DefaultBiome.OCEAN.Name);
                this.defaultColor = "0xFF33CC";
                this.defaultWaterLily = 1;
                this.defaultStrongholds = false;
                break;
            case 15: // MushroomIslandShore
                this.defaultRiverBiome = "";
                this.defaultSize = 9;
                this.defaultBorder.add(DefaultBiome.MUSHROOM_ISLAND.Name);
                this.defaultColor = "0xFF9999";
                this.defaultStrongholds = false;
                break;
            case 16: // Beach
                this.defaultTrees = 0;
                this.defaultSize = 8;
                this.defaultBorder.add(DefaultBiome.OCEAN.Name);
                this.defaultNotBorderNear.add(DefaultBiome.RIVER.Name);
                this.defaultNotBorderNear.add(DefaultBiome.SWAMPLAND.Name);
                this.defaultNotBorderNear.add(DefaultBiome.EXTREME_HILLS.Name);
                this.defaultNotBorderNear.add(DefaultBiome.MUSHROOM_ISLAND.Name);
                this.defaultColor = "0xFFFF00";
                this.defaultStrongholds = false;
                break;
            case 17: // DesertHills
                this.defaultWaterLakes = false;
                this.defaultSize = 6;
                this.defaultRarity = 97;
                this.defaultIsle.add(DefaultBiome.DESERT.Name);
                this.defaultTrees = 0;
                this.defaultDeadBrush = 4;
                this.defaultGrass = 0;
                this.defaultReed = 50;
                this.defaultCactus = 10;
                this.defaultColor = "0x996600";
                this.defaultWell = new Object[]
                {
                    DefaultMaterial.SANDSTONE, DefaultMaterial.STEP + ":1", DefaultMaterial.WATER, 1, 0.1, 2, worldConfig.WorldHeight, DefaultMaterial.SAND
                };
                this.defaultVillageType = VillageType.sandstone;
                this.defaultRareBuildingType = RareBuildingType.desertPyramid;
                break;
            case 18: // ForestHills
                this.defaultSize = 6;
                this.defaultRarity = 97;
                this.defaultIsle.add(DefaultBiome.FOREST.Name);
                this.defaultTrees = 10;
                this.defaultGrass = 15;
                this.defaultColor = "0x009900";
                break;
            case 19: // TaigaHills
                this.defaultSize = 6;
                this.defaultRarity = 97;
                this.defaultIsle.add(DefaultBiome.TAIGA.Name);
                this.defaultTrees = 10;
                this.defaultGrass = 10;
                this.defaultColor = "0x003300";
                this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
                break;
            case 20: // Extreme Hills Edge
                this.defaultSize = 8;
                this.defaultBorder.add(DefaultBiome.EXTREME_HILLS.Name);
                this.defaultColor = "0x666600";
                break;
            case 21: // Jungle
                this.defaultTrees = 50;
                this.defaultGrass = 25;
                this.defaultFlowers = 4;
                this.defaultColor = "0xCC6600";
                this.defaultRareBuildingType = RareBuildingType.jungleTemple;
                break;
            case 22: // JungleHills
                this.defaultTrees = 50;
                this.defaultGrass = 25;
                this.defaultFlowers = 4;
                this.defaultColor = "0x663300";
                this.defaultIsle.add(DefaultBiome.JUNGLE.Name);
                this.defaultRareBuildingType = RareBuildingType.jungleTemple;
                break;
        }
    }

    public void Serialize(DataOutputStream stream) throws IOException
    {
        writeStringToStream(stream, this.name);

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
        super(readStringFromStream(stream), null);
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