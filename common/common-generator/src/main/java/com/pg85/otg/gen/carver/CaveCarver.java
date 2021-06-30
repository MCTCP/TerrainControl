package com.pg85.otg.gen.carver;

import java.util.BitSet;
import java.util.Random;

import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;

public class CaveCarver extends Carver
{
	public CaveCarver(int heightLimit, IWorldConfig worldConfig)
	{
		super(heightLimit, worldConfig);
	}

	public boolean carve(ChunkBuffer chunk, Random random, int seaLevel, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet carvingMask)
	{
		int branchFactor = (this.getBranchFactor() * 2 - 1) * 16;
		int caveCount = random.nextInt(random.nextInt(random.nextInt(this.getMaxCaveCount()) + 1) + 1);

		if (this.worldConfig.isEvenCaveDistribution()) {
			caveCount = this.worldConfig.getCaveFrequency();
		}

		for (int cave = 0; cave < caveCount; ++cave)
		{
			double x = chunkX * 16 + random.nextInt(16);
			double y = this.getCaveY(random);
			double z = chunkZ * 16 + random.nextInt(16);
			// Vanilla Behavior: Default to 1.
//			int tunnelCount = 1;
			int tunnelCount = this.worldConfig.getCaveSystemFrequency();

			float size;
			if (random.nextInt(100) <= this.worldConfig.getIndividualCaveRarity())
			{
				size = 1.0F + random.nextFloat() * 6.0F;
				this.carveCave(chunk, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, size, 0.5D, carvingMask);
				// Vanilla Behavior: Add 0 to 3 more caves when generating a spherical cave.
//				tunnelCount += random.nextInt(4);
				tunnelCount += RandomHelper.numberInRange(random, this.worldConfig.getCaveSystemPocketMinSize(), this.worldConfig.getCaveSystemPocketMaxSize());
			}
			else if (random.nextInt(100) <= this.worldConfig.getCaveSystemPocketChance() - 1)
			{
				tunnelCount += RandomHelper.numberInRange(random, this.worldConfig.getCaveSystemPocketMinSize(), this.worldConfig.getCaveSystemPocketMaxSize());
			}

			for (int r = 0; r < tunnelCount; ++r)
			{
				float yaw = random.nextFloat() * 6.2831855F;
				size = (random.nextFloat() - 0.5F) / 4.0F;
				float width = this.getTunnelSystemWidth(random);
				int branchCount = branchFactor - random.nextInt(branchFactor / 4);
				this.carveTunnels(chunk, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, width, yaw, size, 0, branchCount, this.getTunnelSystemHeightWidthRatio(), carvingMask);
			}
		}

		return true;
	}

	public boolean shouldCarve(Random random, int chunkX, int chunkZ)
	{
		if (
			!this.worldConfig.getCavesEnabled() ||
			this.worldConfig.getCaveFrequency() <= 0
		)
		{
			return false;
		}

		// TODO: This should be changed to 1 / rarity
		return random.nextInt(100) < this.worldConfig.getCaveRarity();
	}

	protected int getMaxCaveCount()
	{
		// Vanilla Behavior: Defaults to 15.
//		return 15;
		return this.worldConfig.getCaveFrequency();
	}

	protected float getTunnelSystemWidth(Random random)
	{
		float width = random.nextFloat() * 2.0F + random.nextFloat();
		if (random.nextInt(10) == 0)
		{
			width *= random.nextFloat() * random.nextFloat() * 3.0F + 1.0F;
		}

		return width;
	}

	protected double getTunnelSystemHeightWidthRatio()
	{
		return 1.0D;
	}

	protected int getCaveY(Random random)
	{
		// Vanilla Behavior: Random value from 8 to 120, biased downwards.
//		return random.nextInt(random.nextInt(120) + 8);
		if (this.worldConfig.isEvenCaveDistribution())
		{
			return RandomHelper.numberInRange(random, this.worldConfig.getCaveMinAltitude(), this.worldConfig.getCaveMaxAltitude());
		} else {
			return random.nextInt(random.nextInt(this.worldConfig.getCaveMaxAltitude() - this.worldConfig.getCaveMinAltitude() + 1) + 1) + this.worldConfig.getCaveMinAltitude();
		}
	}

	protected void carveCave(ChunkBuffer chunk, long seed, int seaLevel, int mainChunkX, int mainChunkZ, double x, double y, double z, float yaw, double yawPitchRatio, BitSet carvingMask)
	{
		double scaledYaw = 1.5D + (double) (MathHelper.sin(1.5707964F) * yaw);
		double scaledPitch = scaledYaw * yawPitchRatio;
		this.carveRegion(chunk, seed, seaLevel, mainChunkX, mainChunkZ, x + 1.0D, y, z, scaledYaw, scaledPitch, carvingMask);
	}

	protected void carveTunnels(ChunkBuffer chunk, long seed, int seaLevel, int mainChunkX, int mainChunkZ, double x, double y, double z, float width, float yaw, float pitch, int branchStartIndex, int branchCount, double yawPitchRatio, BitSet carvingMask)
	{
		Random random = new Random(seed);
		int nextBranchIndex = random.nextInt(branchCount / 2) + branchCount / 4;
		boolean isBigger = random.nextInt(6) == 0;
		float yawChange = 0.0F;
		float pitchChange = 0.0F;

		for (int branchIndex = branchStartIndex; branchIndex < branchCount; ++branchIndex)
		{
			double currentYaw = 1.5D + (double) (MathHelper.sin(3.1415927F * (float) branchIndex / (float) branchCount) * width);
			double currentPitch = currentYaw * yawPitchRatio;
			float delta = MathHelper.cos(pitch);
			x += MathHelper.cos(yaw) * delta;
			y += MathHelper.sin(pitch);
			z += MathHelper.sin(yaw) * delta;
			pitch *= isBigger ? 0.92F : 0.7F;
			pitch += pitchChange * 0.1F;
			yaw += yawChange * 0.1F;
			pitchChange *= 0.9F;
			yawChange *= 0.75F;
			pitchChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			yawChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
			if (branchIndex == nextBranchIndex && width > 1.0F)
			{
				this.carveTunnels(chunk, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, random.nextFloat() * 0.5F + 0.5F, yaw - 1.5707964F, pitch / 3.0F, branchIndex, branchCount, 1.0D, carvingMask);
				this.carveTunnels(chunk, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, random.nextFloat() * 0.5F + 0.5F, yaw + 1.5707964F, pitch / 3.0F, branchIndex, branchCount, 1.0D, carvingMask);
				return;
			}

			if (random.nextInt(4) != 0)
			{
				if (!this.canCarveBranch(mainChunkX, mainChunkZ, x, z, branchIndex, branchCount, width))
				{
					return;
				}

				this.carveRegion(chunk, seed, seaLevel, mainChunkX, mainChunkZ, x, y, z, currentYaw, currentPitch, carvingMask);
			}
		}

	}

	protected boolean isPositionExcluded(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y)
	{
		return scaledRelativeY <= -0.7D || scaledRelativeX * scaledRelativeX + scaledRelativeY * scaledRelativeY + scaledRelativeZ * scaledRelativeZ >= 1.0D;
	}
}
