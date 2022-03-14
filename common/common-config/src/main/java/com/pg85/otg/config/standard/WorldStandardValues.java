package com.pg85.otg.config.standard;

import com.pg85.otg.config.settingType.MaterialListSetting;
import com.pg85.otg.config.settingType.MaterialSetting;
import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.settingType.Settings;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.constants.SettingsEnums.ImageMode;
import com.pg85.otg.constants.SettingsEnums.ImageOrientation;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.util.ArrayList;
import java.util.List;

public class WorldStandardValues extends Settings
{
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
	public static final Setting<BiomeMode> BIOME_MODE = enumSetting("BiomeMode", BiomeMode.Normal);
	public static final Setting<ImageMode> IMAGE_MODE = enumSetting("ImageMode", ImageMode.Mirror);
	public static final Setting<ImageOrientation> IMAGE_ORIENTATION = enumSetting("ImageOrientation", ImageOrientation.West);
	public static final Setting<CustomStructureType> CUSTOM_STRUCTURE_TYPE = enumSetting("CustomStructureType", CustomStructureType.BO3);
	
	public static final Setting<String>
		AUTHOR = stringSetting("Author", "Unknown"),
		SHORT_PRESET_NAME = stringSetting("ShortPresetName", ""),
		DESCRIPTION = stringSetting("Description", "No description given"),
		IMAGE_FILE = stringSetting("ImageFile", "map.png"),
		IMAGE_FILL_BIOME = stringSetting("ImageFillBiome", "Ocean"),
		BO3_AT_SPAWN = stringSetting("BO3AtSpawn", ""),
		DEFAULT_OCEAN_BIOME = stringSetting("DefaultOceanBiome", "Ocean"),
		DEFAULT_FROZEN_OCEAN_BIOME = stringSetting("DefaultFrozenOceanBiome", "Ocean"),
		DEFAULT_WARM_OCEAN_BIOME = stringSetting("DefaultWarmOceanBiome", "Ocean"),
		DEFAULT_LUKEWARM_OCEAN_BIOME = stringSetting("DefaultLukewarmOceanBiome", "Ocean"),
		DEFAULT_COLD_OCEAN_BIOME = stringSetting("DefaultColdOceanBiome", "Ocean"),
		INFINIBURN = stringSetting("InfiniBurn", "minecraft:infiniburn_overworld"),
		EFFECTS_LOCATION = stringSetting("EffectsLocation", "minecraft:overworld"),
		PORTAL_COLOR = stringSetting("PortalColor", "Default"),
		PORTAL_IGNITION_SOURCE = stringSetting("PortalIgnitionSource", "minecraft:flint_and_steel"),
		PORTAL_MOB = stringSetting("PortalMob", "minecraft:zombified_piglin")
	;

