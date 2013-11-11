package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.Rotation;

import java.util.Random;

/**
 * Holds a custom object along with the absolute spawn coordinates.
 *
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
        return (Integer.valueOf(x).hashCode() >> 13) ^ (Integer.valueOf(y).hashCode() >> 7) ^ Integer.valueOf(z).hashCode() ^ object.getName().hashCode() ^ rotation.toString().hashCode();
    }
}
