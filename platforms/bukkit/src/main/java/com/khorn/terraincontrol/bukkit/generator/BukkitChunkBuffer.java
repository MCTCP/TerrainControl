package com.khorn.terraincontrol.bukkit.generator;

import static com.khorn.terraincontrol.util.ChunkCoordinate.CHUNK_Y_SIZE;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.bukkit.BukkitMaterialData;
import com.khorn.terraincontrol.generator.ChunkBuffer;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import net.minecraft.server.v1_8_R2.Blocks;

class BukkitChunkBuffer implements ChunkBuffer
{
    private static final int CHUNK_SECTION_SIZE = 16;
    private static final int CHUNK_SECTION_VOLUME = CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE;

    private final ChunkCoordinate chunkCoord;
    private final short[][] sections = new short[CHUNK_Y_SIZE / CHUNK_SECTION_SIZE][];

    /**
     * Creates a new {@code BukkitChunkBuffer}.
     * 
     * @param chunkCoord
     *            The coordinate of the chunk that will be generated.
     */
    BukkitChunkBuffer(ChunkCoordinate chunkCoord)
    {
        this.chunkCoord = chunkCoord;
    }

    /**
     * Gets access to the raw data.
     * 
     * @return The byte array.
     */
    short[][] accessRawValues()
    {
        return sections;
    }

    @Override
    public LocalMaterialData getBlock(int blockX, int blockY, int blockZ)
    {
        int sectionId = getSectionId(blockY);
        short[] section = sections[sectionId];
        if (section == null)
        {
            return BukkitMaterialData.ofMinecraftBlock(Blocks.AIR);
        }

        short blockId = section[getPositionInSectionArray(blockX, blockY, blockZ)];
        return BukkitMaterialData.ofIds(blockId, 0);
    }

    @Override
    public ChunkCoordinate getChunkCoordinate()
    {
        return chunkCoord;
    }

    /**
     * Gets the position in the section array that represents the block with
     * the given coords.
     * @param blockX X of the block.
     * @param blockY Y of the block.
     * @param blockZ Z of the block.
     * @return The position in the section array.
     */
    private int getPositionInSectionArray(int blockX, int blockY, int blockZ)
    {
        return (blockY & 0xF) << 8 | blockZ << 4 | blockX;
    }

    /**
     * Gets the id of the section the block with the given y would be in.
     * @param blockY The block y.
     * @return The section id.
     */
    private int getSectionId(int blockY)
    {
        return blockY >> 4;
    }

    @Override
    public void setBlock(int blockX, int blockY, int blockZ, LocalMaterialData material)
    {
        int sectionId = getSectionId(blockY);
        short[] section = sections[sectionId];
        if (section == null)
        {
            section = new short[CHUNK_SECTION_VOLUME];
            sections[sectionId] = section;
        }
        section[getPositionInSectionArray(blockX, blockY, blockZ)] = (short) material.getBlockId();
    }

}
