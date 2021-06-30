package com.pg85.otg.gen.resource;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

/**
 * Generates a disk-alike structure for sand, gravel, and clay.
 * TODO: This needs to be renamed to DiskGen()
 */
public class UnderWaterOreResource extends FrequencyResourceBase
{
	private final LocalMaterialData material;
	private final int size;
	private final MaterialSet sourceBlocks;

	public UnderWaterOreResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(5, args);
		this.material = materialReader.readMaterial(args.get(0));
		this.size = readInt(args.get(1), 1, 8);
		this.frequency = readInt(args.get(2), 1, 100);
		this.rarity = readRarity(args.get(3));
		this.sourceBlocks = readMaterials(args, 4, materialReader);
	}

	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random rand, boolean villageInChunk, int x, int z)
	{
		int firstSolidBlock = worldGenRegion.getBlockAboveSolidHeight(x, z) - 1;
		if (worldGenRegion.getBlockAboveLiquidHeight(x, z) < firstSolidBlock || firstSolidBlock == -1)
		{
			return;
		}

		if(worldGenRegion.getWorldConfig().isDisableOreGen())
		{
			if(this.material.isOre())
			{
				return;
			}
		}
		
		int currentSize = rand.nextInt(this.size) + 2;
		int deltaX;
		int deltaZ;
		LocalMaterialData sourceBlock;
		for (int currentX = x - currentSize; currentX <= x + currentSize; currentX++)
		{
			for (int currentZ = z - currentSize; currentZ <= z + currentSize; currentZ++)
			{
				deltaX = currentX - x;
				deltaZ = currentZ - z;
				if (deltaX * deltaX + deltaZ * deltaZ <= currentSize * currentSize)
				{
					for (int y = firstSolidBlock - 2; y <= firstSolidBlock + 2; y++)
					{
						sourceBlock = worldGenRegion.getMaterial(currentX, y, currentZ);
						if (this.sourceBlocks.contains(sourceBlock))
						{
							worldGenRegion.setBlock(currentX, y, currentZ, this.material);
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "UnderWaterOre(" + this.material + "," + this.size + "," + this.frequency + "," + this.rarity + makeMaterials(this.sourceBlocks) + ")";
	}	
}
