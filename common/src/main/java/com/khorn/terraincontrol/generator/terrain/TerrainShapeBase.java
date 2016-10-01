package com.khorn.terraincontrol.generator.terrain;

import com.khorn.terraincontrol.util.ChunkCoordinate;

/**
 * Some constants on the terrain shape.
 */
public abstract class TerrainShapeBase
{
    /**
     * The size in blocks of a noise piece in the x direction.
     */
    public static final int PIECE_X_SIZE = 4;

    /**
     * The size in blocks of a noise piece in the y direction.
     */
    public static final int PIECE_Y_SIZE = 8;

    /**
     * The size in blocks of a noise piece in the z direction.
     */
    public static final int PIECE_Z_SIZE = 4;

    /**
     * The amount of noise pieces that fit inside a chunk on the x axis.
     */
    public static final int PIECES_PER_CHUNK_X = ChunkCoordinate.CHUNK_X_SIZE / PIECE_X_SIZE;

    /**
     * The amount of noise pieces that fit inside a chunk on the y axis.
     */
    public static final int PIECES_PER_CHUNK_Y = ChunkCoordinate.CHUNK_Y_SIZE / PIECE_Y_SIZE;

    /**
     * The amount of noise pieces that fit inside a chunk on the z axis.
     */
    public static final int PIECES_PER_CHUNK_Z = ChunkCoordinate.CHUNK_Z_SIZE / PIECE_Z_SIZE;

}
