package com.pg85.otg.config.standard;

import com.pg85.otg.config.settingType.DoubleArraySetting;
import com.pg85.otg.config.settingType.MaterialSetting;
import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.settingType.Settings;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.constants.SettingsEnums.GrassColorModifier;
import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.OceanRuinsType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.RuinedPortalType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BiomeStandardValues extends Settings
{
	// >> Biome Extensions & Related
	public static final Collection<String> BiomeConfigExtensions = Arrays.asList(
		"BiomeConfig.ini", 
		".biome", 
		".bc", 
		".bc.ini",
		".biome.ini"
	);

	public static final Setting<Boolean>
		USE_WORLD_WATER_LEVEL = booleanSetting("UseWorldWaterLevel", true),		 
		DISABLE_BIOME_HEIGHT = booleanSetting("DisableBiomeHeight", false),
		STRONGHOLDS_ENABLED = booleanSetting("StrongholdsEnabled", true),
		NETHER_FORTRESSES_ENABLED = booleanSetting("NetherFortressesEnabled", false),
		OCEAN_MONUMENTS_ENABLED = booleanSetting("OceanMonumentsEnabled", false),
		WOODLAND_MANSIONS_ENABLED = booleanSetting("WoodlandMansionsEnabled", false),
		BURIED_TREASURE_ENABLED = booleanSetting("BuriedTreasureEnabled", false),
		SHIP_WRECK_ENABLED = booleanSetting("ShipWreckEnabled", false),
		SHIP_WRECK_BEACHED_ENABLED = booleanSetting("ShipWreckBeachedEnabled", false),
		PILLAGER_OUTPOST_ENABLED = booleanSetting("PillagerOutpostEnabled", false),
		BASTION_REMNANT_ENABLED = booleanSetting("BastionRemnantEnabled", false),
		NETHER_FOSSIL_ENABLED = booleanSetting("NetherFossilEnabled", false),
		END_CITY_ENABLED = booleanSetting("EndCityEnabled", false),
		REPLACE_CURRENT_MUSIC = booleanSetting("ReplaceCurrentMusic", false),
		USE_FROZEN_OCEAN_TEMPERATURE = booleanSetting("UseFrozenOceanTemperature", false),
			TEMPLATE_FOR_BIOME = booleanSetting("TemplateForBiome", false)
	;

	public static final Setting<String>
		RIVER_BIOME = stringSetting("RiverBiome", "River"),
		INHERIT_MOBS_BIOME_NAME = stringSetting("InheritMobsBiomeName", ""),
		PARTICLE_TYPE = stringSetting("ParticleType", ""),
		MUSIC = stringSetting("Music", ""),
		AMBIENT_SOUND = stringSetting("AmbientSound", ""),
		MOOD_SOUND = stringSetting("MoodSound", "minecraft:ambient.cave"),
		ADDITIONS_SOUND = stringSetting("AdditionsSound", ""),
		BIOME_CATEGORY = stringSetting("BiomeCategory", "plains"),
		LEGACY_GRASS_COLOR2 = stringSetting("GrassColor2", "#FFFFFF"),
		LEGACY_FOLIAGE_COLOR2 = stringSetting("FoliageColor2", "#FFFFFF")
	;

	public static final Setting<Integer>
		BIOME_SIZE = intSetting("BiomeSize", 4, 0, 20),
		BIOME_SIZE_WHEN_ISLE = intSetting("BiomeSizeWhenIsle", 6, 0, 20),
		BIOME_SIZE_WHEN_BORDER = intSetting("BiomeSizeWhenBorder", 8, 0, 20),
		BIOME_RARITY = intSetting("BiomeRarity", 100, 0, Integer.MAX_VALUE),
		BIOME_RARITY_WHEN_ISLE = intSetting("BiomeRarityWhenIsle", 97, 0, Integer.MAX_VALUE),
		SMOOTH_RADIUS = intSetting("SmoothRadius", 2, 0, 32),
		CUSTOM_HEIGHT_CONTROL_SMOOTH_RADIUS = intSetting("CustomHeightControlSmoothRadius", 2, 0, 32),
		WATER_LEVEL_MAX = WorldStandardValues.WATER_LEVEL_MAX,
		WATER_LEVEL_MIN = WorldStandardValues.WATER_LEVEL_MIN,
		VILLAGE_SIZE = intSetting("VillageSize", 6, 0, Integer.MAX_VALUE),
		PILLAGER_OUTPOST_SIZE = intSetting("PillagerOutpostSize", 7, 0, Integer.MAX_VALUE),
		BASTION_REMNANT_SIZE = intSetting("BastionRemnantSize", 6, 0, Integer.MAX_VALUE),
		MUSIC_MIN_DELAY = intSetting("MusicMinDelay", 0, 0, Integer.MAX_VALUE),
		MUSIC_MAX_DELAY = intSetting("MusicMaxDelay", 0, 0, Integer.MAX_VALUE),
		MOOD_SOUND_DELAY = intSetting("MoodSoundDelay", 6000, 0, Integer.MAX_VALUE),
		MOOD_SEARCH_RANGE = intSetting("MoodSearchRange", 8, 0, Integer.MAX_VALUE)
	;

	public static final Setting<Integer>
		BIOME_COLOR = colorSetting("BiomeColor", "#FFFFFF"),
		SKY_COLOR = colorSetting("SkyColor", "#7BA5FF"),
		WATER_COLOR = colorSetting("WaterColor", "#FFFFFF"),
		GRASS_COLOR = colorSetting("GrassColor", "#FFFFFF"),
		FOLIAGE_COLOR = colorSetting("FoliageColor", "#FFFFFF"),
		FOG_COLOR = colorSetting("FogColor", "#000000"),
		WATER_FOG_COLOR = colorSetting("WaterFogColor", "#000000")
	;

	public static final Setting<List<String>>
		BIOME_DICT_TAGS = stringListSetting("BiomeDictTags", ""),
		ISLE_IN_BIOME = stringListSetting("IsleInBiome", "Ocean"),
		BIOME_IS_BORDER = stringListSetting("BiomeIsBorder"),
		ONLY_BORDER_NEAR = stringListSetting("OnlyBorderNear"),
		NOT_BORDER_NEAR = stringListSetting("NotBorderNear")
	;

	public static final Setting<Double>
		VOLATILITY_1 = doubleSetting("Volatility1", 0, -1000, 1000),
		VOLATILITY_2 = doubleSetting("Volatility2", 0, -1000, 1000),
		VOLATILITY_WEIGHT_1 = doubleSetting("VolatilityWeight1", 0.5, -1000, 1000),
		VOLATILITY_WEIGHT_2 = doubleSetting("VolatilityWeight2", 0.45, -1000, 1000),
		MAX_AVERAGE_HEIGHT = doubleSetting("MaxAverageHeight", 0, -1000, 1000),
		MAX_AVERAGE_DEPTH = doubleSetting("MaxAverageDepth", 0, -1000, 1000),
		MOOD_OFFSET = doubleSetting("MoodOffset", 2.0, 0, Double.MAX_VALUE),
		ADDITIONS_TICK_CHANCE = doubleSetting("AdditionsTickChance", 0, 0, Double.MAX_VALUE)
	;

	public static final Setting<LocalMaterialData>
		STONE_BLOCK = new MaterialSetting("StoneBlock", LocalMaterials.STONE_NAME),
		SURFACE_BLOCK = new MaterialSetting("SurfaceBlock", LocalMaterials.GRASS_NAME),
		UNDER_WATER_SURFACE_BLOCK = new MaterialSetting("UnderWaterSurfaceBlock", ""),				
		GROUND_BLOCK = new MaterialSetting("GroundBlock", LocalMaterials.DIRT_NAME),
		COOLED_LAVA_BLOCK = WorldStandardValues.COOLED_LAVA_BLOCK,
		WATER_BLOCK = WorldStandardValues.WATER_BLOCK,
		ICE_BLOCK = WorldStandardValues.ICE_BLOCK,
		PACKED_ICE_BLOCK = new MaterialSetting("PackedIceBlock", LocalMaterials.PACKED_ICE_NAME),
		SNOW_BLOCK = new MaterialSetting("SnowBlock", LocalMaterials.SNOW_BLOCK_NAME)
	;

	public static final Setting<double[]>
		CUSTOM_HEIGHT_CONTROL = new DoubleArraySetting("CustomHeightControl")
	;

	public static final Setting<Float>
		BIOME_TEMPERATURE = floatSetting("BiomeTemperature", 0.5f, 0, 2),
		BIOME_WETNESS = floatSetting("BiomeWetness", 0.5f, 0, 1),
		BIOME_HEIGHT = floatSetting("BiomeHeight", 0.1f, -10, 10),
		BIOME_VOLATILITY = floatSetting("BiomeVolatility", 0.3f, -1000, 1000),
		// TODO: Find the proper max values for these probabilities, likely 1 for most.
		MINESHAFT_PROBABILITY = floatSetting("MineshaftProbability", 0.004f, 0f, 1f),
		OCEAN_RUINS_LARGE_PROBABILITY = floatSetting("OceanRuinsLargeProbability", 0.3f, 0f, 1f),
		OCEAN_RUINS_CLUSTER_PROBABILITY = floatSetting("OceanRuinsClusterProbability", 0.9f, 0f, 1f),
		BURIED_TREASURE_PROBABILITY = floatSetting("BuriedTreasureProbability", 0.01f, 0f, 1f),
		PARTICLE_PROBABILITY = floatSetting("ParticleProbability", 0f, 0, 1f),
		FOG_DENSITY = floatSetting("FogDensity", 0.0f, 0f, 1f)
	;

	public static final Setting<List<WeightedMobSpawnGroup>>
		SPAWN_MONSTERS = mobGroupListSetting("SpawnMonsters"),
		SPAWN_CREATURES = mobGroupListSetting("SpawnCreatures"),
		SPAWN_WATER_CREATURES = mobGroupListSetting("SpawnWaterCreatures"),
		SPAWN_UNDERGROUND_WATER_CREATURES = mobGroupListSetting("SpawnUndergroundWaterCreatures"),
		SPAWN_AMBIENT_CREATURES = mobGroupListSetting("SpawnAmbientCreatures"),
		SPAWN_WATER_AMBIENT_CREATURES = mobGroupListSetting("SpawnWaterAmbientCreatures"),
		SPAWN_MISC_CREATURES = mobGroupListSetting("SpawnMiscCreatures")
	;
	
	public static final Setting<ColorSet>
		GRASS_COLOR_CONTROL = colorSetSetting("GrassColorControl"),
		FOLIAGE_COLOR_CONTROL = colorSetSetting("FoliageColorControl"),
		WATER_COLOR_CONTROL = colorSetSetting("WaterColorControl")
	;

	public static final Setting<VillageType> VILLAGE_TYPE = enumSetting("VillageType", VillageType.disabled);
	public static final Setting<MineshaftType> MINESHAFT_TYPE = enumSetting("MineshaftType", MineshaftType.normal);
	public static final Setting<RareBuildingType> RARE_BUILDING_TYPE = enumSetting("RareBuildingType", RareBuildingType.disabled);
	public static final Setting<RuinedPortalType> RUINED_PORTAL_TYPE = enumSetting("RuinedPortalType", RuinedPortalType.disabled);
	public static final Setting<OceanRuinsType> OCEAN_RUINS_TYPE = enumSetting("OceanRuinsType", OceanRuinsType.disabled);
	public static final Setting<GrassColorModifier> GRASS_COLOR_MODIFIER = enumSetting("GrassColorModifier", GrassColorModifier.None);
	public static final Setting<SettingsEnums.TemplateBiomeType> TEMPLATE_BIOME_TYPE = enumSetting("VillageType", SettingsEnums.TemplateBiomeType.Overworld);
	public static final Setting<ReplaceBlockMatrix> REPLACED_BLOCKS = replacedBlocksSetting("ReplacedBlocks");	
	public static final Object[] SURFACE_AND_GROUND_CONTROL = new Object[0];
}
