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

public class RavineCarver extends Carver
{
	public RavineCarver(int heightLimit, IWorldConfig worldConfig)
	{
		super(heightLimit, worldConfig);
	}

	@Override
	public boolean isStartChunk(Random random, int chunkX, int chunkZ)
	{
		return
			this.worldConfig.getRavinesEnabled() &&
			// Vanilla uses 0.0-1.0, we use 0-100.
			(random.nextInt(100) < this.worldConfig.getRavineRarity());
	}

	@Override
	public boolean carve(ISurfaceGeneratorNoiseProvider noiseProvider, ChunkBuffer chunk, Random random, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet bitSet, ICachedBiomeProvider cachedBiomeProvider)
	{
		double x = chunkX * Constants.CHUNK_SIZE + random.nextInt(Constants.CHUNK_SIZE);
		double z = chunkZ * Constants.CHUNK_SIZE + random.nextInt(Constants.CHUNK_SIZE);			

		// Vanilla behavior: Bias ravines downwards, with a min of 20.
		// double y = random.nextInt(random.nextInt(40) + 8) + 20;
		double y = RandomHelper.numberInRange(random, this.worldConfig.getRavineMinAltitude(), this.worldConfig.getRavineMaxAltitude());

		//float yaw = random.nextFloat() * 6.2831855F;
		float yaw = random.nextFloat() * ((float)Math.PI * 2F);
		float pitch = (random.nextFloat() - 0.5F) * 2.0F / 8.0F;
		float width = (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;

		// Vanilla behavior: Subtract 0% - 25% of the branching factor. Default Branching factor is 112.
		// int branchingFactor = (this.getBranchFactor() * 2 - 1) * 16;		
		// int branchCount = branchingFactor - random.nextInt(branchingFactor / 4);
		int branchCount = RandomHelper.numberInRange(random, this.worldConfig.getRavineMinLength(), this.worldConfig.getRavineMaxLength());
		branchCount = branchCount - random.nextInt(branchCount / 4);		
		double yawPitchRatio = worldConfig.getRavineDepth();

		this.carveRavine(noiseProvider, chunk, random.nextLong(), mainChunkX, mainChunkZ, x, y, z, width, yaw, pitch, 0, branchCount, yawPitchRatio, bitSet, cachedBiomeProvider);
		return true;
	}

	private void carveRavine(ISurfaceGeneratorNoiseProvider noiseProvider, ChunkBuffer chunk, long seed, int mainChunkX, int mainChunkZ, double x, double y, double z, float width, float yaw, float pitch, int branchStartIndex, int branchCount, double yawPitchRatio, BitSet carvingMask, ICachedBiomeProvider cachedBiomeProvider)
	{
		Random random = new Random(seed);
		float stretchFactor = 1.0F;

		float[] heightToHorizontalStretchFactor = new float[1024];
		for (int y1 = 0; y1 < Constants.WORLD_HEIGHT; ++y1)
		{
			if (y1 == 0 || random.nextInt(3) == 0)
			{
				stretchFactor = 1.0F + random.nextFloat() * random.nextFloat();
			}
			heightToHorizontalStretchFactor[y1] = stretchFactor * stretchFactor;
		}

		float yawChange = 0.0F;
		float pitchChange = 0.0F;
		double currentYaw;
		double currentPitch;
		float deltaXZ;
		float deltaY;
		for (int branchIndex = branchStartIndex; branchIndex < branchCount; ++branchIndex)
		{		
			//currentYaw = 1.5D + (double)(MathHelper.sin((float)branchIndex * (float)Math.PI / (float)branchCount) * width);
			currentYaw = 1.5D + (double) (MathHelper.sin((float) branchIndex * 3.1415927F / (float) branchCount) * width);			
			currentPitch = currentYaw * yawPitchRatio;
			currentYaw *= (double) random.nextFloat() * 0.25D + 0.75D;
			currentPitch *= (double) random.nextFloat() * 0.25D + 0.75D;
			deltaXZ = MathHelper.cos(pitch);
			deltaY = MathHelper.sin(pitch);
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
				this.carveRegion(noiseProvider, heightToHorizontalStretchFactor, chunk, seed, mainChunkX, mainChunkZ, x, y, z, currentYaw, currentPitch, carvingMask, cachedBiomeProvider);
			}
		}
	}

	@Override
	protected boolean isPositionExcluded(float[] cache, double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y)
	{
		return (scaledRelativeX * scaledRelativeX + scaledRelativeZ * scaledRelativeZ) * (double) cache[y - 1] + scaledRelativeY * scaledRelativeY / 6.0D >= 1.0D;
	}
}
