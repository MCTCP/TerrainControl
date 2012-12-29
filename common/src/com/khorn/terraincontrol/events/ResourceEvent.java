package com.khorn.terraincontrol.events;

import java.util.Random;

import com.khorn.terraincontrol.LocalWorld;

public class ResourceEvent extends ChunkEvent
{
	public enum Type
	{
		ABOVE_WATER,
		CACTUS,
		CUSTOM_OBJECT,
		DUNGEON,
		GRASS,
		ICE,
		LIQUID,
		ORE,
		PLANT,
		REED,
		SMALL_LAKE,
		TREE,
		UNDERGROUND_LAKE,
		UNDERWATER_ORE,
		VINES
	}
	
	private final Type type;
	private final int blockId;
	private final int blockData;
	private boolean cancel = false;
	
	public ResourceEvent(Type type, LocalWorld world, Random random, int chunkX, int chunkZ, int blockId, int blockData, boolean hasGeneratedAVillage)
	{
		super(world, random, chunkX, chunkZ, hasGeneratedAVillage);
		this.type = type;
		this.blockId = blockId;
		this.blockData = blockData;
	}

	public boolean isCancelled()
	{
		return cancel;
	}

	public void cancel()
	{
		this.cancel = true;
	}

	public Type getType()
	{
		return type;
	}

	public int getBlockId()
	{
		return blockId;
	}

	public int getBlockData()
	{
		return blockData;
	}
}
