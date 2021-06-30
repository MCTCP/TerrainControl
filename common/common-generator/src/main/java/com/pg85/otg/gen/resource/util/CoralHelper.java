package com.pg85.otg.gen.resource.util;

import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.OTGDirection;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialProperties;

import java.util.Random;

public final class CoralHelper
{
	private static final OTGDirection[] HORIZONTAL = {
		OTGDirection.NORTH, 
		OTGDirection.EAST, 
		OTGDirection.SOUTH, 
		OTGDirection.WEST
	};

	private CoralHelper() {}

	public static boolean placeCoralBlock(IWorldGenRegion world, Random random, int x, int y, int z, LocalMaterialData data)
	{
		LocalMaterialData currentState = world.getMaterial(x, y, z);
		
		// Check for water or coral here, and water above
		if (
			(currentState.isMaterial(LocalMaterials.WATER) || isCoral(currentState)) && 
			world.getMaterial(x, y + 1, z).isMaterial(LocalMaterials.WATER)
		)
		{
			// Set the coral state
			world.setBlock(x, y, z, data);

			if (random.nextFloat() < 0.25f)
			{
				// Set random coral on the top block
				world.setBlock(x, y + 1, z, getRandomCoral(random));
			}
			else if (random.nextFloat() < 0.05f)
			{
				// Place a pickle above, 1 in 20 coral placements
				world.setBlock(x, y + 1, z, LocalMaterials.SEA_PICKLE.withProperty(MaterialProperties.PICKLES_1_4, random.nextInt(4) + 1));
			}

			LocalMaterialData wallMaterial;
			for (OTGDirection direction : HORIZONTAL)
			{
				if (random.nextFloat() < 0.2f)
				{
					if (world.getMaterial(x + direction.getX(), y + direction.getY(), z + direction.getZ()).isMaterial(LocalMaterials.WATER))
					{
						wallMaterial = getRandomWallCoral(random).withProperty(MaterialProperties.HORIZONTAL_DIRECTION, direction);
						world.setBlock(x + direction.getX(), y + direction.getY(), z + direction.getZ(), wallMaterial);
					}
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isCoral(LocalMaterialData data)
	{
		return LocalMaterials.CORALS.contains(data);
	}

	public static LocalMaterialData getRandomCoral(Random random)
	{
		return LocalMaterials.CORALS.get(random.nextInt(LocalMaterials.CORALS.size()));
	}

	public static LocalMaterialData getRandomWallCoral(Random random)
	{
		return LocalMaterials.WALL_CORALS.get(random.nextInt(LocalMaterials.WALL_CORALS.size()));
	}

	public static LocalMaterialData getRandomCoralBlock(Random random)
	{
		return LocalMaterials.CORAL_BLOCKS.get(random.nextInt(LocalMaterials.CORAL_BLOCKS.size()));
	}
}
