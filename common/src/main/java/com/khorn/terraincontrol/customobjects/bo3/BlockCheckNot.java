package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.BlockHelper;

import java.util.ArrayList;

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
        rotatedCheck.blockIds = blockIds;

        rotatedCheck.blockDatas = new ArrayList<Byte>();
        for (int i = 0; i < blockDatas.size(); i++)
        {
            // Only try to rotate if some block data has been set.
            if (blockDatas.get(i) == -1)
            {
                rotatedCheck.blockDatas.add((byte) -1);
            } else
            {
                rotatedCheck.blockDatas.add((byte) BlockHelper.rotateData(blockIds.get(i), blockDatas.get(i)));
            }
        }

        return rotatedCheck;
    }

}
