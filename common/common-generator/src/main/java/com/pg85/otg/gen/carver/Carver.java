package com.pg85.otg.gen.carver;

import java.util.BitSet;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.ISurfaceGeneratorNoiseProvider;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.MutableBoolean;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

public abstract class Carver
{
	protected final int heightLimit;
	protected final IWorldConfig worldConfig;

	public Carver(int heightLimit, IWorldConfig worldConfig)
	{
		this.heightLimit = heightLimit;
		this.worldConfig = worldConfig;
	}

	public int getBranchFactor()
	{
		return 4;
	}

	protected boolean carveRegion(ISurfaceGeneratorNoiseProvider noiseProvider, float[] cache, ChunkBuffer chunkBuffer, long seed, int chunkX, int chunkZ, double x, double y, double z, double yaw, double pitch, BitSet carvingMask, ICachedBiomeProvider cachedBiomeProvider)
	{
		Random random = new Random(seed + (long) chunkX + (long) chunkZ);
		double d = chunkX * Constants.CHUNK_SIZE + DecorationArea.CARVER_OFFSET;
		double e = chunkZ * Constants.CHUNK_SIZE + DecorationArea.CARVER_OFFSET;
		boolean bl;
		IBiomeConfig[] biomeConfigs = cachedBiomeProvider.getBiomeConfigsForChunk(chunkBuffer.getChunkCoordinate());
		if (
			!(x < d - 16.0D - yaw * 2.0D) && 
			!(z < e - 16.0D - yaw * 2.0D) && 
			!(x > d + 16.0D + yaw * 2.0D) && 
			!(z > e + 16.0D + yaw * 2.0D)
		)
		{
			int i = Math.max(MathHelper.floor(x - yaw) - chunkX * Constants.CHUNK_SIZE - 1, 0);
			int j = Math.min(MathHelper.floor(x + yaw) - chunkX * Constants.CHUNK_SIZE + 1, Constants.CHUNK_SIZE);
			int k = Math.max(MathHelper.floor(y - pitch) - 1, 1);
			int l = Math.min(MathHelper.floor(y + pitch) + 1, this.heightLimit - 8);
			int m = Math.max(MathHelper.floor(z - yaw) - chunkZ * Constants.CHUNK_SIZE - 1, 0);
			int n = Math.min(MathHelper.floor(z + yaw) - chunkZ * Constants.CHUNK_SIZE + 1, Constants.CHUNK_SIZE);
			int worldX;
			double f;
			int worldZ;
			double g;
			double h;
			IBiomeConfig biomeConfig;
			MutableBoolean foundSurface;
			if (this.isRegionUncarvable(chunkBuffer, chunkX, chunkZ, i, j, k, l, m, n))
			{
				return false;
			} else {
				bl = false;
				for (int o = i; o < j; ++o)
				{
					worldX = o + chunkX * Constants.CHUNK_SIZE;
					f = ((double) worldX + 0.5D - x) / yaw;
					for (int q = m; q < n; ++q)
					{
						worldZ = q + chunkZ * Constants.CHUNK_SIZE;
						biomeConfig = biomeConfigs[o * Constants.CHUNK_SIZE + q];
						g = ((double) worldZ + 0.5D - z) / yaw;
						if (!(f * f + g * g >= 1.0D))
						//if (f * f + g * g < 1.0D)
						{
							foundSurface = new MutableBoolean(false);
							for (int s = l; s > k; --s)
							{
								h = ((double) s - 0.5D - y) / pitch;
								if (!this.isPositionExcluded(cache, f, h, g, s))
								{								
									bl |= this.carveAtPoint(noiseProvider, chunkBuffer, carvingMask, random, biomeConfig.getWaterLevelMax(), chunkX, chunkZ, worldX, worldZ, o, s, q, foundSurface, biomeConfig);
								}
							}
						}
					}
				}
				return bl;
			}
		} else {
			return false;
		}
	}

