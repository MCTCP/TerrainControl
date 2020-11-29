package com.pg85.otg.customobject.structures;

import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.bo4.BO4;
import com.pg85.otg.customobject.bofunctions.ModDataFunction;
import com.pg85.otg.customobject.bofunctions.ParticleFunction;
import com.pg85.otg.customobject.bofunctions.SpawnerFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobject.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.bo4.BO4CustomStructure;
import com.pg85.otg.customobject.structures.bo4.CustomStructurePlotter;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IChunkPopulator;
import com.pg85.otg.util.interfaces.ICustomObjectManager;
import com.pg85.otg.util.interfaces.ICustomObjectResourcesManager;
import com.pg85.otg.util.interfaces.ICustomStructureGen;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IStructuredCustomObject;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

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
	private final int dimensionId;
	private final String worldName;
	private final long worldSeed;
	
	// BO3
	private FifoMap<ChunkCoordinate, BO3CustomStructure> bo3StructureCache;
	
	// BO4
	
	// Contains bo4StructureCache of plotted but not yet populated branches
    private CustomStructurePlotter plotter; 

    // Common

	// WorldInfoChunks holds info on all chunks that had structures plotted on them for this world. 
    // Used for /otg structure and spawners/particles/moddata for BO structures and objects.
    // For BO4's this is also used used to avoid resources like lakes spawning on structures.
    // WorldInfoChunks is persisted to disk, the bo4 plotter's structurecache (of plotted but
    // not yet populated branches) is assembled from WorldInfoChunks when loaded from disk.
    // WorldInfoChunks is used as little as possible, due to its size and slowness.
    private Map<ChunkCoordinate, StructureDataRegion> worldInfoChunks;

    public CustomStructureCache(String worldName, Path worldSaveDir, int dimensionId, long worldSeed, boolean isBO4Enabled, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        this.worldInfoChunks = new HashMap<ChunkCoordinate, StructureDataRegion>();
        this.plotter = new CustomStructurePlotter();
        this.bo3StructureCache = new FifoMap<ChunkCoordinate, BO3CustomStructure>(400);
        this.worldSaveDir = worldSaveDir;
        this.isBO4Enabled = isBO4Enabled;
        this.dimensionId = dimensionId;
        this.worldName = worldName;
        this.worldSeed = worldSeed;
        loadStructureCache(otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
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
					structure.modDataManager.modData.addAll(existingObject.modDataManager.modData);
					structure.particlesManager.particleData.addAll(existingObject.particlesManager.particleData);
					structure.spawnerManager.spawnerData.addAll(existingObject.spawnerManager.spawnerData);
					addToWorldInfoChunks(structure, chunkCoord, true);
				} else {
					existingObject.modDataManager.modData.addAll(structure.modDataManager.modData);
					existingObject.particlesManager.particleData.addAll(structure.particlesManager.particleData);
					existingObject.spawnerManager.spawnerData.addAll(structure.spawnerManager.spawnerData);
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

    public BO3CustomStructure getBo3StructureStart(IWorldGenRegion worldGenRegion, Random worldRandom, int chunkX, int chunkZ, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
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
       	IBiomeConfig biomeConfig = worldGenRegion.getBiomeConfig(chunkX * 16 + 15, chunkZ * 16 + 15);
       	ICustomStructureGen structureGen = biomeConfig.getStructureGen();

        if (structureGen != null)
        {
            BO3CustomStructureCoordinate customObject = getRandomObjectCoordinate(structureGen, worldGenRegion, random, chunkX, chunkZ, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
            if (customObject != null)
            {
                structureStart = new BO3CustomStructure(worldGenRegion, customObject, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
                bo3StructureCache.put(chunkCoord, structureStart);
                return structureStart;
            }
        }
        bo3StructureCache.put(chunkCoord, new BO3CustomStructure(null));
        return null;
    }
    
    // TODO: Taken from structuregen, duplicate code, clean this up.
    private BO3CustomStructureCoordinate getRandomObjectCoordinate(ICustomStructureGen structureGen, IWorldGenRegion worldGenRegion, Random random, int chunkX, int chunkZ, Path otgRootFolder, boolean spawnLog, ILogger logger, ICustomObjectManager customObjectManager, IMaterialReader materialReader, ICustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        if (structureGen.isEmpty())
        {
            return null;
        }
        for (int objectNumber = 0; objectNumber < structureGen.getObjects(worldGenRegion.getPresetName(), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker).size(); objectNumber++)
        {
            if (random.nextDouble() * 100.0 < structureGen.getObjectChance(objectNumber))
            {
            	IStructuredCustomObject object = structureGen.getObjects(worldGenRegion.getPresetName(), otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker).get(objectNumber);
            	if(object != null && object instanceof BO3) // TODO: How could a BO4 end up here? seen it happen once..
            	{
            		return (BO3CustomStructureCoordinate)((BO3)object).makeCustomStructureCoordinate(worldGenRegion.getPresetName(), random, chunkX, chunkZ);
            	} else {
            		if(spawnLog)
            		{
            			IBiomeConfig biomeConfig = worldGenRegion.getBiomeConfig(chunkX * 16 + 15, chunkZ * 16 + 15);
            			logger.log(LogMarker.WARN, "Error: Could not find BO3 for CustomStructure in biome " + biomeConfig.getName() + ". BO3: " + structureGen.getObjectName(objectNumber));
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
    
    // Only used by ObjectSpawner during population
    public void plotBo4Structures(IWorldGenRegion worldGenRegion, Random rand, ChunkCoordinate chunkCoord, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	plotter.plotStructures(this, worldGenRegion, rand, chunkCoord, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
    }

    // Only used by ObjectSpawner during population
	public void spawnBo4Chunk(IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated, Path otgRootFolder, boolean developerMode, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		this.plotter.spawnBO4Chunk(chunkCoord, this, worldGenRegion, chunkBeingPopulated, otgRootFolder, developerMode, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}
	
	// Only used by /spawn command	
    public ChunkCoordinate plotBo4Structure(IWorldGenRegion worldGenRegion, BO4 structure, ArrayList<String> biomes, ChunkCoordinate chunkCoord, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	plotter.plotStructures(structure, biomes, this, worldGenRegion, new Random(), chunkCoord, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
    	return null;
    }

    // Persistence - WorldInfoChunks for BO3+BO4, plotter structurecache for BO4

    public void saveToDisk(boolean spawnLog, ILogger logger, IChunkPopulator chunkPopulator)
    {
    	logger.log(LogMarker.INFO, "Saving structure and pregenerator data.");
    	boolean firstLog = false;
    	long starTime = System.currentTimeMillis();
		while(true)
		{
			// TODO: Make this prettier
			synchronized(chunkPopulator.getLockingObject())
			{
				if(!chunkPopulator.isPopulating())
				{
					chunkPopulator.beginSave();
					break;
				}
			}
			if(firstLog)
			{
				logger.log(LogMarker.WARN, "SaveToDisk waiting on Populate. Although other mods could be causing this and there may not be any problem, this can potentially cause an endless loop!");
				firstLog = false;
			}
			int interval = 300;
			if(System.currentTimeMillis() - starTime > (interval * 1000))
			{
				logger.log(LogMarker.FATAL, "SaveToDisk waited on populate longer than " + interval + " seconds, something went wrong!");
				throw new RuntimeException("SaveToDisk waited on populate longer than " + interval + " seconds, something went wrong!");
			}
		}    	

		saveStructureCache(spawnLog, logger);

		synchronized(chunkPopulator.getLockingObject())
		{
			chunkPopulator.endSave();
		}
		logger.log(LogMarker.INFO, "Structure and pregenerator data saved.");
    }

    private void saveStructureCache(boolean spawnLog, ILogger logger)
    {
	    CustomStructureFileManager.saveStructureData(this.worldInfoChunks, this.dimensionId, this.worldSaveDir, spawnLog, logger);
	    
	    if(this.isBO4Enabled)
	    {
	    	plotter.saveStructureCache(this.worldSaveDir, this.dimensionId, this.isBO4Enabled, spawnLog, logger);
	    }
    }

	private void loadStructureCache(Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{		
		logger.log(LogMarker.DEBUG, "Loading structures and pre-generator data");

        this.worldInfoChunks = new HashMap<ChunkCoordinate, StructureDataRegion>();
		
    	Map<CustomStructure, ArrayList<ChunkCoordinate>> loadedStructures = CustomStructureFileManager.loadStructureData(this.worldName, this.worldSaveDir, this.dimensionId, this.worldSeed, this.isBO4Enabled, otgRootFolder, spawnLog, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		if(loadedStructures != null)
		{
	        if(this.isBO4Enabled)
	        {
	        	this.plotter.loadStructureCache(this.worldSaveDir, this.dimensionId, this.isBO4Enabled, loadedStructures, spawnLog, logger);
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

				for(ModDataFunction<?> modDataFunc : loadedStructure.getKey().modDataManager.modData)
				{
					addToWorldInfoChunks(loadedStructure.getKey(), ChunkCoordinate.fromBlockCoords(modDataFunc.x, modDataFunc.z), false);
				}

				for(SpawnerFunction<?> spawnerFunc : loadedStructure.getKey().spawnerManager.spawnerData)
				{
					addToWorldInfoChunks(loadedStructure.getKey(), ChunkCoordinate.fromBlockCoords(spawnerFunc.x, spawnerFunc.z), false);
				}

				for(ParticleFunction<?> particleFunc : loadedStructure.getKey().particlesManager.particleData)
				{
					addToWorldInfoChunks(loadedStructure.getKey(), ChunkCoordinate.fromBlockCoords(particleFunc.x, particleFunc.z), false);
				}
			}
		}

		logger.log(LogMarker.DEBUG, "Loading done");
	}
}
