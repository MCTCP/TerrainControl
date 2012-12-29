package com.khorn.terraincontrol.events;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.customobjects.CustomObject;

public class CustomObjectSpawnEvent
{
	private final CustomObject object;
	private final LocalWorld world;
	private final int x;
	private final int y;
	private final int z;
	
	/**
     * This event should NOT be used to modify data in the object or the world.
     * 
     * @param object
     *            The object that is about to spawn.
     * @param world
     *            The world where it will spawn.
     * @param x
     *            The x of the object origin.
     * @param y
     *            The y of the object origin.
     * @param z
     *            The z of the object origin.
	 */
	public CustomObjectSpawnEvent(CustomObject object, LocalWorld world, int x, int y, int z)
	{
		this.object = object;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public CustomObject getObject()
	{
		return object;
	}

	public LocalWorld getWorld()
	{
		return world;
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
}
