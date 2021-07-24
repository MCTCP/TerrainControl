package com.pg85.otg.gen.resource;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

/**
 * Generates a waterfall feature, called a Spring in minecraft code.
 */
public class LiquidResource extends FrequencyResourceBase
{
	private final LocalMaterialData material;
	private final int maxAltitude;
	private final int minAltitude;
	private final MaterialSet sourceBlocks;

	public LiquidResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(6, args);

		this.material = materialReader.readMaterial(args.get(0));
		this.frequency = readInt(args.get(1), 1, 5000);
		this.rarity = readRarity(args.get(2));
		this.minAltitude = readInt(args.get(3), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(4), this.minAltitude, Constants.WORLD_HEIGHT - 1);
		this.sourceBlocks = readMaterials(args, 5, materialReader);
		System.out.println("LiquidResource constructed: (Material:" + this.material + ", Freq:" + this.frequency + ", Rarity:" + this.rarity + ", Min Altitude:" + this.minAltitude + ", Max Altitude:" + this.maxAltitude + ", Source Blocks:" + this.sourceBlocks);
	}

	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random rand, int x, int z)
	{
		System.out.println("Attempting to Spawn Liquid() at " + x + ", " + z);
		int y = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);
		System.out.println("Y value: " + y);
		LocalMaterialData worldMaterial = worldGenRegion.getMaterial(x, y + 1, z);
		if (worldMaterial == null || !this.sourceBlocks.contains(worldMaterial))
		{
			System.out.println("WorldMaterial Null Above");
			System.out.println("WorldMaterial: " + worldMaterial);
			return;
		}
		
		worldMaterial = worldGenRegion.getMaterial(x, y - 1, z);
		if (worldMaterial == null || !this.sourceBlocks.contains(worldMaterial))
		{
			System.out.println("WorldMaterial Null Below");
			System.out.println("WorldMaterial: " + worldMaterial);
			return;
		}

		worldMaterial = worldGenRegion.getMaterial(x, y, z);
		if (worldMaterial == null || (!worldMaterial.isAir() && !this.sourceBlocks.contains(worldMaterial)))
		{
			System.out.println("WorldMaterial Null Same");
			System.out.println("WorldMaterial: " + worldMaterial);
			return;
		}

		int sourceCount = 0;
		int airCount = 0;

		worldMaterial = worldGenRegion.getMaterial(x - 1, y, z);
		System.out.println("Block x - 1: " + worldMaterial);
		sourceCount = (worldMaterial != null && this.sourceBlocks.contains(worldMaterial)) ? sourceCount + 1 : sourceCount;
		airCount = (worldMaterial != null && worldMaterial.isAir()) ? airCount + 1 : airCount;

		worldMaterial = worldGenRegion.getMaterial(x + 1, y, z);
		System.out.println("Block x + 1: " + worldMaterial);
		sourceCount = (worldMaterial != null && this.sourceBlocks.contains(worldMaterial)) ? sourceCount + 1 : sourceCount;
		airCount = (worldMaterial != null && worldMaterial.isAir()) ? airCount + 1 : airCount;

		worldMaterial = worldGenRegion.getMaterial(x, y, z - 1);
		System.out.println("Block z - 1: " + worldMaterial);
		sourceCount = (worldMaterial != null && this.sourceBlocks.contains(worldMaterial)) ? sourceCount + 1 : sourceCount;
		airCount = (worldMaterial != null && worldMaterial.isAir()) ? airCount + 1 : airCount;

		worldMaterial = worldGenRegion.getMaterial(x, y, z + 1);
		System.out.println("Block z + 1: " + worldMaterial);
		sourceCount = (worldMaterial != null && this.sourceBlocks.contains(worldMaterial)) ? sourceCount + 1 : sourceCount;
		airCount = (worldMaterial != null && worldMaterial.isAir()) ? airCount + 1 : airCount;

		if ((sourceCount == 3) && (airCount == 1))
		{
			System.out.println(">>> SETTING BLOCK...");
			worldGenRegion.setBlock(x, y, z, this.material);
		}
	}
	
	@Override
	public String toString()
	{
		return "Liquid(" + this.material + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}	
}
