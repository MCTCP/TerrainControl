package com.pg85.otg.gen.resource;

import java.util.List;
import java.util.Random;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

public class IcebergResource extends BiomeResourceBase implements IBasicResource
{
	private final LocalMaterialData[] materials;
	private final LocalMaterialData[] materials2;
	private final double[] rarities;
	private final double totalRarity;

	public IcebergResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(4, args);

		int size = (int) Math.floor(args.size() / 3);
		this.materials = new LocalMaterialData[size];
		this.materials2 = new LocalMaterialData[size];
		this.rarities = new double[size];

		int pos = 0;
		for (int i = 0; i < args.size() - 1; i+=3)
		{
			this.materials[pos] = materialReader.readMaterial(args.get(i));
			this.materials2[pos] = materialReader.readMaterial(args.get(i + 1));
			this.rarities[pos] = readRarity(args.get(i + 2));
			pos++;
		}
		this.totalRarity = StringHelper.readDouble(args.get(args.size() - 1), 0.000001, Integer.MAX_VALUE);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion world, Random random, ILogger logger, IMaterialReader materialReader)
	{
		LocalMaterialData material = null;
		LocalMaterialData material2 = null;
		double currentRarity = 0;
		int rarity = random.nextInt((int) this.totalRarity);

		for (int i = 0; i < this.rarities.length; i++)
		{
			currentRarity += this.rarities[i];
			if (currentRarity >= rarity)
			{
				material = this.materials[i];
				material2 = this.materials2[i];
				break;
			}
		}
		
		if (material == null)
		{
			return;
		}
		
		int x = world.getDecorationArea().getChunkBeingDecorated().getBlockX();
		int z = world.getDecorationArea().getChunkBeingDecorated().getBlockZ();
		int y = world.getBiomeConfigForDecoration(x, z).getWaterLevelMax();
		boolean flag1 = random.nextDouble() > 0.7D;
		double drandom1 = random.nextDouble() * 2.0D * Math.PI;
		int irandom1 = 11 - random.nextInt(5);
		int irandom2 = 3 + random.nextInt(3);
		boolean flag2 = random.nextDouble() > 0.7D;

		int ismoothheight = flag2 ? random.nextInt(6) + 6 : random.nextInt(15) + 3;
		if (!flag2 && random.nextDouble() > 0.9D)
		{
			ismoothheight += random.nextInt(19) + 7;
		}

		int irandom4 = Math.min(ismoothheight + random.nextInt(11), 18);
		int irandom5 = Math.min(ismoothheight + random.nextInt(7) - random.nextInt(5), 11);
		int irandom6 = flag2 ? irandom1 : 11;

		for (int x1 = -irandom6; x1 < irandom6; x1++)
		{
			for (int z1 = -irandom6; z1 < irandom6; z1++)
			{
				for (int y1 = 0; y1 < ismoothheight; y1++)
				{
					int heightDependentRadiusEllipse = flag2 ? heightDependentRadiusEllipse(y1, ismoothheight, irandom5) : heightDependentRadiusRound(random, y1, ismoothheight, irandom5);
					if (flag2 || x1 < heightDependentRadiusEllipse)
					{
						generateIcebergBlock(world, random, x, y, z, ismoothheight, x1, y1, z1, heightDependentRadiusEllipse, irandom6, flag2, irandom2, drandom1, flag1, material, material2);
					}
				}
			}
		}

		smooth(world, x, y, z, irandom5, ismoothheight, flag2, irandom1, material, material2);

		for (int x1 = -irandom6; x1 < irandom6; x1++)
		{
			for (int z1 = -irandom6; z1 < irandom6; z1++)
			{
				for (int y1 = -1; y1 > -irandom4; y1--)
				{
					int irandom7 = flag2 ? MathHelper.ceil((float)irandom6 * (1.0F - (float) Math.pow((double)y1, 2.0D) / ((float)irandom4 * 8.0F))) : irandom6;
					int heightDependentRadiusSteep = heightDependentRadiusSteep(random, -y1, irandom4, irandom5);
					if (x1 < heightDependentRadiusSteep)
					{
						generateIcebergBlock(world, random, x, y, z, irandom4, x1, y1, z1, heightDependentRadiusSteep, irandom7, flag2, irandom2, drandom1, flag1, material, material2);
					}
				}
			}
		}
		boolean flag3 = flag2 ? random.nextDouble() > 0.1D : random.nextDouble() > 0.7D;
		if (flag3)
		{
			generateCutOut(random, world, irandom5, ismoothheight, x, y, z, flag2, irandom1, drandom1, irandom2, material, material2);
		}
	}

	private void generateCutOut(Random random, IWorldGenRegion world, int irandom, int irandom1, int x, int y, int z, boolean flag, int irandom2, double drandom1, int irandom3, LocalMaterialData material, LocalMaterialData material2)
	{
		int irandom4 = random.nextBoolean() ? -1 : 1;
		int irandom5 = random.nextBoolean() ? -1 : 1;
		int irandom6 = random.nextInt(Math.max(irandom / 2 - 2, 1));
		if (random.nextBoolean())
		{
			irandom6 = irandom / 2 + 1 - random.nextInt(Math.max(irandom - irandom / 2 - 1, 1));
		}

		int irandom7 = random.nextInt(Math.max(irandom / 2 - 2, 1));
		if (random.nextBoolean())
		{
			irandom7 = irandom / 2 + 1 - random.nextInt(Math.max(irandom - irandom / 2 - 1, 1));
		}

		if (flag)
		{
			irandom6 = irandom7 = random.nextInt(Math.max(irandom2 - 5, 1));
		}

		int x2 = irandom4 * irandom6;
		int z2 = irandom5 * irandom7;
		double drandom2 = flag ? drandom1 + (Math.PI / 2D) : random.nextDouble() * 2.0D * Math.PI;
		//double drandom2 = flag ? (drandom1 + 1.5707963267948966D) : (random.nextDouble() * 2.0D * Math.PI);

		for (int irandom8 = 0; irandom8 < irandom1 - 3; ++irandom8)
		{
			int heightDependentRadiusRound = heightDependentRadiusRound(random, irandom8, irandom1, irandom);
			carve(heightDependentRadiusRound, irandom8, x, y, z, world, false, drandom2, x2, z2, irandom2, irandom3, material, material2);
		}
		for (int irandom8 = -1; irandom8 > -irandom1 + random.nextInt(5); --irandom8)
		{
			int heightDependentRadiusSteep = heightDependentRadiusSteep(random, -irandom8, irandom1, irandom);
			carve(heightDependentRadiusSteep, irandom8, x, y, z, world, true, drandom2, x2, z2, irandom2, irandom3, material, material2);
		}
	}

	private void carve(int heightDependentRadius, int irandom, int x, int y, int z, IWorldGenRegion world, boolean flag, double drandom, int x3, int z3, int irandom1, int irandom2, LocalMaterialData material, LocalMaterialData material2)
	{
		int irandom3 = heightDependentRadius + 1 + irandom1 / 3;
		int irandom4 = Math.min(heightDependentRadius - 3, 3) + irandom2 / 2 - 1;
		int x2;
		int y2;
		int z2;
		IBiomeConfig biomeConfig;
		LocalMaterialData replacedMaterial;
		LocalMaterialData replacedMaterial2;
		LocalMaterialData current;
		for (int x1 = -irandom3; x1 < irandom3; x1++)
		{
			for (int z1 = -irandom3; z1 < irandom3; z1++)
			{
				double signedDistanceEllipse = signedDistanceEllipse(x1, z1, x3, 0, z3, irandom3, irandom4, drandom);
				if (signedDistanceEllipse < 0.0D)
				{
					x2 = x + x1;
					y2 = y + irandom;
					z2 = z + z1;
					current = world.getMaterialDirect(x2, y2, z2);
					// TODO: Request all up front instead of using cacheChunk true?
					biomeConfig = world.getCachedBiomeProvider().getBiomeConfig(x2, z2, true);
					replacedMaterial = material;
					replacedMaterial2 = material2;
					if(biomeConfig.getReplaceBlocks() != null)
					{
						replacedMaterial = biomeConfig.getReplaceBlocks().replaceBlock(y2, material);
						replacedMaterial2 = biomeConfig.getReplaceBlocks().replaceBlock(y2, material2);
					}
					if (isIcebergBlock(current, replacedMaterial, replacedMaterial2) || current.isMaterial(LocalMaterials.SNOW_BLOCK))
					{
						if (flag)
						{
							world.setBlockDirect(x2, y2, z2, LocalMaterials.WATER);
						} else {
							world.setBlockDirect(x2, y2, z2, LocalMaterials.AIR);
							removeFloatingSnowLayer(world, x2, y2, z2);
						}
					}
				}
			}
		}
	}

	private void removeFloatingSnowLayer(IWorldGenRegion world, int x, int y, int z)
	{
		LocalMaterialData materialAbove = world.getMaterialDirect(x, y + 1, z);	
		if (materialAbove.isMaterial(LocalMaterials.SNOW))
		{
			world.setBlockDirect(x, y + 1, z, LocalMaterials.AIR);
		}
	}

	private void generateIcebergBlock(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z, int irandom, int x1, int y1, int z1, int irandom1, int irandom2, boolean flag, int irandom3, double drandom, boolean flag1, LocalMaterialData material, LocalMaterialData material2)
	{
		double signedDistance = flag
			? signedDistanceEllipse(x1, z1, 0, 0, 0, irandom2, getEllipseC(y1, irandom, irandom3), drandom)
			: signedDistanceCircle(x1, z1, 0, 0, 0, irandom1, random);

		if (signedDistance < 0.0D)
		{
			int x2 = x + x1;
			int y2 = y + y1;
			int z2 = z + z1;
			double lvt_19_1_ = flag ? -0.5D : (double)(-6 - random.nextInt(3));
			if (signedDistance > lvt_19_1_ && random.nextDouble() > 0.9D)
			{
				return;
			}
			setIcebergBlock(x2, y2, z2, worldGenRegion, random, irandom - y1, irandom, flag, flag1, material, material2);
		}
	}

	private void setIcebergBlock(int x, int y, int z, IWorldGenRegion world, Random random, int irandomy, int irandom, boolean flag, boolean flag1, LocalMaterialData material, LocalMaterialData material2)
	{
		LocalMaterialData current = world.getMaterialDirect(x, y, z);
		if (current.isAir() || current.isMaterial(LocalMaterials.SNOW_BLOCK) || current.isMaterial(LocalMaterials.ICE) || current.isMaterial(LocalMaterials.WATER))
		{
			boolean flag2 = !flag || random.nextDouble() > 0.05D;
			int irandom1 = flag ? 3 : 2;
			if (flag1 && !current.isMaterial(LocalMaterials.WATER) && (double)irandomy <= (double)random.nextInt(Math.max(1, irandom / irandom1)) + (double)irandom * 0.6D && flag2)
			{
				world.setBlockDirect(x, y, z, material2);
			} else {
				world.setBlockDirect(x, y, z, material);
			}
		}
	}

	private int getEllipseC(int y1, int irandom1, int irandom2)
	{
		int ellipsec = irandom2;
		if (y1 > 0 && irandom1 - y1 <= 3)
		{
			ellipsec -= (4 - (irandom1 - y1));
		}
		return ellipsec;
	}

	private double signedDistanceCircle(int x1, int z1, int x, int y, int z, int irandom, Random random)
	{
		float frandom = 10.0F * MathHelper.clamp(random.nextFloat(), 0.2F, 0.8F) / (float)irandom;
		return (double)frandom + Math.pow((double)(x1 - x), 2.0D) + Math.pow((double)(z1 - z), 2.0D) - Math.pow((double)irandom, 2.0D);
	}

	private double signedDistanceEllipse(int x1, int z1, int x, int y, int z, int irandom, int iellipsec, double drandom)
	{
		return Math.pow(((double)(x1 - x) * Math.cos(drandom) - (double)(z1 - z) * Math.sin(drandom)) / (double)irandom, 2.0D) + Math.pow(((double)(x1 - x) * Math.sin(drandom) + (double)(z1 - z) * Math.cos(drandom)) / (double)iellipsec, 2.0D) - 1.0D;
	}

	private int heightDependentRadiusRound(Random random, int irandom1, int irandom2, int irandom3)
	{
		float frandom1 = 3.5F - random.nextFloat();
		float frandom2 = (1.0F - (float) Math.pow((double)irandom1, 2.0D) / ((float)irandom2 * frandom1)) * (float)irandom3;
		if (irandom2 > 15 + random.nextInt(5))
		{
			int irandom4 = irandom1 < 3 + random.nextInt(6) ? irandom1 / 2 : irandom1;
			frandom2 = (1.0F - (float)irandom4 / ((float)irandom2 * frandom1 * 0.4F)) * (float)irandom3;
		}
		return MathHelper.ceil(frandom2 / 2.0F);
	}

	private int heightDependentRadiusEllipse(int irandomy, int irandom1, int irandom2)
	{
		float frandom = (1.0F - (float)Math.pow((double)irandomy, 2.0D) / ((float)irandom1 * 1.0F)) * (float)irandom2;
		return MathHelper.ceil(frandom / 2.0F);
	}

	private int heightDependentRadiusSteep(Random random, int random1, int random2, int random3)
	{
		float frandom = 1.0F + random.nextFloat() / 2.0F;
		float frandom1 = (1.0F - (float)random1 / ((float)random2 * frandom)) * (float)random3;
		return MathHelper.ceil(frandom1 / 2.0F);
	}

	private boolean isIcebergBlock(LocalMaterialData target, LocalMaterialData material, LocalMaterialData material2)
	{
		return target.isMaterial(material) || target.isMaterial(material2);
	}

	private boolean belowIsAir(IWorldGenRegion world, int x, int y, int z)
	{
		return world.getMaterialDirect(x, y - 1, z).isAir();
	}

	private void smooth(IWorldGenRegion world, int x, int y, int z, int irandom1, int ismoothheight, boolean flag, int irandom3, LocalMaterialData material, LocalMaterialData material2)
	{
		int iradius = flag ? irandom3 : irandom1 / 2;

		int x2;
		int y2;
		int z2;
		IBiomeConfig biomeConfig;
		LocalMaterialData replacedMaterial;
		LocalMaterialData replacedMaterial2;
		LocalMaterialData current;
		int iheight;
		for (int x1 = -iradius; x1 <= iradius; x1++)
		{
			for (int z1 = -iradius; z1 <= iradius; z1++)
			{
				for (int y1 = 0; y1 <= ismoothheight; y1++)
				{
					x2 = x + x1;
					y2 = y + y1;
					z2 = z + z1;
					// TODO: Request all up front instead of using cacheChunk true?
					biomeConfig = world.getCachedBiomeProvider().getBiomeConfig(x2, z2, true);
					replacedMaterial = material;
					replacedMaterial2 = material2;
					if(biomeConfig.getReplaceBlocks() != null)
					{
						replacedMaterial = biomeConfig.getReplaceBlocks().replaceBlock(y2, material);
						replacedMaterial2 = biomeConfig.getReplaceBlocks().replaceBlock(y2, material2);
					}
					current = world.getMaterialDirect(x2, y2, z2);
					if (isIcebergBlock(current, replacedMaterial, replacedMaterial2) || current.isMaterial(LocalMaterials.SNOW))
					{
						if (belowIsAir(world, x2, y2, z2))
						{
							world.setBlockDirect(x2, y2, z2, LocalMaterials.AIR);
							world.setBlockDirect(x2, y2 + 1, z2, LocalMaterials.AIR);	
						}
						else if (isIcebergBlock(current, replacedMaterial, replacedMaterial2))
						{
							LocalMaterialData[] materials =
							{
								world.getMaterialDirect(x2 - 1, y2, z2),
								world.getMaterialDirect(x2 + 1, y2, z2),
								world.getMaterialDirect(x2, y2, z2 - 1),
								world.getMaterialDirect(x2, y2, z2 + 1)
							};
							IBiomeConfig[] biomeConfigs =
							{
								world.getCachedBiomeProvider().getBiomeConfig(x2 - 1, z2, true),
								world.getCachedBiomeProvider().getBiomeConfig(x2 + 1, z2, true),
								world.getCachedBiomeProvider().getBiomeConfig(x2, z2 - 1, true),
								world.getCachedBiomeProvider().getBiomeConfig(x2, z2 + 1, true)
							};
							iheight = 0;
							int i = 0;
							for (LocalMaterialData mat : materials)
							{
								if(biomeConfigs[i].getReplaceBlocks() != null)
								{
									replacedMaterial = biomeConfigs[i].getReplaceBlocks().replaceBlock(y2, material);
									replacedMaterial2 = biomeConfigs[i].getReplaceBlocks().replaceBlock(y2, material2);
								}
								if (!isIcebergBlock(mat, replacedMaterial, replacedMaterial2))
								{
									iheight++;
								}
								i++;
							}
							if (iheight >= 3)
							{
								world.setBlockDirect(x2, y2, z2, LocalMaterials.AIR);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("Iceberg(");
		for (int i = 0; i < this.materials.length; i++) {
			builder.append(this.materials[i] + ", ");
			builder.append(this.materials2[i] + ", ");
			builder.append(this.rarities[i] + ", ");
		}
		builder.append(this.totalRarity + ")");
		
		return builder.toString();
	}
}
