package com.pg85.otg.forge.gen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.gen.OTGChunkGenerator;
import com.pg85.otg.gen.OTGChunkPopulator;
import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.jigsaw.JigsawJunction;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.VillageStructure;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.NoiseSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraft.world.storage.FolderName;
import net.minecraft.world.storage.IServerWorldInfo;

public final class OTGNoiseChunkGenerator extends ChunkGenerator
{
	// Create a codec to serialise/deserialise OTGNoiseChunkGenerator
	public static final Codec<OTGNoiseChunkGenerator> CODEC = RecordCodecBuilder.create(
		(p_236091_0_) ->
		{
			return p_236091_0_
				.group(
					Codec.STRING.fieldOf("otg_dimension_config").forGetter(
						(p_236090_0_) -> {
							// TODO: Use bytestream instead?
							return p_236090_0_.dimensionConfig.toYamlString();
						}
					),
					BiomeProvider.CODEC.fieldOf("biome_source").forGetter(
						(p_236096_0_) -> { return p_236096_0_.biomeSource; }
					),
					Codec.LONG.fieldOf("seed").stable().forGetter(
						(p_236093_0_) -> { return p_236093_0_.worldSeed; }
					),
					DimensionSettings.CODEC.fieldOf("settings").forGetter(
						(p_236090_0_) -> { return p_236090_0_.dimensionSettingsSupplier; }
					)
				).apply(
					p_236091_0_,
					p_236091_0_.stable(OTGNoiseChunkGenerator::new)
				)
			;
		}
	);

	private final Supplier<DimensionSettings> dimensionSettingsSupplier;
	private final long worldSeed;
	private final int noiseHeight;

	private final OTGChunkGenerator internalGenerator;
	private final OTGChunkPopulator chunkPopulator;
	private final DimensionConfig dimensionConfig;
	private final Preset preset;
	// Unloaded chunk data caches for BO4's
	private final FifoMap<BlockPos2D, LocalMaterialData[]> unloadedBlockColumnsCache;
	private final FifoMap<ChunkCoordinate, IChunk> unloadedChunksCache;
	// TODO: Move this to WorldLoader when ready?
	private CustomStructureCache structureCache;
	// TODO: Move this to WorldLoader when ready?
	private boolean isInitialised = false;

	private final Map<Integer, List<Structure<?>>> biomeStructures;

