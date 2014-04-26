package com.khorn.terraincontrol.util;

import com.khorn.terraincontrol.util.helpers.MathHelper;

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
    public static final int CHUNK_X_SIZE = 16;
    public static final int CHUNK_Y_SIZE = 256;
    public static final int CHUNK_Z_SIZE = 16;
    private static final int CHUNK_HALF_X_SIZE = CHUNK_X_SIZE / 2;
    private static final int CHUNK_HALF_Z_SIZE = CHUNK_Z_SIZE / 2;

    private final int chunkX;
    private final int chunkZ;

    protected ChunkCoordinate(int chunkX, int chunkZ)
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
     * @param blockX X coordinate of the block.
     * @param blockZ Z coordinate of the block.
     * @return The coordinates of the chunk.
     */
    public static ChunkCoordinate getPopulatingChunk(int blockX, int blockZ)
    {
        // Because of the way Minecraft population works, objects should never
        // be placed in the bottom left corner of a chunk. That's why this
        // formula looks a bit overly complicated.
        return new ChunkCoordinate(
                MathHelper.floor((blockX - CHUNK_HALF_X_SIZE) / (double) CHUNK_X_SIZE),
                MathHelper.floor((blockZ - CHUNK_HALF_Z_SIZE) / (double) CHUNK_Z_SIZE));
    }

    /**
     * Gets the coordinates of the chunk that will contain the given block.
     * @param blockX The x position of the block.
     * @param blockZ The z position of the block.
     * @return The coordinates.
     */
    public static ChunkCoordinate fromBlockCoords(int blockX, int blockZ)
    {
        return new ChunkCoordinate(
                MathHelper.floor(blockX / (double) CHUNK_X_SIZE), 
                MathHelper.floor(blockZ / (double) CHUNK_Z_SIZE));
    }

    public static ChunkCoordinate fromChunkCoords(int chunkX, int chunkZ)
    {
        return new ChunkCoordinate(chunkX, chunkZ);
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
        return chunkX * CHUNK_X_SIZE + CHUNK_HALF_X_SIZE;
    }
    
    /**
     * Gets the z position of the block in the center of this chunk.
     * @return The z position.
     */
    public int getBlockZCenter() {
        return chunkZ * CHUNK_Z_SIZE + CHUNK_HALF_Z_SIZE;
    }

    /**
     * Gets the x position of the block with the lowest x coordinate that is
     * still in this chunk.
     * @return The x position of the block.
     */
    public int getBlockX()
    {
        return chunkX * CHUNK_X_SIZE;
    }

    /**
     * Gets the z position of the block with the lowest z coordinate that is
     * still in this chunk.
     * @return The z position of the block.
     */
    public int getBlockZ()
    {
        return chunkZ * CHUNK_Z_SIZE;
    }
}
