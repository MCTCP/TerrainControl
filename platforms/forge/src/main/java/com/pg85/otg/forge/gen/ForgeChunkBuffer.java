package com.pg85.otg.forge.gen;

import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkPrimer;

public class ForgeChunkBuffer extends ChunkBuffer
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
