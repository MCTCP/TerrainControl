package com.pg85.otg.spigot.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.gen.OTGChunkGenerator;
import com.pg85.otg.gen.OTGChunkDecorator;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.ILayerSource;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.biome.SpigotBiome;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.spigot.presets.SpigotPresetLoader;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.materials.LocalMaterialData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.server.v1_16_R3.*;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;

import javax.annotation.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class OTGNoiseChunkGenerator extends ChunkGenerator
{	
	// Create a codec to serialise/deserialise OTGNoiseChunkGenerator
	public static final Codec<OTGNoiseChunkGenerator> CODEC = RecordCodecBuilder.create(
		(p_236091_0_) -> p_236091_0_
			.group(
				Codec.STRING.fieldOf("preset_folder_name").forGetter(
					(p_236090_0_) -> p_236090_0_.presetFolderName
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
	protected final IBlockData defaultBlock;
	protected final IBlockData defaultFluid;

	private final ShadowChunkGenerator shadowChunkGenerator;
	private final OTGChunkGenerator internalGenerator;
	private final OTGChunkDecorator chunkDecorator;
	private final NoiseGenerator surfaceNoise;
	private final String presetFolderName;
	private final Preset preset;
	private final StructureSettings structSettings;
	protected final SeededRandom random;

	// TODO: Move this to WorldLoader when ready?
	private CustomStructureCache structureCache;

	// Used to specify which chunk to regen biomes and structures for
	// Necessary because Spigot calls those methods before we have the chance to inject
	private ChunkCoordinate fixBiomesForChunk = null;

	public OTGNoiseChunkGenerator (WorldChunkManager biomeProvider, long seed, Supplier<GeneratorSettingBase> dimensionSettingsSupplier)
	{
		this("default", biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	public OTGNoiseChunkGenerator (String presetName, WorldChunkManager biomeProvider, long seed, Supplier<GeneratorSettingBase> dimensionSettingsSupplier)
	{
		this(presetName, biomeProvider, biomeProvider, seed, dimensionSettingsSupplier);
	}

	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	private OTGNoiseChunkGenerator (String presetFolderName, WorldChunkManager biomeProvider1, WorldChunkManager biomeProvider2, long seed, Supplier<GeneratorSettingBase> dimensionSettingsSupplier)
	{
		// getStructures() -> a()
		super(biomeProvider1, biomeProvider2, dimensionSettingsSupplier.get().a(), seed);
		structSettings = dimensionSettingsSupplier.get().a();
		if (!(biomeProvider1 instanceof ILayerSource))
		{
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}

		this.presetFolderName = presetFolderName;
		this.worldSeed = seed;
		GeneratorSettingBase dimensionsettings = dimensionSettingsSupplier.get();
		this.dimensionSettingsSupplier = dimensionSettingsSupplier;
		// getNoise() -> b()
		NoiseSettings noisesettings = dimensionsettings.b();
		// func_236169_a_() -> a()
		this.noiseHeight = noisesettings.a();
		
		this.defaultBlock = dimensionsettings.c();
		this.defaultFluid = dimensionsettings.d();
		
		this.random = new SeededRandom(seed);
		this.surfaceNoise = (NoiseGenerator)(noisesettings.i() ? new NoiseGenerator3(this.random, IntStream.rangeClosed(-3, 0)) : new NoiseGeneratorOctaves(this.random, IntStream.rangeClosed(-3, 0)));

		this.preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
		this.shadowChunkGenerator = new ShadowChunkGenerator();
		this.internalGenerator = new OTGChunkGenerator(this.preset, seed, (ILayerSource) biomeProvider1, ((SpigotPresetLoader)OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetFolderName), OTG.getEngine().getLogger());
		this.chunkDecorator = new OTGChunkDecorator();
	}

	public ICachedBiomeProvider getCachedBiomeProvider()
	{
		return this.internalGenerator.getCachedBiomeProvider();
	}

	public void saveStructureCache ()
	{
		if (this.chunkDecorator.getIsSaveRequired() && this.structureCache != null)
		{
			this.structureCache.saveToDisk(OTG.getEngine().getLogger(), this.chunkDecorator);
		}
	}

	@Override
	public StructureSettings getSettings() {
		return this.structSettings;
	}

	// Code borrowed from ChunkGenerator.java
	@Override
	public void createStructures(IRegistryCustom iregistrycustom, StructureManager structuremanager, IChunkAccess ichunkaccess, DefinedStructureManager definedstructuremanager, long i)
	{
		ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
		BiomeBase biomebase = this.b.getBiome((chunkcoordintpair.x << 2) + 2, 0, (chunkcoordintpair.z << 2) + 2);
		this.createSingleStructure(StructureFeatures.k, iregistrycustom, structuremanager, ichunkaccess, definedstructuremanager, i, chunkcoordintpair, biomebase);

		for (Supplier<StructureFeature<?, ?>> supplier : biomebase.e().a())
		{
			StructureFeature<?, ?> structurefeature = supplier.get();
			if (structurefeature.d == StructureGenerator.STRONGHOLD)
			{
				synchronized(structurefeature)
				{
					this.createSingleStructure(structurefeature, iregistrycustom, structuremanager, ichunkaccess, definedstructuremanager, i, chunkcoordintpair, biomebase);
				}
			} else {
				this.createSingleStructure(structurefeature, iregistrycustom, structuremanager, ichunkaccess, definedstructuremanager, i, chunkcoordintpair, biomebase);
			}
		}
	}

	// This is janky... but it works. THX Authvin
	private void createSingleStructure(StructureFeature<?, ?> structurefeature, IRegistryCustom iregistrycustom, StructureManager structuremanager, IChunkAccess ichunkaccess, DefinedStructureManager definedstructuremanager, long i, ChunkCoordIntPair chunkcoordintpair, BiomeBase biomebase)
	{
		StructureStart<?> structurestart = structuremanager.a(SectionPosition.a(ichunkaccess.getPos(), 0), structurefeature.d, ichunkaccess);
		int j = structurestart != null ? structurestart.j() : 0;
		StructureSettingsFeature structuresettingsfeature = this.structSettings.a(structurefeature.d);
		if (structuresettingsfeature != null)
		{
			StructureStart<?> structurestart1 = structurefeature.a(iregistrycustom, this, this.b, definedstructuremanager, i, chunkcoordintpair, biomebase, j, structuresettingsfeature);
			structuremanager.a(SectionPosition.a(ichunkaccess.getPos(), 0), structurefeature.d, structurestart1, ichunkaccess);
		}
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
		// OTG handles surface/ground blocks during base terrain gen. For non-OTG biomes used
		// with TemplateForBiome, we want to use registered surfacebuilders though.
		// TODO: Disable any surface/ground block related features for Template BiomeConfigs. 

		ChunkCoordIntPair chunkpos = chunk.getPos();
		int i = chunkpos.x;
		int j = chunkpos.z;
		SeededRandom sharedseedrandom = new SeededRandom();
		sharedseedrandom.a(i, j);
		ChunkCoordIntPair chunkpos1 = chunk.getPos();
		int chunkMinX = chunkpos1.d();
		int chunkMinZ = chunkpos1.e();
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
				if(biome.getBiomeConfig().getTemplateForBiome())
				{
					i2 = chunk.getHighestBlock(HeightMap.Type.WORLD_SURFACE_WG, xInChunk, zInChunk) + 1;
					d1 = this.surfaceNoise.a((double)worldX * 0.0625D, (double)worldZ * 0.0625D, 0.0625D, (double)xInChunk * 0.0625D) * 15.0D;
					((SpigotBiome)biome).getBiomeBase().a(sharedseedrandom, chunk, chunkMinX, chunkMinZ, worldX, d1, defaultFluid, defaultBlock, worldZ, i2);
				}
			}
		}
		// Skip bedrock, OTG always handles that.
	}

	// Carvers: Caves and ravines

	// GenerationStage -> WorldGenStage, Carver -> Features
	@Override
	public void doCarving (long seed, BiomeManager biomeManager, IChunkAccess chunk, WorldGenStage.Features stage)
	{
		if (stage == WorldGenStage.Features.AIR)
		{
			ProtoChunk protoChunk = (ProtoChunk) chunk;
			ChunkBuffer chunkBuffer = new SpigotChunkBuffer(protoChunk);
			BitSet carvingMask = protoChunk.b(stage);
			this.internalGenerator.carve(chunkBuffer, seed, protoChunk.getPos().x, protoChunk.getPos().z, carvingMask, true, true); //TODO: Don't use hardcoded true
		}
		super.doCarving(seed, biomeManager, chunk, stage);
	}

	// Population / decoration

	// Does decoration for a given pos/chunk
	@SuppressWarnings("deprecation")
	@Override
	public void addDecorations (RegionLimitedWorldAccess worldGenRegion, StructureManager structureManager)
	{
		if(!OTG.getEngine().getPluginConfig().getDecorationEnabled())
		{
			return;
		}

		// Do OTG resource decoration, then MC decoration for any non-OTG resources registered to this biome, then snow.
		
		// Taken from vanilla
		// getMainChunkX -> a()
		// getMainChunkZ -> b()
		int worldX = worldGenRegion.a() * Constants.CHUNK_SIZE;
		int worldZ = worldGenRegion.b() * Constants.CHUNK_SIZE;
		BlockPosition blockpos = new BlockPosition(worldX, 0, worldZ);
		// SharedSeedRandom -> SeededRandom
		SeededRandom sharedseedrandom = new SeededRandom();
		// setDecorationSeed() -> a()
		long decorationSeed = sharedseedrandom.a(worldGenRegion.getSeed(), worldX, worldZ);
		//
		
		ChunkCoordinate chunkBeingDecorated = ChunkCoordinate.fromBlockCoords(worldX, worldZ);
		SpigotWorldGenRegion spigotWorldGenRegion = new SpigotWorldGenRegion(this.preset.getFolderName(), this.preset.getWorldConfig(), worldGenRegion, this);
		IBiome biome = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.a() << 2) + 2, (worldGenRegion.b() << 2) + 2);		
		IBiomeConfig biomeConfig = biome.getBiomeConfig();
		// World save folder name may not be identical to level name, fetch it.
		Path worldSaveFolder = worldGenRegion.getMinecraftWorld().getWorld().getWorldFolder().toPath();
		
		try
		{
			this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, spigotWorldGenRegion, biomeConfig, getStructureCache(worldSaveFolder));
			((SpigotBiome)biome).getBiomeBase().a(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
			// Template biomes handle their own snow, OTG biomes use OTG snow.
			// TODO: Snow is handled per chunk, so this may cause some artifacts on biome borders.
			if(!biome.getBiomeConfig().getTemplateForBiome())
			{
				this.chunkDecorator.doSnowAndIce(spigotWorldGenRegion, chunkBeingDecorated);
			}
		}
		catch (Exception exception)
		{
			// makeCrashReport() -> a()
			CrashReport crashreport = CrashReport.a(exception, "Biome decoration");
			// crashReport.makeCategory() -> crashReport.a()
			// crashReport.addDetail() -> crashReport.a()
			crashreport.a("Generation").a("CenterX", worldX).a("CenterZ", worldZ).a("Seed", decorationSeed);
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public void createBiomes (IRegistry<BiomeBase> iregistry, IChunkAccess ichunkaccess)
	{
		ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
		((ProtoChunk) ichunkaccess).a(new BiomeStorage(iregistry, chunkcoordintpair, this.c));
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
		IBiome biome = this.internalGenerator.getCachedBiomeProvider().getBiome(chunkX * Constants.CHUNK_SIZE + DecorationArea.DECORATION_OFFSET, chunkZ * Constants.CHUNK_SIZE + DecorationArea.DECORATION_OFFSET);	
		SeededRandom sharedseedrandom = new SeededRandom();
		// setDecorationSeed() -> a()
		sharedseedrandom.a(worldGenRegion.getSeed(), chunkX << 4, chunkZ << 4);
		// performWorldGenSpawning() -> a()
		SpawnerCreature.a(worldGenRegion, ((SpigotBiome)biome).getBiomeBase(), chunkX, chunkZ, sharedseedrandom);
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

		IBiomeConfig biomeConfig = this.internalGenerator.getCachedBiomeProvider().getBiomeConfig(x, z);
		
		IBlockData state;
		double x0z0y0;
		double x0z1y0;
		double x1z0y0;
		double x1z1y0;
		double x0z0y1;
		double x0z1y1;
		double x1z0y1;
		double x1z1y1;
		double yLerp;
		double density;
		int y;
		// [0, 32] -> noise chunks
		for (int noiseY = this.internalGenerator.getNoiseSizeY() - 1; noiseY >= 0; --noiseY)
		{
			// Gets all the noise in a 2x2x2 cube and interpolates it together.
			// Lower pieces
			x0z0y0 = noiseData[0][noiseY];
			x0z1y0 = noiseData[1][noiseY];
			x1z0y0 = noiseData[2][noiseY];
			x1z1y0 = noiseData[3][noiseY];
			// Upper pieces
			x0z0y1 = noiseData[0][noiseY + 1];
			x0z1y1 = noiseData[1][noiseY + 1];
			x1z0y1 = noiseData[2][noiseY + 1];
			x1z1y1 = noiseData[3][noiseY + 1];

			// [0, 8] -> noise pieces
			for (int pieceY = 7; pieceY >= 0; --pieceY)
			{
				yLerp = (double) pieceY / 8.0;
				// Density at this position given the current y interpolation
				// MathHelper.lerp3() -> MathHelper.a()
				density = MathHelper.a(yLerp, xLerp, zLerp, x0z0y0, x0z0y1, x1z0y0, x1z0y1, x0z1y0, x0z1y1, x1z1y0, x1z1y1);

				// Get the real y position (translate noise chunk and noise piece)
				y = (noiseY * 8) + pieceY;

				state = this.getBlockState(density, y, biomeConfig);
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

	public Preset getPreset()
	{
		return preset;
	}

	public CustomStructureCache getStructureCache(Path worldSaveFolder)
	{
		if(this.structureCache == null)
		{
			this.structureCache = OTG.getEngine().createCustomStructureCache(this.preset.getFolderName(), worldSaveFolder, this.worldSeed, this.preset.getWorldConfig().getCustomStructureType() == SettingsEnums.CustomStructureType.BO4);
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
		return this.shadowChunkGenerator.checkHasVanillaStructureWithoutLoading(world, this, this.b, this.getSettings(), chunkCoord, this.internalGenerator.getCachedBiomeProvider());
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
