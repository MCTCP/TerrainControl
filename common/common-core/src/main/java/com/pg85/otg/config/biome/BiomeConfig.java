package com.pg85.otg.config.biome;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;

import com.pg85.otg.OTG;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.IceSpikeType;
import com.pg85.otg.customobject.resource.CustomObjectResource;
import com.pg85.otg.customobject.resource.CustomStructureResource;
import com.pg85.otg.customobject.resource.SaplingResource;
import com.pg85.otg.customobject.resource.TreeResource;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.gen.resource.*;
import com.pg85.otg.gen.surface.SimpleSurfaceGenerator;
import com.pg85.otg.gen.surface.SurfaceGeneratorSetting;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import com.pg85.otg.util.biome.SimpleColorSet;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.minecraft.EntityCategory;
import com.pg85.otg.util.minecraft.PlantType;
import com.pg85.otg.util.minecraft.SaplingType;

/**
 * BiomeConfig (*.bc) classes
 * 
 * IBiomeConfig defines anything that's used/exposed between projects.
 * BiomeConfigBase implements anything needed for IBiomeConfig. BiomeConfig
 * contains only fields/methods used for io/serialisation/instantiation.
 * 
 * BiomeConfig should be used only in common-core and platform-specific layers,
 * when reading/writing settings on app start. IBiomeConfig should be used
 * wherever settings are used in code.
 */
