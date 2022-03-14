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
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.PlantType;

import java.util.List;
import java.util.Random;

public class UnderWaterPlantResource extends FrequencyResourceBase
{
	private final int maxAltitude;
	private final int minAltitude;
	private final PlantType plant;
	private final MaterialSet sourceBlocks;

	public UnderWaterPlantResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(6, args);

		this.plant = PlantType.getPlant(args.get(0), materialReader);
		this.frequency = readInt(args.get(1), 1, 100);
		this.rarity = readRarity(args.get(2));
		this.minAltitude = readInt(args.get(3), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(4), this.minAltitude, Constants.WORLD_HEIGHT - 1);
		this.sourceBlocks = readMaterials(args, 5, materialReader);
	}

	@Override
	public void spawn(IWorldGenRegion worldGenregion, Random rand, int x, int z)
	{
		int y = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);

		int j;
		int k;
		int m;
		LocalMaterialData worldMaterial;
		LocalMaterialData worldMaterialBelow;		
		for (int i = 0; i < 64; i++)
		{
			j = x + rand.nextInt(8) - rand.nextInt(8);
			k = y + rand.nextInt(4) - rand.nextInt(4);
			m = z + rand.nextInt(8) - rand.nextInt(8);
			worldMaterial = worldGenregion.getMaterial(j, k , m);
			worldMaterialBelow = worldGenregion.getMaterial(j, k - 1, m);
			if (
				(worldMaterial == null || !worldMaterial.isMaterial(LocalMaterials.WATER)) ||
				(worldMaterialBelow == null || !this.sourceBlocks.contains(worldMaterialBelow))
			)
			{
				continue;
			}
			this.plant.spawn(worldGenregion, j, k, m);
		}
	}
	
	@Override
	public String toString()
	{
		return "UnderWaterPlant(" + this.plant.getName() + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}	
}
