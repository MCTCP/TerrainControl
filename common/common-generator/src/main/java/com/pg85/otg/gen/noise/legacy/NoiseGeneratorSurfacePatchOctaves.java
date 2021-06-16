package com.pg85.otg.gen.noise.legacy;

import java.util.Random;

@Deprecated
public class NoiseGeneratorSurfacePatchOctaves
{
	private NoiseGeneratorSurfacePatch[] noiseArray;
	private int numOctaves;

	public NoiseGeneratorSurfacePatchOctaves(Random random, int numOctaves)
	{
		this.numOctaves = numOctaves;
		this.noiseArray = new NoiseGeneratorSurfacePatch[numOctaves];
		for (int i = 0; i < numOctaves; i++)
			this.noiseArray[i] = new NoiseGeneratorSurfacePatch(random);
	}

	/**
	 * Convenience Method for getting simple noise at a specific x and z, composites noise from
	 * {@code numOctaves} octaves.
	 * @param x The x coordinate
	 * @param z The z coordinate
	 * @return the noise value of y
	 */
	public double getYNoise(double x, double z)
	{
		double resultingY = 0.0D;
		double octaveAmplitude = 1.0D;

		for (int var9 = 0; var9 < this.numOctaves; ++var9)
		{
			resultingY += this.noiseArray[var9].getYNoise(x * octaveAmplitude, z * octaveAmplitude) / octaveAmplitude;
			octaveAmplitude /= 2.0D;
		}

		return resultingY;
	}
}
