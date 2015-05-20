package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.util.BoundingBox;
import com.khorn.terraincontrol.util.Rotation;

import java.util.Random;

/**
 * Represents CustomObjects that can have other objects attached
 * to it making a structure.
 *
 */
public interface StructuredCustomObject extends CustomObject
{
    /**
     * Returns whether this object has branches attached to it.
     * 
     * @return Whether this object has branches attached to it.
     */
    public boolean hasBranches();

    /**
     * Returns a list of all branches in this object. Null is not a valid
     * return value, return an empty list instead.
     *
     * @param rotation The rotation for the branches.
     * @return A list of all branches in this object.
     */
    public Branch[] getBranches(Rotation rotation);

    /**
     * Create a coordinate for this at a random position in the chunk.
     * Should respect it's own rarity setting. Can return null.
     *
     * @param random Random number generator based on the world seed.
     * @param chunkX The chunk x.
     * @param chunkZ The chunk z.
     * @return The CustomObjectCoordinate
     */
    public CustomObjectCoordinate makeCustomObjectCoordinate(Random random, int chunkX, int chunkZ);

    /**
     * Branches can have branches which can have branches, etc. This
     * method returns the limit of searching for branches.
     * 
     * @return The maximum branch depth.
     */
    public int getMaxBranchDepth();

    /**
     * Returns the height at which the whole structure should spawn.
     * 
     * @return The height.
     */
    public StructurePartSpawnHeight getStructurePartSpawnHeight();

    /**
     * Gets the bounding box of this object if it is rotated in the given way.
     * @param rotation The rotation of the object.
     * @return The bounding box.
     */
    public BoundingBox getBoundingBox(Rotation rotation);
}
