package com.pg85.otg.generator;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.generator.biome.layers.LayerSource;
import com.pg85.otg.generator.noise.OctavePerlinNoiseSampler;
import com.pg85.otg.generator.noise.PerlinNoiseSampler;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.MathHelper;

import it.unimi.dsi.fastutil.HashCommon;

/**
 * 1.16 version of {@link ChunkProviderOTG}. Will be renamed at some point.
 */
public class NewOTGChunkGenerator
{
	// "It's a number that made the worldgen look good!" - Dinnerbone 2020
	private static final double WORLD_GEN_CONSTANT = 684.412;

	private static final float[] BIOME_WEIGHT_TABLE = make(new float[65 * 65], (array) ->
	{
		for (int x = -32; x <= 32; ++x)
		{
			for (int z = -32; z <= 32; ++z)
			{
				float f = 10.0F / MathHelper.sqrt((float) (x * x + z * z) + 0.2F);
				array[x + 32 + (z + 32) * 65] = f;
			}
		}
	});

	private final OctavePerlinNoiseSampler interpolationNoise;     // Volatility noise
	private final OctavePerlinNoiseSampler lowerInterpolatedNoise; // Volatility1 noise
	private final OctavePerlinNoiseSampler upperInterpolatedNoise; // Volatility2 noise

	private final OctavePerlinNoiseSampler depthNoise;
	private final long seed;
	private final LayerSource biomeGenerator;
	private final LocalMaterialData stoneBlock;
	private final LocalMaterialData waterBlock;

	private final int noiseSizeX = 4;
	private final int noiseSizeY = 32;
	private final int noiseSizeZ = 4;

	private final ThreadLocal<NoiseCache> noiseCache;

