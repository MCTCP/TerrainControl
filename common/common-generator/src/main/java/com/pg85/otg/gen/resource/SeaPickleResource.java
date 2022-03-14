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
import com.pg85.otg.util.materials.MaterialProperties;

public class SeaPickleResource extends FrequencyResourceBase
{
	private final int attempts;

	public SeaPickleResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		this.frequency = readInt(args.get(0), 1, 500);
		this.rarity = readRarity(args.get(1));
		this.attempts = readInt(args.get(2), 1, 256);
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, int x, int z)
	{
		int dx;
		int dz;
		int y;
		LocalMaterialData bottom;
		LocalMaterialData here;
		for (int i = 0; i < this.attempts; i++)
		{
			dx = x + random.nextInt(8) - random.nextInt(8);
			dz = z + random.nextInt(8) - random.nextInt(8);
			y = world.getBlockAboveSolidHeight(dx, dz);

			bottom = world.getMaterial(dx, y - 1, dz);
			here = world.getMaterial(dx, y, dz);
			if (bottom == null || here == null)
			{
				continue;
			}

			if (bottom.isSolid() && here.isLiquid())
			{
				world.setBlock(dx, y, dz, LocalMaterials.SEA_PICKLE.withProperty(MaterialProperties.PICKLES_1_4, random.nextInt(4) + 1));
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "SeaPickle(" + this.frequency + "," + this.rarity + "," + this.attempts + ")";
	}	
}
