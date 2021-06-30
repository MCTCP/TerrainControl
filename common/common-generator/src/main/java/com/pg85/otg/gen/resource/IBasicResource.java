package com.pg85.otg.gen.resource;

import java.util.Random;

import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public interface IBasicResource
{
	default void processForChunkDecoration(IWorldGenRegion worldGenregion, Random random, boolean villageInChunk, ILogger logger, IMaterialReader materialReader)
	{
		// TODO: Fire Forge resource decoration events, when they're available.
		spawnForChunkDecoration(worldGenregion, random, villageInChunk, logger, materialReader);
	}

	void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ILogger logger, IMaterialReader materialReader);
}
