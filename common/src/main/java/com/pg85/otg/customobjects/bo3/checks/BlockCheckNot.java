package com.pg85.otg.customobjects.bo3.checks;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.customstructure.CustomObjectCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public final class BlockCheckNot extends BlockCheck
{
    @Override
    public boolean preventsSpawn(LocalWorld world, int x, int y, int z)
    {
        // We want the exact opposite as BlockCheck
        return !super.preventsSpawn(world, x, y, z);
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

    @Override
    public BO3Check rotate(Rotation rotation)
    {
    	BlockCheckNot rotatedBlock = new BlockCheckNot();

        CustomObjectCoordinate rotatedCoords = CustomObjectCoordinate.getRotatedBO3CoordsJustified(x, y, z, rotation);

        rotatedBlock.x = rotatedCoords.getX();
        rotatedBlock.y = rotatedCoords.getY();
        rotatedBlock.z = rotatedCoords.getZ();

        rotatedBlock.toCheck = this.toCheck;

    	for(int i = 0; i < rotation.getRotationId(); i++)
    	{
            rotatedBlock.toCheck = rotatedBlock.toCheck.rotate();
    	}

        return rotatedBlock;
    }
}
