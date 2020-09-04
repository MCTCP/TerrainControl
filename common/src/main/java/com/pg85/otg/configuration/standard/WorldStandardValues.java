package com.pg85.otg.configuration.standard;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.biome.settings.ReplaceBlocks;
import com.pg85.otg.configuration.settingType.MaterialListSetting;
import com.pg85.otg.configuration.settingType.MaterialSetting;
import com.pg85.otg.configuration.settingType.Setting;
import com.pg85.otg.configuration.settingType.Settings;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.configuration.world.WorldConfig.ImageMode;
import com.pg85.otg.configuration.world.WorldConfig.ImageOrientation;
import com.pg85.otg.configuration.world.WorldConfig.TerrainMode;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import java.util.ArrayList;
import java.util.List;

public class WorldStandardValues extends Settings
{
    // Files and folders
    public static final String WORLD_CONFIG_FILE_NAME = "WorldConfig.ini";
    public static final String FALLBACK_FILE_NAME = "Fallbacks.ini";
    public static final String WORLD_BIOMES_DIRECTORY_NAME = "WorldBiomes";
    public static final String WORLD_OBJECTS_DIRECTORY_NAME = "WorldObjects";

	public static String DimensionsConfigFileName = "Config.yaml";
	public static String DimensionsConfigBackupFileName = "Config-backup.yaml";
	public static String BiomeIdDataFileName = "BiomeIds.txt";
	public static String BiomeIdDataBackupFileName = "BiomeIds-backup.txt";
	public static String DimensionsDataFileName = "Dimensions.txt";
	public static String DimensionsDataBackupFileName = "Dimensions-backup.txt";
	public static String StructureDataFileName = "StructureData.txt";
	public static String StructureDataBackupFileName = "StructureData-backup.txt";
	public static String NullChunksFileName = "NullChunks.txt";
	public static String NullChunksBackupFileName = "NullChunks-backup.txt";
	public static String SpawnedStructuresFileName = "SpawnedStructures.txt";
	public static String SpawnedStructuresBackupFileName = "SpawnedStructures-backup.txt";
	public static String ChunkProviderPopulatedChunksFileName = "ChunkProviderPopulatedChunks.txt";
	public static String PregeneratedChunksFileName = "PregeneratedChunks.txt";
	public static String PregeneratedChunksBackupFileName = "PregeneratedChunks-backup.txt";
	public static String WorldSaveDataFileName = "WorldSave.txt";
	public static String WorldSaveDataBackupFileName = "WorldSave-backup.txt";
    
    /**
     * Temperatures below this temperature will cause the biome to be covered
     * by snow.
     */
	// OTG biome temperature is between 0.0 and 2.0, snow should appear below 0.15.
	// According to the configs, snow and ice should appear between 0.2 (at y > 90) and 0.1 (entire biome covered in ice).
	// Make sure that at 0.2, snow layers start with thickness 0 at y 90 and thickness 7 around y255.
	// In a 0.2 temp biome, y90 temp is 0.156, y255 temp is -0.12
    public static final float SNOW_AND_ICE_TEMP = 0.15F;
    public static final float SNOW_AND_ICE_MAX_TEMP = -0.115f;
    public static final float ICE_GROUP_MAX_TEMP = 0.33F;

    public static class BiomeGroupNames
    {
        public static final String NORMAL = "NormalBiomes";
        public static final String ICE = "IceBiomes";
        public static final String COLD = "ColdBiomes";
        public static final String HOT = "HotBiomes";
        public static final String MESA = "MesaBiomes";
        public static final String JUNGLE = "JungleBiomes";
        public static final String MEGA_TAIGA = "Mega TaigaBiomes";
    }

    public static final Setting<ConfigMode> SETTINGS_MODE = enumSetting("SettingsMode", ConfigMode.WriteAll);
    public static final Setting<ConfigMode> SETTINGS_MODE_BO3 = enumSetting("SettingsMode", ConfigMode.WriteDisable);
    public static final Setting<TerrainMode> TERRAIN_MODE = enumSetting("TerrainMode", TerrainMode.Normal);
    public static final Setting<ImageMode> IMAGE_MODE = enumSetting("ImageMode", ImageMode.Mirror);
    public static final Setting<ImageOrientation> IMAGE_ORIENTATION = enumSetting("ImageOrientation", ImageOrientation.West);

