package com.pg85.otg.gen;

import static com.pg85.otg.util.ChunkCoordinate.CHUNK_SIZE;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import com.pg85.otg.gen.biome.BiomeInterpolator;
import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.gen.carver.Carver;
import com.pg85.otg.gen.carver.CaveCarver;
import com.pg85.otg.gen.carver.RavineCarver;
import com.pg85.otg.gen.noise.OctavePerlinNoiseSampler;
import com.pg85.otg.gen.noise.PerlinNoiseSampler;
import com.pg85.otg.gen.noise.legacy.NoiseGeneratorPerlinMesaBlocks;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import it.unimi.dsi.fastutil.HashCommon;

/**
 * Generates the base terrain, surface, and caves for chunks.
 */
public class OTGChunkGenerator
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

	private final Preset preset;
	private final long seed;
	private final LayerSource biomeGenerator;

	private final int noiseSizeX = 4;
	private final int noiseSizeY = 32;
	private final int noiseSizeZ = 4;

	private final ThreadLocal<NoiseCache> noiseCache;
	private final NoiseGeneratorPerlinMesaBlocks biomeBlocksNoiseGen;
	// Carvers
	private final Carver caves;
	private final Carver ravines;
	// Biome blocks noise
	// TODO: Use new noise?
	private double[] biomeBlocksNoise = new double[CHUNK_SIZE * CHUNK_SIZE];
	private int lastX = Integer.MAX_VALUE;
	private int lastZ = Integer.MAX_VALUE;
	private double lastNoise = 0;

	public OTGChunkGenerator(Preset preset, long seed, LayerSource biomeGenerator)
	{
		this.preset = preset;
		this.seed = seed;
		this.biomeGenerator = biomeGenerator;

		// Setup noises
		Random random = new Random(seed);

		this.interpolationNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-7, 0));
		this.lowerInterpolatedNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));
		this.upperInterpolatedNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));
		this.depthNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));

		this.noiseCache = ThreadLocal.withInitial(() -> new NoiseCache(128, this.noiseSizeY + 1));

		this.biomeBlocksNoiseGen = new NoiseGeneratorPerlinMesaBlocks(random, 4);

		this.caves = new CaveCarver(256, preset.getWorldConfig());
		this.ravines = new RavineCarver(256, preset.getWorldConfig());
	}

	private static <T> T make(T object, Consumer<T> consumer)
	{
		consumer.accept(object);
		return object;
	}

	private double sampleNoise(int x, int y, int z, double horizontalScale, double verticalScale, double horizontalStretch, double verticalStretch, double volatility1, double volatility2, double volatilityWeight1, double volatilityWeight2)
	{
		// The algorithm for noise generation varies slightly here as it calculates the interpolation first and then the interpolated noise to avoid sampling noise that will never be used.
		// The end result is ~2x faster terrain generation.

		double delta = getInterpolationNoise(x, y, z, horizontalStretch, verticalStretch);

		if (delta < volatilityWeight1)
		{
			return getInterpolatedNoise(this.lowerInterpolatedNoise, x, y, z, horizontalScale, verticalScale) / 512.0D * volatility1;
		} else if (delta > volatilityWeight2)
		{
			return getInterpolatedNoise(this.upperInterpolatedNoise, x, y, z, horizontalScale, verticalScale) / 512.0D * volatility2;
		} else
		{
			// TODO: should probably use clamping here to prevent weird artifacts
			return MathHelper.lerp(
					delta,
					getInterpolatedNoise(this.lowerInterpolatedNoise, x, y, z, horizontalScale, verticalScale) / 512.0D * volatility1,
					getInterpolatedNoise(this.upperInterpolatedNoise, x, y, z, horizontalScale, verticalScale) / 512.0D * volatility2);
		}
	}

	private double getInterpolationNoise(int x, int y, int z, double horizontalStretch, double verticalStretch)
	{
		double interpolation = 0.0D;
		double amplitude = 1.0D;
		for (int i = 0; i < 8; i++)
		{
			PerlinNoiseSampler interpolationSampler = this.interpolationNoise.getOctave(i);
			if (interpolationSampler != null)
			{
				interpolation += interpolationSampler.sample(OctavePerlinNoiseSampler.maintainPrecision((double) x * horizontalStretch * amplitude), OctavePerlinNoiseSampler.maintainPrecision((double) y * verticalStretch * amplitude), OctavePerlinNoiseSampler.maintainPrecision((double) z * horizontalStretch * amplitude), verticalStretch * amplitude, (double) y * verticalStretch * amplitude) / amplitude;
			}

			amplitude /= 2.0D;
		}

		return (interpolation / 10.0D + 1.0D) / 2.0D;
	}

	private double getInterpolatedNoise(OctavePerlinNoiseSampler sampler, int x, int y, int z, double horizontalScale, double verticalScale)
	{
		double noise = 0.0D;
		double amplitude = 1.0D;
		for (int i = 0; i < 16; ++i)
		{
			double scaledX = OctavePerlinNoiseSampler.maintainPrecision((double) x * horizontalScale * amplitude);
			double scaledY = OctavePerlinNoiseSampler.maintainPrecision((double) y * verticalScale * amplitude);
			double scaledZ = OctavePerlinNoiseSampler.maintainPrecision((double) z * horizontalScale * amplitude);
			double scaledVerticalScale = verticalScale * amplitude;
			PerlinNoiseSampler perlinNoiseSampler = sampler.getOctave(i);
			if (perlinNoiseSampler != null)
			{
				noise += perlinNoiseSampler.sample(scaledX, scaledY, scaledZ, scaledVerticalScale, (double) y * scaledVerticalScale) / amplitude;
			}

			amplitude /= 2.0D;
		}

		return noise;
	}

	private double getExtraHeightAt(int x, int z, double maxAverageDepth, double maxAverageHeight)
	{
		double noiseHeight = this.depthNoise.sample(x * 200, 10.0D, z * 200, 1.0D, 0.0D, true) * 65535.0 / 8000.0;

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

	public void getNoiseColumn(double[] buffer, int x, int z)
	{
		// TODO: check only for edges
		this.noiseCache.get().get(buffer, x, z);
	}

	private void generateNoiseColumn(double[] noiseColumn, int noiseX, int noiseZ)
	{
		IBiomeConfig center = getBiomeAt(noiseX, noiseZ);

		final int maxYSections = this.preset.getWorldConfig().getWorldHeightCap() / 8 + 1;
		final int usedYSections = this.preset.getWorldConfig().getWorldHeightScale() / 8 + 1;

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

		for (int x1 = -center.getSmoothRadius(); x1 <= center.getSmoothRadius(); ++x1)
		{
			for (int z1 = -center.getSmoothRadius(); z1 <= center.getSmoothRadius(); ++z1)
			{
				// TODO: thread local lossy cache for biome sampling
				IBiomeConfig biome = getBiomeAt(noiseX + x1, noiseZ + z1);

				float heightAt = biome.getBiomeHeight();
				// TODO: vanilla reduces the weight by half when the depth here is greater than the center depth, but OTG doesn't do that?
				float weightAt = BIOME_WEIGHT_TABLE[x1 + 32 + (z1 + 32) * 65] / (heightAt + 2.0F);
				weightAt = Math.abs(weightAt); // This is required to prevent seams when height goes below -2

				weight += weightAt;

				height += heightAt * weightAt;
				volatility += biome.getBiomeVolatility() * weightAt;
				volatility1 += biome.getVolatility1() * weightAt;
				volatility2 += biome.getVolatility2() * weightAt;
				horizontalFracture += biome.getFractureHorizontal() * weightAt;
				verticalFracture += biome.getFractureVertical() * weightAt;
				volatilityWeight1 += biome.getVolatilityWeight1() * weightAt;
				volatilityWeight2 += biome.getVolatilityWeight2() * weightAt;
				maxAverageDepth += biome.getMaxAverageDepth() * weightAt;
				maxAverageHeight += biome.getMaxAverageHeight() * weightAt;
			}
		}

		// CHC Smoothing
		double chcWeight = 0;
		for (int x1 = -center.getCHCSmoothRadius(); x1 <= center.getCHCSmoothRadius(); ++x1)
		{
			for (int z1 = -center.getCHCSmoothRadius(); z1 <= center.getCHCSmoothRadius(); ++z1)
			{
				IBiomeConfig biome = getBiomeAt(noiseX + x1, noiseZ + z1);

				float heightAt = biome.getBiomeHeight();
				float weightAt = BIOME_WEIGHT_TABLE[x1 + 32 + (z1 + 32) * 65] / (heightAt + 2.0F);
				weightAt = Math.abs(weightAt);

				chcWeight += weightAt;

				for (int y = 0; y < this.noiseSizeY + 1; y++)
				{
					chc[y] += biome.getCHCData(y) * weightAt;
				}
			}
		}

		// Normalize biome data
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

		// Normalize CHC
		for (int y = 0; y < this.noiseSizeY + 1; y++)
		{
			chc[y] /= chcWeight;
		}

		// Vary the height with more noise
		float extraHeight = (float) (getExtraHeightAt(noiseX, noiseZ, maxAverageDepth, maxAverageHeight) * 0.2);

		// Do some math on volatility and height
		volatility = volatility * 0.9f + 0.1f;
		height = (height * 4.0F - 1.0F) / 8.0F;

		// Factor in y sections
		height = usedYSections * (2.0f + height + extraHeight) / 4.0f;

		for (int y = 0; y <= this.noiseSizeY; ++y)
		{
			// Calculate falloff
			double falloff = (height - y) * 12.0D * 128.0D / this.preset.getWorldConfig().getWorldHeightCap() / volatility;
			if (falloff > 0.0)
			{
				falloff *= 4.0;
			}

			double horizontalScale = WORLD_GEN_CONSTANT * horizontalFracture;
			double verticalScale = WORLD_GEN_CONSTANT * verticalFracture;

			double noise = sampleNoise(noiseX, y, noiseZ, horizontalScale, verticalScale, horizontalScale / 80, verticalScale / 160, volatility1, volatility2, volatilityWeight1, volatilityWeight2);

			if (!center.disableNotchHeightControl())
			{
				// Add the falloff at this height
				noise += falloff;

				// Reduce the last 3 layers
				if (y > 28)
				{
					noise = MathHelper.clampedLerp(noise, -10, ((double) y - 28) / 3.0);
				}
			}

			// Add chc data
			noise += chc[y];

			// Store value
			noiseColumn[y] = noise;
		}
	}

	private IBiomeConfig getBiomeAt(int biomeX, int biomeZ)
	{
		return biomeGenerator.getConfig(biomeX, biomeZ);
	}

	public IBiomeConfig getBiomeAtWorldCoord(int x, int z)
	{
		// TODO: Technically, we should be providing the hashed seed here. Perhaps this may work for the time being?
		return BiomeInterpolator.getConfig(this.seed, x, 0, z, this.biomeGenerator);
	}

	public void populateNoise(int worldHeightCap, Random random, ChunkBuffer buffer, ChunkCoordinate pos)
	{
		// Fill waterLevel array, used when placing stone/ground/surface blocks.
		// TODO: water levels
		byte[] waterLevel = new byte[CHUNK_SIZE * CHUNK_SIZE];
		Arrays.fill(waterLevel, (byte) 63);

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
									IBiomeConfig biomeConfig = this.getBiomeAtWorldCoord(realX, realZ);
									buffer.setBlock(localX, realY, localZ, biomeConfig.getStoneBlockReplaced((short) realY));
									buffer.setHighestBlockForColumn(pieceX + noiseX * 4, noiseZ * 4 + pieceZ, realY);
								} else if (realY < 63)
								{
									// TODO: water levels
									IBiomeConfig biomeConfig = this.getBiomeAtWorldCoord(realX, realZ);
									buffer.setBlock(localX, realY, localZ, biomeConfig.getWaterBlockReplaced(realY));
									buffer.setHighestBlockForColumn(pieceX + noiseX * 4, noiseZ * 4 + pieceZ, realY);
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

		doSurfaceAndGroundControl(random, worldHeightCap, this.seed, buffer, waterLevel);
	}

	public void carve(ChunkBuffer chunk, long seed, int chunkX, int chunkZ, BitSet carvingMask)
	{

		Random random = new Random();
		for (int localChunkX = chunkX - 8; localChunkX <= chunkX + 8; ++localChunkX)
		{
			for (int localChunkZ = chunkZ - 8; localChunkZ <= chunkZ + 8; ++localChunkZ)
			{
				setCarverSeed(random, seed, localChunkX, localChunkZ);

				if (this.caves.shouldCarve(random, localChunkX, localChunkZ))
				{
					this.caves.carve(chunk, random, 63, localChunkX, localChunkZ, chunkX, chunkZ, carvingMask);
				}

				if (this.ravines.shouldCarve(random, localChunkX, localChunkZ))
				{
					this.ravines.carve(chunk, random, 63, localChunkX, localChunkZ, chunkX, chunkZ, carvingMask);
				}
			}
		}
	}

	// Surface / ground / stone blocks / SAGC

	private long setCarverSeed(Random random, long seed, int x, int z)
	{
		random.setSeed(seed);
		long i = random.nextLong();
		long j = random.nextLong();
		long k = (long) x * i ^ (long) z * j ^ seed;
		random.setSeed(k);
		return k;
	}

	public int getNoiseSizeY()
	{
		return noiseSizeY + 1;
	}

	// Previously ChunkProviderOTG.addBiomeBlocksAndCheckWater
	private void doSurfaceAndGroundControl(Random random, int heightCap, long worldSeed, ChunkBuffer chunkBuffer, byte[] waterLevel)
	{
		// Process surface and ground blocks for each column in the chunk
		ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();
		double d1 = 0.03125D;
		this.biomeBlocksNoise = this.biomeBlocksNoiseGen.getRegion(this.biomeBlocksNoise, chunkCoord.getBlockX(), chunkCoord.getBlockZ(), CHUNK_SIZE, CHUNK_SIZE, d1 * 2.0D, d1 * 2.0D, 1.0D);
		GeneratingChunk generatingChunk = new GeneratingChunk(random, waterLevel, this.biomeBlocksNoise, heightCap);
		for (int x = 0; x < CHUNK_SIZE; x++)
		{
			for (int z = 0; z < CHUNK_SIZE; z++)
			{
				// Get the current biome config and some properties

				IBiomeConfig biomeConfig = getBiomeAtWorldCoord(chunkCoord.getBlockX() + x, chunkCoord.getBlockZ() + z);
				biomeConfig.doSurfaceAndGroundControl(worldSeed, generatingChunk, chunkBuffer, biomeConfig, chunkCoord.getBlockX() + x, chunkCoord.getBlockZ() + z);
			}
		}
	}

	// Used by sagc for generating surface/ground block patterns
	public double getBiomeBlocksNoiseValue(int blockX, int blockZ)
	{
		double noise = this.lastNoise;
		if (this.lastX != blockX || this.lastZ != blockZ)
		{
			double d1 = 0.03125D;
			noise = this.biomeBlocksNoiseGen.getRegion(new double[1], blockX, blockZ, 1, 1, d1 * 2.0D, d1 * 2.0D, 1.0D)[0];
			this.lastX = blockX;
			this.lastZ = blockZ;
			this.lastNoise = noise;
		}
		return noise;
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
			} else
			{
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