	protected boolean carveAtPoint(ISurfaceGeneratorNoiseProvider noiseProvider, ChunkBuffer chunkBuffer, BitSet carvingMask, Random random, int seaLevel, int mainChunkX, int mainChunkZ, int worldX, int worldZ, int relativeX, int y, int relativeZ, MutableBoolean foundSurface, IBiomeConfig biomeConfig)
	{
		int i = relativeX | relativeZ << 4 | y << 8;
		if (carvingMask.get(i))
		{
			return false;
		} else {
			carvingMask.set(i);

			LocalMaterialData material = chunkBuffer.getBlock(worldX, y, worldZ);
			if(material.isNonCaveAir() || material.isMaterial(LocalMaterials.WATER))
			{
				return false;
			}

			LocalMaterialData blockAbove = chunkBuffer.getBlock(worldX, y + 1, worldZ);

			// Check to see if we've found the surface, if so place surfaceblocks.
			// TODO: Search a larger height up instead of just the current carving sphere?
			// Vanilla logic
			// Normally doesn't see sand as surface?
			if(material.isMaterial(biomeConfig.getSurfaceBlockAtHeight(noiseProvider, worldX, y - 1, worldZ)))
			{
				foundSurface.setValue(true);
			}				
			if (material.isSolid() && !blockAbove.isMaterial(LocalMaterials.WATER))
			{
				if (y <= this.worldConfig.getCarverLavaBlockHeight())
				{
					// Not sure Why world coords are passed to chunkbuffer, it just does >> 4.
					chunkBuffer.setBlock(worldX, y, worldZ, this.worldConfig.getCarverLavaBlock());
				} else {
					chunkBuffer.setBlock(worldX, y, worldZ, LocalMaterials.CAVE_AIR);
					if(foundSurface.isValue())
					{
						LocalMaterialData blockBelow = chunkBuffer.getBlock(worldX, y - 1, worldZ);
						if(blockBelow.isMaterial(biomeConfig.getGroundBlockAtHeight(noiseProvider, worldX, y - 1, worldZ)))
						{
							chunkBuffer.setBlock(worldX, y - 1, worldZ, biomeConfig.getSurfaceBlockAtHeight(noiseProvider, worldX, y - 1, worldZ));
						}
					}
				}			
			} else {
				return false;
			}

			return true;
		}
	}

	protected boolean isRegionUncarvable(ChunkBuffer chunk, int mainChunkX, int mainChunkZ, int relMinX, int relMaxX, int minY, int maxY, int relMinZ, int relMaxZ)
	{
		for (int i = relMinX; i < relMaxX; ++i)
		{
			for (int j = relMinZ; j < relMaxZ; ++j)
			{
				for (int k = minY - 1; k <= maxY + 1; ++k)
				{
					if (chunk.getBlock(i + mainChunkX * Constants.CHUNK_SIZE, k, j + mainChunkZ * Constants.CHUNK_SIZE).isMaterial(LocalMaterials.WATER))
					{
						return true;
					}

					if (k != maxY + 1 && !this.isOnBoundary(relMinX, relMaxX, relMinZ, relMaxZ, i, j))
					{
						k = maxY;
					}
				}
			}
		}

		return false;
	}

	private boolean isOnBoundary(int minX, int maxX, int minZ, int maxZ, int x, int z)
	{
		return x == minX || x == maxX - 1 || z == minZ || z == maxZ - 1;
	}

	protected boolean canCarveBranch(int mainChunkX, int mainChunkZ, double x, double z, int branch, int branchCount, float baseWidth)
	{
		double d = mainChunkX * Constants.CHUNK_SIZE + DecorationArea.CARVER_OFFSET;
		double e = mainChunkZ * Constants.CHUNK_SIZE + DecorationArea.CARVER_OFFSET;
		double f = x - d;
		double g = z - e;
		double h = branchCount - branch;
		double i = baseWidth + 2.0F + 16.0F;
		return f * f + g * g - h * h <= i * i;
	}

	public abstract boolean carve(ISurfaceGeneratorNoiseProvider noiseProvider, ChunkBuffer chunk, Random random, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet carvingMask, ICachedBiomeProvider cachedBiomeProvider);

	public abstract boolean isStartChunk(Random random, int chunkX, int chunkZ);

	protected abstract boolean isPositionExcluded(float[] cache, double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y);
}
