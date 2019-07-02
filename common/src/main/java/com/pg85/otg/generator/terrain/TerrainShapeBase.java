package com.pg85.otg.generator.terrain;

import com.pg85.otg.util.ChunkCoordinate;

/**
 * Some constants on the terrain shape.
 */
public abstract class TerrainShapeBase
{
    /**
     * The size in blocks of a noise piece in the y direction.
     */
    public static final int PIECE_Y_SIZE = 8;

    /**
     * The amount of noise pieces that fit inside a chunk on the y axis.
     */
    public static final int PIECES_PER_CHUNK_Y = ChunkCoordinate.CHUNK_Y_SIZE / PIECE_Y_SIZE;
}
