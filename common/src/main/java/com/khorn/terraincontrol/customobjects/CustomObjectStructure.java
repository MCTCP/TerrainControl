package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;
import com.khorn.terraincontrol.util.helpers.RandomHelper;

import java.util.*;

/**
 * Represents a collection of all {@link CustomObject}s in a structure. It is
 * calculated by finding the branches of one object, then finding the branches
 * of those branches, etc., until
 * {@link CustomObject#getMaxBranchDepth()} is reached.
 *
 */
public class CustomObjectStructure
{
    protected final Random random;
    protected LocalWorld world;
    protected CustomObjectCoordinate start;
    protected StructurePartSpawnHeight height;
    private Map<ChunkCoordinate, Set<CustomObjectCoordinate>> objectsToSpawn;
    private int maxBranchDepth;

    CustomObjectStructure(LocalWorld world, CustomObjectCoordinate start)
    {
        CustomObject object = start.getObject();

        this.world = world;
        this.start = start;
        this.height = object.getStructurePartSpawnHeight();
        this.maxBranchDepth = object.getMaxBranchDepth();
        random = RandomHelper.getRandomForCoords(start.getX(), start.getY(), start.getZ(), world.getSeed());

        // Calculate all branches and add them to a list
        objectsToSpawn = new LinkedHashMap<ChunkCoordinate, Set<CustomObjectCoordinate>>();
        addToSpawnList(start); // Add the object itself
        addBranches(start, 1);
    }

    private void addBranches(CustomObjectCoordinate coordObject, int depth)
    {
        for (Branch branch : getBranches(coordObject.getObject(), coordObject.getRotation()))
        {
            CustomObjectCoordinate childCoordObject = branch.toCustomObjectCoordinate(world, random, coordObject.getX(),
                    coordObject.getY(), coordObject.getZ());

            // Don't add null objects
            if (childCoordObject == null)
            {
                continue;
            }

            // Add this object to the chunk
            addToSpawnList(childCoordObject);

            // Also add the branches of this object
            if (depth < maxBranchDepth)
            {
                addBranches(childCoordObject, depth + 1);
            }
        }
    }

    private Branch[] getBranches(CustomObject customObject, Rotation rotation)
    {
        return customObject.getBranches(rotation);
    }

    /**
     * Adds the object to the spawn list of each chunk that the object
     * touches.
     * @param coordObject The object.
     */
    private void addToSpawnList(CustomObjectCoordinate coordObject)
    {
        ChunkCoordinate chunkCoordinate = coordObject.getPopulatingChunk();

        Set<CustomObjectCoordinate> objectsInChunk = objectsToSpawn.get(chunkCoordinate);
        if (objectsInChunk == null)
        {
            objectsInChunk = new LinkedHashSet<CustomObjectCoordinate>();
            objectsToSpawn.put(chunkCoordinate, objectsInChunk);
        }
        objectsInChunk.add(coordObject);
    }

    /**
     * Spawns all the objects that should be spawned in that chunk.
     * @param chunkCoordinate The chunk to spawn in.
     */
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
