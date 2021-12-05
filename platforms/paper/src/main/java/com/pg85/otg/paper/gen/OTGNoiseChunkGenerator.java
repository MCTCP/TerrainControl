package com.pg85.otg.paper.gen;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.pg85.otg.util.gen.DecorationArea;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.gen.OTGChunkDecorator;
import com.pg85.otg.core.gen.OTGChunkGenerator;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.ILayerSource;
import com.pg85.otg.paper.biome.PaperBiome;
import com.pg85.otg.paper.presets.PaperPresetLoader;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.materials.LocalMaterialData;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OTGNoiseChunkGenerator extends ChunkGenerator
{	
	// Create a codec to serialise/deserialise OTGNoiseChunkGenerator
	public static final Codec<OTGNoiseChunkGenerator> CODEC = RecordCodecBuilder.create(
		(p_236091_0_) -> p_236091_0_
			.group(
				Codec.STRING.fieldOf("preset_folder_name").forGetter(
					(p_236090_0_) -> p_236090_0_.presetFolderName
				),
				BiomeSource.CODEC.fieldOf("biome_source").forGetter(
					(p_236096_0_) -> p_236096_0_.biomeSource
				),
				Codec.LONG.fieldOf("seed").stable().forGetter(
					(p_236093_0_) -> p_236093_0_.worldSeed
				),
				NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(
					(p_236090_0_) -> p_236090_0_.generatorSettings
				)
			).apply(
				p_236091_0_,
				p_236091_0_.stable(OTGNoiseChunkGenerator::new)
			)
	);

	private final Supplier<NoiseGeneratorSettings> generatorSettings;
	private final long worldSeed;
	private final int noiseHeight;
	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;

	private final ShadowChunkGenerator shadowChunkGenerator;
	public final OTGChunkGenerator internalGenerator;
	private final OTGChunkDecorator chunkDecorator;
	private final String presetFolderName;
	private final Preset preset;
	private final StructureSettings structSettings;
	protected final WorldgenRandom random;

	// TODO: Move this to WorldLoader when ready?
	private CustomStructureCache structureCache;

	// Used to specify which chunk to regen biomes and structures for
	// Necessary because Spigot calls those methods before we have the chance to inject
	private ChunkCoordinate fixBiomesForChunk = null;

	public OTGNoiseChunkGenerator (BiomeSource biomeProvider, long seed, Supplier<NoiseGeneratorSettings> generatorSettings)
	{
		this("default", biomeProvider, biomeProvider, seed, generatorSettings);
	}

	public OTGNoiseChunkGenerator (String presetName, BiomeSource biomeProvider, long seed, Supplier<NoiseGeneratorSettings> generatorSettings)
	{
		this(presetName, biomeProvider, biomeProvider, seed, generatorSettings);
	}

	// TODO: Why are there 2 biome providers, and why does getBiomeProvider() return the second, while we're using the first?
	// It looks like vanilla just inserts the same biomeprovider twice?
	private OTGNoiseChunkGenerator (String presetFolderName, BiomeSource biomeProvider1, BiomeSource biomeProvider2, long seed, Supplier<NoiseGeneratorSettings> generatorSettings)
	{
		super(biomeProvider1, biomeProvider2, generatorSettings.get().structureSettings(), seed);
		structSettings = generatorSettings.get().structureSettings();
		if (!(biomeProvider1 instanceof ILayerSource))
		{
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}

		this.presetFolderName = presetFolderName;
		this.worldSeed = seed;
		NoiseGeneratorSettings dimensionsettings = generatorSettings.get();
		this.generatorSettings = generatorSettings;
		NoiseSettings noisesettings = dimensionsettings.noiseSettings();
		this.noiseHeight = noisesettings.height();

		this.defaultBlock = dimensionsettings.getDefaultBlock();
		this.defaultFluid = dimensionsettings.getDefaultFluid();

		this.random = new WorldgenRandom(seed);

		this.preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
		this.shadowChunkGenerator = new ShadowChunkGenerator();
		this.internalGenerator = new OTGChunkGenerator(this.preset, seed, (ILayerSource) biomeProvider1, ((PaperPresetLoader) OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetFolderName), OTG.getEngine().getLogger());
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
	public void createStructures(RegistryAccess iregistrycustom, StructureFeatureManager structuremanager, ChunkAccess chunk, StructureManager definedstructuremanager, long i)
	{
		ChunkPos chunkcoordintpair = chunk.getPos();
		Biome biomebase = this.biomeSource.getPrimaryBiome(chunk.getPos());
		this.createSingleStructure(StructureFeatures.STRONGHOLD, iregistrycustom, structuremanager, chunk, definedstructuremanager, i, chunkcoordintpair, biomebase);

		for (Supplier<ConfiguredStructureFeature<?, ?>> supplier : biomebase.getGenerationSettings().structures())
		{
			ConfiguredStructureFeature<?, ?> structurefeature = supplier.get();
			if (structurefeature == StructureFeatures.STRONGHOLD)
			{
				synchronized(structurefeature)
				{
					this.createSingleStructure(structurefeature, iregistrycustom, structuremanager, chunk, definedstructuremanager, i, chunkcoordintpair, biomebase);
				}
			} else {
				this.createSingleStructure(structurefeature, iregistrycustom, structuremanager, chunk, definedstructuremanager, i, chunkcoordintpair, biomebase);
			}
		}
	}

	// This is janky... but it works. THX Authvin
	private void createSingleStructure(ConfiguredStructureFeature<?, ?> structurefeature, RegistryAccess registryManager, StructureFeatureManager structuremanager, ChunkAccess chunk, StructureManager definedstructuremanager, long worldSeed, ChunkPos chunkPos, Biome biome)
	{
		StructureStart<?> structurestart = structuremanager.getStartForFeature(SectionPos.bottomOf(chunk), structurefeature.feature, chunk);
		int j = structurestart != null ? structurestart.getReferences() : 0;
		StructureFeatureConfiguration structuresettingsfeature = this.structSettings.getConfig(structurefeature.feature);
		if (structuresettingsfeature != null)
		{
			StructureStart<?> structurestart1 = structurefeature.generate(registryManager, this, this.biomeSource, definedstructuremanager, worldSeed, chunkPos, biome, j, structuresettingsfeature, chunk);
			structuremanager.setStartForFeature(SectionPos.bottomOf(chunk), structurefeature.feature, structurestart1, chunk);
		}
	}

	// Base terrain gen

	// Generates the base terrain for a chunk. Spigot compatible.
	public void buildNoiseSpigot (ServerLevel world, org.bukkit.generator.ChunkGenerator.ChunkData chunk, ChunkCoordinate chunkCoord, Random random)
	{
		ChunkBuffer buffer = new PaperChunkBuffer(chunk, chunkCoord);
		ChunkAccess cachedChunk = this.shadowChunkGenerator.getChunkFromCache(chunkCoord);
		if (cachedChunk != null)
		{
			this.shadowChunkGenerator.fillWorldGenChunkFromShadowChunk(chunkCoord, chunk, cachedChunk);
		} else {
			// Setup jigsaw data
//			ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
//			ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);
//			ChunkPos pos = new ChunkPos(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
//			int chunkX = pos.x;
//			int chunkZ = pos.z;
//			int startX = chunkX << 4;
//			int startZ = chunkZ << 4;
//
//			StructureManager manager = world.getStructureManager();
//			// Iterate through all of the jigsaw structures (villages, pillager outposts, nether fossils)
//			for (StructureFeature<?> structure : StructureFeature.NOISE_AFFECTING_FEATURES) {
//				// Get all structure starts in this chunk
//				manager.a(SectionPosition.a(pos, 0), structure).forEach((start) -> {
//					// Iterate through the pieces in the structure
//					for (StructurePiece piece : start.d()) {
//						// Check if it intersects with this chunk
//						if (piece.a(pos, 12)) {
//							StructureBoundingBox box = piece.g();
//
//							if (piece instanceof WorldGenFeaturePillagerOutpostPoolPiece) {
//								WorldGenFeaturePillagerOutpostPoolPiece villagePiece = (WorldGenFeaturePillagerOutpostPoolPiece) piece;
//								// Add to the list if it's a rigid piece
//								if (villagePiece.b().e() == WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID) {
//									structures.add(new JigsawStructureData(box.a, box.b, box.c, box.d, villagePiece.d(), box.f, true, 0, 0, 0));
//								}
//
//								// Get all the junctions in this piece
//								for (WorldGenFeatureDefinedStructureJigsawJunction junction : villagePiece.e()) {
//									int sourceX = junction.a();
//									int sourceZ = junction.c();
//
//									// If the junction is in this chunk, then add to list
//									if (sourceX > startX - 12 && sourceZ > startZ - 12 && sourceX < startX + 15 + 12 && sourceZ < startZ + 15 + 12) {
//										junctions.add(new JigsawStructureData(0, 0, 0, 0, 0, 0, false, junction.a(), junction.b(), junction.c()));
//									}
//								}
//							} else {
//								structures.add(new JigsawStructureData(box.a, box.b, box.c, box.d, 0, box.f, false, 0, 0, 0));
//							}
//						}
//					}
//
//				});
//			}

			StructureFeatureManager manager = world.structureFeatureManager();
			// Setup jigsaw data
			ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
			ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);
			ChunkPos pos = new ChunkPos(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
			int chunkX = pos.x;
			int chunkZ = pos.z;
			int startX = chunkX << 4;
			int startZ = chunkZ << 4;

			// Iterate through all of the jigsaw structures (villages, pillager outposts, nether fossils)
			for(StructureFeature<?> structure : StructureFeature.NOISE_AFFECTING_FEATURES) {
				// Get all structure starts in this chunk
				manager.startsForFeature(SectionPos.of(pos, 0), structure).forEach((start) -> {
					// Iterate through the pieces in the structure
					for(StructurePiece piece : start.getPieces()) {
						// Check if it intersects with this chunk
						if (piece.isCloseToChunk(pos, 12)) {
							BoundingBox box = piece.getBoundingBox();

							if (piece instanceof PoolElementStructurePiece villagePiece) {
								// Add to the list if it's a rigid piece
								if (villagePiece.getElement().getProjection() == StructureTemplatePool.Projection.RIGID) {
									structures.add(new JigsawStructureData(box.minX(), box.minY(), box.minZ(),box.maxX(), villagePiece.getGroundLevelDelta(), box.maxZ(), true, 0, 0, 0));
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
								structures.add(new JigsawStructureData(box.minX(), box.minY(), box.minZ(),box.maxX(), 0, box.maxZ(),  false, 0, 0, 0));
							}
						}
					}

				});
			}

			this.internalGenerator.populateNoise(this.preset.getWorldConfig().getWorldHeightCap(), random, buffer, buffer.getChunkCoordinate(), structures, junctions);
			this.shadowChunkGenerator.setChunkGenerated(chunkCoord);			
		}
	}


	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, StructureFeatureManager accessor, ChunkAccess chunk)
	{
		buildNoise(accessor, chunk);

		return CompletableFuture.completedFuture(chunk);
	}

	// Generates the base terrain for a chunk.
	public void buildNoise (StructureFeatureManager manager, ChunkAccess chunk)
	{
		LevelAccessor world = chunk.getLevel();
		// If we've already generated and cached this
		// chunk while it was unloaded, use cached data.
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);

		// When generating the spawn area, Spigot will get the structure and biome info for the first chunk before we can inject
		// Therefore, we need to re-do these calls now, for that one chunk
		if (fixBiomesForChunk != null && fixBiomesForChunk.equals(chunkCoord))
		{
			// Should only run when first creating the world, on a single chunk
			this.createStructures(world.getMinecraftWorld().registryAccess(), world.getMinecraftWorld().structureFeatureManager(), chunk,
				world.getMinecraftWorld().getStructureManager(), world.getMinecraftWorld().getSeed());
			this.createBiomes(((CraftServer) Bukkit.getServer()).getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), chunk);
			fixBiomesForChunk = null;
		}
		ChunkBuffer buffer = new PaperChunkBuffer(chunk);
		ChunkAccess cachedChunk = this.shadowChunkGenerator.getChunkFromCache(chunkCoord);
		if (cachedChunk != null)
		{
			this.shadowChunkGenerator.fillWorldGenChunkFromShadowChunk(chunkCoord, chunk, cachedChunk);
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
			for(StructureFeature<?> structure : StructureFeature.NOISE_AFFECTING_FEATURES) {
				// Get all structure starts in this chunk
				manager.startsForFeature(SectionPos.of(pos, 0), structure).forEach((start) -> {
					// Iterate through the pieces in the structure
					for(StructurePiece piece : start.getPieces()) {
						// Check if it intersects with this chunk
						if (piece.isCloseToChunk(pos, 12)) {
							BoundingBox box = piece.getBoundingBox();

							if (piece instanceof PoolElementStructurePiece villagePiece) {
								// Add to the list if it's a rigid piece
								if (villagePiece.getElement().getProjection() == StructureTemplatePool.Projection.RIGID) {
									structures.add(new JigsawStructureData(box.minX(), box.minY(), box.minZ(),box.maxX(), villagePiece.getGroundLevelDelta(), box.maxZ(), true, 0, 0, 0));
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
								structures.add(new JigsawStructureData(box.minX(), box.minY(), box.minZ(),box.maxX(), 0, box.maxZ(),  false, 0, 0, 0));
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
	public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunk)
	{
		// OTG handles surface/ground blocks during base terrain gen. For non-OTG biomes used
	}

	// Carvers: Caves and ravines

	@Override
	public void applyCarvers(long seed, BiomeManager biomeManager, ChunkAccess chunk, GenerationStep.Carving stage)
	{
		if (stage == GenerationStep.Carving.AIR)
		{
			ProtoChunk protoChunk = (ProtoChunk) chunk;
			ChunkBuffer chunkBuffer = new PaperChunkBuffer(protoChunk);
			BitSet carvingMask = protoChunk.getOrCreateCarvingMask(stage);
			this.internalGenerator.carve(chunkBuffer, seed, protoChunk.getPos().x, protoChunk.getPos().z, carvingMask, true, true); //TODO: Don't use hardcoded true
		}
		super.applyCarvers(seed, biomeManager, chunk, stage);
	}

	// Population / decoration

	// Does decoration for a given pos/chunk
	@Override
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
		
		ChunkCoordinate chunkBeingDecorated = ChunkCoordinate.fromBlockCoords(worldX, worldZ);
		PaperWorldGenRegion spigotWorldGenRegion = new PaperWorldGenRegion(this.preset.getFolderName(), this.preset.getWorldConfig(), worldGenRegion, this);
		IBiome biome = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2) + 2, (worldGenRegion.getCenter().z << 2) + 2);
		IBiome biome1 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2), (worldGenRegion.getCenter().z << 2));
		IBiome biome2 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2), (worldGenRegion.getCenter().z << 2) + 4);
		IBiome biome3 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2) + 4, (worldGenRegion.getCenter().z << 2));
		IBiome biome4 = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2) + 4, (worldGenRegion.getCenter().z << 2) + 4);		
		IBiomeConfig biomeConfig = biome.getBiomeConfig();
		// World save folder name may not be identical to level name, fetch it.
		Path worldSaveFolder = worldGenRegion.getMinecraftWorld().getWorld().getWorldFolder().toPath();

		// Get most common biome in chunk and use that for decoration - Frank
		if (!getPreset().getWorldConfig().improvedBorderDecoration()) {
			List<IBiome> biomes = new ArrayList<IBiome>();
			biomes.add(biome);
			biomes.add(biome1);
			biomes.add(biome2);
			biomes.add(biome3);
			biomes.add(biome4);
			Map<IBiome, Integer> map = new HashMap<>();
			for (IBiome b : biomes) {
				Integer val = map.get(b);
				map.put(b, val == null ? 1 : val + 1);
			}

			Map.Entry<IBiome, Integer> max = null;

			for (Map.Entry<IBiome, Integer> ent : map.entrySet()) {
				if (max == null || ent.getValue() > max.getValue()) max = ent;
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
			this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, spigotWorldGenRegion, biomeConfig, getStructureCache(worldSaveFolder));
			alreadyDecorated.add(biome.getBiomeConfig().getOTGBiomeId());
			// Attempt to decorate other biomes if ImprovedBiomeDecoration - Frank
			if (getPreset().getWorldConfig().improvedBorderDecoration()) {
				if (!alreadyDecorated.contains(biome1.getBiomeConfig().getOTGBiomeId())) {
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, spigotWorldGenRegion, biome1.getBiomeConfig(), getStructureCache(worldSaveFolder));
					((PaperBiome) biome1).getBiome().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
					alreadyDecorated.add(biome1.getBiomeConfig().getOTGBiomeId());
				}
				if (!alreadyDecorated.contains(biome2.getBiomeConfig().getOTGBiomeId())) {
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, spigotWorldGenRegion, biome2.getBiomeConfig(), getStructureCache(worldSaveFolder));
					((PaperBiome) biome2).getBiome().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
					alreadyDecorated.add(biome2.getBiomeConfig().getOTGBiomeId());
				}
				if (!alreadyDecorated.contains(biome3.getBiomeConfig().getOTGBiomeId())) {
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, spigotWorldGenRegion, biome3.getBiomeConfig(), getStructureCache(worldSaveFolder));
					((PaperBiome) biome3).getBiome().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
					alreadyDecorated.add(biome3.getBiomeConfig().getOTGBiomeId());
				}
				if (!alreadyDecorated.contains(biome4.getBiomeConfig().getOTGBiomeId())) {
					this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, spigotWorldGenRegion, biome4.getBiomeConfig(), getStructureCache(worldSaveFolder));
					((PaperBiome) biome4).getBiome().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
				}
			} else {
				((PaperBiome)biome).getBiome().generate(structureManager, this, worldGenRegion, decorationSeed, sharedseedrandom, blockpos);
			}
			this.chunkDecorator.doSnowAndIce(spigotWorldGenRegion, chunkBeingDecorated);
		}
		catch (Exception exception)
		{
			CrashReport crashreport = CrashReport.forThrowable(exception, "Biome decoration");
			crashreport.addCategory("Generation").setDetail("CenterX", worldX).setDetail("CenterZ", worldZ).setDetail("Seed", decorationSeed);
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public void createBiomes (Registry<Biome> biomeRegistry, ChunkAccess chunk)
	{
		ChunkPos chunkcoordintpair = chunk.getPos();
		((ProtoChunk)chunk).setBiomes(new ChunkBiomeContainer(biomeRegistry, chunk, chunkcoordintpair, this.runtimeBiomeSource));
	}

	// Mob spawning on initial chunk spawn (animals).
	@Override
	public void spawnOriginalMobs(WorldGenRegion region)
	{
		// We don't respect the mob spawning setting, because we can't access it
		int chunkX = region.getCenter().x;
		int chunkZ = region.getCenter().z;
		IBiome biome = this.internalGenerator.getCachedBiomeProvider().getBiome(chunkX * Constants.CHUNK_SIZE + DecorationArea.DECORATION_OFFSET, chunkZ * Constants.CHUNK_SIZE + DecorationArea.DECORATION_OFFSET);
		WorldgenRandom sharedseedrandom = new WorldgenRandom();
		sharedseedrandom.setDecorationSeed(region.getSeed(), chunkX << 4, chunkZ << 4);
		NaturalSpawner.spawnMobsForChunkGeneration(region, ((PaperBiome)biome).getBiome(), region.getCenter(), sharedseedrandom);
	}

	// Mob spawning on chunk tick
	@Override
	public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureManager, MobCategory entityClassification, BlockPos blockPos)
	{
		if (structureManager.getStructureAt(blockPos, true, StructureFeature.SWAMP_HUT).isValid())
		{
			if (entityClassification == MobCategory.MONSTER)
			{
				return StructureFeature.SWAMP_HUT.getSpecialEnemies();
			}

			if (entityClassification == MobCategory.CREATURE)
			{
				return StructureFeature.SWAMP_HUT.getSpecialAnimals();
			}
		}

		if (entityClassification == MobCategory.MONSTER)
		{
			if (structureManager.getStructureAt(blockPos, false, StructureFeature.PILLAGER_OUTPOST).isValid())
			{
				return StructureFeature.PILLAGER_OUTPOST.getSpecialEnemies();
			}

			if (structureManager.getStructureAt(blockPos, false, StructureFeature.OCEAN_MONUMENT).isValid())
			{
				return StructureFeature.OCEAN_MONUMENT.getSpecialEnemies();
			}

			if (structureManager.getStructureAt(blockPos, true, StructureFeature.NETHER_BRIDGE).isValid())
			{
				return StructureFeature.NETHER_BRIDGE.getSpecialEnemies();
			}
		}

		return entityClassification == MobCategory.UNDERGROUND_WATER_CREATURE && structureManager.getStructureAt(blockPos, false, StructureFeature.OCEAN_MONUMENT).isValid() ? StructureFeature.OCEAN_MONUMENT.getSpecialUndergroundWaterAnimals() : super.getMobsAt(biome, structureManager, entityClassification, blockPos);
	}

	// Noise
	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor world)
	{
		return this.sampleHeightmap(x, z, null, heightmap.isOpaque());
	}

	// Provides a sample of the full column for structure generation.
	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world)
	{
		BlockState[] ablockstate = new BlockState[256];
		this.sampleHeightmap(x, z, ablockstate, null);
		return new NoiseColumn(0, ablockstate);
	}

	// Samples the noise at a column and provides a view of the blockstates, or fills a heightmap.
	private int sampleHeightmap (int x, int z, @Nullable BlockState[] blockStates, @Nullable Predicate<BlockState> predicate)
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

		//IBiomeConfig biomeConfig = this.internalGenerator.getCachedBiomeProvider().getBiomeConfig(x, z);
		
		BlockState state;
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
				density = Mth.lerp3(yLerp, xLerp, zLerp, x0z0y0, x0z0y1, x1z0y0, x1z0y1, x0z1y0, x0z1y1, x1z1y0, x1z1y1);

				// Get the real y position (translate noise chunk and noise piece)
				y = (noiseY * 8) + pieceY;

				//state = this.getBlockState(density, y, biomeConfig);
				state = this.getBlockState(density, y);
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
	//protected IBlockData getBlockState(double density, int y, IBiomeConfig config)
	private BlockState getBlockState(double density, int y)
	{
		if (density > 0.0D)
		{
			return this.defaultBlock;
			//return ((PaperMaterialData) config.getStoneBlockReplaced(y)).internalBlock();
		}
		else if (y < this.getSeaLevel())
		{
			return this.defaultFluid;
			//return ((PaperMaterialData) config.getWaterBlockReplaced(y)).internalBlock();
		} else {
			return Blocks.AIR.defaultBlockState();
		}
	}

	// Getters / misc

	@Override
	public ChunkGenerator withSeed(long seed)
	{
		return new OTGNoiseChunkGenerator(this.biomeSource.withSeed(seed), seed, this.generatorSettings);
	}

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
	public int getSeaLevel ()
	{
		// TODO: remove supplier
		return this.generatorSettings.get().seaLevel();
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

	public Boolean checkHasVanillaStructureWithoutLoading(ServerLevel world, ChunkCoordinate chunkCoord)
	{
		return this.shadowChunkGenerator.checkHasVanillaStructureWithoutLoading(world, this, this.biomeSource, this.getSettings(), chunkCoord, this.internalGenerator.getCachedBiomeProvider());
	}

	public int getHighestBlockYInUnloadedChunk(Random worldRandom, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, ServerLevel level)
	{
		return this.shadowChunkGenerator.getHighestBlockYInUnloadedChunk(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), worldRandom, x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow, level);
	}

	public LocalMaterialData getMaterialInUnloadedChunk(Random worldRandom, int x, int y, int z, ServerLevel level)
	{
		return this.shadowChunkGenerator.getMaterialInUnloadedChunk(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), worldRandom, x, y, z, level);
	}

	public PaperChunkBuffer getChunkWithoutLoadingOrCaching(Random random, ChunkCoordinate chunkCoord, ServerLevel level)
	{
		return this.shadowChunkGenerator.getChunkWithoutLoadingOrCaching(this.internalGenerator, this.preset.getWorldConfig().getWorldHeightCap(), random, chunkCoord, level);
	}	
}
