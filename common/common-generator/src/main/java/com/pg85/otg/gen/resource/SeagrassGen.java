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
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

public class SeagrassGen extends Resource
{
	private final double tallChance;

	public SeagrassGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		this.frequency = readInt(args.get(0), 1, 500);
		this.rarity = readRarity(args.get(1));
		this.tallChance = readDouble(args.get(2), 0.0, 1.0);
	}

	@Override
	public String toString()
	{
		return "Seagrass(" + this.frequency + "," + this.rarity + +this.tallChance + ")";
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		// Triangle distribution from provided x/z
		int dx = random.nextInt(8) - random.nextInt(8) + x;
		int dz = random.nextInt(8) - random.nextInt(8) + z;

		// Find lowest point
		int y = world.getBlockAboveSolidHeight(dx, dz, chunkBeingPopulated);

		LocalMaterialData below = world.getMaterial(dx, y - 1, dz, chunkBeingPopulated);
		if (below == null || !below.isSolid())
		{
			return;
		}

		LocalMaterialData material = world.getMaterial(dx, y, dz, chunkBeingPopulated);

		if (material == null)
		{
			return;
		}

		if (material.isLiquid())
		{
			if (random.nextDouble() <= this.tallChance && world.getMaterial(dx, y + 1, dz, chunkBeingPopulated).isLiquid())
			{
				world.setBlock(x, y, z, LocalMaterials.TALL_SEAGRASS_LOWER, null, chunkBeingPopulated, false);
				world.setBlock(x, y + 1, z, LocalMaterials.TALL_SEAGRASS_UPPER, null, chunkBeingPopulated, false);
			} else
			{
				world.setBlock(x, y, z, LocalMaterials.SEAGRASS, null, chunkBeingPopulated, false);
			}
		}
	}
}
