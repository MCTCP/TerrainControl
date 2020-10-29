package com.pg85.otg.forge.generator;

import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.util.ChunkCoordinate;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkPrimer;

public class ForgeChunkBuffer implements ChunkBuffer
{
	private final ChunkPrimer chunk;

	public ForgeChunkBuffer(ChunkPrimer chunk)
	{
		this.chunk = chunk;
	}
	@Override
	public ChunkCoordinate getChunkCoordinate()
	{
		ChunkPos pos = this.chunk.getPos();
		return ChunkCoordinate.fromChunkCoords(pos.x, pos.z);
	}

	@Override
	public void setBlock(int blockX, int blockY, int blockZ, LocalMaterialData material)
	{
		this.chunk.setBlockState(new BlockPos(blockX, blockY, blockZ), ((ForgeMaterialData) material).internalBlock(), false);
	}

	@Override
	public LocalMaterialData getBlock(int blockX, int blockY, int blockZ)
	{
		return ForgeMaterialData.ofMinecraftBlockState(this.chunk.getBlockState(new BlockPos(blockX, blockY, blockZ)));
	}
}
