package com.pg85.otg.paper.gen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import com.pg85.otg.paper.util.ObfuscationHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

import org.bukkit.craftbukkit.v1_17_R1.generator.CraftChunkData;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.gen.OTGChunkGenerator;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.paper.biome.PaperBiome;
import com.pg85.otg.paper.materials.PaperMaterialData;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.PillagerOutpostFeature;
import net.minecraft.world.level.levelgen.feature.VillageFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/**
 * Shadow chunk generation means generating base terrain for chunks
 * without using mc's world generation flow. OTG's chunkgenerator is
 * called internally to generate base terrain for dummy chunks.
 * Shadowgenned chunks are stored in a fixed size FIFO cache, data is
 * reused when base terraingen is requested for those chunks via
 * normal worldgen. Shadowgen is used for BO4's and /otg mapterrain.
 *
 * Shadowgen can only be done for chunks that don't contain vanilla structures,
 * since those structures may use density based smoothing applied during noisegen,
 * which unfortunately is done in a non-thread-safe/blocking manner, necessitating
 * the use of a WorldGenRegion.
 */
public class ShadowChunkGenerator
{
	// TODO: Add a setting to the worldconfig for the size of these caches?
	private final FifoMap<BlockPos2D, LocalMaterialData[]> unloadedBlockColumnsCache = new FifoMap<BlockPos2D, LocalMaterialData[]>(1024);
	private final FifoMap<ChunkCoordinate, ChunkAccess> unloadedChunksCache = new FifoMap<>(512);
	private final FifoMap<ChunkCoordinate, Boolean> hasVanillaStructureChunkCache = new FifoMap<ChunkCoordinate, Boolean>(2048);

	static Field heightMaps;
	static Field light;
	static Field sections;

	static
	{
		try
		{
			heightMaps = ObfuscationHelper.getField(ProtoChunk.class, "heightmaps", "f");
			heightMaps.setAccessible(true);

			light = ObfuscationHelper.getField(ProtoChunk.class, "lights", "l");
			light.setAccessible(true);

			sections = ObfuscationHelper.getField(ProtoChunk.class, "sections", "j");
			sections.setAccessible(true);
		} catch (ReflectiveOperationException ex)
		{
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private int cacheHits = 0;
	@SuppressWarnings("unused")
	private int cacheMisses = 0;

	public ShadowChunkGenerator() { }

	private PaperChunkBuffer getUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random random, ChunkCoordinate chunkCoordinate, LevelHeightAccessor level)
	{
		// Make a dummy chunk, we'll fill this with base terrain data ourselves, without touching any MC worldgen logic.
		// As an optimisation, we cache the dummy chunk in a limited size FIFO cache. Later when MC requests the chunk 
		// during world generation, we swap the dummy chunk's data into the real chunk.
		ProtoChunk chunk = new ProtoChunk(new ChunkPos(chunkCoordinate.getChunkX(), chunkCoordinate.getChunkZ()), null, level, null);
		PaperChunkBuffer buffer = new PaperChunkBuffer(chunk);

		// This is where vanilla processes any noise affecting structures like villages, in order to spawn smoothing areas.
		// Doing this for unloaded chunks causes a hang on load since getChunk is called by StructureManager.
		// BO4's/shadowgen avoid villages, so this method should never be called to fetch unloaded chunks that contain villages,
		// so we can skip noisegen affecting structures here.
		// *TODO: Do we need to avoid any noisegen affecting structures other than villages?

		ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
		ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);

		otgChunkGenerator.populateNoise(worldHeightCap, random, buffer, buffer.getChunkCoordinate(), structures, junctions);
		return buffer;
	}

	public ChunkAccess getChunkFromCache(ChunkCoordinate chunkCoord)
	{
		ChunkAccess cachedChunk = this.unloadedChunksCache.get(chunkCoord);
		if(cachedChunk != null)
		{
			return cachedChunk;
		} else {
			return null;
		}
	}

	public void fillWorldGenChunkFromShadowChunk(ChunkCoordinate chunkCoord, org.bukkit.generator.ChunkGenerator.ChunkData chunk, ChunkAccess cachedChunk)
	{
		// Re-use base terrain generated via shadowgen for worldgen.
		// TODO: Find some way to clone/swap chunk data efficiently,
		// like we do for forge with accesstransformers.
		/*
		((ProtoChunk)chunk).sections = ((ProtoChunk)cachedChunk).sections;
		((ProtoChunk)chunk).heightmaps = ((ProtoChunk)cachedChunk).heightmaps;
		((ProtoChunk)chunk).lights = ((ProtoChunk)cachedChunk).lights;
		*/
		CraftChunkData data = (CraftChunkData) chunk;
		for (int x = 0; x < Constants.CHUNK_SIZE; x++)
		{
			for (int z = 0; z < Constants.CHUNK_SIZE; z++)
			{
				int endY = cachedChunk.getHeight(Types.WORLD_SURFACE_WG, x, z);
				for (int y = 0; y <= endY; y++)
				{
					BlockPos pos = new BlockPos(x, y, z);
					data.setRegion(x, y, z, x + 1, y + 1, z + 1, cachedChunk.getBlockState(pos));
				}
			}
		}

		this.cacheHits++;
		//OTG.log(LogMarker.INFO, "Cache hit " + this.cacheHits);
		this.unloadedChunksCache.remove(chunkCoord);
	}

