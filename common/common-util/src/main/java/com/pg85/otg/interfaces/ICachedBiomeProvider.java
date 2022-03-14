package com.pg85.otg.interfaces;

import com.pg85.otg.util.ChunkCoordinate;

public interface ICachedBiomeProvider
{
	public IBiomeConfig[] getBiomeConfigsForChunk(ChunkCoordinate chunkCoordinate);
	public IBiome[] getBiomesForChunk(ChunkCoordinate chunkCoordinate);
	public IBiome[] getBiomesForChunks(ChunkCoordinate chunkCoord, int widthHeightInChunks);	
	public IBiomeConfig getBiomeConfig(int x, int z, boolean cacheChunk);
	public IBiomeConfig getBiomeConfig(int x, int z);
	public IBiome getBiome(int x, int z);

	public IBiomeConfig[] getNoiseBiomeConfigsForRegion(int noiseStartX, int noiseStartZ, int widthHeight);
	public IBiomeConfig getNoiseBiomeConfig(int x, int z, boolean cacheChunk);
	public IBiome getNoiseBiome(int x, int z);
}
