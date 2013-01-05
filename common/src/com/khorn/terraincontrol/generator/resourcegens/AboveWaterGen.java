package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.List;
import java.util.Random;

public class AboveWaterGen extends Resource
{
    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(3, args);

        blockId = readBlockId(args.get(0));
        blockData = readBlockData(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readInt(args.get(2), 1, 100);
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = world.getLiquidHeight(x, z);
        if (y == -1)
            return;
        y++;

        for (int i = 0; i < 10; i++)
        {
            int j = x + rand.nextInt(8) - rand.nextInt(8);
            int m = z + rand.nextInt(8) - rand.nextInt(8);
            if (!world.isEmpty(j, y, m) || !world.getMaterial(j, y - 1, m).isLiquid())
                continue;
            world.setBlock(j, y, m, blockId, blockData, false, false, false);
        }
    }

    @Override
    public String makeString()
    {
        return "AboveWaterRes(" + makeMaterial(blockId) + "," + frequency + "," + rarity + ")";
    }

}