	public void fillWorldGenChunkFromShadowChunk(ChunkCoordinate chunkCoord, ChunkAccess chunk, ChunkAccess cachedChunk)
	{
		// TODO: This is experimental and may be slower than not cloning it
		try
		{
			sections.set((ProtoChunk) chunk, sections.get((ProtoChunk) cachedChunk));
			light.set((ProtoChunk) chunk, light.get((ProtoChunk) cachedChunk));
			heightMaps.set((ProtoChunk) chunk, heightMaps.get((ProtoChunk) cachedChunk));
		} catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}


		for (int x = 0; x < Constants.CHUNK_SIZE; x++)
		{
			for (int z = 0; z < Constants.CHUNK_SIZE; z++)
			{
				int endY = cachedChunk.getOrCreateHeightmapUnprimed(Types.WORLD_SURFACE_WG).getFirstAvailable(x, z);
				for (int y = 0; y <= endY; y++)
				{
					BlockPos pos = new BlockPos(x, y, z);
					chunk.setBlockState(pos, cachedChunk.getBlockState(pos), false);
				}
			}
		}

		this.cacheHits++;
		//OTG.log(LogMarker.INFO, "Cache hit " + this.cacheHits);
		this.unloadedChunksCache.remove(chunkCoord);
	}

	public void setChunkGenerated(ChunkCoordinate chunkCoord)
	{
		this.cacheMisses++;
		//OTG.log(LogMarker.INFO, "Cache miss " + + this.cacheMisses);
	}

	// Vanilla structure detection (avoidance)
	// Some vanilla structures use density based smoothing of terrain underneath, which is factored into noisegen.
	// Unfortunately this requires fetching structure data in a non-thread-safe manner, so we can't do async
	// chunkgen (base terrain) for these chunks and have to avoid them.

	public boolean checkHasVanillaStructureWithoutLoading(ServerLevel serverWorld, ChunkGenerator chunkGenerator, BiomeSource biomeProvider, StructureSettings dimensionStructuresSettings, ChunkCoordinate chunkCoordinate, ICachedBiomeProvider cachedBiomeProvider)
	{
		// Since we can't check for structure components/references, only structure starts,
		// we'll keep a safe distance away from any vanilla structure start points.
		int radiusInChunks = 5;
		ProtoChunk chunk;
		ChunkPos chunkpos;
		IBiome biome;
		ChunkCoordinate searchChunk;
		Boolean result;
		if (serverWorld.getServer().getWorldData().worldGenSettings().generateFeatures())
		{
			List<ChunkCoordinate> chunksToHandle = new ArrayList<ChunkCoordinate>();
			Map<ChunkCoordinate,Boolean> chunksHandled = new HashMap<ChunkCoordinate,Boolean>();
			synchronized(this.hasVanillaStructureChunkCache)
			{
				for (int cycle = 0; cycle <= radiusInChunks; ++cycle)
				{
					for (int xOffset = -cycle; xOffset <= cycle; ++xOffset)
					{
						for (int zOffset = -cycle; zOffset <= cycle; ++zOffset)
						{
							int distance = (int)Math.floor(Math.sqrt(Math.pow (xOffset, 2) + Math.pow (zOffset, 2)));
							if (distance == cycle)
							{
								searchChunk = ChunkCoordinate.fromChunkCoords(chunkCoordinate.getChunkX() + xOffset, chunkCoordinate.getChunkZ() + zOffset);
								result = this.hasVanillaStructureChunkCache.get(searchChunk);
								if(result != null)
								{
									if(result.booleanValue())
									{
										return true;
									}
								} else {
									chunksToHandle.add(searchChunk);
								}
							}
						}
					}
				}
			}
			for(ChunkCoordinate chunkToHandle : chunksToHandle)
			{
				chunk = new ProtoChunk(new ChunkPos(chunkToHandle.getChunkX(), chunkToHandle.getChunkZ()), null, serverWorld, serverWorld);
				chunkpos = chunk.getPos();

				// Borrowed from STRUCTURE_STARTS phase of chunkgen, only determines structure start point
				// based on biome and resource settings (distance etc). Does not plot any structure components.

				// TODO: Optimise this for biome lookups, fetch a whole region of noise biome info at once?
				biome = cachedBiomeProvider.getNoiseBiome((chunkpos.x << 2) + 2, (chunkpos.z << 2) + 2);
				for(Supplier<ConfiguredStructureFeature<?, ?>> supplier : ((PaperBiome)biome).getBiome().getGenerationSettings().structures())
				{
					// *TODO: Do we need to avoid any structures other than villages?
					if(supplier.get().feature instanceof VillageFeature)
					{
						if(hasStructureStart(supplier.get(), dimensionStructuresSettings, serverWorld.getSeed(), chunkpos))
						{
							chunksHandled.put(chunkToHandle, true);
							synchronized(this.hasVanillaStructureChunkCache)
							{
								this.hasVanillaStructureChunkCache.putAll(chunksHandled);
							}
							return true;
						}
					}
				}
				chunksHandled.put(chunkToHandle, false);
			}
			synchronized(this.hasVanillaStructureChunkCache)
			{
				this.hasVanillaStructureChunkCache.putAll(chunksHandled);
			}
		}
		return false;
	}

	// Taken from PillagerOutpostStructure.isNearVillage
	private static boolean hasStructureStart(ConfiguredStructureFeature<?, ?> structureFeature, StructureSettings dimensionStructuresSettings, long seed, ChunkPos chunkPos)
	{
		StructureFeatureConfiguration structureSeparationSettings = dimensionStructuresSettings.getConfig(structureFeature.feature);
		if (structureSeparationSettings != null)
		{
			WorldgenRandom sharedSeedRandom = new WorldgenRandom();
			ChunkPos chunkPosPotential = structureFeature.feature.getPotentialFeatureChunk(structureSeparationSettings, seed, sharedSeedRandom, chunkPos.x, chunkPos.z);
			if (
				chunkPos.x == chunkPosPotential.x &&
				chunkPos.z == chunkPosPotential.z
			) {
				return true;
			}
			return false;
		}
		return false;
	}

	// /otg mapterrain

	// /otg mapterrain fetches chunks in order to create a map of base terrain, without touching any of the caches or
	// resources used for worldgen or bo4 shadowgen, since the chunks aren't actually supposed to generate in the world.
	// We won't get any density based smoothing applied to noisegen for vanilla structures, but that's ok for /otg mapterrain.

	public PaperChunkBuffer getChunkWithoutLoadingOrCaching(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random random, ChunkCoordinate chunkCoordinate, LevelHeightAccessor level)
	{
		return getUnloadedChunk(otgChunkGenerator, worldHeightCap, random, chunkCoordinate, level);
	}

	// BO4's / Smoothing Areas

	// BO4's and smoothing areas may do material and height checks in unloaded chunks during decoration.
	// Shadowgen is used to do this without causing cascades. Shadowgenned chunks are requested on-demand for the worldgen thread (BO4's).

	private LocalMaterialData[] getBlockColumnInUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random worldRandom, int x, int z, LevelHeightAccessor level)
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

		ChunkAccess chunk = getChunkFromCache(chunkCoord);
		if (chunk == null)
		{
			// Generate a chunk without loading/decorating it
			chunk = getUnloadedChunk(otgChunkGenerator, worldHeightCap, worldRandom, chunkCoord, level).getChunk();
			this.unloadedChunksCache.put(chunkCoord, chunk);
		}

		cachedColumn = new LocalMaterialData[256];

		LocalMaterialData[] blocksInColumn = new LocalMaterialData[256];
		BlockState blockInChunk;
		for (short y = 0; y < 256; y++)
		{
			blockInChunk = chunk.getBlockState(new BlockPos(blockX, y, blockZ));
			if (blockInChunk != null)
			{
				blocksInColumn[y] = PaperMaterialData.ofBlockData(blockInChunk);
			} else {
				break;
			}
		}
		this.unloadedBlockColumnsCache.put(blockPos, cachedColumn);

		return blocksInColumn;
	}

	public LocalMaterialData getMaterialInUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random worldRandom, int x, int y, int z, LevelHeightAccessor level)
	{
		LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(otgChunkGenerator, worldHeightCap, worldRandom, x, z, level);
		return blockColumn[y];
	}

	public int getHighestBlockYInUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random worldRandom, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, LevelHeightAccessor level)
	{
		int height = -1;

		LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(otgChunkGenerator, worldHeightCap, worldRandom, x, z, level);
		PaperMaterialData material;
		boolean isLiquid;
		boolean isSolid;

		for (int y = 255; y >= 0; y--)
		{
			material = (PaperMaterialData) blockColumn[y];
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
}
