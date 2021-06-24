package com.pg85.otg.forge.gen;

import java.nio.file.Path;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

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
import com.pg85.otg.gen.OTGChunkDecorator;
import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.materials.LocalMaterialData;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
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
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.INoiseGenerator;
import net.minecraft.world.gen.OctavesNoiseGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.jigsaw.JigsawJunction;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.settings.NoiseSettings;
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
	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;
	private final INoiseGenerator surfaceNoise;
	protected final SharedSeedRandom random;

	private final ShadowChunkGenerator shadowChunkGenerator;
	private final OTGChunkGenerator internalGenerator;
	private final OTGChunkDecorator chunkDecorator;
	private final DimensionConfig dimensionConfig;
	private final Preset preset;
	// TODO: Move this to WorldLoader when ready?
	private CustomStructureCache structureCache;
	// TODO: Move this to WorldLoader when ready?
	private boolean isInitialised = false;
	
	public OTGNoiseChunkGenerator(BiomeProvider biomeProvider, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		this(new DimensionConfig(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName()), biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
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
	@SuppressWarnings("deprecation")
	private OTGNoiseChunkGenerator(DimensionConfig dimensionConfigSupplier, BiomeProvider biomeProvider1, BiomeProvider biomeProvider2, long seed, Supplier<DimensionSettings> dimensionSettingsSupplier)
	{
		super(biomeProvider1, biomeProvider2, dimensionSettingsSupplier.get().structureSettings(), seed);

		if (!(biomeProvider1 instanceof LayerSource))
		{
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}	
		
		this.dimensionConfig = dimensionConfigSupplier;
		this.worldSeed = seed;
		this.dimensionSettingsSupplier = dimensionSettingsSupplier;		
		DimensionSettings dimensionsettings = dimensionSettingsSupplier.get();	
		NoiseSettings noisesettings = dimensionsettings.noiseSettings();
		this.defaultBlock = dimensionsettings.getDefaultBlock();
		this.defaultFluid = dimensionsettings.getDefaultFluid();
		this.random = new SharedSeedRandom(seed);
		this.surfaceNoise = (INoiseGenerator)(noisesettings.useSimplexSurfaceNoise() ? new PerlinNoiseGenerator(this.random, IntStream.rangeClosed(-3, 0)) : new OctavesNoiseGenerator(this.random, IntStream.rangeClosed(-3, 0)));
		this.noiseHeight = noisesettings.height();

		this.preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(this.dimensionConfig.PresetFolderName);
		this.shadowChunkGenerator = new ShadowChunkGenerator(OTG.getEngine().getPluginConfig().getMaxWorkerThreads());
		this.internalGenerator = new OTGChunkGenerator(preset, seed, (LayerSource) biomeProvider1);
		this.chunkDecorator = new OTGChunkDecorator();
	}

	private void init(Path worldSaveFolder)
	{
		if (!isInitialised)
		{
			isInitialised = true;
			this.structureCache = OTG.getEngine().createCustomStructureCache(this.preset.getFolderName(), worldSaveFolder, 0, this.worldSeed, this.preset.getWorldConfig().getCustomStructureType() == CustomStructureType.BO4);
		}
	}
	
	public void saveStructureCache()
	{
		if (this.chunkDecorator.getIsSaveRequired())
		{
			this.structureCache.saveToDisk(OTG.getEngine().getPluginConfig().getSpawnLogEnabled(), OTG.getEngine().getLogger(), this.chunkDecorator);
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

	// Base terrain gen

	// Generates the base terrain for a chunk.
	@Override
	public void fillFromNoise(IWorld world, StructureManager manager, IChunk chunk)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);

		// Fetch any chunks that are cached in the WorldGenRegion, so we can
		// pre-emptively generate and cache base terrain for them asynchronously.
		this.shadowChunkGenerator.queueChunksForWorkerThreads((WorldGenRegion)world, manager, chunk, this, this.biomeSource, this.internalGenerator, this.getSettings(), this.preset.getWorldConfig().getWorldHeightCap());
		
		// If we've already (shadow-)generated and cached this	
		// chunk while it was unloaded, use cached data.
		ChunkBuffer buffer = new ForgeChunkBuffer((ChunkPrimer) chunk);
		IChunk cachedChunk = this.shadowChunkGenerator.getChunkWithWait(chunkCoord);
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
			for(Structure<?> structure : Structure.NOISE_AFFECTING_FEATURES)
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
							MutableBoundingBox box = piece.getBoundingBox();
							if (piece instanceof AbstractVillagePiece)
							{
								AbstractVillagePiece villagePiece = (AbstractVillagePiece) piece;
								// Add to the list if it's a rigid piece
								if (villagePiece.getElement().getProjection() == JigsawPattern.PlacementBehaviour.RIGID)
								{
									structures.add(new JigsawStructureData(box.x0, box.y0, box.z0, box.x1, villagePiece.getGroundLevelDelta(), box.z1, true, 0, 0, 0));
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
								structures.add(new JigsawStructureData(box.x0, box.y0, box.z0,box.x1, 0, box.z1, false, 0, 0, 0));
							}
						}
					}
				});
			}
			this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), world.getRandom(), buffer, buffer.getChunkCoordinate(), structures, junctions);			
			this.shadowChunkGenerator.setChunkGenerated(chunkCoord);
		}
	}

	// Replaces surface and ground blocks in base terrain and places bedrock.
	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, IChunk chunk)
	{
		// OTG handles surface/ground blocks during base terrain gen. For non-OTG biomes used
		// with TemplateForBiome, we want to use registered surfacebuilders though.
		// TODO: Disable any surface/ground block related features for Template BiomeConfigs. 

		// Fetch the biomeConfig by registryKey
		int chunkX = worldGenRegion.getCenterX();
		int chunkZ = worldGenRegion.getCenterZ();
		RegistryKey<Biome> key = ((OTGBiomeProvider) this.biomeSource).getBiomeRegistryKey((chunkX << 2) + 2, 2, (chunkZ << 2) + 2);
		BiomeConfig biomeConfig = OTG.getEngine().getPresetLoader().getBiomeConfig(key.location().toString());
		
		// TODO: Improve this check, make sure a non-otg biome is actually being used with this biomeconfig.
		if(biomeConfig.getTemplateForBiome() != null && biomeConfig.getTemplateForBiome().trim().length() > 0)
		{
			ChunkPos chunkpos = chunk.getPos();
			int i = chunkpos.x;
			int j = chunkpos.z;
			SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
			sharedseedrandom.setBaseChunkSeed(i, j);
			ChunkPos chunkpos1 = chunk.getPos();
			int k = chunkpos1.getMinBlockX();
			int l = chunkpos1.getMinBlockZ();
			BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();	
			for(int i1 = 0; i1 < 16; ++i1)
			{
				for(int j1 = 0; j1 < 16; ++j1)
				{
					int k1 = k + i1;
					int l1 = l + j1;
					int i2 = chunk.getHeight(Heightmap.Type.WORLD_SURFACE_WG, i1, j1) + 1;
					double d1 = this.surfaceNoise.getSurfaceNoiseValue((double)k1 * 0.0625D, (double)l1 * 0.0625D, 0.0625D, (double)i1 * 0.0625D) * 15.0D;
					worldGenRegion.getBiome(blockpos$mutable.set(k + i1, i2, l + j1)).buildSurfaceAt(sharedseedrandom, chunk, k1, l1, i2, d1, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), worldGenRegion.getSeed());
				}
			}
			// Skip bedrock, OTG always handles that.
		}
	}

	// Carves caves and ravines
	// TODO: Allow non-OTG carvers?
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

	// Decoration

	// Does decoration for a given pos/chunk
	@Override
	@SuppressWarnings("deprecation")	
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
		// World save folder name may not be identical to level name, fetch it.
		Path worldSaveFolder = worldGenRegion.getLevel().getServer().getWorldPath(FolderName.PLAYER_DATA_DIR).getParent();
		init(worldSaveFolder);
		ChunkCoordinate chunkBeingDecorated = ChunkCoordinate.fromBlockCoords(blockpos.getX(), blockpos.getZ());		
		try
		{
			// Do OTG resource decoration, then MC decoration for any non-OTG resources registered to this biome, then snow.
			ForgeWorldGenRegion forgeWorldGenRegion = new ForgeWorldGenRegion(this.preset.getFolderName(), this.preset.getWorldConfig(), worldGenRegion, this);
			this.chunkDecorator.decorate(chunkBeingDecorated, forgeWorldGenRegion, biomeConfig, this.structureCache);
			biome.generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
			this.chunkDecorator.doSnowAndIce(forgeWorldGenRegion, chunkBeingDecorated);
		} catch (Exception exception) {
			CrashReport crashreport = CrashReport.forThrowable(exception, "Biome decoration");
			crashreport.addCategory("Generation").setDetail("CenterX", chunkX).setDetail("CenterZ", chunkZ).setDetail("Seed", decorationSeed);
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

	double getBiomeBlocksNoiseValue(int blockX, int blockZ)
	{
		return this.internalGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
	}

	// Shadowgen
	
	public void stopWorkerThreads()
	{
		this.shadowChunkGenerator.stopWorkerThreads();
	}

	public Boolean checkHasVanillaStructureWithoutLoading(ServerWorld world, ChunkCoordinate chunkCoord)
	{
		return this.shadowChunkGenerator.checkHasVanillaStructureWithoutLoading(world, this, this.biomeSource, this.getSettings(), chunkCoord);
	}

	public int getHighestBlockYInUnloadedChunk(Random worldRandom, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
	{
		return this.shadowChunkGenerator.getHighestBlockYInUnloadedChunk(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), worldRandom, x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
	}

	public LocalMaterialData getMaterialInUnloadedChunk(Random worldRandom, int x, int y, int z)
	{
		return this.shadowChunkGenerator.getMaterialInUnloadedChunk(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), worldRandom, x, y, z);
	}

	public ForgeChunkBuffer getChunkWithoutLoadingOrCaching(Random random, ChunkCoordinate chunkCoord)
	{
		return this.shadowChunkGenerator.getChunkWithoutLoadingOrCaching(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), random, chunkCoord);
	}
}