	public static final Setting<Integer>
		MAJOR_VERSION = intSetting("MajorVersion", 0 , 0, Integer.MAX_VALUE),
		MINOR_VERSION = intSetting("MinorVersion", 0 , 0, Integer.MAX_VALUE),
		WORLD_HEIGHT_SCALE_BITS = intSetting("WorldHeightScaleBits", 7, 5, 8),
		WORLD_HEIGHT_CAP_BITS = intSetting("WorldHeightCapBits", 8, 5, 8),
		GENERATION_DEPTH = intSetting("GenerationDepth", 10, 1, 20),
		BIOME_RARITY_SCALE = intSetting("BiomeRarityScale", 100, 1, Integer.MAX_VALUE),
		LAND_RARITY = intSetting("LandRarity", 99, 0, 100),
		LAND_SIZE = intSetting("LandSize", 0, 0, 20),
		OCEAN_BIOME_SIZE = intSetting("OceanBiomeSize", 6, 0, 20),
		LAND_FUZZY = intSetting("LandFuzzy", 5, 0, 20),
		ICE_RARITY = intSetting("IceRarity", 90, 1, 100),
		ICE_SIZE = intSetting("IceSize", 3, 0, 20),
		RIVER_RARITY = intSetting("RiverRarity", 4, 0, 20),
		RIVER_SIZE = intSetting("RiverSize", 0, 0, 20),
		WATER_LEVEL_MAX = intSetting("WaterLevelMax", 63, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
		WATER_LEVEL_MIN = intSetting("WaterLevelMin", 0, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
		IMAGE_X_OFFSET = intSetting("ImageXOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
		IMAGE_Z_OFFSET = intSetting("ImageZOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
		CAVE_RARITY = intSetting("CaveRarity", 14, 0, 100),
		CAVE_FREQUENCY = intSetting("CaveFrequency", 15, 0, 200),
		CAVE_MIN_ALTITUDE = intSetting("CaveMinAltitude", 8, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
		CAVE_MAX_ALTITUDE = intSetting("CaveMaxAltitude", 128, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
		INDIVIDUAL_CAVE_RARITY = intSetting("IndividualCaveRarity", 25, 0, 100),
		CAVE_SYSTEM_FREQUENCY = intSetting("CaveSystemFrequency", 1, 0, 200),
		CAVE_SYSTEM_POCKET_CHANCE = intSetting("CaveSystemPocketChance", 0, 0, 100),
		CAVE_SYSTEM_POCKET_MIN_SIZE = intSetting("CaveSystemPocketMinSize", 0, 0, 100),
		CAVE_SYSTEM_POCKET_MAX_SIZE = intSetting("CaveSystemPocketMaxSize", 3, 0, 100),
		RAVINE_RARITY = intSetting("RavineRarity", 2, 0, 100),
		RAVINE_MIN_ALTITUDE = intSetting("RavineMinAltitude", 20, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
		RAVINE_MAX_ALTITUDE = intSetting("RavineMaxAltitude", 68, Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1),
		RAVINE_MIN_LENGTH = intSetting("RavineMinLength", 84, 1, 500),
		RAVINE_MAX_LENGTH = intSetting("RavineMaxLength", 112, 1, 500),
		MAXIMUM_CUSTOM_STRUCTURE_RADIUS = intSetting("MaximumCustomStructureRadius", 5, 1, 100),
		CARVER_LAVA_BLOCK_HEIGHT = intSetting("CarverLavaBlockHeight", 10, 0, 255),
		RANDOM_TICK_SPEED = intSetting("RandomTickSpeed", 3, 0, Integer.MAX_VALUE),
		SPAWN_RADIUS = intSetting("SpawnRadius", 10, 0, Integer.MAX_VALUE),
		MAX_ENTITY_CRAMMING = intSetting("MaxEntityCramming", 24, 0, Integer.MAX_VALUE),
		MAX_COMMAND_CHAIN_LENGTH = intSetting("MaxCommandChainLength", 65536, 0, Integer.MAX_VALUE),
		LOGICAL_HEIGHT = intSetting("LogicalHeight", 256, 0, Integer.MAX_VALUE),
		SPAWN_POINT_X = intSetting("SpawnPointX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
		SPAWN_POINT_Y = intSetting("SpawnPointY", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
		SPAWN_POINT_Z = intSetting("SpawnPointZ", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),		
		VILLAGE_SPACING = intSetting("VillageSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE),
		VILLAGE_SEPARATION = intSetting("VillageSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE),
		DESERTPYRAMID_SPACING = intSetting("DesertPyramidSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE),
		DESERTPYRAMID_SEPARATION = intSetting("DesertPyramidSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE),
		IGLOO_SPACING = intSetting("IglooSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE),
		IGLOO_SEPARATION = intSetting("IglooSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE),
		JUNGLETEMPLE_SPACING = intSetting("JungleTempleSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE),
		JUNGLETEMPLE_SEPARATION = intSetting("JungleTempleSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE),
		SWAMPHUT_SPACING = intSetting("SwampHutSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE),
		SWAMPHUT_SEPARATION = intSetting("SwampHutSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE),
		PILLAGEROUTPOST_SPACING = intSetting("PillagerOutpostSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE),
		PILLAGEROUTPOST_SEPARATION = intSetting("PillagerOutpostSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE),
		STRONGHOLD_SPACING = intSetting("StrongholdSpacing", 1, Integer.MIN_VALUE, Integer.MAX_VALUE),
		STRONGHOLD_SEPARATION = intSetting("StrongholdSeparation", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
		STRONGHOLD_DISTANCE = intSetting("StrongholdDistance", 32, Integer.MIN_VALUE, Integer.MAX_VALUE),
		STRONGHOLD_SPREAD = intSetting("StrongholdSpread", 3, Integer.MIN_VALUE, Integer.MAX_VALUE),
		STRONGHOLD_COUNT = intSetting("StrongholdCount", 128, Integer.MIN_VALUE, Integer.MAX_VALUE),
		OCEANMONUMENT_SPACING = intSetting("OceanMonumentSpacing", 32, Integer.MIN_VALUE, Integer.MAX_VALUE),
		OCEANMONUMENT_SEPARATION = intSetting("OceanMonumentSeparation", 5, Integer.MIN_VALUE, Integer.MAX_VALUE),
		ENDCITY_SPACING = intSetting("EndCitySpacing", 20, Integer.MIN_VALUE, Integer.MAX_VALUE),
		ENDCITY_SEPARATION = intSetting("EndCitySeparation", 11, Integer.MIN_VALUE, Integer.MAX_VALUE),
		WOODLANDMANSION_SPACING = intSetting("WoodlandMansionSpacing", 80, Integer.MIN_VALUE, Integer.MAX_VALUE),
		WOODLANDMANSION_SEPARATION = intSetting("WoodlandMansionSeparation", 20, Integer.MIN_VALUE, Integer.MAX_VALUE),
		BURIEDTREASURE_SPACING = intSetting("BuriedTreasureSpacing", 1, Integer.MIN_VALUE, Integer.MAX_VALUE),
		BURIEDTREASURE_SEPARATION = intSetting("BuriedTreasureSeparation", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
		MINESHAFT_SPACING = intSetting("MineshaftSpacing", 1, Integer.MIN_VALUE, Integer.MAX_VALUE),
		MINESHAFT_SEPARATION = intSetting("MineshaftSeparation", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
		RUINEDPORTAL_SPACING = intSetting("RuinedPortalSpacing", 40, Integer.MIN_VALUE, Integer.MAX_VALUE),
		RUINEDPORTAL_SEPARATION = intSetting("RuinedPortalSeparation", 15, Integer.MIN_VALUE, Integer.MAX_VALUE),
		SHIPWRECK_SPACING = intSetting("ShipwreckSpacing", 24, Integer.MIN_VALUE, Integer.MAX_VALUE),
		SHIPWRECK_SEPARATION = intSetting("ShipwreckSeparation", 4, Integer.MIN_VALUE, Integer.MAX_VALUE),
		OCEANRUIN_SPACING = intSetting("OceanRuinSpacing", 20, Integer.MIN_VALUE, Integer.MAX_VALUE),
		OCEANRUIN_SEPARATION = intSetting("OceanRuinSeparation", 8, Integer.MIN_VALUE, Integer.MAX_VALUE),
		BASTIONREMNANT_SPACING = intSetting("BastionRemnantSpacing", 27, Integer.MIN_VALUE, Integer.MAX_VALUE),
		BASTIONREMNANT_SEPARATION = intSetting("BastionRemnantSeparation", 4, Integer.MIN_VALUE, Integer.MAX_VALUE),
		NETHERFORTRESS_SPACING = intSetting("NetherFortressSpacing", 27, Integer.MIN_VALUE, Integer.MAX_VALUE),
		NETHERFORTRESS_SEPARATION = intSetting("NetherFortressSeparation", 4, Integer.MIN_VALUE, Integer.MAX_VALUE),
		NETHERFOSSIL_SPACING = intSetting("NetherFossilSpacing", 2, Integer.MIN_VALUE, Integer.MAX_VALUE),
		NETHERFOSSIL_SEPARATION = intSetting("NetherFossilSeparation", 1, Integer.MIN_VALUE, Integer.MAX_VALUE)
	;
	
	public static final Setting<Long>
		FIXED_TIME = longSetting("FixedTime", -1l, -1l, 24000)
	;
	
	public static final Setting<Boolean>
		FORCE_LAND_AT_SPAWN = booleanSetting("ForceLandAtSpawn", true),
		RIVERS_ENABLED = booleanSetting("RiversEnabled", true),
		RANDOM_RIVERS = booleanSetting("RandomRivers", false),
		FROZEN_OCEAN = booleanSetting("FrozenOcean", true),
		BETTER_SNOW_FALL = booleanSetting("BetterSnowFall", false),
		EVEN_CAVE_DISTRIBUTION = booleanSetting("EvenCaveDistribution", false),
		DISABLE_BEDROCK = booleanSetting("DisableBedrock", false),
		CEILING_BEDROCK = booleanSetting("CeilingBedrock", false),
		FLAT_BEDROCK = booleanSetting("FlatBedrock", false),
		REMOVE_SURFACE_STONE = booleanSetting("RemoveSurfaceStone", false),
		USE_OLD_BO3_STRUCTURE_RARITY = booleanSetting("UseOldBO3StructureRarity", true),				
		DECORATION_BOUNDS_CHECK = booleanSetting("DecorationBoundsCheck", true),
		DISABLE_OREGEN = booleanSetting("DisableOreGen", false),

		OLD_GROUP_RARITY = booleanSetting("OldGroupRarity", true), //TODO: for 1.16 1.0, switch this to false --Authvin
		OLD_LAND_RARITY = booleanSetting("OldLandRarity", true), //TODO: for 1.16 1.0, switch this to false --Authvin
		CAVES_ENABLED = booleanSetting("CavesEnabled", true),
		RAVINES_ENABLED = booleanSetting("RavinesEnabled", true),
		MINESHAFTS_ENABLED = booleanSetting("MineshaftsEnabled", true),
		OCEAN_MONUMENTS_ENABLED = booleanSetting("OceanMonumentsEnabled", true),
		RARE_BUILDINGS_ENABLED = booleanSetting("RareBuildingsEnabled", true),
		STRONGHOLDS_ENABLED = booleanSetting("StrongholdsEnabled", true),
		WOODLAND_MANSIONS_ENABLED = booleanSetting("WoodlandsMansionsEnabled", true),
		NETHER_FORTRESSES_ENABLED = booleanSetting("NetherFortressesEnabled", true),
		BURIED_TREASURE_ENABLED = booleanSetting("BuriedTreasureEnabled", true),
		OCEAN_RUINS_ENABLED = booleanSetting("OceanRuinsEnabled", true),
		PILLAGER_OUTPOSTS_ENABLED = booleanSetting("PillagerOutpostsEnabled", true),
		BASTION_REMNANTS_ENABLED = booleanSetting("BastionRemnantsEnabled", true),
		NETHER_FOSSILS_ENABLED = booleanSetting("NetherFossilsEnabled", true),
		END_CITIES_ENABLED = booleanSetting("EndCitiesEnabled", true),
		RUINED_PORTALS_ENABLED = booleanSetting("RuinedPortalsEnabled", true),
		SHIPWRECKS_ENABLED = booleanSetting("ShipwrecksEnabled", true),
		VILLAGES_ENABLED = booleanSetting("VillagesEnabled", true),
		
		OVERRIDE_GAME_RULES = booleanSetting("OverrideGameRules", false),
		DO_FIRE_TICK = booleanSetting("DoFireTick", true),
		MOB_GRIEFING = booleanSetting("MobGriefing", true),
		KEEP_INVENTORY = booleanSetting("KeepInventory", false),
		DO_MOB_SPAWNING = booleanSetting("DoMobSpawning", true),
		DO_MOB_LOOT = booleanSetting("DoMobLoot", true),
		DO_TILE_DROPS = booleanSetting("DoTileDrops", true),
		DO_ENTITY_DROPS = booleanSetting("DoEntityDrops", true),
		COMMAND_BLOCK_OUTPUT = booleanSetting("CommandBlockOutput", true),
		NATURAL_REGENERATION = booleanSetting("NaturalRegeneration", true),
		DO_DAY_LIGHT_CYCLE = booleanSetting("DoDaylightCycle", true),
		LOG_ADMIN_COMMANDS = booleanSetting("LogAdminCommands", true),
		SHOW_DEATH_MESSAGES = booleanSetting("ShowDeathMessages", true),
		SEND_COMMAND_FEEDBACK = booleanSetting("SendCommandFeedback", true),
		SPECTATORS_GENERATE_CHUNKS = booleanSetting("SpectatorsGenerateChunks", true),
		DISABLE_ELYTRA_MOVEMENT_CHECK = booleanSetting("DisableElytraMovementCheck", false),
		DO_WEATHER_CYCLE = booleanSetting("DoWeatherCycle", true),
		DO_LIMITED_CRAFTING = booleanSetting("DoLimitedCrafting", false),
		ANNOUNCE_ADVANCEMENTS = booleanSetting("AnnounceAdvancements", true),
		DISABLE_RAIDS = booleanSetting("DisableRaids", false),
		DO_INSOMNIA = booleanSetting("DoInsomnia", true),
		DROWNING_DAMAGE = booleanSetting("DrowningDamage", true),
		FALL_DAMAGE = booleanSetting("FallDamage", true),
		FIRE_DAMAGE = booleanSetting("FireDamage", true),
		DO_PATROL_SPAWNING = booleanSetting("DoPatrolSpawning", true),
		DO_TRADER_SPAWNING = booleanSetting("DoTraderSpawning", true),
		FORGIVE_DEAD_PLAYERS = booleanSetting("ForgiveDeadPlayers", true),
		UNIVERSAL_ANGER = booleanSetting("UniversalAnger", false),		
	
		HAS_SKYLIGHT = booleanSetting("HasSkylight", true),
		HAS_CEILING = booleanSetting("HasCeiling", false),
		ULTRA_WARM = booleanSetting("UltraWarm", false),
		NATURAL = booleanSetting("Natural", true),
		CREATE_DRAGON_FLIGHT = booleanSetting("CreateDragonFight", false),
		PIGLIN_SAFE = booleanSetting("PiglinSafe", false),
		BED_WORKS = booleanSetting("BedWorks", true),
		RESPAWN_ANCHOR_WORKS = booleanSetting("RespawnAnchorWorks", true),
		HAS_RAIDS = booleanSetting("HasRaids", true),
		FIXED_SPAWN_POINT = booleanSetting("FixedSpawnPoint", false),
		IMPROVED_BORDER_DECORATION = booleanSetting("ImprovedBorderDecoration", false),
		LARGE_ORE_VEINS = booleanSetting("LargeOreVeins", false),

		// Legacy, only needed for <= 1.12.2 presets, remove when presets have been updated.
		ISOTGPLUS = booleanSetting("IsOTGPlus", false)
	;

	public static final Setting<LocalMaterialData>
		WATER_BLOCK = new MaterialSetting("WaterBlock", LocalMaterials.WATER_NAME),
		ICE_BLOCK = new MaterialSetting("IceBlock", LocalMaterials.ICE_NAME),
		COOLED_LAVA_BLOCK = new MaterialSetting("CooledLavaBlock", LocalMaterials.LAVA_NAME),
		BEDROCK_BLOCK = new MaterialSetting("BedrockBlock", LocalMaterials.BEDROCK_NAME),
		CARVER_LAVA_BLOCK = new MaterialSetting("CarverLavaBlock", LocalMaterials.LAVA_NAME)
	;

    public static final Setting<ArrayList<LocalMaterialData>> PORTAL_BLOCKS = new MaterialListSetting("PortalBlocks", new String[] { LocalMaterials.QUARTZ_BLOCK_NAME });	
	
	public static final Setting<List<String>>
		ISLE_BIOMES = stringListSetting("IsleBiomes", "Deep Ocean", "MushroomIsland",
			"Ice Mountains", "DesertHills", "ForestHills", "Forest", "TaigaHills",
			"JungleHills", "Cold Taiga Hills", "Birch Forest Hills", "Extreme Hills+",
			"Mesa Plateau", "Mesa Plateau F", "Mesa Plateau M", "Mesa Plateau F M",
			"Mesa (Bryce)", "Mega Taiga Hills", "Mega Spruce Taiga Hills"),
		BORDER_BIOMES = stringListSetting("BorderBiomes",
			"JungleEdge", "JungleEdge M", "MushroomIslandShore", "Beach", "Extreme Hills Edge", "Desert", "Taiga")
	;

	public static final Setting<Float>
		SPAWN_POINT_ANGLE = floatSetting("SpawnPointAngle", 0.0f, Integer.MIN_VALUE, Integer.MAX_VALUE)		
	;
	
	public static final Setting<Double>
		FROZEN_OCEAN_TEMPERATURE = doubleSetting("OceanFreezingTemperature", 0.15, 0, 2),
		RAVINE_DEPTH = doubleSetting("RavineDepth", 3, 0.1, 15),
		CANYON_DEPTH = doubleSetting("CanyonDepth", 3, 0.1, 15),
		FRACTURE_HORIZONTAL = doubleSetting("FractureHorizontal", 0, -500, 500),
		FRACTURE_VERTICAL = doubleSetting("FractureVertical", 0, -500, 500),
		COORDINATE_SCALE = doubleSetting("CoordinateScale", 1.0D, 0.0D, Integer.MAX_VALUE),
		AMBIENT_LIGHT = doubleSetting("AmbientLight", 0.0D, 0.0D, Integer.MAX_VALUE)
	;

	public static final Setting<Integer>
		WORLD_FOG_COLOR = colorSetting("WorldFog", "0xC0D8FF")
	;

	// Deprecated settings
	public static final Setting<Boolean> FROZEN_RIVERS = booleanSetting("FrozenRivers", true);
	public static final Setting<List<String>> NORMAL_BIOMES = stringListSetting(
		"NormalBiomes", "Desert", "Forest", "Extreme Hills", "Swampland", "Plains", "Taiga", "Jungle", "River"
	);
	public static final Setting<List<String>> BLACKLISTED_BIOMES = stringListSetting("BlacklistedBiomes", "");	
	
	public static final Setting<List<String>> ICE_BIOMES = stringListSetting("IceBiomes", "Ice Plains");
}
