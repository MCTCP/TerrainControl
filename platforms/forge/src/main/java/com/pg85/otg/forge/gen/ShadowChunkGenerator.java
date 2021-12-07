package com.pg85.otg.forge.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import com.pg85.otg.core.gen.OTGChunkGenerator;
import com.pg85.otg.forge.biome.ForgeBiome;
import com.pg85.otg.forge.biome.OTGBiomeProvider;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.server.level.ServerLevel;

/**
 * Shadow chunk generation means generating base terrain for chunks
 * without using mc's world generation flow. OTG's chunkgenerator is
 * called internally to generate base terrain for dummy chunks in a
 * thread-safe/non-blocking way. Shadowgenned chunks are stored in a
 * fixed size FIFO cache, data is reused when base terraingen is requested
 * for those chunks via normal worldgen. Shadowgen is used for BO4's,
 * worker threads to speed up world generation and /otg mapterrain.
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
	private final FifoMap<ChunkCoordinate, ChunkAccess> unloadedChunksCache = new FifoMap<ChunkCoordinate, ChunkAccess>(512);
	private final FifoMap<ChunkCoordinate, Integer> hasVanillaStructureChunkCache = new FifoMap<ChunkCoordinate, Integer>(2048);
	private final FifoMap<ChunkCoordinate, Integer> hasVanillaNoiseStructureChunkCache = new FifoMap<ChunkCoordinate, Integer>(2048);

	private final Object workerLock = new Object();
	private final int maxConcurrent;
	private final Worker[] threads;
	private boolean threadsInitialized = false;
	private final LinkedList<ChunkCoordinate> chunksToLoad = new LinkedList<ChunkCoordinate>();
	private final int maxQueueSize = 512;
	private final ChunkCoordinate[] chunksBeingLoaded;
	private final int waitTimeInMS = 25;
	private final int idleTimeInMS = 50;
	@SuppressWarnings("unused")
	private int cacheHits = 0;
	@SuppressWarnings("unused")
	private int cacheMisses = 0;

	public ShadowChunkGenerator(int maxConcurrentThreads)
	{
		this.maxConcurrent = maxConcurrentThreads;
		this.threads = new Worker[this.maxConcurrent];
		// chunksBeingLoaded[maxConcurrent] means worldgen thread, not a worker thread.
		this.chunksBeingLoaded = new ChunkCoordinate[this.maxConcurrent + 1];
	}

	// Called on world unload to stop threads and release resources.
	public void stopWorkerThreads()
	{
		if(this.maxConcurrent > 0)
		{
			for(int i = 0; i < this.maxConcurrent; i++)
			{
				if(this.threads[i] != null)
				{
					this.threads[i].stop();
				}
			}
		}
	}

	// Whenever MC requests noisegen/base terrain gen for a chunk, it also exposes a cache of chunks currently loaded/queued.
	// These chunks are highly likely to be requested next, so we can filter out any that need noisegen/base terrain gen and
	// pre-emptively generate and cache them asynchronously. When MC requests those chunks a moment later as part of worldgen,
	// we return the async generated chunk data.
	public void queueChunksForWorkerThreads(WorldGenRegion worldGenRegion, StructureFeatureManager manager, ChunkAccess chunk, ChunkGenerator chunkGenerator, OTGBiomeProvider biomeProvider, OTGChunkGenerator otgChunkGenerator, StructureSettings dimensionStructuresSettings, int worldHeightCap)
	{
		if(this.maxConcurrent > 0)
		{
			if(!this.threadsInitialized)
			{
				for(int i = 0; i < this.maxConcurrent; i++)
				{
					@SuppressWarnings("deprecation")
					Worker thread = this.new Worker(i, this.unloadedChunksCache, this.chunksToLoad, this.chunksBeingLoaded, worldGenRegion.getLevel(), chunkGenerator, biomeProvider, otgChunkGenerator, dimensionStructuresSettings, worldHeightCap);
					this.threads[i] = thread;
					thread.start(worldGenRegion.getRandom());
				}
				this.threadsInitialized = true;
			}
			synchronized(this.workerLock)
			{
				if(this.chunksToLoad.size() == 0)
				{
					//OTG.log(LogMarker.INFO, "Fetching chunks for async chunkgen");
					for(ChunkAccess wgrChunk : worldGenRegion.cache)
					{
						ChunkCoordinate wgrChunkCoord = ChunkCoordinate.fromChunkCoords(wgrChunk.getPos().x, wgrChunk.getPos().z);
						if(wgrChunk != chunk && !wgrChunk.getStatus().isOrAfter(ChunkStatus.NOISE))
						{
							if (!this.unloadedChunksCache.containsKey(wgrChunkCoord))
							{
								boolean bFound = false;
								for(int i = 0; i < this.chunksBeingLoaded.length; i++)
								{
									if(this.chunksBeingLoaded[i] == wgrChunkCoord)
									{
										bFound = true;
										break;
									}
								}
								if(!bFound)
								{
									// TODO: Queue order shouldn't really matter bc
									// of the way maxQueueSize is enforced here.
									// Might affect cache hits/misses and waits tho, test?
									this.chunksToLoad.addFirst(wgrChunkCoord);
									if(this.chunksToLoad.size() == this.maxQueueSize)
									{
										break;
									}
								}
							}
						}
					};
				}
			}
		}
	}

	private ForgeChunkBuffer getUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random random, ChunkCoordinate chunkCoordinate, ServerLevel level)
	{
		ProtoChunk chunk = new ProtoChunk(new ChunkPos(chunkCoordinate.getChunkX(), chunkCoordinate.getChunkZ()), null, level);
		ForgeChunkBuffer buffer = new ForgeChunkBuffer(chunk);

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

	public ChunkAccess getChunkWithWait(ChunkCoordinate chunkCoord)
	{
		// Fetch the chunk if it is cached, otherwise check if no other thread
		// is generating the chunk. If not, claim the chunk and generate it.
		// If so, wait for the other thread to finish.
		synchronized(this.workerLock)
		{
			ChunkAccess cachedChunk = this.unloadedChunksCache.get(chunkCoord);
			if(cachedChunk != null)
			{
				return cachedChunk;
			} else {
				// If a chunk is in unloadedChunksCache but is null, it's in a chunk that
				// shouldn't be generated async due to a vanilla structure start nearby.
				if(this.unloadedChunksCache.containsKey(chunkCoord))
				{
					this.unloadedChunksCache.remove(chunkCoord);
					this.chunksToLoad.remove(chunkCoord);
					// MaxConcurrent means worldgen thread, not a worker thread.
					this.chunksBeingLoaded[this.maxConcurrent] = chunkCoord;
					return null;
				} else {
					boolean bFound = false;
					for(int i = 0; i < this.chunksBeingLoaded.length; i++)
					{
						if(this.chunksBeingLoaded[i] == chunkCoord)
						{
							bFound = true;
							break;
						}
					}
					if(!bFound)
					{
						this.chunksToLoad.remove(chunkCoord);
						// MaxConcurrent means worldgen thread, not a worker thread.
						this.chunksBeingLoaded[this.maxConcurrent] = chunkCoord;
						return null;
					}
				}
			}
		}

		// A worker thread is generating the chunk, wait.

		while(true)
		{
			try {
				//OTG.log(LogMarker.INFO, "Waiting for chunk");
				// TODO: If a worker thread is stuck or crashed, this may wait indefinitely.
				Thread.sleep(this.waitTimeInMS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized(this.workerLock)
			{
				ChunkAccess cachedChunk = this.unloadedChunksCache.get(chunkCoord);
				if(cachedChunk != null)
				{
					return cachedChunk;
				} else {
					// If a chunk is in unloadedChunksCache but is null, it's in a chunk that
					// shouldn't be generated async due to a vanilla structure start nearby.
					if(this.unloadedChunksCache.containsKey(chunkCoord))
					{
						this.unloadedChunksCache.remove(chunkCoord);
						this.chunksToLoad.remove(chunkCoord);
						// MaxConcurrent means worldgen thread, not a worker thread.
						this.chunksBeingLoaded[this.maxConcurrent] = chunkCoord;
						return null;
					}
				}
			}
		}
	}

	public void fillWorldGenChunkFromShadowChunk(ChunkAccess chunk, ChunkAccess cachedChunk)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);
		// Re-use base terrain generated via shadowgen for worldgen.
		((ProtoChunk)chunk).sections = ((ProtoChunk)cachedChunk).sections;
		((ProtoChunk)chunk).heightmaps = ((ProtoChunk)cachedChunk).heightmaps;
		((ProtoChunk)chunk).lights = ((ProtoChunk)cachedChunk).lights;
		this.cacheHits++;
		//OTG.log(LogMarker.INFO, "Cache hit " + this.cacheHits);
		synchronized(this.workerLock)
		{
			this.unloadedChunksCache.remove(chunkCoord);
		}
	}

	public void setChunkGenerated(ChunkCoordinate chunkCoord)
	{
		this.cacheMisses++;
		//OTG.log(LogMarker.INFO, "Cache miss " + + this.cacheMisses);
		synchronized(workerLock)
		{
			// Zero index, so MaxConcurrent means worldgen thread, not a worker thread.
			this.chunksBeingLoaded[this.maxConcurrent] = null;
			this.chunksToLoad.remove(chunkCoord);
		}
	}

	// Vanilla structure detection (avoidance)
	// Some vanilla structures use density based smoothing of terrain underneath, which is factored into noisegen.
	// Unfortunately this requires fetching structure data in a non-thread-safe manner, so we can't do async
	// chunkgen (base terrain) for these chunks and have to avoid them.

	public boolean checkHasVanillaStructureWithoutLoading(ServerLevel serverWorld, ChunkGenerator chunkGenerator, OTGBiomeProvider biomeProvider, StructureSettings dimensionStructuresSettings, ChunkCoordinate chunkCoordinate, ICachedBiomeProvider cachedBiomeProvider, boolean noiseAffectingStructuresOnly)
	{
		// Since we can't check for structure components/references, only structure starts,
		// we'll keep a safe distance away from any vanilla structure start points.
		int radiusInChunks = 5;
		ProtoChunk chunk;
		ChunkPos chunkpos;
		IBiome biome;
		if (serverWorld.getServer().getWorldData().worldGenSettings().generateFeatures())
		{
			List<ChunkCoordinate> chunksToHandle = new ArrayList<>();
			Map<ChunkCoordinate,Integer> chunksHandled = new HashMap<>();
			if(noiseAffectingStructuresOnly)
			{
				synchronized(this.hasVanillaNoiseStructureChunkCache)
				{
					if(checkHasVanillaStructureWithoutLoadingCache(this.hasVanillaNoiseStructureChunkCache, chunkCoordinate, radiusInChunks, chunksToHandle))
					{
						return true;
					}
				}
			} else {
				synchronized(this.hasVanillaStructureChunkCache)
				{
					if(checkHasVanillaStructureWithoutLoadingCache(this.hasVanillaStructureChunkCache, chunkCoordinate, radiusInChunks, chunksToHandle))
					{
						return true;
					}
				}
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<String>[] structuresPerDistance = new ArrayList[radiusInChunks];
			structuresPerDistance[4] = new ArrayList<String>(Arrays.asList(
				new String[] {
					"minecraft:village",
					"minecraft:endcity",
					"minecraft:bastion_remnant",
					"minecraft:monument",
					"minecraft:mansion"
				}
			));
			structuresPerDistance[3] = new ArrayList<String>(Arrays.asList(new String[]{}));
			structuresPerDistance[2] = new ArrayList<String>(Arrays.asList(new String[]{}));
			structuresPerDistance[1] = new ArrayList<String>(Arrays.asList(
				new String[] {
					"minecraft:jungle_pyramid",
					"minecraft:desert_pyramid",
					"minecraft:ruined_portal",
					"minecraft:swamp_hut",
					"minecraft:igloo",
					"minecraft:shipwreck",
					"minecraft:pillager_outpost",
					"minecraft:ocean_ruin"
				}
			));
			structuresPerDistance[0] = new ArrayList<String>(Arrays.asList(new String[]{}));
		
			for(ChunkCoordinate chunkToHandle : chunksToHandle)
			{
				chunk = new ProtoChunk(new ChunkPos(chunkToHandle.getChunkX(), chunkToHandle.getChunkZ()), null, serverWorld);
				chunkpos = chunk.getPos();
				int distance = (int)Math.floor(Math.sqrt(Math.pow (chunkToHandle.getChunkX() - chunkCoordinate.getChunkX(), 2) + Math.pow (chunkToHandle.getChunkZ() - chunkCoordinate.getChunkZ(), 2)));
				
				// Borrowed from STRUCTURE_STARTS phase of chunkgen, only determines structure start point
				// based on biome and resource settings (distance etc). Does not plot any structure components.

				// TODO: Optimise this for biome lookups, fetch a whole region of noise biome info at once?
				biome = cachedBiomeProvider.getNoiseBiome((chunkpos.x << 2) + 2, (chunkpos.z << 2) + 2);
				for(Supplier<ConfiguredStructureFeature<?, ?>> supplier : ((ForgeBiome)biome).getBiomeBase().getGenerationSettings().structures())
				{
					ConfiguredStructureFeature<?, ?> structure = supplier.get();
					if(
						structure.feature.step() == Decoration.SURFACE_STRUCTURES &&
						(
							!noiseAffectingStructuresOnly ||
							StructureFeature.NOISE_AFFECTING_FEATURES.contains(structure.feature)
						)
					)
					{
						ResourceLocation structureRegistryKey = ForgeRegistries.STRUCTURE_FEATURES.getKey(structure.feature);
						String structureRegistryName = structureRegistryKey.toString();
						if(!structureRegistryKey.getNamespace().equals("minecraft"))
						{
							// For modded structures, use a default radius
							int moddedStructuresDefaultRadius = 1;
							if(hasStructureStart(structure, dimensionStructuresSettings, serverWorld.getSeed(), chunkpos))
							{
								chunksHandled.put(chunkToHandle, new Integer(moddedStructuresDefaultRadius));
								if(moddedStructuresDefaultRadius >= distance)
								{
									if(noiseAffectingStructuresOnly)
									{
										synchronized(this.hasVanillaNoiseStructureChunkCache)
										{
											this.hasVanillaNoiseStructureChunkCache.putAll(chunksHandled);
										}
									} else {
										synchronized(this.hasVanillaStructureChunkCache)
										{
											this.hasVanillaStructureChunkCache.putAll(chunksHandled);
										}
									}									
									return true;
								}
							}
						} else {
							for(int i = structuresPerDistance.length - 1; i > 0; i--)
							{
								ArrayList<String> structuresAtDistance = structuresPerDistance[i];
								if(structuresAtDistance.contains(structureRegistryName))
								{
									if(hasStructureStart(structure, dimensionStructuresSettings, serverWorld.getSeed(), chunkpos))
									{
										chunksHandled.put(chunkToHandle, new Integer(i));
										if(i >= distance)
										{
											if(noiseAffectingStructuresOnly)
											{
												synchronized(this.hasVanillaNoiseStructureChunkCache)
												{													
													this.hasVanillaNoiseStructureChunkCache.putAll(chunksHandled);
												}
											} else {
												synchronized(this.hasVanillaStructureChunkCache)
												{
													this.hasVanillaStructureChunkCache.putAll(chunksHandled);
												}
											}							
											return true;
										}
									}
									break;
								}
							}
						}
					}
				}
				chunksHandled.putIfAbsent(chunkToHandle, new Integer(0));
			}
			if(noiseAffectingStructuresOnly)
			{
				synchronized(this.hasVanillaNoiseStructureChunkCache)
				{
					this.hasVanillaNoiseStructureChunkCache.putAll(chunksHandled);
				}
			} else {
				synchronized(this.hasVanillaStructureChunkCache)
				{
					this.hasVanillaStructureChunkCache.putAll(chunksHandled);
				}
			}
		}
		return false;
	}
	
	private boolean checkHasVanillaStructureWithoutLoadingCache(FifoMap<ChunkCoordinate, Integer> cache, ChunkCoordinate chunkCoordinate, int radiusInChunks, List<ChunkCoordinate> chunksToHandle)
	{
		for (int cycle = 0; cycle < radiusInChunks; ++cycle)
		{
			for (int xOffset = -cycle; xOffset <= cycle; ++xOffset)
			{
				for (int zOffset = -cycle; zOffset <= cycle; ++zOffset)
				{
					int distance = (int)Math.floor(Math.sqrt(Math.pow (xOffset, 2) + Math.pow (zOffset, 2)));
					if (distance == cycle)
					{
						ChunkCoordinate searchChunk = ChunkCoordinate.fromChunkCoords(chunkCoordinate.getChunkX() + xOffset, chunkCoordinate.getChunkZ() + zOffset);
						Integer result = cache.get(searchChunk);
						if(result != null)
						{
							if(result.intValue() > 0 && result.intValue() >= distance)
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
		return false;
	}

	// Taken from PillagerOutpostStructure.isNearVillage
	private boolean hasStructureStart(ConfiguredStructureFeature<?, ?> structureFeature, StructureSettings dimensionStructuresSettings, long seed, ChunkPos chunkPos)
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

	public ForgeChunkBuffer getChunkWithoutLoadingOrCaching(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random random, ChunkCoordinate chunkCoordinate, ServerLevel level)
	{
		return getUnloadedChunk(otgChunkGenerator, worldHeightCap, random, chunkCoordinate, level);
	}

	// BO4's / Smoothing Areas

	// BO4's and smoothing areas may do material and height checks in unloaded chunks during decoration.
	// Shadowgen is used to do this without causing cascades. Shadowgenned chunks are requested on-demand for the worldgen thread (BO4's).
	// Async worker threads may also pre-emptively shadowgen and cache unloaded chunks, which speeds up base terrain generation but also BO4's.
	// Note: BO4's are always processed on the worldgen thread, never on a worker thread, since they are not a part of base terrain generation.

	private LocalMaterialData[] getBlockColumnInUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random worldRandom, int x, int z, ServerLevel level)
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

		ChunkAccess chunk = this.getChunkWithWait(chunkCoord);
		if (chunk == null)
		{
			// Generate a chunk without loading/decorating it
			chunk = getUnloadedChunk(otgChunkGenerator, worldHeightCap, worldRandom, chunkCoord, level).getChunk();
			synchronized(this.workerLock)
			{
				this.unloadedChunksCache.put(chunkCoord, chunk);
				// Zero index, so MaxConcurrent means worldgen thread, not a worker thread.
				this.chunksBeingLoaded[this.maxConcurrent] = null;
			}
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
		this.unloadedBlockColumnsCache.put(blockPos, cachedColumn);

		return blocksInColumn;
	}

	public LocalMaterialData getMaterialInUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random worldRandom, int x, int y, int z, ServerLevel level)
	{
		LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(otgChunkGenerator, worldHeightCap, worldRandom, x, z, level);
		return blockColumn[y];
	}

	public int getHighestBlockYInUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random worldRandom, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, ServerLevel level)
	{
		int height = -1;

		LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(otgChunkGenerator, worldHeightCap, worldRandom, x, z, level);
		ForgeMaterialData material;
		boolean isLiquid;
		boolean isSolid;

		for (int y = 255; y >= 0; y--)
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

	// Async Worker for generating chunks up to ChunkStatus.NOISE.
	// This is only used for chunks that don't require density based
	// smoothing for vanilla structures, since that cannot be done
	// in a thread-safe/non-blocking manner.

	private class Worker implements Runnable
	{
		private Thread runner;
		private boolean stop = false;
		private Random worldRandom;

		private final int index;
		private final FifoMap<ChunkCoordinate, ChunkAccess> unloadedChunksCache;
		private final List<ChunkCoordinate> chunksToLoad;
		private final ChunkCoordinate[] chunksBeingLoaded;
		private final ServerLevel serverWorld;
		private final ChunkGenerator chunkGenerator;
		private final OTGBiomeProvider biomeProvider;
		private final OTGChunkGenerator otgChunkGenerator;
		private final StructureSettings dimensionStructuresSettings;
		private final int worldHeightCap;

		Worker(int index, FifoMap<ChunkCoordinate, ChunkAccess> unloadedChunksCache, List<ChunkCoordinate> chunksToLoad, ChunkCoordinate[] chunksBeingLoaded, ServerLevel serverWorld, ChunkGenerator chunkGenerator, OTGBiomeProvider biomeProvider, OTGChunkGenerator otgChunkGenerator, StructureSettings dimensionStructuresSettings, int worldHeightCap)
		{
			this.index = index;
			this.unloadedChunksCache = unloadedChunksCache;
			this.chunksToLoad = chunksToLoad;
			this.chunksBeingLoaded = chunksBeingLoaded;
			this.serverWorld = serverWorld;
			this.chunkGenerator = chunkGenerator;
			this.biomeProvider  = biomeProvider;
			this.otgChunkGenerator = otgChunkGenerator;
			this.dimensionStructuresSettings = dimensionStructuresSettings;
			this.worldHeightCap = worldHeightCap;
		}

		public void start(Random worldRandom)
		{
			this.runner = new Thread(this);
			this.runner.start();
			this.worldRandom = worldRandom;
		}

		public void stop()
		{
			this.stop = true;
		}

		@Override
		public void run()
		{
			// Process chunks if any are in the queue,
			// otherwise wait for the queue to be filled.
			while(true)
			{
				if(this.stop)
				{
					this.stop = false;
					return;
				}

				ChunkCoordinate coords = null;
				int sizeLeft;
				synchronized(workerLock)
				{
					sizeLeft = this.chunksToLoad.size();
					if(sizeLeft > 0)
					{
						coords = this.chunksToLoad.remove(sizeLeft - 1);
						this.chunksBeingLoaded[this.index] = coords;
					}
				}
				if(coords != null)
				{
					if(!checkHasVanillaStructureWithoutLoading(this.serverWorld, this.chunkGenerator, this.biomeProvider, this.dimensionStructuresSettings, coords, this.otgChunkGenerator.getCachedBiomeProvider(), true))
					{
						// Generate a chunk without loading/decorating it.
						ChunkAccess cachedChunk = getUnloadedChunk(this.otgChunkGenerator, this.worldHeightCap, this.worldRandom, coords, this.serverWorld.getLevel()).getChunk();
						synchronized(workerLock)
						{
							this.unloadedChunksCache.put(coords, cachedChunk);
							this.chunksBeingLoaded[this.index] = null;
						}
					} else {
						synchronized(workerLock)
						{
							// This chunk should not be shadowgenned, add it
							// to the unloadedChunksCache as null so workers
							// avoid it and the worldgen thread takes care
							// of it in getChunkWithWait().
							this.unloadedChunksCache.put(coords, null);
							this.chunksBeingLoaded[this.index] = null;
						}
					}
				} else {
					try {
						//OTG.log(LogMarker.INFO, "Worker " + this.index + " idle");
						Thread.sleep(idleTimeInMS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
