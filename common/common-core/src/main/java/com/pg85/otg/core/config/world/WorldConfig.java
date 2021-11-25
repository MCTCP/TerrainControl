package com.pg85.otg.core.config.world;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalLong;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeGroup;
import com.pg85.otg.config.biome.BiomeGroupManager;
import com.pg85.otg.config.biome.TemplateBiome;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.BiomeRegistryNames;

/**
 * WorldConfig.ini classes
 * 
 * IWorldConfig defines anything that's used/exposed between projects.
 * WorldConfigBase implements anything needed for IWorldConfig. 
 * WorldConfig contains only fields/methods used for io/serialisation/instantiation.
 * 
 * WorldConfig should be used only in common-core and platform-specific layers, when reading/writing settings on app start.
 * IWorldConfig should be used wherever settings are used in code. 
 */
public class WorldConfig extends WorldConfigBase
{
	public static final HashMap<String, Class<? extends ConfigFunction<?>>> CONFIG_FUNCTIONS = new HashMap<>();
	static
	{
		CONFIG_FUNCTIONS.put("BiomeGroup", BiomeGroup.class);
		CONFIG_FUNCTIONS.put("TemplateBiome", TemplateBiome.class);
	}

	// TODO: Not used atm, implement these.

	private boolean frozenOcean;

	// Fields used only in common-core or platform layers that aren't in IWorldConfig

	// TODO: Refactor BiomeGroups classes, since we have new biome groups now.
	// TODO: Refactor to IBiomeGroupManager and move to Base?	
	private BiomeGroupManager biomeGroupManager;
	// TODO: Refactor to ITemplateBiome and move to Base?	
	private List<TemplateBiome> templateBiomes;
	
	// Private fields, only used when reading/writing
	
	private int worldHeightScaleBits;
	private int worldHeightCapBits;
	
	public WorldConfig(Path settingsDir, SettingsMap settingsReader, ArrayList<String> biomes, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader, String presetFolderName)
	{
		super(settingsReader.getName());

		this.worldBiomes.addAll(biomes);
		this.renameOldSettings(settingsReader, logger, materialReader);
		this.readConfigSettings(settingsReader, biomeResourcesManager, logger, materialReader, presetFolderName);
		this.validateAndCorrectSettings(settingsDir, logger);		 
	}

	// TODO: Refactor to IBiomeGroupManager and move to Base?
	public BiomeGroupManager getBiomeGroupManager()
	{
		return this.biomeGroupManager;
	}

	// TODO: Refactor to ITemplateBiome and move to Base?
	public List<TemplateBiome> getTemplateBiomes()
	{
		return this.templateBiomes;
	}
	
	@Override
	protected void renameOldSettings(SettingsMap reader, ILogger logger, IMaterialReader materialReader)
	{
		// Rename BeforeGroups -> NoGroups
		if (reader.getSetting(WorldStandardValues.BIOME_MODE, logger) == BiomeMode.BeforeGroups)
		{
			reader.putSetting(WorldStandardValues.BIOME_MODE, BiomeMode.NoGroups);
		}

		// Put BiomeMode in compatibility mode when NormalBiomes is found and create default groups
		if (reader.hasSetting(WorldStandardValues.NORMAL_BIOMES))
		{
			if (reader.getSetting(WorldStandardValues.BIOME_MODE, logger) == BiomeMode.Normal)
			{
				reader.putSetting(WorldStandardValues.BIOME_MODE, BiomeMode.NoGroups);
			}
			
			int landSize = reader.getSetting(WorldStandardValues.LAND_SIZE, logger);
			int landRarity = reader.getSetting(WorldStandardValues.LAND_RARITY, logger);
			List<String> normalBiomes = reader.getSetting(WorldStandardValues.NORMAL_BIOMES, logger);

			BiomeGroup normalGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.NORMAL, landSize, landRarity, normalBiomes);

			int iceSize = reader.getSetting(WorldStandardValues.ICE_SIZE, logger);
			int iceRarity = reader.getSetting(WorldStandardValues.ICE_RARITY, logger);
			List<String> iceBiomes = reader.getSetting(WorldStandardValues.ICE_BIOMES, logger);
			BiomeGroup iceGroup = new BiomeGroup(this, WorldStandardValues.BiomeGroupNames.ICE, iceSize, iceRarity, iceBiomes);

			reader.addConfigFunctions(Arrays.asList(normalGroup, iceGroup));
		}

		// Rename old settings

