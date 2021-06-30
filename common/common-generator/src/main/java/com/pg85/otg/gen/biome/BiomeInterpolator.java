package com.pg85.otg.gen.biome;

import com.pg85.otg.gen.biome.layers.LayerSource;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.util.helpers.MathHelper;

/**
 * Interpolates the given biome from biome coords (pos >> 2) to real coords.
 * This is required as a vanilla change in 1.15 changed biomes from being stored in real resolution, changing them to be
 * stored in a 4x4x4 cubes instead, allowing for 3d biomes at the cost of resolution. This class interpolates and provides
 * a rough estimation of the correct biome at the given world coords.
 */
public class BiomeInterpolator
{
	public static IBiomeConfig getConfig(long seed, int x, int y, int z, LayerSource generator)
	{
		long pos = sample(seed, x, y, z);
		return generator.getConfig(MathHelper.getXFromLong(pos), MathHelper.getZFromLong(pos));
	}

	public static String getBiomeRegistryName(long seed, int x, int y, int z, LayerSource generator)
	{
		long pos = sample(seed, x, y, z);
		return generator.getBiomeRegistryName(MathHelper.getXFromLong(pos), 0, MathHelper.getZFromLong(pos));
	}

	public static int getId(long seed, int x, int y, int z, LayerSource generator)
	{
		long pos = sample(seed, x, y, z);
		return generator.getSampler().sample(MathHelper.getXFromLong(pos), MathHelper.getZFromLong(pos));
	}

	private static long sample(long seed, int x, int y, int z)
	{
		int startX = x - 2;
		int startY = y - 2;
		int startZ = z - 2;
		
		int chunkX = startX >> 2;
		int chunkY = startY >> 2;
		int chunkZ = startZ >> 2;
		
		double localX = (double) (startX & 3) / 4.0D;
		double localY = (double) (startY & 3) / 4.0D;
		double localZ = (double) (startZ & 3) / 4.0D;
		
		double maxDistance = Double.MAX_VALUE;
		int idx = Integer.MIN_VALUE;
		
		for (int i = 0; i < 8; ++i)
		{
			boolean isX = (i & 4) == 0;
			boolean isY = (i & 2) == 0;
			boolean isZ = (i & 1) == 0;
			
			int lerpX = isX ? chunkX : chunkX + 1;
			int lerpY = isY ? chunkY : chunkY + 1;
			int lerpZ = isZ ? chunkZ : chunkZ + 1;
			
			double xFraction = isX ? localX : localX - 1.0D;
			double yFraction = isY ? localY : localY - 1.0D;
			double zFraction = isZ ? localZ : localZ - 1.0D;
			
			double distance = calcSquaredDistance(seed, lerpX, lerpY, lerpZ, xFraction, yFraction, zFraction);
			
			if (maxDistance > distance)
			{
				maxDistance = distance;
				idx = i;
			}
		}
		
		int finalX = (idx & 4) == 0 ? chunkX : chunkX + 1;
		// int finalY = (idx & 2) == 0 ? chunkY : chunkY + 1; // y coord is not used currently
		int finalZ = (idx & 1) == 0 ? chunkZ : chunkZ + 1;
		
		return MathHelper.toLong(finalX, finalZ);
	}

	private static double calcSquaredDistance(long seed, int x, int y, int z, double xFraction, double yFraction, double zFraction)
	{
		long mixedSeed = MathHelper.mixSeed(seed, x);
		mixedSeed = MathHelper.mixSeed(mixedSeed, y);
		mixedSeed = MathHelper.mixSeed(mixedSeed, z);
		mixedSeed = MathHelper.mixSeed(mixedSeed, x);
		mixedSeed = MathHelper.mixSeed(mixedSeed, y);
		mixedSeed = MathHelper.mixSeed(mixedSeed, z);
		double xOffset = distribute(mixedSeed);
		mixedSeed = MathHelper.mixSeed(mixedSeed, seed);
		double yOffset = distribute(mixedSeed);
		mixedSeed = MathHelper.mixSeed(mixedSeed, seed);
		double zOffset = distribute(mixedSeed);
		return square(zFraction + zOffset) + square(yFraction + yOffset) + square(xFraction + xOffset);
	}

	private static double distribute(long seed)
	{
		double d = (double) ((int) (seed >> 24) & 1023) / 1024.0D;
		return (d - 0.5D) * 0.9D;
	}

	private static double square(double d)
	{
		return d * d;
	}
}
