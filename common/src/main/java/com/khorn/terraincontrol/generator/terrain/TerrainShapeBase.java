package com.khorn.terraincontrol.generator.terrain;

import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import java.io.Closeable;

/**
 * The terrain shape interface. It defines the general shape of the terrain
 * (excluding ravines, trees, etc.). How does it work? Each block is assigned a
 * noise value. If this value is larger than 0, ground is generated, otherwise
 * air.
 *
 * <p>For every cuboid of {@value #PIECE_X_SIZE} * {@value #PIECE_Y_SIZE} *
 * {@value #PIECE_Z_SIZE} blocks, only a single noise value is calculated using
 * 3D Perlin noise. Historically, this cuboid is called a "noise piece". </p>
 *
 * <p>For all other blocks in the cuboid, the noise value is calculated using
 * linear interpolation of the Perlin values. Calculating the noise value only
 * once for a whole cuboid was done to improve both performance and playability.
 * (<a href="http://notch.tumblr.com/post/3746989361/terrain-generation-part-1">
 * See the blog of Notch</a>.)</p>
 *
 * <p>This class allows you to calculate the single noise values of the noise
 * pieces. The pieces use a scaled-down coordinate system (just like chunks do,
 * but using different scaling factors):</p>
 *
 * <pre>
 * int blockX = pieceX * {@link #PIECE_X_SIZE};
 * int blockY = pieceY * {@link #PIECE_Y_SIZE};
 * int blockZ = pieceZ * {@link #PIECE_Z_SIZE};
 * </pre>;
 */
public abstract class TerrainShapeBase implements Closeable
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

    /**
     * Pre-calculates the noise values for all noise pieces in the chunk.
     *
     * @param biomeGenerator The biome generator. (The noise depends on the
     *                       biomes.)
     * @param chunkCoord     The chunk.
     */
    public abstract void open(BiomeGenerator biomeGenerator, ChunkCoordinate chunkCoord);

    /**
     * Pre-calculates the noise values for all noise pieces within the area.
     *
     * @param biomeGenerator The biome generator. (The noise depends on the
     *                       biomes.)
     * @param xStart         The x coord of the noise piece with the lowest x.
     * @param yStart         The y coord of the noise piece with the lowest y.
     * @param zStart         The z coord of the noise piece with the lowest z.
     * @param xSize          The amount of noise pieces on the x-axis.
     * @param ySize          The amount of noise pieces on the y-axis.
     * @param zSize          The amount of noise pieces on the z-axis.
     */
    public abstract void open(BiomeGenerator biomeGenerator, int xStart, int yStart, int zStart, int xSize, int ySize, int zSize);

    /**
     * Closes the noise gen after {@link #open(BiomeGenerator, ChunkCoordinate)
     * opening} it.
     */
    @Override
    public abstract void close();

    /**
     * Gets the water level for the given column of noise pieces.
     *
     * @param noisePieceX The x coord of the noise piece, relative to the start
     *                    position you provided when you called {@link
     *                    #open(BiomeGenerator, int, int, int, int, int, int)}.
     * @param noisePieceZ The y coord of the noise piece, relative to the start
     *                    position you provided when you called {@link
     *                    #open(BiomeGenerator, int, int, int, int, int, int)}.
     * @return The water level in block coords. Every air block below this level
     * is replaced by water. So if you return 32, air blocks at y=31 and below
     * will be filled with water.
     */
    public abstract int getWaterLevel(int noisePieceX, int noisePieceZ);

    /**
     * Gets the noise value for the given noise piece.
     *
     * @param noisePieceX The x coord of the noise piece, relative to the start
     *                    position you provided when you called {@link
     *                    #open(BiomeGenerator, int, int, int, int, int, int)}.
     * @param noisePieceY The y coord of the noise piece, relative to the start
     *                    position you provided when you called {@link
     *                    #open(BiomeGenerator, int, int, int, int, int, int)}.
     * @param noisePieceZ The z coord of the noise piece, relative to the start
     *                    position you provided when you called {@link
     *                    #open(BiomeGenerator, int, int, int, int, int, int)}.
     * @return The noise value. noise > 0 means ground, noise <= 0 means air or
     * water.
     * @see #getWaterLevel(int, int) Whether air or water is generated when the
     * returned noise <= 0.
     */
    public abstract double getNoise(int noisePieceX, int noisePieceY, int noisePieceZ);
}
