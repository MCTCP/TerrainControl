package com.pg85.otg.spigot.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.gen.OTGChunkGenerator;
import com.pg85.otg.gen.OTGChunkPopulator;
import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.biome.OTGBiomeProvider;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
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
import org.bukkit.block.Structure;

import net.minecraft.server.v1_16_R3.*;

import javax.annotation.Nullable;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OTGNoiseChunkGenerator extends ChunkGenerator
{
	// Create a codec to serialise/deserialise OTGNoiseChunkGenerator
	public static final Codec<OTGNoiseChunkGenerator> CODEC = RecordCodecBuilder.create(
			(p_236091_0_) ->
					p_236091_0_
							.group(
									Codec.STRING.fieldOf("otg_dimension_config").forGetter(
											(p_236090_0_) -> p_236090_0_.dimensionConfig.toYamlString() // TODO: Use bytestream instead?
									),
									// BiomeProvider -> WorldChunkManager
									WorldChunkManager.a.fieldOf("biome_source").forGetter(
											(p_236096_0_) -> p_236096_0_.b
									),
									Codec.LONG.fieldOf("seed").stable().forGetter(
											(p_236093_0_) -> p_236093_0_.worldSeed
									),
									// DimensionSettings -> GeneratorSettingsBase
									GeneratorSettingBase.b.fieldOf("settings").forGetter(
											(p_236090_0_) -> p_236090_0_.dimensionSettingsSupplier
									)
							).apply(
							p_236091_0_,
							p_236091_0_.stable(OTGNoiseChunkGenerator::new)
					)
	);

	private final Supplier<GeneratorSettingBase> dimensionSettingsSupplier;
	private final long worldSeed;
	private final int noiseHeight;

	private final OTGChunkGenerator internalGenerator;
	private final OTGChunkPopulator chunkPopulator;
	private final DimensionConfig dimensionConfig;
	private final Preset preset;
	// Unloaded chunk data caches for BO4's
	private final FifoMap<BlockPos2D, LocalMaterialData[]> unloadedBlockColumnsCache;
	// IChunk -> IChunkAccess
	private final FifoMap<ChunkCoordinate, IChunkAccess> unloadedChunksCache;
	// Structure -> StructureGenerator
	private final Map<Integer, List<StructureGenerator<?>>> biomeStructures;
	// TODO: Move this to WorldLoader when ready?
	private CustomStructureCache structureCache;
	// TODO: Move this to WorldLoader when ready?
	private boolean isInitialised = false;

	public GeneratorAccess world = null;

	public OTGNoiseChunkGenerator (WorldChunkManager biomeProvider, long seed, Supplier<GeneratorSettingBase> dimensionSettingsSupplier)
	{
		this(new DimensionConfig(OTG.getEngine().getPresetLoader().getDefaultPresetName()), biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	private OTGNoiseChunkGenerator (String dimensionConfigYaml, WorldChunkManager biomeProvider, long seed, Supplier<GeneratorSettingBase> dimensionSettingsSupplier)
	{
		this(DimensionConfig.fromYamlString(dimensionConfigYaml), biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	public OTGNoiseChunkGenerator (DimensionConfig dimensionConfig, WorldChunkManager biomeProvider, long seed, Supplier<GeneratorSettingBase> dimensionSettingsSupplier)
	{
		this(dimensionConfig, biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	private OTGNoiseChunkGenerator (DimensionConfig dimensionConfigSupplier, WorldChunkManager biomeProvider1, WorldChunkManager biomeProvider2, long seed, Supplier<GeneratorSettingBase> dimensionSettingsSupplier)
	{
		// getStructures() -> a()
		super(biomeProvider1, biomeProvider2, dimensionSettingsSupplier.get().a(), seed);

		if (!(biomeProvider1 instanceof LayerSource))
		{
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}

		// stream() -> g()
		this.biomeStructures = IRegistry.STRUCTURE_FEATURE.g().collect(Collectors.groupingBy((structure) ->
		{
			// getDecorationStage() -> f()
			return structure.f().ordinal();
		}));

		this.dimensionConfig = dimensionConfigSupplier;
		this.worldSeed = seed;
		GeneratorSettingBase dimensionsettings = dimensionSettingsSupplier.get();
		this.dimensionSettingsSupplier = dimensionSettingsSupplier;
		// getNoise() -> b()
		NoiseSettings noisesettings = dimensionsettings.b();
		// func_236169_a_() -> a()
		this.noiseHeight = noisesettings.a();

		// Unloaded chunk data caches for BO4's
		// TODO: Add a setting to the world config for the size of these caches.
		// Worlds with lots of BO4's and large smoothing areas may want to increase this.
		this.unloadedBlockColumnsCache = new FifoMap<>(1024);
		this.unloadedChunksCache = new FifoMap<>(128);
		//

		this.preset = OTG.getEngine().getPresetLoader().getPresetByName(this.dimensionConfig.PresetName);

		this.internalGenerator = new OTGChunkGenerator(preset, seed, (LayerSource) biomeProvider1);
		this.chunkPopulator = new OTGChunkPopulator();
	}

	public void saveStructureCache ()
	{
		if (this.chunkPopulator.getIsSaveRequired())
		{
			this.structureCache.saveToDisk(OTG.getEngine().getPluginConfig().getSpawnLogEnabled(), OTG.getEngine().getLogger(), this.chunkPopulator);
		}
	}

	private void init (String worldName)
	{
		if (!isInitialised)
		{
			isInitialised = true;
			this.structureCache = OTG.getEngine().createCustomStructureCache(worldName, Paths.get("./saves/" + worldName + "/"), 0, this.worldSeed, this.preset.getWorldConfig().getCustomStructureType() == SettingsEnums.CustomStructureType.BO4);
		}
	}

	// Base terrain gen

	// Generates the base terrain for a chunk. Spigot compatible.
	// IWorld -> GeneratorAccess
	public void buildNoiseSpigot (org.bukkit.generator.ChunkGenerator.ChunkData chunk, ChunkCoordinate chunkCoord)
	{
		ChunkBuffer buffer = new SpigotChunkBuffer(chunk, chunkCoord);
		this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), this.world.getRandom(), buffer, buffer.getChunkCoordinate(), new ObjectArrayList<>(), new ObjectArrayList<>());
	}

	// Generates the base terrain for a chunk.
	// IWorld -> GeneratorAccess
	@Override
	public void buildNoise (GeneratorAccess world, StructureManager manager, IChunkAccess chunk)
	{
		// If we've already generated and cached this
		// chunk while it was unloaded, use cached data.
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);
		// ChunkPrimer -> ProtoChunk
		ChunkBuffer buffer = new SpigotChunkBuffer((ProtoChunk) chunk);
		IChunkAccess cachedChunk = unloadedChunksCache.get(chunkCoord);
		if (cachedChunk != null)
		{
			// TODO: Find some way to clone/swap chunk data efficiently :/
			for (int x = 0; x < ChunkCoordinate.CHUNK_SIZE; x++)
			{
				for (int z = 0; z < ChunkCoordinate.CHUNK_SIZE; z++)
				{
					int endY = cachedChunk.a(HeightMap.Type.WORLD_SURFACE_WG).a(x, z);
					for (int y = 0; y <= endY; y++)
					{
						BlockPosition pos = new BlockPosition(x, y, z);
						chunk.setType(pos, cachedChunk.getType(pos), false);
					}
				}
			}
			this.unloadedChunksCache.remove(chunkCoord);
		}
		else
		{
			// Setup jigsaw data
			ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
			ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);
			ChunkCoordIntPair pos = chunk.getPos();
			int chunkX = pos.x;
			int chunkZ = pos.z;
			int startX = chunkX << 4;
			int startZ = chunkZ << 4;

			// Iterate through all of the jigsaw structures (villages, pillager outposts, nether fossils)
			for(StructureGenerator<?> structure : StructureGenerator.t) {
				// Get all structure starts in this chunk
				manager.a(SectionPosition.a(pos, 0), structure).forEach((start) -> {
					// Iterate through the pieces in the structure
					for(StructurePiece piece : start.d()) {
						// Check if it intersects with this chunk
						if (piece.a(pos, 12)) {
							StructureBoundingBox box = piece.g();

							if (piece instanceof WorldGenFeaturePillagerOutpostPoolPiece) {
								WorldGenFeaturePillagerOutpostPoolPiece villagePiece = (WorldGenFeaturePillagerOutpostPoolPiece) piece;
								// Add to the list if it's a rigid piece
								if (villagePiece.b().e() == WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID) {
									structures.add(new JigsawStructureData(box.a, box.b, box.c,box.d, villagePiece.d(), box.f, true, 0, 0, 0));
								}

								// Get all the junctions in this piece
								for(WorldGenFeatureDefinedStructureJigsawJunction junction : villagePiece.e()) {
									int sourceX = junction.a();
									int sourceZ = junction.c();

									// If the junction is in this chunk, then add to list
									if (sourceX > startX - 12 && sourceZ > startZ - 12 && sourceX < startX + 15 + 12 && sourceZ < startZ + 15 + 12) {
										junctions.add(new JigsawStructureData(0, 0, 0,0, 0, 0, false, junction.a(), junction.b(), junction.c()));
									}
								}
							} else {
								structures.add(new JigsawStructureData(box.a, box.b, box.c,box.d, 0, box.f,  false, 0, 0, 0));
							}
						}
					}

				});
			}

			this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), world.getRandom(), buffer, buffer.getChunkCoordinate(), structures, junctions);
		}
	}


	// Replaces surface and ground blocks in base terrain and places bedrock.
	// WorldGenRegion -> RegionLimitedWorldAccess
	@Override
	public void buildBase (RegionLimitedWorldAccess worldGenRegion, IChunkAccess chunk)
	{
		// Done during this.internalGenerator.populateNoise
	}

	// Carves caves and ravines
	// GenerationStage -> WorldGenStage, Carver -> Features
	@Override
	public void doCarving (long seed, BiomeManager biomeManager, IChunkAccess chunk, WorldGenStage.Features stage)
	{
		// Call here to get around the weird restrictions in CustomChunkGenerator
		if (stage == WorldGenStage.Features.AIR)
		{
			//buildNoise(world, null, chunk);
			ProtoChunk protoChunk = (ProtoChunk) chunk;
			ChunkBuffer chunkBuffer = new SpigotChunkBuffer(protoChunk);
			BitSet carvingMask = protoChunk.b(stage);
			this.internalGenerator.carve(chunkBuffer, seed, protoChunk.getPos().x, protoChunk.getPos().z, carvingMask);
		}
	}

	// Population / decoration

	// Does population for a given pos/chunk
	@Override
	public void addDecorations (RegionLimitedWorldAccess worldGenRegion, StructureManager structureManager)
	{
		// getMainChunkX -> a()
		// getMainChunkZ -> b()
		int chunkX = worldGenRegion.a();
		int chunkZ = worldGenRegion.b();
		int blockX = chunkX * 16;
		int blockZ = chunkZ * 16;
		BlockPosition blockpos = new BlockPosition(blockX, 0, blockZ);

		// Fetch the biomeConfig by registryKey
		// this.biomeProvider -> this.b
		ResourceKey<BiomeBase> key = ((OTGBiomeProvider) this.b).getBiomeRegistryKey((chunkX << 2) + 2, 2, (chunkZ << 2) + 2);
		BiomeConfig biomeConfig = OTG.getEngine().getPresetLoader().getBiomeConfig(key.a().toString());

		// SharedSeedRandom -> SeededRandom
		SeededRandom sharedseedrandom = new SeededRandom();
		// setDecorationSeed() -> a()
		long decorationSeed = sharedseedrandom.a(worldGenRegion.getSeed(), blockX, blockZ);
		try
		{
			// Override normal population (Biome.func_242427_a()) with OTG's.
			biomePopulate(biomeConfig, structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
		}
		catch (Exception exception)
		{
			// makeCrashReport() -> a()
			CrashReport crashreport = CrashReport.a(exception, "Biome decoration");
			// crashReport.makeCategory() -> crashReport.a()
			// crashReport.addDetail() -> crashReport.a()
			crashreport.a("Generation").a("CenterX", chunkX).a("CenterZ", chunkZ).a("Seed", decorationSeed);
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public void createBiomes (IRegistry<BiomeBase> iregistry, IChunkAccess ichunkaccess)
	{
		ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
		((ProtoChunk) ichunkaccess).a(new BiomeStorage(iregistry, chunkcoordintpair, this.c));
	}

	@Override
	public void createStructures (IRegistryCustom iregistrycustom, StructureManager structuremanager, IChunkAccess ichunkaccess, DefinedStructureManager definedstructuremanager, long i)
	{
		super.createStructures(iregistrycustom, structuremanager, ichunkaccess, definedstructuremanager, i);
	}

	// Chunk population method taken from Biome (Biome.func_242427_a())
	private void biomePopulate (BiomeConfig biomeConfig, StructureManager structureManager, ChunkGenerator chunkGenerator, RegionLimitedWorldAccess world, long seed, SeededRandom random, BlockPosition pos)
	{
		init(((IWorldDataServer) world.getWorldData()).getName());
		ChunkCoordinate chunkBeingPopulated = ChunkCoordinate.fromBlockCoords(pos.getX(), pos.getZ());
		this.chunkPopulator.populate(chunkBeingPopulated, new SpigotWorldGenRegion(this.preset.getName(), this.preset.getWorldConfig(), world, this), biomeConfig, this.structureCache);

		// TODO: clean up/optimise this
		// Structure generation
		for (int step = 0; step < WorldGenStage.Decoration.values().length; ++step)
		{
			int index = 0;
			// Generate features if enabled
			if (structureManager.a())
			{
				// Go through all the structures set to generate at this step
				for (StructureGenerator<?> structure : this.biomeStructures.getOrDefault(step, Collections.emptyList()))
				{
					// Reset the random
					// setFeatureSeed() -> b()
					random.b(seed, index, step);
					int chunkX = pos.getX() >> 4;
					int chunkZ = pos.getZ() >> 4;
					int chunkStartX = chunkX << 4;
					int chunkStartZ = chunkZ << 4;

					try
					{
						// Generate the structure if it exists in a biome in this chunk.
						// We don't have to do any work here, we can just let StructureManager handle it all.
						structureManager.a(SectionPosition.a(pos), structure)
								.forEach(start ->
										start.a(
												world,
												structureManager,
												chunkGenerator,
												random,
												new StructureBoundingBox(
														chunkStartX,
														chunkStartZ,
														chunkStartX + 15,
														chunkStartZ + 15
												),
												new ChunkCoordIntPair(chunkX, chunkZ)
										)
								)
						;
					}
					catch (Exception exception)
					{
						CrashReport crashreport = CrashReport.a(exception, "Feature placement");
						crashreport.a("Feature")
								.a("Id", IRegistry.STRUCTURE_FEATURE.getKey(structure))
								.a("Description", structure::toString)
						;
						throw new ReportedException(crashreport);
					}

					++index;
				}
			}
		}

	}

	// Mob spawning on initial chunk spawn (animals).
	@Override
	public void addMobs (RegionLimitedWorldAccess worldGenRegion)
	{
		//TODO: Make this respect the doMobSpawning game rule

		// getMainChunkX -> a()
		// getMainChunkZ -> b()
		int i = worldGenRegion.a();
		int j = worldGenRegion.a();
		BiomeBase biome = worldGenRegion.getBiome((new ChunkCoordIntPair(i, j)).l());
		SeededRandom sharedseedrandom = new SeededRandom();
		// setDecorationSeed() -> a()
		sharedseedrandom.a(worldGenRegion.getSeed(), i << 4, j << 4);
		// performWorldGenSpawning() -> a()
		SpawnerCreature.a(worldGenRegion, biome, i, j, sharedseedrandom);

	}

	// Mob spawning on chunk tick
	// EntityClassification -> EnumCreatureType
	@Override
	public List<BiomeSettingsMobs.c> getMobsFor (BiomeBase biome, StructureManager structureManager, EnumCreatureType entityClassification, BlockPosition blockPos)
	{
		// getStructureStart() -> a()
		// isValid() -> e()
		if (structureManager.a(blockPos, true, StructureGenerator.SWAMP_HUT).e())
		{
			if (entityClassification == EnumCreatureType.MONSTER)
			{
				// getSpawnList() -> c()
				return StructureGenerator.SWAMP_HUT.c();
			}

			if (entityClassification == EnumCreatureType.CREATURE)
			{
				// getCreatureSpawnList() -> j()
				return StructureGenerator.SWAMP_HUT.j();
			}
		}

		if (entityClassification == EnumCreatureType.MONSTER)
		{
			if (structureManager.a(blockPos, false, StructureGenerator.PILLAGER_OUTPOST).e())
			{
				return StructureGenerator.PILLAGER_OUTPOST.c();
			}

			if (structureManager.a(blockPos, false, StructureGenerator.MONUMENT).e())
			{
				return StructureGenerator.MONUMENT.c();
			}

			if (structureManager.a(blockPos, true, StructureGenerator.FORTRESS).e())
			{
				return StructureGenerator.FORTRESS.c();
			}
		}

		return super.getMobsFor(biome, structureManager, entityClassification, blockPos);
	}

	// Noise

	@Override
	public int getBaseHeight (int x, int z, HeightMap.Type heightmapType)
	{
		return this.sampleHeightmap(x, z, null, heightmapType.e());
	}

	// Provides a sample of the full column for structure generation.
	@Override
	public IBlockAccess a (int x, int z)
	{
		IBlockData[] ablockstate = new IBlockData[256];
		this.sampleHeightmap(x, x, ablockstate, null);
		return new BlockColumn(ablockstate);
	}

	// Samples the noise at a column and provides a view of the blockstates, or fills a heightmap.
	private int sampleHeightmap (int x, int z, @Nullable IBlockData[] blockStates, @Nullable Predicate<IBlockData> predicate)
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
				// MathHelper.lerp3() -> MathHelper.a()
				double density = MathHelper.a(yLerp, xLerp, zLerp, x0z0y0, x0z0y1, x1z0y0, x1z0y1, x0z1y0, x0z1y1, x1z1y0, x1z1y1);

				// Get the real y position (translate noise chunk and noise piece)
				int y = (noiseY * 8) + pieceY;

				IBlockData state = this.getBlockState(density, y, this.internalGenerator.getBiomeAtWorldCoord(x, z));
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

	protected IBlockData getBlockState (double density, int y, IBiomeConfig config)
	{
		if (density > 0.0D)
		{
			return ((SpigotMaterialData) config.getStoneBlockReplaced(y)).internalBlock();
		}
		else if (y < this.getSeaLevel())
		{
			return ((SpigotMaterialData) config.getWaterBlockReplaced(y)).internalBlock();
		}
		else
		{
			return Blocks.AIR.getBlockData();
		}
	}

	// Getters / misc

	@Override
	protected Codec<? extends ChunkGenerator> a ()
	{
		return CODEC;
	}

	@Override
	public int getGenerationDepth ()
	{
		return this.noiseHeight;
	}

	@Override
	public int getSeaLevel ()
	{
		return this.dimensionSettingsSupplier.get().g();
	}

	// BO4's / Smoothing Areas

	// BO4's and smoothing areas may do material and height checks in unloaded chunks, OTG generates
	// base terrain for the chunks in memory and caches the result in a limited size-cache. Cached
	// data is used if/when the chunk is "properly" generated.

	private LocalMaterialData[] getBlockColumnInUnloadedChunk (IWorldGenRegion worldGenRegion, int x, int z)
	{
		BlockPos2D blockPos = new BlockPos2D(x, z);
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);

		// Get internal coordinates for block in chunk
		byte blockX = (byte) (x & 0xF);
		byte blockZ = (byte) (z & 0xF);

		LocalMaterialData[] cachedColumn = this.unloadedBlockColumnsCache.get(blockPos);

		if (cachedColumn != null)
		{
			return cachedColumn;
		}

		IChunkAccess chunk = this.unloadedChunksCache.get(chunkCoord);
		if (chunk == null)
		{
			// Generate a chunk without populating it
			chunk = getUnloadedChunk(worldGenRegion.getWorldRandom(), chunkCoord);
			unloadedChunksCache.put(chunkCoord, chunk);
		}

		cachedColumn = new LocalMaterialData[256];

		LocalMaterialData[] blocksInColumn = new LocalMaterialData[256];
		IBlockData blockInChunk;
		for (short y = 0; y < 256; y++)
		{
			blockInChunk = chunk.getType(new BlockPosition(blockX, y, blockZ));
			if (blockInChunk != null)
			{
				blocksInColumn[y] = SpigotMaterialData.ofBlockData(blockInChunk);
			}
			else
			{
				break;
			}
		}
		unloadedBlockColumnsCache.put(blockPos, cachedColumn);

		return blocksInColumn;
	}

	private IChunkAccess getUnloadedChunk (Random random, ChunkCoordinate chunkCoordinate)
	{
		ProtoChunk chunk = new ProtoChunk(new ChunkCoordIntPair(chunkCoordinate.getChunkX(), chunkCoordinate.getChunkZ()), null);
		ChunkBuffer buffer = new SpigotChunkBuffer(chunk);
		this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), random, buffer, buffer.getChunkCoordinate(), new ObjectArrayList<>(), new ObjectArrayList<>());
		return chunk;
	}

	LocalMaterialData getMaterialInUnloadedChunk (IWorldGenRegion worldGenRegion, int x, int y, int z)
	{
		LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(worldGenRegion, x, z);
		return blockColumn[y];
	}

	int getHighestBlockYInUnloadedChunk (IWorldGenRegion worldGenRegion, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
	{
		int height = -1;

		LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(worldGenRegion, x, z);
		LocalMaterialData material;
		boolean isLiquid;
		boolean isSolid;

		for (int y = 255; y > -1; y--)
		{
			material = blockColumn[y];
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

	double getBiomeBlocksNoiseValue (int blockX, int blockZ)
	{
		return this.internalGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
	}
}
