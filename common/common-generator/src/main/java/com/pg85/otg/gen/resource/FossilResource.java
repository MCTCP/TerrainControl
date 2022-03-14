package com.pg85.otg.gen.resource;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.RandomHelper;

import java.util.List;
import java.util.Random;

public final class FossilResource extends FrequencyResourceBase
{
	private final int rarity;
	private final int maxAltitude;
	private final int minAltitude;
	
	public FossilResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(3, args);

		this.frequency = 1;
		this.rarity = readInt(args.get(0), 1, Integer.MAX_VALUE);
		this.minAltitude = readInt(args.get(1), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(2), minAltitude, Constants.WORLD_HEIGHT - 1);		
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, ILogger logger, IMaterialReader materialReader)
	{
	}

	@Override
	public String toString()
	{
		return "Fossil(" + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + ")";
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, int x, int z)
	{
		int y = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);		
		world.placeFossil(random, world.getDecorationArea().getChunkBeingDecoratedCenterX(), y, world.getDecorationArea().getChunkBeingDecoratedCenterZ());
	}
}
