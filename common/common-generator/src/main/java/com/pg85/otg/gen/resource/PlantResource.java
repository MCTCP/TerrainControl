package com.pg85.otg.gen.resource;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.gen.resource.util.BerryBush;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.PlantType;
import com.pg85.otg.gen.resource.util.BerryBush.SparseOption;

import java.util.List;
import java.util.Random;

public class PlantResource extends FrequencyResourceBase
{
	private final int maxAltitude;
	private final int minAltitude;
	private final PlantType plant;
	private final MaterialSet sourceBlocks;
	private SparseOption sparseOption = null;

	public PlantResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(6, args);

		this.plant = PlantType.getPlant(args.get(0), materialReader);
		int i = 0;
		if (args.get(1).equalsIgnoreCase("Sparse") || args.get(1).equalsIgnoreCase("Decorated")){
			this.sparseOption = args.get(1).equalsIgnoreCase("Sparse") ? SparseOption.Sparse : SparseOption.Decorated;
			i = 1;
		}
		this.frequency = readInt(args.get(1 + i), 1, 100);
		this.rarity = readRarity(args.get(2 + i));
		this.minAltitude = readInt(args.get(3 + i), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(4 + i), this.minAltitude, Constants.WORLD_HEIGHT - 1);
		this.sourceBlocks = readMaterials(args, 5 + i, materialReader);
	}

	@Override
	public void spawn(IWorldGenRegion worldGenregion, Random rand, int x, int z)
	{
		if (sparseOption != null && plant == PlantType.BerryBush){
			BerryBush.spawnBerryBushes(worldGenregion, rand, x, z, plant, frequency, minAltitude, maxAltitude, sourceBlocks, sparseOption);
			return;
		}
		int y = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);

		LocalMaterialData worldMaterial;
		LocalMaterialData worldMaterialBelow;
		
		int localX;
		int localY;
		int localZ;
		for (int i = 0; i < 64; i++)
		{
			localX = x + rand.nextInt(8) - rand.nextInt(8);
			localY = y + rand.nextInt(4) - rand.nextInt(4);
			localZ = z + rand.nextInt(8) - rand.nextInt(8);
			worldMaterial = worldGenregion.getMaterial(localX, localY, localZ);
			worldMaterialBelow = worldGenregion.getMaterial(localX, localY - 1, localZ);
			if (
				(worldMaterial == null || !worldMaterial.isAir()) ||
				(worldMaterialBelow == null || !this.sourceBlocks.contains(worldMaterialBelow))
			)
			{
				continue;
			}

			this.plant.spawn(worldGenregion, localX, localY, localZ);
		}
	}
	
	@Override
	public String toString()
	{
		String sparse = (sparseOption == null) ? "" : sparseOption + ",";
		return "Plant(" + this.plant.getName() + "," + sparse + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}
}
