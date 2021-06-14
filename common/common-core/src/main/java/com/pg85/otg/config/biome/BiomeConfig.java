package com.pg85.otg.config.biome;

import com.pg85.otg.OTG;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.settingType.Setting;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.resource.CustomObjectGen;
import com.pg85.otg.customobject.resource.CustomStructureGen;
import com.pg85.otg.customobject.resource.SaplingGen;
import com.pg85.otg.customobject.resource.TreeGen;
import com.pg85.otg.gen.resource.*;
import com.pg85.otg.gen.surface.SimpleSurfaceGenerator;
import com.pg85.otg.gen.surface.SurfaceGeneratorSetting;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.biome.BiomeResourceLocation;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.minecraft.EntityCategory;
import com.pg85.otg.util.minecraft.SaplingType;

import java.nio.file.Path;
import java.util.*;

/**
 * BiomeConfig (*.bc) classes
 * 
 * IBiomeConfig defines anything that's used/exposed between projects.
 * BiomeConfigBase implements anything needed for IBiomeConfig. 
 * BiomeConfig contains only fields/methods used for io/serialisation/instantiation.
 * 
 * BiomeConfig should be used only in common-core and platform-specific layers, when reading/writing settings on app start.
 * IBiomeConfig should be used wherever settings are used in code. 
 */
public class BiomeConfig extends BiomeConfigBase
{	
	public static final HashMap<String, Class<? extends ConfigFunction<?>>> CONFIG_FUNCTIONS = new HashMap<>();
	static
	{
		CONFIG_FUNCTIONS.put("AboveWaterRes", AboveWaterGen.class);
		CONFIG_FUNCTIONS.put("Boulder", BoulderGen.class);
		CONFIG_FUNCTIONS.put("Cactus", CactusGen.class);
		CONFIG_FUNCTIONS.put("Dungeon", DungeonGen.class);
		CONFIG_FUNCTIONS.put("Grass", GrassGen.class);
		CONFIG_FUNCTIONS.put("Fossil", FossilGen.class);
		CONFIG_FUNCTIONS.put("IceSpike", IceSpikeGen.class);
		CONFIG_FUNCTIONS.put("Liquid", LiquidGen.class);
		CONFIG_FUNCTIONS.put("Ore", OreGen.class);
		CONFIG_FUNCTIONS.put("Plant", PlantGen.class);
		CONFIG_FUNCTIONS.put("UnderWaterPlant", UnderWaterPlantGen.class);
		CONFIG_FUNCTIONS.put("Reed", ReedGen.class);
		CONFIG_FUNCTIONS.put("SmallLake", SmallLakeGen.class);
		CONFIG_FUNCTIONS.put("SurfacePatch", SurfacePatchGen.class);
		CONFIG_FUNCTIONS.put("UndergroundLake", UndergroundLakeGen.class);
		CONFIG_FUNCTIONS.put("UnderWaterOre", UnderWaterOreGen.class);
		CONFIG_FUNCTIONS.put("Vein", VeinGen.class);
		CONFIG_FUNCTIONS.put("Vines", VinesGen.class);
		CONFIG_FUNCTIONS.put("Well", WellGen.class);
		CONFIG_FUNCTIONS.put("CustomObject", CustomObjectGen.class);
		CONFIG_FUNCTIONS.put("CustomStructure", CustomStructureGen.class);		
		CONFIG_FUNCTIONS.put("Sapling", SaplingGen.class);		
		CONFIG_FUNCTIONS.put("Tree", TreeGen.class);
		CONFIG_FUNCTIONS.put("Bamboo", BambooGen.class);
		CONFIG_FUNCTIONS.put("Seagrass", SeagrassGen.class);
		CONFIG_FUNCTIONS.put("Kelp", KelpGen.class);
		CONFIG_FUNCTIONS.put("SeaPickle", SeaPickleGen.class);
		CONFIG_FUNCTIONS.put("Registry", RegistryGen.class);
		CONFIG_FUNCTIONS.put("CoralMushroom", CoralMushroomGen.class);
		CONFIG_FUNCTIONS.put("CoralTree", CoralTreeGen.class);
		CONFIG_FUNCTIONS.put("CoralClaw", CoralClawGen.class);
	}

	// This field is only used for reading/writing,
	// We read InheritMobsBiomeName from the raw settings when 
	// processing mob inheritance, rather than using this field.
	private String inheritMobsBiomeName;

	// TODO: Not used atm, implement these.
	private String biomeDictId;
	
	// Fields used only in common-core or platform layers that aren't in IBiomeConfig	
	// TODO: Refactor, expose via IBiomeConfig?	
	private Map<SaplingType, SaplingGen> saplingGrowers = new EnumMap<SaplingType, SaplingGen>(SaplingType.class);
	private Map<LocalMaterialData, SaplingGen> customSaplingGrowers = new HashMap<>();
	private Map<LocalMaterialData, SaplingGen> customBigSaplingGrowers = new HashMap<>();	

	// Private fields, only used when loading/saving

	private boolean doResourceInheritance = true;
	
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

