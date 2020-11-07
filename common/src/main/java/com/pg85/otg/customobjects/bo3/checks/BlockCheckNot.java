package com.pg85.otg.customobjects.bo3.checks;

import com.pg85.otg.common.LocalWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;

public final class BlockCheckNot extends BlockCheck
{
    @Override
    public boolean preventsSpawn(LocalWorldGenRegion worldGenRegion, int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        // We want the exact opposite as BlockCheck
        return !super.preventsSpawn(worldGenRegion, x, y, z, chunkBeingPopulated);
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