	public NewOTGChunkGenerator(long seed, LayerSource biomeGenerator, LocalMaterialData stoneBlock, LocalMaterialData waterBlock)
	{
		this.seed = seed;
		this.biomeGenerator = biomeGenerator;
		this.stoneBlock = stoneBlock;
		this.waterBlock = waterBlock;

		// Setup noises
		Random random = new Random(seed);

		this.interpolationNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-7, 0));
		this.lowerInterpolatedNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));
		this.upperInterpolatedNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));

		this.depthNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));

		this.noiseCache = ThreadLocal.withInitial(() -> new NoiseCache(128, this.noiseSizeY + 1));
	}

	public static <T> T make(T object, Consumer<T> consumer)
	{
		consumer.accept(object);
		return object;
	}

	private double sampleNoise(int x, int y, int z, double horizontalScale, double verticalScale, double horizontalStretch, double verticalStretch, double volatility1, double volatility2, double volatilityWeight1, double volatilityWeight2)
	{
		double lowerInterpolation = 0.0D;
		double upperInterpolation = 0.0D;
		double interpolation = 0.0D;
		double amplitude = 1.0D;

		// TODO: interpolation optimization
		for (int i = 0; i < 16; ++i)
		{
			double scaledX = OctavePerlinNoiseSampler.maintainPrecision((double) x * horizontalScale * amplitude);
			double scaledY = OctavePerlinNoiseSampler.maintainPrecision((double) y * verticalScale * amplitude);
			double scaledZ = OctavePerlinNoiseSampler.maintainPrecision((double) z * horizontalScale * amplitude);
			double scaledVerticalScale = verticalScale * amplitude;
			PerlinNoiseSampler lowerSampler = this.lowerInterpolatedNoise.getOctave(i);
			if (lowerSampler != null)
			{
				lowerInterpolation += lowerSampler.sample(scaledX, scaledY, scaledZ, scaledVerticalScale, (double) y * scaledVerticalScale) / amplitude;
			}

			PerlinNoiseSampler upperSampler = this.upperInterpolatedNoise.getOctave(i);
			if (upperSampler != null)
			{
				upperInterpolation += upperSampler.sample(scaledX, scaledY, scaledZ, scaledVerticalScale, (double) y * scaledVerticalScale) / amplitude;
			}

			if (i < 8)
			{
				PerlinNoiseSampler interpolationSampler = this.interpolationNoise.getOctave(i);
				if (interpolationSampler != null)
				{
					interpolation += interpolationSampler.sample(OctavePerlinNoiseSampler.maintainPrecision((double) x * horizontalStretch * amplitude), OctavePerlinNoiseSampler.maintainPrecision((double) y * verticalStretch * amplitude), OctavePerlinNoiseSampler.maintainPrecision((double) z * horizontalStretch * amplitude), verticalStretch * amplitude, (double) y * verticalStretch * amplitude) / amplitude;
				}
			}

			amplitude /= 2.0D;
		}

		double delta = (interpolation / 10.0D + 1.0D) / 2.0D;

		double lowerNoise = lowerInterpolation / 512.0D * volatility1;
		double upperNoise = upperInterpolation / 512.0D * volatility2;

		if (delta < volatilityWeight1)
		{
			return lowerNoise;
		} else if (delta > volatilityWeight2)
		{
			return upperNoise;
		} else
		{
			// TODO: should probably use clamping here to prevent weird artifacts
			return MathHelper.lerp(delta, lowerNoise, upperNoise);
		}
	}

	private double getExtraHeightAt(int x, int z, double maxAverageDepth, double maxAverageHeight)
	{
		double noiseHeight = this.depthNoise.sample(x * 200, 10.0D, z * 200, 1.0D, 0.0D, true) / 8000.0;

		if (noiseHeight < 0.0D)
		{
			noiseHeight = -noiseHeight * 0.3D;
		}
		noiseHeight = noiseHeight * 3.0D - 2.0D;

		if (noiseHeight < 0.0D)
		{
			noiseHeight /= 2.0D;
			if (noiseHeight < -1.0D)
			{
				noiseHeight = -1.0D;
			}
			noiseHeight -= maxAverageDepth;
			noiseHeight /= 1.4D;
			noiseHeight /= 2.0D;
		} else
		{
			if (noiseHeight > 1.0D)
			{
				noiseHeight = 1.0D;
			}
			noiseHeight += maxAverageHeight;
			noiseHeight /= 8.0D;
		}

		return noiseHeight;
	}

	protected void getNoiseColumn(double[] buffer, int x, int z)
	{
		// TODO: check only for edges
		this.noiseCache.get().get(buffer, x, z);
	}

	private void generateNoiseColumn(double[] noiseColumn, int noiseX, int noiseZ)
	{
		BiomeGenData center = getBiomeAt(noiseX, noiseZ);

		float height = 0; // depth
		float volatility = 0; // scale
		double volatility1 = 0;
		double volatility2 = 0;
		double horizontalFracture = 0;
		double verticalFracture = 0;
		double volatilityWeight1 = 0;
		double volatilityWeight2 = 0;
		double maxAverageDepth = 0;
		double maxAverageHeight = 0;
		double[] chc = new double[this.noiseSizeY + 1];
		float weight = 0;

		for (int x1 = -center.smoothRadius; x1 <= center.smoothRadius; ++x1)
		{
			for (int z1 = -center.smoothRadius; z1 <= center.smoothRadius; ++z1)
			{
				// TODO: thread local lossy cache for biome sampling
				BiomeGenData data = getBiomeAt(noiseX + x1, noiseZ + z1);

				float heightAt = data.biomeHeight;
				// TODO: vanilla reduces the weight by half when the depth here is greater than the center depth, but OTG doesn't do that?
				float weightAt = BIOME_WEIGHT_TABLE[x1 + 32 + (z1 + 32) * 65] / (heightAt + 2.0F);

				weight += weightAt;

				height += heightAt * weightAt;
				volatility += data.biomeVolatility * weightAt;
				volatility1 += data.volatility1 * weightAt;
				volatility2 += data.volatility2 * weightAt;
				horizontalFracture += data.horizontalFracture * weightAt;
				verticalFracture += data.verticalFracture * weightAt;
				volatilityWeight1 += data.getVolatilityWeight1() * weightAt;
				volatilityWeight2 += data.getVolatilityWeight2() * weightAt;
				maxAverageDepth += data.maxAverageDepth * weightAt;
				maxAverageHeight += data.maxAverageHeight * weightAt;

				for (int y = 0; y < this.noiseSizeY + 1; y++)
				{
					chc[y] += data.chc[y] * weightAt;
				}
			}
		}

		height /= weight;
		volatility /= weight;
		volatility1 /= weight;
		volatility2 /= weight;
		horizontalFracture /= weight;
		verticalFracture /= weight;
		volatilityWeight1 /= weight;
		volatilityWeight2 /= weight;
		maxAverageDepth /= weight;
		maxAverageHeight /= weight;

		for (int y = 0; y < this.noiseSizeY + 1; y++)
		{
			chc[y] /= weight;
		}

		volatility = volatility * 0.9f + 0.1f;
		height = (height * 4.0F - 1.0F) / 8.0F;

		height += getExtraHeightAt(noiseX, noiseZ, maxAverageDepth, maxAverageHeight) * 0.2;

		for (int y = 0; y <= this.noiseSizeY; ++y)
		{
			double falloff = ((8.5D + height * 8.5D / 8.0D * 4.0D) - y) * 12.0D * 128.0D / 256.0 / volatility;
			if (falloff > 0.0)
			{
				falloff *= 4.0;
			}

			double horizontalScale = WORLD_GEN_CONSTANT * horizontalFracture;
			double verticalScale = WORLD_GEN_CONSTANT * verticalFracture;

			double noise = sampleNoise(noiseX, y, noiseZ, horizontalScale, verticalScale, horizontalScale / 80, verticalScale / 160, volatility1, volatility2, volatilityWeight1, volatilityWeight2);

			// TODO: add if statement for biome height control here
			noise += falloff;

			if (y > 28)
			{
				noise = MathHelper.clampedLerp(noise, -10, ((double) y - 28) / 3.0);
			}

			noise += chc[y];

			noiseColumn[y] = noise;
		}
	}

	private BiomeGenData getBiomeAt(int x, int z)
	{
		return BiomeGenData.LOOKUP[biomeGenerator.getSampler().sample(x, z)];
	}

	public void populateNoise(ChunkBuffer buffer, ChunkCoordinate pos)
	{
		// TODO: this double[][][] is probably really bad for performance
		double[][][] noiseData = new double[2][this.noiseSizeZ + 1][this.noiseSizeY + 1];

		// Initialize noise data on the x0 column.
		for (int noiseZ = 0; noiseZ < this.noiseSizeZ + 1; ++noiseZ)
		{
			noiseData[0][noiseZ] = new double[this.noiseSizeY + 1];
			this.getNoiseColumn(noiseData[0][noiseZ], pos.getChunkX() * this.noiseSizeX, pos.getChunkZ() * this.noiseSizeZ + noiseZ);
			noiseData[1][noiseZ] = new double[this.noiseSizeY + 1];
		}

		// [0, 4] -> x noise chunks
		for (int noiseX = 0; noiseX < this.noiseSizeX; ++noiseX)
		{
			// Initialize noise data on the x1 column
			int noiseZ;
			for (noiseZ = 0; noiseZ < this.noiseSizeZ + 1; ++noiseZ)
			{
				this.getNoiseColumn(noiseData[1][noiseZ], pos.getChunkX() * this.noiseSizeX + noiseX + 1, pos.getChunkZ() * this.noiseSizeZ + noiseZ);
			}

			// [0, 4] -> z noise chunks
			for (noiseZ = 0; noiseZ < this.noiseSizeZ; ++noiseZ)
			{

				// [0, 32] -> y noise chunks
				for (int noiseY = this.noiseSizeY - 1; noiseY >= 0; --noiseY)
				{
					// Lower samples
					double x0z0y0 = noiseData[0][noiseZ][noiseY];
					double x0z1y0 = noiseData[0][noiseZ + 1][noiseY];
					double x1z0y0 = noiseData[1][noiseZ][noiseY];
					double x1z1y0 = noiseData[1][noiseZ + 1][noiseY];
					// Upper samples
					double x0z0y1 = noiseData[0][noiseZ][noiseY + 1];
					double x0z1y1 = noiseData[0][noiseZ + 1][noiseY + 1];
					double x1z0y1 = noiseData[1][noiseZ][noiseY + 1];
					double x1z1y1 = noiseData[1][noiseZ + 1][noiseY + 1];

					// [0, 8] -> y noise pieces
					for (int pieceY = 8 - 1; pieceY >= 0; --pieceY)
					{
						int realY = noiseY * 8 + pieceY;

						// progress within loop
						double yLerp = (double) pieceY / 8.0;

						// Interpolate noise data based on y progress
						double x0z0 = MathHelper.lerp(yLerp, x0z0y0, x0z0y1);
						double x1z0 = MathHelper.lerp(yLerp, x1z0y0, x1z0y1);
						double x0z1 = MathHelper.lerp(yLerp, x0z1y0, x0z1y1);
						double x1z1 = MathHelper.lerp(yLerp, x1z1y0, x1z1y1);

						// [0, 4] -> x noise pieces
						for (int pieceX = 0; pieceX < 4; ++pieceX)
						{
							int realX = pos.getBlockX() + noiseX * 4 + pieceX;
							int localX = realX & 15;
							double xLerp = (double) pieceX / 4.0;
							// Interpolate noise based on x progress
							double z0 = MathHelper.lerp(xLerp, x0z0, x1z0);
							double z1 = MathHelper.lerp(xLerp, x0z1, x1z1);

							// [0, 4) -> z noise pieces
							for (int pieceZ = 0; pieceZ < 4; ++pieceZ)
							{
								int realZ = pos.getBlockZ() + noiseZ * 4 + pieceZ;
								int localZ = realZ & 15;
								double zLerp = (double) pieceZ / 4.0;
								// Get the real noise here by interpolating the last 2 noises together
								double rawNoise = MathHelper.lerp(zLerp, z0, z1);
								// Normalize the noise from (-256, 256) to [-1, 1]
								double density = MathHelper.clamp(rawNoise / 200.0D, -1.0D, 1.0D);

								if (density > 0.0)
								{
									buffer.setBlock(localX, realY, localZ, this.stoneBlock);
								} else if (realY < 63)
								{ // TODO: water levels
									buffer.setBlock(localX, realY, localZ, this.waterBlock);
								}
							}
						}
					}
				}
			}

			// Reuse noise data from the previous column for speed
			double[][] xColumn = noiseData[0];
			noiseData[0] = noiseData[1];
			noiseData[1] = xColumn;
		}
	}

	private class NoiseCache
	{
		private final long[] keys;
		private final double[] values;

		private final int mask;

		private NoiseCache(int size, int noiseSize)
		{
			size = MathHelper.smallestEncompassingPowerOfTwo(size);
			this.mask = size - 1;

			this.keys = new long[size];
			Arrays.fill(this.keys, Long.MIN_VALUE);
			this.values = new double[size * noiseSize];
		}

		public double[] get(double[] buffer, int x, int z)
		{
			long key = key(x, z);
			int idx = hash(key) & this.mask;

			// if the entry here has a key that matches ours, we have a cache hit
			if (this.keys[idx] == key)
			{
				// Copy values into buffer
				System.arraycopy(this.values, idx * buffer.length, buffer, 0, buffer.length);
			} else {
				// cache miss: sample and put the result into our cache entry

				// Sample the noise column to store the new values
				generateNoiseColumn(buffer, x, z);

				// Create copy of the array
				System.arraycopy(buffer, 0, this.values, idx * buffer.length, buffer.length);

				this.keys[idx] = key;
			}

			return buffer;
		}

		private int hash(long key)
		{
			return (int) HashCommon.mix(key);
		}

		private long key(int x, int z)
		{
			return MathHelper.toLong(x, z);
		}
	}
}