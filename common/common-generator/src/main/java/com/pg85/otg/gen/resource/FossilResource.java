package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.util.List;
import java.util.Random;

public final class FossilResource extends BiomeResourceBase implements IBasicResource
{
	private final int chance;
	private final int maxAltitude;
	private final int minAltitude;
	
	public FossilResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(1, args);

		this.chance = readInt(args.get(0), 1, Integer.MAX_VALUE);
		this.minAltitude = readInt(args.get(1), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(2), minAltitude, Constants.WORLD_HEIGHT - 1);		
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ILogger logger, IMaterialReader materialReader)
	{
		int y = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);		
		worldGenRegion.placeFossil(random, worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterX(), y, worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterZ(), this.chance);
	}

	@Override
	public String toString()
	{
		return "Fossil(" + this.chance + "," + this.minAltitude + "," + this.maxAltitude + ")";
	}
}
