package com.pg85.otg.gen.carver;

import java.util.BitSet;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
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

	protected boolean carveRegion(ChunkBuffer chunkBuffer, long seed, int seaLevel, int chunkX, int chunkZ, double x, double y, double z, double yaw, double pitch, BitSet carvingMask, ICachedBiomeProvider cachedBiomeProvider)
	{
		Random random = new Random(seed + (long) chunkX + (long) chunkZ);
		double d = chunkX * Constants.CHUNK_SIZE + DecorationArea.CARVER_OFFSET;
		double e = chunkZ * Constants.CHUNK_SIZE + DecorationArea.CARVER_OFFSET;
		boolean bl;
		IBiomeConfig[] biomeConfigs = cachedBiomeProvider.getBiomeConfigsForChunk(chunkBuffer.getChunkCoordinate());
		if (x >= d - 16.0D - yaw * 2.0D && z >= e - 16.0D - yaw * 2.0D && x <= d + 16.0D + yaw * 2.0D && z <= e + 16.0D + yaw * 2.0D)
		{
			int i = Math.max(MathHelper.floor(x - yaw) - chunkX * 16 - 1, 0);
			int j = Math.min(MathHelper.floor(x + yaw) - chunkX * 16 + 1, 16);
			int k = Math.max(MathHelper.floor(y - pitch) - 1, 1);
			int l = Math.min(MathHelper.floor(y + pitch) + 1, this.heightLimit - 8);
			int m = Math.max(MathHelper.floor(z - yaw) - chunkZ * 16 - 1, 0);
			int n = Math.min(MathHelper.floor(z + yaw) - chunkZ * 16 + 1, 16);
			int chunkBufferInternalX;
			double f;
			int chunkBufferInternalZ;
			double g;
			MutableBoolean foundSurface;
			double h;
			IBiomeConfig biomeConfig;
			if (this.isRegionUncarvable(chunkBuffer, chunkX, chunkZ, i, j, k, l, m, n))
			{
				return false;
			} else {
				bl = false;
				for (int o = i; o < j; ++o)
				{
					chunkBufferInternalX = o + chunkX * 16;
					f = ((double) chunkBufferInternalX + 0.5D - x) / yaw;
					for (int q = m; q < n; ++q)
					{
						chunkBufferInternalZ = q + chunkZ * 16;

						// Chunkbuffer appears to accept both world and internal (0-15) coordinates,
						// and is using world coordinates here, so do & 0xf to get 0-15.
						biomeConfig = biomeConfigs[(chunkBufferInternalX & 0xf) * Constants.CHUNK_SIZE + (chunkBufferInternalZ & 0xf)];							
						
						g = ((double) chunkBufferInternalZ + 0.5D - z) / yaw;
						if (f * f + g * g < 1.0D)
						{
							foundSurface = new MutableBoolean(false);
							for (int s = l; s > k; --s)
							{
								h = ((double) s - 0.5D - y) / pitch;
								if (!this.isPositionExcluded(f, h, g, s))
								{								
									bl |= this.carveAtPoint(chunkBuffer, carvingMask, random, seaLevel, chunkX, chunkZ, chunkBufferInternalX, chunkBufferInternalZ, o, s, q, foundSurface, biomeConfig);
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

	protected boolean carveAtPoint(ChunkBuffer chunkBuffer, BitSet carvingMask, Random random, int seaLevel, int mainChunkX, int mainChunkZ, int chunkBufferInternalX, int chunkBufferInternalZ, int relativeX, int y, int relativeZ, MutableBoolean foundSurface, IBiomeConfig biomeConfig)
	{
		int i = relativeX | relativeZ << 4 | y << 8;
		if (carvingMask.get(i))
		{
			return false;
		} else {
			carvingMask.set(i);

			// TODO: check the biome's top block instead of just grass, and search a larger height up instead of just the current carving sphere
			// Check to see if we've found the surface.
			if (chunkBuffer.getBlock(chunkBufferInternalX, y, chunkBufferInternalZ).isMaterial(biomeConfig.getSurfaceBlockReplaced(y)))
			{
				foundSurface.setValue(true);
			}

			// If there is already air here, we don't carve - don't want an ocean of lava under Skylands
			// Skip bedrock carving as well
			LocalMaterialData data = chunkBuffer.getBlock(chunkBufferInternalX, y, chunkBufferInternalZ);

			if (data.isAir() || data.isMaterial(this.worldConfig.getDefaultBedrockBlock()))
			{
				return false;
			}
			if (y <= this.worldConfig.getCarverLavaBlockHeight())
			{
				chunkBuffer.setBlock(chunkBufferInternalX, y, chunkBufferInternalZ, this.worldConfig.getCarverLavaBlock());
			} else {
				chunkBuffer.setBlock(chunkBufferInternalX, y, chunkBufferInternalZ, LocalMaterials.CAVE_AIR);
			}

			// If we found the surface and the below block is dirt, set the below block to grass
			if (foundSurface.isValue() && chunkBuffer.getBlock(chunkBufferInternalX, y - 1, chunkBufferInternalZ).isMaterial(biomeConfig.getGroundBlockReplaced(y - 1)))
			{
				chunkBuffer.setBlock(chunkBufferInternalX, y - 1, chunkBufferInternalZ, biomeConfig.getSurfaceBlockReplaced(y - 1));
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
					if (chunk.getBlock(i + mainChunkX * 16, k, j + mainChunkZ * 16).isLiquid())
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

	public abstract boolean carve(ChunkBuffer chunk, Random random, int seaLevel, int chunkX, int chunkZ, int mainChunkX, int mainChunkZ, BitSet carvingMask, ICachedBiomeProvider cachedBiomeProvider);

	public abstract boolean shouldCarve(Random random, int chunkX, int chunkZ);

	protected abstract boolean isPositionExcluded(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y);
}
