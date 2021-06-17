package com.pg85.otg.gen.carver;

import java.util.BitSet;
import java.util.Random;

import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IWorldConfig;

public class RavineCarver extends Carver
{
	private final float[] heightToHorizontalStretchFactor = new float[1024];

	public RavineCarver(int heightLimit, IWorldConfig worldConfig)
	{
		super(heightLimit, worldConfig);
	}
	public boolean shouldCarve(Random random, int chunkX, int chunkZ)
	{
		// TODO: This should be changed to 1 / rarity
		return random.nextInt(100) < this.worldConfig.getRavineRarity();
	}

	public boolean carve(ChunkBuffer chunk, Random random, int seaLevel, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet bitSet)
	{
//		int branchingFactor = (this.getBranchFactor() * 2 - 1) * 16;

		double x = chunkX * 16 + random.nextInt(16);
		// Vanilla behavior: Bias ravines downwards, with a min of 20.
//		double y = random.nextInt(random.nextInt(40) + 8) + 20;
		double y = RandomHelper.numberInRange(random, this.worldConfig.getRavineMinAltitude(), this.worldConfig.getRavineMaxAltitude());
		double z = chunkZ * 16 + random.nextInt(16);

		float yaw = random.nextFloat() * 6.2831855F;
		float pitch = (random.nextFloat() - 0.5F) * 2.0F / 8.0F;
		float width = (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;

		// Vanilla behavior: Subtract 0% - 25% of the branching factor. Default Branching factor is 112.
//		int branchCount = branchingFactor - random.nextInt(branchingFactor / 4);
		int branchCount = RandomHelper.numberInRange(random, this.worldConfig.getRavineMinLength(), this.worldConfig.getRavineMaxLength());
		double yawPitchRatio = worldConfig.getRavineDepth();

		this.carveRavine(chunk, random.nextLong(), seaLevel, mainChunkX, mainChunkZ, x, y, z, width, yaw, pitch, 0, branchCount, yawPitchRatio, bitSet);
		return true;
	}

	private void carveRavine(ChunkBuffer chunk, long seed, int seaLevel, int mainChunkX, int mainChunkZ, double x, double y, double z, float width, float yaw, float pitch, int branchStartIndex, int branchCount, double yawPitchRatio, BitSet carvingMask)
	{
		Random random = new Random(seed);
		float stretchFactor = 1.0F;

		for (int y1 = 0; y1 < 256; ++y1)
		{
			if (y1 == 0 || random.nextInt(3) == 0)
			{
				stretchFactor = 1.0F + random.nextFloat() * random.nextFloat();
			}

			// TODO: Pretty sure this isn't thread safe
			this.heightToHorizontalStretchFactor[y1] = stretchFactor * stretchFactor;
		}

		float yawChange = 0.0F;
		float pitchChange = 0.0F;

		for (int branchIndex = branchStartIndex; branchIndex < branchCount; ++branchIndex)
		{
			double currentYaw = 1.5D + (double) (MathHelper.sin((float) branchIndex * 3.1415927F / (float) branchCount) * width);
			double currentPitch = currentYaw * yawPitchRatio;
			currentYaw *= (double) random.nextFloat() * 0.25D + 0.75D;
			currentPitch *= (double) random.nextFloat() * 0.25D + 0.75D;
			float deltaXZ = MathHelper.cos(pitch);
			float deltaY = MathHelper.sin(pitch);
			x += MathHelper.cos(yaw) * deltaXZ;
			y += deltaY;
			z += MathHelper.sin(yaw) * deltaXZ;
			pitch *= 0.7F;
			pitch += pitchChange * 0.05F;
			yaw += yawChange * 0.05F;
			pitchChange *= 0.8F;
			yawChange *= 0.5F;
			pitchChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			yawChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
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
		return (scaledRelativeX * scaledRelativeX + scaledRelativeZ * scaledRelativeZ) * (double) this.heightToHorizontalStretchFactor[y - 1] + scaledRelativeY * scaledRelativeY / 6.0D >= 1.0D;
	}
}
