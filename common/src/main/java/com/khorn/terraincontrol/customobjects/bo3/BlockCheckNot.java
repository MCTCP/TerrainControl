package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;

public class BlockCheckNot extends BlockCheck
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

}
