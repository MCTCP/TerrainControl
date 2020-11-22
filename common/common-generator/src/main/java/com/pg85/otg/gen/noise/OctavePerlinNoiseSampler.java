package com.pg85.otg.gen.noise;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.pg85.otg.util.Pair;
import com.pg85.otg.util.helpers.MathHelper;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;


// Derived from net.minecraft.world.gen.OctavesNoiseGenerator
public class OctavePerlinNoiseSampler
{
	private final PerlinNoiseSampler[] octaves;
	private final DoubleList amplitudes;
	private final double persistence;
	private final double lacunarity;

	public OctavePerlinNoiseSampler(Random random, IntStream octaves)
	{
		this(random, octaves.boxed().collect(ImmutableList.toImmutableList()));
	}

	private OctavePerlinNoiseSampler(Random random, List<Integer> octaves)
	{
		this(random, new IntRBTreeSet(octaves));
	}

	private OctavePerlinNoiseSampler(Random random, IntSortedSet octaves)
	{
		this(random, generateAmplitudes(octaves));
	}

	private OctavePerlinNoiseSampler(Random random, Pair<Integer, DoubleList> octaves)
	{
		int octaveCount = octaves.getFirst();
		this.amplitudes = octaves.getSecond();
		PerlinNoiseSampler sampler = new PerlinNoiseSampler(random);
		int amplitudeLength = this.amplitudes.size();
		int totalOctaves = -octaveCount;
		this.octaves = new PerlinNoiseSampler[amplitudeLength];
		if (totalOctaves >= 0 && totalOctaves < amplitudeLength)
		{
			double amplitude = this.amplitudes.getDouble(totalOctaves);
			if (amplitude != 0.0D)
			{
				this.octaves[totalOctaves] = sampler;
			}
		}

		for (int i1 = totalOctaves - 1; i1 >= 0; --i1)
		{
			if (i1 < amplitudeLength)
			{
				double amplitude = this.amplitudes.getDouble(i1);
				if (amplitude != 0.0D)
				{
					this.octaves[i1] = new PerlinNoiseSampler(random);
				}
			}
		}

		if (totalOctaves < amplitudeLength - 1)
		{
			long randomSeed = (long) (sampler.sample(0.0D, 0.0D, 0.0D, 0.0D, 0.0D) * (double) 9.223372E18F);
			Random rand = new Random(randomSeed);

			for (int l = totalOctaves + 1; l < amplitudeLength; ++l)
			{
				if (l >= 0)
				{
					double amplitude = this.amplitudes.getDouble(l);
					if (amplitude != 0.0D)
					{
						this.octaves[l] = new PerlinNoiseSampler(rand);
					}
				}
			}
		}

		this.lacunarity = Math.pow(2.0D, -totalOctaves);
		this.persistence = Math.pow(2.0D, amplitudeLength - 1) / (Math.pow(2.0D, amplitudeLength) - 1.0D);
	}

	public static OctavePerlinNoiseSampler create(Random random, int octaves, DoubleList amplitudes)
	{
		return new OctavePerlinNoiseSampler(random, Pair.of(octaves, amplitudes));
	}

	private static Pair<Integer, DoubleList> generateAmplitudes(IntSortedSet octaves)
	{
		if (octaves.isEmpty())
		{
			throw new IllegalArgumentException("Need some octaves!");
		} else {
			int startOctave = -octaves.firstInt();
			int endOctave = octaves.lastInt();
			int totalOctaves = startOctave + endOctave + 1;
			if (totalOctaves < 1)
			{
				throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
			} else {
				DoubleList amplitudes = new DoubleArrayList(new double[totalOctaves]);
				IntBidirectionalIterator iterator = octaves.iterator();

				while (iterator.hasNext())
				{
					int l = iterator.nextInt();
					amplitudes.set(l + startOctave, 1.0D);
				}

				return Pair.of(-startOctave, amplitudes);
			}
		}
	}

	public static double maintainPrecision(double value)
	{
		return value - (double) MathHelper.lfloor(value / 3.3554432E7D + 0.5D) * 3.3554432E7D;
	}

	public double sample(double x, double y, double z)
	{
		return this.sample(x, y, z, 0.0D, 0.0D, false);
	}

	public double sample(double x, double y, double z, double yScale, double yOffset, boolean useOffset)
	{
		double sum = 0.0D;
		double lacunarity = this.lacunarity;
		double persistence = this.persistence;

		for (int i = 0; i < this.octaves.length; ++i)
		{
			PerlinNoiseSampler sampler = this.octaves[i];
			if (sampler != null)
			{
				sum += this.amplitudes.getDouble(i) * sampler.sample(maintainPrecision(x * lacunarity), useOffset ? -sampler.yOffset : maintainPrecision(y * lacunarity), maintainPrecision(z * lacunarity), yScale * lacunarity, yOffset * lacunarity) * persistence;
			}

			lacunarity *= 2.0D;
			persistence /= 2.0D;
		}

		return sum;
	}

	public PerlinNoiseSampler getOctave(int index)
	{
		return this.octaves[this.octaves.length - 1 - index];
	}

	public double sample(double x, double y, double yScale, double yOffset)
	{
		return this.sample(x, y, 0.0D, yScale, yOffset, false);
	}
}