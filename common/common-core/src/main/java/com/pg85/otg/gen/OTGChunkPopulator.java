package com.pg85.otg.gen;

import com.pg85.otg.OTG;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
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

/**
 * Takes care of resource population. Spawns all OTG resources. Some of the population steps (like vanilla structures
 * and mob spawning) use mc logic and are spawned by mc itself. For those population steps, OTG only fills in the 
 * required configurations when registering the biomes in the platform-specific layer (see OTGBiome/ForgeBiome).
 */
public class OTGChunkPopulator implements IChunkPopulator
{
	private final Random rand;

	// Locking objects / checks to prevent populate running on multiple threads,
	// or when the world is waiting for an opportunity to save.
	// TODO: Make this prettier, may need to move this to chunk save as well.
	private final Object lockingObject = new Object();
	private boolean populating;
	private boolean processing = false;
	private boolean saving;
	private boolean saveRequired;

	public OTGChunkPopulator()
	{
		this.rand = new Random();
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

	public void populate(ChunkCoordinate chunkCoord, IWorldGenRegion worldGenRegion, BiomeConfig biomeConfig, CustomStructureCache structureCache)
	{
		boolean unlockWhenDone = false;
		// Wait for another thread running SaveToDisk, then place a lock.
		boolean firstLog = false;
		while(true)
		{
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
		boolean developerMode = OTG.getEngine().getPluginConfig().getDeveloperModeEnabled();
		boolean spawnLog = OTG.getEngine().getPluginConfig().getSpawnLogEnabled();
		boolean isBO4Enabled = worldGenRegion.getWorldConfig().getCustomStructureType() == CustomStructureType.BO4;
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

			// If developer mode is enabled in OTG.ini, log the stack trace
			// so users can figure out which mod is causing the cascade.
			if(developerMode)
			{			
				logger.log(LogMarker.INFO, Arrays.toString(Thread.currentThread().getStackTrace()));
			}
		}
		
		// Release the lock
		synchronized(this.lockingObject)
		{
			// This assumes that this method can only be called alone or recursively, never by multiple threads at once.
			// TODO: Make sure that's the case.
			if(unlockWhenDone)
			{
				this.populating = false;
			}
		}
	}

	// TODO: Fire population events.
	private void doPopulate(ChunkCoordinate chunkCoord, IWorldGenRegion worldGenRegion, BiomeConfig biomeConfig, boolean isBO4Enabled, boolean developerMode, boolean spawnLog, ILogger logger, IMaterialReader materialReader, Path otgRootFolder, CustomStructureCache structureCache, CustomObjectManager customObjectManager, CustomObjectResourcesManager customObjectResourcesManager, IModLoadedChecker modLoadedChecker)
	{		
		// Get the corner block coords
		int x = chunkCoord.getBlockX();
		int z = chunkCoord.getBlockZ();

		// Null check
		if (biomeConfig == null)
		{
			logger.log(LogMarker.WARN, "Unknown biome at {},{}  (chunk {}). Could not populate chunk.", x + 8, z + 8, chunkCoord);
			return;
		}

		// Get the random generator
		long resourcesSeed = worldGenRegion.getSeed();
		this.rand.setSeed(resourcesSeed);
		long l1 = this.rand.nextLong() / 2L * 2L + 1L;
		long l2 = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

		boolean hasVillage = false;

		// Use BO4 logic for BO4 worlds
		if(isBO4Enabled)
		{
			plotAndSpawnBO4s(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		}

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
		
		// Snow and ice
		// TODO: Snow is appearing below structures, indicating it spawned before 
		// it should. Check and align population bounds for resources and make sure
		// freezing is done during the correct population step.
		FrozenSurfaceHelper.freezeChunk(worldGenRegion, chunkCoord);
	}

	private void plotAndSpawnBO4s(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated, Path otgRootFolder, boolean developerMode, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager customObjectResourcesManager, IModLoadedChecker modLoadedChecker)
	{
		// Plot and spawn BO4's for all chunks that may have blocks spawned on them while populating this chunk, 
		// so we can be sure those chunks have had a chance to plot+spawn bo4's before other resources.
		
		structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		structureCache.plotBo4Structures(worldGenRegion, this.rand, chunkCoord, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
	}

	private void spawnBO4(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated, Path otgRootFolder, boolean developerMode, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		structureCache.spawnBo4Chunk(worldGenRegion, chunkCoord, chunkBeingPopulated, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}
}
