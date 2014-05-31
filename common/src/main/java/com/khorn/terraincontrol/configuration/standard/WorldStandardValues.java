package com.khorn.terraincontrol.configuration.standard;

import static com.khorn.terraincontrol.TerrainControl.WORLD_DEPTH;
import static com.khorn.terraincontrol.TerrainControl.WORLD_HEIGHT;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.WorldConfig.ImageMode;
import com.khorn.terraincontrol.configuration.WorldConfig.ImageOrientation;
import com.khorn.terraincontrol.configuration.WorldConfig.TerrainMode;
import com.khorn.terraincontrol.configuration.settingType.MaterialSetting;
import com.khorn.terraincontrol.configuration.settingType.Setting;
import com.khorn.terraincontrol.configuration.settingType.Settings;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.List;

public class WorldStandardValues extends Settings
{
    // Files and folders
    public static final String WORLD_CONFIG_FILE_NAME = "WorldConfig.ini";
    public static final String WORLD_BIOMES_DIRECTORY_NAME = "WorldBiomes";
    public static final String WORLD_OBJECTS_DIRECTORY_NAME = "WorldObjects";

    /**
     * Temperatures below this temperature will cause the biome to be covered
     * by snow.
     */
    public static final float SNOW_AND_ICE_MAX_TEMP = 0.15F;

    public static final Setting<ConfigMode> SETTINGS_MODE = enumSetting("SettingsMode", ConfigMode.WriteAll);
    public static final Setting<TerrainMode> TERRAIN_MODE = enumSetting("TerrainMode", TerrainMode.Normal);
    public static final Setting<ImageMode> IMAGE_MODE = enumSetting("ImageMode", ImageMode.Mirror);
    public static final Setting<ImageOrientation> IMAGE_ORIENTATION = enumSetting("ImageOrientation", ImageOrientation.West);

    public static final Setting<String>
            BIOME_MODE = stringSetting("BiomeMode", "Normal"),
            IMAGE_FILE = stringSetting("ImageFile", "map.png"),
            IMAGE_FILL_BIOME = stringSetting("ImageFillBiome", "Ocean");

