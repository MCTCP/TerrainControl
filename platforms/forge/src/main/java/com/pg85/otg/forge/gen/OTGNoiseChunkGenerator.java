package com.pg85.otg.forge.gen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

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
import com.pg85.otg.gen.OTGChunkGenerator;
import com.pg85.otg.gen.OTGChunkDecorator;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.ILayerSource;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.materials.LocalMaterialData;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.INoiseGenerator;
import net.minecraft.world.gen.OctavesNoiseGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.jigsaw.JigsawJunction;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.BastionRemantsStructure;
import net.minecraft.world.gen.feature.structure.BuriedTreasureStructure;
import net.minecraft.world.gen.feature.structure.DesertPyramidStructure;
import net.minecraft.world.gen.feature.structure.EndCityStructure;
import net.minecraft.world.gen.feature.structure.FortressStructure;
import net.minecraft.world.gen.feature.structure.IglooStructure;
import net.minecraft.world.gen.feature.structure.JunglePyramidStructure;
import net.minecraft.world.gen.feature.structure.MineshaftStructure;
import net.minecraft.world.gen.feature.structure.NetherFossilStructure;
import net.minecraft.world.gen.feature.structure.OceanMonumentStructure;
import net.minecraft.world.gen.feature.structure.OceanRuinStructure;
import net.minecraft.world.gen.feature.structure.PillagerOutpostStructure;
import net.minecraft.world.gen.feature.structure.RuinedPortalStructure;
import net.minecraft.world.gen.feature.structure.ShipwreckStructure;
import net.minecraft.world.gen.feature.structure.StrongholdStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.SwampHutStructure;
import net.minecraft.world.gen.feature.structure.VillageStructure;
import net.minecraft.world.gen.feature.structure.WoodlandMansionStructure;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.NoiseSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraft.world.storage.FolderName;

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
		super(biomeProvider1, biomeProvider2, dimensionSettingsSupplier.get().structureSettings(), seed);

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
		NoiseSettings noisesettings = dimensionsettings.noiseSettings();
		this.random = new WorldgenRandom(seed);
		this.surfaceNoise = (SurfaceNoise)(noisesettings.useSimplexSurfaceNoise() ? new PerlinSimplexNoise(this.random, IntStream.rangeClosed(-3, 0)) : new PerlinNoise(this.random, IntStream.rangeClosed(-3, 0)));
		this.noiseHeight = noisesettings.height();

		this.shadowChunkGenerator = new ShadowChunkGenerator(OTG.getEngine().getPluginConfig().getMaxWorkerThreads());
		this.internalGenerator = new OTGChunkGenerator(this.preset, seed, (ILayerSource) biomeProvider1,((ForgePresetLoader)OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetFolderName), OTG.getEngine().getLogger());
		this.chunkDecorator = new OTGChunkDecorator();
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
								if (villagePiece.getElement().getProjection() == StructureTemplatePool.Projection.RIGID)
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
		// TODO: Disable any surface/ground block related features for Template BiomeConfigs. 

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
					((ForgeBiome)biome).getBiomeBase().buildSurfaceAt(sharedseedrandom, chunk, worldX, worldZ, i2, d1, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), this.getSeaLevel(), 50, worldGenRegion.getSeed());
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
			List<Supplier<ConfiguredCarver<?>>> list = biomegenerationsettings.getCarvers(stage);
			
			// TODO: When using template biomes with MC biomes, we don't control which
			// default carvers were registered to biomes like we do with OTG biomes (always
			// ConfiguredCarvers.CAVE / ConfiguredCarvers.CANYON), so we only use OTG 
			// carvers for some MC biomes atm.
			
			boolean cavesEnabled = 
				this.preset.getWorldConfig().getCavesEnabled() &&
				list.stream().anyMatch(a -> a.get() == ConfiguredCarvers.CAVE || a.get() == ConfiguredCarvers.NETHER_CAVE)
			;
			
			// Nether biomes don't have canyons normally, so when NETHER_CAVE is present just add OTG canyons if enabled.
			boolean ravinesEnabled = 
				this.preset.getWorldConfig().getRavinesEnabled() && 
				list.stream().anyMatch(a -> a.get() == ConfiguredCarvers.CANYON || a.get() == ConfiguredCarvers.NETHER_CAVE)
			;
			
			if(cavesEnabled || ravinesEnabled)
			{
				ChunkPrimer protoChunk = (ChunkPrimer) chunk;
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
		BitSet bitset = ((ChunkPrimer)chunk).getOrCreateCarvingMask(stage);

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
					// OTG uses its own caves and canyon carvers, ignore the default ones.
					if(
						configuredcarver != ConfiguredCarvers.CAVE &&
						configuredcarver != ConfiguredCarvers.NETHER_CAVE &&
						configuredcarver != ConfiguredCarvers.CANYON						
					)
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
	public void createStructures(DynamicRegistries p_242707_1_, StructureManager p_242707_2_, IChunk p_242707_3_, TemplateManager p_242707_4_, long p_242707_5_)
	{
		ChunkPos chunkpos = p_242707_3_.getPos();
		ForgeBiome biome = (ForgeBiome)this.getCachedBiomeProvider().getNoiseBiome((chunkpos.x << 2) + 2, (chunkpos.z << 2) + 2);
		// Strongholds are hardcoded apparently, even if they aren't registered to the biome, so check worldconfig and biomeconfig toggles. 
		if(this.preset.getWorldConfig().getStrongholdsEnabled() && biome.getBiomeConfig().getStrongholdsEnabled())
		{
			createStructure(StructureFeatures.STRONGHOLD, p_242707_1_, p_242707_2_, p_242707_3_, p_242707_4_, p_242707_5_, chunkpos, biome.getBiomeBase());
		}
		for(Supplier<StructureFeature<?, ?>> supplier : biome.getBiomeBase().getGenerationSettings().structures())
		{
			// This doesn't catch modded structures, modded structures don't appear to have a type so we can't filter except by name.
			// We don't have to check biomeconfig toggles here, as that would only apply to non-template biomes and for those we
			// register structures ourselves, so we just don't register them in the first place.			
			if(
				(this.preset.getWorldConfig().getStrongholdsEnabled() || !(supplier.get().feature instanceof StrongholdStructure)) &&
				(this.preset.getWorldConfig().getVillagesEnabled() || !(supplier.get().feature instanceof VillageStructure)) &&				
				(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(supplier.get().feature instanceof SwampHutStructure)) &&				
				(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(supplier.get().feature instanceof IglooStructure)) &&
				(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(supplier.get().feature instanceof JunglePyramidStructure)) &&								
				(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(supplier.get().feature instanceof DesertPyramidStructure)) &&				
				(this.preset.getWorldConfig().getMineshaftsEnabled() || !(supplier.get().feature instanceof MineshaftStructure)) &&
				(this.preset.getWorldConfig().getRuinedPortalsEnabled() || !(supplier.get().feature instanceof RuinedPortalStructure)) &&
				(this.preset.getWorldConfig().getOceanRuinsEnabled() || !(supplier.get().feature instanceof OceanRuinStructure)) &&
				(this.preset.getWorldConfig().getShipWrecksEnabled() || !(supplier.get().feature instanceof ShipwreckStructure)) &&
				(this.preset.getWorldConfig().getOceanMonumentsEnabled() || !(supplier.get().feature instanceof OceanMonumentStructure)) &&
				(this.preset.getWorldConfig().getBastionRemnantsEnabled() || !(supplier.get().feature instanceof BastionRemantsStructure)) &&
				(this.preset.getWorldConfig().getBuriedTreasureEnabled() || !(supplier.get().feature instanceof BuriedTreasureStructure)) &&
				(this.preset.getWorldConfig().getEndCitiesEnabled() || !(supplier.get().feature instanceof EndCityStructure)) &&
				(this.preset.getWorldConfig().getNetherFortressesEnabled() || !(supplier.get().feature instanceof FortressStructure)) &&
				(this.preset.getWorldConfig().getNetherFossilsEnabled() || !(supplier.get().feature instanceof NetherFossilStructure)) &&
				(this.preset.getWorldConfig().getPillagerOutpostsEnabled() || !(supplier.get().feature instanceof PillagerOutpostStructure)) &&
				(this.preset.getWorldConfig().getWoodlandMansionsEnabled() || !(supplier.get().feature instanceof WoodlandMansionStructure))
			)
			{
				createStructure(supplier.get(), p_242707_1_, p_242707_2_, p_242707_3_, p_242707_4_, p_242707_5_, chunkpos, biome.getBiomeBase());
			}
		}
	}
	
	private void createStructure(StructureFeature<?, ?> p_242705_1_, DynamicRegistries p_242705_2_, StructureManager p_242705_3_, IChunk p_242705_4_, TemplateManager p_242705_5_, long p_242705_6_, ChunkPos p_242705_8_, Biome p_242705_9_)
	{
		StructureStart<?> structurestart = p_242705_3_.getStartForFeature(SectionPos.of(p_242705_4_.getPos(), 0), p_242705_1_.feature, p_242705_4_);
		int i = structurestart != null ? structurestart.getReferences() : 0;
		StructureSeparationSettings structureseparationsettings = this.getSettings().getConfig(p_242705_1_.feature);
		if (structureseparationsettings != null)
		{
			StructureStart<?> structurestart1 = p_242705_1_.generate(p_242705_2_, this, this.biomeSource, p_242705_5_, p_242705_6_, p_242705_8_, p_242705_9_, i, structureseparationsettings);
			p_242705_3_.setStartForFeature(SectionPos.of(p_242705_4_.getPos(), 0), p_242705_1_.feature, structurestart1, p_242705_4_);
		}
	}

	@Nullable
	@Override
	public BlockPos findNearestMapFeature(ServerWorld world, Structure<?> structure, BlockPos blockPos, int i1, boolean b1) 
	{
		// This doesn't catch modded structures, modded structures don't appear to have a type so we can't filter except by name.
		// We don't have to check biomeconfig toggles here, as that would only apply to non-template biomes and for those we
		// register structures ourselves, so we just don't register them in the first place.
		if(
			(this.preset.getWorldConfig().getStrongholdsEnabled() || !(structure instanceof StrongholdStructure)) &&
			(this.preset.getWorldConfig().getVillagesEnabled() || !(structure instanceof VillageStructure)) &&
			(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(structure instanceof SwampHutStructure)) &&
			(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(structure instanceof IglooStructure)) &&
			(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(structure instanceof JunglePyramidStructure)) &&
			(this.preset.getWorldConfig().getRareBuildingsEnabled() || !(structure instanceof DesertPyramidStructure)) &&
			(this.preset.getWorldConfig().getMineshaftsEnabled() || !(structure instanceof MineshaftStructure)) &&
			(this.preset.getWorldConfig().getRuinedPortalsEnabled() || !(structure instanceof RuinedPortalStructure)) &&
			(this.preset.getWorldConfig().getOceanRuinsEnabled() || !(structure instanceof OceanRuinStructure)) &&
			(this.preset.getWorldConfig().getShipWrecksEnabled() || !(structure instanceof ShipwreckStructure)) &&
			(this.preset.getWorldConfig().getOceanMonumentsEnabled() || !(structure instanceof OceanMonumentStructure)) &&
			(this.preset.getWorldConfig().getBastionRemnantsEnabled() || !(structure instanceof BastionRemantsStructure)) &&
			(this.preset.getWorldConfig().getBuriedTreasureEnabled() || !(structure instanceof BuriedTreasureStructure)) &&
			(this.preset.getWorldConfig().getEndCitiesEnabled() || !(structure instanceof EndCityStructure)) &&
			(this.preset.getWorldConfig().getNetherFortressesEnabled() || !(structure instanceof FortressStructure)) &&
			(this.preset.getWorldConfig().getNetherFossilsEnabled() || !(structure instanceof NetherFossilStructure)) &&
			(this.preset.getWorldConfig().getPillagerOutpostsEnabled() || !(structure instanceof PillagerOutpostStructure)) &&
			(this.preset.getWorldConfig().getWoodlandMansionsEnabled() || !(structure instanceof WoodlandMansionStructure))
		)
		{
			return super.findNearestMapFeature(world, structure, blockPos, i1, b1);
		}
		return null;
	}

	@Override
	public boolean hasStronghold(ChunkPos chunkPos)
	{
		// super.hasStronghold generates stronghold start points (default settingds appear 
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
			this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome.getBiomeConfig(), getStructureCache(worldSaveFolder));
			((ForgeBiome)biome).getBiomeBase().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
			alreadyDecorated.add(biome.getBiomeConfig().getOTGBiomeId());
			// Attempt to decorate other biomes if ImprovedBiomeDecoration - Frank
			if (getPreset().getWorldConfig().improvedBorderDecoration())
			{
				if (!alreadyDecorated.contains(biome1.getBiomeConfig().getOTGBiomeId()))
				{
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome1.getBiomeConfig(), getStructureCache(worldSaveFolder));
					if (!alreadyDecorated.contains(biome1.getBiomeConfig().getOTGBiomeId()))
					{
						((ForgeBiome)biome1).getBiomeBase().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
						alreadyDecorated.add(biome1.getBiomeConfig().getOTGBiomeId());						
					}					
				}
				if (!alreadyDecorated.contains(biome2.getBiomeConfig().getOTGBiomeId()))
				{
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome2.getBiomeConfig(), getStructureCache(worldSaveFolder));
					if (!alreadyDecorated.contains(biome2.getBiomeConfig().getOTGBiomeId()))
					{
						((ForgeBiome)biome2).getBiomeBase().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
						alreadyDecorated.add(biome2.getBiomeConfig().getOTGBiomeId());
					}					
				}
				if (!alreadyDecorated.contains(biome3.getBiomeConfig().getOTGBiomeId()))
				{
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome3.getBiomeConfig(), getStructureCache(worldSaveFolder));
					if (!alreadyDecorated.contains(biome3.getBiomeConfig().getOTGBiomeId()))
					{
						((ForgeBiome)biome3).getBiomeBase().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
						alreadyDecorated.add(biome3.getBiomeConfig().getOTGBiomeId());
					}					
				}
				if (!alreadyDecorated.contains(biome4.getBiomeConfig().getOTGBiomeId()))
				{
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, biome4.getBiomeConfig(), getStructureCache(worldSaveFolder));
					if (!alreadyDecorated.contains(biome4.getBiomeConfig().getOTGBiomeId()))
					{
						((ForgeBiome)biome4).getBiomeBase().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
					}
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
		BlockState[] ablockstate = new BlockState[256];
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

		IBiomeConfig biomeConfig = this.internalGenerator.getCachedBiomeProvider().getBiomeConfig(x, z);

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

				BlockState state = this.getBlockState(density, y, biomeConfig);
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

	protected BlockState getBlockState(double density, int y, IBiomeConfig config)
	{
		if (density > 0.0D)
		{
			return ((ForgeMaterialData) config.getStoneBlockReplaced(y)).internalBlock();
		}
		else if (y < this.getSeaLevel())
		{
			return ((ForgeMaterialData) config.getWaterBlockReplaced(y)).internalBlock();
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
		return this.shadowChunkGenerator.checkHasVanillaStructureWithoutLoading(world, this, (OTGBiomeProvider)this.biomeSource, this.getSettings(), chunkCoord, this.internalGenerator.getCachedBiomeProvider());
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
