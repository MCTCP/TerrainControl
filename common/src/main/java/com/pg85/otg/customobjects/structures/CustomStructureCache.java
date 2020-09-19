package com.pg85.otg.customobjects.structures;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.customobjects.bofunctions.ModDataFunction;
import com.pg85.otg.customobjects.bofunctions.ParticleFunction;
import com.pg85.otg.customobjects.bofunctions.SpawnerFunction;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructure;
import com.pg85.otg.customobjects.structures.bo4.CustomStructurePlotter;
import com.pg85.otg.generator.resource.CustomStructureGen;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.helpers.RandomHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Each world has a CustomObjectStructureCache with data for spawned and unfinished structures
 *
 */
public class CustomStructureCache
{	    
	// BO3	
	
	private FifoMap<ChunkCoordinate, BO3CustomStructure> bo3StructureCache;
	
	// BO4
       
	public boolean StructurePlottedAtSpawn;
	
    private CustomStructurePlotter plotter;
	
	// WorldInfo holds info on all structures ever spawned for this world, used for for finding distance, /otg structure and spawners/particles/moddata.
	// Also used to make sure other resources like lakes don't spawn on top of structures.
	public Map<ChunkCoordinate, CustomStructure> worldInfoChunks;
	
	// Structurecache only holds structures outside the pregenerated area and sets spawned chunks to null.
	// Key is present in structurecache == plotted or populated.	
	// Key not present in structurecache == was never plotted or populated
	// Key is present and value is CustomStructure with non-null as Start == plotted with BO4
	// *Key is present and value is CustomStructure with null as Start == shouldn't happen, can only happen for bo3structurecache?
	// Key is present and value is null == plotted and populated or plotted as empty chunk
	// If a chunk of a CustomStructure is spawned then the CustomObjectStructure's SmoothingAreasToSpawn 
	// and ObjectsToSpawn entries for that chunk are removed.	
    private Map<ChunkCoordinate, BO4CustomStructure> bo4StructureCache;

    // Common
    
    private LocalWorld world;
    
    public CustomStructureCache(LocalWorld world)
    {
        this.world = world;
        this.plotter = new CustomStructurePlotter();
        loadStructureCache();
    }    
    
    // BO3 methods
    
    public void reloadBo3StructureCache(LocalWorld world)
    {
    	// Only used for Bukkit?
        this.world = world;
        this.bo3StructureCache.clear();
    }
    
    public BO3CustomStructure getBO3StructureStart(Random worldRandom, int chunkX, int chunkZ)
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
        Random random = RandomHelper.getRandomForCoords(chunkX ^ 2, (chunkZ + 1) * 2, world.getSeed());
       	BiomeConfig biomeConfig = world.getBiome(chunkX * 16 + 15, chunkZ * 16 + 15).getBiomeConfig();
       	CustomStructureGen structureGen = biomeConfig.structureGen;
        
