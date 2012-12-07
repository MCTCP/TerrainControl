package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidResourceException;

import java.util.List;
import java.util.Random;

public class AboveWaterGen extends Resource
{
    private int blockId;
    private int blockData;

    @Override
    public void load(List<String> args) throws InvalidResourceException
    {
        if (args.size() < 3)
        {
            throw new InvalidResourceException("Too few arguments supplied");
        }

        blockId = getBlockId(args.get(0));
        blockData = getBlockData(args.get(0));
        frequency = getInt(args.get(1), 1, 100);
        rarity = getInt(args.get(2), 1, 100);
    }

    @Override
    public void spawn(LocalWorld world, Random rand, int x, int z)
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
    public ResourceType getType()
    {
        return ResourceType.biomeConfigResource;
    }

    @Override
    public String makeString()
    {
        return "AboveWaterGen(" + makeMaterial(blockId) + "," + frequency + "," + rarity + ")";
    }

}
