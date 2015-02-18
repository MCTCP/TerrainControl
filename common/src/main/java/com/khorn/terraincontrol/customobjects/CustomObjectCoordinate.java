package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.BoundingBox;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;

import java.util.Random;

/**
 * Holds a custom object along with the absolute spawn coordinates.
 */
public class CustomObjectCoordinate
{

    private final CustomObject object;
    private final Rotation rotation;
    private final int x;
    private final int y;
    private final int z;

    public CustomObjectCoordinate(CustomObject object, Rotation rotation, int x, int y, int z)
    {
        this.object = object;
        this.rotation = rotation;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    /**
     * Returns the object of this coordinate.
     *
     * @return The object.
     */
    public CustomObject getObject()
    {
        return object;
    }

    /**
     * Returns the object of this coordinate, casted to a
     * StructuredCustomObject. Will throw a ClassCastExcpetion
     * if the object isn't a StructuredCustomObject
     *
     * @return The casted object.
     */
    public StructuredCustomObject getStructuredObject()
    {
        return (StructuredCustomObject) object;
    }

    public Rotation getRotation()
    {
        return rotation;
    }

    public boolean spawnWithChecks(LocalWorld world, StructurePartSpawnHeight height, Random random)
    {
        int y = height.getCorrectY(world, x, this.y, z);
        if (!object.canSpawnAt(world, rotation, x, y, z))
        {
            return false;
        }
        return object.spawnForced(world, random, rotation, x, y, z);
    }

    @Override
    public boolean equals(Object otherObject)
    {
        if (otherObject == null)
        {
            return false;
        }
        if (!(otherObject instanceof CustomObjectCoordinate))
        {
            return false;
        }
        CustomObjectCoordinate otherCoord = (CustomObjectCoordinate) otherObject;
        if (otherCoord.x != x)
        {
            return false;
        }
        if (otherCoord.y != y)
        {
            return false;
        }
        if (otherCoord.z != z)
        {
            return false;
        }
        if (!otherCoord.rotation.equals(rotation))
        {
            return false;
        }
        if (!otherCoord.object.getName().equals(object.getName()))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return (x >> 13) ^ (y >> 7) ^ z ^ object.getName().hashCode() ^ rotation.toString().hashCode();
    }

    /**
     * Gets the chunk that should populate for this object.
     * @return The chunk.
     */
    public ChunkCoordinate getPopulatingChunk()
    {
        // In the past we simply returned the chunk populating for the origin
        // of the object. However, the origin is not guaranteed to be at the
        // center of the object. We need to know the exact center to choose
        // the appropriate spawning chunk.
        int centerX;
        int centerZ;
        if (object instanceof StructuredCustomObject)
        {
            // Calculate the actual center of the object, based on all blocks
            // of the object
            BoundingBox box = ((StructuredCustomObject) object).getBoundingBox(rotation);
            centerX = x + box.getMinX() + (box.getWidth() / 2);
            centerZ = z + box.getMinZ() + (box.getDepth() / 2);
        } else
        {
            // Just assume the author of the object placed the origin near the
            // center
            centerX = x;
            centerZ = z;
        }

        return ChunkCoordinate.getPopulatingChunk(centerX, centerZ);
    }
}
