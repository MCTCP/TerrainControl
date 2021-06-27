package com.pg85.otg.gen;

import com.pg85.otg.OTG;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.ErroredFunction;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.constants.SettingsEnums.CustomStructureType;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.resource.ICustomObjectResource;
import com.pg85.otg.customobject.resource.ICustomStructureResource;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.gen.resource.IBasicResource;
import com.pg85.otg.gen.surface.FrozenSurfaceHelper;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IChunkDecorator;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

/**
 * Takes care of resource decoration. Spawns all OTG resources. Some of the decoration steps (like vanilla 
 * structures and mob spawning) use mc logic and are spawned by mc itself. For those decoration steps, OTG 
 * only fills in the required configurations when registering the biomes in the platform-specific layer (see OTGBiome/ForgeBiome).
 */
public class OTGChunkDecorator implements IChunkDecorator
{
	private final Random rand;

	// Locking objects / checks to prevent decorate running on multiple threads,
	// or when the world is waiting for an opportunity to save.
	// TODO: Is this still required for 1.16?
	private final Object lockingObject = new Object();
	private boolean decorating;
	private boolean processing = false;
	private boolean saving;
	private boolean saveRequired;
	private Object asynChunkDecorationLock = new Object();

	public OTGChunkDecorator()
	{
		this.rand = new Random();
	}
	
	@Override
	public boolean getIsSaveRequired()
	{
		return this.saveRequired;
	}

	@Override
	public boolean isDecorating()
	{
		return this.decorating;
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

	public void decorate(String presetFolderName, ChunkCoordinate chunkCoord, IWorldGenRegion worldGenRegion, BiomeConfig biomeConfig, CustomStructureCache structureCache)
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
					// If decorating then this method is being called recursively (indicating cascading chunk-gen).
					// This method can be called recursively, but should never be called by two threads at once.
					// TODO: Make sure that's the case.
					if(!this.decorating)
					{
						this.decorating = true;
						unlockWhenDone = true;
					}
					break;
				} else {
					if(firstLog)
					{
						OTG.log(LogMarker.WARN, "Decorate waiting on SaveToDisk. Although other mods could be causing this and there may not be any problem, this can potentially cause an endless loop!");
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
		IMaterialReader materialReader = OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName);
		CustomObjectResourcesManager customObjectResourcesManager = OTG.getEngine().getCustomObjectResourcesManager();
		IModLoadedChecker modLoadedChecker = OTG.getEngine().getModLoadedChecker();

		if (!this.processing)
		{
			this.processing = true;
			doDecorate(chunkCoord, worldGenRegion, biomeConfig, isBO4Enabled, developerMode, spawnLog, logger, materialReader, otgRootFolder, structureCache, customObjectManager, customObjectResourcesManager, modLoadedChecker);			
			this.processing = false;
		} else {			
			logger.log(LogMarker.INFO, "Cascading chunk generation detected.");
			
			doDecorate(chunkCoord, worldGenRegion, biomeConfig, isBO4Enabled, developerMode, spawnLog, logger, materialReader, otgRootFolder, structureCache, customObjectManager, customObjectResourcesManager, modLoadedChecker);

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
				this.decorating = false;
			}
		}
	}

	// TODO: Fire decoration events.
	private void doDecorate(ChunkCoordinate chunkCoord, IWorldGenRegion worldGenRegion, BiomeConfig biomeConfig, boolean isBO4Enabled, boolean developerMode, boolean spawnLog, ILogger logger, IMaterialReader materialReader, Path otgRootFolder, CustomStructureCache structureCache, CustomObjectManager customObjectManager, CustomObjectResourcesManager customObjectResourcesManager, IModLoadedChecker modLoadedChecker)
	{		
		if (biomeConfig == null)
		{
			logger.log(LogMarker.WARN, "Unknown biome at {},{}  (chunk {}). Could not decorate chunk.", chunkCoord.getChunkX(), chunkCoord.getChunkZ(), chunkCoord);
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
			// BO4 Plotting cannot currently be done in a thread-safe/non-blocking way,
			// Paper may try to do async chunkgen, so lock here. This will ofcourse 
			// slow down any multithreaded chunk decoration implementation.
			synchronized(asynChunkDecorationLock)
			{
				plotAndSpawnBO4s(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
			}
		}

		// Resource sequence
		for (ConfigFunction<IBiomeConfig> res : biomeConfig.getResourceQueue())
		{
			if (res instanceof ICustomObjectResource)
			{
				((ICustomObjectResource)res).processForChunkDecoration(structureCache, worldGenRegion, this.rand, hasVillage, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
			}
			else if (res instanceof ICustomStructureResource)
			{
				((ICustomStructureResource)res).processForChunkDecoration(structureCache, worldGenRegion, this.rand, hasVillage, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
			}			
			else if (res instanceof IBasicResource)
			{
				((IBasicResource)res).processForChunkDecoration(worldGenRegion, this.rand, hasVillage, logger, materialReader);
			}
			else if(res instanceof ErroredFunction)
			{
				if(OTG.getEngine().getPluginConfig().getDeveloperModeEnabled())
				{
					((ErroredFunction<IBiomeConfig>)res).log(logger, biomeConfig.getName());
				}
			}
		}
	}

	public void doSnowAndIce(IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord)
	{
		// Snow and ice
		// TODO: Snow is appearing below structures, indicating it spawned before 
		// it should. Check and align decoration bounds for resources and make sure
		// freezing is done during the correct decoration step.
		FrozenSurfaceHelper.freezeChunk(worldGenRegion, chunkCoord);
	}
	
	private void plotAndSpawnBO4s(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingDecorated, Path otgRootFolder, boolean developerMode, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager customObjectResourcesManager, IModLoadedChecker modLoadedChecker)
	{
		// Plot and spawn BO4's for all chunks that may have blocks spawned on them while decorating this chunk, 
		// so we can be sure those chunks have had a chance to plot+spawn bo4's before other resources.

		structureCache.plotBo4Structures(worldGenRegion, this.rand, chunkCoord, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);		
		structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);		
		structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() , chunkCoord.getChunkZ() + 1), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);			
		structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);	

		spawnBO4(structureCache, worldGenRegion, chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);	
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);		
	}

	private void spawnBO4(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, Path otgRootFolder, boolean developerMode, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		structureCache.spawnBo4Chunk(worldGenRegion, chunkCoord, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}
}
