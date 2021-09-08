package com.pg85.otg.paper.gen;

import com.pg85.otg.paper.materials.PaperMaterialData;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.materials.LocalMaterialData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import net.minecraft.world.level.chunk.ProtoChunk;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;

public class PaperChunkBuffer extends ChunkBuffer
{
	private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
	private ChunkCoordinate chunkCoord = null;
	private ChunkGenerator.ChunkData chunkData = null;
	private ChunkAccess chunk = null;

	public PaperChunkBuffer(ChunkAccess chunk)
	{
		this.chunk = chunk;
	}

	public PaperChunkBuffer(ChunkGenerator.ChunkData chunkData, ChunkCoordinate chunkCoord)
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
		ChunkPos pos = this.chunk.getPos();
		return ChunkCoordinate.fromChunkCoords(pos.x, pos.z);
	}

	@Override
	public void setBlock (int blockX, int blockY, int blockZ, LocalMaterialData material)
	{
		if (chunkData != null)
		{
			this.chunkData.setBlock(blockX, blockY, blockZ, ((PaperMaterialData) material).toSpigotBlockData());
		} else {
			// Forge: setPos()
			// Spigot: d()
			this.chunk.setBlockState(this.mutable.set(blockX, blockY, blockZ), ((PaperMaterialData) material).internalBlock(), false);
		}
	}

	@Override
	public LocalMaterialData getBlock (int blockX, int blockY, int blockZ)
	{
		if (chunkData != null)
		{
			Material material = this.chunkData.getType(blockX, blockY, blockZ);
			return PaperMaterialData.ofSpigotMaterial(material);
		}
		BlockState blockData = this.chunk.getBlockState(this.mutable.set(blockX, blockY, blockZ));
		return PaperMaterialData.ofBlockData(blockData);
	}

	public ChunkAccess getChunk()
	{
		return this.chunk;
	}
}
