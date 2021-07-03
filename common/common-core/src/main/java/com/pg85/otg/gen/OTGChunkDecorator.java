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
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IChunkDecorator;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.nio.file.Path;
import java.text.MessageFormat;
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
	private int decorating = 0;
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
		return this.decorating != 0;
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
		ILogger logger = OTG.getEngine().getLogger();
		
		// Wait for another thread running SaveToDisk, then place a lock.
		boolean firstLog = false;
		while(true)
		{
			synchronized(this.lockingObject)
			{
				if(!this.saving)
				{
					this.decorating++;
					this.saveRequired = true;
					break;
				} else {
					if(firstLog)
					{
						logger.log(LogLevel.WARN, LogCategory.MAIN, "Decorate waiting on SaveToDisk. Although other mods could be causing this and there may not be any problem, this can potentially cause an endless loop!");
						firstLog = false;
					}
				}
			}
		}

		Path otgRootFolder = OTG.getEngine().getOTGRootFolder();
		CustomObjectManager customObjectManager = OTG.getEngine().getCustomObjectManager();
		IMaterialReader materialReader = OTG.getEngine().getPresetLoader().getMaterialReader(presetFolderName);
		CustomObjectResourcesManager customObjectResourcesManager = OTG.getEngine().getCustomObjectResourcesManager();
		IModLoadedChecker modLoadedChecker = OTG.getEngine().getModLoadedChecker();

		doDecorate(chunkCoord, worldGenRegion, biomeConfig, logger, materialReader, otgRootFolder, structureCache, customObjectManager, customObjectResourcesManager, modLoadedChecker);			
		
		// Release the lock
		synchronized(this.lockingObject)
		{
			this.decorating--;
		}
	}

	// TODO: Fire decoration events.
	private void doDecorate(ChunkCoordinate chunkCoord, IWorldGenRegion worldGenRegion, BiomeConfig biomeConfig, ILogger logger, IMaterialReader materialReader, Path otgRootFolder, CustomStructureCache structureCache, CustomObjectManager customObjectManager, CustomObjectResourcesManager customObjectResourcesManager, IModLoadedChecker modLoadedChecker)
	{		
		if (biomeConfig == null)
		{
			if(logger.getLogCategoryEnabled(LogCategory.DECORATION))
			{
				logger.log(
					LogLevel.ERROR,
					LogCategory.DECORATION,
					MessageFormat.format(
						"Unknown biome at {0},{1}  (chunk {2}). Could not decorate chunk.", 
						chunkCoord.getChunkX(), 
						chunkCoord.getChunkZ(), 
						chunkCoord
					)
				);
			}
			return;
		}

		// Get the random generator
		long resourcesSeed = worldGenRegion.getSeed();
		this.rand.setSeed(resourcesSeed);
		long l1 = this.rand.nextLong() / 2L * 2L + 1L;
		long l2 = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed(chunkCoord.getChunkX() * l1 + chunkCoord.getChunkZ() * l2 ^ resourcesSeed);

		// Use BO4 logic for BO4 worlds
		if(worldGenRegion.getWorldConfig().getCustomStructureType() == CustomStructureType.BO4)
		{
			// BO4 Plotting cannot currently be done in a thread-safe/non-blocking way,
			// Paper may try to do async chunkgen, so lock here. This will ofcourse 
			// slow down any multithreaded chunk decoration implementation.
			synchronized(asynChunkDecorationLock)
			{
				plotAndSpawnBO4s(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ()), chunkCoord, otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
			}
		}

		long startTimeAll = System.currentTimeMillis();
		// Resource sequence
		for (ConfigFunction<IBiomeConfig> res : biomeConfig.getResourceQueue())
		{
			long startTime = System.currentTimeMillis();
			if (res instanceof ICustomObjectResource)
			{
				((ICustomObjectResource)res).processForChunkDecoration(structureCache, worldGenRegion, this.rand, otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
				if(logger.getLogCategoryEnabled(LogCategory.PERFORMANCE) && (System.currentTimeMillis() - startTime) > 50)
				{
					logger.log(LogLevel.WARN, LogCategory.PERFORMANCE, "Warning: Processing resource " + res.toString() + " in biome " + biomeConfig.getName() + " took " + (System.currentTimeMillis() - startTime) + " Ms.");
				}
			}
			else if (res instanceof ICustomStructureResource)
			{
				((ICustomStructureResource)res).processForChunkDecoration(structureCache, worldGenRegion, this.rand, otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
				if(logger.getLogCategoryEnabled(LogCategory.PERFORMANCE) && (System.currentTimeMillis() - startTime) > 50)
				{
					logger.log(LogLevel.WARN, LogCategory.PERFORMANCE, "Warning: Processing resource " + res.toString() + " in biome " + biomeConfig.getName() + " took " + (System.currentTimeMillis() - startTime) + " Ms.");
				}
			}
			else if (res instanceof IBasicResource)
			{
				((IBasicResource)res).processForChunkDecoration(worldGenRegion, this.rand, logger, materialReader);
				if(logger.getLogCategoryEnabled(LogCategory.PERFORMANCE) && (System.currentTimeMillis() - startTime) > 50)
				{
					logger.log(LogLevel.WARN, LogCategory.PERFORMANCE, "Warning: Processing resource " + res.toString() + " in biome " + biomeConfig.getName() + " took " + (System.currentTimeMillis() - startTime) + " Ms.");
				}				
			}
			else if(res instanceof ErroredFunction)
			{
				if(logger.getLogCategoryEnabled(LogCategory.DECORATION))
				{
					if(!((ErroredFunction<IBiomeConfig>)res).isLogged)
					{
						((ErroredFunction<IBiomeConfig>)res).isLogged = true;
						if(logger.getLogCategoryEnabled(LogCategory.DECORATION))
						{
							logger.log(LogLevel.ERROR, LogCategory.DECORATION, "Errored setting ignored for biome " + biomeConfig.getName() + " : " + toString());
						}
					}					
				}
			}
		}
		if(logger.getLogCategoryEnabled(LogCategory.PERFORMANCE) && (System.currentTimeMillis() - startTimeAll) > 50)
		{
			logger.log(LogLevel.WARN, LogCategory.PERFORMANCE, "Warning: Processing resources in biome " + biomeConfig.getName() + " took " + (System.currentTimeMillis() - startTimeAll) + " Ms.");
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

	private void plotAndSpawnBO4s(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingDecorated, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager customObjectResourcesManager, IModLoadedChecker modLoadedChecker)
	{
		// Plot and spawn BO4's for all chunks that may have blocks spawned on them while decorating this chunk, 
		// so we can be sure those chunks have had a chance to plot+spawn bo4's before other resources.

		structureCache.plotBo4Structures(worldGenRegion, this.rand, chunkCoord, otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);		
		structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);		
		structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() , chunkCoord.getChunkZ() + 1), otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);			
		structureCache.plotBo4Structures(worldGenRegion, this.rand, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);	

		spawnBO4(structureCache, worldGenRegion, chunkCoord, otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ()), otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);	
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX(), chunkCoord.getChunkZ() + 1), otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);
		spawnBO4(structureCache, worldGenRegion, ChunkCoordinate.fromChunkCoords(chunkCoord.getChunkX() + 1, chunkCoord.getChunkZ() + 1), otgRootFolder, logger, customObjectManager, materialReader, customObjectResourcesManager, modLoadedChecker);		
	}

	private void spawnBO4(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		structureCache.spawnBo4Chunk(worldGenRegion, chunkCoord, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}
}
