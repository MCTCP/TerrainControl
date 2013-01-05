package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UnderWaterOreGen extends Resource
{
    private List<Integer> sourceBlocks;
    private int size;

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = world.getSolidHeight(x, z);
        if (world.getLiquidHeight(x, z) < y || y == -1)
            return;

        int currentSize = rand.nextInt(size);
        int two = 2;
        for (int k = x - currentSize; k <= x + currentSize; k++)
        {
            for (int m = z - currentSize; m <= z + currentSize; m++)
            {
                int n = k - x;
                int i1 = m - z;
                if (n * n + i1 * i1 <= currentSize * currentSize)
                {
                    for (int i2 = y - two; i2 <= y + two; i2++)
                    {
                        int i3 = world.getTypeId(k, i2, m);
                        if (sourceBlocks.contains(i3))
                        {
                            world.setBlock(k, i2, m, blockId, blockData, false, false, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(5, args);
        blockId = readBlockId(args.get(0));
        blockData = readBlockData(args.get(0));
        size = readInt(args.get(1), 1, 8);
        frequency = readInt(args.get(2), 1, 100);
        rarity = readInt(args.get(3), 1, 100);
        sourceBlocks = new ArrayList<Integer>();
        for (int i = 4; i < args.size(); i++)
        {
            sourceBlocks.add(readBlockId(args.get(i)));
        }
    }

    @Override
    public String makeString()
    {
        return "UnderWaterOre(" + makeMaterial(blockId, blockData) + "," + size + "," + frequency + "," + rarity + makeMaterial(sourceBlocks) + ")";
    }
}
