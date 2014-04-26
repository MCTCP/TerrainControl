package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.helpers.RandomHelper;

import java.util.*;

/**
 * This class creates the branch structure based on one parent object, and spawns all
 * objects that should spawn in a chunk.
 * 
 * Although it shouldn't be too slow to recalculate, a structure cache should be kept.
 *
 */
public class CustomObjectStructure
{
    protected final Random random;
    protected LocalWorld world;
    protected CustomObjectCoordinate start;
    protected StructurePartSpawnHeight height;
    protected Map<ChunkCoordinate, Set<CustomObjectCoordinate>> objectsToSpawn;
    protected int maxBranchDepth;

    public CustomObjectStructure(LocalWorld world, CustomObjectCoordinate start)
    {
        if (!(start.getObject() instanceof StructuredCustomObject))
        {
            throw new IllegalArgumentException("Start object has to be a structure!");
        }

        this.world = world;
        this.start = start;
        this.height = start.getStructuredObject().getStructurePartSpawnHeight();
        this.maxBranchDepth = start.getStructuredObject().getMaxBranchDepth();
        random = RandomHelper.getRandomForCoords(start.getX(), start.getY(), start.getZ(), world.getSeed());

        // Calculate all branches and add them to a list
        objectsToSpawn = new LinkedHashMap<ChunkCoordinate, Set<CustomObjectCoordinate>>();
        addToChunk(start); // Add the object itself
        addBranches(start, 1);
    }

    protected void addBranches(CustomObjectCoordinate coordObject, int depth)
    {
        for (Branch branch : coordObject.getStructuredObject().getBranches(coordObject.getRotation()))
        {
            CustomObjectCoordinate childCoordObject = branch.toCustomObjectCoordinate(world, random, coordObject.getX(), coordObject.getY(), coordObject.getZ());

            // Don't add null objects
            if (childCoordObject == null)
            {
                continue;
            }

            // Add this object to the chunk
            addToChunk(childCoordObject);

            // Also add the branches of this object
            if (depth < maxBranchDepth)
            {
                addBranches(childCoordObject, depth + 1);
            }
        }
    }

    public void addToChunk(CustomObjectCoordinate coordObject)
    {
        ChunkCoordinate chunkCoordinate = ChunkCoordinate.getPopulatingChunk(coordObject.getX(), coordObject.getZ());
        Set<CustomObjectCoordinate> objectsInChunk = objectsToSpawn.get(chunkCoordinate);
        if (objectsInChunk == null)
        {
            objectsInChunk = new LinkedHashSet<CustomObjectCoordinate>();
        }
        objectsInChunk.add(coordObject);
        objectsToSpawn.put(chunkCoordinate, objectsInChunk);
    }

    public void spawnForChunk(ChunkCoordinate chunkCoordinate)
    {
        Set<CustomObjectCoordinate> objectsInChunk = objectsToSpawn.get(chunkCoordinate);
        if (objectsInChunk != null)
        {
            for (CustomObjectCoordinate coordObject : objectsInChunk)
            {
                coordObject.spawnWithChecks(world, height, random);
            }
        }
    }
}