        if (structureGen != null)
        {
            BO3CustomStructureCoordinate customObject = structureGen.getRandomObjectCoordinate(world, random, chunkX, chunkZ);
            if (customObject != null)
            {
                structureStart = new BO3CustomStructure(world, customObject);
                bo3StructureCache.put(chunkCoord, structureStart);
                return structureStart;
            }
        }
        bo3StructureCache.put(chunkCoord, new BO3CustomStructure(null));
        return null;
    }    
    
    // BO4 methods
    
    public CustomStructurePlotter getPlotter()
    {
    	return this.plotter;
    }

    public ChunkCoordinate plotBo4Structure(BO4 structure, ArrayList<String> biomes, ChunkCoordinate chunkCoord)
    {
    	boolean spawned = plotter.plotStructures(structure, biomes, this.world, new Random(), chunkCoord, false, this.bo4StructureCache, this.worldInfoChunks);
    	if(spawned)
    	{
    		return chunkCoord;
    	}
    	return null;
    }
    
    public void plotBo4Structures(Random rand, ChunkCoordinate chunkCoord, boolean spawningStructureAtSpawn)
    {
    	plotter.plotStructures(this.world, rand, chunkCoord, spawningStructureAtSpawn, this.bo4StructureCache, this.worldInfoChunks);
    }    

    // Only used by other resources like lakes to cancel 
    // spawning if a BO4 has spawned in this chunk. 
    public boolean isBo4ChunkOccupied(ChunkCoordinate chunkCoord)
    {
    	if(!world.isBO4Enabled())
    	{
    		return false;
    	} else {
    		return this.worldInfoChunks.containsKey(chunkCoord);
    	}
    }

	public boolean isBo4ChunkPlotted(LocalWorld world, ChunkCoordinate chunkCoordinate)
	{
	    // Check if any other structures in world are in this chunk
		return
			world.isInsidePregeneratedRegion(chunkCoordinate) || 
			world.getStructureCache().bo4StructureCache.containsKey(chunkCoordinate)
		;
	}    

	public void finaliseBo4Chunk(ChunkCoordinate chunkCoord)
	{
		// All done spawning structures for this chunk, clean up cache
		// TODO: How would a chunk currently being spawned be inside the pregenerated region? xD
		if(!world.isInsidePregeneratedRegion(chunkCoord))
		{
			this.bo4StructureCache.put(chunkCoord, null);
		} else {
			this.bo4StructureCache.remove(chunkCoord);
		}

		// Let plotter know the chunk is taken
		this.plotter.invalidateChunkInStructuresPerChunkCache(chunkCoord);
	}

	public void addBo4ToStructureCache(ChunkCoordinate chunkCoord, BO4CustomStructure structure)
	{
		this.bo4StructureCache.put(chunkCoord, structure);
		
		// Let plotter know the chunk is taken
		this.plotter.invalidateChunkInStructuresPerChunkCache(chunkCoord);
	}

	public BO4CustomStructure getBo4FromStructureCache(ChunkCoordinate chunkCoord)
	{
		return this.bo4StructureCache.get(chunkCoord);
	}

    // BO4 Persistence

    public void compressCache()
    {
    	OTG.log(LogMarker.INFO, "Compressing structure-cache and pre-generator data");

    	// If a chunk in the structurecache is inside the outermost ring of
    	// chunks in the pre-generated area then it can be safely removed

    	int structuresRemoved = 0;
    	
    	// Fill a new structureCache based on the  existing one, remove all the chunks inside the pregenerated region that we know will no longer be used
    	HashMap<ChunkCoordinate, BO4CustomStructure> newStructureCache = new HashMap<ChunkCoordinate, BO4CustomStructure>();
    	for (Map.Entry<ChunkCoordinate, BO4CustomStructure> cachedChunk : bo4StructureCache.entrySet())
    	{
			if(!world.isInsidePregeneratedRegion(cachedChunk.getKey()))
			{
				newStructureCache.put(cachedChunk.getKey(), cachedChunk.getValue());
			} else {
				// If this structure is not done spawning or on/outside the border of the pre-generated area then keep it
				structuresRemoved += 1;
			}
    	}

    	bo4StructureCache = newStructureCache;

    	OTG.log(LogMarker.INFO, "Removed " + structuresRemoved + " cached chunks");
    }

    public void saveToDisk()
    {
    	OTG.log(LogMarker.DEBUG, "Saving structure data");
    	boolean firstLog = false;
    	long starTime = System.currentTimeMillis();
		while(true)
		{
			synchronized(world.getObjectSpawner().lockingObject)
			{
				if(!world.getObjectSpawner().populating)
				{
					world.getObjectSpawner().saving = true;
					break;
				}
			}
			if(firstLog)
			{
				OTG.log(LogMarker.WARN, "SaveToDisk waiting on Populate. Although other mods could be causing this and there may not be any problem, this can potentially cause an endless loop!");
				firstLog = false;
			}
			int interval = 300;
			if(System.currentTimeMillis() - starTime > (interval * 1000))
			{
				OTG.log(LogMarker.FATAL, "SaveToDisk waited on populate longer than " + interval + " seconds, something went wrong!");
				throw new RuntimeException("SaveToDisk waited on populate longer than " + interval + " seconds, something went wrong!");
			}
		}

		if(world.isBO4Enabled())
		{
			compressCache();
		}
		saveStructureCache();

		synchronized(world.getObjectSpawner().lockingObject)
		{
	    	world.getObjectSpawner().saveRequired = false;
	    	world.getObjectSpawner().saving = false;
		}
    }

    private void saveStructureCache()
    {
    	OTG.log(LogMarker.DEBUG, "Saving structures and pre-generator data");

	    Map<ChunkCoordinate, CustomStructure> worldInfoChunksToSave = new HashMap<ChunkCoordinate, CustomStructure>();

	    for (Map.Entry<ChunkCoordinate, CustomStructure> cachedChunk : worldInfoChunks.entrySet()) // WorldInfo holds info on all BO3's ever spawned for this world, structurecache only holds those outside the pregenerated area and sets spawned chunks to null!
	    {
	    	if(cachedChunk.getValue() != null)
	    	{
	    		worldInfoChunksToSave.put(cachedChunk.getKey(), cachedChunk.getValue());
	    	} else {
	    		throw new RuntimeException();
	    	}
	    }

	    CustomStructureFileManager.saveStructuresFile(worldInfoChunksToSave, this.world);
	    
	    if(this.world.isBO4Enabled())
	    {
		    ArrayList<ChunkCoordinate> nullChunks = new ArrayList<ChunkCoordinate>();
	    	for (Map.Entry<ChunkCoordinate, BO4CustomStructure> cachedChunk : bo4StructureCache.entrySet()) // Save null chunks from structurecache so that when loading we can reconstitute it based on worldInfoChunks, null chunks and the pregenerator border
	    	{
	    		if(cachedChunk.getValue() == null)
	    		{
	    			if(!world.isInsidePregeneratedRegion(cachedChunk.getKey()))
	    			{
	    				nullChunks.add(cachedChunk.getKey());
					}
	    		}
	    	}

	    	CustomStructureFileManager.saveNullChunksFile(nullChunks, this.world);

	    	this.plotter.saveSpawnedStructures(this.world);
	    }

		OTG.log(LogMarker.DEBUG, "Saving done");
    }

	private void loadStructureCache()
	{
		OTG.log(LogMarker.DEBUG, "Loading structures and pre-generator data");

        this.bo3StructureCache = new FifoMap<ChunkCoordinate, BO3CustomStructure>(400);
        this.bo4StructureCache = new HashMap<ChunkCoordinate, BO4CustomStructure>();
        this.worldInfoChunks = new HashMap<ChunkCoordinate, CustomStructure>();
		
    	int structuresLoaded = 0;

		Map<ChunkCoordinate, CustomStructure> loadedStructures = CustomStructureFileManager.loadStructuresFile(this.world);
		if(loadedStructures != null)
		{
			for(Map.Entry<ChunkCoordinate, CustomStructure> loadedStructure : loadedStructures.entrySet())
			{
				structuresLoaded += 1;
	
				if(loadedStructure == null)
				{
					throw new RuntimeException("This shouldn't happen, please ask for help on the OTG Discord and/or file an issue on the OTG github.");
				}
	
				worldInfoChunks.put(loadedStructure.getKey(), loadedStructure.getValue());
	
				if(world.isBO4Enabled())
				{
					// Dont override any loaded structures that have been added to the structure cache
					if(!bo4StructureCache.containsKey(loadedStructure.getKey())) 
					{
						// This chunk is either
						// A. outside the border and has no objects to spawn (empty chunk) but has not yet been populated.
						// B. Part of but not the starting point of a branching structure, therefore the structure's ObjectsToSpawn and SmoothingAreasToSpawn were not saved with this file.
						// This is used for the other caches
						bo4StructureCache.put(loadedStructure.getKey(), (BO4CustomStructure)loadedStructure.getValue());
					} else {
						//throw new RuntimeException();
					}
	
					// The starting structure in a branching structure is saved with the ObjectsToSpawn, SmoothingAreasToSpawn & modData of all its branches.
					// All branches are saved as individual structures but without any ObjectsToSpawn/SmoothingAreasToSpawn/modData (only essential data for structure placement remains).
					// The starting structure overrides any empty branches that were added as structures here if it has any ObjectsToSpawn/SmoothingAreasToSpawn/modData in their chunks.
					for(ChunkCoordinate chunkCoord : ((BO4CustomStructure)loadedStructure.getValue()).objectsToSpawn.keySet())
					{
						bo4StructureCache.put(chunkCoord, (BO4CustomStructure)loadedStructure.getValue()); // This structure has blocks that need to be spawned
					}
					for(ChunkCoordinate chunkCoord : ((BO4CustomStructure)loadedStructure.getValue()).smoothingAreasToSpawn.keySet())
					{
						bo4StructureCache.put(chunkCoord, (BO4CustomStructure)loadedStructure.getValue()); // This structure has smoothing area blocks that need to be spawned
					}
				}
	
				for(ModDataFunction<?> modDataFunc : loadedStructure.getValue().modDataManager.modData)
				{
					worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(modDataFunc.x, modDataFunc.z), loadedStructure.getValue());
				}
	
				for(SpawnerFunction<?> spawnerFunc : loadedStructure.getValue().spawnerManager.spawnerData)
				{
					worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(spawnerFunc.x, spawnerFunc.z), loadedStructure.getValue());
				}
	
				for(ParticleFunction<?> particleFunc : loadedStructure.getValue().particlesManager.particleData)
				{
					worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(particleFunc.x, particleFunc.z), loadedStructure.getValue());
				}
			}
		}

		OTG.log(LogMarker.DEBUG, "Loaded " + structuresLoaded + " structure chunks");

		if(world.isBO4Enabled())
		{
			ArrayList<ChunkCoordinate> nullChunks = CustomStructureFileManager.loadNullChunksFile(this.world);
			if(nullChunks != null)
			{
				for(ChunkCoordinate chunkCoord : nullChunks)
				{
					bo4StructureCache.put(chunkCoord, null); // This chunk has been completely populated and spawned
				}
			}

			plotter.loadSpawnedStructures(this.world);

			for(ChunkCoordinate chunkCoord : bo4StructureCache.keySet())
			{
				plotter.invalidateChunkInStructuresPerChunkCache(chunkCoord); // This is an optimisation so that PlotStructures knows not to plot anything in this chunk
			}

			if((loadedStructures != null && loadedStructures.size() > 0) || (nullChunks != null && nullChunks.size() > 0) || plotter.getStructureCount() > 0)
			{
				StructurePlottedAtSpawn = true;
			}
		}

		OTG.log(LogMarker.DEBUG, "Loading done");
	}
}
