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

// TODO: spawners/particles/moddata for customobjects also use this, so not just structures. refactor?
/**
 * Each world has a CustomObjectStructureCache with data for spawned and unfinished structures
 */
public class CustomStructureCache
{
	// BO3	
	private FifoMap<ChunkCoordinate, BO3CustomStructure> bo3StructureCache;
	
	// BO4
    private CustomStructurePlotter plotter;

    // Common

    private LocalWorld world;

	// WorldInfo holds info on all structures ever spawned for this world
    // BO3: Used for /otg structure and spawners/particles/moddata (also used or Bo3 customobjects).
    // BO4: Used for finding distance, /otg structure and spawners/particles/moddata.
	// Also used to make sure other resources like lakes don't spawn on top of structures.
    private Map<ChunkCoordinate, CustomStructure> worldInfoChunks;

    public CustomStructureCache(LocalWorld world)
    {
        this.world = world;
        if(this.world.isBo4Enabled())
        {
	        this.plotter = new CustomStructurePlotter();
	        loadStructureCache();
        } else {
            this.bo3StructureCache = new FifoMap<ChunkCoordinate, BO3CustomStructure>(400);
        }
    }

    private void addToWorldInfoChunks(ChunkCoordinate chunkCoord, CustomStructure structure, boolean canOverride)
    {
		// Add Structure to worldInfoChunks for /otg structure and spawners/particles/moddata
		// Make sure not to override any ModData/Spawner/Particle data added by CustomObjects
		if(this.worldInfoChunks.containsKey(chunkCoord))
		{
			CustomStructure existingObject = world.getStructureCache().worldInfoChunks.get(chunkCoord);
			structure.modDataManager.modData.addAll(existingObject.modDataManager.modData);
			structure.particlesManager.particleData.addAll(existingObject.particlesManager.particleData);
			structure.spawnerManager.spawnerData.addAll(existingObject.spawnerManager.spawnerData);
			if(canOverride)
			{
				this.worldInfoChunks.put(chunkCoord, structure);	
			}
		} else {
			this.worldInfoChunks.put(chunkCoord, structure);	
		}		
    }
    
    public boolean chunkHasData(ChunkCoordinate chunkCoord)
    {
		return this.worldInfoChunks.containsKey(chunkCoord);
    }
    
    // TODO: This is only used for fetching spawner/particles/moddata,
    // doesn't really need to know about structure. refactor?
    public CustomStructure getChunkData(ChunkCoordinate chunkCoord)
    {
		return this.worldInfoChunks.get(chunkCoord);
    }
    
    // BO3 methods
    
	// Only used for Bukkit
    // TODO: Document this, remove if possible.
    public void reloadBo3StructureCache(LocalWorld world)
    {
        this.world = world;
        this.bo3StructureCache.clear();
    }
    
    public BO3CustomStructure getBo3StructureStart(Random worldRandom, int chunkX, int chunkZ)
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
    
	public void addBo3ToStructureCache(ChunkCoordinate chunkCoord, CustomStructure structure, boolean canOverride)
	{
		addToWorldInfoChunks(chunkCoord, structure, canOverride);
	}
    
    // BO4 methods

	// Used while calculating branches
	public boolean isBo4ChunkPlotted(LocalWorld world, ChunkCoordinate chunkCoordinate)
	{
		return this.plotter.isBo4ChunkPlotted(world, chunkCoordinate);
	}
	
	// Called when branches have been calculated
	public void addBo4ToStructureCache(ChunkCoordinate chunkCoord, BO4CustomStructure structure)
	{
		this.plotter.addBo4ToStructureCache(chunkCoord, structure);		
		addToWorldInfoChunks(chunkCoord, structure, true);		
	}

    // Only used by other resources like lakes 
    public boolean isBo4ChunkOccupied(ChunkCoordinate chunkCoord)
    {
    	if(!world.isBo4Enabled())
    	{
    		return false;
    	} else {
    		return this.worldInfoChunks.containsKey(chunkCoord);
    	}
    }
    
    // Only used by ObjectSpawner
    public void plotBo4Structures(Random rand, ChunkCoordinate chunkCoord)
    {
    	CustomStructure spawnedStructure = plotter.plotStructures(this.world, rand, chunkCoord);
    	if(spawnedStructure != null)
    	{
    		// Always add the structure start to worldInfoChunks, since it might not have ObjectsToSpawn or SmoothingAreas in the given chunk,
    		// so it may not have been added after processing branches. We don't have to worry about overriding particles/spawner/moddata 
    		// added by others at this point, since CustomObject haven't had a chance to spawn in the chunk yet.
    		ChunkCoordinate spawnedCoord = ChunkCoordinate.fromChunkCoords(spawnedStructure.start.getChunkX(), spawnedStructure.start.getChunkZ());
    		this.worldInfoChunks.put(spawnedCoord, spawnedStructure);
    	}
    }    

