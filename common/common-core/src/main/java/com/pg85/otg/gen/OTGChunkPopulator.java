package com.pg85.otg.gen;

import com.pg85.otg.OTG;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.resource.CustomObjectResource;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.gen.surface.FrozenSurfaceHelper;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IChunkPopulator;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public class OTGChunkPopulator implements IChunkPopulator
{
    private final Random rand;
	// Locking objects / checks to prevent populate running on multiple threads,
	// or when the world is waiting for an opportunity to save.
	// TODO: Make this prettier
    private Object lockingObject = new Object();
	private boolean populating;
	private boolean processing = false;
	private boolean saving;
	private boolean saveRequired;
	//
   
    public OTGChunkPopulator()
    {
        this.rand = new Random();
    }

    public void populate(ChunkCoordinate chunkCoord, IWorldGenRegion worldGenRegion, BiomeConfig biomeConfig, CustomStructureCache structureCache)
    {
    	boolean unlockWhenDone = false;
		// Wait for another thread running SaveToDisk, then place a lock.
		boolean firstLog = false;
		while(true)
		{
			//OTG.log(LogMarker.INFO, "Populate waiting on SaveToDisk.");
			synchronized(this.lockingObject)
			{
				if(!this.saving)
				{
					// If populating then this method is being called recursively (indicating cascading chunk-gen).
					// This method can be called recursively, but should never be called by two threads at once.
					// TODO: Make sure that's the case.
					if(!this.populating)
					{
						this.populating = true;
						unlockWhenDone = true;
					}
					break;
				} else {
					if(firstLog)
					{
						OTG.log(LogMarker.WARN, "Populate waiting on SaveToDisk. Although other mods could be causing this and there may not be any problem, this can potentially cause an endless loop!");
						firstLog = false;
					}
				}
			}
		}
		synchronized(this.lockingObject)
		{
			this.saveRequired = true;
		}
		
    	Path otgRootFolder = OTG.getEngine().getOTGRootFolder();
    	boolean developerMode = OTG.getEngine().getPluginConfig().developerMode;
    	boolean spawnLog = OTG.getEngine().getPluginConfig().spawnLog;
    	boolean isBO4Enabled = worldGenRegion.getWorldConfig().isOTGPlus();
    	ILogger logger = OTG.getEngine().getLogger();
    	CustomObjectManager customObjectManager = OTG.getEngine().getCustomObjectManager();
    	IMaterialReader materialReader = OTG.getEngine().getMaterialReader();
    	CustomObjectResourcesManager customObjectResourcesManager = OTG.getEngine().getCustomObjectResourcesManager();
    	IModLoadedChecker modLoadedChecker = OTG.getEngine().getModLoadedChecker();
    	
		if (!this.processing)
		{
			this.processing = true;

			// Cache all biomes in the are being populated (2x2 chunks)
			worldGenRegion.cacheBiomesForPopulation(chunkCoord);
			doPopulate(chunkCoord, worldGenRegion, biomeConfig, isBO4Enabled, developerMode, spawnLog, logger, materialReader, otgRootFolder, structureCache, customObjectManager, customObjectResourcesManager, modLoadedChecker);
			
			this.processing = false;
		} else {

			// Don't use the population chunk biome cache during cascading chunk generation
			worldGenRegion.invalidatePopulationBiomeCache();
			doPopulate(chunkCoord, worldGenRegion, biomeConfig, isBO4Enabled, developerMode, spawnLog, logger, materialReader, otgRootFolder, structureCache, customObjectManager, customObjectResourcesManager, modLoadedChecker);
			
			logger.log(LogMarker.INFO, "Cascading chunk generation detected.");
			if(developerMode)
			{			
				logger.log(LogMarker.INFO, Arrays.toString(Thread.currentThread().getStackTrace()));
			}
		}
		
		// Release the lock
		synchronized(this.lockingObject)
		{
			// This assumes that this method can only be called alone or recursively, never by 2 threads at once.
			// TODO: Make sure that's the case.
			if(unlockWhenDone)
			{
				this.populating = false;
			}
		}
    }

    private void doPopulate(ChunkCoordinate chunkCoord, IWorldGenRegion worldGenRegion, BiomeConfig biomeConfig, boolean isBO4Enabled, boolean developerMode, boolean spawnLog, ILogger logger, IMaterialReader materialReader, Path otgRootFolder, CustomStructureCache structureCache, CustomObjectManager customObjectManager, CustomObjectResourcesManager customObjectResourcesManager, IModLoadedChecker modLoadedChecker)
    {    	
        // Get the corner block coords
        int x = chunkCoord.getChunkX() * 16;
        int z = chunkCoord.getChunkZ() * 16;

        // Null check
        if (biomeConfig == null)
        {
            logger.log(LogMarker.WARN, "Unknown biome at {},{}  (chunk {}). Could not populate chunk.", x + 8, z + 8, chunkCoord);
            return;
        }

        // Get the random generator
        long resourcesSeed = worldGenRegion.getWorldConfig().getResourcesSeed() != 0L ? worldGenRegion.getWorldConfig().getResourcesSeed() : worldGenRegion.getSeed();
        this.rand.setSeed(resourcesSeed);
        long l1 = this.rand.nextLong() / 2L * 2L + 1L;
        long l2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

        boolean hasVillage = false;

        // Use BO4 logic for BO4 worlds
		if(isBO4Enabled)
		{
			// Plot BO4's for all 4 chunks being populated, so we can be sure the chunks have
			// had a chance to be plotted before being populated. We'll spawn BO4's after 
			// ores and lakes, but before any other resources.
			structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
			structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
			structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
			structureCache.plotBo4Structures(worldGenRegion, this.rand, chunkCoord, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);

			spawnBO4s(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		}

        // Generate structures
        //hasVillage = world.placeDefaultStructures(this.rand, chunkCoord);

        // Mark population started
        //OTG.firePopulationStartEvent(world, this.rand, hasVillage, chunkCoord);

        // Resource sequence
        for (ConfigFunction<IBiomeConfig> res : biomeConfig.getResourceSequence())
        {
            if (res instanceof CustomObjectResource)
            {
                ((CustomObjectResource)res).process(structureCache, worldGenRegion, this.rand, hasVillage, chunkCoord, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
            }
            else if (res instanceof Resource)
            {
                ((Resource)res).process(worldGenRegion, this.rand, hasVillage, chunkCoord, OTG.getEngine().getLogger(), OTG.getEngine().getMaterialReader());
            }
        }

        // Animals
        //world.placePopulationMobs(biome, this.rand, chunkCoord);

        // Snow and ice
		// TODO: Fire PopulateChunkEvent.Populate.EventType.ICE for Forge
        new FrozenSurfaceHelper().freezeChunk(worldGenRegion, chunkCoord);

        // Replace blocks
        //world.replaceBlocks(chunkCoord);

        // Mark population ended
        //OTG.firePopulationEndEvent(world, this.rand, hasVillage, chunkCoord);
    }

	// BO4's should always stay within chunk borders, so we can spawn them for all
	// 4 of the chunk in the populated area, ensuring all resources that should be
	// placed afterwards spawn on top of bo4's. 
	private void spawnBO4s(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated, Path otgRootFolder, boolean developerMode, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	private void spawnBO4(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated, Path otgRootFolder, boolean developerMode, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		structureCache.spawnBo4Chunk(worldGenRegion, chunkCoord, chunkBeingPopulated, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	@Override
	public boolean getIsSaveRequired()
	{
		return this.saveRequired;
	}

	@Override
	public boolean isPopulating()
	{
		return this.populating;
	}

	@Override
	public void beginSave()
	{
		this.saving = true;
	}

	@Override
	public void endSave()
	{
		this.saveRequired = false;
		this.saving = false;
	}

	@Override
	public Object getLockingObject()
	{
		return this.lockingObject;
	}
}
