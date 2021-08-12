package com.pg85.otg.customobject.bo3.checks;

import com.pg85.otg.interfaces.IWorldGenRegion;

public final class BlockCheckAbsoluteNot extends BlockCheckAbsolute
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
		return makeString("BlockCheckAbsoluteNot");
	}

	@Override
	public BO3Check rotate()
	{
		BlockCheckAbsoluteNot rotatedCheck = new BlockCheckAbsoluteNot();
		rotatedCheck.x = z;
		rotatedCheck.y = y;
		rotatedCheck.z = -x;
		rotatedCheck.toCheck = toCheck.rotate();
		return rotatedCheck;
	}
}
