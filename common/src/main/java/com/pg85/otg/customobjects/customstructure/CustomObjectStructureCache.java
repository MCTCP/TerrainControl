package com.pg85.otg.customobjects.customstructure;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.customobjects.bo3.bo3function.ModDataFunction;
import com.pg85.otg.customobjects.bo3.bo3function.ParticleFunction;
import com.pg85.otg.customobjects.bo3.bo3function.SpawnerFunction;
import com.pg85.otg.generator.resource.CustomStructureGen;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.RandomHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Each world has a cache of unfinished structures. This class is the cache.
 *
 */
public class CustomObjectStructureCache
{
	// OTG+

	// Key not present in structurecache == was never populated or plotted
	// Key is present and Value is CustomObjectStructure with null as Start == plotted as emtpy chunk (this chunk was populated but no BO3 was plotted on it so only add trees, lakes, ores etc)
	// Key is present and Value is CustomObjectStructure with non-null as Start == plotted with BO3
	// Key is present and Value is null == plotted and spawned
	// If a chunk of a CustomObjectStructure has been spawned then the CustomObjectStructure's SmoothingAreasToSpawn and ObjectsToSpawn entries for that chunk
	// have been removed (cleans up cache and makes sure nothing is ever spawned twice, although a second spawn call should never be made in the first place ofc ><).

	// WorldInfo holds info on all BO3's ever spawned for this world, structurecache only holds those outside the pregenerated area and sets spawned chunks to null.
	
    public Map<ChunkCoordinate, CustomObjectStructure> structureCache;
	// Used for the /otg BO3 command, stores information about every BO3 that has been spawned so that author and description information can be requested by chunk.
	public Map<ChunkCoordinate, CustomObjectStructure> worldInfoChunks; 	
	
    private LocalWorld world;
    private CustomObjectStructurePlotter plotter;
    
    public CustomObjectStructureCache(LocalWorld world)
    {
        this.world = world;
        this.structureCache = new HashMap<ChunkCoordinate, CustomObjectStructure>();
        this.worldInfoChunks = new HashMap<ChunkCoordinate, CustomObjectStructure>();        
        this.plotter = new CustomObjectStructurePlotter();
        
        LoadStructureCache();
    }
    
    public CustomObjectStructurePlotter getPlotter()
    {
    	return this.plotter;
    }
    
    public void PlotStructures(Random rand, ChunkCoordinate chunkCoord, boolean spawningStructureAtSpawn)
    {
    	plotter.PlotStructures(this.world, rand, chunkCoord, spawningStructureAtSpawn, this.structureCache, this.worldInfoChunks);
    }

    public void reload(LocalWorld world)
    {
    	// Only used for Bukkit?
        this.world = world;
        structureCache.clear();
    }

    public CustomObjectStructure getStructureStart(Random worldRandom, int chunkX, int chunkZ)
    {
    	if(world.getConfigs().getWorldConfig().IsOTGPlus)
    	{
    		throw new RuntimeException();
    	} else {
	        ChunkCoordinate coord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
	        CustomObjectStructure structureStart = structureCache.get(coord);

	        // Clear cache if needed
	        if (structureCache.size() > 400)
	        {
	            structureCache.clear();
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
	            CustomObjectCoordinate customObject = structureGen.getRandomObjectCoordinate(world, random, chunkX, chunkZ);
	            if (customObject != null)
	            {
	                structureStart = new CustomObjectStructure(worldRandom, world, customObject);
	                structureCache.put(coord, structureStart);
	                return structureStart;
	            }	        }

	        return null;
    	}
    }

    // persistence stuff

