package com.pg85.otg.util.gen;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.ChunkCoordinate;

import java.util.Random;

/**
 * Holds early generator information about a chunk, like water levels, noise
 * values, etc.
 */
public final class GeneratingChunk
{

	private static final int BEDROCK_LAYER_HEIGHT = 5;

	public final int heightCap;
	public final Random random;
	private final int[] waterLevel;
	private final double[] surfaceNoise;

	public GeneratingChunk(Random random, int[] waterLevel, double[] surfaceNoise, int heightCap)
	{
		this.random = random;
		this.waterLevel = waterLevel;
		this.surfaceNoise = surfaceNoise;
		this.heightCap = heightCap;
	}

	/**
	 * Gets the surface noise value at the given position.
	 * 
	 * @param x X position, 0 <= x < {@value ChunkCoordinate#CHUNK_SIZE}.
	 * @param z Z position, 0 <= z < {@value ChunkCoordinate#CHUNK_SIZE}.
	 * @return The surface noise value.
	 */
	public double getNoise(int x, int z)
	{
		return this.surfaceNoise[x + z * Constants.CHUNK_SIZE];
	}

	/**
	 * Gets the water level at the given position.
	 * 
	 * @param x X position, 0 <= x < {@value ChunkCoordinate#CHUNK_SIZE}.
	 * @param z Z position, 0 <= z < {@value ChunkCoordinate#CHUNK_SIZE}.
	 * @return The water level.
	 */
	public int getWaterLevel(int x, int z)
	{
		return this.waterLevel[z + x * Constants.CHUNK_SIZE];
	}

	/**
	 * Gets whether bedrock should be created at the given position.
	 *
	 * @param worldConfig The worldConfig, for bedrock settings.
	 * @param y			The y position.
	 * @return True if bedrock should be created, false otherwise.
	 */
	public boolean mustCreateBedrockAt(boolean flatBedrock, boolean disableBedrock, boolean ceilingBedrock, int y)
	{
		// The "- 2" that appears in this method, comes from that heightCap -
		// 1 is the highest place where a block can be placed, and heightCap -
		// 2 is the highest place where bedrock can be generated to make sure
		// there are no light glitches - see #117

		// Handle flat bedrock
		if (flatBedrock)
		{
			if (!disableBedrock && y == 0)
			{
				return true;
			}
			if (ceilingBedrock && y >= this.heightCap - 1)
			{
				return true;
			}
			return false;
		}

		// Otherwise we have normal bedrock
		if (!disableBedrock && y < 5)
		{
			return y <= this.random.nextInt(BEDROCK_LAYER_HEIGHT);
		}
		if (ceilingBedrock)
		{
			int amountBelowHeightCap = this.heightCap - y - 1;
			if (amountBelowHeightCap < 0 || amountBelowHeightCap > BEDROCK_LAYER_HEIGHT)
			{
				return false;
			}

			return amountBelowHeightCap <= this.random.nextInt(BEDROCK_LAYER_HEIGHT);
		}
		return false;
	} 
}
