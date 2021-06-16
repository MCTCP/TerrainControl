package com.pg85.otg.util.gen;

import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;

public abstract class LocalWorldGenRegion implements IWorldGenRegion
{
	protected final String presetFolderName;
	private final IWorldConfig worldConfig;
	
	protected LocalWorldGenRegion(String presetFolderName, IWorldConfig worldConfig)
	{
		this.presetFolderName = presetFolderName;
		this.worldConfig = worldConfig;
	}
	
	public IWorldConfig getWorldConfig()
	{
		return this.worldConfig;
	}
	
	public String getPresetFolderName()
	{
		return this.presetFolderName;
	}
	
	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag nbt, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks)
	{
		setBlock(x, y, z, material, nbt, chunkBeingPopulated, null, replaceBlocks, true);
	}
	
	@Override
	public void setBlock(int x, int y, int z, LocalMaterialData material, NamedBinaryTag nbt, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks, boolean useResourceBounds)
	{
		setBlock(x, y, z, material, nbt, chunkBeingPopulated, null, replaceBlocks, useResourceBounds);
	}

}
