package com.pg85.otg.gen.noise.legacy;

import com.pg85.otg.util.helpers.MathHelper;

import java.util.Random;

@Deprecated
public class NoiseGeneratorPerlinOctaves
{

	private NoiseGeneratorPerlin[] noiseArray;
	private int numOctaves;

	public NoiseGeneratorPerlinOctaves(Random random, int numOctaves)
	{
		this.numOctaves = numOctaves;
		this.noiseArray = new NoiseGeneratorPerlin[numOctaves];

		for (int j = 0; j < numOctaves; ++j)
		{
			this.noiseArray[j] = new NoiseGeneratorPerlin(random);
		}
	}

	public double[] Noise3D(double[] doubleArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale)
	{
		if (doubleArray == null)
		{
			doubleArray = new double[xSize * ySize * zSize];
		} else {
			for (int k1 = 0; k1 < doubleArray.length; ++k1)
			{
				doubleArray[k1] = 0.0D;
			}
		}

		double d3 = 1.0D;

		for (int l1 = 0; l1 < this.numOctaves; ++l1)
		{
			double d4 = (double) xOffset * d3 * xScale;
			double d5 = (double) yOffset * d3 * yScale;
			double d6 = (double) zOffset * d3 * zScale;
			long i2 = MathHelper.lfloor(d4);
			long j2 = MathHelper.lfloor(d6);
			
			d4 -= (double) i2;
			d6 -= (double) j2;
			i2 %= 16777216L;
			j2 %= 16777216L;
			d4 += (double) i2;
			d6 += (double) j2;
			this.noiseArray[l1].populateNoiseArray3D(doubleArray, d4, d5, d6, xSize, ySize, zSize, xScale * d3, yScale * d3, zScale * d3, d3);
			d3 /= 2.0D;
		}

		return doubleArray;
	}


	public double[] Noise2D(double[] doubleArray, int xOffset, int zOffset, int xSize, int zSize, double xScale, double zScale)
	{
		if (doubleArray == null)
		{
			doubleArray = new double[xSize * zSize];
		} else
		{
			for (int k1 = 0; k1 < doubleArray.length; ++k1)
			{
				doubleArray[k1] = 0.0D;
			}
		}

		double d3 = 1.0D;

		for (int l1 = 0; l1 < this.numOctaves; ++l1)
		{
			double d4 = (double) xOffset * d3 * xScale;
			double d6 = (double) zOffset * d3 * zScale;
			long i2 = MathHelper.lfloor(d4);
			long j2 = MathHelper.lfloor(d6);

			d4 -= (double) i2;
			d6 -= (double) j2;
			i2 %= 16777216L;
			j2 %= 16777216L;
			d4 += (double) i2;
			d6 += (double) j2;
			this.noiseArray[l1].populateNoiseArray2D(doubleArray, d4, d6, xSize, zSize, xScale * d3, zScale * d3, d3);
			d3 /= 2.0D;
		}

		return doubleArray;
	}
}
