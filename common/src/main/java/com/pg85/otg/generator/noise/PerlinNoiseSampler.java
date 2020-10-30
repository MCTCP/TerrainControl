package com.pg85.otg.generator.noise;

import java.util.Random;

import com.pg85.otg.util.helpers.MathHelper;

// Derived from net.minecraft.world.gen.ImprovedNoiseGenerator
public class PerlinNoiseSampler
{
	public final double xOffset;
	public final double yOffset;
	public final double zOffset;
	private final byte[] permutations;

	public PerlinNoiseSampler(Random random)
	{
		this.xOffset = random.nextDouble() * 256.0D;
		this.yOffset = random.nextDouble() * 256.0D;
		this.zOffset = random.nextDouble() * 256.0D;
		this.permutations = new byte[256];

		for (int i = 0; i < 256; ++i)
		{
			this.permutations[i] = (byte) i;
		}

		for (int k = 0; k < 256; ++k)
		{
			int j = random.nextInt(256 - k);
			byte b0 = this.permutations[k];
			this.permutations[k] = this.permutations[k + j];
			this.permutations[k + j] = b0;
		}

	}

	private static double grad(int gradIndex, double xFactor, double yFactor, double zFactor)
	{
		int i = gradIndex & 15;
		return SimplexNoiseSampler.dot(SimplexNoiseSampler.GRAD[i], xFactor, yFactor, zFactor);
	}

	// TODO: yScale and yOffset params are probably wrong. More research needs to be done to figure out what these are.
	public double sample(double x, double y, double z, double yScale, double yOffset)
	{
		double d0 = x + this.xOffset;
		double d1 = y + this.yOffset;
		double d2 = z + this.zOffset;
		int i = MathHelper.floor(d0);
		int j = MathHelper.floor(d1);
		int k = MathHelper.floor(d2);
		double d3 = d0 - (double) i;
		double d4 = d1 - (double) j;
		double d5 = d2 - (double) k;
		double d6 = MathHelper.smoothstep(d3);
		double d7 = MathHelper.smoothstep(d4);
		double d8 = MathHelper.smoothstep(d5);
		double d9;
		if (yScale != 0.0D)
		{
			double d10 = Math.min(yOffset, d4);
			d9 = (double) MathHelper.floor(d10 / yScale) * yScale;
		} else {
			d9 = 0.0D;
		}

		return this.sample(i, j, k, d3, d4 - d9, d5, d6, d7, d8);
	}

	private int permute(int idx)
	{
		return this.permutations[idx & 255] & 255;
	}

	public double sample(int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double smoothedX, double smoothedY, double smoothedZ)
	{
		int i = this.permute(sectionX) + sectionY;
		int j = this.permute(i) + sectionZ;
		int k = this.permute(i + 1) + sectionZ;
		int l = this.permute(sectionX + 1) + sectionY;
		int i1 = this.permute(l) + sectionZ;
		int j1 = this.permute(l + 1) + sectionZ;
		double d0 = grad(this.permute(j), localX, localY, localZ);
		double d1 = grad(this.permute(i1), localX - 1.0D, localY, localZ);
		double d2 = grad(this.permute(k), localX, localY - 1.0D, localZ);
		double d3 = grad(this.permute(j1), localX - 1.0D, localY - 1.0D, localZ);
		double d4 = grad(this.permute(j + 1), localX, localY, localZ - 1.0D);
		double d5 = grad(this.permute(i1 + 1), localX - 1.0D, localY, localZ - 1.0D);
		double d6 = grad(this.permute(k + 1), localX, localY - 1.0D, localZ - 1.0D);
		double d7 = grad(this.permute(j1 + 1), localX - 1.0D, localY - 1.0D, localZ - 1.0D);
		return MathHelper.lerp3(smoothedX, smoothedY, smoothedZ, d0, d1, d2, d3, d4, d5, d6, d7);
	}
}