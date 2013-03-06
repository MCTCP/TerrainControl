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
        int firstSolidBlock = world.getSolidHeight(x, z) - 1;
        if (world.getLiquidHeight(x, z) < firstSolidBlock || firstSolidBlock == -1)
        {
            return;
        }

        int currentSize = rand.nextInt(size);
        int two = 2;
        for (int currentX = x - currentSize; currentX <= x + currentSize; currentX++)
        {
            for (int currentZ = z - currentSize; currentZ <= z + currentSize; currentZ++)
            {
                int deltaX = currentX - x;
                int deltaZ = currentZ - z;
                if (deltaX * deltaX + deltaZ * deltaZ <= currentSize * currentSize)
                {
                    for (int y = firstSolidBlock - two; y <= firstSolidBlock + two; y++)
                    {
                        int i3 = world.getTypeId(currentX, y, currentZ);
                        if (sourceBlocks.contains(i3))
                        {
                            world.setBlock(currentX, y, currentZ, blockId, blockData, false, false, false);
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
        rarity = readRarity(args.get(3));
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
