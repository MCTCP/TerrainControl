package com.khorn.terraincontrol.configuration.standard;

import static com.khorn.terraincontrol.TerrainControl.WORLD_DEPTH;
import static com.khorn.terraincontrol.TerrainControl.WORLD_HEIGHT;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import com.khorn.terraincontrol.configuration.ReplacedBlocksMatrix;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.configuration.settingType.DoubleArraySetting;
import com.khorn.terraincontrol.configuration.settingType.MaterialSetting;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.configuration.settingType.Settings;
import com.khorn.terraincontrol.generator.surface.SurfaceGenerator;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BiomeStandardValues extends Settings
{
    // >> Biome Extensions & Related
    public static final Collection<String> BiomeConfigExtensions = Arrays.asList("BiomeConfig.ini", ".biome", ".bc", ".bc.ini",
            ".biome.ini");

    public static final Setting<Boolean>
            RESOURCE_INHERITANCE = booleanSetting("ResourceInheritance", true),
            USE_WORLD_WATER_LEVEL = booleanSetting("UseWorldWaterLevel", true),
            GRASS_COLOR_IS_MULTIPLIER = booleanSetting("GrassColorIsMultiplier", true),
            FOLIAGE_COLOR_IS_MULTIPLIER = booleanSetting("FoliageColorIsMultiplier", true),
            DISABLE_BIOME_HEIGHT = booleanSetting("DisableBiomeHeight", false),
            STRONGHOLDS_ENABLED = WorldStandardValues.STRONGHOLDS_ENABLED,
            NETHER_FORTRESSES_ENABLED = WorldStandardValues.NETHER_FORTRESSES_ENABLED,
            SPAWN_MONSTERS_ADD_DEFAULTS = booleanSetting("SpawnMonstersAddDefaults", true),
            SPAWN_CREATURES_ADD_DEFAULTS = booleanSetting("SpawnCreaturesAddDefaults", true),
            SPAWN_WATER_CREATURES_ADD_DEFAULTS = booleanSetting("SpawnWaterCreaturesAddDefaults", true),
            SPAWN_AMBIENT_CREATURES_ADD_DEFAULTS = booleanSetting("SpawnAmbientCreaturesAddDefaults", true);

    public static final Setting<String>
            BIOME_CONFIG_EXTENSION = stringSetting("BiomeConfigExtension", ".bc"),
            BIOME_EXTENDS = stringSetting("BiomeExtends", ""),
            RIVER_BIOME = stringSetting("RiverBiome", "River"),
            REPLACE_TO_BIOME_NAME = stringSetting("ReplaceToBiomeName", "");

    public static final Setting<Integer>
            BIOME_SIZE = intSetting("BiomeSize", 5, 0, 20),
            BIOME_RARITY = intSetting("BiomeRarity", 100, 0, Integer.MAX_VALUE),
            SMOOTH_RADIUS = intSetting("SmoothRadius", 2, 0, 32),
            RIVER_WATER_LEVEL = intSetting("RiverWaterLevel", 63, WORLD_DEPTH, WORLD_HEIGHT),
            WATER_LEVEL_MAX = WorldStandardValues.WATER_LEVEL_MAX,
            WATER_LEVEL_MIN = WorldStandardValues.WATER_LEVEL_MIN;

    public static final Setting<Integer>
            BIOME_COLOR = colorSetting("BiomeColor", "#ffffff"),
            SKY_COLOR = colorSetting("SkyColor", "#7BA5FF"),
            WATER_COLOR = colorSetting("WaterColor", "#FFFFFF"),
            GRASS_COLOR = colorSetting("GrassColor", "#000000"),
            FOLIAGE_COLOR = colorSetting("FoliageColor", "#000000");

    public static final Setting<List<String>>
            ISLE_IN_BIOME = stringListSetting("IsleInBiome", "Ocean"),
            BIOME_IS_BORDER = stringListSetting("BiomeIsBorder"),
            NOT_BORDER_NEAR = stringListSetting("NotBorderNear"),
            BIOME_OBJECTS = stringListSetting("BiomeObjects");

    public static final Setting<Double>
            VOLATILITY_1 = doubleSetting("Volatility1", 0, -1000, 1000),
            VOLATILITY_2 = doubleSetting("Volatility2", 0, -1000, 1000),
            VOLATILITY_WEIGHT_1 = doubleSetting("VolatilityWeight1", 0.5, -1000, 1000),
            VOLATILITY_WEIGHT_2 = doubleSetting("VolatilityWeight2", 0.45, -1000, 1000),
            MAX_AVERAGE_HEIGHT = doubleSetting("MaxAverageHeight", 0, -1000, 1000),
            MAX_AVERAGE_DEPTH = doubleSetting("MaxAverageDepth", 0, -1000, 1000),
            MINESHAFT_RARITY = doubleSetting("MineshaftRarity", 1, 0, 100);

    public static final Setting<LocalMaterialData>
            STONE_BLOCK = new MaterialSetting("StoneBlock", DefaultMaterial.STONE),
            SURFACE_BLOCK = new MaterialSetting("SurfaceBlock", DefaultMaterial.GRASS),
            GROUND_BLOCK = new MaterialSetting("GroundBlock", DefaultMaterial.DIRT),
            WATER_BLOCK = WorldStandardValues.WATER_BLOCK,
            ICE_BLOCK = WorldStandardValues.ICE_BLOCK;

    public static final Setting<double[]>
            CUSTOM_HEIGHT_CONTROL = new DoubleArraySetting("CustomHeightControl"),
            RIVER_CUSTOM_HEIGHT_CONTROL = new DoubleArraySetting("RiverCustomHeightControl");

    public static final Setting<Float>
            BIOME_TEMPERATURE = floatSetting("BiomeTemperature", 0.5f, 0, 2),
            BIOME_WETNESS = floatSetting("BiomeWetness", 0.5f, 0, 1),
            BIOME_HEIGHT = floatSetting("BiomeHeight", 0.1f, -10, 10),
            BIOME_VOLATILITY = floatSetting("BiomeVolatility", 0.3f, -1000, 1000),
            RIVER_HEIGHT = floatSetting("RiverHeight", -1, -10, 10),
            RIVER_VOLATILITY = floatSetting("RiverVolatility", 0.3f, -1000, 1000);

    public static final Setting<List<WeightedMobSpawnGroup>>
            SPAWN_MONSTERS = mobGroupListSetting("SpawnMonsters"),
            SPAWN_CREATURES = mobGroupListSetting("SpawnCreatures"),
            SPAWN_WATER_CREATURES = mobGroupListSetting("SpawnWaterCreatures"),
            SPAWN_AMBIENT_CREATURES = mobGroupListSetting("SpawnAmbientCreatures");

    public static final Setting<VillageType> VILLAGE_TYPE = enumSetting("VillageType", VillageType.disabled);
    public static final Setting<RareBuildingType> RARE_BUILDING_TYPE = enumSetting("RareBuildingType", RareBuildingType.disabled);

    public static final Setting<SurfaceGenerator> SURFACE_AND_GROUND_CONTROL = surfaceGeneratorSetting("SurfaceAndGroundControl");
    public static final Setting<ReplacedBlocksMatrix> REPLACED_BLOCKS = replacedBlocksSetting("ReplacedBlocks");

    // Deprecated settings
    public static final Setting<Boolean> BIOME_RIVERS = booleanSetting("BiomeRivers", true);
    public static final Setting<Boolean> DISABLE_NOTCH_PONDS = booleanSetting("DisableNotchPonds", false);
    /**
     * Used to read ReplacedBlocks as a string, so that conversion to the new
     * format is possible.
     */
    public static final Setting<String> REPLACED_BLOCKS_OLD = stringSetting("ReplacedBlocks", "");
    // End biome settings

    // Resource settings
    public static final int SmallLakeWaterFrequency = 4;
    public static final int SmallLakeLavaFrequency = 2;
    public static final int SmallLakeWaterRarity = 7;
    public static final int SmallLakeLavaRarity = 1;
    public static final int SmallLakeMinAltitude = 8;
    public static final int SmallLakeMaxAltitude = 120;

    public static final int undergroundLakeFrequency = 2;
    public static final int undergroundLakeRarity = 5;
    public static final int undergroundLakeMinSize = 50;
    public static final int undergroundLakeMaxSize = 60;
    public static final int undergroundLakeMinAltitude = 0;
    public static final int undergroundLakeMaxAltitude = 50;

    public static final int dungeonRarity = 100;
    public static final int dungeonFrequency = 8;
    public static final int dungeonMinAltitude = 0;

    public static final int dirtDepositRarity = 100;
    public static final int dirtDepositFrequency = 20;
    public static final int dirtDepositSize = 32;
    public static final int dirtDepositMinAltitude = 0;
    public static final int dirtDepositMaxAltitude = 128;

    public static final int gravelDepositRarity = 100;
    public static final int gravelDepositFrequency = 10;
    public static final int gravelDepositSize = 32;
    public static final int gravelDepositMinAltitude = 0;
    public static final int gravelDepositMaxAltitude = 128;

    public static final int clayDepositRarity = 100;
    public static final int clayDepositFrequency = 1;
    public static final int clayDepositSize = 32;
    public static final int clayDepositMinAltitude = 0;
    public static final int clayDepositMaxAltitude = 128;

    public static final int coalDepositRarity = 100;
    public static final int coalDepositFrequency = 20;
    public static final int coalDepositSize = 16;
    public static final int coalDepositMinAltitude = 0;
    public static final int coalDepositMaxAltitude = 128;

    public static final int ironDepositRarity = 100;
    public static final int ironDepositFrequency = 20;
    public static final int ironDepositSize = 8;
    public static final int ironDepositMinAltitude = 0;
    public static final int ironDepositMaxAltitude = 64;

    public static final int goldDepositRarity = 100;
    public static final int goldDepositFrequency = 2;
    public static final int goldDepositSize = 8;
    public static final int goldDepositMinAltitude = 0;
    public static final int goldDepositMaxAltitude = 32;

    public static final int redstoneDepositRarity = 100;
    public static final int redstoneDepositFrequency = 8;
    public static final int redstoneDepositSize = 7;
    public static final int redstoneDepositMinAltitude = 0;
    public static final int redstoneDepositMaxAltitude = 16;

    public static final int diamondDepositRarity = 100;
    public static final int diamondDepositFrequency = 1;
    public static final int diamondDepositSize = 7;
    public static final int diamondDepositMinAltitude = 0;
    public static final int diamondDepositMaxAltitude = 16;

    public static final int lapislazuliDepositRarity = 100;
    public static final int lapislazuliDepositFrequency = 1;
    public static final int lapislazuliDepositSize = 7;
    public static final int lapislazuliDepositMinAltitude = 0;
    public static final int lapislazuliDepositMaxAltitude = 16;

    public static final int emeraldDepositRarity = 100;
    public static final int emeraldDepositFrequency = 1;
    public static final int emeraldDepositSize = 5;
    public static final int emeraldDepositMinAltitude = 4;
    public static final int emeraldDepositMaxAltitude = 32;

    public static final int waterClayDepositRarity = 100;
    public static final int waterClayDepositSize = 4;

    public static final int waterSandDepositRarity = 100;
    public static final int waterSandDepositFrequency = 4;
    public static final int waterSandDepositSize = 7;

    public static final int roseDepositRarity = 100;
    public static final int roseDepositMinAltitude = 0;
    public static final int roseDepositMaxAltitude = 128;

    public static final int blueOrchidDepositRarity = 100;
    public static final int blueOrchidDepositMinAltitude = 0;

    public static final int flowerDepositRarity = 100;
    public static final int flowerDepositMinAltitude = 0;
    public static final int flowerDepositMaxAltitude = 128;

    public static final int tulipDepositRarity = 25;

    public static final int redMushroomDepositRarity = 50;
    public static final int redMushroomDepositMinAltitude = 0;
    public static final int redMushroomDepositMaxAltitude = 128;

    public static final int brownMushroomDepositRarity = 50;
    public static final int brownMushroomDepositMinAltitude = 0;
    public static final int brownMushroomDepositMaxAltitude = 128;

    public static final int longGrassDepositRarity = 100;
    public static final int longGrassGroupedDepositRarity = 60;

    public static final int doubleGrassDepositRarity = 100;
    public static final int doubleGrassGroupedDepositRarity = 15;

    public static final int deadBushDepositRarity = 100;

    public static final int pumpkinDepositRarity = 3;
    public static final int pumpkinDepositFrequency = 1;
    public static final int pumpkinDepositMinAltitude = 0;
    public static final int pumpkinDepositMaxAltitude = 128;

    public static final int reedDepositRarity = 100;
    public static final int reedDepositMinAltitude = 0;
    public static final int reedDepositMaxAltitude = 128;

    public static final int cactusDepositRarity = 100;
    public static final int cactusDepositMinAltitude = 0;
    public static final int cactusDepositMaxAltitude = 128;

    public static final int vinesRarity = 100;
    public static final int vinesFrequency = 50;
    public static final int vinesMinAltitude = 64;

    public static final int waterSourceDepositRarity = 100;
    public static final int waterSourceDepositFrequency = 20;
    public static final int waterSourceDepositMinAltitude = 8;
    public static final int waterSourceDepositMaxAltitude = 128;

    public static final int lavaSourceDepositRarity = 100;
    public static final int lavaSourceDepositFrequency = 10;
    public static final int lavaSourceDepositMinAltitude = 8;
    public static final int lavaSourceDepositMaxAltitude = 128;

    public static final int boulderDepositRarity = 30;
    public static final int boulderDepositMinAltitude = 0;
    public static final int boulderDepositMaxAltitude = 256;

    public static final int iceSpikeDepositMinHeight = 60;
    public static final int iceSpikeDepositMaxHeight = 128;
    // End resource settings

}