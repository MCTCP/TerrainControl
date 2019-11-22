package com.pg85.otg.customobjects.structures;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.WorldStandardValues;
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
	// Key not present in structurecache == was never populated or plotted
	// Key is present and Value is CustomObjectStructure with null as Start == plotted as empty chunk (this chunk was populated but no BO3 was plotted on it so only add trees, lakes, ores etc)
	// Key is present and Value is CustomObjectStructure with non-null as Start == plotted with BO3
	// Key is present and Value is null == plotted and spawned
	// If a chunk of a CustomObjectStructure has been spawned then the CustomObjectStructure's SmoothingAreasToSpawn and ObjectsToSpawn entries for that chunk
	// have been removed (cleans up cache and makes sure nothing is ever spawned twice, although a second spawn call should never be made in the first place ofc ><).

	// WorldInfo holds info on all BO3's ever spawned for this world, structurecache only holds those outside the pregenerated area and sets spawned chunks to null.
	
	private Map<ChunkCoordinate, BO3CustomStructure> bo3StructureCache;
    public Map<ChunkCoordinate, BO4CustomStructure> bo4StructureCache;
    
	// Used for the /otg BO3 command, stores information about every BO3 that has been spawned so that author and description information can be requested by chunk.
    // Also used to store location of spawners/particles/moddata.
	public Map<ChunkCoordinate, CustomStructure> worldInfoChunks; 	
	
    private LocalWorld world;
    private CustomStructurePlotter plotter;
    
    public CustomStructureCache(LocalWorld world)
    {
        this.world = world;
        this.bo3StructureCache = new HashMap<ChunkCoordinate, BO3CustomStructure>();
        this.bo4StructureCache = new HashMap<ChunkCoordinate, BO4CustomStructure>();
        this.worldInfoChunks = new HashMap<ChunkCoordinate, CustomStructure>();        
        this.plotter = new CustomStructurePlotter();
        
        loadStructureCache();
    }
    
    public CustomStructurePlotter getPlotter()
    {
    	return this.plotter;
    }
    
    public void plotStructures(Random rand, ChunkCoordinate chunkCoord, boolean spawningStructureAtSpawn)
    {
    	plotter.plotStructures(this.world, rand, chunkCoord, spawningStructureAtSpawn, this.bo4StructureCache, this.worldInfoChunks);
    }

    public void reload(LocalWorld world)
    {
    	// Only used for Bukkit?
        this.world = world;
        bo3StructureCache.clear();
    }

    // Only used for OTG Customstructure
    public CustomStructure getStructureStart(Random worldRandom, int chunkX, int chunkZ)
    {
        ChunkCoordinate coord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
        BO3CustomStructure structureStart = bo3StructureCache.get(coord);

        // Clear cache if needed
        if (bo3StructureCache.size() > 400)
        {
        	bo3StructureCache.clear();
        }

        if (structureStart != null)
        {
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
                bo3StructureCache.put(coord, structureStart);
                return structureStart;
            }	        
        }

        return null;
    }

    // persistence

    public void compressCache()
    {
    	OTG.log(LogMarker.INFO, "Compressing structure-cache and pre-generator data");

    	// If a chunk in the structurecache is inside the outermost ring of
    	// chunks in the pre-generated area then it can be safely removed

    	int structuresRemoved = 0;
        int a = 0;
    	
    	// Fill a new structureCache based on the  existing one, remove all the chunks inside the pregenerated region that we know will no longer be used
    	HashMap<ChunkCoordinate, BO4CustomStructure> newStructureCache = new HashMap<ChunkCoordinate, BO4CustomStructure>();
    	for (Map.Entry<ChunkCoordinate, BO4CustomStructure> cachedChunk : bo4StructureCache.entrySet())
    	{
			// If this structure is not done spawning or on/outside the border of the pre-generated area then keep it
			if(!world.isInsidePregeneratedRegion(cachedChunk.getKey()))
			{
				newStructureCache.put(cachedChunk.getKey(), cachedChunk.getValue());
			} else {

				structuresRemoved += 1;

				// Null means fully populated, plotted and spawned
				if(cachedChunk.getValue() != null)
				{
					a++;
					OTG.log(LogMarker.FATAL, "Running " + world.getWorldSession().getPreGeneratorIsRunning() +  " L" + world.getWorldSession().getPregeneratedBorderLeft() + " R" + world.getWorldSession().getPregeneratedBorderRight() + " T" + world.getWorldSession().getPregeneratedBorderTop() + " B" + world.getWorldSession().getPregeneratedBorderBottom());
					OTG.log(LogMarker.FATAL, "Error at Chunk X" + cachedChunk.getKey().getChunkX() + " Z" + cachedChunk.getKey().getChunkZ() + ". " + (!this.bo4StructureCache.containsKey(cachedChunk.getKey()) ? (world.isInsidePregeneratedRegion(cachedChunk.getKey()) ? "Inside pregenned region" : "Not plotted") : this.bo4StructureCache.get(cachedChunk.getKey()) == null ? "Plotted and spawned" : this.bo4StructureCache.get(cachedChunk.getKey()).start != null ? this.bo4StructureCache.get(cachedChunk.getKey()).start.bo3Name : "Trees"));

					//throw new RuntimeException();
				}
			}
    	}
    	if(a > 0)
    	{
    		throw new RuntimeException();
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

		if(world.getConfigs().getWorldConfig().isOTGPlus)
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

	    if(world.getConfigs().getWorldConfig().isOTGPlus)
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

	    	CustomStructureFileManager.saveChunksFile(nullChunks, WorldStandardValues.NullChunksFileName, this.world);

	    	this.plotter.saveSpawnedStructures(this.world);
	    }

		OTG.log(LogMarker.DEBUG, "Saving done");
    }

	private void loadStructureCache()
	{
		OTG.log(LogMarker.DEBUG, "Loading structures and pre-generator data");

    	int structuresLoaded = 0;

		Map<ChunkCoordinate, CustomStructure> loadedStructures = CustomStructureFileManager.loadStructuresFile(this.world);

		for(Map.Entry<ChunkCoordinate, CustomStructure> loadedStructure : loadedStructures.entrySet())
		{
			structuresLoaded += 1;

			if(loadedStructure == null)
			{
				throw new RuntimeException();
			}

			worldInfoChunks.put(loadedStructure.getKey(), loadedStructure.getValue());

			if(world.getConfigs().getWorldConfig().isOTGPlus)
			{
				if(!world.isInsidePregeneratedRegion(loadedStructure.getKey()) && !bo4StructureCache.containsKey(loadedStructure.getKey())) // Dont override any loaded structures that have been added to the structure cache
				{
					// This chunk is either
					// A. outside the border and has no objects to spawn (empty chunk) but has not yet been populated
					// B. Part of but not the starting point of a branching structure, therefore the structure's ObjectsToSpawn and SmoothingAreasToSpawn were not saved with this file.
					bo4StructureCache.put(loadedStructure.getKey(), (BO4CustomStructure)loadedStructure.getValue());
				}

				// The starting structure in a branching structure is saved with the ObjectsToSpawn, SmoothingAreasToSpawn & modData of all its branches.
				// All branches are saved as individual structures but without any ObjectsToSpawn/SmoothingAreasToSpawn/modData (only essential data for structure placement remains).
				// The starting structure overrides any empty branches that were added as structures here if it has any ObjectsToSpawn/SmoothingAreasToSpawn/modData in their chunks.
				for(ChunkCoordinate chunkCoord : ((BO4CustomStructure)loadedStructure.getValue()).objectsToSpawn.keySet())
				{
					if(!world.isInsidePregeneratedRegion(chunkCoord))
					{
						bo4StructureCache.put(chunkCoord, (BO4CustomStructure)loadedStructure.getValue()); // This structure has BO3 blocks that need to be spawned
					} else {
						throw new RuntimeException();
					}
				}
				for(ChunkCoordinate chunkCoord : ((BO4CustomStructure)loadedStructure.getValue()).smoothingAreasToSpawn.keySet())
				{
					if(!world.isInsidePregeneratedRegion(chunkCoord))
					{
						bo4StructureCache.put(chunkCoord, (BO4CustomStructure)loadedStructure.getValue()); // This structure has smoothing area blocks that need to be spawned
					} else {
						throw new RuntimeException();
					}
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

		OTG.log(LogMarker.DEBUG, "Loaded " + structuresLoaded + " structure chunks");

		if(world.getConfigs().getWorldConfig().isOTGPlus)
		{
			ArrayList<ChunkCoordinate> nullChunks = CustomStructureFileManager.loadChunksFile(WorldStandardValues.NullChunksFileName, this.world);
			for(ChunkCoordinate chunkCoord : nullChunks)
			{
				bo4StructureCache.remove(chunkCoord);
				if(!world.isInsidePregeneratedRegion(chunkCoord))
				{
					bo4StructureCache.put(chunkCoord, null); // This chunk has been completely populated and spawned
				} else {

					// This should only happen when a world is loaded that was generated with a PregenerationRadius of 0 and then had its PregenerationRadius increased
					// TODO: This never seems to happen?
					OTG.log(LogMarker.FATAL, "Running " + world.getWorldSession().getPreGeneratorIsRunning() +  " L" + world.getWorldSession().getPregeneratedBorderLeft() + " R" + world.getWorldSession().getPregeneratedBorderRight() + " T" + world.getWorldSession().getPregeneratedBorderTop() + " B" + world.getWorldSession().getPregeneratedBorderBottom());
					OTG.log(LogMarker.FATAL, "Error at Chunk X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ());
					throw new RuntimeException("Error at Chunk X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ());
				}
			}

			plotter.loadSpawnedStructures(this.world);

			for(ChunkCoordinate chunkCoord : bo4StructureCache.keySet())
			{
				plotter.addToStructuresPerChunkCache(chunkCoord, new ArrayList<String>()); // This is an optimisation so that PlotStructures knows not to plot anything in this chunk
			}

			if(loadedStructures.size() > 0 || nullChunks.size() > 0 || plotter.getStructureCount() > 0)
			{
				world.getObjectSpawner().StructurePlottedAtSpawn = true;
			}
		}

		OTG.log(LogMarker.DEBUG, "Loading done");
	}
}
