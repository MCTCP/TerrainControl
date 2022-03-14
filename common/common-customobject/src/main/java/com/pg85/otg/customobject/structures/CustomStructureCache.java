package com.pg85.otg.customobject.structures;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructure;
import com.pg85.otg.customobject.structures.bo4.CustomStructurePlotter;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IChunkDecorator;
import com.pg85.otg.interfaces.ICustomObjectManager;
import com.pg85.otg.interfaces.ICustomObjectResourcesManager;
import com.pg85.otg.interfaces.ICustomStructureGen;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IStructuredCustomObject;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

// TODO: spawners/particles/moddata for customobjects also use this, so not just structures. refactor?
/**
 * Each world has a CustomObjectStructureCache with data for spawned and unfinished structures
 */
public class CustomStructureCache
{
	private final Path worldSaveDir;
	private final boolean isBO4Enabled;
	private final String presetFolderName;
	private final long worldSeed;
	
	// BO3
	private FifoMap<ChunkCoordinate, BO3CustomStructure> bo3StructureCache;
	
	// BO4
	
	// Contains bo4StructureCache of plotted but not yet decorated branches
	private CustomStructurePlotter plotter; 

	// Common

	// WorldInfoChunks holds info on all chunks that had structures plotted on them for this world. 
	// Used for /otg structure and spawners/particles/moddata for BO structures and objects.
	// For BO4's this is also used used to avoid resources like lakes spawning on structures.
	// WorldInfoChunks is persisted to disk, the bo4 plotter's structurecache (of plotted but
	// not yet decorated branches) is assembled from WorldInfoChunks when loaded from disk.
	// WorldInfoChunks is used as little as possible, due to its size and slowness.
	private Map<ChunkCoordinate, StructureDataRegion> worldInfoChunks;
	
