package com.pg85.otg.gen.resource;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

public class BoulderResource extends FrequencyResourceBase
{
	private final MaterialSet sourceBlocks;
	private final LocalMaterialData material;
	private final int minAltitude;
	private final int maxAltitude;

	public BoulderResource(IBiomeConfig config, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(config, args, logger, materialReader);
		assureSize(6, args);

		this.material = materialReader.readMaterial(args.get(0));
		this.frequency = readInt(args.get(1), 1, 5000);
		this.rarity = readRarity(args.get(2));
		this.minAltitude = readInt(args.get(3), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(4), this.minAltitude, Constants.WORLD_HEIGHT - 1);
		this.sourceBlocks = readMaterials(args, 5, materialReader);
	}

	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int z)
	{
		int y = worldGenRegion.getHighestBlockAboveYAt(x, z);
		if (y < this.minAltitude || y > this.maxAltitude)
		{
			return;
		}
		
		LocalMaterialData material;
		while (y > 3)
		{
			material = worldGenRegion.getMaterial(x, y - 1, z);
			if (this.sourceBlocks.contains(material))
			{
				break;
			}
			y--;
		}
		if (y <= 3)
		{
			return;
		}

		int i = 0;
		int j = 0;
		int k;
		int m;
		int n;
		float f1;
		float f2;
		float f3;
		float f4;
		while ((i >= 0) && (j < 3))
		{
			k = i + random.nextInt(2);
			m = i + random.nextInt(2);
			n = i + random.nextInt(2);
			f1 = (k + m + n) * 0.333F + 0.5F;
			for (int i1 = x - k; i1 <= x + k; i1++)
			{
				for (int i2 = z - n; i2 <= z + n; i2++)
				{
					IBiomeConfig biome = worldGenRegion.getBiomeConfigForDecoration(i1, i2);
					for (int i3 = y - m; i3 <= y + m; i3++)
					{
						f2 = i1 - x;
						f3 = i2 - z;
						f4 = i3 - y;
						if (f2 * f2 + f3 * f3 + f4 * f4 <= f1 * f1)
						{
							worldGenRegion.setBlock(i1, i3, i2, this.material, biome.getReplaceBlocks());
						}
					}
				}
			}
			x += random.nextInt(2 + i * 2) - 1 - i;
			z += random.nextInt(2 + i * 2) - 1 - i;
			y -= random.nextInt(2);
			j++;
		}
	}

	@Override
	public String toString()
	{
		return "Boulder(" + this.material + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}
}
