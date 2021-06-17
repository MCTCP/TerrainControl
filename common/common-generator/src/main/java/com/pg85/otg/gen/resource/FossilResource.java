package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.ResourceBase;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.util.List;
import java.util.Random;

public final class FossilResource extends ResourceBase implements IBasicResource
{
	private final double rarity;
	
	public FossilResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(1, args);

		this.rarity = readRarity(args.get(0));
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ILogger logger, IMaterialReader materialReader)
	{
		if (random.nextDouble() * 100.0 > this.rarity)
		{
			return;
		}
		
		// Unfortunately, Minecraft ignores the passed random instance, and
		// creates one based on the chunk coords and world seed. This means
		// that spawning the object multiple times in a chunk will just
		// spawn exactly the same object at exactly the same location. In
		// other words: don't bother adding a frequency parameter, unless
		// you are going to rewrite the fossil code.
		worldGenRegion.placeFossil(random, worldGenRegion.getDecorationArea().getChunkBeingDecorated());
	}

	@Override
	public String toString()
	{
		return "Fossil(" + this.rarity + ")";
	}
}
