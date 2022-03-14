package com.pg85.otg.gen.resource;

import java.util.List;
import java.util.Random;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialSet;

public class BambooResource extends FrequencyResourceBase
{
	private final double podzolChance;
	private final MaterialSet sourceBlocks;

	public BambooResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);

		this.frequency = readInt(args.get(0), 1, 500);
		this.rarity = readRarity(args.get(1));
		this.podzolChance = readDouble(args.get(2), 0.0, 1.0);
		this.sourceBlocks = readMaterials(args, 3, materialReader);
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, int x, int z)
	{
		int y = world.getHighestBlockAboveYAt(x, z);
		int height = random.nextInt(12) + 5;

		if (world.getMaterial(x, y, z).isAir())
		{
			// Don't continue if the material below us isn't in the source blocks or is null
			LocalMaterialData below = world.getMaterial(x, y - 1, z);
			if (below == null || !this.sourceBlocks.contains(below))
			{
				return;
			}

			// Search upwards for the max height that it can place bamboo
			for (int y1 = 0; y1 < height; y1++)
			{
				if (!world.getMaterial(x, y + y1, z).isAir())
				{
					height = y1;
					break;
				}
			}

			// Place podzol around bamboo bottom with chance
			if (random.nextDouble() <= this.podzolChance)
			{
				// TODO: should the radius be configurable?
				int radius = random.nextInt(4) + 1;
				int topY;
				for (int x1 = -radius; x1 <= radius; x1++)
				{
					for (int z1 = -radius; z1 <= radius; z1++)
					{
						// Include only inside the circle
						if (x1 * x1 + z1 * z1 <= radius * radius)
						{
							topY = world.getHighestBlockAboveYAt(x + x1, z + z1);
							if (this.sourceBlocks.contains(world.getMaterial(x + x1, topY - 1, z + z1)))
							{
								world.setBlock(x + x1, topY - 1, z + z1, LocalMaterials.PODZOL);
							}
						}
					}
				}
			}

			// Place bamboo
			LocalMaterialData bamboo;
			for (int y1 = 0; y1 < height; y1++)
			{
				bamboo = LocalMaterials.BAMBOO;
				if (y1 == height - 1)
				{
					bamboo = LocalMaterials.BAMBOO_LARGE_GROWING;
				}
				else if (y1 == height - 2)
				{
					bamboo = LocalMaterials.BAMBOO_LARGE;
				}
				else if (y1 == height - 3)
				{
					bamboo = LocalMaterials.BAMBOO_SMALL;
				}

				world.setBlock(x, y + y1, z, bamboo);
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "Bamboo(" + this.frequency + "," + this.rarity + "," + this.podzolChance + "," + this.sourceBlocks + ")";
	}	
}