    public static final Setting<Integer>
            WORLD_HEIGHT_SCALE_BITS = intSetting("WorldHeightScaleBits", 7, 5, 8),
            WORLD_HEIGHT_CAP_BITS = intSetting("WorldHeightCapBits", 8, 5, 8),
            GENERATION_DEPTH = intSetting("GenerationDepth", 10, 1, 20),
            BIOME_RARITY_SCALE = intSetting("BiomeRarityScale", 100, 1, Integer.MAX_VALUE),
            LAND_RARITY = intSetting("LandRarity", 97, 1, 100),
            LAND_SIZE = intSetting("LandSize", 0, 0, 20),
            LAND_FUZZY = intSetting("LandFuzzy", 6, 0, 20),
            ICE_RARITY = intSetting("IceRarity", 90, 1, 100),
            ICE_SIZE = intSetting("IceSize", 3, 0, 20),
            RIVER_RARITY = intSetting("RiverRarity", 4, 0, 20),
            RIVER_SIZE = intSetting("RiverSize", 0, 0, 20),
            WATER_LEVEL_MAX = intSetting("WaterLevelMax", 63, WORLD_DEPTH, WORLD_HEIGHT),
            WATER_LEVEL_MIN = intSetting("WaterLevelMin", 0, WORLD_DEPTH, WORLD_HEIGHT),
            IMAGE_X_OFFSET = intSetting("ImageXOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
            IMAGE_Z_OFFSET = intSetting("ImageZOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
            CAVE_RARITY = intSetting("CaveRarity", 7, 0, 100),
            CAVE_FREQUENCY = intSetting("CaveFrequency", 40, 0, 200),
            CAVE_MIN_ALTITUDE = intSetting("CaveMinAltitude", 8, WORLD_DEPTH, WORLD_HEIGHT),
            CAVE_MAX_ALTITUDE = intSetting("CaveMaxAltitude", 128, WORLD_DEPTH, WORLD_HEIGHT),
            INDIVIDUAL_CAVE_RARITY = intSetting("IndividualCaveRarity", 25, 0, 100),
            CAVE_SYSTEM_FREQUENCY = intSetting("CaveSystemFrequency", 1, 0, 200),
            CAVE_SYSTEM_POCKET_CHANCE = intSetting("CaveSystemPocketChance", 0, 0, 100),
            CAVE_SYSTEM_POCKET_MIN_SIZE = intSetting("CaveSystemPocketMinSize", 0, 0, 100),
            CAVE_SYSTEM_POCKET_MAX_SIZE = intSetting("CaveSystemPocketMaxSize", 4, 0, 100),
            CANYON_RARITY = intSetting("CanyonRarity", 2, 0, 100),
            CANYON_MIN_ALTITUDE = intSetting("CanyonMinAltitude", 20, WORLD_DEPTH, WORLD_HEIGHT),
            CANYON_MAX_ALTITUDE = intSetting("CanyonMaxAltitude", 68, WORLD_DEPTH, WORLD_HEIGHT),
            CANYON_MIN_LENGTH = intSetting("CanyonMinLength", 84, 1, 500),
            CANYON_MAX_LENGTH = intSetting("CanyonMaxLength", 112, 1, 500),
            OBJECT_SPAWN_RATIO = intSetting("ObjectSpawnRatio", 1, 1, 1000),
            STRONGHOLD_COUNT = intSetting("StrongholdCount", 3, 0, 1000),
            STRONGHOLD_SPREAD = intSetting("StrongholdSpread", 3, 1, 1000),
            VILLAGE_DISTANCE = intSetting("VillageDistance", 32, 9, 10000),
            VILLAGE_SIZE = intSetting("VillageSize", 0, 0, 10),
            MINIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS = intSetting("MinimumDistanceBetweenRareBuildings", 9, 1, 10000),
            MAXIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS = intSetting("MaximumDistanceBetweenRareBuildings", 32, 1, 10000);

    public static final Setting<Boolean>
            RIVERS_ENABLED = booleanSetting("RiversEnabled", true),
            RANDOM_RIVERS = booleanSetting("RandomRivers", false),
            IMPROVED_RIVERS = booleanSetting("ImprovedRivers", false),
            FROZEN_OCEAN = booleanSetting("FrozenOcean", true),
            EVEN_CAVE_DISTRIBUTION = booleanSetting("EvenCaveDistrubution", false),
            DISABLE_BEDROCK = booleanSetting("DisableBedrock", false),
            CEILING_BEDROCK = booleanSetting("CeilingBedrock", false),
            FLAT_BEDROCK = booleanSetting("FlatBedrock", false),
            REMOVE_SURFACE_STONE = booleanSetting("RemoveSurfaceStone", false),
            POPULATION_BOUNDS_CHECK = booleanSetting("PopulationBoundsCheck", true),
            NETHER_FORTRESSES_ENABLED = booleanSetting("NetherFortressesEnabled", false),
            STRONGHOLDS_ENABLED = booleanSetting("StrongholdsEnabled", true),
            VILLAGES_ENABLED = booleanSetting("VillagesEnabled", true),
            MINESHAFTS_ENABLED = booleanSetting("MineshaftsEnabled", true),
            RARE_BUILDINGS_ENABLED = booleanSetting("RareBuildingsEnabled", true);

    public static final Setting<LocalMaterialData>
            WATER_BLOCK = new MaterialSetting("WaterBlock", DefaultMaterial.STATIONARY_WATER),
            ICE_BLOCK = new MaterialSetting("IceBlock", DefaultMaterial.ICE),
            BEDROCK_BLOCK = new MaterialSetting("BedrockobBlock", DefaultMaterial.BEDROCK);

    public static final Setting<List<String>>
            NORMAL_BIOMES = stringListSetting("NormalBiomes", "Desert", "Forest",
                    "Extreme Hills", "Swampland", "Plains", "Taiga", "Jungle"),
            ICE_BIOMES = stringListSetting("IceBiomes", "Ice Plains"),
            ISLE_BIOMES = stringListSetting("IsleBiomes", "MushroomIsland",
                    "Ice Mountains", "DesertHills", "ForestHills", "TaigaHills",
                    "River", "JungleHills"),
            BORDER_BIOMES = stringListSetting("BorderBiomes",
                    "MushroomIslandShore", "Beach", "Extreme Hills Edge"),
            CUSTOM_BIOMES = stringListSetting("CustomBiomes");

    public static final Setting<Double>
            OLD_BIOME_SIZE = doubleSetting("OldBiomeSize", 1.5, 0.1, 10),
            MIN_MOISTURE = doubleSetting("MinMoisture", 0, 0, 1),
            MAX_MOISTURE = doubleSetting("MaxMoisture", 0, 0, 1),
            MIN_TEMPERATURE = doubleSetting("MinTemperature", 0, 0, 1),
            MAX_TEMPERATURE = doubleSetting("MaxTemperature", 0, 0, 1),
            CANYON_DEPTH = doubleSetting("CanyonDepth", 3, 0.1, 15),
            FRACTURE_HORIZONTAL = doubleSetting("FractureHorizontal", 0, -500, 500),
            FRACTURE_VERTICAL = doubleSetting("FractureVertical", 0, -500, 500),
            STRONGHOLD_DISTANCE = doubleSetting("StrongholdDistance", 32, 1, 1000);

    public static final Setting<Integer>
            WORLD_FOG = colorSetting("WorldFog", "0xC0D8FF"),
            WORLD_NIGHT_FOG = colorSetting("WorldNightFog", "0x0B0D17");

    public static final Setting<Long> RESOURCES_SEED = longSetting("ResourcesSeed", 0, Long.MIN_VALUE, Long.MAX_VALUE);

    // Deprecated settings
    public static final Setting<Boolean> FROZEN_RIVERS = booleanSetting("FrozenRivers", true);
    public static final Setting<Integer> CUSTOM_TREE_CHANCE = intSetting("CustomTreeChance", 0, 0, 100);

}