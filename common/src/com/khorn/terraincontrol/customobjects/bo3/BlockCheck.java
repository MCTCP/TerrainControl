package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.BlockHelper;

import java.util.ArrayList;
import java.util.List;

public class BlockCheck extends BO3Check
{
    public List<Integer> blockIds;
    public List<Byte> blockDatas;

    @Override
    public boolean preventsSpawn(LocalWorld world, int x, int y, int z)
    {
        int blockId = world.getTypeId(x, y, z);
        int indexOf = blockIds.indexOf(blockId);
        if (indexOf == -1)
        {
            // Not a correct block at the location
            return true;
        }
        if (blockDatas.get(indexOf) != -1 && world.getTypeData(x, y, z) != blockDatas.get(indexOf))
        {
            // Incorrect block data
            return true;
        }

        return false;
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(4, args);
        x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -100, 100);
        z = readInt(args.get(2), -100, 100);
        blockIds = new ArrayList<Integer>();
        blockDatas = new ArrayList<Byte>();
        for (int i = 3; i < args.size(); i++)
        {
            String blockIdAndData = args.get(i);
            if (blockIdAndData.contains("."))
            {
                // It's a block id and block data
                blockIds.add(readBlockId(blockIdAndData));
                blockDatas.add((byte) readBlockData(blockIdAndData));
            } else
            {
                // It's only a block id
                blockIds.add(readBlockId(blockIdAndData));
                blockDatas.add((byte) -1);
            }
        }
    }

    @Override
    public String makeString()
    {
        StringBuilder builder = new StringBuilder("BlockCheck(");
        builder.append(x);
        builder.append(',');
        builder.append(y);
        builder.append(',');
        builder.append(z);
        for (int i = 0; i < blockIds.size(); i++)
        {
            builder.append(',');
            builder.append(makeMaterial(blockIds.get(i)));
            if (blockDatas.get(i) != -1)
            {
                builder.append('.');
                builder.append(blockDatas.get(i));
            }
        }
        builder.append(')');
        return builder.toString();
    }

    @Override
    public BO3Check rotate()
    {
        BlockCheck rotatedCheck = new BlockCheck();
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