	public OTGNoiseChunkGenerator(BiomeProvider biomeProvider, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		this(new DimensionConfig(OTG.getEngine().getPresetLoader().getDefaultPresetName()), biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	public OTGNoiseChunkGenerator(DimensionConfig dimensionConfig, BiomeProvider biomeProvider, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		this(dimensionConfig, biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	private OTGNoiseChunkGenerator(String dimensionConfigYaml, BiomeProvider biomeProvider, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		this(DimensionConfig.fromYamlString(dimensionConfigYaml), biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}
	
	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	private OTGNoiseChunkGenerator(DimensionConfig dimensionConfigSupplier, BiomeProvider biomeProvider1, BiomeProvider biomeProvider2, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		super(biomeProvider1, biomeProvider2, dimensionSettingsSupplier.get().structureSettings(), seed);

		if (!(biomeProvider1 instanceof LayerSource))
		{
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}

		this.biomeStructures = Registry.STRUCTURE_FEATURE.stream().collect(Collectors.groupingBy((structure) -> {
			return structure.step().ordinal();
		}));

		this.dimensionConfig = dimensionConfigSupplier;
		this.worldSeed = seed;
		DimensionSettings dimensionsettings = dimensionSettingsSupplier.get();
		this.dimensionSettingsSupplier = dimensionSettingsSupplier;
		NoiseSettings noisesettings = dimensionsettings.noiseSettings();
		this.noiseHeight = noisesettings.height();

		// Unloaded chunk data caches for BO4's
		// TODO: Add a setting to the worldconfig for the size of these caches.
		// Worlds with lots of BO4's and large smoothing areas may want to increase this.
		this.unloadedBlockColumnsCache = new FifoMap<BlockPos2D, LocalMaterialData[]>(1024);
		this.unloadedChunksCache = new FifoMap<ChunkCoordinate, IChunk>(128);
		//

		this.preset = OTG.getEngine().getPresetLoader().getPresetByName(this.dimensionConfig.PresetName);

		this.internalGenerator = new OTGChunkGenerator(preset, seed, (LayerSource) biomeProvider1);
		this.chunkPopulator = new OTGChunkPopulator();
	}

	public void saveStructureCache()
	{
		if (this.chunkPopulator.getIsSaveRequired())
		{
			this.structureCache.saveToDisk(OTG.getEngine().getPluginConfig().getSpawnLogEnabled(), OTG.getEngine().getLogger(), this.chunkPopulator);
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
		return new OTGNoiseChunkGenerator(this.dimensionConfig, this.biomeSource.withSeed(seed), seed, this.dimensionSettingsSupplier);
	}

	private void init(Path worldSaveFolder)
	{
		if (!isInitialised)
		{
			isInitialised = true;
			this.structureCache = OTG.getEngine().createCustomStructureCache(this.preset.getName(), worldSaveFolder, 0, this.worldSeed, this.preset.getWorldConfig().getCustomStructureType() == CustomStructureType.BO4);
		}
	}

	// Base terrain gen

	// Generates the base terrain for a chunk.
	@Override
	public void fillFromNoise(IWorld world, StructureManager manager, IChunk chunk)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);
	
		// If we've already generated and cached this	
		// chunk while it was unloaded, use cached data.		
		ChunkBuffer buffer = new ForgeChunkBuffer((ChunkPrimer) chunk);
		IChunk cachedChunk = unloadedChunksCache.get(chunkCoord);
		if (cachedChunk != null)
		{
			// TODO: Find some way to clone/swap chunk data efficiently :/
			for (int x = 0; x < ChunkCoordinate.CHUNK_SIZE; x++)
			{
				for (int z = 0; z < ChunkCoordinate.CHUNK_SIZE; z++)
				{
					int endY = cachedChunk.getOrCreateHeightmapUnprimed(Type.WORLD_SURFACE_WG).getFirstAvailable(x, z);
					for (int y = 0; y <= endY; y++)
					{
						BlockPos pos = new BlockPos(x, y, z);
						chunk.setBlockState(pos, cachedChunk.getBlockState(pos), false);
					}
				}
			}
			this.unloadedChunksCache.remove(chunkCoord);
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
			for(Structure<?> structure : Structure.NOISE_AFFECTING_FEATURES) {
				// Get all structure starts in this chunk
				manager.startsForFeature(SectionPos.of(pos, 0), structure).forEach((start) -> {
					// Iterate through the pieces in the structure
					for(StructurePiece piece : start.getPieces()) {
						// Check if it intersects with this chunk
						if (piece.isCloseToChunk(pos, 12)) {
							MutableBoundingBox box = piece.getBoundingBox();

							if (piece instanceof AbstractVillagePiece) {
								AbstractVillagePiece villagePiece = (AbstractVillagePiece) piece;
								// Add to the list if it's a rigid piece
								if (villagePiece.getElement().getProjection() == JigsawPattern.PlacementBehaviour.RIGID) {
									structures.add(new JigsawStructureData(box.x0, box.y0, box.z0, box.x1, villagePiece.getGroundLevelDelta(), box.z1, true, 0, 0, 0));
								}

								// Get all the junctions in this piece
								for(JigsawJunction junction : villagePiece.getJunctions()) {
									int sourceX = junction.getSourceX();
									int sourceZ = junction.getSourceZ();

									// If the junction is in this chunk, then add to list
									if (sourceX > startX - 12 && sourceZ > startZ - 12 && sourceX < startX + 15 + 12 && sourceZ < startZ + 15 + 12) {
										junctions.add(new JigsawStructureData(0, 0, 0,0, 0, 0, false, junction.getSourceX(), junction.getSourceGroundY(), junction.getSourceZ()));
									}
								}
							} else {
								structures.add(new JigsawStructureData(box.x0, box.y0, box.z0,box.x1, 0, box.z1, false, 0, 0, 0));
							}
						}
					}

				});
			}

			this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), world.getRandom(), buffer, buffer.getChunkCoordinate(), structures, junctions);
		}
	}

	// Replaces surface and ground blocks in base terrain and places bedrock.
	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, IChunk chunk)
	{
		// Done during this.internalGenerator.populateNoise
		// TODO: Not doing this ignores any SurfaceBuilders registered to this biome. We may have to enable this for non-otg biomes / non-otg surfacebuilders?
	}

	// Carves caves and ravines
	@Override
	public void applyCarvers(long seed, BiomeManager biomeManager, IChunk chunk, GenerationStage.Carving stage)
	{
		if (stage == GenerationStage.Carving.AIR)
		{
			ChunkPrimer protoChunk = (ChunkPrimer) chunk;

			ChunkBuffer chunkBuffer = new ForgeChunkBuffer(protoChunk);
			BitSet carvingMask = protoChunk.getOrCreateCarvingMask(stage);
			this.internalGenerator.carve(chunkBuffer, seed, protoChunk.getPos().x, protoChunk.getPos().z, carvingMask);
		}
	}

	// Population / decoration

	// Does population for a given pos/chunk
	@Override
	public void applyBiomeDecoration(WorldGenRegion worldGenRegion, StructureManager structureManager)
	{
		int chunkX = worldGenRegion.getCenterX();
		int chunkZ = worldGenRegion.getCenterZ();
		int blockX = chunkX * 16;
		int blockZ = chunkZ * 16;
		BlockPos blockpos = new BlockPos(blockX, 0, blockZ);
		
		// Fetch the biomeConfig by registryKey
		RegistryKey<Biome> key = ((OTGBiomeProvider) this.biomeSource).getBiomeRegistryKey((chunkX << 2) + 2, 2, (chunkZ << 2) + 2);
		BiomeConfig biomeConfig = OTG.getEngine().getPresetLoader().getBiomeConfig(key.location().toString());
		Biome biome = this.biomeSource.getNoiseBiome((chunkX << 2) + 2, 2, (chunkZ << 2) + 2);
		SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
		long decorationSeed = sharedseedrandom.setDecorationSeed(worldGenRegion.getSeed(), blockX, blockZ);
		try
		{
			// Override normal population (Biome.func_242427_a()) with OTG's.
			biomePopulate(biome, biomeConfig, structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
		} catch (Exception exception) {
			CrashReport crashreport = CrashReport.forThrowable(exception, "Biome decoration");
			crashreport.addCategory("Generation").setDetail("CenterX", chunkX).setDetail("CenterZ", chunkZ).setDetail("Seed", decorationSeed);
			throw new ReportedException(crashreport);
		}	
	}

	// Chunk population method taken from Biome (Biome.func_242427_a())
	private void biomePopulate(Biome biome, BiomeConfig biomeConfig, StructureManager structureManager, ChunkGenerator chunkGenerator, WorldGenRegion world, long seed, SharedSeedRandom random, BlockPos pos)
	{
		// World save folder name may not be identical to level name, fetch it.
		Path worldSaveFolder = world.getLevel().getServer().getWorldPath(FolderName.PLAYER_DATA_DIR).getParent();
		init(worldSaveFolder);
		ChunkCoordinate chunkBeingPopulated = ChunkCoordinate.fromBlockCoords(pos.getX(), pos.getZ());
		
		// TODO: Implement resources avoiding villages in common: if (world.startsForFeature(SectionPos.of(blockPos), Structure.VILLAGE).findAny().isPresent())
		this.chunkPopulator.populate(chunkBeingPopulated, new ForgeWorldGenRegion(this.preset.getName(), this.preset.getWorldConfig(), world, this), biomeConfig, this.structureCache);
		
		List<List<Supplier<ConfiguredFeature<?, ?>>>> list = biome.getGenerationSettings().features();		
		
		// TODO: Spawn snow only after this!
		// Vanilla structure generation
		for(int step = 0; step < GenerationStage.Decoration.values().length; ++step)
		{
			int index = 0;
			// Generate features if enabled
			if (structureManager.shouldGenerateFeatures())
			{
				// Go through all the structures set to generate at this step
				for (Structure<?> structure : this.biomeStructures.getOrDefault(step, Collections.emptyList()))
				{
					// Reset the random
					random.setFeatureSeed(seed, index, step);
					int chunkX = pos.getX() >> 4;
					int chunkZ = pos.getZ() >> 4;
					int chunkStartX = chunkX << 4;
					int chunkStartZ = chunkZ << 4;

					try
					{
						// Generate the structure if it exists in a biome in this chunk.
						// We don't have to do any work here, we can just let StructureManager handle it all.
						structureManager.startsForFeature(SectionPos.of(pos), structure)
							.forEach(start ->
								start.placeInChunk(
									world, 
									structureManager, 
									chunkGenerator, 
									random, 
									new MutableBoundingBox(
										chunkStartX, 
										chunkStartZ, 
										chunkStartX + 15, 
										chunkStartZ + 15
									), 
									new ChunkPos(chunkX, chunkZ)
								)
							)
						;
					} catch (Exception exception) {
						CrashReport crashreport = CrashReport.forThrowable(exception, "Feature placement");
						crashreport.addCategory("Feature")
							.setDetail("Id", Registry.STRUCTURE_FEATURE.getKey(structure))
							.setDetail("Description", () -> structure.toString())
						;
						throw new ReportedException(crashreport);
					}

					++index;
				}
			}

			// Spawn any non-OTG resources registered to this biome.
			if (list.size() > step)
			{
				for(Supplier<ConfiguredFeature<?, ?>> supplier : list.get(step))
				{
					ConfiguredFeature<?, ?> configuredfeature = supplier.get();
					random.setFeatureSeed(seed, index, step);
					try {
						configuredfeature.place(world, chunkGenerator, random, pos);
					} catch (Exception exception1) {
						CrashReport crashreport1 = CrashReport.forThrowable(exception1, "Feature placement");
						crashreport1.addCategory("Feature").setDetail("Id", Registry.FEATURE.getKey(configuredfeature.feature)).setDetail("Config", configuredfeature.config).setDetail("Description", () -> {
							return configuredfeature.feature.toString();
						});
						throw new ReportedException(crashreport1);
					}
					++index;
				}
			}
		}
	}

	// Mob spawning on initial chunk spawn (animals).
	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion)
	{
		if (!this.dimensionSettingsSupplier.get().disableMobGeneration())
		{
			int chunkX = worldGenRegion.getCenterX();
			int chunkZ = worldGenRegion.getCenterZ();
			Biome biome = worldGenRegion.getBiome((new ChunkPos(chunkX, chunkZ)).getWorldPosition());
			SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
			sharedseedrandom.setDecorationSeed(worldGenRegion.getSeed(), chunkX << 4, chunkZ << 4);
			WorldEntitySpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, chunkX, chunkZ, sharedseedrandom);
		}
	}

