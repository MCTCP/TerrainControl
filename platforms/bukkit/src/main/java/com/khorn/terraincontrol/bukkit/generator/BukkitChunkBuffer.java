package com.khorn.terraincontrol.bukkit.generator;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.bukkit.BukkitMaterialData;
import com.khorn.terraincontrol.generator.ChunkBuffer;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import org.bukkit.generator.ChunkGenerator.ChunkData;

final class BukkitChunkBuffer implements ChunkBuffer
{

    private final ChunkCoordinate chunkCoord;
    private final ChunkData chunkData;

    /**
     * Creates a new {@code BukkitChunkBuffer}.
     * 
     * @param chunkCoord The coordinate of the chunk that will be generated.
     * @param chunkData Object to place the chunk data in.
     */
    BukkitChunkBuffer(ChunkCoordinate chunkCoord, ChunkData chunkData)
    {
        this.chunkCoord = chunkCoord;
        this.chunkData = chunkData;
    }

    @Override
    public LocalMaterialData getBlock(int blockX, int blockY, int blockZ)
    {
        @SuppressWarnings("deprecation")
        int blockId = chunkData.getTypeId(blockX, blockY, blockZ);
        @SuppressWarnings("deprecation")
        byte blockData = chunkData.getData(blockX, blockY, blockZ);
        return BukkitMaterialData.ofIds(blockId, blockData);
    }

    @Override
    public ChunkCoordinate getChunkCoordinate()
    {
        return chunkCoord;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setBlock(int blockX, int blockY, int blockZ, LocalMaterialData material)
    {
        chunkData.setBlock(blockX, blockY, blockZ, material.getBlockId(), material.getBlockData());
    }

}