		reader.renameOldSetting("SpawnPointSet", WorldStandardValues.FIXED_SPAWN_POINT);
		reader.renameOldSetting("PopulationBoundsCheck", WorldStandardValues.DECORATION_BOUNDS_CHECK);
		reader.renameOldSetting("EvenCaveDistrubution", WorldStandardValues.EVEN_CAVE_DISTRIBUTION);
		reader.renameOldSetting("WorldFog", WorldStandardValues.WORLD_FOG_COLOR);
		reader.renameOldSetting("BedrockobBlock", WorldStandardValues.BEDROCK_BLOCK);
		reader.renameOldSetting("DimensionPortalMaterials", WorldStandardValues.PORTAL_BLOCKS);		
	}

	@Override
	protected void validateAndCorrectSettings(Path settingsDir, ILogger logger)
	{
		this.landSize = lowerThanOrEqualTo(this.landSize, this.generationDepth);
		this.landFuzzy = lowerThanOrEqualTo(this.landFuzzy, this.generationDepth - this.landSize);
		this.riverRarity = lowerThanOrEqualTo(this.riverRarity, this.generationDepth);
		this.riverSize = lowerThanOrEqualTo(this.riverSize, this.generationDepth - this.riverRarity);

		this.biomeGroupManager.filterBiomes(this.worldBiomes, logger);
		this.isleBiomes = filterBiomes(this.isleBiomes, this.worldBiomes);
		this.borderBiomes = filterBiomes(this.borderBiomes, this.worldBiomes);

		if (this.biomeMode == BiomeMode.FromImage)
		{
			File mapFile = new File(settingsDir.toString(), this.imageFile);
			if (!mapFile.exists())
			{
				logger.log(LogLevel.ERROR, LogCategory.MAIN, "Biome map file not found. Switching BiomeMode to Normal");
				this.biomeMode = BiomeMode.Normal;
			}
		}
		this.imageFillBiome = (BiomeRegistryNames.Contain(this.imageFillBiome) || this.worldBiomes.contains(this.imageFillBiome)) ? this.imageFillBiome : WorldStandardValues.IMAGE_FILL_BIOME.getDefaultValue(null);

		this.caveMaxAltitude = higherThanOrEqualTo(this.caveMaxAltitude, this.caveMinAltitude);
		this.caveSystemPocketMaxSize = higherThanOrEqualTo(this.caveSystemPocketMaxSize, this.caveSystemPocketMinSize);
		this.ravineMaxAltitude = higherThanOrEqualTo(this.ravineMaxAltitude, this.ravineMinAltitude);
		this.ravineMaxLength = higherThanOrEqualTo(this.ravineMaxLength, this.ravineMinLength);
		this.waterLevelMax = higherThanOrEqualTo(this.waterLevelMax, this.waterLevelMin);
	}

	@Override
	protected void readConfigSettings(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader, String presetFolderName)
	{
		// Misc

		this.settingsMode = reader.getSetting(WorldStandardValues.SETTINGS_MODE, logger);
		this.author = reader.getSetting(WorldStandardValues.AUTHOR, logger);
		this.description = reader.getSetting(WorldStandardValues.DESCRIPTION, logger);
		this.shortPresetName = reader.getSetting(WorldStandardValues.SHORT_PRESET_NAME, logger);
		this.majorVersion = reader.getSetting(WorldStandardValues.MAJOR_VERSION, logger);
		this.minorVersion = reader.getSetting(WorldStandardValues.MINOR_VERSION, logger);

		// Visual settings

		this.worldFogColor = reader.getSetting(WorldStandardValues.WORLD_FOG_COLOR, logger);

		// Biome resources

		this.disableOreGen = reader.getSetting(WorldStandardValues.DISABLE_OREGEN, logger);
		this.disableBedrock = reader.getSetting(WorldStandardValues.DISABLE_BEDROCK, logger);

		// Blocks

		this.removeSurfaceStone = reader.getSetting(WorldStandardValues.REMOVE_SURFACE_STONE, logger);
		this.waterBlock = reader.getSetting(WorldStandardValues.WATER_BLOCK, logger, materialReader);
		this.bedrockBlock = reader.getSetting(WorldStandardValues.BEDROCK_BLOCK, logger, materialReader);		
		this.cooledLavaBlock = reader.getSetting(WorldStandardValues.COOLED_LAVA_BLOCK, logger, materialReader);
		this.iceBlock = reader.getSetting(WorldStandardValues.ICE_BLOCK, logger, materialReader);
		this.carverLavaBlock = reader.getSetting(WorldStandardValues.CARVER_LAVA_BLOCK, logger, materialReader);

		// Bedrock

		this.carverLavaBlockHeight = reader.getSetting(WorldStandardValues.CARVER_LAVA_BLOCK_HEIGHT, logger);
		this.ceilingBedrock = reader.getSetting(WorldStandardValues.CEILING_BEDROCK, logger);
		this.flatBedrock = reader.getSetting(WorldStandardValues.FLAT_BEDROCK, logger);

		// Biome settings

		this.biomeRarityScale = reader.getSetting(WorldStandardValues.BIOME_RARITY_SCALE, logger);
		this.generationDepth = reader.getSetting(WorldStandardValues.GENERATION_DEPTH, logger);
		this.oldGroupRarity = reader.getSetting(WorldStandardValues.OLD_GROUP_RARITY, logger);
		this.oldLandRarity = reader.getSetting(WorldStandardValues.OLD_LAND_RARITY, logger);
		this.landFuzzy = reader.getSetting(WorldStandardValues.LAND_FUZZY, logger);
		this.landRarity = reader.getSetting(WorldStandardValues.LAND_RARITY, logger);
		this.landSize = reader.getSetting(WorldStandardValues.LAND_SIZE, logger);
		this.forceLandAtSpawn = reader.getSetting(WorldStandardValues.FORCE_LAND_AT_SPAWN, logger);
		this.oceanBiomeSize = reader.getSetting(WorldStandardValues.OCEAN_BIOME_SIZE, logger);
		this.defaultOceanBiome = reader.getSetting(WorldStandardValues.DEFAULT_OCEAN_BIOME, logger);
		this.defaultWarmOceanBiome = reader.getSetting(WorldStandardValues.DEFAULT_WARM_OCEAN_BIOME, logger);
		this.defaultLukewarmOceanBiome = reader.getSetting(WorldStandardValues.DEFAULT_LUKEWARM_OCEAN_BIOME, logger);
		this.defaultColdOceanBiome = reader.getSetting(WorldStandardValues.DEFAULT_COLD_OCEAN_BIOME, logger);
		this.defaultFrozenOceanBiome = reader.getSetting(WorldStandardValues.DEFAULT_FROZEN_OCEAN_BIOME, logger);
		this.biomeMode = reader.getSetting(WorldStandardValues.BIOME_MODE, logger);
		this.frozenOcean = reader.getSetting(WorldStandardValues.FROZEN_OCEAN, logger);		
		this.frozenOceanTemperature = reader.getSetting(WorldStandardValues.FROZEN_OCEAN_TEMPERATURE, logger);
		this.isleBiomes = reader.getSetting(WorldStandardValues.ISLE_BIOMES, logger);
		this.borderBiomes = reader.getSetting(WorldStandardValues.BORDER_BIOMES, logger);
		this.randomRivers = reader.getSetting(WorldStandardValues.RANDOM_RIVERS, logger);
		this.riverRarity = reader.getSetting(WorldStandardValues.RIVER_RARITY, logger);
		this.riverSize = reader.getSetting(WorldStandardValues.RIVER_SIZE, logger);
		this.riversEnabled = reader.getSetting(WorldStandardValues.RIVERS_ENABLED, logger);
		this.improvedBorderDecoration = reader.getSetting(WorldStandardValues.IMPROVED_BORDER_DECORATION, logger);
		this.largeOreVeins = reader.getSetting(WorldStandardValues.LARGE_ORE_VEINS, logger);

		// BiomeGroups requires that values like genDepth are initialized
		readTemplateBiomes(reader, biomeResourcesManager, logger, materialReader);
		readBiomeGroups(reader, biomeResourcesManager, logger, materialReader);
		
		this.blackListedBiomes = reader.getSetting(WorldStandardValues.BLACKLISTED_BIOMES, logger);
		
		// Terrain settings

		this.fractureHorizontal = reader.getSetting(WorldStandardValues.FRACTURE_HORIZONTAL, logger);
		this.fractureVertical = reader.getSetting(WorldStandardValues.FRACTURE_VERTICAL, logger);
		this.worldHeightCapBits = reader.getSetting(WorldStandardValues.WORLD_HEIGHT_CAP_BITS, logger);
		this.worldHeightCap = 1 << this.worldHeightCapBits;
		this.worldHeightScaleBits = reader.getSetting(WorldStandardValues.WORLD_HEIGHT_SCALE_BITS, logger);
		this.worldHeightScaleBits = lowerThanOrEqualTo(this.worldHeightScaleBits, this.worldHeightCapBits);
		this.worldHeightScale = 1 << this.worldHeightScaleBits;
		this.betterSnowFall = reader.getSetting(WorldStandardValues.BETTER_SNOW_FALL, logger);
		this.waterLevelMax = reader.getSetting(WorldStandardValues.WATER_LEVEL_MAX, logger);
		this.waterLevelMin = reader.getSetting(WorldStandardValues.WATER_LEVEL_MIN, logger);

		// FromImageMode

		this.imageOrientation = reader.getSetting(WorldStandardValues.IMAGE_ORIENTATION, logger);		
		this.imageFile = reader.getSetting(WorldStandardValues.IMAGE_FILE, logger);
		this.imageFillBiome = reader.getSetting(WorldStandardValues.IMAGE_FILL_BIOME, logger);
		this.imageMode = reader.getSetting(WorldStandardValues.IMAGE_MODE, logger);		
		this.imageXOffset = reader.getSetting(WorldStandardValues.IMAGE_X_OFFSET, logger);
		this.imageZOffset = reader.getSetting(WorldStandardValues.IMAGE_Z_OFFSET, logger);

		// Vanilla structures

		this.villageSpacing = reader.getSetting(WorldStandardValues.VILLAGE_SPACING, logger);
		this.villageSeparation = reader.getSetting(WorldStandardValues.VILLAGE_SEPARATION, logger);
		this.desertPyramidSpacing = reader.getSetting(WorldStandardValues.DESERTPYRAMID_SPACING, logger);
		this.desertPyramidSeparation = reader.getSetting(WorldStandardValues.DESERTPYRAMID_SEPARATION, logger);
		this.iglooSpacing = reader.getSetting(WorldStandardValues.IGLOO_SPACING, logger);
		this.iglooSeparation = reader.getSetting(WorldStandardValues.IGLOO_SEPARATION, logger);
		this.jungleTempleSpacing = reader.getSetting(WorldStandardValues.JUNGLETEMPLE_SPACING, logger);
		this.jungleTempleSeparation = reader.getSetting(WorldStandardValues.JUNGLETEMPLE_SEPARATION, logger);
		this.swampHutSpacing = reader.getSetting(WorldStandardValues.SWAMPHUT_SPACING, logger);
		this.swampHutSeparation = reader.getSetting(WorldStandardValues.SWAMPHUT_SEPARATION, logger);
		this.pillagerOutpostSpacing = reader.getSetting(WorldStandardValues.PILLAGEROUTPOST_SPACING, logger);
		this.pillagerOutpostSeparation = reader.getSetting(WorldStandardValues.PILLAGEROUTPOST_SEPARATION, logger);
		this.strongholdSpacing = reader.getSetting(WorldStandardValues.STRONGHOLD_SPACING, logger);
		this.strongholdSeparation = reader.getSetting(WorldStandardValues.STRONGHOLD_SEPARATION, logger);
		this.strongholdDistance = reader.getSetting(WorldStandardValues.STRONGHOLD_DISTANCE, logger);
		this.strongholdSpread = reader.getSetting(WorldStandardValues.STRONGHOLD_SPREAD, logger);
		this.strongholdCount = reader.getSetting(WorldStandardValues.STRONGHOLD_COUNT, logger);
		this.oceanMonumentSpacing = reader.getSetting(WorldStandardValues.OCEANMONUMENT_SPACING, logger);
		this.oceanMonumentSeparation = reader.getSetting(WorldStandardValues.OCEANMONUMENT_SEPARATION, logger);
		this.endCitySpacing = reader.getSetting(WorldStandardValues.ENDCITY_SPACING, logger);
		this.endCitySeparation = reader.getSetting(WorldStandardValues.ENDCITY_SEPARATION, logger);
		this.woodlandMansionSpacing = reader.getSetting(WorldStandardValues.WOODLANDMANSION_SPACING, logger);
		this.woodlandMansionSeparation = reader.getSetting(WorldStandardValues.WOODLANDMANSION_SEPARATION, logger);
		this.buriedTreasureSpacing = reader.getSetting(WorldStandardValues.BURIEDTREASURE_SPACING, logger);
		this.buriedTreasureSeparation = reader.getSetting(WorldStandardValues.BURIEDTREASURE_SEPARATION, logger);
		this.mineshaftSpacing = reader.getSetting(WorldStandardValues.MINESHAFT_SPACING, logger);
		this.mineshaftSeparation = reader.getSetting(WorldStandardValues.MINESHAFT_SEPARATION, logger);
		this.ruinedPortalSpacing = reader.getSetting(WorldStandardValues.RUINEDPORTAL_SPACING, logger);
		this.ruinedPortalSeparation = reader.getSetting(WorldStandardValues.RUINEDPORTAL_SEPARATION, logger);
		this.shipwreckSpacing = reader.getSetting(WorldStandardValues.SHIPWRECK_SPACING, logger);
		this.shipwreckSeparation = reader.getSetting(WorldStandardValues.SHIPWRECK_SEPARATION, logger);
		this.oceanRuinSpacing = reader.getSetting(WorldStandardValues.OCEANRUIN_SPACING, logger);
		this.oceanRuinSeparation = reader.getSetting(WorldStandardValues.OCEANRUIN_SEPARATION, logger);
		this.bastionRemnantSpacing = reader.getSetting(WorldStandardValues.BASTIONREMNANT_SPACING, logger);
		this.bastionRemnantSeparation = reader.getSetting(WorldStandardValues.BASTIONREMNANT_SEPARATION, logger);
		this.netherFortressSpacing = reader.getSetting(WorldStandardValues.NETHERFORTRESS_SPACING, logger);
		this.netherFortressSeparation = reader.getSetting(WorldStandardValues.NETHERFORTRESS_SEPARATION, logger);
		this.netherFossilSpacing = reader.getSetting(WorldStandardValues.NETHERFOSSIL_SPACING, logger);
		this.netherFossilSeparation = reader.getSetting(WorldStandardValues.NETHERFOSSIL_SEPARATION, logger);
		
		this.woodlandMansionsEnabled = reader.getSetting(WorldStandardValues.WOODLAND_MANSIONS_ENABLED, logger);
		this.netherFortressesEnabled = reader.getSetting(WorldStandardValues.NETHER_FORTRESSES_ENABLED, logger);
		this.buriedTreasureEnabled = reader.getSetting(WorldStandardValues.BURIED_TREASURE_ENABLED, logger);
		this.oceanRuinsEnabled = reader.getSetting(WorldStandardValues.OCEAN_RUINS_ENABLED, logger);
		this.pillagerOutpostsEnabled = reader.getSetting(WorldStandardValues.PILLAGER_OUTPOSTS_ENABLED, logger);
		this.bastionRemnantsEnabled = reader.getSetting(WorldStandardValues.BASTION_REMNANTS_ENABLED, logger);
		this.netherFossilsEnabled = reader.getSetting(WorldStandardValues.NETHER_FOSSILS_ENABLED, logger);
		this.endCitiesEnabled = reader.getSetting(WorldStandardValues.END_CITIES_ENABLED, logger);
		this.ruinedPortalsEnabled = reader.getSetting(WorldStandardValues.RUINED_PORTALS_ENABLED, logger);
		this.shipWrecksEnabled = reader.getSetting(WorldStandardValues.SHIPWRECKS_ENABLED, logger);
		this.strongholdsEnabled = reader.getSetting(WorldStandardValues.STRONGHOLDS_ENABLED, logger);
		this.villagesEnabled = reader.getSetting(WorldStandardValues.VILLAGES_ENABLED, logger);		
		this.mineshaftsEnabled = reader.getSetting(WorldStandardValues.MINESHAFTS_ENABLED, logger);
		this.oceanMonumentsEnabled = reader.getSetting(WorldStandardValues.OCEAN_MONUMENTS_ENABLED, logger);
		this.rareBuildingsEnabled = reader.getSetting(WorldStandardValues.RARE_BUILDINGS_ENABLED, logger);

		// OTG Custom structures

		// IsOTGPlus was renamed to CustomStructureType, but value types are different (bool -> enum)
		// If IsOTGPlus is true, use it. IsOTGPlus isn't written to configs, so this is only required once
		// to update configs.
		boolean isOTGPlus = reader.getSetting(WorldStandardValues.ISOTGPLUS, logger);
		if(isOTGPlus)
		{
			this.customStructureType = CustomStructureType.BO4;
		} else {
			this.customStructureType = reader.getSetting(WorldStandardValues.CUSTOM_STRUCTURE_TYPE, logger);	
		}
		this.useOldBO3StructureRarity = reader.getSetting(WorldStandardValues.USE_OLD_BO3_STRUCTURE_RARITY, logger);
		this.decorationBoundsCheck = reader.getSetting(WorldStandardValues.DECORATION_BOUNDS_CHECK, logger);
		this.maximumCustomStructureRadius = reader.getSetting(WorldStandardValues.MAXIMUM_CUSTOM_STRUCTURE_RADIUS, logger);		
		this.bo3AtSpawn = reader.getSetting(WorldStandardValues.BO3_AT_SPAWN, logger);
		
		// Caves & Ravines
		
		this.cavesEnabled = reader.getSetting(WorldStandardValues.CAVES_ENABLED, logger);
		this.caveFrequency = reader.getSetting(WorldStandardValues.CAVE_FREQUENCY, logger);
		this.caveRarity = reader.getSetting(WorldStandardValues.CAVE_RARITY, logger);
		this.evenCaveDistribution = reader.getSetting(WorldStandardValues.EVEN_CAVE_DISTRIBUTION, logger);		
		this.caveMinAltitude = reader.getSetting(WorldStandardValues.CAVE_MIN_ALTITUDE, logger);
		this.caveMaxAltitude = reader.getSetting(WorldStandardValues.CAVE_MAX_ALTITUDE, logger);
		this.caveSystemFrequency = reader.getSetting(WorldStandardValues.CAVE_SYSTEM_FREQUENCY, logger);
		this.individualCaveRarity = reader.getSetting(WorldStandardValues.INDIVIDUAL_CAVE_RARITY, logger);		
		this.caveSystemPocketChance = reader.getSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_CHANCE, logger);
		this.caveSystemPocketMinSize = reader.getSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_MIN_SIZE, logger);
		this.caveSystemPocketMaxSize = reader.getSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_MAX_SIZE, logger);

		this.ravinesEnabled = reader.getSetting(WorldStandardValues.RAVINES_ENABLED, logger);
		this.ravineRarity = reader.getSetting(WorldStandardValues.RAVINE_RARITY, logger);
		this.ravineMinLength = reader.getSetting(WorldStandardValues.RAVINE_MIN_LENGTH, logger);
		this.ravineMaxLength = reader.getSetting(WorldStandardValues.RAVINE_MAX_LENGTH, logger);
		this.ravineDepth = reader.getSetting(WorldStandardValues.RAVINE_DEPTH, logger);
		this.ravineMinAltitude = reader.getSetting(WorldStandardValues.RAVINE_MIN_ALTITUDE, logger);
		this.ravineMaxAltitude = reader.getSetting(WorldStandardValues.RAVINE_MAX_ALTITUDE, logger);

		// Spawn point

		this.fixedSpawnPoint = reader.getSetting(WorldStandardValues.FIXED_SPAWN_POINT, logger, materialReader);
		this.spawnPointX = reader.getSetting(WorldStandardValues.SPAWN_POINT_X, logger, materialReader);
		this.spawnPointY = reader.getSetting(WorldStandardValues.SPAWN_POINT_Y, logger, materialReader);
		this.spawnPointZ = reader.getSetting(WorldStandardValues.SPAWN_POINT_Z, logger, materialReader);
		this.spawnPointAngle = reader.getSetting(WorldStandardValues.SPAWN_POINT_ANGLE, logger, materialReader);
		
		// Portal settings
		// Only used when preset is not overworld/dimension/end.
		// Can be overridden via DimensionConfig.

		this.portalBlocks = reader.getSetting(WorldStandardValues.PORTAL_BLOCKS, logger, materialReader);
		this.portalColor = reader.getSetting(WorldStandardValues.PORTAL_COLOR, logger);
		this.portalMob = reader.getSetting(WorldStandardValues.PORTAL_MOB, logger);
		this.portalIgnitionSource = reader.getSetting(WorldStandardValues.PORTAL_IGNITION_SOURCE, logger);		
		
		// Dimension settings
		
		long fixedTime = reader.getSetting(WorldStandardValues.FIXED_TIME, logger);
		this.fixedTime = fixedTime == -1l ? OptionalLong.empty() : OptionalLong.of(fixedTime);
		this.hasSkyLight = reader.getSetting(WorldStandardValues.HAS_SKYLIGHT, logger);
		this.hasCeiling = reader.getSetting(WorldStandardValues.HAS_CEILING, logger);
		this.ultraWarm = reader.getSetting(WorldStandardValues.ULTRA_WARM, logger);
		this.natural = reader.getSetting(WorldStandardValues.NATURAL, logger);
		this.coordinateScale = reader.getSetting(WorldStandardValues.COORDINATE_SCALE, logger);
		this.createDragonFight = reader.getSetting(WorldStandardValues.CREATE_DRAGON_FLIGHT, logger);
		this.piglinSafe = reader.getSetting(WorldStandardValues.PIGLIN_SAFE, logger);
		this.bedWorks = reader.getSetting(WorldStandardValues.BED_WORKS, logger);
		this.respawnAnchorWorks = reader.getSetting(WorldStandardValues.RESPAWN_ANCHOR_WORKS, logger);
		this.hasRaids = reader.getSetting(WorldStandardValues.HAS_RAIDS, logger); 
		this.logicalHeight = reader.getSetting(WorldStandardValues.LOGICAL_HEIGHT, logger);
		this.infiniburn = reader.getSetting(WorldStandardValues.INFINIBURN, logger);
		this.effectsLocation = reader.getSetting(WorldStandardValues.EFFECTS_LOCATION, logger);
		this.ambientLight = reader.getSetting(WorldStandardValues.AMBIENT_LIGHT, logger).floatValue();

		// Game rules 
		// Only used when preset is OTG overworld atm, since gamerules are shared across dimensions.
		// Can be overridden via DimensionConfig.

		
		this.overrideGameRules = reader.getSetting(WorldStandardValues.OVERRIDE_GAME_RULES, logger);
		this.doFireTick = reader.getSetting(WorldStandardValues.DO_FIRE_TICK, logger);
		this.mobGriefing = reader.getSetting(WorldStandardValues.MOB_GRIEFING, logger);
		this.keepInventory = reader.getSetting(WorldStandardValues.KEEP_INVENTORY, logger);
		this.doMobSpawning = reader.getSetting(WorldStandardValues.DO_MOB_SPAWNING, logger);
		this.doMobLoot = reader.getSetting(WorldStandardValues.DO_MOB_LOOT, logger);
		this.doTileDrops = reader.getSetting(WorldStandardValues.DO_TILE_DROPS, logger);
		this.doEntityDrops = reader.getSetting(WorldStandardValues.DO_ENTITY_DROPS, logger);
		this.commandBlockOutput = reader.getSetting(WorldStandardValues.COMMAND_BLOCK_OUTPUT, logger);
		this.naturalRegeneration = reader.getSetting(WorldStandardValues.NATURAL_REGENERATION, logger);
		this.doDaylightCycle = reader.getSetting(WorldStandardValues.DO_DAY_LIGHT_CYCLE, logger);
		this.logAdminCommands = reader.getSetting(WorldStandardValues.LOG_ADMIN_COMMANDS, logger);
		this.showDeathMessages = reader.getSetting(WorldStandardValues.SHOW_DEATH_MESSAGES, logger);
		this.randomTickSpeed = reader.getSetting(WorldStandardValues.RANDOM_TICK_SPEED, logger);
		this.sendCommandFeedback = reader.getSetting(WorldStandardValues.SEND_COMMAND_FEEDBACK, logger);
		this.spectatorsGenerateChunks = reader.getSetting(WorldStandardValues.SPECTATORS_GENERATE_CHUNKS, logger);
		this.spawnRadius = reader.getSetting(WorldStandardValues.SPAWN_RADIUS, logger);
		this.disableElytraMovementCheck = reader.getSetting(WorldStandardValues.DISABLE_ELYTRA_MOVEMENT_CHECK, logger);
		this.maxEntityCramming = reader.getSetting(WorldStandardValues.MAX_ENTITY_CRAMMING, logger);
		this.doWeatherCycle = reader.getSetting(WorldStandardValues.DO_WEATHER_CYCLE, logger);
		this.doLimitedCrafting = reader.getSetting(WorldStandardValues.DO_LIMITED_CRAFTING, logger);
		this.maxCommandChainLength = reader.getSetting(WorldStandardValues.MAX_COMMAND_CHAIN_LENGTH, logger);
		this.announceAdvancements = reader.getSetting(WorldStandardValues.ANNOUNCE_ADVANCEMENTS, logger);
		this.disableRaids = reader.getSetting(WorldStandardValues.DISABLE_RAIDS, logger);
		this.doInsomnia = reader.getSetting(WorldStandardValues.DO_INSOMNIA, logger);
		this.drowningDamage = reader.getSetting(WorldStandardValues.DROWNING_DAMAGE, logger);
		this.fallDamage = reader.getSetting(WorldStandardValues.FALL_DAMAGE, logger);
		this.fireDamage = reader.getSetting(WorldStandardValues.FIRE_DAMAGE, logger);
		this.doPatrolSpawning = reader.getSetting(WorldStandardValues.DO_PATROL_SPAWNING, logger);
		this.doTraderSpawning = reader.getSetting(WorldStandardValues.DO_TRADER_SPAWNING, logger);
		this.forgiveDeadPlayers = reader.getSetting(WorldStandardValues.FORGIVE_DEAD_PLAYERS, logger);
		this.universalAnger = reader.getSetting(WorldStandardValues.UNIVERSAL_ANGER, logger);
	}

	private void readTemplateBiomes(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader)
	{
		this.templateBiomes = new ArrayList<TemplateBiome>();
		for (ConfigFunction<IWorldConfig> res : reader.getConfigFunctions((IWorldConfig)this, biomeResourcesManager, logger, materialReader))
		{
			if (res != null)
			{
				if (res instanceof TemplateBiome)
				{
					this.templateBiomes.add((TemplateBiome)res);
				}
			}
		}
	}	
	
	private void readBiomeGroups(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader)
	{
		this.biomeGroupManager = new BiomeGroupManager();
		for (ConfigFunction<IWorldConfig> res : reader.getConfigFunctions((IWorldConfig)this, biomeResourcesManager, logger, materialReader))
		{
			if (res != null)
			{
				if (res instanceof BiomeGroup)
				{
					this.biomeGroupManager.registerGroup((BiomeGroup) res, logger);
				}
			}
		}
	}

	@Override
	protected void writeConfigSettings(SettingsMap writer)
	{
		writer.header1("WorldConfig",
			"Contains settings which affect the entire world, biome specific settings can be found in the Biome Configs.",
			"This file controls biome groupings, ocean and land sizes/rarities, river settings, cave and canyon distribution,",
			"vanilla minecraft structure spawning, sea level, dimension/portal settings and more."
		);

		writer.header2("Config Writing");

		writer.putSetting(WorldStandardValues.SETTINGS_MODE, this.settingsMode,
			"Each time " + Constants.MOD_ID + " reads the config files it can also write to them. With this setting you can change how this behaves. Possible modes:",
			"	WriteAll - Auto-update settings from old versions, order them, add comments, reset invalid settings and remove custom comments. (Recommended)",
			"	WriteWithoutComments - Same as WriteAll, but removes all comments, both the ones added by OTG and custom ones. Removing comments is a recommended optimization for release versions of presets.",
			"	WriteDisable - Doesn't write to the config files. Errors are not corrected, old settings are read but are not corrected. Custom comments won't be removed with this mode."
		);
		
		writer.header2("World Identity");
		
		writer.putSetting(WorldStandardValues.AUTHOR, this.author,
			"The author of this preset"
		);

		writer.putSetting(WorldStandardValues.DESCRIPTION, this.description,
			"A short description of this world"
		);

		writer.putSetting(WorldStandardValues.MAJOR_VERSION, this.majorVersion,
			"The preset major version. Increasing the minor version makes the PresetPacker overwrite,",
			"while increasing the major version will make the PresetPacker save a new copy"
		);
		
		writer.putSetting(WorldStandardValues.MINOR_VERSION, this.minorVersion,
			"The preset minor version. Increasing the minor version makes the PresetPacker overwrite,",
			"while increasing the major version will make the PresetPacker save a new copy"
		);

		writer.putSetting(WorldStandardValues.SHORT_PRESET_NAME, this.shortPresetName,
			"The shortened name for the preset, used in biome resource locations and similar"
		);

		writer.header2("Visual Settings",
			"Controls the world's fog colors. Sky, grass and foliage colors are defined inside the biome configs."
		);
		
		writer.putSetting(WorldStandardValues.WORLD_FOG_COLOR, this.worldFogColor,
			"Color of the distance fog, can be overridden per biome."
		);

		writer.header2("Biome Modes");

		writer.putSetting(WorldStandardValues.BIOME_MODE, this.biomeMode,
			"Possible biome modes:",
			"	Normal - standard random generation with biome groups, uses all features.",
			"	FromImage - biome layout defined by an image file.",
			"	NoGroups - Minecraft 1.0 - 1.6.4 biome generator, only supports the biome groups NormalBiomes and IceBiomes."
		);
		
		writer.header1("Settings for BiomeMode: Normal/NoGroups");
		
		writer.putSetting(WorldStandardValues.GENERATION_DEPTH, this.generationDepth,
			"Defines the maximum number BiomeSize, RiverSize and LandSize can be set to.", 
			"All size settings such as Biome Group Size, RiverSize, LandSize (in the WorldConfig.ini), and BiomeSize (in Biome Configs) must be between 0 (largest) and GenerationDepth (smallest).", 
			"Increasing GenerationDepth by one will roughly double the size of all biomes, similarly decreasing it by 1 will half the size of all biomes.",
			"Small values (1-2) and Large values (20+) may affect generator performance.",
			"This setting is also used in BiomeMode:FromImage when ImageMode is set to ContinueNormal"
		);

		writer.putSetting(WorldStandardValues.BIOME_RARITY_SCALE, this.biomeRarityScale,
			"Max biome rarity from 1 to infinity. By default this is 100, but you can raise it for fine-grained control, or to create biomes with a chance of occurring smaller than 1/100."
		);

		writer.putSetting(WorldStandardValues.OLD_GROUP_RARITY, this.oldGroupRarity,
			"Whether or not OTG should use the old group rarity"
		);

		writer.putSetting(WorldStandardValues.OLD_LAND_RARITY, this.oldLandRarity,
				"Whether or not OTG should use the old land rarity. Disabling this will make LandRarity work as a percentage"
		);

		writer.putSetting(WorldStandardValues.IMPROVED_BORDER_DECORATION, this.improvedBorderDecoration,
				"Whether OTG should do decoration for all biomes found in chunk. This could result in more resources being more common near chunk borders."
		);
		
		writer.header2("Template biomes",
			"Template biomes can be used to include non-OTG biomes (modded or vanilla) in OTG presets.",
			"",
			"Syntax: TemplateBiome(BiomeConfigName, BiomeRegistryName or Tags/Categories[, more BiomeRegistryName or Tags/Categories[, ...]], minTemperature, maxTemperature)",
			"BiomeConfigName - Name of a corresponding biome config. Case sensitive.",			
			"BiomeRegistryName - The registry name of a non-otg biome, for example \"minecraft:plains\".",
			"Tags/Categories - Instead of BiomeRegistryName, Forge Biome Dictionary id's and/or MC Biome Categories.",
			"OTG fetches all non-OTG biomes that match the specified category/tags and associates them with the BiomeConfig.",
			"The BiomeConfig must use TemplateForBiome:true, or it is ignored.",
			"Example: TemplateBiome(MCForest, category.forest tag.overworld)",
			"Adds all forest biomes in the overworld. Biomes are never added twice.",
			"- Use space as an AND operator, in the above example \"category.forest tag.overworld\" matches biomes with category forest AND tag overworld.",
			"To target both minecraft and modded biomes, use \"category.\" or \"tag.\".",
			"To target only modded biomes, use \"modcategory.\" or \"modtag.\".",
			"To target only minecraft biomes, use \"mccategory.\" or \"mctag.\".",
			"To filter biomes for a specific mod, add \"mod.<namespace>\", for example \"mod.byg category.plains tag.overworld\".",
			"To exclude specific biome registry names, tags, categories or mods, use \"-\", for example -tag.overworld to exclude overworld biomes.",		
			"MinTemperature/MaxTemperature - Optional, only biomes within this temperature range are allowed.",	
			"Example: TemplateBiome(TagPlains, category.plains -tag.overworld) or TemplateBiome(TagPlains, category.plains -tag.overworld, -0.2, 0.2)",
			"This targets a BiomeConfig named TagPlains.bc, and adds to it all non-OTG biomes that are of category \"plains\" but do not have biome dictionary tag \"overworld\",",
			"the second example includes a temperature range between -0.2 and 0.2.",
			"Note:",
			"Each biome can only be assigned to one biome config, so the order of TemplateBiome()s is important. Put your most specific TemplateBiome first, and the most generic last.",
			"When using BiomeRegistryName to include or exclude a biome, it must have its own entry, for example: \",minecraft:forest,-minecraft:plains,\""
		);

		writer.addConfigFunctions(this.templateBiomes);
		
		writer.header2("Biome Groups",
			"Biome groups group similar biomes together so that they spawn next to each other.", 
			"Only standard biomes are required to be part of biome groups, isle, border and river biomes are configured separately.",
			"",
			"Syntax: BiomeGroup(GroupName, GroupSize, GroupRarity, BiomeName or Tags/Categories[, AnotherName[, ...]], minTemperature, maxTemperature)",
			"GroupName - must be unique, choose something descriptive.",
			"Size - from 0 to GenerationDepth. Lower number = larger. All biomes in the group must be smaller (higher BiomeSize number) or equal to this value.",
			"Rarity - relative spawn chance.",
			"BiomeName - Name of a corresponding biome config. Case sensitive. Can also be a registry name (minecraft:plains), if there is a associated TemplateBiome().",
			"If the biome config is a template biome, all associated non-otg biomes are added to the group.",
			"Tags/Categories - Instead of BiomeName, Forge Biome Dictionary id's and/or MC Biome Categories. ",
			"OTG fetches all non-OTG biomes that match the specified category/tags and adds them to the biome group.",
			"A TemplateBiome() that targets the biome must exist, or it is ignored.",
			"Example: BiomeGroup(NormalBiomes, 1, 100, category.plains tag.overworld, tag.hot tag.dry)",
			"Adds 2 entries; all plains biomes in the overworld, all hot+dry biomes. Biomes are never added twice.",
			"- Use space as an AND operator, in the above example \"category.plains tag.overworld\" matches biomes with category plains AND tag overworld.",
			"To target both minecraft and modded biomes, use \"category.\" or \"tag.\".",
			"To target only modded biomes, use \"modcategory.\" or \"modtag.\".",
			"To target only minecraft biomes, use \"mccategory.\" or \"mctag.\".",
			"To filter biomes for a specific mod, add \"mod.<namespace>\", for example \"mod.byg category.plains tag.overworld\".",
			"To exclude specific biome registry names, tags, categories or mods, use \"-\", for example -tag.overworld to exclude overworld biomes.",			
			"MinTemperature/MaxTemperature - Optional, when using Tags/Categories, only biomes within this temperature range are used.",
			"Example: BiomeGroup(NormalBiomes, 1, 100, category.plains tag.overworld, tag.hot tag.dry, -1.0, 1.0)",
			"Same example as before, but only includes biomes with temperature between -1.0 and 1.0.",
			"Note:", 
			"When using BiomeRegistryName to include or exclude a biome, it must have its own entry, for example: \",minecraft:forest,-minecraft:plains,\"",
			"If using BiomeMode: Normal, there are no limitations on the number of biome groups you can have or their names.",
			"If using BiomeMode: NoGroups, only two biome group names are valid, NormalBiomes and IceBiomes, other groups are ignored. Only the size and rarity of the group named IceBiomes will be used, the size and rarity of the NormalBiomes group is ignored."
		);

		writer.addConfigFunctions(this.biomeGroupManager.getGroups());

		writer.putSetting(WorldStandardValues.BLACKLISTED_BIOMES, this.blackListedBiomes,
			"When using biome dictionary tags and/or biome categories with biome groups, these (non-OTG) biomes are excluded. Example: minecraft:plains."
		);
		
		writer.header2("Isle & Border Biomes");
		
		writer.putSetting(WorldStandardValues.ISLE_BIOMES, this.isleBiomes,
			"Isle biomes are biomes which spawn inside another biome (e.g. an island in an ocean). As well as listing every isle biome here, you must set IsleInBiome in each biome config too. Biome name is case sensitive."
		);

		writer.putSetting(WorldStandardValues.BORDER_BIOMES, this.borderBiomes,
			"Biomes used as borders of other biomes. As well as listing every border biome here, you must set BiomeIsBorder in each biome config too. Biome name is case sensitive."
		);

		writer.header2("Landmass Settings");

		writer.putSetting(WorldStandardValues.LAND_RARITY, this.landRarity,
			"Land rarity from 100 to 1. Higher numbers result in more land."
		);

		writer.putSetting(WorldStandardValues.LAND_SIZE, this.landSize,
			"Land size from 0 to GenerationDepth. Higher LandSize numbers will make the size of the land smaller. Landsize number should always be lower than any biome groups."
		);

		writer.putSetting(WorldStandardValues.FORCE_LAND_AT_SPAWN, this.forceLandAtSpawn,
			"If enabled, land will always spawn at or near 0,0"
		);

		writer.putSetting(WorldStandardValues.OCEAN_BIOME_SIZE, this.oceanBiomeSize,
			"Ocean biome size 0 to GenerationDepth. Higher OceanBiomeSize numbers will make the size of the ocean biomes smaller."
		);

		writer.putSetting(WorldStandardValues.LAND_FUZZY, this.landFuzzy,
			"Generates more lakes (via small ocean biomes) at the edges of continents. As a side effect, the continent will also get a bit larger. Must be from 0 to GenerationDepth minus LandSize."
		);

		writer.putSetting(WorldStandardValues.DEFAULT_OCEAN_BIOME, this.defaultOceanBiome,
			"Set the default Ocean biome for this world."
		);

		writer.putSetting(WorldStandardValues.DEFAULT_WARM_OCEAN_BIOME, this.defaultWarmOceanBiome,
			"Set the default Warm Ocean biome for this world."
		);

		writer.putSetting(WorldStandardValues.DEFAULT_LUKEWARM_OCEAN_BIOME, this.defaultLukewarmOceanBiome,
			"Set the default Lukewarm Ocean biome for this world."
		);

		writer.putSetting(WorldStandardValues.DEFAULT_COLD_OCEAN_BIOME, this.defaultColdOceanBiome,
			"Set the default Cold Ocean biome for this world."
		);

		writer.putSetting(WorldStandardValues.DEFAULT_FROZEN_OCEAN_BIOME, this.defaultFrozenOceanBiome,
			"The default Frozen Ocean biome for this world."
		);

		writer.putSetting(WorldStandardValues.LARGE_ORE_VEINS, this.largeOreVeins,
				"Add large ore veins to this world."
		);

		writer.header2("Ice Area Settings");

		writer.putSetting(WorldStandardValues.FROZEN_OCEAN, this.frozenOcean,
			"Can be true or false, makes the water of the oceans near a cold biome frozen. The definition of 'cold' is controlled by the next setting.",
			"Set this to false to stop the ocean from freezing near when an \"ice area\" intersects with an ocean."
		);

		writer.putSetting(WorldStandardValues.FROZEN_OCEAN_TEMPERATURE, this.frozenOceanTemperature,
			"This is the maximum biome temperature when a biome is still considered cold. Water in oceans nearby cold biomes freezes if FrozenOcean is set to true.",
			"Temperature reference from vanilla Minecraft: < 0.15 for snow, 0.15 - 0.95 for rain, or > 1.0 for dry."
		);
		
		writer.header2("Rivers");
		
		writer.putSetting(WorldStandardValues.RIVERS_ENABLED, this.riversEnabled,
			"Set this to false to prevent the river generator from doing anything."
		);

		writer.putSetting(WorldStandardValues.RANDOM_RIVERS, this.randomRivers,
			"When this setting is false, rivers follow the biome borders most of the time. Set this setting to true to disable this behavior."
		);		
		writer.putSetting(WorldStandardValues.RIVER_RARITY, this.riverRarity,
			"Controls the rarity of rivers. Must be from 0 to GenerationDepth. A higher number means more rivers. To define which rivers flow through which biomes see the individual biome configs."
		);

		writer.putSetting(WorldStandardValues.RIVER_SIZE, this.riverSize,
			"Controls the size of rivers. Can range from 0 to GenerationDepth minus RiverRarity. Making this larger will make the rivers larger, without affecting how often rivers will spawn."
		);

		writer.header1("Settings For BiomeMode:FromImage",
			"In each of the BiomeConfigs there is a BiomeColor variable, this variable is the hexadecimal color of the biome.",
			"These colors are used to define the biome layout in the input image (as well as the colour of the biome when using the /otg map command). Two biomes must not have the same color.", 
			"The settings in this section are for FromImage mode only."
		);

		writer.putSetting(WorldStandardValues.IMAGE_MODE, this.imageMode,
			"Defines what to do when terrain is generated outside the boundaries of the image:",
			"	Repeat - repeats the image",
			"	Mirror - repeats and mirrors the image",
			"	ContinueNormal - continues with random generation, using settings for BiomeMode: Normal",
			"	FillEmpty - fills the space with one biome (defined below)"
		);

		writer.putSetting(WorldStandardValues.IMAGE_FILE, this.imageFile,
			"The image which will provide the Biomes must be a PNG file without transparency, once placed in the same folder as WorldConfig.ini OTG will use it as a reference for the Biomes generation.",
			"Source png file name for FromImage biome mode."
		);

		writer.putSetting(WorldStandardValues.IMAGE_ORIENTATION, this.imageOrientation,
			"How the image is oriented: North, South, East or West. When this is set to North, the top of your picture is north (no rotation).", 
			"When it is set to East, the image is rotated 90 degrees counter-clockwise, therefore what is on the east in the image becomes north in the world.", 
			"Possible values: North, East, South, West."
		);

		writer.putSetting(WorldStandardValues.IMAGE_FILL_BIOME, this.imageFillBiome,
			"Biome name for filling outside image boundaries with FillEmpty mode."
		);

		writer.putSetting(WorldStandardValues.IMAGE_X_OFFSET, this.imageXOffset,
			"Translates the map origin. This number needs to be multiplied by -1 when using FillEmpty."
		);
		
		writer.putSetting(WorldStandardValues.IMAGE_Z_OFFSET, this.imageZOffset,
			"Translates the map origin. This number needs to be multiplied by -1 when using FillEmpty." 
		);
		
		writer.header1("Terrain Height and Volatility",
			"The settings in this section control terrain settings that are not specific to any biome."
		);
		
		writer.putSetting(WorldStandardValues.WORLD_HEIGHT_SCALE_BITS, this.worldHeightScaleBits,
			"The height scale of the world. Increasing this by one doubles the terrain height of the world, substracting one halves the terrain height. Values must be between 5 and 8, inclusive."
		);

		writer.putSetting(WorldStandardValues.WORLD_HEIGHT_CAP_BITS, this.worldHeightCapBits,
			"The height cap of the world. A cap of 7 will make sure that there is no terrain above 128 (y=2^7). Near this cap less and less terrain generates with no terrain above this cap.", 
			"Values must be between 5 and 8 (inclusive), and may not be lower that WorldHeightScaleBits."
		);

		writer.putSetting(WorldStandardValues.FRACTURE_HORIZONTAL, this.fractureHorizontal,
			"Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured horizontally.",
			"Values less than 0 will 'relax' the terrain, leading to more gradual and smoother height transitions."
		);

		writer.putSetting(WorldStandardValues.FRACTURE_VERTICAL, this.fractureVertical,
			"Can increase (values greater than 0) or decrease (values less than 0) how much the landscape is fractured vertically.",
			"Values above 0 will lead to large cliffs/overhangs, floating islands, and/or a cavern world depending on other settings.",
			"Values less than 0 will make terrain volatility more 'spiky' but lessen the likelihood of overhangs and floating terrain."
		);
		
		writer.header1("Blocks");
		
		writer.putSetting(WorldStandardValues.REMOVE_SURFACE_STONE, this.removeSurfaceStone,
			"Set this to true to place the biome surface block on top of all exposed stone."
		);		
		
		writer.header2("Bedrock");
		
		writer.putSetting(WorldStandardValues.BEDROCK_BLOCK, this.bedrockBlock,
			"Block used as bedrock."
		);
		
		writer.putSetting(WorldStandardValues.DISABLE_BEDROCK, this.disableBedrock,
			"Disable bottom of map bedrock generation. Doesn't affect bedrock on the ceiling of the map."
		);

		writer.putSetting(WorldStandardValues.CEILING_BEDROCK, this.ceilingBedrock,
			"Enable ceiling of map bedrock generation."
		);

		writer.putSetting(WorldStandardValues.FLAT_BEDROCK, this.flatBedrock,
			"Make a single flat layer of bedrock."
		);
		
		writer.header2("Water / Lava / Frozen States");
		
		writer.putSetting(WorldStandardValues.WATER_LEVEL_MAX, this.waterLevelMax,
			"Set water level. Every empty block under this level will be fill water or another block from WaterBlock."
		);
		
		writer.putSetting(WorldStandardValues.WATER_LEVEL_MIN, this.waterLevelMin);

		writer.putSetting(WorldStandardValues.WATER_BLOCK, this.waterBlock,
			"Block used as water in WaterLevel."
		);
		
		writer.putSetting(WorldStandardValues.ICE_BLOCK, this.iceBlock,
			"Block used as ice."
		);

		writer.putSetting(WorldStandardValues.COOLED_LAVA_BLOCK, this.cooledLavaBlock,
			"Block used as cooled or frozen lava.",
			"Set this to OBSIDIAN for \"frozen\" lava lakes in cold biomes"
		);

		writer.putSetting(WorldStandardValues.BETTER_SNOW_FALL, this.betterSnowFall,
			"When set to false, 1 layer of snow falls on the highest block only.",
			"When set to true, the number of layers (1-8) is dependent on biome temperature.",
			"Higher altitudes have lower temperatures, so snow becomes deeper higher up.",
			"Also causes snow to fall through leaves, leaves can carry 3 layers while the rest falls through."
		);

		writer.header1("Resources");
		
		writer.putSetting(WorldStandardValues.DISABLE_OREGEN, this.disableOreGen,
			"Disables Ore(), UnderWaterOre() and Vein() biome resources that use any type of ore block."
		);

		// Structures

		writer.header1("Structures",
			"These are global on/off toggles and spacing/separation settings for the entire world for each",
			"vanilla structure type. Spacing/separation work the same way as they do for datapacks.",
			"When set to true, structures configured in biome configs are able to spawn.",
			"Check the biome configs for customisation options per structure type per biome (size etc)."
		);
	
		writer.putSetting(WorldStandardValues.VILLAGES_ENABLED, this.villagesEnabled);
		writer.putSetting(WorldStandardValues.VILLAGE_SPACING, this.villageSpacing);
		writer.putSetting(WorldStandardValues.VILLAGE_SEPARATION, this.villageSeparation);		
		writer.putSetting(WorldStandardValues.MINESHAFTS_ENABLED, this.mineshaftsEnabled);
		writer.putSetting(WorldStandardValues.MINESHAFT_SPACING, this.mineshaftSpacing);
		writer.putSetting(WorldStandardValues.MINESHAFT_SEPARATION, this.mineshaftSeparation);
		writer.putSetting(WorldStandardValues.STRONGHOLDS_ENABLED, this.strongholdsEnabled);
		writer.putSetting(WorldStandardValues.STRONGHOLD_SPACING, this.strongholdSpacing);
		writer.putSetting(WorldStandardValues.STRONGHOLD_SEPARATION, this.strongholdSeparation);
		writer.putSetting(WorldStandardValues.STRONGHOLD_DISTANCE, this.strongholdDistance);
		writer.putSetting(WorldStandardValues.STRONGHOLD_SPREAD, this.strongholdSpread);
		writer.putSetting(WorldStandardValues.STRONGHOLD_COUNT, this.strongholdCount);
		writer.putSetting(WorldStandardValues.RARE_BUILDINGS_ENABLED, this.rareBuildingsEnabled);		
		writer.putSetting(WorldStandardValues.DESERTPYRAMID_SPACING, this.desertPyramidSpacing);
		writer.putSetting(WorldStandardValues.DESERTPYRAMID_SEPARATION, this.desertPyramidSeparation);
		writer.putSetting(WorldStandardValues.IGLOO_SPACING, this.iglooSpacing);
		writer.putSetting(WorldStandardValues.IGLOO_SEPARATION, this.iglooSeparation);
		writer.putSetting(WorldStandardValues.JUNGLETEMPLE_SPACING, this.jungleTempleSpacing);
		writer.putSetting(WorldStandardValues.JUNGLETEMPLE_SEPARATION, this.jungleTempleSeparation);
		writer.putSetting(WorldStandardValues.SWAMPHUT_SPACING, this.swampHutSpacing);
		writer.putSetting(WorldStandardValues.SWAMPHUT_SEPARATION, this.swampHutSeparation);		
		writer.putSetting(WorldStandardValues.WOODLAND_MANSIONS_ENABLED, this.woodlandMansionsEnabled);
		writer.putSetting(WorldStandardValues.WOODLANDMANSION_SPACING, this.woodlandMansionSpacing);
		writer.putSetting(WorldStandardValues.WOODLANDMANSION_SEPARATION, this.woodlandMansionSeparation);		
		writer.putSetting(WorldStandardValues.OCEAN_MONUMENTS_ENABLED, this.oceanMonumentsEnabled);	
		writer.putSetting(WorldStandardValues.OCEANMONUMENT_SPACING, this.oceanMonumentSpacing);
		writer.putSetting(WorldStandardValues.OCEANMONUMENT_SEPARATION, this.oceanMonumentSeparation);		
		writer.putSetting(WorldStandardValues.NETHER_FORTRESSES_ENABLED, this.netherFortressesEnabled);
		writer.putSetting(WorldStandardValues.NETHERFORTRESS_SPACING, this.netherFortressSpacing);
		writer.putSetting(WorldStandardValues.NETHERFORTRESS_SEPARATION, this.netherFortressSeparation);		
		writer.putSetting(WorldStandardValues.BURIED_TREASURE_ENABLED, this.buriedTreasureEnabled);
		writer.putSetting(WorldStandardValues.BURIEDTREASURE_SPACING, this.buriedTreasureSpacing);
		writer.putSetting(WorldStandardValues.BURIEDTREASURE_SEPARATION, this.buriedTreasureSeparation);		
		writer.putSetting(WorldStandardValues.OCEAN_RUINS_ENABLED, this.oceanRuinsEnabled);
		writer.putSetting(WorldStandardValues.OCEANRUIN_SPACING, this.oceanRuinSpacing);
		writer.putSetting(WorldStandardValues.OCEANRUIN_SEPARATION, this.oceanRuinSeparation);		
		writer.putSetting(WorldStandardValues.PILLAGER_OUTPOSTS_ENABLED, this.pillagerOutpostsEnabled);
		writer.putSetting(WorldStandardValues.PILLAGEROUTPOST_SPACING, this.pillagerOutpostSpacing);
		writer.putSetting(WorldStandardValues.PILLAGEROUTPOST_SEPARATION, this.pillagerOutpostSeparation);		
		writer.putSetting(WorldStandardValues.BASTION_REMNANTS_ENABLED, this.bastionRemnantsEnabled);
		writer.putSetting(WorldStandardValues.BASTIONREMNANT_SPACING, this.bastionRemnantSpacing);
		writer.putSetting(WorldStandardValues.BASTIONREMNANT_SEPARATION, this.bastionRemnantSeparation);		
		writer.putSetting(WorldStandardValues.NETHER_FOSSILS_ENABLED, this.netherFossilsEnabled);
		writer.putSetting(WorldStandardValues.NETHERFOSSIL_SPACING, this.netherFossilSpacing);
		writer.putSetting(WorldStandardValues.NETHERFOSSIL_SEPARATION, this.netherFossilSeparation);				
		writer.putSetting(WorldStandardValues.END_CITIES_ENABLED, this.endCitiesEnabled);
		writer.putSetting(WorldStandardValues.ENDCITY_SPACING, this.endCitySpacing);
		writer.putSetting(WorldStandardValues.ENDCITY_SEPARATION, this.endCitySeparation);		
		writer.putSetting(WorldStandardValues.RUINED_PORTALS_ENABLED, this.ruinedPortalsEnabled);
		writer.putSetting(WorldStandardValues.RUINEDPORTAL_SPACING, this.ruinedPortalSpacing);
		writer.putSetting(WorldStandardValues.RUINEDPORTAL_SEPARATION, this.ruinedPortalSeparation);		
		writer.putSetting(WorldStandardValues.SHIPWRECKS_ENABLED, this.shipWrecksEnabled);
		writer.putSetting(WorldStandardValues.SHIPWRECK_SPACING, this.shipwreckSpacing);
		writer.putSetting(WorldStandardValues.SHIPWRECK_SEPARATION, this.shipwreckSeparation);		
		
		writer.header2("OTG Custom structures and objects (BO2/BO3/BO4)");
		
		writer.putSetting(WorldStandardValues.CUSTOM_STRUCTURE_TYPE, this.customStructureType,
			"Sets the type of structures the world should spawn, BO3 or BO4.",
			"Allowed values: BO3/BO4.",
			"BO4's allow for collision detection, fine control over structure distribution, advanced branching mechanics for",
			"procedurally generated structures, smoothing areas, extremely large structures, settings for blending structures",
			"with surrounding terrain, etc. BO3's are simpler, seed based CustomStructures, more like vanilla mc structures.",
			"Worlds currently can only use one type of structure."
		);			
		
		writer.putSetting(WorldStandardValues.BO3_AT_SPAWN, this.bo3AtSpawn,
			"This BO3 will be spawned at the world's spawn point as a CustomObject (Max size 32x32)."
		);

		writer.header2("BO3 Custom structures");
		
		writer.putSetting(WorldStandardValues.USE_OLD_BO3_STRUCTURE_RARITY, this.useOldBO3StructureRarity,
			"For 1.12.2 v9.0_r11 and earlier, BO3 customstructures used 2 rarity rolls,",
			"one for the rarity in the CustomStructure() tag, one for the rarity in the BO3 itself.",
			"For 1.16, we use only the rarity roll from the CustomStructure() tag. Set this to true",
			"to use the old system."
		);
		
		writer.putSetting(WorldStandardValues.MAXIMUM_CUSTOM_STRUCTURE_RADIUS, this.maximumCustomStructureRadius,
			"Maximum radius of custom structures in chunks. Custom structures are spawned by",
			"the CustomStructure resource in the biome configuration files. Not used for BO4's."
		);
		
		writer.putSetting(WorldStandardValues.DECORATION_BOUNDS_CHECK, this.decorationBoundsCheck,
			"Set this to false to disable the bounds check during chunk decoration.",
			"While this allows you to spawn objects larger than 32x32, it also makes terrain generation dependent on the direction you explored the world in."
		);

		writer.header1("Carvers: Caves and Ravines");

		writer.putSetting(WorldStandardValues.CARVER_LAVA_BLOCK, this.carverLavaBlock,
			"Block that replaces all air blocks from Y0 up to CarverLavaBlockHeight.",
			"For example, vanilla replaces air in caves with lava up to Y10.",
			"Defaults to: LAVA"
		);

		writer.putSetting(WorldStandardValues.CARVER_LAVA_BLOCK_HEIGHT, this.carverLavaBlockHeight,
			"All air blocks are replaced to CarverLavaBlock from Y0 up to CarverLavaBlockHeight.",
			"For example, vanilla replaces air in caves with lava up to Y10.",
			"Defaults to: 10"
		);

		writer.header2("Caves");

		writer.putSetting(WorldStandardValues.CAVES_ENABLED, this.cavesEnabled,
			"Enables/disables OTG caves. OTG should automatically disable caves/carvers for biomes when modded carvers are detected."
		);
		
		writer.putSetting(WorldStandardValues.CAVE_RARITY, this.caveRarity,
			"This controls the odds that a given chunk will host a single cave and/or the start of a cave system."
		);

		writer.putSetting(WorldStandardValues.CAVE_FREQUENCY, this.caveFrequency,
			"The number of times the cave generation algorithm will attempt to create single caves and cave",
			"systems in the given chunk. This value is larger because the likelihood for the cave generation",
			"algorithm to bailout is fairly high and it is used in a randomizer that trends towards lower",
			"random numbers. With an input of 40 (default) the randomizer will result in an average random",
			"result of 5 to 6. This can be turned off by setting evenCaveDistribution (below) to true."
		);

		writer.putSetting(WorldStandardValues.CAVE_MIN_ALTITUDE, this.caveMinAltitude,
			"Sets the minimum and maximum altitudes at which caves will be generated. These values are",
			"used in a randomizer that trends towards lower numbers so that caves become more frequent",
			"the closer you get to the bottom of the map. Setting even cave distribution (above) to true",
			"will turn off this randomizer and use a flat random number generator that will create an even",
			"density of caves at all altitudes."
		);
		writer.putSetting(WorldStandardValues.CAVE_MAX_ALTITUDE, this.caveMaxAltitude);

		writer.putSetting(WorldStandardValues.INDIVIDUAL_CAVE_RARITY, this.individualCaveRarity,
			"The odds that the cave generation algorithm will generate a single cavern without an accompanying",
			"cave system. Note that whenever the algorithm generates an individual cave it will also attempt to",
			"generate a pocket of cave systems in the vicinity (no guarantee of connection or that the cave system",
			"will actually be created)."
		);

		writer.putSetting(WorldStandardValues.CAVE_SYSTEM_FREQUENCY, this.caveSystemFrequency,
			"The number of times the algorithm will attempt to start a cave system in a given chunk per cycle of",
			"the cave generation algorithm (see cave frequency setting above). Note that setting this value too",
			"high with an accompanying high cave frequency value can cause extremely long world generation time."
		);

		writer.putSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_CHANCE, this.caveSystemPocketChance,
			"This can be set to create an additional chance that a cave system pocket (a higher than normal",
			"density of cave systems) being started in a given chunk. Normally, a cave pocket will only be",
			"attempted if an individual cave is generated, but this will allow more cave pockets to be generated",
			"in addition to the individual cave trigger."
		);

		writer.putSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_MIN_SIZE, this.caveSystemPocketMinSize,
			"The minimum and maximum size that a cave system pocket can be. This modifies/overrides the",
			"cave system frequency setting (above) when triggered."
		);
		writer.putSetting(WorldStandardValues.CAVE_SYSTEM_POCKET_MAX_SIZE, this.caveSystemPocketMaxSize);

		writer.putSetting(WorldStandardValues.EVEN_CAVE_DISTRIBUTION, this.evenCaveDistribution,
			"Setting this to true will turn off the randomizer for cave frequency (above). Do note that",
			"if you turn this on you will probably want to adjust the cave frequency down to avoid long",
			"load times at world creation."
		);

		writer.header2("Ravines");

		writer.putSetting(WorldStandardValues.RAVINES_ENABLED, this.ravinesEnabled,
			"Enables/disables OTG ravines. OTG should automatically disable ravines/carvers for biomes when modded carvers are detected."
		);

		writer.putSetting(WorldStandardValues.RAVINE_RARITY, this.ravineRarity);
		writer.putSetting(WorldStandardValues.RAVINE_MIN_ALTITUDE, this.ravineMinAltitude);
		writer.putSetting(WorldStandardValues.RAVINE_MAX_ALTITUDE, this.ravineMaxAltitude);
		writer.putSetting(WorldStandardValues.RAVINE_MIN_LENGTH, this.ravineMinLength);
		writer.putSetting(WorldStandardValues.RAVINE_MAX_LENGTH, this.ravineMaxLength);
		writer.putSetting(WorldStandardValues.RAVINE_DEPTH, this.ravineDepth);

		writer.header1("Spawn point settings");

		writer.putSetting(WorldStandardValues.FIXED_SPAWN_POINT, this.fixedSpawnPoint,
			"Set this to true to enable SpawnPointX/SpawnPointY/SpawnPointZ/SpawnPointAngle."
		);
		writer.putSetting(WorldStandardValues.SPAWN_POINT_X, this.spawnPointX,
			"When FixedSpawnPoint: true, this sets the world's spawn point."
		);
		writer.putSetting(WorldStandardValues.SPAWN_POINT_Y, this.spawnPointY,
			"When FixedSpawnPoint: true, this sets the world's spawn point."		
		);
		writer.putSetting(WorldStandardValues.SPAWN_POINT_Z, this.spawnPointZ,
			"When FixedSpawnPoint: true, this sets the world's spawn point."				
		);
		writer.putSetting(WorldStandardValues.SPAWN_POINT_ANGLE, this.spawnPointAngle,
			"When FixedSpawnPoint: true, this sets the angle the player is looking when spawned at the spawn point."
		);
		
		writer.header2("Portal settings (Forge)");

		writer.putSetting(WorldStandardValues.PORTAL_BLOCKS, this.portalBlocks,
			"A list of one or more portal blocks used to build a portal to this dimension, or back to the overworld.",
			"Only applies for dimensions, not overworld/nether/end."
		);
		writer.putSetting(WorldStandardValues.PORTAL_COLOR, this.portalColor,
			"The portal color used for this world's portals, only applies for dimensions, not overworld/nether/end.",
			"Options: beige, black, blue, crystalblue, darkblue, darkgreen, darkred, emerald, flame, gold,",
			"green, grey, lightblue, lightgreen, orange, pink, red, white, yellow, default."
		);
		writer.putSetting(WorldStandardValues.PORTAL_MOB, this.portalMob,
			"The mob that spawns from this portal, minecraft:zombified_piglin by default.",
			"Only applies for dimensions, not overworld/nether/end."
		);
		writer.putSetting(WorldStandardValues.PORTAL_IGNITION_SOURCE, this.portalIgnitionSource,
			"The ignition source for this portal, minecraft:flint_and_steel by default.",
			"Only applies for dimensions, not overworld/nether/end."
		);

		writer.header1("Dimension settings (Forge)",
				"Note: At world creation, these settings are written to the world save's datapack folder (\\saves\\WorldName\\datapacks\\otg\\)",
				"as dimension_type json file. The json file is used by MC on world load to fetch the settings. If you want to change dimension",
				"settings for already created worlds make sure to edit the dimension_type json file, since changes to the WorldConfig dimension",
				"settings won't be picked up on world load, only on world creation."
		);

		writer.putSetting(WorldStandardValues.FIXED_TIME, !this.fixedTime.isPresent() ? WorldStandardValues.FIXED_TIME.getDefaultValue() : this.fixedTime.getAsLong(),
				"The time this dimension is fixed at, from 0 to 24000.",
				"-1 by default, meaning disabled, so time passes normally.",
				"Vanilla Nether uses 18000, End uses 6000."
		);
		writer.putSetting(WorldStandardValues.HAS_SKYLIGHT, this.hasSkyLight, 
			"Whether this dimension uses a skylight, defaults to true.",
			"Vanilla nether and end use false, nether combines this with AmbientLight:0.1."
		);
		writer.putSetting(WorldStandardValues.HAS_CEILING, this.hasCeiling,
			"Whether this dimension has a ceiling, affects mob spawning, weather (thunder), maps.",
			"Defaults to false, vanilla nether uses true."				
		);
		writer.putSetting(WorldStandardValues.ULTRA_WARM, this.ultraWarm,
			"Whether water evaporates in this dimension. Also appears to affect lava/lava flow.",
			"Defaults to false. Vanilla nether uses true."
		);
		writer.putSetting(WorldStandardValues.NATURAL, this.natural,
			"When set to false, mobs do not spawn from portals and players cannot use beds in this dimension.",
			"Defaults to true."
		);
		writer.putSetting(WorldStandardValues.COORDINATE_SCALE, this.coordinateScale,
			"The amount of blocks traveled compared to other dimensions.",
			"1 by default, same as vanilla overworld, nether uses 8."
		);
		writer.putSetting(WorldStandardValues.CREATE_DRAGON_FLIGHT, this.createDragonFight,
			"Probably starts a dragon fight, we think. Try it, what could possibly go wrong?"
		);
		writer.putSetting(WorldStandardValues.PIGLIN_SAFE, this.piglinSafe,
			"Whether this dimension can spawn piglins, false by default."
		);
		writer.putSetting(WorldStandardValues.BED_WORKS, this.bedWorks, 
			"Whether beds can be used to sleep and skip time in this dimension, true by default.");
		writer.putSetting(WorldStandardValues.RESPAWN_ANCHOR_WORKS, this.respawnAnchorWorks,
			"Whether RespawnAnchorBlocks can be used, false by default."
		);
		writer.putSetting(WorldStandardValues.HAS_RAIDS, this.hasRaids, 
			"Whether the dimension has raids, true by default."
		);
		writer.putSetting(WorldStandardValues.LOGICAL_HEIGHT, this.logicalHeight, 
			"World height, 256 by default. Affects portals and chorus fruits."
		);
		writer.putSetting(WorldStandardValues.INFINIBURN, this.infiniburn, 
			"Infiniburn block tag registry key, minecraft:infiniburn_overworld by default.",
			"Can be either overworld/nether/end (or potentially modded)."
		);
		writer.putSetting(WorldStandardValues.EFFECTS_LOCATION, this.effectsLocation, 
			"Effects registry key, minecraft:overworld by default.",
			"Can be either overworld/nether/end (or potentially modded)."
		);
		writer.putSetting(WorldStandardValues.AMBIENT_LIGHT, (double)this.ambientLight,
			"The base ambient light level for the world, 0.0 for overworld/end, 0.1 for nether."
		);
		
		writer.header1("Game rules (Forge)",
			"See: https://minecraft.fandom.com/wiki/Game_rule",
			"Since game rules are shared across all dimensions, these settings only apply if this preset is used as the overworld.",
			"These settings can be overridden via a DimensionConfig with a GameRules entry."
		);

		writer.putSetting(WorldStandardValues.OVERRIDE_GAME_RULES, this.overrideGameRules,
			"Set this to true to enable the settings below."
		);
		
		writer.putSetting(WorldStandardValues.DO_FIRE_TICK, this.doFireTick); 
		writer.putSetting(WorldStandardValues.MOB_GRIEFING, this.mobGriefing);
		writer.putSetting(WorldStandardValues.KEEP_INVENTORY, this.keepInventory);
		writer.putSetting(WorldStandardValues.DO_MOB_SPAWNING, this.doMobSpawning);
		writer.putSetting(WorldStandardValues.DO_MOB_LOOT, this.doMobLoot);
		writer.putSetting(WorldStandardValues.DO_TILE_DROPS, this.doTileDrops);
		writer.putSetting(WorldStandardValues.DO_ENTITY_DROPS, this.doEntityDrops);
		writer.putSetting(WorldStandardValues.COMMAND_BLOCK_OUTPUT, this.commandBlockOutput);
		writer.putSetting(WorldStandardValues.NATURAL_REGENERATION, this.naturalRegeneration);
		writer.putSetting(WorldStandardValues.DO_DAY_LIGHT_CYCLE, this.naturalRegeneration);
		writer.putSetting(WorldStandardValues.LOG_ADMIN_COMMANDS, this.logAdminCommands);
		writer.putSetting(WorldStandardValues.SHOW_DEATH_MESSAGES, this.showDeathMessages);
		writer.putSetting(WorldStandardValues.RANDOM_TICK_SPEED, this.randomTickSpeed);
		writer.putSetting(WorldStandardValues.SEND_COMMAND_FEEDBACK, this.sendCommandFeedback);
		writer.putSetting(WorldStandardValues.SPECTATORS_GENERATE_CHUNKS, this.spectatorsGenerateChunks);
		writer.putSetting(WorldStandardValues.SPAWN_RADIUS, this.spawnRadius);
		writer.putSetting(WorldStandardValues.DISABLE_ELYTRA_MOVEMENT_CHECK, this.disableElytraMovementCheck); 
		writer.putSetting(WorldStandardValues.MAX_ENTITY_CRAMMING, this.maxEntityCramming);
		writer.putSetting(WorldStandardValues.DO_WEATHER_CYCLE, this.doWeatherCycle);
		writer.putSetting(WorldStandardValues.DO_LIMITED_CRAFTING, this.doLimitedCrafting); 
		writer.putSetting(WorldStandardValues.MAX_COMMAND_CHAIN_LENGTH, this.maxCommandChainLength); 
		writer.putSetting(WorldStandardValues.ANNOUNCE_ADVANCEMENTS, this.announceAdvancements); 
		writer.putSetting(WorldStandardValues.DISABLE_RAIDS, this.disableRaids); 
		writer.putSetting(WorldStandardValues.DO_INSOMNIA, this.doInsomnia);
		writer.putSetting(WorldStandardValues.DROWNING_DAMAGE, this.drowningDamage); 
		writer.putSetting(WorldStandardValues.FALL_DAMAGE, this.fallDamage);
		writer.putSetting(WorldStandardValues.FIRE_DAMAGE, this.fireDamage);
		writer.putSetting(WorldStandardValues.DO_PATROL_SPAWNING, this.doPatrolSpawning);
		writer.putSetting(WorldStandardValues.DO_TRADER_SPAWNING, this.doTraderSpawning); 
		writer.putSetting(WorldStandardValues.FORGIVE_DEAD_PLAYERS, this.forgiveDeadPlayers); 
		writer.putSetting(WorldStandardValues.UNIVERSAL_ANGER, this.universalAnger);
	}
}
