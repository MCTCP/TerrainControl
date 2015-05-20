package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.util.ChunkCoordinate;

/**
 * A chunk buffer holds all blocks of a chunk. It is not part of the world.
 *
 */
public interface ChunkBuffer {

    /**
     * Gets the chunk coordinate of this buffer.
     * 
     * @return The chunk coordinate.
     */
    ChunkCoordinate getChunkCoordinate();

    /**
     * Sets the block at the given location in the chunk. The implementation may
     * ignore block data or may ignore blocks with ids larger than 255.
     *
     * @param blockX Block x, from 0 to ({@link ChunkCoordinate#CHUNK_X_SIZE}
     *               - 1), inclusive.
     * @param blockY Block y, from 0 to ({@link ChunkCoordinate#CHUNK_Y_SIZE}
     *               - 1), inclusive.
     * @param blockZ Block z, from 0 to ({@link ChunkCoordinate#CHUNK_Z_SIZE}
     *               - 1), inclusive.
     * @param material The material to set the block to.
     */
    void setBlock(int blockX, int blockY, int blockZ, LocalMaterialData material);

    /**
     * Gets the block material at the given position.
     * @param blockX Block x, from 0 to ({@link ChunkCoordinate#CHUNK_X_SIZE}
     *               - 1), inclusive.
     * @param blockY Block y, from 0 to ({@link ChunkCoordinate#CHUNK_Y_SIZE}
     *               - 1), inclusive.
     * @param blockZ Block z, from 0 to ({@link ChunkCoordinate#CHUNK_Z_SIZE}
     *               - 1), inclusive.
     * @return The block material.
     */
    LocalMaterialData getBlock(int blockX, int blockY, int blockZ);
}