	public BiomeConfig(String biomeName, BiomeConfigStub biomeConfigStub, Path settingsDir, SettingsMap settings, IWorldConfig worldConfig, String presetShortName, int presetVersion, String presetFolderName, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		super(biomeName, new BiomeResourceLocation(presetShortName, presetVersion, presetFolderName, biomeName));

		// Mob inheritance
		// Mob spawning data was already loaded seperately before the rest of the biomeconfig to make inheritance work properly
		// Forge: If this is a vanilla biome then mob spawning settings have been inherited from vanilla MC biomes
		// This includes any mobs added to vanilla biomes by other mods when MC started.

		if(biomeConfigStub != null)
		{
			spawnMonsters.addAll(biomeConfigStub.getSpawner(EntityCategory.MONSTER));
			spawnCreatures.addAll(biomeConfigStub.getSpawner(EntityCategory.CREATURE));
			spawnWaterCreatures.addAll(biomeConfigStub.getSpawner(EntityCategory.WATER_CREATURE));
			spawnAmbientCreatures.addAll(biomeConfigStub.getSpawner(EntityCategory.AMBIENT_CREATURE));
			spawnWaterAmbientCreatures.addAll(biomeConfigStub.getSpawner(EntityCategory.WATER_AMBIENT));
			spawnMiscCreatures.addAll(biomeConfigStub.getSpawner(EntityCategory.MISC));
			
			spawnMonstersMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.MONSTER));
			spawnCreaturesMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.CREATURE));
			spawnWaterCreaturesMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.WATER_CREATURE));
			spawnAmbientCreaturesMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.AMBIENT_CREATURE));
			spawnWaterAmbientCreaturesMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.WATER_AMBIENT));
			spawnMiscCreaturesMerged.addAll(biomeConfigStub.getSpawnerMerged(EntityCategory.MISC));
		}

		this.worldConfig = worldConfig;

		this.renameOldSettings(settings, logger, materialReader);
		this.readConfigSettings(settings, biomeResourcesManager, spawnLog, logger, materialReader);
		this.validateAndCorrectSettings(settingsDir, true, logger);

		// Set water level
		if (this.useWorldWaterLevel)
		{
			this.waterLevelMax = worldConfig.getWaterLevelMax();
			this.waterLevelMin = worldConfig.getWaterLevelMin();
			this.waterBlock = worldConfig.getWaterBlock();
			this.iceBlock = worldConfig.getIceBlock();
			this.cooledLavaBlock = worldConfig.getCooledLavaBlock();
		} else {
			this.waterLevelMax = this.configWaterLevelMax;
			this.waterLevelMin = this.configWaterLevelMin;
			this.waterBlock = this.configWaterBlock;
			this.iceBlock = this.configIceBlock;
			this.cooledLavaBlock = this.configCooledLavaBlock;
		}
	}
	
	public SaplingGen getSaplingGen(SaplingType type)
	{
		SaplingGen gen = saplingGrowers.get(type);
		if (gen == null && type.growsTree())
		{
			gen = saplingGrowers.get(SaplingType.All);
		}
		return gen;
	}

	public SaplingGen getCustomSaplingGen(LocalMaterialData materialData, boolean wideTrunk)
	{
		// TODO: Re-implement this when block data works
		if (wideTrunk)
		{
			return customBigSaplingGrowers.get(materialData);
			//return customBigSaplingGrowers.get(materialData.withBlockData(materialData.getBlockData() % 8));
		}
		return customSaplingGrowers.get(materialData);
		//return customSaplingGrowers.get(materialData.withBlockData(materialData.getBlockData() % 8));
	}	
	
	@Override
	protected void readConfigSettings(SettingsMap reader, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		this.biomeExtends = reader.getSetting(BiomeStandardValues.BIOME_EXTENDS, logger);
		this.biomeCategory = reader.getSetting(BiomeStandardValues.BIOME_CATEGORY, logger);
		this.doResourceInheritance = reader.getSetting(BiomeStandardValues.RESOURCE_INHERITANCE, logger);
		this.biomeSize = reader.getSetting(BiomeStandardValues.BIOME_SIZE, logger);
		this.biomeRarity = reader.getSetting(BiomeStandardValues.BIOME_RARITY, logger);
		this.biomeRarityWhenIsle = reader.getSetting(BiomeStandardValues.BIOME_RARITY_WHEN_ISLE, logger);
		this.biomeColor = reader.getSetting(BiomeStandardValues.BIOME_COLOR, logger);
		this.riverBiome = reader.getSetting(BiomeStandardValues.RIVER_BIOME, logger);
		this.isleInBiome = reader.getSetting(BiomeStandardValues.ISLE_IN_BIOME, logger);
		this.biomeSizeWhenIsle = reader.getSetting(BiomeStandardValues.BIOME_SIZE_WHEN_ISLE, logger);
		this.biomeIsBorder = reader.getSetting(BiomeStandardValues.BIOME_IS_BORDER, logger);
		this.notBorderNear = reader.getSetting(BiomeStandardValues.NOT_BORDER_NEAR, logger);
		this.biomeSizeWhenBorder = reader.getSetting(BiomeStandardValues.BIOME_SIZE_WHEN_BORDER, logger);
		this.biomeTemperature = reader.getSetting(BiomeStandardValues.BIOME_TEMPERATURE, logger);
		this.biomeWetness = reader.getSetting(BiomeStandardValues.BIOME_WETNESS, logger);
		this.vanillaBiome = reader.getSetting(BiomeStandardValues.VANILLA_BIOME, logger);
		this.biomeHeight = reader.getSetting(BiomeStandardValues.BIOME_HEIGHT, logger);
		this.biomeVolatility = reader.getSetting(BiomeStandardValues.BIOME_VOLATILITY, logger);
		this.smoothRadius = reader.getSetting(BiomeStandardValues.SMOOTH_RADIUS, logger);
		this.CHCSmoothRadius = reader.getSetting(BiomeStandardValues.CUSTOM_HEIGHT_CONTROL_SMOOTH_RADIUS, logger);		
		this.stoneBlock = reader.getSetting(BiomeStandardValues.STONE_BLOCK, logger, materialReader);
		this.surfaceBlock = reader.getSetting(BiomeStandardValues.SURFACE_BLOCK, logger, materialReader);
		this.groundBlock = reader.getSetting(BiomeStandardValues.GROUND_BLOCK, logger, materialReader);
		this.configWaterBlock = reader.getSetting(BiomeStandardValues.WATER_BLOCK, logger, materialReader);
		this.configIceBlock = reader.getSetting(BiomeStandardValues.ICE_BLOCK, logger, materialReader);
		this.configCooledLavaBlock = reader.getSetting(BiomeStandardValues.COOLED_LAVA_BLOCK, logger, materialReader);
		this.replacedBlocks = reader.getSetting(BiomeStandardValues.REPLACED_BLOCKS, logger, materialReader);
		this.sandStoneBlock = LocalMaterials.SANDSTONE;
		this.redSandStoneBlock = LocalMaterials.RED_SANDSTONE;
		this.surfaceAndGroundControl = reader.getSetting(SurfaceGeneratorSetting.SURFACE_AND_GROUND_CONTROL, new SimpleSurfaceGenerator(), logger, materialReader);
		this.useWorldWaterLevel = reader.getSetting(BiomeStandardValues.USE_WORLD_WATER_LEVEL, logger);
		this.configWaterLevelMax = reader.getSetting(BiomeStandardValues.WATER_LEVEL_MAX, logger);
		this.configWaterLevelMin = reader.getSetting(BiomeStandardValues.WATER_LEVEL_MIN, logger);
		this.skyColor = reader.getSetting(BiomeStandardValues.SKY_COLOR, logger);
		this.waterColor = reader.getSetting(BiomeStandardValues.WATER_COLOR, logger);
		this.grassColor = reader.getSetting(BiomeStandardValues.GRASS_COLOR, logger);
		this.grassColorModifier = reader.getSetting(BiomeStandardValues.GRASS_COLOR_MODIFIER, logger);
		this.foliageColor = reader.getSetting(BiomeStandardValues.FOLIAGE_COLOR, logger);
		this.fogColor = reader.getSetting(BiomeStandardValues.FOG_COLOR, logger);
		this.waterFogColor = reader.getSetting(BiomeStandardValues.WATER_FOG_COLOR, logger);
		this.particleType = reader.getSetting(BiomeStandardValues.PARTICLE_TYPE, logger);
		this.music = reader.getSetting(BiomeStandardValues.MUSIC, logger);
		this.musicMinDelay = reader.getSetting(BiomeStandardValues.MUSIC_MIN_DELAY, logger);
		this.musicMaxDelay = reader.getSetting(BiomeStandardValues.MUSIC_MAX_DELAY, logger);
		this.replaceCurrentMusic = reader.getSetting(BiomeStandardValues.REPLACE_CURRENT_MUSIC, logger);
		this.ambientSound = reader.getSetting(BiomeStandardValues.AMBIENT_SOUND, logger);
		this.moodSound = reader.getSetting(BiomeStandardValues.MOOD_SOUND, logger);
		this.moodSoundDelay = reader.getSetting(BiomeStandardValues.MOOD_SOUND_DELAY, logger);
		this.moodSearchRange = reader.getSetting(BiomeStandardValues.MOOD_SEARCH_RANGE, logger);
		this.moodOffset = reader.getSetting(BiomeStandardValues.MOOD_OFFSET, logger);
		this.additionsSound = reader.getSetting(BiomeStandardValues.ADDITIONS_SOUND, logger);
		this.additionsTickChance = reader.getSetting(BiomeStandardValues.ADDITIONS_TICK_CHANCE, logger);
		this.particleProbability = reader.getSetting(BiomeStandardValues.PARTICLE_PROBABILITY, logger);
		this.volatilityRaw1 = reader.getSetting(BiomeStandardValues.VOLATILITY_1, logger);
		this.volatilityRaw2 = reader.getSetting(BiomeStandardValues.VOLATILITY_2, logger);
		this.volatilityWeightRaw1 = reader.getSetting(BiomeStandardValues.VOLATILITY_WEIGHT_1, logger);
		this.volatilityWeightRaw2 = reader.getSetting(BiomeStandardValues.VOLATILITY_WEIGHT_2, logger);
		this.disableBiomeHeight = reader.getSetting(BiomeStandardValues.DISABLE_BIOME_HEIGHT, logger);
		this.maxAverageHeight = reader.getSetting(BiomeStandardValues.MAX_AVERAGE_HEIGHT, logger);
		this.maxAverageDepth = reader.getSetting(BiomeStandardValues.MAX_AVERAGE_DEPTH, logger);
		this.strongholdsEnabled = reader.getSetting(BiomeStandardValues.STRONGHOLDS_ENABLED, logger);
		this.oceanMonumentsEnabled = reader.getSetting(BiomeStandardValues.OCEAN_MONUMENTS_ENABLED, logger);
		this.woodLandMansionsEnabled = reader.getSetting(BiomeStandardValues.WOODLAND_MANSIONS_ENABLED, logger);
		this.netherFortressesEnabled = reader.getSetting(BiomeStandardValues.NETHER_FORTRESSES_ENABLED, logger);
		this.villageType = reader.getSetting(BiomeStandardValues.VILLAGE_TYPE, logger);
		this.villageSize = reader.getSetting(BiomeStandardValues.VILLAGE_SIZE, logger);		
		this.mineshaftType = reader.getSetting(BiomeStandardValues.MINESHAFT_TYPE, logger);
		this.rareBuildingType = reader.getSetting(BiomeStandardValues.RARE_BUILDING_TYPE, logger);
		this.buriedTreasureEnabled = reader.getSetting(BiomeStandardValues.BURIED_TREASURE_ENABLED, logger);
		this.shipWreckEnabled = reader.getSetting(BiomeStandardValues.SHIP_WRECK_ENABLED, logger);
		this.shipWreckBeachedEnabled = reader.getSetting(BiomeStandardValues.SHIP_WRECK_BEACHED_ENABLED, logger);
		this.pillagerOutpostEnabled = reader.getSetting(BiomeStandardValues.PILLAGER_OUTPOST_ENABLED, logger);
		this.bastionRemnantEnabled = reader.getSetting(BiomeStandardValues.BASTION_REMNANT_ENABLED, logger);
		this.netherFossilEnabled = reader.getSetting(BiomeStandardValues.NETHER_FOSSIL_ENABLED, logger);
		this.endCityEnabled = reader.getSetting(BiomeStandardValues.END_CITY_ENABLED, logger);		
		this.mineshaftProbability = reader.getSetting(BiomeStandardValues.MINESHAFT_PROBABILITY, logger);
		this.ruinedPortalType = reader.getSetting(BiomeStandardValues.RUINED_PORTAL_TYPE, logger);
		this.oceanRuinsType = reader.getSetting(BiomeStandardValues.OCEAN_RUINS_TYPE, logger);
		this.oceanRuinsLargeProbability = reader.getSetting(BiomeStandardValues.OCEAN_RUINS_LARGE_PROBABILITY, logger);
		this.oceanRuinsClusterProbability = reader.getSetting(BiomeStandardValues.OCEAN_RUINS_CLUSTER_PROBABILITY, logger);
		this.buriedTreasureProbability = reader.getSetting(BiomeStandardValues.BURIED_TREASURE_PROBABILITY, logger);
		this.pillagerOutpostSize = reader.getSetting(BiomeStandardValues.PILLAGER_OUTPOST_SIZE, logger);
		this.bastionRemnantSize = reader.getSetting(BiomeStandardValues.BASTION_REMNANT_SIZE, logger);		
		this.biomeDictId = reader.getSetting(BiomeStandardValues.BIOME_DICT_ID, logger);
		this.inheritMobsBiomeName = reader.getSetting(BiomeStandardValues.INHERIT_MOBS_BIOME_NAME, logger);

		this.readResourceSettings(reader, biomeResourcesManager, spawnLog, logger, materialReader);
		
		this.chcData = new double[this.worldConfig.getWorldHeightCap() / Constants.PIECE_Y_SIZE + 1];
		this.readHeightSettings(reader, this.chcData, BiomeStandardValues.CUSTOM_HEIGHT_CONTROL, BiomeStandardValues.CUSTOM_HEIGHT_CONTROL.getDefaultValue(), logger);
	}

	private void readHeightSettings(SettingsMap settings, double[] heightMatrix, Setting<double[]> setting, double[] defaultValue, ILogger logger)
	{
		double[] keys = settings.getSetting(setting, defaultValue, logger);
		for (int i = 0; i < heightMatrix.length && i < keys.length; i++)
		{
			heightMatrix[i] = keys[i];
		}
	}

	private void readResourceSettings(SettingsMap settings, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		// Disable resourceinheritance for saplings
		List<ConfigFunction<IBiomeConfig>> resources = new ArrayList<>(settings.getConfigFunctions(this, false, biomeResourcesManager, spawnLog, logger, materialReader));
		for (ConfigFunction<IBiomeConfig> res : resources)
		{
			if (res != null)
			{
				if (res instanceof SaplingGen)
				{
					SaplingGen sapling = (SaplingGen) res;
					if (sapling.saplingType == SaplingType.Custom)
					{
						try
						{	
							// Puts big custom saplings in the big list and small in the small list
							if (sapling.wideTrunk)
							{
								customBigSaplingGrowers.put(sapling.saplingMaterial, sapling);
							} else {
								customSaplingGrowers.put(sapling.saplingMaterial, sapling);
							}
						}
						catch (NullPointerException e)
						{
							OTG.log(LogMarker.WARN, "Unrecognized sapling type in biome "+ this.getName());
						}
					} else {
						this.saplingGrowers.put(sapling.saplingType, sapling);
					}
				}
			}
		}

		resources = new ArrayList<>(settings.getConfigFunctions(this, this.doResourceInheritance, biomeResourcesManager, spawnLog, logger, materialReader));
		for (ConfigFunction<IBiomeConfig> res : resources)
		{
			if (res != null)
			{
				if (!(res instanceof SaplingGen))
				{
					this.resourceSequence.add(res);
				}
				if(res instanceof CustomStructureGen)
				{
					this.customStructures.add((CustomStructureGen)res);
				}
			}
		}
	}

	@Override
	protected void writeConfigSettings(SettingsMap writer)
	{
		writer.header1("Biome Identity");
		
		writer.putSetting(BiomeStandardValues.VANILLA_BIOME, this.vanillaBiome,
			"When converting an existing OTG world to a vanilla world that can be",
			"used without OTG, this is the vanilla biome that replaces this biome.",
			"* Converting to vanilla biomes via console command is not yet",
			"  implemented, but is planned for the future.",
			"Example: minecraft:plains"
		);
		
		writer.putSetting(BiomeStandardValues.BIOME_DICT_ID, this.biomeDictId,
			"Forge Biome Dictionary ID used by other mods to identify a biome and",
			"place modded blocks, items and mobs in it."
		);

		writer.putSetting(BiomeStandardValues.BIOME_CATEGORY, this.biomeCategory,
				"Set a category for this biome, used by vanilla for... something",
				"Accepts one of the following values:",
				"none, taiga, extreme_hills, jungle, mesa, plains, savanna, icy, the_end, beach, forest, ocean, desert, river, swamp, mushroom, nether"
		);

		writer.header1("Biome Inheritance");

		writer.putSetting(BiomeStandardValues.BIOME_EXTENDS, this.biomeExtends,
			"The name of the BiomeConfig you wish to extend (if any).",
			"The extended config's settings are used as default values, and will",
			"overwrite any settings that are left empty in this config (excluding",
			"mob spawning, saplings and resources, which have their own inheritance",
			"settings)."
		);

		writer.putSetting(BiomeStandardValues.RESOURCE_INHERITANCE, this.doResourceInheritance,
			"When set to true, all resources of the extended biome (if any) will be copied",
			"to the resources queue of this biome, except for saplings. If a resource in",
			"the parent biome looks very similar to that of a child biome (for example,",
			"two ores of the same type), it won't be copied."
		);

		writer.header1("Biome placement");

		writer.putSetting(BiomeStandardValues.BIOME_SIZE, this.biomeSize,
			"Biome size from 0 to GenerationDepth. Defines in which biome layer this biome will be generated (see GenerationDepth).",
			"Higher numbers result in a smaller biome, lower numbers a larger biome.",
			"How this setting is used depends on the value of BiomeMode in the WorldConfig.",
			"It will be used for:",
			"- normal biomes, ice biomes, isle biomes and border biomes when BiomeMode is set to NoGroups",
			"- biomes spawned as part of a BiomeGroup when BiomeMode is set to Normal.",
			"  For biomes spawned as isles, borders or rivers other settings are available.",
			"  Isle biomes:   " + BiomeStandardValues.BIOME_SIZE_WHEN_ISLE + " (see below)",
			"  Border biomes: " + BiomeStandardValues.BIOME_SIZE_WHEN_BORDER + " (see below)",
			"  River biomes:  " + WorldStandardValues.RIVER_SIZE + " (see WorldConfig)"
		);

		writer.putSetting(BiomeStandardValues.BIOME_RARITY, this.biomeRarity,
			"Biome rarity from 100 to 1. If this is normal or ice biome - chance for spawn this biome then others.",
			"Example for normal biome :",
			"  100 rarity mean 1/6 chance than other ( with 6 default normal biomes).",
			"  50 rarity mean 1/11 chance than other",
			"For isle biomes see the " + BiomeStandardValues.BIOME_RARITY_WHEN_ISLE + " setting below.",
			"Doesn`t work on Ocean and River (frozen versions too) biomes when not added as normal biome."
		);

		writer.putSetting(BiomeStandardValues.BIOME_COLOR, this.biomeColor,
			"The hexadecimal color value of this biome. Used in the output of the /otg map command,",
			"and used in the input of BiomeMode: FromImage."
		);

		writer.header2("Isle biomes",
			"To spawn a biome as an isle, first add it to the",
			WorldStandardValues.ISLE_BIOMES + " list in the WorldConfig.",
			""
		);

		writer.putSetting(BiomeStandardValues.ISLE_IN_BIOME, this.isleInBiome,
			"List of biomes in which this biome will spawn as an isle.",
			"For example, Mushroom Isles spawn inside the Ocean biome."
		);

		writer.putSetting(BiomeStandardValues.BIOME_SIZE_WHEN_ISLE, this.biomeSizeWhenIsle,
			"Size of this biome when spawned as an isle biome in BiomeMode: Normal.",
			"Valid values range from 0 to GenerationDepth.",
			"Larger numbers give *smaller* islands. The biome must be smaller than the biome it's going",
			"to spawn in, so the " + BiomeStandardValues.BIOME_SIZE_WHEN_ISLE + " number must be larger than the " + BiomeStandardValues.BIOME_SIZE + " of the other biome."
		);

		writer.putSetting(BiomeStandardValues.BIOME_RARITY_WHEN_ISLE, this.biomeRarityWhenIsle,
			"Rarity of this biome when spawned as an isle biome in BiomeMode: Normal."
		);

		writer.smallTitle("Border biomes",
			"To spawn a biome as a border, first add it to the",
			WorldStandardValues.BORDER_BIOMES + " list in the WorldConfig.",
			""
		);

		writer.putSetting(BiomeStandardValues.BIOME_IS_BORDER, this.biomeIsBorder,
			"List of biomes this biome can be a border of.",
			"For example, the Beach biome is a border on the Ocean biome, so",
			"it can spawn anywhere on the border of an ocean."
		);

		writer.putSetting(BiomeStandardValues.NOT_BORDER_NEAR, this.notBorderNear,
			"List of biomes that cancel spawning of this biome.",
			"For example, the Beach biome will never spawn next to an Extreme Hills biome."
		);

		writer.putSetting(BiomeStandardValues.BIOME_SIZE_WHEN_BORDER, this.biomeSizeWhenBorder,
			"Size of this biome when spawned as a border biome in BiomeMode: Normal.",
			"Valid values range from 0 to GenerationDepth.",
			"Larger numbers give *smaller* borders. The biome must be smaller than the biome it's going",
			"to spawn in, so the " + BiomeStandardValues.BIOME_SIZE_WHEN_BORDER
			+ " number must be larger than the " + BiomeStandardValues.BIOME_SIZE + " of the other biome."
		);

		writer.header1("Terrain height and volatility");

		writer.putSetting(BiomeStandardValues.BIOME_HEIGHT, this.biomeHeight,
			"BiomeHeight defines how much height will be added during terrain generation",
			"Must be between -10.0 and 10.0",
			"Value 0.0 is equivalent to half of map height with all other settings at defaults."
		);

		writer.putSetting(BiomeStandardValues.BIOME_VOLATILITY, this.biomeVolatility,
			"Biome volatility."
		);

		writer.putSetting(BiomeStandardValues.SMOOTH_RADIUS, this.smoothRadius,
			"Smooth radius between biomes. Must be between 0 and 32, inclusive. The resulting",
			"smooth radius seems to be  (thisSmoothRadius + 1 + smoothRadiusOfBiomeOnOtherSide) * 4 .",
			"So if two biomes next to each other have both a smooth radius of 2, the",
			"resulting smooth area will be (2 + 1 + 2) * 4 = 20 blocks wide."
		);

		writer.putSetting(BiomeStandardValues.CUSTOM_HEIGHT_CONTROL_SMOOTH_RADIUS, this.CHCSmoothRadius,
			"Works the same way as SmoothRadius but only acts on CustomHeightControl. Must be between 0 and 32, inclusive.",
			"Does nothing if Custom Height Control smoothing is not enabled in the world config."
		);

		writer.putSetting(BiomeStandardValues.MAX_AVERAGE_HEIGHT, this.maxAverageHeight,
			"If this value is greater than 0, then it will affect how much, on average, the terrain will rise before leveling off when it begins to increase in elevation.",
			"If the value is less than 0, then it will cause the terrain to either increase to a lower height before leveling out or decrease in height if the value is a large enough negative."
		);

		writer.putSetting(BiomeStandardValues.MAX_AVERAGE_DEPTH, this.maxAverageDepth,
			"If this value is greater than 0, then it will affect how much, on average, the terrain (usually at the ottom of the ocean) will fall before leveling off when it begins to decrease in elevation. ",
			"If the value is less than 0, then it will cause the terrain to either fall to a lesser depth before leveling out or increase in height if the value is a large enough negative."
		);

		writer.putSetting(BiomeStandardValues.VOLATILITY_1, this.volatilityRaw1,
			"Another type of noise. This noise is independent from biomes. The larger the values the more chaotic/volatile landscape generation becomes.",
			"Setting the values to negative will have the opposite effect and make landscape generation calmer/gentler."
		);
		
		writer.putSetting(BiomeStandardValues.VOLATILITY_2, this.volatilityRaw2);

		writer.putSetting(BiomeStandardValues.VOLATILITY_WEIGHT_1, this.volatilityWeightRaw1,
			"Adjust the weight of the corresponding volatility settings. This allows you to change how prevalent you want either of the volatility settings to be in the terrain."
		);

		writer.putSetting(BiomeStandardValues.VOLATILITY_WEIGHT_2, this.volatilityWeightRaw2);

		writer.putSetting(BiomeStandardValues.DISABLE_BIOME_HEIGHT, this.disableBiomeHeight,
			"Disable all noises except Volatility1 and Volatility2. Also disable default block chance from height."
		);

		writer.putSetting(BiomeStandardValues.CUSTOM_HEIGHT_CONTROL, this.chcData,
			"List of custom height factors, 17 double entries, each controls about 7",
			"blocks height, starting at the bottom of the world. Positive entry - larger chance of spawn blocks, negative - smaller",
			"Values which affect your configuration may be found only experimentally. Values may be very big, like ~3000.0 depends from height",
			"Example:",
			"  CustomHeightControl:0.0,-2500.0,0.0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0",
			"Makes empty layer above bedrock layer. "
		);

		writer.header1("Rivers");

		writer.putSetting(BiomeStandardValues.RIVER_BIOME, this.riverBiome,
			"The biome used as the river biome."
		);

		writer.header1("Blocks");

		writer.putSetting(BiomeStandardValues.STONE_BLOCK, this.stoneBlock,
			"The stone block used for the biome, usually STONE."
		);

		writer.putSetting(BiomeStandardValues.SURFACE_BLOCK, this.surfaceBlock,
			"The surface block used for the biome, usually GRASS."
		);

		writer.putSetting(BiomeStandardValues.GROUND_BLOCK, this.groundBlock,
			"The ground block used for the biome, usually DIRT."
		);

		writer.putSetting(SurfaceGeneratorSetting.SURFACE_AND_GROUND_CONTROL, this.surfaceAndGroundControl,
			"Setting for biomes with more complex surface and ground blocks.",
			"Each column in the world has a noise value from what appears to be -7 to 7.",
			"Values near 0 are more common than values near -7 and 7. This setting is",
			"used to change the surface block based on the noise value for the column.",
			"Syntax: SurfaceBlockName,GroundBlockName,MaxNoise,[AnotherSurfaceBlockName,[AnotherGroundBlockName,MaxNoise[,...]]",
			"Example: " + SurfaceGeneratorSetting.SURFACE_AND_GROUND_CONTROL + ": STONE,STONE,-0.8,GRAVEL,STONE,0.0,DIRT,DIRT,10.0",
			"  When the noise is below -0.8, stone is the surface and ground block, between -0.8 and 0",
			"  gravel with stone just below and between 0.0 and 10.0 there's only dirt.",
			"  Because 10.0 is higher than the noise can ever get, the normal " + BiomeStandardValues.SURFACE_BLOCK,
			"  and " + BiomeStandardValues.GROUND_BLOCK + " will never appear in this biome.",
			"",
			"Alternatively, you can use Mesa, MesaForest or MesaBryce to get blocks",
			"like the blocks found in the Mesa biomes.",
			"You can also use Iceberg to get iceberg generation like in vanilla frozen oceans."
		);

		writer.putSetting(BiomeStandardValues.REPLACED_BLOCKS, replacedBlocks,
			"Replace Variable: (blockFrom,blockTo[:blockDataTo][,minHeight,maxHeight])",
			"Example :",
			"  ReplacedBlocks: (GRASS,DIRT,100,127),(GRAVEL,GLASS)",
			"Replace grass block to dirt from 100 to 127 height and replace gravel to glass on all height ",
			"Only the following biome resources are affected: CustomObject, CustomStructure, Ore, UnderWaterOre, ",
			"Vein, SurfacePatch, Boulder, IceSpike.",
			"BO's used as CustomObject/CustomStructure may have DoReplaceBlocks:false to save performance."
		);

		writer.header2("Water / Lava & Frozen States");

		writer.putSetting(BiomeStandardValues.USE_WORLD_WATER_LEVEL, this.useWorldWaterLevel,
			"Set this to false to use the \"Water / Lava & Frozen States\" settings of this biome."
		);

		writer.putSetting(BiomeStandardValues.WATER_LEVEL_MAX, this.configWaterLevelMax,
			"Set water level. Every empty between this levels will be fill water or another block from WaterBlock."
		);

		writer.putSetting(BiomeStandardValues.WATER_LEVEL_MIN, this.configWaterLevelMin);

		writer.putSetting(BiomeStandardValues.WATER_BLOCK, this.configWaterBlock,
			"The block used when placing water in the biome."
		);

		writer.putSetting(BiomeStandardValues.ICE_BLOCK, this.configIceBlock,
			"The block used as ice. Ice only spawns if the BiomeTemperature is low enough."
		);

		writer.putSetting(WorldStandardValues.COOLED_LAVA_BLOCK, this.cooledLavaBlock,
			"The block used as cooled or frozen lava.",
			"Set this to OBSIDIAN for \"frozen\" lava lakes in cold biomes"
		);

		writer.header1("Visuals and weather");

		writer.putSetting(BiomeStandardValues.BIOME_TEMPERATURE, this.biomeTemperature,
			"Biome temperature. Float value from 0.0 to 2.0.",
			"When this value is around 0.2, snow will fall on mountain peaks above y=90.",
			"When this value is around 0.1, the whole biome will be covered in snow and ice.",
			"However, on default biomes, this won't do anything except changing the grass and leaves colors slightly."
		);

		writer.putSetting(BiomeStandardValues.BIOME_WETNESS, this.biomeWetness,
			"Biome wetness. Float value from 0.0 to 1.0.",
			"If this biome is a custom biome, and this value is set to 0, no rain will fall.",
			"On default biomes, this won't do anything except changing the grass and leaves colors slightly."
		);

		writer.putSetting(BiomeStandardValues.SKY_COLOR, this.skyColor,
			"Biome sky color."
		);

		writer.putSetting(BiomeStandardValues.WATER_COLOR, this.waterColor,
			"Biome water color multiplier."
		);
		
		writer.putSetting(BiomeStandardValues.GRASS_COLOR, this.grassColor,
			"Biome grass color."
		);
		
		writer.putSetting(BiomeStandardValues.GRASS_COLOR_MODIFIER, this.grassColorModifier,
			"Biome grass color modifier, can be None, Swamp or DarkForest."
		);

		writer.putSetting(BiomeStandardValues.FOLIAGE_COLOR, this.foliageColor,
			"Biome foliage color."
		);

		writer.putSetting(BiomeStandardValues.FOG_COLOR, this.fogColor, "Biome fog color.");
		
		writer.putSetting(BiomeStandardValues.WATER_FOG_COLOR, this.waterFogColor, "Biome water fog color.");

		writer.putSetting(BiomeStandardValues.PARTICLE_TYPE, this.particleType,
			"Biome particle type, for example minecraft:white_ash.",
			"Use the \"otg particles\" console command to get a list of particles."
		);
		
		writer.putSetting(BiomeStandardValues.PARTICLE_PROBABILITY, this.particleProbability,
			"Biome particle probability, 0 by default.",
			"*TODO: Test different values and document usage."
		);

		writer.putSetting(BiomeStandardValues.MUSIC, this.music,
			"Music for the biome, takes a resource location. Leave empty to disable. Examples: ",
				"Music: minecraft:music_disc.cat",
				"Music: minecraft:music.nether.basalt_deltas"
		);

		writer.putSetting(BiomeStandardValues.MUSIC_MIN_DELAY, this.musicMinDelay,
				"Minimum delay for music to start, in ticks"
		);

		writer.putSetting(BiomeStandardValues.MUSIC_MAX_DELAY, this.musicMaxDelay,
				"Maximum delay for music to start, in ticks"
		);

		writer.putSetting(BiomeStandardValues.REPLACE_CURRENT_MUSIC, this.replaceCurrentMusic,
				"Whether music replaces the current playing music in the client or not"
		);

		writer.putSetting(BiomeStandardValues.AMBIENT_SOUND, this.ambientSound,
				"Ambient sound for the biome. Leave empty to disable. Example:",
				"AmbientSound: minecraft:ambient.cave"
		);

		writer.putSetting(BiomeStandardValues.MOOD_SOUND, this.moodSound,
				"Mood sound for the biome. Leave empty to disable. Example:",
				"MoodSound: minecraft:ambient.crimson_forest.mood"
		);

		writer.putSetting(BiomeStandardValues.MOOD_SOUND_DELAY, this.moodSoundDelay,
				"The delay in ticks between triggering mood sound"
		);

		writer.putSetting(BiomeStandardValues.MOOD_SEARCH_RANGE, this.moodSearchRange,
				"How far from the player a mood sound can play"
		);

		writer.putSetting(BiomeStandardValues.MOOD_OFFSET, this.moodOffset,
				"The offset of the sound event"
		);

		writer.putSetting(BiomeStandardValues.ADDITIONS_SOUND, this.additionsSound,
				"Additions sound for the biome. Leave empty to disable. Example:",
				"AdditionsSound: minecraft:ambient.soul_sand_valley.additions"
		);

		writer.putSetting(BiomeStandardValues.ADDITIONS_TICK_CHANCE, this.additionsTickChance,
				"The tick chance that the additions sound plays"
		);

		writer.header1("Resource queue",
			"This section controls all resources spawning during population.",
			"The resources will be placed in this order.",
			"",
			"Keep in mind that a high size, frequency or rarity may slow down terrain generation.",
			"",
			"Possible resources:",
			"AboveWaterRes(BlockName,Frequency,Rarity)",
			"Boulder(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..]",
			"Cactus(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",			
			"CustomObject(Object[,AnotherObject[,...]])",
			"CustomStructure([Object,Object_Chance[,AnotherObject,Object_Chance[,...]]])",
			"Dungeon(Frequency,Rarity,MinAltitude,MaxAltitude)",			
			"Fossil(Rarity)",
			"Grass(PlantType,Grouped/NotGrouped,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])",
			"IceSpike(BlockName,IceSpikeType,Frequency,Rarity,MinAltitude,MaxAltitude,Blocksource[,BlockSource2,...])",			
			"Liquid(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
			"Ore(BlockName,Size,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",			
			"Plant(PlantType,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",
			"Reed(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",			
			"SmallLake(BlockName,Frequency,Rarity,MinAltitude,MaxAltitude)",			
			"SurfacePatch(BlockName,DecorationBlockName,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....]",
			"Tree(Frequency,TreeType,TreeTypeChance[,AdditionalTreeType,AdditionalTreeTypeChance.....])",
			"UnderGroundLake(MinSize,MaxSize,Frequency,Rarity,MinAltitude,MaxAltitude)",
			"UnderWaterOre(BlockName,Size,Frequency,Rarity,BlockSource[,BlockSource2,BlockSource3.....])",			
			"UnderWaterPlant(PlantType,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,BlockSource3.....])",					
			"Vein(BlockName,MinRadius,MaxRadius,Rarity,OreSize,OreFrequency,OreRarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])",			
			"Vines(Frequency,Rarity,MinAltitude,MaxAltitude)",
			"Well(BaseBlockName,HalfSlabBlockName,WaterBlockName,Frequency,Rarity,MinAltitude,MaxAltitude,BlockSource[,BlockSource2,..])",		
			"",
			"BlockName:	  	The name of the block, can include data.",
			"BlockSource:	List of blocks the resource can spawn on/in. You can also use \"Solid\" or \"All\".",
			"Frequency:	  	Number of attempts to place this resource in each chunk.",
			"Rarity:		Chance for each attempt, Rarity:100 - mean 100% to pass, Rarity:1 - mean 1% to pass.",
			"MinAltitude and MaxAltitude: Height limits.",
			"TreeType:	   	Tree (original oak tree) - BigTree - Birch - TallBirch - SwampTree -",
			"				HugeMushroom (randomly red or brown) - HugeRedMushroom - HugeBrownMushroom -",
			"				Taiga1 - Taiga2 - HugeTaiga1 - HugeTaiga2 -",
			"				JungleTree (the huge jungle tree) - GroundBush - CocoaTree (smaller jungle tree)",
			"				DarkOak (from the roofed forest biome) - Acacia",
			"				You can also use your own custom objects, as long as they have Tree:true in their settings.",
			"TreeTypeChance: Similar to Rarity. Example:",
			"				Tree(10,Taiga1,35,Taiga2,100) - tries 10 times, for each attempt it tries to place Taiga1 (35% chance),",
			"				if that fails, it attempts to place Taiga2 (100% chance).",
			"PlantType:	  	One of the plant types: " + StringHelper.join(PlantType.values(), ", "),
			"				or a block name",
			"IceSpikeType:  One of the ice spike types: " + StringHelper.join(IceSpikeGen.SpikeType.values(), ","),
			"Object:		Any custom object (bo2 or bo3) file but without the file extension. ",
			"",
			"Plant and Grass resource: Both a resource of one block. Plant can place blocks underground, Grass cannot.",
			"UnderWaterPlant resource: Similar to plant, but places blocks underwater.",
			"Liquid resource: A one-block water or lava source",
			"SmallLake and UnderGroundLake resources: Small lakes of about 8x8 blocks",
			"Vein resource: Starts an area where ores will spawn. Can be slow, so use a low Rarity (smaller than 1).",
			"CustomStructure resource: Starts a BO3 or BO4 structure in the chunk if spawn requirements are met.",
			""
		);
		writer.addConfigFunctions(this.resourceSequence);

		writer.header1("Sapling resource",
			Constants.MOD_ID + " allows you to grow your custom objects from saplings, instead",
			"of the vanilla trees. Add one or more Sapling functions here to override vanilla",
			"spawning for that sapling.",
			"",
			"The syntax is: Sapling(SaplingType,TreeType,TreeType_Chance[,Additional_TreeType,Additional_TreeType_Chance.....])",
			"Works like Tree resource instead first parameter.",
			"For custom saplings (Forge only); Sapling(Custom,SaplingMaterial,WideTrunk,TreeType,TreeType_Chance.....)",
			"SaplingMaterial is a material name from a mod.",
			"WideTrunk is 'true' or 'false', whether or not it requires 4 saplings.",
			"",
			"Sapling types: " + StringHelper.join(SaplingType.values(), ", "),
			"All - will make the tree spawn from all saplings, but not from mushrooms.",
			"BigJungle - for when 4 jungle saplings grow at once.",
			"RedMushroom/BrownMushroom - will only grow when bonemeal is used.",
			""
		);

		writer.addConfigFunctions(this.saplingGrowers.values());
		writer.addConfigFunctions(this.customSaplingGrowers.values());
		writer.addConfigFunctions(this.customBigSaplingGrowers.values());

		writer.header1("Vanilla structures",
			"Vanilla structure settings, each structure type has a global on/off", 
			"toggle in the WorldConfig, be sure to enable it to allow biomes to",
			"spawn structures.",
			"* Fossils and Dungeons count as resources, not structures."
		);

		writer.putSetting(BiomeStandardValues.STRONGHOLDS_ENABLED, strongholdsEnabled,
			"Toggles strongholds spawning in this biome."
		);

		writer.putSetting(BiomeStandardValues.WOODLAND_MANSIONS_ENABLED, woodLandMansionsEnabled,
			"Toggles woodland mansions spawning in this biome."
		);

		writer.putSetting(BiomeStandardValues.OCEAN_MONUMENTS_ENABLED, oceanMonumentsEnabled,
			"Toggles ocean monuments spawning in this biome."
		);
		
		writer.putSetting(BiomeStandardValues.NETHER_FORTRESSES_ENABLED, netherFortressesEnabled,
			"Toggles nether fortresses spawning in this biome."
		);
		
		writer.putSetting(BiomeStandardValues.VILLAGE_TYPE, villageType,
			"The type of villages in this biome. Can be wood, sandstone, taiga, savanna, snowy or disabled."
		);

		writer.putSetting(BiomeStandardValues.VILLAGE_SIZE, villageSize,
			"The size of villages in this biome, 6 by default.",
			"*TODO: Test different values and document usage."
		);
		
		writer.putSetting(BiomeStandardValues.MINESHAFT_TYPE, mineshaftType,
			"The type of mineshafts in this biome. Can be normal, mesa or disabled."
		);

		writer.putSetting(BiomeStandardValues.MINESHAFT_PROBABILITY, mineshaftProbability,
			"Probability of mineshafts spawning, 0.004 by default.", 
			"*TODO: Test different values and document usage."
		);
		
		writer.putSetting(BiomeStandardValues.RARE_BUILDING_TYPE, rareBuildingType,
			"The type of the aboveground rare building in this biome.", 
			"Can be desertPyramid, jungleTemple, swampHut, igloo or disabled."
		);
		
		writer.putSetting(BiomeStandardValues.BURIED_TREASURE_ENABLED, buriedTreasureEnabled,
			"Toggles buried treasure spawning in this biome."
		);

		writer.putSetting(BiomeStandardValues.BURIED_TREASURE_PROBABILITY, buriedTreasureProbability,
			"Probability of buried treasure spawning, 0.01 by default.",
			"*TODO: Test different values and document usage."
		);
		
		writer.putSetting(BiomeStandardValues.SHIP_WRECK_ENABLED, shipWreckEnabled,
			"Toggles shipwrecks spawning in this biome."
		);

		writer.putSetting(BiomeStandardValues.SHIP_WRECK_BEACHED_ENABLED, shipWreckBeachedEnabled,
			"Toggles beached shipwrecks spawning in this biome."
		);
		
		writer.putSetting(BiomeStandardValues.PILLAGER_OUTPOST_ENABLED, pillagerOutpostEnabled,
			"Toggles pillager outposts spawning in this biome."
		);

		writer.putSetting(BiomeStandardValues.PILLAGER_OUTPOST_SIZE, pillagerOutpostSize,
			"The size of pillager outposts in this biome, 7 by default.",
			"*TODO: Test different values and document usage."
		);
		
		writer.putSetting(BiomeStandardValues.BASTION_REMNANT_ENABLED, bastionRemnantEnabled,
			"Toggles bastion remnants spawning in this biome."
		);

		writer.putSetting(BiomeStandardValues.BASTION_REMNANT_SIZE, bastionRemnantSize,
			"The size of bastion remnants in this biome, 6 by default.",
			"*TODO: Test different values and document usage."
		);
		
		writer.putSetting(BiomeStandardValues.NETHER_FOSSIL_ENABLED, netherFossilEnabled,
			"Toggles nether fossils spawning in this biome.",
			"Caution: Nether fossils spawn at all heights."
		);
		
		writer.putSetting(BiomeStandardValues.END_CITY_ENABLED, endCityEnabled,
			"Toggles end cities spawning in this biome."
		);
		
		writer.putSetting(BiomeStandardValues.RUINED_PORTAL_TYPE, ruinedPortalType,
			"The type of ruined portals in this biome.", 
			"Can be normal, desert, jungle, swamp, mountain, ocean, nether or disabled."
		);
		
		writer.putSetting(BiomeStandardValues.OCEAN_RUINS_TYPE, oceanRuinsType,
			"The type of ocean ruins in this biome.", 
			"Can be cold, warm or disabled."
		);

		writer.putSetting(BiomeStandardValues.OCEAN_RUINS_LARGE_PROBABILITY, oceanRuinsLargeProbability,
			"Probability of large ocean ruins spawning, 0.3 by default.",
			"*TODO: Test different values and document usage."
		);

		writer.putSetting(BiomeStandardValues.OCEAN_RUINS_CLUSTER_PROBABILITY, oceanRuinsClusterProbability,
			"Probability of ocean ruins spawning clusters, 0.9 by default.",
			"*TODO: Test different values and document usage."
		);
		
		writer.header1("Mob spawning",
			"Mob spawning is configured via mob groups, see http://minecraft.gamepedia.com/Spawn#Mob_spawning",
			"",
			"A mobgroups is made of four parts; mob name, weight, min and max.",
			"- Mob name is one of the Minecraft internal mob names. See http://minecraft.gamepedia.com/Chunk_format#Mobs",
			"- Weight is used for a random selection. Must be a positive number.",
			"- Min is the minimum amount of mobs spawning as a group. Must be a positive number.",
			"- Max is the maximum amount of mobs spawning as a group. Must be a positive number.",
			"",
			"Mob groups are written to the config files as Json.",
			"Json is a tree document format: http://en.wikipedia.org/wiki/JSON",
			"Syntax: {\"mob\": \"mobname\", \"weight\": integer, \"min\": integer, \"max\": integer}",
			"Example: {\"mob\": \"minecraft:ocelot\", \"weight\": 10, \"min\": 2, \"max\": 6}",
			"Example: {\"mob\": \"minecraft:mooshroom\", \"weight\": 5, \"min\": 2, \"max\": 2}",
			"A json list of mobgroups looks like this: [ mobgroup, mobgroup, mobgroup... ]",
			"This would be an ampty list: []",
			"You can validate your json here: http://jsonlint.com/",
			"",
			"There are six categories of mobs: monsters, creatures, water creatures, ambient creatures, water ambient creatures and miscellaneous.",
			"You can add your own mobs to the mobgroups below, mobs may only work when used in specific categories, depending on their type.",
			"To see the mob category a mob belongs to, use /otg entities. The mob's category (if any) is listed after its name.",
			"Also supports modded mobs, if they are of the correct mob category."
		);

		writer.putSetting(BiomeStandardValues.SPAWN_MONSTERS, this.spawnMonsters,
			"The monsters (blazes, cave spiders, creepers, drowned, elder guardians, ender dragons, endermen, endermites, evokers, ghasts, giants,",
			"guardians, hoglins, husks, illusioners, magma cubes, phantoms, piglins, pillagers, ravagers, shulkers, silverfishes, skeletons, slimes,",
			"spiders, strays, vexes, vindicators, witches, zoglins, zombies, zombie villagers, zombified piglins) that spawn in this biome.",
			"For instance [{\"mob\": \"minecraft:spider\", \"weight\": 100, \"min\": 4, \"max\": 4}, {\"mob\": \"minecraft:zombie\", \"weight\": 100, \"min\": 4, \"max\": 4}]",
			"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
			"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
		);

		writer.putSetting(BiomeStandardValues.SPAWN_CREATURES, this.spawnCreatures,
			"The friendly creatures (bees, cats, chickens, cows, donkeys, foxes, horses, llama, mooshrooms, mules, ocelots, panda's, parrots,",
			"pigs, polar bears, rabbits, sheep, skeleton horses, striders, trader llama's, turtles, wandering traders, wolves, zombie horses)",
			"that spawn in this biome.",
			"For instance [{\"mob\": \"minecraft:sheep\", \"weight\": 12, \"min\": 4, \"max\": 4}, {\"mob\": \"minecraft:pig\", \"weight\": 10, \"min\": 4, \"max\": 4}]",
			"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
			"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
		);

		writer.putSetting(BiomeStandardValues.SPAWN_WATER_CREATURES, this.spawnWaterCreatures,
			"The water creatures (squids and dolphins) that spawn in this biome",
			"For instance [{\"mob\": \"minecraft:squid\", \"weight\": 10, \"min\": 4, \"max\": 4}]",
			"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
			"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
		);

		writer.putSetting(BiomeStandardValues.SPAWN_AMBIENT_CREATURES, this.spawnAmbientCreatures,
			"The ambient creatures (only bats in vanila) that spawn in this biome",
			"For instance [{\"mob\": \"minecraft:bat\", \"weight\": 10, \"min\": 8, \"max\": 8}]",
			"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
			"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
		);

		writer.putSetting(BiomeStandardValues.SPAWN_WATER_AMBIENT_CREATURES, this.spawnWaterAmbientCreatures,
			"The ambient water creatures (cod, pufferfish, salmon, tropical fish) that spawn in this biome",
			"For instance [{\"mob\": \"minecraft:cod\", \"weight\": 10, \"min\": 8, \"max\": 8}]",
			"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
			"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
		);
		
		writer.putSetting(BiomeStandardValues.SPAWN_MISC_CREATURES, this.spawnMiscCreatures,
			"The miscellaneous creatures (iron golems, snow golems and villagers) that spawn in this biome",
			"For instance [{\"mob\": \"minecraft:villager\", \"weight\": 10, \"min\": 8, \"max\": 8}]",
			"Use the \"/otg entities\" console command to get a list of possible mobs and mob categories.",
			"Use the \"/otg biome -m\" console command to get the list of registered mobs for a biome."
		);

		writer.putSetting(BiomeStandardValues.INHERIT_MOBS_BIOME_NAME, inheritMobsBiomeName,
			"Inherit the internal mobs list of another biome. Inherited mobs can be overridden using",
			"the mob spawn settings in this biome config. Any mob type defined in this biome config", 
			"will override inherited mob settings for the same mob in the same mob category.",
			"Use this setting to inherit mob spawn lists from other biomes.",
			"Accepts both OTG and non-OTG (vanilla or other mods') biomes. See also: BiomeDictId."
		);
	}

	@Override
	protected void validateAndCorrectSettings(Path settingsDir, boolean logWarnings, ILogger logger)
	{
		this.biomeExtends = (this.biomeExtends == null || this.biomeExtends.equals("null")) ? "" : this.biomeExtends;
		this.biomeSize = lowerThanOrEqualTo(biomeSize, worldConfig.getGenerationDepth());
		this.biomeSizeWhenIsle = lowerThanOrEqualTo(biomeSizeWhenIsle, worldConfig.getGenerationDepth());
		this.biomeSizeWhenBorder = lowerThanOrEqualTo(biomeSizeWhenBorder, worldConfig.getGenerationDepth());
		this.biomeRarity = lowerThanOrEqualTo(biomeRarity, worldConfig.getBiomeRarityScale());
		this.biomeRarityWhenIsle = lowerThanOrEqualTo(biomeRarityWhenIsle, worldConfig.getBiomeRarityScale());
		this.isleInBiome = filterBiomes(this.isleInBiome, this.worldConfig.getWorldBiomes());
		this.biomeIsBorder = filterBiomes(this.biomeIsBorder, this.worldConfig.getWorldBiomes());
		this.notBorderNear = filterBiomes(this.notBorderNear, this.worldConfig.getWorldBiomes());
		this.volatility1 = this.volatilityRaw1 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw1) + 1.0D) : this.volatilityRaw1 + 1.0D;
		this.volatility2 = this.volatilityRaw2 < 0.0D ? 1.0D / (Math.abs(this.volatilityRaw2) + 1.0D) : this.volatilityRaw2 + 1.0D;
		this.volatilityWeight1 = (this.volatilityWeightRaw1 - 0.5D) * 24.0D;
		this.volatilityWeight2 = (0.5D - this.volatilityWeightRaw2) * 24.0D;
		this.waterLevelMax = higherThanOrEqualTo(waterLevelMax, this.waterLevelMin);
	}

	@Override
	protected void renameOldSettings(SettingsMap settings, ILogger logger, IMaterialReader materialReader)
	{
		settings.renameOldSetting("ReplaceToBiomeName", BiomeStandardValues.VANILLA_BIOME);
		settings.renameOldSetting("DisableNotchHeightControl", BiomeStandardValues.DISABLE_BIOME_HEIGHT);
	}
}
