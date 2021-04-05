package com.pg85.otg.gen.resource;

import java.util.List;
import java.util.Random;

import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public class RegistryGen extends Resource
{
	private final String id;

	public RegistryGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(1, args);

		this.id = args.get(0);
	}

	@Override
	public String toString()
	{
		return "Registry(" + this.id + ")";
	}

	@Override
	protected void spawnInChunk(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, ILogger logger, IMaterialReader materialReader)
	{
		int chunkX = chunkBeingPopulated.getBlockXCenter();
		int chunkZ = chunkBeingPopulated.getBlockZCenter();

		createCache();

		spawn(worldGenRegion, random, false, chunkX, chunkZ, chunkBeingPopulated);

		clearCache();
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		world.placeFromRegistry(random, chunkBeingPopulated, this.id);
	}
}