	// Mob spawning on chunk tick
	@Override
	public List<MobSpawnInfo.Spawners> getMobsAt(Biome biome, StructureManager structureManager, EntityClassification entityClassification, BlockPos blockPos)
	{
		// TODO: Allow users to configure spawn lists for vanilla structures?
		
		if (structureManager.getStructureAt(blockPos, true, Structure.SWAMP_HUT).isValid())
		{
			if (entityClassification == EntityClassification.MONSTER)
			{
				return Structure.SWAMP_HUT.getDefaultSpawnList();
			}

			if (entityClassification == EntityClassification.CREATURE)
			{
				return Structure.SWAMP_HUT.getDefaultCreatureSpawnList();
			}
		}

		if (entityClassification == EntityClassification.MONSTER)
		{
			if (structureManager.getStructureAt(blockPos, false, Structure.PILLAGER_OUTPOST).isValid())
			{
				return Structure.PILLAGER_OUTPOST.getDefaultSpawnList();
			}

			if (structureManager.getStructureAt(blockPos, false, Structure.OCEAN_MONUMENT).isValid())
			{
				return Structure.OCEAN_MONUMENT.getDefaultSpawnList();
			}

			if (structureManager.getStructureAt(blockPos, true, Structure.NETHER_BRIDGE).isValid())
			{
				return Structure.NETHER_BRIDGE.getDefaultSpawnList();
			}
		}

		return super.getMobsAt(biome, structureManager, entityClassification, blockPos);
	}

