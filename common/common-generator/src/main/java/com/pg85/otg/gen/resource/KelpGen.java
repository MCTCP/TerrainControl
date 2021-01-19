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

public class KelpGen extends Resource
{
	public KelpGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		this.frequency = readInt(args.get(0), 1, 500);
		this.rarity = readRarity(args.get(1));
	}

	@Override
	public String toString()
	{
		return "Kelp(" + this.frequency + ", " + this.rarity + ")";
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		int y = world.getBlockAboveSolidHeight(x, z, chunkBeingPopulated);

		LocalMaterialData below = world.getMaterial(x, y - 1, z, chunkBeingPopulated);
		if (below == null || !below.isSolid())
		{
			return;
		}

		int height = 1 + random.nextInt(10);

		for (int y1 = 0; y1 <= height; y1++)
		{
			int dy = y + y1;

			if (!world.getMaterial(x, dy, + z, chunkBeingPopulated).isLiquid()) {
				break;
			}

			if (!world.getMaterial(x, dy + 1, + z, chunkBeingPopulated).isLiquid()) {
				if (y1 > 0) {
					world.setBlock(x, dy, z, LocalMaterials.KELP.withProperty(MaterialProperties.AGE_0_25, 20 + random.nextInt(4)), null, chunkBeingPopulated, false);
				}

				break;
			}

			if (y1 == height) {
				world.setBlock(x, dy, z, LocalMaterials.KELP.withProperty(MaterialProperties.AGE_0_25, 20 + random.nextInt(4)), null, chunkBeingPopulated, false);
			} else {
				world.setBlock(x, dy, z, LocalMaterials.KELP_PLANT, null, chunkBeingPopulated, false);
			}
		}
	}
}
