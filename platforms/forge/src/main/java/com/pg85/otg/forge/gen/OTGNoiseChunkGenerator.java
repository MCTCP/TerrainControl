package com.pg85.otg.forge.gen;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.constants.SettingsEnums.MineshaftType;
import com.pg85.otg.constants.SettingsEnums.OceanRuinsType;
import com.pg85.otg.constants.SettingsEnums.RareBuildingType;
import com.pg85.otg.constants.SettingsEnums.RuinedPortalType;
import com.pg85.otg.constants.SettingsEnums.VillageType;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.config.dimensions.DimensionConfig;
import com.pg85.otg.core.config.dimensions.DimensionConfig.OTGDimension;
import com.pg85.otg.core.gen.OTGChunkDecorator;
import com.pg85.otg.core.gen.OTGChunkGenerator;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.forge.biome.ForgeBiome;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.interfaces.*;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.BastionPieces;
import net.minecraft.data.worldgen.DesertVillagePools;
import net.minecraft.data.worldgen.PillagerOutpostPools;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.data.worldgen.SavannaVillagePools;
import net.minecraft.data.worldgen.SnowyVillagePools;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.TaigaVillagePools;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.Climate.TargetPoint;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool.Projection;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public final class OTGNoiseChunkGenerator extends ChunkGenerator
{
	// Create a codec to serialise/deserialise OTGNoiseChunkGenerator
	public static final Codec<OTGNoiseChunkGenerator> CODEC = RecordCodecBuilder.create(
		(p_236091_0_) ->
		{
			return p_236091_0_
				.group(
					Codec.STRING.fieldOf("preset_folder_name").forGetter(
						(p_236090_0_) -> { return p_236090_0_.preset.getFolderName(); }
					),
					Codec.STRING.fieldOf("dim_config_name").forGetter(
						(p_236090_0_) -> { return p_236090_0_.dimConfigName; }
					),
					RegistryLookupCodec.create(Registry.NOISE_REGISTRY).forGetter(
						(p_188716_) -> { return p_188716_.noises; }
					),
					BiomeSource.CODEC.fieldOf("biome_source").forGetter(
						(p_236096_0_) -> { return p_236096_0_.biomeSource; }
					),
					Codec.LONG.fieldOf("seed").stable().forGetter(
						(p_236093_0_) -> { return p_236093_0_.seed; }
					),
					NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(
						(p_236090_0_) -> { return p_236090_0_.dimensionSettingsSupplier; }
					)
				).apply(
					p_236091_0_, p_236091_0_.stable(OTGNoiseChunkGenerator::new)
				)
			;
		}
	);

	private final Supplier<NoiseGeneratorSettings> dimensionSettingsSupplier;
	private final int noiseHeight;
	//private final SurfaceNoise surfaceNoise;
	//protected final WorldgenRandom random;

	private final ShadowChunkGenerator shadowChunkGenerator;
	private final OTGChunkGenerator internalGenerator;
	private final OTGChunkDecorator chunkDecorator;
	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;
	private final Registry<NormalNoise.NoiseParameters> noises;
	protected final long seed;
	private final Preset preset;
	private final String dimConfigName;
	private final DimensionConfig dimConfig;
	private CustomStructureCache structureCache; // TODO: Move this?
	
	// TODO: Modpack config specific, move this?
	private boolean portalDataProcessed = false;
	private List<LocalMaterialData> portalBlocks;
	private String portalColor;
	private String portalMob;
	private String portalIgnitionSource;

	public OTGNoiseChunkGenerator(Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeProvider, long seed, Supplier<NoiseGeneratorSettings> dimensionSettingsSupplier)
	{
		this(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName(), null, noises, biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	public OTGNoiseChunkGenerator(String presetFolderName, String dimConfigName, Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeProvider, long seed, Supplier<NoiseGeneratorSettings> dimensionSettingsSupplier)
	{
		this(presetFolderName, dimConfigName, noises, biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	@SuppressWarnings("deprecation")
	private OTGNoiseChunkGenerator(String presetFolderName, String dimConfigName, Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeProvider1, BiomeSource biomeProvider2, long seed, Supplier<NoiseGeneratorSettings> dimensionSettingsSupplier)
	{
		super(biomeProvider1, biomeProvider2, overrideStructureSettings(dimensionSettingsSupplier.get().structureSettings(), presetFolderName, (OTGBiomeProvider)biomeProvider1), seed);

		if (!(biomeProvider1 instanceof ILayerSource))
		{
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}

		this.noises = noises;
		this.seed = seed;
		this.preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
		if(dimConfigName != null && dimConfigName.trim().length() > 0)
		{
			this.dimConfigName = dimConfigName;
			this.dimConfig = DimensionConfig.fromDisk(this.dimConfigName);
		} else {
			this.dimConfigName = "";
			this.dimConfig = null;
		}
		this.dimensionSettingsSupplier = dimensionSettingsSupplier;		
		NoiseGeneratorSettings dimensionsettings = dimensionSettingsSupplier.get();
		this.defaultBlock = dimensionsettings.getDefaultBlock();
		this.defaultFluid = dimensionsettings.getDefaultFluid();		
		NoiseSettings noisesettings = dimensionsettings.noiseSettings();
		//this.random = new WorldgenRandom(seed);
		//this.surfaceNoise = (SurfaceNoise)(noisesettings.useSimplexSurfaceNoise() ? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0)) : new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0)));
		this.noiseHeight = noisesettings.height();

		this.shadowChunkGenerator = new ShadowChunkGenerator(OTG.getEngine().getPluginConfig().getMaxWorkerThreads());
		this.internalGenerator = new OTGChunkGenerator(this.preset, seed, (ILayerSource) biomeProvider1,((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetFolderName), OTG.getEngine().getLogger());
		this.chunkDecorator = new OTGChunkDecorator();
	}
	
	// Structure settings
	
	private static StructureSettings overrideStructureSettings(StructureSettings oldSettings, String presetFolderName, OTGBiomeProvider otgBiomeProvider)
	{
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
		IWorldConfig worldConfig = preset.getWorldConfig();
		com.google.common.collect.ImmutableMap.Builder<StructureFeature<?>, StructureFeatureConfiguration> separationSettings = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder();
		if(worldConfig.getVillagesEnabled())
		{
			separationSettings.put(StructureFeature.VILLAGE, new StructureFeatureConfiguration(worldConfig.getVillageSpacing(), worldConfig.getVillageSeparation(), 10387312));
		}
		if(worldConfig.getRareBuildingsEnabled())
		{
			separationSettings.put(StructureFeature.DESERT_PYRAMID, new StructureFeatureConfiguration(worldConfig.getDesertPyramidSpacing(), worldConfig.getDesertPyramidSeparation(), 14357617));
		}
		if(worldConfig.getRareBuildingsEnabled())
		{
			separationSettings.put(StructureFeature.IGLOO, new StructureFeatureConfiguration(worldConfig.getIglooSpacing(), worldConfig.getIglooSeparation(), 14357618));
		}
		if(worldConfig.getRareBuildingsEnabled())
		{
			separationSettings.put(StructureFeature.JUNGLE_TEMPLE, new StructureFeatureConfiguration(worldConfig.getJungleTempleSpacing(), worldConfig.getJungleTempleSeparation(), 14357619));
		}
		if(worldConfig.getRareBuildingsEnabled())
		{
			separationSettings.put(StructureFeature.SWAMP_HUT, new StructureFeatureConfiguration(worldConfig.getSwampHutSpacing(), worldConfig.getSwampHutSeparation(), 14357620));
		}
		if(worldConfig.getPillagerOutpostsEnabled())
		{
			separationSettings.put(StructureFeature.PILLAGER_OUTPOST, new StructureFeatureConfiguration(worldConfig.getPillagerOutpostSpacing(), worldConfig.getPillagerOutpostSeparation(), 165745296));
		}
		if(worldConfig.getStrongholdsEnabled())
		{
			separationSettings.put(StructureFeature.STRONGHOLD, new StructureFeatureConfiguration(worldConfig.getStrongholdSpacing(), worldConfig.getStrongholdSeparation(), 0));
		}
		if(worldConfig.getOceanMonumentsEnabled())
		{
			separationSettings.put(StructureFeature.OCEAN_MONUMENT, new StructureFeatureConfiguration(worldConfig.getOceanMonumentSpacing(), worldConfig.getOceanMonumentSeparation(), 10387313));
		}
		if(worldConfig.getEndCitiesEnabled())
		{
			separationSettings.put(StructureFeature.END_CITY, new StructureFeatureConfiguration(worldConfig.getEndCitySpacing(), worldConfig.getEndCitySeparation(), 10387313));
		}
		if(worldConfig.getWoodlandMansionsEnabled())
		{
			separationSettings.put(StructureFeature.WOODLAND_MANSION, new StructureFeatureConfiguration(worldConfig.getWoodlandMansionSpacing(), worldConfig.getWoodlandMansionSeparation(), 10387319));
		}
		if(worldConfig.getBuriedTreasureEnabled())
		{
			separationSettings.put(StructureFeature.BURIED_TREASURE, new StructureFeatureConfiguration(worldConfig.getBuriedTreasureSpacing(), worldConfig.getBuriedTreasureSeparation(), 0));
		}
		if(worldConfig.getMineshaftsEnabled())
		{
			separationSettings.put(StructureFeature.MINESHAFT, new StructureFeatureConfiguration(worldConfig.getMineshaftSpacing(), worldConfig.getMineshaftSeparation(), 0));
		}
		if(worldConfig.getRuinedPortalsEnabled())
		{
			separationSettings.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(worldConfig.getRuinedPortalSpacing(), worldConfig.getRuinedPortalSeparation(), 34222645));
		}
		if(worldConfig.getShipWrecksEnabled())
		{
			separationSettings.put(StructureFeature.SHIPWRECK, new StructureFeatureConfiguration(worldConfig.getShipwreckSpacing(), worldConfig.getShipwreckSeparation(), 165745295));
		}
		if(worldConfig.getOceanRuinsEnabled())
		{
			separationSettings.put(StructureFeature.OCEAN_RUIN, new StructureFeatureConfiguration(worldConfig.getOceanRuinSpacing(), worldConfig.getOceanRuinSeparation(), 14357621));
		}
		if(worldConfig.getBastionRemnantsEnabled())
		{
			separationSettings.put(StructureFeature.BASTION_REMNANT, new StructureFeatureConfiguration(worldConfig.getBastionRemnantSpacing(), worldConfig.getBastionRemnantSeparation(), 30084232));
		}
		if(worldConfig.getNetherFortressesEnabled())
		{
			separationSettings.put(StructureFeature.NETHER_BRIDGE, new StructureFeatureConfiguration(worldConfig.getNetherFortressSpacing(), worldConfig.getNetherFortressSeparation(), 30084232));
		}
		if(worldConfig.getNetherFossilsEnabled())
		{
			separationSettings.put(StructureFeature.NETHER_FOSSIL, new StructureFeatureConfiguration(worldConfig.getNetherFossilSpacing(), worldConfig.getNetherFossilSeparation(), 14357921));
		}
		separationSettings.putAll(
			oldSettings.structureConfig().entrySet().stream().filter(a -> 
				a.getKey() != StructureFeature.VILLAGE &&
				a.getKey() != StructureFeature.DESERT_PYRAMID &&
				a.getKey() != StructureFeature.IGLOO &&
				a.getKey() != StructureFeature.JUNGLE_TEMPLE &&
				a.getKey() != StructureFeature.SWAMP_HUT &&
				a.getKey() != StructureFeature.PILLAGER_OUTPOST &&
				a.getKey() != StructureFeature.STRONGHOLD &&
				a.getKey() != StructureFeature.OCEAN_MONUMENT &&
				a.getKey() != StructureFeature.END_CITY &&
				a.getKey() != StructureFeature.WOODLAND_MANSION &&
				a.getKey() != StructureFeature.BURIED_TREASURE &&
				a.getKey() != StructureFeature.MINESHAFT &&
				a.getKey() != StructureFeature.RUINED_PORTAL &&
				a.getKey() != StructureFeature.SHIPWRECK &&
				a.getKey() != StructureFeature.OCEAN_RUIN &&
				a.getKey() != StructureFeature.BASTION_REMNANT &&
				a.getKey() != StructureFeature.NETHER_BRIDGE &&
				a.getKey() != StructureFeature.NETHER_FOSSIL
			).collect(Collectors.toMap(Entry::getKey, Entry::getValue))
		);

		StructureSettings newSettings = new StructureSettings(
			worldConfig.getStrongholdsEnabled() ? Optional.of(
				new StrongholdConfiguration(
					worldConfig.getStrongHoldDistance(), 
					worldConfig.getStrongHoldSpread(), 
					worldConfig.getStrongHoldCount()
				)
			) : Optional.empty(), 
			Maps.newHashMap(separationSettings.build())
		);

		addBiomeConfigStructures(preset, preset.getWorldConfig(), otgBiomeProvider, oldSettings, newSettings);
		
		return newSettings;
	}
	
	private static void addBiomeConfigStructures(Preset preset, IWorldConfig worldConfig, OTGBiomeProvider otgBiomeProvider, StructureSettings oldSettings, StructureSettings newSettings)
	{
		// TODO: Currently we can only enable/disable structures per biome and use any configuration options exposed by the vanilla structure 
		// classes (size for villages fe). If we want to be able to customise more, we'll need to implement our own structure classes.
		// TODO: Allow users to create their own jigsaw patterns (for villages, end cities, pillager outposts etc)?
		// TODO: Amethyst Geodes (1.17?)	

		// Build vanilla structure settings for OTG biomes
		List<ResourceKey<Biome>> biomeKeys = ((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getBiomeRegistryKeys(preset.getFolderName());
		HashMap<StructureFeature<?>, com.google.common.collect.ImmutableMultimap.Builder<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> hashmap = new HashMap<>();
		preset.getAllBiomeConfigs().stream().forEach(biomeConfig -> {
			if(!biomeConfig.getIsTemplateForBiome())
			{
				Optional<ResourceKey<Biome>> biomeKey = biomeKeys.stream().filter(b -> b.getRegistryName().toString().equals(biomeConfig.getRegistryKey().toResourceLocationString())).findFirst();
				if(biomeKey.isPresent())
				{
					Optional<Biome> biome = otgBiomeProvider.possibleBiomes().stream().filter(b -> b.getRegistryName().toString().equals(biomeConfig.getRegistryKey().toResourceLocationString())).findFirst();
					if(biome.isPresent())
					{
						// Villages
						// TODO: Allow spawning multiple types in a single biome?
						if(worldConfig.getVillagesEnabled() && biomeConfig.getVillageType() != VillageType.disabled)
						{
							int villageSize = biomeConfig.getVillageSize();
							VillageType villageType = biomeConfig.getVillageType();
							ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> customVillage = register(
								((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("village").toResourceLocationString(),
								StructureFeature.VILLAGE.configured(
									new JigsawConfiguration(
										() -> {
											switch(villageType)
											{
												case sandstone:
													return DesertVillagePools.START;
												case savanna:
													return SavannaVillagePools.START;
												case taiga:
													return TaigaVillagePools.START;
												case wood:
													return PlainVillagePools.START;
												case snowy:
													return SnowyVillagePools.START;
												case disabled: // Should never happen
													break;
											}
											return PlainVillagePools.START;
										},
										villageSize
									)
								)
							);
							addStructureToBiome(hashmap, customVillage, biomeKey.get());
						}
						
						// Strongholds
						if(worldConfig.getStrongholdsEnabled() && biomeConfig.getStrongholdsEnabled())
						{
							addStructureToBiome(hashmap, StructureFeatures.STRONGHOLD, biomeKey.get());
						}		

						// Ocean Monuments
						if(worldConfig.getOceanMonumentsEnabled() && biomeConfig.getOceanMonumentsEnabled())
						{
							addStructureToBiome(hashmap, StructureFeatures.OCEAN_MONUMENT, biomeKey.get());
						}
						
						// Rare buildings
						// TODO: Allow spawning multiple types in a single biome?
						if(worldConfig.getRareBuildingsEnabled() && biomeConfig.getRareBuildingType() != RareBuildingType.disabled)
						{
							switch(biomeConfig.getRareBuildingType())
							{
								case desertPyramid:
									addStructureToBiome(hashmap, StructureFeatures.DESERT_PYRAMID, biomeKey.get());
									break;
								case igloo:
									addStructureToBiome(hashmap, StructureFeatures.IGLOO, biomeKey.get());
									break;
								case jungleTemple:
									addStructureToBiome(hashmap, StructureFeatures.JUNGLE_TEMPLE, biomeKey.get());			
									break;
								case swampHut:
									addStructureToBiome(hashmap, StructureFeatures.SWAMP_HUT, biomeKey.get());
									break;
								case disabled:
									break;					
							}
						}
						
						// Woodland Mansions
						if(worldConfig.getWoodlandMansionsEnabled() && biomeConfig.getWoodlandMansionsEnabled())
						{
							addStructureToBiome(hashmap, StructureFeatures.WOODLAND_MANSION, biomeKey.get());
						}
						
						// Nether Fortresses
						if(worldConfig.getNetherFortressesEnabled() && biomeConfig.getNetherFortressesEnabled())
						{
							addStructureToBiome(hashmap, StructureFeatures.NETHER_BRIDGE, biomeKey.get());
						}

						// Mineshafts
						if(worldConfig.getMineshaftsEnabled() && biomeConfig.getMineShaftType() != MineshaftType.disabled)
						{
							float mineShaftProbability = biomeConfig.getMineShaftProbability();
							MineshaftType mineShaftType = biomeConfig.getMineShaftType();
							ConfiguredStructureFeature<MineshaftConfiguration, ? extends StructureFeature<MineshaftConfiguration>> customMineShaft = register(
								((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("mineshaft").toResourceLocationString(),
								StructureFeature.MINESHAFT.configured(
									new MineshaftConfiguration(
										mineShaftProbability,
										mineShaftType == MineshaftType.mesa ? MineshaftFeature.Type.MESA : MineshaftFeature.Type.NORMAL
									)
								)
							);
							addStructureToBiome(hashmap, customMineShaft, biomeKey.get());
						}
						
						// Buried Treasure
						if(worldConfig.getBuriedTreasureEnabled() && biomeConfig.getBuriedTreasureEnabled())
						{
							float buriedTreasureProbability = biomeConfig.getBuriedTreasureProbability();
							ConfiguredStructureFeature<ProbabilityFeatureConfiguration, ? extends StructureFeature<ProbabilityFeatureConfiguration>> customBuriedTreasure = register(
								((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("buried_treasure").toResourceLocationString(),
								StructureFeature.BURIED_TREASURE.configured(new ProbabilityFeatureConfiguration(buriedTreasureProbability))
							);
							addStructureToBiome(hashmap, customBuriedTreasure, biomeKey.get());
						}
						
						// Ocean Ruins
						if(worldConfig.getOceanRuinsEnabled() && biomeConfig.getOceanRuinsType() != OceanRuinsType.disabled)
						{
							float oceanRuinsLargeProbability = biomeConfig.getOceanRuinsLargeProbability();
							float oceanRuinsClusterProbability = biomeConfig.getOceanRuinsClusterProbability();
							OceanRuinsType oceanRuinsType = biomeConfig.getOceanRuinsType();
							ConfiguredStructureFeature<OceanRuinConfiguration, ? extends StructureFeature<OceanRuinConfiguration>> customOceanRuins = register(
								((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("ocean_ruin").toResourceLocationString(),
								StructureFeature.OCEAN_RUIN.configured(
									new OceanRuinConfiguration(
										oceanRuinsType == OceanRuinsType.cold ? OceanRuinFeature.Type.COLD : OceanRuinFeature.Type.WARM,
										oceanRuinsLargeProbability,
										oceanRuinsClusterProbability
									)
								)
							);
							addStructureToBiome(hashmap, customOceanRuins, biomeKey.get());
						}

						// Shipwrecks
						// TODO: Allowing both types in the same biome, make sure this won't cause problems.
						if(worldConfig.getShipWrecksEnabled())
						{
							if(biomeConfig.getShipWreckEnabled())
							{
								addStructureToBiome(hashmap, StructureFeatures.SHIPWRECK, biomeKey.get());
							}
							if(biomeConfig.getShipWreckBeachedEnabled())
							{
								addStructureToBiome(hashmap, StructureFeatures.SHIPWRECK_BEACHED, biomeKey.get());
							}			
						}
						
						// Pillager Outpost
						if(worldConfig.getPillagerOutpostsEnabled() && biomeConfig.getPillagerOutpostEnabled())
						{
							int outpostSize = biomeConfig.getPillagerOutPostSize();
							ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> customOutpost = register(
								((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("pillager_outpost").toResourceLocationString(), 
								StructureFeature.PILLAGER_OUTPOST.configured(
									new JigsawConfiguration(
										() -> {
											return PillagerOutpostPools.START;
										},
										outpostSize
									)
								)
							);
							addStructureToBiome(hashmap, customOutpost, biomeKey.get());							
						}
						
						// Bastion Remnants
						if(worldConfig.getBastionRemnantsEnabled() && biomeConfig.getBastionRemnantEnabled())
						{
							int bastionRemnantSize = biomeConfig.getBastionRemnantSize();
							ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> customBastionRemnant = register(
								((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("bastion_remnant").toResourceLocationString(), 
								StructureFeature.BASTION_REMNANT.configured(
									new JigsawConfiguration(
										() -> {
											return BastionPieces.START;
										},
										bastionRemnantSize
									)
								)
							);
							addStructureToBiome(hashmap, customBastionRemnant, biomeKey.get());
						}
						
						// Nether Fossils
						if(worldConfig.getNetherFossilsEnabled() && biomeConfig.getNetherFossilEnabled())
						{
							addStructureToBiome(hashmap, StructureFeatures.NETHER_FOSSIL, biomeKey.get());
						}
						
						// End Cities
						if(worldConfig.getEndCitiesEnabled() && biomeConfig.getEndCityEnabled())
						{
							addStructureToBiome(hashmap, StructureFeatures.END_CITY, biomeKey.get());
						}
						
						// Ruined Portals
						if(worldConfig.getRuinedPortalsEnabled() && biomeConfig.getRuinedPortalType() != RuinedPortalType.disabled)
						{
							switch(biomeConfig.getRuinedPortalType())
							{
								case normal:
									addStructureToBiome(hashmap, StructureFeatures.RUINED_PORTAL_STANDARD, biomeKey.get());
									break;
								case desert:
									addStructureToBiome(hashmap, StructureFeatures.RUINED_PORTAL_DESERT, biomeKey.get());
									break;
								case jungle:
									addStructureToBiome(hashmap, StructureFeatures.RUINED_PORTAL_JUNGLE, biomeKey.get());
									break;
								case swamp:
									addStructureToBiome(hashmap, StructureFeatures.RUINED_PORTAL_SWAMP, biomeKey.get());
									break;
								case mountain:
									addStructureToBiome(hashmap, StructureFeatures.RUINED_PORTAL_MOUNTAIN, biomeKey.get());
									break;
								case ocean:
									addStructureToBiome(hashmap, StructureFeatures.RUINED_PORTAL_OCEAN, biomeKey.get());
									break;
								case nether:
									addStructureToBiome(hashmap, StructureFeatures.RUINED_PORTAL_NETHER, biomeKey.get());
									break;
								case disabled:
									break;
							}
						}											
					}
				}
			}
		});
		
		ImmutableMap<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> configuredStructures = 
			hashmap.entrySet().stream().collect(
				ImmutableMap.toImmutableMap(
					Entry<StructureFeature<?>, com.google.common.collect.ImmutableMultimap.Builder<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>>::getKey, 
					(p_189370_) -> { 
						return p_189370_.getValue().build(); 
					}
				)
			)
		;
		
		// Add any existing (non-vanilla) structure settings
		HashMap<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> configuredStructures2 = new HashMap<>(configuredStructures);
		oldSettings.configuredStructures.entrySet().stream().forEach(a -> {
			if(
				a.getKey() != StructureFeature.VILLAGE &&
				a.getKey() != StructureFeature.DESERT_PYRAMID &&
				a.getKey() != StructureFeature.IGLOO &&
				a.getKey() != StructureFeature.JUNGLE_TEMPLE &&
				a.getKey() != StructureFeature.SWAMP_HUT &&
				a.getKey() != StructureFeature.PILLAGER_OUTPOST &&
				a.getKey() != StructureFeature.STRONGHOLD &&
				a.getKey() != StructureFeature.OCEAN_MONUMENT &&
				a.getKey() != StructureFeature.END_CITY &&
				a.getKey() != StructureFeature.WOODLAND_MANSION &&
				a.getKey() != StructureFeature.BURIED_TREASURE &&
				a.getKey() != StructureFeature.MINESHAFT &&
				a.getKey() != StructureFeature.RUINED_PORTAL &&
				a.getKey() != StructureFeature.SHIPWRECK &&
				a.getKey() != StructureFeature.OCEAN_RUIN &&
				a.getKey() != StructureFeature.BASTION_REMNANT &&
				a.getKey() != StructureFeature.NETHER_BRIDGE &&
				a.getKey() != StructureFeature.NETHER_FOSSIL
			)
			{
				configuredStructures2.putIfAbsent(a.getKey(), a.getValue());
			}
		});
		
		newSettings.configuredStructures = new ImmutableMap.Builder<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>>().putAll(configuredStructures2).build();
	}
	
	// StructureFeatures.register()
	private static <FC extends FeatureConfiguration, F extends StructureFeature<FC>> ConfiguredStructureFeature<FC, F> register(String name, ConfiguredStructureFeature<FC, F> structure)
	{
		return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, name, structure);
	}	
	
	private static void addStructureToBiome(HashMap<StructureFeature<?>, com.google.common.collect.ImmutableMultimap.Builder<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> hashmap, ConfiguredStructureFeature<?, ?> configuredStructureFeature, ResourceKey<Biome> biomeKey)
	{
		BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> consumer = (p_189367_, p_189368_) -> {
			hashmap.computeIfAbsent(
				p_189367_.feature,
				(p_189374_) -> { 
					return new ImmutableMultimap.Builder<ConfiguredStructureFeature<?,?>,ResourceKey<Biome>>(); 
				}
			).put(p_189367_, p_189368_);
		};
		consumer.accept(configuredStructureFeature, biomeKey);
	}	
	
	//
	
	public ICachedBiomeProvider getCachedBiomeProvider()
	{
		return this.internalGenerator.getCachedBiomeProvider();
	}

	public void saveStructureCache()
	{
		if (this.chunkDecorator.getIsSaveRequired() && this.structureCache != null)
		{
			this.structureCache.saveToDisk(OTG.getEngine().getLogger(), this.chunkDecorator);
		}
	}

	public Preset getPreset()
	{
		return this.preset;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ChunkGenerator withSeed(long seed)
	{
		return new OTGNoiseChunkGenerator(this.preset.getFolderName(), this.dimConfigName, this.noises, this.biomeSource.withSeed(seed), seed, this.dimensionSettingsSupplier);
	}
	
	public boolean stable(long p_64376_, ResourceKey<NoiseGeneratorSettings> p_64377_)
	{
		return this.seed == p_64376_ && this.dimensionSettingsSupplier.get().stable(p_64377_);
	}
	
	// Base terrain gen
	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager accessor, ChunkAccess chunk)
	{
		buildNoise(accessor, chunk);

		return CompletableFuture.completedFuture(chunk);
	}
	
	// Generates the base terrain for a chunk.
	public void buildNoise(StructureFeatureManager manager, ChunkAccess chunk)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);

		// Dummy random, as we can't get the level random right now
		Random random = new Random();
		// Fetch any chunks that are cached in the WorldGenRegion, so we can
		// pre-emptively generate and cache base terrain for them asynchronously.
		this.shadowChunkGenerator.queueChunksForWorkerThreads((WorldGenRegion)chunk.getWorldForge(), manager, chunk, this, (OTGBiomeProvider)this.biomeSource, this.internalGenerator, this.getSettings(), this.preset.getWorldConfig().getWorldHeightCap());
		
		// If we've already (shadow-)generated and cached this	
		// chunk while it was unloaded, use cached data.
		ChunkBuffer buffer = new ForgeChunkBuffer((ProtoChunk) chunk);
		ChunkAccess cachedChunk = this.shadowChunkGenerator.getChunkWithWait(chunkCoord);
		if (cachedChunk != null)
		{
			this.shadowChunkGenerator.fillWorldGenChunkFromShadowChunk(chunk, cachedChunk);
		} else {			
			// Setup jigsaw data
			ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
			ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);
			ChunkPos pos = chunk.getPos();
			int chunkX = pos.x;
			int chunkZ = pos.z;
			int startX = chunkX << 4;
			int startZ = chunkZ << 4;

			// Iterate through all of the jigsaw structures (villages, pillager outposts, nether fossils)
			for(StructureFeature<?> structure : StructureFeature.NOISE_AFFECTING_FEATURES)
			{
				// Get all structure starts in this chunk
				manager.startsForFeature(SectionPos.of(pos, 0), structure).forEach((start) ->
				{
					// Iterate through the pieces in the structure
					for(StructurePiece piece : start.getPieces())
					{
						// Check if it intersects with this chunk
						if (piece.isCloseToChunk(pos, 12))
						{
							BoundingBox box = piece.getBoundingBox();
							if (piece instanceof PoolElementStructurePiece)
							{
								PoolElementStructurePiece villagePiece = (PoolElementStructurePiece) piece;
								// Add to the list if it's a rigid piece
								if (villagePiece.getElement().getProjection() == Projection.RIGID)
								{
									structures.add(new JigsawStructureData(box.minX(), box.minY(), box.minZ(), box.maxX(), villagePiece.getGroundLevelDelta(), box.maxZ(), true, 0, 0, 0));
								}

								// Get all the junctions in this piece
								for(JigsawJunction junction : villagePiece.getJunctions())
								{
									int sourceX = junction.getSourceX();
									int sourceZ = junction.getSourceZ();

									// If the junction is in this chunk, then add to list
									if (sourceX > startX - 12 && sourceZ > startZ - 12 && sourceX < startX + 15 + 12 && sourceZ < startZ + 15 + 12)
									{
										junctions.add(new JigsawStructureData(0, 0, 0,0, 0, 0, false, junction.getSourceX(), junction.getSourceGroundY(), junction.getSourceZ()));
									}
								}
							} else {
								structures.add(new JigsawStructureData(box.minX(), box.minY(), box.minZ(),box.maxX(), 0, box.maxZ(), false, 0, 0, 0));
							}
						}
					}
				});
			}		
			this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), random, buffer, buffer.getChunkCoordinate(), structures, junctions);
			this.shadowChunkGenerator.setChunkGenerated(chunkCoord);
		}
	}
	
	// Replaces surface and ground blocks in base terrain and places bedrock.
	@Override
	public void buildSurface(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager, ChunkAccess chunk)
	{
		// OTG handles surface/ground blocks during base terrain gen. For non-OTG biomes used
		// with TemplateForBiome, we want to use registered surfacebuilders though.

		/*
		ChunkPos chunkpos = chunk.getPos();
		int i = chunkpos.x;
		int j = chunkpos.z;
		WorldgenRandom sharedseedrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
		sharedseedrandom.setBaseChunkSeed(i, j);
		ChunkPos chunkpos1 = chunk.getPos();
		int chunkMinX = chunkpos1.getMinBlockX();
		int chunkMinZ = chunkpos1.getMinBlockZ();
		int worldX;
		int worldZ;
		int i2;
		double d1;
		IBiome[] biomesForChunk = this.internalGenerator.getCachedBiomeProvider().getBiomesForChunk(ChunkCoordinate.fromBlockCoords(chunkMinX, chunkMinZ));
		IBiome biome;
		
		for(int xInChunk = 0; xInChunk < Constants.CHUNK_SIZE; ++xInChunk)
		{
			for(int zInChunk = 0; zInChunk < Constants.CHUNK_SIZE; ++zInChunk)
			{
				worldX = chunkMinX + xInChunk;
				worldZ = chunkMinZ + zInChunk;
				biome = biomesForChunk[xInChunk * Constants.CHUNK_SIZE + zInChunk];
				if(biome.getBiomeConfig().getIsTemplateForBiome())
				{
					i2 = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, xInChunk, zInChunk) + 1;
					d1 = this.surfaceNoise.getSurfaceNoiseValue((double)worldX * 0.0625D, (double)worldZ * 0.0625D, 0.0625D, (double)xInChunk * 0.0625D) * 15.0D;
					((ForgeBiome)biome).getBiomeBase().buildSurfaceAt(sharedseedrandom, chunk, worldX, worldZ, i2, d1, ((ForgeMaterialData)biome.getBiomeConfig().getDefaultStoneBlock()).internalBlock(), ((ForgeMaterialData)biome.getBiomeConfig().getDefaultWaterBlock()).internalBlock(), this.getSeaLevel(), 50, worldGenRegion.getSeed());
				}
			}
		}
		*/
		// Skip bedrock, OTG always handles that.
	}

	// Carvers: Caves and ravines
	
	@Override
	public void applyCarvers(WorldGenRegion p_187691_, long seed, BiomeManager biomeManager, StructureFeatureManager structureFeatureManager, ChunkAccess chunk, Carving stage)
	{
		// OTG has its own caves and canyons carvers. We register default carvers to OTG biomes,
		// then check if they have been overridden by mods before using our own carvers.

		if (stage == GenerationStep.Carving.AIR)
		{
			ForgeBiome biome = (ForgeBiome)this.getCachedBiomeProvider().getNoiseBiome(chunk.getPos().x << 2, chunk.getPos().z << 2);
			BiomeGenerationSettings biomegenerationsettings = biome.getBiomeBase().getGenerationSettings();
			List<Supplier<ConfiguredWorldCarver<?>>> list = biomegenerationsettings.getCarvers(stage);

			// Only use OTG carvers when default mc carvers are found
			List<String> defaultCaves = Arrays.asList("minecraft:cave", "minecraft:underwater_cave", "minecraft:nether_cave");			
			boolean cavesEnabled = this.preset.getWorldConfig().getCavesEnabled() && list.stream().anyMatch(
				a -> defaultCaves.stream().anyMatch(
					b -> b.equals(
						ForgeRegistries.WORLD_CARVERS.getKey(a.get().worldCarver).toString()
					)
				)
			);
			List<String> defaultRavines = Arrays.asList("minecraft:canyon", "minecraft:underwater_canyon");
			boolean ravinesEnabled = this.preset.getWorldConfig().getRavinesEnabled() && list.stream().anyMatch(
				a -> defaultRavines.stream().anyMatch(
					b -> b.equals(
						ForgeRegistries.WORLD_CARVERS.getKey(a.get().worldCarver).toString()
					)
				)
			);
			if(cavesEnabled || ravinesEnabled)
			{
				ProtoChunk protoChunk = (ProtoChunk) chunk;
				ChunkBuffer chunkBuffer = new ForgeChunkBuffer(protoChunk);
				/*
				* The following code exists as Minecraft 1.18 has a new "carvingMask"
				* class that they use instead of BitSet
				* However, that class is really just a wrapper that makes it harder
				* to access the BitSet inside.
				* We simply use reflections to access the BitSet
				* Which enables us to send it up into common code.
				*
				* - Frank
				* TODO: This code compiles, but it still does not generate caves on Forge.
				 */
				CarvingMask carvingMaskRaw = protoChunk.getOrCreateCarvingMask(stage);
				try {
					Field theRealMask = carvingMaskRaw.getClass().getDeclaredField("mask");
					theRealMask.setAccessible(true);
					BitSet carvingMask = (BitSet)theRealMask.get(carvingMaskRaw);
					this.internalGenerator.carve(chunkBuffer, seed, protoChunk.getPos().x, protoChunk.getPos().z, carvingMask, cavesEnabled, ravinesEnabled);
				} catch (NoSuchFieldException e) {
					if (OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.MAIN)) {
						OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "!!! Error obtaining the carving mask! Caves will not generate! Stacktrace:\n" + e.getStackTrace());
					}
				} catch (IllegalAccessException e) {
					if (OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.MAIN)) {
						OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "!!! Error obtaining the carving mask! Caves will not generate! Stacktrace:\n" + e.getStackTrace());
					}
				}
			}
		}
		applyNonOTGCarvers(seed, biomeManager, chunk, stage);

	}

	public void applyNonOTGCarvers(long seed, BiomeManager biomeManager, ChunkAccess chunk, GenerationStep.Carving stage)
	{
		/*
		BiomeManager biomemanager = biomeManager.withDifferentSource(this.biomeSource);
		WorldgenRandom sharedseedrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
		CarvingContext carvingcontext = new CarvingContext(this, chunk);
		ChunkPos chunkpos = chunk.getPos();
		Aquifer aquifer = this.createAquifer(chunk);
		int j = chunkpos.x;
		int k = chunkpos.z;
		ForgeBiome biome = (ForgeBiome)this.getCachedBiomeProvider().getNoiseBiome(chunk.getPos().x << 2, chunk.getPos().z << 2);
		BiomeGenerationSettings biomegenerationsettings = biome.getBiomeBase().getGenerationSettings();
		BitSet bitset = ((ProtoChunk)chunk).getOrCreateCarvingMask(stage);

		List<String> defaultCavesAndRavines = Arrays.asList("minecraft:cave", "minecraft:underwater_cave", "minecraft:nether_cave", "minecraft:canyon", "minecraft:underwater_canyon");					
		for(int l = j - 8; l <= j + 8; ++l)
		{
			for(int i1 = k - 8; i1 <= k + 8; ++i1)
			{
				ChunkPos chunkpos1 = new ChunkPos(chunkpos.x + j, chunkpos.z + k);
				List<Supplier<ConfiguredWorldCarver<?>>> list = biomegenerationsettings.getCarvers(stage);
				ListIterator<Supplier<ConfiguredWorldCarver<?>>> listiterator = list.listIterator();
				while(listiterator.hasNext())
				{
					int j1 = listiterator.nextIndex();
					ConfiguredWorldCarver<?> configuredcarver = listiterator.next().get();
					String carverRegistryName = ForgeRegistries.WORLD_CARVERS.getKey(configuredcarver.worldCarver).toString();
					// OTG uses its own caves and canyon carvers, ignore the default ones.
					if(defaultCavesAndRavines.stream().noneMatch(a -> a.equals(carverRegistryName)))
					{
						sharedseedrandom.setLargeFeatureSeed(seed + (long)j1, l, i1);
						if (configuredcarver.isStartChunk(sharedseedrandom))
						{
							configuredcarver.carve(carvingcontext, chunk, biomemanager::getBiome, sharedseedrandom, aquifer, chunkpos1, bitset);
						}
					}
				}
			}
		}
		*/
	}

	// Decoration

	// Does decoration for a given pos/chunk
	/*
	@Override
	@SuppressWarnings("deprecation")	
	public void applyBiomeDecoration(WorldGenRegion worldGenRegion, StructureFeatureManager structureManager)
	{
		if(!OTG.getEngine().getPluginConfig().getDecorationEnabled())
		{
			return;
		}
		
		// Do OTG resource decoration, then MC decoration for any non-OTG resources registered to this biome, then snow.
		
		// Taken from vanilla
		int worldX = worldGenRegion.getCenter().x * Constants.CHUNK_SIZE;
		int worldZ = worldGenRegion.getCenter().z * Constants.CHUNK_SIZE;
		BlockPos blockpos = new BlockPos(worldX, 0, worldZ);
		WorldgenRandom sharedseedrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
		long decorationSeed = sharedseedrandom.setDecorationSeed(worldGenRegion.getSeed(), worldX, worldZ);	
		//

		ChunkCoordinate chunkBeingDecorated = ChunkCoordinate.fromBlockCoords(worldX, worldZ);
		ForgeWorldGenRegion forgeWorldGenRegion = new ForgeWorldGenRegion(this.preset.getFolderName(), this.preset.getWorldConfig(), (WorldGenRegion)worldGenRegion, this);
		IBiome biome = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2) + 2, (worldGenRegion.getCenter().z << 2) + 2);
		IBiome biome1 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2), (worldGenRegion.getCenter().z << 2));
		IBiome biome2 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2), (worldGenRegion.getCenter().z << 2) + 4);
		IBiome biome3 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2) + 4, (worldGenRegion.getCenter().z << 2));
		IBiome biome4 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2) + 4, (worldGenRegion.getCenter().z << 2) + 4);
		// World save folder name may not be identical to level name, fetch it.
		Path worldSaveFolder = worldGenRegion.getLevel().getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).getParent();

		// Get most common biome in chunk and use that for decoration - Frank
		if (!getPreset().getWorldConfig().improvedBorderDecoration())
		{
			List<IBiome> biomes = new ArrayList<IBiome>();
			biomes.add(biome);
			biomes.add(biome1);
			biomes.add(biome2);
			biomes.add(biome3);
			biomes.add(biome4);
			
			Map<IBiome, Integer> map = new HashMap<>();
			for (IBiome b : biomes)
			{
				Integer val = map.get(b);
				map.put(b, val == null ? 1 : val + 1);
			}

			Map.Entry<IBiome, Integer> max = null;
			for (Map.Entry<IBiome, Integer> ent : map.entrySet())
			{
				if (max == null || ent.getValue() > max.getValue())
				{
					max = ent;
				}
			}

			biome = max.getKey();
		}

		try
		{
			//
			// Here's how the code works that was added for the ImprovedBorderDecoration code.
			// - List of biome ids is initialized, will be used to ensure biomes are not populated twice.
			// - Placement is done for the main biome
			// - If ImprovedBorderDecoration is true, will attempt to perform decoration from any biomes that have not
			// already been decorated. Thus preventing decoration from happening twice.
			//
			// - Frank
			//
			List<Integer> alreadyDecorated = new ArrayList<>();
			this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome.getBiomeConfig(), getStructureCache(worldSaveFolder));
			((ForgeBiome)biome).getBiomeBase().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
			alreadyDecorated.add(biome.getBiomeConfig().getOTGBiomeId());
			// Attempt to decorate other biomes if ImprovedBiomeDecoration - Frank
			if (getPreset().getWorldConfig().improvedBorderDecoration())
			{
				if (!alreadyDecorated.contains(biome1.getBiomeConfig().getOTGBiomeId()))
				{
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome1.getBiomeConfig(), getStructureCache(worldSaveFolder));
					((ForgeBiome)biome1).getBiomeBase().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
					alreadyDecorated.add(biome1.getBiomeConfig().getOTGBiomeId());										
				}
				if (!alreadyDecorated.contains(biome2.getBiomeConfig().getOTGBiomeId()))
				{
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome2.getBiomeConfig(), getStructureCache(worldSaveFolder));
					((ForgeBiome)biome2).getBiomeBase().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
					alreadyDecorated.add(biome2.getBiomeConfig().getOTGBiomeId());					
				}
				if (!alreadyDecorated.contains(biome3.getBiomeConfig().getOTGBiomeId()))
				{
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome3.getBiomeConfig(), getStructureCache(worldSaveFolder));
					((ForgeBiome)biome3).getBiomeBase().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
					alreadyDecorated.add(biome3.getBiomeConfig().getOTGBiomeId());					
				}
				if (!alreadyDecorated.contains(biome4.getBiomeConfig().getOTGBiomeId()))
				{
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome4.getBiomeConfig(), getStructureCache(worldSaveFolder));
					((ForgeBiome)biome4).getBiomeBase().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
				}
			}
			// Template biomes handle their own snow, OTG biomes use OTG snow.
			// TODO: Snow is handled per chunk, so this may cause some artifacts on biome borders.
			if(
				!biome.getBiomeConfig().getIsTemplateForBiome() ||
				!biome1.getBiomeConfig().getIsTemplateForBiome() ||
				!biome2.getBiomeConfig().getIsTemplateForBiome() ||
				!biome3.getBiomeConfig().getIsTemplateForBiome() ||				
				!biome4.getBiomeConfig().getIsTemplateForBiome()
			)
			{
				this.chunkDecorator.doSnowAndIce(forgeWorldGenRegion, chunkBeingDecorated);
			}
		} catch (Exception exception) {
			CrashReport crashreport = CrashReport.forThrowable(exception, "Biome decoration");
			crashreport.addCategory("Generation").setDetail("CenterX", worldX).setDetail("CenterZ", worldZ).setDetail("Seed", decorationSeed);
			throw new ReportedException(crashreport);
		}
	}
	*/

	@Override
	public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess p_187713_, StructureFeatureManager p_187714_)
	{
		if(!OTG.getEngine().getPluginConfig().getDecorationEnabled())
		{
			return;
		}
		
		ChunkPos chunkpos = p_187713_.getPos();
		if (!SharedConstants.debugVoidTerrain(chunkpos))
		{
			WorldGenRegion worldGenRegion = ((WorldGenRegion)worldGenLevel);
			SectionPos sectionpos = SectionPos.of(chunkpos, worldGenRegion.getMinSection());
			BlockPos blockpos = sectionpos.origin();
			Map<Integer, List<StructureFeature<?>>> map = Registry.STRUCTURE_FEATURE.stream().collect(Collectors.groupingBy((p_187720_) -> {
				return p_187720_.step().ordinal();
			}));
			List<BiomeSource.StepFeatureData> list = this.biomeSource.featuresPerStep();
			WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
			long i = worldgenrandom.setDecorationSeed(worldGenRegion.getSeed(), blockpos.getX(), blockpos.getZ());
			
			int worldX = worldGenRegion.getCenter().x * Constants.CHUNK_SIZE;
			int worldZ =worldGenRegion.getCenter().z * Constants.CHUNK_SIZE;
			ChunkCoordinate chunkBeingDecorated = ChunkCoordinate.fromBlockCoords(worldX, worldZ);
			IBiome biome = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2) + 2, (worldGenRegion.getCenter().z << 2) + 2);
			IBiome biome1 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2), (worldGenRegion.getCenter().z << 2));
			IBiome biome2 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2), (worldGenRegion.getCenter().z << 2) + 4);
			IBiome biome3 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2) + 4, (worldGenRegion.getCenter().z << 2));
			IBiome biome4 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2) + 4, (worldGenRegion.getCenter().z << 2) + 4);
			IBiomeConfig biomeConfig = biome.getBiomeConfig();
			ForgeWorldGenRegion forgeWorldGenRegion = new ForgeWorldGenRegion(this.preset.getFolderName(), this.preset.getWorldConfig(), worldGenRegion, this);
			// World save folder name may not be identical to level name, fetch it.
			Path worldSaveFolder = worldGenRegion.getLevel().getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).getParent();
			if (!getPreset().getWorldConfig().improvedBorderDecoration()) {
				List<IBiome> biomes = new ArrayList<IBiome>();

				biomes.add(biome);
				biomes.add(biome1);
				biomes.add(biome2);
				biomes.add(biome3);
				biomes.add(biome4);

				Map<IBiome, Integer> map1 = new HashMap<>();
				for (IBiome b : biomes)
				{
					Integer val = map1.get(b);
					map1.put(b, val == null ? 1 : val + 1);
				}

				Map.Entry<IBiome, Integer> max = null;
				for (Map.Entry<IBiome, Integer> ent : map1.entrySet())
				{
					if (max == null || ent.getValue() > max.getValue())
					{
						max = ent;
					}
				}

				biome = max.getKey();
			}

			try {
				/*
				 * Here's how the code works that was added for the ImprovedBorderDecoration code.
				 * - List of biome ids is initialized, will be used to ensure biomes are not populated twice.
				 * - Placement is done for the main biome
				 * - If ImprovedBorderDecoration is true, will attempt to perform decoration from any biomes that have not
				 * already been decorated. Thus preventing decoration from happening twice.
				 *
				 * Another note for 1.18+:
				 * I commented out the generators for the vanilla resources, as
				 * defaults to improved
				 *
				 * - Frank
				 */
				List<Integer> alreadyDecorated = new ArrayList<>();
				this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome.getBiomeConfig(), getStructureCache(worldSaveFolder));
				alreadyDecorated.add(biome.getBiomeConfig().getOTGBiomeId());
				// Attempt to decorate other biomes if ImprovedBiomeDecoration - Frank
				if (getPreset().getWorldConfig().improvedBorderDecoration())
				{
					if (!alreadyDecorated.contains(biome1.getBiomeConfig().getOTGBiomeId()))
					{
						this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome1.getBiomeConfig(), getStructureCache(worldSaveFolder));
						//((PaperBiome) biome1).getBiome().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
						alreadyDecorated.add(biome1.getBiomeConfig().getOTGBiomeId());
					}
					if (!alreadyDecorated.contains(biome2.getBiomeConfig().getOTGBiomeId()))
					{
						this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome2.getBiomeConfig(), getStructureCache(worldSaveFolder));
						//((PaperBiome) biome2).getBiome().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
						alreadyDecorated.add(biome2.getBiomeConfig().getOTGBiomeId());
					}
					if (!alreadyDecorated.contains(biome3.getBiomeConfig().getOTGBiomeId()))
					{
						this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome3.getBiomeConfig(), getStructureCache(worldSaveFolder));
						//((PaperBiome) biome3).getBiome().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
						alreadyDecorated.add(biome3.getBiomeConfig().getOTGBiomeId());
					}
					if (!alreadyDecorated.contains(biome4.getBiomeConfig().getOTGBiomeId()))
					{
						this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome4.getBiomeConfig(), getStructureCache(worldSaveFolder));
						//((PaperBiome) biome4).getBiome().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
					}
				}
			}
			catch (Exception exception)
			{
				CrashReport crashreport = CrashReport.forThrowable(exception, "Biome decoration");
				crashreport.addCategory("Generation").setDetail("CenterX", worldX).setDetail("CenterZ", worldZ).setDetail("Seed", seed);
				throw new ReportedException(crashreport);
			}

			// This line has been moved up - Frank
			//this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome.getBiomeConfig(), getStructureCache(worldSaveFolder));

			Set<Biome> set = new ObjectArraySet<>();
			//if (this instanceof FlatLevelSource)
			//{
				set.addAll(this.biomeSource.possibleBiomes());
			//} else {
				/* Behold, the future
				ChunkPos.rangeClosed(sectionpos.chunk(), 1).forEach((p_196730_) -> {
					ChunkAccess chunkaccess = p_187712_.getChunk(p_196730_.x, p_196730_.z);
		
					for(LevelChunkSection levelchunksection : chunkaccess.getSections())
					{
						levelchunksection.getBiomes().getAll(set::add);
					}
				});
				set.retainAll(this.biomeSource.possibleBiomes());
				*/
			//}
		
			int j = list.size();
		
			try {
				Registry<PlacedFeature> registry = worldGenRegion.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
				Registry<StructureFeature<?>> registry1 = worldGenRegion.registryAccess().registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY);
				int k = Math.max(GenerationStep.Decoration.values().length, j);
			
				for(int l = 0; l < k; ++l)
				{
					int i1 = 0;
					if (p_187714_.shouldGenerateFeatures())
					{
						for(StructureFeature<?> structurefeature : map.getOrDefault(l, Collections.emptyList()))
						{
							worldgenrandom.setFeatureSeed(i, i1, l);
							Supplier<String> supplier = () -> {
								return registry1.getResourceKey(structurefeature).map(Object::toString).orElseGet(structurefeature::toString);
							};
					
							try {
								worldGenRegion.setCurrentlyGenerating(supplier);
								p_187714_.startsForFeature(sectionpos, structurefeature).forEach((p_196726_) -> {
									p_196726_.placeInChunk(worldGenRegion, p_187714_, this, worldgenrandom, getWritableArea(p_187713_), chunkpos);
								});
							} catch (Exception exception) {
								CrashReport crashreport1 = CrashReport.forThrowable(exception, "Feature placement");
								crashreport1.addCategory("Feature").setDetail("Description", supplier::get);
								throw new ReportedException(crashreport1);
							}
					
							++i1;
						}
					}
					
					if (l < j)
					{
						IntSet intset = new IntArraySet();
					
						for(Biome biome2x : set)
						{
							List<List<Supplier<PlacedFeature>>> list2 = biome2x.getGenerationSettings().features();
							if (l < list2.size())
							{
								List<Supplier<PlacedFeature>> list1 = list2.get(l);
								BiomeSource.StepFeatureData biomesource$stepfeaturedata1 = list.get(l);
								list1.stream().map(Supplier::get).forEach((p_196751_) -> {
									intset.add(biomesource$stepfeaturedata1.indexMapping().applyAsInt(p_196751_));
								});
							}
						}
						
						int j1 = intset.size();
						int[] aint = intset.toIntArray();
						Arrays.sort(aint);
						BiomeSource.StepFeatureData biomesource$stepfeaturedata = list.get(l);
						
						for(int k1 = 0; k1 < j1; ++k1)
						{
							int l1 = aint[k1];
							PlacedFeature placedfeature = biomesource$stepfeaturedata.features().get(l1);
							Supplier<String> supplier1 = () -> {
								return registry.getResourceKey(placedfeature).map(Object::toString).orElseGet(placedfeature::toString);
							};
							worldgenrandom.setFeatureSeed(i, l1, l);
							
							try {
								worldGenRegion.setCurrentlyGenerating(supplier1);
								placedfeature.placeWithBiomeCheck(worldGenRegion, this, worldgenrandom, blockpos);
							} catch (Exception exception1) {
								CrashReport crashreport2 = CrashReport.forThrowable(exception1, "Feature placement");
								crashreport2.addCategory("Feature").setDetail("Description", supplier1::get);
								throw new ReportedException(crashreport2);
							}
						}
					}
				}
				
				worldGenRegion.setCurrentlyGenerating((Supplier<String>)null);
			} catch (Exception exception2) {
				CrashReport crashreport = CrashReport.forThrowable(exception2, "Biome decoration");
				crashreport.addCategory("Generation").setDetail("CenterX", chunkpos.x).setDetail("CenterZ", chunkpos.z).setDetail("Seed", i);
				throw new ReportedException(crashreport);
			}
		}
	}
	
	private static BoundingBox getWritableArea(ChunkAccess p_187718_)
	{
		ChunkPos chunkpos = p_187718_.getPos();
		int i = chunkpos.getMinBlockX();
		int j = chunkpos.getMinBlockZ();
		LevelHeightAccessor levelheightaccessor = p_187718_.getHeightAccessorForGeneration();
		int k = levelheightaccessor.getMinBuildHeight() + 1;
		int l = levelheightaccessor.getMaxBuildHeight() - 1;
		return new BoundingBox(i, k, j, i + 15, l, j + 15);
	}
	
	// Mob spawning on chunk tick
	@Override
	public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureManager, MobCategory entityClassification, BlockPos blockPos)
	{
		if (!structureManager.hasAnyStructureAt(blockPos))
		{
			return super.getMobsAt(biome, structureManager, entityClassification, blockPos);
		} else {
			WeightedRandomList<MobSpawnSettings.SpawnerData> spawns = net.minecraftforge.common.world.StructureSpawnManager.getStructureSpawns(structureManager, entityClassification, blockPos);
			if (spawns != null) return spawns;
			return (entityClassification == MobCategory.UNDERGROUND_WATER_CREATURE || entityClassification == MobCategory.AXOLOTLS)
					&& structureManager.getStructureAt(blockPos, StructureFeature.OCEAN_MONUMENT).isValid()
					? MobSpawnSettings.EMPTY_MOB_LIST
					: super.getMobsAt(biome, structureManager, entityClassification, blockPos);
		}
	}
	
	// Mob spawning on initial chunk spawn (animals).
	@SuppressWarnings("deprecation")
	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion)
	{
		if (!this.dimensionSettingsSupplier.get().disableMobGeneration())
		{
			int chunkX = worldGenRegion.getCenter().x;
			int chunkZ = worldGenRegion.getCenter().z;
			IBiome biome = this.internalGenerator.getCachedBiomeProvider().getBiome(chunkX * Constants.CHUNK_SIZE, chunkZ * Constants.CHUNK_SIZE);
			WorldgenRandom sharedseedrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
			sharedseedrandom.setDecorationSeed(worldGenRegion.getSeed(), chunkX << 4, chunkZ << 4);
			NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, ((ForgeBiome)biome).getBiomeBase(), worldGenRegion.getCenter(), sharedseedrandom);
		}
	}	

	// Noise

	@Override
	public int getBaseHeight(int x, int z, Types heightmapType, LevelHeightAccessor world)
	{
		return this.sampleHeightmap(x, z, null, heightmapType.isOpaque());
	}

	// Provides a sample of the full column for structure generation.
	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world)
	{
		BlockState[] ablockstate = new BlockState[this.internalGenerator.getNoiseSizeY() * 8];
		this.sampleHeightmap(x, x, ablockstate, null);
		return new NoiseColumn(0, ablockstate);
	}

	// Samples the noise at a column and provides a view of the blockstates, or fills a heightmap.
	private int sampleHeightmap(int x, int z, @Nullable BlockState[] blockStates, @Nullable Predicate<BlockState> predicate)
	{
		// Get all of the coordinate starts and positions
		int xStart = Math.floorDiv(x, 4);
		int zStart = Math.floorDiv(z, 4);
		int xProgress = Math.floorMod(x, 4);
		int zProgress = Math.floorMod(z, 4);
		double xLerp = (double) xProgress / 4.0;
		double zLerp = (double) zProgress / 4.0;
		// Create the noise data in a 2 * 2 * 32 grid for interpolation.
		double[][] noiseData = new double[4][this.internalGenerator.getNoiseSizeY() + 1];

		// Initialize noise array.
		for (int i = 0; i < noiseData.length; i++)
		{
			noiseData[i] = new double[this.internalGenerator.getNoiseSizeY() + 1];
		}

		// Sample all 4 nearby columns.
		this.internalGenerator.getNoiseColumn(noiseData[0], xStart, zStart);
		this.internalGenerator.getNoiseColumn(noiseData[1], xStart, zStart + 1);
		this.internalGenerator.getNoiseColumn(noiseData[2], xStart + 1, zStart);
		this.internalGenerator.getNoiseColumn(noiseData[3], xStart + 1, zStart + 1);

		// [0, 32] -> noise chunks
		for (int noiseY = this.internalGenerator.getNoiseSizeY() - 1; noiseY >= 0; --noiseY)
		{
			// Gets all the noise in a 2x2x2 cube and interpolates it together.
			// Lower pieces
			double x0z0y0 = noiseData[0][noiseY];
			double x0z1y0 = noiseData[1][noiseY];
			double x1z0y0 = noiseData[2][noiseY];
			double x1z1y0 = noiseData[3][noiseY];
			// Upper pieces
			double x0z0y1 = noiseData[0][noiseY + 1];
			double x0z1y1 = noiseData[1][noiseY + 1];
			double x1z0y1 = noiseData[2][noiseY + 1];
			double x1z1y1 = noiseData[3][noiseY + 1];

			// [0, 8] -> noise pieces
			for (int pieceY = 7; pieceY >= 0; --pieceY)
			{
				double yLerp = (double) pieceY / 8.0;
				// Density at this position given the current y interpolation
				double density = Mth.lerp3(yLerp, xLerp, zLerp, x0z0y0, x0z0y1, x1z0y0, x1z0y1, x0z1y0, x0z1y1, x1z1y0, x1z1y1);

				// Get the real y position (translate noise chunk and noise piece)
				int y = (noiseY * 8) + pieceY;

				BlockState state = this.getBlockState(density, y);
				if (blockStates != null)
				{
					blockStates[y] = state;
				}

				// return y if it fails the check
				if (predicate != null && predicate.test(state))
				{
					return y + 1;
				}
			}
		}

		return 0;
	}

	// MC's NoiseChunkGenerator returns defaultBlock and defaultFluid here, so callers 
	// apparently don't rely on any blocks (re)placed after base terrain gen, only on
	// the default block/liquid set for the dimension (stone/water for overworld, 
	// netherrack/lava for nether), that MC uses for base terrain gen.
	// We can do the same, no need to pass biome config and fetch replaced blocks etc. 
	// OTG does place blocks other than defaultBlock/defaultLiquid during base terrain gen 
	// (for replaceblocks/sagc), but that shouldn't matter for the callers of this method.
	// Actually, it's probably better if they don't see OTG's replaced blocks, and just see
	// the default blocks instead, as vanilla MC would do.
	private BlockState getBlockState(double density, int y)
	{
		if (density > 0.0D)
		{
			return this.defaultBlock;
		}
		else if (y < this.getSeaLevel())
		{
			return this.defaultFluid;
		} else {
			return Blocks.AIR.defaultBlockState();
		}
	}

	// Getters / misc

	@Override
	protected Codec<? extends ChunkGenerator> codec()
	{
		return CODEC;
	}

	@Override
	public int getGenDepth()
	{
		return this.noiseHeight;
	}

	@Override
	public int getSeaLevel()
	{
		return this.dimensionSettingsSupplier.get().seaLevel();
	}
	
	@Override
	public int getMinY()
	{
		return this.dimensionSettingsSupplier.get().noiseSettings().minY();
	}	

	public CustomStructureCache getStructureCache(Path worldSaveFolder)
	{
		if(this.structureCache == null)
		{
			this.structureCache = OTG.getEngine().createCustomStructureCache(this.preset.getFolderName(), worldSaveFolder, this.seed, this.preset.getWorldConfig().getCustomStructureType() == CustomStructureType.BO4);
		}
		return this.structureCache;
	}

	double getBiomeBlocksNoiseValue(int blockX, int blockZ)
	{
		return this.internalGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
	}

	// Shadowgen
	
	public void stopWorkerThreads()
	{
		this.shadowChunkGenerator.stopWorkerThreads();
	}

	public Boolean checkHasVanillaStructureWithoutLoading(ServerLevel world, ChunkCoordinate chunkCoord)
	{
		return this.shadowChunkGenerator.checkHasVanillaStructureWithoutLoading(world, this, (OTGBiomeProvider)this.biomeSource, this.getSettings(), chunkCoord, this.internalGenerator.getCachedBiomeProvider(), false);
	}

	public int getHighestBlockYInUnloadedChunk(Random worldRandom, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, ServerLevel level)
	{
		return this.shadowChunkGenerator.getHighestBlockYInUnloadedChunk(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), worldRandom, x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow, level);
	}

	public LocalMaterialData getMaterialInUnloadedChunk(Random worldRandom, int x, int y, int z, ServerLevel level)
	{
		return this.shadowChunkGenerator.getMaterialInUnloadedChunk(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), worldRandom, x, y, z, level);
	}

	public ForgeChunkBuffer getChunkWithoutLoadingOrCaching(Random random, ChunkCoordinate chunkCoord, ServerLevel level)
	{
		return this.shadowChunkGenerator.getChunkWithoutLoadingOrCaching(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), random, chunkCoord, level);
	}
	
	// Modpack config
	// TODO: Move this?

	public String getPortalColor()
	{
		processDimensionConfigData();
		return this.portalColor;
	}

	public String getPortalMob()
	{
		processDimensionConfigData();
		return this.portalMob;
	}

	public String getPortalIgnitionSource()
	{
		processDimensionConfigData();
		return this.portalIgnitionSource;
	}
		
	public List<LocalMaterialData> getPortalBlocks()
	{
		processDimensionConfigData();
		return this.portalBlocks;
	}

	private void processDimensionConfigData()
	{
		if(!this.portalDataProcessed)
		{
			this.portalDataProcessed = true;
			if(this.dimConfig != null)
			{
				IMaterialReader materialReader = OTG.getEngine().getPresetLoader().getMaterialReader(this.preset.getFolderName());
				for(OTGDimension dim : this.dimConfig.Dimensions)
				{
					if(dim.PresetFolderName != null && this.preset.getFolderName().equals(dim.PresetFolderName))
					{
						if(dim.PortalBlocks != null && dim.PortalBlocks.trim().length() > 0)
						{
							String[] portalBlocks = dim.PortalBlocks.split(",");
							ArrayList<LocalMaterialData> materials = new ArrayList<LocalMaterialData>();					
							for(String materialString : portalBlocks)
							{
								LocalMaterialData material = null;
								try {
									material = materialReader.readMaterial(materialString.trim());
								} catch (InvalidConfigException e) { }
								if(material != null)
								{
									materials.add(material);
								}
							}
							this.portalBlocks = materials;
						}					
						this.portalColor = dim.PortalColor;
						this.portalMob = dim.PortalMob;
						this.portalIgnitionSource = dim.PortalIgnitionSource;
						break;
					}
				}
			}
			if(this.portalBlocks == null || this.portalBlocks.size() == 0)
			{
				this.portalBlocks = this.preset.getWorldConfig().getPortalBlocks(); 
			}
			if(this.portalColor == null)
			{
				this.portalColor = this.preset.getWorldConfig().getPortalColor();	
			}
			if(this.portalMob == null)
			{
				this.portalMob = this.preset.getWorldConfig().getPortalMob();
			}
			if(this.portalIgnitionSource == null)
			{
				this.portalIgnitionSource = this.preset.getWorldConfig().getPortalIgnitionSource();
			}
		}
	}

	protected final OTGNoiseSampler sampler = new OTGNoiseSampler();
	
	@Override
	public Sampler climateSampler()
	{
		return sampler;
	}
	
	public class OTGNoiseSampler implements Climate.Sampler
	{
		@Override
		public TargetPoint sample(int p_186975_, int p_186976_, int p_186977_)
		{
			return null;
		}
	}
	
	/** @deprecated */
	@Deprecated
	public Optional<BlockState> topMaterial(CarvingContext p_188669_, Function<BlockPos, Biome> p_188670_, ChunkAccess p_188671_, NoiseChunk p_188672_, BlockPos p_188673_, boolean p_188674_)
	{
		//return this.surfaceSystem.topMaterial(this.settings.get().surfaceRule(), p_188669_, p_188670_, p_188671_, p_188672_, p_188673_, p_188674_);
		return Optional.empty();
	}
}
