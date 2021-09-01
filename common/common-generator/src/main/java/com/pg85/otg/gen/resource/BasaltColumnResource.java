package com.pg85.otg.gen.resource;

import java.util.List;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.gen.resource.util.PositionHelper;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

public class BasaltColumnResource extends FrequencyResourceBase
{
	private int baseSize;
	private int sizeVariance;
	private int baseHeight;
	private int heightVariance;
	private int minAltitude;
	private int maxAltitude;
	private LocalMaterialData material;
	private final MaterialSet sourceBlocks;

	public BasaltColumnResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader)
			throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(8, args);

		this.material = readMaterial(args.get(0), materialReader);
		this.frequency = readInt(args.get(1), 1, 100);
		this.rarity = readRarity(args.get(2));
		this.baseSize = readInt(args.get(3), 1, 5);
		this.sizeVariance = readInt(args.get(4), 0, 5);
		this.baseHeight = readInt(args.get(5), 1, 5);
		this.heightVariance = readInt(args.get(6), 0, 5);
		this.minAltitude = readInt(args.get(7), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(8), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.sourceBlocks = readMaterials(args, 9, materialReader);

	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, int x, int z)
	{
		int y = world.getHighestBlockYAt(x, z, true, false, true, true, true) + 1;
		
		if (y < this.minAltitude || y > this.maxAltitude)
			return;

		if (!canPlaceAt(world, x, y, z))
			return;

		int height = this.heightVariance == 0 ? this.baseHeight : this.baseHeight + random.nextInt(this.heightVariance + 1);

		boolean lvt_8_1_ = (random.nextFloat() < 0.9F);
		int lvt_9_1_ = Math.min(height, lvt_8_1_ ? 5 : 8);
		int lvt_10_1_ = lvt_8_1_ ? 50 : 15;

		for (int[] lvt_13_1_ : PositionHelper.randomBetweenClosed(random, lvt_10_1_, x - lvt_9_1_, y, z - lvt_9_1_,
				x + lvt_9_1_, y, z + lvt_9_1_))
		{
			int lvt_14_1_ = height - PositionHelper.distManhattan(lvt_13_1_[0], lvt_13_1_[1], lvt_13_1_[2], x, y, z);
			if (lvt_14_1_ >= 0)
				placeColumn(world, y, lvt_13_1_[0], lvt_13_1_[1], lvt_13_1_[2], lvt_14_1_, this.sizeVariance == 0 ? this.baseSize : this.baseSize + random.nextInt(this.sizeVariance + 1));

		}
	}

	private void placeColumn(IWorldGenRegion world, int p_236248_2_, int x, int y, int z, int p_236248_4_,
			int p_236248_5_)
	{

		for (int[] lvt_8_1_ : PositionHelper.betweenClosed(x - p_236248_5_, y, z - p_236248_5_, x + p_236248_5_, y,
				z + p_236248_5_))
		{
			int lvt_9_1_ = PositionHelper.distManhattan(lvt_8_1_[0], lvt_8_1_[1], lvt_8_1_[2], x, y, z);

			int[] lvt_10_1_ = world.getMaterialDirect(lvt_8_1_[0], lvt_8_1_[1], lvt_8_1_[2]).isAir()
					? findSurface(world, p_236248_2_, lvt_8_1_, lvt_9_1_)
					: findAir(world, lvt_8_1_, lvt_9_1_);
			if (lvt_10_1_ == null)
				continue;

			int lvt_11_1_ = p_236248_4_ - lvt_9_1_ / 2;
			int x2 = lvt_10_1_[0];
			int y2 = lvt_10_1_[1];
			int z2 = lvt_10_1_[2];

			while (lvt_11_1_ >= 0)
			{
				LocalMaterialData current = world.getMaterialDirect(x2, y2, z2);
				if (this.sourceBlocks.contains(current))
				{
					world.setBlockDirect(x2, y2, z2, this.material);
					y2++;
				} else if (current.isMaterial(this.material))
				{
					y2++;

				} else
				{
					break;
				}
				lvt_11_1_--;

			}
		}
	}

	private int[] findAir(IWorldGenRegion world, int[] pos, int p_236249_2_)
	{
		while (pos[1] < Constants.WORLD_HEIGHT && p_236249_2_ > 0)
		{
			p_236249_2_--;
			if (this.sourceBlocks.contains(world.getMaterialDirect(pos[0], pos[1], pos[2])))
				return pos;

			pos[1]++;
		}
		return null;
	}

	private static boolean canPlaceAt(IWorldGenRegion world, int x, int y, int z)
	{
		if (world.getMaterialDirect(x, y, z).isAir())
		{
			return (!world.getMaterialDirect(x, y - 1, z).isAir());
		}
		return false;
	}

	private static int[] findSurface(IWorldGenRegion world, int p_236246_1_, int[] pos, int p_236246_3_)
	{
		while (pos[1] > 1 && p_236246_3_ > 0)
		{
			p_236246_3_--;
			if (canPlaceAt(world, pos[0], pos[1], pos[2]))
				return pos;

			pos[1]--;
		}
		return null;
	}

	@Override
	public String toString()
	{
		return "BasaltColumn(" + this.material + "," + this.frequency + "," + this.rarity + "," + this.baseSize + ","
				+ this.sizeVariance + "," + this.baseHeight + "," + this.heightVariance + "," + this.minAltitude + ","
				+ this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}
}