	// Noise

	@Override
	public int getBaseHeight(int x, int z, Type heightmapType)
	{
		return this.sampleHeightmap(x, z, null, heightmapType.isOpaque());
	}

	// Provides a sample of the full column for structure generation.
	@Override
	public IBlockReader getBaseColumn(int x, int z)
	{
		BlockState[] ablockstate = new BlockState[256];
		this.sampleHeightmap(x, x, ablockstate, null);
		return new Blockreader(ablockstate);
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
				double density = MathHelper.lerp3(yLerp, xLerp, zLerp, x0z0y0, x0z0y1, x1z0y0, x1z0y1, x0z1y0, x0z1y1, x1z1y0, x1z1y1);

				// Get the real y position (translate noise chunk and noise piece)
				int y = (noiseY * 8) + pieceY;

				BlockState state = this.getBlockState(density, y, this.internalGenerator.getBiomeAtWorldCoord(x, z));
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

	public CustomStructureCache getStructureCache()
	{
		return this.structureCache;
	}

	// BO4's / Smoothing Areas

	// BO4's and smoothing areas may do material and height checks in unloaded chunks, OTG generates 
	// base terrain for the chunks in memory and caches the result in a limited size-cache. Cached
	// data is used if/when the chunk is "properly" generated.

	private LocalMaterialData[] getBlockColumnInUnloadedChunk(IWorldGenRegion worldGenRegion, int x, int z)
	{
		BlockPos2D blockPos = new BlockPos2D(x, z);
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);

		// Get internal coordinates for block in chunk
		byte blockX = (byte) (x &= 0xF);
		byte blockZ = (byte) (z &= 0xF);

		LocalMaterialData[] cachedColumn = this.unloadedBlockColumnsCache.get(blockPos);

		if (cachedColumn != null)
		{
			return cachedColumn;
		}

		IChunk chunk = this.unloadedChunksCache.get(chunkCoord);
		if (chunk == null)
		{
			// Generate a chunk without populating it
			chunk = getUnloadedChunk(worldGenRegion.getWorldRandom(), chunkCoord);
			unloadedChunksCache.put(chunkCoord, chunk);
		}

		cachedColumn = new LocalMaterialData[256];

		LocalMaterialData[] blocksInColumn = new LocalMaterialData[256];
		BlockState blockInChunk;
		for (short y = 0; y < 256; y++)
		{
			blockInChunk = chunk.getBlockState(new BlockPos(blockX, y, blockZ));
			if (blockInChunk != null)
			{
				blocksInColumn[y] = ForgeMaterialData.ofBlockState(blockInChunk);
			} else {
				break;
			}
		}
		unloadedBlockColumnsCache.put(blockPos, cachedColumn);

		return blocksInColumn;
	}

