package com.pg85.otg.customobjects.customstructure;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.util.Rotation;

import java.util.Random;

/**
 * Represents a branch of a CustomObject.
 *
 */
public interface Branch
{
    /**
     * Makes a CustomObjectCoordinate out of this branch. Is allowed
     * to return null if based on the random number generator no
     * branch should spawn here.
     * 
     * @param world  The world.
     * @param random The random number generator.
     * @param rotation Rotation of the origin of the object.
     * @param x      X coordinate of the origin of the object.
     * @param y      Y coordinate of the origin of the object.
     * @param z      Z coordinate of the origin of the object.
     * @return The CustomObjectCoordinate of this branch.
     */
    public CustomObjectCoordinate toCustomObjectCoordinate(LocalWorld world, Random random, Rotation rotation, int x, int y, int z, String startBO3Name);
}
