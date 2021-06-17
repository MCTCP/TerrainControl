package com.pg85.otg.customobject.resource;

import java.nio.file.Path;
import java.util.Random;

import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructure;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public interface ICustomStructureResource
{
	// This code is only used for BO3 custom structures, they share the same biomeconfig
	// resource with bo4's though, so this is probably as clean a separation as we'll get.

	default void processForChunkDecoration(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		// TODO: Fire Forge resource decoration events, when they're available.
		spawnForChunkDecoration(structureCache, worldGenRegion, random, villageInChunk, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	// Only used for BO3 CustomStructure
	default void spawnForChunkDecoration(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		// Don't process BO4's, they're plotted and spawned separately from other resources.
		if(worldGenRegion.getWorldConfig().getCustomStructureType() != CustomStructureType.BO4)
		{
			// Find all structures that reach this chunk, and spawn them
			int searchRadius = worldGenRegion.getWorldConfig().getMaximumCustomStructureRadius();
			int currentChunkX = worldGenRegion.getDecorationArea().getChunkBeingDecorated().getChunkX();
			int currentChunkZ = worldGenRegion.getDecorationArea().getChunkBeingDecorated().getChunkZ();
			BO3CustomStructure structureStart;
			for (int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; searchChunkX++)
			{
				for (int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; searchChunkZ++)
				{
					structureStart = structureCache.getBo3StructureStart(worldGenRegion, random, searchChunkX, searchChunkZ, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
					if (structureStart != null)
					{
						structureStart.spawnInChunk(structureCache, worldGenRegion, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
					}
				}
			}
		}
	}
}
