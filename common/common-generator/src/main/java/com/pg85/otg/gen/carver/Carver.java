package com.pg85.otg.gen.carver;

import java.util.BitSet;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.MutableBoolean;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.interfaces.IWorldConfig;
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

	protected boolean carveRegion(ChunkBuffer chunk, long seed, int seaLevel, int chunkX, int chunkZ, double x, double y, double z, double yaw, double pitch, BitSet carvingMask)
	{
		Random random = new Random(seed + (long) chunkX + (long) chunkZ);
		double d = chunkX * 16 + DecorationArea.CARVER_OFFSET;
		double e = chunkZ * 16 + DecorationArea.CARVER_OFFSET;
		if (x >= d - 16.0D - yaw * 2.0D && z >= e - 16.0D - yaw * 2.0D && x <= d + 16.0D + yaw * 2.0D && z <= e + 16.0D + yaw * 2.0D)
		{
			int i = Math.max(MathHelper.floor(x - yaw) - chunkX * 16 - 1, 0);
			int j = Math.min(MathHelper.floor(x + yaw) - chunkX * 16 + 1, 16);
			int k = Math.max(MathHelper.floor(y - pitch) - 1, 1);
			int l = Math.min(MathHelper.floor(y + pitch) + 1, this.heightLimit - 8);
			int m = Math.max(MathHelper.floor(z - yaw) - chunkZ * 16 - 1, 0);
			int n = Math.min(MathHelper.floor(z + yaw) - chunkZ * 16 + 1, 16);
			if (this.isRegionUncarvable(chunk, chunkX, chunkZ, i, j, k, l, m, n))
			{
				return false;
			} else
			{
				boolean bl = false;

				for (int o = i; o < j; ++o)
				{
					int p = o + chunkX * 16;
					double f = ((double) p + 0.5D - x) / yaw;

					for (int q = m; q < n; ++q)
					{
						int r = q + chunkZ * 16;
						double g = ((double) r + 0.5D - z) / yaw;
						if (f * f + g * g < 1.0D)
						{
							MutableBoolean foundSurface = new MutableBoolean(false);

							for (int s = l; s > k; --s)
							{
								double h = ((double) s - 0.5D - y) / pitch;
								if (!this.isPositionExcluded(f, h, g, s))
								{
									bl |= this.carveAtPoint(chunk, carvingMask, random, seaLevel, chunkX, chunkZ, p, r, o, s, q, foundSurface);
								}
							}
						}
					}
				}

				return bl;
			}
		} else
		{
			return false;
		}
	}

	protected boolean carveAtPoint(ChunkBuffer chunk, BitSet carvingMask, Random random, int seaLevel, int mainChunkX, int mainChunkZ, int x, int z, int relativeX, int y, int relativeZ, MutableBoolean foundSurface)
	{
		int i = relativeX | relativeZ << 4 | y << 8;
		if (carvingMask.get(i))
		{
			return false;
		} else
		{
			carvingMask.set(i);

			// Check to see if we've found the surface.
			// TODO: check the biome's top block instead of just grass, and search a larger height up instead of just the current carving sphere
			if (chunk.getBlock(x, y, z).isMaterial(LocalMaterials.GRASS))
			{
				foundSurface.setValue(true);
			}

			// If there is already air here, we don't carve - don't want an ocean of lava under Skylands
			// Skip bedrock carving as well
			LocalMaterialData data = chunk.getBlock(x, y, z);

			if (data.isMaterial(LocalMaterials.AIR) || data.isMaterial(LocalMaterials.BEDROCK))
			{
				return false;
			}
			if (y < 11)
			{
				chunk.setBlock(x, y, z, LocalMaterials.LAVA);
			} else
			{
				// TODO: should be cave_air
				chunk.setBlock(x, y, z, LocalMaterials.AIR);
			}

			// If we found the surface and the below block is dirt, set the below block to grass
			if (foundSurface.isValue() && chunk.getBlock(x, y - 1, z).isMaterial(LocalMaterials.DIRT))
			{
				chunk.setBlock(x, y - 1, z, LocalMaterials.GRASS);
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
					if (chunk.getBlock(i + mainChunkX * 16, k, j + mainChunkZ * 16).isMaterial(LocalMaterials.WATER))
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
		double d = mainChunkX * 16 + DecorationArea.CARVER_OFFSET;
		double e = mainChunkZ * 16 + DecorationArea.CARVER_OFFSET;
		double f = x - d;
		double g = z - e;
		double h = branchCount - branch;
		double i = baseWidth + 2.0F + 16.0F;
		return f * f + g * g - h * h <= i * i;
	}

	public abstract boolean carve(ChunkBuffer chunk, Random random, int seaLevel, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet carvingMask);

	public abstract boolean shouldCarve(Random random, int chunkX, int chunkZ);

	protected abstract boolean isPositionExcluded(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y);
}
