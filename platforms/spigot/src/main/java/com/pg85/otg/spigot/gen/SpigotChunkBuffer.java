package com.pg85.otg.spigot.gen;

import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.materials.LocalMaterialData;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ProtoChunk;
import org.bukkit.generator.ChunkGenerator;

public class SpigotChunkBuffer extends ChunkBuffer
{
	private ChunkCoordinate chunkCoord = null;
	private ChunkGenerator.ChunkData chunkData = null;
	private ProtoChunk chunk = null;

	public SpigotChunkBuffer (ProtoChunk chunk)
	{
		this.chunk = chunk;
	}

	public SpigotChunkBuffer (ChunkGenerator.ChunkData chunkData, ChunkCoordinate chunkCoord)
	{
		this.chunkCoord = chunkCoord;
		this.chunkData = chunkData;
	}

	@Override
	public ChunkCoordinate getChunkCoordinate ()
	{
		if (chunkData != null)
		{
			return chunkCoord;
		}
		ChunkCoordIntPair pos = this.chunk.getPos();
		return ChunkCoordinate.fromChunkCoords(pos.x, pos.z);
	}

	@Override
	public void setBlock (int blockX, int blockY, int blockZ, LocalMaterialData material)
	{
		if (chunkData != null)
		{
			this.chunkData.setBlock(blockX, blockY, blockZ, ((SpigotMaterialData) material).toSpigotBlockData());
		}
		else
		{
			this.chunk.setType(new BlockPosition(blockX, blockY, blockZ), ((SpigotMaterialData) material).internalBlock(), false);
		}
	}

	@Override
	public LocalMaterialData getBlock (int blockX, int blockY, int blockZ)
	{
		if (chunkData != null)
		{
			return SpigotMaterialData.ofSpigotMaterial(this.chunkData.getType(blockX, blockY, blockZ));
		}
		return SpigotMaterialData.ofBlockData(this.chunk.getType(new BlockPosition(blockX, blockY, blockZ)));
	}
}
