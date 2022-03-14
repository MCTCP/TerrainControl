package com.pg85.otg.gen.carver;

import java.util.BitSet;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.ISurfaceGeneratorNoiseProvider;
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

	@Override
	public boolean carve(ISurfaceGeneratorNoiseProvider noiseProvider, ChunkBuffer chunk, Random random, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet carvingMask, ICachedBiomeProvider cachedBiomeProvider)
	{
		int branchFactor = (this.getBranchFactor() * 2 - 1) * Constants.CHUNK_SIZE;
		int caveCount = random.nextInt(random.nextInt(random.nextInt(this.getMaxCaveCount()) + 1) + 1);

		if (this.worldConfig.isEvenCaveDistribution())
		{
			caveCount = this.worldConfig.getCaveFrequency();
		}

		for (int cave = 0; cave < caveCount; ++cave)
		{
			double x = chunkX * Constants.CHUNK_SIZE + random.nextInt(Constants.CHUNK_SIZE);
			double y = this.getCaveY(random);
			double z = chunkZ * Constants.CHUNK_SIZE + random.nextInt(Constants.CHUNK_SIZE);
			// Vanilla Behavior: Defaults to 1.
			int tunnelCount = this.worldConfig.getCaveSystemFrequency();

			if (random.nextInt(100) < this.worldConfig.getIndividualCaveRarity())
			{
				float size = 1.0F + random.nextFloat() * 6.0F;
				this.carveCave(noiseProvider, chunk, random.nextLong(), mainChunkX, mainChunkZ, x, y, z, size, 0.5D, carvingMask, cachedBiomeProvider);
				// Vanilla Behavior: Add 0 to 3 more caves when generating a spherical cave.
				// tunnelCount += random.nextInt(4);
				tunnelCount += RandomHelper.numberInRange(random, this.worldConfig.getCaveSystemPocketMinSize(), this.worldConfig.getCaveSystemPocketMaxSize());
			}
			else if (random.nextInt(100) <= this.worldConfig.getCaveSystemPocketChance() - 1)
			{
				tunnelCount += RandomHelper.numberInRange(random, this.worldConfig.getCaveSystemPocketMinSize(), this.worldConfig.getCaveSystemPocketMaxSize());
			}

			for (int r = 0; r < tunnelCount; ++r)
			{
				float yaw = random.nextFloat() * ((float)Math.PI * 2F);//* 6.2831855F;
				float size = (random.nextFloat() - 0.5F) / 4.0F;
				float width = this.getTunnelSystemWidth(random);
				int branchCount = branchFactor - random.nextInt(branchFactor / 4);
				this.carveTunnels(noiseProvider, chunk, random.nextLong(), mainChunkX, mainChunkZ, x, y, z, width, yaw, size, 0, branchCount, this.getTunnelSystemHeightWidthRatio(), carvingMask, cachedBiomeProvider);
			}
		}

		return true;
	}

	@Override
	public boolean isStartChunk(Random random, int chunkX, int chunkZ)
	{
		if (this.worldConfig.getCaveFrequency() <= 0)
		{
			return false;
		}

		// Vanilla uses 0.0-1.0, we use 0-100.
		return random.nextInt(100) < this.worldConfig.getCaveRarity();
	}

	protected int getMaxCaveCount()
	{
		// Vanilla Behavior: Defaults to 15.
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
		// return random.nextInt(random.nextInt(120) + 8);
		if (this.worldConfig.isEvenCaveDistribution())
		{
			return RandomHelper.numberInRange(random, this.worldConfig.getCaveMinAltitude(), this.worldConfig.getCaveMaxAltitude());
		} else {
			return random.nextInt(random.nextInt(this.worldConfig.getCaveMaxAltitude() - this.worldConfig.getCaveMinAltitude() + 1) + 1) + this.worldConfig.getCaveMinAltitude();
		}
	}

	protected void carveCave(ISurfaceGeneratorNoiseProvider noiseProvider, ChunkBuffer chunk, long seed, int mainChunkX, int mainChunkZ, double x, double y, double z, float yaw, double yawPitchRatio, BitSet carvingMask, ICachedBiomeProvider cachedBiomeProvider)
	{
		//double scaledYaw = 1.5D + (double)(MathHelper.sin(((float)Math.PI / 2F)) * yaw);
		double scaledYaw = 1.5D + (double) (MathHelper.sin(1.5707964F) * yaw);
		double scaledPitch = scaledYaw * yawPitchRatio;
		this.carveRegion(noiseProvider, null, chunk, seed, mainChunkX, mainChunkZ, x + 1.0D, y, z, scaledYaw, scaledPitch, carvingMask, cachedBiomeProvider);
	}

	protected void carveTunnels(ISurfaceGeneratorNoiseProvider noiseProvider, ChunkBuffer chunk, long seed, int mainChunkX, int mainChunkZ, double x, double y, double z, float width, float yaw, float pitch, int branchStartIndex, int branchCount, double yawPitchRatio, BitSet carvingMask, ICachedBiomeProvider cachedBiomeProvider)
	{
		Random random = new Random(seed);
		int nextBranchIndex = random.nextInt(branchCount / 2) + branchCount / 4;
		boolean isBigger = random.nextInt(6) == 0;
		float yawChange = 0.0F;
		float pitchChange = 0.0F;
		double currentYaw;
		double currentPitch;
		float delta;
		
		for (int branchIndex = branchStartIndex; branchIndex < branchCount; ++branchIndex)
		{
			currentYaw = 1.5D + (double) (MathHelper.sin(3.1415927F * (float) branchIndex / (float) branchCount) * width);
			//currentYaw = 1.5D + (double)(MathHelper.sin((float)Math.PI * (float)branchIndex / (float)branchCount) * width);
			currentPitch = currentYaw * yawPitchRatio;
			delta = MathHelper.cos(pitch);
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
				this.carveTunnels(noiseProvider, chunk, random.nextLong(), mainChunkX, mainChunkZ, x, y, z, random.nextFloat() * 0.5F + 0.5F, yaw - 1.5707964F, pitch / 3.0F, branchIndex, branchCount, 1.0D, carvingMask, cachedBiomeProvider);
				this.carveTunnels(noiseProvider, chunk, random.nextLong(), mainChunkX, mainChunkZ, x, y, z, random.nextFloat() * 0.5F + 0.5F, yaw + 1.5707964F, pitch / 3.0F, branchIndex, branchCount, 1.0D, carvingMask, cachedBiomeProvider);
				return;
			}

			if (random.nextInt(4) != 0)
			{
				if (!this.canCarveBranch(mainChunkX, mainChunkZ, x, z, branchIndex, branchCount, width))
				{
					return;
				}

				this.carveRegion(noiseProvider, null, chunk, seed, mainChunkX, mainChunkZ, x, y, z, currentYaw, currentPitch, carvingMask, cachedBiomeProvider);
			}
		}
	}

	@Override
	protected boolean isPositionExcluded(float[] cache, double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y)
	{
		return scaledRelativeY <= -0.7D || scaledRelativeX * scaledRelativeX + scaledRelativeY * scaledRelativeY + scaledRelativeZ * scaledRelativeZ >= 1.0D;
	}
}
