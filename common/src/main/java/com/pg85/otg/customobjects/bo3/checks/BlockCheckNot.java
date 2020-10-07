package com.pg85.otg.customobjects.bo3.checks;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.util.ChunkCoordinate;

public final class BlockCheckNot extends BlockCheck
{
    @Override
    public boolean preventsSpawn(LocalWorld world, int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        // We want the exact opposite as BlockCheck
        return !super.preventsSpawn(world, x, y, z, chunkBeingPopulated);
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
        rotatedCheck.x = z - 1;
        rotatedCheck.y = y;
        rotatedCheck.z = -x;
        rotatedCheck.toCheck = toCheck.rotate();
        return rotatedCheck;
    }
}