public class BiomeConfig extends BiomeConfigBase
{
	public static final HashMap<String, Class<? extends ConfigFunction<?>>> RESOURCE_QUEUE_RESOURCES = new HashMap<>();
	static
	{
		RESOURCE_QUEUE_RESOURCES.put("AboveWaterRes", AboveWaterResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Boulder", BoulderResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Cactus", CactusResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Dungeon", DungeonResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Grass", GrassResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Fossil", FossilResource.class);
		RESOURCE_QUEUE_RESOURCES.put("IceSpike", IceSpikeResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Liquid", LiquidResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Ore", OreResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Plant", PlantResource.class);
		RESOURCE_QUEUE_RESOURCES.put("UnderWaterPlant", UnderWaterPlantResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Reed", ReedResource.class);
		RESOURCE_QUEUE_RESOURCES.put("SmallLake", SmallLakeResource.class);
		RESOURCE_QUEUE_RESOURCES.put("SurfacePatch", SurfacePatchResource.class);
		RESOURCE_QUEUE_RESOURCES.put("UndergroundLake", UndergroundLakeResource.class);
		RESOURCE_QUEUE_RESOURCES.put("UnderWaterOre", UnderWaterOreResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Vein", VeinResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Vines", VinesResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Well", WellResource.class);
		RESOURCE_QUEUE_RESOURCES.put("CustomObject", CustomObjectResource.class);
		RESOURCE_QUEUE_RESOURCES.put("CustomStructure", CustomStructureResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Sapling", SaplingResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Tree", TreeResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Bamboo", BambooResource.class);
		RESOURCE_QUEUE_RESOURCES.put("SeaGrass", SeaGrassResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Kelp", KelpResource.class);
		RESOURCE_QUEUE_RESOURCES.put("SeaPickle", SeaPickleResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Registry", RegistryResource.class);
		RESOURCE_QUEUE_RESOURCES.put("CoralMushroom", CoralMushroomResource.class);
		RESOURCE_QUEUE_RESOURCES.put("CoralTree", CoralTreeResource.class);
		RESOURCE_QUEUE_RESOURCES.put("CoralClaw", CoralClawResource.class);
		RESOURCE_QUEUE_RESOURCES.put("Iceberg", IcebergResource.class);
		RESOURCE_QUEUE_RESOURCES.put("BasaltColumn", BasaltColumnResource.class);
	}

	// Private fields, only used when reading/writing

	// Settings container, used so we can copy a biomeconfig while 
	// changing only its id and registry key, used for non-otg 
	// biomes in otg worlds.
	private SettingsContainer privateSettings = new SettingsContainer();
	class SettingsContainer
	{	
		private int configWaterLevelMax;
		private int configWaterLevelMin;
		
		private LocalMaterialData configWaterBlock;
		private LocalMaterialData configIceBlock;
		private LocalMaterialData configCooledLavaBlock;
	
		private double volatilityRaw1;
		private double volatilityRaw2;
		private double volatilityWeightRaw1;
		private double volatilityWeightRaw2;
	
		private List<WeightedMobSpawnGroup> spawnMonsters = new ArrayList<WeightedMobSpawnGroup>();
		private List<WeightedMobSpawnGroup> spawnCreatures = new ArrayList<WeightedMobSpawnGroup>();
		private List<WeightedMobSpawnGroup> spawnWaterCreatures = new ArrayList<WeightedMobSpawnGroup>();
		private List<WeightedMobSpawnGroup> spawnAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
		private List<WeightedMobSpawnGroup> spawnWaterAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
		private List<WeightedMobSpawnGroup> spawnMiscCreatures = new ArrayList<WeightedMobSpawnGroup>();
	}

	public BiomeConfig(String biomeName)
	{
		super(biomeName);
	}

	public BiomeConfig(
		String biomeName, BiomeConfigStub biomeConfigStub, Path presetFolder, SettingsMap settings, 
		IWorldConfig worldConfig, String presetShortName, int presetMajorVersion, 
		IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader
	)
	{
		super(biomeName);
		this.setRegistryKey(new OTGBiomeResourceLocation(presetFolder, presetShortName, presetMajorVersion, biomeName));

		// Mob inheritance
		// Mob spawning data was already loaded seperately before the rest of the
		// biomeconfig to make inheritance work properly
		// Forge: If this is a vanilla biome then mob spawning settings have been
		// inherited from vanilla MC biomes
		// This includes any mobs added to vanilla biomes by other mods when MC started.

		if (biomeConfigStub != null)
		{
			this.privateSettings.spawnMonsters.addAll(biomeConfigStub.getSpawner(EntityCategory.MONSTER));
			this.privateSettings.spawnCreatures.addAll(biomeConfigStub.getSpawner(EntityCategory.CREATURE));
			this.privateSettings.spawnWaterCreatures.addAll(biomeConfigStub.getSpawner(EntityCategory.WATER_CREATURE));
			this.privateSettings.spawnAmbientCreatures.addAll(biomeConfigStub.getSpawner(EntityCategory.AMBIENT_CREATURE));
			this.privateSettings.spawnWaterAmbientCreatures.addAll(biomeConfigStub.getSpawner(EntityCategory.WATER_AMBIENT));
			this.privateSettings.spawnMiscCreatures.addAll(biomeConfigStub.getSpawner(EntityCategory.MISC));

			this.settings.spawnMonstersMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.MONSTER));
			this.settings.spawnCreaturesMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.CREATURE));
			this.settings.spawnWaterCreaturesMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.WATER_CREATURE));
			this.settings.spawnAmbientCreaturesMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.AMBIENT_CREATURE));
			this.settings.spawnWaterAmbientCreaturesMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.WATER_AMBIENT));
			this.settings.spawnMiscCreaturesMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.MISC));
		}

		this.settings.worldConfig = worldConfig;

		this.renameOldSettings(settings, logger, materialReader);
		this.readConfigSettings(settings, biomeResourcesManager, logger, materialReader, presetFolder.toFile().getName());
		this.validateAndCorrectSettings(presetFolder, logger);

		// Set water level
		if (this.settings.useWorldWaterLevel)
		{
			this.settings.waterLevelMax = worldConfig.getWaterLevelMax();
			this.settings.waterLevelMin = worldConfig.getWaterLevelMin();
			this.settings.waterBlock = worldConfig.getWaterBlock();
			this.settings.iceBlock = worldConfig.getIceBlock();
			this.settings.cooledLavaBlock = worldConfig.getCooledLavaBlock();
		} else {
			this.settings.waterLevelMax = this.privateSettings.configWaterLevelMax;
			this.settings.waterLevelMin = this.privateSettings.configWaterLevelMin;
			this.settings.waterBlock = this.privateSettings.configWaterBlock;
			this.settings.iceBlock = this.privateSettings.configIceBlock;
			this.settings.cooledLavaBlock = this.privateSettings.configCooledLavaBlock;
		}
	}

	@Override
	protected void readConfigSettings(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader, String presetFolderName)
	{
		this.settings.templateForBiome = reader.getSetting(BiomeStandardValues.TEMPLATE_FOR_BIOME, logger);

		boolean isTemplateBiome = this.settings.templateForBiome;
		
		if(!isTemplateBiome)
		{
			this.settings.biomeCategory = reader.getSetting(BiomeStandardValues.BIOME_CATEGORY, logger);
			this.settings.biomeTemperature = reader.getSetting(BiomeStandardValues.BIOME_TEMPERATURE, logger);
			this.settings.biomeWetness = reader.getSetting(BiomeStandardValues.BIOME_WETNESS, logger);
			this.settings.stoneBlock = reader.getSetting(BiomeStandardValues.STONE_BLOCK, logger, materialReader);
			this.settings.surfaceBlock = reader.getSetting(BiomeStandardValues.SURFACE_BLOCK, logger, materialReader);
			this.settings.groundBlock = reader.getSetting(BiomeStandardValues.GROUND_BLOCK, logger, materialReader);
			this.settings.underWaterSurfaceBlock = reader.getSetting(BiomeStandardValues.UNDER_WATER_SURFACE_BLOCK, logger, materialReader);		
			if(this.settings.underWaterSurfaceBlock == null)
			{
				this.settings.underWaterSurfaceBlock = this.settings.groundBlock;
			}
			this.settings.skyColor = reader.getSetting(BiomeStandardValues.SKY_COLOR, logger);
			this.settings.waterColor = reader.getSetting(BiomeStandardValues.WATER_COLOR, logger);
			this.settings.waterColorControl = reader.getSetting(BiomeStandardValues.WATER_COLOR_CONTROL, logger);
			this.settings.grassColor = reader.getSetting(BiomeStandardValues.GRASS_COLOR, logger);
			this.settings.grassColorControl = reader.getSetting(BiomeStandardValues.GRASS_COLOR_CONTROL, logger);
			this.settings.grassColorModifier = reader.getSetting(BiomeStandardValues.GRASS_COLOR_MODIFIER, logger);
			this.settings.foliageColor = reader.getSetting(BiomeStandardValues.FOLIAGE_COLOR, logger);
			this.settings.foliageColorControl = reader.getSetting(BiomeStandardValues.FOLIAGE_COLOR_CONTROL, logger);
			this.settings.fogColor = reader.getSetting(BiomeStandardValues.FOG_COLOR, logger);
			this.settings.fogDensity = reader.getSetting(BiomeStandardValues.FOG_DENSITY, logger);
			this.settings.waterFogColor = reader.getSetting(BiomeStandardValues.WATER_FOG_COLOR, logger);
			this.settings.particleType = reader.getSetting(BiomeStandardValues.PARTICLE_TYPE, logger);
			this.settings.music = reader.getSetting(BiomeStandardValues.MUSIC, logger);
			this.settings.musicMinDelay = reader.getSetting(BiomeStandardValues.MUSIC_MIN_DELAY, logger);
			this.settings.musicMaxDelay = reader.getSetting(BiomeStandardValues.MUSIC_MAX_DELAY, logger);
			this.settings.replaceCurrentMusic = reader.getSetting(BiomeStandardValues.REPLACE_CURRENT_MUSIC, logger);
			this.settings.ambientSound = reader.getSetting(BiomeStandardValues.AMBIENT_SOUND, logger);
			this.settings.moodSound = reader.getSetting(BiomeStandardValues.MOOD_SOUND, logger);
			this.settings.moodSoundDelay = reader.getSetting(BiomeStandardValues.MOOD_SOUND_DELAY, logger);
			this.settings.moodSearchRange = reader.getSetting(BiomeStandardValues.MOOD_SEARCH_RANGE, logger);
			this.settings.moodOffset = reader.getSetting(BiomeStandardValues.MOOD_OFFSET, logger);
			this.settings.additionsSound = reader.getSetting(BiomeStandardValues.ADDITIONS_SOUND, logger);
			this.settings.additionsTickChance = reader.getSetting(BiomeStandardValues.ADDITIONS_TICK_CHANCE, logger);
			this.settings.particleProbability = reader.getSetting(BiomeStandardValues.PARTICLE_PROBABILITY, logger);
			this.settings.strongholdsEnabled = reader.getSetting(BiomeStandardValues.STRONGHOLDS_ENABLED, logger);
			this.settings.oceanMonumentsEnabled = reader.getSetting(BiomeStandardValues.OCEAN_MONUMENTS_ENABLED, logger);
			this.settings.woodLandMansionsEnabled = reader.getSetting(BiomeStandardValues.WOODLAND_MANSIONS_ENABLED, logger);
			this.settings.netherFortressesEnabled = reader.getSetting(BiomeStandardValues.NETHER_FORTRESSES_ENABLED, logger);
			this.settings.villageType = reader.getSetting(BiomeStandardValues.VILLAGE_TYPE, logger);
			this.settings.villageSize = reader.getSetting(BiomeStandardValues.VILLAGE_SIZE, logger);		
			this.settings.mineshaftType = reader.getSetting(BiomeStandardValues.MINESHAFT_TYPE, logger);
			this.settings.rareBuildingType = reader.getSetting(BiomeStandardValues.RARE_BUILDING_TYPE, logger);
			this.settings.buriedTreasureEnabled = reader.getSetting(BiomeStandardValues.BURIED_TREASURE_ENABLED, logger);
			this.settings.shipWreckEnabled = reader.getSetting(BiomeStandardValues.SHIP_WRECK_ENABLED, logger);
			this.settings.shipWreckBeachedEnabled = reader.getSetting(BiomeStandardValues.SHIP_WRECK_BEACHED_ENABLED, logger);
			this.settings.pillagerOutpostEnabled = reader.getSetting(BiomeStandardValues.PILLAGER_OUTPOST_ENABLED, logger);
			this.settings.bastionRemnantEnabled = reader.getSetting(BiomeStandardValues.BASTION_REMNANT_ENABLED, logger);
			this.settings.netherFossilEnabled = reader.getSetting(BiomeStandardValues.NETHER_FOSSIL_ENABLED, logger);
			this.settings.endCityEnabled = reader.getSetting(BiomeStandardValues.END_CITY_ENABLED, logger);		
			this.settings.mineshaftProbability = reader.getSetting(BiomeStandardValues.MINESHAFT_PROBABILITY, logger);
			this.settings.ruinedPortalType = reader.getSetting(BiomeStandardValues.RUINED_PORTAL_TYPE, logger);
			this.settings.oceanRuinsType = reader.getSetting(BiomeStandardValues.OCEAN_RUINS_TYPE, logger);
			this.settings.oceanRuinsLargeProbability = reader.getSetting(BiomeStandardValues.OCEAN_RUINS_LARGE_PROBABILITY, logger);
			this.settings.oceanRuinsClusterProbability = reader.getSetting(BiomeStandardValues.OCEAN_RUINS_CLUSTER_PROBABILITY, logger);
			this.settings.buriedTreasureProbability = reader.getSetting(BiomeStandardValues.BURIED_TREASURE_PROBABILITY, logger);
			this.settings.pillagerOutpostSize = reader.getSetting(BiomeStandardValues.PILLAGER_OUTPOST_SIZE, logger);
			this.settings.bastionRemnantSize = reader.getSetting(BiomeStandardValues.BASTION_REMNANT_SIZE, logger);		
			this.settings.biomeDictTags = reader.getSetting(BiomeStandardValues.BIOME_DICT_TAGS, logger);
			this.settings.inheritMobsBiomeName = reader.getSetting(BiomeStandardValues.INHERIT_MOBS_BIOME_NAME, logger);
			this.settings.useFrozenOceanTemperature = reader.getSetting(BiomeStandardValues.USE_FROZEN_OCEAN_TEMPERATURE, logger);
		} else {
			this.settings.biomeCategory = BiomeStandardValues.BIOME_CATEGORY.getDefaultValue();
			this.settings.biomeTemperature = BiomeStandardValues.BIOME_TEMPERATURE.getDefaultValue();
			this.settings.biomeWetness = BiomeStandardValues.BIOME_WETNESS.getDefaultValue();
			this.settings.stoneBlock = LocalMaterials.STONE;
			this.settings.surfaceBlock = LocalMaterials.STONE;
			this.settings.groundBlock = LocalMaterials.STONE;
			this.settings.underWaterSurfaceBlock = LocalMaterials.STONE;		
			this.settings.skyColor = BiomeStandardValues.SKY_COLOR.getDefaultValue();
			this.settings.waterColor = BiomeStandardValues.WATER_COLOR.getDefaultValue();
			this.settings.waterColorControl = BiomeStandardValues.WATER_COLOR_CONTROL.getDefaultValue();
			this.settings.grassColor = BiomeStandardValues.GRASS_COLOR.getDefaultValue();
			this.settings.grassColorControl = BiomeStandardValues.GRASS_COLOR_CONTROL.getDefaultValue();
			this.settings.grassColorModifier = BiomeStandardValues.GRASS_COLOR_MODIFIER.getDefaultValue();
			this.settings.foliageColor = BiomeStandardValues.FOLIAGE_COLOR.getDefaultValue();
			this.settings.foliageColorControl = BiomeStandardValues.FOLIAGE_COLOR_CONTROL.getDefaultValue();
			this.settings.fogColor = BiomeStandardValues.FOG_COLOR.getDefaultValue();
			this.settings.fogDensity = BiomeStandardValues.FOG_DENSITY.getDefaultValue();
			this.settings.waterFogColor = BiomeStandardValues.WATER_FOG_COLOR.getDefaultValue();
			this.settings.particleType = BiomeStandardValues.PARTICLE_TYPE.getDefaultValue();
			this.settings.music = BiomeStandardValues.MUSIC.getDefaultValue();
			this.settings.musicMinDelay = BiomeStandardValues.MUSIC_MIN_DELAY.getDefaultValue();
			this.settings.musicMaxDelay = BiomeStandardValues.MUSIC_MAX_DELAY.getDefaultValue();
			this.settings.replaceCurrentMusic = BiomeStandardValues.REPLACE_CURRENT_MUSIC.getDefaultValue();
			this.settings.ambientSound = BiomeStandardValues.AMBIENT_SOUND.getDefaultValue();
			this.settings.moodSound = BiomeStandardValues.MOOD_SOUND.getDefaultValue();
			this.settings.moodSoundDelay = BiomeStandardValues.MOOD_SOUND_DELAY.getDefaultValue();
			this.settings.moodSearchRange = BiomeStandardValues.MOOD_SEARCH_RANGE.getDefaultValue();
			this.settings.moodOffset = BiomeStandardValues.MOOD_OFFSET.getDefaultValue();
			this.settings.additionsSound = BiomeStandardValues.ADDITIONS_SOUND.getDefaultValue();
			this.settings.additionsTickChance = BiomeStandardValues.ADDITIONS_TICK_CHANCE.getDefaultValue();
			this.settings.particleProbability = BiomeStandardValues.PARTICLE_PROBABILITY.getDefaultValue();
			this.settings.strongholdsEnabled = BiomeStandardValues.STRONGHOLDS_ENABLED.getDefaultValue();
			this.settings.oceanMonumentsEnabled = BiomeStandardValues.OCEAN_MONUMENTS_ENABLED.getDefaultValue();
			this.settings.woodLandMansionsEnabled = BiomeStandardValues.WOODLAND_MANSIONS_ENABLED.getDefaultValue();
			this.settings.netherFortressesEnabled = BiomeStandardValues.NETHER_FORTRESSES_ENABLED.getDefaultValue();
			this.settings.villageType = BiomeStandardValues.VILLAGE_TYPE.getDefaultValue();
			this.settings.villageSize = BiomeStandardValues.VILLAGE_SIZE.getDefaultValue();
			this.settings.mineshaftType = BiomeStandardValues.MINESHAFT_TYPE.getDefaultValue();
			this.settings.rareBuildingType = BiomeStandardValues.RARE_BUILDING_TYPE.getDefaultValue();
			this.settings.buriedTreasureEnabled = BiomeStandardValues.BURIED_TREASURE_ENABLED.getDefaultValue();
			this.settings.shipWreckEnabled = BiomeStandardValues.SHIP_WRECK_ENABLED.getDefaultValue();
			this.settings.shipWreckBeachedEnabled = BiomeStandardValues.SHIP_WRECK_BEACHED_ENABLED.getDefaultValue();
			this.settings.pillagerOutpostEnabled = BiomeStandardValues.PILLAGER_OUTPOST_ENABLED.getDefaultValue();
			this.settings.bastionRemnantEnabled = BiomeStandardValues.BASTION_REMNANT_ENABLED.getDefaultValue();
			this.settings.netherFossilEnabled = BiomeStandardValues.NETHER_FOSSIL_ENABLED.getDefaultValue();
			this.settings.endCityEnabled = BiomeStandardValues.END_CITY_ENABLED.getDefaultValue();
			this.settings.mineshaftProbability = BiomeStandardValues.MINESHAFT_PROBABILITY.getDefaultValue();
			this.settings.ruinedPortalType = BiomeStandardValues.RUINED_PORTAL_TYPE.getDefaultValue();
			this.settings.oceanRuinsType = BiomeStandardValues.OCEAN_RUINS_TYPE.getDefaultValue();
			this.settings.oceanRuinsLargeProbability = BiomeStandardValues.OCEAN_RUINS_LARGE_PROBABILITY.getDefaultValue();
			this.settings.oceanRuinsClusterProbability = BiomeStandardValues.OCEAN_RUINS_CLUSTER_PROBABILITY.getDefaultValue();
			this.settings.buriedTreasureProbability = BiomeStandardValues.BURIED_TREASURE_PROBABILITY.getDefaultValue();
			this.settings.pillagerOutpostSize = BiomeStandardValues.PILLAGER_OUTPOST_SIZE.getDefaultValue();
			this.settings.bastionRemnantSize = BiomeStandardValues.BASTION_REMNANT_SIZE.getDefaultValue();
			this.settings.biomeDictTags = BiomeStandardValues.BIOME_DICT_TAGS.getDefaultValue();
			this.settings.inheritMobsBiomeName = BiomeStandardValues.INHERIT_MOBS_BIOME_NAME.getDefaultValue();
			this.settings.useFrozenOceanTemperature = BiomeStandardValues.USE_FROZEN_OCEAN_TEMPERATURE.getDefaultValue();
		}

		this.settings.biomeSize = reader.getSetting(BiomeStandardValues.BIOME_SIZE, logger);
		this.settings.biomeRarity = reader.getSetting(BiomeStandardValues.BIOME_RARITY, logger);
		this.settings.biomeRarityWhenIsle = reader.getSetting(BiomeStandardValues.BIOME_RARITY_WHEN_ISLE, logger);
		this.settings.biomeColor = reader.getSetting(BiomeStandardValues.BIOME_COLOR, logger);
		this.settings.riverBiome = reader.getSetting(BiomeStandardValues.RIVER_BIOME, logger);
		this.settings.isleInBiome = reader.getSetting(BiomeStandardValues.ISLE_IN_BIOME, logger);
		this.settings.biomeSizeWhenIsle = reader.getSetting(BiomeStandardValues.BIOME_SIZE_WHEN_ISLE, logger);
		this.settings.biomeIsBorder = reader.getSetting(BiomeStandardValues.BIOME_IS_BORDER, logger);
		this.settings.onlyBorderNear = reader.getSetting(BiomeStandardValues.ONLY_BORDER_NEAR, logger);
		this.settings.notBorderNear = reader.getSetting(BiomeStandardValues.NOT_BORDER_NEAR, logger);
		this.settings.biomeSizeWhenBorder = reader.getSetting(BiomeStandardValues.BIOME_SIZE_WHEN_BORDER, logger);	
		this.settings.biomeHeight = reader.getSetting(BiomeStandardValues.BIOME_HEIGHT, logger);
		this.settings.biomeVolatility = reader.getSetting(BiomeStandardValues.BIOME_VOLATILITY, logger);
		this.settings.smoothRadius = reader.getSetting(BiomeStandardValues.SMOOTH_RADIUS, logger);
		this.settings.CHCSmoothRadius = reader.getSetting(BiomeStandardValues.CUSTOM_HEIGHT_CONTROL_SMOOTH_RADIUS, logger);		
		this.privateSettings.configWaterBlock = reader.getSetting(BiomeStandardValues.WATER_BLOCK, logger, materialReader);
		this.privateSettings.configIceBlock = reader.getSetting(BiomeStandardValues.ICE_BLOCK, logger, materialReader);
		this.settings.packedIceBlock = reader.getSetting(BiomeStandardValues.PACKED_ICE_BLOCK, logger, materialReader);
		this.settings.snowBlock = reader.getSetting(BiomeStandardValues.SNOW_BLOCK, logger, materialReader);
		this.privateSettings.configCooledLavaBlock = reader.getSetting(BiomeStandardValues.COOLED_LAVA_BLOCK, logger, materialReader);
		this.settings.replacedBlocks = reader.getSetting(BiomeStandardValues.REPLACED_BLOCKS, logger, materialReader);
		this.settings.sandStoneBlock = LocalMaterials.SANDSTONE;
		this.settings.redSandStoneBlock = LocalMaterials.RED_SANDSTONE;
		this.settings.surfaceAndGroundControl = reader.getSetting(SurfaceGeneratorSetting.SURFACE_AND_GROUND_CONTROL, new SimpleSurfaceGenerator(), logger, materialReader);
		this.settings.useWorldWaterLevel = reader.getSetting(BiomeStandardValues.USE_WORLD_WATER_LEVEL, logger);
		this.privateSettings.configWaterLevelMax = reader.getSetting(BiomeStandardValues.WATER_LEVEL_MAX, logger);
		this.privateSettings.configWaterLevelMin = reader.getSetting(BiomeStandardValues.WATER_LEVEL_MIN, logger);
		this.privateSettings.volatilityRaw1 = reader.getSetting(BiomeStandardValues.VOLATILITY_1, logger);
		this.privateSettings.volatilityRaw2 = reader.getSetting(BiomeStandardValues.VOLATILITY_2, logger);
		this.privateSettings.volatilityWeightRaw1 = reader.getSetting(BiomeStandardValues.VOLATILITY_WEIGHT_1, logger);
		this.privateSettings.volatilityWeightRaw2 = reader.getSetting(BiomeStandardValues.VOLATILITY_WEIGHT_2, logger);
		this.settings.disableBiomeHeight = reader.getSetting(BiomeStandardValues.DISABLE_BIOME_HEIGHT, logger);
		this.settings.maxAverageHeight = reader.getSetting(BiomeStandardValues.MAX_AVERAGE_HEIGHT, logger);
		this.settings.maxAverageDepth = reader.getSetting(BiomeStandardValues.MAX_AVERAGE_DEPTH, logger);

		this.readResourceSettings(reader, biomeResourcesManager, logger, materialReader, presetFolderName);
		
		this.settings.chcData = new double[this.settings.worldConfig.getWorldHeightCap() / Constants.PIECE_Y_SIZE + 1];
		this.readHeightSettings(reader, this.settings.chcData, BiomeStandardValues.CUSTOM_HEIGHT_CONTROL, BiomeStandardValues.CUSTOM_HEIGHT_CONTROL.getDefaultValue(), logger);
	
		if(!isTemplateBiome)
		{
			updateLegacySettings(reader, materialReader, logger);
		}
	}

	private void updateLegacySettings(SettingsMap reader,IMaterialReader materialReader, ILogger logger)
	{
		if (this.settings.fogDensity == 0.5 && this.settings.fogColor == BiomeStandardValues.FOG_COLOR.getDefaultValue())
		{
			this.settings.fogDensity = 0.0f;
		}

		this.readLegacyColorSetting(reader, materialReader, BiomeStandardValues.LEGACY_GRASS_COLOR2, logger).ifPresent(set -> this.settings.grassColorControl = set);
		this.readLegacyColorSetting(reader, materialReader, BiomeStandardValues.LEGACY_FOLIAGE_COLOR2, logger).ifPresent(set -> this.settings.foliageColorControl = set);
	}

	private Optional<ColorSet> readLegacyColorSetting(SettingsMap reader, IMaterialReader materialReader, Setting<String> oldSetting, ILogger logger)
	{
		ColorSet colorSet = null;
		if (reader.hasSetting(oldSetting))
		{
			String color = reader.getSetting(oldSetting, logger);
			if (!color.equals(oldSetting.getDefaultValue()))
			{
				try
				{
					colorSet = new SimpleColorSet(new String[]
					{ color, "-0.1" }, materialReader);
				} catch (InvalidConfigException e) {
					if (logger.getLogCategoryEnabled(LogCategory.CONFIGS))
					{
						logger.log(LogLevel.ERROR, LogCategory.CONFIGS, MessageFormat.format(
							"Encountered an invalid value while reading legacy setting {0} with value {1}: {2}",
							oldSetting.getName(), color, e.getMessage()
						));
					}
				}
			}
		}
		return Optional.ofNullable(colorSet);
	}

	private void readHeightSettings(SettingsMap settings, double[] heightMatrix, Setting<double[]> setting, double[] defaultValue, ILogger logger)
	{
		double[] keys = settings.getSetting(setting, defaultValue, logger);
		for (int i = 0; i < heightMatrix.length && i < keys.length; i++)
		{
			heightMatrix[i] = keys[i];
		}
	}

	private void readResourceSettings(SettingsMap settings, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader, String presetFolderName)
	{
		List<ConfigFunction<IBiomeConfig>> resources = new ArrayList<>(settings.getConfigFunctions(this, biomeResourcesManager, logger, materialReader, presetFolderName, OTG.getEngine().getPluginConfig()));
		for (ConfigFunction<IBiomeConfig> res : resources)
		{
			if (res != null)
			{
				if (res instanceof SaplingResource)
				{
					SaplingResource sapling = (SaplingResource) res;
					if (sapling.saplingType == SaplingType.Custom)
					{
						try
						{
							// Puts big custom saplings in the big list and small in the small list
							if (sapling.wideTrunk)
							{
								this.settings.customBigSaplingGrowers.put(sapling.saplingMaterial, sapling);
							} else {
								this.settings.customSaplingGrowers.put(sapling.saplingMaterial, sapling);
							}
						} catch (NullPointerException e) {
							if (logger.getLogCategoryEnabled(LogCategory.CONFIGS) && logger.canLogForPreset(presetFolderName))
							{
								logger.log(LogLevel.ERROR, LogCategory.CONFIGS, "Unrecognized sapling type in biome " + this.getName());
							}
						}
					} else {
						this.settings.saplingGrowers.put(sapling.saplingType, sapling);
					}
				}
			}
		}

		resources = new ArrayList<>(settings.getConfigFunctions(this, biomeResourcesManager, logger, materialReader, presetFolderName, OTG.getEngine().getPluginConfig()));
		for (ConfigFunction<IBiomeConfig> res : resources)
		{
			if (res != null)
			{
				if (!(res instanceof SaplingResource))
				{
					this.settings.resourceQueue.add(res);
				}
				if (res instanceof CustomStructureResource)
				{
					this.settings.customStructures.add((CustomStructureResource) res);
				}
			}
		}
	}

	@Override
	protected void writeConfigSettings(SettingsMap writer)
	{
		boolean isTemplateBiome = this.settings.templateForBiome;
		
		writer.header1("Biome Identity");
		
		writer.putSetting(BiomeStandardValues.TEMPLATE_FOR_BIOME, this.settings.templateForBiome,
			"Set this to true if this biome config is used with non-OTG biomes, configured in the WorldConfig via TemplateBiome()",
			"OTG generates the terrain for the biome as configured in this file and spawns resources, but also allows the biome to spawn ",
			"its own resources and mobs and apply its settings. Because of this, the following OTG settings cannot be used:",
			"- Colors, Mob spawning, particles, sounds, vanilla structures, wetness, temperature.",
			"What can be configured: ",
			" - Biome generator settings.",
			" - Terrain settings.",
			" - Resources. Non-OTG biome resources are currently spawned after all OTG resources in the resourcequeue.",
			" - OTG settings not mentioned above that are handled by OTG and don't rely on MC logic.");

		if(!isTemplateBiome)
		{		
			writer.putSetting(BiomeStandardValues.BIOME_DICT_TAGS, this.settings.biomeDictTags,
				"Forge Biome Dictionary tags used by other mods to identify a biome and",
				"place modded blocks, items and mobs in it.", "Example: HOT, DRY, SANDY, OVERWORLD",
				"TemplateForBiome biomes inherit these from the targeted biomes.");
	
			writer.putSetting(BiomeStandardValues.BIOME_CATEGORY, this.settings.biomeCategory,
				"Set a category for this biome, used by vanilla for... something",
				"Accepts one of the following values:",
				"none, taiga, extreme_hills, jungle, mesa, plains, savanna, icy, the_end, beach, forest, ocean, desert, river, swamp, mushroom, nether",
				"TemplateForBiome biomes inherits this from the targeted biomes.");
		}

		writer.header1("Biome placement");

		writer.putSetting(BiomeStandardValues.BIOME_SIZE, this.settings.biomeSize,
			"Biome size from 0 to GenerationDepth. Defines in which biome layer this biome will be generated (see GenerationDepth).",
			"Higher numbers result in a smaller biome, lower numbers a larger biome.",
			"How this setting is used depends on the value of BiomeMode in the WorldConfig.",
			"It will be used for:",
			"- normal biomes, ice biomes, isle biomes and border biomes when BiomeMode is set to NoGroups",
			"- biomes spawned as part of a BiomeGroup when BiomeMode is set to Normal.",
			"  For biomes spawned as isles, borders or rivers other settings are available.",
			"  Isle biomes:	" + BiomeStandardValues.BIOME_SIZE_WHEN_ISLE + " (see below)",
			"  Border biomes: " + BiomeStandardValues.BIOME_SIZE_WHEN_BORDER + " (see below)",
			"  River biomes:  " + WorldStandardValues.RIVER_SIZE + " (see WorldConfig)");

		writer.putSetting(BiomeStandardValues.BIOME_RARITY, this.settings.biomeRarity,
			"Biome rarity from 100 to 1. If this is normal or ice biome - chance for spawn this biome then others.",
			"Example for normal biome :",
			"  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).",
			"  50 rarity mean 1/11 chance than other",
			"For isle biomes see the " + BiomeStandardValues.BIOME_RARITY_WHEN_ISLE + " setting below.",
			"Doesn`t work on Ocean and River (frozen versions too) biomes when not added as normal biome.");

		writer.putSetting(BiomeStandardValues.BIOME_COLOR, this.settings.biomeColor,
			"The hexadecimal color value of this biome. Used in the output of the /otg map command,",
			"and used in the input of BiomeMode: FromImage.");

		writer.header2("Isle biomes", "To spawn a biome as an isle, first add it to the",
			WorldStandardValues.ISLE_BIOMES + " list in the WorldConfig.", "");

		writer.putSetting(BiomeStandardValues.ISLE_IN_BIOME, this.settings.isleInBiome,
			"List of biomes in which this biome will spawn as an isle.",
			"For example, Mushroom Isles spawn inside the Ocean biome.");

		writer.putSetting(BiomeStandardValues.BIOME_SIZE_WHEN_ISLE, this.settings.biomeSizeWhenIsle,
			"Size of this biome when spawned as an isle biome in BiomeMode: Normal.",
			"Valid values range from 0 to GenerationDepth.",
			"Larger numbers give *smaller* islands. The biome must be smaller than the biome it's going",
			"to spawn in, so the " + BiomeStandardValues.BIOME_SIZE_WHEN_ISLE + " number must be larger than the "
					+ BiomeStandardValues.BIOME_SIZE + " of the other biome.");

		writer.putSetting(BiomeStandardValues.BIOME_RARITY_WHEN_ISLE, this.settings.biomeRarityWhenIsle,
			"Rarity of this biome when spawned as an isle biome in BiomeMode: Normal.");

		writer.smallTitle("Border biomes", "To spawn a biome as a border, first add it to the",
			WorldStandardValues.BORDER_BIOMES + " list in the WorldConfig.", "");

		writer.putSetting(BiomeStandardValues.BIOME_IS_BORDER, this.settings.biomeIsBorder,
			"List of biomes this biome can be a border of.",
			"For example, the Beach biome is a border on the Ocean biome, so",
			"it can spawn anywhere on the border of an ocean.");

		writer.putSetting(BiomeStandardValues.ONLY_BORDER_NEAR, this.settings.onlyBorderNear,
			"Whitelist of neighouring biomes that allow this border biome to spawn.");

		writer.putSetting(BiomeStandardValues.NOT_BORDER_NEAR, this.settings.notBorderNear,
			"Blacklist of neighbouring biomes that do not allow this border biome to spawn.",
			"For example, the Beach biome will never spawn next to an Extreme Hills biome.",
			"Only used when OnlyBorderNear is empty / not used.");

		writer.putSetting(BiomeStandardValues.BIOME_SIZE_WHEN_BORDER, this.settings.biomeSizeWhenBorder,
			"Size of this biome when spawned as a border biome in BiomeMode: Normal.",
			"Valid values range from 0 to GenerationDepth.",
			"Larger numbers give *smaller* borders. The biome must be smaller than the biome it's going",
			"to spawn in, so the " + BiomeStandardValues.BIOME_SIZE_WHEN_BORDER + " number must be larger than the "
			+ BiomeStandardValues.BIOME_SIZE + " of the other biome.");

		writer.header1("Terrain height and volatility");

		writer.putSetting(BiomeStandardValues.BIOME_HEIGHT, this.settings.biomeHeight,
			"BiomeHeight defines how much height will be added during terrain generation",
			"Must be between -10.0 and 10.0",
			"Value 0.0 is equivalent to half of map height with all other settings at defaults.");

		writer.putSetting(BiomeStandardValues.BIOME_VOLATILITY, this.settings.biomeVolatility, "Biome volatility.");

		writer.putSetting(BiomeStandardValues.SMOOTH_RADIUS, this.settings.smoothRadius,
			"Smooth radius between biomes. Must be between 0 and 32, inclusive. The resulting",
			"smooth radius seems to be  (thisSmoothRadius + 1 + smoothRadiusOfBiomeOnOtherSide) * 4 .",
			"So if two biomes next to each other have both a smooth radius of 2, the",
			"resulting smooth area will be (2 + 1 + 2) * 4 = 20 blocks wide.");

		writer.putSetting(BiomeStandardValues.CUSTOM_HEIGHT_CONTROL_SMOOTH_RADIUS, this.settings.CHCSmoothRadius,
			"Works the same way as SmoothRadius but only acts on CustomHeightControl. Must be between 0 and 32, inclusive.",
			"Does nothing if Custom Height Control smoothing is not enabled in the world config.");

		writer.putSetting(BiomeStandardValues.MAX_AVERAGE_HEIGHT, this.settings.maxAverageHeight,
			"If this value is greater than 0, then it will affect how much, on average, the terrain will rise before leveling off when it begins to increase in elevation.",
			"If the value is less than 0, then it will cause the terrain to either increase to a lower height before leveling out or decrease in height if the value is a large enough negative.");

		writer.putSetting(BiomeStandardValues.MAX_AVERAGE_DEPTH, this.settings.maxAverageDepth,
			"If this value is greater than 0, then it will affect how much, on average, the terrain (usually at the ottom of the ocean) will fall before leveling off when it begins to decrease in elevation. ",
			"If the value is less than 0, then it will cause the terrain to either fall to a lesser depth before leveling out or increase in height if the value is a large enough negative.");

		writer.putSetting(BiomeStandardValues.VOLATILITY_1, this.privateSettings.volatilityRaw1,
			"Another type of noise. This noise is independent from biomes. The larger the values the more chaotic/volatile landscape generation becomes.",
			"Setting the values to negative will have the opposite effect and make landscape generation calmer/gentler.");

		writer.putSetting(BiomeStandardValues.VOLATILITY_2, this.privateSettings.volatilityRaw2);

		writer.putSetting(BiomeStandardValues.VOLATILITY_WEIGHT_1, this.privateSettings.volatilityWeightRaw1,
			"Adjust the weight of the corresponding volatility settings. This allows you to change how prevalent you want either of the volatility settings to be in the terrain.");

		writer.putSetting(BiomeStandardValues.VOLATILITY_WEIGHT_2, this.privateSettings.volatilityWeightRaw2);

		writer.putSetting(BiomeStandardValues.DISABLE_BIOME_HEIGHT, this.settings.disableBiomeHeight,
			"Disable all noises except Volatility1 and Volatility2. Also disable default block chance from height.");

		writer.putSetting(BiomeStandardValues.CUSTOM_HEIGHT_CONTROL, this.settings.chcData,
			"List of custom height factors, 17 double entries, each controls about 7",
			"blocks height, starting at the bottom of the world. Positive entry - larger chance of spawn blocks, negative - smaller",
			"Values which affect your configuration may be found only experimentally. Values may be very big, like ~3000.0 depends from height",
			"Example:",
			"  CustomHeightControl:0.0,-2500.0,0.0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0",
			"Makes empty layer above bedrock layer. ");

		writer.header1("Rivers");

		writer.putSetting(BiomeStandardValues.RIVER_BIOME, this.settings.riverBiome, "The biome used as the river biome.");

		writer.header1("Blocks");
		
		if(!isTemplateBiome)
		{
			writer.putSetting(BiomeStandardValues.STONE_BLOCK, this.settings.stoneBlock,
				"The stone block used for the biome, usually STONE.");
	
			writer.putSetting(BiomeStandardValues.SURFACE_BLOCK, this.settings.surfaceBlock,
				"The surface block used for the biome, usually GRASS.");
	
			writer.putSetting(BiomeStandardValues.GROUND_BLOCK, this.settings.groundBlock,
				"The ground block used for the biome, usually DIRT.");
			
			writer.putSetting(BiomeStandardValues.UNDER_WATER_SURFACE_BLOCK, this.settings.underWaterSurfaceBlock,
				"The surface block used for the biome when underwater, usually the same as GroundBlock.");
		}

		writer.putSetting(SurfaceGeneratorSetting.SURFACE_AND_GROUND_CONTROL, this.settings.surfaceAndGroundControl,
			"Setting for biomes with more complex surface and ground blocks.",
			"Each column in the world has a noise value from what appears to be -7 to 7.",
			"Values near 0 are more common than values near -7 and 7. This setting is",
			"used to change the surface block based on the noise value for the column.",
			"1.12.2 Syntax: SurfaceBlockName,GroundBlockName,MaxNoise[,AnotherSurfaceBlockName,AnotherGroundBlockName,MaxNoise][,...]",
			"Example: " + SurfaceGeneratorSetting.SURFACE_AND_GROUND_CONTROL + ": STONE,STONE,-0.8,GRAVEL,STONE,0.0,DIRT,DIRT,10.0",
			"1.16.x Syntax: SurfaceBlockName,UnderWaterSurfaceBlockName,GroundBlockName,MaxNoise,[AnotherSurfaceBlockName,AnotherUnderWaterSurfaceBlockName,AnotherGroundBlockName,MaxNoise[,...]]",
			"  When the noise is below -0.8, stone is the surface and ground block, between -0.8 and 0",
			"  gravel with stone just below and between 0.0 and 10.0 there's only dirt.",
			"  Because 10.0 is higher than the noise can ever get, the normal " + BiomeStandardValues.SURFACE_BLOCK,
			"  and " + BiomeStandardValues.GROUND_BLOCK + " will never appear in this biome.", "",
			"Alternatively, you can use Mesa, MesaForest or MesaBryce to get blocks",
			"like the blocks found in the Mesa biomes.",
			"You can also use Iceberg to get iceberg generation like in vanilla frozen oceans. Iceberg accepts a normal SAGC string: \"Iceberg <SAGC>\", so you can use normal SAGC with it.");

		writer.putSetting(BiomeStandardValues.REPLACED_BLOCKS, this.settings.replacedBlocks,
			"Replace Variable: (blockFrom,blockTo[:blockDataTo][,minHeight,maxHeight])", "Example :",
			"  ReplacedBlocks: (GRASS,DIRT,100,127),(GRAVEL,GLASS)",
			"Replace grass block to dirt from 100 to 127 height and replace gravel to glass on all height ",
			"Only the following biome resources are affected: CustomObject, CustomStructure, Ore, UnderWaterOre, ",
			"Vein, SurfacePatch, Boulder, IceSpike.",
			"BO's used as CustomObject/CustomStructure may have DoReplaceBlocks:false to save performance.");

		writer.header2("Water / Lava & Frozen States");

		writer.putSetting(BiomeStandardValues.USE_WORLD_WATER_LEVEL, this.settings.useWorldWaterLevel,
			"Set this to false to use the \"Water / Lava & Frozen States\" settings of this biome.");

		writer.putSetting(BiomeStandardValues.WATER_LEVEL_MAX, this.privateSettings.configWaterLevelMax,
			"Set water level. Every empty between this levels will be fill water or another block from WaterBlock.");

		writer.putSetting(BiomeStandardValues.WATER_LEVEL_MIN, this.privateSettings.configWaterLevelMin);

		writer.putSetting(BiomeStandardValues.WATER_BLOCK, this.privateSettings.configWaterBlock,
			"The block used when placing water in the biome.");

		writer.putSetting(BiomeStandardValues.ICE_BLOCK, this.privateSettings.configIceBlock,
			"The block used as ice. Ice only spawns if the BiomeTemperature is low enough.");

		writer.putSetting(BiomeStandardValues.PACKED_ICE_BLOCK, this.settings.packedIceBlock,
			"The block used as packed ice. Packed ice only spawns when using Iceberg SurfaceAndGroundControl.");
		
		writer.putSetting(BiomeStandardValues.SNOW_BLOCK, this.settings.snowBlock,
			"The block used as snow (block, not tile). Snow blocks only spawn when using Iceberg SurfaceAndGroundControl.");		
	
		writer.putSetting(WorldStandardValues.COOLED_LAVA_BLOCK, this.settings.cooledLavaBlock,
			"The block used as cooled or frozen lava.",
			"Set this to OBSIDIAN for \"frozen\" lava lakes in cold biomes");

		writer.header1("Visuals and weather");

		if(!isTemplateBiome)
		{
			writer.putSetting(BiomeStandardValues.BIOME_TEMPERATURE, this.settings.biomeTemperature,
				"Biome temperature. Float value from 0.0 to 2.0.",
				"When this value is around 0.2, snow will fall on mountain peaks above y=90.",
				"When this value is around 0.1, the whole biome will be covered in snow and ice.",
				"However, on default biomes, this won't do anything except changing the grass and leaves colors slightly.");
	
			writer.putSetting(BiomeStandardValues.USE_FROZEN_OCEAN_TEMPERATURE, this.settings.useFrozenOceanTemperature,
				"Set this to true to use variable temperatures within the biome based on noise.",
				"Used for vanilla Frozen Ocean and Deep Frozen Ocean biomes to create patches of water/ice.");
			
			writer.putSetting(BiomeStandardValues.BIOME_WETNESS, this.settings.biomeWetness,
				"Biome wetness. Float value from 0.0 to 1.0.",
				"If this biome is a custom biome, and this value is set to 0, no rain will fall.",
				"On default biomes, this won't do anything except changing the grass and leaves colors slightly.");
	
			writer.putSetting(BiomeStandardValues.SKY_COLOR, this.settings.skyColor, "Biome sky color.");
	
			writer.putSetting(BiomeStandardValues.WATER_COLOR, this.settings.waterColor, "Biome water color.");
			
			writer.putSetting(BiomeStandardValues.WATER_COLOR_CONTROL, this.settings.waterColorControl,
				"Setting for biomes with more complex colors.",
				"Each column in the world has a noise value from what appears to be -1 to 1.",
				"Values near 0 are more common than values near -1 and 1. This setting is",
				"used to change the water color based on the noise value for the column.",
				"Syntax: Color,MaxNoise,[AnotherColor,MaxNoise[,...]]",
				"Example: " + BiomeStandardValues.WATER_COLOR_CONTROL + ": #FFFFFF,-0.8,#000000,0.0",
				"  When the noise is below -0.8, the water will be white, between -0.8 and 0",
				"  the water will be black, and above 0 the water will be the normal " + BiomeStandardValues.WATER_COLOR + ".");
	
			writer.putSetting(BiomeStandardValues.GRASS_COLOR, this.settings.grassColor, "Biome grass color.");
			
			writer.putSetting(BiomeStandardValues.GRASS_COLOR_CONTROL, this.settings.grassColorControl, "Biome grass color control. See " + BiomeStandardValues.WATER_COLOR_CONTROL + ".");
	
			writer.putSetting(BiomeStandardValues.GRASS_COLOR_MODIFIER, this.settings.grassColorModifier,
				"Biome grass color modifier, can be None, Swamp or DarkForest.");
	
			writer.putSetting(BiomeStandardValues.FOLIAGE_COLOR, this.settings.foliageColor, "Biome foliage color.");
			
			writer.putSetting(BiomeStandardValues.FOLIAGE_COLOR_CONTROL, this.settings.foliageColorControl, "Biome foliage color control. See " + BiomeStandardValues.WATER_COLOR_CONTROL + ".");
	
			writer.putSetting(BiomeStandardValues.FOG_COLOR, this.settings.fogColor, "Biome fog color.");

			writer.putSetting(BiomeStandardValues.FOG_DENSITY, this.settings.fogDensity, "Biome fog density, from 0.0 to 1.0. 0 will mimic vanilla fog density.");
	
			writer.putSetting(BiomeStandardValues.WATER_FOG_COLOR, this.settings.waterFogColor, "Biome water fog color.");
	
			writer.putSetting(BiomeStandardValues.PARTICLE_TYPE, this.settings.particleType,
				"Biome particle type, for example minecraft:white_ash.",
				"Use the \"otg particles\" console command to get a list of particles.");
	
			writer.putSetting(BiomeStandardValues.PARTICLE_PROBABILITY, this.settings.particleProbability,
				"Biome particle probability, 0 by default.", "*TODO: Test different values and document usage.");
	
			writer.putSetting(BiomeStandardValues.MUSIC, this.settings.music,
				"Music for the biome, takes a resource location. Leave empty to disable. Examples: ",
				"	Music: minecraft:music_disc.cat", "	Music: minecraft:music.nether.basalt_deltas");
	
			writer.putSetting(BiomeStandardValues.MUSIC_MIN_DELAY, this.settings.musicMinDelay,
				"Minimum delay for music to start, in ticks");
	
			writer.putSetting(BiomeStandardValues.MUSIC_MAX_DELAY, this.settings.musicMaxDelay,
				"Maximum delay for music to start, in ticks");
	
			writer.putSetting(BiomeStandardValues.REPLACE_CURRENT_MUSIC, this.settings.replaceCurrentMusic,
				"Whether music replaces the current playing music in the client or not");
	
			writer.putSetting(BiomeStandardValues.AMBIENT_SOUND, this.settings.ambientSound,
				"Ambient sound for the biome. Leave empty to disable. Example:",
				"	AmbientSound: minecraft:ambient.cave");
	
			writer.putSetting(BiomeStandardValues.MOOD_SOUND, this.settings.moodSound,
				"Mood sound for the biome. Leave empty to disable. Example:",
				"	MoodSound: minecraft:ambient.crimson_forest.mood");
	
			writer.putSetting(BiomeStandardValues.MOOD_SOUND_DELAY, this.settings.moodSoundDelay,
				"The delay in ticks between triggering mood sound");
	
			writer.putSetting(BiomeStandardValues.MOOD_SEARCH_RANGE, this.settings.moodSearchRange,
				"How far from the player a mood sound can play");
	
			writer.putSetting(BiomeStandardValues.MOOD_OFFSET, this.settings.moodOffset, "The offset of the sound event");
	
			writer.putSetting(BiomeStandardValues.ADDITIONS_SOUND, this.settings.additionsSound,
				"Additions sound for the biome. Leave empty to disable. Example:",
				"	AdditionsSound: minecraft:ambient.soul_sand_valley.additions");
	
			writer.putSetting(BiomeStandardValues.ADDITIONS_TICK_CHANCE, this.settings.additionsTickChance,
				"The tick chance that the additions sound plays");
		}

		writer.header1("Resource queue", "This section controls all resources spawning during decoration.",
			"The resources will be placed in this order.", "",
			"Keep in mind that a high size, frequency or rarity may slow down terrain generation.", "",
			"Possible resources:", "AboveWaterRes(BlockName,Frequency,Rarity)",
			"Boulder(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..]",
			"Cactus(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
			"CustomObject(Object[,AnotherObject[,...]])",
			"CustomStructure([Object,Object_Chance[,AnotherObject,Object_Chance[,...]]])",
			"Dungeon(Rarity,MinAltitude,MaxAltitude)", "Fossil(Rarity,MinAltitude,MaxAltitude)",
			"Grass(PlantType,Grouped/NotGrouped,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])",
			"IceSpike(BlockName,IceSpikeType,Frequency,Rarity,MinAltitude,MaxAltitude,Blocksource[,BlockSource2,...])",
			"Liquid(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
			"Ore(BlockName,Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....],ExtendedParams,MaxSpawn)",
			"Plant(PlantType,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
			"Reed(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
			"SmallLake(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude)",
			"SurfacePatch(BlockName,DecorationBlockName,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
			"Tree(Frequency,TreeType,TreeTypeChance[,AdditionalTreeType,AdditionalTreeTypeChance.....],ExtendedParams,MaxSpawn)",
			"UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)",
			"UnderWaterOre(BlockName,Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])",
			"UnderWaterPlant(PlantType,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
			"Vein(BlockName,MinRadius,MaxRadius,Rarity,OreSize,OreFrequency,OreRarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])",
			"Vines(Frequency,Rarity,MinAltitude,MaxAltitude)",
			"Well(BaseBlockName,HalfSlabBlockName,WaterBlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])",
			"Bamboo(Frequency,Rarity,PodzolChance,BlockSource[,BlockSource2,BlockSource3.....])",
			"SeaGrass(Frequency,Rarity,TallChance)", 
			"Kelp(Frequency,Rarity)",
			"SeaPickle(Frequency,Rarity,Attempts)", 
			"Registry(RegistryKey,DecorationStage)", 
			"CoralMushroom(Frequency,Rarity)",
			"CoralTree(Frequency,Rarity)", 
			"CoralClaw(Frequency,Rarity)", 
			"Iceberg(BlockName1,BlockName2,Chance[,AdditionalBlockName1,AdditionalBlockName2,AdditionalChance.....],TotalChance)", 
			"BasaltColumn(BlockName,Frequency,Rarity,BaseSize,SizeVariance,BaseHeight,HeightVariance,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])", "",
			"BlockName:	  	The name of the block, can include data.",
			"BlockSource:	List of blocks the resource can spawn on/in. You can also use \"Solid\" or \"All\".",
			"Frequency:	  	Number of attempts to place this resource in each chunk.",
			"Rarity:		Chance for each attempt, Rarity:100 - mean 100% to pass, Rarity:1 - mean 1% to pass.",
			"MinAltitude and MaxAltitude: Height limits.", "TallChance:	Number between 0.0 and 1.0",
			"TreeType:		Tree (original oak tree) - BigTree - Birch - TallBirch - SwampTree -",
			"				HugeMushroom (randomly red or brown) - HugeRedMushroom - HugeBrownMushroom -",
			"				Taiga1 - Taiga2 - HugeTaiga1 - HugeTaiga2 -",
			"				JungleTree (the huge jungle tree) - GroundBush - CocoaTree (smaller jungle tree)",
			"				DarkOak (from the roofed forest biome) - Acacia",
			"				New for 1.16.5: CrimsonFungi, WarpedFungi, ChorusPlant.",
			"				You can also use your own custom objects, as long as they have Tree:true in their settings.",
			"TreeTypeChance: Similar to Rarity. Example:",
			"				Tree(10,Taiga1,35,Taiga2,100) - tries 10 times, for each attempt it tries to place Taiga1 (35% chance),",
			"				if that fails, it attempts to place Taiga2 (100% chance).",
			"PlantType:	  	One of the plant types: " + StringHelper.join(PlantType.values(), ", "),
			"				or a block name",
			"IceSpikeType:  One of the ice spike types: " + StringHelper.join(IceSpikeType.values(), ","),
			"Object:		Any custom object (bo2 or bo3) file but without the file extension. ",
			"RegistryKey:	Registry key for a (non-OTG) default or configured feature. For example: minecraft:plain_vegetation",
			"DecorationStage: Optional, one of the vanilla decoration stages.",
			"               Can be: RAW_GENERATION, LAKES, LOCAL_MODIFICATIONS, UNDERGROUND_STRUCTURES, ",
			"				SURFACE_STRUCTURES, STRONGHOLDS, UNDERGROUND_ORES, UNDERGROUND_DECORATION,",
			"				VEGETAL_DECORATION, TOP_LAYER_MODIFICATION. VEGETAL_DECORATION by default.",
			"ExtendedParams: Optional, set this to true if you want to use additional parameters, like MaxSpawn.",
			"MaxSpawn: 		Optional, used with Frequency. When MaxSpawn spawn attempts have succeeded, stop spawning (for the current chunk).",
			"				For example, you can do 100 spawn attempts per chunk but stop after 5 have succeeded.",
			"",
			"Plant and Grass resource: Both a resource of one block. Plant can place blocks underground, Grass cannot.",
			"UnderWaterPlant resource: Similar to plant, but places blocks underwater.",
			"Liquid resource: A one-block water or lava source",
			"SmallLake and UnderGroundLake resources: Small lakes of about 8x8 blocks",
			"Vein resource: Starts an area where ores will spawn. Can be slow, so use a low Rarity (smaller than 1).",
			"CustomStructure resource: Starts a BO3 or BO4 structure in the chunk if spawn requirements are met.",
			"");
		writer.addConfigFunctions(this.settings.resourceQueue);

		writer.header1("Sapling resource",
			Constants.MOD_ID + " allows you to grow your custom objects from saplings, instead",
			"of the vanilla trees. Add one or more Sapling functions here to override vanilla",
			"spawning for that sapling.", "",
			"The syntax is: Sapling(SaplingType,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])",
			"Works like Tree resource instead first parameter.",
			"For custom saplings; Sapling(Custom,SaplingMaterial,WideTrunk,TreeType,TreeType_Chance.....)",
			"SaplingMaterial is the name of the sapling block.",
			"WideTrunk is 'true' or 'false', whether or not it requires 4 saplings.", "",
			"TreeType is one of the vanilla tree types (see resource queue comments) or a BO2/BO3 name.", "",
			"Sapling types: " + StringHelper.join(SaplingType.values(), ", "),
			"All - will make the tree spawn from all saplings, but not from mushrooms.",
			"BigJungle - for when 4 jungle saplings grow at once.",
			"RedMushroom/BrownMushroom - will only grow when bonemeal is used.", "");

		writer.addConfigFunctions(this.settings.saplingGrowers.values());
		writer.addConfigFunctions(this.settings.customSaplingGrowers.values());
		writer.addConfigFunctions(this.settings.customBigSaplingGrowers.values());

		if(!isTemplateBiome)
		{
			writer.header1("Vanilla structures", "Vanilla structure settings, each structure type has a global on/off",
				"toggle in the WorldConfig, be sure to enable it to allow biomes to", "spawn structures.",
				"* Fossils and Dungeons count as resources, not structures.");
	
			writer.putSetting(BiomeStandardValues.STRONGHOLDS_ENABLED, this.settings.strongholdsEnabled,
				"Toggles strongholds spawning in this biome.");
	
			writer.putSetting(BiomeStandardValues.WOODLAND_MANSIONS_ENABLED, this.settings.woodLandMansionsEnabled,
				"Toggles woodland mansions spawning in this biome.");
	
			writer.putSetting(BiomeStandardValues.OCEAN_MONUMENTS_ENABLED, this.settings.oceanMonumentsEnabled,
				"Toggles ocean monuments spawning in this biome.");
	
			writer.putSetting(BiomeStandardValues.NETHER_FORTRESSES_ENABLED, this.settings.netherFortressesEnabled,
				"Toggles nether fortresses spawning in this biome.");
	
			writer.putSetting(BiomeStandardValues.VILLAGE_TYPE, this.settings.villageType,
				"The type of villages in this biome. Can be wood, sandstone, taiga, savanna, snowy or disabled.");
	
			writer.putSetting(BiomeStandardValues.VILLAGE_SIZE, this.settings.villageSize,
				"The size of villages in this biome, 6 by default.",
				"*TODO: Test different values and document usage.");
	
			writer.putSetting(BiomeStandardValues.MINESHAFT_TYPE, this.settings.mineshaftType,
				"The type of mineshafts in this biome. Can be normal, mesa or disabled.");
	
			writer.putSetting(BiomeStandardValues.MINESHAFT_PROBABILITY, this.settings.mineshaftProbability,
				"Probability of mineshafts spawning, 0.004 by default.",
				"*TODO: Test different values and document usage.");
	
			writer.putSetting(BiomeStandardValues.RARE_BUILDING_TYPE, this.settings.rareBuildingType,
				"The type of the aboveground rare building in this biome.",
				"Can be desertPyramid, jungleTemple, swampHut, igloo or disabled.");
	
			writer.putSetting(BiomeStandardValues.BURIED_TREASURE_ENABLED, this.settings.buriedTreasureEnabled,
				"Toggles buried treasure spawning in this biome.");
	
			writer.putSetting(BiomeStandardValues.BURIED_TREASURE_PROBABILITY, this.settings.buriedTreasureProbability,
				"Probability of buried treasure spawning, 0.01 by default.",
				"*TODO: Test different values and document usage.");
	
			writer.putSetting(BiomeStandardValues.SHIP_WRECK_ENABLED, this.settings.shipWreckEnabled,
				"Toggles shipwrecks spawning in this biome.");
	
			writer.putSetting(BiomeStandardValues.SHIP_WRECK_BEACHED_ENABLED, this.settings.shipWreckBeachedEnabled,
				"Toggles beached shipwrecks spawning in this biome.");
	
			writer.putSetting(BiomeStandardValues.PILLAGER_OUTPOST_ENABLED, this.settings.pillagerOutpostEnabled,
				"Toggles pillager outposts spawning in this biome.");
	
			writer.putSetting(BiomeStandardValues.PILLAGER_OUTPOST_SIZE, this.settings.pillagerOutpostSize,
				"The size of pillager outposts in this biome, 7 by default.",
				"*TODO: Test different values and document usage.");
	
			writer.putSetting(BiomeStandardValues.BASTION_REMNANT_ENABLED, this.settings.bastionRemnantEnabled,
				"Toggles bastion remnants spawning in this biome.");
	
			writer.putSetting(BiomeStandardValues.BASTION_REMNANT_SIZE, this.settings.bastionRemnantSize,
				"The size of bastion remnants in this biome, 6 by default.",
				"*TODO: Test different values and document usage.");
	
			writer.putSetting(BiomeStandardValues.NETHER_FOSSIL_ENABLED, this.settings.netherFossilEnabled,
				"Toggles nether fossils spawning in this biome.", "Caution: Nether fossils spawn at all heights.");
	
			writer.putSetting(BiomeStandardValues.END_CITY_ENABLED, this.settings.endCityEnabled,
				"Toggles end cities spawning in this biome.");
	
			writer.putSetting(BiomeStandardValues.RUINED_PORTAL_TYPE, this.settings.ruinedPortalType,
				"The type of ruined portals in this biome.",
				"Can be normal, desert, jungle, swamp, mountain, ocean, nether or disabled.");
	
			writer.putSetting(BiomeStandardValues.OCEAN_RUINS_TYPE, this.settings.oceanRuinsType,
				"The type of ocean ruins in this biome.", "Can be cold, warm or disabled.");
	
			writer.putSetting(BiomeStandardValues.OCEAN_RUINS_LARGE_PROBABILITY, this.settings.oceanRuinsLargeProbability,
				"Probability of large ocean ruins spawning, 0.3 by default.",
				"*TODO: Test different values and document usage.");
	
			writer.putSetting(BiomeStandardValues.OCEAN_RUINS_CLUSTER_PROBABILITY, this.settings.oceanRuinsClusterProbability,
				"Probability of ocean ruins spawning clusters, 0.9 by default.",
				"*TODO: Test different values and document usage.");
	
			writer.header1("Mob spawning",
				"Mob spawning is configured via mob groups, see http://minecraft.gamepedia.com/Spawn#Mob_spawning", "",
				"A mobgroups is made of four parts; mob name, weight, min and max.",
				"- Mob name is one of the Minecraft internal mob names. See http://minecraft.gamepedia.com/Chunk_format#Mobs",
				"- Weight is used for a random selection. Must be a positive number.",
				"- Min is the minimum amount of mobs spawning as a group. Must be a positive number.",
				"- Max is the maximum amount of mobs spawning as a group. Must be a positive number.", "",
				"Mob groups are written to the config files as Json.",
				"Json is a tree document format: http://en.wikipedia.org/wiki/JSON",
				"Syntax: {\"mob\": \"mobname\", \"weight\": integer, \"min\": integer, \"max\": integer}",
				"Example: {\"mob\": \"minecraft:ocelot\", \"weight\": 10, \"min\": 2, \"max\": 6}",
				"Example: {\"mob\": \"minecraft:mooshroom\", \"weight\": 5, \"min\": 2, \"max\": 2}",
				"A json list of mobgroups looks like this: [ mobgroup, mobgroup, mobgroup... ]",
				"This would be an ampty list: []", "You can validate your json here: http://jsonlint.com/", "",
				"There are six categories of mobs: monsters, creatures, water creatures, ambient creatures, water ambient creatures and miscellaneous.",
				"You can add your own mobs to the mobgroups below, mobs may only work when used in specific categories, depending on their type.",
				"To see the mob category a mob belongs to, use /otg entities. The mob's category (if any) is listed after its name.",
				"Also supports modded mobs, if they are of the correct mob category.");
	
			writer.putSetting(BiomeStandardValues.SPAWN_MONSTERS, this.privateSettings.spawnMonsters,
				"The monsters (blazes, cave spiders, creepers, drowned, elder guardians, ender dragons, endermen, endermites, evokers, ghasts, giants,",
				"guardians, hoglins, husks, illusioners, magma cubes, phantoms, piglins, pillagers, ravagers, shulkers, silverfishes, skeletons, slimes,",
				"spiders, strays, vexes, vindicators, witches, zoglins, zombies, zombie villagers, zombified piglins) that spawn in this biome.",
				"For instance [{\"mob\": \"minecraft:spider\", \"weight\": 100, \"min\": 4, \"max\": 4}, {\"mob\": \"minecraft:zombie\", \"weight\": 100, \"min\": 4, \"max\": 4}]",
				"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
				"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome.");
	
			writer.putSetting(BiomeStandardValues.SPAWN_CREATURES, this.privateSettings.spawnCreatures,
				"The friendly creatures (bees, cats, chickens, cows, donkeys, foxes, horses, llama, mooshrooms, mules, ocelots, panda's, parrots,",
				"pigs, polar bears, rabbits, sheep, skeleton horses, striders, trader llama's, turtles, wandering traders, wolves, zombie horses)",
				"that spawn in this biome.",
				"For instance [{\"mob\": \"minecraft:sheep\", \"weight\": 12, \"min\": 4, \"max\": 4}, {\"mob\": \"minecraft:pig\", \"weight\": 10, \"min\": 4, \"max\": 4}]",
				"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
				"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome.");
	
			writer.putSetting(BiomeStandardValues.SPAWN_WATER_CREATURES, this.privateSettings.spawnWaterCreatures,
				"The water creatures (squids and dolphins) that spawn in this biome",
				"For instance [{\"mob\": \"minecraft:squid\", \"weight\": 10, \"min\": 4, \"max\": 4}]",
				"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
				"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome.");
	
			writer.putSetting(BiomeStandardValues.SPAWN_AMBIENT_CREATURES, this.privateSettings.spawnAmbientCreatures,
				"The ambient creatures (only bats in vanila) that spawn in this biome",
				"For instance [{\"mob\": \"minecraft:bat\", \"weight\": 10, \"min\": 8, \"max\": 8}]",
				"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
				"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome.");
	
			writer.putSetting(BiomeStandardValues.SPAWN_WATER_AMBIENT_CREATURES, this.privateSettings.spawnWaterAmbientCreatures,
				"The ambient water creatures (cod, pufferfish, salmon, tropical fish) that spawn in this biome",
				"For instance [{\"mob\": \"minecraft:cod\", \"weight\": 10, \"min\": 8, \"max\": 8}]",
				"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
				"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome.");
	
			writer.putSetting(BiomeStandardValues.SPAWN_MISC_CREATURES, this.privateSettings.spawnMiscCreatures,
				"The miscellaneous creatures (iron golems, snow golems and villagers) that spawn in this biome",
				"For instance [{\"mob\": \"minecraft:villager\", \"weight\": 10, \"min\": 8, \"max\": 8}]",
				"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
				"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome.");
	
			writer.putSetting(BiomeStandardValues.INHERIT_MOBS_BIOME_NAME, this.settings.inheritMobsBiomeName,
				"Inherit the internal mobs list of another biome. Inherited mobs can be overridden using",
				"the mob spawn settings in this biome config. Any mob type defined in this biome config",
				"will override inherited mob settings for the same mob in the same mob category.",
				"Use this setting to inherit mob spawn lists from other biomes.",
				"Accepts both OTG and non-OTG (vanilla or other mods') biomes. See also: BiomeDictId.");
		}
	}

	@Override
	protected void validateAndCorrectSettings(Path settingsDir, ILogger logger)
	{
		this.settings.biomeSize = lowerThanOrEqualTo(this.settings.biomeSize, this.settings.worldConfig.getGenerationDepth());
		this.settings.biomeSizeWhenIsle = lowerThanOrEqualTo(this.settings.biomeSizeWhenIsle, this.settings.worldConfig.getGenerationDepth());
		this.settings.biomeSizeWhenBorder = lowerThanOrEqualTo(this.settings.biomeSizeWhenBorder, this.settings.worldConfig.getGenerationDepth());
		this.settings.biomeRarity = lowerThanOrEqualTo(this.settings.biomeRarity, this.settings.worldConfig.getBiomeRarityScale());
		this.settings.biomeRarityWhenIsle = lowerThanOrEqualTo(this.settings.biomeRarityWhenIsle, this.settings.worldConfig.getBiomeRarityScale());
		this.settings.isleInBiome = filterBiomes(this.settings.isleInBiome, this.settings.worldConfig.getWorldBiomes());
		this.settings.biomeIsBorder = filterBiomes(this.settings.biomeIsBorder, this.settings.worldConfig.getWorldBiomes());
		this.settings.onlyBorderNear = filterBiomes(this.settings.onlyBorderNear, this.settings.worldConfig.getWorldBiomes());
		this.settings.notBorderNear = filterBiomes(this.settings.notBorderNear, this.settings.worldConfig.getWorldBiomes());
		this.settings.volatility1 = this.privateSettings.volatilityRaw1 < 0.0D ? 1.0D / (Math.abs(this.privateSettings.volatilityRaw1) + 1.0D) : this.privateSettings.volatilityRaw1 + 1.0D;
		this.settings.volatility2 = this.privateSettings.volatilityRaw2 < 0.0D ? 1.0D / (Math.abs(this.privateSettings.volatilityRaw2) + 1.0D) : this.privateSettings.volatilityRaw2 + 1.0D;
		this.settings.volatilityWeight1 = (this.privateSettings.volatilityWeightRaw1 - 0.5D) * 24.0D;
		this.settings.volatilityWeight2 = (0.5D - this.privateSettings.volatilityWeightRaw2) * 24.0D;
		this.settings.waterLevelMax = higherThanOrEqualTo(this.settings.waterLevelMax, this.settings.waterLevelMin);
	}

	@Override
	protected void renameOldSettings(SettingsMap settings, ILogger logger, IMaterialReader materialReader)
	{
		settings.renameOldSetting("DisableNotchHeightControl", BiomeStandardValues.DISABLE_BIOME_HEIGHT);
		settings.renameOldSetting("BiomeDictId", BiomeStandardValues.BIOME_DICT_TAGS);
	}
	
	@Override
	public IBiomeConfig createTemplateBiome()
	{
		BiomeConfig biomeConfig = new BiomeConfig(this.configName);
		biomeConfig.privateSettings = this.privateSettings;
		biomeConfig.settings = this.settings;
		return biomeConfig;
	}
}
