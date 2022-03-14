package com.pg85.otg.gen.resource;

import java.util.Random;

import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;

// Biome resources are spawned during decoration, for each chunk being decorated, resources can
// spawn blocks with a 2x2 chunk area (TODO: Will need to move to 3x3 for 1.16). For each resource,
// processForChunkDecoration is called when a chunk is decorated. By default, this calls 
// spawnForChunkDecoration() to perform a single spawn attempt for the resource. Most of OTG's 
// biome resources are FrequencyResourceBase resources, that use rarity/frequency in 
// processForChunkDecoration to determine the number of calls to spawnForChunkDecoration for each 
// chunk, as well as randomising coordinates. Other resources that don't rely on frequency/rarity 
// setting implement their own processForChunkDecoration/spawnForChunkDecoration logic.
// * Note: Resources that use custom objects like CustomObject/CustomStructure/Tree/Sapling are in
// the common-customobject project.
public interface IBasicResource
{
	default void processForChunkDecoration(IWorldGenRegion worldGenregion, Random random, ILogger logger, IMaterialReader materialReader)
	{
		// TODO: Fire Forge resource decoration events, when they're available.
		spawnForChunkDecoration(worldGenregion, random, logger, materialReader);
	}

	void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, ILogger logger, IMaterialReader materialReader);
}
