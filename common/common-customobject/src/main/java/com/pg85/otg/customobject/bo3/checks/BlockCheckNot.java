package com.pg85.otg.customobject.bo3.checks;

import com.pg85.otg.interfaces.IWorldGenRegion;

public final class BlockCheckNot extends BlockCheck
{
	@Override
	public boolean preventsSpawn(IWorldGenRegion worldGenRegion, int x, int y, int z)
	{
		// We want the exact opposite as BlockCheck
		return !super.preventsSpawn(worldGenRegion, x, y, z);
	}

	@Override
	public String makeString()
	{
		return makeString("BlockCheckNot");
	}

	@Override
	public BO3Check rotate()
	{
		BlockCheckNot rotatedCheck = new BlockCheckNot();
		rotatedCheck.x = z;
		rotatedCheck.y = y;
		rotatedCheck.z = -x;
		rotatedCheck.toCheck = toCheck.rotate();
		return rotatedCheck;
	}
}