    public static final Setting<String>
        BIOME_MODE = stringSetting("BiomeMode", "Normal"),
        IMAGE_FILE = stringSetting("ImageFile", "map.png"),
        IMAGE_FILL_BIOME = stringSetting("ImageFillBiome", "Ocean"),
        AUTHOR = stringSetting("Author", "Unknown"),
        DESCRIPTION = stringSetting("Description", "No description given"),
		WORLDPACKER_MODNAME = stringSetting("WorldPackerModName", ""),
		WORLD_SEED = stringSetting("WorldSeed", ""),
		BO3_AT_SPAWN = stringSetting("BO3AtSpawn", ""),
		DIMENSIONBELOW = stringSetting("DimensionBelow", ""),
		DIMENSIONABOVE = stringSetting("DimensionAbove", ""),

		WelcomeMessage = stringSetting("WelcomeMessage", ""),
		DepartMessage = stringSetting("DepartMessage", ""),
		ITEMS_TO_ADD_ON_JOIN_DIMENSION = stringSetting("ItemsToAddOnJoinDimension", ""),
		ITEMS_TO_REMOVE_ON_JOIN_DIMENSION = stringSetting("ItemsToRemoveOnJoinDimension", ""),
		ITEMS_TO_ADD_ON_LEAVE_DIMENSION = stringSetting("ItemsToAddOnLeaveDimension", ""),
		ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION = stringSetting("ItemsToRemoveOnLeaveDimension", ""),
		ITEMS_TO_ADD_ON_RESPAWN = stringSetting("ItemsToAddOnRespawn", ""),
		
    	DEFAULT_OCEAN_BIOME = stringSetting("DefaultOceanBiome", "Ocean"),
    	DEFAULT_FROZEN_OCEAN_BIOME = stringSetting("DefaultFrozenOceanBiome", "FrozenOcean"),
    	
    	PORTAL_COLOR = stringSetting("PortalColor", "default"),
    	PORTAL_PARTICLE_TYPE = stringSetting("PortalParticleType", "portal"),
    	PORTAL_MOB_TYPE = stringSetting("PortalMobType", "zombie_pigman");
	;

