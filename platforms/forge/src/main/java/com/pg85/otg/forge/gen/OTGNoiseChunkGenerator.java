package com.pg85.otg.forge.gen;

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
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
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
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.NoiseSettings;
import net.minecraft.world.spawner.WorldEntitySpawner;
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
						(p_236090_0_) ->
						{
							// TODO: Use bytestream instead?
							return p_236090_0_.dimensionConfig.toYamlString();
						}
					),
					BiomeProvider.CODEC.fieldOf("biome_source").forGetter(
						(p_236096_0_) ->
						{
							return p_236096_0_.biomeProvider;
						}
					),
					Codec.LONG.fieldOf("seed").stable().forGetter(
						(p_236093_0_) ->
						{
							return p_236093_0_.worldSeed;
						}
					),
					DimensionSettings.field_236098_b_.fieldOf("settings").forGetter(
						(p_236090_0_) ->
						{
							return p_236090_0_.dimensionSettingsSupplier;
						}
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

	private OTGNoiseChunkGenerator(String dimensionConfigYaml, BiomeProvider biomeProvider, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		this(DimensionConfig.fromYamlString(dimensionConfigYaml), biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	public OTGNoiseChunkGenerator(DimensionConfig dimensionConfig, BiomeProvider biomeProvider, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		this(dimensionConfig, biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	private OTGNoiseChunkGenerator(DimensionConfig dimensionConfigSupplier, BiomeProvider biomeProvider1, BiomeProvider biomeProvider2, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		super(biomeProvider1, biomeProvider2, dimensionSettingsSupplier.get().getStructures(), seed);

		if (!(biomeProvider1 instanceof LayerSource))
		{
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}

		this.biomeStructures = Registry.STRUCTURE_FEATURE.stream().collect(Collectors.groupingBy((structure) -> {
			return structure.getDecorationStage().ordinal();
		}));

		this.dimensionConfig = dimensionConfigSupplier;
		this.worldSeed = seed;
		DimensionSettings dimensionsettings = dimensionSettingsSupplier.get();
		this.dimensionSettingsSupplier = dimensionSettingsSupplier;
		NoiseSettings noisesettings = dimensionsettings.getNoise();
		this.noiseHeight = noisesettings.func_236169_a_();

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

	@OnlyIn(Dist.CLIENT)
	@Override
	public ChunkGenerator func_230349_a_(long seed)
	{
		return new OTGNoiseChunkGenerator(this.dimensionConfig, this.biomeProvider.getBiomeProvider(seed), seed, this.dimensionSettingsSupplier);
	}

	private void init(String worldName)
	{
		if (!isInitialised)
		{
			isInitialised = true;
			this.structureCache = OTG.getEngine().createCustomStructureCache(worldName, Paths.get("./saves/" + worldName + "/"), 0, this.worldSeed, this.preset.getWorldConfig().getCustomStructureType() == CustomStructureType.BO4);
		}
	}

	// Base terrain gen

	// Generates the base terrain for a chunk.
	@Override
	public void func_230352_b_(IWorld world, StructureManager manager, IChunk chunk)
	{
		// If we've already generated and cached this	
		// chunk while it was unloaded, use cached data.
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);
		ChunkBuffer buffer = new ForgeChunkBuffer((ChunkPrimer) chunk);
		IChunk cachedChunk = unloadedChunksCache.get(chunkCoord);
		if (cachedChunk != null)
		{
			// TODO: Find some way to clone/swap chunk data efficiently :/
			for (int x = 0; x < ChunkCoordinate.CHUNK_SIZE; x++)
			{
				for (int z = 0; z < ChunkCoordinate.CHUNK_SIZE; z++)
				{
					int endY = cachedChunk.getHeightmap(Type.WORLD_SURFACE_WG).getHeight(x, z);
					for (int y = 0; y <= endY; y++)
					{
						BlockPos pos = new BlockPos(x, y, z);
						chunk.setBlockState(pos, cachedChunk.getBlockState(pos), false);
					}
				}
			}
			this.unloadedChunksCache.remove(chunkCoord);
		} else {
			this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), world.getRandom(), buffer, buffer.getChunkCoordinate());
		}
	}

	// Replaces surface and ground blocks in base terrain and places bedrock.
	@Override
	public void generateSurface(WorldGenRegion worldGenRegion, IChunk chunk)
	{
		// Done during this.internalGenerator.populateNoise
	}

	// Carves caves and ravines
	@Override
	public void func_230350_a_(long seed, BiomeManager biomeManager, IChunk chunk, GenerationStage.Carving stage)
	{
		if (stage == GenerationStage.Carving.AIR)
		{
			ChunkPrimer protoChunk = (ChunkPrimer) chunk;

			ChunkBuffer chunkBuffer = new ForgeChunkBuffer(protoChunk);
			BitSet carvingMask = protoChunk.getOrAddCarvingMask(stage);
			this.internalGenerator.carve(chunkBuffer, seed, protoChunk.getPos().x, protoChunk.getPos().z, carvingMask);
		}
	}

	// Population / decoration

	// Does population for a given pos/chunk
	@Override
	public void func_230351_a_(WorldGenRegion worldGenRegion, StructureManager structureManager)
	{
		int chunkX = worldGenRegion.getMainChunkX();
		int chunkZ = worldGenRegion.getMainChunkZ();
		int blockX = chunkX * 16;
		int blockZ = chunkZ * 16;
		BlockPos blockpos = new BlockPos(blockX, 0, blockZ);

		// Fetch the biomeConfig by registryKey
		RegistryKey<Biome> key = ((OTGBiomeProvider) this.biomeProvider).getBiomeRegistryKey((chunkX << 2) + 2, 2, (chunkZ << 2) + 2);
		BiomeConfig biomeConfig = OTG.getEngine().getPresetLoader().getBiomeConfig(key.getLocation().toString());

		SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
		long decorationSeed = sharedseedrandom.setDecorationSeed(worldGenRegion.getSeed(), blockX, blockZ);
		try
		{
			// Override normal population (Biome.func_242427_a()) with OTG's.
			biomePopulate(biomeConfig, structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
		} catch (Exception exception) {
			CrashReport crashreport = CrashReport.makeCrashReport(exception, "Biome decoration");
			crashreport.makeCategory("Generation").addDetail("CenterX", chunkX).addDetail("CenterZ", chunkZ).addDetail("Seed", decorationSeed);
			throw new ReportedException(crashreport);
		}
	}

	// Chunk population method taken from Biome (Biome.func_242427_a())
	private void biomePopulate(BiomeConfig biomeConfig, StructureManager structureManager, ChunkGenerator chunkGenerator, WorldGenRegion world, long seed, SharedSeedRandom random, BlockPos pos)
	{
		init(((IServerWorldInfo) world.getWorldInfo()).getWorldName());
		ChunkCoordinate chunkBeingPopulated = ChunkCoordinate.fromBlockCoords(pos.getX(), pos.getZ());
		this.chunkPopulator.populate(chunkBeingPopulated, new ForgeWorldGenRegion(this.preset.getName(), this.preset.getWorldConfig(), world, this), biomeConfig, this.structureCache);

		// TODO: clean up/optimise this
		// Structure generation
		for(int step = 0; step < GenerationStage.Decoration.values().length; ++step)
		{
			int index = 0;
			// Generate features if enabled
			if (structureManager.canGenerateFeatures())
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
						// Generate the structure if it exists in a biome this chunk.
						// We don't have to do any work here, we can just let StructureManager handle it all.
						structureManager.func_235011_a_(SectionPos.from(pos), structure).forEach(start ->
								start.func_230366_a_(world, structureManager, chunkGenerator, random, new MutableBoundingBox(chunkStartX, chunkStartZ, chunkStartX + 15, chunkStartZ + 15), new ChunkPos(chunkX, chunkZ)));
					} catch (Exception exception) {
						CrashReport crashreport = CrashReport.makeCrashReport(exception, "Feature placement");
						crashreport.makeCategory("Feature").addDetail("Id", Registry.STRUCTURE_FEATURE.getKey(structure)).addDetail("Description", () ->
								structure.toString());
						throw new ReportedException(crashreport);
					}

					++index;
				}
			}
		}

	}

	// Mob spawning on initial chunk spawn (animals).
	@Override
	public void func_230354_a_(WorldGenRegion worldGenRegion)
	{
		if (!this.dimensionSettingsSupplier.get().func_236120_h_())
		{
			int i = worldGenRegion.getMainChunkX();
			int j = worldGenRegion.getMainChunkZ();
			Biome biome = worldGenRegion.getBiome((new ChunkPos(i, j)).asBlockPos());
			SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
			sharedseedrandom.setDecorationSeed(worldGenRegion.getSeed(), i << 4, j << 4);
			WorldEntitySpawner.performWorldGenSpawning(worldGenRegion, biome, i, j, sharedseedrandom);
		}
	}

	// Mob spawning on chunk tick
	@Override
	public List<MobSpawnInfo.Spawners> func_230353_a_(Biome biome, StructureManager structureManager, EntityClassification entityClassification, BlockPos blockPos)
	{
		if (structureManager.getStructureStart(blockPos, true, Structure.SWAMP_HUT).isValid())
		{
			if (entityClassification == EntityClassification.MONSTER)
			{
				return Structure.SWAMP_HUT.getSpawnList();
			}

			if (entityClassification == EntityClassification.CREATURE)
			{
				return Structure.SWAMP_HUT.getCreatureSpawnList();
			}
		}

		if (entityClassification == EntityClassification.MONSTER)
		{
			if (structureManager.getStructureStart(blockPos, false, Structure.PILLAGER_OUTPOST).isValid())
			{
				return Structure.PILLAGER_OUTPOST.getSpawnList();
			}

			if (structureManager.getStructureStart(blockPos, false, Structure.MONUMENT).isValid())
			{
				return Structure.MONUMENT.getSpawnList();
			}

			if (structureManager.getStructureStart(blockPos, true, Structure.FORTRESS).isValid())
			{
				return Structure.FORTRESS.getSpawnList();
			}
		}

		return super.func_230353_a_(biome, structureManager, entityClassification, blockPos);
	}

	// Noise

	@Override
	public int getHeight(int x, int z, Type heightmapType)
	{
		return this.sampleHeightmap(x, z, null, heightmapType.getHeightLimitPredicate());
	}

	// Provides a sample of the full column for structure generation.
	@Override
	public IBlockReader func_230348_a_(int x, int z)
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
			return Blocks.AIR.getDefaultState();
		}
	}

	// Getters / misc

	@Override
	protected Codec<? extends ChunkGenerator> func_230347_a_()
	{
		return CODEC;
	}

	@Override
	public int getMaxBuildHeight()
	{
		return this.noiseHeight;
	}

	@Override
	public int getSeaLevel()
	{
		return this.dimensionSettingsSupplier.get().func_236119_g_();
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
				blocksInColumn[y] = ForgeMaterialData.ofMinecraftBlockState(blockInChunk);
			} else {
				break;
			}
		}
		unloadedBlockColumnsCache.put(blockPos, cachedColumn);

		return blocksInColumn;
	}

	private IChunk getUnloadedChunk(Random random, ChunkCoordinate chunkCoordinate)
	{
		IChunk chunk = new ChunkPrimer(new ChunkPos(chunkCoordinate.getChunkX(), chunkCoordinate.getChunkZ()), null);
		ChunkBuffer buffer = new ForgeChunkBuffer((ChunkPrimer) chunk);
		this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), random, buffer, buffer.getChunkCoordinate());
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

	double getBiomeBlocksNoiseValue(int blockX, int blockZ)
	{
		return this.internalGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
	}
}
