package com.pg85.otg.gen.resource;

import java.util.List;
import java.util.Random;

import com.pg85.otg.config.biome.ResourceBase;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public abstract class FrequencyResourceBase extends ResourceBase implements IBasicResource
{
	protected int frequency;
	protected double rarity;
	
	public FrequencyResourceBase(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader)
	{
		super(biomeConfig, args, logger, materialReader);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingDecorated, ILogger logger, IMaterialReader materialReader)
	{
		int blockX = chunkBeingDecorated.getBlockXCenter();
		int blockZ = chunkBeingDecorated.getBlockZCenter();		

		for (int t = 0; t < this.frequency; t++)
		{
			if (random.nextDouble() * 100.0 > this.rarity)
			{
				continue;
			}
			int x = blockX + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
			int z = blockZ + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
			spawn(worldGenRegion, random, false, x, z, chunkBeingDecorated);
		}
	}

	public abstract void spawn(IWorldGenRegion world, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingDecorated);	
}
