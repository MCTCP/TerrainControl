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
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

public class IcebergResource extends BiomeResourceBase implements IBasicResource
{
	public IcebergResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion world, Random random, ILogger logger, IMaterialReader materialReader)
	{
		int x = world.getDecorationArea().getChunkBeingDecorated().getBlockX();
		int z = world.getDecorationArea().getChunkBeingDecorated().getBlockZ();
		int y = world.getBiomeConfigForDecoration(x, z).getWaterLevelMax();
		boolean lvt_6_1_ = (random.nextDouble() > 0.7D);
		LocalMaterialData pakcedIce = LocalMaterials.PACKED_ICE;

		double lvt_8_1_ = random.nextDouble() * 2.0D * Math.PI;
		int lvt_10_1_ = 11 - random.nextInt(5);
		int lvt_11_1_ = 3 + random.nextInt(3);
		boolean lvt_12_1_ = (random.nextDouble() > 0.7D);

		int lvt_14_1_ = lvt_12_1_ ? (random.nextInt(6) + 6) : (random.nextInt(15) + 3);
		if (!lvt_12_1_ && random.nextDouble() > 0.9D)
		{
			lvt_14_1_ += random.nextInt(19) + 7;
		}

		int lvt_15_1_ = Math.min(lvt_14_1_ + random.nextInt(11), 18);
		int lvt_16_1_ = Math.min(lvt_14_1_ + random.nextInt(7) - random.nextInt(5), 11);
		int lvt_17_1_ = lvt_12_1_ ? lvt_10_1_ : 11;

		for (int lvt_18_1_ = -lvt_17_1_; lvt_18_1_ < lvt_17_1_; lvt_18_1_++)
		{
			for (int lvt_19_1_ = -lvt_17_1_; lvt_19_1_ < lvt_17_1_; lvt_19_1_++)
			{
				for (int lvt_20_1_ = 0; lvt_20_1_ < lvt_14_1_; lvt_20_1_++)
				{
					int lvt_21_1_ = lvt_12_1_ ? heightDependentRadiusEllipse(lvt_20_1_, lvt_14_1_, lvt_16_1_)
							: heightDependentRadiusRound(random, lvt_20_1_, lvt_14_1_, lvt_16_1_);
					if (lvt_12_1_ || lvt_18_1_ < lvt_21_1_)
					{
						generateIcebergBlock(world, random, x, y, z, lvt_14_1_, lvt_18_1_, lvt_20_1_, lvt_19_1_,
								lvt_21_1_, lvt_17_1_, lvt_12_1_, lvt_11_1_, lvt_8_1_, lvt_6_1_, pakcedIce);
					}
				}
			}
		}
		smooth(world, x, y, z, lvt_16_1_, lvt_14_1_, lvt_12_1_, lvt_10_1_);

		for (int lvt_18_2_ = -lvt_17_1_; lvt_18_2_ < lvt_17_1_; lvt_18_2_++)
		{
			for (int lvt_19_2_ = -lvt_17_1_; lvt_19_2_ < lvt_17_1_; lvt_19_2_++)
			{
				for (int lvt_20_2_ = -1; lvt_20_2_ > -lvt_15_1_; lvt_20_2_--)
				{
					int lvt_21_2_ = lvt_12_1_
							? MathHelper.ceil(lvt_17_1_ * (1.0F - (float) Math.pow(lvt_20_2_, 2.0D) / lvt_15_1_ * 8.0F))
							: lvt_17_1_;
					int lvt_22_1_ = heightDependentRadiusSteep(random, -lvt_20_2_, lvt_15_1_, lvt_16_1_);
					if (lvt_18_2_ < lvt_22_1_)
					{
						generateIcebergBlock(world, random, x, y, z, lvt_15_1_, lvt_18_2_, lvt_20_2_, lvt_19_2_,
								lvt_22_1_, lvt_21_2_, lvt_12_1_, lvt_11_1_, lvt_8_1_, lvt_6_1_, pakcedIce);
					}
				}
			}
		}
		boolean lvt_18_3_ = lvt_12_1_ ? ((random.nextDouble() > 0.1D)) : ((random.nextDouble() > 0.7D));
		if (lvt_18_3_)
		{
			generateCutOut(random, world, lvt_16_1_, lvt_14_1_, x, y, z, lvt_12_1_, lvt_10_1_, lvt_8_1_, lvt_11_1_);
		}
	}

	private void generateCutOut(Random random, IWorldGenRegion world, int p_205184_3_, int p_205184_4_,
			int x, int y, int z, boolean p_205184_6_, int p_205184_7_, double p_205184_8_, int p_205184_10_)
	{
		int lvt_11_1_ = random.nextBoolean() ? -1 : 1;
		int lvt_12_1_ = random.nextBoolean() ? -1 : 1;

		int lvt_13_1_ = random.nextInt(Math.max(p_205184_3_ / 2 - 2, 1));
		if (random.nextBoolean())
		{
			lvt_13_1_ = p_205184_3_ / 2 + 1 - random.nextInt(Math.max(p_205184_3_ - p_205184_3_ / 2 - 1, 1));
		}

		int lvt_14_1_ = random.nextInt(Math.max(p_205184_3_ / 2 - 2, 1));
		if (random.nextBoolean())
		{
			lvt_14_1_ = p_205184_3_ / 2 + 1 - random.nextInt(Math.max(p_205184_3_ - p_205184_3_ / 2 - 1, 1));
		}

		if (p_205184_6_)
		{
			lvt_13_1_ = lvt_14_1_ = random.nextInt(Math.max(p_205184_7_ - 5, 1));
		}

		int x2 = lvt_11_1_ * lvt_13_1_;
		int z2 = lvt_12_1_ * lvt_14_1_;
		double lvt_16_1_ = p_205184_6_ ? (p_205184_8_ + 1.5707963267948966D) : (random.nextDouble() * 2.0D * Math.PI);

		for (int lvt_18_1_ = 0; lvt_18_1_ < p_205184_4_ - 3; ++lvt_18_1_)
		{
			int lvt_19_1_ = heightDependentRadiusRound(random, lvt_18_1_, p_205184_4_, p_205184_3_);
			carve(lvt_19_1_, lvt_18_1_, x, y, z, world, false, lvt_16_1_, x2, z2, p_205184_7_, p_205184_10_);

		}
		for (int lvt_18_2_ = -1; lvt_18_2_ > -p_205184_4_ + random.nextInt(5); --lvt_18_2_)
		{
			int lvt_19_2_ = heightDependentRadiusSteep(random, -lvt_18_2_, p_205184_4_, p_205184_3_);
			carve(lvt_19_2_, lvt_18_2_, x, y, z, world, true, lvt_16_1_, x2, z2, p_205184_7_, p_205184_10_);
		}
	}

	private void carve(int p_205174_1_, int p_205174_2_, int x, int y, int z, IWorldGenRegion world, boolean p_205174_5_, double p_205174_6_, int x3, int z3, int p_205174_9_, int p_205174_10_)
	{
		int lvt_11_1_ = p_205174_1_ + 1 + p_205174_9_ / 3;
		int lvt_12_1_ = Math.min(p_205174_1_ - 3, 3) + p_205174_10_ / 2 - 1;
		int x2;
		int y2;
		int z2;
		for (int lvt_13_1_ = -lvt_11_1_; lvt_13_1_ < lvt_11_1_; lvt_13_1_++)
		{
			for (int lvt_14_1_ = -lvt_11_1_; lvt_14_1_ < lvt_11_1_; lvt_14_1_++)
			{
				double lvt_15_1_ = signedDistanceEllipse(lvt_13_1_, lvt_14_1_, x3, z3, lvt_11_1_, lvt_12_1_, p_205174_6_);
				if (lvt_15_1_ < 0.0D)
				{
					x2 = x + lvt_13_1_;
					y2 = y + p_205174_2_;
					z2 = z + lvt_14_1_;
					LocalMaterialData material = world.getMaterialDirect(x2, y2, z2);
					if (isIcebergBlock(material) || material.isMaterial(LocalMaterials.SNOW_BLOCK))
					{
						if (p_205174_5_)
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

	private void generateIcebergBlock(
		IWorldGenRegion p_205181_1_, Random p_205181_2_, int x, int y, int z, int p_205181_4_, 
		int p_205181_5_, int p_205181_6_, int p_205181_7_, int p_205181_8_, int p_205181_9_, 
		boolean p_205181_10_, int p_205181_11_, double p_205181_12_, boolean p_205181_14_, 
		LocalMaterialData material
	)
	{
		double lvt_16_1_ = p_205181_10_
			? signedDistanceEllipse(p_205181_5_, p_205181_7_, 0, 0, p_205181_9_, getEllipseC(p_205181_6_, p_205181_4_, p_205181_11_), p_205181_12_)
			: signedDistanceCircle(p_205181_5_, p_205181_7_, 0, 0, p_205181_8_, p_205181_2_);

		if (lvt_16_1_ < 0.0D)
		{
			int x2 = x + p_205181_5_;
			int y2 = y + p_205181_6_;
			int z2 = z + p_205181_7_;
			double lvt_19_1_ = p_205181_10_ ? -0.5D : (-6 - p_205181_2_.nextInt(3));
			if (lvt_16_1_ > lvt_19_1_ && p_205181_2_.nextDouble() > 0.9D)
			{
				return;
			}
			setIcebergBlock(x2, y2, z2, p_205181_1_, p_205181_2_, p_205181_4_ - p_205181_6_, p_205181_4_, p_205181_10_, p_205181_14_, material);
		}
	}

	private void setIcebergBlock(
		int x, int y, int z, IWorldGenRegion world, Random p_205175_3_, int p_205175_4_,
		int p_205175_5_, boolean p_205175_6_, boolean p_205175_7_, LocalMaterialData material
	)
	{
		LocalMaterialData current = world.getMaterialDirect(x, y, z);
		if (current.isAir() || current.isMaterial(LocalMaterials.SNOW_BLOCK) || current.isMaterial(LocalMaterials.ICE) || current.isMaterial(LocalMaterials.WATER))
		{
			boolean lvt_10_1_ = (!p_205175_6_ || p_205175_3_.nextDouble() > 0.05D);
			int lvt_11_1_ = p_205175_6_ ? 3 : 2;
			if (
				p_205175_7_ && (current == null || !current.isMaterial(LocalMaterials.WATER))
				&& p_205175_4_ <= p_205175_3_.nextInt(Math.max(1, p_205175_5_ / lvt_11_1_)) + p_205175_5_ * 0.6D
				&& lvt_10_1_
			)
			{
				world.setBlockDirect(x, y, z, LocalMaterials.SNOW_BLOCK);
			} else {
				world.setBlockDirect(x, y, z, material);
			}
		}
	}

	private int getEllipseC(int p_205176_1_, int p_205176_2_, int p_205176_3_)
	{
		int lvt_4_1_ = p_205176_3_;
		if (p_205176_1_ > 0 && p_205176_2_ - p_205176_1_ <= 3)
		{
			lvt_4_1_ -= 4 - p_205176_2_ - p_205176_1_;
		}

		return lvt_4_1_;
	}

	private double signedDistanceCircle(int p_205177_1_, int p_205177_2_, int x, int z, int p_205177_4_, Random random)
	{
		float lvt_6_1_ = (float) (10.0F * MathHelper.clamp(random.nextFloat(), 0.2F, 0.8F) / p_205177_4_);
		return lvt_6_1_ + Math.pow((p_205177_1_ - x), 2.0D) + Math.pow((p_205177_2_ - z), 2.0D) - Math.pow(p_205177_4_, 2.0D);
	}

	private double signedDistanceEllipse(int p_205180_1_, int p_205180_2_, int x, int z, int p_205180_4_, int p_205180_5_, double p_205180_6_)
	{
		return Math
			.pow(((p_205180_1_ - x) * Math.cos(p_205180_6_) - (p_205180_2_ - z) * Math.sin(p_205180_6_))
					/ p_205180_4_, 2.0D)
			+ Math.pow(((p_205180_1_ - x) * Math.sin(p_205180_6_) + (p_205180_2_ - z) * Math.cos(p_205180_6_))
					/ p_205180_5_, 2.0D)
			- 1.0D;
	}

	private int heightDependentRadiusRound(Random p_205183_1_, int p_205183_2_, int p_205183_3_, int p_205183_4_)
	{
		float lvt_5_1_ = 3.5F - p_205183_1_.nextFloat();
		float lvt_6_1_ = (1.0F - (float) Math.pow(p_205183_2_, 2.0D) / p_205183_3_ * lvt_5_1_) * p_205183_4_;

		if (p_205183_3_ > 15 + p_205183_1_.nextInt(5))
		{
			int lvt_7_1_ = (p_205183_2_ < 3 + p_205183_1_.nextInt(6)) ? (p_205183_2_ / 2) : p_205183_2_;
			lvt_6_1_ = (1.0F - lvt_7_1_ / p_205183_3_ * lvt_5_1_ * 0.4F) * p_205183_4_;

		}
		return MathHelper.ceil(lvt_6_1_ / 2.0F);
	}

	private int heightDependentRadiusEllipse(int p_205178_1_, int p_205178_2_, int p_205178_3_)
	{
		float lvt_4_1_ = 1.0F;
		float lvt_5_1_ = (1.0F - (float) Math.pow(p_205178_1_, 2.0D) / p_205178_2_ * 1.0F) * p_205178_3_;
		return MathHelper.ceil(lvt_5_1_ / 2.0F);
	}

	private int heightDependentRadiusSteep(Random p_205187_1_, int p_205187_2_, int p_205187_3_, int p_205187_4_)
	{
		float lvt_5_1_ = 1.0F + p_205187_1_.nextFloat() / 2.0F;
		float lvt_6_1_ = (1.0F - p_205187_2_ / p_205187_3_ * lvt_5_1_) * p_205187_4_;
		return MathHelper.ceil(lvt_6_1_ / 2.0F);
	}

	private boolean isIcebergBlock(LocalMaterialData material)
	{
		return (material.isMaterial(LocalMaterials.PACKED_ICE) || material.isMaterial(LocalMaterials.SNOW_BLOCK) || material.isMaterial(LocalMaterials.BLUE_ICE));
	}

	private boolean belowIsAir(IWorldGenRegion world, int x, int y, int z)
	{
		return world.getMaterialDirect(x, y - 1, z).isAir();
	}

	private void smooth(IWorldGenRegion world, int x, int y, int z, int p_205186_3_, int p_205186_4_, boolean p_205186_5_, int p_205186_6_)
	{
		int lvt_7_1_ = p_205186_5_ ? p_205186_6_ : (p_205186_3_ / 2);

		int x2;
		int y2;
		int z2;
		for (int lvt_8_1_ = -lvt_7_1_; lvt_8_1_ <= lvt_7_1_; lvt_8_1_++)
		{
			for (int lvt_9_1_ = -lvt_7_1_; lvt_9_1_ <= lvt_7_1_; lvt_9_1_++)
			{
				for (int lvt_10_1_ = 0; lvt_10_1_ <= p_205186_4_; lvt_10_1_++)
				{
					x2 = x + lvt_8_1_;
					y2 = y + lvt_10_1_;
					z2 = z + lvt_9_1_;
					LocalMaterialData material = world.getMaterialDirect(x2, y2, z2);
					if (isIcebergBlock(material) || material.isMaterial(LocalMaterials.SNOW))
					{
						if (belowIsAir(world, x2, y2, z2))
						{
							world.setBlockDirect(x2, y2, z2, LocalMaterials.AIR);
							world.setBlockDirect(x2, y2 + 1, z2, LocalMaterials.AIR);	
						}
						else if (isIcebergBlock(material))
						{	
							LocalMaterialData[] lvt_13_1_ =
							{
								world.getMaterialDirect(x2 + 1, y2, z2), world.getMaterialDirect(x2 - 1, y2, z2),
								world.getMaterialDirect(x2, y2, z2 + 1), world.getMaterialDirect(x2, y2, z2 - 1) 
							};
							int lvt_14_1_ = 0;
							for (LocalMaterialData mat : lvt_13_1_)
							{
								if (!isIcebergBlock(mat))
								{
									lvt_14_1_++;
								}
							}
							if (lvt_14_1_ >= 3)
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
		return "Iceberg(" + ")";
	}
}
