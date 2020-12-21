package com.pg85.otg.customobject.resource;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public abstract class CustomObjectResource extends Resource
{
	CustomObjectResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader)
	{
		super(biomeConfig, args, logger, materialReader);
	}

    protected void spawnInChunk(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, ILogger logger, IMaterialReader materialReader)
    {
    	// Not used for CustomObjectResource
    }
    
    protected abstract void spawnInChunk(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker);
    
    public final void process(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        // Fire event
        //if (!worldGenregion.fireResourceProcessEvent(this, random, villageInChunk, chunkBeingPopulated.getChunkX(), chunkBeingPopulated.getChunkZ()))
        {
            //return;
        }

        // Spawn
        spawnInChunk(structureCache, worldGenRegion, random, villageInChunk, chunkBeingPopulated, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
    }
}
