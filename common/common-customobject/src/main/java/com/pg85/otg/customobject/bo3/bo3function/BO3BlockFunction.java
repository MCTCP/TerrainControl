package com.pg85.otg.customobject.bo3.bo3function;

import java.util.Random;

import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.bofunctions.BlockFunction;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

/**
 * Represents a block in a BO3.
 */
public class BO3BlockFunction extends BlockFunction<BO3Config>
{
	public BO3BlockFunction() { }
	
	public BO3BlockFunction(BO3Config holder)
	{
		this.holder = holder;
	}
	
	public BO3BlockFunction rotate()
	{
		BO3BlockFunction rotatedBlock = new BO3BlockFunction();
		rotatedBlock.x = z;
		rotatedBlock.y = y;
		rotatedBlock.z = -x;
		rotatedBlock.material = material.rotate();
		rotatedBlock.nbt = nbt;
		rotatedBlock.nbtName = nbtName;

		return rotatedBlock;
	}

	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z, ChunkCoordinate chunkBeingPopulated, boolean replaceBlock)
	{
		worldGenRegion.setBlock(x, y, z, material, nbt, chunkBeingPopulated, replaceBlock);
	}
	
	@Override
	public Class<BO3Config> getHolderType()
	{
		return BO3Config.class;
	}
}