	public CustomStructureCache(String presetFolderName, Path worldSaveDir, long worldSeed, boolean isBO4Enabled, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		this.worldInfoChunks = new HashMap<ChunkCoordinate, StructureDataRegion>();
		this.plotter = new CustomStructurePlotter();
		this.bo3StructureCache = new FifoMap<ChunkCoordinate, BO3CustomStructure>(400);
		this.worldSaveDir = worldSaveDir;
		this.isBO4Enabled = isBO4Enabled;
		this.presetFolderName = presetFolderName;
		this.worldSeed = worldSeed;
		loadStructureCache(otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}
	
	// WorldInfoChunks
	
	private boolean worldInfoChunksContainsKey(ChunkCoordinate chunkCoordinate)
	{
		ChunkCoordinate regionCoord = chunkCoordinate.toRegionCoord();		
		StructureDataRegion chunkRegion = worldInfoChunks.get(regionCoord);
		return chunkRegion != null && chunkRegion.getStructure(chunkCoordinate.getRegionInternalX(), chunkCoordinate.getRegionInternalZ()) != null;
	}
	
	private CustomStructure getFromWorldInfoChunks(ChunkCoordinate chunkCoordinate)
	{
		ChunkCoordinate regionCoord = chunkCoordinate.toRegionCoord();
		StructureDataRegion chunkRegion = this.worldInfoChunks.get(regionCoord);
		if(chunkRegion != null)
		{
			return chunkRegion.getStructure(chunkCoordinate.getRegionInternalX(), chunkCoordinate.getRegionInternalZ());
		}
		return null;
	}
	
	private void addToWorldInfoChunks(CustomStructure structure, ChunkCoordinate chunkCoordinate, boolean requiresSave)
	{
		ChunkCoordinate regionCoord = chunkCoordinate.toRegionCoord();
		StructureDataRegion chunkRegion = this.worldInfoChunks.get(regionCoord);
		if(chunkRegion == null)
		{
			chunkRegion = new StructureDataRegion();
			this.worldInfoChunks.put(regionCoord, chunkRegion);
		}
		chunkRegion.setStructure(chunkCoordinate.getRegionInternalX(), chunkCoordinate.getRegionInternalZ(), structure, requiresSave);
	}

	public void markRegionForSaving(ChunkCoordinate regionCoordinate)
	{
		StructureDataRegion region = this.worldInfoChunks.get(regionCoordinate);
		if(region != null)
		{
			region.markSaveRequired();
		}
	}
	
	private void addToWorldInfoChunks(ChunkCoordinate chunkCoord, CustomStructure structure, boolean canOverride)
	{		
		// Add Structure  to worldInfoChunks for /otg structure and spawners/particles/moddata
		// Make sure not to override any ModData/Spawner/Particle data added by CustomObjects
		if(worldInfoChunksContainsKey(chunkCoord))
		{
			CustomStructure existingObject = getFromWorldInfoChunks(chunkCoord);
			if(existingObject != structure)
			{
				if(canOverride)
				{
					addToWorldInfoChunks(structure, chunkCoord, true);
				}
			}
		} else {
			addToWorldInfoChunks(structure, chunkCoord, true);	
		}
	}
	
	// TODO: This is only used for fetching spawner/particles/moddata,
	// doesn't really need to know about structure. refactor?
	public CustomStructure getChunkData(ChunkCoordinate chunkCoord)
	{
		return getFromWorldInfoChunks(chunkCoord);
	}
	
	// BO3 methods
	
	// Only used for Bukkit
	// TODO: Document this, remove if possible.
	public void reloadBo3StructureCache()
	{
		this.bo3StructureCache.clear();
	}

	public BO3CustomStructure getBo3StructureStart(IWorldGenRegion worldGenRegion, Random worldRandom, int chunkX, int chunkZ, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
		BO3CustomStructure structureStart = bo3StructureCache.get(chunkCoord);

		if (structureStart != null)
		{
			if(structureStart.start == null)
			{
				return null;
			}
			return structureStart;
		}

		// No structure found, create one
		Random random = RandomHelper.getRandomForCoords(chunkX ^ 2, (chunkZ + 1) * 2, worldGenRegion.getSeed());
		IBiomeConfig biomeConfig = worldGenRegion.getCachedBiomeProvider().getBiomeConfig(chunkX * 16 + 15, chunkZ * 16 + 15);
		ICustomStructureGen structureGen = biomeConfig.getStructureGen();

		if (structureGen != null)
		{
			BO3CustomStructureCoordinate customObject = getRandomObjectCoordinate(structureGen, worldGenRegion, random, chunkX, chunkZ, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
			if (customObject != null)
			{
				structureStart = new BO3CustomStructure(worldGenRegion, customObject, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
				bo3StructureCache.put(chunkCoord, structureStart);
				return structureStart;
			}
		}
		bo3StructureCache.put(chunkCoord, new BO3CustomStructure(null));
		return null;
	}
	
	// TODO: Taken from structuregen, duplicate code, clean this up.
	private BO3CustomStructureCoordinate getRandomObjectCoordinate(ICustomStructureGen structureGen, IWorldGenRegion worldGenRegion, Random random, int chunkX, int chunkZ, Path otgRootFolder, ICustomObjectManager customObjectManager, IMaterialReader materialReader, ICustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if (structureGen.isEmpty())
		{
			return null;
		}
		for (int objectNumber = 0; objectNumber < structureGen.getObjects(worldGenRegion.getPresetFolderName(), otgRootFolder, worldGenRegion.getLogger(), customObjectManager, materialReader, manager, modLoadedChecker).size(); objectNumber++)
		{
			if (random.nextDouble() * 100.0 < structureGen.getObjectChance(objectNumber))
			{
				IStructuredCustomObject object = structureGen.getObjects(worldGenRegion.getPresetFolderName(), otgRootFolder, worldGenRegion.getLogger(), customObjectManager, materialReader, manager, modLoadedChecker).get(objectNumber);
				if(object != null && object instanceof BO3)
				{
					return (BO3CustomStructureCoordinate)((BO3)object).makeCustomStructureCoordinate(worldGenRegion.getPresetFolderName(), worldGenRegion.getWorldConfig().getUseOldBO3StructureRarity(), random, chunkX, chunkZ);
				} else {
					if(worldGenRegion.getLogger().getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						IBiomeConfig biomeConfig = worldGenRegion.getCachedBiomeProvider().getBiomeConfig(chunkX * 16 + 15, chunkZ * 16 + 15);
						worldGenRegion.getLogger().log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Error: Could not find BO3 for CustomStructure in biome " + biomeConfig.getName() + ". BO3: " + structureGen.getObjectName(objectNumber));
					}
				}
			}
		}
		return null;
	}
	
	public void addBo3ToStructureCache(ChunkCoordinate chunkCoord, CustomStructure structure, boolean canOverride)
	{
		addToWorldInfoChunks(chunkCoord, structure, canOverride);
	}
	
	// BO4 methods

	// Used while calculating branches during plotting
	public boolean isChunkOccupied(ChunkCoordinate chunkCoordinate)
	{
		return this.plotter.isBo4ChunkPlotted(chunkCoordinate);
	}

	// Called for each structure start plotted, and each ObjectToSpawn / 
	// SmoothingArea chunk after branches have been calculated
	public void addBo4ToStructureCache(ChunkCoordinate chunkCoord, BO4CustomStructure structure)
	{
		this.plotter.addBo4ToStructureCache(chunkCoord, structure);		
		addToWorldInfoChunks(chunkCoord, structure, true);
	}

	// Only used by other resources like lakes 
	public boolean isBo4ChunkOccupied(ChunkCoordinate chunkCoord)
	{
		if(this.isBO4Enabled)
		{
			return this.worldInfoChunks.containsKey(chunkCoord);
		}
		return false;
	}
	
	// Only used by ChunkDecorator during decoration
	public void plotBo4Structures(IWorldGenRegion worldGenRegion, Random rand, ChunkCoordinate chunkCoord, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		plotter.plotStructures(this, worldGenRegion, rand, chunkCoord, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	// Only used by ChunkDecorator during decoration
	public void spawnBo4Chunk(IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		this.plotter.spawnBO4Chunk(chunkCoord, this, worldGenRegion, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}
	
	// Only used by /spawn command	
	public ChunkCoordinate plotBo4Structure(IWorldGenRegion worldGenRegion, BO4 structure, ArrayList<String> biomes, ChunkCoordinate chunkCoord, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker, boolean force)
	{
		return plotter.plotStructures(structure, biomes, this, worldGenRegion, new Random(), chunkCoord, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker, force);
	}

	// Persistence - WorldInfoChunks for BO3+BO4, plotter structurecache for BO4

	public void saveToDisk(ILogger logger, IChunkDecorator chunkPopulator)
	{
		logger.log(LogLevel.INFO, LogCategory.MAIN, "Saving structure and pregenerator data.");
		boolean firstLog = false;
		long starTime = System.currentTimeMillis();
		while(true)
		{
			// TODO: Make this prettier
			synchronized(chunkPopulator.getLockingObject())
			{
				if(!chunkPopulator.isDecorating())
				{
					chunkPopulator.beginSave();
					break;
				}
			}
			if(firstLog)
			{
				logger.log(LogLevel.WARN, LogCategory.MAIN, "SaveToDisk waiting on Populate. Although other mods could be causing this and there may not be any problem, this can potentially cause an endless loop!");
				firstLog = false;
			}
			int interval = 300;
			if(System.currentTimeMillis() - starTime > (interval * 1000))
			{
				logger.log(LogLevel.FATAL, LogCategory.MAIN, "SaveToDisk waited on decorate longer than " + interval + " seconds, something went wrong!");
				throw new RuntimeException("SaveToDisk waited on decorate longer than " + interval + " seconds, something went wrong!");
			}
		}

		saveStructureCache(logger);

		synchronized(chunkPopulator.getLockingObject())
		{
			chunkPopulator.endSave();
		}
		logger.log(LogLevel.INFO, LogCategory.MAIN, "Structure and pregenerator data saved.");
	}

	private void saveStructureCache(ILogger logger)
	{
		CustomStructureFileManager.saveStructureData(this.worldInfoChunks, this.presetFolderName, this.worldSaveDir, logger);
		
		if(this.isBO4Enabled)
		{
			plotter.saveStructureCache(this.worldSaveDir, this.presetFolderName, this.isBO4Enabled, logger);
		}
	}

	private void loadStructureCache(Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{		
		logger.log(LogLevel.INFO, LogCategory.MAIN, "Loading structure data");

		this.worldInfoChunks = new HashMap<ChunkCoordinate, StructureDataRegion>();
		
		Map<CustomStructure, ArrayList<ChunkCoordinate>> loadedStructures = CustomStructureFileManager.loadStructureData(this.presetFolderName, this.worldSaveDir, this.worldSeed, this.isBO4Enabled, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		if(loadedStructures != null)
		{
			if(this.isBO4Enabled)
			{
				this.plotter.loadStructureCache(this.worldSaveDir, this.presetFolderName, this.isBO4Enabled, loadedStructures, logger);
			}

			for(Entry<CustomStructure, ArrayList<ChunkCoordinate>> loadedStructure : loadedStructures.entrySet())
			{
				if(loadedStructure == null)
				{
					throw new RuntimeException("This shouldn't happen, please ask for help on the OTG Discord and/or file an issue on the OTG github.");
				}

				for(ChunkCoordinate chunkCoord : loadedStructure.getValue())
				{
					addToWorldInfoChunks(loadedStructure.getKey(), chunkCoord, false);
				}
			}
		}

		logger.log(LogLevel.INFO, LogCategory.MAIN, "Loading done");
	}
}
