package com.pg85.otg.customobject.resource;

import java.nio.file.Path;
import java.util.Random;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public interface ICustomObjectResource
{
	default void processForChunkDecoration(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		// TODO: Fire Forge resource decoration events, when they're available.
		spawnForChunkDecoration(structureCache, worldGenRegion, random, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	void spawnForChunkDecoration(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker);
}
