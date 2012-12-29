package com.khorn.terraincontrol.events;

import java.util.Random;

import com.khorn.terraincontrol.LocalWorld;

public class PopulateEvent extends ChunkEvent
{
	public enum Type
	{
		BEGIN,
		END
	}
	
	private final Type type;
	
	public PopulateEvent(Type type, LocalWorld world, Random random, int chunkX, int chunkZ, boolean hasGeneratedAVillage)
	{
		super(world, random, chunkX, chunkZ, hasGeneratedAVillage);
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}
}
