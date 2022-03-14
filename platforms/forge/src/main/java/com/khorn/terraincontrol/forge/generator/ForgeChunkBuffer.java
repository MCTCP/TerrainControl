package com.khorn.terraincontrol.forge.generator;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.forge.ForgeMaterialData;
import com.khorn.terraincontrol.generator.ChunkBuffer;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

/**
 * Implementation of {@link ChunkBuffer}. This implementation supports block
 * data, as well as extended ids. It uses a {@code Block[]} array to store
 * blocks internally, just like Minecraft does for chunk generation.
 *
 */
class ForgeChunkBuffer implements ChunkBuffer
{

    private final ChunkCoordinate chunkCoord;
    private final ChunkPrimer chunkPrimer;

    ForgeChunkBuffer(ChunkCoordinate chunkCoord)
    {
        this.chunkCoord = chunkCoord;
        this.chunkPrimer = new ChunkPrimer();
    }

    @Override
    public ChunkCoordinate getChunkCoordinate()
    {
        return this.chunkCoord;
    }

    @Override
    public void setBlock(int blockX, int blockY, int blockZ, LocalMaterialData material)
    {
        this.chunkPrimer.setBlockState(blockX, blockY, blockZ, ((ForgeMaterialData) material).internalBlock());
    }

    @Override
    public LocalMaterialData getBlock(int blockX, int blockY, int blockZ)
    {
        IBlockState blockState = this.chunkPrimer.getBlockState(blockX, blockY, blockZ);
        return ForgeMaterialData.ofMinecraftBlockState(blockState);
    }

    /**
     * Creates a Minecraft chunk of the data of this chunk buffer.
     *
     * @param world
     *            The world the chunk will be in.
     * @return The chunk.
     */
    Chunk toChunk(World world)
    {
        return new Chunk(world, this.chunkPrimer, this.chunkCoord.getChunkX(), this.chunkCoord.getChunkZ());
    }

}
