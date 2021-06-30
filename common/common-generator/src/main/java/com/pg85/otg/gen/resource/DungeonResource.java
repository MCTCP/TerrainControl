package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.RandomHelper;

import java.util.List;
import java.util.Random;

public class DungeonResource extends BiomeResourceBase implements IBasicResource
{
	private final int range;
	private final int count;
	private final int maxAltitude;
	private final int minAltitude;

	public DungeonResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(4, args);
		
		this.range = readInt(args.get(0), 1, Integer.MAX_VALUE);
		this.count = readInt(args.get(0), 1, Integer.MAX_VALUE);
		this.minAltitude = readInt(args.get(2), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(3), minAltitude, Constants.WORLD_HEIGHT - 1);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, ILogger logger, IMaterialReader materialReader)
	{
		int y = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);
		worldGenRegion.placeDungeon(random, worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterX(), y, worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterZ(), this.range, this.count);
	}

	@Override
	public String toString()
	{
		return "Dungeon(" + this.range + "," + this.count + "," + this.minAltitude + "," + this.maxAltitude + ")";
	}	
}
