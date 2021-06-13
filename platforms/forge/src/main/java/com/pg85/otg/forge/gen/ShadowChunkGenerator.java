package com.pg85.otg.forge.gen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.gen.OTGChunkGenerator;
import com.pg85.otg.util.BlockPos2D;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.block.BlockState;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.VillageStructure;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;

/**
 * Shadow chunk generation means generating base terrain for chunks
 * without using mc's world generation flow. OTG's chunkgenerator is 
 * called internally to generate base terrain for dummy chunks in a 
 * thread-safe way. Shadowgenned chunks are stored in a fixed size
 * FIFO cache, data is reused when base terraingen is requested
 * for those chunks via normal worldgen. Shadowgen is used for BO4's,
 * worker threads to speed up world generation and /otg mapterrain.
 * 
 * Shadowgen can only be done for chunks that don't contain vanilla strucutures, 
 * since those structures may use density based smoothing applied during noisegen, 
 * which unfortunately is done in a non-thread-safe/blocking manner, necessitating
 * the use of a WorldGenRegion.
 */
public class ShadowChunkGenerator
{
	// TODO: Add a setting to the worldconfig for the size of these caches?
	private final FifoMap<BlockPos2D, LocalMaterialData[]> unloadedBlockColumnsCache = new FifoMap<BlockPos2D, LocalMaterialData[]>(1024);
	private final FifoMap<ChunkCoordinate, IChunk> unloadedChunksCache = new FifoMap<ChunkCoordinate, IChunk>(512);
	private final FifoMap<ChunkCoordinate, Boolean> hasVanillaStructureChunkCache = new FifoMap<ChunkCoordinate, Boolean>(2048);	

	private final Object workerLock = new Object();
	private final int maxConcurrent;
	private final Worker[] threads;
	private boolean threadsInitialized = false;
	private final LinkedList<ChunkCoordinate> chunksToLoad = new LinkedList<ChunkCoordinate>();
	private final int maxQueueSize = 512;
	private final ChunkCoordinate[] chunksBeingLoaded;

	private static int cacheHits = 0;
	private static int cacheMisses = 0;

	public ShadowChunkGenerator(int maxConcurrentThreads)
	{
		this.maxConcurrent = maxConcurrentThreads;
		this.threads = new Worker[this.maxConcurrent];
		// chunksBeingLoaded[maxConcurrent] means worldgen thread, not a worker thread.
		this.chunksBeingLoaded = new ChunkCoordinate[this.maxConcurrent + 1];
	}

	public void stopWorkerThreads()
	{
		if(this.maxConcurrent > 0)
		{
			for(int i = 0; i < maxConcurrent; i++)
			{
				Worker thread = threads[i];
				thread.stop();
			}
		}
	}
	