    public static final Setting<Integer>
        WORLD_HEIGHT_SCALE_BITS = intSetting("WorldHeightScaleBits", 7, 5, 8),
        WORLD_HEIGHT_CAP_BITS = intSetting("WorldHeightCapBits", 8, 5, 8),
        GENERATION_DEPTH = intSetting("GenerationDepth", 10, 1, 20),
        BIOME_RARITY_SCALE = intSetting("BiomeRarityScale", 100, 1, Integer.MAX_VALUE),
        LAND_RARITY = intSetting("LandRarity", 99, 1, 100),
        LAND_SIZE = intSetting("LandSize", 0, 0, 20),
        LAND_FUZZY = intSetting("LandFuzzy", 5, 0, 20),
        ICE_RARITY = intSetting("IceRarity", 90, 1, 100),
        ICE_SIZE = intSetting("IceSize", 3, 0, 20),
        RIVER_RARITY = intSetting("RiverRarity", 4, 0, 20),
        RIVER_SIZE = intSetting("RiverSize", 0, 0, 20),
        WATER_LEVEL_MAX = intSetting("WaterLevelMax", 63, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT - 1),
        WATER_LEVEL_MIN = intSetting("WaterLevelMin", 0, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT - 1),
        IMAGE_X_OFFSET = intSetting("ImageXOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
        IMAGE_Z_OFFSET = intSetting("ImageZOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
        CAVE_RARITY = intSetting("CaveRarity", 7, 0, 100),
        CAVE_FREQUENCY = intSetting("CaveFrequency", 40, 0, 200),
        CAVE_MIN_ALTITUDE = intSetting("CaveMinAltitude", 8, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT - 1),
        CAVE_MAX_ALTITUDE = intSetting("CaveMaxAltitude", 127, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT - 1),
        INDIVIDUAL_CAVE_RARITY = intSetting("IndividualCaveRarity", 25, 0, 100),
        CAVE_SYSTEM_FREQUENCY = intSetting("CaveSystemFrequency", 1, 0, 200),
        CAVE_SYSTEM_POCKET_CHANCE = intSetting("CaveSystemPocketChance", 0, 0, 100),
        CAVE_SYSTEM_POCKET_MIN_SIZE = intSetting("CaveSystemPocketMinSize", 0, 0, 100),
        CAVE_SYSTEM_POCKET_MAX_SIZE = intSetting("CaveSystemPocketMaxSize", 3, 0, 100),
        RAVINE_RARITY = intSetting("RavineRarity", 2, 0, 100),
        RAVINE_MIN_ALTITUDE = intSetting("RavineMinAltitude", 20, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT - 1),
        RAVINE_MAX_ALTITUDE = intSetting("RavineMaxAltitude", 67, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT - 1),
        RAVINE_MIN_LENGTH = intSetting("RavineMinLength", 84, 1, 500),
        RAVINE_MAX_LENGTH = intSetting("RavineMaxLength", 111, 1, 500),
        OBJECT_SPAWN_RATIO = intSetting("ObjectSpawnRatio", 1, 1, 1000),
        STRONGHOLD_COUNT = intSetting("StrongholdCount", 128, 0, 1000),
        STRONGHOLD_SPREAD = intSetting("StrongholdSpread", 3, 1, 1000),
        VILLAGE_DISTANCE = intSetting("VillageDistance", 32, 9, 10000),
        VILLAGE_SIZE = intSetting("VillageSize", 0, 0, 10),
        MINIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS = intSetting("MinimumDistanceBetweenRareBuildings", 9, 1, 10000),
        MAXIMUM_DISTANCE_BETWEEN_RARE_BUILDINGS = intSetting("MaximumDistanceBetweenRareBuildings", 32, 1, 10000),
        OCEAN_MONUMENT_GRID_SIZE = intSetting("OceanMonumentGridSize", 32, 5, 10000),
        OCEAN_MONUMENT_RANDOM_OFFSET = intSetting("OceanMonumentRandomOffset", 26, 0, 10000),
        MAXIMUM_CUSTOM_STRUCTURE_RADIUS = intSetting("MaximumCustomStructureRadius", 5, 1, 100),
        PREGENERATION_RADIUS = intSetting("PreGenerationRadius", 0, 0, 999999),
        WORLD_BORDER_RADIUS = intSetting("WorldBorderRadius", 0, 0, 999999),

		MaxCommandChainLength = intSetting("MaxCommandChainLength", 65536, 0, 999999), 
		MaxEntityCramming = intSetting("MaxEntityCramming", 24, 0, 999999),
		RandomTickSpeed = intSetting("RandomTickSpeed", 3, 0, 999999),
		SpawnRadius = intSetting("SpawnRadius", 10, 0, 999999),
		CloudHeight = intSetting("CloudHeight", 128, 0, 999999),

		DIMENSIONBELOWHEIGHT = intSetting("DimensionBelowHeight", 0, -999999, 999999),
		DIMENSIONABOVEHEIGHT = intSetting("DimensionAboveHeight", 256, -999999, 999999),

		RESPAWN_DIMENSION = intSetting("RespawnDimension", 0, -999999, 999999),
		MOVEMENT_FACTOR = intSetting("MovementFactor", 1, 1, 999999),

		SPAWN_POINT_X = intSetting("SpawnPointX", 0, -999999, 999999),
		SPAWN_POINT_Y = intSetting("SpawnPointY", 0, -999999, 999999),
		SPAWN_POINT_Z = intSetting("SpawnPointZ", 0, -999999, 999999),
		
		PORTAL_MOB_SPAWN_CHANCE = intSetting("PortalMobSpawnChance", 2000, 0, 999999);
    ;
    
    public static final Setting<Boolean>
        RIVERS_ENABLED = booleanSetting("RiversEnabled", true),
        GROUP_FREEZE_ENABLED = booleanSetting("FreezeAllBiomesInColdGroup", false),
        RANDOM_RIVERS = booleanSetting("RandomRivers", false),
        IMPROVED_RIVERS = booleanSetting("ImprovedRivers", false),
        FROZEN_OCEAN = booleanSetting("FrozenOcean", true),
        BETTER_SNOW_FALL = booleanSetting("BetterSnowFall", false),
        FULLY_FREEZE_LAKES = booleanSetting("FullyFreezeLakes", false),
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
        RARE_BUILDINGS_ENABLED = booleanSetting("RareBuildingsEnabled", true),
        OCEAN_MONUMENTS_ENABLED = booleanSetting("OceanMonumentsEnabled", true),
		WOODLAND_MANSIONS_ENABLED = booleanSetting("WoodLandMansionsEnabled", true),
        POPULATE_USING_SAVED_BIOMES = booleanSetting("PopulateUsingSavedBiomes", false),
        DISABLE_OREGEN = booleanSetting("DisableOreGen", false),
		IMPROVED_BIOME_BORDERS = booleanSetting("ImprovedBiomeBorders", false),
		IMPROVED_BIOME_GROUPS = booleanSetting("ImprovedBiomeGroups", false),
		CUSTOM_HEIGHT_CONTROL_SMOOTHING = booleanSetting("CustomHeightControlSmoothing", false),
		IMPROVED_SMOOTHING = booleanSetting("ImprovedSmoothing", false),

		TeleportToSpawnOnly = booleanSetting("TeleportToSpawnOnly", false),
		CommandBlockOutput = booleanSetting("CommandBlockOutput", true),
		DisableElytraMovementCheck = booleanSetting("DisableElytraMovementCheck", false),
		DoDaylightCycle = booleanSetting("DoDaylightCycle", true),
		DoEntityDrops = booleanSetting("DoEntityDrops", true),
		DoFireTick = booleanSetting("DoFireTick", true),
		DoLimitedCrafting = booleanSetting("DoLimitedCrafting", false),
		DoMobLoot = booleanSetting("DoMobLoot", true),
		DoMobSpawning = booleanSetting("DoMobSpawning", true),
		DoTileDrops = booleanSetting("DoTileDrops", true),
		DoWeatherCycle = booleanSetting("DoWeatherCycle", true),
		GameLoopFunction = booleanSetting("GameLoopFunction", true),
		KeepInventory = booleanSetting("KeepInventory", false),
		LogAdminCommands = booleanSetting("LogAdminCommands", true),				
		MobGriefing = booleanSetting("MobGriefing", true),
		NaturalRegeneration = booleanSetting("NaturalRegeneration", true),
		ReducedDebugInfo = booleanSetting("ReducedDebugInfo", false),
		SendCommandFeedback = booleanSetting("SendCommandFeedback", true),
		ShowDeathMessages = booleanSetting("ShowDeathMessages", true),
		SpectatorsGenerateChunks = booleanSetting("SpectatorsGenerateChunks", true),
		HasSkyLight = booleanSetting("HasSkyLight", true),
		IsSurfaceWorld = booleanSetting("IsSurfaceWorld", true),
		CanRespawnHere = booleanSetting("CanRespawnHere", true),
		DoesWaterVaporize = booleanSetting("DoesWaterVaporize", false),
		DoesXZShowFog = booleanSetting("DoesXZShowFog", false),
		IsSkyColored = booleanSetting("IsSkyColored", true),
		CanDoLightning = booleanSetting("CanDoLightning", true),
		CanDoRainSnowIce = booleanSetting("CanDoRainSnowIce", true),
		IsNightWorld = booleanSetting("IsNightWorld", false),
		ShouldMapSpin = booleanSetting("ShouldMapSpin", false),
		CanDropChunk = booleanSetting("CanDropChunk", true),
		UseCustomFogColor = booleanSetting("UseCustomFogColor", false),
		IS_OTG_PLUS = booleanSetting("IsOTGPlus", false),

        SPAWN_POINT_SET = booleanSetting("SpawnPointSet", false),
        PLAYERS_CAN_BREAK_BLOCKS = booleanSetting("PlayersCanBreakBlocks", true),
        EXPLOSIONS_CAN_BREAK_BLOCKS = booleanSetting("ExplosionsCanBreakBlocks", true),
        PLAYERS_CAN_PLACE_BLOCKS = booleanSetting("PlayersCanPlaceBlocks", true)
    ;

    public static final Setting<LocalMaterialData>
    	WATER_BLOCK = new MaterialSetting("WaterBlock", DefaultMaterial.STATIONARY_WATER),
        ICE_BLOCK = new MaterialSetting("IceBlock", DefaultMaterial.ICE),
        COOLED_LAVA_BLOCK = new MaterialSetting("CooledLavaBlock", DefaultMaterial.STATIONARY_LAVA),
        BEDROCK_BLOCK = new MaterialSetting("BedrockobBlock", DefaultMaterial.BEDROCK);

    public static final Setting<ArrayList<LocalMaterialData>>
    	DIMENSION_PORTAL_MATERIALS = new MaterialListSetting("DimensionPortalMaterials", new DefaultMaterial[] { DefaultMaterial.QUARTZ_BLOCK });

    public static final Setting<List<ReplaceBlocks>>
		REPLACE_BLOCKS_LIST = replaceBlocksListSetting("ReplaceBlocksList");

    public static final Setting<List<String>>
        ISLE_BIOMES = stringListSetting("IsleBiomes", "Deep Ocean", "MushroomIsland",
                "Ice Mountains", "DesertHills", "ForestHills", "Forest", "TaigaHills",
                "JungleHills", "Cold Taiga Hills", "Birch Forest Hills", "Extreme Hills+",
                "Mesa Plateau", "Mesa Plateau F", "Mesa Plateau M", "Mesa Plateau F M",
                "Mesa (Bryce)", "Mega Taiga Hills", "Mega Spruce Taiga Hills"),
        BORDER_BIOMES = stringListSetting("BorderBiomes",
                "JungleEdge", "JungleEdge M", "MushroomIslandShore", "Beach", "Extreme Hills Edge", "Desert", "Taiga"),
    	CUSTOM_BIOMES = stringListSetting("CustomBiomes"),
		DIMENSIONS = stringListSetting("Dimensions")
	;

    public static final Setting<Double>
        FROZEN_OCEAN_TEMPERATURE = doubleSetting("OceanFreezingTemperature", 0.15, 0, 2),
        RAVINE_DEPTH = doubleSetting("RavineDepth", 3, 0.1, 15),
        CANYON_DEPTH = doubleSetting("CanyonDepth", 3, 0.1, 15),
        FRACTURE_HORIZONTAL = doubleSetting("FractureHorizontal", 0, -500, 500),
        FRACTURE_VERTICAL = doubleSetting("FractureVertical", 0, -500, 500),
        STRONGHOLD_DISTANCE = doubleSetting("StrongholdDistance", 32, 1, 1000),

		FogColorRed = doubleSetting("FogColorRed", 0.20000000298023224D, Double.MIN_VALUE, Double.MAX_VALUE),
		FogColorGreen = doubleSetting("FogColorGreen", 0.029999999329447746D, Double.MIN_VALUE, Double.MAX_VALUE),
		FogColorBlue = doubleSetting("FogColorBlue", 0.029999999329447746D, Double.MIN_VALUE, Double.MAX_VALUE),
		VoidFogYFactor = doubleSetting("VoidFogYFactor", 0.03125D, Double.MIN_VALUE, Double.MAX_VALUE),

		GravityFactor = doubleSetting("GravityFactor", 0.08D, 0.0D, Double.MAX_VALUE)
    ;

    public static final Setting<Integer>
	    WORLD_FOG = colorSetting("WorldFog", "0xC0D8FF"),
	    WORLD_NIGHT_FOG = colorSetting("WorldNightFog", "0x0B0D17");

    public static final Setting<Long> RESOURCES_SEED = longSetting("ResourcesSeed", 0, Long.MIN_VALUE, Long.MAX_VALUE);

    // Deprecated settings
    public static final Setting<Boolean> FROZEN_RIVERS = booleanSetting("FrozenRivers", true);
    public static final Setting<List<String>> NORMAL_BIOMES = stringListSetting("NormalBiomes", "Desert", "Forest", "Extreme Hills",
            "Swampland", "Plains", "Taiga", "Jungle", "River");
    public static final Setting<List<String>> ICE_BIOMES = stringListSetting("IceBiomes", "Ice Plains");

}
