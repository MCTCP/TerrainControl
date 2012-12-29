package com.khorn.terraincontrol.events;

import java.util.Random;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.events.PopulateEvent.Type;

public abstract class ChunkEvent
{
	private final LocalWorld world;
	private final Random random;
	private final int x;
	private final int z;
	private final boolean hasGeneratedAVillage;
	
	/**
     * @param world
     *            The world populated.
     * @param chunkX
     *            The x (chunk) of the populated chunk.
     * @param chunkZ
     *            The z (chunk) of the populated chunk.
	 */
	public ChunkEvent(LocalWorld world, Random random, int chunkX, int chunkZ, boolean hasGeneratedAVillage)
	{
		this.world = world;
		this.random = random;
		this.x = chunkX;
		this.z = chunkZ;
		this.hasGeneratedAVillage = hasGeneratedAVillage;
	}

	public LocalWorld getWorld() {
		return world;
	}

	public Random getRandom() {
		return random;
	}

	public int getChunkX() {
		return x;
	}

	public int getChunkZ() {
		return z;
	}

	public boolean hasGeneratedAVillage() {
		return hasGeneratedAVillage;
	}
}