    public void CompressCache()
    {
    	OTG.log(LogMarker.DEBUG, "Compressing structure-cache and pre-generator data");

    	// If a chunk in the structurecache is inside the outermost ring of
    	// chunks in the pre-generated area then it can be safely removed

    	int structuresRemoved = 0;
        int a = 0;
    	
    	// Fill a new structureCache based on the  existing one, remove all the chunks inside the pregenerated region that we know will no longer be used
    	HashMap<ChunkCoordinate, CustomObjectStructure> newStructureCache = new HashMap<ChunkCoordinate,CustomObjectStructure>();
    	for (Map.Entry<ChunkCoordinate, CustomObjectStructure> cachedChunk : structureCache.entrySet())
    	{
			// If this structure is not done spawning or on/outside the border of the pre-generated area then keep it
			if(!world.IsInsidePregeneratedRegion(cachedChunk.getKey()))
			{
				newStructureCache.put(cachedChunk.getKey(), cachedChunk.getValue());
			} else {

				structuresRemoved += 1;

				// Null means fully populated, plotted and spawned
				if(cachedChunk.getValue() != null)
				{
					a++;
					OTG.log(LogMarker.FATAL, "Running " + world.GetWorldSession().getPreGeneratorIsRunning() +  " L" + world.GetWorldSession().getPregeneratedBorderLeft() + " R" + world.GetWorldSession().getPregeneratedBorderRight() + " T" + world.GetWorldSession().getPregeneratedBorderTop() + " B" + world.GetWorldSession().getPregeneratedBorderBottom());
					OTG.log(LogMarker.FATAL, "Error at Chunk X" + cachedChunk.getKey().getChunkX() + " Z" + cachedChunk.getKey().getChunkZ() + ". " + (!this.structureCache.containsKey(cachedChunk.getKey()) ? (world.IsInsidePregeneratedRegion(cachedChunk.getKey()) ? "Inside pregenned region" : "Not plotted") : this.structureCache.get(cachedChunk.getKey()) == null ? "Plotted and spawned" : this.structureCache.get(cachedChunk.getKey()).Start != null ? this.structureCache.get(cachedChunk.getKey()).Start.BO3Name : "Trees"));

					//throw new RuntimeException();
				}
			}
    	}
    	if(a > 0)
    	{
    		throw new RuntimeException();
    	}

    	structureCache = newStructureCache;

    	OTG.log(LogMarker.DEBUG, "Removed " + structuresRemoved + " cached chunks");
    }

    public void SaveToDisk()
    {
    	OTG.log(LogMarker.DEBUG, "Saving structure data");
    	int i = 0;
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
			if(i == 0 || i == 100)
			{
				OTG.log(LogMarker.WARN, "SaveToDisk waiting on Populate. Although other mods could be causing this and there may not be any problem, this can potentially cause an endless loop!");
				i = 0;
			}
			i += 1;
			if(System.currentTimeMillis() - starTime > (300 * 1000))
			{
				OTG.log(LogMarker.FATAL, "SaveToDisk waited on populate longer than 300 seconds, something went wrong!");
				throw new RuntimeException("SaveToDisk waited on populate longer than 300 seconds, something went wrong!");
			}
		}

		if(world.getConfigs().getWorldConfig().IsOTGPlus)
		{
			CompressCache();
		}
		SaveStructureCache();

