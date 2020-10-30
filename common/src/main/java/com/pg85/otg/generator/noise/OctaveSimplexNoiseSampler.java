package com.pg85.otg.generator.noise;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

// Derived from net.minecraft.world.gen.PerlinNoiseGenerator
// The name is mis-mapped in MCP, it is indeed simplex noise.
public class OctaveSimplexNoiseSampler
{
	private final SimplexNoiseSampler[] octaves;
	private final double persistence;
	private final double lacunarity;

	public OctaveSimplexNoiseSampler(Random random, IntStream octaves)
	{
		this(random, octaves.boxed().collect(ImmutableList.toImmutableList()));
	}

	public OctaveSimplexNoiseSampler(Random random, List<Integer> octaves)
	{
		this(random, new IntRBTreeSet(octaves));
	}

	private OctaveSimplexNoiseSampler(Random random, IntSortedSet octaves)
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
				SimplexNoiseSampler sampler = new SimplexNoiseSampler(random);
				this.octaves = new SimplexNoiseSampler[totalOctaves];
				if (endOctave >= 0 && endOctave < totalOctaves && octaves.contains(0))
				{
					this.octaves[endOctave] = sampler;
				}

				for (int i1 = endOctave + 1; i1 < totalOctaves; ++i1)
				{
					if (i1 >= 0 && octaves.contains(endOctave - i1))
					{
						this.octaves[i1] = new SimplexNoiseSampler(random);
					}
				}

				if (endOctave > 0)
				{
					long randomSeed = (long) (sampler.sample(sampler.xOffset, sampler.yOffset, sampler.zOffset) * (double) 9.223372E18F);
					Random rand = new Random(randomSeed);

					for (int j1 = endOctave - 1; j1 >= 0; --j1)
					{
						if (j1 < totalOctaves && octaves.contains(endOctave - j1))
						{
							this.octaves[j1] = new SimplexNoiseSampler(rand);
						}
					}
				}

				this.lacunarity = Math.pow(2.0D, endOctave);
				this.persistence = 1.0D / (Math.pow(2.0D, totalOctaves) - 1.0D);
			}
		}
	}

	public double sample(double x, double y, boolean useNoiseOffsets)
	{
		double sum = 0.0D;
		double lacunarity = this.lacunarity;
		double persistence = this.persistence;

		for (SimplexNoiseSampler sampler : this.octaves)
		{
			if (sampler != null)
			{
				sum += sampler.sample(x * lacunarity + (useNoiseOffsets ? sampler.xOffset : 0.0D), y * lacunarity + (useNoiseOffsets ? sampler.yOffset : 0.0D)) * persistence;
			}

			lacunarity /= 2.0D;
			persistence *= 2.0D;
		}

		return sum;
	}

	public double sample(double x, double y, double yScale, double yOffset)
	{
		return this.sample(x, y, true) * 0.55D;
	}
}