	// Whenever MC requests noisegen/base terrain gen for a chunk, it also exposes a cache of chunks currently loaded/queued.
	// These chunks are highly likely to be requested next, so we can filter out any that need noisegen/base terrain gen and 
	// pre-emptively generate and cache them asynchronously. When MC requests those chunks a moment later as part of worldgen,
	// we return the async generated chunk data.
	public void queueChunksForWorkerThreads(WorldGenRegion worldGenRegion, StructureManager manager, IChunk chunk, ChunkGenerator chunkGenerator, BiomeProvider biomeProvider, OTGChunkGenerator otgChunkGenerator, DimensionStructuresSettings dimensionStructuresSettings, int worldHeightCap)
	{
		if(this.maxConcurrent > 0)
		{
			if(!this.threadsInitialized)
			{
				for(int i = 0; i < this.maxConcurrent; i++)
				{
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
					for(IChunk wgrChunk : worldGenRegion.cache)
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
								if(
									!bFound && 
									!this.chunksToLoad.contains(wgrChunkCoord)
								)
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
	
	private ForgeChunkBuffer getUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random random, ChunkCoordinate chunkCoordinate)
	{
		ChunkPrimer chunk = new ChunkPrimer(new ChunkPos(chunkCoordinate.getChunkX(), chunkCoordinate.getChunkZ()), null);
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
	
	public IChunk getChunkWithWait(ChunkCoordinate chunkCoord)
	{
		// Fetch the chunk if it is cached, otherwise check if no other thread
		// is generating the chunk. If not, claim the chunk and generate it.
		// If so, wait for the other thread to finish.
		synchronized(this.workerLock)
		{
			IChunk cachedChunk = this.unloadedChunksCache.get(chunkCoord);
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
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
			synchronized(this.workerLock)
			{
				IChunk cachedChunk = this.unloadedChunksCache.get(chunkCoord);
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

	public void fillWorldGenChunkFromShadowChunk(IChunk chunk, IChunk cachedChunk)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);
		// Re-use base terrain generated via shadowgen for worldgen.
		((ChunkPrimer)chunk).sections = ((ChunkPrimer)cachedChunk).sections;
		((ChunkPrimer)chunk).heightmaps = ((ChunkPrimer)cachedChunk).heightmaps;			
		((ChunkPrimer)chunk).lights = ((ChunkPrimer)cachedChunk).lights;
		cacheHits++;
		//OTG.log(LogMarker.INFO, "Cache hit " + cacheHits);
		synchronized(this.workerLock)
		{
			this.unloadedChunksCache.remove(chunkCoord);
		}
	}
	
	public void setChunkGenerated(ChunkCoordinate chunkCoord)
	{
		cacheMisses++;
		//OTG.log(LogMarker.INFO, "Cache miss " + + cacheMisses);
		synchronized(workerLock)
		{
			// Zero index, so MaxConcurrent means worldgen thread, not a worker thread.
			this.chunksBeingLoaded[maxConcurrent] = null;
			this.chunksToLoad.remove(chunkCoord);
		}
	}
	
	// Vanilla structure detection (avoidance)
	// Some vanilla structures use density based smoothing of terrain underneath, which is factored into noisegen.
	// Unfortunately this requires fetching structure data in a non-thread-safe manner, so we can't do async 
	// chunkgen (base terrain) for these chunks and have to avoid them.
	
	public boolean checkHasVanillaStructureWithoutLoading(ServerWorld serverWorld, ChunkGenerator chunkGenerator, BiomeProvider biomeProvider, DimensionStructuresSettings dimensionStructuresSettings, ChunkCoordinate chunkCoordinate)
	{
		// Since we can't check for structure components/references, only structure starts,  
		// we'll keep a safe distance away from any vanilla structure start points.
		int radiusInChunks = 5;
		ChunkPrimer chunk;
		ChunkPos chunkpos;
		Biome biome;
		ChunkCoordinate searchChunk;
		Boolean result;
		if (serverWorld.getServer().getWorldData().worldGenSettings().generateFeatures())
		{		
			List<ChunkCoordinate> chunksToHandle = new ArrayList<ChunkCoordinate>();
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
				chunk = new ChunkPrimer(new ChunkPos(chunkToHandle.getChunkX(), chunkToHandle.getChunkZ()), null);
				chunkpos = chunk.getPos();

				// Borrowed from STRUCTURE_STARTS phase of chunkgen, only determines structure start point
				// based on biome and resource settings (distance etc). Does not plot any structure components.
				
				biome = biomeProvider.getNoiseBiome((chunkpos.x << 2) + 2, 0, (chunkpos.z << 2) + 2);
				for(Supplier<StructureFeature<?, ?>> supplier : biome.getGenerationSettings().structures())
				{
					// *TODO: Do we need to avoid any structures other than villages?
					if(supplier.get().feature instanceof VillageStructure)
					{
						if(hasStructureStart(supplier.get(), dimensionStructuresSettings, serverWorld.registryAccess(), serverWorld.structureFeatureManager(), chunk, serverWorld.getStructureManager(), chunkGenerator, biomeProvider, serverWorld.getSeed(), chunkpos, biome))
						{
							synchronized(this.hasVanillaStructureChunkCache)
							{
								this.hasVanillaStructureChunkCache.put(chunkToHandle, new Boolean(true));
							}
							return true;
						}
					}
				}
				synchronized(this.hasVanillaStructureChunkCache)
				{
					this.hasVanillaStructureChunkCache.put(chunkToHandle, new Boolean(false));
				}
			}
		}
		return false;
	}
	
	// Taken from PillagerOutpostStructure.isNearVillage
	private static boolean hasStructureStart(StructureFeature<?, ?> structureFeature, DimensionStructuresSettings dimensionStructuresSettings, DynamicRegistries dynamicRegistries, StructureManager structureManager, IChunk chunk, TemplateManager templateManager, ChunkGenerator chunkGenerator, BiomeProvider biomeProvider, long seed, ChunkPos chunkPos, Biome biome)
	{
		StructureSeparationSettings structureSeparationSettings = dimensionStructuresSettings.getConfig(structureFeature.feature);
		if (structureSeparationSettings != null)
		{
			SharedSeedRandom sharedSeedRandom = new SharedSeedRandom();
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
	
	public ForgeChunkBuffer getChunkWithoutLoadingOrCaching(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random random, ChunkCoordinate chunkCoordinate)
	{
		return getUnloadedChunk(otgChunkGenerator, worldHeightCap, random, chunkCoordinate);
	}
	
	// BO4's / Smoothing Areas

	// BO4's and smoothing areas may do material and height checks in unloaded chunks during population/decoration.
	// Shadowgen is used to do this without causing cascades. Shadowgenned chunks are requested on-demand for the worldgen thread (BO4's).
	// Async worker threads may also pre-emptively shadowgen and cache unloaded chunks, which speeds up base terrain generation but also BO4's. 
	// Note: BO4's are always processed on the worldgen thread, never on a worker thread, since they are not a part of base terrain generation.
	
	private LocalMaterialData[] getBlockColumnInUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random worldRandom, int x, int z)
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

		IChunk chunk = this.getChunkWithWait(chunkCoord);
		if (chunk == null)
		{
			// Generate a chunk without loading/populating it
			chunk = getUnloadedChunk(otgChunkGenerator, worldHeightCap, worldRandom, chunkCoord).getChunk();
			synchronized(this.workerLock)
			{
				this.unloadedChunksCache.put(chunkCoord, chunk);
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

	LocalMaterialData getMaterialInUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random worldRandom, int x, int y, int z)
	{
		LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(otgChunkGenerator, worldHeightCap, worldRandom, x, z);
		return blockColumn[y];
	}

	int getHighestBlockYInUnloadedChunk(OTGChunkGenerator otgChunkGenerator, int worldHeightCap, Random worldRandom, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow)
	{
		int height = -1;

		LocalMaterialData[] blockColumn = getBlockColumnInUnloadedChunk(otgChunkGenerator, worldHeightCap, worldRandom, x, z);
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
	
	public class Worker implements Runnable
	{
		private boolean stop = false;
		private Thread runner;
		private final int index;
		private Random worldRandom;
		private final FifoMap<ChunkCoordinate, IChunk> unloadedChunksCache;
		private final List<ChunkCoordinate> chunksToLoad;
		private final ChunkCoordinate[] chunksBeingLoaded;
		private final ServerWorld serverWorld;
	    private final ChunkGenerator chunkGenerator;
	    private final BiomeProvider biomeProvider;
	    private final OTGChunkGenerator otgChunkGenerator;
	    private final DimensionStructuresSettings dimensionStructuresSettings;
	    private final int worldHeightCap;
		
	    public Worker(int index, FifoMap<ChunkCoordinate, IChunk> unloadedChunksCache, List<ChunkCoordinate> chunksToLoad, ChunkCoordinate[] chunksBeingLoaded, ServerWorld serverWorld, ChunkGenerator chunkGenerator, BiomeProvider biomeProvider, OTGChunkGenerator otgChunkGenerator, DimensionStructuresSettings dimensionStructuresSettings, int worldHeightCap)
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
	    			if(!checkHasVanillaStructureWithoutLoading(this.serverWorld, this.chunkGenerator, this.biomeProvider, this.dimensionStructuresSettings, coords))
	    			{
						// Generate a chunk without loading/populating it.	    				
	    				IChunk cachedChunk = getUnloadedChunk(this.otgChunkGenerator, this.worldHeightCap, this.worldRandom, coords).getChunk();
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
	    				//OTG.log(LogMarker.INFO, "Thread " + this.index + " idle");
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	    		}
	    	}
	    }
	}
}
