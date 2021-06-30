package com.pg85.otg.gen.resource;

import java.util.List;
import java.util.Random;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;

public abstract class FrequencyResourceBase extends BiomeResourceBase implements IBasicResource
{
	protected int frequency;
	protected double rarity;
	
	public FrequencyResourceBase(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader)
	{
		super(biomeConfig, args, logger, materialReader);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, ILogger logger, IMaterialReader materialReader)
	{
		int blockX = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterX();
		int blockZ = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterZ();		

		for (int t = 0; t < this.frequency; t++)
		{
			if (random.nextDouble() * 100.0 > this.rarity)
			{
				continue;
			}
			int x = blockX + random.nextInt(Constants.CHUNK_SIZE);
			int z = blockZ + random.nextInt(Constants.CHUNK_SIZE);
			spawn(worldGenRegion, random, x, z);
		}
	}

	public abstract void spawn(IWorldGenRegion world, Random random, int x, int z);	
}