    // Only used by ObjectSpawner
	public void spawnBo4Chunk(ChunkCoordinate chunkCoord, ChunkCoordinate chunkBeingPopulated)
	{
		this.plotter.spawnBO4Chunk(chunkCoord, this.world, chunkBeingPopulated);
	}
	
	// Only used for /spawn command	
    public ChunkCoordinate plotBo4Structure(BO4 structure, ArrayList<String> biomes, ChunkCoordinate chunkCoord)
    {
    	CustomStructure spawnedStructure = plotter.plotStructures(structure, biomes, this.world, new Random(), chunkCoord);
    	if(spawnedStructure != null)
    	{
    		// Always add the structure start to worldInfoChunks, since it might not have ObjectsToSpawn or SmoothingAreas in the given chunk,
    		// so it may not have been added after processing branches. We don't have to worry about overriding particles/spawner/moddata 
    		// added by others at this point, since CustomObject haven't had a chance to spawn in the chunk yet.
    		ChunkCoordinate spawnedCoord = ChunkCoordinate.fromChunkCoords(spawnedStructure.start.getChunkX(), spawnedStructure.start.getChunkZ());
    		this.worldInfoChunks.put(spawnedCoord, spawnedStructure);
    		return spawnedCoord;
    	}
    	return null;
    }

    // Persistence - WorldInfoChunks for BO3+BO4, plotter structurecache for BO4

    public void saveToDisk()
    {
    	OTG.log(LogMarker.DEBUG, "Saving structure data");
    	boolean firstLog = false;
    	long starTime = System.currentTimeMillis();
		while(true)
		{
			synchronized(this.world.getObjectSpawner().lockingObject)
			{
				if(!this.world.getObjectSpawner().populating)
				{
					this.world.getObjectSpawner().saving = true;
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

		if(this.world.isBo4Enabled())
		{
			this.plotter.compressCache(this.world);
		}
		saveStructureCache();

		synchronized(this.world.getObjectSpawner().lockingObject)
		{
			this.world.getObjectSpawner().saveRequired = false;
			this.world.getObjectSpawner().saving = false;
		}
    }
    
	public void compressCache()
	{
		this.plotter.compressCache(this.world);		
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
	    
	    if(this.world.isBo4Enabled())
	    {
	    	plotter.saveStructureCache(this.world);
	    }

		OTG.log(LogMarker.DEBUG, "Saving done");
    }

	private void loadStructureCache()
	{
		OTG.log(LogMarker.DEBUG, "Loading structures and pre-generator data");

        this.worldInfoChunks = new HashMap<ChunkCoordinate, CustomStructure>();
		
        int structuresLoaded = 0;
	
		Map<ChunkCoordinate, CustomStructure> loadedStructures = CustomStructureFileManager.loadStructuresFile(this.world);
		if(loadedStructures != null)
		{
	        if(this.world.isBo4Enabled())
	        {
	        	this.plotter.loadStructureCache(this.world, loadedStructures);
	        }
			
			for(Map.Entry<ChunkCoordinate, CustomStructure> loadedStructure : loadedStructures.entrySet())
			{
				structuresLoaded += 1;
	
				if(loadedStructure == null)
				{
					throw new RuntimeException("This shouldn't happen, please ask for help on the OTG Discord and/or file an issue on the OTG github.");
				}
	
				this.worldInfoChunks.put(loadedStructure.getKey(), loadedStructure.getValue());
	
				for(ModDataFunction<?> modDataFunc : loadedStructure.getValue().modDataManager.modData)
				{
					this.worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(modDataFunc.x, modDataFunc.z), loadedStructure.getValue());
				}
	
				for(SpawnerFunction<?> spawnerFunc : loadedStructure.getValue().spawnerManager.spawnerData)
				{
					this.worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(spawnerFunc.x, spawnerFunc.z), loadedStructure.getValue());
				}
	
				for(ParticleFunction<?> particleFunc : loadedStructure.getValue().particlesManager.particleData)
				{
					this.worldInfoChunks.put(ChunkCoordinate.fromBlockCoords(particleFunc.x, particleFunc.z), loadedStructure.getValue());
				}
			}
		}
		OTG.log(LogMarker.DEBUG, "Loaded " + structuresLoaded + " structure chunks");

		OTG.log(LogMarker.DEBUG, "Loading done");
	}
}