		synchronized(world.getObjectSpawner().lockingObject)
		{
	    	world.getObjectSpawner().saveRequired = false;
	    	world.getObjectSpawner().saving = false;
		}
    }

    private void SaveStructureCache()
    {
    	OTG.log(LogMarker.DEBUG, "Saving structures and pre-generator data");

	    Map<ChunkCoordinate, CustomObjectStructure> worldInfoChunksToSave = new HashMap<ChunkCoordinate, CustomObjectStructure>();

	    for (Map.Entry<ChunkCoordinate, CustomObjectStructure> cachedChunk : worldInfoChunks.entrySet()) // WorldInfo holds info on all BO3's ever spawned for this world, structurecache only holds those outside the pregenerated area and sets spawned chunks to null!
	    {
	    	if(cachedChunk.getValue() != null)
	    	{
	    		worldInfoChunksToSave.put(cachedChunk.getKey(), cachedChunk.getValue());
	    	} else {
	    		throw new RuntimeException();
	    	}
	    }

	    CustomObjectStructureFileManager.SaveStructuresFile(worldInfoChunksToSave, this.world);

	    for (Map.Entry<ChunkCoordinate, CustomObjectStructure> cachedChunk : worldInfoChunks.entrySet())
	    {
	    	if(cachedChunk.getValue() != null)
	    	{
	    		cachedChunk.getValue().saveRequired = false;
	    	}
	    }

	    if(world.getConfigs().getWorldConfig().IsOTGPlus)
	    {
		    ArrayList<ChunkCoordinate> nullChunks = new ArrayList<ChunkCoordinate>();
	    	for (Map.Entry<ChunkCoordinate, CustomObjectStructure> cachedChunk : structureCache.entrySet()) // Save null chunks from structurecache so that when loading we can reconstitute it based on worldInfoChunks, null chunks and the pregenerator border
	    	{
	    		if(cachedChunk.getValue() == null)
	    		{
	    			if(!world.IsInsidePregeneratedRegion(cachedChunk.getKey()))
	    			{
	    				nullChunks.add(cachedChunk.getKey());
					}
	    		}
	    	}

	    	CustomObjectStructureFileManager.SaveChunksFile(nullChunks, "NullChunks.txt", this.world);

	    	this.plotter.saveSpawnedStructures(this.world);
	    }

		OTG.log(LogMarker.DEBUG, "Saving done");
    }

	private void LoadStructureCache()
	{
		OTG.log(LogMarker.DEBUG, "Loading structures and pre-generator data");

    	int structuresLoaded = 0;

		Map<ChunkCoordinate, CustomObjectStructure> loadedStructures = CustomObjectStructureFileManager.LoadStructuresFile(this.world);

		for(Map.Entry<ChunkCoordinate, CustomObjectStructure> loadedStructure : loadedStructures.entrySet())
		{
			structuresLoaded += 1;

			if(loadedStructure == null)
			{
				throw new RuntimeException();
			}

			worldInfoChunks.put(loadedStructure.getKey(), loadedStructure.getValue());

			if(world.getConfigs().getWorldConfig().IsOTGPlus)
			{
				if(!world.IsInsidePregeneratedRegion(loadedStructure.getKey()) && !structureCache.containsKey(loadedStructure.getKey())) // Dont override any loaded structures that have been added to the structure cache
				{
					// This chunk is either
					// A. outside the border and has no objects to spawn (empty chunk) but has not yet been populated
					// B. Part of but not the starting point of a branching structure, therefore the structure's ObjectsToSpawn and SmoothingAreasToSpawn were not saved with this file.
					structureCache.put(loadedStructure.getKey(), loadedStructure.getValue());
				}

				// The starting structure in a branching structure is saved with the ObjectsToSpawn, SmoothingAreasToSpawn & modData of all its branches.
				// All branches are saved as individual structures but without any ObjectsToSpawn/SmoothingAreasToSpawn/modData (only essential data for structure placement remains).
				// The starting structure overrides any empty branches that were added as structures here if it has any ObjectsToSpawn/SmoothingAreasToSpawn/modData in their chunks.

				for(ChunkCoordinate chunkCoord : loadedStructure.getValue().ObjectsToSpawn.keySet())
				{
					if(!world.IsInsidePregeneratedRegion(chunkCoord))
					{
						structureCache.put(chunkCoord, loadedStructure.getValue()); // This structure has BO3 blocks that need to be spawned
					} else {
						throw new RuntimeException();
					}
				}
				for(ChunkCoordinate chunkCoord : loadedStructure.getValue().SmoothingAreasToSpawn.keySet())
				{
					if(!world.IsInsidePregeneratedRegion(chunkCoord))
					{
						structureCache.put(chunkCoord, loadedStructure.getValue()); // This structure has smoothing area blocks that need to be spawned
					} else {
						throw new RuntimeException();
					}
				}
			}

			for(ModDataFunction modDataFunc : loadedStructure.getValue().modDataManager.modData)
			{
				worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(modDataFunc.x, modDataFunc.z), loadedStructure.getValue());
			}

			for(SpawnerFunction spawnerFunc : loadedStructure.getValue().spawnerManager.spawnerData)
			{
				worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(spawnerFunc.x, spawnerFunc.z), loadedStructure.getValue());
			}

			for(ParticleFunction particleFunc : loadedStructure.getValue().particlesManager.particleData)
			{
				worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(particleFunc.x, particleFunc.z), loadedStructure.getValue());
			}
		}

		OTG.log(LogMarker.DEBUG, "Loaded " + structuresLoaded + " structure chunks");

		if(world.getConfigs().getWorldConfig().IsOTGPlus)
		{
			ArrayList<ChunkCoordinate> nullChunks = CustomObjectStructureFileManager.LoadChunksFile("NullChunks.txt", this.world);
			for(ChunkCoordinate chunkCoord : nullChunks)
			{
				structureCache.remove(chunkCoord);
				if(!world.IsInsidePregeneratedRegion(chunkCoord))
				{
					structureCache.put(chunkCoord, null); // This chunk has been completely populated and spawned
				} else {

					// This should only happen when a world is loaded that was generated with a PregenerationRadius of 0 and then had its PregenerationRadius increased
					// TODO: This never seems to happen?
					OTG.log(LogMarker.FATAL, "Running " + world.GetWorldSession().getPreGeneratorIsRunning() +  " L" + world.GetWorldSession().getPregeneratedBorderLeft() + " R" + world.GetWorldSession().getPregeneratedBorderRight() + " T" + world.GetWorldSession().getPregeneratedBorderTop() + " B" + world.GetWorldSession().getPregeneratedBorderBottom());
					OTG.log(LogMarker.FATAL, "Error at Chunk X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ());
					throw new RuntimeException("Error at Chunk X" + chunkCoord.getChunkX() + " Z" + chunkCoord.getChunkZ());
				}
			}

			plotter.loadSpawnedStructures(this.world);

			for(ChunkCoordinate chunkCoord : structureCache.keySet())
			{
				plotter.AddToStructuresPerChunkCache(chunkCoord, new ArrayList<String>()); // This is an optimisation so that PlotStructures knows not to plot anything in this chunk
			}

			if(loadedStructures.size() > 0 || nullChunks.size() > 0 || plotter.getStructureCount() > 0)
			{
				world.getObjectSpawner().StructurePlottedAtSpawn = true;
			}
		}

		OTG.log(LogMarker.DEBUG, "Loading done");
	}
}
