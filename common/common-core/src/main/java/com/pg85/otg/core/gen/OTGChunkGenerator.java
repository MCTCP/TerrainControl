package com.pg85.otg.core.gen;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.gen.biome.CachedBiomeProvider;
import com.pg85.otg.gen.carver.Carver;
import com.pg85.otg.gen.carver.CaveCarver;
import com.pg85.otg.gen.carver.RavineCarver;
import com.pg85.otg.gen.gen.OreVeinData;
import com.pg85.otg.gen.gen.OreVeinGenerator;
import com.pg85.otg.gen.noise.OctavePerlinNoiseSampler;
import com.pg85.otg.gen.noise.PerlinNoiseSampler;
import com.pg85.otg.gen.noise.legacy.NoiseGeneratorPerlinMesaBlocks;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.ILayerSource;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.ISurfaceGeneratorNoiseProvider;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;

/**
 * Generates the base terrain, sets stone/ground/surface blocks and does SurfaceAndGroundControl, generates caves and canyons.
 */
@SuppressWarnings("deprecation")
public class OTGChunkGenerator implements ISurfaceGeneratorNoiseProvider
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

	private static final float[] NOISE_WEIGHT_TABLE = make(new float[24 * 24 * 24], (array) ->
	{
		for (int z = 0; z < 24; ++z)
		{
			for (int x = 0; x < 24; ++x)
			{
				for (int y = 0; y < 24; ++y)
				{
					array[z * 24 * 24 + x * 24 + y] = (float) calculateNoiseWeight(x - 12, y - 12, z - 12);
				}
			}
		}

	});

	// TODO: ThreadLocal is used mostly as a crutch here, ideally these classes wouldn't maintain state.
	// ThreadLocal may have some overhead for the gets/sets, even when used on a single thread.
	// Some of these classes may not be thread-safe (tho testing seems ok), need to check all the internal state.
	
	private final OctavePerlinNoiseSampler interpolationNoise;	 // Volatility noise
	private final OctavePerlinNoiseSampler lowerInterpolatedNoise; // Volatility1 noise
	private final OctavePerlinNoiseSampler upperInterpolatedNoise; // Volatility2 noise
	private final OctavePerlinNoiseSampler depthNoise;

	private final Preset preset;
	private final long seed;
	private final CachedBiomeProvider cachedBiomeProvider;

	private final int noiseSizeX = 4;
	private final int noiseSizeY;
	private final int noiseSizeZ = 4;

	private final ThreadLocal<NoiseCache> noiseCache;
	private final NoiseGeneratorPerlinMesaBlocks biomeBlocksNoiseGen;
	// Carvers
	private final Carver caves;
	private final Carver ravines;
	// Biome blocks noise
	// TODO: Use new noise?
	private ThreadLocal<double[]> biomeBlocksNoise = ThreadLocal.withInitial(() -> new double[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE]);
	private ThreadLocal<Integer> lastX = ThreadLocal.withInitial(() -> Integer.MAX_VALUE);
	private ThreadLocal<Integer> lastZ = ThreadLocal.withInitial(() -> Integer.MAX_VALUE);
	private ThreadLocal<Double> lastNoise = ThreadLocal.withInitial(() -> 0d);
	private final OreVeinGenerator oreVeinGenerator;

	public OTGChunkGenerator(Preset preset, long seed, ILayerSource biomeProvider, IBiome[] biomesById, ILogger logger)
	{
		this.preset = preset;
		this.seed = seed;
		this.cachedBiomeProvider = new CachedBiomeProvider(this.seed, biomeProvider, biomesById, logger);

		// Setup noises
		Random random = new Random(seed);

		this.noiseSizeY = preset.getWorldConfig().getWorldHeightCap() / 8;

		this.interpolationNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-7, 0));
		this.lowerInterpolatedNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));
		this.upperInterpolatedNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));
		this.depthNoise = new OctavePerlinNoiseSampler(random, IntStream.rangeClosed(-15, 0));

		this.noiseCache = ThreadLocal.withInitial(() -> new NoiseCache(128, this.noiseSizeY + 1));

		this.biomeBlocksNoiseGen = new NoiseGeneratorPerlinMesaBlocks(random, 4);

		this.caves = new CaveCarver(Constants.WORLD_HEIGHT, preset.getWorldConfig());
		this.ravines = new RavineCarver(Constants.WORLD_HEIGHT, preset.getWorldConfig());

		this.oreVeinGenerator = preset.getWorldConfig().getLargeOreVeins() ? new OreVeinGenerator(seed) : null;
	}
	
	public ICachedBiomeProvider getCachedBiomeProvider()
	{
		return this.cachedBiomeProvider;
	}

	private static <T> T make(T object, Consumer<T> consumer)
	{
		consumer.accept(object);
		return object;
	}

	private static double getNoiseWeight(int x, int y, int z)
	{
		int arrayX = x + 12;
		int arrayZ = y + 12;
		int arrayY = z + 12;
		if (arrayX >= 0 && arrayX < 24)
		{
			if (arrayZ >= 0 && arrayZ < 24)
			{
				return arrayY >= 0 && arrayY < 24 ? (double) NOISE_WEIGHT_TABLE[arrayY * 24 * 24 + arrayX * 24 + arrayZ] : 0.0D;
			} else {
				return 0.0D;
			}
		} else {
			return 0.0D;
		}
	}

	private static double calculateNoiseWeight(int x, int y, int z)
	{
		// Make a circle cutout
		double sqrXZ = x * x + z * z;

		// Offset the y to prevent 0
		double offsetY = (double) y + 0.5D;

		// Square the y to make a
		double sqrY = offsetY * offsetY;

		// Get the density of the current position
		double density = Math.pow(Math.E, -(sqrY / 16.0D + sqrXZ / 16.0D));

		// Controls the density (bottom is solid, top is air)
		double yOffset = -offsetY * MathHelper.fastInverseSqrt(sqrY / 2.0D + sqrXZ / 2.0D) / 2.0D;

		// Multiply the density by the y offset to get the final density
		return yOffset * density;
	}

	private double sampleNoise(int x, int y, int z, double horizontalScale, double verticalScale, double horizontalStretch, double verticalStretch, double volatility1, double volatility2, double volatilityWeight1, double volatilityWeight2)
	{
		// The algorithm for noise generation varies slightly here as it calculates the interpolation first and then the interpolated noise to avoid sampling noise that will never be used.
		// The end result is ~2x faster terrain generation.

		double delta = getInterpolationNoise(x, y, z, horizontalStretch, verticalStretch);

		if (delta < volatilityWeight1)
		{
			return getInterpolatedNoise(this.lowerInterpolatedNoise, x, y, z, horizontalScale, verticalScale) / 512.0D * volatility1;
		}
		else if (delta > volatilityWeight2)
		{
			return getInterpolatedNoise(this.upperInterpolatedNoise, x, y, z, horizontalScale, verticalScale) / 512.0D * volatility2;
		} else {
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
		PerlinNoiseSampler interpolationSampler;
		for (int i = 0; i < 8; i++)
		{
			interpolationSampler = this.interpolationNoise.getOctave(i);
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
		double scaledX;
		double scaledY;
		double scaledZ;
		double scaledVerticalScale;
		PerlinNoiseSampler perlinNoiseSampler;
		for (int i = 0; i < Constants.CHUNK_SIZE; ++i)
		{
			scaledX = OctavePerlinNoiseSampler.maintainPrecision((double) x * horizontalScale * amplitude);
			scaledY = OctavePerlinNoiseSampler.maintainPrecision((double) y * verticalScale * amplitude);
			scaledZ = OctavePerlinNoiseSampler.maintainPrecision((double) z * horizontalScale * amplitude);
			scaledVerticalScale = verticalScale * amplitude;
			
			perlinNoiseSampler = sampler.getOctave(i);
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
		} else {
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
		IBiomeConfig center = this.cachedBiomeProvider.getNoiseBiomeConfig(noiseX, noiseZ, true);

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
		
		int radius = Math.max(center.getSmoothRadius(), center.getCHCSmoothRadius());
		int areaSize = radius * 2 + 1;
		IBiomeConfig biomes[] = this.cachedBiomeProvider.getNoiseBiomeConfigsForRegion(noiseX - radius, noiseZ - radius, areaSize);
		IBiomeConfig biome;
		float heightAt;
		float weightAt;
		int cacheX;
		int cacheZ;
		for (int x1 = -center.getSmoothRadius(); x1 <= center.getSmoothRadius(); ++x1)
		{
			cacheX = x1 + radius;
			for (int z1 = -center.getSmoothRadius(); z1 <= center.getSmoothRadius(); ++z1)
			{
				cacheZ = z1 + radius;
				biome = biomes[cacheX * areaSize + cacheZ];
				heightAt = biome.getBiomeHeight();
				// TODO: vanilla reduces the weight by half when the depth here is greater than the center depth, but OTG doesn't do that?
				weightAt = BIOME_WEIGHT_TABLE[x1 + 32 + (z1 + 32) * 65] / (heightAt + 2.0F);
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
			cacheX = x1 + radius;
			for (int z1 = -center.getCHCSmoothRadius(); z1 <= center.getCHCSmoothRadius(); ++z1)
			{
				cacheZ = z1 + radius;
				biome = biomes[cacheX * areaSize + cacheZ];

				heightAt = biome.getBiomeHeight();
				weightAt = BIOME_WEIGHT_TABLE[x1 + 32 + (z1 + 32) * 65] / (heightAt + 2.0F);
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
		
		double falloff;
		double horizontalScale;
		double verticalScale;
		double noise;
		for (int y = 0; y <= this.noiseSizeY; ++y)
		{
			// Calculate falloff
			falloff = (height - y) * 12.0D * 128.0D / this.preset.getWorldConfig().getWorldHeightCap() / volatility;
			if (falloff > 0.0)
			{
				falloff *= 4.0;
			}

			horizontalScale = WORLD_GEN_CONSTANT * horizontalFracture;
			verticalScale = WORLD_GEN_CONSTANT * verticalFracture;
			noise = sampleNoise(noiseX, y, noiseZ, horizontalScale, verticalScale, horizontalScale / 80, verticalScale / 160, volatility1, volatility2, volatilityWeight1, volatilityWeight2);

			if (!center.disableBiomeHeight())
			{
				// Add the falloff at this height
				noise += falloff;

				// Reduce the last 4 layers
				if (y > 28)
				{
					noise = MathHelper.clampedLerp(noise, -10, ((double) y - 28) / 4.0);
				}
			}

			// Add chc data
			noise += chc[y];

			// Store value
			noiseColumn[y] = noise;
		}
	}

	// Surface / ground / stone blocks / SAGC

	public void populateNoise(int worldHeightCap, Random random, ChunkBuffer buffer, ChunkCoordinate chunkCoord, ObjectList<JigsawStructureData> structures, ObjectList<JigsawStructureData> junctions)
	{
		ILogger logger = OTG.getEngine().getLogger();

		ObjectListIterator<JigsawStructureData> structureIterator = structures.iterator();
		ObjectListIterator<JigsawStructureData> junctionsIterator = junctions.iterator();

		long startTime = System.currentTimeMillis();

		OreVeinData data = this.oreVeinGenerator == null ? null : this.oreVeinGenerator.getForChunk(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
		
		// Fill waterLevel array, used when placing stone/ground/surface blocks.
		int[] waterLevel = new int[383];

		int blockX = chunkCoord.getBlockX();
		int blockZ = chunkCoord.getBlockZ();

		IBiome[] biomes = this.cachedBiomeProvider.getBiomesForChunk(chunkCoord);
		for (int x = 0; x < Constants.CHUNK_SIZE; x++)
		{
			for (int z = 0; z < Constants.CHUNK_SIZE; z++)
			{
				// TODO: water levels used to be interpolated via bilinear interpolation. Do we still need to do that?
				waterLevel[x * Constants.CHUNK_SIZE + z] = biomes[x * Constants.CHUNK_SIZE + z].getBiomeConfig().getWaterLevelMax();
			}
		}

		// TODO: this double[][][] is probably really bad for performance
		double[][][] noiseData = new double[2][this.noiseSizeZ + 1][this.noiseSizeY + 1];
//		double[][][][] oreVeinData
		// Max smoothing radius is 32, so area covered is 32+5+32=69 (noise/biome coords, so *4)
	
		// Initialize noise data on the x0 column.
		for (int noiseZ = 0; noiseZ < this.noiseSizeZ + 1; ++noiseZ)
		{
			noiseData[0][noiseZ] = new double[this.noiseSizeY + 1];
			this.getNoiseColumn(
				noiseData[0][noiseZ], 
				chunkCoord.getChunkX() * this.noiseSizeX, 
				chunkCoord.getChunkZ() * this.noiseSizeZ + noiseZ 
			);
			noiseData[1][noiseZ] = new double[this.noiseSizeY + 1];
		}

		IBiomeConfig biomeConfig;
		// [0, 4] -> x noise chunks
		int noiseZ;
		double x0z0y0;
		double x0z1y0;
		double x1z0y0;
		double x1z1y0;
		double x0z0y1;
		double x0z1y1;
		double x1z0y1;
		double x1z1y1;
		int realY;
		double yLerp;
		double x0z0;
		double x1z0;
		double x0z1;
		double x1z1;
		int realX;
		int localX;
		double xLerp;
		double z0;
		double z1;
		int realZ;
		int localZ;
		double zLerp;
		double rawNoise;
		double density;
		int structureX;
		int structureY;
		int structureZ;
		JigsawStructureData structure;
		JigsawStructureData junction;
		int sourceX;
		int sourceY;
		int sourceZ;
		double[][] xColumn;
		for (int noiseX = 0; noiseX < this.noiseSizeX; ++noiseX)
		{
			// Initialize noise data on the x1 column
			for (noiseZ = 0; noiseZ < this.noiseSizeZ + 1; ++noiseZ)
			{
				this.getNoiseColumn(
					noiseData[1][noiseZ], 
					chunkCoord.getChunkX() * this.noiseSizeX + noiseX + 1, 
					chunkCoord.getChunkZ() * this.noiseSizeZ + noiseZ 
				);
			}

			// [0, 4] -> z noise chunks
			for (noiseZ = 0; noiseZ < this.noiseSizeZ; ++noiseZ)
			{
				// [0, 32] -> y noise chunks
				for (int noiseY = this.noiseSizeY - 1; noiseY >= 0; --noiseY)
				{
					// Lower samples
					x0z0y0 = noiseData[0][noiseZ][noiseY];
					x0z1y0 = noiseData[0][noiseZ + 1][noiseY];
					x1z0y0 = noiseData[1][noiseZ][noiseY];
					x1z1y0 = noiseData[1][noiseZ + 1][noiseY];
					// Upper samples
					x0z0y1 = noiseData[0][noiseZ][noiseY + 1];
					x0z1y1 = noiseData[0][noiseZ + 1][noiseY + 1];
					x1z0y1 = noiseData[1][noiseZ][noiseY + 1];
					x1z1y1 = noiseData[1][noiseZ + 1][noiseY + 1];

					// [0, 8] -> y noise pieces
					for (int pieceY = 8 - 1; pieceY >= 0; --pieceY)
					{
						realY = noiseY * 8 + pieceY;

						// progress within loop
						yLerp = (double) pieceY / 8.0;

						// Interpolate noise data based on y progress
						x0z0 = MathHelper.lerp(yLerp, x0z0y0, x0z0y1);
						x1z0 = MathHelper.lerp(yLerp, x1z0y0, x1z0y1);
						x0z1 = MathHelper.lerp(yLerp, x0z1y0, x0z1y1);
						x1z1 = MathHelper.lerp(yLerp, x1z1y0, x1z1y1);

						// [0, 4] -> x noise pieces
						for (int pieceX = 0; pieceX < 4; ++pieceX)
						{
							realX = blockX + noiseX * 4 + pieceX;
							localX = realX & 15;
							xLerp = (double) pieceX / 4.0;
							// Interpolate noise based on x progress
							z0 = MathHelper.lerp(xLerp, x0z0, x1z0);
							z1 = MathHelper.lerp(xLerp, x0z1, x1z1);

							// [0, 4) -> z noise pieces
							for (int pieceZ = 0; pieceZ < 4; ++pieceZ)
							{
								realZ = blockZ + noiseZ * 4 + pieceZ;
								localZ = realZ & 15;
								zLerp = (double) pieceZ / 4.0;
								// Get the real noise here by interpolating the last 2 noises together
								rawNoise = MathHelper.lerp(zLerp, z0, z1);
								// Normalize the noise from (-256, 256) to [-1, 1]
								density = MathHelper.clamp(rawNoise / 200.0D, -1.0D, 1.0D);

								biomeConfig = biomes[localX * 16 + localZ].getBiomeConfig();

								// TODO: make this bigger and look better
								// Iterate through structures to add density
								structureX = 0;
								structureY = 0;
								structureZ = 0;
								for(density = density / 2.0D - density * density * density / 24.0D; structureIterator.hasNext(); density += getNoiseWeight(structureX, structureY, structureZ) * 0.8D)
								{
									structure = structureIterator.next();
									structureX = Math.max(0, Math.max(structure.minX - realX, realX - structure.maxX));
									structureY = realY - (structure.minY + (structure.useDelta ? structure.delta : 0));
									structureZ = Math.max(0, Math.max(structure.minZ - realZ, realZ - structure.maxZ));
								}
								structureIterator.back(structures.size());

								// Iterate through jigsawws to add density
								while(junctionsIterator.hasNext())
								{
									junction = junctionsIterator.next();
									sourceX = realX - junction.sourceX;
									sourceY = realY - junction.groundY;
									sourceZ = realZ - junction.sourceZ;
									density += getNoiseWeight(sourceX, sourceY, sourceZ) * 0.4D;
								}
								junctionsIterator.back(junctions.size());

								if (density > 0.0)
								{
									LocalMaterialData material = biomeConfig.getStoneBlockReplaced(realY);
									if (this.oreVeinGenerator != null) {
										LocalMaterialData ore = this.oreVeinGenerator.getMaterial(realX, realY, realZ, noiseX, noiseY, noiseZ, xLerp, yLerp, zLerp, data);

										if (ore != null) {
											material = ore;
										}
									}

									buffer.setBlock(localX, realY, localZ, material);
									buffer.setHighestBlockForColumn(pieceX + noiseX * 4, noiseZ * 4 + pieceZ, realY);
								}
								else if (realY < waterLevel[localX * 16 + localZ] && realY > biomeConfig.getWaterLevelMin())
								{
									buffer.setBlock(localX, realY, localZ, biomeConfig.getWaterBlockReplaced(realY));
									buffer.setHighestBlockForColumn(pieceX + noiseX * 4, noiseZ * 4 + pieceZ, realY);
								}
							}
						}
					}
				}
			}

			// Reuse noise data from the previous column for speed
			xColumn = noiseData[0];
			noiseData[0] = noiseData[1];
			noiseData[1] = xColumn;
		}
		// Deepslate - Frank
		// TODO: Change this from stone and deepslate noise
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				biomeConfig = biomes[x*16+z].getBiomeConfig();
				for (int y = 8; y > -65; y--) {
					// I think bedrock-like generation will do for this - Frank
					if (y > random.nextInt(8)) continue;
					buffer.setBlock(x, y, z, biomeConfig.getDefaultDeepslateBlock());
				}
			}
		}

		doSurfaceAndGroundControl(biomes, random, worldHeightCap, this.seed, buffer, waterLevel);
		
		if(logger.getLogCategoryEnabled(LogCategory.PERFORMANCE) && (System.currentTimeMillis() - startTime) > 50)
		{
			logger.log(LogLevel.WARN, LogCategory.PERFORMANCE, "Warning: Terrain generation for chunk at " + (chunkCoord.getBlockX() + DecorationArea.DECORATION_OFFSET) + " ~ " + (chunkCoord.getBlockZ() + DecorationArea.DECORATION_OFFSET) + " took " + (System.currentTimeMillis() - startTime) + " Ms.");
		}
	}

	public void carve(ChunkBuffer chunk, long seed, int chunkX, int chunkZ, BitSet carvingMask, boolean cavesEnabled, boolean ravinesEnabled)
	{
		// TODO: it should be possible to cache these carver graphs to make larger carvers more efficient and easier to use
		if(cavesEnabled || ravinesEnabled)
		{
			Random random = new Random();
			for (int localChunkX = chunkX - 8; localChunkX <= chunkX + 8; ++localChunkX)
			{
				for (int localChunkZ = chunkZ - 8; localChunkZ <= chunkZ + 8; ++localChunkZ)
				{
					setCarverSeed(random, seed, localChunkX, localChunkZ);
					
					if(cavesEnabled && this.caves.isStartChunk(random, localChunkX, localChunkZ))
					{
						this.caves.carve(this, chunk, random, localChunkX, localChunkZ, chunkX, chunkZ, carvingMask, this.cachedBiomeProvider);
					}
					
					setCarverSeed(random, seed, localChunkX, localChunkZ);
					
					if(ravinesEnabled && this.ravines.isStartChunk(random, localChunkX, localChunkZ))
					{
						this.ravines.carve(this, chunk, random, localChunkX, localChunkZ, chunkX, chunkZ, carvingMask, this.cachedBiomeProvider);
					}
				}
			}
		}
	}

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
		return noiseSizeY;
	}

	private void doSurfaceAndGroundControl(IBiome[] biomes, Random random, int heightCap, long worldSeed, ChunkBuffer chunkBuffer, int[] waterLevel)
	{
		// Process surface and ground blocks for each column in the chunk
		ChunkCoordinate chunkCoord = chunkBuffer.getChunkCoordinate();		
		double d1 = 0.03125D;
		this.biomeBlocksNoise.set(this.biomeBlocksNoiseGen.getRegion(this.biomeBlocksNoise.get(), chunkCoord.getBlockX(), chunkCoord.getBlockZ(), Constants.CHUNK_SIZE, Constants.CHUNK_SIZE, d1 * 2.0D, d1 * 2.0D, 1.0D));
		GeneratingChunk generatingChunk = new GeneratingChunk(random, waterLevel, this.biomeBlocksNoise.get(), heightCap);
		IBiome biome;
		for (int x = 0; x < Constants.CHUNK_SIZE; x++)
		{
			for (int z = 0; z < Constants.CHUNK_SIZE; z++)
			{
				// Get the current biome config and some properties
				biome = biomes[x * Constants.CHUNK_SIZE + z];
				biome.getBiomeConfig().doSurfaceAndGroundControl(worldSeed, generatingChunk, chunkBuffer, chunkCoord.getBlockX() + x, chunkCoord.getBlockZ() + z, biome);
			}
		}
	}
	
	// Used by sagc for generating surface/ground block patterns
	public double getBiomeBlocksNoiseValue(int blockX, int blockZ)
	{
		double noise = this.lastNoise.get();
		if (this.lastX.get() != blockX || this.lastZ.get() != blockZ)
		{
			double d1 = 0.03125D;
			noise = this.biomeBlocksNoiseGen.getRegion(new double[1], blockX, blockZ, 1, 1, d1 * 2.0D, d1 * 2.0D, 1.0D)[0];
			this.lastX.set(blockX);
			this.lastZ.set(blockZ);
			this.lastNoise.set(noise);
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

		public double[] get(double[] buffer, int noiseX, int noiseZ)
		{
			long key = key(noiseX, noiseZ);
			int idx = hash(key) & this.mask;

			// if the entry here has a key that matches ours, we have a cache hit
			if (this.keys[idx] == key)
			{
				// Copy values into buffer
				System.arraycopy(this.values, idx * buffer.length, buffer, 0, buffer.length);
			} else {
				// cache miss: sample and put the result into our cache entry

				// Sample the noise column to store the new values
				generateNoiseColumn(buffer, noiseX, noiseZ);

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
