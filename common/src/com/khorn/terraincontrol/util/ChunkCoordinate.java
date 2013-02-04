package com.khorn.terraincontrol.util;

/**
 * Position of a chunk, used as a key in HashMaps/HashSets.
 *
 */
public class ChunkCoordinate
{
    private final int chunkX;
    private final int chunkZ;

    protected ChunkCoordinate(int chunkX, int chunkZ)
    {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public int getChunkX()
    {
        return chunkX;
    }

    public int getChunkZ()
    {
        return chunkZ;
    }

    @Override
    public int hashCode()
    {
        return (Integer.valueOf(chunkX).hashCode() >> 13) ^ Integer.valueOf(chunkZ).hashCode();
    }

    @Override
    public boolean equals(Object otherObject)
    {
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

    public static ChunkCoordinate fromBlockCoords(int blockX, int blockZ)
    {
        // Because of the way Minecraft population works, objects should never
        // be placed in the bottom left corner of a chunk. That's why this
        // formula looks a bit overly complicated.
        return new ChunkCoordinate((int) Math.floor((blockX - 8) / 16.0), (int) Math.floor((blockZ - 8) / 16.0));
    }

    public static ChunkCoordinate fromChunkCoords(int chunkX, int chunkZ)
    {
        return new ChunkCoordinate(chunkX, chunkZ);
    }
}
