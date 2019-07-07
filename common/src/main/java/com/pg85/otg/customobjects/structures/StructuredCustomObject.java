package com.pg85.otg.customobjects.structures;

import java.util.Random;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo3.StructurePartSpawnHeight;
import com.pg85.otg.util.bo3.BoundingBox;
import com.pg85.otg.util.bo3.Rotation;

/**
 * Represents CustomObjects that can have other objects attached
 * to it making a structure.
 *
 */
public interface StructuredCustomObject extends CustomObject
{
	// TODO: Clean up
	
    /**
     * Returns a list of all branches in this object. Null is not a valid
     * return value, return an empty list instead.
     * 
     * @return A list of all branches in this object.
     */
    public Branch[] getBranches(Rotation rotation); // Only used for Non-OTG+

    /**
     * Returns a list of all branches in this object. Null is not a valid
     * return value, return an empty list instead.
     * 
     * @return A list of all branches in this object.
     */
    public Branch[] getBranches(); // Only used for OTG+
     
    /**
     * Create a coordinate for this at a random position in the chunk.
     * Should respect it's own rarity setting. Can return null.
     * 
     * @param chunkX The chunk x.
     * @param chunkZ The chunk z.
     * @return The CustomObjectCoordinate
     */
    public CustomStructureCoordinate makeCustomObjectCoordinate(LocalWorld world, Random random, int chunkX, int chunkZ); // Only used for Non-OTG+

    /**
     * Returns the height at which the whole structure should spawn.
     * 
     * @return The height.
     */
    public StructurePartSpawnHeight getStructurePartSpawnHeight(); // Only used for Non-OTG+
    
    public int getMaxBranchDepth(); // Only used for Non-OTG+
    
    public BoundingBox getBoundingBox(Rotation rotation); // Only used for Non-OTG+
}
