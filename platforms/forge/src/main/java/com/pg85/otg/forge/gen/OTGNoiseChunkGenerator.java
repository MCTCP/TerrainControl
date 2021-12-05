package com.pg85.otg.forge.gen;

import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.config.dimensions.DimensionConfig;
import com.pg85.otg.core.config.dimensions.DimensionConfig.OTGDimension;
import com.pg85.otg.core.gen.OTGChunkDecorator;
import com.pg85.otg.core.gen.OTGChunkGenerator;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.forge.presets.ForgePresetLoader;
import com.pg85.otg.forge.biome.ForgeBiome;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.ILayerSource;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.materials.LocalMaterialData;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.BastionFeature;
import net.minecraft.world.level.levelgen.feature.BuriedTreasureFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.DesertPyramidFeature;
import net.minecraft.world.level.levelgen.feature.EndCityFeature;
import net.minecraft.world.level.levelgen.feature.IglooFeature;
import net.minecraft.world.level.levelgen.feature.JunglePyramidFeature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.NetherFortressFeature;
import net.minecraft.world.level.levelgen.feature.OceanMonumentFeature;
import net.minecraft.world.level.levelgen.feature.PillagerOutpostFeature;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.ShipwreckFeature;
import net.minecraft.world.level.levelgen.feature.StrongholdFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.SwamplandHutFeature;
import net.minecraft.world.level.levelgen.feature.VillageFeature;
import net.minecraft.world.level.levelgen.feature.WoodlandMansionFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool.Projection;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;
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
						(p_236090_0_) -> {
							return p_236090_0_.preset.getFolderName();
						}
					),
					Codec.STRING.fieldOf("dim_config_name").forGetter(
						(p_236090_0_) -> {
							return p_236090_0_.dimConfigName;
						}
					),
					BiomeSource.CODEC.fieldOf("biome_source").forGetter(
						(p_236096_0_) -> { return p_236096_0_.biomeSource; }
					),
					Codec.LONG.fieldOf("seed").stable().forGetter(
						(p_236093_0_) -> { return p_236093_0_.worldSeed; }
					),
					NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(
						(p_236090_0_) -> { return p_236090_0_.dimensionSettingsSupplier; }
					)
				).apply(
					p_236091_0_,
					p_236091_0_.stable(OTGNoiseChunkGenerator::new)
				)
			;
		}
	);

	private final Supplier<NoiseGeneratorSettings> dimensionSettingsSupplier;
	private final long worldSeed;
	private final int noiseHeight;
	private final SurfaceNoise surfaceNoise;
	protected final WorldgenRandom random;

	private final ShadowChunkGenerator shadowChunkGenerator;
	private final OTGChunkGenerator internalGenerator;
	private final OTGChunkDecorator chunkDecorator;
	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;	
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

	public OTGNoiseChunkGenerator(BiomeSource biomeProvider, long seed, Supplier<NoiseGeneratorSettings> dimensionSettingsSupplier)
	{
		this(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName(), null, biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	public OTGNoiseChunkGenerator(String presetFolderName, String dimConfigName, BiomeSource biomeProvider, long seed, Supplier<NoiseGeneratorSettings> dimensionSettingsSupplier)
	{
		this(presetFolderName, dimConfigName, biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	@SuppressWarnings("deprecation")
	private OTGNoiseChunkGenerator(String presetFolderName, String dimConfigName, BiomeSource biomeProvider1, BiomeSource biomeProvider2, long seed, Supplier<NoiseGeneratorSettings> dimensionSettingsSupplier)
	{
		super(biomeProvider1, biomeProvider2, overrideStructureSettings(dimensionSettingsSupplier.get().structureSettings(), presetFolderName), seed);

		if (!(biomeProvider1 instanceof ILayerSource))
		{
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}

		this.worldSeed = seed;
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
		this.random = new WorldgenRandom(seed);
		this.surfaceNoise = (SurfaceNoise)(noisesettings.useSimplexSurfaceNoise() ? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0)) : new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0)));
		this.noiseHeight = noisesettings.height();

		this.shadowChunkGenerator = new ShadowChunkGenerator(OTG.getEngine().getPluginConfig().getMaxWorkerThreads());
		this.internalGenerator = new OTGChunkGenerator(this.preset, seed, (ILayerSource) biomeProvider1,((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetFolderName), OTG.getEngine().getLogger());
		this.chunkDecorator = new OTGChunkDecorator();
	}
	
	private static StructureSettings overrideStructureSettings(StructureSettings oldSettings, String presetFolderName)
	{
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
		IWorldConfig worldConfig = preset.getWorldConfig();

		StructureSettings newSettings = new StructureSettings(
			Optional.of(new StrongholdConfiguration(worldConfig.getStrongHoldDistance(), worldConfig.getStrongHoldSpread(), worldConfig.getStrongHoldCount())), 
			Maps.newHashMap(
				ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder()
				.put(StructureFeature.VILLAGE, new StructureFeatureConfiguration(worldConfig.getVillageSpacing(), worldConfig.getVillageSeparation(), 10387312))
				.put(StructureFeature.DESERT_PYRAMID, new StructureFeatureConfiguration(worldConfig.getDesertPyramidSpacing(), worldConfig.getDesertPyramidSeparation(), 14357617))
				.put(StructureFeature.IGLOO, new StructureFeatureConfiguration(worldConfig.getIglooSpacing(), worldConfig.getIglooSeparation(), 14357618))
				.put(StructureFeature.JUNGLE_TEMPLE, new StructureFeatureConfiguration(worldConfig.getJungleTempleSpacing(), worldConfig.getJungleTempleSeparation(), 14357619))
				.put(StructureFeature.SWAMP_HUT, new StructureFeatureConfiguration(worldConfig.getSwampHutSpacing(), worldConfig.getSwampHutSeparation(), 14357620))
				.put(StructureFeature.PILLAGER_OUTPOST, new StructureFeatureConfiguration(worldConfig.getPillagerOutpostSpacing(), worldConfig.getPillagerOutpostSeparation(), 165745296))
				.put(StructureFeature.STRONGHOLD, new StructureFeatureConfiguration(worldConfig.getStrongholdSpacing(), worldConfig.getStrongholdSeparation(), 0))
				.put(StructureFeature.OCEAN_MONUMENT, new StructureFeatureConfiguration(worldConfig.getOceanMonumentSpacing(), worldConfig.getOceanMonumentSeparation(), 10387313))
				.put(StructureFeature.END_CITY, new StructureFeatureConfiguration(worldConfig.getEndCitySpacing(), worldConfig.getEndCitySeparation(), 10387313))
				.put(StructureFeature.WOODLAND_MANSION, new StructureFeatureConfiguration(worldConfig.getWoodlandMansionSpacing(), worldConfig.getWoodlandMansionSeparation(), 10387319))
				.put(StructureFeature.BURIED_TREASURE, new StructureFeatureConfiguration(worldConfig.getBuriedTreasureSpacing(), worldConfig.getBuriedTreasureSeparation(), 0))
				.put(StructureFeature.MINESHAFT, new StructureFeatureConfiguration(worldConfig.getMineshaftSpacing(), worldConfig.getMineshaftSeparation(), 0))
				.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(worldConfig.getRuinedPortalSpacing(), worldConfig.getRuinedPortalSeparation(), 34222645))
				.put(StructureFeature.SHIPWRECK, new StructureFeatureConfiguration(worldConfig.getShipwreckSpacing(), worldConfig.getShipwreckSeparation(), 165745295))
				.put(StructureFeature.OCEAN_RUIN, new StructureFeatureConfiguration(worldConfig.getOceanRuinSpacing(), worldConfig.getOceanRuinSeparation(), 14357621))
				.put(StructureFeature.BASTION_REMNANT, new StructureFeatureConfiguration(worldConfig.getBastionRemnantSpacing(), worldConfig.getBastionRemnantSeparation(), 30084232))
				.put(StructureFeature.NETHER_BRIDGE, new StructureFeatureConfiguration(worldConfig.getNetherFortressSpacing(), worldConfig.getNetherFortressSeparation(), 30084232))
				.put(StructureFeature.NETHER_FOSSIL, new StructureFeatureConfiguration(worldConfig.getNetherFossilSpacing(), worldConfig.getNetherFossilSeparation(), 14357921))
				.putAll(
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
				).build()
			)
		);
		return newSettings;
	}

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
		return new OTGNoiseChunkGenerator(this.preset.getFolderName(), this.dimConfigName, this.biomeSource.withSeed(seed), seed, this.dimensionSettingsSupplier);
	}
	
	// Base terrain gen
	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, StructureFeatureManager accessor, ChunkAccess chunk)
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
		//this.shadowChunkGenerator.queueChunksForWorkerThreads((WorldGenRegion)world, manager, chunk, this, (OTGBiomeProvider)this.biomeSource, this.internalGenerator, this.getSettings(), this.preset.getWorldConfig().getWorldHeightCap());
		
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
	public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunk)
	{
		// OTG handles surface/ground blocks during base terrain gen. For non-OTG biomes used
		// with TemplateForBiome, we want to use registered surfacebuilders though.

		ChunkPos chunkpos = chunk.getPos();
		int i = chunkpos.x;
		int j = chunkpos.z;
		WorldgenRandom sharedseedrandom = new WorldgenRandom();
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
		// Skip bedrock, OTG always handles that.
	}

	// Carvers: Caves and ravines

	@Override
	public void applyCarvers(long seed, BiomeManager biomeManager, ChunkAccess chunk, GenerationStep.Carving stage)
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
				BitSet carvingMask = protoChunk.getOrCreateCarvingMask(stage);
				this.internalGenerator.carve(chunkBuffer, seed, protoChunk.getPos().x, protoChunk.getPos().z, carvingMask, cavesEnabled, ravinesEnabled);
			}
		}
		applyNonOTGCarvers(seed, biomeManager, chunk, stage);
	}

	public void applyNonOTGCarvers(long seed, BiomeManager biomeManager, ChunkAccess chunk, GenerationStep.Carving stage)
	{
		BiomeManager biomemanager = biomeManager.withDifferentSource(this.biomeSource);
		WorldgenRandom sharedseedrandom = new WorldgenRandom();
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
	}
	
	// Structures
	
	// Override structure spawning to make sure any structures registered
	// to biomes are allowed to spawn according to worldconfig settings. 
	@Override
	public void createStructures(RegistryAccess p_242707_1_, StructureFeatureManager p_242707_2_, ChunkAccess p_242707_3_, StructureManager p_242707_4_, long p_242707_5_)
	{
		ChunkPos chunkpos = p_242707_3_.getPos();
		ForgeBiome biome = (ForgeBiome)this.getCachedBiomeProvider().getNoiseBiome((chunkpos.x << 2) + 2, (chunkpos.z << 2) + 2);
		// Strongholds are hardcoded apparently, even if they aren't registered to the biome, so check worldconfig and biomeconfig toggles. 
		if(this.preset.getWorldConfig().getStrongholdsEnabled() && biome.getBiomeConfig().getStrongholdsEnabled())
		{
			createStructure(StructureFeatures.STRONGHOLD, p_242707_1_, p_242707_2_, p_242707_3_, p_242707_4_, p_242707_5_, chunkpos, biome.getBiomeBase());
		}
		for(Supplier<ConfiguredStructureFeature<?, ?>> supplier : biome.getBiomeBase().getGenerationSettings().structures())
		{
			// This doesn't catch modded structures, modded structures don't appear to have a type so we can't filter except by name.
			// We don't have to check biomeconfig toggles here, as that would only apply to non-template biomes and for those we
			// register structures ourselves, so we just don't register them in the first place.
			if(
				(this.preset.getWorldConfig().getStrongholdsEnabled() || !(supplier.get().feature instanceof StrongholdFeature)) &&
				(this.preset.getWorldConfig().getVillagesEnabled() || !(supplier.get().feature instanceof VillageFeature)) &&				
				(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(supplier.get().feature instanceof SwamplandHutFeature)) &&				
				(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(supplier.get().feature instanceof IglooFeature)) &&
				(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(supplier.get().feature instanceof JunglePyramidFeature)) &&								
				(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(supplier.get().feature instanceof DesertPyramidFeature)) &&				
				(this.preset.getWorldConfig().getMineshaftsEnabled() || !(supplier.get().feature instanceof MineshaftFeature)) &&
				(this.preset.getWorldConfig().getRuinedPortalsEnabled() || !(supplier.get().feature instanceof RuinedPortalFeature)) &&
				(this.preset.getWorldConfig().getOceanRuinsEnabled() || !(supplier.get().feature instanceof OceanRuinFeature)) &&
				(this.preset.getWorldConfig().getShipWrecksEnabled() || !(supplier.get().feature instanceof ShipwreckFeature)) &&
				(this.preset.getWorldConfig().getOceanMonumentsEnabled() || !(supplier.get().feature instanceof OceanMonumentFeature)) &&
				(this.preset.getWorldConfig().getBastionRemnantsEnabled() || !(supplier.get().feature instanceof BastionFeature)) &&
				(this.preset.getWorldConfig().getBuriedTreasureEnabled() || !(supplier.get().feature instanceof BuriedTreasureFeature)) &&
				(this.preset.getWorldConfig().getEndCitiesEnabled() || !(supplier.get().feature instanceof EndCityFeature)) &&
				(this.preset.getWorldConfig().getNetherFortressesEnabled() || !(supplier.get().feature instanceof NetherFortressFeature)) &&
				(this.preset.getWorldConfig().getNetherFossilsEnabled() || !(supplier.get().feature instanceof NetherFossilFeature)) &&
				(this.preset.getWorldConfig().getPillagerOutpostsEnabled() || !(supplier.get().feature instanceof PillagerOutpostFeature)) &&
				(this.preset.getWorldConfig().getWoodlandMansionsEnabled() || !(supplier.get().feature instanceof WoodlandMansionFeature))
			)
			{
				createStructure(supplier.get(), p_242707_1_, p_242707_2_, p_242707_3_, p_242707_4_, p_242707_5_, chunkpos, biome.getBiomeBase());
			}
		}
	}
	
	private void createStructure(ConfiguredStructureFeature<?, ?> p_242705_1_, RegistryAccess p_242705_2_, StructureFeatureManager p_242705_3_, ChunkAccess p_242705_4_, StructureManager p_242705_5_, long p_242705_6_, ChunkPos p_242705_8_, Biome p_242705_9_)
	{
		StructureStart<?> structurestart = p_242705_3_.getStartForFeature(SectionPos.of(p_242705_4_.getPos(), 0), p_242705_1_.feature, p_242705_4_);
		int i = structurestart != null ? structurestart.getReferences() : 0;
		StructureFeatureConfiguration structureseparationsettings = this.getSettings().getConfig(p_242705_1_.feature);
		if (structureseparationsettings != null)
		{
			StructureStart<?> structurestart1 = p_242705_1_.generate(p_242705_2_, this, this.biomeSource, p_242705_5_, p_242705_6_, p_242705_8_, p_242705_9_, i, structureseparationsettings, p_242705_4_);
			p_242705_3_.setStartForFeature(SectionPos.of(p_242705_4_.getPos(), 0), p_242705_1_.feature, structurestart1, p_242705_4_);
		}
	}

	@Nullable
	@Override
	public BlockPos findNearestMapFeature(ServerLevel world, StructureFeature<?> structure, BlockPos blockPos, int i1, boolean b1) 
	{
		// This doesn't catch modded structures, modded structures don't appear to have a type so we can't filter except by name.
		// We don't have to check biomeconfig toggles here, as that would only apply to non-template biomes and for those we
		// register structures ourselves, so we just don't register them in the first place.
		if(
			(this.preset.getWorldConfig().getStrongholdsEnabled() || !(structure instanceof StrongholdFeature)) &&
			(this.preset.getWorldConfig().getVillagesEnabled() || !(structure instanceof VillageFeature)) &&
			(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(structure instanceof SwamplandHutFeature)) &&
			(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(structure instanceof IglooFeature)) &&
			(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(structure instanceof JunglePyramidFeature)) &&
			(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(structure instanceof DesertPyramidFeature)) &&
			(this.preset.getWorldConfig().getMineshaftsEnabled() || !(structure instanceof MineshaftFeature)) &&
			(this.preset.getWorldConfig().getRuinedPortalsEnabled() || !(structure instanceof RuinedPortalFeature)) &&
			(this.preset.getWorldConfig().getOceanRuinsEnabled() || !(structure instanceof OceanRuinFeature)) &&
			(this.preset.getWorldConfig().getShipWrecksEnabled() || !(structure instanceof ShipwreckFeature)) &&
			(this.preset.getWorldConfig().getOceanMonumentsEnabled() || !(structure instanceof OceanMonumentFeature)) &&
			(this.preset.getWorldConfig().getBastionRemnantsEnabled() || !(structure instanceof BastionFeature)) &&
			(this.preset.getWorldConfig().getBuriedTreasureEnabled() || !(structure instanceof BuriedTreasureFeature)) &&
			(this.preset.getWorldConfig().getEndCitiesEnabled() || !(structure instanceof EndCityFeature)) &&
			(this.preset.getWorldConfig().getNetherFortressesEnabled() || !(structure instanceof NetherFortressFeature)) &&
			(this.preset.getWorldConfig().getNetherFossilsEnabled() || !(structure instanceof NetherFossilFeature)) &&
			(this.preset.getWorldConfig().getPillagerOutpostsEnabled() || !(structure instanceof PillagerOutpostFeature)) &&
			(this.preset.getWorldConfig().getWoodlandMansionsEnabled() || !(structure instanceof WoodlandMansionFeature))
		)
		{
			return super.findNearestMapFeature(world, structure, blockPos, i1, b1);
		}
		return null;
	}

	@Override
	public boolean hasStronghold(ChunkPos chunkPos)
	{
		// super.hasStronghold generates stronghold start points (default settings appear 
		// determined per dim type), so check worldconfig and biomeconfig toggles.
		ForgeBiome biome = (ForgeBiome)this.getCachedBiomeProvider().getNoiseBiome((chunkPos.x << 2) + 2, (chunkPos.z << 2) + 2);
		if(this.preset.getWorldConfig().getStrongholdsEnabled() && biome.getBiomeConfig().getStrongholdsEnabled())
		{
			return super.hasStronghold(chunkPos);
		}
		return false;
	}
	
	// Decoration

	// Does decoration for a given pos/chunk
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
		WorldgenRandom sharedseedrandom = new WorldgenRandom();
		long decorationSeed = sharedseedrandom.setDecorationSeed(worldGenRegion.getSeed(), worldX, worldZ);	
		//

		ChunkCoordinate chunkBeingDecorated = ChunkCoordinate.fromBlockCoords(worldX, worldZ);
		ForgeWorldGenRegion forgeWorldGenRegion = new ForgeWorldGenRegion(this.preset.getFolderName(), this.preset.getWorldConfig(), worldGenRegion, this);
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
			/*
			 * Here's how the code works that was added for the ImprovedBorderDecoration code.
			 * - List of biome ids is initialized, will be used to ensure biomes are not populated twice.
			 * - Placement is done for the main biome
			 * - If ImprovedBorderDecoration is true, will attempt to perform decoration from any biomes that have not
			 * already been decorated. Thus preventing decoration from happening twice.
			 *
			 * - Frank
			 */
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
			WorldgenRandom sharedseedrandom = new WorldgenRandom();
			sharedseedrandom.setDecorationSeed(worldGenRegion.getSeed(), chunkX << 4, chunkZ << 4);
			NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, ((ForgeBiome)biome).getBiomeBase(), worldGenRegion.getCenter(), sharedseedrandom);
		}
	}
	
	// Mob spawning on chunk tick
	@Override
	public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureManager, MobCategory entityClassification, BlockPos blockPos)
	{
		// Forge code injected into NoiseChunkGenerator
		WeightedRandomList<MobSpawnSettings.SpawnerData> spawns = net.minecraftforge.common.world.StructureSpawnManager.getStructureSpawns(structureManager, entityClassification, blockPos);
		if (spawns != null)
		{
			return spawns;
		}
		return super.getMobsAt(biome, structureManager, entityClassification, blockPos);
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

	public CustomStructureCache getStructureCache(Path worldSaveFolder)
	{
		if(this.structureCache == null)
		{
			this.structureCache = OTG.getEngine().createCustomStructureCache(this.preset.getFolderName(), worldSaveFolder, this.worldSeed, this.preset.getWorldConfig().getCustomStructureType() == CustomStructureType.BO4);
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
}
