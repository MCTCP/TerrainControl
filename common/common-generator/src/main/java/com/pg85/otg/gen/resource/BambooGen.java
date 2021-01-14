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
import com.pg85.otg.util.materials.MaterialSet;

public class BambooGen extends Resource
{
	private final double podzolRarity;
	private final MaterialSet sourceBlocks;

	public BambooGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);

		this.frequency = readInt(args.get(0), 1, 500);
		this.rarity = readRarity(args.get(1));
		this.podzolRarity = readDouble(args.get(2), 0.0, 1.0);
		this.sourceBlocks = readMaterials(args, 3, materialReader);
	}

	@Override
	public String toString()
	{
		return "Bamboo(" + this.frequency + "," + this.rarity + + this.podzolRarity + this.sourceBlocks + ")";
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
	{
		int y = world.getHighestBlockAboveYAt(x, z, chunkBeingPopulated);

		int height = random.nextInt(12) + 5;

		if (world.getMaterial(x, y, z, chunkBeingPopulated).isAir()) {

			// Don't continue if the material below us isn't in the source blocks or is null
			LocalMaterialData below = world.getMaterial(x, y - 1, z, chunkBeingPopulated);
			if (below == null || !this.sourceBlocks.contains(below))
			{
				return;
			}

			// Place podzol around bamboo bottom with chance
			if (random.nextDouble() <= this.podzolRarity)
			{
				// TODO: should the radius be configurable?
				int radius = random.nextInt(4) + 1;

				for (int x1 = -radius; x1 <= radius; x1++)
				{
					for (int z1 = -radius; z1 <= radius; z1++)
					{
						// Include only inside the circle
						if (x1 * x1 + z1 * z1 <= radius * radius)
						{
							int topY = world.getHighestBlockAboveYAt(x + x1, z + z1, chunkBeingPopulated);

							world.setBlock(x + x1, topY - 1, z + z1, LocalMaterials.PODZOL, null, chunkBeingPopulated, false);
						}
					}
				}
			}

			// Place bamboo
			for (int y1 = 0; y1 < height; y1++)
			{
				LocalMaterialData bamboo = LocalMaterials.BAMBOO;
				if (y1 == height - 1) {
					bamboo = LocalMaterials.BAMBOO_LARGE_GROWING;
				} else if (y1 == height - 2) {
					bamboo = LocalMaterials.BAMBOO_LARGE;
				} else if (y1 == height - 3) {
					bamboo = LocalMaterials.BAMBOO_SMALL;
				}

				world.setBlock(x, y + y1, z, bamboo, null, chunkBeingPopulated, false);
			}
		}
	}
}
