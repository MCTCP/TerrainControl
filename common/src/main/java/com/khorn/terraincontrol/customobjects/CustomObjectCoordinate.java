package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.BoundingBox;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;

import java.util.Random;

/**
 * Represents an object along with its location in the world.
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
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getZ()
    {
        return this.z;
    }

    /**
     * Returns the object of this coordinate.
     *
     * @return The object.
     */
    public CustomObject getObject()
    {
        return this.object;
    }

    public Rotation getRotation()
    {
        return this.rotation;
    }

    boolean spawnWithChecks(LocalWorld world, StructurePartSpawnHeight height, Random random)
    {
        int y = height.getCorrectY(world, this.x, this.y, this.z);
        if (!this.object.canSpawnAt(world, this.rotation, this.x, this.y, this.z))
        {
            return false;
        }
        return this.object.spawnForced(world, random, this.rotation, this.x, y, this.z);
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
        if (otherCoord.x != this.x)
        {
            return false;
        }
        if (otherCoord.y != this.y)
        {
            return false;
        }
        if (otherCoord.z != this.z)
        {
            return false;
        }
        if (!otherCoord.rotation.equals(this.rotation))
        {
            return false;
        }
        if (!otherCoord.object.getName().equals(this.object.getName()))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return (this.x >> 13) ^ (this.y >> 7) ^ this.z ^ this.object.getName().hashCode() ^ this.rotation.toString().hashCode();
    }

    /**
     * Gets the chunk that should populate for this object.
     * @return The chunk.
     */
    ChunkCoordinate getPopulatingChunk()
    {
        // In the past we simply returned the chunk populating for the origin
        // of the object. However, the origin is not guaranteed to be at the
        // center of the object. We need to know the exact center to choose
        // the appropriate spawning chunk.

        BoundingBox box = this.object.getBoundingBox(rotation);
        int centerX = x + box.getMinX() + (box.getWidth() / 2);
        int centerZ = z + box.getMinZ() + (box.getDepth() / 2);

        return ChunkCoordinate.getPopulatingChunk(centerX, centerZ);
    }
}
