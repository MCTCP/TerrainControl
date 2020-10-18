package com.pg85.otg.customobjects.structures.bo3;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.StructurePartSpawnHeight;
import com.pg85.otg.customobjects.structures.Branch;
import com.pg85.otg.customobjects.structures.CustomStructure;
import com.pg85.otg.customobjects.structures.CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.RandomHelper;

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
    
    public BO3CustomStructure(LocalWorld world, BO3CustomStructureCoordinate start)
    {
        StructuredCustomObject object = (StructuredCustomObject)start.getObject();

        if(object == null)
        {
        	return;
        }
    	if(!(object instanceof BO3))
		{
    		if(OTG.getPluginConfig().spawnLog)
    		{
    			OTG.log(LogMarker.WARN, "BO3CustomStructure loaded with non-BO3 object " + object.getName());
    		}
    		return;
		}
        
        this.start = start;
        this.height = ((BO3)object).getStructurePartSpawnHeight();
        this.maxBranchDepth = ((BO3)object).getMaxBranchDepth();
        this.random = RandomHelper.getRandomForCoords(start.getX(), start.getY(), start.getZ(), world.getSeed());

        // Calculate all branches and add them to a list
        objectsToSpawn = new LinkedHashMap<ChunkCoordinate, Set<CustomStructureCoordinate>>();

        addToSpawnList((BO3CustomStructureCoordinate)start, object); // Add the object itself
        addBranches((BO3CustomStructureCoordinate)start, 1, world);
    }

    private void addBranches(BO3CustomStructureCoordinate coordObject, int depth, LocalWorld world)
    {
    	StructuredCustomObject object = coordObject.getObject();

    	if(object != null)
    	{
	        for (Branch branch : getBranches(object, coordObject.getRotation()))
	        {
	        	// TODO: Does passing null as startbo3name work?
	        	BO3CustomStructureCoordinate childCoordObject = (BO3CustomStructureCoordinate)branch.toCustomObjectCoordinate(world, random, coordObject.getRotation(), coordObject.getX(), coordObject.getY(), coordObject.getZ(), null);

	            // Don't add null objects
	            if (childCoordObject == null)
	            {
	                continue;
	            }

	            // Add this object to the chunk
	            addToSpawnList(childCoordObject, object);

	            // Also add the branches of this object
	            if (depth < maxBranchDepth)
	            {
	                addBranches(childCoordObject, depth + 1, world);
	            }
	        }
    	}
    }

    private Branch[] getBranches(CustomObject customObject, Rotation rotation)
    {
        return ((BO3)customObject).getBranches(rotation);
    }

    /**
     * Adds the object to the spawn list of each chunk that the object
     * touches.
     * @param coordObject The object.
     */
    private void addToSpawnList(BO3CustomStructureCoordinate coordObject, CustomObject parent)
    {
        ChunkCoordinate chunkCoordinate = coordObject.getPopulatingChunk();
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
    		if(OTG.getPluginConfig().spawnLog)
    		{
	    		OTG.log(LogMarker.WARN, "Error reading branch in BO3 " + parent.getName()  + " Could not find BO3: " + coordObject.bo3Name);
    		}
        }
    }

    public void spawnInChunk(ChunkCoordinate chunkCoordinate, LocalWorld world)
    {
        Set<CustomStructureCoordinate> objectsInChunk = objectsToSpawn.get(chunkCoordinate);
        if(!world.getConfigs().getWorldConfig().populationBoundsCheck)
        {
        	chunkCoordinate = null;
        }
        if (objectsInChunk != null)
        {
            for (CustomStructureCoordinate coordObject : objectsInChunk)
            {
                BO3 bo3 = ((BO3)((BO3CustomStructureCoordinate)coordObject).getObject());
                bo3.trySpawnAt(this, world, random, coordObject.rotation, coordObject.x, height.getCorrectY(world, coordObject.x, coordObject.y, coordObject.z, chunkCoordinate), coordObject.z, bo3.getSettings().minHeight, bo3.getSettings().maxHeight, coordObject.y, chunkCoordinate, bo3.doReplaceBlocks());
            }
        }
    }
}
