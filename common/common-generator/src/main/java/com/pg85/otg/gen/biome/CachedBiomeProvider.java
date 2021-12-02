package com.pg85.otg.gen.biome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.ILayerSource;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.helpers.MathHelper;

/**
 * A cache used throughout an entire session, so that base
 * terrain generation, carvers and decoration can fetch biomes
 * more efficiently. 
 */
public class CachedBiomeProvider implements ICachedBiomeProvider
{
	static int cacheHits = 0;
	static int smallCacheHits = 0;
	@SuppressWarnings("unused")
	private final ILogger logger;
	
	private final long seed;
	private final ILayerSource biomeProvider;
	private final IBiome[] biomesById;
	
	private final Object lock = new Object();
	private boolean locked = false;
	private boolean locked2 = false;
	private final FifoMap<ChunkCoordinate, IBiome[]> biomesCache = new FifoMap<>(256);
	private final FifoMap<ChunkCoordinate, IBiomeConfig[]> biomeConfigsCache = new FifoMap<>(256);
	
	private final Object noiseLock = new Object();
	private final FifoMap<ChunkCoordinate, IBiomeConfig[]> noiseBiomeConfigsCache = new FifoMap<>(1024);	

	public CachedBiomeProvider(long seed, ILayerSource biomeProvider, IBiome[] biomesById, ILogger logger)
	{
		this.seed = seed;
		this.biomeProvider = biomeProvider;
		this.biomesById = biomesById;
		this.logger = logger;
	}

