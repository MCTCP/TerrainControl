package com.pg85.otg.util;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.helpers.MathHelper;

/**
 * Position of a chunk.
 *
 * <p>Minecraft uses two coordinate systems: chunk coords and block coords.
 * It is important that the two are kept separate. This class should be used
 * whenever a chunk coordinate is passed along, so that it is clear that chunk
 * coordinates are used, and not block coordinates.
 *
 * <p>This class contains some helper methods to switch from/to block
 * coordinates.
 */
public class ChunkCoordinate
{
	public static final int CHUNK_SIZE = 16;
	public static final int CHUNK_Y_SIZE = 256;
	private static final int CHUNK_POPULATION_OFFSET_X = CHUNK_SIZE / 2 - 1;
	private static final int CHUNK_POPULATION_OFFSET_Z = CHUNK_SIZE / 2 - 1;

	private final int chunkX;
	private final int chunkZ;

	private ChunkCoordinate(int chunkX, int chunkZ)
	{
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
	}

	/**
	 * Gets the x position of the chunk in the world.
	 * @return The x position.
	 */
	public int getChunkX()
	{
		return chunkX;
	}

	/**
	 * Gets the z position of the chunk in the world.
	 * @return The z position.
	 */
	public int getChunkZ()
	{
		return chunkZ;
	}

	@Override
	public int hashCode()
	{
		return (chunkX >> 13) ^ chunkZ;
	}

	@Override
	public boolean equals(Object otherObject)
	{
		if (otherObject == this)
		{
			return true;
		}
		if (otherObject == null)
		{
			return false;
		}
		if (!(otherObject instanceof ChunkCoordinate))
		{
			return false;
		}
		ChunkCoordinate otherChunkCoordinate = (ChunkCoordinate) otherObject;
		if (otherChunkCoordinate.chunkX != chunkX)
		{
			return false;
		}
		if (otherChunkCoordinate.chunkZ != chunkZ)
		{
			return false;
		}
		return true;
	}

	/**
	 * Gets the coordinates of the chunk that is responsible for populating
	 * the given block.
	 *
	 * <p>During terrain population, these four chunks are
	 * guaranteed to be loaded when the top-left chunk is being populated:
	 * <pre>
	 * +--------+--------+ . = no changes in blocks for now
	 * |........|........| # = blocks are replaced
	 * |....####|####....|
	 * |....####|####....|
	 * +--------+--------+
	 * |....####|####....|
	 * |....####|####....|
	 * |........|........|
	 * +--------+--------+
	 * </pre>
	 * This offset makes it possible for objects like trees to extend a little
	 * bit outside the area marked with <code>#</code> without hitting
	 * unloaded chunks.
	 *
	 * <p>This method essentially returns the top left chunk for the whole
	 * area marked with <code>#</code>, even though only 1/4 of that area is
	 * actually in the top left chunk.
	 *
	 * @param blockX X coordinate of the block.
	 * @param blockZ Z coordinate of the block.
	 * @return The coordinates of the chunk.
	 */
	public static ChunkCoordinate getPopulatingChunk(int blockX, int blockZ)
	{
		// Because of the way Minecraft population works, objects should never
		// be placed in the bottom left corner of a chunk. That's why this
		// formula looks a bit overly complicated. <-- TODO: Why should objects never be placed in the bottom-left corner of a chunk?
		return new ChunkCoordinate(
			(blockX - CHUNK_POPULATION_OFFSET_X) >> 4,
			(blockZ - CHUNK_POPULATION_OFFSET_Z) >> 4
		);
	}

	/**
	 * Gets the coordinates of the chunk that will contain the given block.
	 * @param blockX The x position of the block.
	 * @param blockZ The z position of the block.
	 * @return The coordinates.
	 */
	public static ChunkCoordinate fromBlockCoords(int blockX, int blockZ)
	{
		return new ChunkCoordinate(blockX >> 4, blockZ >> 4);
	}

	public static ChunkCoordinate fromChunkCoords(int chunkX, int chunkZ)
	{
		return new ChunkCoordinate(chunkX, chunkZ);
	}
	
	public ChunkCoordinate toRegionCoord()
	{
		return ChunkCoordinate.fromChunkCoords(
			MathHelper.floor((double)getChunkX() / (double)Constants.REGION_SIZE), 
			MathHelper.floor((double)getChunkZ() / (double)Constants.REGION_SIZE)
		);
	}

	public int getRegionInternalX()
	{
		return MathHelper.mod(getChunkX(), Constants.REGION_SIZE);
	}
	
	public int getRegionInternalZ()
	{
		return MathHelper.mod(getChunkZ(), Constants.REGION_SIZE);
	}
	
	@Override
	public String toString()
	{
		return chunkX + "," + chunkZ;
	}

	/**
	 * Gets the x position of the block in the center of this chunk.
	 * @return The x position.
	 */
	public int getBlockXCenter() {
		return chunkX * CHUNK_SIZE + CHUNK_POPULATION_OFFSET_X;
	}
	
	/**
	 * Gets the z position of the block in the center of this chunk.
	 * @return The z position.
	 */
	public int getBlockZCenter() {
		return chunkZ * CHUNK_SIZE + CHUNK_POPULATION_OFFSET_Z;
	}

	/**
	 * Gets the x position of the block with the lowest x coordinate that is
	 * still in this chunk.
	 * @return The x position of the block.
	 */
	public int getBlockX()
	{
		return chunkX * CHUNK_SIZE;
	}

	/**
	 * Gets the z position of the block with the lowest z coordinate that is
	 * still in this chunk.
	 * @return The z position of the block.
	 */
	public int getBlockZ()
	{
		return chunkZ * CHUNK_SIZE;
	}

	/**
	 * Gets whether the given chunk coordinates match the chunk x and chunk z
	 * of this chunk.
	 * @param chunkX The chunk x to check.
	 * @param chunkZ The chunk z to check.
	 * @return True if the coordinates match, false otherwise.
	 */
	public boolean coordsMatch(int chunkX, int chunkZ)
	{
		return this.chunkX == chunkX && this.chunkZ == chunkZ;
	}
	
	public static boolean IsInAreaBeingPopulated(int blockX, int blockZ, ChunkCoordinate chunkBeingPopulated)
	{
		return
			(
				blockX >= chunkBeingPopulated.getBlockX() &&
				blockX < chunkBeingPopulated.getBlockX() + (CHUNK_SIZE * 2)
			) && (
				blockZ >= chunkBeingPopulated.getBlockZ() &&
				blockZ < chunkBeingPopulated.getBlockZ() + (CHUNK_SIZE * 2)
			)
		;		
	}
}
