package com.pg85.otg.forge.gen;

import com.pg85.otg.forge.materials.ForgeMaterialData;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;

public class ForgeChunkBuffer extends ChunkBuffer
{
	private final BlockPos.Mutable mutable = new BlockPos.Mutable();
	private final ChunkPrimer chunk;

	ForgeChunkBuffer(ChunkPrimer chunk)
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
	public void setBlock(int internalX, int blockY, int internalZ, LocalMaterialData material)
	{
		this.chunk.setBlockState(this.mutable.set(internalX, blockY, internalZ), ((ForgeMaterialData) material).internalBlock(), false);
	}

	@Override
	public LocalMaterialData getBlock(int internalX, int blockY, int internalZ)
	{
		BlockState blockState = this.chunk.getBlockState(this.mutable.set(internalX, blockY, internalZ));
		return blockState == null ? null : ForgeMaterialData.ofBlockState(blockState);
	}
	
	public IChunk getChunk()
	{
		return this.chunk;
	}
}
