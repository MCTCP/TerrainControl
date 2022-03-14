package com.pg85.otg.gen.resource;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums.IceSpikeType;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

public class IceSpikeResource extends FrequencyResourceBase
{
	private final LocalMaterialData material;
	private final int maxAltitude;
	private final int minAltitude;
	private final MaterialSet sourceBlocks;
	private IceSpikeType type;

	public IceSpikeResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(2, args);

		this.material = materialReader.readMaterial(args.get(0));

		// Read type
		String typeString = args.get(1);
		this.type = null;
		for(IceSpikeType possibleType : IceSpikeType.values())
		{
			if (possibleType.toString().equalsIgnoreCase(typeString))
			{
				this.type = possibleType;
				break;
			}
		}
		if (this.type == null)
		{
			throw new InvalidConfigException("Unknown spike type " + typeString);
		}

		this.frequency = readInt(args.get(2), 1, 30);
		this.rarity = readRarity(args.get(3));
		this.minAltitude = readInt(args.get(4), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(5), this.minAltitude, Constants.WORLD_HEIGHT - 1);

		this.sourceBlocks = readMaterials(args, 6, materialReader);
	}

	@Override
	public void spawn(IWorldGenRegion worldGenregion, Random random, int x, int z)
	{
		switch(this.type)
		{
			case Basement:
				spawnBasement(worldGenregion, random, x, z);
				break;
			case HugeSpike:
				spawnSpike(worldGenregion, random, x, z, true);
				break;
			case SmallSpike:
				spawnSpike(worldGenregion, random, x, z, false);
				break;
		}
	}

	private void spawnBasement(IWorldGenRegion worldGenRegion, Random random,int x, int z)
	{
		int y = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);

		LocalMaterialData worldMaterial;
		while (
			y > 2 && 
			(worldMaterial = worldGenRegion.getMaterial(x, y, z)) != null && 
			worldMaterial.isAir()
		)
		{
			y--;
		}
		
		if (
			(worldMaterial = worldGenRegion.getMaterial(x, y, z)) == null || 
			!this.sourceBlocks.contains(worldMaterial)
		)
		{
			return;
		}
		
		int radius = random.nextInt(2) + 2;
		int one = 1;
		int deltaX;
		int deltaZ;
		IBiomeConfig biomeConfig;
		for (int actualX = x - radius; actualX <= x + radius; actualX++)
		{
			for (int actualZ = z - radius; actualZ <= z + radius; actualZ++)
			{
				biomeConfig = worldGenRegion.getBiomeConfigForDecoration(actualX, actualZ);
				deltaX = actualX - x;
				deltaZ = actualZ - z;
				if (deltaX * deltaX + deltaZ * deltaZ <= radius * radius)
				{
					for (int deltaY = y - one; deltaY <= y + one; deltaY++)
					{
						worldMaterial = worldGenRegion.getMaterial(actualX, deltaY, actualZ);
						if (worldMaterial != null && this.sourceBlocks.contains(worldMaterial))
						{
							worldGenRegion.setBlock(actualX, deltaY, actualZ, this.material, biomeConfig.getReplaceBlocks());
						}
					}
				}
			}
		}
	}

	private void spawnSpike(IWorldGenRegion worldGenRegion, Random random, int x, int z, boolean hugeSpike)
	{
		int y = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);
		LocalMaterialData worldMaterial;
		while (
			y > 2 && 
			(worldMaterial = worldGenRegion.getMaterial(x, y, z)) != null && 
			worldMaterial.isAir()
		)
		{
			--y;
		}
		
		if (
			(worldMaterial = worldGenRegion.getMaterial(x, y, z)) == null || 
			!this.sourceBlocks.contains(worldMaterial)
		)
		{
			return;
		}

		y += random.nextInt(4);
		int var6 = random.nextInt(4) + 7;
		int var7 = var6 / 4 + random.nextInt(2);

		if (var7 > 1 && hugeSpike)
		{
			y += 10 + random.nextInt(30);
		}

		int var8;
		float var9;
		int var10;
		int var11;
		float var12;
		float var14;
		IBiomeConfig biomeConfig;
		for (var8 = 0; var8 < var6; ++var8)
		{
			var9 = (1.0F - (float) var8 / (float) var6) * var7;
			var10 = MathHelper.ceil(var9);

			for (var11 = -var10; var11 <= var10; ++var11)
			{
				var12 = MathHelper.abs(var11) - 0.25F;
				
				for (int var13 = -var10; var13 <= var10; ++var13)
				{
					var14 = MathHelper.abs(var13) - 0.25F;
					biomeConfig = worldGenRegion.getBiomeConfigForDecoration(x + var11, z + var13);

					if ((var11 == 0 && var13 == 0 || var12 * var12 + var14 * var14 <= var9 * var9) && (var11 != -var10 && var11 != var10 && var13 != -var10 && var13 != var10 || random.nextFloat() <= 0.75F))
					{
						if (
							(worldMaterial = worldGenRegion.getMaterial(x + var11, y + var8, z + var13)) != null && 
							(worldMaterial.isAir() || this.sourceBlocks.contains(worldMaterial))
						)
						{
							worldGenRegion.setBlock(x + var11, y + var8, z + var13, this.material, biomeConfig.getReplaceBlocks());
						}

						if (var8 != 0 && var10 > 1)
						{
							if (
								(worldMaterial = worldGenRegion.getMaterial(x + var11, y - var8, z + var13)) != null && 
								(worldMaterial.isAir() || this.sourceBlocks.contains(worldMaterial))
							)
							{
								worldGenRegion.setBlock(x + var11, y - var8, z + var13, this.material, biomeConfig.getReplaceBlocks());
							}							
						}
					}
				}
			}
		}

		var8 = var7 - 1;

		if (var8 < 0)
		{
			var8 = 0;
		}
		else if (var8 > 1)
		{
			var8 = 1;
		}

		int var17;
		for (int var16 = -var8; var16 <= var8; ++var16)
		{
			var10 = -var8;

			while (var10 <= var8)
			{
				var11 = y - 1;
				var17 = 50;

				if (Math.abs(var16) == 1 && Math.abs(var10) == 1)
				{
					var17 = random.nextInt(5);
				}

				while (true)
				{
					if (var11 > 50)
					{
						if(
							(worldMaterial = worldGenRegion.getMaterial(x + var16, var11, z + var10)) != null &&
							(
								worldMaterial.isAir() || 
								this.sourceBlocks.contains(worldMaterial) || 
								worldMaterial.equals(this.material)
							)
						)
						{
							worldGenRegion.setBlock(x + var16, var11, z + var10, this.material, worldGenRegion.getBiomeConfigForDecoration(x + var16, z + var10).getReplaceBlocks());
							--var11;
							--var17;

							if (var17 <= 0)
							{
								var11 -= random.nextInt(5) + 1;
								var17 = random.nextInt(5);
							}

							continue;
						}
					}

					++var10;
					break;
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "IceSpike(" + this.material + "," + this.type + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}	
}
