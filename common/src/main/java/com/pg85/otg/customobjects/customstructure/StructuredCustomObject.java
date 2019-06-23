package com.pg85.otg.customobjects.customstructure;

import java.util.Random;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo3.StructurePartSpawnHeight;
import com.pg85.otg.util.Rotation;

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
     * @return A list of all branches in this object.
     */
    public Branch[] getBranches(Rotation rotation);

    /**
     * Returns a list of all branches in this object. Null is not a valid
     * return value, return an empty list instead.
     * 
     * @return A list of all branches in this object.
     */
    public Branch[] getBranches();
    
    /**
     * Create a coordinate for this at a random position in the chunk.
     * Should respect it's own rarity setting. Can return null.
     * 
     * @param chunkX The chunk x.
     * @param chunkZ The chunk z.
     * @return The CustomObjectCoordinate
     */
    public CustomObjectCoordinate makeCustomObjectCoordinate(LocalWorld world, Random random, int chunkX, int chunkZ);

    /**
     * Returns the height at which the whole structure should spawn.
     * 
     * @return The height.
     */
    public StructurePartSpawnHeight getStructurePartSpawnHeight();   
}
