package com.pg85.otg.gen.resource;

import java.util.Random;

import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;

public interface IBasicResource
{
	default void processForChunkDecoration(IWorldGenRegion worldGenregion, Random random, ILogger logger, IMaterialReader materialReader)
	{
		// TODO: Fire Forge resource decoration events, when they're available.
		spawnForChunkDecoration(worldGenregion, random, logger, materialReader);
	}

	void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, ILogger logger, IMaterialReader materialReader);
}
