package com.pg85.otg.spigot.gen;

import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.materials.LocalMaterialData;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ProtoChunk;

public class SpigotChunkBuffer extends ChunkBuffer
{
	private final ProtoChunk chunk;

	public SpigotChunkBuffer (ProtoChunk chunk)
	{
		this.chunk = chunk;
	}

	@Override
	public ChunkCoordinate getChunkCoordinate ()
	{
		ChunkCoordIntPair pos = this.chunk.getPos();
		return ChunkCoordinate.fromChunkCoords(pos.x, pos.z);
	}

	@Override
	public void setBlock (int blockX, int blockY, int blockZ, LocalMaterialData material)
	{
		this.chunk.setType(new BlockPosition(blockX, blockY, blockZ), ((SpigotMaterialData) material).internalBlock(), false);
	}

	@Override
	public LocalMaterialData getBlock (int blockX, int blockY, int blockZ)
	{
		return SpigotMaterialData.ofBlockData(this.chunk.getType(new BlockPosition(blockX, blockY, blockZ)));
	}
}