	private IChunk getUnloadedChunk(Random random, ChunkCoordinate chunkCoordinate)
	{
		ChunkPrimer chunk = new ChunkPrimer(new ChunkPos(chunkCoordinate.getChunkX(), chunkCoordinate.getChunkZ()), null);
		ChunkBuffer buffer = new ForgeChunkBuffer(chunk);
		
		// This is where vanilla processes any noise affecting structures like villages, in order to spawn smoothing areas.
		// Doing this for unloaded chunks causes a hang on load since getChunk is called by StructureManager.
		// BO4's avoid villages, so this method should never be called to fetch unloaded chunks that contain villages, 
		// so we can skip noisegen affecting structures here.
		// *TODO: Do we need to avoid any noisegen affecting structures other than villages?

		ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
		ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);

		this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), random, buffer, buffer.getChunkCoordinate(), structures, junctions);
		return chunk;
	}

	LocalMaterialData getMaterialInUnloadedChunk(IWorldGenRegion worldGenRegion, int x, int y, int z)
	{
		LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(worldGenRegion, x, z);
		return blockColumn[y];
	}

	int getHighestBlockYInUnloadedChunk(IWorldGenRegion worldGenRegion, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
	{
		int height = -1;

		LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(worldGenRegion, x, z);
		ForgeMaterialData material;
		boolean isLiquid;
		boolean isSolid;

		for (int y = 255; y > -1; y--)
		{
			material = (ForgeMaterialData) blockColumn[y];
			isLiquid = material.isLiquid();
			isSolid = material.isSolid() || (!ignoreSnow && material.isMaterial(LocalMaterials.SNOW));
			if (!(isLiquid && ignoreLiquid))
			{
				if ((findSolid && isSolid) || (findLiquid && isLiquid))
				{
					return y;
				}
				if ((findSolid && isLiquid) || (findLiquid && isSolid))
				{
					return -1;
				}
			}
		}
		return height;
	}
	
	public boolean checkHasVanillaStructureWithoutLoading(ServerWorld serverWorld, ChunkCoordinate chunkCoordinate)
	{
		// Since we can't check for structure components/references, only structure starts,  
		// we'll keep a safe distance away from any vanilla structure start points.
		int radiusInChunks = 4;
        int chunkX = chunkCoordinate.getChunkX();
        int chunkZ = chunkCoordinate.getChunkZ();
        for (int cycle = 0; cycle <= radiusInChunks; ++cycle)
        {
            for (int xOffset = -cycle; xOffset <= cycle; ++xOffset)
            {
                for (int zOffset = -cycle; zOffset <= cycle; ++zOffset)
                {
                    int distance = (int)Math.floor(Math.sqrt(Math.pow (chunkX-chunkX + xOffset, 2) + Math.pow (chunkZ-chunkZ + zOffset, 2)));                    
                    if (distance == cycle)
                    {				
						ChunkPrimer chunk = new ChunkPrimer(new ChunkPos(chunkCoordinate.getChunkX() + xOffset, chunkCoordinate.getChunkZ() + zOffset), null);					
						ChunkPos chunkpos = chunk.getPos();
						
						// Borrowed from STRUCTURE_STARTS phase of chunkgen, only determines structure start point
						// based on biome and resource settings (distance etc). Does not plot any structure components.
						if (serverWorld.getServer().getWorldData().worldGenSettings().generateFeatures())
						{
							// TODO: Optimise this, make the BO4 plotter reuse biome/default structure information.
							Biome biome = this.biomeSource.getNoiseBiome((chunkpos.x << 2) + 2, 0, (chunkpos.z << 2) + 2);
							for(Supplier<StructureFeature<?, ?>> supplier : biome.getGenerationSettings().structures())
							{
								// *TODO: Do we need to avoid any structures other than villages?
								if(supplier.get().feature instanceof VillageStructure)
								{
									if(this.hasStructureStart(supplier.get(), serverWorld.registryAccess(), serverWorld.structureFeatureManager(), chunk, serverWorld.getStructureManager(), serverWorld.getSeed(), chunkpos, biome))
									{
										return true;
									}
								}
							}
						}
                    }
                }
            }
        }
        return false;
	}
	
	private boolean hasStructureStart(StructureFeature<?, ?> structureFeature, DynamicRegistries dynamicRegistries, StructureManager structureManager, IChunk chunk, TemplateManager templateManager, long seed, ChunkPos chunkPos, Biome biome)
	{
		StructureSeparationSettings structureseparationsettings = this.getSettings().getConfig(structureFeature.feature);
		if (structureseparationsettings != null)
		{
			StructureStart<?> structureStart1 = structureFeature.generate(dynamicRegistries, this, this.biomeSource, templateManager, seed, chunkPos, biome, 0, structureseparationsettings);
			if(structureStart1 != StructureStart.INVALID_START)
			{
				return true;
			}
		}
		return false;
	}

	double getBiomeBlocksNoiseValue(int blockX, int blockZ)
	{
		return this.internalGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
	}
}
