package com.pg85.otg.util.gen;

import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.materials.LocalMaterialData;

/**
 * A chunk buffer holds all blocks of a chunk. It is not part of the world.
 *
 */
public abstract class ChunkBuffer
{

	/**
     * Gets the chunk coordinate of this buffer.
     * 
     * @return The chunk coordinate.
     */
	public abstract ChunkCoordinate getChunkCoordinate();

    /**
     * Sets the block at the given location in the chunk. The implementation may
     * ignore block data or may ignore blocks with ids larger than 255.
     *
     * @param blockX Block x, from 0 to ({@link ChunkCoordinate#CHUNK_SIZE}
     *               - 1), inclusive.
     * @param blockY Block y, from 0 to ({@link ChunkCoordinate#CHUNK_Y_SIZE}
     *               - 1), inclusive.
     * @param blockZ Block z, from 0 to ({@link ChunkCoordinate#CHUNK_SIZE}
     *               - 1), inclusive.
     * @param material The material to set the block to.
     */
    public abstract void setBlock(int blockX, int blockY, int blockZ, LocalMaterialData material);

    /**
     * Gets the block material at the given position.
     * @param blockX Block x, from 0 to ({@link ChunkCoordinate#CHUNK_SIZE}
     *               - 1), inclusive.
     * @param blockY Block y, from 0 to ({@link ChunkCoordinate#CHUNK_Y_SIZE}
     *               - 1), inclusive.
     * @param blockZ Block z, from 0 to ({@link ChunkCoordinate#CHUNK_SIZE}
     *               - 1), inclusive.
     * @return The block material.
     */
    public abstract LocalMaterialData getBlock(int blockX, int blockY, int blockZ);

    private final short[] highestBlockHeight = new short[ChunkCoordinate.CHUNK_SIZE * ChunkCoordinate.CHUNK_SIZE];
	public int getHighestBlockForColumn(int blockX, int blockZ)
	{
		return highestBlockHeight[blockX * ChunkCoordinate.CHUNK_SIZE + blockZ];
	}

	public void setHighestBlockForColumn(int blockX, int blockZ, int height)
	{
		if(height > highestBlockHeight[blockX * ChunkCoordinate.CHUNK_SIZE + blockZ])
		{
			highestBlockHeight[blockX * ChunkCoordinate.CHUNK_SIZE + blockZ] = (short)height;
		}
	}
}
