package com.pg85.otg.gen.resource;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.util.List;
import java.util.Random;

public class DungeonResource extends FrequencyResourceBase
{
	private final int maxAltitude;
	private final int minAltitude;

	public DungeonResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(4, args);

		this.frequency = readInt(args.get(0), 1, 100);
		this.rarity = readRarity(args.get(1));
		this.minAltitude = readInt(args.get(2), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(3), minAltitude, Constants.WORLD_HEIGHT - 1);
	}

	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingDecorated)
	{
		int y = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);
		worldGenRegion.placeDungeon(random, x, y, z);
	}
	
	@Override
	public String toString()
	{
		return "Dungeon(" + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + ")";
	}	
}
