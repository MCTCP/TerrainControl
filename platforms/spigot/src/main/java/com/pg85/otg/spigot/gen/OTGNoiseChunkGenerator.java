package com.pg85.otg.spigot.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.gen.OTGChunkGenerator;
import com.pg85.otg.gen.OTGChunkDecorator;
import com.pg85.otg.gen.biome.BiomeInterpolator;
import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.biome.OTGBiomeProvider;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.materials.LocalMaterialData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.BiomeManager;
import net.minecraft.server.v1_16_R3.BiomeSettingsMobs;
import net.minecraft.server.v1_16_R3.BiomeStorage;
import net.minecraft.server.v1_16_R3.BlockColumn;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ChunkGenerator;
import net.minecraft.server.v1_16_R3.CrashReport;
import net.minecraft.server.v1_16_R3.EnumCreatureType;
import net.minecraft.server.v1_16_R3.GeneratorAccess;
import net.minecraft.server.v1_16_R3.GeneratorSettingBase;
import net.minecraft.server.v1_16_R3.HeightMap;
import net.minecraft.server.v1_16_R3.IBlockAccess;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IChunkAccess;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.NoiseSettings;
import net.minecraft.server.v1_16_R3.ProtoChunk;
import net.minecraft.server.v1_16_R3.RegionLimitedWorldAccess;
import net.minecraft.server.v1_16_R3.ReportedException;
import net.minecraft.server.v1_16_R3.ResourceKey;
import net.minecraft.server.v1_16_R3.SectionPosition;
import net.minecraft.server.v1_16_R3.SeededRandom;
import net.minecraft.server.v1_16_R3.SpawnerCreature;
import net.minecraft.server.v1_16_R3.StructureBoundingBox;
import net.minecraft.server.v1_16_R3.StructureGenerator;
import net.minecraft.server.v1_16_R3.StructureManager;
import net.minecraft.server.v1_16_R3.StructurePiece;
import net.minecraft.server.v1_16_R3.WorldChunkManager;
import net.minecraft.server.v1_16_R3.WorldGenFeatureDefinedStructureJigsawJunction;
import net.minecraft.server.v1_16_R3.WorldGenFeatureDefinedStructurePoolTemplate;
import net.minecraft.server.v1_16_R3.WorldGenFeaturePillagerOutpostPoolPiece;
import net.minecraft.server.v1_16_R3.WorldGenStage;
import net.minecraft.server.v1_16_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import javax.annotation.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OTGNoiseChunkGenerator extends ChunkGenerator
{
	// Create a codec to serialise/deserialise OTGNoiseChunkGenerator
	public static final Codec<OTGNoiseChunkGenerator> CODEC = RecordCodecBuilder.create(
		(p_236091_0_) -> p_236091_0_
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

	private final ShadowChunkGenerator shadowChunkGenerator;
	private final OTGChunkGenerator internalGenerator;
	private final OTGChunkDecorator chunkDecorator;
	private final DimensionConfig dimensionConfig;
	private final Preset preset;
	// TODO: Move this to WorldLoader when ready?
	private CustomStructureCache structureCache;

	// Used to specify which chunk to regen biomes and structures for
	// Necessary because Spigot calls those methods before we have the chance to inject
	private ChunkCoordinate fixBiomesForChunk = null;

	public OTGNoiseChunkGenerator (WorldChunkManager biomeProvider, long seed, Supplier<GeneratorSettingBase> dimensionSettingsSupplier)
	{
		this(new DimensionConfig(OTG.getEngine().getPresetLoader().getDefaultPresetFolderName()), biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
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

		this.dimensionConfig = dimensionConfigSupplier;
		this.worldSeed = seed;
		GeneratorSettingBase dimensionsettings = dimensionSettingsSupplier.get();
		this.dimensionSettingsSupplier = dimensionSettingsSupplier;
		// getNoise() -> b()
		NoiseSettings noisesettings = dimensionsettings.b();
		// func_236169_a_() -> a()
		this.noiseHeight = noisesettings.a();

		this.preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(this.dimensionConfig.PresetFolderName);
		this.shadowChunkGenerator = new ShadowChunkGenerator();
		this.internalGenerator = new OTGChunkGenerator(preset, seed, (LayerSource) biomeProvider1);
		this.chunkDecorator = new OTGChunkDecorator();
	}
	
	public void saveStructureCache ()
	{
		if (this.chunkDecorator.getIsSaveRequired() && this.structureCache != null)
		{
			this.structureCache.saveToDisk(OTG.getEngine().getPluginConfig().getSpawnLogEnabled(), OTG.getEngine().getLogger(), this.chunkDecorator);
		}
	}

	// Used by OTGSpigotChunkGen to query biome in a given location
	// Need this because the normal Bukkit way of checking for biomes use the Bukkit Biome enum
	public String getBiomeRegistryName(int blockX, int blockY, int blockZ)
	{
		return BiomeInterpolator.getBiomeRegistryName(this.worldSeed, blockX, blockY, blockZ, (OTGBiomeProvider) this.b);
	}

	// Base terrain gen

	// Generates the base terrain for a chunk. Spigot compatible.
	// IWorld -> GeneratorAccess
	public void buildNoiseSpigot (WorldServer world, org.bukkit.generator.ChunkGenerator.ChunkData chunk, ChunkCoordinate chunkCoord, Random random)
	{
		ChunkBuffer buffer = new SpigotChunkBuffer(chunk, chunkCoord);
		IChunkAccess cachedChunk = this.shadowChunkGenerator.getChunkFromCache(chunkCoord);
		if (cachedChunk != null)
		{
			this.shadowChunkGenerator.fillWorldGenChunkFromShadowChunk(chunkCoord, chunk, cachedChunk);
		} else {
			// Setup jigsaw data
			ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
			ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);
			ChunkCoordIntPair pos = new ChunkCoordIntPair(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
			int chunkX = pos.x;
			int chunkZ = pos.z;
			int startX = chunkX << 4;
			int startZ = chunkZ << 4;

			StructureManager manager = world.getStructureManager();
			// Iterate through all of the jigsaw structures (villages, pillager outposts, nether fossils)
			for (StructureGenerator<?> structure : StructureGenerator.t) {
				// Get all structure starts in this chunk
				manager.a(SectionPosition.a(pos, 0), structure).forEach((start) -> {
					// Iterate through the pieces in the structure
					for (StructurePiece piece : start.d()) {
						// Check if it intersects with this chunk
						if (piece.a(pos, 12)) {
							StructureBoundingBox box = piece.g();

							if (piece instanceof WorldGenFeaturePillagerOutpostPoolPiece) {
								WorldGenFeaturePillagerOutpostPoolPiece villagePiece = (WorldGenFeaturePillagerOutpostPoolPiece) piece;
								// Add to the list if it's a rigid piece
								if (villagePiece.b().e() == WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID) {
									structures.add(new JigsawStructureData(box.a, box.b, box.c, box.d, villagePiece.d(), box.f, true, 0, 0, 0));
								}

								// Get all the junctions in this piece
								for (WorldGenFeatureDefinedStructureJigsawJunction junction : villagePiece.e()) {
									int sourceX = junction.a();
									int sourceZ = junction.c();

									// If the junction is in this chunk, then add to list
									if (sourceX > startX - 12 && sourceZ > startZ - 12 && sourceX < startX + 15 + 12 && sourceZ < startZ + 15 + 12) {
										junctions.add(new JigsawStructureData(0, 0, 0, 0, 0, 0, false, junction.a(), junction.b(), junction.c()));
									}
								}
							} else {
								structures.add(new JigsawStructureData(box.a, box.b, box.c, box.d, 0, box.f, false, 0, 0, 0));
							}
						}
					}

				});
			}

			this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), random, buffer, buffer.getChunkCoordinate(), structures, junctions);
			this.shadowChunkGenerator.setChunkGenerated(chunkCoord);			
		}
	}

	// Generates the base terrain for a chunk.
	// IWorld -> GeneratorAccess
	@Override
	public void buildNoise (GeneratorAccess world, StructureManager manager, IChunkAccess chunk)
	{
		// If we've already generated and cached this
		// chunk while it was unloaded, use cached data.
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);

		// When generating the spawn area, Spigot will get the structure and biome info for the first chunk before we can inject
		// Therefore, we need to re-do these calls now, for that one chunk
		if (fixBiomesForChunk != null && fixBiomesForChunk.equals(chunkCoord))
		{
			// Should only run when first creating the world, on a single chunk
			this.createStructures(world.getMinecraftWorld().r(), world.getMinecraftWorld().getStructureManager(), chunk,
				world.getMinecraftWorld().n(), world.getMinecraftWorld().getSeed());
			this.createBiomes(((CraftServer) Bukkit.getServer()).getServer().customRegistry.b(IRegistry.ay), chunk);
			fixBiomesForChunk = null;
		}
		// ChunkPrimer -> ProtoChunk
		ChunkBuffer buffer = new SpigotChunkBuffer((ProtoChunk)chunk);
		IChunkAccess cachedChunk = this.shadowChunkGenerator.getChunkFromCache(chunkCoord);
		if (cachedChunk != null)
		{
			this.shadowChunkGenerator.fillWorldGenChunkFromShadowChunk(chunkCoord, chunk, cachedChunk);
		} else {
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
			this.shadowChunkGenerator.setChunkGenerated(chunkCoord);
		}
	}


	// Replaces surface and ground blocks in base terrain and places bedrock.
	// WorldGenRegion -> RegionLimitedWorldAccess
	@Override
	public void buildBase (RegionLimitedWorldAccess worldGenRegion, IChunkAccess chunk)
	{
		// Done during this.internalGenerator.populateNoise
		// TODO: Not doing this ignores any SurfaceBuilderss registered to this biome. We may have to enable this for non-otg biomes / non-otg surfacebuilders?
	}

	// Carves caves and ravines
	// GenerationStage -> WorldGenStage, Carver -> Features
	@Override
	public void doCarving (long seed, BiomeManager biomeManager, IChunkAccess chunk, WorldGenStage.Features stage)
	{
		if (stage == WorldGenStage.Features.AIR)
		{
			ProtoChunk protoChunk = (ProtoChunk) chunk;
			ChunkBuffer chunkBuffer = new SpigotChunkBuffer(protoChunk);
			BitSet carvingMask = protoChunk.b(stage);
			this.internalGenerator.carve(chunkBuffer, seed, protoChunk.getPos().x, protoChunk.getPos().z, carvingMask);
		}
		super.doCarving(seed, biomeManager, chunk, stage);
	}

	// Population / decoration

	// Does decoration for a given pos/chunk
	@Override
	public void addDecorations (RegionLimitedWorldAccess worldGenRegion, StructureManager structureManager)
	{
		// getMainChunkX -> a()
		// getMainChunkZ -> b()
		int chunkX = worldGenRegion.a();
		int chunkZ = worldGenRegion.b();
		int blockX = chunkX * Constants.CHUNK_SIZE;
		int blockZ = chunkZ * Constants.CHUNK_SIZE;
		BlockPosition blockpos = new BlockPosition(blockX, 0, blockZ);

		// Fetch the biomeConfig by registryKey
		// this.biomeProvider -> this.b
		ResourceKey<BiomeBase> key = ((OTGBiomeProvider) this.b).getBiomeRegistryKey((chunkX << 2) + 2, 2, (chunkZ << 2) + 2);
		BiomeConfig biomeConfig = OTG.getEngine().getPresetLoader().getBiomeConfig(key.a().toString());
		BiomeBase biome = this.c.getBiome((chunkX << 2) + 2, 2, (chunkZ << 2) + 2);

		// SharedSeedRandom -> SeededRandom
		SeededRandom sharedseedrandom = new SeededRandom();
		// setDecorationSeed() -> a()
		long decorationSeed = sharedseedrandom.a(worldGenRegion.getSeed(), blockX, blockZ);
		try
		{
			// Override normal decoration (Biome.func_242427_a()) with OTG's.
			biomeDecorate(biome, biomeConfig, structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
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
	
	// Chunk decoration method taken from Biome class
	@SuppressWarnings("deprecation")
	private void biomeDecorate (BiomeBase biome, BiomeConfig biomeConfig, StructureManager structureManager, ChunkGenerator chunkGenerator, RegionLimitedWorldAccess world, long seed, SeededRandom random, BlockPosition pos)
	{
		// Do OTG resource decoration, then MC decoration for any non-OTG resources registered to this biome, then snow.
		ChunkCoordinate chunkBeingDecorated = ChunkCoordinate.fromBlockCoords(pos.getX(), pos.getZ());
		SpigotWorldGenRegion spigotWorldGenRegion = new SpigotWorldGenRegion(this.preset.getFolderName(), this.preset.getWorldConfig(), world, this);
		this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, spigotWorldGenRegion, biomeConfig, getStructureCache(world.getMinecraftWorld().getWorld().getWorldFolder().toPath()));
		biome.a(structureManager, this, world, seed, random, pos);
		this.chunkDecorator.doSnowAndIce(spigotWorldGenRegion, chunkBeingDecorated);
	}

	// Mob spawning on initial chunk spawn (animals).
	@Override
	public void addMobs (RegionLimitedWorldAccess worldGenRegion)
	{
		//TODO: Make this respect the doMobSpawning game rule

		// getMainChunkX -> a()
		// getMainChunkZ -> b()
		int chunkX = worldGenRegion.a();
		int chunkZ = worldGenRegion.b();
		BiomeBase biome = worldGenRegion.getBiome((new ChunkCoordIntPair(chunkX, chunkZ)).l());
		SeededRandom sharedseedrandom = new SeededRandom();
		// setDecorationSeed() -> a()
		sharedseedrandom.a(worldGenRegion.getSeed(), chunkX << 4, chunkZ << 4);
		// performWorldGenSpawning() -> a()
		SpawnerCreature.a(worldGenRegion, biome, chunkX, chunkZ, sharedseedrandom);
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
		} else {
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

	public CustomStructureCache getStructureCache(Path worldSaveFolder)
	{
		if(this.structureCache == null)
		{
			this.structureCache = OTG.getEngine().createCustomStructureCache(this.preset.getFolderName(), worldSaveFolder, 0, this.worldSeed, this.preset.getWorldConfig().getCustomStructureType() == SettingsEnums.CustomStructureType.BO4);
		}
		return this.structureCache;
	}
	
	double getBiomeBlocksNoiseValue (int blockX, int blockZ)
	{
		return this.internalGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
	}

	public void fixBiomes(int chunkX, int chunkZ)
	{
		this.fixBiomesForChunk = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
	}
	
	// Shadowgen
	
	public Boolean checkHasVanillaStructureWithoutLoading(WorldServer world, ChunkCoordinate chunkCoord)
	{
		return this.shadowChunkGenerator.checkHasVanillaStructureWithoutLoading(world, this, this.b, this.getSettings(), chunkCoord);
	}

	public int getHighestBlockYInUnloadedChunk(Random worldRandom, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
	{
		return this.shadowChunkGenerator.getHighestBlockYInUnloadedChunk(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), worldRandom, x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow);
	}

	public LocalMaterialData getMaterialInUnloadedChunk(Random worldRandom, int x, int y, int z)
	{
		return this.shadowChunkGenerator.getMaterialInUnloadedChunk(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), worldRandom, x, y, z);
	}

	public SpigotChunkBuffer getChunkWithoutLoadingOrCaching(Random random, ChunkCoordinate chunkCoord)
	{
		return this.shadowChunkGenerator.getChunkWithoutLoadingOrCaching(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), random, chunkCoord);
	}	
}
