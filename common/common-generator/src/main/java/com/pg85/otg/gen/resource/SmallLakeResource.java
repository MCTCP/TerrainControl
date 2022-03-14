package com.pg85.otg.gen.resource;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.util.List;
import java.util.Random;

/**
 * Generates a small lake. The end result will invariably look poor and cause issues so it's recommended to create custom objects for your lakes.
 */
public class SmallLakeResource extends FrequencyResourceBase
{
	private final LocalMaterialData material;
	private final int maxAltitude;
	private final int minAltitude;

	public SmallLakeResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(5, args);
		this.material = materialReader.readMaterial(args.get(0));
		this.frequency = readInt(args.get(1), 1, 100);
		this.rarity = readRarity(args.get(2));
		this.minAltitude = readInt(args.get(3), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(4), this.minAltitude, Constants.WORLD_HEIGHT - 1);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, ILogger logger, IMaterialReader materialReader)
	{
		// TODO: This only checks for a structure start, need to expose the MC method for finding villages on worldGenRegion.
		if(worldGenRegion.chunkHasDefaultStructure(random, worldGenRegion.getDecorationArea().getChunkBeingDecorated()))
		{
			return;
		}
		super.spawnForChunkDecoration(worldGenRegion, random, logger, materialReader);
	}	
	
	@Override
	public void spawn(IWorldGenRegion world, Random rand, int x, int z)
	{
		int y = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);

		// Search any free space
		LocalMaterialData worldMaterial;
		while (
			y > 5 && 
			(worldMaterial = world.getMaterial(x, y, z)) != null && 
			worldMaterial.isAir()
		)
		{
			y--;
		}

		if (y <= 4)
		{
			return;
		}

		// y = floor
		y -= 4;

		LocalMaterialData localMaterialData;
		LocalMaterialData localMaterialData2;
		LocalMaterialData air = LocalMaterials.AIR;
		boolean[] lakeMask = new boolean[2048];

		double lakeSizeX;
		double lakeSizeY;
		double lakeSizeZ;
		double scaledLakeX;
		double scaledLakeY;
		double scaledLakeZ;
		double distX;
		double distY;
		double distZ;
		double distance;
		
		boolean flag;
		for (int j = 0; j < rand.nextInt(4) + 4; j++)
		{
			lakeSizeX = rand.nextDouble() * 6.0D + 3.0D;
			lakeSizeY = rand.nextDouble() * 4.0D + 2.0D;
			lakeSizeZ = rand.nextDouble() * 6.0D + 3.0D;

			scaledLakeX = rand.nextDouble() * (16.0D - lakeSizeX - 2.0D) + 1.0D + lakeSizeX / 2.0D;
			scaledLakeY = rand.nextDouble() * (8.0D - lakeSizeY - 4.0D) + 2.0D + lakeSizeY / 2.0D;
			scaledLakeZ = rand.nextDouble() * (16.0D - lakeSizeZ - 2.0D) + 1.0D + lakeSizeZ / 2.0D;

			for (int lakeX = 1; lakeX < 15; lakeX++)
			{
				for (int lakeZ = 1; lakeZ < 15; lakeZ++)
				{
					for (int lakeY = 1; lakeY < 7; lakeY++)
					{
						distX = (lakeX - scaledLakeX) / (lakeSizeX / 2.0D);
						distY = (lakeY - scaledLakeY) / (lakeSizeY / 2.0D);
						distZ = (lakeZ - scaledLakeZ) / (lakeSizeZ / 2.0D);
						distance = distX * distX + distY * distY + distZ * distZ;
						if (distance >= 1.0D)
						{
							continue;
						}

						lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)] = true;
					}
				}
			}
		}


		for (int lakeX = 0; lakeX < 16; lakeX++)
		{
			for (int lakeZ = 0; lakeZ < 16; lakeZ++)
			{
				for (int lakeY = 0; lakeY < 8; lakeY++)
				{
					flag = (!lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)])
						&& (((lakeX < 15) && (lakeMask[(((lakeX + 1) * 16 + lakeZ) * 8 + lakeY)]))
						|| ((lakeX > 0) && (lakeMask[(((lakeX - 1) * 16 + lakeZ) * 8 + lakeY)]))
						|| ((lakeZ < 15) && (lakeMask[((lakeX * 16 + (lakeZ + 1)) * 8 + lakeY)]))
						|| ((lakeZ > 0) && (lakeMask[((lakeX * 16 + (lakeZ - 1)) * 8 + lakeY)]))
						|| ((lakeY < 7) && (lakeMask[((lakeX * 16 + lakeZ) * 8 + (lakeY + 1))]))
						|| ((lakeY > 0) && (lakeMask[((lakeX * 16 + lakeZ) * 8 + (lakeY - 1))])));

					if (flag)
					{
						localMaterialData = world.getMaterial(x + lakeX, y + lakeY, z + lakeZ);
						if ((lakeY >= 4) && (localMaterialData == null || localMaterialData.isLiquid()))
						{
							return;
						}
						localMaterialData2 = world.getMaterial(x + lakeX, y + lakeY, z + lakeZ);
						if ((lakeY < 4) && (localMaterialData == null || !localMaterialData.isSolid()) && (localMaterialData2 == null || !localMaterialData2.equals(material)))
						{
							return;
						}
					}
				}
			}
		}

		for (int lakeX = 0; lakeX < 16; lakeX++)
		{
			for (int lakeZ = 0; lakeZ < 16; lakeZ++)
			{
				for (int lakeY = 0; lakeY < 4; lakeY++)
				{
					if (lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)])
					{
						world.setBlock(x + lakeX, y + lakeY, z + lakeZ, this.material);
						lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)] = false;
					}
				}

				for (int lakeY = 4; lakeY < 8; lakeY++)
				{
					if (lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)])
					{
						world.setBlock(x + lakeX, y + lakeY, z + lakeZ, air);
						lakeMask[((lakeX * 16 + lakeZ) * 8 + lakeY)] = false;
					}
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "SmallLake(" + this.material + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + ")";
	}	
}
