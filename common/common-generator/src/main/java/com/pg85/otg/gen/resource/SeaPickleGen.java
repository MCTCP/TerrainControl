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
import com.pg85.otg.util.materials.MaterialProperties;

public class SeaPickleGen extends Resource
{
	private final int attempts;

	public SeaPickleGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		this.frequency = readInt(args.get(0), 1, 500);
		this.rarity = readRarity(args.get(1));
		this.attempts = readInt(args.get(2), 1, 256);
	}

	@Override
	public String toString()
	{
		return "SeaPickle(" + this.frequency + "," + this.rarity + "," + this.attempts + ")";
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		for (int i = 0; i < this.attempts; i++)
		{
			int dx = x + random.nextInt(8) - random.nextInt(8);
			int dz = z + random.nextInt(8) - random.nextInt(8);
			int y = world.getBlockAboveSolidHeight(dx, dz, chunkBeingPopulated);

			LocalMaterialData bottom = world.getMaterial(dx, y - 1, dz, chunkBeingPopulated);
			LocalMaterialData here = world.getMaterial(dx, y, dz, chunkBeingPopulated);
			if (bottom == null || here == null)
			{
				continue;
			}

			if (bottom.isSolid() && here.isLiquid())
			{
				world.setBlock(dx, y, dz, LocalMaterials.SEA_PICKLE.withProperty(MaterialProperties.PICKLES_1_4, random.nextInt(4) + 1), null, chunkBeingPopulated, false);
			}
		}
	}
}
