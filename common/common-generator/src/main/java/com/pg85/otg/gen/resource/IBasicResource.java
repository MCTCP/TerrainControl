package com.pg85.otg.gen.resource;

import java.util.Random;

import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public interface IBasicResource
{
	default void processForChunkDecoration(IWorldGenRegion worldGenregion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingDecorated, ILogger logger, IMaterialReader materialReader)
	{
		// TODO: Fire Forge resource decoration events, when they're available.
		spawnForChunkDecoration(worldGenregion, random, villageInChunk, chunkBeingDecorated, logger, materialReader);
	}

	void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingDecorated, ILogger logger, IMaterialReader materialReader);
}
