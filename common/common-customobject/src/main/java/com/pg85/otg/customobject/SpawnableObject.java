package com.pg85.otg.customobject;

import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.bo3.Rotation;

import java.util.Random;

/**
 * Describes some small object (must fit in a single chunk) that can be spawned
 * in a world.
 */
interface SpawnableObject
{
	/**
	 * Spawns the object at the given position. It shouldn't execute any spawn
	 * checks, so it should make an effort to spawn at this location, even if
	 * the location is not suitable..
	 *
	 * @param world	World to spawn in.
	 * @param random	Random number generator based on the world seed.
	 * @param rotation Rotation to spawn the object in.
	 * @param x		X coord of the object origin.
	 * @param y		Y coord of the object origin.
	 * @param z		Z coord of the object origin.
	 * @return Whether the attempt was successful. (It should never fail, but you never know.)
	 */
	public boolean spawnForced(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z, boolean allowReplaceBlocks);
	
	/**
	 * Returns the name of this object.
	 *
	 * @return The name. If this object is loaded from a file, the file
	 * extension (.bo3, .nbt) is excluded.
	 */
	public String getName();
}
