package com.pg85.otg.gen.resource;

import java.util.List;
import java.util.Random;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;

public class RegistryResource  extends BiomeResourceBase implements IBasicResource
{
	private final String id;

	public RegistryResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(1, args);

		this.id = args.get(0);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, ILogger logger, IMaterialReader materialReader)
	{
		worldGenRegion.placeFromRegistry(random, worldGenRegion.getDecorationArea().getChunkBeingDecorated(), this.id);
	}
	
	@Override
	public String toString()
	{
		return "Registry(" + this.id + ")";
	}	
}