	// Used by any method that can preemptively request a chunk of biomeconfigs,
	// rather than making separate requests for each column. 
	// TODO: Allow regions rather than chunks.
	@Override
	public IBiomeConfig[] getBiomeConfigsForChunk(ChunkCoordinate chunkCoord)
	{
		IBiomeConfig[] biomeConfigs;
		synchronized(this.lock)
		{
			this.locked = true;
			biomeConfigs = this.biomeConfigsCache.get(chunkCoord);
			if(biomeConfigs == null)
			{
				IBiome[] biomes = new IBiome[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
				biomeConfigs = new IBiomeConfig[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
				int biomeId;
				IBiome biome;
				for (int x = 0; x < Constants.CHUNK_SIZE; x++)
				{
					for (int z = 0; z < Constants.CHUNK_SIZE; z++)
					{
						// TODO: Technically, we should be providing the hashed seed here. Perhaps this may work for the time being?
						biomeId = BiomeInterpolator.getId(this.seed, x + chunkCoord.getBlockX(), 0, z + chunkCoord.getBlockZ(), this.biomeProvider);
						biome = this.biomesById[biomeId];
						biomes[x * Constants.CHUNK_SIZE + z] = biome;
						biomeConfigs[x * Constants.CHUNK_SIZE + z] = biome.getBiomeConfig();
					}
				}
				this.biomesCache.put(chunkCoord, biomes);
				this.biomeConfigsCache.put(chunkCoord, biomeConfigs);
			} else {
				cacheHits++;
				//logger.log(LogLevel.INFO, LogCategory.MAIN, "Cache hit " + cacheHits);
			}
		}
		this.locked = false;
		return biomeConfigs;		
	}
	
	// Used by any method that can preemptively request a chunk of biomeconfigs,
	// rather than making separate requests for each column.
	@Override
	public IBiome[] getBiomesForChunk(ChunkCoordinate chunkCoord)
	{
		IBiome[] biomes;
		synchronized(this.lock)
		{
			this.locked = true;
			biomes = this.biomesCache.get(chunkCoord);
			if(biomes == null)
			{
				biomes = new IBiome[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
				IBiomeConfig[]  biomeConfigs = new IBiomeConfig[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
				int biomeId;
				IBiome biome;
				for (int x = 0; x < Constants.CHUNK_SIZE; x++)
				{
					for (int z = 0; z < Constants.CHUNK_SIZE; z++)
					{
						// TODO: Technically, we should be providing the hashed seed here. Perhaps this may work for the time being?
						biomeId = BiomeInterpolator.getId(this.seed,  x + chunkCoord.getBlockX(), 0, z + chunkCoord.getBlockZ(), this.biomeProvider);
						biome = this.biomesById[biomeId];
						biomes[x * Constants.CHUNK_SIZE + z] = biome;
						biomeConfigs[x * Constants.CHUNK_SIZE + z] = biome.getBiomeConfig();
					}
				}
				this.biomesCache.put(chunkCoord, biomes);
				this.biomeConfigsCache.put(chunkCoord, biomeConfigs);
			} else {
				cacheHits++;
				//logger.log(LogLevel.INFO, LogCategory.MAIN, "Cache hit " + cacheHits);
			}
		}
		this.locked = false;
		return biomes;
	}
	
	@Override
	public IBiome[] getBiomesForChunks(ChunkCoordinate chunkCoord, int widthHeightInBlocks)
	{
		IBiome[] biomes = new IBiome[widthHeightInBlocks * widthHeightInBlocks];
		IBiome[] chunkBiomes;
		int widthHeightInChunks = (int)Math.ceil(widthHeightInBlocks / 16f);
		synchronized(this.lock)
		{
			this.locked2 = true;
			for(int chunkX = 0; chunkX < widthHeightInChunks; chunkX++)
			{
				for(int chunkZ = 0; chunkZ < widthHeightInChunks; chunkZ++)
				{
					chunkBiomes = getBiomesForChunk(ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + chunkX, chunkCoord.getChunkZ() + chunkZ));
					for(int x = 0; x < Constants.CHUNK_SIZE; x++)
					{
						for(int z = 0; z < Constants.CHUNK_SIZE; z++)
						{
							biomes[(chunkX * Constants.CHUNK_SIZE + x) * widthHeightInBlocks + (chunkZ * Constants.CHUNK_SIZE + z)] = chunkBiomes[x * Constants.CHUNK_SIZE + z];
						}
					}
				}
			}
		}
		this.locked2 = false;
		return biomes;
	}
	
	// Used by any method that will request a region of biomeconfigs,
	// but cannot avoid making a request for each column.
	// TODO: Any method calling this will have cacheChunk=true,
	// ideally all the callers should request entire regions up
	// front instead of relying on this method to cache them.
	@Override
	public IBiomeConfig getBiomeConfig(int x, int z, boolean cacheChunk)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromBlockCoords(x, z);
		// TODO: Do we want/need a lock here? Overhead of the lock would be big.
		IBiomeConfig[] biomeConfigs = null;
		if(!this.locked && !this.locked2)
		{
			biomeConfigs = this.biomeConfigsCache.get(chunkCoord);
		}
		int internalX = x - chunkCoord.getBlockX();
		int internalZ = z - chunkCoord.getBlockZ();
		if(biomeConfigs == null)
		{
			if(cacheChunk && !this.locked && !this.locked2)
			{
				return getBiomeConfigsForChunk(chunkCoord)[internalX * Constants.CHUNK_SIZE + internalZ];
			}
			// TODO: Technically, we should be providing the hashed seed here. Perhaps this may work for the time being?
			int biomeId = BiomeInterpolator.getId(this.seed,  x, 0, z, this.biomeProvider);
			return this.biomesById[biomeId].getBiomeConfig();
		} else {
			smallCacheHits++;
			//logger.log(LogLevel.INFO, LogCategory.MAIN, "Small cache hit " + cacheHits);
		}
		return biomeConfigs[internalX * Constants.CHUNK_SIZE + internalZ];
	}

	// These methods don't use the cache because the overhead
	// of locking likely wouldn't be worth the cache hits.
	
	@Override
	public IBiomeConfig getBiomeConfig(int x, int z)
	{
		return getBiome(x, z).getBiomeConfig();
	}	
	
	@Override
	public IBiome getBiome(int x, int z)
	{	
		// TODO: Technically, we should be providing the hashed seed here. Perhaps this may work for the time being?
		return this.biomesById[BiomeInterpolator.getId(this.seed,  x, 0, z, this.biomeProvider)];
	}

	// Noise biome/biomeconfig, for unzoomed (1/4, low resolution) lookups.

	// Used by any method that can preemptively request a region of biomeconfigs, rather than 
	// making separate requests for each column. Regions are requested and cached per 8x8, 
	// each cell equal to 4x4 blocks in the world.
	@Override
	public IBiomeConfig[] getNoiseBiomeConfigsForRegion(int noiseStartX, int noiseStartZ, int widthHeight)
	{
		int regionSize = 8;
		int regionStartX = noiseStartX >> 3;
		int regionStartZ = noiseStartZ >> 3;
		int cacheOffsetX = noiseStartX - (regionStartX << 3);
		int cacheOffsetZ = noiseStartZ - (regionStartZ << 3);
		int regionWidth = (int)Math.ceil((((regionStartX >> 3) + widthHeight) - (regionStartX >> 3)) / 8f);
		int regionHeight = (int)Math.ceil((((regionStartZ >> 3) + widthHeight) - (regionStartZ >> 3)) / 8f);
		IBiomeConfig[] biomeConfigs = new IBiomeConfig[widthHeight * widthHeight];

		IBiome biome;
		IBiomeConfig[] region;
		ChunkCoordinate regionCoord;
		List<ChunkCoordinate> regionsToHandle = new ArrayList<ChunkCoordinate>();
		int cacheX;
		int cacheZ;
		synchronized(this.noiseLock)
		{
			for(int regionX = regionStartX; regionX <= regionStartX + regionWidth; regionX++)
			{
				for(int regionZ = regionStartZ; regionZ <= regionStartZ + regionHeight; regionZ++)
				{
					regionCoord = ChunkCoordinate.fromChunkCoords(regionX, regionZ);
					region = this.noiseBiomeConfigsCache.get(regionCoord);
					if(region != null)
					{
						for(int x = 0; x < regionSize; x++)
						{
							for(int z = 0; z < regionSize; z++)
							{
								cacheX = ((regionX - regionStartX) << 3) + x - cacheOffsetX;								
								cacheZ = ((regionZ - regionStartZ) << 3) + z - cacheOffsetZ;
								if(
									cacheX < widthHeight && cacheX >= 0 &&
									cacheZ < widthHeight && cacheZ >= 0
								)
								{
									biomeConfigs[cacheX * widthHeight + cacheZ] = region[(x << 3) + z];
									if(
										cacheX == widthHeight - 1 &&
										cacheZ == widthHeight - 1 &&
										regionsToHandle.size() == 0 
									)
									{
										return biomeConfigs;
									}
								}
							}
						}
					} else {
						regionsToHandle.add(regionCoord);
					}
				}
			}
		}
		Map<ChunkCoordinate, IBiomeConfig[]> regionsHandled = new HashMap<ChunkCoordinate, IBiomeConfig[]>();
		for(ChunkCoordinate regionTohandle : regionsToHandle)
		{
			region = new IBiomeConfig[regionSize * regionSize];
			for(int x = 0; x < regionSize; x++)
			{
				for(int z = 0; z < regionSize; z++)
				{
					// TODO: Technically, we should be providing the hashed seed here. Perhaps this may work for the time being?
					biome = this.biomesById[this.biomeProvider.getSampler().sample((regionTohandle.getChunkX() << 3) + x, (regionTohandle.getChunkZ() << 3) + z)];
					region[(x << 3) + z] = biome.getBiomeConfig();
					
					// TODO: Abort and don't cache region if requested area is smaller than 8x8?
					cacheX = ((regionTohandle.getChunkX() - regionStartX) << 3) + x - cacheOffsetX;
					cacheZ = ((regionTohandle.getChunkZ() - regionStartZ) << 3) + z - cacheOffsetZ;
					if(
						cacheX < widthHeight && cacheX >= 0 &&
						cacheZ < widthHeight && cacheZ >= 0
					)
					{
						biomeConfigs[cacheX * widthHeight + cacheZ] = biome.getBiomeConfig();
					}
				}
			}
			regionsHandled.put(regionTohandle, region);
		}
		synchronized(this.noiseLock)
		{
			this.noiseBiomeConfigsCache.putAll(regionsHandled);
		}
		return biomeConfigs;
	}

	@Override
	public IBiomeConfig getNoiseBiomeConfig(int noiseX, int noiseZ, boolean cacheChunk)
	{
		return getNoiseBiome(noiseX, noiseZ, cacheChunk).getBiomeConfig();
	}

	@Override
	public IBiome getNoiseBiome(int noiseX, int noiseZ)
	{
		return getNoiseBiome(noiseX, noiseZ, false);
	}

	private IBiome getNoiseBiome(int noiseX, int noiseZ, boolean cacheChunk)
	{
		return this.biomesById[this.biomeProvider.getSampler().sample(noiseX, noiseZ)];
	}

	/**
	 * Interpolates the given biome from biome coords (pos >> 2) to real coords.
	 * This is required as a vanilla change in 1.15 changed biomes from being stored in real resolution, changing them to be
	 * stored in a 4x4x4 cubes instead, allowing for 3d biomes at the cost of resolution. This class interpolates and provides
	 * a rough estimation of the correct biome at the given world coords.
	*/	
	private static class BiomeInterpolator
	{
		public static int getId(long seed, int x, int y, int z, ILayerSource biomeProvider)
		{
			long pos = sample(seed, x, y, z);
			int biomeId = biomeProvider.getSampler().sample(MathHelper.getXFromLong(pos), MathHelper.getZFromLong(pos));
			return biomeId;
		}
		
		private static long sample(long seed, int x, int y, int z)
		{
			int startX = x - 2;
			int startY = y - 2;
			int startZ = z - 2;
			
			int chunkX = startX >> 2;
			int chunkY = startY >> 2;
			int chunkZ = startZ >> 2;
			
			double localX = (double) (startX & 3) / 4.0D;
			double localY = (double) (startY & 3) / 4.0D;
			double localZ = (double) (startZ & 3) / 4.0D;
			
			double maxDistance = Double.MAX_VALUE;
			int idx = Integer.MIN_VALUE;
			
			for (int i = 0; i < 8; ++i)
			{
				boolean isX = (i & 4) == 0;
				boolean isY = (i & 2) == 0;
				boolean isZ = (i & 1) == 0;
				
				int lerpX = isX ? chunkX : chunkX + 1;
				int lerpY = isY ? chunkY : chunkY + 1;
				int lerpZ = isZ ? chunkZ : chunkZ + 1;
				
				double xFraction = isX ? localX : localX - 1.0D;
				double yFraction = isY ? localY : localY - 1.0D;
				double zFraction = isZ ? localZ : localZ - 1.0D;
				
				double distance = calcSquaredDistance(seed, lerpX, lerpY, lerpZ, xFraction, yFraction, zFraction);
				
				if (maxDistance > distance)
				{
					maxDistance = distance;
					idx = i;
				}
			}
			
			int finalX = (idx & 4) == 0 ? chunkX : chunkX + 1;
			// int finalY = (idx & 2) == 0 ? chunkY : chunkY + 1; // y coord is not used currently
			int finalZ = (idx & 1) == 0 ? chunkZ : chunkZ + 1;
			
			return MathHelper.toLong(finalX, finalZ);
		}
	
		private static double calcSquaredDistance(long seed, int x, int y, int z, double xFraction, double yFraction, double zFraction)
		{
			long mixedSeed = MathHelper.mixSeed(seed, x);
			mixedSeed = MathHelper.mixSeed(mixedSeed, y);
			mixedSeed = MathHelper.mixSeed(mixedSeed, z);
			mixedSeed = MathHelper.mixSeed(mixedSeed, x);
			mixedSeed = MathHelper.mixSeed(mixedSeed, y);
			mixedSeed = MathHelper.mixSeed(mixedSeed, z);
			double xOffset = distribute(mixedSeed);
			mixedSeed = MathHelper.mixSeed(mixedSeed, seed);
			double yOffset = distribute(mixedSeed);
			mixedSeed = MathHelper.mixSeed(mixedSeed, seed);
			double zOffset = distribute(mixedSeed);
			return square(zFraction + zOffset) + square(yFraction + yOffset) + square(xFraction + xOffset);
		}
	
		private static double distribute(long seed)
		{
			double d = (double) ((int) (seed >> 24) & 1023) / 1024.0D;
			return (d - 0.5D) * 0.9D;
		}
	
		private static double square(double d)
		{
			return d * d;
		}
	}
}
