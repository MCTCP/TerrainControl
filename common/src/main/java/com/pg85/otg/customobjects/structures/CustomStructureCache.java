package com.pg85.otg.customobjects.structures;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorldGenRegion;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.customobjects.bofunctions.SpawnerFunction;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructure;
import com.pg85.otg.customobjects.structures.bo4.CustomStructurePlotter;
import com.pg85.otg.gen.resource.CustomStructureGen;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.helpers.RandomHelper;

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
	public static final int REGION_SIZE = 100;
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

    public CustomStructureCache(String worldName, Path worldSaveDir, int dimensionId, long worldSeed, boolean isBO4Enabled)
    {
        this.worldInfoChunks = new HashMap<ChunkCoordinate, StructureDataRegion>();
        this.plotter = new CustomStructurePlotter();
        this.bo3StructureCache = new FifoMap<ChunkCoordinate, BO3CustomStructure>(400);
        this.worldSaveDir = worldSaveDir;
        this.isBO4Enabled = isBO4Enabled;
        this.dimensionId = dimensionId;
        this.worldName = worldName;
        this.worldSeed = worldSeed;
        loadStructureCache();
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

    public BO3CustomStructure getBo3StructureStart(LocalWorldGenRegion worldGenRegion, Random worldRandom, int chunkX, int chunkZ)
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
       	BiomeConfig biomeConfig = worldGenRegion.getBiomeConfig(chunkX * 16 + 15, chunkZ * 16 + 15);
       	CustomStructureGen structureGen = biomeConfig.structureGen;

        if (structureGen != null)
        {
            BO3CustomStructureCoordinate customObject = structureGen.getRandomObjectCoordinate(worldGenRegion, random, chunkX, chunkZ);
            if (customObject != null)
            {
                structureStart = new BO3CustomStructure(worldGenRegion, customObject);
                bo3StructureCache.put(chunkCoord, structureStart);
                return structureStart;
            }
        }
        bo3StructureCache.put(chunkCoord, new BO3CustomStructure(null));
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
    public void plotBo4Structures(LocalWorldGenRegion worldGenRegion, Random rand, ChunkCoordinate chunkCoord)
    {
    	plotter.plotStructures(this, worldGenRegion, rand, chunkCoord);
    }

    // Only used by ObjectSpawner during population
	public void spawnBo4Chunk(LocalWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated)
	{
		this.plotter.spawnBO4Chunk(chunkCoord, this, worldGenRegion, chunkBeingPopulated);
	}
	
	// Only used by /spawn command	
    public ChunkCoordinate plotBo4Structure(LocalWorldGenRegion worldGenRegion, BO4 structure, ArrayList<String> biomes, ChunkCoordinate chunkCoord)
    {
    	plotter.plotStructures(structure, biomes, this, worldGenRegion, new Random(), chunkCoord);
    	return null;
    }

    // Persistence - WorldInfoChunks for BO3+BO4, plotter structurecache for BO4

    public void saveToDisk()
    {
    	OTG.log(LogMarker.INFO, "Saving structure and pregenerator data.");
    	boolean firstLog = false;
    	long starTime = System.currentTimeMillis();

		saveStructureCache();

		OTG.log(LogMarker.INFO, "Structure and pregenerator data saved.");
    }

    private void saveStructureCache()
    {
	    CustomStructureFileManager.saveStructureData(this.worldInfoChunks, this.dimensionId, this.worldSaveDir);
	    
	    if(this.isBO4Enabled)
	    {
	    	plotter.saveStructureCache(this.worldSaveDir, this.dimensionId, this.isBO4Enabled);
	    }
    }

	private void loadStructureCache()
	{		
		OTG.log(LogMarker.DEBUG, "Loading structures and pre-generator data");

        this.worldInfoChunks = new HashMap<ChunkCoordinate, StructureDataRegion>();
		
    	Map<CustomStructure, ArrayList<ChunkCoordinate>> loadedStructures = CustomStructureFileManager.loadStructureData(this.worldName, this.worldSaveDir, this.dimensionId, this.worldSeed, this.isBO4Enabled);
		if(loadedStructures != null)
		{
	        if(this.isBO4Enabled)
	        {
	        	this.plotter.loadStructureCache(this.worldSaveDir, this.dimensionId, this.isBO4Enabled, loadedStructures);
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

		OTG.log(LogMarker.DEBUG, "Loading done");
	}
}
