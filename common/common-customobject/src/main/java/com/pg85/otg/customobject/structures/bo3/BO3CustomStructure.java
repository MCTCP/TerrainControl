package com.pg85.otg.customobject.structures.bo3;

import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.bo3.BO3;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.Branch;
import com.pg85.otg.customobject.structures.CustomStructure;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.customobject.structures.CustomStructureCoordinate;
import com.pg85.otg.customobject.structures.StructuredCustomObject;
import com.pg85.otg.customobject.util.StructurePartSpawnHeight;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.ICustomObject;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;
import com.pg85.otg.util.interfaces.IStructuredCustomObject;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents a collection of all {@link CustomObject}s in a structure. It is
 * calculated by finding the branches of one object, then finding the branches
 * of those branches, etc., until
 * {@link CustomObject#getMaxBranchDepth()} is reached.
 *
 */
public class BO3CustomStructure extends CustomStructure
{
	private StructurePartSpawnHeight height;
	private int maxBranchDepth;
    
    public BO3CustomStructure(BO3CustomStructureCoordinate start)
    {
    	this.start = start;
    }
    
    public BO3CustomStructure(IWorldGenRegion worldGenRegion, BO3CustomStructureCoordinate start, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        StructuredCustomObject object = (StructuredCustomObject)start.getObject(otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);

        if(object == null)
        {
        	return;
        }
    	if(!(object instanceof BO3))
		{
    		if(spawnLog)
    		{
    			logger.log(LogMarker.WARN, "BO3CustomStructure loaded with non-BO3 object " + object.getName());
    		}
    		return;
		}
        
        this.start = start;
        this.height = ((BO3)object).getStructurePartSpawnHeight();
        this.maxBranchDepth = ((BO3)object).getMaxBranchDepth();
        this.random = RandomHelper.getRandomForCoords(start.getX(), start.getY(), start.getZ(), worldGenRegion.getSeed());

        // Calculate all branches and add them to a list
        objectsToSpawn = new LinkedHashMap<ChunkCoordinate, Set<CustomStructureCoordinate>>();

        addToSpawnList((BO3CustomStructureCoordinate)start, object, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker); // Add the object itself
        addBranches((BO3CustomStructureCoordinate)start, 1, worldGenRegion, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
    }

    private void addBranches(BO3CustomStructureCoordinate coordObject, int depth, IWorldGenRegion worldGenRegion, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	IStructuredCustomObject object = coordObject.getObject(otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);

    	if(object != null)
    	{
	        for (Branch branch : getBranches(object, coordObject.getRotation()))
	        {
	        	// TODO: Does passing null as startbo3name work?
	        	BO3CustomStructureCoordinate childCoordObject = (BO3CustomStructureCoordinate)branch.toCustomObjectCoordinate(worldGenRegion.getWorldName(), random, coordObject.getRotation(), coordObject.getX(), coordObject.getY(), coordObject.getZ(), null, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);

	            // Don't add null objects
	            if (childCoordObject == null)
	            {
	                continue;
	            }

	            // Add this object to the chunk
	            addToSpawnList(childCoordObject, object, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
	            
	            // Also add the branches of this object
	            if (depth < maxBranchDepth)
	            {
	                addBranches(childCoordObject, depth + 1, worldGenRegion, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
	            }
	        }
    	}
    }

    private Branch[] getBranches(ICustomObject customObject, Rotation rotation)
    {
        return ((BO3)customObject).getBranches(rotation);
    }

    /**
     * Adds the object to the spawn list of each chunk that the object
     * touches.
     * @param coordObject The object.
     */
    private void addToSpawnList(BO3CustomStructureCoordinate coordObject, ICustomObject parent, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        ChunkCoordinate chunkCoordinate = coordObject.getPopulatingChunk(otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
        if(chunkCoordinate != null)
        {
	        Set<CustomStructureCoordinate> objectsInChunk = objectsToSpawn.get(chunkCoordinate);
	        if (objectsInChunk == null)
	        {
	            objectsInChunk = new LinkedHashSet<CustomStructureCoordinate>();
	            objectsToSpawn.put(chunkCoordinate, objectsInChunk);
	        }
	        objectsInChunk.add(coordObject);
        } else {
    		if(spawnLog)
    		{
	    		logger.log(LogMarker.WARN, "Error reading branch in BO3 " + parent.getName()  + " Could not find BO3: " + coordObject.bo3Name);
    		}
        }
    }

    public void spawnInChunk(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, ChunkCoordinate chunkCoordinate, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        Set<CustomStructureCoordinate> objectsInChunk = objectsToSpawn.get(chunkCoordinate);
        if(!worldGenRegion.getWorldConfig().doPopulationBoundsCheck())
        {
        	chunkCoordinate = null;
        }
        if (objectsInChunk != null)
        {
            for (CustomStructureCoordinate coordObject : objectsInChunk)
            {
                BO3 bo3 = ((BO3)((BO3CustomStructureCoordinate)coordObject).getObject(otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker));
                bo3.trySpawnAt(this, structureCache, worldGenRegion, random, coordObject.rotation, coordObject.x, height.getCorrectY(worldGenRegion, coordObject.x, coordObject.y, coordObject.z, chunkCoordinate), coordObject.z, bo3.getSettings().minHeight, bo3.getSettings().maxHeight, coordObject.y, chunkCoordinate, bo3.doReplaceBlocks());
            }
        }
    }
}
