package com.pg85.otg.util.gen;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.interfaces.IBiome;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public class DecorationBiomeCache
{
	private final IBiome[] biomes;
	private final int areaHeight;
	private final int startX;
	private final int startZ;

	public DecorationBiomeCache(int areaWidth, int areaHeight, int startChunkX, int startChunkZ)
	{
		this.biomes = new IBiome[areaWidth * areaHeight];
		this.areaHeight = areaHeight;
		this.startX = startChunkX * Constants.CHUNK_SIZE;
		this.startZ = startChunkZ * Constants.CHUNK_SIZE;
	}

	public IBiome getBiome(int blockX, int blockZ, IWorldGenRegion worldGenRegion)
	{
		// If this throws an indexoutofbounds exception, there's a bug in other code that needs to be fixed.
		// Any code requesting biomes during decoration should not be doing so outside of bounds.
		IBiome biome = this.biomes[(blockX - this.startX) * areaHeight + (blockZ - this.startZ)];
		if(biome == null)
		{
			biome = worldGenRegion.getBiome(blockX, blockZ);
			this.biomes[(blockX - this.startX) * areaHeight + (blockZ - this.startZ)] = biome;
		}
		return biome;
	}
}
