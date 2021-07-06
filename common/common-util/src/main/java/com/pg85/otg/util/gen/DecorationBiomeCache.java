package com.pg85.otg.util.gen;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.util.ChunkCoordinate;

public class DecorationBiomeCache
{
	private final int startX;
	private final int startZ;
	private final IBiome[] biomes;

	public DecorationBiomeCache(int startChunkX, int startChunkZ, ICachedBiomeProvider cachedBiomeProvider)
	{
		this.startX = startChunkX * Constants.CHUNK_SIZE;
		this.startZ = startChunkZ * Constants.CHUNK_SIZE;
		this.biomes = cachedBiomeProvider.getBiomesForChunks(ChunkCoordinate.fromBlockCoords(this.startX, this.startZ), DecorationArea.WIDTH);
	}

	public IBiome getBiome(int x, int z)
	{
		// If this throws an indexoutofbounds exception, there's a bug in calling code that needs to be fixed.
		// Any code requesting biomes during decoration should not be doing so outside of bounds.
		int internalX = x - this.startX;
		int internalZ = z - this.startZ;
		return this.biomes[internalX * DecorationArea.HEIGHT + internalZ];
	}

	public IBiomeConfig getBiomeConfig(int x, int z)
	{
		return getBiome(x, z).getBiomeConfig();
	}
}
