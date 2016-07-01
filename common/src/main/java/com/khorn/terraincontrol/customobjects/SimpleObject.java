package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.util.BoundingBox;
import com.khorn.terraincontrol.util.Rotation;

import java.util.Random;

/**
 * Base class for simple custom objects: custom objects that have no branches,
 * no interesting bounding box and that cannot rotate.
 */
public abstract class SimpleObject implements CustomObject
{
    @Override
    public boolean canRotateRandomly()
    {
        return false;
    }

    @Override
    public BoundingBox getBoundingBox(Rotation rotation)
    {
        return BoundingBox.newEmptyBox();
    }

    @Override
    public Branch[] getBranches(Rotation rotation)
    {
        return new Branch[0];
    }

    @Override
    public int getMaxBranchDepth()
    {
        return 0;
    }

    @Override
    public StructurePartSpawnHeight getStructurePartSpawnHeight()
    {
        return StructurePartSpawnHeight.PROVIDED;
    }

    @Override
    public boolean hasBranches()
    {
        return false;
    }

    @Override
    public CustomObjectCoordinate makeCustomObjectCoordinate(Random random, int chunkX, int chunkZ)
    {
        // Cannot start a structure using this object
        return null;
    }
}
