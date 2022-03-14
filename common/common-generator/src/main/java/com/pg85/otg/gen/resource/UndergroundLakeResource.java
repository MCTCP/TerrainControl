package com.pg85.otg.gen.resource;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.util.List;
import java.util.Random;

public class UndergroundLakeResource extends FrequencyResourceBase
{
	private final int maxAltitude;
	private final int maxSize;
	private final int minAltitude;
	private final int minSize;

	public UndergroundLakeResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(6, args);
		this.minSize = readInt(args.get(0), 1, 25);
		this.maxSize = readInt(args.get(1), this.minSize, 60);
		this.frequency = readInt(args.get(2), 1, 100);
		this.rarity = readRarity(args.get(3));
		this.minAltitude = readInt(args.get(4), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(5), this.minAltitude, Constants.WORLD_HEIGHT - 1);
	}

	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random rand, int x, int z)
	{
		int y = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);
		if (y >= worldGenRegion.getHighestBlockAboveYAt(x, z))
		{
			return;
		}
		
		int size = RandomHelper.numberInRange(rand, this.minSize, this.maxSize);

		float mPi = rand.nextFloat() * 3.141593F;

		double x1 = x + 8 + MathHelper.sin(mPi) * size / 8.0F;
		double x2 = x + 8 - MathHelper.sin(mPi) * size / 8.0F;
		double z1 = z + 8 + MathHelper.cos(mPi) * size / 8.0F;
		double z2 = z + 8 - MathHelper.cos(mPi) * size / 8.0F;

		double y1 = y + rand.nextInt(3) + 2;
		double y2 = y + rand.nextInt(3) + 2;

		double xAdjusted;
		double yAdjusted;
		double zAdjusted;
		double horizontalSizeMultiplier;
		double verticalSizeMultiplier;
		double horizontalSize;
		double verticalSize;
		LocalMaterialData material;
		double xBounds;
		double yBounds;
		double zBounds;
		LocalMaterialData materialBelow;
		for (int i = 0; i <= size; i++)
		{
			xAdjusted = x1 + (x2 - x1) * i / size;
			yAdjusted = y1 + (y2 - y1) * i / size;
			zAdjusted = z1 + (z2 - z1) * i / size;

			horizontalSizeMultiplier = rand.nextDouble() * size / 16.0D;
			verticalSizeMultiplier = rand.nextDouble() * size / 32.0D;
			horizontalSize = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * horizontalSizeMultiplier + 1.0D;
			verticalSize = (MathHelper.sin(i * 3.141593F / size) + 1.0F) * verticalSizeMultiplier + 1.0D;

			for (int xLake = (int) (xAdjusted - horizontalSize / 2.0D); xLake <= (int) (xAdjusted + horizontalSize / 2.0D); xLake++)
			{
				for (int yLake = (int) (yAdjusted - verticalSize / 2.0D); yLake <= (int) (yAdjusted + verticalSize / 2.0D); yLake++)
				{
					for (int zLake = (int) (zAdjusted - horizontalSize / 2.0D); zLake <= (int) (zAdjusted + horizontalSize / 2.0D); zLake++)
					{
						material = worldGenRegion.getMaterial(xLake, yLake, zLake);
						if (material == null || material.isEmptyOrAir() || material.isMaterial(LocalMaterials.BEDROCK))
						{
							// Don't replace air or bedrock
							continue;
						}

						xBounds = (xLake + 0.5D - xAdjusted) / (horizontalSize / 2.0D);
						yBounds = (yLake + 0.5D - yAdjusted) / (verticalSize / 2.0D);
						zBounds = (zLake + 0.5D - zAdjusted) / (horizontalSize / 2.0D);
						if (xBounds * xBounds + yBounds * yBounds + zBounds * zBounds >= 1.0D)
						{
							continue;
						}

						materialBelow = worldGenRegion.getMaterial(xLake, yLake - 1, zLake);
						if (materialBelow != null && materialBelow.isAir())
						{
							// Air block, also set position above to air
							worldGenRegion.setBlock(xLake, yLake, zLake, LocalMaterials.AIR);
						} else {
							// Not air, set position above to water
							worldGenRegion.setBlock(xLake, yLake, zLake, LocalMaterials.WATER);
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "UnderGroundLake(" + this.minSize + "," + this.maxSize + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + ")";
	}	
}
